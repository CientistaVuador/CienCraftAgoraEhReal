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
public abstract class SimpleBlock implements Block, SolidBlockCheck {

    private final String name;
    private final int[] sideTextures = new int[6];
    private int blockId = -1;

    public SimpleBlock(String name, int texture) {
        this.name = name;
        Arrays.fill(sideTextures, texture);
    }

    public SimpleBlock(String name, int topTexture, int sideTexture, int bottomTexture) {
        this(name, sideTexture);
        sideTextures[BlockSide.TOP.index()] = topTexture;
        sideTextures[BlockSide.BOTTOM.index()] = bottomTexture;
    }

    public SimpleBlock(String name, int topBottomTexture, int sideTexture) {
        this(name, topBottomTexture, sideTexture, topBottomTexture);
    }

    public SimpleBlock(
            String name,
            int northTexture,
            int southTexture,
            int eastTexture,
            int westTexture,
            int topTexture,
            int bottomTexture
    ) {
        this.name = name;
        sideTextures[BlockSide.NORTH.index()] = northTexture;
        sideTextures[BlockSide.SOUTH.index()] = southTexture;
        sideTextures[BlockSide.EAST.index()] = eastTexture;
        sideTextures[BlockSide.WEST.index()] = westTexture;
        sideTextures[BlockSide.TOP.index()] = topTexture;
        sideTextures[BlockSide.BOTTOM.index()] = bottomTexture;
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
    public void setId(int id) {
        this.blockId = id;
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
        boolean[] sides = isSolidBlockSides(chunk, chunkBlockX, chunkBlockY, chunkBlockZ);
        
        int verticesSize = 0;
        for (int i = 0; i < sides.length; i++) {
            verticesSize += (!sides[i] ? Block.VERTEX_SIZE_ELEMENTS * 6 : 0);
        }

        if (verticesSize == 0) {
            return null;
        }

        float chunkX = chunkBlockX + 0.5f;
        float chunkY = chunkBlockY + 0.5f;
        float chunkZ = chunkBlockZ - 0.5f;
        
        float[] vertices = new float[verticesSize];
        int pos = 0;

        for (int i = 0; i < sides.length; i++) {
            if (sides[i]) {
                continue;
            }
            BlockSide side = BlockSide.sideOf(i);
            float[] sideVertices = BlockFacesVertices.generateFaceVertices(side, chunkX, chunkY, chunkZ, sideTextures[i]);
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }
        
        return vertices;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleBlock other = (SimpleBlock) obj;
        return this.getId() == other.getId();
    }
    
}
