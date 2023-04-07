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
import cientistavuador.ciencraftreal.chunk.Chunk;
import java.util.Arrays;

/**
 *
 * @author Cien
 */
public class AmbientOcclusion {

    public static final float AO_STEP = 0.1f;
    
    public static final AmbientOcclusion NO_OCCLUSION = new AmbientOcclusion() {
        @Override
        public float getAO(int index) {
            return 0f;
        }

        @Override
        public void setBlock(Chunk chunk, int blockX, int blockY, int blockZ) {
            throw new UnsupportedOperationException("AO Default Null Object");
        }
    };

    public static final int UP_LEFT_SOUTH_INDEX = 0;
    public static final int UP_RIGHT_SOUTH_INDEX = 1;
    public static final int UP_RIGHT_NORTH_INDEX = 2;
    public static final int UP_LEFT_NORTH_INDEX = 3;
    public static final int DOWN_LEFT_SOUTH_INDEX = 4;
    public static final int DOWN_RIGHT_SOUTH_INDEX = 5;
    public static final int DOWN_RIGHT_NORTH_INDEX = 6;
    public static final int DOWN_LEFT_NORTH_INDEX = 7;

    // ):
    public static int getIndexByXYZ(boolean x, boolean y, boolean z) {
        if (y) {
            if (x) {
                if (z) {
                    return UP_RIGHT_SOUTH_INDEX;
                } else {
                    return UP_RIGHT_NORTH_INDEX;
                }
            } else {
                if (z) {
                    return UP_LEFT_SOUTH_INDEX;
                } else {
                    return UP_LEFT_NORTH_INDEX;
                }
            }
        } else {
            if (x) {
                if (z) {
                    return DOWN_RIGHT_SOUTH_INDEX;
                } else {
                    return DOWN_RIGHT_NORTH_INDEX;
                }
            } else {
                if (z) {
                    return DOWN_LEFT_SOUTH_INDEX;
                } else {
                    return DOWN_LEFT_NORTH_INDEX;
                }
            }
        }
    }
    
    private Chunk chunk;
    private int chunkBlockX;
    private int chunkBlockY;
    private int chunkBlockZ;
    private final float[] ao = new float[8];

    public AmbientOcclusion() {

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

    public Chunk getChunk() {
        return chunk;
    }

    public float getAO(int index) {
        return this.ao[index];
    }

    public void setBlock(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        this.chunk = chunk;
        this.chunkBlockX = chunkBlockX;
        this.chunkBlockY = chunkBlockY;
        this.chunkBlockZ = chunkBlockZ;
        Arrays.fill(this.ao, -1f);
    }

    private int isAOSolid(int offsetX, int offsetY, int offsetZ) {
        offsetX += this.chunkBlockX;
        offsetY += this.chunkBlockY;
        offsetZ += this.chunkBlockZ;
        if (offsetY < 0 || offsetY >= Chunk.CHUNK_HEIGHT) {
            return 0;
        }
        Block block = Chunk.getBlock(this.chunk, offsetX, offsetY, offsetZ);
        if (block == Blocks.AIR) {
            return 0;
        }
        return (block.isAOSolid() ? 1 : 0);
    }

    private float vertexAO(int side1, int side2, int corner) {
        if (side1 == 1 && side2 == 1) {
            return AO_STEP * 3;
        }
        return AO_STEP * (side1 + side2 + corner);
    }
    
    private float generateAOIndex(int index) {
        if (this.ao[index] != -1f) {
            return this.ao[index];
        }
        switch (index) {
            case UP_LEFT_SOUTH_INDEX -> {
                return vertexAO(isAOSolid(-1, 1, 0), isAOSolid(0, 1, 1), isAOSolid(-1, 1, 1));
            }
            case UP_RIGHT_SOUTH_INDEX -> {
                return vertexAO(isAOSolid(1, 1, 0), isAOSolid(0, 1, 1), isAOSolid(1, 1, 1));
            }
            case UP_RIGHT_NORTH_INDEX -> {
                return vertexAO(isAOSolid(1, 1, 0), isAOSolid(0, 1, -1), isAOSolid(1, 1, -1));
            }
            case UP_LEFT_NORTH_INDEX -> {
                return vertexAO(isAOSolid(-1, 1, 0), isAOSolid(0, 1, -1), isAOSolid(-1, 1, -1));
            }
            case DOWN_LEFT_SOUTH_INDEX -> {
                return vertexAO(isAOSolid(-1, -1, 0), isAOSolid(0, -1, 1), isAOSolid(-1, -1, 1));
            }
            case DOWN_RIGHT_SOUTH_INDEX -> {
                return vertexAO(isAOSolid(1, -1, 0), isAOSolid(0, -1, 1), isAOSolid(1, -1, 1));
            }
            case DOWN_RIGHT_NORTH_INDEX -> {
                return vertexAO(isAOSolid(1, -1, 0), isAOSolid(0, -1, -1), isAOSolid(1, -1, -1));
            }
            case DOWN_LEFT_NORTH_INDEX -> {
                return vertexAO(isAOSolid(-1, -1, 0), isAOSolid(0, -1, -1), isAOSolid(-1, -1, -1));
            }
        }
        return 0f;
    }

    public void generateAO(BlockSide side) {
        switch (side) {
            case TOP -> {
                this.ao[UP_LEFT_SOUTH_INDEX] = generateAOIndex(UP_LEFT_SOUTH_INDEX);
                this.ao[UP_RIGHT_SOUTH_INDEX] = generateAOIndex(UP_RIGHT_SOUTH_INDEX);
                this.ao[UP_RIGHT_NORTH_INDEX] = generateAOIndex(UP_RIGHT_NORTH_INDEX);
                this.ao[UP_LEFT_NORTH_INDEX] = generateAOIndex(UP_LEFT_NORTH_INDEX);
            }
            case BOTTOM -> {
                this.ao[DOWN_LEFT_SOUTH_INDEX] = generateAOIndex(DOWN_LEFT_SOUTH_INDEX);
                this.ao[DOWN_RIGHT_SOUTH_INDEX] = generateAOIndex(DOWN_RIGHT_SOUTH_INDEX);
                this.ao[DOWN_RIGHT_NORTH_INDEX] = generateAOIndex(DOWN_RIGHT_NORTH_INDEX);
                this.ao[DOWN_LEFT_NORTH_INDEX] = generateAOIndex(DOWN_LEFT_NORTH_INDEX);
            }
            case EAST -> {
                this.ao[UP_RIGHT_NORTH_INDEX] = generateAOIndex(UP_RIGHT_NORTH_INDEX);
                this.ao[UP_RIGHT_SOUTH_INDEX] = generateAOIndex(UP_RIGHT_SOUTH_INDEX);
                this.ao[DOWN_RIGHT_NORTH_INDEX] = generateAOIndex(DOWN_RIGHT_NORTH_INDEX);
                this.ao[DOWN_RIGHT_SOUTH_INDEX] = generateAOIndex(DOWN_RIGHT_SOUTH_INDEX);
            }
            case WEST -> {
                this.ao[UP_LEFT_NORTH_INDEX] = generateAOIndex(UP_LEFT_NORTH_INDEX);
                this.ao[UP_LEFT_SOUTH_INDEX] = generateAOIndex(UP_LEFT_SOUTH_INDEX);
                this.ao[DOWN_LEFT_NORTH_INDEX] = generateAOIndex(DOWN_LEFT_NORTH_INDEX);
                this.ao[DOWN_LEFT_SOUTH_INDEX] = generateAOIndex(DOWN_LEFT_SOUTH_INDEX);
            }
            case NORTH -> {
                this.ao[UP_LEFT_NORTH_INDEX] = generateAOIndex(UP_LEFT_NORTH_INDEX);
                this.ao[UP_RIGHT_NORTH_INDEX] = generateAOIndex(UP_RIGHT_NORTH_INDEX);
                this.ao[DOWN_LEFT_NORTH_INDEX] = generateAOIndex(DOWN_LEFT_NORTH_INDEX);
                this.ao[DOWN_RIGHT_NORTH_INDEX] = generateAOIndex(DOWN_RIGHT_NORTH_INDEX);
            }
            case SOUTH -> {
                this.ao[UP_LEFT_SOUTH_INDEX] = generateAOIndex(UP_LEFT_SOUTH_INDEX);
                this.ao[UP_RIGHT_SOUTH_INDEX] = generateAOIndex(UP_RIGHT_SOUTH_INDEX);
                this.ao[DOWN_LEFT_SOUTH_INDEX] = generateAOIndex(DOWN_LEFT_SOUTH_INDEX);
                this.ao[DOWN_RIGHT_SOUTH_INDEX] = generateAOIndex(DOWN_RIGHT_SOUTH_INDEX);
            }
        }
    }

}
