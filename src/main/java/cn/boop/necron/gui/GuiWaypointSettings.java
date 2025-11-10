package cn.boop.necron.gui;

import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen;
import cn.boop.necron.Necron;
import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.NotificationType;
import cn.boop.necron.module.impl.Waypoint;
import cn.boop.necron.utils.JsonUtils;
import cn.boop.necron.utils.LocationUtils;
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
    private String nameInput = "";
    private String islandInput = "";
    private boolean editingX = false;
    private boolean editingY = false;
    private boolean editingZ = false;
    private boolean editingRotation = false;
    private boolean editingName = false;
    private boolean editingIsland = false;

    private boolean directionDropdownOpen = false;
    private boolean typeDropdownOpen = false;
    private final String[] directions = {"forward", "back", "left", "right"};
    private final String[] directionLabels = {"Forward", "Back", "Left", "Right"};
    private final String[] typeLabels = {"Router", "Normal", "Farming"};

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
            nameInput = currentWaypoint.getName() != null ? currentWaypoint.getName() : "";
        }
        LocationUtils.Island requiredIsland = Waypoint.getRequiredIsland();
        islandInput = requiredIsland != null ? requiredIsland.name() : "";
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
        int uiWidth = 300;
        int uiHeight = 250;

        float x = (screenWidth - uiWidth) / 2f;
        float y = (screenHeight - uiHeight) / 2f;

        nanoVGHelper.drawRoundedRect(vg, x, y, uiWidth, uiHeight, new Color(20, 20, 20, 100).getRGB(), 7.6f);
        nanoVGHelper.drawHollowRoundRect(
                vg,
                x - 1f,
                y - 1f,
                uiWidth + 1f,
                uiHeight + 0.5f,
                RenderUtils.getChromaColor(new Color(217, 39, 236), new Color(0, 159, 255), 0, 2, 5).getRGB(),
                8,
                0.6f
        );
        nanoVGHelper.drawText(vg, "Waypoint Settings", x + 30, y + 20, -1, 16, Fonts.REGULAR);

        float contentStartY = y + 70;
        float contentX = x + 20;

        drawTypeSpecificProperties(vg, inputHandler, x, y);
        drawBackButton(vg, inputHandler, x, y);
        drawWaypointNavigation(vg, inputHandler, x, y);
        drawRotationInput(vg, inputHandler, contentX + 110, contentStartY + 50);
        drawCoordinateInput(vg, inputHandler, contentX, contentStartY);
        drawDirectionSelection(vg, inputHandler, contentX, contentStartY + 50);
        drawTypeSelection(vg, inputHandler, x, y, uiWidth);

        handleDataInput();
        nanoVGHelper.drawText(vg, "Necron Client v" + Necron.VERSION, x + 4, y + uiHeight - 8, new Color(175, 175, 175, 255).getRGB(), 8, Fonts.REGULAR);
        nanoVGHelper.drawText(vg, fileName, x + uiWidth - 4 - nanoVGHelper.getTextWidth(vg, fileName, 8, Fonts.REGULAR), y + uiHeight - 8, new Color(175, 175, 175, 255).getRGB(), 8, Fonts.REGULAR);
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
        float prevX = containerX + 20;
        float arrowSize = 12f;
        float spacing = 5f;

        String waypointText = "Waypoint " + (currentWaypointIndex + 1) + "/" + totalWaypoints;

        boolean prevHovered = inputHandler.isAreaHovered(prevX, navY, arrowSize, arrowSize);
        nanoVGHelper.setAlpha(vg, prevHovered ? 1.0f : 0.7f);
        nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ChevronLeft.svg"), prevX, navY + 2, arrowSize, arrowSize, -1, 10f);
        nanoVGHelper.setAlpha(vg, 1.0f);

        if (prevHovered && inputHandler.isClicked()) {
            if (currentWaypointIndex > 0) {
                currentWaypointIndex--;
            } else {
                currentWaypointIndex = totalWaypoints - 1;
            }
            loadWaypointData();
            directionDropdownOpen = false;
        }

        float textX = prevX + arrowSize + spacing;
        nanoVGHelper.drawText(vg, waypointText, textX, navY + 9, -1, 10, Fonts.REGULAR);

        float textWidth = nanoVGHelper.getTextWidth(vg, waypointText, 10, Fonts.REGULAR);
        float nextX = textX + textWidth + spacing;
        boolean nextHovered = inputHandler.isAreaHovered(nextX, navY, arrowSize, arrowSize);
        nanoVGHelper.setAlpha(vg, nextHovered ? 1.0f : 0.7f);
        nanoVGHelper.drawSvg(vg, new SVG("/assets/oneconfig/old-icons/ChevronRight.svg"), nextX, navY + 2, arrowSize, arrowSize, -1, 10f);
        nanoVGHelper.setAlpha(vg, 1.0f);

        if (nextHovered && inputHandler.isClicked()) {
            if (currentWaypointIndex < totalWaypoints - 1) {
                currentWaypointIndex++;
            } else {
                currentWaypointIndex = 0;
            }
            loadWaypointData();
            directionDropdownOpen = false;
        }
    }

    private void drawTypeSelection(long vg, InputHandler inputHandler, float containerX, float containerY, int uiWidth) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 20f;
        float dropdownWidth = 50f;
        float arrowSize = 10f;

        float labelX = containerX + uiWidth - dropdownWidth - 60;
        float labelY = containerY + 10 + inputHeight / 2 + 1;
        nanoVGHelper.drawText(vg, "Type:", labelX, labelY, -1, 10, Fonts.REGULAR);

        float dropdownX = containerX + uiWidth - dropdownWidth - 30;
        float dropdownY = containerY + 10;

        String currentTypeLabel = Waypoint.getCurrentType().name();

        boolean isMainHovered = inputHandler.isAreaHovered(dropdownX, dropdownY, dropdownWidth, inputHeight);
        int mainBgColor = isMainHovered ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, dropdownX, dropdownY, dropdownWidth, inputHeight, mainBgColor, 3);
        nanoVGHelper.drawCenteredText(vg, currentTypeLabel, dropdownX + dropdownWidth / 2, dropdownY + inputHeight / 2 + 1, -1, 10, Fonts.REGULAR);

        float arrowX = dropdownX + dropdownWidth + 2;
        float arrowY = dropdownY + (inputHeight - arrowSize) / 2;
        boolean isArrowHovered = inputHandler.isAreaHovered(arrowX, arrowY, arrowSize, arrowSize);
        nanoVGHelper.setAlpha(vg, isArrowHovered ? 1.0f : 0.7f);
        nanoVGHelper.drawSvg(vg, typeDropdownOpen ? SVGs.CHEVRON_UP : SVGs.CHEVRON_DOWN,
                arrowX, arrowY, arrowSize, arrowSize, -1, 10f);
        nanoVGHelper.setAlpha(vg, 1.0f);

        if ((isMainHovered || isArrowHovered) && inputHandler.isClicked()) {
            typeDropdownOpen = !typeDropdownOpen;
        }

        if (typeDropdownOpen) {
            float dropdownHeight = typeLabels.length * 20f;

            nanoVGHelper.drawRoundedRect(vg, dropdownX, dropdownY + inputHeight + 2, dropdownWidth, dropdownHeight, new Color(30, 30, 30, 220).getRGB(), 3);

            for (int i = 0; i < typeLabels.length; i++) {
                float optionY = dropdownY + inputHeight + 2 + i * 20f;
                boolean isOptionHovered = inputHandler.isAreaHovered(dropdownX, optionY, dropdownWidth, 20f);
                boolean isSelected = Waypoint.getCurrentType().name().equals(typeLabels[i]);

                int optionBgColor;
                if (isSelected) {
                    optionBgColor = new Color(58, 136, 239, 153).getRGB();
                } else if (isOptionHovered) {
                    optionBgColor = new Color(60, 60, 60, 200).getRGB();
                } else {
                    optionBgColor = new Color(40, 40, 40, 150).getRGB();
                }

                nanoVGHelper.drawRoundedRect(vg, dropdownX, optionY, dropdownWidth, 20f, optionBgColor, 3);
                nanoVGHelper.drawCenteredText(vg, typeLabels[i], dropdownX + dropdownWidth / 2, optionY + 10f, -1, 9, Fonts.REGULAR);

                if (isOptionHovered && inputHandler.isClicked()) {
                    Waypoint.TYPE selectedType = Waypoint.TYPE.valueOf(typeLabels[i]);
                    Waypoint.setCurrentType(selectedType);
                    typeDropdownOpen = false;
                    triggerAutoSave();
                }
            }
        }
    }

    private void drawCoordinateInput(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 25f;
        float inputWidth = 50f;
        float spacing = 15f;

        nanoVGHelper.drawText(vg, "X:", x, y, -1, 10, Fonts.REGULAR); // 标签在输入框上方
        drawSingleCoordInput(vg, inputHandler, x, y + 6, inputWidth, inputHeight - 4, xInput, editingX, () -> {
            editingX = true;
            editingY = editingZ = editingRotation = editingIsland = editingName = false;
        });

        nanoVGHelper.drawText(vg, "Y:", x + inputWidth + spacing, y, -1, 10, Fonts.REGULAR); // 标签在输入框上方
        drawSingleCoordInput(vg, inputHandler, x + inputWidth + spacing, y + 6, inputWidth, inputHeight - 4, yInput, editingY, () -> {
            editingY = true;
            editingX = editingZ = editingRotation = editingIsland = editingName = false;
        });

        nanoVGHelper.drawText(vg, "Z:", x + 2 * (inputWidth + spacing), y, -1, 10, Fonts.REGULAR); // 标签在输入框上方
        drawSingleCoordInput(vg, inputHandler, x + 2 * (inputWidth + spacing), y + 6, inputWidth, inputHeight - 4, zInput, editingZ, () -> {
            editingZ = true;
            editingX = editingY = editingRotation = editingIsland = editingName = false;
        });
    }

    private void drawDirectionSelection(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 25f;
        float dropdownWidth = 70f;
        float arrowSize = 12f;

        nanoVGHelper.drawText(vg, "Direction:", x, y, -1, 10, Fonts.REGULAR);

        String currentDirectionLabel = getDirectionLabel(currentWaypoint.getDirection());

        boolean isMainHovered = inputHandler.isAreaHovered(x, y + 6, dropdownWidth, inputHeight - 4);
        int mainBgColor = isMainHovered ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, x, y + 6, dropdownWidth, inputHeight - 4, mainBgColor, 3);
        nanoVGHelper.drawCenteredText(vg, currentDirectionLabel, x + dropdownWidth / 2, y + 6 + (inputHeight - 4) / 2 + 1, -1, 10, Fonts.REGULAR);

        float arrowX = x + dropdownWidth + 2;
        float arrowY = y + 6 + (inputHeight - 4 - arrowSize) / 2;
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

            nanoVGHelper.drawRoundedRect(vg, x, dropdownY, dropdownWidth, dropdownHeight, new Color(30, 30, 30, 220).getRGB(), 3);

            for (int i = 0; i < directions.length; i++) {
                float optionY = dropdownY + i * 20f;
                boolean isOptionHovered = inputHandler.isAreaHovered(x, optionY, dropdownWidth, 20f);
                boolean isSelected = currentWaypoint.getDirection().equals(directions[i]);

                int optionBgColor;
                if (isSelected) {
                    optionBgColor = new Color(58, 136, 239, 153).getRGB();
                } else if (isOptionHovered) {
                    optionBgColor = new Color(60, 60, 60, 200).getRGB();
                } else {
                    optionBgColor = new Color(40, 40, 40, 150).getRGB();
                }

                nanoVGHelper.drawRoundedRect(vg, x, optionY, dropdownWidth, 20f, optionBgColor, 3);
                nanoVGHelper.drawCenteredText(vg, directionLabels[i], x + dropdownWidth / 2, optionY + 10f, -1, 9, Fonts.REGULAR);

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

        float inputHeight = 21f;
        float inputWidth = 70f;

        nanoVGHelper.drawText(vg, "Rotation:", x, y, -1, 10, Fonts.REGULAR);

        drawSingleCoordInput(vg, inputHandler, x, y + 6, inputWidth, inputHeight, rotationInput, editingRotation, () -> {
            editingRotation = true;
            editingX = editingY = editingZ = editingIsland = editingName =false;
        });
    }

    private void drawTypeSpecificProperties(long vg, InputHandler inputHandler, float containerX, float containerY) {
        Waypoint.TYPE currentType = Waypoint.getCurrentType();

        float startY = containerY + 170;

        if (currentType == Waypoint.TYPE.Normal) {
            drawIslandInput(vg, inputHandler, containerX + 20, startY);
            drawNameInput(vg, inputHandler, containerX + 130, startY);
        }
    }

    private void drawIslandInput(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 21f;
        float inputWidth = 100f;

        nanoVGHelper.drawText(vg, "Island:", x, y, -1, 10, Fonts.REGULAR);

        if (editingIsland) {
            nanoVGHelper.drawHollowRoundRect(vg, x - 0.8f, y + 6 - 0.8f, inputWidth + 1.3f, inputHeight + 1.3f,
                    new Color(0, 159, 255).getRGB(), 3.2f, 0.25f);
        }

        int bgColor = editingIsland ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, x, y + 6, inputWidth, inputHeight, bgColor, 3);

        String displayText = islandInput.isEmpty() ? "Island" : islandInput;
        nanoVGHelper.drawCenteredText(vg, displayText, x + inputWidth / 2, y + 6 + inputHeight / 2 + 1.5f, -1, 10, Fonts.REGULAR);

        if (inputHandler.isAreaHovered(x, y + 6, inputWidth, inputHeight) && inputHandler.isClicked()) {
            editingIsland = true;
            editingX = editingY = editingZ = editingRotation = editingName = false;
        }
    }

    private void drawSingleCoordInput(long vg, InputHandler inputHandler, float x, float y, float width, float height,
                                      String value, boolean editing, Runnable onClick) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        if (editing) {
            nanoVGHelper.drawHollowRoundRect(vg,x - 0.8f, y - 0.8f, width + 1.3f, height + 1.3f,
                    new Color(0, 159, 255).getRGB(), 3.2f, 0.25f);
        }

        int bgColor = editing ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, x, y, width, height, bgColor, 3);

        String displayText = value.isEmpty() ? "0" : value;
        nanoVGHelper.drawCenteredText(vg, displayText, x + width / 2, y + height / 2 + 1, -1, 10, Fonts.REGULAR);

        if (inputHandler.isAreaHovered(x, y, width, height) && inputHandler.isClicked()) {
            onClick.run();
        }
    }

    private void drawNameInput(long vg, InputHandler inputHandler, float x, float y) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

        float inputHeight = 21f;
        float inputWidth = 100f;

        nanoVGHelper.drawText(vg, "Name:", x, y, -1, 10, Fonts.REGULAR);

        if (editingName) {
            nanoVGHelper.drawHollowRoundRect(vg, x - 0.8f, y + 6 - 0.8f, inputWidth + 1.3f, inputHeight + 1.3f,
                    new Color(0, 159, 255).getRGB(), 3.2f, 0.25f);
        }

        int bgColor = editingName ? new Color(60, 60, 60, 200).getRGB() : new Color(50, 50, 50, 150).getRGB();
        nanoVGHelper.drawRoundedRect(vg, x, y + 6, inputWidth, inputHeight, bgColor, 3);

        String displayText = nameInput.isEmpty() ? "Name" : nameInput;
        nanoVGHelper.drawCenteredText(vg, displayText, x + inputWidth / 2, y + 6 + inputHeight / 2 + 1.5f, -1, 10, Fonts.REGULAR);

        if (inputHandler.isAreaHovered(x, y + 6, inputWidth, inputHeight) && inputHandler.isClicked()) {
            editingName = true;
            editingX = editingY = editingZ = editingRotation = editingIsland = false;
        }
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

    private void handleDataInput() {
        if (!(editingX || editingY || editingZ || editingRotation || editingName || editingIsland)) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyPressTime < 150) {
            return;
        }

        if (Keyboard.isCreated() && Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
            char keyChar = Keyboard.getEventCharacter();
            int keyCode = Keyboard.getEventKey();

            if (editingIsland || editingName) {
                if (keyCode == Keyboard.KEY_BACK) {
                    if (editingIsland && !islandInput.isEmpty()) {
                        islandInput = islandInput.substring(0, islandInput.length() - 1);
                        lastKeyPressTime = currentTime;
                    } else if (editingName && !nameInput.isEmpty()) {
                        nameInput = nameInput.substring(0, nameInput.length() - 1);
                        lastKeyPressTime = currentTime;
                    }
                } else if (keyCode == Keyboard.KEY_RETURN) {
                    if (editingIsland) {
                        editingIsland = false;
                        try {
                            if (islandInput.isEmpty()) {
                                Waypoint.setRequiredIsland(null);
                            } else {
                                LocationUtils.Island island = LocationUtils.Island.valueOf(islandInput);
                                Waypoint.setRequiredIsland(island);
                            }
                        } catch (IllegalArgumentException e) {
                            ClientNotification.sendNotification("Invalid Input", "Please Check your input.", NotificationType.WARN, 4000);
                            Waypoint.setRequiredIsland(null);
                        }
                    } else {
                        editingName = false;
                    }
                    triggerAutoSave();
                } else if (keyChar != 0) {
                    if (editingIsland) {
                        islandInput += keyChar;
                        lastKeyPressTime = currentTime;
                    } else {
                        nameInput += keyChar;
                        lastKeyPressTime = currentTime;
                    }
                }
            }
            // 处理数字输入（坐标和旋转）
            else if (editingX || editingY || editingZ || editingRotation) {
                if (keyChar >= '0' && keyChar <= '9') {
                    appendToCurrentInput(String.valueOf(keyChar));
                    lastKeyPressTime = currentTime;
                } else if (keyCode == Keyboard.KEY_BACK) {
                    String current = getCurrentInput();
                    if (!current.isEmpty()) {
                        setCurrentInput(current.substring(0, current.length() - 1));
                        lastKeyPressTime = currentTime;
                    }
                } else if (keyCode == Keyboard.KEY_MINUS && (editingX || editingY || editingZ || editingRotation || editingName)) {
                    String current = getCurrentInput();
                    if (current.isEmpty() || current.charAt(0) != '-') {
                        setCurrentInput("-" + current);
                    } else {
                        setCurrentInput(current.substring(1));
                    }
                    lastKeyPressTime = currentTime;
                } else if ((keyCode == Keyboard.KEY_PERIOD || keyChar == '.') && editingRotation) {
                    String current = getCurrentInput();
                    if (!current.contains(".")) {
                        setCurrentInput(current + ".");
                    }
                    lastKeyPressTime = currentTime;
                } else if (keyCode == Keyboard.KEY_RETURN) {
                    // 重置所有数字编辑状态
                    editingX = editingY = editingZ = editingRotation = false;
                    triggerAutoSave();
                }
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
            currentWaypoint.setName(nameInput.isEmpty() ? null : nameInput);

            JsonUtils.saveWaypoints(Waypoint.getWaypoints(), Necron.WP_FILE_DIR + fileName);

        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void onScreenClose() {
        triggerAutoSave();
    }
}
