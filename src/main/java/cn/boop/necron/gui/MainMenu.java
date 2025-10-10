package cn.boop.necron.gui;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.hud.ModuleList;
import cn.boop.necron.utils.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cn.boop.necron.config.impl.NecronOptionsImpl.*;

public final class MainMenu extends GuiScreen {
    private static final List<ResourceLocation> BACKGROUND_TEXTURES = new ArrayList<>();
    private static final ResourceLocation MOD_ICON =
            new ResourceLocation("necron", "gui/icon.png");
    private static int currentBackgroundIndex = 0;
    private static int nextBackgroundIndex = 1;
    private static long lastSwitchTime = System.currentTimeMillis();
    private static float fadeProgress = 0.0f;
    private static boolean isFading = false;

    private float mouseXOffset, mouseYOffset;
    private static final float MAX_OFFSET = 0.05f;
    private static final float PARALLAX_FACTOR = 0.5f;
    private static final float SMOOTH_FACTOR = 0.15f;

    public MainMenu() {
        if (Necron.mc != null && Necron.mc.getTextureManager() != null) {
            loadBackgroundTextures();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateBackgroundTransition();

        float centerX = this.width / 2.0f;
        float centerY = this.height / 2.0f;
        float targetXOffset = (mouseX - centerX) / centerX * MAX_OFFSET;
        float targetYOffset = (mouseY - centerY) / centerY * MAX_OFFSET;
        mouseXOffset += (targetXOffset - mouseXOffset) * SMOOTH_FACTOR;
        mouseYOffset += (targetYOffset - mouseYOffset) * SMOOTH_FACTOR;

        this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURES.get(currentBackgroundIndex));
        drawBackgroundQuad();

        if (isFading) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.color(1.0F, 1.0F, 1.0F, fadeProgress);
            this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURES.get(nextBackgroundIndex));
            drawBackgroundQuad();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        drawBackgroundRect();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(MOD_ICON);
        int xPos = (this.width - 64) / 2;
        int yPos = (this.height / 2 ) - 76;
        GlStateManager.scale(0.5f, 0.5f, 1.0f);
        drawModalRectWithCustomSizedTexture(
                xPos * 2, yPos * 2,
                0, 0,
                128, 128,
                128, 128
        );
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
        Color textChColor = ModuleList.getChromaColor(
                new Color(142, 221, 255),
                new Color(166, 166, 166),
                1,
                4
        );
        String s1 = "Minecraft 1.8.9";
        String s2 = "Necron " + Necron.VERSION;
        String s3 = "Cheaters get banned!";
        this.mc.fontRendererObj.drawStringWithShadow(s1, 2, this.height - 10, 0xFFFFFF);
        //this.mc.fontRendererObj.drawStringWithShadow(s2, 2, this.height - 20, 0x8EDDFF);
        this.mc.fontRendererObj.drawStringWithShadow(s2, 2, this.height - 20, textChColor.getRGB());
        this.mc.fontRendererObj.drawStringWithShadow(s3, this.width - fontRendererObj.getStringWidth(s3) - 2, this.height - 10, 0xFFFFFF);
    }

    @Override
    public void initGui() {
        this.buttonList.add(new ClientButton(0, this.width / 2 - 90, this.height / 2 + 1, 180, 18, "Singleplayer"));
        this.buttonList.add(new ClientButton(1, this.width / 2 - 90, this.height / 2 + 23, 180, 18, "Multiplayer"));
        this.buttonList.add(new ClientButton(2, this.width / 2 - 90, this.height / 2 + 45, 180, 18, "Mods"));
        this.buttonList.add(new ClientButton(3, this.width / 2 - 90, this.height / 2 + 67, 88, 18, "Options"));
        this.buttonList.add(new ClientButton(4, this.width / 2 + 2, this.height / 2 + 67, 88, 18, "Quit"));
        this.buttonList.add(new ClientButton(5, this.width - 55, 5, 50, 18, "Vanilla"));
        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: {
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            }
            case 1: {
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            }
            case 2: {
                this.mc.displayGuiScreen(new GuiModList(this));
                break;
            }
            case 3: {
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            }
            case 4: {
                this.mc.shutdown();
                break;
            }
            case 5: {
                customMainMenu = false;
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && this.mc.currentScreen instanceof MainMenu) return;
        super.keyTyped(typedChar, keyCode);
    }

    private void drawBackgroundQuad() {
        GlStateManager.pushMatrix();
        float parallaxX = mouseXOffset * width * PARALLAX_FACTOR;
        float parallaxY = mouseYOffset * height * PARALLAX_FACTOR;
        GlStateManager.translate(parallaxX, parallaxY, 0);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer worldrenderer = tess.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        float scale = 1.1f;
        float scaledWidth = width * scale;
        float scaledHeight = height * scale;
        float offsetX = (width - scaledWidth) / 2;
        float offsetY = (height - scaledHeight) / 2;

        worldrenderer.pos(offsetX, scaledHeight + offsetY, 0).tex(0, 1).endVertex();
        worldrenderer.pos(scaledWidth + offsetX, scaledHeight + offsetY, 0).tex(1, 1).endVertex();
        worldrenderer.pos(scaledWidth + offsetX, offsetY, 0).tex(1, 0).endVertex();
        worldrenderer.pos(offsetX, offsetY, 0).tex(0, 0).endVertex();

        tess.draw();
        GlStateManager.popMatrix();
    }

    private void drawBackgroundRect() {
        int rectWidth = 190;
        int rectHeight = 180;

        int x = (width - rectWidth) / 2;
        int y = (height - rectHeight) / 2;

        RenderUtils.drawRoundedRect(x, y, x + rectWidth, y + rectHeight, 8.0f, new Color(0xFAEAEAE, true).getRGB());
        RenderUtils.drawBorderedRoundedRect(x, y, rectWidth, rectHeight, 8.0f, 2.0f, new Color(0x73969696, true).getRGB());
    }

    private void updateBackgroundTransition() {
        long currentTime = System.currentTimeMillis();

        if (!isFading && currentTime - lastSwitchTime >= (switchInterval * 1000L)) {
            isFading = true;
            nextBackgroundIndex = (currentBackgroundIndex + 1) % BACKGROUND_TEXTURES.size();
        }

        if (isFading) {
            fadeProgress = Math.min(1.0f, (float)(currentTime - lastSwitchTime - (switchInterval * 1000L)) / fadeDuration);

            if (fadeProgress >= 1.0f) {
                currentBackgroundIndex = nextBackgroundIndex;
                isFading = false;
                lastSwitchTime = currentTime;
            }
        }
    }

    private void loadBackgroundTextures() {
        if (!BACKGROUND_TEXTURES.isEmpty()) return;

        File bgDir = new File(Necron.BG_FILE_DIR);
        if (!bgDir.exists() && bgDir.mkdirs()) {
            Necron.LOGGER.info("Created backgrounds directory: {}", bgDir.getAbsolutePath());
        }

        File[] bgFiles = bgDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png"));

        if (bgFiles != null) {
            for (File file : bgFiles) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        DynamicTexture dynamicTexture =
                                new DynamicTexture(image);

                        ResourceLocation textureLocation = Necron.mc.getTextureManager().getDynamicTextureLocation(
                                        "bg" + file.getName(), dynamicTexture);

                        BACKGROUND_TEXTURES.add(textureLocation);
                        Necron.LOGGER.info("Loaded external background: {}", file.getName());
                    }
                } catch (Exception e) {
                    Necron.LOGGER.error("Failed to load background image: {}", file.getName(), e);
                }
            }
        }

        if (BACKGROUND_TEXTURES.isEmpty()) {
            Necron.LOGGER.warn("No external backgrounds found, using default backgrounds");
            BACKGROUND_TEXTURES.add(new ResourceLocation("necron", "gui/bg.png"));
        }

        Necron.LOGGER.info("Loaded {} background textures", BACKGROUND_TEXTURES.size());
    }
}
