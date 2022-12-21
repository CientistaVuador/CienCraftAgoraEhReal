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
package cientistavuador.ciencraftreal.resources.image;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * A Native image that must be manually freed.
 * @author Cien
 */
public class NativeImage {

    private boolean freed = false;
    private final ByteBuffer data;
    private final int width;
    private final int height;
    private final int channels;
    
    protected NativeImage(ByteBuffer data, int width, int height, int channels) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public ByteBuffer getData() {
        throwExceptionIfFreed();
        return data;
    }

    public int getWidth() {
        throwExceptionIfFreed();
        return width;
    }

    public int getHeight() {
        throwExceptionIfFreed();
        return height;
    }

    public int getChannels() {
        throwExceptionIfFreed();
        return channels;
    }
    
    private void throwExceptionIfFreed() {
        if (this.freed) {
            throw new RuntimeException("Image is already freed!");
        }
    }
    
    public void free() {
        throwExceptionIfFreed();
        MemoryUtil.memFree(this.data);
        this.freed = true;
    }
    
}
