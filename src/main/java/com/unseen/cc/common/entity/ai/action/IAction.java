package com.unseen.cc.common.entity.ai.action;

import com.unseen.cc.common.entity.base.EntityCCMobBase;
import net.minecraft.entity.EntityLivingBase;

/**
 * Works with the Attack Manager and helps condese code into there separate classes for attack sequences
 */
public interface IAction {
    void performAction(EntityCCMobBase actor, EntityLivingBase target);

    IAction NONE = (actor, target) -> {
    };
}
