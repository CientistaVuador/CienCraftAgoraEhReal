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
package cientistavuador.ciencraftreal.block;

import static cientistavuador.ciencraftreal.block.BlockSide.BOTTOM;
import static cientistavuador.ciencraftreal.block.BlockSide.EAST;
import static cientistavuador.ciencraftreal.block.BlockSide.NORTH;
import static cientistavuador.ciencraftreal.block.BlockSide.SOUTH;
import static cientistavuador.ciencraftreal.block.BlockSide.TOP;
import static cientistavuador.ciencraftreal.block.BlockSide.WEST;
import cientistavuador.ciencraftreal.chunk.render.layer.vertices.VerticesStream;

/**
 *
 * @author Cien
 */
public class BlockFacesVertices {

    public static void generateFaceVertices(VerticesStream stream, BlockSide side, float x, float y, float z, int texture) {
        generateFaceVertices(stream, side, x, y, z, texture, 1f, 1f, 1f);
    }

    public static void generateFaceVertices(VerticesStream stream, BlockSide side, float x, float y, float z, int texture, AmbientOcclusion ao) {
        generateFaceVertices(stream, side, x, y, z, texture, 1f, 1f, 1f, ao);
    }

    public static void generateFaceVertices(VerticesStream stream, BlockSide side, float x, float y, float z, int texture, float scaleX, float scaleY, float scaleZ) {
        generateFaceVertices(stream, side, x, y, z, texture, scaleX, scaleY, scaleZ, null);
    }

    public static void generateFaceVertices(VerticesStream stream, BlockSide side, float x, float y, float z, int texture, float scaleX, float scaleY, float scaleZ, AmbientOcclusion ao) {
        if (ao == null) {
            ao = AmbientOcclusion.NO_OCCLUSION;
        }

        float sizeX = 0.5f * scaleX;
        float sizeY = 0.5f * scaleY;
        float sizeZ = 0.5f * scaleZ;

        float xP = sizeX + x;
        float xN = -sizeX + x;
        float yP = sizeY + y;
        float yN = -sizeY + y;
        float zP = sizeZ + z;
        float zN = -sizeZ + z;

        stream.offset();

        switch (side) {
            case NORTH -> {
                stream.vertex(xN, yP, zN, 1f, 1f, texture, ao.getSideVertexAO(false, true, false));
                stream.vertex(xP, yN, zN, 0f, 0f, texture, ao.getSideVertexAO(true, false, false));
                stream.vertex(xN, yN, zN, 1f, 0f, texture, ao.getSideVertexAO(false, false, false));
                stream.vertex(xP, yP, zN, 0f, 1f, texture, ao.getSideVertexAO(true, true, false));
                if (ao.generateFlippedQuad()) {
                    stream.index(new int[]{0, 3, 2, 3, 1, 2});
                } else {
                    stream.index(new int[]{0, 1, 2, 0, 3, 1});
                }
                return;
            }
            case SOUTH -> {
                stream.vertex(xN, yP, zP, 0f, 1f, texture, ao.getSideVertexAO(false, true, true));
                stream.vertex(xN, yN, zP, 0f, 0f, texture, ao.getSideVertexAO(false, false, true));
                stream.vertex(xP, yN, zP, 1f, 0f, texture, ao.getSideVertexAO(true, false, true));
                stream.vertex(xP, yP, zP, 1f, 1f, texture, ao.getSideVertexAO(true, true, true));
                if (ao.generateFlippedQuad()) {
                    stream.index(new int[]{0, 1, 3, 3, 1, 2});
                } else {
                    stream.index(new int[]{0, 1, 2, 0, 2, 3});
                }
                return;
            }
            case EAST -> {
                stream.vertex(xP, yP, zN, 1f, 1f, texture, ao.getSideVertexAO(true, true, false));
                stream.vertex(xP, yN, zP, 0f, 0f, texture, ao.getSideVertexAO(true, false, true));
                stream.vertex(xP, yN, zN, 1f, 0f, texture, ao.getSideVertexAO(true, false, false));
                stream.vertex(xP, yP, zP, 0f, 1f, texture, ao.getSideVertexAO(true, true, true));
                if (ao.generateFlippedQuad()) {
                    stream.index(new int[]{0, 3, 2, 2, 3, 1});
                } else {
                    stream.index(new int[]{0, 1, 2, 3, 1, 0});
                }
                return;
            }
            case WEST -> {
                stream.vertex(xN, yP, zN, 0f, 1f, texture, ao.getSideVertexAO(false, true, false));
                stream.vertex(xN, yN, zN, 0f, 0f, texture, ao.getSideVertexAO(false, false, false));
                stream.vertex(xN, yN, zP, 1f, 0f, texture, ao.getSideVertexAO(false, false, true));
                stream.vertex(xN, yP, zP, 1f, 1f, texture, ao.getSideVertexAO(false, true, true));
                if (ao.generateFlippedQuad()) {
                    stream.index(new int[]{0, 1, 3, 1, 2, 3});
                } else {
                    stream.index(new int[]{0, 1, 2, 3, 0, 2});
                }
                return;
            }
            case TOP -> {
                stream.vertex(xN, yP, zP, 0f, 0f, texture, ao.getSideVertexAO(false, true, true));
                stream.vertex(xP, yP, zP, 1f, 0f, texture, ao.getSideVertexAO(true, true, true));
                stream.vertex(xN, yP, zN, 0f, 1f, texture, ao.getSideVertexAO(false, true, false));
                stream.vertex(xP, yP, zN, 1f, 1f, texture, ao.getSideVertexAO(true, true, false));
                if (ao.generateFlippedQuad()) {
                    stream.index(new int[]{0, 3, 2, 0, 1, 3});
                } else {
                    stream.index(new int[]{0, 1, 2, 2, 1, 3});
                }
                return;
            }
            case BOTTOM -> {
                stream.vertex(xN, yN, zP, 0f, 1f, texture, ao.getSideVertexAO(false, false, true));
                stream.vertex(xN, yN, zN, 0f, 0f, texture, ao.getSideVertexAO(false, false, false));
                stream.vertex(xP, yN, zP, 1f, 1f, texture, ao.getSideVertexAO(true, false, true));
                stream.vertex(xP, yN, zN, 1f, 0f, texture, ao.getSideVertexAO(true, false, false));
                if (ao.generateFlippedQuad()) {
                    stream.index(new int[]{0, 3, 2, 0, 1, 3});
                } else {
                    stream.index(new int[]{0, 1, 2, 1, 3, 2});
                }
                return;
            }
        }
        throw new RuntimeException("Unknown side: " + side.name());
    }

    @Deprecated
    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture) {
        return generateFaceVertices(side, x, y, z, texture, 1f, 1f, 1f);
    }

    @Deprecated
    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture, AmbientOcclusion ao) {
        return generateFaceVertices(side, x, y, z, texture, 1f, 1f, 1f, ao);
    }

    @Deprecated
    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture, float scaleX, float scaleY, float scaleZ) {
        return generateFaceVertices(side, x, y, z, texture, scaleX, scaleY, scaleZ, null);
    }

    @Deprecated
    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture, float scaleX, float scaleY, float scaleZ, AmbientOcclusion ao) {
        if (ao == null) {
            ao = AmbientOcclusion.NO_OCCLUSION;
        }

        float textureFloat = Float.intBitsToFloat(texture);

        float sizeX = 0.5f * scaleX;
        float sizeY = 0.5f * scaleY;
        float sizeZ = 0.5f * scaleZ;

        float xP = sizeX + x;
        float xN = -sizeX + x;
        float yP = sizeY + y;
        float yN = -sizeY + y;
        float zP = sizeZ + z;
        float zN = -sizeZ + z;

        switch (side) {
            case NORTH -> {
                return new float[]{
                    xN, yP, zN, 1f, 1f, textureFloat, ao.getSideVertexAO(false, true, false),
                    xP, yN, zN, 0f, 0f, textureFloat, ao.getSideVertexAO(true, false, false),
                    xN, yN, zN, 1f, 0f, textureFloat, ao.getSideVertexAO(false, false, false),
                    xN, yP, zN, 1f, 1f, textureFloat, ao.getSideVertexAO(false, true, false),
                    xP, yP, zN, 0f, 1f, textureFloat, ao.getSideVertexAO(true, true, false),
                    xP, yN, zN, 0f, 0f, textureFloat, ao.getSideVertexAO(true, false, false)
                };
            }
            case SOUTH -> {
                return new float[]{
                    xN, yP, zP, 0f, 1f, textureFloat, ao.getSideVertexAO(false, true, true),
                    xN, yN, zP, 0f, 0f, textureFloat, ao.getSideVertexAO(false, false, true),
                    xP, yN, zP, 1f, 0f, textureFloat, ao.getSideVertexAO(true, false, true),
                    xN, yP, zP, 0f, 1f, textureFloat, ao.getSideVertexAO(false, true, true),
                    xP, yN, zP, 1f, 0f, textureFloat, ao.getSideVertexAO(true, false, true),
                    xP, yP, zP, 1f, 1f, textureFloat, ao.getSideVertexAO(true, true, true)
                };
            }
            case EAST -> {
                return new float[]{
                    xP, yP, zN, 1f, 1f, textureFloat, ao.getSideVertexAO(true, true, false),
                    xP, yN, zP, 0f, 0f, textureFloat, ao.getSideVertexAO(true, false, true),
                    xP, yN, zN, 1f, 0f, textureFloat, ao.getSideVertexAO(true, false, false),
                    xP, yP, zP, 0f, 1f, textureFloat, ao.getSideVertexAO(true, true, true),
                    xP, yN, zP, 0f, 0f, textureFloat, ao.getSideVertexAO(true, false, true),
                    xP, yP, zN, 1f, 1f, textureFloat, ao.getSideVertexAO(true, true, false)
                };
            }
            case WEST -> {
                return new float[]{
                    xN, yP, zN, 0f, 1f, textureFloat, ao.getSideVertexAO(false, true, false),
                    xN, yN, zN, 0f, 0f, textureFloat, ao.getSideVertexAO(false, false, false),
                    xN, yN, zP, 1f, 0f, textureFloat, ao.getSideVertexAO(false, false, true),
                    xN, yP, zP, 1f, 1f, textureFloat, ao.getSideVertexAO(false, true, true),
                    xN, yP, zN, 0f, 1f, textureFloat, ao.getSideVertexAO(false, true, false),
                    xN, yN, zP, 1f, 0f, textureFloat, ao.getSideVertexAO(false, false, true)
                };
            }
            case TOP -> {
                return new float[]{
                    xN, yP, zP, 0f, 0f, textureFloat, ao.getSideVertexAO(false, true, true),
                    xP, yP, zP, 1f, 0f, textureFloat, ao.getSideVertexAO(true, true, true),
                    xN, yP, zN, 0f, 1f, textureFloat, ao.getSideVertexAO(false, true, false),
                    xN, yP, zN, 0f, 1f, textureFloat, ao.getSideVertexAO(false, true, false),
                    xP, yP, zP, 1f, 0f, textureFloat, ao.getSideVertexAO(true, true, true),
                    xP, yP, zN, 1f, 1f, textureFloat, ao.getSideVertexAO(true, true, false)
                };
            }
            case BOTTOM -> {
                return new float[]{
                    xN, yN, zP, 0f, 1f, textureFloat, ao.getSideVertexAO(false, false, true),
                    xN, yN, zN, 0f, 0f, textureFloat, ao.getSideVertexAO(false, false, false),
                    xP, yN, zP, 1f, 1f, textureFloat, ao.getSideVertexAO(true, false, true),
                    xN, yN, zN, 0f, 0f, textureFloat, ao.getSideVertexAO(false, false, false),
                    xP, yN, zN, 1f, 0f, textureFloat, ao.getSideVertexAO(true, false, false),
                    xP, yN, zP, 1f, 1f, textureFloat, ao.getSideVertexAO(true, false, true)
                };
            }

        }
        throw new RuntimeException("Unknown side: " + side.name());
    }

    private BlockFacesVertices() {

    }
}
