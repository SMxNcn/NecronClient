package cn.boop.necron.gui;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen;
import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.CropNuker;
import cn.boop.necron.module.impl.EtherwarpRouter;
import cn.boop.necron.module.impl.Waypoint;
import cn.boop.necron.utils.RenderUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuiWaypointList extends OneUIScreen {
    private final List<String> waypointFiles = new ArrayList<>();
    private static final int MAX_VISIBLE_ITEMS = 8;
    private String currentLoadedFile = null;
    private int currentPage = 0;

    public GuiWaypointList() {
        super(true, true);
        initGui();
    }

    @Override
    public void initScreen(int width, int height) {
        super.initScreen(width, height);
        loadWaypointFiles();

        List<Waypoint> currentWaypoints = Waypoint.getWaypoints();
        if (!currentWaypoints.isEmpty() && Waypoint.getCurrentFile() != null) {
            String currentFilePath = Waypoint.getCurrentFile();
            File currentFile = new File(currentFilePath);
            currentLoadedFile = currentFile.getName();
        }
    }

    @Override
    public void draw(long vg, float partialTicks, InputHandler inputHandler) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        int screenWidth = Objects.requireNonNull(getCurrentScreen()).width;
        int screenHeight = Objects.requireNonNull(getCurrentScreen()).height;
        int uiWidth = 400;
        int uiHeight = 250;

        float x = (screenWidth - uiWidth) / 2f;
        float y = (screenHeight - uiHeight) / 2f;

        nanoVGHelper.drawRoundedRect(vg, x, y, uiWidth, uiHeight, new Color(20, 20, 20, 100).getRGB(), 7.6f);
        nanoVGHelper.drawHollowRoundRect(
                vg,
                x - 1,
                y - 1,
                uiWidth + 1f,
                uiHeight + 0.5f,
                RenderUtils.getChromaColor(new Color(217, 39, 236), new Color(0, 159, 255), 0, 2, 5).getRGB(),
                8,
                0.6f
        );
        nanoVGHelper.drawText(vg, "Waypoint List", x + 30, y + 20, -1, 16, Fonts.REGULAR);

        drawWaypointFileList(vg, inputHandler, x, y, uiWidth, uiHeight);
        nanoVGHelper.drawText(vg, "Necron Client v" + Necron.VERSION, x + 4, y + uiHeight - 8, new Color(175, 175, 175, 255).getRGB(), 8, Fonts.REGULAR);
    }

    private void drawWaypointFileList(long vg, InputHandler inputHandler, float containerX, float containerY, int width, int height) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float listStartY = containerY + 40;
        float itemHeight = 20;
        int totalPages = (int) Math.ceil((double) waypointFiles.size() / MAX_VISIBLE_ITEMS);

        int startIndex = currentPage * MAX_VISIBLE_ITEMS;
        int endIndex = Math.min(startIndex + MAX_VISIBLE_ITEMS, waypointFiles.size());
        int visibleItems = Math.min(MAX_VISIBLE_ITEMS, endIndex - startIndex);

        for (int i = 0; i < visibleItems; i++) {
            int index = startIndex + i;
            if (index >= waypointFiles.size()) break;

            String fileName = waypointFiles.get(index);
            float itemY = listStartY + i * (itemHeight + 1);
            boolean isItemHovered = inputHandler.isAreaHovered(containerX + 20, itemY, width - 40, itemHeight);
            int bgColor = isItemHovered ? new Color(40, 40, 40, 150).getRGB() : new Color(40, 40, 40, 100).getRGB();

            nanoVGHelper.drawRoundedRect(vg, containerX + 20, itemY, width - 40, itemHeight, bgColor, 4);

            float textY = itemY + (itemHeight / 2) + 1;
            nanoVGHelper.drawText(vg, fileName, containerX + 25, textY, -1, 10, Fonts.REGULAR);

            boolean isCurrentLoaded = fileName.equals(currentLoadedFile);

            if (isCurrentLoaded) {
                float buttonY = itemY + (itemHeight - 12) / 2;

                float reloadButtonX = containerX + width - 60;
                boolean isReloadHovered = inputHandler.isAreaHovered(reloadButtonX, itemY + 5, 12, 12);
                nanoVGHelper.setAlpha(vg, isReloadHovered ? 1.0f : 0.6f);
                nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/Update.svg"), reloadButtonX, buttonY, 12, 12, -1, 10f);

                if (isReloadHovered && inputHandler.isClicked()) {
                    loadWaypoint(fileName, true);
                }

                float editButtonX = containerX + width - 40;
                boolean isEditHovered = inputHandler.isAreaHovered(editButtonX, itemY + 5, 12, 12);
                nanoVGHelper.setAlpha(vg, isEditHovered ? 1.0f : 0.6f);
                nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/Edit.svg"), editButtonX, buttonY, 12, 12, -1, 10f);

                if (isEditHovered && inputHandler.isClicked()) {
                    GuiUtils.displayScreen(new GuiWaypointSettings(fileName));
                }

                nanoVGHelper.setAlpha(vg, 1.0f);
            } else if (isItemHovered) {
                float buttonY = itemY + (itemHeight - 12) / 2;
                float loadButtonX = containerX + width - 40;
                boolean isLoadHovered = inputHandler.isAreaHovered(loadButtonX, itemY + 5, 12, 12);
                nanoVGHelper.setAlpha(vg, isLoadHovered ? 1.0f : 0.6f);
                nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ArrowUp.svg"), loadButtonX, buttonY, 12, 12, -1, 10f);

                if (isLoadHovered && inputHandler.isClicked()) {
                    loadWaypoint(fileName, false);
                }

                nanoVGHelper.setAlpha(vg, 1.0f);
            }
        }

        if (totalPages > 1) {
            drawPaginationControls(vg, inputHandler, containerX, containerY, width, height, totalPages);
        }
    }

    private void drawPaginationControls(long vg, InputHandler inputHandler, float containerX, float containerY, int width, int height, int totalPages) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float controlsY = containerY + height - 30;
        float centerX = containerX + width / 2f;

        if (currentPage > 0) {
            float prevButtonX = centerX - 35;
            boolean isPrevHovered = inputHandler.isAreaHovered(prevButtonX, controlsY, 15, 15);
            nanoVGHelper.setAlpha(vg, isPrevHovered ? 1.0f : 0.7f);
            nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ChevronLeft.svg"), prevButtonX, controlsY + 1, 15, 15, -1, 10f);
            nanoVGHelper.setAlpha(vg, 1.0f);

            if (isPrevHovered && inputHandler.isClicked()) {
                currentPage--;
            }
        }

        String pageText = (currentPage + 1) + "/" + totalPages;
        nanoVGHelper.drawCenteredText(vg, pageText, centerX + 1, controlsY + 10, -1, 12, Fonts.REGULAR);

        if (currentPage < totalPages - 1) {
            float nextButtonX = centerX + 20;
            boolean isNextHovered = inputHandler.isAreaHovered(nextButtonX, controlsY, 15, 15);
            nanoVGHelper.setAlpha(vg, isNextHovered ? 1.0f : 0.7f);
            nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ChevronRight.svg"), nextButtonX, controlsY + 1, 15, 15, -1, 10f);
            nanoVGHelper.setAlpha(vg, 1.0f);

            if (isNextHovered && inputHandler.isClicked()) {
                currentPage++;
            }
        }
    }

    private void loadWaypoint(String fileName, boolean reload) {
        String cleanFileName = fileName.replace(".json", "");
        Waypoint.loadWaypoints(cleanFileName, reload);
        EtherwarpRouter.loadWaypoints(cleanFileName);
        CropNuker.setIndex(0);
        currentLoadedFile = fileName;
    }

    private void loadWaypointFiles() {
        waypointFiles.clear();
        File waypointsDir = new File(Necron.WP_FILE_DIR);
        if (waypointsDir.exists() && waypointsDir.isDirectory()) {
            File[] files = waypointsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    waypointFiles.add(file.getName());
                }
            }
        }
        waypointFiles.sort(String::compareTo);
    }

    @Override
    public void onScreenClose() {
    }
}
