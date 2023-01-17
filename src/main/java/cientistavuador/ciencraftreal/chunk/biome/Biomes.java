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
package cientistavuador.ciencraftreal.chunk.biome;

import cientistavuador.ciencraftreal.chunk.biome.biomes.GrassMountains;
import cientistavuador.ciencraftreal.chunk.biome.biomes.GrassPlains;

/**
 *
 * @author Cien
 */
public class Biomes {
    
    public static final BiomeRegister DEFAULT_REGISTER = new BiomeRegister();
    public static final BiomeMap DEFAULT_MAP = new BiomeMap(DEFAULT_REGISTER);
    
    public static final GrassPlains PLAINS = new GrassPlains();
    public static final GrassMountains MOUNTAINS = new GrassMountains();
    
    static {
        DEFAULT_REGISTER.register(PLAINS);
        DEFAULT_REGISTER.register(MOUNTAINS);
        
        DEFAULT_MAP.fill(BiomeMap.HUMIDITY_MIN, BiomeMap.TEMPERATURE_MIN, BiomeMap.HUMIDITY_MAX, BiomeMap.TEMPERATURE_MAX, PLAINS);
        DEFAULT_MAP.fill(BiomeMap.HUMIDITY_MIN, BiomeMap.TEMPERATURE_MIN, BiomeMap.HUMIDITY_MAX, 0f, MOUNTAINS);
        
        DEFAULT_MAP.createDefinitionMaps();
    }
    
    public static void init() {
        
    }
    
    private Biomes() {
        
    }
}
