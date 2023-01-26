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
import cientistavuador.ciencraftreal.block.SimpleBlock;
import cientistavuador.ciencraftreal.block.material.ubo.ColorUBO;
import cientistavuador.ciencraftreal.debug.Triangle;
import cientistavuador.ciencraftreal.camera.FreeCamera;
import cientistavuador.ciencraftreal.chunk.generation.WorldChunkGeneratorFactory;
import cientistavuador.ciencraftreal.debug.DebugBlock;
import cientistavuador.ciencraftreal.util.BlockOutline;
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
    private final WorldCamera world = new WorldCamera(camera, 65487321654L, new WorldChunkGeneratorFactory());
    private final BlockOutline outline = new BlockOutline(world, camera);
    private int currentBlockId = Blocks.HAPPY_2023.getId();
    
    private Game() {

    }

    public void start() {
        camera.setPosition(0, 80, 0);
        
        ColorUBO colors = ColorUBO.DEFAULT;
        colors.allocate();
        int object = colors.allocate();
        colors.setColor(object, 0, 0, 1, 1);
        colors.updateUBO();
    }
    
    public void loop() {
        camera.updateMovement();
        triangle.render(camera.getProjection(), camera.getView());
        block.copySideTextures(Blocks.STONE);

        for (int i = 1; i < BlockRegister.numberOfRegisteredBlocks(); i++) {
            block.getModel().identity().translate(1 + (i * 1), 0, 0);
            if (BlockRegister.getBlock(i) instanceof SimpleBlock e) {
                block.copySideTextures(e);
            }
            block.render(camera.getProjection(), camera.getView());
        }
        
        world.update();
        world.render();
        
        outline.update();
        outline.render();
        
        Main.WINDOW_TITLE += " (Block: "+BlockRegister.getBlock(this.currentBlockId).getName()+")";
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
                    camera.getPosition().y(),
                    (float) (Math.random() * 10000)
            );
        }
        if (key == GLFW_KEY_G && action == GLFW_PRESS) {
            System.out.println(world.getWorldBlock(
                    (int) Math.floor(this.camera.getPosition().x()),
                    (int) Math.floor(this.camera.getPosition().y()),
                    (int) Math.ceil(this.camera.getPosition().z())
            ));
        }
        if (key == GLFW_KEY_E && action == GLFW_PRESS) {
            System.out.println(this.outline.getBlock() + " at " + outline.getCastPos());
        }
        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
            this.currentBlockId++;
            if (this.currentBlockId >= BlockRegister.numberOfRegisteredBlocks()) {
                this.currentBlockId = 1;
            }
        }
    }

    public void mouseCallback(long window, int button, int action, int mods) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS && outline.getBlock() != Blocks.AIR) {
            this.world.setWorldBlock(
                    outline.getCastPos().x(),
                    outline.getCastPos().y(),
                    outline.getCastPos().z(),
                    Blocks.AIR
            );
        }
        if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS && outline.getBlock() != Blocks.AIR) {
            this.world.setWorldBlock(
                    outline.getSidePos().x(),
                    outline.getSidePos().y(),
                    outline.getSidePos().z(),
                    BlockRegister.getBlock(this.currentBlockId)
            );
        }
    }
}
