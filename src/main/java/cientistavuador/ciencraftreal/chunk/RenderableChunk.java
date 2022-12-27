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
package cientistavuador.ciencraftreal.chunk;

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.block.BlockTextures;
import cientistavuador.ciencraftreal.util.ProgramCompiler;
import java.nio.FloatBuffer;
import org.joml.Matrix4fc;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class RenderableChunk {

    public static final double MAX_TIME_SPENT_SENDING_CHUNKS_PER_FRAME = 1.0/120.0;
    
    private static final String VERTEX_SHADER_SOURCE
            = """
            #version 330 core
            
            uniform mat4 projection;
            uniform mat4 view;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec2 vertexTexCoords;
            layout (location = 2) in int vertexTextureID;
            
            out vec2 inout_TexCoords;
            flat out int inout_TextureID;
            
            void main() {
                gl_Position = projection * view * vec4(vertexPosition, 1.0);
                inout_TexCoords = vertexTexCoords;
                inout_TextureID = vertexTextureID;
            }
            """;

    private static final String FRAGMENT_SHADER_SOURCE
            = """
            #version 330 core
            
            uniform sampler2DArray blockTextures;
            
            in vec2 inout_TexCoords;
            flat in int inout_TextureID;
            
            layout (location = 0) out vec4 out_Color;
            
            void main() {
                out_Color = texture(blockTextures, vec3(inout_TexCoords, float(inout_TextureID)));
            }
            """;

    private static final int shaderProgram = ProgramCompiler.compile(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
    private static double chunkUploadTimeCounter = 0;
    private static long currentFrame = Main.FRAME;
    
    private final Chunk chunk;
    private final int vao = glGenVertexArrays();
    private final int vbo = glGenBuffers();

    private int amountOfVertices = 0;
    private boolean initialized = false;
    private boolean deleted = false;

    public RenderableChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }

    private void initialize() {
        float[] vertices = this.chunk.getVertices();
        this.amountOfVertices = vertices.length / Chunk.RENDER_VERTEX_SIZE;

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);

        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);

        glEnableVertexAttribArray(2);
        glVertexAttribIPointer(2, 1, GL_INT, 6 * Float.BYTES, 5 * Float.BYTES);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render(Matrix4fc projection, Matrix4fc view) {
        if (this.deleted) {
            throw new IllegalStateException("Deleted Chunk");
        }
        if (!this.initialized) {
            if (currentFrame != Main.FRAME) {
                chunkUploadTimeCounter = 0;
                currentFrame = Main.FRAME;
            }
            if (chunkUploadTimeCounter >= MAX_TIME_SPENT_SENDING_CHUNKS_PER_FRAME) {
                return;
            }
            long nanoHere = System.nanoTime();
            this.initialized = true;
            initialize();
            chunkUploadTimeCounter += (System.nanoTime() - nanoHere) / 1E9d;
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockTextures.GL_TEXTURE_ARRAY);

        glUseProgram(shaderProgram);
        glBindVertexArray(this.vao);

        int projectionUniformLocation = glGetUniformLocation(shaderProgram, "projection");
        int viewUniformLocation = glGetUniformLocation(shaderProgram, "view");
        int texturesUniformLocation = glGetUniformLocation(shaderProgram, "blockTextures");

        glUniform1i(texturesUniformLocation, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(4 * 4);

            projection.get(matrixBuffer);
            glUniformMatrix4fv(projectionUniformLocation, false, matrixBuffer);

            view.get(matrixBuffer);
            glUniformMatrix4fv(viewUniformLocation, false, matrixBuffer);
        }

        glDrawArrays(GL_TRIANGLES, 0, this.amountOfVertices);
        
        glBindVertexArray(0);
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        Main.checkGLError();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        if (this.deleted) {
            throw new IllegalStateException("Deleted Chunk");
        }
        this.deleted = true;

        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);

        Main.checkGLError();
    }

}
