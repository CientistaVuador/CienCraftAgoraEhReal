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
import cientistavuador.ciencraftreal.world.WorldCamera;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3dc;

/**
 *
 * @author Cien
 */
public class ChunkLayersRender {

    public static final int MAX_LAYERS_PER_FRAME = 4;

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

    public static int render(Camera camera, ChunkLayers[] chunks) {
        List<DistancedChunkLayer> layerList = new ArrayList<>(64);

        for (int i = 0; i < chunks.length; i++) {
            ChunkLayers layers = chunks[i];

            for (int j = 0; j < layers.length(); j++) {
                ChunkLayer layer = layers.layerAt(j);

                if (layer.cullingStage0(camera)) {
                    layerList.add(new DistancedChunkLayer(layer, camera));
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

        ArrayDeque<ChunkLayer> alphaRendering = new ArrayDeque<>(64);
        ArrayDeque<ChunkLayer> toProcess = new ArrayDeque<>(64);

        ChunkLayer.useDefaultProgram();
        ChunkLayer.sendPerFrameUniforms(camera);
        ChunkLayer.sendUseAlphaUniform(false);

        int layersToProcess = 0;
        for (DistancedChunkLayer e : layerList) {
            final double maxDistance = (WorldCamera.VIEW_DISTANCE + 0.5) * Chunk.CHUNK_SIZE;
            double xx = e.getCamera().getPosition().x() - e.getLayer().getCenter().x();
            double zz = e.getCamera().getPosition().z() - e.getLayer().getCenter().z();
            if (Math.sqrt((xx*xx) + (zz*zz)) > maxDistance) {
                continue;
            }
            
            ChunkLayer layer = e.getLayer();
            
            if (!layer.checkVerticesStage1(camera)) {
                if (layer.renderStage4()) {
                    drawCalls++;
                }
                alphaRendering.add(layer);
                continue;
            }
            if (layersToProcess >= MAX_LAYERS_PER_FRAME) {
                continue;
            }
            boolean result = layer.prepareVerticesStage2();
            if (result) {
                toProcess.add(layer);
                layersToProcess++;
            }
        }

        ChunkLayer.sendUseAlphaUniform(true);

        //render alpha
        ChunkLayer e;
        while ((e = alphaRendering.pollLast()) != null) {
            if (e.renderAlphaStage5()) {
                drawCalls++;
            }
        }

        if (toProcess.isEmpty()) {
            ChunkLayer.finishRendering();
            ChunkLayer.discardProgram();
            return drawCalls;
        }

        ChunkLayer.sendUseAlphaUniform(false);

        //process the queue
        ChunkLayer f;
        while ((f = toProcess.poll()) != null) {
            boolean result = f.prepareVaoVboStage3();
            if (result) {
                if (f.renderStage4()) {
                    drawCalls++;
                }
                alphaRendering.add(f);
            }
        }

        ChunkLayer.sendUseAlphaUniform(true);

        //render alpha
        ChunkLayer a;
        while ((a = alphaRendering.pollLast()) != null) {
            if (a.renderAlphaStage5()) {
                drawCalls++;
            }
        }

        ChunkLayer.finishRendering();
        ChunkLayer.discardProgram();
        
        return drawCalls;
    }

    private ChunkLayersRender() {

    }

}
