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
package cientistavuador.ciencraftreal;

import cientistavuador.ciencraftreal.chunk.render.layer.ShadowProfile;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ShadowFBO {
    
    public static final int FBO;
    public static final int DEPTH_BUFFER_TEXTURE;
    public static final int DEFAULT_WIDTH = ShadowProfile.VERY_LOW.resolution();
    public static final int DEFAULT_HEIGHT = ShadowProfile.VERY_LOW.resolution();
    private static int width = 0;
    private static int height = 0;
    
    public static int width() {
        return width;
    }
    
    public static int height() {
        return height;
    }
    
    static {
        DEPTH_BUFFER_TEXTURE = glGenTextures();
        updateDepthBufferTextureSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        
        glBindTexture(GL_TEXTURE_2D, DEPTH_BUFFER_TEXTURE);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[] {1f, 1f, 1f, 1f});
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        FBO = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, FBO);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, DEPTH_BUFFER_TEXTURE, 0);
        glDrawBuffers(GL_NONE);
        glReadBuffer(GL_NONE);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create shadow FBO!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    public static void init() {
        
    }
    
    public static void updateDepthBufferTextureSize(int width, int height) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, DEPTH_BUFFER_TEXTURE);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_DEPTH_COMPONENT32,
                width,
                height,
                0,
                GL_DEPTH_COMPONENT,
                GL_UNSIGNED_INT,
                0
        );
        glBindTexture(GL_TEXTURE_2D, 0);
        ShadowFBO.width = width;
        ShadowFBO.height = height;
    }
    
    private ShadowFBO() {
        
    }
    
}
