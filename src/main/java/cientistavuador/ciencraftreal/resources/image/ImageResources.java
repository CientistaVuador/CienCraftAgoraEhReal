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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.stb.STBImage.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author Cien
 */
public class ImageResources {

    /**
     * Loads an image
     *
     * @param name the resource name
     * @param desiredChannels desired number of channels (0 1 2 3 4)
     * @return a native image that must be manually freed
     */
    public static NativeImage load(String name, int desiredChannels) {
        URL url = ImageResources.getImageURL(name);

        if (url == null) {
            throw new NullPointerException("'" + name + "' not found.");
        }

        try {
            URLConnection conn = url.openConnection();
            conn.connect();

            try (InputStream in = conn.getInputStream()) {
                return load(name, in, conn.getContentLength(), desiredChannels);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static NativeImage load(String name, InputStream stream, int size, int desiredChannels) {
        ByteBuffer imageFile = memAlloc(size);
        try {
            byte[] buffer = new byte[4096];

            try {
                int r;
                while ((r = stream.read(buffer)) != -1) {
                    imageFile.put(buffer, 0, r);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            imageFile.flip();

            NativeImage image;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer widthBuffer = stack.callocInt(1);
                IntBuffer heightBuffer = stack.callocInt(1);
                IntBuffer channels = stack.callocInt(1);

                stbi_set_flip_vertically_on_load_thread(1);

                ByteBuffer imageData = stbi_load_from_memory(
                        imageFile,
                        widthBuffer,
                        heightBuffer,
                        channels,
                        desiredChannels
                );

                if (imageData == null) {
                    throw new NullPointerException("Failed to load '" + name + "': " + stbi_failure_reason());
                }

                int resultChannels = channels.get();
                if (desiredChannels != 0) {
                    resultChannels = desiredChannels;
                }

                image = new NativeImage(
                        imageData,
                        widthBuffer.get(),
                        heightBuffer.get(),
                        resultChannels
                );
            }

            return image;
        } finally {
            memFree(imageFile);
        }
    }

    public static URL getImageURL(String name) {
        return ImageResources.class.getResource(name);
    }

    private ImageResources() {

    }

}
