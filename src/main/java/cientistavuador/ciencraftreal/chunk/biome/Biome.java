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
package cientistavuador.ciencraftreal.chunk.biome;

import java.util.Objects;

/**
 *
 * @author Cien
 */
public class Biome {

    private final String name;
    private final BiomeDefinition definition;
    private final BiomeGenerator generator;
    protected int id = -1;
    protected BiomeRegister register = null;
    
    public Biome(String name, BiomeDefinition definition, BiomeGenerator generator) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(definition);
        Objects.requireNonNull(generator);
        this.name = name;
        this.definition = definition;
        this.generator = generator;
    }

    public int getId() {
        if (this.id == -1) {
            throw new NullPointerException("Biome not registered.");
        }
        return id;
    }

    public BiomeRegister getRegister() {
        if (this.register == null) {
            throw new NullPointerException("Biome not registered.");
        }
        return register;
    }
    
    public String getName() {
        return name;
    }

    public BiomeDefinition getDefinition() {
        return definition;
    }

    public BiomeGenerator getGenerator() {
        return generator;
    }

    @Override
    public String toString() {
        return getName();
    }
    
}
