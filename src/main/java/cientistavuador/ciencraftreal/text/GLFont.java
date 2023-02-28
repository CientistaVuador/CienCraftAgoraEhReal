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

import java.util.Map;

/**
 *
 * @author Cien
 */
public class GLFont {
    
    private final String name;
    private final int atlasTexture;
    private final int atlasBoundsTexture;
    private final float[] advance;
    private final int unknownCharacterIndex;
    private final Map<Integer, Integer> unicodeMap;

    protected GLFont(String name, int atlasTexture, int atlasBoundsTexture, float[] advance, int unknownCharacterIndex, Map<Integer, Integer> unicodeMap) {
        this.name = name;
        this.atlasTexture = atlasTexture;
        this.atlasBoundsTexture = atlasBoundsTexture;
        this.advance = advance;
        this.unknownCharacterIndex = unknownCharacterIndex;
        this.unicodeMap = unicodeMap;
    }

    public String getName() {
        return this.name;
    }
    
    public int getAtlasTexture() {
        return this.atlasTexture;
    }

    public int getAtlasBoundsTexture() {
        return this.atlasBoundsTexture;
    }

    public int length() {
        return this.advance.length;
    }
    
    public float getAdvance(int index) {
        return this.advance[index];
    }
    
    public int getUnknownCharacterIndex() {
        return this.unknownCharacterIndex;
    }
    
    public int getIndexOfUnicode(int unicode) {
        Integer index = this.unicodeMap.get(unicode);
        if (index == null) {
            return this.unknownCharacterIndex;
        }
        return index;
    }
    
}
