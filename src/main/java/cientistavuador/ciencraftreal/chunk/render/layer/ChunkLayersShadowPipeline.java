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

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.camera.OrthoCamera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.shaders.ChunkLayerShadowProgram;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import cientistavuador.ciencraftreal.ubo.UBOBindingPoints;
import cientistavuador.ciencraftreal.world.WorldSky;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.joml.Vector3dc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ChunkLayersShadowPipeline {

    public static final OrthoCamera DRAW_SHADOW_CAMERA;
    public static final OrthoCamera READ_SHADOW_CAMERA;

    public static void init() {

    }

    static {
        DRAW_SHADOW_CAMERA = new OrthoCamera();
        DRAW_SHADOW_CAMERA.setUBO(CameraUBO.create(UBOBindingPoints.SHADOW_CAMERA));
        READ_SHADOW_CAMERA = new OrthoCamera();
    }

    private static int drawcallsPerFrame = 0;
    private static final Deque<DistancedChunkLayer> scheduledLayers = new ArrayDeque<>();

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

    public static void prepare(Camera camera, ChunkLayers[] chunks) {
        READ_SHADOW_CAMERA.setPosition(DRAW_SHADOW_CAMERA.getPosition());
        READ_SHADOW_CAMERA.setFront(DRAW_SHADOW_CAMERA.getFront());

        if (chunks.length == 0) {
            return;
        }
        WorldSky sky = chunks[0].getChunk().getWorld().getSky();

        DRAW_SHADOW_CAMERA.setFront(sky.getDirectionalDirection());
        DRAW_SHADOW_CAMERA.setPosition(
                camera.getPosition().x() + (-DRAW_SHADOW_CAMERA.getFront().x() * Chunk.CHUNK_HEIGHT),
                camera.getPosition().y() + (-DRAW_SHADOW_CAMERA.getFront().y() * Chunk.CHUNK_HEIGHT),
                camera.getPosition().z() + (-DRAW_SHADOW_CAMERA.getFront().z() * Chunk.CHUNK_HEIGHT)
        );

        List<DistancedChunkLayer> layerList = new ArrayList<>(chunks.length * (Chunk.CHUNK_HEIGHT / ChunkLayer.HEIGHT));

        for (int i = 0; i < chunks.length; i++) {
            ChunkLayers layers = chunks[i];

            if (!layers.testAab(DRAW_SHADOW_CAMERA)) {
                continue;
            }
            for (int j = 0; j < layers.length(); j++) {
                ChunkLayer layer = layers.layerAt(j);
                if (layer.readyForRendering(false) && layer.testAab(DRAW_SHADOW_CAMERA)) {
                    layerList.add(new DistancedChunkLayer(layer, DRAW_SHADOW_CAMERA));
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

        scheduledLayers.addAll(layerList);
        drawcallsPerFrame = scheduledLayers.size() / Main.SHADOWS_CURRENT_FRAMERATE_DIVISOR;
        if (drawcallsPerFrame <= 0) {
            drawcallsPerFrame = 1;
        }
    }

    public static void render() {
        if (scheduledLayers.isEmpty()) {
            return;
        }

        boolean firstDrawcall = true;
        int drawCalls = drawcallsPerFrame;
        while (drawCalls > 0 || Main.SHADOWS_CURRENT_FRAME == 0) {
            DistancedChunkLayer e = scheduledLayers.poll();
            if (e == null) {
                break;
            }
            ChunkLayer k = e.getLayer();

            if (k.readyForRendering(false)) {
                if (firstDrawcall) {
                    firstDrawcall = false;
                    glUseProgram(ChunkLayerShadowProgram.SHADER_PROGRAM);
                    ChunkLayerShadowProgram.sendPerFrameUniforms(DRAW_SHADOW_CAMERA, k.getChunk().getWorld().getSky());
                }
                ChunkLayerShadowProgram.sendPerDrawUniforms(k.getChunk().getChunkX(), k.getY(), k.getChunk().getChunkZ());
                k.render(false);
                drawCalls--;
            }
        }

        if (!firstDrawcall) {
            ChunkLayerShadowProgram.finishRendering();
            glUseProgram(0);
        }
    }

    private ChunkLayersShadowPipeline() {

    }
}
