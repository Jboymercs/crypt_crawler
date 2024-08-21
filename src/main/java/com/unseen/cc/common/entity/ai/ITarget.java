package com.unseen.cc.common.entity.ai;

import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public interface ITarget {
    Optional<Vec3d> getTarget();
}
