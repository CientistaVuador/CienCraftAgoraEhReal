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
import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.util.ObjectCleaner;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.joml.Matrix4fc;
import org.joml.Vector3dc;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class CameraUBO {

    public static CameraUBO create(int bindingPoint) {
        FloatBuffer buffer = memCallocFloat(16 + 16 + 8);
        int ubo;
        try {
            ubo = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, ubo);
            glBufferData(GL_UNIFORM_BUFFER, buffer, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
            glBindBufferBase(GL_UNIFORM_BUFFER, bindingPoint, ubo);
        } catch (Throwable e) {
            memFree(buffer);
            throw e;
        }
        CameraUBO cameraUbo = new CameraUBO(bindingPoint, buffer, ubo);
        ObjectCleaner.get().register(cameraUbo, () -> {
            memFree(buffer);
            Main.MAIN_TASKS.add(() -> {
                glDeleteBuffers(ubo);
            });
        });
        return cameraUbo;
    }

    private final int bindingPoint;
    private final FloatBuffer buffer;
    private final int ubo;

    private final AtomicBoolean updateProjection = new AtomicBoolean(false);
    private final AtomicBoolean updateView = new AtomicBoolean(false);
    private final AtomicBoolean updatePosition = new AtomicBoolean(false);

    private Camera camera;

    private CameraUBO(int bindingPoint, FloatBuffer buffer, int ubo) {
        this.bindingPoint = bindingPoint;
        this.buffer = buffer;
        this.ubo = ubo;
    }

    public int getBindingPoint() {
        return bindingPoint;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        notifyProjection();
        notifyView();
        notifyPosition();
    }

    public void notifyProjection() {
        this.updateProjection.set(true);
    }

    public void notifyView() {
        this.updateView.set(true);
    }

    public void notifyPosition() {
        this.updatePosition.set(true);
    }

    private void updateProjection() {
        long address = memAddress(this.buffer);

        Matrix4fc projection = this.camera.getProjection();
        projection.getToAddress(address);

        nglBufferSubData(GL_UNIFORM_BUFFER, 0, 4 * 4 * Float.BYTES, address);
        this.updateProjection.set(false);
    }

    private void updateView() {
        long address = memAddress(this.buffer) + (4 * 4 * Float.BYTES);

        Matrix4fc view = this.camera.getView();
        view.getToAddress(address);

        nglBufferSubData(GL_UNIFORM_BUFFER, 4 * 4 * Float.BYTES, 4 * 4 * Float.BYTES, address);
        this.updateView.set(false);
    }

    private void updatePosition() {
        long address = memAddress(this.buffer) + (2 * 4 * 4 * Float.BYTES);

        Vector3dc position = this.camera.getPosition();

        int xInt = (int) Math.floor(position.x());
        int yInt = (int) Math.floor(position.y());
        int zInt = (int) Math.ceil(position.z());
        float xDec = (float) (position.x() - xInt);
        float yDec = (float) (position.y() - yInt);
        float zDec = (float) (position.z() - zInt);
        
        long addressOffset = address;

        memPutInt(addressOffset + 0, xInt);
        memPutInt(addressOffset + 4, yInt);
        memPutInt(addressOffset + 8, zInt);
        memPutInt(addressOffset + 12, 0);

        addressOffset += (12 + 4);

        memPutFloat(addressOffset + 0, xDec);
        memPutFloat(addressOffset + 4, yDec);
        memPutFloat(addressOffset + 8, zDec);
        memPutFloat(addressOffset + 12, 0f);

        nglBufferSubData(GL_UNIFORM_BUFFER, 2 * 4 * 4 * Float.BYTES, 2 * 4 * Float.BYTES, address);
        this.updatePosition.set(false);
    }

    public void updateUBO() {
        boolean projection = this.updateProjection.get();
        boolean view = this.updateView.get();
        boolean position = this.updatePosition.get();

        if (projection || view || position) {
            glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
            if (projection) {
                updateProjection();
            }
            if (view) {
                updateView();
            }
            if (position) {
                updatePosition();
            }
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
    }

}
