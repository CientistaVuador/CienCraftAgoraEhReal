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
import cientistavuador.ciencraftreal.world.WorldCamera;
import java.nio.FloatBuffer;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class BlockOutline {

    public static final float LINE_WIDTH = 3f;
    public static final float LINE_MIN;
    public static final float LINE_MAX;
    
    static {
        int[] lineMinMax = new int[2];
        glGetIntegerv(GL_ALIASED_LINE_WIDTH_RANGE, lineMinMax);
        
        LINE_MIN = lineMinMax[0];
        LINE_MAX = lineMinMax[1];
    }
    
    private static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform mat4 view;
            uniform mat4 projection;
            
            uniform vec3 blockPosition;
            
            layout (location = 0) in vec3 vertexPosition;
            
            void main() {
                gl_Position = projection * view * vec4(vertexPosition + blockPosition, 1.0);
            }
            """;
    
    private static final String FRAGMENT_SHADER = 
            """
            #version 330 core
            
            layout (location = 0) out vec4 fragColor;
            
            void main() {
                fragColor = vec4(0.2, 0.2, 0.3, 1.0);
            }
            """;
    
    private static final int shaderProgram = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final int vao = glGenVertexArrays();
    
    static {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
            //sides
            0f, 0f, 0f,
            0f, 1f, 0f,
            1f, 0f, 0f,
            1f, 1f, 0f,
            0f, 0f, -1f,
            0f, 1f, -1f,
            1f, 0f, -1f,
            1f, 1f, -1f,
            //top
            0f, 1f, 0f,
            0f, 1f, -1f,
            1f, 1f, 0f,
            1f, 1f, -1f,
            0f, 1f, 0f,
            1f, 1f, 0f,
            0f, 1f, -1f,
            1f, 1f, -1f,
            //bottom
            0f, 0f, 0f,
            0f, 0f, -1f,
            1f, 0f, 0f,
            1f, 0f, -1f,
            0f, 0f, 0f,
            1f, 0f, 0f,
            0f, 0f, -1f,
            1f, 0f, -1f,
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
    private final Vector3f castVector = new Vector3f();

    private int castPosX = 0;
    private int castPosY = 0;
    private int castPosZ = 0;
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

    public int getCastPosX() {
        return castPosX;
    }

    public int getCastPosY() {
        return castPosY;
    }

    public int getCastPosZ() {
        return castPosZ;
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

            this.castPosX = (int) Math.floor(castVector.x() + camera.getPosition().x());
            this.castPosY = (int) Math.floor(castVector.y() + camera.getPosition().y());
            this.castPosZ = (int) Math.ceil(castVector.z() + camera.getPosition().z());

            if (ignoreFirst
                    && lastCastPosX == this.castPosX
                    && lastCastPosY == this.castPosY
                    && lastCastPosZ == this.castPosZ) {
                continue;
            }
            
            ignoreFirst = true;
            lastCastPosX = this.castPosX;
            lastCastPosY = this.castPosY;
            lastCastPosZ = this.castPosZ;

            this.block = this.world.getWorldBlock(this.castPosX, this.castPosY, this.castPosZ);

            if (this.block != Blocks.AIR) {
                break;
            }
        }
    }

    public void render() {
        if (this.block == Blocks.AIR) {
            return;
        }
        
        glUseProgram(shaderProgram);
        
        int viewUniformLocation = glGetUniformLocation(shaderProgram, "view");
        int projectionUniformLocation = glGetUniformLocation(shaderProgram, "projection");
        int blockPositionUniformLocation = glGetUniformLocation(shaderProgram, "blockPosition");
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.callocFloat(4 * 4);
            
            camera.getView().get(matrixBuffer);
            glUniformMatrix4fv(viewUniformLocation, false, matrixBuffer);
            
            camera.getProjection().get(matrixBuffer);
            glUniformMatrix4fv(projectionUniformLocation, false, matrixBuffer);
            
            glUniform3f(blockPositionUniformLocation, this.castPosX, this.castPosY, this.castPosZ);
        }
        glBindVertexArray(vao);
        
        float lineWidth = 2f;
        lineWidth = Math.max(LINE_MIN, Math.min(LINE_MAX, lineWidth));
        glLineWidth(lineWidth);
        
        glDrawArrays(GL_LINES, 0, 24);
        
        glLineWidth(1f);
        glBindVertexArray(0);
        glUseProgram(0);
        
        Main.checkGLError();
    }

}
