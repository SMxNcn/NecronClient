package cn.boop.necron.module.impl;

import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.RenderUtils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.m7Waypoints;
import static cn.boop.necron.config.impl.DungeonOptionsImpl.onlyP5;

public class M7Waypoints {
    private static final List<M7Waypoint> M7_WAYPOINTS = Arrays.asList(
            new M7Waypoint(new BlockPos(10, 7, 83), "", new Color(255, 85, 85)),
            new M7Waypoint(new BlockPos(29, 19, 57), "§cTarget", new Color(255, 85, 85)),
            new M7Waypoint(new BlockPos(25, 7, 119), "", new Color(85, 255, 85)),
            new M7Waypoint(new BlockPos(27, 16, 94), "§aTarget", new Color(85, 255, 85)),
            new M7Waypoint(new BlockPos(31, 6, 97), "", new Color(255, 85, 255)),
            new M7Waypoint(new BlockPos(81, 6, 99), "", new Color(255, 85, 255)),
            new M7Waypoint(new BlockPos(56, 20, 124), "§dTarget", new Color(255, 85, 255)),
            new M7Waypoint(new BlockPos(48, 6, 110), "", new Color(85, 255, 255)),
            new M7Waypoint(new BlockPos(85, 20, 98), "§bTarget", new Color(85, 255, 255)),
            new M7Waypoint(new BlockPos(53, 5, 90), "", new Color(255, 170, 0)),
            new M7Waypoint(new BlockPos(84, 20, 59), "§6Target", new Color(255, 170, 0))
    );

    private static class M7Waypoint {
        final BlockPos pos;
        final String name;
        final Color color;

        M7Waypoint(BlockPos pos, String name, Color color) {
            this.pos = pos;
            this.name = name;
            this.color = color;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!LocationUtils.inDungeon) return;
        if (LocationUtils.floor == LocationUtils.Floor.FLOOR_7 || LocationUtils.floor == LocationUtils.Floor.MASTER_7) {
            for (M7Waypoint waypoint : M7_WAYPOINTS) {
                if (onlyP5) {
                    if (LocationUtils.getM7Phase() == LocationUtils.M7Phases.P5) {
                        renderWaypoints(waypoint.pos, waypoint.name, waypoint.color, event.partialTicks);
                    }
                } else renderWaypoints(waypoint.pos, waypoint.name, waypoint.color, event.partialTicks);
            }
        }
    }

    public void renderWaypoints(BlockPos blockPos, String displayName, Color color, float partialTicks) {
        if (!m7Waypoints) return;
        RenderUtils.drawOutlinedBlockESP(
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                color,
                3.0f,
                partialTicks
        );

        RenderUtils.draw3DText(
                displayName,
                blockPos.getX() + 0.5,
                blockPos.getY() + 0.5,
                blockPos.getZ() + 0.5,
                color,
                partialTicks
        );
    }
}
