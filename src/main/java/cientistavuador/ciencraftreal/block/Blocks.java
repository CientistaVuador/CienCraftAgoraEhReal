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
import cientistavuador.ciencraftreal.block.blocks.DeadWood;
import cientistavuador.ciencraftreal.block.blocks.Dirt;
import cientistavuador.ciencraftreal.block.blocks.Grass;
import cientistavuador.ciencraftreal.block.blocks.Happy2023;
import cientistavuador.ciencraftreal.block.blocks.IronOre;
import cientistavuador.ciencraftreal.block.blocks.Leaves;
import cientistavuador.ciencraftreal.block.blocks.Mycelium;
import cientistavuador.ciencraftreal.block.blocks.Sand;
import cientistavuador.ciencraftreal.block.blocks.Stone;
import cientistavuador.ciencraftreal.block.blocks.Water;
import cientistavuador.ciencraftreal.block.blocks.Wood;

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
    
    //1.0-DEV-HAPPY-2023
    public static final Happy2023 HAPPY_2023 = new Happy2023();
    
    //2.0-DEV
    public static final Wood WOOD = new Wood();
    public static final Leaves LEAVES = new Leaves();
    public static final DeadWood DEAD_WOOD = new DeadWood();
    public static final Mycelium MYCELIUM = new Mycelium();
    public static final Sand SAND = new Sand();
    public static final Water WATER = new Water();
    
    static {
        //0.5-DEV
        BlockRegister.register(GRASS);
        BlockRegister.register(DIRT);
        BlockRegister.register(STONE);
        BlockRegister.register(BEDROCK);
        
        //0.6-DEV
        BlockRegister.register(COAL_ORE);
        BlockRegister.register(IRON_ORE);
        
        //1.0-DEV-HAPPY-2023
        BlockRegister.register(HAPPY_2023);
        
        //2.0-DEV
        BlockRegister.register(WOOD);
        BlockRegister.register(LEAVES);
        BlockRegister.register(DEAD_WOOD);
        BlockRegister.register(MYCELIUM);
        BlockRegister.register(SAND);
        BlockRegister.register(WATER);
    }
    
    public static final Block[] ORES = new Block[] {
        COAL_ORE,
        IRON_ORE
    };
    
    public static void init() {
        
    }
    
    private Blocks() {
        
    }
    
}
