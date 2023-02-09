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

import cientistavuador.ciencraftreal.audio.AudioLoader;
import cientistavuador.ciencraftreal.audio.AudioPlayer;

/**
 *
 * @author Cien
 */
public class BlockSounds {
    
    public static final boolean DEBUG_OUTPUT = true;
    
    public static final int NULL = 0;
    public static final int BLOB;
    public static final int WOOD_PLACE;
    public static final int GRASS_STEP;
    public static final int FOLIAGE_STEP;
    public static final int SAND_STEP;
    
    static {
        if (DEBUG_OUTPUT) {
            System.out.println("Loading block sounds.");
        }
        
        int[] buffers = AudioLoader.load(new String[] {
            "blob.ogg",
            "wood_place.ogg",
            "grass_step.ogg",
            "foliage_step.ogg",
            "sand_step.ogg"
        });
        
        BLOB = buffers[0];
        WOOD_PLACE = buffers[1];
        GRASS_STEP = buffers[2];
        FOLIAGE_STEP = buffers[3];
        SAND_STEP = buffers[4];
        
        if (DEBUG_OUTPUT) {
            System.out.println("Finished loading block sounds.");
        }
    }
    
    public static int play(int buffer, double x, double y, double z) {
        return AudioPlayer.play(buffer, x, y, z);
    }
    
    public static int play(int buffer, double x, double y, double z, Runnable deletedCallback) {
        return AudioPlayer.play(buffer, x, y, z, deletedCallback);
    }
    
    public static void init() {
        
    }
    
    private BlockSounds() {
        
    }
}
