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

import cientistavuador.ciencraftreal.ubo.CameraUBO;
import org.joml.Matrix4dc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 *
 * @author Cien
 */
public interface Camera {

    float DEFAULT_FAR_PLANE = 1000f;
    float DEFAULT_NEAR_PLANE = 0.1f;
    float DEFAULT_PITCH = 0f;
    Vector3fc DEFAULT_POSITION = new Vector3f(0, 0, 0);
    Vector3fc DEFAULT_WORLD_UP = new Vector3f(0, 1, 0);
    float DEFAULT_YAW = -90f;

    Vector2fc getDimensions();

    float getFarPlane();
    
    float getNearPlane();

    Vector3fc getRotation();

    Vector3fc getFront();
    
    Vector3fc getRight();
    
    Vector3fc getUp();

    Matrix4fc getProjection();
    
    Matrix4fc getView();
    
    Vector3dc getPosition();
    
    Matrix4dc getProjectionView();
    
    CameraUBO getUBO();

    void setDimensions(float width, float height);

    void setDimensions(Vector2fc dimensions);

    void setFarPlane(float farPlane);

    void setNearPlane(float nearPlane);

    void setPosition(double x, double y, double z);

    void setPosition(Vector3fc position);

    void setRotation(float pitch, float yaw, float roll);

    void setRotation(Vector3fc rotation);
    
    void setUBO(CameraUBO ubo);

}
