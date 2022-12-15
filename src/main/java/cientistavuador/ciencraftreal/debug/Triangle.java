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
package cientistavuador.ciencraftreal.debug;

import cientistavuador.ciencraftreal.Main;
import java.nio.FloatBuffer;
import java.util.Objects;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class Triangle {

    private static final String vertexShaderSource
            = """
            #version 330 core
            
            #define MODEL_ONLY 0
            #define PROJECTION_MUL_VIEW 1
            #define PROJECTION_VIEW 2
            
            uniform int type;
            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec3 vertexColor;
            
            out vec3 fragmentColor;
            
            void main() {
                vec4 resultPosition = vec4(vertexPosition, 1.0);
                switch (type) {
                    case MODEL_ONLY:
                        resultPosition = model * resultPosition;
                        break;
                    case PROJECTION_MUL_VIEW:
                        resultPosition = projection * resultPosition;
                        break;
                    case PROJECTION_VIEW:
                        resultPosition = projection * view * model * resultPosition;
                        break;
                }
                gl_Position = resultPosition;
                fragmentColor = vertexColor;
            }
            """;

    private static final String fragmentShaderSource
            = """
            #version 330 core
            
            in vec3 fragmentColor;
            
            layout (location = 0) out vec4 bufferColor;
            
            void main() {
                bufferColor = vec4(fragmentColor, 1.0);
            }
            """;

    private static final boolean ONLY_OUTPUT_ERRORS = false;

    private static final int vao;
    private static final int program;

    static {
        int vbo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[]{
            0f, 1f, 0f, 1f, 0f, 0f,
            -1f, -1f, 0f, 0f, 1f, 0f,
            1f, -1f, 0f, 0f, 0f, 1f
        }, GL_STATIC_DRAW);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        Main.checkGLError();

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        boolean vertexShaderFailed = glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !vertexShaderFailed) {
            System.out.println("Debug Triangle Vertex Shader Debug Output -> \n" + glGetShaderInfoLog(vertexShader) + "\n-end-");
        } else if (vertexShaderFailed) {
            throw new RuntimeException("Debug Triangle Vertex Shader Compilation Failed! -> \n" + glGetShaderInfoLog(vertexShader) + "\n-end-");
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        boolean fragmentShaderFailed = glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !fragmentShaderFailed) {
            System.out.println("Debug Triangle Fragment Shader Debug Output -> \n" + glGetShaderInfoLog(fragmentShader) + "\n-end-");
        } else if (fragmentShaderFailed) {
            throw new RuntimeException("Debug Triangle Fragment Shader Compilation Failed! -> \n" + glGetShaderInfoLog(fragmentShader) + "\n-end-");
        }

        program = glCreateProgram();

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);

        boolean programLinkFailed = glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE;
        if (!ONLY_OUTPUT_ERRORS && !programLinkFailed) {
            System.out.println("Debug Triangle Program Debug Output -> \n" + glGetProgramInfoLog(program) + "\n-end-");
        } else if (programLinkFailed) {
            throw new RuntimeException("Debug Triangle Program Link Failed! -> \n" + glGetProgramInfoLog(program) + "\n-end-");
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private final Matrix4f model = new Matrix4f();

    public Triangle() {

    }

    public Matrix4f getModel() {
        return model;
    }

    public void render(Matrix4f projectionView) {
        renderImpl(Objects.requireNonNull(projectionView), null);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        renderImpl(
                Objects.requireNonNull(projection),
                Objects.requireNonNull(view)
        );
    }

    public void render() {
        renderImpl(null, null);
    }

    private void renderImpl(Matrix4f projection, Matrix4f view) {
        glUseProgram(program);
        glBindVertexArray(vao);

        int typeUniformLocation = glGetUniformLocation(program, "type");
        int projectionUniformLocation = glGetUniformLocation(program, "projection");
        int viewUniformLocation = glGetUniformLocation(program, "view");
        int modelUniformLocation = glGetUniformLocation(program, "model");

        if (projection == null && view == null) {
            glUniform1i(typeUniformLocation, 0);
        } else if (projection != null && view == null) {
            glUniform1i(typeUniformLocation, 1);
        } else {
            glUniform1i(typeUniformLocation, 2);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(4 * 4);

            if (projection != null) {
                projection.get(matrixBuffer);
                glUniformMatrix4fv(projectionUniformLocation, false, matrixBuffer);
            }

            if (view != null) {
                view.get(matrixBuffer);
                glUniformMatrix4fv(viewUniformLocation, false, matrixBuffer);
            }

            this.model.get(matrixBuffer);
            glUniformMatrix4fv(modelUniformLocation, false, matrixBuffer);
        }
        
        glDrawArrays(GL_TRIANGLES, 0, 3);
        
        glBindVertexArray(0);
        glUseProgram(0);
    }

}
