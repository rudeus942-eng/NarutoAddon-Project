
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySoundNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 234;

   public EntitySoundNPC(ElementsInfTsukAddon instance) {
      super(instance, 234);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "soundnpc"), 234).name("soundnpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SoundNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SoundNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SoundNpcRenderer(RenderManager renderManager) {
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

      protected void renderModel(EntityCustom entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
         float alpha = this.getGhostAlpha(entity);
         if (alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GlStateManager.color(0.7F, 0.85F, 1.0F, alpha);
            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
         }

      }

      private float getGhostAlpha(EntityCustom entity) {
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               return config.getGhostAlpha();
            }
         }

         return 1.0F;
      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private static final int SOUND_DOSU = 0;
      private static final int SOUND_KIN = 1;
      private static final int SOUND_ZAKU = 2;
      private int soundSubtype = -1;
      private boolean soundPhase2 = false;
      private int soundAbility1CD = 0;
      private int soundAbility2CD = 0;
      private int soundAbility3CD = 0;
      private int soundDashCD = 0;
      private int soundNauseaGlobalCD = 0;
      private int soundWindup = 0;
      private int soundWindupType = 0;
      private Vec3d soundWindupPos = null;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (this.soundWindup <= 0) {
               double dmgMul = this.getDamageMultiplier();
               float cdMul = this.soundPhase2 ? 0.75F : 1.0F;
               switch (this.soundSubtype) {
                  case 0:
                     this.processSoundDosu(target, dist, dmgMul, cdMul);
                     break;
                  case 1:
                     this.processSoundKin(target, dist, dmgMul, cdMul);
                     break;
                  case 2:
                     this.processSoundZaku(target, dist, dmgMul, cdMul);
                     break;
                  default:
                     if (this.meleeCooldown <= 0 && dist <= (double)2.5F) {
                        this.performMeleeSwing(target);
                     }
               }

            } else {
               --this.soundWindup;
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  if (this.soundWindupType == 1 && this.soundWindupPos != null) {
                     ws.spawnParticle(EnumParticleTypes.NOTE, this.soundWindupPos.x, this.soundWindupPos.y + (double)1.0F, this.soundWindupPos.z, 3, (double)2.0F, (double)1.0F, (double)2.0F, (double)0.5F, new int[0]);
                  } else if (this.soundWindupType == 2) {
                     ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)0.5F, this.posZ, 5, (double)0.5F, (double)1.0F, (double)0.5F, 0.3, new int[0]);
                  } else if (this.soundWindupType == 3) {
                     ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.5F, this.posZ, 5, (double)1.5F, (double)0.5F, (double)1.5F, 0.8, new int[0]);
                  } else if (this.soundWindupType == 4) {
                     ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 4, 0.3, 0.8, 0.3, 0.05, new int[0]);
                  } else if (this.soundWindupType == 5) {
                     ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 3, (double)0.5F, (double)0.5F, (double)0.5F, 0.3, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.4, 0.6, 0.4, 0.05, new int[0]);
                  }
               }

               if (this.soundWindup <= 0) {
                  this.executeSoundWindup(target);
               }

            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.soundAbility1CD > 0) {
            --this.soundAbility1CD;
         }

         if (this.soundAbility2CD > 0) {
            --this.soundAbility2CD;
         }

         if (this.soundAbility3CD > 0) {
            --this.soundAbility3CD;
         }

         if (this.soundDashCD > 0) {
            --this.soundDashCD;
         }

         if (this.soundNauseaGlobalCD > 0) {
            --this.soundNauseaGlobalCD;
         }

      }

      protected void resetCombatState() {
         this.soundPhase2 = false;
         this.soundAbility1CD = 0;
         this.soundAbility2CD = 0;
         this.soundAbility3CD = 0;
         this.soundDashCD = 0;
         this.soundNauseaGlobalCD = 0;
         this.soundWindup = 0;
         this.soundWindupType = 0;
         this.soundWindupPos = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("soundSubtype", this.soundSubtype);
         compound.setBoolean("soundPhase2", this.soundPhase2);
         compound.setInteger("soundAbility1CD", this.soundAbility1CD);
         compound.setInteger("soundAbility2CD", this.soundAbility2CD);
         compound.setInteger("soundAbility3CD", this.soundAbility3CD);
         compound.setInteger("soundDashCD", this.soundDashCD);
         compound.setInteger("soundNauseaGlobalCD", this.soundNauseaGlobalCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.soundSubtype = compound.hasKey("soundSubtype") ? compound.getInteger("soundSubtype") : -1;
         this.soundPhase2 = compound.getBoolean("soundPhase2");
         this.soundAbility1CD = compound.hasKey("soundAbility1CD") ? compound.getInteger("soundAbility1CD") : 0;
         this.soundAbility2CD = compound.hasKey("soundAbility2CD") ? compound.getInteger("soundAbility2CD") : 0;
         this.soundAbility3CD = compound.hasKey("soundAbility3CD") ? compound.getInteger("soundAbility3CD") : 0;
         this.soundDashCD = compound.hasKey("soundDashCD") ? compound.getInteger("soundDashCD") : 0;
         this.soundNauseaGlobalCD = compound.hasKey("soundNauseaGlobalCD") ? compound.getInteger("soundNauseaGlobalCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && this.soundSubtype == 1 && this.soundAbility2CD <= 0 && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) < (double)5.0F && this.rand.nextFloat() < 0.35F) {
            EntityLivingBase attacker = (EntityLivingBase)trueSource;
            double dx = this.posX - attacker.posX;
            double dz = this.posZ - attacker.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.1) {
               len = (double)1.0F;
            }

            double backX = this.posX + dx / len * (double)4.0F;
            double backZ = this.posZ + dz / len * (double)4.0F;
            if (this.world.isBlockLoaded(new BlockPos(backX, this.posY, backZ)) && this.isPositionSafe(backX, this.posY, backZ) && !this.isEnclosed(backX, this.posY, backZ)) {
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.6F, 1.3F);
               this.internalReposition = true;
               this.setPositionAndUpdate(backX, this.posY, backZ);
               this.internalReposition = false;
               this.soundAbility2CD = 80;
            }
         }

         if (!this.world.isRemote && !this.soundPhase2 && this.getHealth() / this.getMaxHealth() < 0.4F) {
            this.soundPhase2 = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)1.5F, this.posZ, 20, 0.8, 0.8, 0.8, 0.1, new int[0]);
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§4§l* " + this.getCustomNameTag() + " grows desperate! *"));
            }
         }

         return amount;
      }

      public void applyNpcConfig(NpcConfig config) {
         super.applyNpcConfig(config);
         String cid = config.getConfigId();
         if (cid.contains("dosu")) {
            this.soundSubtype = 0;
         } else if (cid.contains("kin")) {
            this.soundSubtype = 1;
         } else if (cid.contains("zaku")) {
            this.soundSubtype = 2;
         } else {
            this.soundSubtype = 2;
         }

      }

      private void processSoundDosu(EntityLivingBase target, double dist, double dmgMul, float cdMul) {
         if (this.soundAbility2CD <= 0 && dist <= (double)8.0F && this.rand.nextFloat() < 0.2F) {
            int[] cd = this.getCooldownRange((int)(200.0F * cdMul), (int)(280.0F * cdMul));
            this.soundAbility2CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.soundWindup = 30;
            this.soundWindupType = 2;
            this.soundWindupPos = new Vec3d(this.posX, this.posY, this.posZ);
            this.soundSay("§5Resonating Echo!");
         } else if (this.soundAbility1CD <= 0 && dist <= (double)12.0F && this.rand.nextFloat() < 0.25F) {
            int[] cd = this.getCooldownRange((int)(100.0F * cdMul), (int)(160.0F * cdMul));
            this.soundAbility1CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.soundWindup = 25;
            this.soundWindupType = 1;
            this.soundWindupPos = new Vec3d(target.posX, target.posY, target.posZ);
            this.soundSay("§dEcho Drill!");
         } else if (this.soundAbility3CD <= 0 && dist <= (double)10.0F && dist >= (double)3.0F && this.rand.nextFloat() < 0.3F) {
            int[] cd = this.getCooldownRange((int)(60.0F * cdMul), (int)(100.0F * cdMul));
            this.soundAbility3CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            float normalDmg = this.jutsuPower * 0.6F * (float)dmgMul;
            float trueDmg = 1.0F;

            for(int i = 0; i < 3; ++i) {
               EntityFutonBullet.EntityCustom wind = new EntityFutonBullet.EntityCustom(this.world, this, normalDmg, trueDmg);
               double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
               double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
               double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
               double d = Math.sqrt(dx * dx + dz * dz);
               wind.shoot(dx, dy + d * 0.01, dz, 0.9F, 3.0F);
               this.world.spawnEntity(wind);
            }

            this.swingArm(EnumHand.MAIN_HAND);
         } else if (this.soundDashCD <= 0 && !this.isDashing && dist >= (double)6.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.15F) {
            int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(180.0F * cdMul));
            this.soundDashCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.startDashStrike(target);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void processSoundKin(EntityLivingBase target, double dist, double dmgMul, float cdMul) {
         if (this.soundAbility2CD <= 0 && dist <= (double)12.0F && this.rand.nextFloat() < 0.18F) {
            int[] cd = this.getCooldownRange((int)(140.0F * cdMul), (int)(200.0F * cdMul));
            this.soundAbility2CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.soundWindup = 25;
            this.soundWindupType = 3;
            this.soundWindupPos = new Vec3d(this.posX, this.posY, this.posZ);
            this.soundSay("§d*rings bells*");
         } else if (this.soundAbility3CD <= 0 && dist <= (double)15.0F && dist >= (double)5.0F && this.rand.nextFloat() < 0.2F) {
            int[] cd = this.getCooldownRange((int)(160.0F * cdMul), (int)(240.0F * cdMul));
            this.soundAbility3CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            float normalDmg = 5.0F * (float)dmgMul;
            float trueDmg = 1.5F;

            for(int i = 0; i < 5; ++i) {
               EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, normalDmg, trueDmg);
               double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
               double dy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY + (this.rand.nextDouble() - (double)0.5F);
               double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
               double d = Math.sqrt(dx * dx + dz * dz);
               kunai.shoot(dx, dy + d * 0.15, dz, this.kunaiSpeed, this.kunaiInaccuracy + 1.0F);
               this.world.spawnEntity(kunai);
            }

            this.swingArm(EnumHand.MAIN_HAND);
            this.soundSay("§dSenbon Barrage!");
         } else if (this.soundAbility1CD <= 0 && dist <= (double)20.0F && dist >= (double)5.0F && this.rand.nextFloat() < 0.3F) {
            int[] cd = this.getCooldownRange((int)(50.0F * cdMul), (int)(80.0F * cdMul));
            this.soundAbility1CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.throwKunai(target);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void processSoundZaku(EntityLivingBase target, double dist, double dmgMul, float cdMul) {
         if (this.soundAbility3CD <= 0 && dist <= (double)12.0F && this.rand.nextFloat() < 0.12F) {
            int[] cd = this.getCooldownRange((int)(220.0F * cdMul), (int)(300.0F * cdMul));
            this.soundAbility3CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.soundWindup = 35;
            this.soundWindupType = 5;
            this.soundWindupPos = new Vec3d(this.posX, this.posY, this.posZ);
            this.soundSay("§4§lExtreme Decapitating Airwaves!");
         } else if (this.soundAbility2CD <= 0 && dist <= (double)8.0F && this.rand.nextFloat() < 0.18F) {
            int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(180.0F * cdMul));
            this.soundAbility2CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.soundWindup = 20;
            this.soundWindupType = 4;
            this.soundWindupPos = new Vec3d(this.posX, this.posY, this.posZ);
            this.soundSay("§cSupersonic Slicing Wave!");
         } else if (this.soundAbility1CD <= 0 && dist <= (double)15.0F && dist >= (double)2.0F && this.rand.nextFloat() < 0.25F) {
            int[] cd = this.getCooldownRange((int)(70.0F * cdMul), (int)(110.0F * cdMul));
            this.soundAbility1CD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.executeSoundCone(target, dmgMul);
         } else if (this.soundDashCD <= 0 && !this.isDashing && dist >= (double)6.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.15F) {
            int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(180.0F * cdMul));
            this.soundDashCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.startDashStrike(target);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

            if (dist > (double)2.5F && dist < (double)6.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, 1.4);
            }

         }
      }

      private void executeSoundWindup(EntityLivingBase target) {
         if (!this.world.isRemote) {
            double dmgMul = this.getDamageMultiplier();
            switch (this.soundWindupType) {
               case 1:
                  if (this.soundWindupPos != null) {
                     float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                     float normalDmg = baseDmg * 0.4F * (float)dmgMul;
                     float trueDmg = baseDmg * 0.2F;

                     for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.soundWindupPos.x - (double)4.0F, this.soundWindupPos.y - (double)1.0F, this.soundWindupPos.z - (double)4.0F, this.soundWindupPos.x + (double)4.0F, this.soundWindupPos.y + (double)3.0F, this.soundWindupPos.z + (double)4.0F))) {
                        if (e != this && !(e instanceof EntityCustom)) {
                           e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                           e.hurtResistantTime = 0;
                           e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                           if (this.soundNauseaGlobalCD <= 0) {
                              e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0));
                           }

                           e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
                        }
                     }

                     if (this.soundNauseaGlobalCD <= 0) {
                        this.soundNauseaGlobalCD = 2400;
                     }

                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.NOTE, this.soundWindupPos.x, this.soundWindupPos.y + (double)1.0F, this.soundWindupPos.z, 30, (double)4.0F, (double)2.0F, (double)4.0F, 0.8, new int[0]);
                     }

                     this.world.playSound((EntityPlayer)null, this.soundWindupPos.x, this.soundWindupPos.y, this.soundWindupPos.z, SoundEvents.BLOCK_NOTE_BASS, SoundCategory.HOSTILE, 2.0F, 0.5F);
                  }
                  break;
               case 2:
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  float normalDmg = baseDmg * 0.5F * (float)dmgMul;
                  float trueDmg = baseDmg * 0.3F;

                  for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)8.0F))) {
                     if (e != this && !(e instanceof EntityCustom)) {
                        e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                        e.hurtResistantTime = 0;
                        e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        if (this.soundNauseaGlobalCD <= 0) {
                           e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
                        }

                        e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                        e.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
                     }
                  }

                  if (this.soundNauseaGlobalCD <= 0) {
                     this.soundNauseaGlobalCD = 2400;
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.NOTE, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)8.0F, (double)3.0F, (double)8.0F, (double)1.0F, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)5.0F, (double)2.0F, (double)5.0F, (double)0.5F, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_NOTE_BASS, SoundCategory.HOSTILE, 3.0F, 0.3F);
                  break;
               case 3:
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  float normalDmg = baseDmg * 0.3F * (float)dmgMul;
                  float trueDmg = baseDmg * 0.2F;

                  for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)10.0F))) {
                     if (e != this && !(e instanceof EntityCustom)) {
                        e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                        e.hurtResistantTime = 0;
                        e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        e.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
                        if (this.soundNauseaGlobalCD <= 0) {
                           e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
                        }

                        e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 0));
                     }
                  }

                  if (this.soundNauseaGlobalCD <= 0) {
                     this.soundNauseaGlobalCD = 2400;
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)8.0F, (double)2.0F, (double)8.0F, (double)1.0F, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_NOTE_BELL, SoundCategory.HOSTILE, 2.0F, 0.3F);
                  break;
               case 4:
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  float normalDmg = baseDmg * 0.5F * (float)dmgMul;
                  float trueDmg = baseDmg * 0.3F;

                  for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)8.0F))) {
                     if (e != this && !(e instanceof EntityCustom)) {
                        e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                        e.hurtResistantTime = 0;
                        e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        double kbx = e.posX - this.posX;
                        double kbz = e.posZ - this.posZ;
                        double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                        if (kblen > 0.1) {
                           e.motionX += kbx / kblen * 1.8;
                           e.motionY += 0.3;
                           e.motionZ += kbz / kblen * 1.8;
                           e.velocityChanged = true;
                        }
                     }
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 10, (double)4.0F, (double)2.0F, (double)4.0F, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.0F);
                  break;
               case 5:
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  float normalDmg = baseDmg * 0.6F * (float)dmgMul;
                  float trueDmg = baseDmg * 0.35F;

                  for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)12.0F))) {
                     if (e != this && !(e instanceof EntityCustom)) {
                        e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                        e.hurtResistantTime = 0;
                        e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        double kbx = e.posX - this.posX;
                        double kbz = e.posZ - this.posZ;
                        double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                        if (kblen > 0.1) {
                           e.motionX += kbx / kblen * (double)2.5F;
                           e.motionY += 0.4;
                           e.motionZ += kbz / kblen * (double)2.5F;
                           e.velocityChanged = true;
                        }
                     }
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)1.0F, this.posZ, 5, (double)6.0F, (double)3.0F, (double)6.0F, 0.1, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)10.0F, (double)3.0F, (double)10.0F, 0.3, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.7F);
            }

            this.soundWindupType = 0;
            this.soundWindupPos = null;
         }
      }

      private void executeSoundCone(EntityLivingBase target, double dmgMul) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float normalDmg = baseDmg * 0.4F * (float)dmgMul;
         float trueDmg = baseDmg * 0.25F;
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double aimAngle = Math.atan2(dz, dx);

         for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)15.0F))) {
            if (e != this && !(e instanceof EntityCustom)) {
               double ex = e.posX - this.posX;
               double ez = e.posZ - this.posZ;
               double entityAngle = Math.atan2(ez, ex);
               double angleDiff = Math.abs(Math.atan2(Math.sin(entityAngle - aimAngle), Math.cos(entityAngle - aimAngle)));
               if (angleDiff <= Math.toRadians((double)30.0F)) {
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double kblen = Math.sqrt(ex * ex + ez * ez);
                  if (kblen > 0.1) {
                     e.motionX += ex / kblen * (double)1.0F;
                     e.motionY += 0.2;
                     e.motionZ += ez / kblen * (double)1.0F;
                     e.velocityChanged = true;
                  }
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 20; ++i) {
               double cDist = (double)2.0F + this.rand.nextDouble() * (double)13.0F;
               double angle = aimAngle + (this.rand.nextDouble() - (double)0.5F) * Math.toRadians((double)60.0F);
               double px = this.posX + Math.cos(angle) * cDist;
               double pz = this.posZ + Math.sin(angle) * cDist;
               ws.spawnParticle(EnumParticleTypes.CLOUD, px, this.posY + (double)0.5F + this.rand.nextDouble(), pz, 1, 0.2, 0.2, 0.2, 0.02, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.5F);
         this.swingArm(EnumHand.MAIN_HAND);
      }

      private void soundSay(String msg) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§c" + this.getCustomNameTag() + ": §f" + msg));
            }

            this.chatCooldown = 80;
         }
      }

      private void startDashStrike(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 10;
            double dashDist = Math.max(horizDist - (double)1.5F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.2;
            this.dashVelZ = nz * velocityPerTick * 1.2;
            this.dashArcSustain = 0.08F;
            this.dashFallAccel = 0.06F;
            this.dashMaxFall = -0.4F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.35;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + 0.8, this.posZ, 8, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            this.dashCooldown = 80 + this.rand.nextInt(60);
         }
      }
   }
}
