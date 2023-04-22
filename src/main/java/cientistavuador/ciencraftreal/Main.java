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

import cientistavuador.ciencraftreal.audio.AudioSystem;
import cientistavuador.ciencraftreal.block.BlockSounds;
import cientistavuador.ciencraftreal.block.BlockTextures;
import cientistavuador.ciencraftreal.block.Blocks;
import cientistavuador.ciencraftreal.text.GLFonts;
import cientistavuador.ciencraftreal.ubo.UBOBindingPoints;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.opengl.GLDebugMessageCallback;
import static org.lwjgl.opengl.KHRDebug.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Main class
 *
 * @author Cien
 */
public class Main {

    public static final boolean USE_MSAA = false;
    public static final boolean DEBUG_ENABLED = true;
    public static final boolean SPIKE_LAG_WARNINGS = false;
    public static final int MIN_TEXTURE_3D_SIZE_SUPPORTED = 2048;
    public static final int MIN_UNIFORM_BUFFER_BINDINGS = UBOBindingPoints.MIN_NUMBER_OF_UBO_BINDING_POINTS;

    static {
        org.lwjgl.system.Configuration.LIBRARY_PATH.set("natives");
    }

    public static class OpenGLErrorException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        private final int error;

        public OpenGLErrorException(int error) {
            super("OpenGL Error " + error);
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

    public static String WINDOW_TITLE = "CienCraft - FPS: 60";
    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    public static double TPF = 1 / 60d;
    public static int FPS = 60;
    public static long WINDOW_POINTER = NULL;
    public static long FRAME = 0;
    public static double ONE_SECOND_COUNTER = 0.0;
    public static double ONE_MINUTE_COUNTER = 0.0;
    public static boolean SHADOWS_ENABLED = true;
    public static final ConcurrentLinkedQueue<Runnable> MAIN_TASKS = new ConcurrentLinkedQueue<>();
    private static GLDebugMessageCallback DEBUG_CALLBACK = null;

    private static String debugSource(int source) {
        return switch (source) {
            case GL_DEBUG_SOURCE_API ->
                "API";
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM ->
                "WINDOW SYSTEM";
            case GL_DEBUG_SOURCE_SHADER_COMPILER ->
                "SHADER COMPILER";
            case GL_DEBUG_SOURCE_THIRD_PARTY ->
                "THIRD PARTY";
            case GL_DEBUG_SOURCE_APPLICATION ->
                "APPLICATION";
            case GL_DEBUG_SOURCE_OTHER ->
                "OTHER";
            default ->
                "UNKNOWN";
        };
    }

    private static String debugType(int type) {
        return switch (type) {
            case GL_DEBUG_TYPE_ERROR ->
                "ERROR";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR ->
                "DEPRECATED BEHAVIOR";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR ->
                "UNDEFINED BEHAVIOR";
            case GL_DEBUG_TYPE_PORTABILITY ->
                "PORTABILITY";
            case GL_DEBUG_TYPE_PERFORMANCE ->
                "PERFORMANCE";
            case GL_DEBUG_TYPE_OTHER ->
                "OTHER";
            case GL_DEBUG_TYPE_MARKER ->
                "MARKER";
            default ->
                "UNKNOWN";
        };
    }

    private static String debugSeverity(int severity) {
        return switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH ->
                "HIGH";
            case GL_DEBUG_SEVERITY_MEDIUM ->
                "MEDIUM";
            case GL_DEBUG_SEVERITY_LOW ->
                "LOW";
            default ->
                "UNKNOWN";
        };
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        glfwSetErrorCallback((error, description) -> {
            GLFWErrorException exception = new GLFWErrorException("GLFW Error " + error + ": " + memASCIISafe(description));
            if (THROW_GL_GLFW_ERRORS) {
                throw exception;
            } else {
                exception.printStackTrace(System.err);
            }
        });

        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GLFW!");
        }

        if (USE_MSAA) {
            glfwWindowHint(GLFW_SAMPLES, 16); //MSAA 16x
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        WINDOW_POINTER = glfwCreateWindow(Main.WIDTH, Main.HEIGHT, Main.WINDOW_TITLE, NULL, NULL);
        if (WINDOW_POINTER == NULL) {
            throw new IllegalStateException("Could not create a OpenGL 3.3 Context Window! Update your drivers or buy a new GPU.");
        }
        glfwMakeContextCurrent(WINDOW_POINTER);

        glfwSwapInterval(0);

        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(WINDOW_POINTER, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }

        GL.createCapabilities();
        AudioSystem.init(); //static initialize

        if (DEBUG_ENABLED) {
            debug:
            {
                if (!GL.getCapabilities().GL_KHR_debug) {
                    System.err.println("[GL-DEBUG] Debug was enabled but KHR_debug is not supported.");
                    break debug;
                }

                glEnable(GL_DEBUG_OUTPUT);

                DEBUG_CALLBACK = new GLDebugMessageCallback() {
                    @Override
                    public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
                        if (severity == GL_DEBUG_SEVERITY_NOTIFICATION) {
                            return;
                        }

                        String msg = memASCII(message, length);

                        PrintStream out = System.out;
                        if (severity == GL_DEBUG_SEVERITY_HIGH) {
                            out = System.err;
                        }

                        out.println("[GL-DEBUG]");
                        out.println("    Severity: " + debugSeverity(severity));
                        out.println("    Source: " + debugSource(source));
                        out.println("    Type: " + debugType(type));
                        out.println("    ID: " + id);
                        out.println("    Message: " + msg);
                    }
                };
                glDebugMessageCallback(DEBUG_CALLBACK, NULL);
            }
        }

        if (USE_MSAA) {
            glEnable(GL_MULTISAMPLE);
        }
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.2f, 0.4f, 0.6f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glClearDepth(1f);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glLineWidth(1f);
        int maxTex3DSize = glGetInteger(GL_MAX_3D_TEXTURE_SIZE);
        if (maxTex3DSize < MIN_TEXTURE_3D_SIZE_SUPPORTED) {
            throw new IllegalStateException("Max 3D Texture Size too small! Update your drivers or buy a new GPU.");
        }
        int maxUBOBindings = glGetInteger(GL_MAX_UNIFORM_BUFFER_BINDINGS);
        if (maxUBOBindings < MIN_UNIFORM_BUFFER_BINDINGS) {
            throw new IllegalStateException("Max UBO Bindings too small! Update your drivers or buy a new GPU.");
        }

        Main.checkGLError();

        //GLPool.init(); //static initialize
        BlockTextures.init();  //static initialize
        GLFonts.init(); //static initialize
        BlockSounds.init(); //static initialize
        Blocks.init(); //static initialize
        ShadowFBO.init(); //static initialize
        Game.get(); //static initialize

        Main.checkGLError();

        GLFWFramebufferSizeCallbackI frameBufferSizecb = (window, width, height) -> {
            glViewport(0, 0, width, height);
            Main.WIDTH = width;
            Main.HEIGHT = height;
            Game.get().windowSizeChanged(width, height);
            Main.checkGLError();
        };
        frameBufferSizecb.invoke(WINDOW_POINTER, Main.WIDTH, Main.HEIGHT);
        glfwSetFramebufferSizeCallback(WINDOW_POINTER, frameBufferSizecb);

        glfwSetCursorPosCallback(WINDOW_POINTER, (window, x, y) -> {
            Game.get().mouseCursorMoved(x, y);
        });

        glfwSetKeyCallback(WINDOW_POINTER, (window, key, scancode, action, mods) -> {
            Game.get().keyCallback(window, key, scancode, action, mods);
        });

        glfwSetMouseButtonCallback(WINDOW_POINTER, (window, button, action, mods) -> {
            Game.get().mouseCallback(window, button, action, mods);
        });

        Game.get().start();

        Main.checkGLError();

        int frames = 0;
        long nextFpsUpdate = System.currentTimeMillis() + 1000;
        long nextTitleUpdate = System.currentTimeMillis() + 100;
        long timeFrameBegin = System.nanoTime();

        while (!glfwWindowShouldClose(WINDOW_POINTER)) {
            Main.TPF = (System.nanoTime() - timeFrameBegin) / 1E9d;
            timeFrameBegin = System.nanoTime();

            if (SPIKE_LAG_WARNINGS) {
                int tpfFps = (int) (1.0 / Main.TPF);
                if (tpfFps < 60 && ((Main.FPS - tpfFps) > 30)) {
                    System.out.println("[Spike Lag Warning] From " + Main.FPS + " FPS to " + tpfFps + " FPS; current frame TPF: " + String.format("%.3f", Main.TPF) + "s");
                }
            }

            glfwPollEvents();
            Main.WINDOW_TITLE = "CienCraft - FPS: " + Main.FPS;

            Game.get().loop();
            
            Runnable r;
            while ((r = MAIN_TASKS.poll()) != null) {
                r.run();
            }

            if (Main.SHADOWS_ENABLED) {
                glBindFramebuffer(GL_FRAMEBUFFER, ShadowFBO.FBO);
                glViewport(0, 0, ShadowFBO.width(), ShadowFBO.height());
                glClear(GL_DEPTH_BUFFER_BIT);

                Game.get().shadowLoop();
            }
            
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

            Game.get().renderLoop();

            glFlush();

            Main.checkGLError();

            glfwSwapBuffers(WINDOW_POINTER);

            frames++;
            if (System.currentTimeMillis() >= nextFpsUpdate) {
                Main.FPS = frames;
                frames = 0;
                nextFpsUpdate = System.currentTimeMillis() + 1000;
            }

            if (System.currentTimeMillis() >= nextTitleUpdate) {
                nextTitleUpdate = System.currentTimeMillis() + 100;
                glfwSetWindowTitle(WINDOW_POINTER, Main.WINDOW_TITLE);
            }

            Main.ONE_SECOND_COUNTER += Main.TPF;
            Main.ONE_MINUTE_COUNTER += Main.TPF;

            if (Main.ONE_SECOND_COUNTER > 1.0) {
                Main.ONE_SECOND_COUNTER = 0.0;
            }
            if (Main.ONE_MINUTE_COUNTER > 60.0) {
                Main.ONE_MINUTE_COUNTER = 0.0;
            }

            Main.FRAME++;
        }
        if (DEBUG_CALLBACK != null) {
            DEBUG_CALLBACK.free();
        }

        glfwTerminate();

        System.exit(0);
    }

}
