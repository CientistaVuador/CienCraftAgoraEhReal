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
package cientistavuador.ciencraftreal.util;

import java.nio.FloatBuffer;
import org.joml.Matrix4fc;

import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class UniformSetter {
    
    private static int getCurrentProgram() {
        int program = glGetInteger(GL_CURRENT_PROGRAM);
        if (program == 0) {
            throw new NullPointerException("No current bound shader program.");
        }
        return program;
    }
    
    private static int getUniformLocation(String name) {
        int program = getCurrentProgram();
        int location = glGetUniformLocation(program, name);
        if (location == -1) {
            throw new NullPointerException("No uniform with name '"+name+"' in program "+program);
        }
        return location;
    }
    
    public static void setMatrix4f(String name, Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 4);
            matrix.get(buffer);
            
            glUniformMatrix4fv(getUniformLocation(name), false, buffer);
        }
    }
    
    private UniformSetter() {
        
    }
}
