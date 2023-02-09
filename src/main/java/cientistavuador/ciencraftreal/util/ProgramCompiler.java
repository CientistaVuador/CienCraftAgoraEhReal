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

import java.util.Map;
import java.util.Map.Entry;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ProgramCompiler {

    private static final boolean ONLY_OUTPUT_ERRORS = false;

    public static int compile(String vertexSource, String fragmentSource) {
        return compile(vertexSource, fragmentSource, null);
    }

    private static String replace(String s, Map<String, String> replacements) {
        for (Entry<String, String> e : replacements.entrySet()) {
            s = s.replace(e.getKey(), e.getValue());
        }
        return s;
    }

    public static int compile(String vertexSource, String fragmentSource, Map<String, String> replacements) {
        if (!ONLY_OUTPUT_ERRORS) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                if (i == 0) {
                    continue;
                }
                StackTraceElement e = stackTrace[i];
                if (!e.getClassName().contains(ProgramCompiler.class.getName())) {
                    System.out.println("Compiling shader in "+e);
                    break;
                }
            }
        }

        if (replacements != null) {
            vertexSource = replace(vertexSource, replacements);
            fragmentSource = replace(fragmentSource, replacements);
        }
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);

        boolean vertexShaderFailed = glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !vertexShaderFailed) {
            System.out.println("Vertex Shader Debug Output -> \n" + glGetShaderInfoLog(vertexShader) + "\n-end-");
        } else if (vertexShaderFailed) {
            throw new RuntimeException("Vertex Shader Compilation Failed! -> \n" + glGetShaderInfoLog(vertexShader) + "\n-end-");
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);

        boolean fragmentShaderFailed = glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !fragmentShaderFailed) {
            System.out.println("Fragment Shader Debug Output -> \n" + glGetShaderInfoLog(fragmentShader) + "\n-end-");
        } else if (fragmentShaderFailed) {
            throw new RuntimeException("Fragment Shader Compilation Failed! -> \n" + glGetShaderInfoLog(fragmentShader) + "\n-end-");
        }

        int program = glCreateProgram();

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);

        boolean programLinkFailed = glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !programLinkFailed) {
            System.out.println("Program Debug Output -> \n" + glGetProgramInfoLog(program) + "\n-end-");
        } else if (programLinkFailed) {
            throw new RuntimeException("Program Link Failed! -> \n" + glGetProgramInfoLog(program) + "\n-end-");
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }

    private ProgramCompiler() {

    }
}
