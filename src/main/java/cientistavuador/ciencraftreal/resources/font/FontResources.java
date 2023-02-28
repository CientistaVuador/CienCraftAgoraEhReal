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
package cientistavuador.ciencraftreal.resources.font;

import cientistavuador.ciencraftreal.resources.image.ImageResources;
import cientistavuador.ciencraftreal.resources.image.NativeImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Cien
 */
public class FontResources {

    public static Font load(String name) {
        Future<NativeImage> futureAtlasImage = CompletableFuture.supplyAsync(() -> {
            InputStream fontStream = getFontStream(name);
            if (fontStream == null) {
                throw new RuntimeException("Font '" + name + "' not found.");
            }
            try {
                try (ZipInputStream zipStream = new ZipInputStream(fontStream, StandardCharsets.UTF_8)) {
                    ZipEntry e;
                    while ((e = zipStream.getNextEntry()) != null) {
                        if (e.getName().equals("atlas.png")) {
                            return ImageResources.load(name+"/atlas.png", zipStream, (int) e.getSize(), 4);
                        }
                    }
                    throw new RuntimeException("Font Atlas '" + name + "/atlas.png' not found.");
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Future<Integer> futureUnknownCharacter = CompletableFuture.supplyAsync(() -> {
            InputStream fontStream = getFontStream(name);
            if (fontStream == null) {
                throw new RuntimeException("Font '" + name + "' not found.");
            }
            try {
                try (ZipInputStream zipStream = new ZipInputStream(fontStream, StandardCharsets.UTF_8)) {
                    ZipEntry e;
                    while ((e = zipStream.getNextEntry()) != null) {
                        if (e.getName().equals("unknown.txt")) {
                            String unknownText;
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8))) {
                                unknownText = reader.readLine();
                            }
                            if (unknownText.length() == 0) {
                                throw new RuntimeException("Font Unknown Character '" + name + "/unknown.txt' is empty.");
                            }
                            return unknownText.codePointAt(0);
                        }
                    }
                    throw new RuntimeException("Font Unknown Character '" + name + "/unknown.txt' not found.");
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Future<FontCharacter[]> futureFontCharacters = CompletableFuture.supplyAsync(() -> {
            InputStream fontStream = getFontStream(name);
            if (fontStream == null) {
                throw new RuntimeException("Font '" + name + "' not found.");
            }
            try {
                try (ZipInputStream zipStream = new ZipInputStream(fontStream, StandardCharsets.UTF_8)) {
                    ZipEntry e;
                    while ((e = zipStream.getNextEntry()) != null) {
                        if (e.getName().equals("metrics.csv")) {
                            List<String> lines = new ArrayList<>();
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8))) {
                                String s;
                                while ((s = reader.readLine()) != null) {
                                    lines.add(s);
                                }
                            }

                            FontCharacter[] characters = new FontCharacter[lines.size()];

                            int index = 0;
                            for (String line : lines) {
                                String[] split = line.split(Pattern.quote(","));

                                if (split.length != 10) {
                                    throw new RuntimeException("Font Metrics is invalid or corrupted.");
                                }

                                int unicodePoint = Integer.parseInt(split[0]);
                                float advance = Float.parseFloat(split[1]);
                                float planeBoundsLeft = Float.parseFloat(split[2]);
                                float planeBoundsBottom = Float.parseFloat(split[3]);
                                float planeBoundsRight = Float.parseFloat(split[4]);
                                float planeBoundsTop = Float.parseFloat(split[5]);
                                float atlasBoundsLeft = Float.parseFloat(split[6]);
                                float atlasBoundsBottom = Float.parseFloat(split[7]);
                                float atlasBoundsRight = Float.parseFloat(split[8]);
                                float atlasBoundsTop = Float.parseFloat(split[9]);
                                
                                characters[index] = new FontCharacter(unicodePoint, advance, planeBoundsLeft, planeBoundsBottom, planeBoundsRight, planeBoundsTop, atlasBoundsLeft, atlasBoundsBottom, atlasBoundsRight, atlasBoundsTop);
                                index++;
                            }

                            return characters;
                        }
                    }
                    throw new RuntimeException("Font Metrics '" + name + "/metrics.csv' not found.");
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        
        NativeImage image;
        try {
            image = futureAtlasImage.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        
        int unknownCharacter;
        try {
            unknownCharacter = futureUnknownCharacter.get();
        } catch (InterruptedException | ExecutionException ex) {
            image.free();
            throw new RuntimeException(ex);
        }
        
        FontCharacter[] characters;
        try {
            characters = futureFontCharacters.get();
        } catch (InterruptedException | ExecutionException ex) {
            image.free();
            throw new RuntimeException(ex);
        }
        
        return new Font(name, image, characters, unknownCharacter);
    }

    public static InputStream getFontStream(String name) {
        return FontResources.class.getResourceAsStream(name);
    }

    public static URL getFontURL(String name) {
        return FontResources.class.getResource(name);
    }

    private FontResources() {

    }
}
