package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.item.ItemProtector;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.NecronOptionsImpl.protectKey;

public class ItemProtectorEventHandler {
    private boolean lastKeyState = false;
    private long lastKeyPressTime = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || Necron.mc.thePlayer == null) return;

        if (Necron.mc.currentScreen == null) {
            lastKeyState = false;
            return;
        }

        boolean currentKeyState = protectKey.isActive();
        long currentTime = System.currentTimeMillis();

        if (currentKeyState && !lastKeyState) {
            if (currentTime - lastKeyPressTime > 80) {
                ItemProtector.toggleHoveredItemProtection();
                lastKeyPressTime = currentTime;
            }
        }

        lastKeyState = currentKeyState;
    }
}
