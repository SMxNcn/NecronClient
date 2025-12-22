package cn.boop.necron.module.impl.autoleap;

import cn.boop.necron.utils.DungeonUtils;

public class ConditionalRule implements TargetRule {
    private final DungeonUtils.DungeonClass targetWhenCore;
    private final DungeonUtils.DungeonClass targetWhenNotCore;

    ConditionalRule(DungeonUtils.DungeonClass targetWhenCore, DungeonUtils.DungeonClass targetWhenNotCore) {
        this.targetWhenCore = targetWhenCore;
        this.targetWhenNotCore = targetWhenNotCore;
    }

    @Override
    public DungeonUtils.DungeonClass getTarget(boolean isCore) {
        return isCore ? targetWhenCore : targetWhenNotCore;
    }
}
