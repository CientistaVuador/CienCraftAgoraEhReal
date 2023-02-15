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
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.opengl.GL33C.*;

/**
 * A Native image that must be manually freed.
 * @author Cien
 */
public class NativeImage {

    public static final boolean USE_ANISOTROPIC_FILTERING = true;
    
    private boolean freed = false;
    private int texture = 0;
    
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
    
    public void createTexture() {
        throwExceptionIfFreed();
        if (this.texture != 0) {
            glDeleteTextures(this.texture);
        }
        this.texture = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.texture);
        
        int internalFormat;
        int format;
        switch (this.channels) {
            case 1 -> {
                internalFormat = GL_R8;
                format = GL_RED;
            }
            case 2 -> {
                internalFormat = GL_RG8;
                format = GL_RG;
            }
            case 3 -> {
                internalFormat = GL_RGB8;
                format = GL_RGB;
            }
            case 4 -> {
                internalFormat = GL_RGBA8;
                format = GL_RGBA;
            }
            default -> {
                throw new RuntimeException("Unknown Number of Channels: "+this.channels);
            }
        }
        
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                internalFormat,
                this.width,
                this.height,
                0,
                format,
                GL_UNSIGNED_BYTE,
                this.data
        );
        glGenerateMipmap(GL_TEXTURE_2D);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        if (USE_ANISOTROPIC_FILTERING && GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            glTexParameterf(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT)
            );
        }
        
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    public int getTexture() {
        throwExceptionIfFreed();
        return this.texture;
    }
    
    public void free() {
        throwExceptionIfFreed();
        MemoryUtil.memFree(this.data);
        if (this.texture != 0) {
            glDeleteTextures(this.texture);
        }
        this.freed = true;
    }
    
}
