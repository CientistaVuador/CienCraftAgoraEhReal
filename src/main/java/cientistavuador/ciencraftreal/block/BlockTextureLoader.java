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

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.resources.image.ImageResources;
import cientistavuador.ciencraftreal.resources.image.NativeImage;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author Cien
 */
public class BlockTextureLoader {

    public static boolean DEBUG_OUTPUT = true;
    public static boolean USE_ANISOTROPIC_FILTERING = true;
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;

    private static final ArrayDeque<Consumer<ByteBuffer>> queue = new ArrayDeque<>(64);
    private static int currentTexture = 0;
    private static int glTextureArray = 0;

    public static int push(final String resource) {
        if (glTextureArray != 0) {
            throw new RuntimeException("Already loaded!");
        }
        final int index = currentTexture;
        queue.add((outputBuffer) -> {
            if (DEBUG_OUTPUT) {
                System.out.println("Loading '" + resource + "' with index " + index);
            }

            NativeImage image = ImageResources.load(resource, 4);

            try {
                if (image.getWidth() != WIDTH || image.getHeight() != HEIGHT) {
                    throw new IllegalArgumentException("Failed to load '" + resource + "' image dimensions must be " + WIDTH + "x" + HEIGHT);
                }

                long source = memAddress(image.getData());
                long dest = memAddress(outputBuffer) + (WIDTH * HEIGHT * 4 * index);
                memCopy(
                        source,
                        dest,
                        WIDTH * HEIGHT * 4
                );
            } finally {
                image.free();
            }

            if (DEBUG_OUTPUT) {
                System.out.println("Finished loading '" + resource + "' with index " + index);
            }
        });
        if (DEBUG_OUTPUT) {
            System.out.println("Pushed '" + resource + "' into the queue with index " + index);
        }

        return currentTexture++;
    }

    public static void loadTextures() {
        if (glTextureArray != 0) {
            throw new RuntimeException("Already loaded!");
        }
        if (DEBUG_OUTPUT) {
            System.out.println("Loading Textures from the hard drive.");
        }

        final ByteBuffer data = memAlloc(WIDTH * HEIGHT * currentTexture * 4);
        final ArrayDeque<Future<?>> tasks = new ArrayDeque<>(64);

        while (true) {
            final Consumer<ByteBuffer> task = queue.poll();
            if (task == null) {
                break;
            }
            tasks.add(CompletableFuture.runAsync(() -> {
                task.accept(data);
            }));
        }

        Future<?> result;
        while ((result = tasks.poll()) != null) {
            try {
                result.get();
            } catch (InterruptedException | ExecutionException ex) {
                memFree(data);
                throw new RuntimeException(ex);
            }
        }

        if (DEBUG_OUTPUT) {
            System.out.flush();

            System.out.println("Finished loading textures from the hard drive.");

            System.out.println("Uploading to the gpu.");
        }

        glTextureArray = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, glTextureArray);

        glTexImage3D(
                GL_TEXTURE_2D_ARRAY,
                0,
                GL_RGBA8,
                WIDTH,
                HEIGHT,
                currentTexture,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                data
        );
        memFree(data);

        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);

        if (USE_ANISOTROPIC_FILTERING && GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            glTexParameterf(
                    GL_TEXTURE_2D_ARRAY,
                    GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT)
            );
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        Main.checkGLError();

        if (DEBUG_OUTPUT) {
            System.out.println("Finished uploading to the gpu.");
        }
    }

    public static int getGLTextureArray() {
        if (glTextureArray == 0) {
            throw new RuntimeException("Not loaded.");
        }
        return glTextureArray;
    }

    private BlockTextureLoader() {
    }
}
