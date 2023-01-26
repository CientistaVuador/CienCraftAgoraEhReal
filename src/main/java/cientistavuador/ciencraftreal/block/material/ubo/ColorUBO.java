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
package cientistavuador.ciencraftreal.block.material.ubo;

import cientistavuador.ciencraftreal.ubo.UBOBindingPoints;
import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ColorUBO {

    public static final ColorUBO DEFAULT = new ColorUBO(UBOBindingPoints.BLOCK_COLORS);
    public static final int SIZE = 1024;
    public static final int NULL = -1;

    private final ConcurrentLinkedQueue<Integer> availableObjects = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Runnable> updateTasks = new ConcurrentLinkedQueue<>();
    private final int ubo;
    private final FloatBuffer data;
    private final int bindingPoint;

    private ColorUBO(int bindingPoint) {
        this.data = memCallocFloat(SIZE * 4);
        this.bindingPoint = bindingPoint;
        try {
            this.ubo = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
            glBufferData(GL_UNIFORM_BUFFER, data, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            glBindBufferBase(GL_UNIFORM_BUFFER, bindingPoint, this.ubo);
        } catch (Throwable e) {
            memFree(this.data);
            throw e;
        }
        for (int i = 0; i < SIZE; i++) {
            availableObjects.add(i);
        }
    }

    public int getBindingPoint() {
        return bindingPoint;
    }
    
    public void updateVBO() {
        if (!updateTasks.isEmpty()) {
            glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
            Runnable r;
            while ((r = this.updateTasks.poll()) != null) {
                r.run();
            }
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
    }

    public int getUBO() {
        return this.ubo;
    }

    public int allocate() {
        Integer obj = availableObjects.poll();
        if (obj == null) {
            throw new IllegalStateException("Out of Color Objects!");
        }
        return obj;
    }

    public void setColor(int colorPointer, float r, float g, float b, float a) {
        long address = memAddress(this.data) + (colorPointer * 4 * 4);
        memPutFloat(address + 0, r);
        memPutFloat(address + 4, g);
        memPutFloat(address + 8, b);
        memPutFloat(address + 12, a);
        updateTasks.add(() -> {
            nglBufferSubData(GL_UNIFORM_BUFFER, colorPointer * 4 * 4, 4 * 4, address);
        });
    }

    public float getColor(int colorPointer, int channel) {
        long address = memAddress(this.data) + (colorPointer * 4 * 4);
        return memGetFloat(address + (channel * 4));
    }

    public void free(int colorPointer) {
        this.availableObjects.add(colorPointer);
    }

    public void delete() {
        glDeleteBuffers(this.ubo);
        memFree(this.data);
    }
}
