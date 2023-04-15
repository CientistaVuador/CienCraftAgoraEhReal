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
package cientistavuador.ciencraftreal.block;

import static cientistavuador.ciencraftreal.block.BlockSide.NORTH;
import static cientistavuador.ciencraftreal.block.BlockSide.SOUTH;
import cientistavuador.ciencraftreal.chunk.Chunk;
import java.util.Arrays;

/**
 *
 * @author Cien
 * https://0fps.net/2013/07/03/ambient-occlusion-for-minecraft-like-worlds/
 */
public class AmbientOcclusion {

    public static final float AO_STEP = 0.15f;
    
    public static final AmbientOcclusion NO_OCCLUSION = new AmbientOcclusion() {
        @Override
        public void setBlock(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
            throw new UnsupportedOperationException("No Occlusion Object");
        }

        @Override
        public void generateSideAO(BlockSide side) {
            throw new UnsupportedOperationException("No Occlusion Object");
        }

        @Override
        public float getSideVertexAO(boolean xPositive, boolean yPositive, boolean zPositive) {
            return 0f;
        }

        @Override
        public boolean generateFlippedQuad() {
            return false;
        }
    };
    
    private Chunk chunk;
    private int chunkBlockX;
    private int chunkBlockY;
    private int chunkBlockZ;
    private final int[] aoSolidCache = new int[3 * 3 * 3];
    private BlockSide side;
    private final float[] sideVertexAO = new float[4];
    private boolean generateFlippedQuad;

    public AmbientOcclusion() {
        Arrays.fill(this.aoSolidCache, -1);
    }

    public Chunk getChunk() {
        return chunk;
    }

    public int getChunkBlockX() {
        return chunkBlockX;
    }

    public int getChunkBlockY() {
        return chunkBlockY;
    }

    public int getChunkBlockZ() {
        return chunkBlockZ;
    }

    public BlockSide getSide() {
        return side;
    }

    public boolean generateFlippedQuad() {
        return this.generateFlippedQuad;
    }
    
    public void setBlock(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        this.chunk = chunk;
        this.chunkBlockX = chunkBlockX;
        this.chunkBlockY = chunkBlockY;
        this.chunkBlockZ = chunkBlockZ;
        Arrays.fill(this.aoSolidCache, -1);
        this.side = null;
        Arrays.fill(this.sideVertexAO, 0f);
    }

    private int isAOSolid(int offsetX, int offsetY, int offsetZ) {
        int cacheIndex = (offsetX + 1) + ((offsetY + 1) * 3) + ((offsetZ + 1) * 3 * 3);

        int cacheValue = this.aoSolidCache[cacheIndex];
        if (cacheValue != -1) {
            return cacheValue;
        }

        offsetX += this.chunkBlockX;
        offsetY += this.chunkBlockY;
        offsetZ += this.chunkBlockZ;

        if (offsetY < 0 || offsetY >= Chunk.CHUNK_HEIGHT) {
            this.aoSolidCache[cacheIndex] = 0;
            return 0;
        }

        Block block = Chunk.getBlock(this.chunk, offsetX, offsetY, offsetZ);
        if (block == Blocks.AIR || !block.isAOSolid()) {
            this.aoSolidCache[cacheIndex] = 0;
            return 0;
        }
        
        this.aoSolidCache[cacheIndex] = 1;
        return 1;
    }
    
    private float vertexAO(int side1, int side2, int corner) {
        int aoLevel;
        if (side1 == 1 && side2 == 1) {
            aoLevel = 3;
        } else {
            aoLevel = (side1 + side2 + corner);
        }
        return aoLevel * AO_STEP;
    }
    
    private boolean shouldFlipQuad(float a00, float a01, float a11, float a10) {
        return !((a00 + a11) > (a01 + a10));
    }
    
    public void generateSideAO(BlockSide side) {
        this.side = side;
        switch (side) {
            case NORTH, SOUTH -> {
                int z = (side.equals(BlockSide.NORTH) ? -1 : 1);
                this.sideVertexAO[0] = vertexAO(isAOSolid(-1, 0, z), isAOSolid(0, -1, z), isAOSolid(-1, -1, z));
                this.sideVertexAO[1] = vertexAO(isAOSolid(1, 0, z), isAOSolid(0, -1, z), isAOSolid(1, -1, z));
                this.sideVertexAO[2] = vertexAO(isAOSolid(1, 0, z), isAOSolid(0, 1, z), isAOSolid(1, 1, z));
                this.sideVertexAO[3] = vertexAO(isAOSolid(-1, 0, z), isAOSolid(0, 1, z), isAOSolid(-1, 1, z));
            }
            case EAST, WEST -> {
                int x = (side.equals(BlockSide.WEST) ? -1 : 1);
                this.sideVertexAO[0] = vertexAO(isAOSolid(x, -1, 0), isAOSolid(x, 0, -1), isAOSolid(x, -1, -1));
                this.sideVertexAO[1] = vertexAO(isAOSolid(x, -1, 0), isAOSolid(x, 0, 1), isAOSolid(x, -1, 1));
                this.sideVertexAO[2] = vertexAO(isAOSolid(x, 1, 0), isAOSolid(x, 0, 1), isAOSolid(x, 1, 1));
                this.sideVertexAO[3] = vertexAO(isAOSolid(x, 1, 0), isAOSolid(x, 0, -1), isAOSolid(x, 1, -1));
            }
            case TOP, BOTTOM -> {
                int y = (side.equals(BlockSide.BOTTOM) ? -1 : 1);
                this.sideVertexAO[0] = vertexAO(isAOSolid(-1, y, 0), isAOSolid(0, y, 1), isAOSolid(-1, y, 1));
                this.sideVertexAO[1] = vertexAO(isAOSolid(1, y, 0), isAOSolid(0, y, 1), isAOSolid(1, y, 1));
                this.sideVertexAO[2] = vertexAO(isAOSolid(1, y, 0), isAOSolid(0, y, -1), isAOSolid(1, y, -1));
                this.sideVertexAO[3] = vertexAO(isAOSolid(-1, y, 0), isAOSolid(0, y, -1), isAOSolid(-1, y, -1));
            }
            default -> throw new UnsupportedOperationException("Unsupported Side "+side);
        }
        this.generateFlippedQuad = shouldFlipQuad(this.sideVertexAO[0], this.sideVertexAO[3], this.sideVertexAO[2], this.sideVertexAO[1]);
    }
    
    public float getSideVertexAO(boolean xPositive, boolean yPositive, boolean zPositive) {
        switch (this.side) {
            case NORTH, SOUTH -> {
                if (!xPositive && !yPositive) return this.sideVertexAO[0];
                if (xPositive && !yPositive) return this.sideVertexAO[1];
                if (xPositive && yPositive) return this.sideVertexAO[2];
                if (!xPositive && yPositive) return this.sideVertexAO[3];
            }
            case EAST, WEST -> {
                if (!yPositive && !zPositive) return this.sideVertexAO[0];
                if (!yPositive && zPositive) return this.sideVertexAO[1];
                if (yPositive && zPositive) return this.sideVertexAO[2];
                if (yPositive && !zPositive) return this.sideVertexAO[3];
            }
            case TOP, BOTTOM -> {
                if (!xPositive && zPositive) return this.sideVertexAO[0];
                if (xPositive && zPositive) return this.sideVertexAO[1];
                if (xPositive && !zPositive) return this.sideVertexAO[2];
                if (!xPositive && !zPositive) return this.sideVertexAO[3];
            }
        }
        throw new UnsupportedOperationException("Unsupported Side "+side);
    }
}
