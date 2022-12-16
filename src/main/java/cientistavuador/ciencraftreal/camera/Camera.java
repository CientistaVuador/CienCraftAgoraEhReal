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
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.glfw.GLFW.*;

/**
 * A Somewhat simple 3D Camera
 * @author Shinoa Hiragi
 */
public class Camera {

    public static final float DEFAULT_NEAR_PLANE = 0.1f;
    public static final float DEFAULT_FAR_PLANE = 1000f;
    public static final float DEFAULT_FOV = 90f;
    public static final float DEFAULT_SENSITIVITY = 8f;
    public static final float DEFAULT_SPEED = 3f;
    public static final float DEFAULT_PITCH = 0f;
    public static final float DEFAULT_YAW = -90f;
    public static final Vector3fc DEFAULT_WORLD_UP = new Vector3f(0, 1, 0);
    public static final Vector3fc DEFAULT_POSITION = new Vector3f(0, 0, 2);
    
    //Camera fields
    private final Vector2f dimensions = new Vector2f(Main.WIDTH, Main.HEIGHT);
    
    private float nearPlane = DEFAULT_NEAR_PLANE;
    private float farPlane = DEFAULT_FAR_PLANE;
    private float fov = DEFAULT_FOV;
    
    private float sensitivity = DEFAULT_SENSITIVITY;
    private float speed = DEFAULT_SPEED;
    
    //Camera axises
    private final Vector3f front = new Vector3f(0, 0, 1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);
    
    //whatever it should capture the cursor or not.
    // press LeftControl in game to capture/release the cursor
    private boolean captureMouse = false;
    private boolean controlAlreadyPressed = false;
    
    //Position and Rotation
    private final Vector3f position = new Vector3f(DEFAULT_POSITION);
    private final Vector3f rotation = new Vector3f(DEFAULT_PITCH, DEFAULT_YAW, 0);
    
    //Matrices
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();
    
    //Last mouse position
    private double lastX = 0;
    private double lastY = 0;
    
    public Camera() {
        updateProjection();
        updateView();
    }

    public Matrix4fc getView() {
        return view;
    }

    public Matrix4fc getProjection() {
        return projection;
    }

    private void updateProjection() {
        this.projection
                .identity()
                .perspective(
                        (float) Math.toRadians(this.fov),
                        (this.dimensions.x() / this.dimensions.y()),
                        this.nearPlane,
                        this.farPlane
                );
    }
    
    private void updateView() {
        float pitchRadians = (float) Math.toRadians(this.rotation.x());
        float yawRadians = (float) Math.toRadians(this.rotation.y());
        
        this.front.set(
                Math.cos(pitchRadians) * Math.cos(yawRadians),
                Math.sin(pitchRadians),
                Math.cos(pitchRadians) * Math.sin(yawRadians)
        ).normalize();
        
        this.right.set(DEFAULT_WORLD_UP).cross(this.front).normalize();
        this.up.set(this.front).cross(this.right).normalize();
        
        this.view.identity().lookAt(
                this.position.x(),
                this.position.y(),
                this.position.z(),
                this.position.x() + this.front.x(),
                this.position.y() + this.front.y(),
                this.position.z() + this.front.z(),
                this.up.x(), this.up.y(), this.up.z()
        );
    }

    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
        updateProjection();
    }

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
        updateProjection();
    }

    public void setFov(float fov) {
        this.fov = fov;
        updateProjection();
    }

    public void setDimensions(float width, float height) {
        this.dimensions.set(width, height);
        updateProjection();
    }
    
    public void setDimensions(Vector2fc dimensions) {
        setDimensions(dimensions.x(), dimensions.y());
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        updateView();
    }
    
    public void setPosition(Vector3fc position) {
        setPosition(position.x(), position.y(), position.z());
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.rotation.set(pitch, yaw, roll);
        updateView();
    }
    
    public void setRotation(Vector3fc rotation) {
        setRotation(rotation.x(), rotation.y(), rotation.z());
    }
    
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public float getFov() {
        return fov;
    }

    public Vector2fc getDimensions() {
        return dimensions;
    }

    public Vector3fc getPosition() {
        return position;
    }

    public Vector3fc getRotation() {
        return rotation;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public float getSpeed() {
        return speed;
    }
    
    public Vector3fc getFront() {
        return front;
    }

    public Vector3fc getRight() {
        return right;
    }

    public Vector3fc getUp() {
        return up;
    }
    
    //movimentation magic
    public void updateMovement() {
        if (isControlPressedOnce()) {
            this.captureMouse = !this.captureMouse;
            // if true, disable cursor,
            // if false, set cursor to normal
            glfwSetInputMode(Main.WINDOW_POINTER, GLFW_CURSOR,
                        this.captureMouse ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
            System.out.println("Capture state: " + this.captureMouse);
        }
        
        //acceleration in local X and Z axis
        float xa = 0;
        float za = 0;
        
        if (isKeyDown(GLFW_KEY_W)) {
            za = this.speed;
        } else if (isKeyDown(GLFW_KEY_S)) {
            za = -this.speed;
        }
        
        za *= Main.TPF;
        
        this.position.add(
                this.front.x() * za,
                this.front.y() * za,
                this.front.z() * za
        );
        
        if (isKeyDown(GLFW_KEY_A)) {
            xa = this.speed;
        } else if (isKeyDown(GLFW_KEY_D)) {
            xa = -this.speed;
        }
        
        xa *= Main.TPF;
        
        this.position.add(
                this.right.x() * xa,
                this.right.y() * xa,
                this.right.z() * xa
        );
        
        updateView();
    }

    // rotates camera using the cursor's position
    public void mouseCursorMoved(double mx, double my) {
        if (captureMouse) {
            double x = lastX - mx;
            double y = lastY - my;
            
            this.rotation.add(
                    (float) ((y * sensitivity) * Main.TPF),
                    (float) ((x * sensitivity) * -Main.TPF),
                    0
            );
            
            if (this.rotation.x() >= 90) {
                this.rotation.setComponent(0, 89.9f);
            }
            if (this.rotation.x() <= -90) {
                this.rotation.setComponent(0, -89.9f);
            }
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
