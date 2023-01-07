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
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.IndicesGenerator;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesCompressor;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesCreator;
import cientistavuador.ciencraftreal.util.OcclusionCube;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static final float TEX_COORDS_MAX = 10f;

    public static final int HEIGHT = 32;

    private final Chunk chunk;
    private final int y;
    private final Vector3d center;

    private boolean useCachedElimination = false;
    private boolean eliminate = false;

    private OcclusionCube occlusionCube = null;
    private Future<Map.Entry<short[], int[]>> futureVerticesIndices = null;
    private short[] vertices = null;
    private int[] indices = null;
    private int vao = 0;
    private int vbo = 0;

    public ChunkLayer(Chunk chunk, int y) {
        this.chunk = chunk;
        this.y = y;
        this.center = new Vector3d(
                chunk.getChunkX() + (Chunk.CHUNK_SIZE / 2.0),
                y + (HEIGHT / 2.0),
                chunk.getChunkZ() - (Chunk.CHUNK_SIZE / 2.0)
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

    public boolean eliminationStage0() {
        if (this.useCachedElimination) {
            return this.eliminate;
        }
        this.useCachedElimination = true;

        if (this.vertices != null && this.vertices.length == 0) {
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
        if (this.occlusionCube != null) {
            return false;
        }

        this.occlusionCube = new OcclusionCube();
        this.occlusionCube
                .getModel()
                .translate(
                        this.chunk.getChunkX() * Chunk.CHUNK_SIZE,
                        this.y,
                        this.chunk.getChunkZ() * Chunk.CHUNK_SIZE
                )
                .scale(Chunk.CHUNK_SIZE, ChunkLayer.HEIGHT, Chunk.CHUNK_SIZE);

        this.occlusionCube.tryRendering(camera);
        return true;
    }

    public boolean prepareVerticesStage2() {
        if (!this.occlusionCube.catchResult()) {
            return false;
        }

        this.futureVerticesIndices = CompletableFuture.supplyAsync(() -> {
            float[] verticesCreated = VerticesCreator.create(this);
            short[] compressedVertices = VerticesCompressor.compress(this, verticesCreated);
            return IndicesGenerator.generate(compressedVertices);
        });

        return true;
    }

    public boolean prepareVaoVboStage3() {
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

        //todo
        return true;
    }

    public void renderStage4(Camera camera) {
        //todo
    }

    public void delete() {
        if (this.occlusionCube != null) {
            this.occlusionCube.delete();
        }
        if (this.vao != 0) {
            glDeleteVertexArrays(this.vao);
        }
        if (this.vbo != 0) {
            glDeleteBuffers(this.vbo);
        }
        if (this.futureVerticesIndices != null && !this.futureVerticesIndices.isDone()) {
            try {
                this.futureVerticesIndices.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        this.useCachedElimination = false;
        this.eliminate = false;

        this.occlusionCube = null;
        this.futureVerticesIndices = null;
        this.vertices = null;
        this.indices = null;
        this.vao = 0;
        this.vbo = 0;
    }
}
