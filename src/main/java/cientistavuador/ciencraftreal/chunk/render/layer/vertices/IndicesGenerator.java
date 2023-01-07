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

import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Cien
 */
public class IndicesGenerator {

    private static final class ShortArray {
        private final short[] array;

        public ShortArray(short[] array) {
            this.array = array;
        }

        public short[] getArray() {
            return array;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Arrays.hashCode(this.array);
            return hash;
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
            final ShortArray other = (ShortArray) obj;
            return Arrays.equals(this.array, other.array);
        }
    }

    public static Map.Entry<short[], int[]> generate(short[] vertices) {
        if (vertices.length == 0) {
            return Map.entry(new short[0], new int[0]);
        }

        final HashMap<ShortArray, Integer> verticesIndicesMap = new HashMap<>();
        final HashMap<Integer, ShortArray> indicesVerticesMap = new HashMap<>();
        
        final short[] vertexStorage = new short[ChunkLayer.VERTEX_SIZE_ELEMENTS];
        final int[] indices = new int[vertices.length / ChunkLayer.VERTEX_SIZE_ELEMENTS];
        
        int vertexIndex = 0;
        
        for (int i = 0; i < indices.length; i++) {
            int vertexOffset = i * ChunkLayer.VERTEX_SIZE_ELEMENTS;

            for (int j = 0; j < vertexStorage.length; j++) {
                vertexStorage[j] = vertices[j + vertexOffset];
            }

            ShortArray vertexStorageObject = new ShortArray(vertexStorage.clone());
            
            Integer vertexStorageIndex = verticesIndicesMap.get(vertexStorageObject);
            
            if (vertexStorageIndex == null) {
                verticesIndicesMap.put(vertexStorageObject, vertexIndex);
                indicesVerticesMap.put(vertexIndex, vertexStorageObject);
                indices[i] = vertexIndex;
                vertexIndex++;
            } else {
                indices[i] = vertexStorageIndex;
            }
        }
        
        final short[] verticesGenerated = new short[vertexIndex * ChunkLayer.VERTEX_SIZE_ELEMENTS];
        
        for (int i = 0; i < vertexIndex; i++) {
            ShortArray array = indicesVerticesMap.get(i);
            
            System.arraycopy(
                    array.getArray(),
                    0,
                    verticesGenerated,
                    i * ChunkLayer.VERTEX_SIZE_ELEMENTS,
                    ChunkLayer.VERTEX_SIZE_ELEMENTS
            );
        }
        
        return Map.entry(verticesGenerated, indices);
    }

    private IndicesGenerator() {

    }
}
