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
package cientistavuador.ciencraftreal.text;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 *
 * @author Cien
 */
public class GLFontSpecification {

    public static final float BOLD = 0.75f;
    public static final float NORMAL = 0.5f;
    public static final float THIN = 0.25f;
    
    private final GLFont font;
    private final float size;
    private final float weight;
    private final float colorRed;
    private final float colorGreen;
    private final float colorBlue;
    private final float colorAlpha;

    public GLFontSpecification(GLFont font, float size, float weight, float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        this.font = font;
        this.size = size;
        this.weight = weight;
        this.colorRed = colorRed;
        this.colorGreen = colorGreen;
        this.colorBlue = colorBlue;
        this.colorAlpha = colorAlpha;
    }

    public GLFontSpecification(GLFont font, float size) {
        this(font, size, NORMAL, 1f, 1f, 1f, 1f);
    }

    public GLFont getFont() {
        return font;
    }

    public float getSize() {
        return size;
    }

    public float getWeight() {
        return weight;
    }

    public float getColorRed() {
        return colorRed;
    }

    public float getColorGreen() {
        return colorGreen;
    }

    public float getColorBlue() {
        return colorBlue;
    }

    public float getColorAlpha() {
        return colorAlpha;
    }
    
    public void getColor(Vector4f rec) {
        rec.set(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha);
    }
    
    public void getColor(Vector3f rec) {
        rec.set(this.colorRed, this.colorGreen, this.colorBlue);
    }
    
    public GLFontSpecification withFont(GLFont font) {
        return new GLFontSpecification(font, this.size, this.weight, this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha);
    }
    
    public GLFontSpecification withSize(float size) {
        return new GLFontSpecification(this.font, size, this.weight, this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha);
    }
    
    public GLFontSpecification withWeight(float weight) {
        return new GLFontSpecification(this.font, this.size, weight, this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha);
    }
    
    public GLFontSpecification withColor(float red, float green, float blue, float alpha) {
        return new GLFontSpecification(this.font, this.size, this.weight, red, green, blue, alpha);
    }
    
    public GLFontSpecification withColor(Vector3fc color) {
        return withColor(color.x(), color.y(), color.z(), 1f);
    }
    
    public GLFontSpecification withColor(Vector4fc color) {
        return withColor(color.x(), color.y(), color.z(), color.w());
    }
    
}
