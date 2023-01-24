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
import cientistavuador.ciencraftreal.block.BlockTextures;
import cientistavuador.ciencraftreal.block.BlockTransparency;
import cientistavuador.ciencraftreal.block.SimpleBlock;
import cientistavuador.ciencraftreal.block.material.BlockMaterial;

/**
 *
 * @author Cien
 */
public class Water extends SimpleBlock {
    
    public static final BlockMaterial WATER_MATERIAL;
    
    static {
        WATER_MATERIAL = BlockMaterial.create();
        WATER_MATERIAL.setColor(1f, 1f, 1f, 0.45f);
        WATER_MATERIAL.setFrameTime(1f);
        WATER_MATERIAL.setFrameStart(BlockTextures.WATER_FRAME_START);
        WATER_MATERIAL.setFrameEnd(BlockTextures.WATER_FRAME_END);
    }
    
    public Water() {
        super("ciencraft_water", WATER_MATERIAL.getTextureID());
    }

    @Override
    protected boolean shouldHideFaceVerticesForBlock(Block block) {
        if (block == this) {
            return true;
        }
        return super.shouldHideFaceVerticesForBlock(block);
    }
    
    @Override
    public BlockTransparency getBlockTransparency() {
        return BlockTransparency.COLORED_GLASS;
    }
    
}
