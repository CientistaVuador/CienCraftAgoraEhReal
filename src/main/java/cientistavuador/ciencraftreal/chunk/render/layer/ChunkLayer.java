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

import cientistavuador.ciencraftreal.chunk.Chunk;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 *
 * @author Cien
 */
public class ChunkLayer {
    public static final int HEIGHT = 32;
    
    private final Chunk chunk;
    private final int y;
    private final Vector3d center;
    
    private boolean useCachedElimination = false;
    private boolean eliminate = false;
    private boolean markedForRegeneration = true;
    
    public ChunkLayer(Chunk chunk, int y) {
        this.chunk = chunk;
        this.y = y;
        this.center = new Vector3d(
                chunk.getChunkX() + (Chunk.CHUNK_SIZE / 2.0),
                y + (HEIGHT / 2.0),
                chunk.getChunkZ() - (Chunk.CHUNK_SIZE / 2.0)
        );
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

    public boolean isMarkedForRegeneration() {
        return markedForRegeneration;
    }
    
    public void markForRegeneration() {
        this.markedForRegeneration = true;
        this.useCachedElimination = false;
    }
    
    public boolean eliminationStage0() {
        if (this.useCachedElimination) {
            return this.eliminate;
        }
        this.useCachedElimination = true;
        
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
    
    public void prepareVerticesStage1() {
        //todo
    }
    
    public void prepareRenderStage2() {
        //todo
    }
    
    public void renderStage3() {
        //todo
    }
    
    public void delete() {
        //todo
    }
}
