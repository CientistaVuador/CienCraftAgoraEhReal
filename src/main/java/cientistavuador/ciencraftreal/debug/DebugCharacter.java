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
package cientistavuador.ciencraftreal.debug;

import cientistavuador.ciencraftreal.text.GLFont;
import cientistavuador.ciencraftreal.text.GLFonts;
import cientistavuador.ciencraftreal.util.ProgramCompiler;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class DebugCharacter {
    
    private static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform sampler2D atlasBounds;
            uniform int unicodeIndex;
            uniform float size;
            uniform vec2 translation;
            
            layout (location = 0) in int leftRight; //0-1
            layout (location = 1) in int bottomTop; //0-1
            
            out vec2 texCoords;
            
            void main() {
                int atlasBoundsSize = textureSize(atlasBounds, 0).x;
                ivec2 atlasBoundsPixel = ivec2((unicodeIndex * 2) % atlasBoundsSize, (unicodeIndex * 2) / atlasBoundsSize);
                
                vec4 planeBounds = texelFetch(atlasBounds, atlasBoundsPixel, 0);
                vec4 atlasBounds = texelFetch(atlasBounds, atlasBoundsPixel + ivec2(1, 0), 0);
                
                gl_Position = vec4((planeBounds[leftRight] * size) + translation.x, (planeBounds[2 + bottomTop] * size) + translation.y, 0.0, 1.0);
                texCoords = vec2(atlasBounds[leftRight], atlasBounds[2 + bottomTop]);
            }
            """;
    
    private static final String FRAGMENT_SHADER = 
            """
            #version 330 core
            
            uniform sampler2D atlas;
            
            in vec2 texCoords;
            
            layout (location = 0) out vec4 fragColor;
            
            float median(float r, float g, float b) {
                return max(min(r, g), min(max(r, g), b));
            }
            
            void main() {
                vec3 sdfColor = texture(atlas, texCoords).rgb;
                float distance = median(sdfColor.r, sdfColor.g, sdfColor.b);
                if (distance < 0.5) {
                    discard;
                }
                fragColor = vec4(1.0);
            }
            """;
    
    private static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final int ATLAS_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "atlas");
    private static final int ATLAS_BOUNDS_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "atlasBounds");
    private static final int UNICODE_INDEX_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "unicodeIndex");
    private static final int SIZE_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "size");
    private static final int TRANSLATION_LOCATION = glGetUniformLocation(SHADER_PROGRAM, "translation");
    
    private static final int VAO;
    
    static {
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //leftRight - 0,1
        //bottomTop - 0,1
        glBufferData(GL_ARRAY_BUFFER, new int[] {
            0,0,
            1,1,
            0,1,
            0,0,
            1,0,
            1,1
        }, GL_STATIC_DRAW);
        
        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(0, 1, GL_INT, 2 * Integer.BYTES, 0);
        
        glEnableVertexAttribArray(1);
        glVertexAttribIPointer(1, 1, GL_INT, 2 * Integer.BYTES, 1 * Integer.BYTES);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private GLFont font = GLFonts.ROBOTO_BOLD;
    
    public DebugCharacter() {
        
    }

    public GLFont getFont() {
        return font;
    }

    public void setFont(GLFont font) {
        this.font = font;
    }
    
    public void render(int unicode, float size, float x, float y) {
        glUseProgram(SHADER_PROGRAM);
        glBindVertexArray(VAO);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.font.getAtlasTexture());
        
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.font.getAtlasBoundsTexture());
        
        glUniform1i(ATLAS_LOCATION, 0);
        glUniform1i(ATLAS_BOUNDS_LOCATION, 1);
        glUniform1i(UNICODE_INDEX_LOCATION, this.font.getIndexOfUnicode(unicode));
        glUniform1f(SIZE_LOCATION, size);
        glUniform2f(TRANSLATION_LOCATION, x, y);
        
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        glBindVertexArray(VAO);
        glUseProgram(0);
    }
    
}
