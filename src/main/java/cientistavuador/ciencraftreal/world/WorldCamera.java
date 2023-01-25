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

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.generation.ChunkGenerator;
import cientistavuador.ciencraftreal.chunk.generation.ChunkGeneratorFactory;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayers;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayersRender;
import cientistavuador.ciencraftreal.noise.OpenSimplex2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Cien
 */
public class WorldCamera {

    public static final int HUMIDITY_TEMPERATURE_SCALE = 4000;
    
    public static final int VIEW_DISTANCE = 5;
    public static final int VIEW_DISTANCE_SIZE = (VIEW_DISTANCE * 2) + 1;
    public static final int VIEW_DISTANCE_NUMBER_OF_CHUNKS = VIEW_DISTANCE_SIZE * VIEW_DISTANCE_SIZE;

    private final Object[] map = new Object[VIEW_DISTANCE_NUMBER_OF_CHUNKS];

    private final long seed;
    private final long temperatureSeed;
    private final long humiditySeed;
    private final Camera camera;
    private int oldChunkX = 0;
    private int oldChunkZ = 0;
    private int chunkX = 0;
    private int chunkZ = 0;
    private final ChunkGeneratorFactory chunkGeneratorFactory;

    public WorldCamera(Camera camera, long seed, ChunkGeneratorFactory chunkGeneratorFactory) {
        this.camera = camera;
        this.seed = seed;
        
        Random random = new Random(seed);
        this.temperatureSeed = random.nextLong();
        this.humiditySeed = random.nextLong();
        this.chunkGeneratorFactory = chunkGeneratorFactory;
    }

    public ChunkGeneratorFactory getChunkGeneratorFactory() {
        return chunkGeneratorFactory;
    }
    
    public long getTemperatureSeed() {
        return temperatureSeed;
    }

    public long getHumiditySeed() {
        return humiditySeed;
    }
    
    private void updatePosition() {
        int camChunkX = (int) Math.floor(camera.getPosition().x() / Chunk.CHUNK_SIZE);
        int camChunkZ = (int) Math.ceil(camera.getPosition().z() / Chunk.CHUNK_SIZE);

        if (this.chunkX != camChunkX || this.chunkZ != camChunkZ) {
            this.oldChunkX = this.chunkX;
            this.oldChunkZ = this.chunkZ;
            this.chunkX = camChunkX;
            this.chunkZ = camChunkZ;
        }
    }
    
    private void moveAreas() {
        int translationX = this.oldChunkX - this.chunkX;
        int translationZ = this.oldChunkZ - this.chunkZ;

        if (Math.abs(translationX) > VIEW_DISTANCE_SIZE || Math.abs(translationZ) > VIEW_DISTANCE_SIZE) {
            for (int i = 0; i < this.map.length; i++) {
                Object m = this.map[i];
                if (m instanceof Chunk e) {
                    e.getLayers().delete();
                }
            }

            Arrays.fill(this.map, null);
            return;
        }

        if (translationX == 0 && translationZ == 0) {
            return;
        }

        Object[] copy = this.map.clone();
        Arrays.fill(this.map, null);

        for (int i = 0; i < copy.length; i++) {
            Object e = copy[i];

            if (e == null) {
                continue;
            }
            
            //todo: fix future chunks being ignored when the camera moves.
            if (e instanceof Chunk m) {
                int newCamRelativeX = m.getChunkX() - this.chunkX;
                int newCamRelativeZ = m.getChunkZ() - this.chunkZ;

                boolean outOfBoundsX = (newCamRelativeX > VIEW_DISTANCE) || (newCamRelativeX < -VIEW_DISTANCE);
                boolean outOfBoundsZ = (newCamRelativeZ > VIEW_DISTANCE) || (newCamRelativeZ < -VIEW_DISTANCE);

                if (outOfBoundsX || outOfBoundsZ) {
                    m.getLayers().delete();
                } else {
                    this.map[(newCamRelativeX + VIEW_DISTANCE) + ((-newCamRelativeZ + VIEW_DISTANCE) * VIEW_DISTANCE_SIZE)] = m;
                }
            }
        }
    }
    
    private void finishUpdatePosition() {
        this.oldChunkX = this.chunkX;
        this.oldChunkZ = this.chunkZ;
    }
    
    private void updateChunks() {
        for (int i = 0; i < VIEW_DISTANCE_NUMBER_OF_CHUNKS; i++) {
            int x = (i % VIEW_DISTANCE_SIZE) - VIEW_DISTANCE;
            int z = -((i / VIEW_DISTANCE_SIZE) - VIEW_DISTANCE);

            Object m = this.map[i];
            if (m == null) {
                this.map[i] = CompletableFuture.supplyAsync(() -> {
                    Chunk chunk = new Chunk(this, this.chunkX + x, this.chunkZ + z);
                    ChunkGenerator generator = this.chunkGeneratorFactory.create(chunk);
                    generator.generate();
                    return chunk;
                });
            } else if (m instanceof Future<?> e) {
                if (!e.isDone()) {
                    continue;
                }
                try {
                    Object result = e.get();
                    if (result instanceof Chunk f) {
                        int chunkX = f.getChunkX();
                        int chunkZ = f.getChunkZ();
                        
                        deleteLayersIfPossible(chunkX + 1, chunkZ);
                        deleteLayersIfPossible(chunkX - 1, chunkZ);
                        deleteLayersIfPossible(chunkX, chunkZ + 1);
                        deleteLayersIfPossible(chunkX, chunkZ - 1);
                        this.map[i] = f;
                    } else {
                        throw new RuntimeException("what");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    private void deleteLayersIfPossible(int chunkX, int chunkZ) {
        Chunk c = getChunk(chunkX, chunkZ);
        if (c != null) {
            c.getLayers().delete();
        }
    }

    public void update() {
        updatePosition();
        moveAreas();
        finishUpdatePosition();
        updateChunks();
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return getLocalChunk(chunkX - this.chunkX, chunkZ - this.chunkZ);
    }
    
    public Chunk getLocalChunk(int chunkX, int chunkZ) {
        chunkX = chunkX + VIEW_DISTANCE;
        chunkZ = -chunkZ + VIEW_DISTANCE;
        
        int index = chunkX + (chunkZ * VIEW_DISTANCE_SIZE);
        if (index < 0 || index >= this.map.length) {
            return null;
        }
        Object m = this.map[index];
        if (m instanceof Chunk e) {
            return e;
        }
        return null;
    }

    public int length() {
        return this.map.length;
    }

    public Chunk chunkAtIndex(int i) {
        Object m = this.map[i];
        if (m instanceof Chunk e) {
            return e;
        }
        return null;
    }
    
    private void deleteLayer(int chunkX, int chunkZ, int y) {
        Chunk c = getLocalChunk(chunkX, chunkZ);
        if (c != null) {
            c.getLayers().layerAtY(y).delete();
        }
    }

    public void setWorldBlock(int x, int y, int z, Block block) {
        if (y >= Chunk.CHUNK_HEIGHT || y < 0) {
            return;
        }

        int cX = (int) Math.floor((float) x / Chunk.CHUNK_SIZE);
        int cZ = (int) Math.ceil((float) z / Chunk.CHUNK_SIZE);

        int camChunkX = cX - this.chunkX;
        int camChunkZ = cZ - this.chunkZ;

        if (camChunkX < -VIEW_DISTANCE || camChunkX > VIEW_DISTANCE) {
            return;
        }

        if (camChunkZ < -VIEW_DISTANCE || camChunkZ > VIEW_DISTANCE) {
            return;
        }

        int chunkBlockX = x - (cX * Chunk.CHUNK_SIZE);
        int chunkBlockZ = z - (cZ * Chunk.CHUNK_SIZE);

        if (chunkBlockX == 0) {
            deleteLayer(camChunkX - 1, camChunkZ, y);
        } else if (chunkBlockX == Chunk.CHUNK_SIZE - 1) {
            deleteLayer(camChunkX + 1, camChunkZ, y);
        }

        if (chunkBlockZ == 0) {
            deleteLayer(camChunkX, camChunkZ + 1, y);
        } else if (chunkBlockZ == -(Chunk.CHUNK_SIZE - 1)) {
            deleteLayer(camChunkX, camChunkZ - 1, y);
        }
        
        Chunk c = getLocalChunk(camChunkX, camChunkZ);
        if (c != null) {
            c.setBlock(
                    chunkBlockX,
                    y,
                    chunkBlockZ,
                    block
            );
        }
    }

    public Block getWorldBlock(int x, int y, int z) {
        if (y >= Chunk.CHUNK_HEIGHT || y < 0) {
            return Blocks.AIR;
        }

        int cX = (int) Math.floor((float) x / Chunk.CHUNK_SIZE);
        int cZ = (int) Math.ceil((float) z / Chunk.CHUNK_SIZE);

        int camChunkX = cX - this.chunkX;
        int camChunkZ = cZ - this.chunkZ;

        if (camChunkX < -VIEW_DISTANCE || camChunkX > VIEW_DISTANCE) {
            return Blocks.AIR;
        }

        if (camChunkZ < -VIEW_DISTANCE || camChunkZ > VIEW_DISTANCE) {
            return Blocks.AIR;
        }
        
        Chunk c = getLocalChunk(camChunkX, camChunkZ);
        if (c != null) {
            return c.getBlock(
                    x - (cX * Chunk.CHUNK_SIZE),
                    y,
                    z - (cZ * Chunk.CHUNK_SIZE)
            );
        }
        
        return Blocks.AIR;
    }

    public void render() {
        List<ChunkLayers> layers = new ArrayList<>();
        
        for (int i = 0; i < length(); i++) {
            Chunk c = chunkAtIndex(i);
            if (c != null) {
                layers.add(c.getLayers());
            }
        }
        
        ChunkLayersRender.render(this.camera, layers.toArray(ChunkLayers[]::new));
    }
    
    public Camera getCamera() {
        return camera;
    }

    public long getSeed() {
        return seed;
    }
    
}