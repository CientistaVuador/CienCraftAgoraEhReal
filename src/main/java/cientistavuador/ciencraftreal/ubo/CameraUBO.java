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
package cientistavuador.ciencraftreal.ubo;

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.util.ObjectCleaner;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class CameraUBO {

    public static CameraUBO create(int bindingPoint) {
        int ubo = glGenBuffers();
        CameraUBO cameraUbo = new CameraUBO(bindingPoint, ubo);
        ObjectCleaner.get().register(cameraUbo, () -> {
            Main.MAIN_TASKS.add(() -> {
                glDeleteBuffers(ubo);
            });
        });
        return cameraUbo;
    }

    private final int bindingPoint;
    private final int ubo;

    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Vector3d position = new Vector3d();

    private boolean projectionUpdate = false;
    private boolean viewUpdate = false;
    private boolean positionUpdate = false;
    private boolean needsUpdate = false;

    private CameraUBO(int bindingPoint, int ubo) {
        this.bindingPoint = bindingPoint;
        this.ubo = ubo;

        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        glBufferData(GL_UNIFORM_BUFFER, (16 + 16 + 4 + 4) * Float.BYTES, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glBindBufferBase(GL_UNIFORM_BUFFER, this.bindingPoint, this.ubo);
    }

    public int getBindingPoint() {
        return bindingPoint;
    }

    public int getUBO() {
        return ubo;
    }

    public void setProjection(Matrix4fc projection) {
        projection.get(this.projection);
        this.projectionUpdate = true;
        this.needsUpdate = true;
    }

    public void setView(Matrix4fc view) {
        view.get(this.view);
        this.viewUpdate = true;
        this.needsUpdate = true;
    }

    public void setPosition(Vector3dc position) {
        position.get(this.position);
        this.positionUpdate = true;
        this.needsUpdate = true;
    }

    public Matrix4fc getProjection() {
        return this.projection;
    }

    public Matrix4fc getView() {
        return this.view;
    }

    public Vector3dc getPosition() {
        return this.position;
    }

    public void updateUBO() {
        if (!this.needsUpdate) {
            return;
        }
        this.needsUpdate = false;

        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        if (this.projectionUpdate) {
            glBufferSubData(GL_UNIFORM_BUFFER, 0, this.projection.get(new float[4 * 4]));
            this.projectionUpdate = false;
        }
        if (this.viewUpdate) {
            glBufferSubData(GL_UNIFORM_BUFFER, (4 * 4) * Float.BYTES, this.view.get(new float[4 * 4]));
            this.viewUpdate = false;
        }
        if (this.positionUpdate) {
            int xInt = (int) Math.floor(position.x());
            int yInt = (int) Math.floor(position.y());
            int zInt = (int) Math.ceil(position.z());
            float xDec = (float) (position.x() - xInt);
            float yDec = (float) (position.y() - yInt);
            float zDec = (float) (position.z() - zInt);
            
            int offset = (2 * 4 * 4) * Float.BYTES;
            
            glBufferSubData(GL_UNIFORM_BUFFER, offset, new int[] {
                xInt,
                yInt,
                zInt,
                0
            });
            glBufferSubData(GL_UNIFORM_BUFFER, offset + (4 * Integer.BYTES), new float[] {
                xDec,
                yDec,
                zDec,
                0f
            });
            this.positionUpdate = false;
        }
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

}
