package cn.boop.necron.utils;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GLUtils {
    public static boolean lightingEnabled, depthTestEnabled;

    public static void backupAndSetupRender() {
        depthTestEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        lightingEnabled = GL11.glGetBoolean(GL11.GL_LIGHTING);

        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    public static void restorePreviousRenderState() {
        GlStateManager.depthMask(true);
        if (depthTestEnabled) GlStateManager.enableDepth();
        else GlStateManager.disableDepth();
        if (lightingEnabled) GlStateManager.enableLighting();
         else GlStateManager.disableLighting();

        GlStateManager.disableBlend();
    }

    public static void resetGLState(int width, int height) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
