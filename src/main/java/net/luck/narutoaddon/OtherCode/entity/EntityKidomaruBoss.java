
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
public class EntityKidomaruBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 241;

   public EntityKidomaruBoss(ElementsInfTsukAddon instance) {
      super(instance, 241);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kidomaruboss"), 241).name("kidomaruboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KidomaruBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KidomaruBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public KidomaruBossRenderer(RenderManager renderManager) {
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
      private static final float KIDOMARU_PHASE_2 = 0.65F;
      private static final float KIDOMARU_PHASE_3 = 0.3F;
      private static final double KIDOMARU_BASE_HP = (double)9000.0F;
      private static final int KIDOMARU_PREFERRED_RANGE = 13;
      private static final int KIDOMARU_SNIPER_RANGE = 18;
      private static final int KIDOMARU_TOO_CLOSE = 5;
      private int kidomaruPhase = 1;
      private boolean kidomaruIntroPlayed = false;
      private int kidomaruWebShotCD = 0;
      private int kidomaruStickyGoldCD = 0;
      private int kidomaruWebTrapCD = 0;
      private int kidomaruWarBowCD = 0;
      private int kidomaruEvasiveLeapCD = 0;
      private int kidomaruSideDashCD = 0;
      private int kidomaruSprintDashCD = 0;
      private int kidomaruKunaiCD = 0;
      private double webTrapX = Double.NaN;
      private double webTrapY = Double.NaN;
      private double webTrapZ = Double.NaN;
      private int webTrapLifeTicks = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            this.kidomaruCheckPhaseTransition(hpPercent);
            if (!this.kidomaruIntroPlayed) {
               this.kidomaruIntroPlayed = true;
               this.kidomaruBroadcast("§6Kidomaru: §eHeh, this'll be fun. Let me figure out your pattern...");
            }

            this.kidomaruTickWebTrap();
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            double yDiff = target.posY - this.posY;
            boolean playerElevated = yDiff > (double)5.0F;
            if (playerElevated && !this.isDashing) {
               if (yDiff > (double)30.0F) {
                  this.internalReposition = true;
                  this.setPositionAndUpdate(target.posX, target.posY, target.posZ);
                  this.internalReposition = false;
               } else {
                  double ldx = target.posX - this.posX;
                  double ldz = target.posZ - this.posZ;
                  double hd = Math.sqrt(ldx * ldx + ldz * ldz);
                  if (hd > (double)0.5F) {
                     double nx = ldx / hd;
                     double nz = ldz / hd;
                     this.isDashing = true;
                     this.isLeaping = true;
                     this.dashType = 4;
                     this.dashDuration = 14;
                     this.dashTicksRemaining = 14;
                     this.dashVelX = nx * 0.6;
                     this.dashVelZ = nz * 0.6;
                     this.dashArcSustain = 0.0F;
                     this.dashFallAccel = 0.06F;
                     this.dashMaxFall = -1.0F;
                     this.dashHasMidairGuidance = true;
                     this.motionY = Math.min((double)2.5F, 0.7 + yDiff * 0.12);
                     this.velocityChanged = true;
                  }
               }
            }

            if (!playerElevated) {
               if (dist < (double)5.0F && this.kidomaruEvasiveLeapCD <= 0 && !this.isDashing) {
                  this.kidomaruEvasiveLeap(target);
                  this.kidomaruEvasiveLeapCD = (int)((float)(this.kidomaruPhase >= 2 ? 40 : 60) * this.configCooldownMultiplier);
                  return;
               }

               if (this.kidomaruPhase == 3 && dist < (double)15.0F && this.kidomaruSprintDashCD <= 0 && !this.isDashing) {
                  this.kidomaruSprintDash(target);
                  this.kidomaruSprintDashCD = (int)(60.0F * this.configCooldownMultiplier);
                  return;
               }

               if (dist >= (double)5.0F && dist <= (double)20.0F && this.kidomaruSideDashCD <= 0 && !this.isDashing) {
                  this.kidomaruSideDash(target);
                  this.kidomaruSideDashCD = (int)((float)(this.kidomaruPhase >= 2 ? 50 : 70) * this.configCooldownMultiplier);
               }

               int preferredRange = this.kidomaruPhase == 3 ? 18 : 13;
               if (dist < (double)(preferredRange - 3)) {
                  this.kidomaruRetreat(target);
               } else if (dist > (double)(preferredRange + 8)) {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.3);
               } else {
                  double strafeX = this.posX + (this.rand.nextBoolean() ? (double)3.0F : (double)-3.0F);
                  double strafeZ = this.posZ + (this.rand.nextBoolean() ? (double)3.0F : (double)-3.0F);
                  this.getNavigator().tryMoveToXYZ(strafeX, this.posY, strafeZ, (double)1.0F);
               }
            } else if (!this.isDashing) {
               this.getNavigator().tryMoveToEntityLiving(target, 1.4);
            }

            if (this.kidomaruPhase == 3 && this.kidomaruWarBowCD <= 0 && dist >= (double)8.0F && dist <= (double)30.0F) {
               this.kidomaruCastWarBow(target);
               this.kidomaruWarBowCD = (int)(80.0F * this.configCooldownMultiplier);
            } else if (dist < (double)6.0F && this.kidomaruStickyGoldCD <= 0) {
               this.kidomaruCastStickyGold(target);
               this.kidomaruStickyGoldCD = (int)((float)(this.kidomaruPhase >= 2 ? 100 : 140) * this.configCooldownMultiplier);
            } else if (this.kidomaruPhase >= 2 && this.kidomaruWebTrapCD <= 0 && dist >= (double)5.0F && dist <= (double)20.0F) {
               this.kidomaruCastWebTrap(target);
               this.kidomaruWebTrapCD = (int)((float)(this.kidomaruPhase == 3 ? 120 : 160) * this.configCooldownMultiplier);
            } else if (this.kidomaruWebShotCD <= 0 && dist >= (double)4.0F && dist <= (double)25.0F) {
               this.kidomaruCastWebShot(target);
               this.kidomaruWebShotCD = (int)((float)(this.kidomaruPhase == 3 ? 50 : (this.kidomaruPhase == 2 ? 70 : 100)) * this.configCooldownMultiplier);
            } else {
               if (this.kidomaruKunaiCD <= 0 && dist >= (double)3.0F && dist <= (double)22.0F) {
                  this.kidomaruCastKunaiBarrage(target);
                  this.kidomaruKunaiCD = (int)((float)(this.kidomaruPhase == 3 ? 30 : (this.kidomaruPhase == 2 ? 40 : 55)) * this.configCooldownMultiplier);
               }

            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.kidomaruWebShotCD > 0) {
            --this.kidomaruWebShotCD;
         }

         if (this.kidomaruStickyGoldCD > 0) {
            --this.kidomaruStickyGoldCD;
         }

         if (this.kidomaruWebTrapCD > 0) {
            --this.kidomaruWebTrapCD;
         }

         if (this.kidomaruWarBowCD > 0) {
            --this.kidomaruWarBowCD;
         }

         if (this.kidomaruEvasiveLeapCD > 0) {
            --this.kidomaruEvasiveLeapCD;
         }

         if (this.kidomaruSideDashCD > 0) {
            --this.kidomaruSideDashCD;
         }

         if (this.kidomaruSprintDashCD > 0) {
            --this.kidomaruSprintDashCD;
         }

         if (this.kidomaruKunaiCD > 0) {
            --this.kidomaruKunaiCD;
         }

      }

      protected void resetCombatState() {
         this.kidomaruPhase = 1;
         this.kidomaruIntroPlayed = false;
         this.kidomaruWebShotCD = 0;
         this.kidomaruStickyGoldCD = 0;
         this.kidomaruWebTrapCD = 0;
         this.kidomaruWarBowCD = 0;
         this.kidomaruEvasiveLeapCD = 0;
         this.kidomaruSideDashCD = 0;
         this.kidomaruSprintDashCD = 0;
         this.kidomaruKunaiCD = 0;
         this.webTrapX = Double.NaN;
         this.webTrapY = Double.NaN;
         this.webTrapZ = Double.NaN;
         this.webTrapLifeTicks = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("kidomaruPhase", this.kidomaruPhase);
         compound.setBoolean("kidomaruIntroPlayed", this.kidomaruIntroPlayed);
         compound.setInteger("kidomaruWebShotCD", this.kidomaruWebShotCD);
         compound.setInteger("kidomaruStickyGoldCD", this.kidomaruStickyGoldCD);
         compound.setInteger("kidomaruWebTrapCD", this.kidomaruWebTrapCD);
         compound.setInteger("kidomaruWarBowCD", this.kidomaruWarBowCD);
         compound.setInteger("kidomaruEvasiveLeapCD", this.kidomaruEvasiveLeapCD);
         compound.setInteger("kidomaruSideDashCD", this.kidomaruSideDashCD);
         compound.setInteger("kidomaruSprintDashCD", this.kidomaruSprintDashCD);
         compound.setInteger("kidomaruKunaiCD", this.kidomaruKunaiCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.kidomaruPhase = compound.hasKey("kidomaruPhase") ? compound.getInteger("kidomaruPhase") : 1;
         this.kidomaruIntroPlayed = compound.getBoolean("kidomaruIntroPlayed");
         this.kidomaruWebShotCD = compound.hasKey("kidomaruWebShotCD") ? compound.getInteger("kidomaruWebShotCD") : 0;
         this.kidomaruStickyGoldCD = compound.hasKey("kidomaruStickyGoldCD") ? compound.getInteger("kidomaruStickyGoldCD") : 0;
         this.kidomaruWebTrapCD = compound.hasKey("kidomaruWebTrapCD") ? compound.getInteger("kidomaruWebTrapCD") : 0;
         this.kidomaruWarBowCD = compound.hasKey("kidomaruWarBowCD") ? compound.getInteger("kidomaruWarBowCD") : 0;
         this.kidomaruEvasiveLeapCD = compound.hasKey("kidomaruEvasiveLeapCD") ? compound.getInteger("kidomaruEvasiveLeapCD") : 0;
         this.kidomaruSideDashCD = compound.hasKey("kidomaruSideDashCD") ? compound.getInteger("kidomaruSideDashCD") : 0;
         this.kidomaruSprintDashCD = compound.hasKey("kidomaruSprintDashCD") ? compound.getInteger("kidomaruSprintDashCD") : 0;
         this.kidomaruKunaiCD = compound.hasKey("kidomaruKunaiCD") ? compound.getInteger("kidomaruKunaiCD") : 0;
      }

      private void kidomaruCheckPhaseTransition(float hpPercent) {
         if (this.kidomaruPhase == 1 && hpPercent <= 0.65F) {
            this.kidomaruPhase = 2;
            this.kidomaruBroadcast("§6Kidomaru: §eYou're more durable than I thought... Let me get serious.");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.42);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
               double cur = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(cur + (double)4.0F);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }

         if (this.kidomaruPhase == 2 && hpPercent <= 0.3F) {
            this.kidomaruPhase = 3;
            this.kidomaruBroadcast("§4§lKidomaru: I'll end this with one shot!");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
               double cur = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(cur + (double)6.0F);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.6, (double)1.0F, 0.6, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 20, 0.4, 0.6, 0.4, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.7F);
         }

      }

      private void kidomaruEvasiveLeap(EntityLivingBase target) {
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len < 0.1) {
            dx = (double)1.0F;
            dz = (double)0.0F;
            len = (double)1.0F;
         }

         double nx = dx / len;
         double nz = dz / len;
         double leapDist = this.kidomaruPhase == 3 ? (double)15.0F : (double)8.0F + this.rand.nextDouble() * (double)2.0F;
         int dur = this.kidomaruPhase == 3 ? 14 : 10;
         double velPT = leapDist / (double)dur;
         this.isDashing = true;
         this.isLeaping = true;
         this.dashType = 3;
         this.dashDuration = dur;
         this.dashTicksRemaining = dur;
         this.dashVelX = nx * velPT * 1.2;
         this.dashVelZ = nz * velPT * 1.2;
         this.dashArcSustain = 0.0F;
         this.dashFallAccel = 0.06F;
         this.dashMaxFall = -0.8F;
         this.dashHasMidairGuidance = false;
         this.motionY = this.kidomaruPhase == 3 ? 0.7 : 0.55;
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 12, (double)0.5F, 0.15, (double)0.5F, 0.03, new int[]{Block.getStateId(Blocks.GRASS.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.2F);
         if (this.kidomaruPhase >= 2 && this.kidomaruWebShotCD <= 0) {
            this.kidomaruCastWebShot(target);
            this.kidomaruWebShotCD = (int)(50.0F * this.configCooldownMultiplier);
         }

      }

      private void kidomaruSideDash(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (!(len < 0.1)) {
            double perpX;
            double perpZ;
            if (this.rand.nextBoolean()) {
               perpX = -dz / len;
               perpZ = dx / len;
            } else {
               perpX = dz / len;
               perpZ = -dx / len;
            }

            double dashDist = (double)5.0F + this.rand.nextDouble() * (double)3.0F;
            int dur = 8;
            double velPT = dashDist / (double)dur;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = dur;
            this.dashTicksRemaining = dur;
            this.dashVelX = perpX * velPT * 1.3;
            this.dashVelZ = perpZ * velPT * 1.3;
            this.dashArcSustain = 0.06F;
            this.dashFallAccel = 0.08F;
            this.dashMaxFall = -0.5F;
            this.dashHasMidairGuidance = false;
            this.motionY = (double)0.25F;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, 0.3, 0.3, 0.1, new int[0]);
            }

         }
      }

      private void kidomaruSprintDash(EntityLivingBase target) {
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len < 0.1) {
            dx = (double)1.0F;
            dz = (double)0.0F;
            len = (double)1.0F;
         }

         double nx = dx / len;
         double nz = dz / len;
         double lat = this.rand.nextBoolean() ? 0.3 : -0.3;
         double fnx = nx + -nz * lat;
         double fnz = nz + nx * lat;
         double fLen = Math.sqrt(fnx * fnx + fnz * fnz);
         fnx /= fLen;
         fnz /= fLen;
         double spDist = (double)10.0F + this.rand.nextDouble() * (double)5.0F;
         int dur = 10;
         double velPT = spDist / (double)dur;
         this.isDashing = true;
         this.dashType = 2;
         this.dashDuration = dur;
         this.dashTicksRemaining = dur;
         this.dashVelX = fnx * velPT * 1.2;
         this.dashVelZ = fnz * velPT * 1.2;
         this.dashArcSustain = 0.06F;
         this.dashFallAccel = 0.08F;
         this.dashMaxFall = -0.5F;
         this.dashHasMidairGuidance = false;
         this.motionY = 0.3;
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.3, 0.3, 0.3, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 0.8F, 1.4F);
      }

      private void kidomaruRetreat(EntityLivingBase target) {
         double distFromSpawn = this.getDistance(this.spawnOriginX, this.spawnOriginY, this.spawnOriginZ);
         if (!Double.isNaN(this.spawnOriginX) && distFromSpawn > (double)40.0F) {
            this.getNavigator().clearPath();
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
         } else {
            double fleeX = this.posX + (this.posX - target.posX) * 0.8;
            double fleeZ = this.posZ + (this.posZ - target.posZ) * 0.8;
            this.getNavigator().tryMoveToXYZ(fleeX, this.posY, fleeZ, (double)1.5F);
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
         }

      }

      private void kidomaruCastWebShot(EntityLivingBase target) {
         float atkDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float damage = atkDmg * 0.5F * (float)this.getDamageMultiplier();
         target.attackEntityFrom(DamageSource.MAGIC, damage);
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 1.2F, 1.5F);
         if (this.world instanceof WorldServer) {
            double ddx = target.posX - this.posX;
            double ddy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double ddz = target.posZ - this.posZ;

            for(int s = 1; s <= 5; ++s) {
               double t = (double)s / (double)5.0F;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + ddx * t, this.posY + (double)1.5F + ddy * t, this.posZ + ddz * t, 3, 0.1, 0.1, 0.1, 0.02, new int[0]);
            }

            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SNOWBALL, target.posX, target.posY + (double)1.0F, target.posZ, 12, 0.4, (double)0.5F, 0.4, 0.05, new int[0]);
         }

         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§7§o* Spider web wraps around your legs! *"));
         }

      }

      private void kidomaruCastStickyGold(EntityLivingBase target) {
         float atkDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float damage = atkDmg * 0.6F * (float)this.getDamageMultiplier();
         target.attackEntityFrom(DamageSource.MAGIC, damage);
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 2));
         target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 40, 1));
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_SLIME_PLACE, SoundCategory.HOSTILE, 1.5F, 0.6F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SLIME, target.posX, target.posY + (double)0.5F, target.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
         }

         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§6§o* Golden spider silk binds you! *"));
         }

      }

      private void kidomaruCastWebTrap(EntityLivingBase target) {
         double trapX = target.posX + target.motionX * (double)10.0F + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F;
         double trapZ = target.posZ + target.motionZ * (double)10.0F + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F;
         double trapY = this.findSafeY(trapX, target.posY, trapZ);
         this.webTrapX = trapX;
         this.webTrapY = trapY;
         this.webTrapZ = trapZ;
         this.webTrapLifeTicks = 100;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, trapX, trapY + 0.2, trapZ, 8, 0.3, 0.1, 0.3, 0.02, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 0.6F, 0.8F);
         this.kidomaruBroadcast("§6Kidomaru: §eI've laid my web... step carefully.");
      }

      private void kidomaruTickWebTrap() {
         if (!Double.isNaN(this.webTrapX) && this.webTrapLifeTicks > 0) {
            --this.webTrapLifeTicks;
            if (this.webTrapLifeTicks <= 0) {
               this.webTrapX = Double.NaN;
            } else {
               for(EntityPlayer p : this.world.playerEntities) {
                  double tdx = p.posX - this.webTrapX;
                  double tdy = p.posY - this.webTrapY;
                  double tdz = p.posZ - this.webTrapZ;
                  if (tdx * tdx + tdy * tdy + tdz * tdz < (double)4.0F) {
                     p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 4));
                     p.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 40, 128));
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SNOWBALL, p.posX, p.posY + (double)0.5F, p.posZ, 25, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, p.posX, p.posY + 0.3, p.posZ, 15, 0.4, 0.2, 0.4, 0.05, new int[0]);
                     }

                     this.world.playSound((EntityPlayer)null, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_SLIME_BREAK, SoundCategory.HOSTILE, 1.5F, 0.5F);
                     if (p instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)p).sendMessage(new TextComponentString("§c§o* You've stepped into Kidomaru's web trap! *"));
                     }

                     this.webTrapX = Double.NaN;
                     return;
                  }
               }

            }
         }
      }

      private void kidomaruCastWarBow(EntityLivingBase target) {
         float atkDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float damage = atkDmg * 1.5F * (float)this.getDamageMultiplier();
         target.attackEntityFrom(DamageSource.MAGIC, damage);
         double kbx = target.posX - this.posX;
         double kbz = target.posZ - this.posZ;
         double kbLen = Math.sqrt(kbx * kbx + kbz * kbz);
         if (kbLen > (double)0.0F) {
            target.motionX += kbx / kbLen * 0.8;
            target.motionY += 0.3;
            target.motionZ += kbz / kbLen * 0.8;
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).velocityChanged = true;
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 2.0F, 0.5F);
         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 1.2F);
         if (this.world instanceof WorldServer) {
            double ddx = target.posX - this.posX;
            double ddy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double ddz = target.posZ - this.posZ;

            for(int s = 1; s <= 8; ++s) {
               double t = (double)s / (double)8.0F;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + ddx * t, this.posY + (double)1.5F + ddy * t + Math.sin(t * Math.PI) * (double)2.0F, this.posZ + ddz * t, 5, 0.1, 0.1, 0.1, 0.05, new int[0]);
            }

            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, target.posX, target.posY + (double)1.0F, target.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, new int[0]);
         }

         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§4§l* Kidomaru's war bow arrow strikes with devastating force! *"));
         }

      }

      private void kidomaruCastKunaiBarrage(EntityLivingBase target) {
         float atkDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float damage = atkDmg * 0.35F * (float)this.getDamageMultiplier();
         int count = this.kidomaruPhase >= 2 ? 3 : 2;

         for(int i = 0; i < count; ++i) {
            target.attackEntityFrom(DamageSource.MAGIC, damage);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.4F);
         if (this.world instanceof WorldServer) {
            double ddx = target.posX - this.posX;
            double ddy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double ddz = target.posZ - this.posZ;

            for(int s = 1; s <= 4; ++s) {
               double t = (double)s / (double)4.0F;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + ddx * t, this.posY + (double)1.5F + ddy * t, this.posZ + ddz * t, 2, 0.15, 0.15, 0.15, 0.02, new int[0]);
            }
         }

      }

      private void kidomaruBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
