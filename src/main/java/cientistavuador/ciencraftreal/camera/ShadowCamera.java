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
import org.joml.Vector4d;

/**
 *
 * @author Cien https://learnopengl.com/Guest-Articles/2021/CSM
 */
public class ShadowCamera implements Camera {

    private CameraUBO ubo = null;

    private final Vector3f front = new Vector3f();
    private final Vector3f right = new Vector3f();
    private final Vector3f up = new Vector3f();

    private final Vector3d position = new Vector3d();
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();

    private final Matrix4d viewPosition = new Matrix4d();
    private final Matrix4d inverseCameraProjectionView = new Matrix4d();

    private final Matrix4d projectionView = new Matrix4d();

    private float farPlane = 0f;
    private float nearPlane = 0f;

    private final Vector2f dimensions = new Vector2f();

    private final Vector4d[] frustumCorners = new Vector4d[2 * 2 * 2];

    public ShadowCamera() {

    }

    public void update(Camera camera, Vector3fc lightDir, float zMult) {
        camera.getProjectionView().invert(this.inverseCameraProjectionView);

        int arrayIndex = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Vector4d corner = this.frustumCorners[arrayIndex];
                    if (corner == null) {
                        corner = new Vector4d();
                    }
                    this.frustumCorners[arrayIndex]
                            = this.inverseCameraProjectionView
                                    .transformProject(
                                            corner.set(
                                                    (2.0 * x) - 1.0,
                                                    (2.0 * y) - 1.0,
                                                    (2.0 * z) - 1.0,
                                                    1.0
                                            )
                                    );
                    arrayIndex++;
                }
            }
        }

        this.position.zero();
        for (int i = 0; i < this.frustumCorners.length; i++) {
            Vector4d corner = this.frustumCorners[i];
            this.position.add(corner.x(), corner.y(), corner.z());
        }
        this.position.div(this.frustumCorners.length);

        this.front.set(lightDir);
        buildView();
        buildProjection(zMult);

        this.projectionView.set(this.projection).mul(this.viewPosition);

        if (this.ubo != null) {
            this.ubo.setPosition(this.position);
        }
    }

    private void buildProjection(float zMult) {
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < this.frustumCorners.length; i++) {
            Vector4d trf = this.viewPosition.transform(this.frustumCorners[i]);
            minX = (float) Math.min(minX, trf.x());
            maxX = (float) Math.max(maxX, trf.x());
            minY = (float) Math.min(minY, trf.y());
            maxY = (float) Math.max(maxY, trf.y());
            minZ = (float) Math.min(minZ, trf.z());
            maxZ = (float) Math.max(maxZ, trf.z());
        }

        if (minZ < 0) {
            minZ *= zMult;
        } else {
            minZ /= zMult;
        }
        if (maxZ < 0) {
            maxZ /= zMult;
        } else {
            maxZ *= zMult;
        }

        this.farPlane = maxZ;
        this.nearPlane = minZ;
        this.dimensions.set((maxX - minX), (maxY - minY));
        this.projection.setOrtho(minX, maxX, minY, maxY, -maxZ, -minZ);
        if (this.ubo != null) {
            this.ubo.setProjection(this.projection);
        }
    }

    private void buildView() {
        this.front.normalize();

        this.right.set(DEFAULT_WORLD_UP).cross(this.front).normalize();
        this.up.set(this.front).cross(this.right).normalize();

        this.view.setLookAt(
                0,
                0,
                0,
                0 + this.front.x(),
                0 + this.front.y(),
                0 + this.front.z(),
                this.up.x(), this.up.y(), this.up.z()
        );
        this.viewPosition
                .set(this.view)
                .translate(-this.position.x(), -this.position.y(), -this.position.z());

        if (this.ubo != null) {
            this.ubo.setView(this.view);
        }
    }

    @Override
    public Vector2fc getDimensions() {
        return this.dimensions;
    }

    @Override
    public float getFarPlane() {
        return this.farPlane;
    }

    @Override
    public float getNearPlane() {
        return this.nearPlane;
    }

    @Override
    public Vector3fc getFront() {
        return this.front;
    }

    @Override
    public Vector3fc getRight() {
        return this.right;
    }

    @Override
    public Vector3fc getUp() {
        return this.up;
    }

    @Override
    public Matrix4fc getProjection() {
        return this.projection;
    }

    @Override
    public Matrix4fc getView() {
        return this.view;
    }

    @Override
    public Vector3dc getPosition() {
        return this.position;
    }

    @Override
    public Matrix4dc getProjectionView() {
        return this.projectionView;
    }

    @Override
    public CameraUBO getUBO() {
        return this.ubo;
    }

    @Override
    public void setDimensions(float width, float height) {

    }

    @Override
    public void setDimensions(Vector2fc dimensions) {

    }

    @Override
    public void setFarPlane(float farPlane) {

    }

    @Override
    public void setNearPlane(float nearPlane) {

    }

    @Override
    public void setPosition(double x, double y, double z) {

    }

    @Override
    public void setPosition(Vector3dc position) {

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

}
