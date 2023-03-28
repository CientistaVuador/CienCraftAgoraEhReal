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

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.resources.font.Font;
import cientistavuador.ciencraftreal.resources.font.FontCharacter;
import cientistavuador.ciencraftreal.ubo.FontTextUBO;
import cientistavuador.ciencraftreal.util.ProgramCompiler;
import java.util.HashMap;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class GLFontRenderer {

    private static final String VERTEX_SHADER
            = 
            """
            #version 330 core
            
            uniform sampler2D atlasBounds;
            uniform float size;
            
            struct UnicodePoint {
                int unicodeIndex;
                float pxRange;
                vec2 origin;
            };
            
            layout (std140) uniform FontTextUBO {
                UnicodePoint text[FONT_TEXT_UBO_SIZE];
            };
            
            layout (location = 0) in int leftRight; //0-1
            layout (location = 1) in int bottomTop; //0-1
            
            out vec2 texCoords;
            flat out float pxRange;
            
            void main() {
                UnicodePoint point = text[gl_InstanceID];
                
                vec2 translation = point.origin;
                int unicodeIndex = point.unicodeIndex;
                
                int atlasBoundsSize = textureSize(atlasBounds, 0).x;
                ivec2 atlasBoundsPixel = ivec2((unicodeIndex * 2) % atlasBoundsSize, (unicodeIndex * 2) / atlasBoundsSize);
                
                vec4 planeBounds = texelFetch(atlasBounds, atlasBoundsPixel, 0);
                vec4 atlasBounds = texelFetch(atlasBounds, atlasBoundsPixel + ivec2(1, 0), 0);
                
                gl_Position = vec4((planeBounds[leftRight] * size) + translation.x, (planeBounds[2 + bottomTop] * size) + translation.y, -1.0, 1.0);
                texCoords = vec2(atlasBounds[leftRight], atlasBounds[2 + bottomTop]);
                pxRange = point.pxRange;
            }
            """;

    private static final String FRAGMENT_SHADER
            = 
            """
            #version 330 core
            
            uniform sampler2D atlas;
            uniform vec4 color;
            uniform float weight;
            
            in vec2 texCoords;
            flat in float pxRange;
            
            layout (location = 0) out vec4 fragColor;
            
            float median(float r, float g, float b) {
                return max(min(r, g), min(max(r, g), b));
            }
            
            void main() {
                vec3 sdfColor = texture(atlas, texCoords).rgb;
                float distance = median(sdfColor.r, sdfColor.g, sdfColor.b);
                float inverseWeight = 1.0 - weight;
                float screenPxDistance = pxRange * (distance - inverseWeight);
                float opacity = clamp(screenPxDistance + inverseWeight, 0.0, 1.0);
                if (opacity == 0.0) {
                    discard;
                }
                fragColor = color * vec4(vec3(1.0), opacity);
            }
            """;

    private static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER, new HashMap<>() {
        {
            put("FONT_TEXT_UBO_SIZE", Integer.toString(FontTextUBO.SIZE));
        }
    });
    private static final int ATLAS_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "atlas");
    private static final int ATLAS_BOUNDS_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "atlasBounds");
    private static final int SIZE_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "size");
    private static final int WEIGHT_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "weight");
    private static final int COLOR_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "color");
    private static final int FONT_TEXT_UBO_INDEX = glGetUniformBlockIndex(SHADER_PROGRAM, "FontTextUBO");

    private static final int VAO;

    static {
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //leftRight - 0,1
        //bottomTop - 0,1
        glBufferData(GL_ARRAY_BUFFER, new int[]{
            0, 0,
            1, 1,
            0, 1,
            0, 0,
            1, 0,
            1, 1
        }, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(0, 1, GL_INT, 2 * Integer.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribIPointer(1, 1, GL_INT, 2 * Integer.BYTES, 1 * Integer.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static void render(float x, float y, GLFontSpecification font, String text) {
        render(x, y, new GLFontSpecification[]{font}, new String[]{text});
    }

    public static void render(float x, float y, GLFontSpecification[] fonts, String[] texts) {
        if (fonts.length != texts.length) {
            throw new RuntimeException("Fonts and Texts must have the same length.");
        }

        float cursorReturnX = x;

        FontTextUBO ubo = FontTextUBO.DEFAULT;

        glUseProgram(SHADER_PROGRAM);
        glBindVertexArray(VAO);

        glUniform1i(ATLAS_LOCATION, 0);
        glUniform1i(ATLAS_BOUNDS_LOCATION, 1);

        glUniformBlockBinding(SHADER_PROGRAM, FONT_TEXT_UBO_INDEX, ubo.getBindingPoint());

        for (int i = 0; i < fonts.length; i++) {
            GLFontSpecification spec = fonts[i];
            GLFont font = spec.getFont();
            Font rawFont = font.getFont();

            float fontSize = spec.getSize();
            float spaceAdvance = font.getAdvance(font.getIndexOfUnicode(' '));

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, font.getAtlasTexture());

            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, font.getAtlasBoundsTexture());

            glUniform1f(SIZE_LOCATION, fontSize);
            glUniform4f(COLOR_LOCATION, spec.getColorRed(), spec.getColorGreen(), spec.getColorBlue(), spec.getColorAlpha());
            glUniform1f(WEIGHT_LOCATION, spec.getWeight());

            String text = texts[i];

            int length = text.length();
            for (int j = 0; j < length; j++) {
                int unicode = text.codePointAt(j);
                int unicodeIndex = font.getIndexOfUnicode(unicode);
                FontCharacter rawCharacter = rawFont.getCharacter(unicodeIndex);
                float advance = rawCharacter.getAdvance();

                boolean skipPush = false;

                switch (unicode) {
                    case '\r' -> {
                        x = cursorReturnX;
                        skipPush = true;
                    }
                    case '\n' -> {
                        y -= fontSize;
                        x = cursorReturnX;
                        skipPush = true;
                    }
                    case ' ' -> {
                        x += (fontSize * spaceAdvance);
                        skipPush = true;
                    }
                    case '\t' -> {
                        x += (fontSize * spaceAdvance * 4);
                        skipPush = true;
                    }
                }
                
                if (!skipPush) {
                    float charWidth = rawCharacter.getAtlasBoundsRight() - rawCharacter.getAtlasBoundsLeft();
                    float charHeight = rawCharacter.getAtlasBoundsTop() - rawCharacter.getAtlasBoundsBottom();
                    float quadWidth = ((rawCharacter.getPlaneBoundsRight() - rawCharacter.getPlaneBoundsLeft()) * fontSize) * Main.WIDTH;
                    float quadHeight = ((rawCharacter.getPlaneBoundsTop() - rawCharacter.getPlaneBoundsBottom()) * fontSize) * Main.HEIGHT;
                    
                    float pxRange = (((quadWidth/charWidth) * 4f) + ((quadHeight/charHeight) * 4f)) / 2f;
                    
                    ubo.push(unicodeIndex, pxRange, x, y);
                    x += (fontSize * advance);
                }

                if (!ubo.canPush() || (j >= length - 1)) {
                    ubo.flipAndUpdate();
                    glDrawArraysInstanced(GL_TRIANGLES, 0, 6, ubo.getLength());
                }
            }
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }
}
