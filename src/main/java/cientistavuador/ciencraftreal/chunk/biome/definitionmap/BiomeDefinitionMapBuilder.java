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
package cientistavuador.ciencraftreal.chunk.biome.definitionmap;

import cientistavuador.ciencraftreal.chunk.biome.BiomeMap;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author Cien
 */
public class BiomeDefinitionMapBuilder {
    
    private final int srcFbo = glGenFramebuffers();
    private final int srcTx = glGenTextures();
    
    private final int dstFbo = glGenFramebuffers();
    private final int dstTx = glGenTextures();
    
    private BiomeMap map = null;
    private int definitionField = 0;
    
    protected BiomeDefinitionMapBuilder() {
        int size = BiomeMap.MAP_SIZE;
        int sizeMul = size * BiomeDefinitionMap.MAP_SIZE_MULTIPLIER;
        
        glActiveTexture(GL_TEXTURE0);
        
        glBindTexture(GL_TEXTURE_2D, this.srcTx);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, size, size, 0, GL_RED, GL_FLOAT, 0);
        
        glBindTexture(GL_TEXTURE_2D, this.dstTx);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, sizeMul, sizeMul, 0, GL_RED, GL_FLOAT, 0);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        
        glBindFramebuffer(GL_FRAMEBUFFER, this.srcFbo);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.srcTx, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO not complete.");
        }
        glReadBuffer(GL_COLOR_ATTACHMENT0);
        glDrawBuffer(GL_NONE);
        
        glBindFramebuffer(GL_FRAMEBUFFER, this.dstFbo);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.dstTx, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO not complete.");
        }
        glReadBuffer(GL_NONE);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getDefinitionField() {
        return definitionField;
    }

    public BiomeMap getMap() {
        return map;
    }

    public BiomeDefinitionMapBuilder setDefinitionField(int definitionField) {
        this.definitionField = definitionField;
        return this;
    }

    public BiomeDefinitionMapBuilder setMap(BiomeMap map) {
        this.map = map;
        return this;
    }
    
    public BiomeDefinitionMap build() {
        int size = BiomeMap.MAP_SIZE;
        FloatBuffer definitionInput = memAllocFloat(size * size);
        for (int i = 0; i < this.map.length(); i++) {
            definitionInput.put(this.map.getBiomeAtIndex(i).getDefinition().get(this.definitionField));
        }
        definitionInput.flip();
        
        glActiveTexture(GL_TEXTURE0);
        
        glBindTexture(GL_TEXTURE_2D, this.srcTx);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, size, size, 0, GL_RED, GL_FLOAT, definitionInput);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        memFree(definitionInput);
        
        int sizeMul = size * BiomeDefinitionMap.MAP_SIZE_MULTIPLIER;
        
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.srcFbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.dstFbo);
        glBlitFramebuffer(0, 0, size, size, 0, 0, sizeMul, sizeMul, GL_COLOR_BUFFER_BIT, GL_LINEAR);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        glBindTexture(GL_TEXTURE_2D, this.dstTx);
        float[] definitionOutput = new float[sizeMul * sizeMul];
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RED, GL_FLOAT, definitionOutput);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        return new BiomeDefinitionMap(this.map, definitionOutput);
    }
    
    public void delete() {
        glDeleteFramebuffers(this.srcFbo);
        glDeleteTextures(this.srcTx);
        glDeleteFramebuffers(this.dstFbo);
        glDeleteTextures(this.dstTx);
    }
    
}
