package com.github.tommyettinger.glamer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.distancefield.DistanceFieldGenerator;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.StringBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    private String[] filenames = {
            "assets/DejaVuSansMono-Bold.ttf",
            "assets/DejaVuSansMono-BoldOblique.ttf",
            "assets/DejaVuSansMono-Oblique.ttf",
            "assets/DejaVuSansMono.ttf",
            "assets/Iosevka-Bold.ttf",
            "assets/Iosevka-BoldOblique.ttf",
            "assets/Iosevka-Oblique.ttf",
            "assets/Iosevka-Slab-Bold.ttf",
            "assets/Iosevka-Slab-BoldOblique.ttf",
            "assets/Iosevka-Slab-Oblique.ttf",
            "assets/Iosevka-Slab.ttf",
            "assets/Iosevka.ttf",
            "assets/SourceCodePro-Bold.otf",
            "assets/SourceCodePro-Medium.otf",
    }, baseNames = {
            "DejaVuSansMono-Bold",
            "DejaVuSansMono-BoldOblique",
            "DejaVuSansMono-Oblique",
            "DejaVuSansMono",
            "Iosevka-Bold",
            "Iosevka-BoldOblique",
            "Iosevka-Oblique",
            "Iosevka-Slab-Bold",
            "Iosevka-Slab-BoldOblique",
            "Iosevka-Slab-Oblique",
            "Iosevka-Slab",
            "Iosevka",
            "SourceCodePro-Bold",
            "SourceCodePro-Medium",

    };
    private String allChars =
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno"+
            "pqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàá"+
            "âãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİı"+
            "ĲĳĴĵĶķĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƒǺǻǼǽǾ"+
            "ǿȘșȚțȷˆˇˉˋ˘˙˚˛˜˝;΄΅Ά·ΈΉΊΌΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυ"+
            "φχψωϊϋόύώЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхц"+
            "чшщъыьэюяѐёђѓєѕіїјљњћќѝўџѴѵҐґẀẁẂẃẄẅỲỳ–—‘’‚‛“”„†‡•…‰‹›ⁿ₤€№™Ω℮←↑→↓∆−√≈" +
            "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬■□▲▼○●◦♀♂♠♣♥♦♪";


    // "msdfgen.exe -font " + filename + " " + codepoint + " -scale 2.5 -translate 2 4.5 -size 32 64 -o " + codepoint + ".png"
    // msdfgen.exe -font assets\Iosevka-Slab-Bold.ttf 64 -scale 2.5 -translate 2 4.5 -size 32 64 -o 64.png
    @Override
    public void create() {
        super.create();
        try {
            int mainSize = 2048,
                    blockWidth = 48, blockHeight = 96;
            // change command[2] to filename
            // change command[3] to the decimal codepoint printed as a string, such as "33"
            // change command[5] to "3.25" after processing DJV
            // change command[8] to "7.5" after processing DJV
            List<String> command = Arrays.asList("msdfgen.exe", "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "2", "-translate", "3", "12", "-size", "48", "96", "-o", "temp.png");
            String filename, baseName;
            for (int nm = 0; nm < filenames.length; nm++) {
                filename = filenames[nm];
                baseName = baseNames[nm];
                if(nm == 4)
                {
                    command.set(5, "3.25");
                    command.set(8, "7.5");
                }
                command.set(2, filename);
                int width = 42, height = 21, baseline = 24;

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(baseName).append("\" size=-48 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=0,0,0,0 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append(64).append(" base=").append(baseline).append(" scaleW=1024 scaleH=1024 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(baseName).append("-distance.png\"\n");
                sb.append("chars count=").append(allChars.length()).append('\n');
                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_4BYTE_ABGR),
                        board;
                Graphics2D g = image.createGraphics();
                g.clearRect(0, 0, mainSize, mainSize);
                int i = 0, max = allChars.length();
                int c;
                ProcessBuilder proc = new ProcessBuilder(command);
                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        c = allChars.charAt(i++);
                        command.set(3, String.valueOf(c));
                        proc.start().waitFor();
                        board = ImageIO.read(new File("temp.png"));
                        g.drawImage(board,
                                x * blockWidth, //bw+(2<<downscale)
                                y * blockHeight, //bh+(2<<downscale)
                                null);
                        //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                        sb.append("char id=").append(c)
                                .append(" x=").append(x * blockWidth) //bw+(2<<downscale)
                                .append(" y=").append(y * blockHeight)
                                .append(" width=").append(blockWidth) //bw+(2<<downscale)
                                .append(" height=").append(blockHeight)
                                .append(" xoffset=-1 yOffset=-1 xadvance=").append(24)
                                .append(" page=0 chnl=15\n");
                    }
                }
                if (i < allChars.length())
                    System.out.println("Too many chars!");
                ImageIO.write(image, "PNG", new File(baseName + "-msdf.png"));
                Gdx.files.local(baseName + "-distance.fnt").writeString(sb.toString(), false);
                sb.setLength(0);
//            char cc;
//            for (int j = 0; j < chars.size; j++) {
//                cc = chars.get(j);
//                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
//            }
                Gdx.files.local(baseName + "-contents.txt").writeString(allChars, false);
            }
            System.out.println("Done!");
        } catch(IOException e){
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }
    public void createSeveral() {
        super.create();
        try {
            int downscale = 4, mainSize = 1024, bigSize = mainSize << downscale,
                    blockWidth = (64 - 14 << downscale), blockHeight = (128 - 14 << downscale);
            for (String filename : filenames) {
                FileHandle fontFile =  Gdx.files.local(filename);
                Rectangle2D bounds, xBounds;
                int bw = 0, bh = 0;
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile.file()).deriveFont(3000f);
                // "assets/Iosevka.ttf" 5.5f
                // "assets/Galaxsea-Starlight-Mono-v3_1.ttf" 12f
                // "assets/SourceCodePro-Medium.otf" // fontSize 6.5f
                // "assets/DejaVuSansMono.ttf" // fontSize 4.75f
                // "assets/Galaxsea-Starlight-Mono-v3_1.ttf" // fontSize 12f
                CharArray chars = new CharArray(128);
                do {
                    font = font.deriveFont(0.95f * font.getSize());
                    BufferedImage tImage = new BufferedImage(2048, 2048, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D tGraphics = tImage.createGraphics();
                    tGraphics.setFont(font);
                    FontRenderContext frc = tGraphics.getFontRenderContext();
                    char[] tc = new char[]{'X'}, all = new char[0x80];
                    for (char i = 0; i < 0x80; i++) {
                        all[i] = i;
                    }
                    all[0x7f] = 0xffff;
                    int missing = font.getMissingGlyphCode();
                    GlyphVector gv = font.createGlyphVector(frc, tc), gv2 = font.createGlyphVector(frc, tc);
                    bounds = gv2.getVisualBounds();
                    xBounds = gv.getVisualBounds();
                    chars.clear();
                    for (char g = 32; g < 0x7f; g++) {
                        tc[0] = g;
                        gv2 = font.createGlyphVector(frc, tc);
                        if (gv2.getGlyphCode(0) != missing) {
                            chars.add(g);
                            bounds.add(gv2.getVisualBounds());
                        }
                    }

                    bw = (((2 << downscale) + (int) bounds.getWidth()) >> downscale) << downscale;
                    bh = (((4 << downscale) + (int) (1 + bounds.getMaxY() - bounds.getMinY())) >> downscale) << downscale;
                } while (bw > blockWidth - (12 << downscale) || bh > blockHeight - (12 << downscale));

                int
                        width = (bigSize - (20 << downscale)) / (blockWidth + (12 << downscale)),
                        height = (bigSize - (20 << downscale)) / (blockHeight + (12 << downscale)),
                        offTop = (int) (bounds.getMaxY() - xBounds.getMaxY()),
                        baseline = (int) ((bounds.getMaxY() - bounds.getMinY()) - xBounds.getMinY() + bounds.getMinY() + (1 << downscale)); // + offTop //some fonts need this
                // (int)(bh + xBounds.getMinY() + ((1<<downscale)-0.001))

                System.out.println("bh: " + bh);
                System.out.println("bw: " + bw);
                System.out.println("offTop: " + offTop);
                System.out.println("baseline: " + baseline);

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(fontFile.nameWithoutExtension()).append("\" size=-24 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=1,1,1,1 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append(((10 << downscale) + blockHeight >> downscale)).append(" base=").append(baseline >> downscale).append(" scaleW=1024 scaleH=1024 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(fontFile.nameWithoutExtension()).append("-distance.png\"\n");
                sb.append("chars count=").append(chars.size).append('\n');
                BufferedImage image = new BufferedImage(bigSize, bigSize, BufferedImage.TYPE_4BYTE_ABGR),
                        board = new BufferedImage((11 << downscale) + blockWidth, (11 << downscale) + blockHeight, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g = image.createGraphics(), gb = board.createGraphics();
                g.clearRect(0, 0, bigSize, bigSize);
                gb.clearRect(0, 0, (11 << downscale) + blockWidth, (11 << downscale) + blockHeight);
                gb.setColor(Color.white);
                gb.setFont(font);
                gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                gb.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
                gb.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                int i = 0, max = chars.size;
                char c;
                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        c = chars.get(i++);
                        gb.clearRect(0, 0, (11 << downscale) + blockWidth, (11 << downscale) + blockHeight);
                        gb.drawString(String.valueOf(c), 1 << downscale, baseline); // + (1 << downscale)
                        g.drawImage(board,
                                (20 << downscale) + x * (blockWidth + (12 << downscale)), //bw+(2<<downscale)
                                (20 << downscale) + y * (blockHeight + (12 << downscale)), //bh+(2<<downscale)
                                null);
                        //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                        sb.append("char id=").append((int) c)
                                .append(" x=").append((20 << downscale) + x * (blockWidth + (12 << downscale)) >> downscale) //bw+(2<<downscale)
                                .append(" y=").append((20 << downscale) + y * (blockHeight + (12 << downscale)) >> downscale) //bh+(2<<downscale)
                                .append(" width=").append(((10 << downscale) + blockWidth >> downscale) + 1) //bw+(2<<downscale)
                                .append(" height=").append(((10 << downscale) + blockHeight >> downscale))    //bh+(2<<downscale)
                                .append(" xoffset=-1 yOffset=-1 xadvance=").append((((10 << downscale) + blockWidth) >> downscale))
                                .append(" page=0 chnl=15\n");
                    }
                }
                if (i < chars.size)
                    System.out.println("Too many chars!");
                System.out.println("Showed " + i + " chars out of " + chars.size);
                //ImageIO.write(image, "PNG", new File(fontFile.nameWithoutExtension() + ".png"));
                DistanceFieldGenerator dfg = new DistanceFieldGenerator();
                dfg.setDownscale(1 << downscale);
                dfg.setSpread((1 << downscale) * 10f);
                BufferedImage dfgi = dfg.generateDistanceField(image);
                ImageIO.write(dfgi, "PNG", new File(fontFile.nameWithoutExtension() + "-distance.png"));
                LookupTable lookup = new LookupTable(0, 4)
                {
                    @Override
                    public int[] lookupPixel(int[] src, int[] dest)
                    {
                        dest[0] = (255-src[0]);
                        dest[1] = (255-src[1]);
                        dest[2] = (255-src[2]);
                        return dest;
                    }
                };
                LookupOp op = new LookupOp(lookup, new RenderingHints(null));
                ImageIO.write(op.filter(dfgi, null), "PNG", new File(fontFile.nameWithoutExtension() + "-distance-preview.png"));
                Gdx.files.local(fontFile.nameWithoutExtension() + "-distance.fnt").writeString(sb.toString(), false);
                sb.setLength(0);
//            char cc;
//            for (int j = 0; j < chars.size; j++) {
//                cc = chars.get(j);
//                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
//            }
                Gdx.files.local(fontFile.nameWithoutExtension() + "-contents.txt").writeString(String.valueOf(chars.toArray()), false);
            }
            System.out.println("Done!");
        } catch(FontFormatException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

    }
    public void createNormal() {
        super.create();
        try {
            int downscale = 3, mainSize = 2048, bigSize = mainSize << downscale;
            float fontSize = 5.4f;
            FileHandle fontFile = (args.length >= 1 && args[0] != null) ? Gdx.files.local(args[0]) : Gdx.files.local(
                    "assets/Iosevka-Slab.ttf"
                    // "assets/Iosevka.ttf" 5.5f
                    // "assets/Galaxsea-Starlight-Mono-v3_1.ttf" 12f
                    // "assets/SourceCodePro-Medium.otf" // fontSize 6.5f
                    // "assets/DejaVuSansMono.ttf" // fontSize 4.75f
                    // "assets/Galaxsea-Starlight-Mono-v3_1.ttf" // fontSize 12f
            );
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile.file()).deriveFont((args.length >= 2 && args[1] != null) ?
                    Float.parseFloat(args[1]) : 64f * fontSize);
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
            int bw = (((2<<downscale) + (int)bounds.getWidth()) >> downscale) << downscale,
                    bh = (((4<<downscale) + (int)(bounds.getHeight() + 1)) >> downscale) << downscale,
                    width = bigSize / (bw+(2<<downscale)), height = bigSize / (bh+(2<<downscale)),
                    offTop = (int) (bounds.getMaxY() - xBounds.getMaxY()),
                    baseline = (int)(bounds.getHeight() - xBounds.getMinY() + bounds.getMinY() + (1 << downscale) + offTop); // + offTop //some fonts need this
            // (int)(bh + xBounds.getMinY() + ((1<<downscale)-0.001))

            System.out.println("bh: " + bh);
            System.out.println("offTop: " + offTop);
            System.out.println("baseline: " + baseline);

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
                    gb.clearRect(0,0, bw, bh);
                    gb.drawString(String.valueOf(c), 1 << downscale, baseline); // + (1 << downscale)
                    g.drawImage(board,
                            x*(bw+(2<<downscale)), //bw+(2<<downscale)
                            y*(bh+(2<<downscale)), //bh+(2<<downscale)
                            null);
                    //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                    sb.append("char id=").append((int)c)
                            .append(" x=").append((x * (bw+(2<<downscale)) >> downscale)) //bw+(2<<downscale)
                            .append(" y=").append(y * (bh+(2<<downscale)) >> downscale) //bh+(2<<downscale)
                            .append(" width=").append((bw>>downscale)+1) //bw+(2<<downscale)
                            .append(" height=").append((bh>>downscale))    //bh+(2<<downscale)
                            .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw)>>downscale))
                            .append(" page=0 chnl=15\n");
                }
            }
            if(i < chars.size)
                System.out.println("Too many chars!");
            System.out.println("Showed " + i + " chars out of " + chars.size);
            //ImageIO.write(image, "PNG", new File(fontFile.nameWithoutExtension() + ".png"));
            DistanceFieldGenerator dfg = new DistanceFieldGenerator();
            dfg.setDownscale(1 << downscale);
            dfg.setSpread((float)Math.pow(2, downscale) * 3.5f * MathUtils.log(5f, fontSize));
            ImageIO.write(dfg.generateDistanceField(image), "PNG", new File(fontFile.nameWithoutExtension() + "-distance.png"));
            Gdx.files.local(fontFile.nameWithoutExtension() + "-distance.fnt").writeString(sb.toString(), false);
            sb.setLength(0);
//            char cc;
//            for (int j = 0; j < chars.size; j++) {
//                cc = chars.get(j);
//                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
//            }
            Gdx.files.local(fontFile.nameWithoutExtension() + "-contents.txt").writeString(String.valueOf(chars.toArray()), false);
            System.out.println("Done!");
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}