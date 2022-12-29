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

import cientistavuador.ciencraftreal.chunk.Chunk;
import java.util.Arrays;

/**
 *
 * @author Cien
 */
public class SimpleBlock implements Block {

    private final int[] sideTextures = new int[6];
    private int blockId = -1;

    public SimpleBlock(int texture) {
        Arrays.fill(sideTextures, texture);
    }

    public SimpleBlock(int topTexture, int sideTexture, int bottomTexture) {
        this(sideTexture);
        sideTextures[BlockSide.TOP.index()] = topTexture;
        sideTextures[BlockSide.BOTTOM.index()] = bottomTexture;
    }

    public SimpleBlock(int topBottomTexture, int sideTexture) {
        this(topBottomTexture, sideTexture, topBottomTexture);
    }

    public SimpleBlock(
            int northTexture,
            int southTexture,
            int eastTexture,
            int westTexture,
            int topTexture,
            int bottomTexture
    ) {
        sideTextures[BlockSide.NORTH.index()] = northTexture;
        sideTextures[BlockSide.SOUTH.index()] = southTexture;
        sideTextures[BlockSide.EAST.index()] = eastTexture;
        sideTextures[BlockSide.WEST.index()] = westTexture;
        sideTextures[BlockSide.TOP.index()] = topTexture;
        sideTextures[BlockSide.BOTTOM.index()] = bottomTexture;
    }

    protected final void setBlockID(int id) {
        this.blockId = id;
    }
    
    protected final void setBlockSideTexture(BlockSide side, int texture) {
        if (side == null) {
            Arrays.fill(sideTextures, texture);
            return;
        }
        sideTextures[side.index()] = texture;
    }

    public int getBlockSideTexture(BlockSide side) {
        return sideTextures[side.index()];
    }

    @Override
    public BlockTransparency getBlockTransparency() {
        return BlockTransparency.SOLID;
    }

    @Override
    public int getId() {
        if (blockId == -1) {
            throw new RuntimeException("Block not Registered.");
        }
        return blockId;
    }

    @Override
    public float[] generateVertices(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        boolean[] sides = {
            containsBlock(chunk, chunkBlockX, chunkBlockY, chunkBlockZ - 1), //north
            containsBlock(chunk, chunkBlockX, chunkBlockY, chunkBlockZ + 1), //south
            containsBlock(chunk, chunkBlockX + 1, chunkBlockY, chunkBlockZ), //east
            containsBlock(chunk, chunkBlockX - 1, chunkBlockY, chunkBlockZ), //west
            containsBlock(chunk, chunkBlockX, chunkBlockY + 1, chunkBlockZ), //top
            containsBlock(chunk, chunkBlockX, chunkBlockY - 1, chunkBlockZ) //bottom
        };
        int verticesSize = 0;
        for (int i = 0; i < sides.length; i++) {
            verticesSize += (!sides[i] ? Chunk.RENDER_VERTEX_SIZE * 6 : 0);
        }

        if (verticesSize == 0) {
            return null;
        }

        float worldX = (chunkBlockX + (chunk.getChunkX() * Chunk.CHUNK_SIZE)) + 0.5f;
        float worldY = chunkBlockY + 0.5f;
        float worldZ = (chunkBlockZ + (chunk.getChunkZ() * Chunk.CHUNK_SIZE)) - 0.5f;
        
        float[] vertices = new float[verticesSize];
        int pos = 0;

        if (!sides[BlockSide.NORTH.index()]) {
            float[] sideVertices = {
                -0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.NORTH.index()]),
                0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.NORTH.index()]),
                -0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.NORTH.index()]),
                -0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.NORTH.index()]),
                0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.NORTH.index()]),
                0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.NORTH.index()])
            };
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }

        if (!sides[BlockSide.SOUTH.index()]) {
            float[] sideVertices = {
                -0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.SOUTH.index()]),
                -0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.SOUTH.index()]),
                0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.SOUTH.index()]),
                -0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.SOUTH.index()]),
                0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.SOUTH.index()]),
                0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.SOUTH.index()])
            };
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }

        if (!sides[BlockSide.EAST.index()]) {
            float[] sideVertices = {
                0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.EAST.index()]),
                0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.EAST.index()]),
                0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.EAST.index()]),
                0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.EAST.index()]),
                0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.EAST.index()]),
                0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.EAST.index()])
            };
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }

        if (!sides[BlockSide.WEST.index()]) {
            float[] sideVertices = {
                -0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.WEST.index()]),
                -0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.WEST.index()]),
                -0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.WEST.index()]),
                -0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.WEST.index()]),
                -0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.WEST.index()]),
                -0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.WEST.index()])
            };
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }

        if (!sides[BlockSide.TOP.index()]) {
            float[] sideVertices = {
                -0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.TOP.index()]),
                0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.TOP.index()]),
                -0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.TOP.index()]),
                -0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.TOP.index()]),
                0.5f + worldX, 0.5f + worldY, 0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.TOP.index()]),
                0.5f + worldX, 0.5f + worldY, -0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.TOP.index()])
            };
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }

        if (!sides[BlockSide.BOTTOM.index()]) {
            float[] sideVertices = {
                -0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 0f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.BOTTOM.index()]),
                -0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.BOTTOM.index()]),
                0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.BOTTOM.index()]),
                -0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 0f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.BOTTOM.index()]),
                0.5f + worldX, -0.5f + worldY, -0.5f + worldZ, 1f, 0f, Float.intBitsToFloat(sideTextures[BlockSide.BOTTOM.index()]),
                0.5f + worldX, -0.5f + worldY, 0.5f + worldZ, 1f, 1f, Float.intBitsToFloat(sideTextures[BlockSide.BOTTOM.index()])
            };
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
        }

        return vertices;
    }

    private boolean containsBlock(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        if (chunkBlockY < 0 || chunkBlockY >= Chunk.CHUNK_HEIGHT) {
            return false;
        }
        
        boolean needsOutOfBoundsCheck = (chunkBlockX < 0 || chunkBlockX >= Chunk.CHUNK_SIZE) || (chunkBlockZ > 0 || chunkBlockZ <= -Chunk.CHUNK_SIZE);
        
        if (needsOutOfBoundsCheck) {
            chunkBlockX += chunk.getChunkX() * Chunk.CHUNK_SIZE;
            chunkBlockZ += chunk.getChunkZ() * Chunk.CHUNK_SIZE;
            return chunk.getWorld().getWorldBlock(chunkBlockX, chunkBlockY, chunkBlockZ) != Blocks.AIR;
        }

        return chunk.getBlock(chunkBlockX, chunkBlockY, chunkBlockZ) != Blocks.AIR;
    }
}
