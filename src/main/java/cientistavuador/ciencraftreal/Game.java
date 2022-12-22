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
package cientistavuador.ciencraftreal;

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.BlockRegister;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.debug.Triangle;
import cientistavuador.ciencraftreal.camera.FreeCamera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import static cientistavuador.ciencraftreal.chunk.Chunk.CHUNK_SIZE;
import cientistavuador.ciencraftreal.debug.DebugBlock;
import cientistavuador.ciencraftreal.debug.DebugCounter;

/**
 *
 * @author Cien
 */
public class Game {

    private static final Game GAME = new Game();

    public static Game get() {
        return GAME;
    }

    private final Triangle triangle = new Triangle();
    private final FreeCamera camera = new FreeCamera();
    private final DebugBlock block = new DebugBlock();
    private final Chunk chunk = new Chunk(65487321654L, 0, 0);

    private Game() {

    }

    public void start() {
        chunk.generateBlocks();

        DebugCounter counter = new DebugCounter("Chunk Generation Benchmark");

        for (int i = 0; i < 1000; i++) {
            counter.markStart("generation");
            chunk.generateBlocks();
            counter.markEnd("generation");
        }

        counter.print();

    }

    int currentY = 255;
    
    public void loop() {
        camera.updateMovement();
        triangle.render(camera.getProjection(), camera.getView());
        block.copySideTextures(Blocks.STONE);

        for (int i = 1; i < BlockRegister.numberOfRegisteredBlocks(); i++) {
            block.getModel().identity().translate(1 + (i * 1), 0, 0);
            block.copySideTextures(BlockRegister.getBlock(i));
            block.render(camera.getProjection(), camera.getView());
        }
        
        for (int z = 0; z >= -(CHUNK_SIZE - 1); z--) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                float worldX = (x + (chunk.getChunkX() * CHUNK_SIZE)) + 0.5f;
                float worldY = currentY + 0.5f;
                float worldZ = (-z + (chunk.getChunkZ() * CHUNK_SIZE)) - 0.5f;

                Block blockAt = chunk.getBlockImpl(x, currentY, z);

                if (blockAt == Blocks.AIR) {
                    continue;
                }

                block.getModel()
                        .identity()
                        .translate(
                                worldX,
                                worldY,
                                worldZ
                        );
                block.render(camera.getProjection(), camera.getView());
            }
        }
        currentY--;
        
        if (currentY <= Chunk.GENERATOR_DESIRED_MIN_HEIGHT) {
            currentY = Chunk.GENERATOR_DESIRED_MAX_HEIGHT;
        }
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }
}
