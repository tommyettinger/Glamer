package com.github.tommyettinger.glamer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.distancefield.DistanceFieldGenerator;
import com.badlogic.gdx.utils.*;

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
import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class GlamerTool extends ApplicationAdapter {
    public GlamerTool() {
        this(new String[0]);
    }

    public GlamerTool(String[] args) {
        switch (args.length)
        {
            case 9: baseline = Integer.parseInt(args[8]);
            case 8: params[9] = args[7];
            case 7: params[8] = args[6];
            case 6: params[12] = args[5];
            case 5: params[11] = args[4];
            case 4: params[6] = args[3];
            case 3: params[1] = args[2];
            case 2: params[4] = args[1];
            case 1: params[3] = args[0];
            break;
            /*
            "./msdfgen", //0
            "mono",      //1
            "-font", "Iosevka.ttf", "32", //2 3 4
            "-scale", "2.5", //5 6
            "-translate", "1", "5", //7 8 9
            "-size", "23", "54", //10 11 12
            "-pxrange", "4", //13 14
            "-o", "temp.png"}; //15 16

             */
            default:
                System.out.println("GlamerTool takes arguments in this form (only the font path is required):");
                System.out.println("java -jar GlamerTool.jar path/to/font.ttf size mode scale sizeX sizeY translateX translateY baseline");
                System.out.println("For example...");
                System.out.println("java -jar GlamerTool.jar Iosevka.ttf 32 mono 2.5 23 54 1 5 57");
                System.out.println("Or...");
                System.out.println("java -jar GlamerTool.jar Roboto-Regular.ttf 32 var 1 40 45 4 9 38");
                System.out.println("Attempting to run anyway on Iosevka.ttf...");

        }
    }
    /*
    Sample command lines:
    Roboto-Regular.ttf:
    java -jar GlamerTool.jar assets/Roboto-Regular.ttf 32 var 1 40 45 4 9 38
    Roboto-Bold.ttf:
    java -jar GlamerTool.jar assets/Roboto-Bold.ttf 32 var 1 40 45 4 9 38
     */
    
    public static void main(String[] args) {
        createApplication(args);
    }

    private static Application createApplication(String[] args) {
        // Note: you can use a custom ApplicationListener implementation for the headless project instead of GlamerTool.
        return new HeadlessApplication(new GlamerTool(args), getDefaultConfiguration());
    }

    private static HeadlessApplicationConfiguration getDefaultConfiguration() {
        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        configuration.renderInterval = -1f; // When this value is negative, GlamerTool#render() is never called.
        return configuration;
    }

    private String[] filenames = {
            "assets/DejaVuSansMono-Bold.ttf",
            "assets/DejaVuSansMono-BoldOblique.ttf",
            "assets/DejaVuSansMono-Oblique.ttf",
            "assets/DejaVuSansMono.ttf",
            "assets/Iosevka-Bold.ttf",
            "assets/Iosevka-BoldOblique.ttf",
            "assets/Iosevka-Oblique.ttf",
            "assets/Iosevka.ttf",
            "assets/Iosevka-Slab-Bold.ttf",
            "assets/Iosevka-Slab-BoldOblique.ttf",
            "assets/Iosevka-Slab-Oblique.ttf",
            "assets/Iosevka-Slab.ttf",
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
            "Iosevka",
            "Iosevka-Slab-Bold",
            "Iosevka-Slab-BoldOblique",
            "Iosevka-Slab-Oblique",
            "Iosevka-Slab",
            "SourceCodePro-Bold",
            "SourceCodePro-Medium",

    };
//    private String allChars =
//            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno"+
//            "pqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàá"+
//            "âãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİı"+
//            "ĲĳĴĵĶķĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƒǺǻǼǽǾ"+
//            "ǿȘșȚțȷˆˇˉˋ˘˙˚˛˜˝;΄΅Ά·ΈΉΊΌΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυ"+
//            "φχψωϊϋόύώЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхц"+
//            "чшщъыьэюяѐёђѓєѕіїјљњћќѝўџѴѵҐґẀẁẂẃẄẅỲỳ–—‘’‚‛“”„†‡•…‰‹›ⁿ₤€№™Ω℮←↑→↓∆−√≈" +
//            "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬■□▲▼○●◦♀♂♠♣♥♦♪";

    private String allChars =
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno" +
                    "pqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàá" +
                    "âãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİı" +
                    "ĴĵĶķĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƒǺǻǼǽǾǿ" +
                    "ȘșȚțȷˆˇˉˋ˘˙˚˛˜˝΄΅Ά·ΈΉΊΌΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυ" +
                    "φχψωϊϋόύώЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхц" +
                    "чшщъыьэюяѐёђѓєѕіїјљњћќѝўџѴѵҐґẀẁẂẃẄẅỲỳ–—‘’‚‛“”„†‡•…‰‹›ⁿ₤€№™Ω℮←↑→↓∆−√≈" +
                    "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬■□▲▼○●◦♀♂♠♣♥♦♪";

    private static String[] params = {
            "./msdfgen", //0
            "mono",      //1
            "-font", "assets/Iosevka.ttf", "32", //2 3 4
            "-scale", "2.5", //5 6
            "-translate", "1", "5", //7 8 9
            "-size", "23", "54", //10 11 12
            "-pxrange", "4", //13 14
            "-o", "temp.png"}; //15 16
    public static int baseline = 57;
    @Override
    public void create() {
        //create_msdf_family();
        if(params[1].contains("mono") || params[1].contains("fixed")) 
            create_msdf("msdf");
        else
            create_msdf_variable("msdf");
        //createFamily();
        //createNormal();
    }

    // "msdfgen.exe -font " + filename + " " + codepoint + " -scale 2.5 -translate 2 4.5 -size 32 64 -o " + codepoint + ".png"
    // msdfgen.exe -font assets\Iosevka-Slab-Bold.ttf 64 -scale 2.5 -translate 2 4.5 -size 32 64 -o 64.png
    public void create_msdf(String mode) {
        super.create();
        try {
            int mainSize = 2048,
                    blockWidth = Integer.parseInt(params[11]), blockHeight = Integer.parseInt(params[12]) - 4;
            // change command[2] to filename
            // change command[3] to the decimal codepoint printed as a string, such as "33"
            String os = System.getProperty("os.name"), processed = "linux";
            if(os.contains("indows"))
                processed = "win64";
            else if(os.contains("ac"))
                processed = "darwin";
            params[0] = processed + "/msdfgen";
            List<String> command = Arrays.asList(params);
            //List<String> command = Arrays.asList("msdfgen.exe", "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "3.2", "-translate", "3.5", "7.5", "-size", "44", "92", "-o", "temp.png");
            String filename, baseName;
            //for (int nm = 0; nm < filenames.length; nm++) {
            for (int nm = 3; nm < 4; nm++) {
                filename = params[3];
                baseName = filename.substring(Math.max(filename.lastIndexOf('/'), 
                        filename.lastIndexOf('\\')) + 1, filename.lastIndexOf('.'));
//                filename = filenames[nm];
//                baseName = baseNames[nm];
                
//                if (nm == 4) {
//                    command.set(5, "3.2");
//                    command.set(7, "3.5");
//                    command.set(8, "7.5");
//                } else if (nm == 8) {
//                    command.set(5, "3.75");
//                    command.set(7, "3.25");
//                    command.set(8, "5");
//                }                 

//                command.set(6, "2.5");    // scale
//                command.set(8, "1");    // translate x
//                command.set(9, "5");    // translate y
//                command.set(3, filename);
                Font font = Font.createFont(Font.TRUETYPE_FONT, Gdx.files.local(filename).file()).deriveFont(24f);
//                allChars = Gdx.files.local("assets/bloccus/bloccus-contents.txt").readString("UTF8");
                
                //allChars = Gdx.files.local("assets/Iosevka-Slab-contents.txt").readString("UTF-8");
                CharArray shown = new CharArray(font.getNumGlyphs());
                for (int i = 0; i < 65536; i++) {
                    if(font.canDisplay(i))
                        shown.add((char) i);
                }
                allChars = new String(shown.items, 0, shown.size);
//                int width = (mainSize - 4) / (blockWidth + 4), height = (mainSize - 4) / (blockHeight + 4),
//                        baseline = 48, idx = 0, c, limit = allChars.length() - 1;
//
//                CharArray chars = new CharArray(1024), regularChars = new CharArray(1024);
//                IntIntMap 
//                        widths = new IntIntMap(limit + 2 << 2),
//                        usedXs = new IntIntMap(limit + 2),
//                        usedYs = new IntIntMap(limit + 2);
//                BufferedImage tImage = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
//                Graphics2D tGraphics = tImage.createGraphics();
//                tGraphics.setFont(font);
//                FontRenderContext frc = tGraphics.getFontRenderContext();
//                char[] tc = new char[]{'X'};
//                GlyphVector gv2;
//
//                chars.add((char) 0);
//                    regularChars.add((char) 0);
//                    idx = 0;
//                    c = allChars.charAt(idx);
//                    while (c < 0xFFFF && idx < allChars.length()) {
//                        c = allChars.charAt(idx++);
//                        if (Character.isDefined(c)) {
//                            switch (Character.getDirectionality(c)) {
//                                case Character.DIRECTIONALITY_WHITESPACE:
//                                    if (c != 32)
//                                        break;
//                                case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
//                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
//                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
//                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
//                                case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
//                                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
//                                case Character.DIRECTIONALITY_UNDEFINED:
//                                case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
//                                    char ac = (char) c;
//                                    if (Character.isSurrogate(ac))
//                                        continue;
//                                    chars.add(ac);                                     
//                                    regularChars.add((char) c);
//                                    tc[0] = (char) (c);
//                                    gv2 = font.createGlyphVector(frc, tc);
////                                    if (c == 32)
////                                        widths.put(ac, 7);
////                                    else
////                                        widths.put(ac, (int) Math.ceil(gv2.getVisualBounds().getWidth()));
//                            }
//                        }
//                    }

                int width = mainSize / (blockWidth + 4), height = mainSize / (blockHeight + 4);

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(baseName).append("\" size=-").append(blockHeight)
                        .append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=1,1,1,1 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append(blockHeight) // very tricky to get right
                        .append(" base=").append(baseline)  // 56 or 57 ?
                        .append(" scaleW=2048 scaleH=2048 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(baseName).append("-msdf.png\"\n");
                sb.append("chars count=").append(allChars.length()+1).append('\n');
//                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_3BYTE_BGR),
                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_4BYTE_ABGR),
                        board;
                Graphics2D g = image.createGraphics();
                final Color clear = new Color(0, 0, 0);
                g.setColor(clear);
                g.fillRect(0, 0, mainSize, mainSize);
                int i = -1, max = allChars.length();
                int c;
                ProcessBuilder proc = new ProcessBuilder(command);

//                for (int y = 0; y < height && i < max; y++) {
//                    for (int x = 0; x < width && i < max; x++) {
//                        if(i == 0)
//                        {
//                            g.setColor(Color.white);
//                            g.fillRect(2, 2, blockWidth, blockHeight);
//                            usedXs.put(c, 2);
//                            usedYs.put(c, 2);
//                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
//                            sb.append("char id=").append(c)
//                                    .append(" x=").append(2) //bw+(2<<downscale)
//                                    .append(" y=").append(2) //bh+(2<<downscale)
//                                    .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
//                                    .append(" height=").append(blockHeight)
//                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth + 2)
//                                    .append(" page=0 chnl=15\n");
//                            x++;
//                        }
//                        c = allChars.charAt(i++);
//                        command.set(4, String.valueOf(c));
//                        proc.start().waitFor();
//                        board = ImageIO.read(new File("temp.png"));
//                        g.drawImage(board,
//                                2 + x * (4 + blockWidth), //bw+(2<<downscale)
//                                2 + y * (4 + blockHeight), //bh+(2<<downscale)
//                                null);
//
//                        usedXs.put(c, 2 + x * (4 + blockWidth));
//                        usedYs.put(c, 2 + y * (4 + blockHeight));
//                        sb.append("char id=").append(c)
//                                .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
//                                .append(" y=").append(2 + y * (4 + blockHeight))
//                                .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
//                                .append(" height=").append(blockHeight)
//                                .append(" xoffset=0 yOffset=-1 xadvance=").append(widths.get(c, -4) + 2)
//                                .append(" page=0 chnl=15\n");
//
//                    }
//                }

                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        if (i == -1) {
                            i++;
                            c = 0;
                            g.setColor(Color.white);
                            g.fillRect(
                                    2 + x * (4 + blockWidth), //bw+(2<<downscale)
                                    2 + y * (4 + blockHeight), //bh+(2<<downscale)
                                    blockWidth, blockHeight
                            );
                            sb.append("char id=").append(c)
                                    .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
                                    .append(" y=").append(2 + y * (4 + blockHeight))
                                    .append(" width=").append(blockWidth) //bw+(2<<downscale)
                                    .append(" height=").append(blockHeight)
                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth)
                                    .append(" page=0 chnl=15\n");
                        } else {
                            c = allChars.charAt(i++);
                            command.set(4, String.valueOf(c));
                            proc.start().waitFor();
                            board = ImageIO.read(new File("temp.png"));
                            g.drawImage(board,
                                    2 + x * (4 + blockWidth), //bw+(2<<downscale)
                                    2 + y * (4 + blockHeight), //bh+(2<<downscale)
                                    null);
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=").append(c)
                                    .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
                                    .append(" y=").append(2 + y * (4 + blockHeight))
                                    .append(" width=").append(blockWidth) //bw+(2<<downscale)
                                    .append(" height=").append(blockHeight)
                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth)
                                    .append(" page=0 chnl=15\n");
                        }
                    }
                }
                if (i + 1 < allChars.length())
                    System.out.println("Too many chars!");
                System.out.println("Showed " + i + " chars out of " + allChars.length());
                ImageIO.write(image, "PNG", new File(baseName + "-msdf.png"));
                Gdx.files.local(baseName + "-msdf.fnt").writeString(sb.toString(), false);
                sb.setLength(0);
//            char cc;
//            for (int j = 0; j < chars.size; j++) {
//                cc = chars.get(j);
//                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
//            }
                Gdx.files.local(baseName + "-contents.txt").writeString(allChars, false);
            }
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create_msdf_variable(String mode) {
        super.create();
        try {
            int mainSize = 2048,
               blockWidth = Integer.parseInt(params[11]), blockHeight = Integer.parseInt(params[12]) - 4;
            // change command[2] to filename
            // change command[3] to the decimal codepoint printed as a string, such as "33"
            String os = System.getProperty("os.name"), processed = "linux";
            if(os.contains("indows"))
                processed = "win64";
            else if(os.contains("ac"))
                processed = "darwin";
            params[0] = processed + "/msdfgen";
            List<String> command = Arrays.asList(params);
            //List<String> command = Arrays.asList("msdfgen.exe", "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "3.2", "-translate", "3.5", "7.5", "-size", "44", "92", "-o", "temp.png");
            String filename, baseName;
            //for (int nm = 0; nm < filenames.length; nm++) {
            for (int nm = 3; nm < 4; nm++) {
                filename = params[3];
                baseName = filename.substring(Math.max(filename.lastIndexOf('/'),
                   filename.lastIndexOf('\\')) + 1, filename.lastIndexOf('.'));
//                filename = filenames[nm];
//                baseName = baseNames[nm];

//                if (nm == 4) {
//                    command.set(5, "3.2");
//                    command.set(7, "3.5");
//                    command.set(8, "7.5");
//                } else if (nm == 8) {
//                    command.set(5, "3.75");
//                    command.set(7, "3.25");
//                    command.set(8, "5");
//                }                 

//                command.set(6, "2.5");    // scale
//                command.set(8, "1");    // translate x
//                command.set(9, "5");    // translate y
//                command.set(3, filename);
                Font font = Font.createFont(Font.TRUETYPE_FONT, (Gdx.files.internal(filename).exists()
                   ? Gdx.files.internal(filename) : Gdx.files.local(filename)).file()).deriveFont(24f);
//                allChars = Gdx.files.local("assets/bloccus/bloccus-contents.txt").readString("UTF8");

                //allChars = Gdx.files.local("assets/Iosevka-Slab-contents.txt").readString("UTF-8");
                CharArray shown = new CharArray(font.getNumGlyphs());
                for (int i = 0; i < 65536; i++) {
                    if(font.canDisplay(i))
                        shown.add((char) i);
                }
                allChars = new String(shown.items, 0, shown.size);
                int width = mainSize / (blockWidth + 4), height = mainSize / (blockHeight + 4),
                   baseline = 48, idx = 0, c, limit = allChars.length() - 1;

                CharArray chars = new CharArray(1024), regularChars = new CharArray(1024);
                IntIntMap
                   widths = new IntIntMap(limit + 2 << 2),
                   usedXs = new IntIntMap(limit + 2),
                   usedYs = new IntIntMap(limit + 2);
                BufferedImage tImage = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D tGraphics = tImage.createGraphics();
                tGraphics.setFont(font);
                FontRenderContext frc = tGraphics.getFontRenderContext();
                char[] tc = new char[]{'X'};
                GlyphVector gv2;

                chars.add((char) 0);
                regularChars.add((char) 0);
                c = allChars.charAt(idx);
                while (c < 0xFFFF && idx < allChars.length()) {
                    c = allChars.charAt(idx++);
                    if (Character.isDefined(c)) {
                        switch (Character.getDirectionality(c)) {
                        case Character.DIRECTIONALITY_WHITESPACE:
                            if (c != 32)
                                break;
                        case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                        case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
                        case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                        case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                        case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                        case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                        case Character.DIRECTIONALITY_UNDEFINED:
                        case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
                            char ac = (char) c;
                            if (Character.isSurrogate(ac))
                                continue;
                            chars.add(ac);
                            regularChars.add((char) c);
                            tc[0] = (char) (c);
                            gv2 = font.createGlyphVector(frc, tc);
                            if (c == 32)
                                widths.put(ac, 7);
                            else
                                widths.put(ac, (int) Math.ceil(gv2.getVisualBounds().getWidth()));
                        }
                    }
                }

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(baseName).append("\" size=-").append(blockHeight)
                   .append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=1,1,1,1 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append(blockHeight) // very tricky to get right
                   .append(" base=").append(baseline)  // 56 or 57 ?
                   .append(" scaleW=2048 scaleH=2048 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(baseName).append("-msdf.png\"\n");
                sb.append("chars count=").append(allChars.length()+1).append('\n');
//                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_3BYTE_BGR),
                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_4BYTE_ABGR),
                   board;
                Graphics2D g = image.createGraphics();
                final Color clear = new Color(0, 0, 0);
                g.setColor(clear);
                g.fillRect(0, 0, mainSize, mainSize);
                int i = -1, max = allChars.length();
                ProcessBuilder proc = new ProcessBuilder(command);

                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        if(i == -1)
                        {
                            i++;
                            c = 0;
                            g.setColor(Color.white);
                            g.fillRect(2, 2, blockWidth, blockHeight);
                            usedXs.put(c, 2);
                            usedYs.put(c, 2);
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=").append(c)
                               .append(" x=").append(2) //bw+(2<<downscale)
                               .append(" y=").append(2) //bh+(2<<downscale)
                               .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
                               .append(" height=").append(blockHeight)
                               .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth + 2)
                               .append(" page=0 chnl=15\n");
                            x++;
                        }
                        c = allChars.charAt(i++);
                        command.set(4, String.valueOf(c));
                        proc.start().waitFor();
                        board = ImageIO.read(new File("temp.png"));
                        g.drawImage(board,
                           2 + x * (4 + blockWidth), //bw+(2<<downscale)
                           2 + y * (4 + blockHeight), //bh+(2<<downscale)
                           null);

                        usedXs.put(c, 2 + x * (4 + blockWidth));
                        usedYs.put(c, 2 + y * (4 + blockHeight));
                        sb.append("char id=").append(c)
                           .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
                           .append(" y=").append(2 + y * (4 + blockHeight))
                           .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
                           .append(" height=").append(blockHeight)
                           .append(" xoffset=0 yOffset=-1 xadvance=").append(widths.get(c, -4) + 4)
                           .append(" page=0 chnl=15\n");

                    }
                }
////used for monospace fonts
//                for (int y = 0; y < height && i < max; y++) {
//                    for (int x = 0; x < width && i < max; x++) {
//                        if (i == -1) {
//                            i++;
//                            c = 0;
//                            g.setColor(Color.white);
//                            g.fillRect(
//                                    2 + x * (4 + blockWidth), //bw+(2<<downscale)
//                                    2 + y * (4 + blockHeight), //bh+(2<<downscale)
//                                    blockWidth, blockHeight
//                            );
//                            sb.append("char id=").append(c)
//                                    .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
//                                    .append(" y=").append(2 + y * (4 + blockHeight))
//                                    .append(" width=").append(blockWidth) //bw+(2<<downscale)
//                                    .append(" height=").append(blockHeight)
//                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth)
//                                    .append(" page=0 chnl=15\n");
//                        } else {
//                            c = allChars.charAt(i++);
//                            command.set(4, String.valueOf(c));
//                            proc.start().waitFor();
//                            board = ImageIO.read(new File("temp.png"));
//                            g.drawImage(board,
//                                    2 + x * (4 + blockWidth), //bw+(2<<downscale)
//                                    2 + y * (4 + blockHeight), //bh+(2<<downscale)
//                                    null);
//                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
//                            sb.append("char id=").append(c)
//                                    .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
//                                    .append(" y=").append(2 + y * (4 + blockHeight))
//                                    .append(" width=").append(blockWidth) //bw+(2<<downscale)
//                                    .append(" height=").append(blockHeight)
//                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth)
//                                    .append(" page=0 chnl=15\n");
//                        }
//                    }
//                }
                if (i + 1 < allChars.length())
                    System.out.println("Too many chars!");
                System.out.println("Showed " + i + " chars out of " + allChars.length());
                ImageIO.write(image, "PNG", new File(baseName + "-msdf.png"));
                Gdx.files.local(baseName + "-msdf.fnt").writeString(sb.toString(), false);
                sb.setLength(0);
//            char cc;
//            for (int j = 0; j < chars.size; j++) {
//                cc = chars.get(j);
//                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
//            }
                Gdx.files.local(baseName + "-contents.txt").writeString(allChars, false);
            }
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create_msdf_family(String mode) {
        super.create();
        String[] fonts = {
/*
                "assets/GoMono-Regular.ttf",
                "assets/GoMono-Bold.ttf",
                "assets/GoMono-Italic.ttf",
                "assets/GoMono-BoldItalic.ttf",
                "GoMono-Family", //5.33f
*/
//                "assets/Iosevka_Full/Iosevka-Regular.ttf",
//                "assets/Iosevka_Full/Iosevka-Bold.ttf",
//                "assets/Iosevka_Full/Iosevka-Oblique.ttf",
//                "assets/Iosevka_Full/Iosevka-BoldOblique.ttf",
//                "Iosevka-Family", //2.66f


//                "assets/Iosevka_Full/Iosevka-Slab-Regular.ttf",
//                "assets/Iosevka_Full/Iosevka-Slab-Bold.ttf",
//                "assets/Iosevka_Full/Iosevka-Slab-Oblique.ttf",
//                "assets/Iosevka_Full/Iosevka-Slab-BoldOblique.ttf",                                                                                
//                "Iosevka-Slab-Family", //2.66f

                "assets/NotoSerif-Regular.ttf",
                "assets/NotoSerif-Bold.ttf",
                "assets/NotoSerif-Italic.ttf",
                "assets/NotoSerif-BoldItalic.ttf",
                "NotoSerif-Family" //  3.66f

        };
        float fontSize = 2f;
        int mainSize = 4096,
                blockWidth = 40, blockHeight = 40;
        String os = System.getProperty("os.name"), processed = "linux";
        if(os.contains("indows"))
            processed = "win64";
        else if(os.contains("ac"))
            processed = "darwin";
        
        // change command[2] to filename
        // change command[3] to the decimal codepoint printed as a string, such as "33"
        List<String> command = Arrays.asList(processed + "/msdfgen", mode, "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "0.75", "-translate", "7", "11", "-size", "20", "52", "-o", "temp.png");
        command.set(11, ""+blockWidth);
        command.set(12, ""+blockHeight);                 
        //List<String> command = Arrays.asList("msdfgen.exe", "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "3.2", "-translate", "3.5", "7.5", "-size", "44", "92", "-o", "temp.png");
        String filename, baseName;

        try {
            //int downscale = 3, mainSize = 2048, bigSize = mainSize << downscale;
            {
                FileHandle[] fontFiles = {
                        Gdx.files.local(fonts[0]),
                        Gdx.files.local(fonts[1]),
                        Gdx.files.local(fonts[2]),
                        Gdx.files.local(fonts[3]),
                };
                Font[] font = {
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[0].file()).deriveFont(24f),
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[1].file()).deriveFont(24f),
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[2].file()).deriveFont(24f),
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[3].file()).deriveFont(24f),
                };
                command.set(3, fonts[0]);
//                allChars = Gdx.files.local("assets/Iosevka-Slab-contents.txt").readString();
                allChars = Gdx.files.local("assets/NotoSerif-contents.txt").readString();
                int width = (mainSize - 4) / (blockWidth + 4), height = (mainSize - 4) / (blockHeight + 4), baseline = 54, idx = 0, c, limit = allChars.length() - 1;
                
                CharArray chars = new CharArray(1024), regularChars = new CharArray(1024);
                IntIntMap aliases = new IntIntMap(512),
                        widths = new IntIntMap(limit + 2 << 2),
                        usedXs = new IntIntMap(limit + 2),
                        usedYs = new IntIntMap(limit + 2);
                IntSet aliased = new IntSet(512);
                
                
                BufferedImage tImage = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D tGraphics = tImage.createGraphics();
                tGraphics.setFont(font[3]);
                FontRenderContext frc = tGraphics.getFontRenderContext();
                char[] tc = new char[]{'X'};
                GlyphVector gv2;

                
                for (int face = 0; face < 4; face++) {
                    chars.add((char) (face << 14));
                    if(face == 0)
                        regularChars.add((char) 0);
                    idx = 0;
                    c = allChars.charAt(idx);
                    while (c < 0x4000) {
                        c = allChars.charAt(idx++);
                        if (Character.isDefined(c)) {
                            switch (Character.getDirectionality(c)) {
                                case Character.DIRECTIONALITY_WHITESPACE:
                                    if (c != 32)
                                        break;
                                case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                                case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                                case Character.DIRECTIONALITY_UNDEFINED:
                                case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
                                    if (Character.isSurrogate((char) c))
                                        continue;                                     
                                    if (Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.BOX_DRAWING)) {
                                            if(0 != (face & -2)) // only true if italic, which is aliased to regular or bold
                                            {
                                                aliases.put((c | (face & 1) << 14), (c | face << 14));
                                                aliased.add((c | face << 14));
                                            }
                                        }
                                        char ac = (char) (c | face << 14);
                                        chars.add(ac);
                                        if(face == 0)
                                            regularChars.add((char)c);
                                        tc[0] = (char) (c);
                                        gv2 = font[face].createGlyphVector(frc, tc);
                                        if(c == 32)
                                            widths.put(ac, 7);
                                        else
                                            widths.put(ac, (int)Math.ceil(gv2.getVisualBounds().getWidth()));
                            }
                        }
                    }
                }
                int max = chars.size;
                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(fonts[4]).append("\" size=-").append(blockHeight)
                        .append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=0,0,0,0 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append(42) // very tricky to get right
                        .append(" base=").append(baseline)
                        .append(" scaleW=4096 scaleH=4096 pages=1 packed=0 alphaChnl=0 redChnl=1 greenChnl=2 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(fonts[4]).append("-msdf.png\"\n");
                sb.append("chars count=").append(max).append('\n');
                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_4BYTE_ABGR),
                        board;
                Graphics2D g = image.createGraphics();
                g.clearRect(0, 0, mainSize, mainSize);
                ProcessBuilder proc = new ProcessBuilder(command);
                
//                for (int y = 0; y < height && idx < max; y++) {
//                    for (int x = 0; x < width && idx < max; x++) {
//                        c = allChars.charAt(idx++);
//                        command.set(3, String.valueOf(c));
//                        proc.start().waitFor();
//                        board = ImageIO.read(new File("temp.png"));
//                        g.drawImage(board,
//                                2 + x * (4 + blockWidth), //bw+(2<<downscale)
//                                2 + y * (4 + blockHeight), //bh+(2<<downscale)
//                                null);
//                        sb.append("char id=").append(c)
//                                .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
//                                .append(" y=").append(2 + y * (4 + blockHeight))
//                                .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
//                                .append(" height=").append(blockHeight)
//                                .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth + 2)
//                                .append(" page=0 chnl=15\n");
//                    }
//                }
                
                int i = 0, face = -1;
                int shown;
                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        c = chars.get(i++);
                        shown = (c & 0x3fff);
                        if (shown == 0) {
                            ++face;
                            command.set(3, fonts[face]);
                            if(face == 0) {
                                g.setColor(Color.white);
                                g.fillRect(2, 2, blockWidth, blockHeight);
                            }
                            else 
                                x--;
                            usedXs.put(shown, 2);
                            usedYs.put(shown, 2);
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=").append(c)
                                    .append(" x=").append(2) //bw+(2<<downscale)
                                    .append(" y=").append(2) //bh+(2<<downscale)
                                    .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
                                    .append(" height=").append(blockHeight)
                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(blockWidth + 2)
                                    .append(" page=0 chnl=15\n");
                        } else if(!aliased.contains(c)) {
                            String st = String.valueOf(shown);
                            command.set(4, st);
                            proc.start().waitFor();
                            board = ImageIO.read(new File("temp.png"));

                            g.drawImage(board,
                                    2 + x * (4 + blockWidth), //bw+(2<<downscale)
                                    2 + y * (4 + blockHeight), //bh+(2<<downscale)
                                    null);
                            usedXs.put(c, 2 + x * (4 + blockWidth));
                            usedYs.put(c, 2 + y * (4 + blockHeight));
                            sb.append("char id=").append(c)
                                    .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
                                    .append(" y=").append(2 + y * (4 + blockHeight))
                                    .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
                                    .append(" height=").append(blockHeight)
                                    .append(" xoffset=0 yOffset=-1 xadvance=").append(widths.get(c, -4) + 2)
                                    .append(" page=0 chnl=15\n");
                            if(aliases.containsKey(c))
                            {
                                sb.append("char id=").append(aliases.get(c, c))
                                        .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
                                        .append(" y=").append(2 + y * (4 + blockHeight))
//                                        .append(" x=").append(usedXs.get(cs, 2)) //bw+(2<<downscale)
//                                        .append(" y=").append(usedYs.get(cs, 2))
                                        .append(" width=").append(blockWidth + 2) //bw+(2<<downscale)
                                        .append(" height=").append(blockHeight)
                                        .append(" xoffset=0 yOffset=-1 xadvance=").append(
                                                widths.get(aliases.get(c, c), -4) + 2)
                                        .append(" page=0 chnl=15\n");
                            }
                        }
                        else {
                            --x;
                        }
                    }
                }
//                if (idx < allChars.length())
//                    System.out.println("Too many chars?");
                ImageIO.write(image, "PNG", new File(fonts[4] + "-msdf.png"));
                Gdx.files.local(fonts[4] + "-msdf.fnt").writeString(sb.toString(), false);
                sb.setLength(0);
                Gdx.files.local(fonts[4] + "-contents-msdf.txt").writeString(String.valueOf(regularChars.toArray()), false);
                System.out.println("Done!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    
    /**
     * Note: this method doesn't handle spacing 100% correctly, and letters/punctuation won't line up.
     * If using this method, you must have installed node.js and npm, then run this command in Glamer's project root:
     * <br>
     * {@code npm install msdf-bmfont-xml}
     * <br>
     * This will make soimy's MSDF JSON BMFont generator available to Glamer to run, and then Glamer will convert the
     * JSON BMFonts to .fnt files that libGDX can read.
     */
    public void create_msdf_generator() {
        super.create();
        try {
            int mainSize = 2048,
                    blockWidth = 44, blockHeight = 92;
            // change command[2] to filename
            // change command[3] to the decimal codepoint printed as a string, such as "33"
            // change command[5] to "3.25" after processing DJV
            // change command[8] to "7.5" after processing DJV
            
            //List<String> command = Arrays.asList("msdfgen", "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "1.6", "-translate", "3.25", "15", "-size", "44", "92", "-o", "temp.png");
            //List<String> command = Arrays.asList("msdfgen.exe", "-font", "assets/DejaVuSansMono-Bold.ttf", "33", "-scale", "3.2", "-translate", "3.5", "7.5", "-size", "44", "92", "-o", "temp.png");
            List<String> command = Arrays.asList("node", "node_modules/msdf-bmfont-xml/cli.js", "-f","json", "-i","assets/Iosevka-Slab-contents.txt", "-m","2048,2048", "-s","56", "assets/Iosevka-Slab-Oblique.ttf");
            String filename, baseName;
            //for (int nm = 0; nm < filenames.length; nm++)
            {
                baseName = "Iosevka-Slab-Oblique";
//                filename = filenames[nm];
//                baseName = baseNames[nm];
//                if (nm == 4) {
//                    command.set(5, "3.2");
//                    command.set(7, "3.5");
//                    command.set(8, "7.5");
//                } else if (nm == 8) {
//                    command.set(5, "3.75");
//                    command.set(7, "3.25");
//                    command.set(8, "5");
//                }
//                command.set(2, filename);
                ProcessBuilder proc = new ProcessBuilder(command);
                Process p = proc.directory(new File(".")).start();
                p.waitFor();
                Gdx.files.local("assets/" + baseName + ".png").moveTo(Gdx.files.local(baseName + "-msdf.png"));
                Gdx.files.local("assets/" + baseName + ".json").moveTo(Gdx.files.local(baseName + ".json"));
                JsonReader jsonReader = new JsonReader();
                JsonValue json = jsonReader.parse(Gdx.files.local(baseName + ".json"));
                int width = 42, height = 21, baseline = 24;
                JsonValue info = json.get("info");

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(baseName).append("\" size=").append(info.getInt("size")).append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=6,6,6,6 spacing=0,0 outline=0\n");
                JsonValue common = json.get("common");
                sb.append("common lineHeight=").append(common.getInt("lineHeight")).append(" base=").append(common.getInt("base")).append(" scaleW=2048 scaleH=2048 pages=1 packed=0 alphaChnl=0 redChnl=1 greenChnl=2 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(baseName).append("-msdf.png\"\n");
                String[] allStrings = info.get("charset").asStringArray();
                sb.append("chars count=").append(allStrings.length).append('\n');
                JsonValue cs = json.get("chars");
                for(JsonValue jsv : cs) {
                    sb.append("char id=").append(jsv.getInt("id"))
                            .append(" x=").append(jsv.getInt("x")) //bw+(2<<downscale)
                            .append(" y=").append(jsv.getInt("y"))
                            .append(" width=").append(jsv.getInt("width")) //bw+(2<<downscale)
                            .append(" height=").append(jsv.getInt("height"))
                            .append(" xoffset=").append(jsv.getInt("xoffset"))
                            .append(" yOffset=").append(jsv.getInt("yoffset"))
                            .append(" xadvance=").append(jsv.getInt("xadvance"))
                            .append(" page=0 chnl=15\n");
                }
//                BufferedImage image = new BufferedImage(mainSize, mainSize, BufferedImage.TYPE_4BYTE_ABGR),
//                        board;
//                Graphics2D g = image.createGraphics();
//                g.clearRect(0, 0, mainSize, mainSize);
//                int i = 0, max = allChars.length();
//                int c;
////                ProcessBuilder proc = new ProcessBuilder(command);
//                for (int y = 0; y < height && i < max; y++) {
//                    for (int x = 0; x < width && i < max; x++) {
//                        c = allChars.charAt(i++);
//                        command.set(3, String.valueOf(c));
//                        proc.start().waitFor();
//                        board = ImageIO.read(new File("temp.png"));
//                        g.drawImage(board,
//                                2 + x * (4 + blockWidth), //bw+(2<<downscale)
//                                2 + y * (4 + blockHeight), //bh+(2<<downscale)
//                                null);
//                        //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
//                        sb.append("char id=").append(c)
//                                .append(" x=").append(2 + x * (4 + blockWidth)) //bw+(2<<downscale)
//                                .append(" y=").append(2 + y * (4 + blockHeight))
//                                .append(" width=").append(blockWidth - 4) //bw+(2<<downscale)
//                                .append(" height=").append(blockHeight)
//                                .append(" xoffset=12 yOffset=-1 xadvance=").append(24)
//                                .append(" page=0 chnl=15\n");
//                    }
//                }
//                if (i < allChars.length())
//                    System.out.println("Too many chars!");
//                ImageIO.write(image, "PNG", new File(baseName + "-msdf.png"));
                Gdx.files.local(baseName + "-msdf.fnt").writeString(sb.toString(), false);
                sb.setLength(0);
//            char cc;
//            for (int j = 0; j < chars.size; j++) {
//                cc = chars.get(j);
//                sb.append("index: ").append(String.format("%04X", (int)cc)).append(" glyph: ").append(cc).append(" \n");
//            }
                //Gdx.files.local(baseName + "-contents.txt").writeString(allChars, false);
            }
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createSeveral() {
        super.create();
        try {
            int downscale = 4, mainSize = 1024, bigSize = mainSize << downscale,
                    blockWidth = (64 - 14 << downscale), blockHeight = (128 - 14 << downscale);
            for (String filename : filenames) {
                FileHandle fontFile = Gdx.files.local(filename);
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
                LookupTable lookup = new LookupTable(0, 4) {
                    @Override
                    public int[] lookupPixel(int[] src, int[] dest) {
                        dest[0] = (255 - src[0]);
                        dest[1] = (255 - src[1]);
                        dest[2] = (255 - src[2]);
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
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createFamily() {
        super.create();
        String[] fonts = {
/*
                "assets/GoMono-Regular.ttf",
                "assets/GoMono-Bold.ttf",
                "assets/GoMono-Italic.ttf",
                "assets/GoMono-BoldItalic.ttf",
                "GoMono-Family", //5.33f
*/
                "assets/Iosevka_Full/Iosevka-Regular.ttf",
                "assets/Iosevka_Full/Iosevka-Bold.ttf",
                "assets/Iosevka_Full/Iosevka-Oblique.ttf",
                "assets/Iosevka_Full/Iosevka-BoldOblique.ttf",
                "Iosevka-Family", //2.66f


                "assets/Iosevka_Full/Iosevka-Slab-Regular.ttf",
                "assets/Iosevka_Full/Iosevka-Slab-Bold.ttf",
                "assets/Iosevka_Full/Iosevka-Slab-Oblique.ttf",
                "assets/Iosevka_Full/Iosevka-Slab-BoldOblique.ttf",
                "Iosevka-Slab-Family", //2.66f

        };
        float fontSize = 2.66f;

        /*
        mapping.put("assets/Iosevka_Full/Iosevka-Bold.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-BoldOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Oblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Regular.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-ExtraLight.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-ExtraLightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Heavy.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-HeavyOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Light.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-LightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Medium.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-MediumOblique.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Bold.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-BoldOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Oblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Regular.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-ExtraLight.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-ExtraLightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Heavy.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-HeavyOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Light.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-LightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Medium.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-MediumOblique.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Inconsolata-LGC-Square.ttf", 7.1f);
        mapping.put("assets/Inconsolata-LGC-Custom.ttf", 9.6f);
        mapping.put("assets/Iosevka.ttf", 5.5f);
        mapping.put("assets/Iosevka-Slab.ttf", 5.25f);
        mapping.put("assets/Iosevka-Light.ttf", 5.5f);
        mapping.put("assets/Iosevka-Slab-Thin.ttf", 5.4f);
        mapping.put("assets/Iosevka-Slab-Light.ttf", 5.4f);
        mapping.put("assets/Iosevka-Wide.ttf", 5.25f);
        mapping.put("assets/Iosevka-Wide-Slab.ttf", 5.2f);
        mapping.put("assets/Iosevka-Wide-Light.ttf", 5.25f);
        mapping.put("assets/Iosevka-Wide-Slab-Light.ttf", 5.25f);
        mapping.put("assets/SourceCodePro-Medium.otf", 6.65f);
        mapping.put("assets/DejaVuSansMono.ttf", 4.85f);
        */
        //mapping.put("assets/Galaxsea-Starlight-Mono-v3_1.ttf", 12f);
        // "assets/BoxedIn.ttf" // 12f

        try {
            int downscale = 3, mainSize = 2048, bigSize = mainSize << downscale;
            {
                BufferedImage image = new BufferedImage(bigSize, bigSize, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g = image.createGraphics();

                FileHandle[] fontFiles = {
                        Gdx.files.local(fonts[0]),
                        Gdx.files.local(fonts[1]),
                        Gdx.files.local(fonts[2]),
                        Gdx.files.local(fonts[3]),
                };
                Font[] font = {
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[0].file()).deriveFont(64f * fontSize),
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[1].file()).deriveFont(64f * fontSize),
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[2].file()).deriveFont(64f * fontSize),
                        Font.createFont(Font.TRUETYPE_FONT, fontFiles[3].file()).deriveFont(64f * fontSize),
                };
                BufferedImage tImage = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D tGraphics = tImage.createGraphics();
                tGraphics.setFont(font[0]);
                FontRenderContext frc = tGraphics.getFontRenderContext();
                char[] tc = new char[]{'\u253C'}, all = new char[0x4000];
                for (char i = 0; i <= 0x3fff; i++) {
                    all[i] = i;
                }
                int missing = font[0].getMissingGlyphCode();
                GlyphVector gv = font[0].createGlyphVector(frc, all), gv2 = font[0].createGlyphVector(frc, tc);
                Rectangle2D bounds = null, xBounds;
                boolean incomplete = true;
                CharArray chars = new CharArray(1024), regularChars = new CharArray(1024);
                if (gv2.getGlyphCode(0) != missing) { // cross shaped box drawing char, very large
                    gv2 = font[0].createGlyphVector(frc, tc);
                    bounds = gv2.getVisualBounds();
                    incomplete = false;
                }

                tc[0] = 'x';
                gv2 = font[0].createGlyphVector(frc, tc);
                if (gv2.getGlyphCode(0) == missing) {
                    tc[0] = 'X';
                    gv2 = font[0].createGlyphVector(frc, tc);
                    if (gv2.getGlyphCode(0) == missing)
                        throw new IllegalArgumentException("Font is missing both 'x' and 'X'; at least one is needed to judge metrics.");
                    xBounds = gv2.getVisualBounds();
                } else {
                    xBounds = gv2.getVisualBounds();
                }

                if (bounds == null)
                    bounds = xBounds.getBounds2D();
                else
                    bounds = bounds.createUnion(xBounds.getBounds2D());

                for (int face = 1; face < 4; face++) {
                    tGraphics.setFont(font[face]);
                    frc = tGraphics.getFontRenderContext();
                    tc[0] = '\u253C';
                    gv = font[face].createGlyphVector(frc, all);
                    gv2 = font[face].createGlyphVector(frc, tc);
                    if (gv2.getGlyphCode(0) != missing) {
                        gv2 = font[face].createGlyphVector(frc, tc);
                        bounds = bounds.createUnion(gv2.getVisualBounds());
                        incomplete = false;
                    }

                    tc[0] = 'x';
                    gv2 = font[0].createGlyphVector(frc, tc);
                    if (gv2.getGlyphCode(0) == missing) {
                        tc[0] = 'X';
                        gv2 = font[0].createGlyphVector(frc, tc);
                        if (gv2.getGlyphCode(0) == missing)
                            throw new IllegalArgumentException("Font is missing both 'x' and 'X'; at least one is needed to judge metrics.");
                        xBounds = xBounds.createUnion(gv2.getVisualBounds());
                    } else {
                        xBounds = xBounds.createUnion(gv2.getVisualBounds());
                    }
                }
                IntIntMap aliases = new IntIntMap(512);
                IntSet aliased = new IntSet(512);
                for (int face = 0; face < 4; face++) {
                    chars.add((char) (face << 14));
                    if(face == 0)
                        regularChars.add((char) 0);
                    for (int i = 32; i <= 0x3fff; i++) {
                        if (gv.getGlyphCode(i) != missing && Character.isDefined(i)) {
                            switch (Character.getDirectionality(i)) {
                                case Character.DIRECTIONALITY_WHITESPACE:
                                    if (i != 32)
                                        break;
                                case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                                case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                                case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                                case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                                case Character.DIRECTIONALITY_UNDEFINED:
                                case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
                                    if (Character.isSurrogate((char) i))
                                        continue;
                                    if (incomplete) {
                                        if (Character.UnicodeBlock.of(i).equals(Character.UnicodeBlock.BOX_DRAWING)) {
                                            tc[0] = (char) (i & 0x3fff);
                                            gv2 = font[face & 1].createGlyphVector(frc, tc); // we don't use italic box drawing chars
                                            if (gv2.getGlyphCode(0) != missing) {
                                                chars.add((char) (i | face << 14));
                                                if(face == 0)
                                                    regularChars.add((char)i);
                                                bounds.add(gv2.getVisualBounds());
                                                if(0 != (face & -2)) // only true if italic, which is aliased to regular or bold
                                                {
                                                    aliases.put((i | (face & 1) << 14), (i | face << 14));
                                                    aliased.add((i | face << 14));
                                                }
                                            }
                                        } else {
                                            tc[0] = (char) (i & 0x3fff);
                                            gv2 = font[face].createGlyphVector(frc, tc);
                                            if (gv2.getGlyphCode(0) != missing) {
                                                chars.add((char) (i | face << 14));
                                                if(face == 0)
                                                    regularChars.add((char)i);
                                                bounds.add(gv2.getVisualBounds());
                                            }
                                        }
                                    } else {
                                        if (Character.UnicodeBlock.of(i).equals(Character.UnicodeBlock.BOX_DRAWING)) {
                                            if(0 != (face & -2)) // only true if italic, which is aliased to regular or bold
                                            {
                                                aliases.put((i | (face & 1) << 14), (i | face << 14));
                                                aliased.add((i | face << 14));
                                            }
                                        }
                                        chars.add((char) (i | face << 14));
                                        if(face == 0)
                                            regularChars.add((char)i);

                                    }
                            }
                        }
                    }
                }
//            chars.clear();
//            chars.addAll('x', 'X', '┼', '.');
                int bw = (((2 << downscale) + (int) bounds.getWidth()) >> downscale) << downscale,
                        bh = (((2 << downscale) + (int) (bounds.getHeight())) >> downscale) << downscale,
                        width = bigSize / (bw + (2 << downscale)), height = bigSize / (bh + (2 << downscale)),
                        offTop = (int) (bounds.getMaxY() - xBounds.getMaxY()),
                        baseline = (int) (bounds.getHeight() - xBounds.getMinY() + bounds.getMinY() + offTop); // + (1 << downscale)

                System.out.println("bh: " + bh);
                System.out.println("offTop: " + offTop);
                System.out.println("baseline: " + baseline);

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(fonts[4]).append("\" size=-24 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=1,1,1,1 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append((bh >> downscale)).append(" base=").append(baseline >> downscale).append(" scaleW=1024 scaleH=1024 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(fonts[4]).append("-distance.png\"\n");
                sb.append("chars count=").append(chars.size).append('\n');
                BufferedImage board = new BufferedImage(bw, bh, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D gb = board.createGraphics();
                g.clearRect(0, 0, bigSize, bigSize);
                gb.clearRect(0, 0, bw, bh);
                gb.setColor(Color.white);
                gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                gb.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
                gb.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

                int i = 0, max = chars.size, face = -1;
                char c, shown;
                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        c = chars.get(i++);
                        shown = (char)(c & 0x3fff);
                        if (shown == 0) {
                            ++face;
                            gb.setFont(font[face]);
                            if(face == 0) {
                                gb.fillRect(0, 0, bw, bh);
                                g.drawImage(board,
                                        x * (bw + (2 << downscale)), //bw+(2<<downscale)
                                        y * (bh + (2 << downscale)), //bh+(2<<downscale)
                                        null);
                            }
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=").append((int)c)
                                    .append(" x=").append(0) //bw+(2<<downscale)
                                    .append(" y=").append(0) //bh+(2<<downscale)
                                    .append(" width=").append((bw >> downscale)) //bw+(2<<downscale)
                                    .append(" height=").append((bh >> downscale))    //bh+(2<<downscale)
                                    .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw) >> downscale))
                                    .append(" page=0 chnl=15\n");
                        } else if(!aliased.contains(c)) {
                            gb.clearRect(0, 0, bw, bh);
                            gb.drawString(String.valueOf(shown), 1 << downscale, baseline); // + (1 << downscale)
                            // gb.drawString(String.valueOf(c), 0, baseline); // + (1 << downscale)
                            g.drawImage(board,
                                    x * (bw + (2 << downscale)), //bw+(2<<downscale)
                                    y * (bh + (2 << downscale)), //bh+(2<<downscale)
                                    null);
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=").append((int)c)
                                    .append(" x=").append((x * (bw + (2 << downscale)) >> downscale)) //bw+(2<<downscale)
                                    .append(" y=").append(y * (bh + (2 << downscale)) >> downscale) //bh+(2<<downscale)
                                    .append(" width=").append((bw >> downscale)) //bw+(2<<downscale)
                                    .append(" height=").append((bh >> downscale))    //bh+(2<<downscale)
                                    .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw) >> downscale))
                                    .append(" page=0 chnl=15\n");
                            if(aliases.containsKey(c))
                            {
                                sb.append("char id=").append(aliases.get(c, c))
                                        .append(" x=").append((x * (bw + (2 << downscale)) >> downscale)) //bw+(2<<downscale)
                                        .append(" y=").append(y * (bh + (2 << downscale)) >> downscale) //bh+(2<<downscale)
                                        .append(" width=").append((bw >> downscale)) //bw+(2<<downscale)
                                        .append(" height=").append((bh >> downscale))    //bh+(2<<downscale)
                                        .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw) >> downscale))
                                        .append(" page=0 chnl=15\n");
                            }
                        }
                        else {
                            --x;
                        }
                    }
                }
                if (i < chars.size)
                    System.out.println("Too many chars!");
                System.out.println("Showed " + i + " chars out of " + chars.size);

                //ImageIO.write(image, "PNG", new File(fontFile.nameWithoutExtension() + ".png"));
                DistanceFieldGenerator dfg = new DistanceFieldGenerator();
                dfg.setDownscale(1 << downscale);
                dfg.setSpread((float) Math.pow(2, downscale) * 3.5f * MathUtils.log(5f, fontSize));

                //use this instead for BoxedIn
                //dfg.setSpread((float)Math.pow(2, downscale) * 3.5f * MathUtils.log(5f, 4f)); // MathUtils.log(5f, fontSize));
                ImageIO.write(dfg.generateDistanceField(image), "PNG", new File(fonts[4] + "-distance.png"));
                Gdx.files.local(fonts[4] + "-distance.fnt").writeString(sb.toString(), false);
                Gdx.files.local(fonts[4] + "-contents.txt").writeString(String.valueOf(regularChars.toArray()), false);
                System.out.println("Done!");
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

    }

    public void createNormal() {
        super.create();
        ObjectFloatMap<String> mapping = new ObjectFloatMap<>(16);
        mapping.put("assets/awesome-solid.ttf", 2f);
        //mapping.put("assets/square.ttf", 2.55f);
        /*
        mapping.put("assets/NotoSerif-Bold.ttf", 2.55f);
        mapping.put("assets/NotoSerif-BoldItalic.ttf", 2.55f);
        mapping.put("assets/NotoSerif-Italic.ttf", 2.55f);
        mapping.put("assets/NotoSerif-Regular.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-Bold.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-BoldOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Oblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Regular.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-ExtraLight.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-ExtraLightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Heavy.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-HeavyOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Light.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-LightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Medium.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-MediumOblique.ttf", 2.55f);
        */
        /*
        mapping.put("assets/SourceHanCodeJP-Regular.otf", 1.42f);
        mapping.put("assets/SourceHanCodeJP-Bold.otf", 1.42f);
        mapping.put("assets/SourceHanCodeJP-RegularIt.otf", 1.42f);
        mapping.put("assets/SourceHanCodeJP-BoldIt.otf", 1.42f);
        */
        //mapping.put("assets/GoMono-Regular.ttf", 5.33f);
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Bold.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-BoldOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Oblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Regular.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-ExtraLight.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-ExtraLightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Heavy.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-HeavyOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Light.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-LightOblique.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-Medium.ttf", 2.55f);
        mapping.put("assets/Iosevka_Full/Iosevka-Slab-MediumOblique.ttf", 2.55f);
        */
        /*
        mapping.put("assets/Inconsolata-LGC-Square.ttf", 7.1f);
        mapping.put("assets/Inconsolata-LGC-Custom.ttf", 9.6f);
        mapping.put("assets/Iosevka.ttf", 5.5f);
        mapping.put("assets/Iosevka-Slab.ttf", 5.25f);
        mapping.put("assets/Iosevka-Light.ttf", 5.5f);
        mapping.put("assets/Iosevka-Slab-Thin.ttf", 5.4f);
        mapping.put("assets/Iosevka-Slab-Light.ttf", 5.4f);
        mapping.put("assets/Iosevka-Wide.ttf", 5.25f);
        mapping.put("assets/Iosevka-Wide-Slab.ttf", 5.2f);
        mapping.put("assets/Iosevka-Wide-Light.ttf", 5.25f);
        mapping.put("assets/Iosevka-Wide-Slab-Light.ttf", 5.25f);
        mapping.put("assets/SourceCodePro-Medium.otf", 6.65f);
        mapping.put("assets/DejaVuSansMono.ttf", 4.85f);
        */
        //mapping.put("assets/Galaxsea-Starlight-Mono-v3_1.ttf", 12f);
        // "assets/BoxedIn.ttf" // 12f

        try {
            int downscale = 2, mainSize = 4096, bigSize = mainSize << downscale;
            for (ObjectFloatMap.Entry<String> entry : mapping) {
                float fontSize = entry.value;

                FileHandle fontFile = Gdx.files.local(params[3]);
                        // "assets/BoxedIn.ttf" // 12f
                        // "assets/Iosevka-Slab.ttf" // 5.4f
                        // "assets/Iosevka-Wide-Slab-Light.ttf" 5.25f
                        // "assets/Iosevka.ttf" 5.5f
                        // "assets/Iosevka-Light.ttf" 5.5f
                        // "assets/Iosevka-Slab-Light.ttf" 5.4f
                        // "assets/Iosevka-Slab-Thin.ttf" 5.4f
                        // "assets/Iosevka-Wide.ttf" 5.25f
                        // "assets/Iosevka-Wide-Slab.ttf" 5.25f (too high?)
                        // "assets/Iosevka-Wide-Light.ttf" 5.25f
                        // "assets/Iosevka-Wide-Slab-Light.ttf" 5.25f
                        // "assets/Galaxsea-Starlight-Mono-v3_1.ttf" 12f
                        // "assets/SourceCodePro-Medium.otf" // fontSize 6.5f
                        // "assets/DejaVuSansMono.ttf" // fontSize 4.75f
                        // "assets/Galaxsea-Starlight-Mono-v3_1.ttf" // fontSize 12f
                //);
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile.file()).deriveFont(Float.parseFloat(params[4]));
                BufferedImage tImage = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D tGraphics = tImage.createGraphics();
                tGraphics.setFont(font);
                FontRenderContext frc = tGraphics.getFontRenderContext();
                char[] tc = new char[]{'\u253C'}, all = new char[0x10000];
                for (int i = 0; i <= 0xffff; i++) {
                    all[i] = (char)i;
                }
                int missing = font.getMissingGlyphCode();
                GlyphVector gv = font.createGlyphVector(frc, all), gv2 = font.createGlyphVector(frc, tc);
                Rectangle2D bounds = null, xBounds;
                boolean incomplete = true;
                CharArray chars = new CharArray(1024);
                if (gv2.getGlyphCode(0) != missing) {
                    gv2 = font.createGlyphVector(frc, tc);
                    bounds = gv2.getVisualBounds();
                    //incomplete = false;
                }

                tc[0] = 'x';
                gv2 = font.createGlyphVector(frc, tc);
                if (gv2.getGlyphCode(0) == missing) {
                    tc[0] = 'X';
                    gv2 = font.createGlyphVector(frc, tc);
                    if (gv2.getGlyphCode(0) == missing)
                    {
                        tc[0] = '\uf549';
                        gv2 = font.createGlyphVector(frc, tc);
                        if(gv2.getGlyphCode(0) == missing) 
                            throw new IllegalArgumentException("Font is missing both 'x' and 'X'; at least one is needed to judge metrics.");
                    }
                    xBounds = gv2.getVisualBounds();
                } else {
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
                if (bounds == null)
                    bounds = xBounds.getBounds2D();
                for (int i = 32; i <= 0xffff; i++) {
                    if (gv.getGlyphCode(i) != missing && Character.isDefined(i)) {
                        switch (Character.getDirectionality(i)) {
                            case Character.DIRECTIONALITY_WHITESPACE:
                                if (i != 32)
                                    break;
                            case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
                                if(i == '〱' || i == '〲') break;
                            case Character.DIRECTIONALITY_EUROPEAN_NUMBER:
                            case Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
                            case Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
                            case Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
                            case Character.DIRECTIONALITY_OTHER_NEUTRALS:
                            case Character.DIRECTIONALITY_UNDEFINED:
                            case Character.DIRECTIONALITY_SEGMENT_SEPARATOR:
                                if (Character.isSurrogate((char) i))
                                    continue;
                                if (incomplete) {
                                    tc[0] = (char) i;
                                    gv2 = font.createGlyphVector(frc, tc);
                                    if (gv2.getGlyphCode(0) != missing) {
                                        chars.add((char) i);
                                        bounds.add(gv2.getVisualBounds());
                                    }
                                } else {
                                    chars.add((char) i);
                                }
                        }
                    }
                }
//            chars.clear();
//            chars.addAll('x', 'X', '┼', '.');
                int bw = (((2 << downscale) + (int) bounds.getWidth()) >> downscale) << downscale,
                        bh = (((2 << downscale) + (int) (bounds.getHeight())) >> downscale) << downscale,
                        width = bigSize / (bw + (2 << downscale)), height = bigSize / (bh + (2 << downscale)),
                        offTop = (int) (bounds.getMaxY() - xBounds.getMaxY()),
                        baseline = (int) (bounds.getHeight() - xBounds.getMinY() + bounds.getMinY() + (1 << downscale) +
                                ("assets/SourceCodePro-Medium.otf".equals(entry.key) ? 0 : offTop)); // + offTop //some fonts need this

//                int bw = (((int) bounds.getWidth()) >> downscale) << downscale,
//                        bh = (((int) (bounds.getHeight())) >> downscale) << downscale,
//                        width = bigSize / (bw + (4 << downscale)), height = bigSize / (bh + (4 << downscale)),
//                        offTop = (int) (bounds.getMinY() - xBounds.getMinY()),
//                        baseline = (int) (bh);// + xBounds.getMaxY() - bounds.getMaxY()); // + offTop //some fonts need this
                // (int)(bh + xBounds.getMinY() + ((1<<downscale)-0.001))

                System.out.println("bh: " + bh);
                System.out.println("offTop: " + offTop);
                System.out.println("baseline: " + baseline);

                StringBuilder sb = new StringBuilder(0x10000);
                sb.append("info face=\"").append(fontFile.nameWithoutExtension()).append("\" size=-24 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1 padding=1,1,1,1 spacing=0,0 outline=0\n");
                sb.append("common lineHeight=").append((bh >> downscale)).append(" base=").append(baseline >> downscale).append(" scaleW=1024 scaleH=1024 pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n");
                sb.append("page id=0 file=\"").append(fontFile.nameWithoutExtension()).append("-distance.png\"\n");
                sb.append("chars count=").append(chars.size + 1).append('\n');
                BufferedImage image = new BufferedImage(bigSize, bigSize, BufferedImage.TYPE_4BYTE_ABGR),
                        board = new BufferedImage(bw, bh, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g = image.createGraphics(), gb = board.createGraphics();
                g.clearRect(0, 0, bigSize, bigSize);
                gb.clearRect(0, 0, bw, bh);
                gb.setColor(Color.white);
                gb.setFont(font);
                gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                gb.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
                gb.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

                int i = 0, max = chars.size;
                char c;
                for (int y = 0; y < height && i < max; y++) {
                    for (int x = 0; x < width && i < max; x++) {
                        if (x == 0 && y == 0) {
                            gb.fillRect(0, 0, bw, bh);
                            g.drawImage(board,
                                    x * (bw + (2 << downscale)), //bw+(2<<downscale)
                                    y * (bh + (2 << downscale)), //bh+(2<<downscale)
                                    null);
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=0")
                                    .append(" x=").append((x * (bw + (2 << downscale)) >> downscale)) //bw+(2<<downscale)
                                    .append(" y=").append(y * (bh + (2 << downscale)) >> downscale) //bh+(2<<downscale)
                                    .append(" width=").append((bw >> downscale)) //bw+(2<<downscale)
                                    .append(" height=").append((bh >> downscale))    //bh+(2<<downscale)
                                    .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw) >> downscale))
                                    .append(" page=0 chnl=15\n");
                        } else {
                            c = chars.get(i++);
                            gb.clearRect(0, 0, bw, bh);
                            gb.drawString(String.valueOf(c), 1 << downscale, baseline); // + (1 << downscale)
                            // gb.drawString(String.valueOf(c), 0, baseline); // + (1 << downscale)
                            g.drawImage(board,
                                    x * (bw + (2 << downscale)), //bw+(2<<downscale)
                                    y * (bh + (2 << downscale)), //bh+(2<<downscale)
                                    null);
                            //gb.drawString(String.valueOf(c), x * bw + 8, (y+1) * bh + 8);
                            sb.append("char id=").append((int) c)
                                    .append(" x=").append((x * (bw + (2 << downscale)) >> downscale)) //bw+(2<<downscale)
                                    .append(" y=").append(y * (bh + (2 << downscale)) >> downscale) //bh+(2<<downscale)
                                    .append(" width=").append((bw >> downscale)) //bw+(2<<downscale)
                                    .append(" height=").append((bh >> downscale))    //bh+(2<<downscale)
                                    .append(" xoffset=-1 yOffset=-1 xadvance=").append(((bw) >> downscale))
                                    .append(" page=0 chnl=15\n");
                        }
                    }
                }
                if (i < chars.size)
                    System.out.println("Too many chars!");
                System.out.println("Showed " + i + " chars out of " + chars.size);
                //ImageIO.write(image, "PNG", new File(fontFile.nameWithoutExtension() + ".png"));
                DistanceFieldGenerator dfg = new DistanceFieldGenerator();
                dfg.setDownscale(1 << downscale);
                dfg.setSpread((float) Math.pow(2, downscale) * 8f * MathUtils.log(5f, fontSize));
                //dfg.setSpread((float) Math.pow(2, downscale) * 3.5f * MathUtils.log(5f, fontSize));

                //use this instead for BoxedIn
                //dfg.setSpread((float)Math.pow(2, downscale) * 3.5f * MathUtils.log(5f, 4f)); // MathUtils.log(5f, fontSize));
                BufferedImage dfgi = dfg.generateDistanceField(image);
                ImageIO.write(dfgi, "PNG", new File(fontFile.nameWithoutExtension() + "-distance.png"));
                LookupTable lookup = new LookupTable(0, 4) {
                    @Override
                    public int[] lookupPixel(int[] src, int[] dest) {
                        dest[0] = (255 - src[0]);
                        dest[1] = (255 - src[1]);
                        dest[2] = (255 - src[2]);
                        dest[3] = Math.max(0, Math.min(255, (int) (Math.pow(src[3] * 0x1p-8, 3.0) * 750 - 10)));
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
                System.out.println("Done!");
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

    }

}
