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

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Main class
 * @author Cien
 */
public class Main {

    static {
        org.lwjgl.system.Configuration.LIBRARY_PATH.set("natives");
    }
    
    public static class OpenGLErrorException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        private final int error;
        
        public OpenGLErrorException(int error) {
            super("OpenGL Error "+error);
            this.error = error;
        }

        public int getError() {
            return error;
        }
    }
    
    public static class GLFWErrorException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        public GLFWErrorException(String error) {
            super(error);
        }
    }
    
    public static boolean THROW_GL_GLFW_ERRORS = true;
    public static void checkGLError() {
        int error = glGetError();
        if (error != 0) {
            OpenGLErrorException err = new OpenGLErrorException(error);
            if (THROW_GL_GLFW_ERRORS) {
                throw err;
            } else {
                err.printStackTrace(System.err);
            }
        }
    }
    
    public static double TPF = 1/60d;
    public static int FPS = 60;
    public static long WINDOW_POINTER = NULL;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        glfwSetErrorCallback((error, description) -> {
            GLFWErrorException exception = new GLFWErrorException("GLFW Error "+error+": "+MemoryUtil.memASCIISafe(description));
            if (THROW_GL_GLFW_ERRORS) {
                throw exception;
            } else {
                exception.printStackTrace(System.err);
            }
        });
        
        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GLFW!");
        }
        
        //glfwWindowHint(GLFW_SAMPLES, 4); //MSAA 4x
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        WINDOW_POINTER = glfwCreateWindow(800, 600, "CienCraft - FPS: 60", NULL, NULL);
        if (WINDOW_POINTER == NULL) {
            throw new IllegalStateException("Could not create a OpenGL 3.3 Context Window! Update your drivers or buy a new GPU.");
        }
        glfwMakeContextCurrent(WINDOW_POINTER);
        
        glfwSwapInterval(1);
        
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(WINDOW_POINTER, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }
        
        GL.createCapabilities();
        
        //glEnable(GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.2f, 0.4f, 0.6f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glClearDepth(1f);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        Main.checkGLError();
        
        Game.get(); //static initialize
        
        glfwSetFramebufferSizeCallback(WINDOW_POINTER, (window, width, height) -> {
            glViewport(0, 0, width, height);
            Game.get().windowSizeChanged(width, height);
            Main.checkGLError();
        });
        
        Game.get().start();
        
        Main.checkGLError();
        
        int frames = 0;
        long nextFpsUpdate = System.currentTimeMillis() + 1000;
        
        while (!glfwWindowShouldClose(WINDOW_POINTER)) {
            long timeFrameBegin = System.nanoTime();
            
            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
            glfwPollEvents();
            
            Game.get().loop();
            
            glFlush();
            
            Main.checkGLError();
            
            glfwSwapBuffers(WINDOW_POINTER);
            
            frames++;
            if (System.currentTimeMillis() >= nextFpsUpdate) {
                Main.FPS = frames;
                frames = 0;
                nextFpsUpdate = System.currentTimeMillis() + 1000;
                
                glfwSetWindowTitle(WINDOW_POINTER, "CienCraft - FPS: "+Main.FPS);
            }
            
            Main.TPF = (System.nanoTime()-timeFrameBegin) / 1E9d;
        }
        
        glfwTerminate();
    }
    
}
