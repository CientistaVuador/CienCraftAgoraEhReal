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
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayer;

/**
 *
 * @author Cien
 */
public class VerticesCompressor {

    private static short floatToShort(float f) {
        return (short) (int) (f * ((Short.MAX_VALUE * 2) + 1));
    }
    
    private static short floatToShortSigned(float f) {
        if (f < 0) {
            return (short) (int) (f * Short.MIN_VALUE);
        }
        return (short) (int) (f * Short.MAX_VALUE);
    }

    public static short[] compress(ChunkLayer layer, float[] vertices) {
        if (vertices.length == 0) {
            return new short[0];
        }
        
        Chunk chunk = layer.getChunk();
        
        final int[] offsets = {
            chunk.getChunkX() * Chunk.CHUNK_SIZE,
            layer.getY(),
            chunk.getChunkZ() * Chunk.CHUNK_SIZE
        };
        
        final float[] sizes = {
            Chunk.CHUNK_SIZE,
            ChunkLayer.HEIGHT,
            Chunk.CHUNK_SIZE
        };

        int amountOfVertices = vertices.length / Block.VERTEX_SIZE_ELEMENTS;
        short[] outputVertices = new short[ChunkLayer.VERTEX_SIZE_ELEMENTS * amountOfVertices];

        for (int i = 0; i < amountOfVertices; i++) {
            int vertexIndex = i * Block.VERTEX_SIZE_ELEMENTS;
            int outputIndex = i * ChunkLayer.VERTEX_SIZE_ELEMENTS;

            for (int j = 0; j < Block.VERTEX_SIZE_ELEMENTS; j++) {
                float value = vertices[vertexIndex];
                short output = 0;

                processValue:
                {
                    if (j < 3) {
                        value = Math.abs(value - offsets[j]) / sizes[j];
                        output = floatToShort(value);
                        break processValue;
                    }
                    if (j < 5) {
                        value /= ChunkLayer.TEX_COORDS_MAX;
                        output = floatToShortSigned(value);
                        break processValue;
                    }
                    if (j < 6) {
                        output = (short) Float.floatToRawIntBits(value);
                        break processValue;
                    }
                }

                outputVertices[outputIndex] = output;

                vertexIndex++;
                outputIndex++;
            }
        }

        return outputVertices;
    }

    private VerticesCompressor() {

    }
}
