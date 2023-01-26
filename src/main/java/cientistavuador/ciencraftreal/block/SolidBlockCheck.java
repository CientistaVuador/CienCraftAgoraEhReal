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

/**
 *
 * @author Cien
 */
public interface SolidBlockCheck {
    public default boolean isSolidBlock(Block block) {
        return block != Blocks.AIR && !block.getBlockTransparency().isTransparent();
    }
    public default boolean isSolidBlock(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        if (chunkBlockY < 0 || chunkBlockY >= Chunk.CHUNK_HEIGHT) {
            return false;
        }
        Block block = Chunk.getBlock(chunk, chunkBlockX, chunkBlockY, chunkBlockZ);
        return isSolidBlock(block);
    }
    public default boolean[] isSolidBlockSides(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        return new boolean[] {
            isSolidBlock(chunk, chunkBlockX, chunkBlockY, chunkBlockZ - 1), //north
            isSolidBlock(chunk, chunkBlockX, chunkBlockY, chunkBlockZ + 1), //south
            isSolidBlock(chunk, chunkBlockX + 1, chunkBlockY, chunkBlockZ), //east
            isSolidBlock(chunk, chunkBlockX - 1, chunkBlockY, chunkBlockZ), //west
            isSolidBlock(chunk, chunkBlockX, chunkBlockY + 1, chunkBlockZ), //top
            isSolidBlock(chunk, chunkBlockX, chunkBlockY - 1, chunkBlockZ) //bottom
        };
    }
}
