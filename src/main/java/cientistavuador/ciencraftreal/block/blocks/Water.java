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
package cientistavuador.ciencraftreal.block.blocks;

import cientistavuador.ciencraftreal.block.Block;
import cientistavuador.ciencraftreal.block.BlockFacesVertices;
import cientistavuador.ciencraftreal.block.BlockSide;
import static cientistavuador.ciencraftreal.block.BlockSide.BOTTOM;
import static cientistavuador.ciencraftreal.block.BlockSide.EAST;
import static cientistavuador.ciencraftreal.block.BlockSide.NORTH;
import static cientistavuador.ciencraftreal.block.BlockSide.SOUTH;
import static cientistavuador.ciencraftreal.block.BlockSide.TOP;
import static cientistavuador.ciencraftreal.block.BlockSide.WEST;
import cientistavuador.ciencraftreal.block.BlockTextures;
import cientistavuador.ciencraftreal.block.BlockTransparency;
import cientistavuador.ciencraftreal.block.SolidBlockCheck;
import cientistavuador.ciencraftreal.block.StateOfMatter;
import cientistavuador.ciencraftreal.block.material.BlockMaterial;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesStream;

/**
 *
 * @author Cien
 */
public class Water implements Block, SolidBlockCheck {
    
    public static final BlockMaterial WATER_MATERIAL;
    
    static {
        WATER_MATERIAL = BlockMaterial.create();
        WATER_MATERIAL.setColor(1f, 1f, 1f, 0.5f);
        WATER_MATERIAL.setFrameTime(0.75f);
        WATER_MATERIAL.setFrameStart(BlockTextures.WATER_TOP_START);
        WATER_MATERIAL.setFrameEnd(BlockTextures.WATER_TOP_END);
    }
    
    public static final BlockMaterial WATER_MATERIAL_SIDE;
    
    static {
        WATER_MATERIAL_SIDE = BlockMaterial.create();
        WATER_MATERIAL_SIDE.setColor(1f, 1f, 1f, 0.5f);
        WATER_MATERIAL_SIDE.setFrameTime(0.4f);
        WATER_MATERIAL_SIDE.setFrameStart(BlockTextures.WATER_SIDE_START);
        WATER_MATERIAL_SIDE.setFrameEnd(BlockTextures.WATER_SIDE_END);
    }
    
    private final int textureId = WATER_MATERIAL.getTextureID();
    private final int textureIdSide = WATER_MATERIAL_SIDE.getTextureID();
    private final String name = "ciencraft_water";
    private int id = -1;
    
    public Water() {
        
    }

    @Override
    public StateOfMatter getStateOfMatter() {
        return StateOfMatter.LIQUID;
    }
    
    @Override
    public boolean isSolidBlock(Block block) {
        if (block == this) {
            return true;
        }
        return SolidBlockCheck.super.isSolidBlock(block);
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getId() {
        if (this.id == -1) {
            throw new RuntimeException("Unregistered block");
        }
        return this.id;
    }

    @Override
    public void setId(int id) {
        if (this.id != -1) {
            throw new RuntimeException("Registered block");
        }
        this.id = id;
    }

    @Override
    public void generateVertices(VerticesStream stream, Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        boolean[] sides = isSolidBlockSides(chunk, chunkBlockX, chunkBlockY, chunkBlockZ);
        
        boolean empty = true;
        for (int i = 0; i < sides.length; i++) {
            if (!sides[i]) {
                empty = false;
                break;
            }
        }
        if (empty) {
            return;
        }

        float chunkX = chunkBlockX + 0.5f;
        float chunkY = chunkBlockY + 0.5f;
        float chunkZ = chunkBlockZ - 0.5f;
        
        for (int i = 0; i < sides.length; i++) {
            if (sides[i]) {
                continue;
            }
            BlockSide side = BlockSide.sideOf(i);
            
            int texture = 0;
            switch (side) {
                case TOP, BOTTOM -> texture = this.textureId;
                case EAST, NORTH, SOUTH, WEST -> texture = this.textureIdSide;
            }
            
            BlockFacesVertices.generateFaceVertices(stream, side, chunkX, chunkY, chunkZ, texture);
        }
    }
    
    @Override
    public float[] generateVertices(Chunk chunk, int chunkBlockX, int chunkBlockY, int chunkBlockZ) {
        boolean[] sides = isSolidBlockSides(chunk, chunkBlockX, chunkBlockY, chunkBlockZ);
        
        int verticesSize = 0;
        for (int i = 0; i < sides.length; i++) {
            verticesSize += (!sides[i] ? Block.VERTEX_SIZE_ELEMENTS * 6 : 0);
        }

        if (verticesSize == 0) {
            return null;
        }

        float chunkX = chunkBlockX + 0.5f;
        float chunkY = chunkBlockY + 0.5f;
        float chunkZ = chunkBlockZ - 0.5f;
        
        float[] vertices = new float[verticesSize];
        int pos = 0;

        for (int i = 0; i < sides.length; i++) {
            if (sides[i]) {
                continue;
            }
            BlockSide side = BlockSide.sideOf(i);
            
            int texture = 0;
            switch (side) {
                case TOP, BOTTOM -> texture = this.textureId;
                case EAST, NORTH, SOUTH, WEST -> texture = this.textureIdSide;
            }
            
            float[] sideVertices = BlockFacesVertices.generateFaceVertices(side, chunkX, chunkY, chunkZ, texture);
            System.arraycopy(sideVertices, 0, vertices, pos, sideVertices.length);
            pos += sideVertices.length;
        }
        
        return vertices;
    }
    
    @Override
    public BlockTransparency getBlockTransparency() {
        return BlockTransparency.LIKE_COLORED_GLASS;
    }
}
