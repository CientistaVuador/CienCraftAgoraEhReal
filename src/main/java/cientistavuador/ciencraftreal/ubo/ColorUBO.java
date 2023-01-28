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
public class ColorUBO {

    public static final ColorUBO DEFAULT = create(UBOBindingPoints.BLOCK_COLORS);
    public static final int SIZE = 1024;
    public static final int NULL = -1;

    public static ColorUBO create(int bindingPoint) {
        int ubo = glGenBuffers();
        ColorUBO colorUbo = new ColorUBO(bindingPoint, ubo);
        ObjectCleaner.get().register(colorUbo, () -> {
            Main.MAIN_TASKS.add(() -> {
                glDeleteBuffers(ubo);
            });
        });
        return colorUbo;
    }

    private final int bindingPoint;
    private final int ubo;
    
    private final float[] colorsRGBA = new float[SIZE * 4];
    
    private final boolean[] needsUpdate = new boolean[SIZE];
    private boolean needsUpdateGlobal = false;

    private final ConcurrentLinkedQueue<Integer> availableObjects = new ConcurrentLinkedQueue<>();

    private ColorUBO(int bindingPoint, int ubo) {
        this.bindingPoint = bindingPoint;
        this.ubo = ubo;

        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        glBufferData(GL_UNIFORM_BUFFER, this.colorsRGBA, GL_DYNAMIC_DRAW);
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
            throw new IllegalStateException("Out of Color Objects!");
        }
        return obj;
    }

    public float getColor(int pointer, int channel) {
        return this.colorsRGBA[(pointer * 4) + channel];
    }

    public float[] getColor(int pointer) {
        float[] array = new float[4];
        System.arraycopy(this.colorsRGBA, pointer * 4, array, 0, 4);
        return array;
    }

    public void setColor(int pointer, float r, float g, float b, float a) {
        System.arraycopy(new float[]{r, g, b, a}, 0, this.colorsRGBA, pointer * 4, 4);
        this.needsUpdate[pointer] = true;
        this.needsUpdateGlobal = true;
    }

    public void setColor(int pointer, float[] color) {
        System.arraycopy(color, 0, this.colorsRGBA, pointer * 4, 4);
        this.needsUpdate[pointer] = true;
        this.needsUpdateGlobal = true;
    }

    public void free(int colorPointer) {
        this.availableObjects.add(colorPointer);
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
            glBufferSubData(GL_UNIFORM_BUFFER, (i * 4) * Float.BYTES, getColor(i));
            this.needsUpdate[i] = false;
        }
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
    
}
