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

/**
 *
 * @author Cien
 */
public class BlockFacesVertices {

    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture) {
        return generateFaceVertices(side, x, y, z, texture, 1f, 1f, 1f);
    }
    
    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture, AmbientOcclusion ao) {
        return generateFaceVertices(side, x, y, z, texture, 1f, 1f, 1f, ao);
    }
    
    public static float[] generateFaceVertices(BlockSide side, float x, float y, float z, int texture, float scaleX, float scaleY, float scaleZ)  {
        return generateFaceVertices(side, x, y, z, texture, scaleX, scaleY, scaleZ, null);
    }
    
    private static float getAO(AmbientOcclusion ao, boolean x, boolean y, boolean z) {
        return ao.getAO(AmbientOcclusion.getIndexByXYZ(x, y, z));
    }
    
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
                    xN, yP, zN, 1f, 1f, textureFloat, getAO(ao, false, true, false),
                    xP, yN, zN, 0f, 0f, textureFloat, getAO(ao, true, false, false),
                    xN, yN, zN, 1f, 0f, textureFloat, getAO(ao, false, false, false),
                    xN, yP, zN, 1f, 1f, textureFloat, getAO(ao, false, true, false),
                    xP, yP, zN, 0f, 1f, textureFloat, getAO(ao, true, true, false),
                    xP, yN, zN, 0f, 0f, textureFloat, getAO(ao, true, false, false)
                };
            }
            case SOUTH -> {
                return new float[]{
                    xN, yP, zP, 0f, 1f, textureFloat, getAO(ao, false, true, true),
                    xN, yN, zP, 0f, 0f, textureFloat, getAO(ao, false, false, true),
                    xP, yN, zP, 1f, 0f, textureFloat, getAO(ao, true, false, true),
                    xN, yP, zP, 0f, 1f, textureFloat, getAO(ao, false, true, true),
                    xP, yN, zP, 1f, 0f, textureFloat, getAO(ao, true, false, true),
                    xP, yP, zP, 1f, 1f, textureFloat, getAO(ao, true, true, true)
                };
            }
            case EAST -> {
                return new float[]{
                    xP, yP, zN, 1f, 1f, textureFloat, getAO(ao, true, true, false),
                    xP, yN, zP, 0f, 0f, textureFloat, getAO(ao, true, false, true),
                    xP, yN, zN, 1f, 0f, textureFloat, getAO(ao, true, false, false),
                    xP, yP, zP, 0f, 1f, textureFloat, getAO(ao, true, true, true),
                    xP, yN, zP, 0f, 0f, textureFloat, getAO(ao, true, false, true),
                    xP, yP, zN, 1f, 1f, textureFloat, getAO(ao, true, true, false)
                };
            }
            case WEST -> {
                return new float[]{
                    xN, yP, zN, 0f, 1f, textureFloat, getAO(ao, false, true, false),
                    xN, yN, zN, 0f, 0f, textureFloat, getAO(ao, false, false, false),
                    xN, yN, zP, 1f, 0f, textureFloat, getAO(ao, false, false, true),
                    xN, yP, zP, 1f, 1f, textureFloat, getAO(ao, false, true, true),
                    xN, yP, zN, 0f, 1f, textureFloat, getAO(ao, false, true, false),
                    xN, yN, zP, 1f, 0f, textureFloat, getAO(ao, false, false, true)
                };
            }
            case TOP -> {
                return new float[]{
                    xN, yP, zP, 0f, 0f, textureFloat, getAO(ao, false, true, true),
                    xP, yP, zP, 1f, 0f, textureFloat, getAO(ao, true, true, true),
                    xN, yP, zN, 0f, 1f, textureFloat, getAO(ao, false, true, false),
                    xN, yP, zN, 0f, 1f, textureFloat, getAO(ao, false, true, false),
                    xP, yP, zP, 1f, 0f, textureFloat, getAO(ao, true, true, true),
                    xP, yP, zN, 1f, 1f, textureFloat, getAO(ao, true, true, false)
                };
            }
            case BOTTOM -> {
                return new float[]{
                    xN, yN, zP, 0f, 1f, textureFloat, getAO(ao, false, false, true),
                    xN, yN, zN, 0f, 0f, textureFloat, getAO(ao, false, false, false),
                    xP, yN, zP, 1f, 1f, textureFloat, getAO(ao, true, false, true),
                    xN, yN, zN, 0f, 0f, textureFloat, getAO(ao, false, false, false),
                    xP, yN, zN, 1f, 0f, textureFloat, getAO(ao, true, false, false),
                    xP, yN, zP, 1f, 1f, textureFloat, getAO(ao, true, false, true)
                };
            }




        }
        throw new RuntimeException("Unknown side: " + side.name());
    }

    private BlockFacesVertices() {

    }
}
