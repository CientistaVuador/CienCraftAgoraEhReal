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
package cientistavuador.ciencraftreal.resources.audio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 *
 * @author Cien
 */
public class AudioResources {
    
    public static NativeAudio load(String name) {
        URL url = AudioResources.getAudioURL(name);

        if (url == null) {
            throw new NullPointerException("'" + name + "' not found.");
        }
        
        try {
            URLConnection conn = url.openConnection();
            conn.connect();

            InputStream in = conn.getInputStream();

            ByteBuffer audioFile = memAlloc(conn.getContentLength());
            try {

                byte[] buffer = new byte[4096];

                int r;
                while ((r = in.read(buffer)) != -1) {
                    audioFile.put(buffer, 0, r);
                }

                audioFile.flip();

                NativeAudio audio;
                
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer channelsBuffer = stack.callocInt(1);
                    IntBuffer sampleRateBuffer = stack.callocInt(1);
                    ShortBuffer data = stb_vorbis_decode_memory(audioFile, channelsBuffer, sampleRateBuffer);
                    int channels = channelsBuffer.get();
                    int sampleRate = sampleRateBuffer.get();
                    if (data == null) {
                        throw new RuntimeException("Could not read '"+name+"'");
                    }
                    audio = new NativeAudio(data, channels, sampleRate);
                }
                
                return audio;
            } finally {
                memFree(audioFile);
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static URL getAudioURL(String name) {
        return AudioResources.class.getResource(name);
    }
    
    private AudioResources() {
        
    }
}
