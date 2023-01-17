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
package cientistavuador.ciencraftreal.chunk.biome.biomes.generators;

import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.biome.Biome;
import cientistavuador.ciencraftreal.chunk.biome.BiomeDefinition;
import cientistavuador.ciencraftreal.chunk.biome.BiomeGenerator;
import cientistavuador.ciencraftreal.chunk.biome.Biomes;

/**
 *
 * @author Cien
 */
public class GrassPlainsGenerator implements BiomeGenerator {

    @Override
    public void generateColumn(Chunk chunk, int chunkBlockX, int chunkBlockZ) {
        float humidity = chunk.getHumidity(chunkBlockX, chunkBlockZ);
        float temperature = chunk.getTemperature(chunkBlockX, chunkBlockZ);
        int yStart = (int) Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_DESIRED_MAX_HEIGHT);
        
        for (int y = yStart; y >= 0; y--) {
            chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.STONE);
        }
    }
    
}
