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

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class AabRender {

    private static final String VERTEX_SHADER
            = """
            #version 330 core
            
            layout (std140) uniform Camera {
                mat4 projection;
                mat4 view;
                ivec4 icamPos;
                vec4 dcamPos;
            };
            
            uniform ivec3 iaabPos;
            uniform vec3 daabPos;
            uniform vec3 aabScale;
            
            layout (location = 0) in vec3 vertexPos;
            
            void main() {
                vec3 resultVertex = (vertexPos * aabScale) + vec3(iaabPos - icamPos.xyz) + vec3(daabPos - dcamPos.xyz);
                gl_Position = projection * view * vec4(resultVertex, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER
            = """
            #version 330 core
            
            layout (location = 0) out vec4 outputColor;
            
            void main() {
                outputColor = vec4(1.0, 0.0, 0.0, 1.0);
            }
            """;

    private static final ConcurrentLinkedQueue<Runnable> renderQueue = new ConcurrentLinkedQueue<>();

    private static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final int CAMERA_UBO_INDEX = glGetUniformBlockIndex(SHADER_PROGRAM, "Camera");
    private static final int AAB_POS_INTEGER_INDEX = glGetUniformLocation(SHADER_PROGRAM, "iaabPos");
    private static final int AAB_POS_DECIMAL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "daabPos");
    private static final int AAB_SCALE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "aabScale");
    private static final int VAO;

    static {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[]{
            //bottom quad
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            //top quad
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            //connections
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f
        }, GL_STATIC_DRAW);

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static void beginRendering(Camera camera) {
        CameraUBO ubo = camera.getUBO();
        if (ubo == null) {
            throw new NullPointerException("Camera UBO is null");
        }

        glUseProgram(SHADER_PROGRAM);
        glUniformBlockBinding(SHADER_PROGRAM, CAMERA_UBO_INDEX, ubo.getBindingPoint());

        glBindVertexArray(VAO);
    }

    public static void render(double x0, double y0, double z0, double x1, double y1, double z1) {
        double centerX = ((x1 - x0) * 0.5) + x0;
        double centerY = ((y1 - y0) * 0.5) + y0;
        double centerZ = ((z1 - z0) * 0.5) + z0;
        int xInt = (int) Math.floor(centerX);
        int yInt = (int) Math.floor(centerY);
        int zInt = (int) Math.ceil(centerZ);
        float xDec = (float) (centerX - xInt);
        float yDec = (float) (centerY - yInt);
        float zDec = (float) (centerZ - zInt);
        float scaleX = (float) (x1 - x0);
        float scaleY = (float) (y1 - y0);
        float scaleZ = (float) (z1 - z0);

        glUniform3i(AAB_POS_INTEGER_INDEX, xInt, yInt, zInt);
        glUniform3f(AAB_POS_DECIMAL_INDEX, xDec, yDec, zDec);
        glUniform3f(AAB_SCALE_INDEX, scaleX, scaleY, scaleZ);

        glDrawArrays(GL_LINES, 0, 24);
    }

    public static void endRendering() {
        glBindVertexArray(0);
        glUseProgram(0);
    }

    public static void queueRender(double x0, double y0, double z0, double x1, double y1, double z1) {
        double centerX = ((x1 - x0) * 0.5) + x0;
        double centerY = ((y1 - y0) * 0.5) + y0;
        double centerZ = ((z1 - z0) * 0.5) + z0;
        int xInt = (int) Math.floor(centerX);
        int yInt = (int) Math.floor(centerY);
        int zInt = (int) Math.ceil(centerZ);
        float xDec = (float) (centerX - xInt);
        float yDec = (float) (centerY - yInt);
        float zDec = (float) (centerZ - zInt);
        float scaleX = (float) (x1 - x0);
        float scaleY = (float) (y1 - y0);
        float scaleZ = (float) (z1 - z0);

        renderQueue.add(() -> {
            glUniform3i(AAB_POS_INTEGER_INDEX, xInt, yInt, zInt);
            glUniform3f(AAB_POS_DECIMAL_INDEX, xDec, yDec, zDec);
            glUniform3f(AAB_SCALE_INDEX, scaleX, scaleY, scaleZ);

            glDrawArrays(GL_LINES, 0, 24);
        });
    }

    public static int renderQueue(Camera camera) {
        int drawCalls = 0;
        
        beginRendering(camera);
        Runnable r;
        while ((r = renderQueue.poll()) != null) {
            r.run();
            drawCalls++;
        }
        endRendering();
        
        return drawCalls;
    }

    private AabRender() {

    }

}
