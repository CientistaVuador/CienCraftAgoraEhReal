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
import java.nio.ByteOrder;
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

    private static int to10Bits(float a) {
        return (int) (((a + 1f) / 2f) * 1023f);
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
    
    public int numberOfVertices() {
        return this.verticesIndex / ChunkLayer.VERTEX_SIZE_ELEMENTS;
    }
    
    public int numberOfIndices() {
        return this.indicesIndex;
    }

    public void vertex(float x, float y, float z, float nX, float nY, float nZ, float texX, float texY, int texture, float ao) {
        if ((this.verticesIndex + ChunkLayer.VERTEX_SIZE_ELEMENTS) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + ChunkLayer.VERTEX_SIZE_ELEMENTS);
        }
        int normalXYZ = (to10Bits(nX) << 0) | (to10Bits(nY) << 10) | (to10Bits(nZ) << 20);

        this.vertices[this.verticesIndex + 0] = floatToShort(Math.abs(x) / Chunk.CHUNK_SIZE);
        this.vertices[this.verticesIndex + 1] = floatToShort(Math.abs(y - this.layerY) / ChunkLayer.HEIGHT);
        this.vertices[this.verticesIndex + 2] = floatToShort(Math.abs(z) / Chunk.CHUNK_SIZE);
        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
            this.vertices[this.verticesIndex + 3] = (short) (normalXYZ >>> 16);
            this.vertices[this.verticesIndex + 4] = (short) (normalXYZ >>> 0);
        } else {
            this.vertices[this.verticesIndex + 3] = (short) (normalXYZ >>> 0);
            this.vertices[this.verticesIndex + 4] = (short) (normalXYZ >>> 16);
        }
        this.vertices[this.verticesIndex + 5] = floatToShortSigned(texX / ChunkLayer.TEX_COORDS_MAX);
        this.vertices[this.verticesIndex + 6] = floatToShortSigned(texY / ChunkLayer.TEX_COORDS_MAX);
        this.vertices[this.verticesIndex + 7] = (short) texture;
        this.vertices[this.verticesIndex + 8] = floatToShort(ao);

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

    public void triangle(int i0, int i1, int i2) {
        if ((this.indicesIndex + 3) > this.indices.length) {
            this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + 3);
        }

        this.indices[this.indicesIndex + 0] = i0 + this.offset;
        this.indices[this.indicesIndex + 1] = i1 + this.offset;
        this.indices[this.indicesIndex + 2] = i2 + this.offset;

        this.indicesIndex += 3;
    }

    public void quad(int a0, int a1, int a2, int b0, int b1, int b2) {
        if ((this.indicesIndex + 6) > this.indices.length) {
            this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + 6);
        }

        this.indices[this.indicesIndex + 0] = a0 + this.offset;
        this.indices[this.indicesIndex + 1] = a1 + this.offset;
        this.indices[this.indicesIndex + 2] = a2 + this.offset;
        this.indices[this.indicesIndex + 3] = b0 + this.offset;
        this.indices[this.indicesIndex + 4] = b1 + this.offset;
        this.indices[this.indicesIndex + 5] = b2 + this.offset;

        this.indicesIndex += 6;
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
