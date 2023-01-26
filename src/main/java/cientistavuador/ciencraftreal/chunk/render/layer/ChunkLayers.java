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
package cientistavuador.ciencraftreal.chunk.render.layer;

import cientistavuador.ciencraftreal.chunk.Chunk;

/**
 *
 * @author Cien
 */
public class ChunkLayers {

    private final Chunk chunk;
    private final ChunkLayer[] layers = new ChunkLayer[Chunk.CHUNK_HEIGHT / ChunkLayer.HEIGHT];
    
    public ChunkLayers(Chunk chunk) {
        this.chunk = chunk;
        for (int i = 0; i < this.layers.length; i++) {
            layers[i] = new ChunkLayer(chunk, i * ChunkLayer.HEIGHT);
        }
    }
    
    public Chunk getChunk() {
        return chunk;
    }
    
    public int length() {
        return this.layers.length;
    }
    
    public ChunkLayer layerAt(int index) {
        return this.layers[index];
    }
    
    public ChunkLayer layerAtY(int y) {
        y /= ChunkLayer.HEIGHT;
        return layerAt(y);
    }

    public void delete() {
        for (int i = 0; i < this.layers.length; i++) {
            this.layers[i].delete();
        }
    }
    
}
