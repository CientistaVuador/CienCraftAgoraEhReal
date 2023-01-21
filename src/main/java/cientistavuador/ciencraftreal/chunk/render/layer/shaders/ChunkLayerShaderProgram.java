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
package cientistavuador.ciencraftreal.chunk.render.layer.shaders;

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayer;
import cientistavuador.ciencraftreal.util.ProgramCompiler;
import cientistavuador.ciencraftreal.util.UniformSetter;

/**
 *
 * @author Cien
 */
public class ChunkLayerShaderProgram {
    
    public static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform ivec3 layerBlockPos;
            
            uniform mat4 projection;
            uniform mat4 view;
            
            layout (location = 0) in vec3 inVertexPos;
            layout (location = 1) in vec2 inTexCoords;
            layout (location = 2) in int inVertexTextureID;
            layout (location = 3) in float inVertexAO;
            
            out vec2 inoutTexCoords;
            flat out int inoutTextureID;
            
            void main() {
                vec3 vertexPos = vec3(
                    inVertexPos.x * CHUNK_SIZE,
                    inVertexPos.y * LAYER_HEIGHT,
                    -inVertexPos.z * CHUNK_SIZE
                );
                vertexPos += vec3(layerBlockPos);
                vec2 texCoords = inTexCoords * TEX_COORDS_SIZE;
                
                inoutTexCoords = texCoords;
                inoutTextureID = inVertexTextureID;
                gl_Position = projection * view * vec4(vertexPos, 1.0);
            }
            """
                    .replace("CHUNK_SIZE", Integer.toString(Chunk.CHUNK_SIZE) + ".0")
                    .replace("LAYER_HEIGHT", Integer.toString(ChunkLayer.HEIGHT) + ".0")
                    .replace("TEX_COORDS_SIZE", Integer.toString(ChunkLayer.TEX_COORDS_MAX) + ".0")
            ;
    
    public static final String FRAGMENT_SHADER = 
            """
            #version 330 core
            
            uniform bool useAlpha;
            uniform sampler2DArray textures;
            
            in vec2 inoutTexCoords;
            flat in int inoutTextureID;
            
            layout (location = 0) out vec4 out_Color;
            
            void main() {
                vec4 color = texture(textures, vec3(inoutTexCoords, float(inoutTextureID)));
                vec4 output = color;
                if (!useAlpha) {
                    if (color.a < 0.5) {
                        discard;
                    }
                    output.a = 1.0;
                }
                out_Color = output;
            }
            """;
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    
    public static void sendUniforms(Camera camera, int chunkX, int blockY, int chunkZ, boolean useAlpha) {
        UniformSetter.setMatrix4f("projection", camera.getProjection());
        UniformSetter.setMatrix4f("view", camera.getView());
        
        UniformSetter.setVector3i("layerBlockPos", chunkX * Chunk.CHUNK_SIZE, blockY, chunkZ * Chunk.CHUNK_SIZE);
        UniformSetter.setInt("textures", 0);
        UniformSetter.setInt("useAlpha", (useAlpha ? 1 : 0));
    }
    
    private ChunkLayerShaderProgram() {
        
    }
}
