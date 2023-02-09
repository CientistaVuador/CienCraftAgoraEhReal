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
package cientistavuador.ciencraftreal.player;

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.BlockSounds;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.world.WorldCamera;

/**
 *
 * @author Cien
 */
public class Player extends PlayerPhysicsInput {

    private boolean canPlayStepSound = true;
    private int stepBlockX = -1;
    private int stepBlockY = -1;
    private int stepBlockZ = -1;
    private int lastStepBlockX = -1;
    private int lastStepBlockY = -1;
    private int lastStepBlockZ = -1;

    public Player(WorldCamera world) {
        super(world);
    }

    @Override
    public void update() {
        super.update();

        this.stepBlockX = (int) Math.floor(getPosition().x());
        this.stepBlockY = (int) Math.floor(getPosition().y());
        this.stepBlockZ = (int) Math.ceil(getPosition().z());
        if (this.stepBlockX != this.lastStepBlockX || this.stepBlockY != this.lastStepBlockY || this.stepBlockZ != this.lastStepBlockZ) {
            Block collisionBlock = getCollisionBlockY();
            if (collisionBlock != Blocks.AIR) {
                double speedX = getSpeed().x();
                double speedZ = getSpeed().z();
                double lengthSquared = (speedX * speedX) + (speedZ * speedZ);

                if (this.canPlayStepSound && lengthSquared > 0.01) {
                    this.canPlayStepSound = false;
                    BlockSounds.play(collisionBlock.getStepSound(),
                            getPosition().x(),
                            getPosition().y(),
                            getPosition().z(),
                            () -> {
                                this.canPlayStepSound = true;
                            }
                    );
                }
            }

        }
        this.lastStepBlockX = this.stepBlockX;
        this.lastStepBlockY = this.stepBlockY;
        this.lastStepBlockZ = this.stepBlockZ;
    }

}
