/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.ciencraftreal.chunk.render.layer;

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.shaders.ChunkLayerProgram;
import cientistavuador.ciencraftreal.world.WorldCamera;
import cientistavuador.ciencraftreal.world.WorldSky;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3dc;
import static org.lwjgl.opengl.GL20C.glUseProgram;

/**
 *
 * @author Cien
 */
public class ChunkLayersPipeline {

    private static class DistancedChunkLayer {

        private final ChunkLayer layer;
        private final Camera camera;
        private final float distance;

        public DistancedChunkLayer(ChunkLayer layer, Camera camera) {
            this.layer = layer;
            this.camera = camera;
            Vector3dc cameraPos = camera.getPosition();
            this.distance = (float) layer.getCenter().distance(
                    cameraPos.x(),
                    cameraPos.y(),
                    cameraPos.z()
            );
        }

        public ChunkLayer getLayer() {
            return layer;
        }

        public Camera getCamera() {
            return camera;
        }

        public float getDistance() {
            return distance;
        }

    }

    public static void render(Camera camera, ChunkLayers[] chunks, ShadowProfile shadowProfile) {
        long time = System.nanoTime();

        if (chunks.length == 0) {
            return;
        }
        WorldSky sky = chunks[0].getChunk().getWorld().getSky();

        List<DistancedChunkLayer> layerList = new ArrayList<>(chunks.length * (Chunk.CHUNK_HEIGHT / ChunkLayer.HEIGHT));

        final double maxDistance = (WorldCamera.VIEW_DISTANCE + 0.5) * Chunk.CHUNK_SIZE;
        for (int i = 0; i < chunks.length; i++) {
            ChunkLayers layers = chunks[i];

            double xx = camera.getPosition().x() - ((layers.getChunk().getChunkX() + 0.5) * Chunk.CHUNK_SIZE);
            double zz = camera.getPosition().z() - ((layers.getChunk().getChunkZ() - 0.5) * Chunk.CHUNK_SIZE);
            if (Math.sqrt((xx * xx) + (zz * zz)) >= maxDistance) {
                continue;
            }
            if (!layers.testAab(camera)) {
                continue;
            }
            for (int j = 0; j < layers.length(); j++) {
                ChunkLayer layer = layers.layerAt(j);
                if (!layer.testEmpty() && layer.testAab(camera)) {
                    layerList.add(new DistancedChunkLayer(layer, camera));
                }
            }
        }

        if (layerList.isEmpty()) {
            return;
        }

        layerList.sort((o1, o2) -> {
            if (o1.getDistance() > o2.getDistance()) {
                return 1;
            }
            if (o1.getDistance() < o2.getDistance()) {
                return -1;
            }
            return 0;
        });

        glUseProgram(ChunkLayerProgram.SHADER_PROGRAM);
        ChunkLayerProgram.sendPerFrameUniforms(camera, sky, shadowProfile);

        ChunkLayerProgram.sendUseAlphaUniform(false);

        for (DistancedChunkLayer e : layerList) {
            ChunkLayer k = e.getLayer();
            k.update(time);
            
            if (k.readyForRendering(false)) {
                ChunkLayerProgram.sendPerDrawUniforms(k.getChunk().getChunkX(), k.getY(), k.getChunk().getChunkZ());
                k.render(false);
            }
        }

        ChunkLayerProgram.sendUseAlphaUniform(true);

        for (int i = (layerList.size() - 1); i >= 0; i--) {
            ChunkLayer k = layerList.get(i).getLayer();

            if (k.readyForRendering(true)) {
                ChunkLayerProgram.sendPerDrawUniforms(k.getChunk().getChunkX(), k.getY(), k.getChunk().getChunkZ());
                k.render(true);
            }
        }

        ChunkLayerProgram.finishRendering();
        glUseProgram(0);
    }

    private ChunkLayersPipeline() {

    }
}
