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

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.ChunkManager;
import java.util.Arrays;

/**
 *
 * @author Cien
 */
public class WorldCamera {

    public static final int VIEW_DISTANCE = 10;
    public static final int VIEW_DISTANCE_SIZE = (VIEW_DISTANCE * 2) + 1;
    public static final int VIEW_DISTANCE_NUMBER_OF_CHUNKS = VIEW_DISTANCE_SIZE * VIEW_DISTANCE_SIZE;

    private final ChunkManager[] map = new ChunkManager[VIEW_DISTANCE_NUMBER_OF_CHUNKS];

    private final long seed;
    private final Camera camera;
    private int oldChunkX = 0;
    private int oldChunkZ = 0;
    private int chunkX = 0;
    private int chunkZ = 0;

    public WorldCamera(Camera camera, long seed) {
        this.camera = camera;
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    private void updateChunks() {
        for (int i = 0; i < VIEW_DISTANCE_NUMBER_OF_CHUNKS; i++) {
            int x = (i % VIEW_DISTANCE_SIZE) - VIEW_DISTANCE;
            int z = -((i / VIEW_DISTANCE_SIZE) - VIEW_DISTANCE);
            
            ChunkManager m = this.map[i];
            if (m == null) {
                m = new ChunkManager(new Chunk(this, this.chunkX + x, this.chunkZ + z));
                this.map[i] = m;
            }

            m.update();
        }
    }

    private void updatePosition() {
        int camChunkX = (int) Math.floor(camera.getPosition().x() / Chunk.CHUNK_SIZE);
        int camChunkZ = (int) Math.floor(camera.getPosition().z() / Chunk.CHUNK_SIZE);

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
                ChunkManager m = this.map[i];
                if (m != null) {
                    m.delete();
                }
            }

            Arrays.fill(this.map, null);
            return;
        }

        if (translationX == 0 && translationZ == 0) {
            return;
        }

        ChunkManager[] copy = this.map.clone();
        Arrays.fill(this.map, null);

        for (int i = 0; i < copy.length; i++) {
            ChunkManager m = copy[i];
            
            if (m == null) {
                continue;
            }
            
            int newCamRelativeX = m.getChunk().getChunkX() - this.chunkX;
            int newCamRelativeZ = m.getChunk().getChunkZ() - this.chunkZ;

            boolean outOfBoundsX = (newCamRelativeX > VIEW_DISTANCE) || (newCamRelativeX < -VIEW_DISTANCE);
            boolean outOfBoundsZ = (newCamRelativeZ > VIEW_DISTANCE) || (newCamRelativeZ < -VIEW_DISTANCE);

            if (outOfBoundsX || outOfBoundsZ) {
                m.delete();
            } else {
                this.map[(newCamRelativeX + VIEW_DISTANCE) + ((-newCamRelativeZ + VIEW_DISTANCE) * VIEW_DISTANCE_SIZE)] = m;
            }
        }
    }

    private void finishUpdatePosition() {
        this.oldChunkX = this.chunkX;
        this.oldChunkZ = this.chunkZ;
    }

    public void update() {
        updatePosition();
        moveAreas();
        finishUpdatePosition();
        updateChunks();
    }

    public void render() {
        for (int i = 0; i < VIEW_DISTANCE_NUMBER_OF_CHUNKS; i++) {
            ChunkManager m = this.map[i];
            if (m != null && m.isFinished()) {
                m.getRenderableChunk().render(this.camera.getProjection(), this.camera.getView());
            }
        }
    }

}
