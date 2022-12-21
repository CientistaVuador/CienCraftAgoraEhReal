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
package cientistavuador.ciencraftreal.chunk.stages;

import java.util.HashMap;
import java.util.Map;
import cientistavuador.ciencraftreal.chunk.ParallelNextStage;

/**
 *
 * @author Cien
 */
@ParallelNextStage
public class ChunkBase {
    private static final Map<Class<?>, Boolean> multithreadedMap = new HashMap<>();
    
    public static boolean isNextStageParallel(Class<?> clazz) {
        Boolean b = multithreadedMap.get(clazz);
        if (b == null) {
            b = clazz.getAnnotation(ParallelNextStage.class) != null;
            multithreadedMap.put(clazz, b);
        }
        return b;
    }
    
    public static final int WIDTH = 32;
    public static final int HEIGHT = 256;
    public static final int DEPTH = 32;
    
    protected long seed;
    protected int chunkX;
    protected int chunkY;
    protected int chunkZ;
    
    public ChunkBase(long seed, int chunkX, int chunkY, int chunkZ) {
        this.seed = seed;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
    }

    public long getSeed() {
        return seed;
    }
    
    public int getChunkX() {
        return chunkX;
    }

    public int getChunkY() {
        return chunkY;
    }

    public int getChunkZ() {
        return chunkZ;
    }
    
    public ChunkSurface nextStage() {
        return new ChunkSurface(this);
    }
    
}
