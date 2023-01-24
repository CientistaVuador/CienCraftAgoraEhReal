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

import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author Cien
 */
public class MaterialUBO {
    
    public static final MaterialUBO DEFAULT = new MaterialUBO(1);
    public static final int SIZE = 1024;
    public static final int NULL = -1;

    private final ConcurrentLinkedQueue<Integer> availableObjects = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Runnable> updateTasks = new ConcurrentLinkedQueue<>();
    private final int ubo;
    private final IntBuffer data;
    private final int bindingPoint;
    
    private MaterialUBO(int bindingPoint) {
        this.data = memCallocInt(SIZE * 4);
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

    public void updateUBO() {
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
        return ubo;
    }
    
    public int allocate() {
        Integer obj = availableObjects.poll();
        if (obj == null) {
            throw new IllegalStateException("Out of Color Objects!");
        }
        return obj;
    }
    
    private void setValue(int materialPointer, int index, int value) {
        long offset = (materialPointer * 4 * 4) + (index * 4);
        long address = memAddress(this.data) + offset;
        memPutInt(address, value);
        updateTasks.add(() -> {
            nglBufferSubData(GL_UNIFORM_BUFFER, offset, 4, address);
        });
    }
    
    private int getValue(int materialPointer, int index) {
        long offset = (materialPointer * 4 * 4) + (index * 4);
        long address = memAddress(this.data) + offset;
        return memGetInt(address);
    }
    
    public void setColorPointer(int materialPointer, int colorPointer) {
        setValue(materialPointer, 0, colorPointer);
    }
    
    public void setFrameTime(int materialPointer, float time) {
        setValue(materialPointer, 1, Float.floatToRawIntBits(time));
    }
    
    public void setFrameStart(int materialPointer, int frameStart) {
        setValue(materialPointer, 2, frameStart);
    }
    
    public void setFrameEnd(int materialPointer, int frameEnd) {
        setValue(materialPointer, 3, frameEnd);
    }
    
    public int getColorPointer(int materialPointer) {
        return getValue(materialPointer, 0);
    }
    
    public float getFrameTime(int materialPointer) {
        return Float.intBitsToFloat(getValue(materialPointer, 1));
    }
    
    public int getFrameStart(int materialPointer) {
        return getValue(materialPointer, 2);
    }
    
    public int getFrameEnd(int materialPointer) {
        return getValue(materialPointer, 3);
    }
    
    public void free(int materialPointer) {
        this.availableObjects.add(materialPointer);
    }

    public void delete() {
        glDeleteBuffers(this.ubo);
        memFree(this.data);
    }
    
}
