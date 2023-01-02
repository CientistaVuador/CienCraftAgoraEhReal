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
package cientistavuador.ciencraftreal.chunk.render;

import org.joml.Vector3fc;

/**
 *
 * @author Cien
 */
public class ChunkNodeNode implements ChunkNode {

    private final ChunkNode left;
    private final ChunkNode right;
    private final float height;
    private final float size;
    private final boolean onY;

    public ChunkNodeNode(ChunkNode topLeft, ChunkNode bottomRight, boolean onY) {
        this.left = topLeft;
        this.right = bottomRight;
        this.onY = onY;

        float heightCalc = 0f;
        float sizeCalc = 0f;

        if (topLeft != null) {
            if (onY) {
                heightCalc += topLeft.getHeight();
                sizeCalc = topLeft.getSize();
            } else {
                heightCalc = topLeft.getHeight();
                sizeCalc += topLeft.getSize();
            }
        }
        if (bottomRight != null) {
            if (onY) {
                heightCalc += bottomRight.getHeight();
                sizeCalc = bottomRight.getSize();
            } else {
                heightCalc = bottomRight.getHeight();
                sizeCalc += bottomRight.getSize();
            }
        }

        this.height = heightCalc;
        this.size = sizeCalc;
    }

    public boolean isOnY() {
        return this.onY;
    }
    
    @Override
    public Vector3fc getPosition() {
        //todo
    }
    
    @Override
    public ChunkNode getParent() {
        //todo
    }

    @Override
    public void setParent(ChunkNode parent) {
        //todo
    }
    
    public ChunkNode getTop() {
        return this.left;
    }
    
    public ChunkNode getBottom() {
        return this.right;
    }
    
    @Override
    public ChunkNode getLeft() {
        return this.left;
    }

    @Override
    public ChunkNode getRight() {
        return this.right;
    }
    
    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public float getSize() {
        return this.size;
    }

    @Override
    public void tryRender() {
        //todo
    }

    @Override
    public boolean fetchResult() {
        //todo
    }

    @Override
    public void delete() {
        //todo
    }
    
}
