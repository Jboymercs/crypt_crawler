package com.unseen.cc.common.entity.base.scaling;

import com.unseen.cc.common.entity.base.EntityCCMobBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.List;

public class ServerScaleUtil {
    //This sets the Current Health in accordance with how many players are current
    //Each Player after 1 adding 0.5 * currentHealth
    public static float changeHealthAccordingToPlayers(EntityLivingBase actor, World world) {

        int playersNearby = 0;

        if(!world.isRemote) {
            List<EntityPlayer> nearbyPlayers = actor.world.getEntitiesWithinAABB(EntityPlayer.class, actor.getEntityBoundingBox().grow(60D), e -> !e.getIsInvulnerable());

            if(!nearbyPlayers.isEmpty()) {
                for(EntityPlayer playerCap : nearbyPlayers) {
                    if(!playerCap.isCreative() && !playerCap.isSpectator()) {
                        playersNearby++;
                    }
                }
            }

            if(playersNearby > 1) {
                double additiveHealth = actor.getHealth() * ((playersNearby * 0.4) - 0.4); //We have to subtract atleats 0.5D due to the player count 1 not adding anything
                //Returns the additive amount and this is what sets the health
                return (float) (actor.getHealth() + additiveHealth);
            } else {
                return actor.getHealth();
            }
        }

        return actor.getHealth();
    }

    //This sets the Max Health of an Entity in accordance with how many players are nearby
    //Why we need to adjust the max health is to show little to no difference in health bar when a player joins the fight

    public static double setMaxHealthWithPlayers(EntityLivingBase actor, World world) {

        int playersNearby = 0;
        if(!world.isRemote) {
            List<EntityPlayer> nearbyPlayers = actor.world.getEntitiesWithinAABB(EntityPlayer.class, actor.getEntityBoundingBox().grow(60D), e -> !e.getIsInvulnerable());

            if(!nearbyPlayers.isEmpty()) {
                for(EntityPlayer playerCap : nearbyPlayers) {
                    if(!playerCap.isCreative() && !playerCap.isSpectator()) {
                        playersNearby++;
                    }
                }
            }

            if(playersNearby > 1) {
                double additiveHealth = actor.getMaxHealth() * ((playersNearby * 0.4) - 0.4); //We have to subtract atleats 0.5D due to the player count 1 not adding anything
                //Returns the additive amount and this is what sets the health

                return (float) (actor.getMaxHealth() + additiveHealth);
            } else {
                return actor.getMaxHealth();
            }
        }
        return actor.getMaxHealth();
    }

    //This scales the Attack Damage with how many players are near on startUp
    public static double scaleAttackDamageInAccordanceWithPlayers(EntityLivingBase actor, World world) {
        double currentAttackDamage = actor.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int playersNearby = 0;
        if(!world.isRemote) {
            List<EntityPlayer> nearbyPlayers = actor.world.getEntitiesWithinAABB(EntityPlayer.class, actor.getEntityBoundingBox().grow(60D), e-> !e.getIsInvulnerable());
            if(!nearbyPlayers.isEmpty()) {
                for(EntityPlayer playerCap : nearbyPlayers) {
                    if(!playerCap.isCreative() && !playerCap.isSpectator()) {
                        playersNearby++;
                    }
                }
            }

            if(playersNearby > 1) {
                double additiveAttackDamage = currentAttackDamage * ((playersNearby * 0.18) - 0.18);

                return currentAttackDamage + additiveAttackDamage;
            } else {
                return currentAttackDamage;
            }
        }

        return currentAttackDamage;
    }

    //Sets Max health for regular mobs with scale REGULAR MOBS
    public static double regularSetMaxHealthWithPlayers(EntityLivingBase actor, World world) {

        int playersNearby = 0;
        if(!world.isRemote) {
            List<EntityPlayer> nearbyPlayers = actor.world.getEntitiesWithinAABB(EntityPlayer.class, actor.getEntityBoundingBox().grow(60D), e -> !e.getIsInvulnerable());

            if(!nearbyPlayers.isEmpty()) {
                for(EntityPlayer playerCap : nearbyPlayers) {
                    if(!playerCap.isCreative() && !playerCap.isSpectator()) {
                        playersNearby++;
                    }
                }
            }

            if(playersNearby > 1) {
                double additiveHealth = actor.getMaxHealth() * ((playersNearby * 0.15) - 0.15); //We have to subtract atleats 0.5D due to the player count 1 not adding anything
                //Returns the additive amount and this is what sets the health

                return (float) (actor.getMaxHealth() + additiveHealth);
            } else {
                return actor.getMaxHealth();
            }
        }
        return actor.getMaxHealth();
    }

    //This scales the Attack Damage with how many players are near on startUp REGULAR MOBS
    public static double regularScaleAttackDamageInAccordanceWithPlayers(EntityLivingBase actor, World world) {
        double currentAttackDamage = actor.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int playersNearby = 0;
        if(!world.isRemote) {
            List<EntityPlayer> nearbyPlayers = actor.world.getEntitiesWithinAABB(EntityPlayer.class, actor.getEntityBoundingBox().grow(60D), e-> !e.getIsInvulnerable());
            if(!nearbyPlayers.isEmpty()) {
                for(EntityPlayer playerCap : nearbyPlayers) {
                    if(!playerCap.isCreative() && !playerCap.isSpectator()) {
                        playersNearby++;
                    }
                }
            }

            if(playersNearby > 1) {
                double additiveAttackDamage = currentAttackDamage * ((playersNearby * 0.05) - 0.05);

                return currentAttackDamage + additiveAttackDamage;
            } else {
                return currentAttackDamage;
            }
        }

        return currentAttackDamage;
    }

    //This functions allows us to switch between targets and allow everyone to get a bit of the fun

    public static EntityLivingBase targetSwitcher(EntityCCMobBase actor, World world) {
        EntityLivingBase currentTarget = actor.getAttackTarget();
        if(currentTarget != null && !world.isRemote && currentTarget instanceof EntityPlayer) {
            double range = actor.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            List<EntityPlayer> nearbySimilarTargets = actor.world.getEntitiesWithinAABB(EntityPlayer.class, actor.getEntityBoundingBox().grow(range), e-> !e.getIsInvulnerable());

            int currentPlayerCountCanSee = 0;
            if(!nearbySimilarTargets.isEmpty()) {
                //Firts gets a count
                for(EntityPlayer baseToo : nearbySimilarTargets) {
                    if(!baseToo.isSpectator() && !baseToo.isCreative()) {
                        currentPlayerCountCanSee++;
                    }
                }
                //After checking how many players it runs a test to see if there is more than one this entity can see and then selects them as it's new target
                for(EntityPlayer baseFrom : nearbySimilarTargets) {
                    if(currentPlayerCountCanSee > 1 && actor.getEntitySenses().canSee(baseFrom) && !baseFrom.isCreative() && !baseFrom.isSpectator()) {
                        return baseFrom;
                    } else {
                        //If there is only one, return current target
                        return currentTarget;
                    }

                }
            }
        }

        return currentTarget;
    }
}
