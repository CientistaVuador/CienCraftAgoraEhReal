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
package cientistavuador.ciencraftreal.chunk;

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.BlockRegister;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.chunk.biome.Biome;
import cientistavuador.ciencraftreal.chunk.biome.Biomes;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayers;
import cientistavuador.ciencraftreal.world.WorldCamera;

/**
 *
 * @author Cien
 */
public class Chunk {

    public static final int CHUNK_SIZE = 32;
    public static final int CHUNK_HEIGHT = 128;

    public static final int RENDER_VERTEX_SIZE = 3 + 2 + 1;

    private final WorldCamera world;
    private final int chunkX;
    private final int chunkZ;

    private final byte[] blocks = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT];
    private final int[] blocksInHeight = new int[CHUNK_HEIGHT];
    private final float[] humidityTemperatureMap = new float[CHUNK_SIZE * CHUNK_SIZE * 2];
    private final byte[] biomeMap = new byte[CHUNK_SIZE * CHUNK_SIZE];
    private final ChunkLayers layers;
    
    private int highestY = 0;

    public Chunk(WorldCamera world, int chunkX, int chunkZ) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.layers = new ChunkLayers(this);
        //generateBiomes();
        //generateBlocks();
    }
    
    private void generateBiomes() {
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                double blockX = (x + (this.chunkX * Chunk.CHUNK_SIZE)) + 0.5;
                double blockZ = (-z + (this.chunkZ * Chunk.CHUNK_SIZE)) - 0.5;
                
                float humidity = this.world.getHumidity(blockX, blockZ);
                float temperature = this.world.getTemperature(blockX, blockZ);
                
                Biome biome = Biomes.DEFAULT_MAP.getBiomeAt(humidity, temperature);
                
                this.humidityTemperatureMap[((x + (z * Chunk.CHUNK_SIZE)) * 2) + 0] = humidity;
                this.humidityTemperatureMap[((x + (z * Chunk.CHUNK_SIZE)) * 2) + 1] = temperature;
                this.biomeMap[x + (z * Chunk.CHUNK_SIZE)] = (byte) biome.getId();
            }
        }
    }
    
    private void generateBlocks() {
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                //todo: fix constructor leak
                getBiomeImpl(x, -z).getGenerator().generateColumn(this, x, -z);
            }
        }
        /*for (int y = 0; y < CHUNK_HEIGHT; y++) {
        for (int z = 0; z < CHUNK_SIZE; z++) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
        switch (y) {
        case 0 -> setBlockImpl(x, y, -z, Blocks.BEDROCK);
        case 1 -> setBlockImpl(x, y, -z, Blocks.IRON_ORE);
        case 2 -> setBlockImpl(x, y, -z, Blocks.COAL_ORE);
        case 3 -> setBlockImpl(x, y, -z, Blocks.DIRT);
        case 4 -> setBlockImpl(x, y, -z, Blocks.GRASS);
        }
        }
        }
        }*/
    }

    private void setBlockImpl(int x, int y, int z, Block block) {
        int index = x + (-z * CHUNK_SIZE) + (y * CHUNK_SIZE * CHUNK_SIZE);
        
        boolean removing = (Blocks.AIR == block);
        byte blockAtIndex = this.blocks[index];

        if (removing && blockAtIndex == 0) {
            return;
        }

        boolean removingAndPlacing = !removing && blockAtIndex != 0;

        if (removing) {
            this.blocks[index] = 0;
            this.blocksInHeight[y]--;

            if (y == this.highestY && this.blocksInHeight[y] == 0) {
                if (this.highestY != 0) {
                    this.highestY--;
                    for (int i = this.highestY; i >= 0; i--) {
                        this.highestY = i;
                        if (this.blocksInHeight[i] != 0) {
                            break;
                        }
                    }
                }
            }
        } else {
            this.blocks[index] = (byte) block.getId();

            if (!removingAndPlacing) {
                this.blocksInHeight[y]++;
                if (y > this.highestY) {
                    this.highestY = y;
                }
            }
        }
        
        this.layers.layerAtY(y).delete();
        if (y < (CHUNK_HEIGHT-1)) {
            this.layers.layerAtY(y+1).delete();
        }
        if (y > 0) {
            this.layers.layerAtY(y-1).delete();
        }
    }
    
    public void setBlock(int x, int y, int z, Block block) {
        setBlockImpl(x, y, z, block);
    }
    
    private Block getBlockImpl(int x, int y, int z) {
        int index = x + (-z * CHUNK_SIZE) + (y * CHUNK_SIZE * CHUNK_SIZE);
        return BlockRegister.getBlock(Byte.toUnsignedInt(this.blocks[index]));
    }
    
    public Block getBlock(int x, int y, int z) {
        return getBlockImpl(x, y, z);
    }
    
    public int getAmountOfBlocksInY(int y) {
        return this.blocksInHeight[y];
    }
    
    public int getHighestY() {
        return highestY;
    }

    public WorldCamera getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }
    
    public ChunkLayers getLayers() {
        return layers;
    }
    
    public float getHumidity(int x, int z) {
        return this.humidityTemperatureMap[((x + (-z * Chunk.CHUNK_SIZE)) * 2) + 0];
    }
    
    public float getTemperature(int x, int z) {
        return this.humidityTemperatureMap[((x + (-z * Chunk.CHUNK_SIZE)) * 2) + 1];
    }
    
    private Biome getBiomeImpl(int x, int z) {
        return Biomes.DEFAULT_REGISTER.getBiome(Byte.toUnsignedInt(this.biomeMap[x + (-z * Chunk.CHUNK_SIZE)]));
    }
    
    public Biome getBiome(int x, int z) {
        return getBiomeImpl(x, z);
    }
    
}
