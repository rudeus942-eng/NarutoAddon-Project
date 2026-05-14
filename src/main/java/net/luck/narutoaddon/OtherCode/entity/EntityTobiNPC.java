
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
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
public class EntityTobiNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 324;

   public EntityTobiNPC(ElementsInfTsukAddon instance) {
      super(instance, 324);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "tobinpc"), 324).name("tobinpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, TobiNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class TobiNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public TobiNpcRenderer(RenderManager renderManager) {
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
         float alpha = 1.0F;
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               alpha = config.getGhostAlpha();
            }
         }

         if (alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GlStateManager.color(0.4F, 0.6F, 1.0F, alpha);
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
   }

   public static class EntityCustom extends QuestNpcBase {
      private int kamuiTeleportCooldown = 0;
      private int phaseDashCooldown = 0;
      private int fireJutsuCooldown = 0;
      private int meleeFollowupTimer = 0;
      private boolean tobiIntroPlayed = false;
      private boolean lowHpDialogPlayed = false;
      private static final int KAMUI_TELEPORT_BASE_CD = 120;
      private static final int PHASE_DASH_BASE_CD = 80;
      private static final int FIRE_JUTSU_BASE_CD = 200;
      private static final float KAMUI_DODGE_CHANCE = 0.25F;
      private static final float LOW_HP_THRESHOLD = 0.3F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processTobiCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.kamuiTeleportCooldown > 0) {
            --this.kamuiTeleportCooldown;
         }

         if (this.phaseDashCooldown > 0) {
            --this.phaseDashCooldown;
         }

         if (this.fireJutsuCooldown > 0) {
            --this.fireJutsuCooldown;
         }

         if (this.meleeFollowupTimer > 0) {
            --this.meleeFollowupTimer;
         }

      }

      protected void resetCombatState() {
         this.tobiIntroPlayed = false;
         this.lowHpDialogPlayed = false;
         this.kamuiTeleportCooldown = 0;
         this.phaseDashCooldown = 0;
         this.fireJutsuCooldown = 0;
         this.meleeFollowupTimer = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("tobiIntroPlayed", this.tobiIntroPlayed);
         compound.setBoolean("lowHpDialogPlayed", this.lowHpDialogPlayed);
         compound.setInteger("kamuiTeleportCooldown", this.kamuiTeleportCooldown);
         compound.setInteger("phaseDashCooldown", this.phaseDashCooldown);
         compound.setInteger("fireJutsuCooldown", this.fireJutsuCooldown);
         compound.setInteger("meleeFollowupTimer", this.meleeFollowupTimer);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.tobiIntroPlayed = compound.getBoolean("tobiIntroPlayed");
         this.lowHpDialogPlayed = compound.getBoolean("lowHpDialogPlayed");
         this.kamuiTeleportCooldown = compound.hasKey("kamuiTeleportCooldown") ? compound.getInteger("kamuiTeleportCooldown") : 0;
         this.phaseDashCooldown = compound.hasKey("phaseDashCooldown") ? compound.getInteger("phaseDashCooldown") : 0;
         this.fireJutsuCooldown = compound.hasKey("fireJutsuCooldown") ? compound.getInteger("fireJutsuCooldown") : 0;
         this.meleeFollowupTimer = compound.hasKey("meleeFollowupTimer") ? compound.getInteger("meleeFollowupTimer") : 0;
      }

      protected void onCombatDeath() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 20; ++i) {
               double angle = (double)i / (double)20.0F * Math.PI * (double)2.0F;
               double radius = (double)1.5F;
               double px = this.posX + Math.cos(angle) * radius;
               double pz = this.posZ + Math.sin(angle) * radius;
               ws.spawnParticle(EnumParticleTypes.PORTAL, px, this.posY + (double)1.0F, pz, 3, 0.1, 0.3, 0.1, 0.02, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.5F, 0.5F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (!this.world.isRemote && this.rand.nextFloat() < 0.25F) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double teleX = this.posX + Math.cos(angle) * (double)5.0F;
            double teleZ = this.posZ + Math.sin(angle) * (double)5.0F;
            double teleY = this.posY;

            for(int yOff = 0; yOff <= 3; ++yOff) {
               if (!this.world.isAirBlock(new BlockPos(teleX, teleY + (double)yOff - (double)1.0F, teleZ)) && this.world.isAirBlock(new BlockPos(teleX, teleY + (double)yOff, teleZ)) && this.world.isAirBlock(new BlockPos(teleX, teleY + (double)yOff + (double)1.0F, teleZ))) {
                  teleY += (double)yOff;
                  break;
               }
            }

            this.setPositionAndUpdate(teleX, teleY, teleZ);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.PORTAL, teleX, teleY + (double)1.0F, teleZ, 25, 0.4, 0.6, 0.4, 0.3, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, teleX, teleY + (double)1.0F, teleZ, 10, 0.3, 0.4, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, teleX, teleY, teleZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.5F);
            this.tobiBroadcast("§5Tobi: §dOops! Missed me~");
            return 0.0F;
         } else {
            return amount;
         }
      }

      private void processTobiCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (!this.tobiIntroPlayed) {
               this.tobiIntroPlayed = true;
               this.tobiBroadcast("§5Tobi: §dTobi is a good boy! But Tobi also likes to play rough~");
            }

            if (!this.lowHpDialogPlayed && hpPercent <= 0.3F) {
               this.lowHpDialogPlayed = true;
               this.tobiBroadcast("§5Tobi: §dHmm... you're actually pretty strong. Maybe Tobi should get serious...");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.6, (double)1.0F, 0.6, (double)0.5F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.5F, this.posZ, 20, 0.4, 0.6, 0.4, 0.08, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 0.6F, 1.2F);
            }

            if (this.ticksExisted % 8 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + 1.2, this.posZ, 3, 0.2, 0.4, 0.2, 0.1, new int[0]);
            }

            if (this.meleeFollowupTimer == 1 && dist <= (double)4.0F) {
               target.hurtResistantTime = 0;
               this.performMeleeSwing(target);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.2, 0.3, 0.2, 0.03, new int[0]);
               }
            }

            if (this.fireJutsuCooldown <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.3F) {
               this.fireKatonFireball(target);
               this.fireJutsuCooldown = this.cdMul(200);
            } else if (this.kamuiTeleportCooldown <= 0 && dist >= (double)4.0F && dist <= (double)30.0F && this.rand.nextFloat() < 0.35F) {
               this.kamuiTeleport(target);
               this.kamuiTeleportCooldown = this.cdMul(120);
            } else if (this.phaseDashCooldown <= 0 && dist >= (double)8.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.3F) {
               this.phaseDash(target, dist);
               this.phaseDashCooldown = this.cdMul(80);
            } else {
               double moveSpeed = hpPercent <= 0.3F ? (double)1.5F : 1.3;
               if (dist > (double)5.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
               } else if (dist > (double)2.5F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed * 0.9);
               }

               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)4.0F) {
                  target.hurtResistantTime = 0;
                  this.performMeleeSwing(target);
                  this.meleeFollowupTimer = 10;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 6, 0.2, 0.3, 0.2, 0.04, new int[0]);
                  }
               }

               if (this.natureType != 0 && this.jutsuCooldown <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.12F) {
                  this.startNatureJutsu(target);
                  this.jutsuCooldown = this.cdMul(100);
               }

            }
         }
      }

      private void kamuiTeleport(EntityLivingBase target) {
         double behindX = target.posX + Math.sin(Math.toRadians((double)target.rotationYaw)) * (double)2.0F;
         double behindZ = target.posZ - Math.cos(Math.toRadians((double)target.rotationYaw)) * (double)2.0F;
         double behindY = target.posY;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 16; ++i) {
               double angle = (double)i / (double)16.0F * Math.PI * (double)2.0F;
               double px = this.posX + Math.cos(angle) * 0.8;
               double pz = this.posZ + Math.sin(angle) * 0.8;
               ws.spawnParticle(EnumParticleTypes.PORTAL, px, this.posY + (double)1.0F, pz, 3, 0.05, 0.3, 0.05, (double)0.5F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.9F, 1.3F);
         this.setPositionAndUpdate(behindX, behindY, behindZ);
         double dx = target.posX - behindX;
         double dz = target.posZ - behindZ;
         float yaw = (float)(Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
         this.rotationYaw = yaw;
         this.rotationYawHead = yaw;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 16; ++i) {
               double angle = (double)i / (double)16.0F * Math.PI * (double)2.0F;
               double px = behindX + Math.cos(angle) * 0.8;
               double pz = behindZ + Math.sin(angle) * 0.8;
               ws.spawnParticle(EnumParticleTypes.PORTAL, px, behindY + (double)1.0F, pz, 3, 0.05, 0.3, 0.05, (double)0.5F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, behindX, behindY + (double)1.0F, behindZ, 15, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, behindX, behindY, behindZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.9F, 1.3F);
         double dmgMult = this.getDamageMultiplier();
         float burstDmg = 12.0F * (float)dmgMult;
         float trueDmg = (this.combatTier >= 4 ? 5.0F : (this.combatTier >= 3 ? 4.0F : 3.0F)) * this.trueDamageMultiplier;
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), burstDmg);
         if (trueDmg > 0.0F) {
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.rand.nextFloat() < 0.5F) {
            this.tobiBroadcast("§5Tobi: §dYou can't touch what isn't there~");
         }

      }

      private void phaseDash(EntityLivingBase target, double dist) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            double dashDist = horizDist + (double)3.0F;
            int duration = 6;
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.6;
            this.dashVelZ = nz * velocityPerTick * 1.6;
            this.dashArcSustain = 0.03F;
            this.dashFallAccel = 0.04F;
            this.dashMaxFall = -0.25F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.15;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 20, 0.2, 0.3, 0.2, 0.8, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, 0.4, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.4F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.5F, 1.8F);
            if (this.rand.nextFloat() < 0.4F) {
               this.tobiBroadcast("§5Tobi: §dCatch me if you can!");
            }

         }
      }

      private void fireKatonFireball(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 10.0F * (float)dmgMult;
         float trueDmg = (this.combatTier >= 4 ? 5.0F : (this.combatTier >= 3 ? 4.0F : 3.0F)) * this.trueDamageMultiplier;
         EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - fireball.posY;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         fireball.shoot(dx, dy + horizDist * 0.05, dz, 1.4F, 1.5F);
         this.world.spawnEntity(fireball);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.5F, this.posZ, 12, 0.3, 0.3, 0.3, 0.05, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 1.2, this.posZ, 6, 0.2, 0.3, 0.2, 0.02, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0F, 1.2F);
         if (this.rand.nextFloat() < 0.4F) {
            this.tobiBroadcast("§5Tobi: §dFire Style!");
         }

      }

      private void tobiBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
