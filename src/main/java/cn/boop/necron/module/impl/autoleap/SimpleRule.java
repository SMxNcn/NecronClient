package cn.boop.necron.module.impl.autoleap;

import cn.boop.necron.utils.DungeonUtils;

public class SimpleRule implements TargetRule {
    private final DungeonUtils.DungeonClass target;

    SimpleRule(DungeonUtils.DungeonClass target) {
        this.target = target;
    }

    @Override
    public DungeonUtils.DungeonClass getTarget(boolean isCore) {
        return target;
    }
}