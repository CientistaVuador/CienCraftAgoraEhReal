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
package cientistavuador.ciencraftreal.resources.font;

/**
 *
 * @author Cien
 */
public class FontCharacter {
    
    private final int unicode;
    private final float advance;
    private final float planeBoundsLeft;
    private final float planeBoundsBottom;
    private final float planeBoundsRight;
    private final float planeBoundsTop;
    private final float atlasBoundsLeft;
    private final float atlasBoundsBottom;
    private final float atlasBoundsRight;
    private final float atlasBoundsTop;

    protected FontCharacter(int unicode, float advance, float planeBoundsLeft, float planeBoundsBottom, float planeBoundsRight, float planeBoundsTop, float atlasBoundsLeft, float atlasBoundsBottom, float atlasBoundsRight, float atlasBoundsTop) {
        this.unicode = unicode;
        this.advance = advance;
        this.planeBoundsLeft = planeBoundsLeft;
        this.planeBoundsBottom = planeBoundsBottom;
        this.planeBoundsRight = planeBoundsRight;
        this.planeBoundsTop = planeBoundsTop;
        this.atlasBoundsLeft = atlasBoundsLeft;
        this.atlasBoundsBottom = atlasBoundsBottom;
        this.atlasBoundsRight = atlasBoundsRight;
        this.atlasBoundsTop = atlasBoundsTop;
    }

    public int getUnicode() {
        return unicode;
    }

    public float getAdvance() {
        return advance;
    }

    public float getPlaneBoundsLeft() {
        return planeBoundsLeft;
    }

    public float getPlaneBoundsBottom() {
        return planeBoundsBottom;
    }

    public float getPlaneBoundsRight() {
        return planeBoundsRight;
    }

    public float getPlaneBoundsTop() {
        return planeBoundsTop;
    }

    public float getAtlasBoundsLeft() {
        return atlasBoundsLeft;
    }

    public float getAtlasBoundsBottom() {
        return atlasBoundsBottom;
    }

    public float getAtlasBoundsRight() {
        return atlasBoundsRight;
    }

    public float getAtlasBoundsTop() {
        return atlasBoundsTop;
    }
}
