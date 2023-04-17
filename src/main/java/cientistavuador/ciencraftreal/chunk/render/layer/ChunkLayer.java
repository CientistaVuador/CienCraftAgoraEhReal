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

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesCreator;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesStream;
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

    //pos, normal, tex coords, tex id, ao
    public static final int VERTEX_SIZE_ELEMENTS = 3 + 2 + 1 + 1 + 1 + 1;
    public static final int TEX_COORDS_MAX = 10;

    public static final int HEIGHT = 32;

    private final Chunk chunk;
    private final int y;
    private final Vector3d center;

    private boolean useCachedElimination = false;
    private boolean empty = false;
    private boolean deleted = true;
    private boolean culled = false;

    private Future<VerticesStream> futureVertices = null;
    private short[] vertices = null;
    private int[] indices = null;
    private int vao = 0;
    private int vbo = 0;
    private int ebo = 0;

    private Future<VerticesStream> futureVerticesAlpha = null;
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

    public boolean isCulled() {
        return culled;
    }

    public boolean isEmpty() {
        return empty;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public boolean requiresUpdate(Camera camera) {
        this.deleted = false;
        if (this.futureVertices != null) {
            return true;
        }
        if (doPreElimination()) {
            return false;
        }
        double xMin = this.chunk.getChunkX() * Chunk.CHUNK_SIZE;
        double yMin = this.y;
        double zMin = this.chunk.getChunkZ() * Chunk.CHUNK_SIZE;
        this.culled = !camera.getProjectionView().testAab(
                xMin, yMin, zMin - Chunk.CHUNK_SIZE,
                xMin + Chunk.CHUNK_SIZE, yMin + HEIGHT, zMin
        );
        return !this.culled;
    }

    public void update() {
        if (this.vertices == null && this.futureVertices == null) {
            this.futureVertices = CompletableFuture.supplyAsync(() -> VerticesCreator.generateStream(this, false));
            this.futureVerticesAlpha = CompletableFuture.supplyAsync(() -> VerticesCreator.generateStream(this, true));
            return;
        }

        if (this.futureVertices != null && (this.futureVertices.isDone() && this.futureVerticesAlpha.isDone())) {
            glDeleteVertexArrays(this.vao);
            glDeleteBuffers(this.vbo);
            glDeleteBuffers(this.ebo);
            glDeleteVertexArrays(this.vaoAlpha);
            glDeleteBuffers(this.vboAlpha);
            glDeleteBuffers(this.eboAlpha);
            this.vao = 0;
            this.vbo = 0;
            this.ebo = 0;
            this.vaoAlpha = 0;
            this.vboAlpha = 0;
            this.eboAlpha = 0;

            prepareVaoVbo();
            prepareVaoVboAlpha();

            this.futureVertices = null;
            this.futureVerticesAlpha = null;
            return;
        }
    }

    public boolean render(boolean alpha) {
        if (this.culled) {
            return false;
        }
        if (alpha && (this.verticesAlpha == null || this.verticesAlpha.length == 0)) {
            return false;
        }
        if (!alpha && (this.vertices == null || this.vertices.length == 0)) {
            return false;
        }

        if (alpha) {
            glBindVertexArray(this.vaoAlpha);
            glDrawElements(GL_TRIANGLES, this.indicesAlpha.length, GL_UNSIGNED_INT, 0);
        } else {
            glBindVertexArray(this.vao);
            glDrawElements(GL_TRIANGLES, this.indices.length, GL_UNSIGNED_INT, 0);
        }
        glBindVertexArray(0);
        
        return true;
    }

    public void delete(boolean lazy) {
        if (this.deleted) {
            return;
        }

        this.useCachedElimination = false;
        this.deleted = true;

        if (lazy) {
            this.futureVertices = CompletableFuture.supplyAsync(() -> VerticesCreator.generateStream(this, false));
            this.futureVerticesAlpha = CompletableFuture.supplyAsync(() -> VerticesCreator.generateStream(this, true));
        } else {
            glDeleteVertexArrays(this.vao);
            glDeleteBuffers(this.vbo);
            glDeleteBuffers(this.ebo);

            this.futureVertices = null;
            this.vertices = null;
            this.indices = null;
            this.vao = 0;
            this.vbo = 0;
            this.ebo = 0;

            glDeleteVertexArrays(this.vaoAlpha);
            glDeleteBuffers(this.vboAlpha);
            glDeleteBuffers(this.eboAlpha);

            this.futureVerticesAlpha = null;
            this.verticesAlpha = null;
            this.indicesAlpha = null;
            this.vaoAlpha = 0;
            this.vboAlpha = 0;
            this.eboAlpha = 0;
        }
    }

    private boolean doPreElimination() {
        if (this.useCachedElimination) {
            return this.empty;
        }
        this.useCachedElimination = true;

        if ((this.vertices != null && this.vertices.length == 0) && (this.verticesAlpha != null && this.verticesAlpha.length == 0)) {
            this.empty = true;
            return this.empty;
        }
        if (this.chunk.getHighestY() < this.y) {
            this.empty = true;
            return this.empty;
        }
        for (int blockY = 0; blockY < ChunkLayer.HEIGHT; blockY++) {
            if (this.chunk.getAmountOfBlocksInY(blockY + this.y) != 0) {
                this.empty = false;
                return this.empty;
            }
        }
        this.empty = true;
        return this.empty;
    }

    private void setupVao() {
        //position, 3 of unsigned short (normalized)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_UNSIGNED_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, 0);

        //normal, 2 of short interpreted as int (normalized)
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_UNSIGNED_INT_2_10_10_10_REV, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3) * Short.BYTES);

        //texture coordinates, 2 of signed short (normalized)
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3 + 2) * Short.BYTES);

        //texture id, 1 of unsigned short (integer)
        glEnableVertexAttribArray(3);
        glVertexAttribIPointer(3, 1, GL_UNSIGNED_SHORT, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3 + 2 + 2) * Short.BYTES);

        //ao, 1 of unsigned short (normalized)
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 1, GL_UNSIGNED_SHORT, true, VERTEX_SIZE_ELEMENTS * Short.BYTES, (3 + 2 + 2 + 1) * Short.BYTES);
    }

    private void prepareVaoVbo() {
        VerticesStream result;
        try {
            result = this.futureVertices.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        this.vertices = result.vertices();
        this.indices = result.indices();

        if (this.vertices.length == 0) {
            return;
        }

        this.vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, this.vertices, GL_STATIC_DRAW);
        
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indices, GL_STATIC_DRAW);
        
        setupVao();

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void prepareVaoVboAlpha() {
        VerticesStream result;
        try {
            result = this.futureVerticesAlpha.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        this.verticesAlpha = result.vertices();
        this.indicesAlpha = result.indices();

        if (this.verticesAlpha.length == 0) {
            return;
        }

        this.vboAlpha = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.vboAlpha);
        glBufferData(GL_ARRAY_BUFFER, this.verticesAlpha, GL_STATIC_DRAW);
        
        this.vaoAlpha = glGenVertexArrays();
        glBindVertexArray(this.vaoAlpha);

        this.eboAlpha = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboAlpha);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indicesAlpha, GL_STATIC_DRAW);

        setupVao();

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
