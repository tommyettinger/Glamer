package com.github.tommyettinger.glamer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.distancefield.DistanceFieldGenerator;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.StringBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GlamerTool extends ApplicationAdapter {
    String[] args;
    public GlamerTool()
    {
        this(new String[0]);
    }
    public GlamerTool(String[] params)
    {
        args = (params == null) ? new String[0] : params;
    }

    @Override
    public void create() {
        super.create();
        try {
            int downscale = 3, mainSize = 2048, bigSize = mainSize << downscale;
            FileHandle fontFile = (args.length >= 1 && args[0] != null) ? Gdx.files.local(args[0]) : Gdx.files.local("assets/Galaxsea-Starlight-Mono-v3_1.ttf"); //"assets/SourceCodePro-Medium.otf"
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile.file()).deriveFont((args.length >= 2 && args[1] != null) ? Float.parseFloat(args[1]) : 64f * 13f);
            BufferedImage tImage = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D tGraphics = tImage.createGraphics();
            tGraphics.setFont(font);
            FontRenderContext frc = tGraphics.getFontRenderContext();
            char[] tc = new char[]{'\u253C'}, all = new char[0x10000];
            for (char i = 0; i < 0xffff; i++) {
                all[i] = i;
            }
            all[0xffff] = 0xffff;
            int missing = font.getMissingGlyphCode();
            GlyphVector gv = font.createGlyphVector(frc, all), gv2 = font.createGlyphVector(frc, tc);
            Rectangle2D bounds = null, xBounds;
            boolean incomplete = true;
            CharArray chars = new CharArray(1024);
            if(gv2.getGlyphCode(0) != missing) {
                gv2 = font.createGlyphVector(frc, tc);
                bounds = gv2.getVisualBounds();
                incomplete = false;
            }

            tc[0] = 'x';
            gv2 = font.createGlyphVector(frc, tc);
            if(gv2.getGlyphCode(0) == missing)
            {
                tc[0] = 'X';
                gv2 = font.createGlyphVector(frc, tc);
                if(gv2.getGlyphCode(0) == missing)
                    throw new IllegalArgumentException("Font is missing both 'x' and 'X'; at least one is needed to judge metrics.");
                xBounds = gv2.getVisualBounds();
            }
            else
            {
                xBounds = gv2.getVisualBounds();
            }/*
            Character.UnicodeBlock ub;
            for (int i = 32; i <= 0xffff; i++) {
                if(gv.getGlyphCode(i) != missing)
                {
                    ub = Character.UnicodeBlock.of(i);
                    if(ub != Character.UnicodeBlock.ARABIC
                            && ub != Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A
                            && ub != Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B)
                        chars.add((char)i);
                }
            }*/
            if(bounds == null)
                bounds = xBounds.getBounds2D();
            for (int i = 32; i <= 0xffff; i++) {
                if(gv.getGlyphCode(i) != missing)
                {
                    switch (Character.getDirectionality(i)) {
                        case Character.DIRECTIONALITY_WHITESPACE:
                            if(i != 32)
                                break;
                        case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                        case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
                        case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                        case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                        case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                        case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                        case Character.DIRECTIONALITY_UNDEFINED:
                        case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
                            if(incomplete) {
                                tc[0] = (char) i;
                                gv2 = font.createGlyphVector(frc, tc);
                                if (gv2.getGlyphCode(0) != missing) {
                                    chars.add((char) i);
                                    bounds.add(gv2.getVisualBounds());
                                }
                            }
                            else
                            {
                                chars.add((char)i);
                            }
                    }
                }
            }
            int bw = (((2 << downscale) + (int)bounds.getWidth()) >> downscale) << downscale,
                    bh = (((2 << downscale) + (int)(bounds.getHeight() + 1)) >> downscale) << downscale,
                    width = bigSize / (bw+(2<<downscale)), height = bigSize / (bh+(2<<downscale)), baseline = (int)(bh + xBounds.getMinY()); // (int)(bh + xBounds.getMinY() + ((1<<downscale)-0.001))
            StringBuilder sb = new StringBuilder(0x10000);
            sb.append("info face=\"").append(fontFile.nameWithoutExtension()).append("\" size=-24 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=1,1,1,1 spacing=0,0 outline=0\n");
            sb.append("common lineHeight=").append((bh>>downscale)).append(" base=").append(baseline>>downscale).append(" scaleW=1024 scaleH=1024 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
            sb.append("page id=0 file=\"").append(fontFile.nameWithoutExtension()).append("-distance.png\"\n");
            sb.append("chars count=").append(chars.size).append('\n');
            BufferedImage image = new BufferedImage(bigSize, bigSize, BufferedImage.TYPE_4BYTE_ABGR),
                    board = new BufferedImage(bw, bh, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics(), gb = board.createGraphics();
            g.clearRect(0, 0, bigSize, bigSize);
            gb.clearRect(0,0,bw,bh);
            gb.setColor(Color.white);
            gb.setFont(font);
            gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            gb.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            gb.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            int i = 0, max = chars.size;
            char c;
            for (int y = 0; y < height && i < max; y++) {
                for (int x = 0; x < width && i < max; x++) {
                    c=chars.get(i++);
                    gb.clearRect(0,0,bw,bh);
                    gb.drawString(String.valueOf(c), 1 << downscale, baseline + (1 << downscale));
                    g.drawImage(board, x*(bw+(2<<downscale))+1,y*(bh+(2<<downscale))+1, null);
                    //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                    sb.append("char id=").append((int)c).append(" x=").append(x * (bw+(2<<downscale)) >> downscale).append(" y=").append(y * (bh+2) >> downscale).append(" width=").append((bw+(2<<downscale)>>downscale)-1).append(" height=").append(bh+(2<<downscale)>>downscale)
                            .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw+2)>>downscale)).append(" page=0 chnl=15\n");
                }
            }
            if(i < chars.size)
                System.out.println("Too many chars!");
            System.out.println("Showed " + i + " chars out of " + chars.size);
            //ImageIO.write(image, "PNG", new File(fontFile.nameWithoutExtension() + ".png"));
            DistanceFieldGenerator dfg = new DistanceFieldGenerator();
            dfg.setDownscale(1 << downscale);
            dfg.setSpread(4 << downscale);
            ImageIO.write(dfg.generateDistanceField(image), "PNG", new File(fontFile.nameWithoutExtension() + "-distance.png"));
            Gdx.files.local(fontFile.nameWithoutExtension() + "-distance.fnt").writeString(sb.toString(), false);
            sb.setLength(0);
            char cc;
            for (int j = 0; j < chars.size; j++) {
                cc = chars.get(j);
                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
            }
            //String.copyValueOf(chars.toArray())
            Gdx.files.local(fontFile.nameWithoutExtension() + "-contents.txt").writeString(sb.toString(), false);
            System.out.println("Done!");
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}