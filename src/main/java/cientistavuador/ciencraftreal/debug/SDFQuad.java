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

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.resources.image.NativeImage;
import cientistavuador.ciencraftreal.ubo.CameraUBO;
import cientistavuador.ciencraftreal.util.ProgramCompiler;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SDFQuad {

    private static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform ivec3 intPos;
            uniform vec3 decPos;
            uniform vec3 scale;
            
            layout (std140) uniform Camera {
                mat4 projection;
                mat4 view;
                ivec4 icamPos;
                vec4 dcamPos;
            };
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec2 vertexTexCoords;
            
            out vec2 texCoords;
            
            void main() {
                vec3 resultVertex = (vertexPosition * scale) + vec3(intPos - icamPos.xyz) + (decPos - dcamPos.xyz);
                texCoords = vertexTexCoords;
                gl_Position = projection * view * vec4(resultVertex, 1.0);
            }
            """;
    
    private static final String FRAGMENT_SHADER = 
            """
            #version 330 core
            
            uniform vec4 color;
            uniform sampler2D quadTexture;
            uniform bool multiChannelSDF;
            uniform float thickness;
            
            in vec2 texCoords;
            
            layout (location = 0) out vec4 fragColor;
            
            float median(float r, float g, float b) {
                return max(min(r, g), min(max(r, g), b));
            }
            
            void main() {
                vec3 sdfColor = texture(quadTexture, texCoords).rgb;
                float distance = 0.0;
                if (multiChannelSDF) {
                    distance = median(sdfColor.r, sdfColor.g, sdfColor.b);
                } else {
                    distance = sdfColor.r;
                }
                if (distance < (1.0 - thickness)) {
                    discard;
                }
                fragColor = color;
            }
            """;
    
    private static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final int INT_POS_INDEX = glGetUniformLocation(SHADER_PROGRAM, "intPos");
    private static final int DEC_POS_INDEX = glGetUniformLocation(SHADER_PROGRAM, "decPos");
    private static final int SCALE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "scale");
    private static final int COLOR_INDEX = glGetUniformLocation(SHADER_PROGRAM, "color");
    private static final int QUAD_TEXTURE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "quadTexture");
    private static final int MULTI_CHANNEL_SDF_INDEX = glGetUniformLocation(SHADER_PROGRAM, "multiChannelSDF");
    private static final int THICKNESS_INDEX = glGetUniformLocation(SHADER_PROGRAM, "thickness");
    private static final int CAMERA_UBO_INDEX = glGetUniformBlockIndex(SHADER_PROGRAM, "Camera");
    
    private static final int VAO;
    
    static {
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
            -0.5f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 1.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.5f, 1.0f, 0.0f, 1.0f, 1.0f,
            
            0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.0f, 0.0f, 0.0f, 0.0f,
            -0.5f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
            -0.5f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 1.0f, 0.0f, 1.0f, 1.0f
        }, GL_STATIC_DRAW);
        
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, (5 * Float.BYTES), 0);
        
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, (5 * Float.BYTES), (3 * Float.BYTES));
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public static void render(Camera camera, SDFQuad[] quads) {
        CameraUBO camUbo = camera.getUBO();
        if (camUbo == null) {
            throw new NullPointerException("Camera UBO not found.");
        }
        
        glUseProgram(SHADER_PROGRAM);
        glBindVertexArray(VAO);
        
        glActiveTexture(GL_TEXTURE0);
        glUniform1i(QUAD_TEXTURE_INDEX, 0);
        glUniformBlockBinding(SHADER_PROGRAM, CAMERA_UBO_INDEX, camUbo.getBindingPoint());
        
        for (SDFQuad quad : quads) {
            quad.render();
        }
        
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    private final NativeImage image;
    private final Vector3d position = new Vector3d(0);
    private final Vector3f scale = new Vector3f(1);
    private final Vector4f color = new Vector4f(1);

    private final Vector3i intPart = new Vector3i(0);
    private final Vector3f decPart = new Vector3f(0);
    private boolean multiChannelSDF;
    private float thickness = 0.5f;

    public SDFQuad(NativeImage image, boolean multiChannelSDF) {
        this.image = image;
        this.multiChannelSDF = multiChannelSDF;
    }
    
    public SDFQuad(NativeImage image) {
        this(image, false);
    }

    public NativeImage getImage() {
        return image;
    }

    public boolean isMultiChannelSDF() {
        return multiChannelSDF;
    }

    public void setMultiChannelSDF(boolean multiChannelSDF) {
        this.multiChannelSDF = multiChannelSDF;
    }

    public float getThickness() {
        return thickness;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }
    
    public Vector3dc getPosition() {
        return position;
    }

    public void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);

        int xInt = (int) Math.floor(position.x());
        int yInt = (int) Math.floor(position.y());
        int zInt = (int) Math.ceil(position.z());
        float xDec = (float) (position.x() - xInt);
        float yDec = (float) (position.y() - yInt);
        float zDec = (float) (position.z() - zInt);
        
        this.intPart.set(xInt, yInt, zInt);
        this.decPart.set(xDec, yDec, zDec);
    }

    public void setPosition(Vector3dc position) {
        setPosition(position.x(), position.y(), position.z());
    }

    public Vector3fc getScale() {
        return scale;
    }
    
    public void setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
    }
    
    public void setScale(Vector3fc scale) {
        this.scale.set(scale);
    }

    public Vector4fc getColor() {
        return color;
    }
    
    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }
    
    public void setColor(Vector4f color) {
        setColor(color.x(), color.y(), color.z(), color.w());
    }

    public void render() {
        if (this.image.getTexture() == 0) {
            this.image.createTexture();
        }
        glBindTexture(GL_TEXTURE_2D, this.image.getTexture());
        
        glUniform3i(INT_POS_INDEX, this.intPart.x(), this.intPart.y(), this.intPart.z());
        glUniform3f(DEC_POS_INDEX, this.decPart.x(), this.decPart.y(), this.decPart.z());
        glUniform3f(SCALE_INDEX, this.scale.x(), this.scale.y(), this.scale.z());
        glUniform4f(COLOR_INDEX, this.color.x(), this.color.y(), this.color.z(), this.color.w());
        glUniform1i(MULTI_CHANNEL_SDF_INDEX, (this.multiChannelSDF ? 1 : 0));
        glUniform1f(THICKNESS_INDEX, this.thickness);
        
        glDrawArrays(GL_TRIANGLES, 0, 12);
    }

}
