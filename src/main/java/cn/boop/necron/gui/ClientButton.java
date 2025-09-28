package cn.boop.necron.gui;

import cn.boop.necron.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class ClientButton extends GuiButton {
    private float hoverAlpha = 0f;
    private static final int CORNER_RADIUS = 4;
    private static final float HOVER_IN_SPEED = 0.5f;
    private static final float HOVER_OUT_SPEED = 0.5f;

    public ClientButton(int id, int x, int y, int widthIn, int heightIn ,String text) {
        super(id, x, y, widthIn, heightIn, text);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;
        boolean isHovered = mouseX >= this.xPosition &&
                          mouseY >= this.yPosition &&
                          mouseX < this.xPosition + this.width &&
                          mouseY < this.yPosition + this.height;

        hoverAlpha += (isHovered ? HOVER_IN_SPEED / 10f : -HOVER_OUT_SPEED / 10f);
        hoverAlpha = MathHelper.clamp_float(hoverAlpha, 0.0F, 0.5F);
        //if (!isHovered && hoverAlpha < 0.005F) hoverAlpha = 0.0F;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int baseColor = new Color(0x33AAAAAA, true).getRGB();
        int borderColor = new Color(0x73969696, true).getRGB();
        int hoverColor = new Color(0xB3AAAAAA, true).getRGB();

        RenderUtils.drawRoundedRect(xPosition, yPosition,
            xPosition + width, yPosition + height, CORNER_RADIUS, baseColor);
        RenderUtils.drawBorderedRoundedRect(xPosition, yPosition,
                width, height, CORNER_RADIUS, 1.5f,
                isHovered ? hoverColor : borderColor
        );

        if (isHovered && hoverAlpha > 0.01f) {
            int alpha = (int)(hoverAlpha * 0.6f * 255);
            int overlayColor = (alpha << 24) | 0x00FFFFFF;
            RenderUtils.drawRoundedRect(xPosition, yPosition,
                    xPosition + width, yPosition + height,
                    CORNER_RADIUS, overlayColor);
        }

        int textAlpha = (int)((0.7f + hoverAlpha*0.3f)*255);
        this.drawCenteredString(mc.fontRendererObj, this.displayString,
            this.xPosition + this.width/2,
            this.yPosition + (this.height - 8)/2,
         (textAlpha << 24) | 0x00FFFFFF);
    }
}
