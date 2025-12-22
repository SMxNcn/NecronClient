package cn.boop.necron.module.impl.autoleap;

import cn.boop.necron.utils.DungeonUtils;

public interface TargetRule {
    DungeonUtils.DungeonClass getTarget(boolean isCore);
}
