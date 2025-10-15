package cn.boop.necron.config;

import cn.boop.necron.Necron;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FontManager {
    private static final String FONT_DOWNLOAD_URL = "https://gitee.com/mixturedg/necron-client-repo/raw/master/fonts/";
    public static final String FONT_DIR = "config/necron/fonts/";
    public static final String BOLD_FONT = "Bold.ttf";
    public static final String SEMIBOLD_FONT = "SemiBold.ttf";
    public static final String MEDIUM_FONT = "Medium.ttf";
    public static final String REGULAR_FONT = "Regular.ttf";

    public static void initFonts() {
        File fontDir = new File(FONT_DIR);
        if (!fontDir.exists()) {
            if (fontDir.mkdirs()) {
                ensureFontExists(BOLD_FONT);
                ensureFontExists(SEMIBOLD_FONT);
                ensureFontExists(MEDIUM_FONT);
                ensureFontExists(REGULAR_FONT);
            }
        }
    }

    private static void ensureFontExists(String fontName) {
        File fontFile = new File(FONT_DIR, fontName);
        if (!fontFile.exists()) {
            Necron.LOGGER.info("Font {} not found, downloading...", fontName);
            downloadFont(fontName);
        }
    }

    private static void downloadFont(String fontName) {
        new Thread(() -> {
            try {
                URL website = new URL(FONT_DOWNLOAD_URL + fontName);
                try (ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                     FileOutputStream fos = new FileOutputStream(FONT_DIR + File.separator + fontName)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
                Necron.LOGGER.info("Successfully downloaded font: {}", fontName);
            } catch (Exception e) {
                Necron.LOGGER.error("Failed to download font: {}", fontName, e);
            }
        }, "Download").start();
    }

    public static String getFontPath(String fontName) {
        if (fontName.endsWith(".otf")) {
            fontName = fontName.replace(".otf", ".ttf");
        }

        File fontFile = new File(FONT_DIR, fontName);
        if (fontFile.exists()) {
            return fontFile.getAbsolutePath();
        }
        return null;
    }
}
