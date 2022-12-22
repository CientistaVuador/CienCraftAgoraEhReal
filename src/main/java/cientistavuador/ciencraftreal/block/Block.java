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
public class Block {
    
    private final int[] sideTextures = new int[6];
    private int blockId = -1;
    
    public Block(int texture) {
        Arrays.fill(sideTextures, texture);
    }
    
    public Block(int topTexture, int sideTexture, int bottomTexture) {
        this(sideTexture);
        sideTextures[BlockSide.TOP.index()] = topTexture;
        sideTextures[BlockSide.BOTTOM.index()] = bottomTexture;
    }
    
    public Block(int topBottomTexture, int sideTexture) {
        this(topBottomTexture, sideTexture, topBottomTexture);
    }
    
    public Block(
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
    
    public BlockTransparency getBlockTransparency() {
        return BlockTransparency.SOLID;
    }
    
    public int getId() {
        if (blockId == -1) {
            for (int i = 0; i < BlockRegister.numberOfRegisteredBlocks(); i++) {
                if (BlockRegister.getBlock(i) == this) {
                    blockId = i;
                    return blockId;
                }
            }
            throw new RuntimeException("Block not Registered.");
        }
        return blockId;
    }
    
    public float[] generateVertices(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        return null;
    }
}
