package cn.boop.necron.config;

import cn.boop.necron.Necron;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;


public class SoundManager {
    public static int soundCount = 12;
    public static ResourceLocation[] EF_SOUNDS = new ResourceLocation[soundCount];
    public static boolean isPlaying = false;
    private static PositionedSoundRecord currentSound;

    public static void registerSounds() {
        for (int i = 0; i < soundCount; i++) {
            String soundName = "necron:ef" + (i + 1);
            EF_SOUNDS[i] = new ResourceLocation(soundName);
        }
    }

    public static void playSound(int index, float pitch) {
        if (index >= 0 && index < EF_SOUNDS.length) {
            //currentSound = PositionedSoundRecord.create(EF_SOUNDS[index], pitch);
            Necron.mc.addScheduledTask(() -> {
                isPlaying = true;
                currentSound = PositionedSoundRecord.create(EF_SOUNDS[index], pitch);
                Necron.mc.getSoundHandler().playSound(currentSound);
            });
        }
    }

    public static boolean checkIfStillPlaying() {
        if (!isPlaying || currentSound == null) {
            return false;
        }

        if (!Necron.mc.getSoundHandler().isSoundPlaying(currentSound)) {
            isPlaying = false;
            currentSound = null;
            return false;
        }

        return true;
    }
}
