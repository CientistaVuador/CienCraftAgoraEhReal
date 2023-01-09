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
package cientistavuador.ciencraftreal.chunk.render.layer.vertices;

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.chunk.Chunk;
import static cientistavuador.ciencraftreal.chunk.Chunk.CHUNK_SIZE;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayer;
import java.util.ArrayDeque;

/**
 *
 * @author Cien
 */
public class VerticesCreator {

    public static float[] create(ChunkLayer layer) {
        Chunk chunk = layer.getChunk();
        int yPos = layer.getY();
        
        ArrayDeque<float[]> blockVerticesQueue = new ArrayDeque<>(64);
        int size = 0;
        
        for (int y = (yPos + (ChunkLayer.HEIGHT - 1)); y >= yPos; y--) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int z = 0; z >= -(CHUNK_SIZE - 1); z--) {
                    Block block = chunk.getBlock(x, y, z);

                    if (block == Blocks.AIR) {
                        continue;
                    }

                    float[] blockVertices = block.generateVertices(
                            chunk,
                            x,
                            y,
                            z
                    );

                    if (blockVertices == null || blockVertices.length == 0) {
                        continue;
                    }

                    blockVerticesQueue.add(blockVertices);
                    size += blockVertices.length;
                }
            }
        }
        
        if (size == 0) {
            return new float[0];
        }
        
        float[] resultVertices = new float[size];
        int index = 0;
        
        float[] blockVertices;
        while ((blockVertices = blockVerticesQueue.poll()) != null) {
            System.arraycopy(blockVertices, 0, resultVertices, index, blockVertices.length);
            index += blockVertices.length;
        }
        
        return resultVertices;
    }

    private VerticesCreator() {

    }
}
