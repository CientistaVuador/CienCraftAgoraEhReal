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
package cientistavuador.ciencraftreal.world;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 *
 * @author Cien
 */
public class WorldSky {
    
    private final Vector3f directionalDiffuseColor = new Vector3f(0xDD / 255f, 0x54 / 255f, 0x1C / 255f).mul(1.2f);
    private final Vector3f directionalAmbientColor = new Vector3f(0xDD / 255f, 0x54 / 255f, 0x1C / 255f).mul(0.2f);
    private final Vector3f directionalDirection = new Vector3f(1f, -0.5f, 1f).normalize();
    
    public WorldSky() {
        
    }

    public Vector3fc getDirectionalAmbientColor() {
        return directionalAmbientColor;
    }

    public Vector3fc getDirectionalDiffuseColor() {
        return directionalDiffuseColor;
    }

    public Vector3fc getDirectionalDirection() {
        return directionalDirection;
    }
    
    
    
}
