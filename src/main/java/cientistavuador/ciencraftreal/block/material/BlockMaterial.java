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
package cientistavuador.ciencraftreal.block.material;

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.ubo.ColorUBO;
import cientistavuador.ciencraftreal.ubo.MaterialUBO;
import cientistavuador.ciencraftreal.util.ObjectCleaner;

/**
 *
 * @author Cien
 */
public class BlockMaterial {
    
    public static BlockMaterial create() {
        return create(ColorUBO.DEFAULT, MaterialUBO.DEFAULT);
    }
    
    public static BlockMaterial create(ColorUBO colorUbo, MaterialUBO materialUbo) {
        int colorPointer = colorUbo.allocate();
        int materialPointer = materialUbo.allocate();
        BlockMaterial mat = new BlockMaterial(colorUbo, materialUbo, colorPointer, materialPointer);
        ObjectCleaner.get().register(mat, () -> {
            colorUbo.free(colorPointer);
            materialUbo.free(materialPointer);
        });
        return mat;
    }
    
    private final ColorUBO colorUbo;
    private final MaterialUBO materialUbo;
    private final int colorPointer;
    private final int materialPointer;
    
    private BlockMaterial(ColorUBO color, MaterialUBO material, int colorPointer, int materialPointer) {
        this.colorUbo = color;
        this.materialUbo = material;
        this.colorPointer = colorPointer;
        this.materialPointer = materialPointer;
        
        this.colorUbo.setColor(this.colorPointer, 1, 1, 1, 1);
        this.materialUbo.setColorPointer(this.materialPointer, this.colorPointer);
        this.materialUbo.setFrameTime(this.materialPointer, 0f);
        this.materialUbo.setFrameStart(this.materialPointer, 0);
        this.materialUbo.setFrameEnd(this.materialPointer, 0);
    }
    
    public int getTextureID() {
        return this.materialPointer + Main.MIN_TEXTURE_3D_SIZE_SUPPORTED;
    }
    
    public void setColorEnabled(boolean enabled) {
        if (enabled) {
            this.materialUbo.setColorPointer(this.materialPointer, this.colorPointer);
        } else {
            this.materialUbo.setColorPointer(this.materialPointer, ColorUBO.NULL);
        }
    }
    
    public void setColor(float r, float g, float b, float a) {
        this.colorUbo.setColor(this.colorPointer, r, g, b, a);
    }
    
    public void setFrameTime(float time) {
        this.materialUbo.setFrameTime(this.materialPointer, time);
    }
    
    public void setFrameStart(int frameStart) {
        this.materialUbo.setFrameStart(this.materialPointer, frameStart);
    }
    
    public void setFrameEnd(int frameEnd) {
        this.materialUbo.setFrameEnd(this.materialPointer, frameEnd);
    }
    
    public boolean isColorEnabled() {
        return this.materialUbo.getColorPointer(this.materialPointer) != ColorUBO.NULL;
    }
    
    public float getColor(int channel) {
        return this.colorUbo.getColor(this.colorPointer, channel);
    }
    
    public float getFrameTime() {
        return this.materialUbo.getFrameTime(this.materialPointer);
    }
    
    public int getFrameStart() {
        return this.materialUbo.getFrameStart(this.materialPointer);
    }
    
    public int getFrameEnd() {
        return this.materialUbo.getFrameEnd(this.materialPointer);
    }
    
}
