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
import cientistavuador.ciencraftreal.chunk.render.layer.shaders.ChunkLayerShaderProgram;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ChunkLayersRender {
    
    public static final float DESIRED_PROCESSING_TIME_PER_RENDER = 1f/60f;
    
    private static class DistancedChunkLayer {
        private final ChunkLayer layer;
        private final Camera camera;
        private final float distance;
        
        public DistancedChunkLayer(ChunkLayer layer, Camera camera) {
            this.layer = layer;
            this.camera = camera;
            Vector3fc cameraPos = camera.getPosition();
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
    
    public static void render(Camera camera, ChunkLayers[] chunks) {
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
        
        ArrayDeque<ChunkLayer> alphaRendering = new ArrayDeque<>(64);
        List<ChunkLayer> toProcess = new ArrayList<>();
        
        glUseProgram(ChunkLayerShaderProgram.SHADER_PROGRAM);
        
        for (DistancedChunkLayer e:layerList) {
            ChunkLayer layer = e.getLayer();
            
            if (!layer.checkVerticesStage1(camera)) {
                layer.renderStage5(camera, true);
                alphaRendering.add(layer);
                continue;
            }
            toProcess.add(layer);
        }
        
        //render alpha
        ChunkLayer e;
        while ((e = alphaRendering.pollLast()) != null) {
            e.renderAlphaStage6(camera, true);
        }
        
        if (toProcess.isEmpty()) {
            glUseProgram(0);
            return;
        }
        
        ArrayDeque<ChunkLayer> finishedProcessing = new ArrayDeque<>(64);
        
        double time = 0.0;
        ChunkLayer[] buffer = new ChunkLayer[Runtime.getRuntime().availableProcessors()];
        int packLength = toProcess.size() / buffer.length;
        if (toProcess.size() % buffer.length != 0) {
            packLength++;
        }
        for (int i = 0; i < packLength; i++) {
            int packIndex = i*buffer.length;
            int bufferLength = toProcess.size() - packIndex;
            if (bufferLength >= buffer.length) {
                bufferLength = buffer.length;
            }
            
            for (int j = 0; j < bufferLength; j++) {
                buffer[j] = toProcess.get(j + packIndex);
            }
            
            long here = System.nanoTime();
            //stage 2
            for (int j = 0; j < bufferLength; j++) {
                buffer[j].occlusionStage2(camera);
            }
            //stage 3
            for (int j = 0; j < bufferLength; j++) {
                boolean result = buffer[j].prepareVerticesStage3();
                if (!result) {
                    buffer[j] = null;
                }
            }
            //stage 4
            for (int j = 0; j < bufferLength; j++) {
                if (buffer[j] == null) {
                    continue;
                }
                if (buffer[j].prepareVaoVboStage4()) {
                    finishedProcessing.add(buffer[j]);
                }
            }
            time += (System.nanoTime() - here) / 1E9d;
            if (time >= DESIRED_PROCESSING_TIME_PER_RENDER) {
                break;
            }
        }
        
        //process the queue
        ChunkLayer f;
        while ((f = finishedProcessing.poll()) != null) {
            f.renderStage5(camera, true);
            alphaRendering.add(f);
        }
        
        //render alpha
        ChunkLayer a;
        while ((a = alphaRendering.pollLast()) != null) {
            a.renderAlphaStage6(camera, true);
        }
        
        glUseProgram(0);
    }
    
    private ChunkLayersRender() {
        
    }
    
}
