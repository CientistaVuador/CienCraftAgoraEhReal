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
import java.util.Arrays;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class FontTextUBO {
    
    public static final FontTextUBO DEFAULT = create(UBOBindingPoints.FONT_TEXT);
    public static final int SIZE = 1024;
    public static final int NUMBER_OF_COMPONENTS = 4;

    public static FontTextUBO create(int bindingPoint) {
        int ubo = glGenBuffers();
        FontTextUBO fontTextUbo = new FontTextUBO(bindingPoint, ubo);
        ObjectCleaner.get().register(fontTextUbo, () -> {
            Main.MAIN_TASKS.add(() -> {
                glDeleteBuffers(ubo);
            });
        });
        return fontTextUbo;
    }
    
    private final int ubo;
    private final int bindingPoint;
    
    private final int[] data = new int[SIZE * 4];
    private int index = 0;
    private int length = 0;

    public FontTextUBO(int bindingPoint, int ubo) {
        this.bindingPoint = bindingPoint;
        this.ubo = ubo;
        
        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        glBufferData(GL_UNIFORM_BUFFER, this.data, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glBindBufferBase(GL_UNIFORM_BUFFER, bindingPoint, this.ubo);
    }

    public int getIndex() {
        return this.index;
    }

    public int getLength() {
        return this.length;
    }

    public int getBindingPoint() {
        return bindingPoint;
    }

    public int getUBO() {
        return ubo;
    }
    
    public boolean canPush() {
        return this.index < this.data.length;
    }
    
    public void push(int unicodeIndex, int unused, float originX, float originY) {
        this.data[(this.index * 4) + 0] = unicodeIndex;
        this.data[(this.index * 4) + 1] = unused;
        this.data[(this.index * 4) + 2] = Float.floatToRawIntBits(originX);
        this.data[(this.index * 4) + 3] = Float.floatToRawIntBits(originY);
        
        this.index++;
    }
    
    public void flipAndUpdate() {
        this.length = this.index;
        this.index = 0;
        
        glBindBuffer(GL_UNIFORM_BUFFER, this.ubo);
        glBufferData(GL_UNIFORM_BUFFER, this.data, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
    
}
