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
import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import cientistavuador.ciencraftreal.world.WorldCamera;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class BlockOutline {

    public static final boolean OUTPUT_DEBUG_INFORMATION = true;
    public static final float LINE_WIDTH = 4f;
    public static final float LINE_MIN;
    public static final float LINE_MAX;

    static {
        int[] lineMinMax = new int[2];
        glGetIntegerv(GL_ALIASED_LINE_WIDTH_RANGE, lineMinMax);

        LINE_MIN = lineMinMax[0];
        LINE_MAX = lineMinMax[1];

        if (OUTPUT_DEBUG_INFORMATION) {
            System.out.println("Min Line Width: " + LINE_MIN + ", Max Line Width: " + LINE_MAX);
        }
    }

    private static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            layout (std140) uniform Camera {
                mat4 projection;
                mat4 view;
                ivec4 icamPos;
                vec4 dcamPos;
            };
            
            uniform ivec3 blockPosition;
            
            layout (location = 0) in vec3 vertexPosition;
            
            void main() {
                vec3 position = vertexPosition * 1.01;
                position += vec3(blockPosition - icamPos.xyz);
                position -= dcamPos.xyz;
                gl_Position = projection * view * vec4(position + vec3(0.5, 0.5, -0.5), 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER
            = """
            #version 330 core
            
            layout (location = 0) out vec4 fragColor;
            
            void main() {
                fragColor = vec4(0.15, 0.15, 0.15, 0.5);
            }
            """;

    private static final int shaderProgram = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final int vao = glGenVertexArrays();
    private static final int cameraUboIndex = glGetUniformBlockIndex(shaderProgram, "Camera");
    private static final int blockPositionUniformLocation = glGetUniformLocation(shaderProgram, "blockPosition");
    
    static {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[]{
            //sides
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            //top
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            //bottom
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f
        }, GL_STATIC_DRAW);

        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        Main.checkGLError();
    }

    public static final int MAX_LENGTH = 8;
    public static final float STEP = 0.03f;

    private final WorldCamera world;
    private final Camera camera;
    private final Vector3d castVector = new Vector3d();

    private final Vector3i castPos = new Vector3i();
    private final Vector3i sidePos = new Vector3i();
    private Block block = Blocks.AIR;

    public BlockOutline(WorldCamera world, Camera camera) {
        this.world = world;
        this.camera = camera;
    }

    public WorldCamera getWorld() {
        return world;
    }

    public Camera getCamera() {
        return camera;
    }

    public Block getBlock() {
        return block;
    }

    public Vector3ic getCastPos() {
        return this.castPos;
    }

    public Vector3ic getSidePos() {
        return this.sidePos;
    }

    public void update() {
        int lastCastPosX = 0;
        int lastCastPosY = 0;
        int lastCastPosZ = 0;
        boolean ignoreFirst = false;

        float length = 0f;
        while (length < MAX_LENGTH) {
            castVector.zero().add(camera.getFront()).mul(length);
            length += STEP;

            this.sidePos.set(this.castPos);
            this.castPos.set(
                    (int) Math.floor(castVector.x() + camera.getPosition().x()),
                    (int) Math.floor(castVector.y() + camera.getPosition().y()),
                    (int) Math.ceil(castVector.z() + camera.getPosition().z())
            );

            if (ignoreFirst && this.castPos.equals(lastCastPosX, lastCastPosY, lastCastPosZ)) {
                continue;
            }

            ignoreFirst = true;
            lastCastPosX = this.castPos.x();
            lastCastPosY = this.castPos.y();
            lastCastPosZ = this.castPos.z();

            this.block = this.world.getWorldBlock(this.castPos.x(), this.castPos.y(), this.castPos.z());

            if (this.block != Blocks.AIR) {
                break;
            }
        }
    }

    public void render() {
        if (this.block == Blocks.AIR) {
            return;
        }

        CameraUBO ubo = getCamera().getUBO();
        if (ubo == null) {
            throw new RuntimeException("Camera UBO is null");
        }
        ubo.updateUBO();
        glUniformBlockBinding(shaderProgram, cameraUboIndex, ubo.getBindingPoint());
        
        glUseProgram(shaderProgram);
        glUniform3i(blockPositionUniformLocation, this.castPos.x(), this.castPos.y(), this.castPos.z());
        
        glBindVertexArray(vao);

        float lineWidth = 2f;
        lineWidth = Math.max(LINE_MIN, Math.min(LINE_MAX, lineWidth));
        glLineWidth(lineWidth);

        glDrawArrays(GL_LINES, 0, 24);
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += 24;

        glLineWidth(1f);
        glBindVertexArray(0);
        glUseProgram(0);
    }

}
