package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.Waypoint;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static cn.boop.necron.config.impl.WaypointOptionsImpl.waypoints;

public class WaypointEventHandler {
    private boolean lastLeftPressed = false;
    private boolean lastRightPressed = false;
    public static boolean isEditingWaypoint = false;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (waypoints) {
            Waypoint.renderWaypoints(event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Necron.mc.theWorld == null || Necron.mc.thePlayer == null) return;
        if (!waypoints) {
            isEditingWaypoint = false;
            return;
        }

        MovingObjectPosition rayTraceResult = Necron.mc.thePlayer.rayTrace(5.0d, 1.0f);
        if (rayTraceResult == null || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            resetEditingState();
            return;
        }

        BlockPos lookingAt = rayTraceResult.getBlockPos();

        IBlockState state = Necron.mc.theWorld.getBlockState(lookingAt);
        if (state == null) {
            resetEditingState();
            return;
        }
        
        Block block = state.getBlock();
        if (block.isAir(Necron.mc.theWorld, lookingAt)) return;

        boolean isAltPressed = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
        if (!isAltPressed) {
            resetEditingState();
            return;
        }

        isEditingWaypoint = true;
        handleMouseInput(lookingAt);
    }

    private void resetEditingState() {
        lastLeftPressed = false;
        lastRightPressed = false;
        isEditingWaypoint = false;
    }

    private void handleMouseInput(BlockPos pos) {
        boolean leftPressed = Mouse.isButtonDown(0);
        boolean rightPressed = Mouse.isButtonDown(1);
        boolean edited = false;

        if (rightPressed && !lastRightPressed) {
            Waypoint.addWaypoint(pos);
            edited = true;
        }

        if (leftPressed && !lastLeftPressed) {
            Waypoint.removeWaypoint(pos);
            edited = true;
        }

        if (edited) Waypoint.onWaypointsChanged();

        lastLeftPressed = leftPressed;
        lastRightPressed = rightPressed;
    }
}
