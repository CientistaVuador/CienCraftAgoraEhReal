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

import cientistavuador.ciencraftreal.world.WorldCamera;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Cien
 */
public class ChunkManager {

    public static final boolean OUTPUT_DEBUG_INFORMATION = false;

    private final Chunk chunk;
    private Future<Chunk> futureBlocksChunk = null;
    private Future<Chunk> futureVerticesChunk = null;
    private RenderableChunk renderableChunk = null;
    private boolean finished = false;
    private boolean blocksFinished = false;
    
    public ChunkManager(Chunk chunk) {
        Objects.requireNonNull(chunk, "Chunk is null");
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    public RenderableChunk getRenderableChunk() {
        return this.renderableChunk;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isBlocksFinished() {
        return blocksFinished;
    }

    public void update() {
        if (this.finished) {
            return;
        }

        if (this.futureBlocksChunk == null) {
            this.futureBlocksChunk = CompletableFuture.supplyAsync(() -> {
                long nanoHere = System.nanoTime();
                this.chunk.generateBlocks();
                if (OUTPUT_DEBUG_INFORMATION) {
                    System.out.println("Chunk at x: " + chunk.getChunkX() + ", z: " + chunk.getChunkZ() + " took " + String.format("%.3f", (System.nanoTime() - nanoHere) / 1E6d) + " ms to generate blocks.");
                }
                return this.chunk;
            });
            return;
        }

        if (this.futureVerticesChunk == null) {
            try {
                //if (!this.futureBlocksChunk.isDone()) {
                //    return;
                //}
                
                WorldCamera camera = this.chunk.getWorld();
                
                //camera.markForRegeneration(chunk.getChunkX() + 1, chunk.getChunkZ());
                //camera.markForRegeneration(chunk.getChunkX() - 1, chunk.getChunkZ());
                //camera.markForRegeneration(chunk.getChunkX(), chunk.getChunkZ() - 1);
                //camera.markForRegeneration(chunk.getChunkX(), chunk.getChunkZ() + 1);
                
                final Chunk c = this.futureBlocksChunk.get();
                if (2 == 2) {
                    this.blocksFinished = true;
                    return;
                }
                this.futureVerticesChunk = CompletableFuture.supplyAsync(() -> {
                    long nanoHere = System.nanoTime();
                    c.generateVertices();
                    if (OUTPUT_DEBUG_INFORMATION) {
                        System.out.println("Chunk at x: " + chunk.getChunkX() + ", z: " + chunk.getChunkZ() + " took " + String.format("%.3f", (System.nanoTime() - nanoHere) / 1E6d) + " ms to generate vertices.");
                    }
                    return c;
                });
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            this.blocksFinished = true;
            return;
        }

        if (this.renderableChunk == null) {
            try {
                if (!this.futureVerticesChunk.isDone()) {
                    return;
                }
                final Chunk c = this.futureVerticesChunk.get();
                if (OUTPUT_DEBUG_INFORMATION) {
                    System.out.println("Chunk at x: "+chunk.getChunkX()+", z: "+chunk.getChunkZ()+", total aprox size: "+chunk.getTotalApproximateSizeInBytes()+" bytes, vertices size: "+chunk.getVerticesSizeInBytes()+" bytes");
                }
                this.renderableChunk = new RenderableChunk(c);
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            this.finished = true;
            return;
        }
    }

    public void delete() {
        if (this.renderableChunk != null) {
            this.renderableChunk.delete();
        }
    }

}
