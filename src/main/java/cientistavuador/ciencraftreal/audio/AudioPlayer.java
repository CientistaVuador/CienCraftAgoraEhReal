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
package cientistavuador.ciencraftreal.audio;

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.camera.Camera;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joml.Vector3d;
import static org.lwjgl.openal.AL11.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class AudioPlayer {
    
    private static final Set<Integer> sourceList = new HashSet<>();
    private static final Vector3d cameraPos = new Vector3d(0, 0, 0);
    
    public static int play(int buffer, double x, double y, double z) {
        int source = alGenSources();
        alSourcei(source, AL_LOOPING, AL_FALSE);
        alSourcei(source, AL_BUFFER, buffer);
        alSource3f(source, AL_POSITION,
                (float) (x - cameraPos.x()),
                (float) (y - cameraPos.y()),
                (float) (z - cameraPos.z())
        );
        
        alSourcePlay(source);
        sourceList.add(source);
        
        return source;
    }
    
    public static void update(Camera camera) {
        alListener3f(AL_POSITION, 0, 0, 0);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(6);
            camera.getFront().get(buffer);
            buffer.position(3);
            camera.getUp().get(buffer);
            buffer.position(0);
            alListenerfv(AL_ORIENTATION, buffer);
        }
        
        float xSpeed = (float) ((camera.getPosition().x() - cameraPos.x()) / Main.TPF);
        float ySpeed = (float) ((camera.getPosition().y() - cameraPos.y()) / Main.TPF);
        float zSpeed = (float) ((camera.getPosition().z() - cameraPos.z()) / Main.TPF);
        
        alListener3f(AL_VELOCITY, xSpeed, ySpeed, zSpeed);
        
        List<Integer> toDelete = new ArrayList<>();
        
        for (Integer source:sourceList) {
            if (alGetSourcei(source, AL_SOURCE_STATE) == AL_STOPPED) {
                toDelete.add(source);
                continue;
            }
            
            float[] x = {0f};
            float[] y = {0f};
            float[] z = {0f};
            alGetSource3f(source, AL_POSITION, x, y, z);
            double absX = x[0] + cameraPos.x();
            double absY = y[0] + cameraPos.y();
            double absZ = z[0] + cameraPos.z();
            
            absX -= camera.getPosition().x();
            absY -= camera.getPosition().y();
            absZ -= camera.getPosition().z();
            
            alSource3f(source, AL_POSITION, (float)absX, (float)absY, (float)absZ);
        }
        
        for (Integer delete:toDelete) {
            alDeleteSources(delete);
            sourceList.remove(delete);
        }
        
        cameraPos.set(camera.getPosition());
    }
    
    private AudioPlayer() {
        
    }
    
}
