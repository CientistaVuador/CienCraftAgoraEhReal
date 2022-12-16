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
 *
 * @author Shinoa Hiragi
 */
public class Camera {
    
    private static float NEAR_PLANE = 0.001f;
    private static float FAR_PLANE = 100f;
    
    private static Vector3f AxisX = new Vector3f(1, 0, 0);
    private static Vector3f AxisY = new Vector3f(0, 1, 0);
    private static Vector3f AxisZ = new Vector3f(0, 0, 1);

    public Vector3f position;
    public Vector3f rotation;

    public Matrix4f view;
    public Matrix4f projection;
    
    public float fov;
    
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
        float speed = 2 * (float)Main.TPF;
        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_W) == GLFW_PRESS) {
            this.position.z += speed;
        }
        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_S) == GLFW_PRESS) {
            this.position.z -= speed;
        }

        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_A) == GLFW_PRESS) {
            this.position.x += speed;
        }
        if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_D) == GLFW_PRESS) {
            this.position.x -= speed;
        }
        
        this.view.identity();
        this.view.translate(position);
        this.view.rotate(rotation.x, AxisX);
        this.view.rotate(rotation.y, AxisY);
        this.view.rotate(rotation.z, AxisZ);
    }
    
    public void makeProjection(int width, int height) {
        this.projection.identity();
        this.projection.perspective(fov, width / height, NEAR_PLANE, FAR_PLANE);
    }
}
