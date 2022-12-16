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

import cientistavuador.ciencraftreal.camera.Camera;
import cientistavuador.ciencraftreal.debug.Triangle;

/**
 *
 * @author Cien
 */
public class Game {
    
    private static final Game GAME = new Game();
    
    public static Game get() {
        return GAME;
    }
    
    private final Triangle triangle = new Triangle();
    private final Camera camera = new Camera();
    
    private Game() {
        
    }
    
    public void start() {
        
    }
    
    public void loop() {
        camera.updateMovement();
        triangle.render(camera.getProjection(), camera.getView());
    }
    
    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }
    
    public void windowSizeChanged(int width, int height) {
        camera.setDimensions(width, height);
    }
    
}
