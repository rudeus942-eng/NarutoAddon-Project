
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDeidaraShippudenNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 266;

   public EntityDeidaraShippudenNPC(ElementsInfTsukAddon instance) {
      super(instance, 266);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "deidarashippuden"), 266).name("deidarashippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CustomRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CustomRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/deidara.png");

      public CustomRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayerPoseable(0.0F, false), 0.5F);
         this.addLayer(new LayerHeldItem(this));
         this.addLayer(new LayerBipedArmor(this));
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String texOverride = entity.getTextureOverride();
         if (texOverride != null && !texOverride.isEmpty()) {
            return new ResourceLocation(texOverride);
         } else {
            String configId = entity.getNpcConfigId();
            if (configId != null && !configId.isEmpty()) {
               NpcConfig config = NpcConfigRegistry.get(configId);
               if (config != null && config.getTexture() != null) {
                  return config.getTexture();
               }
            }

            return FALLBACK_TEXTURE;
         }
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               float scale = config.getRenderScale();
               GlStateManager.scale(scale, scale, scale);
            }
         }

         String poseName = entity.getNpcPoseName();
         NpcPose pose = NpcPose.fromName(poseName);
         if (pose.yOffset != 0.0F) {
            GlStateManager.translate(0.0F, pose.yOffset, 0.0F);
         }

      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private int deidaraC1CD = 0;
      private int deidaraC2CD = 0;
      private int deidaraC3CD = 0;
      private int deidaraEvasiveCD = 0;
      private int deidaraMineCD = 0;
      private int deidaraMeleeCD = 0;
      private int deidaraDialogCD = 0;
      private boolean deidaraIntroPlayed = false;
      private boolean deidaraC3Charging = false;
      private int deidaraC3ChargeTimer = 0;
      private double deidaraC3TargetX;
      private double deidaraC3TargetY;
      private double deidaraC3TargetZ;
      private List<double[]> deidaraActiveMines = new ArrayList();
      private static final int DEIDARA_C1_CD_BASE = 60;
      private static final int DEIDARA_C2_CD_BASE = 320;
      private static final int DEIDARA_C3_CD_BASE = 500;
      private static final int DEIDARA_EVASIVE_CD_BASE = 80;
      private static final int DEIDARA_MINE_CD_BASE = 100;
      private static final float DEIDARA_C1_TOTAL = 30.0F;
      private static final float DEIDARA_C2_TOTAL = 23.0F;
      private static final float DEIDARA_C3_TOTAL = 60.0F;
      private static final float DEIDARA_MINE_TOTAL = 26.0F;
      private static final float DEIDARA_MELEE_TOTAL = 17.0F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.deidaraIntroPlayed && target instanceof EntityPlayerMP) {
            this.deidaraIntroPlayed = true;
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§7[§eDeidara§7] §fArt is an explosion! Hm!"));
         }

         if (this.deidaraC3Charging) {
            --this.deidaraC3ChargeTimer;
            if (this.ticksExisted % 4 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)2.0F, this.posZ, 5, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
            }

            if (this.deidaraC3ChargeTimer <= 0) {
               this.deidaraDetonateC3();
               this.deidaraC3Charging = false;
            }

         } else {
            this.deidaraCheckMines();
            if (this.deidaraDialogCD <= 0 && this.rand.nextFloat() < 0.005F) {
               this.deidaraDialogCD = 200;
               String[] lines = new String[]{"§7[§eDeidara§7] §fYou call that art? Hm!", "§7[§eDeidara§7] §fTrue art is fleeting beauty!", "§7[§eDeidara§7] §fKatsu!", "§7[§eDeidara§7] §fMy art will blow you away! Hm!"};
               this.broadcastChat((double)30.0F, lines[this.rand.nextInt(lines.length)]);
            }

            if (dist < (double)5.0F && this.deidaraEvasiveCD <= 0) {
               this.deidaraEvasiveJump(target);
               if (this.deidaraMineCD <= 0) {
                  this.deidaraPlaceMine(target);
               }

            } else {
               float hpFrac = this.getHealth() / this.getMaxHealth();
               if (this.deidaraC3CD <= 0 && dist >= (double)6.0F && dist <= (double)25.0F && hpFrac <= 0.35F && this.rand.nextFloat() < 0.02F) {
                  this.deidaraStartC3(target);
               } else if (this.deidaraC2CD <= 0 && dist >= (double)6.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.04F) {
                  this.deidaraFireC2(target);
               } else if (this.deidaraC1CD <= 0 && dist >= (double)4.0F && dist <= (double)22.0F) {
                  this.deidaraFireC1(target);
               } else if (this.deidaraMineCD <= 0 && dist >= (double)5.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.06F) {
                  this.deidaraPlaceMine(target);
               } else if (this.deidaraMeleeCD <= 0 && dist <= (double)3.0F) {
                  this.deidaraMelee(target);
               } else {
                  if (dist < (double)8.0F) {
                     double dx = this.posX - target.posX;
                     double dz = this.posZ - target.posZ;
                     double d = Math.sqrt(dx * dx + dz * dz);
                     if (d > (double)0.0F) {
                        this.motionX = dx / d * 0.3;
                        this.motionZ = dz / d * 0.3;
                        this.velocityChanged = true;
                     }
                  } else if (dist > (double)25.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
                  }

                  this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.deidaraC1CD > 0) {
            --this.deidaraC1CD;
         }

         if (this.deidaraC2CD > 0) {
            --this.deidaraC2CD;
         }

         if (this.deidaraC3CD > 0) {
            --this.deidaraC3CD;
         }

         if (this.deidaraEvasiveCD > 0) {
            --this.deidaraEvasiveCD;
         }

         if (this.deidaraMineCD > 0) {
            --this.deidaraMineCD;
         }

         if (this.deidaraMeleeCD > 0) {
            --this.deidaraMeleeCD;
         }

         if (this.deidaraDialogCD > 0) {
            --this.deidaraDialogCD;
         }

      }

      protected void resetCombatState() {
         this.deidaraIntroPlayed = false;
         this.deidaraC3Charging = false;
         this.deidaraC3ChargeTimer = 0;
         this.deidaraActiveMines.clear();
         this.deidaraC1CD = 0;
         this.deidaraC2CD = 0;
         this.deidaraC3CD = 0;
         this.deidaraEvasiveCD = 0;
         this.deidaraMineCD = 0;
         this.deidaraMeleeCD = 0;
         this.deidaraDialogCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("deidaraC1CD", this.deidaraC1CD);
         compound.setInteger("deidaraC2CD", this.deidaraC2CD);
         compound.setInteger("deidaraC3CD", this.deidaraC3CD);
         compound.setInteger("deidaraEvasiveCD", this.deidaraEvasiveCD);
         compound.setInteger("deidaraMineCD", this.deidaraMineCD);
         compound.setInteger("deidaraMeleeCD", this.deidaraMeleeCD);
         compound.setInteger("deidaraDialogCD", this.deidaraDialogCD);
         compound.setBoolean("deidaraIntroPlayed", this.deidaraIntroPlayed);
         compound.setBoolean("deidaraC3Charging", this.deidaraC3Charging);
         compound.setInteger("deidaraC3ChargeTimer", this.deidaraC3ChargeTimer);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.deidaraC1CD = compound.hasKey("deidaraC1CD") ? compound.getInteger("deidaraC1CD") : 0;
         this.deidaraC2CD = compound.hasKey("deidaraC2CD") ? compound.getInteger("deidaraC2CD") : 0;
         this.deidaraC3CD = compound.hasKey("deidaraC3CD") ? compound.getInteger("deidaraC3CD") : 0;
         this.deidaraEvasiveCD = compound.hasKey("deidaraEvasiveCD") ? compound.getInteger("deidaraEvasiveCD") : 0;
         this.deidaraMineCD = compound.hasKey("deidaraMineCD") ? compound.getInteger("deidaraMineCD") : 0;
         this.deidaraMeleeCD = compound.hasKey("deidaraMeleeCD") ? compound.getInteger("deidaraMeleeCD") : 0;
         this.deidaraDialogCD = compound.hasKey("deidaraDialogCD") ? compound.getInteger("deidaraDialogCD") : 0;
         this.deidaraIntroPlayed = compound.getBoolean("deidaraIntroPlayed");
         this.deidaraC3Charging = compound.getBoolean("deidaraC3Charging");
         this.deidaraC3ChargeTimer = compound.hasKey("deidaraC3ChargeTimer") ? compound.getInteger("deidaraC3ChargeTimer") : 0;
      }

      private void deidaraFireC1(EntityLivingBase target) {
         this.deidaraC1CD = this.cdMul(60);
         double dmgMul = this.getDamageMultiplier();
         float c1Total = 30.0F * (float)dmgMul;
         float normDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normDmg = c1Total * (1.0F - this.trueDamageSplit);
            trueDmg = c1Total * this.trueDamageSplit * this.trueDamageMultiplier;
         } else {
            normDmg = c1Total;
            trueDmg = 5.0F * this.trueDamageMultiplier;
         }

         double bx = target.posX - this.posX;
         double by = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double bz = target.posZ - this.posZ;

         for(int i = 0; i < 3; ++i) {
            EntityClayBomb.EntityCustom bomb = new EntityClayBomb.EntityCustom(this.world, this, normDmg, trueDmg, 4.0F);
            double spreadAngle = (double)(i - 1) * 0.2;
            double sx = bx * Math.cos(spreadAngle) - bz * Math.sin(spreadAngle);
            double sz = bx * Math.sin(spreadAngle) + bz * Math.cos(spreadAngle);
            bomb.shoot(sx, by, sz, 1.4F, 2.5F);
            this.world.spawnEntity(bomb);
         }

         for(int i = 0; i < 3; ++i) {
            EntityClayBirdC1.EntityCustom c1 = new EntityClayBirdC1.EntityCustom(this.world);
            double spreadX = (double)(i - 1) * (double)2.0F + (this.rand.nextDouble() - (double)0.5F);
            double spreadZ = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            c1.setPosition(this.posX + spreadX, this.posY + (double)1.5F, this.posZ + spreadZ);
            c1.setTarget(target);
            c1.setOwner(this);
            c1.setExplosionDamage(normDmg, trueDmg, 4.0F);
            this.world.spawnEntity(c1);
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 10; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F), this.posY + 1.2, this.posZ + (this.rand.nextDouble() - (double)0.5F), 1, (double)0.0F, 0.03, (double)0.0F, (double)0.0F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.4, 0.3, 0.4, (double)0.0F, new int[0]);

            for(int i = 0; i < 6; ++i) {
               ws.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + (double)1.0F + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 1, (double)0.0F, 0.02, (double)0.0F, 0.02, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.5F);
      }

      private void deidaraFireC2(EntityLivingBase target) {
         this.deidaraC2CD = this.cdMul(320);
         double dmgMul = this.getDamageMultiplier();
         float c2Total = 23.0F * (float)dmgMul;
         float normDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normDmg = c2Total * (1.0F - this.trueDamageSplit);
            trueDmg = c2Total * this.trueDamageSplit * this.trueDamageMultiplier;
         } else {
            normDmg = c2Total;
            trueDmg = 4.0F * this.trueDamageMultiplier;
         }

         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (!(d < 0.1)) {
            double ndx = dx / d;
            double ndz = dz / d;

            for(int i = 0; i < 5; ++i) {
               EntityClayBomb.EntityCustom bomb = new EntityClayBomb.EntityCustom(this.world, this, normDmg, trueDmg, 5.0F);
               double spreadX = ndx + (this.rand.nextDouble() - (double)0.5F) * 0.08;
               double spreadZ = ndz + (this.rand.nextDouble() - (double)0.5F) * 0.08;
               double by = (target.posY + (double)target.height * (double)0.5F - this.posY) / d;
               bomb.shoot(spreadX, by, spreadZ, 1.4F + (float)i * 0.08F, 2.0F);
               this.world.spawnEntity(bomb);
            }

            EntityClayDragonC2.EntityCustom c2 = new EntityClayDragonC2.EntityCustom(this.world);
            c2.setPosition(this.posX, this.posY + (double)2.0F, this.posZ);
            c2.setFlyTarget(target.posX, target.posY + (double)2.0F, target.posZ);
            c2.setOwner(this);
            c2.setExplosionDamage(normDmg * 6.0F, trueDmg * 6.0F, 12.0F);
            this.world.spawnEntity(c2);
            this.broadcastChat((double)30.0F, "§7[§eDeidara§7] §fC2! Dragon!!");
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 20; ++i) {
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)1.0F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
               }

               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.5F, this.posZ, 3, (double)0.5F, 0.3, (double)0.5F, (double)0.0F, new int[0]);

               for(int i = 0; i < 12; ++i) {
                  double frac = (double)(i + 1) / (double)13.0F;
                  double trailX = this.posX + dx * frac;
                  double trailY = this.posY + (double)1.0F + (target.posY - this.posY) * frac;
                  double trailZ = this.posZ + dz * frac;
                  ws.spawnParticle(EnumParticleTypes.FLAME, trailX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, trailY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, trailZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, 2, 0.05, 0.05, 0.05, 0.01, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, trailX, trailY + 0.3, trailZ, 1, 0.1, 0.1, 0.1, 0.01, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.7F, 1.2F);
         }
      }

      private void deidaraStartC3(EntityLivingBase target) {
         this.deidaraC3CD = this.cdMul(500);
         this.deidaraC3Charging = true;
         this.deidaraC3ChargeTimer = 40;
         this.deidaraC3TargetX = target.posX;
         this.deidaraC3TargetY = target.posY;
         this.deidaraC3TargetZ = target.posZ;
         this.broadcastChat((double)50.0F, "§7[§eDeidara§7] §6§lArt is an EXPLOSION!");
         if (this.world instanceof WorldServer) {
            for(int i = 0; i < 25; ++i) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)1.0F + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, 1, (double)0.0F, 0.1, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void deidaraDetonateC3() {
         double dmgMul = this.getDamageMultiplier();
         float c3Total = 60.0F * (float)dmgMul;
         float normDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normDmg = c3Total * (1.0F - this.trueDamageSplit);
            trueDmg = c3Total * this.trueDamageSplit * this.trueDamageMultiplier;
         } else {
            normDmg = c3Total;
            trueDmg = 10.0F * this.trueDamageMultiplier;
         }

         List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.deidaraC3TargetX - (double)10.0F, this.deidaraC3TargetY - (double)5.0F, this.deidaraC3TargetZ - (double)10.0F, this.deidaraC3TargetX + (double)10.0F, this.deidaraC3TargetY + (double)10.0F, this.deidaraC3TargetZ + (double)10.0F), (px) -> px != null && px.isEntityAlive() && !px.isSpectator() && !px.isCreative());

         for(EntityPlayer p : nearby) {
            double pDist = p.getDistance(this.deidaraC3TargetX, this.deidaraC3TargetY, this.deidaraC3TargetZ);
            float falloff = (float)Math.max(0.3, (double)1.0F - pDist / (double)12.0F);
            p.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg * falloff);
            p.hurtResistantTime = 0;
            p.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), trueDmg * falloff);
            p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
         }

         EntityClayBombC3.EntityCustom c3 = new EntityClayBombC3.EntityCustom(this.world);
         c3.setPosition(this.deidaraC3TargetX, this.deidaraC3TargetY + (double)5.0F, this.deidaraC3TargetZ);
         c3.setOwner(this);
         c3.setExplosionDamage(normDmg * 9.0F, trueDmg * 9.0F, 40.0F);
         this.world.spawnEntity(c3);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.deidaraC3TargetX, this.deidaraC3TargetY + (double)2.0F, this.deidaraC3TargetZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);

            for(int i = 0; i < 8; ++i) {
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.deidaraC3TargetX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, this.deidaraC3TargetY + this.rand.nextDouble() * (double)4.0F, this.deidaraC3TargetZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < 60; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.deidaraC3TargetX + (this.rand.nextDouble() - (double)0.5F) * (double)12.0F, this.deidaraC3TargetY + this.rand.nextDouble() * (double)8.0F, this.deidaraC3TargetZ + (this.rand.nextDouble() - (double)0.5F) * (double)12.0F, 1, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.3, 0.03, new int[0]);
            }

            for(int i = 0; i < 30; ++i) {
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.deidaraC3TargetX + (this.rand.nextDouble() - (double)0.5F) * (double)8.0F, this.deidaraC3TargetY + this.rand.nextDouble() * (double)5.0F, this.deidaraC3TargetZ + (this.rand.nextDouble() - (double)0.5F) * (double)8.0F, 1, (double)0.0F, (double)0.0F, (double)0.0F, 0.02, new int[0]);
            }
         }

         for(EntityPlayer p : nearby) {
            if (p instanceof EntityPlayerMP) {
               SPacketTitle timesPacket = new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 20, 10);
               SPacketTitle subtitlePacket = new SPacketTitle(Type.SUBTITLE, new TextComponentString("§c§lKATSU!!"));
               SPacketTitle titlePacket = new SPacketTitle(Type.TITLE, new TextComponentString(""));
               ((EntityPlayerMP)p).connection.sendPacket(timesPacket);
               ((EntityPlayerMP)p).connection.sendPacket(subtitlePacket);
               ((EntityPlayerMP)p).connection.sendPacket(titlePacket);
            }
         }

         this.world.playSound((EntityPlayer)null, this.deidaraC3TargetX, this.deidaraC3TargetY, this.deidaraC3TargetZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.5F);
      }

      private void deidaraEvasiveJump(EntityLivingBase target) {
         this.deidaraEvasiveCD = this.cdMul(80);
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         double jumpX;
         double jumpZ;
         if (d > (double)0.0F) {
            double sideAngle = this.rand.nextBoolean() ? (Math.PI / 4D) : (-Math.PI / 4D);
            jumpX = dx / d * Math.cos(sideAngle) - dz / d * Math.sin(sideAngle);
            jumpZ = dx / d * Math.sin(sideAngle) + dz / d * Math.cos(sideAngle);
         } else {
            jumpX = this.rand.nextDouble() - (double)0.5F;
            jumpZ = this.rand.nextDouble() - (double)0.5F;
         }

         this.motionX = jumpX * 1.8;
         this.motionY = 0.8;
         this.motionZ = jumpZ * 1.8;
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            for(int i = 0; i < 15; ++i) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void deidaraPlaceMine(EntityLivingBase target) {
         this.deidaraMineCD = this.cdMul(100);
         this.deidaraActiveMines.add(new double[]{target.posX, target.posY, target.posZ});

         while(this.deidaraActiveMines.size() > 6) {
            this.deidaraActiveMines.remove(0);
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, target.posX, target.posY + 0.2, target.posZ, 2, 0.1, 0.02, 0.1, (double)0.0F, new int[0]);
         }

      }

      private void deidaraCheckMines() {
         if (!this.deidaraActiveMines.isEmpty()) {
            Iterator<double[]> it = this.deidaraActiveMines.iterator();

            while(it.hasNext()) {
               double[] mine = (double[])it.next();
               List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(mine[0] - (double)3.0F, mine[1] - (double)2.0F, mine[2] - (double)3.0F, mine[0] + (double)3.0F, mine[1] + (double)3.0F, mine[2] + (double)3.0F), (px) -> px != null && px.isEntityAlive() && !px.isSpectator() && !px.isCreative());
               if (!nearby.isEmpty()) {
                  double dmgMul = this.getDamageMultiplier();
                  float mineTotal = 26.0F * (float)dmgMul;
                  float normDmg;
                  float trueDmg;
                  if (this.trueDamageSplit > 0.0F) {
                     normDmg = mineTotal * (1.0F - this.trueDamageSplit);
                     trueDmg = mineTotal * this.trueDamageSplit * this.trueDamageMultiplier;
                  } else {
                     normDmg = mineTotal;
                     trueDmg = 4.0F * this.trueDamageMultiplier;
                  }

                  for(EntityPlayer p : nearby) {
                     double pDist = p.getDistance(mine[0], mine[1], mine[2]);
                     float falloff = (float)Math.max(0.4, (double)1.0F - pDist / (double)4.0F);
                     p.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg * falloff);
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), trueDmg * falloff);
                  }

                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, mine[0], mine[1] + (double)1.0F, mine[2], 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);

                     for(int i = 0; i < 15; ++i) {
                        ws.spawnParticle(EnumParticleTypes.CLOUD, mine[0] + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, mine[1] + this.rand.nextDouble() * (double)2.0F, mine[2] + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, 1, (double)0.0F, 0.05, (double)0.0F, 0.01, new int[0]);
                     }

                     ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, mine[0], mine[1] + (double)0.5F, mine[2], 5, (double)0.5F, 0.3, (double)0.5F, 0.02, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, mine[0], mine[1], mine[2], SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.8F, 1.2F);
                  it.remove();
               }
            }

         }
      }

      private void deidaraMelee(EntityLivingBase target) {
         this.deidaraMeleeCD = 20 + this.rand.nextInt(10);
         double dmgMul = this.getDamageMultiplier();
         float meleeTotal = 17.0F * (float)dmgMul;
         float meleeDmg;
         float meleeTrueDmg;
         if (this.trueDamageSplit > 0.0F) {
            meleeDmg = meleeTotal * (1.0F - this.trueDamageSplit);
            meleeTrueDmg = meleeTotal * this.trueDamageSplit * this.trueDamageMultiplier;
         } else {
            meleeDmg = meleeTotal;
            meleeTrueDmg = 3.0F * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), meleeDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), meleeTrueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > (double)0.0F) {
            target.motionX += dx / d * 0.6;
            target.motionY += (double)0.25F;
            target.motionZ += dz / d * 0.6;
            target.velocityChanged = true;
         }

         this.meleeCooldown = this.deidaraMeleeCD;
      }
   }
}
