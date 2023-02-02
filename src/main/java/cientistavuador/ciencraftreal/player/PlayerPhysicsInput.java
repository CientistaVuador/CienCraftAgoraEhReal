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
package cientistavuador.ciencraftreal.player;

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.world.WorldCamera;
import org.joml.Vector3d;
import static org.lwjgl.glfw.GLFW.*;

/**
 *
 * @author Cien
 */
public class PlayerPhysicsInput extends PlayerPhysics {

    public static final float JUMP_SPEED = 5f;
    public static final float MOVEMENT_SPEED = 5f;
    public static final float SWIM_UP_SPEED = 1.2f;

    private float yaw = 0;
    private boolean running = false;
    private long lastTimeWPressed = 0;
    private boolean crawling = false;

    public PlayerPhysicsInput(WorldCamera world) {
        super(world);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isCrawling() {
        return crawling;
    }

    @Override
    public void update() {
        Vector3d walkSpeed = new Vector3d();
        float multiplier = 1;

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_W) == GLFW_PRESS) {
            walkSpeed.add(1, 0, 0);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_S) == GLFW_PRESS) {
            walkSpeed.add(-1, 0, 0);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_A) == GLFW_PRESS) {
            walkSpeed.add(0, 0, -1);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_D) == GLFW_PRESS) {
            walkSpeed.add(0, 0, 1);
        }

        if (this.running) {
            multiplier = 2;
        }

        if (this.crawling) {
            multiplier = 0.2f;
        }

        walkSpeed
                .normalize()
                .rotateY((float) Math.toRadians(-getYaw()))
                .mul(MOVEMENT_SPEED * multiplier);
        
        if (walkSpeed.isFinite()) {
            setSpeed(walkSpeed.x(), getSpeed().y(), walkSpeed.z());
        } else {
            setSpeed(0, getSpeed().y(), 0);
        }

        boolean liquidStore = isOnLiquid();
        
        super.update();
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_SPACE && isOnLiquid()) {
            if (action == GLFW_PRESS) {
                
            }
            if (action == GLFW_RELEASE) {
                
            }
        }
        if (key == GLFW_KEY_SPACE && action == GLFW_PRESS && getCollisionBlockY() != Blocks.AIR && !isOnLiquid()) {
            
        }
        if (key == GLFW_KEY_W) {
            if (action == GLFW_PRESS) {
                if ((System.currentTimeMillis() - this.lastTimeWPressed) <= 250) {
                    this.running = true;
                }
                this.lastTimeWPressed = System.currentTimeMillis();
            }
            if (action == GLFW_RELEASE) {
                this.running = false;
            }
        }
        if (key == GLFW_KEY_LEFT_ALT) {
            if (action == GLFW_PRESS) {
                this.running = false;
                this.crawling = true;
            }
            if (action == GLFW_RELEASE) {
                this.crawling = false;
            }
        }
    }

}
