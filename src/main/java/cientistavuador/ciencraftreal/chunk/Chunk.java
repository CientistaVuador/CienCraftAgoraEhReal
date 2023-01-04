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
import cientistavuador.ciencraftreal.world.WorldCamera;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Cien
 */
public class Chunk {

    public static final int GENERATOR_SMOOTHNESS = 80;
    public static final int GENERATOR_DESIRED_MAX_HEIGHT = 74;
    public static final int GENERATOR_DESIRED_MIN_HEIGHT = 64;

    public static final int GENERATOR_CAVE_SMOOTHNESS_Y = 20;
    public static final int GENERATOR_CAVE_SMOOTHNESS_XZ = 40;
    public static final float GENERATOR_CAVE_CUTOFF = 0.2f;
    public static final float GENERATOR_CAVE_CUTOFF_SURFACE = 0.65f;

    public static final int CHUNK_SIZE = 32;
    public static final int CHUNK_HEIGHT = 256;

    public static final int RENDER_VERTEX_SIZE = 3 + 2 + 1;

    private final WorldCamera world;
    private final int chunkX;
    private final int chunkZ;

    private final int[] surface = new int[CHUNK_SIZE * CHUNK_SIZE];

    private final byte[] blocks = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT];
    private final int[] blocksInHeight = new int[CHUNK_HEIGHT];
    private int highestY = 0;

    private boolean markedForRegeneration = false;
    private float[] vertices = new float[0];
    
    private RenderableChunk renderableChunk = null;

    public Chunk(WorldCamera world, int chunkX, int chunkZ) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public int getTotalApproximateSizeInBytes() {
        int size = 0;
        size += surface.length * Integer.BYTES;
        size += blocks.length * Byte.BYTES;
        size += blocksInHeight.length * Integer.BYTES;
        size += vertices.length * Float.BYTES;
        return size;
    }
    
    public int getVerticesSizeInBytes() {
        return this.vertices.length * Float.BYTES;
    }
    
    public void generateBlocks() {
        long seed = this.world.getSeed();

        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                float worldX = (x + (chunkX * CHUNK_SIZE)) + 0.5f;
                float worldZ = (-z + (chunkZ * CHUNK_SIZE)) - 0.5f;

                worldX /= GENERATOR_SMOOTHNESS;
                worldZ /= GENERATOR_SMOOTHNESS;

                float value = OpenSimplex2.noise2(seed, worldX, worldZ);
                value = (value + 1f) / 2f;

                int height = (int) (value * (GENERATOR_DESIRED_MAX_HEIGHT - GENERATOR_DESIRED_MIN_HEIGHT)) + GENERATOR_DESIRED_MIN_HEIGHT;

                if (height > this.highestY) {
                    this.highestY = height;
                }

                setSurfaceY(x, -z, height);
            }
        }

        Random random = new Random(seed);

        long caveSeed = random.nextLong();
        long bedrockSeed = random.nextLong();

        for (int y = this.highestY; y >= 0; y--) {
            for (int z = 0; z >= -(CHUNK_SIZE - 1); z--) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    float worldX = (x + (chunkX * CHUNK_SIZE)) + 0.5f;
                    float worldY = (y + 0.5f);
                    float worldZ = (z + (chunkZ * CHUNK_SIZE)) - 0.5f;

                    int surfaceHeight = getSurfaceY(x, z);

                    if (y <= surfaceHeight) {
                        setBlockImpl(x, y, z, Blocks.STONE);
                    }

                    int distanceFromSurface = (y - surfaceHeight);

                    if (distanceFromSurface <= 0 && distanceFromSurface >= -3) {
                        if (distanceFromSurface == 0) {
                            setBlockImpl(x, y, z, Blocks.GRASS);
                        } else {
                            setBlockImpl(x, y, z, Blocks.DIRT);
                        }
                    }

                    float cutoff
                            = (y / ((float) surfaceHeight))
                            * (GENERATOR_CAVE_CUTOFF_SURFACE - GENERATOR_CAVE_CUTOFF)
                            + GENERATOR_CAVE_CUTOFF;

                    if (y > 3 && OpenSimplex2.noise3_ImproveXY(caveSeed, worldX / GENERATOR_CAVE_SMOOTHNESS_XZ, worldY / GENERATOR_CAVE_SMOOTHNESS_Y, worldZ / GENERATOR_CAVE_SMOOTHNESS_XZ) > cutoff) {
                        setBlockImpl(x, y, z, Blocks.AIR);
                    }

                    if (y == 0) {
                        setBlockImpl(x, y, z, Blocks.BEDROCK);
                    }
                    if (y == 1 && OpenSimplex2.noise2(bedrockSeed, worldX, worldZ) > -0.5) {
                        setBlockImpl(x, y, z, Blocks.BEDROCK);
                    }
                    if (y == 2 && OpenSimplex2.noise2(bedrockSeed, worldX + 1f, worldZ - 1f) > 0) {
                        setBlockImpl(x, y, z, Blocks.BEDROCK);
                    }
                    if (y == 3 && OpenSimplex2.noise2(bedrockSeed, worldX + 2f, worldZ - 2f) > 0.5) {
                        setBlockImpl(x, y, z, Blocks.BEDROCK);
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
                }
            }
        }

        this.vertices = new float[blockVerticesLength];
        int vertexCounter = 0;
        for (float[] blockVertices : blockVerticesList) {
            System.arraycopy(blockVertices, 0, this.vertices, vertexCounter, blockVertices.length);
            vertexCounter += blockVertices.length;
        }
        
        if (this.renderableChunk != null) {
            this.renderableChunk.onVertexUpdate();
        }
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
    
    public int getAmountOfBlocksInY(int y) {
        return this.blocksInHeight[y];
    }

    private Block getBlockImpl(int x, int y, int z) {
        int index = x + (-z * CHUNK_SIZE) + (y * CHUNK_SIZE * CHUNK_SIZE);
        return BlockRegister.getBlock(Byte.toUnsignedInt(this.blocks[index]));
    }

    private void setSurfaceY(int x, int z, int value) {
        this.surface[x + (-z * CHUNK_SIZE)] = value;
    }

    public void setBlock(int x, int y, int z, Block block) {
        setBlockImpl(x, y, z, block);
        this.markedForRegeneration = true;
    }
    
    public Block getBlock(int x, int y, int z) {
        return getBlockImpl(x, y, z);
    }

    public float[] getVertices() {
        return vertices.clone();
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

    public int getHighestY() {
        return highestY;
    }

    public int getSurfaceY(int x, int z) {
        return this.surface[x + (-z * CHUNK_SIZE)];
    }

    public RenderableChunk getRenderableChunk() {
        return renderableChunk;
    }

    protected void setRenderableChunk(RenderableChunk renderableChunk) {
        this.renderableChunk = renderableChunk;
        renderableChunk.onVertexUpdate();
    }
    
    public boolean markedForRegeneration() {
        return this.markedForRegeneration;
    }
    
    public void markForRegeneration() {
        this.markedForRegeneration = true;
    }
    
    public void updateVertices() {
        if (this.markedForRegeneration) {
            generateVertices();
            this.markedForRegeneration = false;
        }
    }
    
}
