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
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class MaterialUBO {
    
    public static final MaterialUBO DEFAULT = create(UBOBindingPoints.BLOCK_MATERIALS);
    public static final int SIZE = 1024;
    public static final int NULL = -1;

    public static MaterialUBO create(int bindingPoint) {
        int ubo = glGenBuffers();
        MaterialUBO materialUbo = new MaterialUBO(bindingPoint, ubo);
        ObjectCleaner.get().register(materialUbo, () -> {
            Main.MAIN_TASKS.add(() -> {
                glDeleteBuffers(ubo);
            });
        });
        return materialUbo;
    }
    
    private final int ubo;
    private final int bindingPoint;
    
    private final int[] colorPointer = new int[SIZE];
    private final float[] frameTime = new float[SIZE];
    private final int[] frameStart = new int[SIZE];
    private final int[] frameEnd = new int[SIZE];
    
    private final boolean[] needsUpdate = new boolean[SIZE];
    private boolean needsUpdateGlobal = false;
    
    private final ConcurrentLinkedQueue<Integer> availableObjects = new ConcurrentLinkedQueue<>();
    
    private MaterialUBO(int bindingPoint, int ubo) {
        this.bindingPoint = bindingPoint;
        this.ubo = ubo;

        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        glBufferData(GL_UNIFORM_BUFFER, SIZE * 4, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glBindBufferBase(GL_UNIFORM_BUFFER, bindingPoint, this.ubo);

        for (int i = 0; i < SIZE; i++) {
            availableObjects.add(i);
        }
    }

    public int getBindingPoint() {
        return bindingPoint;
    }

    public int getUBO() {
        return ubo;
    }

    public int allocate() {
        Integer obj = availableObjects.poll();
        if (obj == null) {
            throw new IllegalStateException("Out of Material Objects!");
        }
        return obj;
    }
    
    public void setColorPointer(int pointer, int colorPointer) {
        this.colorPointer[pointer] = colorPointer;
        this.needsUpdate[pointer] = true;
        this.needsUpdateGlobal = true;
    }
    
    public void setFrameTime(int pointer, float frameTime) {
        this.frameTime[pointer] = frameTime;
        this.needsUpdate[pointer] = true;
        this.needsUpdateGlobal = true;
    }
    
    public void setFrameStart(int pointer, int frameStart) {
        this.frameStart[pointer] = frameStart;
        this.needsUpdate[pointer] = true;
        this.needsUpdateGlobal = true;
    }
    
    public void setFrameEnd(int pointer, int frameEnd) {
        this.frameEnd[pointer] = frameEnd;
        this.needsUpdate[pointer] = true;
        this.needsUpdateGlobal = true;
    }
    
    public int getColorPointer(int pointer) {
        return this.colorPointer[pointer];
    }
    
    public float getFrameTime(int pointer) {
        return this.frameTime[pointer];
    }
    
    public int getFrameStart(int pointer) {
        return this.frameStart[pointer];
    }
    
    public int getFrameEnd(int pointer) {
        return this.frameEnd[pointer];
    }
    
    public void free(int pointer) {
        this.availableObjects.add(pointer);
    }
    
    public void updateUBO() {
        if (!this.needsUpdateGlobal) {
            return;
        }
        this.needsUpdateGlobal = false;
        
        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        for (int i = 0; i < SIZE; i++) {
            if (!this.needsUpdate[i]) {
                continue;
            }
            glBufferSubData(GL_UNIFORM_BUFFER, (i * 4) * Integer.BYTES, new int[] {
                this.colorPointer[i],
                Float.floatToRawIntBits(this.frameTime[i]),
                this.frameStart[i],
                this.frameEnd[i]
            });
            this.needsUpdate[i] = false;
        }
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
}
