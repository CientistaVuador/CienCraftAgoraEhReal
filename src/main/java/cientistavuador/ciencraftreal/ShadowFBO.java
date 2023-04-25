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

    public static final int FBO_A;
    public static final int FBO_B;
    public static final int DEPTH_BUFFER_TEXTURE_A;
    public static final int DEPTH_BUFFER_TEXTURE_B;
    public static final int DEFAULT_WIDTH = ShadowProfile.VERY_LOW.resolution();
    public static final int DEFAULT_HEIGHT = ShadowProfile.VERY_LOW.resolution();

    static {
        DEPTH_BUFFER_TEXTURE_A = glGenTextures();
        DEPTH_BUFFER_TEXTURE_B = glGenTextures();
        FBO_A = createFBO(DEPTH_BUFFER_TEXTURE_A);
        FBO_B = createFBO(DEPTH_BUFFER_TEXTURE_B);
    }

    private static int createFBO(int depthBuffer) {
        glActiveTexture(GL_TEXTURE0);
        
        glBindTexture(GL_TEXTURE_2D, depthBuffer);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_DEPTH_COMPONENT32,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                0,
                GL_DEPTH_COMPONENT,
                GL_UNSIGNED_INT,
                0
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[]{1f, 1f, 1f, 1f});

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        glBindTexture(GL_TEXTURE_2D, 0);

        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthBuffer, 0);
        glDrawBuffers(GL_NONE);
        glReadBuffer(GL_NONE);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create shadow FBO!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return fbo;
    }

    private static int drawWidth = DEFAULT_WIDTH;
    private static int drawHeight = DEFAULT_HEIGHT;
    private static int readWidth = DEFAULT_WIDTH;
    private static int readHeight = DEFAULT_HEIGHT;
    private static int drawFBO = FBO_A;
    private static int readFBO = FBO_B;
    private static int drawBuffer = DEPTH_BUFFER_TEXTURE_A;
    private static int readBuffer = DEPTH_BUFFER_TEXTURE_B;
    private static int scheduledUpdate = 0;
    private static int scheduledWidth = 0;
    private static int scheduledHeight = 0;

    public static int drawWidth() {
        return drawWidth;
    }

    public static int drawHeight() {
        return drawHeight;
    }

    public static int readWidth() {
        return readWidth;
    }

    public static int readHeight() {
        return readHeight;
    }

    public static int drawFBO() {
        return drawFBO;
    }

    public static int readFBO() {
        return readFBO;
    }

    public static int drawBuffer() {
        return drawBuffer;
    }

    public static int readBuffer() {
        return readBuffer;
    }

    public static void flip() {
        int draw = ShadowFBO.drawFBO;
        int read = ShadowFBO.readFBO;
        int drawBuf = ShadowFBO.drawBuffer;
        int readBuf = ShadowFBO.readBuffer;
        int readWid = ShadowFBO.readWidth;
        int readHei = ShadowFBO.readHeight;
        int drawWid = ShadowFBO.drawWidth;
        int drawHei = ShadowFBO.drawHeight;

        ShadowFBO.drawFBO = read;
        ShadowFBO.readFBO = draw;
        ShadowFBO.drawBuffer = readBuf;
        ShadowFBO.readBuffer = drawBuf;
        ShadowFBO.drawWidth = readWid;
        ShadowFBO.drawHeight = readHei;
        ShadowFBO.readWidth = drawWid;
        ShadowFBO.readHeight = drawHei;

        if (ShadowFBO.scheduledUpdate != 0) {
            ShadowFBO.drawWidth = ShadowFBO.scheduledWidth;
            ShadowFBO.drawHeight = ShadowFBO.scheduledHeight;
            ShadowFBO.scheduledUpdate--;

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, ShadowFBO.drawBuffer);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_DEPTH_COMPONENT32,
                    ShadowFBO.drawWidth,
                    ShadowFBO.drawHeight,
                    0,
                    GL_DEPTH_COMPONENT,
                    GL_UNSIGNED_INT,
                    0
            );
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    public static void init() {

    }

    public static void updateDepthBufferTextureSize(int width, int height) {
        ShadowFBO.scheduledWidth = width;
        ShadowFBO.scheduledHeight = height;
        ShadowFBO.scheduledUpdate = 2;
    }

    private ShadowFBO() {

    }

}
