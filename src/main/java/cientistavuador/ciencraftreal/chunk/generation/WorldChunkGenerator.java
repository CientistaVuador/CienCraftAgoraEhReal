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
package cientistavuador.ciencraftreal.chunk.generation;

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.BlockRegister;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.noise.OpenSimplex2;
import java.util.Random;

/**
 *
 * @author Cien
 */
public class WorldChunkGenerator implements ChunkGenerator {

    public static final int MAX_HEIGHT = 74;
    public static final int MIN_HEIGHT = 64;
    public static final int SMOOTHNESS = 80;

    public static final int ORE_TYPE_AREA = 500;
    public static final int ORE_SIZE = 400;
    public static final float ORE_CHANCE = 0.1f;

    public static final int BIOME_SIZE = 2000;

    private final Chunk chunk;
    private final int[] surfaceMap = new int[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
    private final int[] oreMap = new int[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
    private final int[] oreClueMap = new int[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
    private final long bedrockSeed;
    private final long oreSeed;
    private final long biomeSeed;
    private final Random treeRandom;
    private final Random oreClueRandom;

    public WorldChunkGenerator(Chunk chunk) {
        this.chunk = chunk;

        Random seedGenerator = new Random(chunk.getWorld().getSeed());
        this.bedrockSeed = seedGenerator.nextLong();
        this.oreSeed = seedGenerator.nextLong();
        this.biomeSeed = seedGenerator.nextLong();

        long chunkSeed = (((long) this.chunk.getChunkX()) << 32) + this.chunk.getChunkZ();
        seedGenerator.setSeed(chunk.getWorld().getSeed() ^ chunkSeed);
        this.treeRandom = new Random(seedGenerator.nextLong());
        this.oreClueRandom = new Random(seedGenerator.nextLong());
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void generate() {
        generateOreMap();
        generateSurface();

        for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
            for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                    generateStone(x, y, -z);
                    generateSoil(x, y, -z);
                    generateBedrock(x, y, -z);
                    generateOres(x, y, -z);
                }
            }
        }
        generateOreClues();
        generateTrees();
    }

    private void generateSurface() {
        long seed = this.chunk.getWorld().getSeed();
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                float value = OpenSimplex2.noise2(
                        seed,
                        ((x + 0.5) + this.chunk.getChunkX() * Chunk.CHUNK_SIZE) / SMOOTHNESS,
                        ((-z - 0.5) + this.chunk.getChunkZ() * Chunk.CHUNK_SIZE) / SMOOTHNESS
                );
                value = (value + 1f) * 0.5f;
                this.surfaceMap[x + (z * Chunk.CHUNK_SIZE)] = (int) (value * (MAX_HEIGHT - MIN_HEIGHT) + MIN_HEIGHT);
            }
        }
    }

    private void generateOreMap() {
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                float value = OpenSimplex2.noise2(this.oreSeed,
                        ((x + 0.5) + this.chunk.getChunkX() * Chunk.CHUNK_SIZE) / ORE_TYPE_AREA,
                        ((-z - 0.5) + this.chunk.getChunkZ() * Chunk.CHUNK_SIZE) / ORE_TYPE_AREA
                );
                value = (value + 1f) * 0.5f;
                this.oreMap[x + (z * Chunk.CHUNK_SIZE)] = (int) Math.floor(value * Blocks.ORES.length);
            }
        }
    }

    private void generateStone(int x, int y, int z) {
        int surface = this.surfaceMap[x + (-z * Chunk.CHUNK_SIZE)];

        if (y > surface) {
            return;
        }

        this.chunk.setBlock(x, y, z, Blocks.STONE);
    }

    private void generateSoil(int x, int y, int z) {
        int surface = this.surfaceMap[x + (-z * Chunk.CHUNK_SIZE)];

        if (y > surface) {
            return;
        }

        int distance = (surface - y);

        if (distance < 4) {
            Block top;
            Block under;

            float value = OpenSimplex2.noise2(this.biomeSeed,
                    ((x + 0.5) + this.chunk.getChunkX() * Chunk.CHUNK_SIZE) / BIOME_SIZE,
                    ((z - 0.5) + this.chunk.getChunkZ() * Chunk.CHUNK_SIZE) / BIOME_SIZE
            );
            value = (value + 1f) * 0.5f;
            
            int biomeType = (int) Math.floor(value * 3);
            
            switch (biomeType) {
                //high forest
                case 1 -> {
                    top = Blocks.MYCELIUM;
                    under = Blocks.DIRT;
                }
                //desert
                case 2 -> {
                    top = Blocks.SAND;
                    under = Blocks.SAND;
                }
                //low forest
                default -> {
                    top = Blocks.GRASS;
                    under = Blocks.DIRT;
                }
            }

            switch (distance) {
                case 0 ->
                    this.chunk.setBlock(x, y, z, top);
                case 1, 2, 3 ->
                    this.chunk.setBlock(x, y, z, under);
            }
        }
    }

    private void generateBedrock(int x, int y, int z) {
        if (y > 3) {
            return;
        }

        double worldX = (x + 0.5) + this.chunk.getChunkX() * Chunk.CHUNK_SIZE;
        double worldZ = (z - 0.5) + this.chunk.getChunkZ() * Chunk.CHUNK_SIZE;

        switch (y) {
            case 0 -> {
                this.chunk.setBlock(x, y, z, Blocks.BEDROCK);
            }
            case 1 -> {
                if (OpenSimplex2.noise2(this.bedrockSeed, worldX, worldZ) > -0.5) {
                    this.chunk.setBlock(x, y, z, Blocks.BEDROCK);
                }
            }
            case 2 -> {
                if (OpenSimplex2.noise2(this.bedrockSeed, worldX + 1f, worldZ - 1f) > 0) {
                    this.chunk.setBlock(x, y, z, Blocks.BEDROCK);
                }
            }
            case 3 -> {
                if (OpenSimplex2.noise2(this.bedrockSeed, worldX + 2f, worldZ - 2f) > 0.5) {
                    this.chunk.setBlock(x, y, z, Blocks.BEDROCK);
                }
            }
        }
    }

    private void generateOres(int x, int y, int z) {
        if (y >= 10 && y <= 50) {
            Block oreType = Blocks.ORES[this.oreMap[x + (-z * Chunk.CHUNK_SIZE)]];

            float value = OpenSimplex2.noise3_ImproveXZ(this.oreSeed,
                    ((x + 0.5) + this.chunk.getChunkX() * Chunk.CHUNK_SIZE) / ORE_SIZE,
                    (y + 0.5) / ORE_SIZE,
                    ((z - 0.5) + this.chunk.getChunkZ() * Chunk.CHUNK_SIZE) / ORE_SIZE
            );
            value = (value + 1f) * 0.5f;

            if (value > (1f - ORE_CHANCE)) {
                this.chunk.setBlock(x, y, z, oreType);
                this.oreClueMap[x + (-z * Chunk.CHUNK_SIZE)] = oreType.getId();
            }
        }
    }

    private void generateOreClues() {
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                int ore = this.oreClueMap[x + (z * Chunk.CHUNK_SIZE)];

                if (ore != 0 && this.oreClueRandom.nextInt(200) == 0) {
                    int surface = this.surfaceMap[x + (z * Chunk.CHUNK_SIZE)];

                    this.chunk.setBlock(x, surface, -z, BlockRegister.getBlock(ore));
                }
            }
        }
    }

    private void generateTrees() {
        for (int x = 1; x < (Chunk.CHUNK_SIZE - 1); x++) {
            for (int z = 1; z < (Chunk.CHUNK_SIZE - 1); z++) {
                if (this.treeRandom.nextFloat() > 0.75f) {
                    int surface = this.surfaceMap[x + (z * Chunk.CHUNK_SIZE)];

                    //placeTree(x, surface, -z);
                }
            }
        }
    }

    private void placeTree(int x, int y, int z) {
        for (int i = 0; i < 5; i++) {
            this.chunk.setBlock(x, y + i, z, Blocks.WOOD);
        }
    }

}
