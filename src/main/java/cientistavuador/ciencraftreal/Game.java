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

import cientistavuador.ciencraftreal.audio.AudioPlayer;
import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.BlockRegister;
import cientistavuador.ciencraftreal.block.BlockSounds;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.block.StateOfMatter;
import cientistavuador.ciencraftreal.camera.FreeCamera;
import cientistavuador.ciencraftreal.camera.OrthoCamera;
import cientistavuador.ciencraftreal.chunk.generation.WorldChunkGeneratorFactory;
import cientistavuador.ciencraftreal.player.Player;
import cientistavuador.ciencraftreal.player.PlayerPhysics;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import cientistavuador.ciencraftreal.ubo.UBOBindingPoints;
import cientistavuador.ciencraftreal.debug.AabRender;
import cientistavuador.ciencraftreal.text.GLFontRenderer;
import cientistavuador.ciencraftreal.text.GLFontSpecification;
import cientistavuador.ciencraftreal.text.GLFontSpecifications;
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

    private final FreeCamera camera = new FreeCamera();
    private final OrthoCamera shadowCamera = new OrthoCamera();
    private final WorldCamera world = new WorldCamera(camera, 65487321654L, new WorldChunkGeneratorFactory());
    private final BlockOutline outline = new BlockOutline(world, camera);
    private final Player player = new Player(world);
    
    private int currentBlockId = Blocks.HAPPY_2023.getId();
    private int drawCalls = 0;
    
    private Game() {

    }

    public void start() {
        camera.setPosition(0, 80, 0);
        camera.setUBO(CameraUBO.create(UBOBindingPoints.PLAYER_CAMERA));
        player.setPosition(0, 100, 0);

        camera.setMovementDisabled(true);
        player.setMovementDisabled(false);
        
        shadowCamera.setDimensions(4f, 4f);
        shadowCamera.setUBO(CameraUBO.create(UBOBindingPoints.SHADOW_CAMERA));
    }
    
    public void loop() {
        drawCalls = 0;
        
        camera.updateMovement();
        
        world.update();
        outline.update();
        
        player.update();
        player.setYaw(camera.getRotation().y());
        if (camera.isMovementDisabled()) {
            camera.setPosition(
                    player.getPosition().x(),
                    player.getPosition().y() + PlayerPhysics.EYES_HEIGHT,
                    player.getPosition().z()
            );
        }
        
        Main.WINDOW_TITLE += " (Block: " + BlockRegister.getBlock(this.currentBlockId).getName() + ")";
        Main.WINDOW_TITLE += " (x:" + (int) Math.floor(camera.getPosition().x()) + ",y:" + (int) Math.floor(camera.getPosition().y()) + ",z:" + (int) Math.ceil(camera.getPosition().z()) + ")";
        
        AudioPlayer.update(camera);
        
        shadowCamera.setFront(world.getSky().getDirectionalDirection());
        shadowCamera.setPosition(
                camera.getPosition().x() + (-shadowCamera.getFront().x() * 128f),
                camera.getPosition().y() + (-shadowCamera.getFront().y() * 128f),
                camera.getPosition().z() + (-shadowCamera.getFront().z() * 128f)
        );
    }
    
    
    public void shadowLoop() {
        drawCalls += world.renderShadow(shadowCamera);
    }
    
    public void renderLoop() {
        drawCalls += world.render(shadowCamera);
        
        outline.render();
        drawCalls++;
        
        drawCalls += AabRender.renderQueue(camera);
        
        String lookingAt = "---";
        Block lookingAtBlock = this.outline.getBlock();
        if (lookingAtBlock != Blocks.AIR) {
            lookingAt = "Looking at " + lookingAtBlock.getName() + " at X: " + outline.getCastPos().x() + ", Y: " + outline.getCastPos().y() + ", Z: " + outline.getCastPos().z();
        }
        drawCalls += GLFontRenderer.render(-1f, 0.90f,
                new GLFontSpecification[]{
                    GLFontSpecifications.OPENSANS_ITALIC_0_10_BANANA_YELLOW,
                    GLFontSpecifications.ROBOTO_THIN_0_05_WHITE
                },
                new String[]{
                    "CienCraft-2.5-DEV\n",
                    new StringBuilder()
                            .append("FPS: ").append(Main.FPS).append('\n')
                            .append("X: ").append(format(camera.getPosition().x())).append(" ")
                            .append("Y: ").append(format(camera.getPosition().y())).append(" ")
                            .append("Z: ").append(format(camera.getPosition().z())).append('\n')
                            .append("Current Block: ").append(BlockRegister.getBlock(this.currentBlockId).getName()).append('\n')
                            .append(lookingAt).append('\n')
                            .append("Controls:\n")
                            .append("\tWASD + Space + Mouse - Move\n")
                            .append("\tShift - Run\n")
                            .append("\tAlt - Wander\n")
                            .append("\tCtrl - Unlock/Lock mouse\n")
                            .append("\tLeft Click - Destroy block.\n")
                            .append("\tRight Click - Place block.\n")
                            .append("\tF - Random teleport.\n")
                            .append("\tR - Change current block.\n")
                            .append("\tV - Freecam\n")
                            .append("\tE - Get block.\n")
                            .append("\tG - Enable/disable shadows.\n")
                            .toString()
                }
        );

        Main.WINDOW_TITLE += " (DrawCalls: " + drawCalls + ")";
    }

    private String format(double d) {
        return String.format("%.2f", d);
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        player.keyCallback(window, key, scancode, action, mods);

        if (key == GLFW_KEY_F && action == GLFW_PRESS) {
            if (player.isMovementDisabled()) {
                camera.setPosition(
                        Math.random() * 100000000,
                        90,
                        Math.random() * 100000000
                );
            } else {
                player.setPosition(
                        Math.random() * 100000000,
                        90,
                        Math.random() * 100000000
                );
                player.setSpeed(0f, 0f, 0f);
            }
        }
        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
            this.currentBlockId++;
            if (this.currentBlockId >= BlockRegister.numberOfRegisteredBlocks()) {
                this.currentBlockId = 1;
            }
        }
        if (key == GLFW_KEY_V && action == GLFW_PRESS) {
            if (player.isMovementDisabled()) {
                camera.setMovementDisabled(true);
                player.setMovementDisabled(false);
                player.setPosition(camera.getPosition());
                player.setSpeed(0, 0, 0);
            } else {
                camera.setMovementDisabled(false);
                player.setMovementDisabled(true);
            }
        }
        if (key == GLFW_KEY_E && action == GLFW_PRESS) {
            if (this.outline.getBlock() != Blocks.AIR) {
                this.currentBlockId = this.outline.getBlock().getId();
            }
        }
        if (key == GLFW_KEY_G && action == GLFW_PRESS) {
            Main.SHADOWS_ENABLED = !Main.SHADOWS_ENABLED;
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
            BlockSounds.play(BlockSounds.BLOB,
                    outline.getCastPos().x() + 0.5,
                    outline.getCastPos().y() + 0.5,
                    outline.getCastPos().z() - 0.5
            );
        }
        if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS && outline.getBlock() != Blocks.AIR) {
            int blockX = outline.getSidePos().x();
            int blockY = outline.getSidePos().y();
            int blockZ = outline.getSidePos().z();
            Block block = BlockRegister.getBlock(this.currentBlockId);

            if (StateOfMatter.SOLID.equals(block.getStateOfMatter()) && block.checkCollision(blockX, blockY, blockZ, this.player)) {
                return;
            }

            this.world.setWorldBlock(
                    blockX,
                    blockY,
                    blockZ,
                    block
            );
            BlockSounds.play(BlockSounds.WOOD_PLACE,
                    blockX + 0.5,
                    blockY + 0.5,
                    blockZ - 0.5
            );
        }
    }
}
