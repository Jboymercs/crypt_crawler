package com.unseen.cc.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A super class that will hold a lot of Misc methods and functions
 */
public class ModUtils {

    public static final String LANG_DESC = ModReference.MOD_ID + ".desc.";

    public static String translateDesc(String key, Object... params) {
        return I18n.format(ModUtils.LANG_DESC + key, params);
    }


    /**
     * Used for Combat, determines the radius and where the damage can be applied
     */

    public static void handleAreaImpact(float radius, Function<Entity, Float> maxDamage, Entity source, Vec3d pos, DamageSource damageSource) {
        handleAreaImpact(radius, maxDamage, source, pos, damageSource, 1, 0);
    }

    public static void handleAreaImpact(float radius, Function<Entity, Float> maxDamage, Entity source, Vec3d pos, DamageSource damageSource,
                                        float knockbackFactor, int fireFactor) {
        handleAreaImpact(radius, maxDamage, source, pos, damageSource, knockbackFactor, fireFactor, true);
    }


    public static void handleAreaImpact(float radius, Function<Entity, Float> maxDamage, Entity source, Vec3d pos, DamageSource damageSource,
                                        float knockbackFactor, int fireFactor, boolean damageDecay) {
        if (source == null) {
            return;
        }

        List<Entity> list = source.world.getEntitiesWithinAABBExcludingEntity(source, new AxisAlignedBB(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z).grow(radius));

        Predicate<Entity> isInstance = i -> i instanceof EntityLivingBase || i instanceof MultiPartEntityPart || i.canBeCollidedWith();
        double radiusSq = Math.pow(radius, 2);

        list.stream().filter(isInstance).forEach((entity) -> {

            // Get the hitbox size of the entity because otherwise explosions are less
            // effective against larger mobs
            double avgEntitySize = entity.getEntityBoundingBox().getAverageEdgeLength() * 0.75;

            // Choose the closest distance from the center or the head to encourage
            // headshots
            double distance = Math.min(Math.min(getCenter(entity.getEntityBoundingBox()).distanceTo(pos),
                            entity.getPositionVector().add(ModUtils.yVec(entity.getEyeHeight())).distanceTo(pos)),
                    entity.getPositionVector().distanceTo(pos));

            // Subtracting the average size makes it so that the full damage can be dealt
            // with a direct hit
            double adjustedDistance = Math.max(distance - avgEntitySize, 0);
            double adjustedDistanceSq = Math.pow(adjustedDistance, 2);
            double damageFactor = damageDecay ? Math.max(0, Math.min(1, (radiusSq - adjustedDistanceSq) / radiusSq)) : 1;

            // Damage decays by the square to make missed impacts less powerful
            double damageFactorSq = Math.pow(damageFactor, 2);
            double damage = maxDamage.apply(entity) * damageFactorSq;
            if (damage > 0 && adjustedDistanceSq < radiusSq) {
                entity.setFire((int) (fireFactor * damageFactorSq));
                if(entity.attackEntityFrom(damageSource, (float) damage)) {
                    double entitySizeFactor = avgEntitySize == 0 ? 1 : Math.max(0.5, Math.min(1, 1 / avgEntitySize));
                    double entitySizeFactorSq = Math.pow(entitySizeFactor, 2);

                    // Velocity depends on the entity's size and the damage dealt squared
                    Vec3d velocity = getCenter(entity.getEntityBoundingBox()).subtract(pos).normalize().scale(damageFactorSq).scale(knockbackFactor).scale(entitySizeFactorSq);
                    entity.addVelocity(velocity.x, velocity.y, velocity.z);
                }
            }
        });
    }

    public static Vec3d yVec(double heightAboveGround) {
        return new Vec3d(0, heightAboveGround, 0);
    }

    private static Vec3d getCenter(AxisAlignedBB box) {
        return new Vec3d(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);
    }

    /**
     * A function that allows an entity to leap to a POS
     */
    public static void leapTowards(EntityLivingBase entity, Vec3d target, float horzVel, float yVel) {
        Vec3d dir = target.subtract(entity.getPositionVector()).normalize();
        Vec3d leap = new Vec3d(dir.x, 0, dir.z).normalize().scale(horzVel).add(ModUtils.yVec(yVel));
        entity.motionX += leap.x;
        if (entity.motionY < 0.1) {
            entity.motionY += leap.y;
        }
        entity.motionZ += leap.z;

        // Normalize to make sure the velocity doesn't go beyond what we expect
        double horzMag = Math.sqrt(Math.pow(entity.motionX, 2) + Math.pow(entity.motionZ, 2));
        double scale = horzVel / horzMag;
        if (scale < 1) {
            entity.motionX *= scale;
            entity.motionZ *= scale;
        }
    }

    /**
     * Used to create a line, with points inbetween
     * @param start
     * @param end
     * @param points
     * @param callback
     */

    public static void lineCallback(Vec3d start, Vec3d end, int points, BiConsumer<Vec3d, Integer> callback) {
        Vec3d dir = end.subtract(start).scale(1 / (float) (points - 1));
        Vec3d pos = start;
        for (int i = 0; i < points; i++) {
            callback.accept(pos, i);
            pos = pos.add(dir);
        }
    }

    /**
     * Provides multiple points in a circle via a callback
     *
     * @param radius          The radius of the circle
     * @param points          The number of points around the circle
     * @param particleSpawner
     */

    public static void circleCallback(float radius, int points, Consumer<Vec3d> particleSpawner) {
        float degrees = 360f / points;
        for (int i = 0; i < points; i++) {
            double radians = Math.toRadians(i * degrees);
            Vec3d offset = new Vec3d(Math.sin(radians), Math.cos(radians), 0).scale(radius);
            particleSpawner.accept(offset);
        }
    }

    public static List<Vec3d> circlePoints(float radius, int numPoints) {
        List<Vec3d> points = new ArrayList<>();
        circleCallback(radius, numPoints, points::add);
        return points;
    }


    /**
     * Used to check if the entity block the current damage source, this will be used for any mechanics like shields, or a mechinic that negates damage
     * @param damageSourceIn
     * @param entity
     * @return
     */
    public static boolean canBlockDamageSource(DamageSource damageSourceIn, EntityLivingBase entity)
    {
        if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking())
        {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vec3d vec3d1 = entity.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(entity.posX, entity.posY, entity.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static BlockPos posToChunk(BlockPos pos) {
        return new BlockPos(pos.getX() / 16f, pos.getY(), pos.getZ() / 16f);
    }


    public static void setEntityPosition(Entity entity, Vec3d vec) {
        entity.setPosition(vec.x, vec.y, vec.z);
    }


    /**
     * Destroys blocks in the given radius
     * @param box
     * @param world
     * @param entity
     */
    public static void destroyBlocksInAABB(AxisAlignedBB box, World world, Entity entity) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.minY);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.floor(box.maxX);
        int i1 = MathHelper.floor(box.maxY);
        int j1 = MathHelper.floor(box.maxZ);

        for (int x = i; x <= l; ++x) {
            for (int y = j; y <= i1; ++y) {
                for (int z = k; z <= j1; ++z) {
                    BlockPos blockpos = new BlockPos(x, y, z);
                    IBlockState iblockstate = world.getBlockState(blockpos);
                    Block block = iblockstate.getBlock();

                    if (!block.isAir(iblockstate, world, blockpos) && iblockstate.getMaterial() != Material.FIRE) {
                        if (ForgeEventFactory.getMobGriefingEvent(world, entity)) {
                            if (block != Blocks.COMMAND_BLOCK &&
                                    block != Blocks.REPEATING_COMMAND_BLOCK &&
                                    block != Blocks.CHAIN_COMMAND_BLOCK &&
                                    block != Blocks.BEDROCK &&
                                    !(block instanceof BlockLiquid)) {
                                if (world.getClosestPlayer(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 20, false) != null) {
                                    world.destroyBlock(blockpos, false);
                                } else {
                                    world.setBlockToAir(blockpos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Allows an entity to search for the given blocks
     * @param box
     * @param world
     * @param entity
     * @param block
     * @return
     */

    public static BlockPos searchForBlocks(AxisAlignedBB box, World world, Entity entity, IBlockState block) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.minY);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.floor(box.maxX);
        int i1 = MathHelper.floor(box.maxY);
        int j1 = MathHelper.floor(box.maxZ);
        for (int x = i; x <= l; ++x) {
            for (int y = j; y <= i1; ++y) {
                for (int z = k; z <= j1; ++z) {
                    BlockPos blockpos = new BlockPos(x, y, z);
                    IBlockState iblockstate = world.getBlockState(blockpos);


                    if(iblockstate == block) {

                        return blockpos;
                    }
                }
            }
        }

        return null;
    }


    public static float getMobDamage(double baseAttackDamage, double healthScaledAttackFactor, float maxHealth, float health) {

        double healthScaledAttack = baseAttackDamage * healthScaledAttackFactor * (((maxHealth * 0.5) - health) / maxHealth);

        return (float) ((healthScaledAttack + baseAttackDamage));
    }


    public static AxisAlignedBB makeBox(Vec3d pos1, Vec3d pos2) {
        return new AxisAlignedBB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
    }

}
