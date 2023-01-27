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
package cientistavuador.ciencraftreal.chunk.render.layer;

import cientistavuador.ciencraftreal.block.BlockTextures;
import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.shaders.ChunkLayerShaderProgram;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.IndicesGenerator;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesCompressor;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesCreator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ChunkLayer {

    //pos, tex coords, tex id, ao, unused 
    public static final int VERTEX_SIZE_ELEMENTS = 3 + 2 + 1 + 1 + 1;
    public static final int TEX_COORDS_MAX = 10;

    public static final int HEIGHT = 32;

    private final Chunk chunk;
    private final int y;
    private final Vector3d center;

    private boolean useCachedElimination = false;
    private boolean eliminate = false;
    private boolean deleted = true;

    private Future<Map.Entry<short[], int[]>> futureVerticesIndices = null;
    private short[] vertices = null;
    private int[] indices = null;
    private int vao = 0;
    private int vbo = 0;
    private int ebo = 0;
    
    private Future<Map.Entry<short[], int[]>> futureVerticesIndicesAlpha = null;
    private short[] verticesAlpha = null;
    private int[] indicesAlpha = null;
    private int vaoAlpha = 0;
    private int vboAlpha = 0;
    private int eboAlpha = 0;

    public ChunkLayer(Chunk chunk, int y) {
        this.chunk = chunk;
        this.y = y;
        this.center = new Vector3d(
                (chunk.getChunkX() * Chunk.CHUNK_SIZE) + (Chunk.CHUNK_SIZE / 2.0),
                y + (HEIGHT / 2.0),
                (chunk.getChunkZ() * Chunk.CHUNK_SIZE) - (Chunk.CHUNK_SIZE / 2.0)
        );
    }

    public short[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public int getY() {
        return this.y;
    }

    public Vector3dc getCenter() {
        return center;
    }

    public boolean cullingStage0(Camera camera) {
        this.deleted = false;
        if (doPreElimination()) {
            return false;
        }
        double xMin = this.chunk.getChunkX() * Chunk.CHUNK_SIZE;
        double yMin = this.y;
        double zMin = this.chunk.getChunkZ() * Chunk.CHUNK_SIZE;
        return camera.getProjectionView().testAab(
                xMin, yMin, zMin - Chunk.CHUNK_SIZE,
                xMin + Chunk.CHUNK_SIZE, yMin + HEIGHT, zMin
        );
    }

    private boolean doPreElimination() {
        if (this.useCachedElimination) {
            return this.eliminate;
        }
        this.useCachedElimination = true;

        if ((this.vertices != null && this.vertices.length == 0) && (this.verticesAlpha != null && this.verticesAlpha.length == 0)) {
            this.eliminate = true;
            return this.eliminate;
        }
        if (this.chunk.getHighestY() < this.y) {
            this.eliminate = true;
            return this.eliminate;
        }
        for (int blockY = 0; blockY < ChunkLayer.HEIGHT; blockY++) {
            if (this.chunk.getAmountOfBlocksInY(blockY + this.y) != 0) {
                this.eliminate = false;
                return this.eliminate;
            }
        }
        this.eliminate = true;
        return this.eliminate;
    }

    public boolean checkVerticesStage1(Camera camera) {
        return !(this.vertices != null && this.verticesAlpha != null);
    }
    
    public boolean prepareVerticesStage2() {
        this.futureVerticesIndices = CompletableFuture.supplyAsync(() -> {
            float[] verticesCreated = VerticesCreator.create(this, false);
            short[] compressedVertices = VerticesCompressor.compress(this, verticesCreated);
            Map.Entry<short[], int[]> indicesVertices = IndicesGenerator.generate(compressedVertices);
            return indicesVertices;
        });
        
        this.futureVerticesIndicesAlpha = CompletableFuture.supplyAsync(() -> {
            float[] verticesCreated = VerticesCreator.create(this, true);
            short[] compressedVertices = VerticesCompressor.compress(this, verticesCreated);
            Map.Entry<short[], int[]> indicesVertices = IndicesGenerator.generate(compressedVertices);
            return indicesVertices;
        });

        return true;
    }

    private void setupVao() {
        //position, 3 of unsigned short (normalized)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_UNSIGNED_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, 0);

        //texture coordinates, 2 of signed short (normalized)
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, 3 * Short.BYTES);

        //texture id, 1 of unsigned short (integer)
        glEnableVertexAttribArray(2);
        glVertexAttribIPointer(2, 1, GL_UNSIGNED_SHORT, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3 + 2) * Short.BYTES);

        //ao, 1 of unsigned short (normalized)
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 1, GL_UNSIGNED_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3 + 2 + 1) * Short.BYTES);

        //unused, 1 of unsigned short (normalized)
        //glEnableVertexAttribArray(4);
        //glVertexAttribPointer(4, 1, GL_UNSIGNED_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3 + 2 + 1 + 1) * Short.BYTES);
    }
    
    private boolean prepareVaoVbo() {
        Map.Entry<short[], int[]> result;
        try {
            result = this.futureVerticesIndices.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        this.vertices = result.getKey();
        this.indices = result.getValue();

        if (this.vertices.length == 0) {
            return false;
        }

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indices, GL_STATIC_DRAW);

        this.vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, this.vertices, GL_STATIC_DRAW);
        
        setupVao();
        
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return true;
    }
    
    private boolean prepareVaoVboAlpha() {
        Map.Entry<short[], int[]> result;
        try {
            result = this.futureVerticesIndicesAlpha.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        this.verticesAlpha = result.getKey();
        this.indicesAlpha = result.getValue();
        
        if (this.verticesAlpha.length == 0) {
            return false;
        }
        
        this.vaoAlpha = glGenVertexArrays();
        glBindVertexArray(this.vaoAlpha);

        this.eboAlpha = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboAlpha);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indicesAlpha, GL_STATIC_DRAW);

        this.vboAlpha = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vboAlpha);
        glBufferData(GL_ARRAY_BUFFER, this.verticesAlpha, GL_STATIC_DRAW);
        
        setupVao();
        
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return true;
    }
    
    public boolean prepareVaoVboStage3() {
        boolean a = prepareVaoVbo();
        boolean b = prepareVaoVboAlpha();
        return a || b;
    }

    public void renderStage4(Camera camera) {
        renderStage4(camera, false);
    }
    
    public void renderStage4(Camera camera, boolean useCurrentShader) {
        if (this.vertices.length == 0) {
            return;
        }
        
        if (!useCurrentShader) {
            glUseProgram(ChunkLayerShaderProgram.SHADER_PROGRAM);
            
            ChunkLayerShaderProgram.sendCameraUniforms(camera);
        }

        ChunkLayerShaderProgram.sendUniforms(
                this.chunk.getChunkX(),
                this.y,
                this.chunk.getChunkZ(),
                false
        );

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockTextures.GL_TEXTURE_ARRAY);
        
        glBindVertexArray(this.vao);

        glDrawElements(GL_TRIANGLES, this.indices.length, GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glBindVertexArray(0);
        
        if (!useCurrentShader) {
            glUseProgram(0);
        }
    }
    
    public void renderAlphaStage5(Camera camera) {
        renderAlphaStage5(camera, false);
    }
    
    public void renderAlphaStage5(Camera camera, boolean useCurrentShader) {
        if (this.verticesAlpha.length == 0) {
            return;
        }
        
        if (!useCurrentShader) {
            glUseProgram(ChunkLayerShaderProgram.SHADER_PROGRAM);
            
            ChunkLayerShaderProgram.sendCameraUniforms(camera);
        }

        ChunkLayerShaderProgram.sendUniforms(
                this.chunk.getChunkX(),
                this.y,
                this.chunk.getChunkZ(),
                true
        );

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockTextures.GL_TEXTURE_ARRAY);
        
        glBindVertexArray(this.vaoAlpha);

        glDrawElements(GL_TRIANGLES, this.indicesAlpha.length, GL_UNSIGNED_INT, 0);

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glBindVertexArray(0);
        
        if (!useCurrentShader) {
            glUseProgram(0);
        }
    }

    public boolean isDeleted() {
        return deleted;
    }
    
    public void delete() {
        if (this.deleted) {
            return;
        }
        this.useCachedElimination = false;
        this.eliminate = false;
        
        if (this.vao != 0) {
            glDeleteVertexArrays(this.vao);
        }
        if (this.vbo != 0) {
            glDeleteBuffers(this.vbo);
        }
        if (this.ebo != 0) {
            glDeleteBuffers(this.ebo);
        }

        this.futureVerticesIndices = null;
        this.vertices = null;
        this.indices = null;
        this.vao = 0;
        this.vbo = 0;
        this.ebo = 0;
        
        if (this.vaoAlpha != 0) {
            glDeleteVertexArrays(this.vaoAlpha);
        }
        if (this.vboAlpha != 0) {
            glDeleteBuffers(this.vboAlpha);
        }
        if (this.eboAlpha != 0) {
            glDeleteBuffers(this.eboAlpha);
        }
        
        this.futureVerticesIndicesAlpha = null;
        this.verticesAlpha = null;
        this.indicesAlpha = null;
        this.vaoAlpha = 0;
        this.vboAlpha = 0;
        this.eboAlpha = 0;
        
        this.deleted = true;
    }
}
