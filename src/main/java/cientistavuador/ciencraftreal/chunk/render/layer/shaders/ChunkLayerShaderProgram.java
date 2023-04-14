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

import cientistavuador.ciencraftreal.Main;
import cientistavuador.ciencraftreal.block.BlockTextures;
import cientistavuador.ciencraftreal.ubo.ColorUBO;
import cientistavuador.ciencraftreal.ubo.MaterialUBO;
import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.chunk.Chunk;
import cientistavuador.ciencraftreal.chunk.render.layer.ChunkLayer;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import cientistavuador.ciencraftreal.util.ProgramCompiler;
import java.util.Map;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ChunkLayerShaderProgram {
    
    public static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform ivec3 layerBlockPos;
            uniform float time;
            
            layout (location = 0) in vec3 inVertexPos;
            layout (location = 1) in vec4 inVertexNormal;
            layout (location = 2) in vec2 inTexCoords;
            layout (location = 3) in int inVertexTextureID;
            layout (location = 4) in float inVertexAO;
            
            layout (std140) uniform Camera {
                mat4 projection;
                mat4 view;
                ivec4 icamPos;
                vec4 dcamPos;
            };
            
            layout (std140) uniform BlockColors {
                vec4 colors[COLORS_UBO_SIZE];
            };
            
            struct BlockMaterial {
                int colorPointer;
                float frameTime;
                int frameStart;
                int frameEnd;
            };
            
            layout (std140) uniform BlockMaterials {
                BlockMaterial materials[MATERIALS_UBO_SIZE];
            };
            
            out Vertex {
                vec3 position;
                vec3 normal;
                vec2 texCoords;
                float ao;
                flat int textureID;
                flat int hasColor;
                flat vec4 color;
            } VOut;
            
            void main() {
                vec3 vertexPos = vec3(
                    inVertexPos.x * CHUNK_SIZE,
                    inVertexPos.y * LAYER_HEIGHT,
                    -inVertexPos.z * CHUNK_SIZE
                );
                vertexPos += vec3(layerBlockPos - icamPos.xyz);
                vertexPos -= dcamPos.xyz;
                vec2 texCoords = inTexCoords * TEX_COORDS_SIZE;
                
                VOut.hasColor = int(false);
            
                int texture = inVertexTextureID;
                if (texture >= MIN_TEXTURE_3D_SIZE_SUPPORTED) {
                    texture -= MIN_TEXTURE_3D_SIZE_SUPPORTED;
                    
                    BlockMaterial material = materials[texture];
                    
                    if (material.frameTime != 0.0) {
                        int frameLength = material.frameEnd - material.frameStart;
                        int currentFrame = material.frameStart + (int(time / material.frameTime) % frameLength);
                        texture = currentFrame;
                    } else {
                        texture = material.frameStart;
                    }
                    
                    if (material.colorPointer != NULL_COLOR_POINTER) {
                        VOut.hasColor = int(true);
                        VOut.color = colors[material.colorPointer];
                    }
                }
                
                VOut.position = vertexPos;
                VOut.normal = (inVertexNormal.xyz - 0.5) * 2.0;
                VOut.texCoords = texCoords;
                VOut.textureID = texture;
                VOut.ao = inVertexAO;
                gl_Position = projection * view * vec4(vertexPos, 1.0);
            }
            """;
    
    public static final String FRAGMENT_SHADER = 
            """
            #version 330 core
            
            uniform bool useAlpha;
            uniform sampler2DArray textures;
            
            in Vertex {
                vec3 position;
                vec3 normal;
                vec2 texCoords;
                float ao;
                flat int textureID;
                flat int hasColor;
                flat vec4 color;
            } FIn;
            
            layout (location = 0) out vec4 out_Color;
            
            void main() {
                vec4 color = texture(textures, vec3(FIn.texCoords, float(FIn.textureID)));
                vec4 outputColor = color;
                if (bool(FIn.hasColor)) {
                    outputColor *= FIn.color;
                }
                if (!useAlpha) {
                    if (outputColor.a < 0.5) {
                        discard;
                    }
                    outputColor.a = 1.0;
                }
                
                const float gamma = 2.2;
                outputColor.rgb = pow(outputColor.rgb, vec3(gamma));
                
                outputColor.rgb *= 1.0 - FIn.ao;
                
                outputColor.rgb = pow(outputColor.rgb, vec3(1.0/gamma));
                out_Color = outputColor;
            }
            """;
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(
            VERTEX_SHADER,
            FRAGMENT_SHADER,
            Map.of(
                    "CHUNK_SIZE", Integer.toString(Chunk.CHUNK_SIZE) + ".0",
                    "LAYER_HEIGHT", Integer.toString(ChunkLayer.HEIGHT) + ".0",
                    "TEX_COORDS_SIZE", Integer.toString(ChunkLayer.TEX_COORDS_MAX) + ".0",
                    "MIN_TEXTURE_3D_SIZE_SUPPORTED", Integer.toString(Main.MIN_TEXTURE_3D_SIZE_SUPPORTED),
                    "COLORS_UBO_SIZE", Integer.toString(ColorUBO.SIZE),
                    "MATERIALS_UBO_SIZE", Integer.toString(MaterialUBO.SIZE),
                    "NULL_COLOR_POINTER", Integer.toString(ColorUBO.NULL)
            )
    );
    
    public static final int BLOCK_COLORS_UBO_INDEX = glGetUniformBlockIndex(SHADER_PROGRAM, "BlockColors");
    public static final int BLOCK_MATERIALS_UBO_INDEX = glGetUniformBlockIndex(SHADER_PROGRAM, "BlockMaterials");
    public static final int LAYER_BLOCK_POS_PROGRAM_INDEX = glGetUniformLocation(SHADER_PROGRAM, "layerBlockPos");
    public static final int TEXTURES_PROGRAM_INDEX = glGetUniformLocation(SHADER_PROGRAM, "textures");
    public static final int USE_ALPHA_PROGRAM_INDEX = glGetUniformLocation(SHADER_PROGRAM, "useAlpha");
    public static final int TIME_PROGRAM_INDEX = glGetUniformLocation(SHADER_PROGRAM, "time");
    public static final int CAMERA_UBO_INDEX = glGetUniformBlockIndex(SHADER_PROGRAM, "Camera");
    
    public static void sendPerFrameUniforms(Camera camera) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, BlockTextures.GL_TEXTURE_ARRAY);
        
        glUniform1i(TEXTURES_PROGRAM_INDEX, 0);
        glUniform1f(TIME_PROGRAM_INDEX, (float) Main.ONE_MINUTE_COUNTER);
        
        CameraUBO cameraUbo = camera.getUBO();
        if (cameraUbo == null) {
            throw new NullPointerException("Camera UBO is null");
        }
        
        glUniformBlockBinding(SHADER_PROGRAM, CAMERA_UBO_INDEX, cameraUbo.getBindingPoint());
        glUniformBlockBinding(SHADER_PROGRAM, BLOCK_COLORS_UBO_INDEX, ColorUBO.DEFAULT.getBindingPoint());
        glUniformBlockBinding(SHADER_PROGRAM, BLOCK_MATERIALS_UBO_INDEX, MaterialUBO.DEFAULT.getBindingPoint());
        
        cameraUbo.updateUBO();
        ColorUBO.DEFAULT.updateUBO();
        MaterialUBO.DEFAULT.updateUBO();
    }
    
    public static void sendUseAlphaUniform(boolean useAlpha) {
        glUniform1i(USE_ALPHA_PROGRAM_INDEX, (useAlpha ? 1 : 0));
    }
    
    public static void sendPerDrawUniforms(int chunkX, int blockY, int chunkZ) {
        glUniform3i(LAYER_BLOCK_POS_PROGRAM_INDEX, chunkX * Chunk.CHUNK_SIZE, blockY, chunkZ * Chunk.CHUNK_SIZE);
    }
    
    public static void finishRendering() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }
    
    private ChunkLayerShaderProgram() {
        
    }
}
