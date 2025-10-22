package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.RenderUtils;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.*;

public class DungeonESP {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!LocationUtils.inDungeon) return;
        for (Object entityObj : Necron.mc.theWorld.loadedEntityList) {
            if (entityObj instanceof EntityWither && witherESP) {
                if (!(LocationUtils.floor == LocationUtils.Floor.MASTER_7 || LocationUtils.floor == LocationUtils.Floor.FLOOR_7)) return;
                EntityWither wither = (EntityWither) entityObj;
                double interpPosX = wither.lastTickPosX + (wither.posX - wither.lastTickPosX) * event.partialTicks;
                double interpPosY = wither.lastTickPosY + (wither.posY - wither.lastTickPosY) * event.partialTicks;
                double interpPosZ = wither.lastTickPosZ + (wither.posZ - wither.lastTickPosZ) * event.partialTicks;

                AxisAlignedBB interpolatedBB = wither.getEntityBoundingBox()
                        .offset(interpPosX - wither.posX, interpPosY - wither.posY, interpPosZ - wither.posZ);

                RenderUtils.drawOutlinedBoundingBox(
                        smoothESP ? interpolatedBB : wither.getEntityBoundingBox(),
                        espColor.toJavaColor(),
                        2.5f, event.partialTicks
                );
            } else if (entityObj instanceof EntityBat && batESP) {
                EntityBat bat = (EntityBat) entityObj;
                double interpPosX = bat.lastTickPosX + (bat.posX - bat.lastTickPosX) * event.partialTicks;
                double interpPosY = bat.lastTickPosY + (bat.posY - bat.lastTickPosY) * event.partialTicks;
                double interpPosZ = bat.lastTickPosZ + (bat.posZ - bat.lastTickPosZ) * event.partialTicks;

                AxisAlignedBB interpolatedBB = bat.getEntityBoundingBox()
                        .offset(interpPosX - bat.posX, interpPosY - bat.posY, interpPosZ - bat.posZ);

                RenderUtils.drawOutlinedBoundingBox(
                        smoothESP ? interpolatedBB : bat.getEntityBoundingBox(),
                        espColor.toJavaColor(),
                        2.5f, event.partialTicks
                );
            }
        }
    }
}
