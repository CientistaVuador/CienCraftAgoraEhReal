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
package cientistavuador.ciencraftreal;

import cientistavuador.ciencraftreal.block.BlockRegister;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.debug.Triangle;
import cientistavuador.ciencraftreal.camera.FreeCamera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.debug.DebugBlock;
import cientistavuador.ciencraftreal.world.WorldCamera;
import static org.lwjgl.glfw.GLFW.*;

/**
 *
 * @author Cien
 */
public class Game {

    private static final Game GAME = new Game();

    public static Game get() {
        return GAME;
    }

    private final Triangle triangle = new Triangle();
    private final FreeCamera camera = new FreeCamera();
    private final DebugBlock block = new DebugBlock();
    private final WorldCamera world = new WorldCamera(camera, 65487321654L);

    private Game() {

    }

    public void start() {
        camera.setPosition(0, Chunk.GENERATOR_DESIRED_MAX_HEIGHT, 0);
    }

    public void loop() {
        camera.updateMovement();
        triangle.render(camera.getProjection(), camera.getView());
        block.copySideTextures(Blocks.STONE);

        for (int i = 1; i < BlockRegister.numberOfRegisteredBlocks(); i++) {
            block.getModel().identity().translate(1 + (i * 1), 0, 0);
            block.copySideTextures(BlockRegister.getBlock(i));
            block.render(camera.getProjection(), camera.getView());
        }

        world.update();
        world.render();
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }
    
    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
            camera.setPosition(
                    (float) (Math.random() * 10000),
                    Chunk.GENERATOR_DESIRED_MAX_HEIGHT,
                    (float) (Math.random() * 10000)
            );
        }
    }
}
