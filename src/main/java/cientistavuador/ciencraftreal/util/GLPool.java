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

import cientistavuador.ciencraftreal.Main;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Cien
 */
public class GLPool {

    public static final boolean OUTPUT_DEBUG = true;
    private static final Thread[] threads;
    private static final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    static {
        int amountOfThreads = Runtime.getRuntime().availableProcessors();

        if (OUTPUT_DEBUG) {
            System.out.println("GLPool: " + amountOfThreads + " CPUs available.");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        threads = new Thread[amountOfThreads];
        for (int i = 0; i < amountOfThreads; i++) {
            long window = glfwCreateWindow(1, 1, "GL-Pool-Window" + i, NULL, Main.WINDOW_POINTER);

            threads[i] = new Thread(() -> {
                glfwMakeContextCurrent(window);
                GL.createCapabilities();

                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glClearColor(0.2f, 0.4f, 0.6f, 1.0f);
                glEnable(GL_DEPTH_TEST);
                glClearDepth(1f);
                glDepthFunc(GL_LEQUAL);
                glEnable(GL_CULL_FACE);
                glCullFace(GL_BACK);
                glLineWidth(1f);

                while (true) {
                    try {
                        Runnable e = tasks.poll(1, TimeUnit.SECONDS);
                        if (e == null) {
                            continue;
                        }
                        e.run();
                    } catch (InterruptedException ex) {
                        break;
                    }
                }

                glfwMakeContextCurrent(NULL);
            }, "GLPool-" + i);
            threads[i].setDaemon(true);
            threads[i].start();

            if (OUTPUT_DEBUG) {
                System.out.println("GLPool: CPU "+i+" Initialized.");
            }
        }
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
    }

    public static void init() {

    }

    public static void run(Runnable task, Consumer<Throwable> callback) {
        tasks.add(() -> {
            try {
                task.run();
                glFinish();
                Main.checkGLError();
            } catch (Throwable f) {
                callback.accept(f);
                return;
            }
            callback.accept(null);
        });
    }

    public static void destroy() {
        for (Thread t:threads) {
            t.interrupt();
        }
        for (Thread t:threads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(GLPool.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }
    
    private GLPool() {

    }

}
