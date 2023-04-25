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

import cientistavuador.ciencraftreal.Main;
import java.util.Map;
import java.util.Map.Entry;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.opengl.KHRDebug;

/**
 *
 * @author Cien
 */
public class ProgramCompiler {

    private static final boolean ONLY_OUTPUT_ERRORS = false;

    public static int compile(String vertexSource, String fragmentSource) {
        return compile(vertexSource, null, fragmentSource);
    }

    public static int compile(String vertexSource, String fragmentSource, Map<String, String> replacements) {
        return compile(vertexSource, null, fragmentSource, replacements);
    }

    public static int compile(String vertexSource, String geometrySource, String fragmentSource) {
        return compile(vertexSource, geometrySource, fragmentSource, null);
    }

    private static String replace(String s, Map<String, String> replacements) {
        for (Entry<String, String> e : replacements.entrySet()) {
            s = s.replace(e.getKey(), e.getValue());
        }
        return s;
    }

    public static int compile(String vertexSource, String geometrySource, String fragmentSource, Map<String, String> replacements) {
        String shaderName = null;
        if (!ONLY_OUTPUT_ERRORS) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                if (i == 0) {
                    continue;
                }
                StackTraceElement e = stackTrace[i];
                if (!e.getClassName().contains(ProgramCompiler.class.getName())) {
                    shaderName = e.toString();
                    System.out.println("Compiling shader in " + shaderName);
                    break;
                }
            }
        }
        
        if (replacements != null) {
            vertexSource = replace(vertexSource, replacements);
            fragmentSource = replace(fragmentSource, replacements);
            if (geometrySource != null) {
                geometrySource = replace(geometrySource, replacements);
            }
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

        int geometryShader = 0;
        if (geometrySource != null) {
            geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
            glShaderSource(geometryShader, geometrySource);
            glCompileShader(geometryShader);

            boolean geometryShaderFailed = glGetShaderi(geometryShader, GL_COMPILE_STATUS) != GL_TRUE;
            if (!ONLY_OUTPUT_ERRORS && !geometryShaderFailed) {
                System.out.println("Geometry Shader Debug Output -> \n" + glGetShaderInfoLog(geometryShader) + "\n-end-");
            } else if (geometryShaderFailed) {
                throw new RuntimeException("Geometry Shader Compilation Failed! -> \n" + glGetShaderInfoLog(geometryShader) + "\n-end-");
            }
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
        if (geometryShader != 0) {
            glAttachShader(program, geometryShader);
        }
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);

        boolean programLinkFailed = glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !programLinkFailed) {
            System.out.println("Program Debug Output -> \n" + glGetProgramInfoLog(program) + "\n-end-");
        } else if (programLinkFailed) {
            throw new RuntimeException("Program Link Failed! -> \n" + glGetProgramInfoLog(program) + "\n-end-");
        }

        glDeleteShader(vertexShader);
        if (geometryShader != 0) {
            glDeleteShader(geometryShader);
        }
        glDeleteShader(fragmentShader);
        
        if (Main.DEBUG_ENABLED && shaderName != null && GL.getCapabilities().GL_KHR_debug) {
            KHRDebug.glObjectLabel(KHRDebug.GL_PROGRAM, program, "Program_"+shaderName);
        }

        return program;
    }

    private ProgramCompiler() {

    }
}
