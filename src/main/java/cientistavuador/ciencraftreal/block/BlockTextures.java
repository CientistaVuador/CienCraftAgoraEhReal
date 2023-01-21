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
 * @author Shinoa Hiragi
 * @author Cien
 */
public class BlockTextures {
    
    //0.5-DEV
    public static final int GRASS = BlockTextureLoader.push("vulpes_grass.png");
    public static final int GRASS_SIDE = BlockTextureLoader.push("vulpes_grass_side.png");
    public static final int DIRT = BlockTextureLoader.push("vulpes_dirt.png");
    public static final int STONE = BlockTextureLoader.push("vulpes_stone.png");
    public static final int BEDROCK = BlockTextureLoader.push("vulpes_bedrock.png");
    
    //0.6-DEV
    public static final int COAL_ORE = BlockTextureLoader.push("vulpes_coal_ore.png");
    public static final int IRON_ORE = BlockTextureLoader.push("vulpes_iron_ore.png");
    
    //1.0-DEV-HAPPY-2023
    public static final int HAPPY_2023 = BlockTextureLoader.push("cien_happy_2023.png");
    
    //2.0-DEV
    public static final int LEAVES = BlockTextureLoader.push("vulpes_leaves.png");
    public static final int TREE = BlockTextureLoader.push("vulpes_tree.png");
    public static final int TREE_TOP = BlockTextureLoader.push("vulpes_tree_top.png");
    public static final int DEAD_TREE = BlockTextureLoader.push("vulpes_dead_tree.png");
    public static final int DEAD_TREE_TOP = BlockTextureLoader.push("vulpes_dead_tree_top.png");
    public static final int MYCELIUM = BlockTextureLoader.push("vulpes_dirt_with_mycelium.png");
    public static final int MYCELIUM_TOP = BlockTextureLoader.push("vulpes_mycelium.png");
    public static final int SAND = BlockTextureLoader.push("vulpes_sand.png");
    public static final int WATER = BlockTextureLoader.push("cien_water.png");
    
    public static final int GL_TEXTURE_ARRAY;
    
    static {
        BlockTextureLoader.loadTextures();
        GL_TEXTURE_ARRAY = BlockTextureLoader.getGLTextureArray();
    }
    
    public static void init() {
        
    }
    
    private BlockTextures() {
        
    }
}
