package com.unseen.cc.common.entity.base;

import akka.japi.pf.FI;
import com.unseen.cc.common.entity.base.navigation.MobGroundNavigate;
import com.unseen.cc.common.entity.base.scaling.ServerScaleUtil;
import com.unseen.cc.util.ModUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Base Entity Class for all entities included in this mod
 */
public abstract class EntityCCMobBase extends EntityMob {

    private float regenTimer;
    protected static final DataParameter<Boolean> IMMOVABLE = EntityDataManager.createKey(EntityCCMobBase.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> FIGHT_MODE = EntityDataManager.createKey(EntityCCMobBase.class, DataSerializers.BOOLEAN);


    protected float sizeScaling = 1.0F;

    //Used for Locking look of the mob during attacks
    public boolean lockLook = false;

    //This boolean when put true in the constructor will add boss scaling
    public boolean iAmBossMob = false;

    //This setting is for all non-targetable Entities, things like AOE rings and such
    public boolean iAmEffectMob = false;
    // Factor that determines how much attack damage is increased by lower health
    protected double healthScaledAttackFactor = 0.0;

    //Boolean to check if the current mob has scaled to nearby players yet
    protected boolean hasStartedScaling = false;
    //int of switching between targets
    protected int checkNearbyPlayers = 250;

    private PriorityQueue<TimedEvent> events = new PriorityQueue<TimedEvent>();

    private static float regenStartTimer = 20;

    private Vec3d initialPosition = null;

    protected boolean isImmovable() {
        return this.dataManager == null ? false : this.dataManager.get(IMMOVABLE);
    }

    protected void setImmovable(boolean immovable) {
        this.dataManager.set(IMMOVABLE, immovable);
    }

    public boolean isFightMode() {return this.dataManager.get(FIGHT_MODE);}
    protected void setFightMode(boolean value) {this.dataManager.set(FIGHT_MODE, Boolean.valueOf(value));}

    public EntityCCMobBase(World worldIn, float x, float y, float z) {
        super(worldIn);
        this.setPosition(x, y, z);
        //Initial check for Nearby Players to scale the bosses Health Accordingly
    }

    public EntityCCMobBase(World worldIn) {
        super(worldIn);
        this.experienceValue = 5;
    }

    //When calling this method, it will grab the mobs healthScaledAttackFactor to determine damage
    public float getAttack() {
        return ModUtils.getMobDamage(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue(), healthScaledAttackFactor, this.getMaxHealth(),
                this.getHealth());
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new MobGroundNavigate(this, worldIn);
    }

    @SideOnly(Side.CLIENT)
    protected void initAnimation() {
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        if(!this.isImmovable()) {
            super.move(type, x, y, z);
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(1.0D);
    }


    @Override
    public void onLivingUpdate() {
        EntityLivingBase target = this.getAttackTarget();

        if(!this.hasStartedScaling && target != null) {
            if(target instanceof EntityPlayer && !world.isRemote) {
                //Scaling for Bosses
                if(this.iAmBossMob) {
                    double changeAttackDamage = ServerScaleUtil.scaleAttackDamageInAccordanceWithPlayers(this, world);
                    double maxHealthCurrently = ServerScaleUtil.setMaxHealthWithPlayers(this, world);
                    this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealthCurrently);
                    this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(changeAttackDamage);
                    this.setHealth(this.getMaxHealth());
                    hasStartedScaling = true;
                }
                //Scaling for Regular Mobs, remove effect mobs like AOE or other entites we don't want scaled
                else if(!this.iAmEffectMob) {
                    double changeAttackDamage = ServerScaleUtil.regularScaleAttackDamageInAccordanceWithPlayers(this, world);
                    double maxHealthCurrently = ServerScaleUtil.regularSetMaxHealthWithPlayers(this, world);
                    this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealthCurrently);
                    this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(changeAttackDamage);
                    this.setHealth(this.getMaxHealth());
                    hasStartedScaling = true;
                }
            }
        }

        //Deletion of Certain Entites due to there spamming
        if(this.iAmBossMob) {
            List<EntityEnderCrystal> nearbyEyes = this.world.getEntitiesWithinAABB(EntityEnderCrystal.class, this.getEntityBoundingBox().grow(30D), e -> !e.getIsInvulnerable());
            if(!nearbyEyes.isEmpty()) {
                for(EntityEnderCrystal eye: nearbyEyes) {
                    eye.setDead();
                }
            }
        }
        //This is where target Switching occurs for bosses
        if(this.iAmBossMob && checkNearbyPlayers <= 0 && target != null) {
            if(target instanceof EntityPlayer) {
                //Makes sure it's a player for the second time in here, just as a double check.
                this.setAttackTarget(ServerScaleUtil.targetSwitcher(this, world));
                this.checkNearbyPlayers = 250;
            }
        } else {
            checkNearbyPlayers--;
        }

        if (!isDead && this.getHealth() > 0) {
            boolean foundEvent = true;
            while (foundEvent) {
                TimedEvent event = events.peek();
                if (event != null && event.ticks <= this.ticksExisted) {
                    events.remove();
                    event.callback.run();
                } else {
                    foundEvent = false;
                }
            }
        }


        if (!world.isRemote) {
            if (this.getAttackTarget() == null) {
                if (this.regenTimer > this.regenStartTimer) {
                    if (this.ticksExisted % 20 == 0) {
                        this.heal(this.getMaxHealth() * 0.015f);
                    }
                } else {
                    this.regenTimer++;
                }
            } else {
                this.regenTimer = 0;
            }
        }
        super.onLivingUpdate();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(IMMOVABLE, Boolean.valueOf(false));
        this.dataManager.register(FIGHT_MODE, Boolean.valueOf(false));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        // Make sure we save as immovable to avoid some weird states
        nbt.setBoolean("isImmovable", this.isImmovable());
        nbt.setBoolean("Fight_Mode", this.isFightMode());
        super.writeEntityToNBT(nbt);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        this.setFightMode(nbt.getBoolean("Fight_Mode"));
        if (nbt.hasKey("isImmovable")) {
            this.setImmovable(nbt.getBoolean("isImmovable"));
        }
        super.readEntityFromNBT(nbt);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {

        return super.attackEntityFrom(source, amount);
    }


    public float getSizeVariation() {
        return this.sizeScaling;
    }


    public void doRender(RenderManager renderManager, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));

    }

    /**
     * Adds an event to be executed at a later time. Negative ticks are executed immediately.
     *
     * @param runnable
     * @param ticksFromNow
     */
    public void addEvent(Runnable runnable, int ticksFromNow) {
        events.add(new TimedEvent(runnable, this.ticksExisted + ticksFromNow));
    }


}
