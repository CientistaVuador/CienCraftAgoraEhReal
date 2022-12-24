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
import cientistavuador.ciencraftreal.noise.OpenSimplex2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Cien
 */
public class Chunk {

    public static final int GENERATOR_SMOOTHNESS = 50;
    public static final int GENERATOR_DESIRED_MAX_HEIGHT = 74;
    public static final int GENERATOR_DESIRED_MIN_HEIGHT = 64;

    public static final int CHUNK_SIZE = 32;
    public static final int CHUNK_HEIGHT = 256;

    private final long seed;
    private final int chunkX;
    private final int chunkZ;

    private final int[] surface = new int[CHUNK_SIZE * CHUNK_SIZE];

    private final byte[] blocks = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT];
    private final int[] blocksInHeight = new int[CHUNK_HEIGHT];
    private int highestY = 0;

    private final int[] zBlockLineVertexStartEnd = new int[CHUNK_SIZE * CHUNK_HEIGHT * 2];
    private float[] vertices = new float[0];

    public Chunk(long seed, int chunkX, int chunkZ) {
        this.seed = seed;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void generateBlocks() {
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                float worldX = (x + (chunkX * CHUNK_SIZE)) + 0.5f;
                float worldZ = (-z + (chunkZ * CHUNK_SIZE)) - 0.5f;

                worldX /= GENERATOR_SMOOTHNESS;
                worldZ /= GENERATOR_SMOOTHNESS;

                float value = OpenSimplex2.noise2(this.seed, worldX, worldZ);
                value = (value + 1f) / 2f;

                int height = (int) (value * (GENERATOR_DESIRED_MAX_HEIGHT - GENERATOR_DESIRED_MIN_HEIGHT)) + GENERATOR_DESIRED_MIN_HEIGHT;

                if (height > this.highestY) {
                    this.highestY = height;
                }

                setSurfaceY(x, -z, height);
            }
        }

        Random random = new Random(this.seed);

        long caveSeed = random.nextLong();
        long bedrockSeed = random.nextLong();

        for (int y = this.highestY; y >= 0; y--) {
            for (int z = 0; z >= -(CHUNK_SIZE - 1); z--) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    if (y <= getSurfaceY(x, z)) {
                        setBlockImpl(x, y, z, Blocks.STONE);
                    }
                }
            }
        }
    }

    public void generateVertices() {
        List<float[]> blockVerticesList = new ArrayList<>(64);
        int blockVerticesLength = 0;
        
        for (int y = this.highestY; y >= 0; y--) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                int zVertexCounter = 0;
                for (int z = 0; z >= -(CHUNK_SIZE - 1); z--) {
                    Block block = getBlockImpl(x, y, z);
                    
                    if (block == Blocks.AIR) {
                        continue;
                    }
                    
                    float[] blockVertices = block.generateVertices(
                            this,
                            x,
                            y,
                            z
                    );
                    
                    if (blockVertices == null || blockVertices.length == 0) {
                        continue;
                    }
                    
                    blockVerticesList.add(blockVertices);
                    blockVerticesLength += blockVertices.length;
                    zVertexCounter += blockVertices.length;
                }
                setZBlockLineVertexStartEnd(x, y, blockVerticesLength - zVertexCounter, blockVerticesLength);
            }
        }
        
        this.vertices = new float[blockVerticesLength];
        int vertexCounter = 0;
        for (float[] blockVertices:blockVerticesList) {
            System.arraycopy(blockVertices, 0, this.vertices, vertexCounter, blockVertices.length);
            vertexCounter += blockVertices.length;
        }
        
    }

    private int getZBlockLineVertexStart(int x, int y) {
        return this.zBlockLineVertexStartEnd[(x + (y * CHUNK_HEIGHT)) * 2];
    }

    private int getZBlockLineVertexEnd(int x, int y) {
        return this.zBlockLineVertexStartEnd[((x + (y * CHUNK_HEIGHT)) * 2) + 1];
    }

    private void setZBlockLineVertexStartEnd(int x, int y, int start, int end) {
        this.zBlockLineVertexStartEnd[(x + (y * CHUNK_HEIGHT)) * 2] = start;
        this.zBlockLineVertexStartEnd[((x + (y * CHUNK_HEIGHT)) * 2) + 1] = end;
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
    }

    public Block getBlockImpl(int x, int y, int z) {
        int index = x + (-z * CHUNK_SIZE) + (y * CHUNK_SIZE * CHUNK_SIZE);
        return BlockRegister.getBlock(Byte.toUnsignedInt(this.blocks[index]));
    }

    private void setSurfaceY(int x, int z, int value) {
        this.surface[x + (-z * CHUNK_SIZE)] = value;
    }

    public long getSeed() {
        return seed;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getHighestY() {
        return highestY;
    }

    public int getSurfaceY(int x, int z) {
        return this.surface[x + (-z * CHUNK_SIZE)];
    }

}
