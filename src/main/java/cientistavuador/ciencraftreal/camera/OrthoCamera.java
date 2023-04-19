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

import static cientistavuador.ciencraftreal.camera.Camera.DEFAULT_POSITION;
import static cientistavuador.ciencraftreal.camera.Camera.DEFAULT_WORLD_UP;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import cientistavuador.ciencraftreal.world.WorldCamera;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 *
 * @author Cien
 */
public class OrthoCamera implements Camera {
    public static final float DEFAULT_FAR_PLANE_ORTHO = 250f;
    public static final float DEFAULT_NEAR_PLANE_ORTHO = 0.025f;
    public static final float DEFAULT_ORTHO_WIDTH = WorldCamera.VIEW_DISTANCE_SIZE * Chunk.CHUNK_SIZE;
    public static final float DEFAULT_ORTHO_HEIGHT = WorldCamera.VIEW_DISTANCE_SIZE * Chunk.CHUNK_SIZE;
    public static final Vector3fc DEFAULT_FRONT = new Vector3f(0f, -1f, 1f).normalize();
    
    //Camera fields
    private final Vector2f dimensions = new Vector2f(DEFAULT_ORTHO_WIDTH, DEFAULT_ORTHO_HEIGHT);
    
    private float nearPlane = DEFAULT_NEAR_PLANE_ORTHO;
    private float farPlane = DEFAULT_FAR_PLANE_ORTHO;
    
    //Camera axises
    private final Vector3f front = new Vector3f(DEFAULT_FRONT);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);
    
    //Position and Rotation
    private final Vector3d position = new Vector3d(DEFAULT_POSITION);
    
    //Matrices
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4d projectionView = new Matrix4d();
    private final Matrix4f projectionViewFloat = new Matrix4f();
    
    //UBO
    private CameraUBO ubo = null;
    
    public OrthoCamera() {
        updateProjection();
        updateView();
    }

    @Override
    public Matrix4fc getView() {
        return view;
    }

    @Override
    public Matrix4fc getProjection() {
        return projection;
    }

    private void updateProjection() {
        this.projection
                .identity()
                .orthoSymmetric(
                        this.dimensions.x(),
                        this.dimensions.y(),
                        this.nearPlane,
                        this.farPlane
                );
        
        if (this.ubo != null) {
            this.ubo.setProjection(this.projection);
        }
        updateProjectionView();
    }
    
    private void updateView() {
        this.front.normalize();
        
        this.right.set(DEFAULT_WORLD_UP).cross(this.front).normalize();
        this.up.set(this.front).cross(this.right).normalize();
        
        this.view.identity().lookAt(
                0,
                0,
                0,
                0 + this.front.x(),
                0 + this.front.y(),
                0 + this.front.z(),
                this.up.x(), this.up.y(), this.up.z()
        );
        
        if (this.ubo != null) {
            this.ubo.setView(this.view);
        }
        updateProjectionView();
    }
    
    private void updateProjectionView() {
        this.projectionView
                .set(this.projection)
                .mul(this.view)
                .translate(-this.position.x(), -this.position.y(), -this.position.z())
                ;
        this.projectionViewFloat.set(this.projectionView);
    }

    @Override
    public void setUBO(CameraUBO ubo) {
        this.ubo = ubo;
        if (this.ubo != null) {
            this.ubo.setProjection(this.projection);
            this.ubo.setView(this.view);
            this.ubo.setPosition(this.position);
        }
    }

    @Override
    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
        updateProjection();
    }

    @Override
    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
        updateProjection();
    }

    @Override
    public void setDimensions(float width, float height) {
        this.dimensions.set(width, height);
        updateProjection();
    }
    
    @Override
    public void setDimensions(Vector2fc dimensions) {
        setDimensions(dimensions.x(), dimensions.y());
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        if (this.ubo != null) {
            this.ubo.setPosition(this.position);
        }
        updateProjectionView();
    }
    
    @Override
    public void setPosition(Vector3fc position) {
        setPosition(position.x(), position.y(), position.z());
    }

    @Override
    public float getNearPlane() {
        return nearPlane;
    }

    @Override
    public float getFarPlane() {
        return farPlane;
    }
    
    @Override
    public Vector2fc getDimensions() {
        return dimensions;
    }

    @Override
    public Vector3dc getPosition() {
        return position;
    }
    
    @Override
    public Vector3fc getFront() {
        return front;
    }

    public void setFront(float x, float y, float z) {
        this.front.set(x, y, z);
        updateView();
    }
    
    public void setFront(Vector3fc dir) {
        setFront(dir.x(), dir.y(), dir.z());
    }
    
    @Override
    public Vector3fc getRight() {
        return right;
    }

    @Override
    public Vector3fc getUp() {
        return up;
    }

    @Override
    public Matrix4dc getProjectionView() {
        return projectionView;
    }

    public Matrix4fc getProjectionViewFloat() {
        return projectionViewFloat;
    }
    
    @Override
    public CameraUBO getUBO() {
        return this.ubo;
    }
    
}
