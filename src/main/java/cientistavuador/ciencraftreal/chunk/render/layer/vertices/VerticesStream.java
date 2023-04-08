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

import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayer;
import java.util.Arrays;

/**
 *
 * @author Cien
 */
public class VerticesStream {
    
    private static short floatToShort(float f) {
        return (short) (int) (f * ((Short.MAX_VALUE * 2) + 1));
    }
    
    private static short floatToShortSigned(float f) {
        if (f < 0) {
            return (short) (int) (f * Short.MIN_VALUE);
        }
        return (short) (int) (f * Short.MAX_VALUE);
    }
    
    private short[] vertices = new short[64];
    private int verticesIndex = 0;
    
    private int[] indices = new int[64];
    private int indicesIndex = 0;
    
    private int offset = 0;
    private int layerY = 0;
    
    public VerticesStream(int layerY) {
        this.layerY = layerY;
    }
    
    public void reset(int layerY) {
        this.verticesIndex = 0;
        this.indicesIndex = 0;
        this.offset = 0;
        this.layerY = layerY;
    }
    
    public void vertex(float x, float y, float z, float texX, float texY, int texture, float ao) {
        if ((this.verticesIndex + ChunkLayer.VERTEX_SIZE_ELEMENTS) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + ChunkLayer.VERTEX_SIZE_ELEMENTS);
        }
        
        this.vertices[this.verticesIndex + 0] = floatToShort(Math.abs(x) / Chunk.CHUNK_SIZE);
        this.vertices[this.verticesIndex + 1] = floatToShort(Math.abs(y - this.layerY) / ChunkLayer.HEIGHT);
        this.vertices[this.verticesIndex + 2] = floatToShort(Math.abs(z) / Chunk.CHUNK_SIZE);
        this.vertices[this.verticesIndex + 3] = floatToShortSigned(texX / ChunkLayer.TEX_COORDS_MAX);
        this.vertices[this.verticesIndex + 4] = floatToShortSigned(texY / ChunkLayer.TEX_COORDS_MAX);
        this.vertices[this.verticesIndex + 5] = (short) texture;
        this.vertices[this.verticesIndex + 6] = floatToShort(ao);
        this.vertices[this.verticesIndex + 7] = 0;
        
        this.verticesIndex += ChunkLayer.VERTEX_SIZE_ELEMENTS;
    }
    
    public void index(int index) {
        if (this.indices.length == this.indicesIndex) {
            this.indices = Arrays.copyOf(this.indices, this.indices.length * 2);
        }
        this.indices[this.indicesIndex] = index + this.offset;
        this.indicesIndex++;
    }
    
    public void index(int[] indices) {
        if ((this.indicesIndex + indices.length) > this.indices.length) {
            this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + indices.length);
        }
        for (int i = 0; i < indices.length; i++) {
            this.indices[this.indicesIndex] = indices[i] + this.offset;
            this.indicesIndex++;
        }
    }
    
    public void offset() {
        this.offset = (this.verticesIndex / ChunkLayer.VERTEX_SIZE_ELEMENTS);
    }
    
    public short[] vertices() {
        return Arrays.copyOf(this.vertices, this.verticesIndex);
    }
    
    public int[] indices() {
        return Arrays.copyOf(this.indices, this.indicesIndex);
    }
    
}
