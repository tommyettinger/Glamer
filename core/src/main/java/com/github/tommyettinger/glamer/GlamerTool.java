package com.github.tommyettinger.glamer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.CharArray;

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
            FileHandle fontFile = (args.length >= 1 && args[0] != null) ? Gdx.files.local(args[0]) : Gdx.files.local("assets/SourceCodePro-Regular.otf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile.file()).deriveFont((args.length >= 2 && args[2] != null) ? Float.parseFloat(args[1]) : 200f);
            BufferedImage tImage = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D tGraphics = tImage.createGraphics();
            tGraphics.setFont(font);
            FontRenderContext frc = tGraphics.getFontRenderContext();
            char[] tc = new char[]{'\u253C'}, all = new char[0x10000];
            for (char i = 0; i < 0xffff; i++) {
                all[i] = i;
            }
            all[0xffff] = 0xffff;
            int missing = font.getMissingGlyphCode(), skip;
            GlyphVector gv = font.createGlyphVector(frc, all), gv2 = font.createGlyphVector(frc, tc);
            Rectangle2D bounds;
            CharArray chars = new CharArray(1024);
            if(gv2.getGlyphCode(0) == missing)
            {
                tc[0] = ' ';
                gv2 = font.createGlyphVector(frc, tc);
                skip = 32;
            }
            else
            {
                skip = 0x253C;
            }
            bounds = gv2.getVisualBounds();

            for (int i = 32; i <= 0xffdf; i++) {
                if(gv.getGlyphCode(i) != missing)
                {
                    chars.add((char)i);
                    /*
                    tc[0] = (char)i;
                    bounds = bounds.createUnion(font.createGlyphVector(frc, tc).getLogicalBounds());
                    */
                }
            }
            int bw = 8 + (int)bounds.getWidth(), bh = 8 + (int)(bounds.getHeight() + 0.5),
                    width = 8192 / bw, height = 8192 / bh;
            int startCode = 32;
            BufferedImage image = new BufferedImage(8192, 8192, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics();
            g.clearRect(0, 0, 8192, 8192);
            g.setColor(Color.white);
            g.setFont(font);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            int i = 0, max = chars.size;
            for (int y = 0; y < height && i < max; y++) {
                for (int x = 0; x < width && i < max; x++) {
                    g.drawString(String.valueOf(chars.get(i++)), x * bw, (y+1) * bh);
                }
            }
            ImageIO.write(image, "PNG", new File(fontFile.nameWithoutExtension() + ".png"));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}