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
package cientistavuador.ciencraftreal.chunk.biome.definitionmap;

import cientistavuador.ciencraftreal.chunk.biome.BiomeMap;
import static cientistavuador.ciencraftreal.chunk.biome.BiomeMap.*;

/**
 *
 * @author Cien
 */
public class BiomeDefinitionMap {
    
    public static final int MAP_SIZE_MULTIPLIER = 8;
    
    private final BiomeMap map;
    private final float[] pixels;
    
    protected BiomeDefinitionMap(BiomeMap map, float[] pixels) {
        this.map = map;
        this.pixels = pixels;
    }

    public BiomeMap getMap() {
        return map;
    }
    
    public float valueAt(float humidity, float temperature) {
        int sizePixels = MAP_SIZE * MAP_SIZE_MULTIPLIER;
        humidity = ((humidity - HUMIDITY_MIN) / (HUMIDITY_MAX - HUMIDITY_MIN)) * sizePixels;
        temperature = ((temperature - TEMPERATURE_MIN) / (TEMPERATURE_MAX - TEMPERATURE_MIN)) * sizePixels;
        int index = (int) (Math.floor(humidity) + (Math.floor(temperature) * sizePixels));
        return this.pixels[index];
    }
    
}
