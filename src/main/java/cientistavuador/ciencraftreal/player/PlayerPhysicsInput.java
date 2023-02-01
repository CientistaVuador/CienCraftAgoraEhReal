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

    private float jumpSpeed = 0;
    private float yaw = 0;

    public PlayerPhysicsInput(WorldCamera world) {
        super(world);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    }
    
    @Override
    public void update() {
        Vector3d walkTranslate = new Vector3d();
        float multiplier = 1;

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_W) == GLFW_PRESS) {
            walkTranslate.add(1, 0, 0);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_S) == GLFW_PRESS) {
            walkTranslate.add(-1, 0, 0);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_A) == GLFW_PRESS) {
            walkTranslate.add(0, 0, -1);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_D) == GLFW_PRESS) {
            walkTranslate.add(0, 0, 1);
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            multiplier = 2;
        }
        
        walkTranslate
                .normalize()
                .rotateY((float) Math.toRadians(-getYaw()))
                .mul(MOVEMENT_SPEED * Main.TPF * multiplier);
        
        if (walkTranslate.isFinite()) {
            addTranslation(walkTranslate);
        }
        
        addTranslation(0, this.jumpSpeed * Main.TPF, 0);
        
        super.update();
        
        if (getGravitySpeed() == 0f) {
            this.jumpSpeed = 0f;
        }
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_SPACE && action == GLFW_PRESS && getGravitySpeed() == 0f) {
            this.jumpSpeed += JUMP_SPEED;
        }
    }

}
