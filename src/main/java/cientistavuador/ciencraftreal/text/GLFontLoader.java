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

import cientistavuador.ciencraftreal.resources.font.Font;
import cientistavuador.ciencraftreal.resources.font.FontCharacter;
import cientistavuador.ciencraftreal.resources.font.FontResources;
import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class GLFontLoader {

    public static final boolean DEBUG_OUTPUT = true;

    public static GLFont[] load(String[] fonts) {
        ArrayDeque<Future<Font>> futureResult = new ArrayDeque<>();
        Font[] result = new Font[fonts.length];

        for (String font : fonts) {
            if (DEBUG_OUTPUT) {
                System.out.println("Loading font '" + font + "'");
            }
            futureResult.add(CompletableFuture.supplyAsync(() -> {
                Font f = FontResources.load(font);
                if (DEBUG_OUTPUT) {
                    System.out.println("Finished loading font '" + font + "' with " + f.getCharactersLength() + " characters and a " + f.getAtlasImage().getWidth() + "x" + f.getAtlasImage().getHeight() + " atlas.");
                }
                return f;
            }));
        }

        Future<Font> futureFont;
        int index = 0;
        while ((futureFont = futureResult.poll()) != null) {
            try {
                result[index] = futureFont.get();
                index++;
            } catch (InterruptedException | ExecutionException ex) {
                for (int i = 0; i < index; i++) {
                    result[index].getAtlasImage().free();
                }
                throw new RuntimeException(ex);
            }
        }

        ArrayDeque<Future<float[]>> futureFontAtlasBoundsDeque = new ArrayDeque<>();
        int[] fontAtlasBoundsSizes = new int[result.length];

        index = 0;
        for (Font font : result) {
            if (DEBUG_OUTPUT) {
                System.out.println("Generating font atlas bounds of '" + font.getName() + "'");
            }
            final Font finalFont = font;
            final int finalIndex = index;
            futureFontAtlasBoundsDeque.add(CompletableFuture.supplyAsync(() -> {
                int fontAtlasBoundsSize = (int) pow(2.0, ceil(log(sqrt(finalFont.getCharactersLength() * 2)) / log(2.0)));
                float[] data = new float[(fontAtlasBoundsSize * fontAtlasBoundsSize) * 4];
                fontAtlasBoundsSizes[finalIndex] = fontAtlasBoundsSize;

                float atlasWidth = finalFont.getAtlasImage().getWidth();
                float atlasHeight = finalFont.getAtlasImage().getHeight();
                for (int i = 0; i < finalFont.getCharactersLength(); i++) {
                    FontCharacter character = finalFont.getCharacter(i);
                    int dataIndex = i * 2 * 4;
                    data[dataIndex + 0] = character.getPlaneBoundsLeft();
                    data[dataIndex + 1] = character.getPlaneBoundsRight();
                    data[dataIndex + 2] = character.getPlaneBoundsBottom();
                    data[dataIndex + 3] = character.getPlaneBoundsTop();

                    data[dataIndex + 4] = character.getAtlasBoundsLeft() / atlasWidth;
                    data[dataIndex + 5] = character.getAtlasBoundsRight() / atlasWidth;
                    data[dataIndex + 6] = character.getAtlasBoundsBottom() / atlasHeight;
                    data[dataIndex + 7] = character.getAtlasBoundsTop() / atlasHeight;
                }

                if (DEBUG_OUTPUT) {
                    System.out.println("Finished generating font atlas bounds of '" + font.getName() + "'");
                }
                return data;
            }));
            index++;
        }

        int[] fontAtlasTextures = new int[result.length];

        index = 0;
        for (Font font : result) {
            if (DEBUG_OUTPUT) {
                System.out.println("Uploading atlas image of '" + font.getName() + "' to the GPU.");
            }
            int texture = glGenTextures();
            fontAtlasTextures[index] = texture;
            index++;

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    font.getAtlasImage().getWidth(),
                    font.getAtlasImage().getHeight(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    font.getAtlasImage().getData()
            );

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glBindTexture(GL_TEXTURE_2D, 0);

            if (DEBUG_OUTPUT) {
                System.out.println("Finished uploading atlas image of '" + font.getName() + "' to the GPU.");
            }
        }

        int[] fontAtlasBoundsTextures = new int[result.length];

        Future<float[]> futureFontAtlasData;
        index = 0;
        while ((futureFontAtlasData = futureFontAtlasBoundsDeque.poll()) != null) {
            Font font = result[index];
            int size = fontAtlasBoundsSizes[index];
            
            float[] data;
            try {
                data = futureFontAtlasData.get();
            } catch (InterruptedException | ExecutionException ex) {
                for (int i = 0; i < index; i++) {
                    glDeleteTextures(fontAtlasBoundsTextures[i]);
                }
                for (int i = 0; i < fontAtlasTextures.length; i++) {
                    glDeleteTextures(fontAtlasTextures[i]);
                }
                for (Font f : result) {
                    f.getAtlasImage().free();
                }
                throw new RuntimeException(ex);
            }

            if (DEBUG_OUTPUT) {
                System.out.println("Uploading atlas bounds image of '" + font.getName() + "' to the GPU. ("+size+"x"+size+")");
            }
            
            int texture = glGenTextures();
            fontAtlasBoundsTextures[index] = texture;
            index++;

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA32F,
                    size,
                    size,
                    0,
                    GL_RGBA,
                    GL_FLOAT,
                    data
            );

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[] {0f, 0f, 0f, 0f});
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
            
            glBindTexture(GL_TEXTURE_2D, 0);

            if (DEBUG_OUTPUT) {
                System.out.println("Finished uploading atlas bounds image of '" + font.getName() + "' to the GPU. ("+size+"x"+size+")");
            }
        }
        
        GLFont[] finalOutput = new GLFont[result.length];
        
        for (int i = 0; i < result.length; i++) {
            Font font = result[i];
            
            String name = font.getName();
            int atlasTexture = fontAtlasTextures[i];
            int atlasBoundsTexture = fontAtlasBoundsTextures[i];
            float[] advance = new float[font.getCharactersLength()];
            for (int j = 0; j < advance.length; j++) {
                advance[j] = font.getCharacter(j).getAdvance();
            }
            int unknownCharacterIndex = font.getUnknownCharacterIndex();
            Map<Integer, Integer> unicodeMap = new HashMap<>();
            for (int j = 0; j < advance.length; j++) {
                unicodeMap.put(font.getCharacter(j).getUnicode(), j);
            }
            
            finalOutput[i] = new GLFont(name, atlasTexture, atlasBoundsTexture, advance, unknownCharacterIndex, unicodeMap);
            
            if (DEBUG_OUTPUT) {
                System.out.println("Finished Loading GLFont "+name+", atlas "+font.getAtlasImage().getWidth()+"x"+font.getAtlasImage().getHeight()+", atlas bounds "+fontAtlasBoundsSizes[i]+"x"+fontAtlasBoundsSizes[i]+", characters "+font.getCharactersLength());
            }
        }
        
        return finalOutput;
    }

    private GLFontLoader() {

    }

}
