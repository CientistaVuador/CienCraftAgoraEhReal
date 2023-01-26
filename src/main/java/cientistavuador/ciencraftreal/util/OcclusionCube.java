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
package cientistavuador.ciencraftreal.util;

import cientistavuador.ciencraftreal.camera.Camera;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class OcclusionCube {
    private static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform vec3 position;
            uniform vec3 size;
            
            uniform mat4 view;
            uniform mat4 projection;
            
            layout (location = 0) in vec3 vertexPos;
            
            void main() {
                gl_Position = projection * view * vec4((vertexPos * size) + position, 1.0);
            }
            """;
    
    private static final String FRAGMENT_SHADER =
            """
            #version 330 core
            
            void main() {
                
            }
            """;
    
    private static final int program = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final int vao;
    
    static {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
            0f, 0f, 0f, //0
            1f, 0f, 0f, //1
            0f, 0f, -1f, //2
            1f, 0f, -1f, //3
            
            0f, 1f, 0f, //4
            1f, 1f, 0f, //5
            0f, 1f, -1f, //6 
            1f, 1f, -1f //7
        }, GL_STATIC_DRAW);
            
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[] {
            0, 2, 1, 1, 2, 3, 0, 1, 2, 1, 3, 2,
            4, 6, 5, 5, 6, 7, 4, 5, 6, 5, 7, 6,
            2, 3, 6, 7, 6, 3, 2, 6, 3, 7, 3, 6,
            0, 1, 4, 5, 4, 1, 0, 4, 1, 5, 1, 4,
            0, 2, 4, 6, 4, 2, 0, 4, 2, 6, 2, 4,
            1, 3, 5, 7, 5, 3, 1, 5, 3, 7, 3, 5,
        }, GL_STATIC_DRAW);
        
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    private final int queryObject = glGenQueries();
    
    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f size = new Vector3f(1);
    
    public OcclusionCube() {
        
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getSize() {
        return size;
    }
    
    public boolean isInside(Vector3fc pos) {
        float xMin = this.position.x();
        float yMin = this.position.y();
        float zMin = this.position.z() - this.size.z();
        float xMax = this.position.x() + this.size.x();
        float yMax = this.position.y() + this.size.y();
        float zMax = this.position.z();
        
        return pos.x() <= xMax && pos.x() >= xMin &&
                pos.y() <= yMax && pos.y() >= yMin &&
                pos.z() <= zMax && pos.z() >= zMin;
                
    }
    
    public boolean isInside(Camera cam) {
        return isInside(cam.getPosition());
    }

    public void render(Camera camera) {
        glUseProgram(program);
        glBindVertexArray(vao);
        
        UniformSetter.setVector3f("position", this.position);
        UniformSetter.setVector3f("size", this.size);
        UniformSetter.setMatrix4f("view", camera.getView());
        UniformSetter.setMatrix4f("projection", camera.getProjection());
        
        glDepthMask(false);
        glBeginQuery(GL_ANY_SAMPLES_PASSED, this.queryObject);
        glDrawElements(GL_TRIANGLES, 3 * 2 * 2 * 6, GL_UNSIGNED_INT, 0);
        glEndQuery(GL_ANY_SAMPLES_PASSED);
        glDepthMask(true);
        
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    public boolean queryResultAvailable() {
        return glGetQueryObjecti(this.queryObject, GL_QUERY_RESULT_AVAILABLE) != 0;
    }
    
    public boolean queryResult() {
        return glGetQueryObjecti(this.queryObject, GL_QUERY_RESULT) != 0;
    }
    
    public void beginConditionalRender(Camera camera) {
        glBeginConditionalRender(this.queryObject, GL_QUERY_WAIT);
    }
    
    public void endConditionalRender() {
        glEndConditionalRender();
    }
    
    public void delete() {
        glDeleteQueries(this.queryObject);
    }
    
}
