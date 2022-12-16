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
package cientistavuador.ciencraftreal.camera;

import cientistavuador.ciencraftreal.Main;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

/**
 * A Somewhat simple 3D Camera
 * @author Shinoa Hiragi
 */
public class Camera {

    private static float NEAR_PLANE = 0.1f;
    private static float FAR_PLANE = 1000f;
    
    //Axises for the view Matrix
    private static Vector3f AxisX = new Vector3f(1, 0, 0);
    private static Vector3f AxisY = new Vector3f(0, 1, 0);
    private static Vector3f AxisZ = new Vector3f(0, 0, 1);

    //Camera's Field of View
    public float fov;

    public Vector3f position;
    public Vector3f rotation;

    public Matrix4f view;
    public Matrix4f projection;
    
    private final float sensitivity = 0.8f;
    private final float speed = 3;

    //whatever it should capture the cursor or not.
    // press LeftControl in game to capture/release the cursor
    private boolean captureMouse = false;

    public Camera(float fov) {
        this(0, 0, 0, fov);
    }

    public Camera(float x, float y, float z, float fov) {
        this.fov = fov;
        position = new Vector3f(x, y, z);
        rotation = new Vector3f(0, 0, 0);
        projection = new Matrix4f();
        view = new Matrix4f();
    }

    //movimentation magic
    public void update() {
        //makes a new view matrix
        this.view.identity();
        this.view.translate(position);
        this.view.rotate(rotation.x, AxisX);
        this.view.rotate(rotation.y, AxisY);
        this.view.rotate(rotation.z, AxisZ);
        
        if (isControlPressedOnce()) {
            this.captureMouse = !this.captureMouse;
            // if true, disable cursor,
            // if false, set cursor to normal
            glfwSetInputMode(Main.WINDOW_POINTER, GLFW_CURSOR,
                        captureMouse ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
            System.out.println("Capture state: " + captureMouse);
        }
        
        //acceleration in X and Z axis
        float xa = 0;
        float za = 0;
        
        if (isKeyDown(GLFW_KEY_W)) {
            za = speed;
        } else if (isKeyDown(GLFW_KEY_S)) {
            za = -speed;
        }
        
        if (isKeyDown(GLFW_KEY_A)) {
            xa = speed;
        } else if (isKeyDown(GLFW_KEY_D)) {
            xa = -speed;
        }
           
        // if these values are 0 we shouldn't
        // calculate anything 
        float dist = xa * xa + za * za;
        if (dist < 0.01F) {
            return;
        }
        
        dist = speed / (float) Math.sqrt(dist);
        xa *= dist;
        za *= dist;
        
        //
        float sin = (float) Math.sin(Math.toRadians(this.rotation.y));
        float cos = (float) Math.cos(Math.toRadians(this.rotation.y));

        this.position.x += (xa * cos - za * sin) * Main.TPF;
        this.position.z += (za * cos + xa * sin) * Main.TPF;
    }

    //makes a projection matrix
    public void makeProjection(int width, int height) {
        this.projection.identity();
        this.projection.perspective(fov, width / height, NEAR_PLANE, FAR_PLANE);
    }
    
    //last mouse position
    double lastX = 0;
    double lastY = 0;

    // rotates camera using the cursor's position
    public void rotate(double mx, double my) {
        if (captureMouse) {
            double x = lastX - mx;
            double y = lastY - my;

            this.rotation.x += (y * sensitivity) * Main.TPF;
            this.rotation.y += (x * sensitivity) * Main.TPF;
        }
        lastX = mx;
        lastY = my;
    }

    //returns true if the key was pressed
    private boolean isKeyDown(int key) {
        return glfwGetKey(Main.WINDOW_POINTER, key) == GLFW_PRESS;
    }

    // returns true if the key wasn't pressed
    private boolean isKeyUp(int key) {
        return glfwGetKey(Main.WINDOW_POINTER, key) == GLFW_RELEASE;
    }

    boolean controlAlreadyPressed = false;

    /**
     * May be a little of Overengineering by me, but, here's the idea: it only
     * returns true one time if the left control key is pressed, it won't return
     * true again until that key is released; and pressed again,
     */
    private boolean isControlPressedOnce() {
        if (isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
            if (!controlAlreadyPressed) {
                controlAlreadyPressed = true;
                return true;
            }
            return false;
        }
        if (isKeyUp(GLFW_KEY_LEFT_CONTROL)) {
            controlAlreadyPressed = false;
        }
        return false;
    }
}
