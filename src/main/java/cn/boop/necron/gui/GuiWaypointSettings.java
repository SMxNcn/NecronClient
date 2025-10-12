package cn.boop.necron.gui;

import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen;
import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.Waypoint;
import cn.boop.necron.utils.JsonUtils;
import cn.boop.necron.utils.RenderUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class GuiWaypointSettings extends OneUIScreen {
    private Waypoint currentWaypoint;
    private int currentWaypointIndex = 0;
    private long lastKeyPressTime = 0;
    private final String fileName;

    private String xInput = "";
    private String yInput = "";
    private String zInput = "";
    private String rotationInput = "";
    private boolean editingX = false;
    private boolean editingY = false;
    private boolean editingZ = false;
    private boolean editingRotation = false;

    private boolean directionDropdownOpen = false;
    private final String[] directions = {"forward", "back", "left", "right"};
    private final String[] directionLabels = {"Forward", "Back", "Left", "Right"};

    public GuiWaypointSettings(String wpFileName) {
        super(true, true);
        this.fileName = wpFileName;
        initGui();
        loadWaypointData();
    }

    private void loadWaypointData() {
        List<Waypoint> waypoints = Waypoint.getWaypoints();
        if (!waypoints.isEmpty() && currentWaypointIndex < waypoints.size()) {
            currentWaypoint = waypoints.get(currentWaypointIndex);
            xInput = String.valueOf(currentWaypoint.getX());
            yInput = String.valueOf(currentWaypoint.getY());
            zInput = String.valueOf(currentWaypoint.getZ());
            rotationInput = String.format("%.1f", currentWaypoint.getRotation());
        }
    }

    @Override
    public void initScreen(int width, int height) {
        super.initScreen(width, height);
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
        nanoVGHelper.drawText(vg, "Waypoint Settings - " + fileName, x + 30, y + 20, -1, 16, Fonts.REGULAR);

        drawWaypointNavigation(vg, inputHandler, x, y);
        drawBackButton(vg, inputHandler, x, y);

        float contentStartY = y + 70;
        float contentX = x + 20;

        drawCoordinateInput(vg, inputHandler, contentX, contentStartY);
        drawRotationInput(vg, inputHandler, contentX, contentStartY + 130);
        drawDirectionSelection(vg, inputHandler, contentX, contentStartY + 95);
        handleNumberInput();
        nanoVGHelper.drawText(vg, "Necron Client v" + Necron.VERSION, x + 4, y + uiHeight - 8, new Color(175, 175, 175, 255).getRGB(), 8, Fonts.REGULAR);
    }

    private void drawBackButton(long vg, InputHandler inputHandler, float containerX, float containerY) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        float buttonX = containerX + 10;
        float buttonY = containerY + 11;
        float buttonSize = 15;

        boolean isHovered = inputHandler.isAreaHovered(buttonX, buttonY, buttonSize, buttonSize);
        nanoVGHelper.setAlpha(vg, isHovered ? 1.0f : 0.7f);
        nanoVGHelper.drawSvg(vg, SVGs.ARROW_LEFT, buttonX, buttonY, buttonSize, buttonSize, -1, 10f);
        nanoVGHelper.setAlpha(vg, 1.0f);

        if (isHovered && inputHandler.isClicked()) {
            GuiUtils.displayScreen(new GuiWaypointList());
        }
    }

    private void drawWaypointNavigation(long vg, InputHandler inputHandler, float containerX, float containerY) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        List<Waypoint> waypoints = Waypoint.getWaypoints();
        int totalWaypoints = waypoints.size();

        if (totalWaypoints <= 1) return;

        float navY = containerY + 40;
        float navX = containerX + 40;

        String waypointText = "Waypoint " + (currentWaypointIndex + 1) + "/" + totalWaypoints;
        nanoVGHelper.drawText(vg, waypointText, navX, navY + 8, -1, 10, Fonts.REGULAR);

        if (currentWaypointIndex > 0) {
            float prevX = navX - 15;
            boolean prevHovered = inputHandler.isAreaHovered(prevX, navY, 12, 12);
            nanoVGHelper.setAlpha(vg, prevHovered ? 1.0f : 0.7f);
            nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ChevronLeft.svg"), prevX, navY, 12, 12, -1, 10f);
            nanoVGHelper.setAlpha(vg, 1.0f);

            if (prevHovered && inputHandler.isClicked()) {
                currentWaypointIndex--;
                loadWaypointData();
                directionDropdownOpen = false;
            }
        }

        if (currentWaypointIndex < totalWaypoints - 1) {
            float nextX = navX + 65;
            boolean nextHovered = inputHandler.isAreaHovered(nextX, navY, 12, 12);
            nanoVGHelper.setAlpha(vg, nextHovered ? 1.0f : 0.7f);
            nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ChevronRight.svg"), nextX, navY, 12, 12, -1, 10f);
            nanoVGHelper.setAlpha(vg, 1.0f);

            if (nextHovered && inputHandler.isClicked()) {
                currentWaypointIndex++;
                loadWaypointData();
                directionDropdownOpen = false;
            }
        }
    }

    private void drawCoordinateInput(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 25f;
        float inputWidth = 140f;
        float spacing = 5f;

        nanoVGHelper.drawText(vg, "X:", x + 8, y + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);
        drawSingleCoordInput(vg, inputHandler, x + 30, y + 2, inputWidth - 80, inputHeight - 4, xInput, editingX, () -> {
            editingX = true;
            editingY = editingZ = editingRotation = false;
        });
        nanoVGHelper.drawText(vg, "请输入文本1", x + inputWidth + 15, y + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);

        nanoVGHelper.drawText(vg, "Y:", x + 8, y + inputHeight + spacing + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);
        drawSingleCoordInput(vg, inputHandler, x + 30, y + inputHeight + spacing + 2, inputWidth - 80, inputHeight - 4, yInput, editingY, () -> {
            editingY = true;
            editingX = editingZ = editingRotation = false;
        });
        nanoVGHelper.drawText(vg, "请输入文本2", x + inputWidth + 15, y + inputHeight + spacing + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);

        nanoVGHelper.drawText(vg, "Z:", x + 8, y + 2 * (inputHeight + spacing) + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);
        drawSingleCoordInput(vg, inputHandler, x + 30, y + 2 * (inputHeight + spacing) + 2, inputWidth - 80, inputHeight - 4, zInput, editingZ, () -> {
            editingZ = true;
            editingX = editingY = editingRotation = false;
        });
        nanoVGHelper.drawText(vg, "请输入文本3", x + inputWidth + 15, y + 2 * (inputHeight + spacing) + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);
    }

    private void drawSingleCoordInput(long vg, InputHandler inputHandler, float x, float y, float width, float height,
                                      String value, boolean editing, Runnable onClick) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        if (editing) {
            RenderUtils.drawBorderedRoundedRect(x - 0.5f, y - 0.5f, width + 1f, height + 1f, 3.5f, 1f,
                    RenderUtils.getChromaColor(new Color(217, 39, 236), new Color(0, 159, 255), 0, 1, 5).getRGB());
        }

        int bgColor = editing ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, x, y, width, height, bgColor, 3);

        String displayText = value.isEmpty() ? "0" : value;
        nanoVGHelper.drawCenteredText(vg, displayText, x + width / 2, y + height / 2 + 1, -1, 10, Fonts.REGULAR);

        if (inputHandler.isAreaHovered(x, y, width, height) && inputHandler.isClicked()) {
            onClick.run();
        }
    }

    private void drawDirectionSelection(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 25f;
        float dropdownWidth = 70f;
        float arrowSize = 12f;

        nanoVGHelper.drawText(vg, "Direction:", x + 8, y + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);

        float dropdownX = x + 60;
        String currentDirectionLabel = getDirectionLabel(currentWaypoint.getDirection());

        boolean isMainHovered = inputHandler.isAreaHovered(dropdownX, y + 2, dropdownWidth, inputHeight - 4);
        int mainBgColor = isMainHovered ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, dropdownX, y + 2, dropdownWidth, inputHeight - 4, mainBgColor, 3);
        nanoVGHelper.drawCenteredText(vg, currentDirectionLabel, dropdownX + dropdownWidth / 2, y + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);

        float arrowX = dropdownX + dropdownWidth + 2;
        float arrowY = y + (inputHeight - arrowSize) / 2;
        boolean isArrowHovered = inputHandler.isAreaHovered(arrowX, arrowY, arrowSize, arrowSize);
        nanoVGHelper.setAlpha(vg, isArrowHovered ? 1.0f : 0.7f);
        nanoVGHelper.drawSvg(vg, directionDropdownOpen ? SVGs.CHEVRON_UP : SVGs.CHEVRON_DOWN,
                arrowX, arrowY, arrowSize, arrowSize, -1, 10f);
        nanoVGHelper.setAlpha(vg, 1.0f);

        if ((isMainHovered || isArrowHovered) && inputHandler.isClicked()) {
            directionDropdownOpen = !directionDropdownOpen;
        }

        if (directionDropdownOpen) {
            float dropdownHeight = directions.length * 20f;
            float dropdownY = y + inputHeight + 2;

            nanoVGHelper.drawRoundedRect(vg, dropdownX, dropdownY, dropdownWidth, dropdownHeight, new Color(30, 30, 30, 220).getRGB(), 3);

            for (int i = 0; i < directions.length; i++) {
                float optionY = dropdownY + i * 20f;
                boolean isOptionHovered = inputHandler.isAreaHovered(dropdownX, optionY, dropdownWidth, 20f);
                boolean isSelected = currentWaypoint.getDirection().equals(directions[i]);

                int optionBgColor = isOptionHovered ? new Color(60, 60, 60, 200).getRGB() :
                        (isSelected ? new Color(58, 136, 239, 153).getRGB() : new Color(40, 40, 40, 150).getRGB());

                nanoVGHelper.drawRoundedRect(vg, dropdownX, optionY, dropdownWidth, 20f, optionBgColor, 3);
                nanoVGHelper.drawCenteredText(vg, directionLabels[i], dropdownX + dropdownWidth / 2, optionY + 10f, -1, 9, Fonts.REGULAR);

                if (isOptionHovered && inputHandler.isClicked()) {
                    currentWaypoint.setDirection(directions[i]);
                    directionDropdownOpen = false;
                    triggerAutoSave();
                }
            }
        }
    }

    private void drawRotationInput(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 25f;
        float inputWidth = 140f;

        nanoVGHelper.drawText(vg, "Rotation:", x + 8, y + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);

        drawSingleCoordInput(vg, inputHandler, x + 60, y + 2, inputWidth - 70, inputHeight - 4, rotationInput, editingRotation, () -> {
            editingRotation = true;
            editingX = editingY = editingZ = false;
        });
    }

    private void triggerAutoSave() {
        saveChanges();
    }

    private String getDirectionLabel(String direction) {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i].equals(direction)) {
                return directionLabels[i];
            }
        }
        return directionLabels[0];
    }

    private void handleNumberInput() {
        if (!(editingX || editingY || editingZ || editingRotation)) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyPressTime < 200) {
            return;
        }

        if (Keyboard.isCreated() && Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
            char keyChar = Keyboard.getEventCharacter();
            int keyCode = Keyboard.getEventKey();

            if (keyChar >= '0' && keyChar <= '9') {
                appendToCurrentInput(String.valueOf(keyChar));
                lastKeyPressTime = currentTime;
            }
            else if (keyCode == Keyboard.KEY_BACK) {
                String current = getCurrentInput();
                if (!current.isEmpty()) {
                    setCurrentInput(current.substring(0, current.length() - 1));
                    lastKeyPressTime = currentTime;
                }
            }
            else if (keyCode == Keyboard.KEY_MINUS && (editingX || editingY || editingZ)) {
                String current = getCurrentInput();
                if (current.isEmpty() || current.charAt(0) != '-') {
                    setCurrentInput("-" + current);
                } else {
                    setCurrentInput(current.substring(1));
                }
                lastKeyPressTime = currentTime;
            }
            else if ((keyCode == Keyboard.KEY_PERIOD || keyChar == '.') && editingRotation) {
                String current = getCurrentInput();
                if (!current.contains(".")) {
                    setCurrentInput(current + ".");
                }
                lastKeyPressTime = currentTime;
            }
            else if (keyCode == Keyboard.KEY_RETURN) {
                editingX = editingY = editingZ = editingRotation = false;
                triggerAutoSave();
            }
        }
    }

    private void appendToCurrentInput(String text) {
        String current = getCurrentInput();
        if (current.length() < 8) {
            setCurrentInput(current + text);
        }
    }

    private void setCurrentInput(String value) {
        if (editingX) xInput = value;
        else if (editingY) yInput = value;
        else if (editingZ) zInput = value;
        else if (editingRotation) rotationInput = value;
    }

    private String getCurrentInput() {
        if (editingX) return xInput;
        if (editingY) return yInput;
        if (editingZ) return zInput;
        if (editingRotation) return rotationInput;
        return "";
    }

    private void saveChanges() {
        if (currentWaypoint == null) return;

        try {
            if (!xInput.isEmpty()) {
                int newX = Integer.parseInt(xInput);
                currentWaypoint.setX(newX);
            }
            if (!yInput.isEmpty()) {
                int newY = Integer.parseInt(yInput);
                currentWaypoint.setY(newY);
            }
            if (!zInput.isEmpty()) {
                int newZ = Integer.parseInt(zInput);
                currentWaypoint.setZ(newZ);
            }

            float newRotation = rotationInput.isEmpty() ? 0.0f : Float.parseFloat(rotationInput);
            currentWaypoint.setRotation(newRotation);

            JsonUtils.saveWaypoints(Waypoint.getWaypoints(), Necron.WP_FILE_DIR + fileName);

        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void onScreenClose() {
    }
}
