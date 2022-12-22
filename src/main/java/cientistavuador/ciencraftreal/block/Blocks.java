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

import cientistavuador.ciencraftreal.block.blocks.Bedrock;
import cientistavuador.ciencraftreal.block.blocks.CoalOre;
import cientistavuador.ciencraftreal.block.blocks.Dirt;
import cientistavuador.ciencraftreal.block.blocks.Grass;
import cientistavuador.ciencraftreal.block.blocks.IronOre;
import cientistavuador.ciencraftreal.block.blocks.Stone;

/**
 * @author Shinoa Hiragi
 * @author Cien
 */
public class Blocks {
    
    //0.5-DEV
    public static final Block AIR = null;
    public static final Grass GRASS = new Grass();
    public static final Dirt DIRT = new Dirt();
    public static final Stone STONE = new Stone();
    public static final Bedrock BEDROCK = new Bedrock();
    
    //0.6-DEV
    public static final CoalOre COAL_ORE = new CoalOre();
    public static final IronOre IRON_ORE = new IronOre();
    
    static {
        //0.5-DEV
        BlockRegister.register(GRASS);
        BlockRegister.register(DIRT);
        BlockRegister.register(STONE);
        BlockRegister.register(BEDROCK);
        
        //0.6-DEV
        BlockRegister.register(COAL_ORE);
        BlockRegister.register(IRON_ORE);
    }
    
    public static void init() {
        
    }
    
    private Blocks() {
        
    }
    
}
