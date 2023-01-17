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
import static cientistavuador.ciencraftreal.chunk.Chunk.CHUNK_SIZE;
import cientistavuador.ciencraftreal.chunk.biome.BiomeDefinition;
import cientistavuador.ciencraftreal.chunk.biome.BiomeGenerator;
import cientistavuador.ciencraftreal.chunk.biome.Biomes;
import cientistavuador.ciencraftreal.noise.OpenSimplex2;
import java.util.Random;

/**
 *
 * @author Cien
 */
public class GrassPlainsGenerator implements BiomeGenerator {

    @Override
    public void generateColumn(Chunk chunk, int chunkBlockX, int chunkBlockZ) {
        float humidity = chunk.getHumidity(chunkBlockX, chunkBlockZ);
        float temperature = chunk.getTemperature(chunkBlockX, chunkBlockZ);

        long seed = chunk.getWorld().getSeed();

        double worldX = (chunkBlockX + (chunk.getChunkX() * CHUNK_SIZE)) + 0.5f;
        double worldZ = (chunkBlockZ + (chunk.getChunkZ() * CHUNK_SIZE)) - 0.5f;

        float generatorSmoothness = Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_SMOOTHNESS);
        
        float value = OpenSimplex2.noise2(seed, worldX / generatorSmoothness, worldZ / generatorSmoothness);
        value = (value + 1f) / 2f;

        int maxHeight = (int) Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_DESIRED_MAX_HEIGHT);
        int minHeight = (int) Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_DESIRED_MIN_HEIGHT);

        int height = (int) (value * (maxHeight - minHeight)) + minHeight;

        Random random = new Random(seed);

        long caveSeed = random.nextLong();
        long bedrockSeed = random.nextLong();
        
        float caveCutoffSurface = Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_CAVE_CUTOFF_SURFACE);
        float caveCutoff = Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_CAVE_CUTOFF);
        
        float caveSmoothnessXZ = Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_CAVE_SMOOTHNESS_XZ);
        float caveSmoothnessY = Biomes.DEFAULT_MAP.getBiomeDefinitionAt(humidity, temperature, BiomeDefinition.GENERATOR_CAVE_SMOOTHNESS_Y);
        
        for (int y = height; y >= 0; y--) {
            double worldY = (y + 0.5f);
            
            chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.STONE);
            
            int distanceFromSurface = (y - height);

            if (distanceFromSurface <= 0 && distanceFromSurface >= -3) {
                if (distanceFromSurface == 0) {
                    chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.GRASS);
                } else {
                    chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.DIRT);
                }
            }

            float cutoff
                    = (y / ((float) height))
                    * (caveCutoffSurface - caveCutoff)
                    + caveCutoff;

            if (y > 3 && OpenSimplex2.noise3_ImproveXY(caveSeed, worldX / caveSmoothnessXZ, worldY / caveSmoothnessY, worldZ / caveSmoothnessXZ) > cutoff) {
                chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.AIR);
            }

            if (y == 0) {
                chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.BEDROCK);
            }
            if (y == 1 && OpenSimplex2.noise2(bedrockSeed, worldX, worldZ) > -0.5) {
                chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.BEDROCK);
            }
            if (y == 2 && OpenSimplex2.noise2(bedrockSeed, worldX + 1f, worldZ - 1f) > 0) {
                chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.BEDROCK);
            }
            if (y == 3 && OpenSimplex2.noise2(bedrockSeed, worldX + 2f, worldZ - 2f) > 0.5) {
                chunk.setBlock(chunkBlockX, y, chunkBlockZ, Blocks.BEDROCK);
            }
        }
    }

}
