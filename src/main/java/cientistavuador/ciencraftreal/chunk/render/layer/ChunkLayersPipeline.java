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
import cientistavuador.ciencraftreal.chunk.render.layer.shaders.ChunkLayerShaderProgram;
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
        private final boolean requiresUpdate;
        private final float distance;

        public DistancedChunkLayer(ChunkLayer layer, Camera camera, boolean requiresUpdate) {
            this.layer = layer;
            this.camera = camera;
            this.requiresUpdate = requiresUpdate;
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

        public boolean requiresUpdate() {
            return requiresUpdate;
        }
        
    }
    
    public static int render(Camera camera, ChunkLayers[] chunks) {
        long time = System.nanoTime();
        
        if (chunks.length == 0) {
            return 0;
        }
        WorldSky sky = chunks[0].getChunk().getWorld().getSky();

        List<DistancedChunkLayer> layerList = new ArrayList<>(chunks.length * (Chunk.CHUNK_HEIGHT / ChunkLayer.HEIGHT));
        
        for (int i = 0; i < chunks.length; i++) {
            ChunkLayers layers = chunks[i];

            for (int j = 0; j < layers.length(); j++) {
                ChunkLayer layer = layers.layerAt(j);

                boolean requiresUpdate = layer.requiresUpdate(camera);
                if ((!layer.isCulled() && !layer.isEmpty()) || requiresUpdate) {
                    DistancedChunkLayer e = new DistancedChunkLayer(layer, camera, requiresUpdate);
                    final double maxDistance = (WorldCamera.VIEW_DISTANCE + 0.5) * Chunk.CHUNK_SIZE;
                    double xx = e.getCamera().getPosition().x() - e.getLayer().getCenter().x();
                    double zz = e.getCamera().getPosition().z() - e.getLayer().getCenter().z();
                    if (Math.sqrt((xx * xx) + (zz * zz)) <= maxDistance) {
                        layerList.add(e);
                    }
                }
            }
        }

        if (layerList.isEmpty()) {
            return 0;
        }

        int drawCalls = 0;

        layerList.sort((o1, o2) -> {
            if (o1.getDistance() > o2.getDistance()) {
                return 1;
            }
            if (o1.getDistance() < o2.getDistance()) {
                return -1;
            }
            return 0;
        });

        glUseProgram(ChunkLayerShaderProgram.SHADER_PROGRAM);
        ChunkLayerShaderProgram.sendPerFrameUniforms(camera, sky);

        ChunkLayerShaderProgram.sendUseAlphaUniform(false);

        for (DistancedChunkLayer e : layerList) {
            ChunkLayer k = e.getLayer();
            if (e.requiresUpdate() && ((System.nanoTime() - time) / 1E9d) < (1.0 / 90.0)) {
                k.update();
            }
            
            ChunkLayerShaderProgram.sendPerDrawUniforms(k.getChunk().getChunkX(), k.getY(), k.getChunk().getChunkZ());
            if (k.render(false)) {
                drawCalls++;
            }
        }

        ChunkLayerShaderProgram.sendUseAlphaUniform(true);

        for (int i = (layerList.size()-1); i >= 0; i--) {
            ChunkLayer k = layerList.get(i).getLayer();
            ChunkLayerShaderProgram.sendPerDrawUniforms(k.getChunk().getChunkX(), k.getY(), k.getChunk().getChunkZ());
            if (k.render(true)) {
                drawCalls++;
            }
        }

        ChunkLayerShaderProgram.finishRendering();
        glUseProgram(0);

        return drawCalls;
    }

    private ChunkLayersPipeline() {

    }
}
