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

import cientistavuador.ciencraftreal.resources.image.NativeImage;
import java.util.HashMap;
import java.util.Map;

/**
 * native image must be freed
 * @author Cien
 */
public class Font {

    private final String name;
    
    private final NativeImage atlasImage;
    private final FontCharacter[] characters;
    private final Map<Integer, Integer> unicodeToIndexMap = new HashMap<>();
    private final int unknownCharacterIndex;

    protected Font(String name, NativeImage atlasImage, FontCharacter[] characters, int unknownCharacterUnicode) {
        this.name = name;
        this.atlasImage = atlasImage;
        this.characters = characters;
        
        int unknownCharacterUnicodeIndex = 0;
        for (int i = 0; i < this.characters.length; i++) {
            unicodeToIndexMap.put(this.characters[i].getUnicode(), i);
            if (this.characters[i].getUnicode() == unknownCharacterUnicode) {
                unknownCharacterUnicodeIndex = i;
            }
        }
        
        this.unknownCharacterIndex = unknownCharacterUnicodeIndex;
    }

    public String getName() {
        return name;
    }
    
    public NativeImage getAtlasImage() {
        return atlasImage;
    }
    
    public int getCharactersLength() {
        return this.characters.length;
    }
    
    public FontCharacter getCharacter(int index) {
        return this.characters[index];
    }
    
    public int getIndexOfUnicode(int unicode) {
        Integer index = this.unicodeToIndexMap.get(unicode);
        if (index == null) {
            return this.unknownCharacterIndex;
        }
        return index;
    }

    public int getUnknownCharacterIndex() {
        return unknownCharacterIndex;
    }
    
}
