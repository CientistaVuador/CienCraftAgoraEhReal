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
import cientistavuador.ciencraftreal.player.Player;
import org.joml.Intersectiond;

/**
 *
 * @author Cien
 */
public interface Block {
    //pos, tex coords, tex id, ao
    public static final int VERTEX_SIZE_ELEMENTS = 3 + 2 + 1 + 1;
    
    String getName();
    int getId();
    void setId(int id);
    float[] generateVertices(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ);
    BlockTransparency getBlockTransparency();
    default StateOfMatter getStateOfMatter() {
        return StateOfMatter.SOLID;
    }
    default boolean checkCollision(int blockX, int blockY, int blockZ, Player player) {
        double x0 = blockX;
        double y0 = blockY;
        double z0 = blockZ - 1;
        double x1 = blockX + 1;
        double y1 = blockY + 1;
        double z1 = blockZ;
        return Intersectiond.testAabAab(
                x0,
                y0,
                z0,
                x1,
                y1,
                z1,
                player.getMin().x(),
                player.getMin().y(),
                player.getMin().z(),
                player.getMax().x(),
                player.getMax().y(),
                player.getMax().z()
        );
    }
}
