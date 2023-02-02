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

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.block.StateOfMatter;
import cientistavuador.ciencraftreal.util.AabRender;
import cientistavuador.ciencraftreal.world.WorldCamera;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 *
 * @author Cien
 */
public class PlayerPhysics {

    public static final boolean DEBUG = true;
    public static final float GRAVITY = 9.8f;
    public static final float GRAVITY_LIQUID_SPEED = 0.5f;

    public static final float HEIGHT = 1.65f;
    public static final float EYES_HEIGHT = 1.5f;
    public static final float WIDTH_DEPTH = 0.60f;

    private final Vector3d position = new Vector3d(0, 0, 0);
    private final Vector3d speed = new Vector3d(0, 0, 0);
    private final Vector3d min = new Vector3d(-(WIDTH_DEPTH / 2f), 0, -(WIDTH_DEPTH / 2f));
    private final Vector3d max = new Vector3d((WIDTH_DEPTH / 2f), HEIGHT, (WIDTH_DEPTH / 2f));
    private final WorldCamera world;

    private Block collisionBlockX = Blocks.AIR;
    private Block collisionBlockY = Blocks.AIR;
    private Block collisionBlockZ = Blocks.AIR;
    private Block feetBlock = Blocks.AIR;

    private boolean onLiquid = false;

    public PlayerPhysics(WorldCamera world) {
        this.world = world;
    }

    public WorldCamera getWorld() {
        return world;
    }

    public Vector3dc getMin() {
        return min;
    }

    public Vector3dc getMax() {
        return max;
    }

    public Vector3dc getPosition() {
        return position;
    }

    public Vector3dc getSpeed() {
        return speed;
    }

    public void setPosition(Vector3dc pos) {
        setPosition(pos.x(), pos.y(), pos.z());
    }

    public void setPosition(double x, double y, double z) {
        double half = WIDTH_DEPTH / 2.0;
        this.min.set(x - half, y, z - half);
        this.max.set(x + half, y + HEIGHT, z + half);
        this.position.set(x, y, z);
    }

    public void setSpeed(Vector3dc speed) {
        this.speed.set(speed);
    }

    public void setSpeed(double x, double y, double z) {
        this.speed.set(x, y, z);
    }

    public void addSpeed(Vector3dc speed) {
        this.speed.add(speed);
    }

    public void addSpeed(double x, double y, double z) {
        this.speed.add(x, y, z);
    }

    public Block getCollisionBlockX() {
        return collisionBlockX;
    }

    public Block getCollisionBlockY() {
        return collisionBlockY;
    }

    public Block getCollisionBlockZ() {
        return collisionBlockZ;
    }

    public Block getFeetBlock() {
        return feetBlock;
    }

    public boolean isOnLiquid() {
        return onLiquid;
    }

    public void update() {
        if (DEBUG) {
            AabRender.queueRender(
                    this.min.x(),
                    this.min.y(),
                    this.min.z(),
                    this.max.x(),
                    this.max.y(),
                    this.max.z()
            );
        }

        addSpeed(0, -GRAVITY * Main.TPF, 0);
        
        this.feetBlock = this.world.getWorldBlock(
                (int) Math.floor(this.position.x()),
                (int) Math.floor(this.position.y()),
                (int) Math.ceil(this.position.z())
        );

        this.collisionBlockX = Blocks.AIR;
        this.collisionBlockY = Blocks.AIR;
        this.collisionBlockZ = Blocks.AIR;
        this.onLiquid = false;

        double xSpeed = getSpeed().x();
        double ySpeed = getSpeed().y();
        double zSpeed = getSpeed().z();
        double xStore = this.position.x();
        double yStore = this.position.y();
        double zStore = this.position.z();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    int blockX = (int) (x + Math.floor(this.position.x()));
                    int blockY = (int) (y + Math.floor(this.position.y()));
                    int blockZ = (int) (z + Math.ceil(this.position.z()));

                    Block block = this.world.getWorldBlock(blockX, blockY, blockZ);

                    if (block == Blocks.AIR || StateOfMatter.GAS.equals(block.getStateOfMatter())) {
                        continue;
                    }
                    
                    if (StateOfMatter.LIQUID.equals(block.getStateOfMatter()) && this.onLiquid) {
                        continue;
                    }
                    
                    if (xSpeed == 0.0 && ySpeed == 0.0 && zSpeed == 0.0 && !StateOfMatter.LIQUID.equals(block.getStateOfMatter())) {
                        continue;
                    }

                    if (DEBUG) {
                        AabRender.queueRender(
                                blockX,
                                blockY,
                                blockZ - 1,
                                blockX + 1,
                                blockY + 1,
                                blockZ
                        );
                    }
                    
                    if (StateOfMatter.LIQUID.equals(block.getStateOfMatter())) {
                        if (block.checkCollision(blockX, blockY, blockZ, this)) {
                            this.onLiquid = true;
                        }
                        continue;
                    }
                    
                    boolean changed = false;
                    if (xSpeed != 0.0) {
                        setPosition(xStore + (xSpeed * Main.TPF), yStore, zStore);
                        if (block.checkCollision(blockX, blockY, blockZ, this)) {
                            xSpeed = 0.0;
                            this.collisionBlockX = block;
                        }
                        changed = true;
                    }
                    if (ySpeed != 0.0) {
                        setPosition(xStore, yStore + (ySpeed * Main.TPF), zStore);
                        if (block.checkCollision(blockX, blockY, blockZ, this)) {
                            ySpeed = 0.0;
                            this.collisionBlockY = block;
                        }
                        changed = true;
                    }
                    if (zSpeed != 0.0) {
                        setPosition(xStore, yStore, zStore + (zSpeed * Main.TPF));
                        if (block.checkCollision(blockX, blockY, blockZ, this)) {
                            zSpeed = 0.0;
                            this.collisionBlockZ = block;
                        }
                        changed = true;
                    }

                    if (changed) {
                        setPosition(xStore, yStore, zStore);
                    }

                }
            }
        }
        setSpeed(xSpeed, ySpeed, zSpeed);
        setPosition(
            xStore + (xSpeed * Main.TPF),
            yStore + (ySpeed * Main.TPF),
            zStore + (zSpeed * Main.TPF)
        );
    }

}
