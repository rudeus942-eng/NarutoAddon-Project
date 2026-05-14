
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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySasukeShippuden extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 269;

   public EntitySasukeShippuden(ElementsInfTsukAddon instance) {
      super(instance, 269);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sasukeshippuden"), 269).name("sasukeshippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SasukeShippudenRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SasukeShippudenRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/sasuke_kid.png");

      public SasukeShippudenRenderer(RenderManager renderManager) {
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
         int phase = entity.getPhaseForRender();
         if (phase >= 2) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            float blueIntensity = phase >= 3 ? 0.4F : 0.2F;
            float r = 1.0F - blueIntensity * 0.5F;
            float g = 1.0F - blueIntensity * 0.2F;
            float b = 1.0F;
            float a = alpha < 1.0F ? alpha : 1.0F;
            GlStateManager.color(r, g, b, a);
            if (alpha < 1.0F) {
               GlStateManager.depthMask(false);
            }

            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            if (alpha < 1.0F) {
               GlStateManager.depthMask(true);
            }

            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else if (alpha < 1.0F) {
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
      private int phase = 1;
      private int attackTick = 0;
      private boolean introPlayed = false;
      private int meleeCD = 0;
      private int chidoriCD = 0;
      private int chidoriStreamCD = 0;
      private int chidoriSenCD = 0;
      private int chidoriSpearCD = 0;
      private int kirinChargeCD = 0;
      private int fireballCD = 0;
      private int snakeSummonCD = 0;
      private int sharinganDodgeCD = 0;
      private int flickerCD = 0;
      private int comboStep = 0;
      private int comboDelay = 0;
      private EntityLivingBase comboTarget = null;
      private int fireballBurstRemaining = 0;
      private int fireballBurstDelay = 0;
      private EntityLivingBase fireballBurstTarget = null;
      private boolean kirinCharging = false;
      private int kirinChargeTicks = 0;
      private boolean kirinUsed = false;
      private List<Entity> spawnedSnakes = new ArrayList();
      private static final UUID PHASE3_SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
      private boolean phase3SpeedApplied = false;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;

      public EntityCustom(World world) {
         super(world);
      }

      public int getPhaseForRender() {
         return this.phase;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.introPlayed) {
               this.introPlayed = true;
               this.sasukeSay("§9You again... Don't waste my time.");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.3, new int[0]);
               }
            }

            ++this.attackTick;
            if (this.kirinCharging) {
               this.processKirinCharge();
            } else if (this.comboStep <= 0 || this.comboTarget == null) {
               this.spawnedSnakes.removeIf((e) -> e == null || !e.isEntityAlive() || e.isDead);
               double dmgMul = this.getDamageMultiplier();
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float cdMul;
               switch (this.phase) {
                  case 2:
                     cdMul = 0.7F;
                     break;
                  case 3:
                     cdMul = 0.5F;
                     break;
                  default:
                     cdMul = 1.0F;
               }

               if (this.phase >= 4 && this.ticksExisted % 30 == 0) {
                  float auraNormal = (float)(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 0.05);
                  float auraTrue = auraNormal * 0.5F;

                  for(int i = 0; i < 3; ++i) {
                     EntityRaitonBeam.EntityCustom bolt = new EntityRaitonBeam.EntityCustom(this.world, this, auraNormal, auraTrue);
                     double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                     bolt.setPosition(this.posX, this.posY + (double)1.0F, this.posZ);
                     bolt.shoot(Math.cos(angle), 0.3 + this.rand.nextDouble() * 0.3, Math.sin(angle), 0.5F, 5.0F);
                     this.world.spawnEntity(bolt);
                  }

                  for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)3.5F))) {
                     if (p.isEntityAlive() && !p.isSpectator()) {
                        float auraDmg = (float)(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 0.1);
                        p.attackEntityFrom(DamageSource.MAGIC, auraDmg);
                        p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 0));
                     }
                  }
               }

               if (this.phase >= 3 && !this.kirinUsed && this.getHealth() / this.getMaxHealth() < 0.25F) {
                  this.kirinCharging = true;
                  this.kirinChargeTicks = 60;
                  this.kirinUsed = true;
                  this.sasukeSay("§b§lDisappear with the thunder...");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)2.0F, this.posZ, 40, (double)2.0F, (double)3.0F, (double)2.0F, 0.2, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 1.5F, 0.5F);
               } else if (this.phase == 3 && this.chidoriSpearCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.18F) {
                  int[] cd = this.getCooldownRange((int)(100.0F * cdMul), (int)(140.0F * cdMul));
                  this.chidoriSpearCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float spearNormal = baseDmg * 2.0F * (float)dmgMul;
                  float spearTrue = baseDmg * 2.0F;
                  EntityRaitonBeam.EntityCustom beam = new EntityRaitonBeam.EntityCustom(this.world, this, spearNormal, spearTrue);
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
                  double dz = target.posZ - this.posZ;
                  beam.shoot(dx, dy, dz, 2.0F, 0.5F);
                  this.world.spawnEntity(beam);
                  this.sasukeSay("§bChidori Sharp Spear!");
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.8F, 1.5F);
                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.phase >= 2 && this.chidoriStreamCD <= 0 && dist <= (double)5.0F && this.rand.nextFloat() < 0.2F) {
                  int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(160.0F * cdMul));
                  this.chidoriStreamCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float streamDmg = baseDmg * 1.0F;
                  float beamNormal = streamDmg * 0.3F * (float)dmgMul;
                  float beamTrue = streamDmg * 0.15F;

                  for(int i = 0; i < 6; ++i) {
                     EntityRaitonBeam.EntityCustom beam = new EntityRaitonBeam.EntityCustom(this.world, this, beamNormal, beamTrue);
                     double angle = (Math.PI / 3D) * (double)i;
                     double bx = Math.cos(angle);
                     double bz = Math.sin(angle);
                     beam.setPosition(this.posX + bx * (double)0.5F, this.posY + (double)1.0F, this.posZ + bz * (double)0.5F);
                     beam.shoot(bx, 0.05, bz, 0.8F, 2.0F);
                     this.world.spawnEntity(beam);
                  }

                  for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)4.0F))) {
                     if (p.isEntityAlive() && !p.isSpectator()) {
                        float normalPart = streamDmg * 0.3F * (float)dmgMul;
                        float truePart = streamDmg * 0.2F;
                        p.attackEntityFrom(DamageSource.causeMobDamage(this), normalPart);
                        p.hurtResistantTime = 0;
                        p.attackEntityFrom(DamageSource.MAGIC, truePart);
                        p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
                     }
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 1.0F, 1.2F);
                  this.sasukeSay("§bChidori Stream!");
                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.phase >= 2 && this.chidoriSenCD <= 0 && dist >= (double)4.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.16F) {
                  int[] cd = this.getCooldownRange((int)(150.0F * cdMul), (int)(200.0F * cdMul));
                  this.chidoriSenCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float senNormal = baseDmg * 0.4F * (float)dmgMul;
                  float senTrue = baseDmg * 0.15F;
                  int needleCount = 6 + this.rand.nextInt(3);
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
                  double dz = target.posZ - this.posZ;

                  for(int i = 0; i < needleCount; ++i) {
                     EntityRaitonBeam.EntityCustom needle = new EntityRaitonBeam.EntityCustom(this.world, this, senNormal, senTrue);
                     needle.shoot(dx, dy, dz, 1.5F, 3.0F);
                     this.world.spawnEntity(needle);
                  }

                  this.sasukeSay("§bChidori Senbon!");
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.5F);
                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.fireballBurstRemaining > 0 && this.fireballBurstTarget != null && this.fireballBurstTarget.isEntityAlive()) {
                  --this.fireballBurstDelay;
                  if (this.fireballBurstDelay <= 0) {
                     this.fireballBurstDelay = 10;
                     --this.fireballBurstRemaining;
                     float fireNormal = baseDmg * 0.7F * (float)dmgMul;
                     float fireTrue = baseDmg * 0.35F;
                     EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(this.world, this, fireNormal, fireTrue);
                     double dx = this.fireballBurstTarget.posX - this.posX;
                     double dy = this.fireballBurstTarget.posY + (double)this.fireballBurstTarget.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
                     double dz = this.fireballBurstTarget.posZ - this.posZ;
                     double horizDist = Math.sqrt(dx * dx + dz * dz);
                     if (horizDist < 0.1) {
                        horizDist = 0.1;
                     }

                     if (this.fireballBurstRemaining == 2) {
                        fb.shoot(dx, dy, dz, 1.3F, 1.0F);
                     } else if (this.fireballBurstRemaining == 1) {
                        double perpX = -dz / horizDist * (double)6.0F;
                        double perpZ = dx / horizDist * (double)6.0F;
                        fb.shoot(dx + perpX, dy, dz + perpZ, 1.1F, 1.0F);
                     } else {
                        double perpX = dz / horizDist * (double)6.0F;
                        double perpZ = -dx / horizDist * (double)6.0F;
                        fb.shoot(dx + perpX, dy, dz + perpZ, 1.1F, 1.0F);
                     }

                     this.world.spawnEntity(fb);
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 1.2, this.posZ, 10, 0.3, 0.3, 0.3, 0.1, new int[0]);
                     }

                     this.swingArm(EnumHand.MAIN_HAND);
                  }

               } else if (this.phase >= 2 && this.fireballCD <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.14F) {
                  int[] cd = this.getCooldownRange((int)(200.0F * cdMul), (int)(260.0F * cdMul));
                  this.fireballCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.fireballBurstRemaining = 3;
                  this.fireballBurstDelay = 0;
                  this.fireballBurstTarget = target;
                  this.sasukeSay("§cFire Style... Phoenix Flower Jutsu!");
               } else if (this.chidoriCD <= 0 && dist >= (double)3.0F && dist <= (double)6.0F && this.rand.nextFloat() < 0.22F) {
                  int[] cd = this.getCooldownRange((int)(80.0F * cdMul), (int)(120.0F * cdMul));
                  this.chidoriCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     double handX = this.posX - Math.sin(Math.toRadians((double)this.rotationYaw)) * 0.4;
                     double handY = this.posY + 0.85;
                     double handZ = this.posZ + Math.cos(Math.toRadians((double)this.rotationYaw)) * 0.4;
                     ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, handX, handY, handZ, 12, 0.08, 0.08, 0.08, 0.02, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, handX, handY, handZ, 8, 0.05, 0.1, 0.05, 0.01, new int[0]);
                  }

                  float tYaw = target.rotationYaw * ((float)Math.PI / 180F);
                  double dashX = target.posX + Math.sin((double)tYaw) * (double)1.5F;
                  double dashZ = target.posZ - Math.cos((double)tYaw) * (double)1.5F;
                  double dashY = this.findSafeY(dashX, target.posY, dashZ);
                  if (this.world.isBlockLoaded(new BlockPos(dashX, dashY, dashZ)) && this.isPositionSafe(dashX, dashY, dashZ)) {
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
                     }

                     this.internalReposition = true;
                     this.setPositionAndUpdate(dashX, dashY, dashZ);
                     this.internalReposition = false;
                     if (this.world instanceof WorldServer) {
                        WorldServer ws = (WorldServer)this.world;
                        ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + 0.9, this.posZ, 20, 0.15, 0.2, 0.15, 0.06, new int[0]);
                        ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.3, 0.3, 0.3, 0.1, new int[0]);
                     }
                  }

                  float chidoriTotal = baseDmg * 1.5F * (float)dmgMul;
                  float chidoriNormal = chidoriTotal * 0.3F;
                  float chidoriTrue = chidoriTotal * 0.7F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), chidoriNormal);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, chidoriTrue);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.6F, 1.8F);
                  this.sasukeSay("§bChidori!");
               } else if (this.flickerCD <= 0 && dist >= (double)3.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.12F) {
                  int[] cd = this.getCooldownRange((int)(60.0F * cdMul), (int)(90.0F * cdMul));
                  this.flickerCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float targetYaw = (float)Math.atan2(target.posX - this.posX, this.posZ - target.posZ);
                  double sideAngle = (double)targetYaw + (this.rand.nextBoolean() ? (Math.PI / 2D) : (-Math.PI / 2D));
                  double flickerDist = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
                  double flickX = target.posX + Math.sin(sideAngle) * flickerDist;
                  double flickZ = target.posZ - Math.cos(sideAngle) * flickerDist;
                  double flickY = this.findSafeY(flickX, target.posY, flickZ);
                  if (this.world.isBlockLoaded(new BlockPos(flickX, flickY, flickZ)) && this.isPositionSafe(flickX, flickY, flickZ)) {
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.3, 0.4, 0.3, 0.05, new int[0]);
                     }

                     this.internalReposition = true;
                     this.setPositionAndUpdate(flickX, flickY, flickZ);
                     this.internalReposition = false;
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 8, 0.2, 0.3, 0.2, 0.04, new int[0]);
                     }

                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.6F, 1.5F);
                  }

               } else if (this.meleeCD <= 0 && dist <= (double)3.0F && this.rand.nextFloat() < 0.3F) {
                  int[] cd = this.getCooldownRange((int)(35.0F * cdMul), (int)(50.0F * cdMul));
                  this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float hit1Normal = baseDmg * 0.7F * (float)dmgMul;
                  float hit1True = baseDmg * 0.15F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), hit1Normal);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, hit1True);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.2F);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.2, 0.3, 0.2, 0.05, new int[0]);
                  }

                  this.comboStep = 1;
                  this.comboDelay = 5;
                  this.comboTarget = target;
               } else {
                  if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     float normalDmg;
                     float trueDmg;
                     if (this.trueDamageSplit > 0.0F) {
                        normalDmg = baseDmg * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                        trueDmg = baseDmg * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
                     } else {
                        normalDmg = baseDmg * (float)dmgMul;
                        trueDmg = 8.0F * this.trueDamageMultiplier;
                     }

                     target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     this.swingArm(EnumHand.MAIN_HAND);
                     int[] cd = this.getCooldownRange((int)(15.0F * cdMul), (int)(20.0F * cdMul));
                     this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  }

               }
            }
         }
      }

      private void processKirinCharge() {
         --this.kirinChargeTicks;
         if (this.kirinChargeTicks % 5 == 0 && this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)2.0F, this.posZ, 30, (double)2.0F, (double)3.0F, (double)2.0F, 0.1, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)3.0F, this.posZ, 15, (double)1.5F, (double)2.0F, (double)1.5F, 0.15, new int[0]);
         }

         if (this.kirinChargeTicks % 20 == 0) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.8F, 0.6F);
         }

         this.motionX = (double)0.0F;
         this.motionZ = (double)0.0F;
         this.velocityChanged = true;
         if (this.kirinChargeTicks <= 0) {
            this.kirinCharging = false;
            float kirinDmg = (float)(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * (double)3.0F);
            EntityPlayer kirinTarget = this.world.getClosestPlayerToEntity(this, (double)15.0F);
            double targetX = kirinTarget != null ? kirinTarget.posX : this.posX;
            double targetY = kirinTarget != null ? kirinTarget.posY : this.posY;
            double targetZ = kirinTarget != null ? kirinTarget.posZ : this.posZ;
            EntityKirinProjectile.EntityCustom kirin = new EntityKirinProjectile.EntityCustom(this.world, targetX, targetY + (double)30.0F, targetZ, kirinDmg * 0.2F, kirinDmg * 0.8F);
            kirin.setTargetPos(targetX, targetY, targetZ);
            this.world.spawnEntity(kirin);
            this.world.addWeatherEffect(new EntityLightningBolt(this.world, targetX, targetY, targetZ, true));
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, targetX, targetY + (double)30.0F, targetZ, 60, (double)4.0F, (double)3.0F, (double)4.0F, 0.3, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, targetX, targetY + (double)25.0F, targetZ, 40, (double)3.0F, (double)5.0F, (double)3.0F, 0.4, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, targetX, targetY, targetZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 3.0F, 0.4F);
            this.sasukeSay("§b§lKirin!");
         }

      }

      private void processSasukeComboHit() {
         if (this.comboTarget != null && this.comboTarget.isEntityAlive() && !(this.getDistanceSq(this.comboTarget) > (double)25.0F)) {
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            if (this.comboStep == 1) {
               float thrustNormal = baseDmg * 0.8F * (float)dmgMul;
               float thrustTrue = baseDmg * 0.2F;
               this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), thrustNormal);
               this.comboTarget.hurtResistantTime = 0;
               this.comboTarget.attackEntityFrom(DamageSource.MAGIC, thrustTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.0F);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 6, 0.2, 0.3, 0.2, 0.06, new int[0]);
               }

               this.comboStep = 2;
               this.comboDelay = 5;
            } else if (this.comboStep == 2) {
               float heavyNormal = baseDmg * 1.2F * (float)dmgMul;
               float heavyTrue = baseDmg * 0.4F;
               this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), heavyNormal);
               this.comboTarget.hurtResistantTime = 0;
               this.comboTarget.attackEntityFrom(DamageSource.MAGIC, heavyTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               double kbX = this.comboTarget.posX - this.posX;
               double kbZ = this.comboTarget.posZ - this.posZ;
               double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
               if (kbDist > 0.01) {
                  EntityLivingBase var10000 = this.comboTarget;
                  var10000.motionX += kbX / kbDist * 0.7;
                  var10000 = this.comboTarget;
                  var10000.motionY += (double)0.25F;
                  var10000 = this.comboTarget;
                  var10000.motionZ += kbZ / kbDist * 0.7;
                  this.comboTarget.velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.8F);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 20, (double)0.5F, 0.6, (double)0.5F, 0.15, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 10, 0.3, 0.4, 0.3, 0.1, new int[0]);
               }

               this.comboStep = 0;
               this.comboTarget = null;
            }

         } else {
            this.comboStep = 0;
            this.comboTarget = null;
         }
      }

      private void checkPhaseTransition() {
         float healthRatio = this.getHealth() / this.getMaxHealth();
         if (this.phase == 1 && healthRatio <= 0.7F) {
            this.phase = 2;
            if (!this.phase2Announced) {
               this.phase2Announced = true;
               this.sasukeSay("§bYou're weaker than I expected. Let me show you real power.");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.5F, this.posZ, 50, (double)2.0F, (double)2.0F, (double)2.0F, 0.3, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.5F, (double)1.5F, (double)1.5F, 0.15, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }
         }

         if (this.phase == 2 && healthRatio <= 0.4F) {
            this.phase = 3;
            if (!this.phase3Announced) {
               this.phase3Announced = true;
               this.sasukeSay("§4You're all... beneath me.");
               if (!this.phase3SpeedApplied) {
                  this.phase3SpeedApplied = true;
                  IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                  AttributeModifier speedMod = new AttributeModifier(PHASE3_SPEED_UUID, "Sasuke Phase 3 Speed", 0.4, 2);
                  if (speedAttr.getModifier(PHASE3_SPEED_UUID) == null) {
                     speedAttr.applyModifier(speedMod);
                  }
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.5F, this.posZ, 80, (double)3.0F, (double)3.0F, (double)3.0F, 0.4, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)2.0F, this.posZ, 40, (double)2.0F, (double)2.0F, (double)2.0F, 0.2, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 3, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 1.5F, 0.7F);
            }
         }

         if (this.phase == 3 && healthRatio <= 0.2F) {
            this.phase = 4;
            this.sasukeSay("§4§lYou still won't give up? Then I'll show you the power of the Uchiha!");
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.5F, this.posZ, 100, (double)3.0F, (double)3.0F, (double)3.0F, (double)0.5F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.5F, (double)1.5F, (double)1.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 2.0F, 0.5F);
            this.kirinUsed = false;
         }

         if (this.phase == 4 && healthRatio <= 0.1F) {
            this.phase = 5;
            this.sasukeSay("§4§l§nThis ends NOW!");
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)2.0F, this.posZ, 150, (double)4.0F, (double)4.0F, (double)4.0F, 0.8, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 5, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.5F, 0.3F);
         }

      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && this.sharinganDodgeCD <= 0 && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) <= (double)6.0F) {
            float dodgeChance;
            switch (this.phase) {
               case 2:
                  dodgeChance = 0.35F;
                  break;
               case 3:
                  dodgeChance = 0.5F;
                  break;
               case 4:
                  dodgeChance = 0.55F;
                  break;
               case 5:
                  dodgeChance = 0.65F;
                  break;
               default:
                  dodgeChance = 0.25F;
            }

            if (this.rand.nextFloat() < dodgeChance) {
               EntityLivingBase attacker = (EntityLivingBase)trueSource;
               float aYaw = attacker.rotationYaw * ((float)Math.PI / 180F);
               double behindX = attacker.posX + Math.sin((double)aYaw) * (double)2.0F;
               double behindZ = attacker.posZ - Math.cos((double)aYaw) * (double)2.0F;
               double behindY = this.findSafeY(behindX, this.posY, behindZ);
               if (this.world.isBlockLoaded(new BlockPos(behindX, behindY, behindZ)) && this.isPositionSafe(behindX, behindY, behindZ)) {
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
                  }

                  this.internalReposition = true;
                  this.setPositionAndUpdate(behindX, behindY, behindZ);
                  this.internalReposition = false;
               }

               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float counterNormal = baseDmg * 0.8F * (float)this.getDamageMultiplier();
               float counterTrue = baseDmg * 0.3F;
               attacker.attackEntityFrom(DamageSource.causeMobDamage(this), counterNormal);
               attacker.hurtResistantTime = 0;
               attacker.attackEntityFrom(DamageSource.MAGIC, counterTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.sharinganDodgeCD = 30;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.15, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.5F);
               if (attacker instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)attacker).sendMessage(new TextComponentString("§c§o* Sasuke reads your movements with the Sharingan... *"));
               }

               return -1.0F;
            }
         }

         if (this.kirinCharging) {
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.velocityChanged = true;
         }

         this.checkPhaseTransition();
         return amount;
      }

      protected void tickStyleCooldowns() {
         if (this.meleeCD > 0) {
            --this.meleeCD;
         }

         if (this.chidoriCD > 0) {
            --this.chidoriCD;
         }

         if (this.chidoriStreamCD > 0) {
            --this.chidoriStreamCD;
         }

         if (this.chidoriSenCD > 0) {
            --this.chidoriSenCD;
         }

         if (this.chidoriSpearCD > 0) {
            --this.chidoriSpearCD;
         }

         if (this.kirinChargeCD > 0) {
            --this.kirinChargeCD;
         }

         if (this.fireballCD > 0) {
            --this.fireballCD;
         }

         if (this.snakeSummonCD > 0) {
            --this.snakeSummonCD;
         }

         if (this.sharinganDodgeCD > 0) {
            --this.sharinganDodgeCD;
         }

         if (this.flickerCD > 0) {
            --this.flickerCD;
         }

         if (this.comboStep > 0 && this.comboTarget != null) {
            if (this.comboDelay > 0) {
               --this.comboDelay;
            } else {
               this.processSasukeComboHit();
            }
         }

         if (this.phase >= 2 && this.world instanceof WorldServer && this.ticksExisted % 3 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * 1.2, this.posY + (double)0.5F + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 1.2, 3, 0.2, 0.3, 0.2, 0.08, new int[0]);
         }

         if (this.phase >= 3 && this.world instanceof WorldServer && this.ticksExisted % 2 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 2, 0.1, 0.2, 0.1, 0.03, new int[0]);
         }

      }

      protected void resetCombatState() {
         this.phase = 1;
         this.attackTick = 0;
         this.introPlayed = false;
         this.meleeCD = 0;
         this.chidoriCD = 0;
         this.chidoriStreamCD = 0;
         this.chidoriSenCD = 0;
         this.chidoriSpearCD = 0;
         this.kirinChargeCD = 0;
         this.fireballCD = 0;
         this.snakeSummonCD = 0;
         this.sharinganDodgeCD = 0;
         this.flickerCD = 0;
         this.comboStep = 0;
         this.comboDelay = 0;
         this.comboTarget = null;
         this.kirinCharging = false;
         this.kirinChargeTicks = 0;
         this.kirinUsed = false;
         this.phase2Announced = false;
         this.phase3Announced = false;
         this.phase3SpeedApplied = false;
         IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         if (speedAttr != null) {
            speedAttr.removeModifier(PHASE3_SPEED_UUID);
         }

      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("sasukePhase", this.phase);
         compound.setBoolean("sasukeIntroPlayed", this.introPlayed);
         compound.setBoolean("sasukeKirinUsed", this.kirinUsed);
         compound.setBoolean("sasukeKirinCharging", this.kirinCharging);
         compound.setInteger("sasukeKirinChargeTicks", this.kirinChargeTicks);
         compound.setBoolean("sasukePhase2Announced", this.phase2Announced);
         compound.setBoolean("sasukePhase3Announced", this.phase3Announced);
         compound.setBoolean("sasukePhase3SpeedApplied", this.phase3SpeedApplied);
         compound.setInteger("sasukeMeleeCD", this.meleeCD);
         compound.setInteger("sasukeChidoriCD", this.chidoriCD);
         compound.setInteger("sasukeChidoriStreamCD", this.chidoriStreamCD);
         compound.setInteger("sasukeChidoriSenCD", this.chidoriSenCD);
         compound.setInteger("sasukeChidoriSpearCD", this.chidoriSpearCD);
         compound.setInteger("sasukeFireballCD", this.fireballCD);
         compound.setInteger("sasukeSnakeSummonCD", this.snakeSummonCD);
         compound.setInteger("sasukeSharinganDodgeCD", this.sharinganDodgeCD);
         compound.setInteger("sasukeFlickerCD", this.flickerCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.phase = compound.hasKey("sasukePhase") ? compound.getInteger("sasukePhase") : 1;
         this.introPlayed = compound.getBoolean("sasukeIntroPlayed");
         this.kirinUsed = compound.getBoolean("sasukeKirinUsed");
         this.kirinCharging = compound.getBoolean("sasukeKirinCharging");
         this.kirinChargeTicks = compound.hasKey("sasukeKirinChargeTicks") ? compound.getInteger("sasukeKirinChargeTicks") : 0;
         this.phase2Announced = compound.getBoolean("sasukePhase2Announced");
         this.phase3Announced = compound.getBoolean("sasukePhase3Announced");
         this.phase3SpeedApplied = compound.getBoolean("sasukePhase3SpeedApplied");
         this.meleeCD = compound.hasKey("sasukeMeleeCD") ? compound.getInteger("sasukeMeleeCD") : 0;
         this.chidoriCD = compound.hasKey("sasukeChidoriCD") ? compound.getInteger("sasukeChidoriCD") : 0;
         this.chidoriStreamCD = compound.hasKey("sasukeChidoriStreamCD") ? compound.getInteger("sasukeChidoriStreamCD") : 0;
         this.chidoriSenCD = compound.hasKey("sasukeChidoriSenCD") ? compound.getInteger("sasukeChidoriSenCD") : 0;
         this.chidoriSpearCD = compound.hasKey("sasukeChidoriSpearCD") ? compound.getInteger("sasukeChidoriSpearCD") : 0;
         this.fireballCD = compound.hasKey("sasukeFireballCD") ? compound.getInteger("sasukeFireballCD") : 0;
         this.snakeSummonCD = compound.hasKey("sasukeSnakeSummonCD") ? compound.getInteger("sasukeSnakeSummonCD") : 0;
         this.sharinganDodgeCD = compound.hasKey("sasukeSharinganDodgeCD") ? compound.getInteger("sasukeSharinganDodgeCD") : 0;
         this.flickerCD = compound.hasKey("sasukeFlickerCD") ? compound.getInteger("sasukeFlickerCD") : 0;
         if (this.phase3SpeedApplied) {
            IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.getModifier(PHASE3_SPEED_UUID) == null) {
               speedAttr.applyModifier(new AttributeModifier(PHASE3_SPEED_UUID, "Sasuke Phase 3 Speed", 0.4, 2));
            }
         }

      }

      protected void onCombatDeath() {
         for(Entity snake : this.spawnedSnakes) {
            if (snake != null && snake.isEntityAlive()) {
               snake.setDead();
            }
         }

         this.spawnedSnakes.clear();
         IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         if (speedAttr != null) {
            speedAttr.removeModifier(PHASE3_SPEED_UUID);
         }

         this.sasukeSay("§7Hmph. You're still not worth my time.");
      }

      private void sasukeSay(String msg) {
         if (this.chatCooldown <= 0) {
            String name = this.getCustomNameTag();
            if (name == null || name.isEmpty()) {
               name = "Sasuke";
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
               p.sendMessage(new TextComponentString("§9" + name + ": " + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
