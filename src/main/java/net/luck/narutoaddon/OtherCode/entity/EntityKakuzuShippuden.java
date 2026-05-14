
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.jutsu.EntityWeightedBoulder;
import net.luck.narutoaddon.OtherCode.jutsu.EntityWindCutter;
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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKakuzuShippuden extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 276;

   public EntityKakuzuShippuden(ElementsInfTsukAddon instance) {
      super(instance, 951);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kakuzushippuden"), 276).name("kakuzushippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KakuzuShippudenRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KakuzuShippudenRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/kakuzu.png");

      public KakuzuShippudenRenderer(RenderManager renderManager) {
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
         int heart = entity.currentHeart;
         if (heart >= 2) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            float intensity = heart >= 5 ? 0.5F : 0.3F;
            float r;
            float g;
            float b;
            switch (heart) {
               case 2:
                  r = 0.8F;
                  g = 0.95F;
                  b = 1.0F;
                  break;
               case 3:
                  r = 1.0F;
                  g = 0.75F;
                  b = 0.5F;
                  break;
               case 4:
                  r = 0.5F;
                  g = 0.7F;
                  b = 1.0F;
                  break;
               case 5:
                  r = 0.4F;
                  g = 0.4F;
                  b = 0.4F;
                  break;
               default:
                  r = 1.0F;
                  g = 1.0F;
                  b = 1.0F;
            }

            float a = alpha < 1.0F ? alpha : 1.0F - intensity * 0.3F;
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
      int currentHeart = 1;
      private int attackTick = 0;
      private boolean introPlayed = false;
      private boolean heart2Announced = false;
      private boolean heart3Announced = false;
      private boolean heart4Announced = false;
      private boolean heart5Announced = false;
      private int heartInvulnTicks = 0;
      private int earthSlamCD = 0;
      private int boulderCD = 0;
      private int windBulletCD = 0;
      private int windPushCD = 0;
      private int fireballCD = 0;
      private int fireAoeCD = 0;
      private int waterBulletCD = 0;
      private int waterAoeCD = 0;
      private int threadPullCD = 0;
      private int meleeCD = 0;
      private int comboStep = 0;
      private int comboDelay = 0;
      private EntityLivingBase comboTarget = null;
      private static final int[] COMBO_DELAYS = new int[]{0, 4, 4, 5, 5, 8};

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.introPlayed) {
               this.introPlayed = true;
               this.kakuzuSay("§8I've lived long enough to know... everyone has a price.");
               if (!this.world.isRemote) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.05, 0.05, 0.05, new int[]{-13421773, 30});
               }
            }

            ++this.attackTick;
            if (this.comboStep > 0 && this.comboTarget != null) {
               this.processThreadCombo();
            } else if (this.heartInvulnTicks <= 0) {
               double dmgMul = this.getDamageMultiplier();
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               this.tickAmbientParticles();
               switch (this.currentHeart) {
                  case 1:
                     this.processEarthPhase(target, dist, baseDmg, dmgMul);
                     break;
                  case 2:
                     this.processWindPhase(target, dist, baseDmg, dmgMul);
                     break;
                  case 3:
                     this.processFirePhase(target, dist, baseDmg, dmgMul);
                     break;
                  case 4:
                     this.processWaterPhase(target, dist, baseDmg, dmgMul);
                     break;
                  case 5:
                     this.processThreadPhase(target, dist, baseDmg, dmgMul);
               }

            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.earthSlamCD > 0) {
            --this.earthSlamCD;
         }

         if (this.boulderCD > 0) {
            --this.boulderCD;
         }

         if (this.windBulletCD > 0) {
            --this.windBulletCD;
         }

         if (this.windPushCD > 0) {
            --this.windPushCD;
         }

         if (this.fireballCD > 0) {
            --this.fireballCD;
         }

         if (this.fireAoeCD > 0) {
            --this.fireAoeCD;
         }

         if (this.waterBulletCD > 0) {
            --this.waterBulletCD;
         }

         if (this.waterAoeCD > 0) {
            --this.waterAoeCD;
         }

         if (this.threadPullCD > 0) {
            --this.threadPullCD;
         }

         if (this.meleeCD > 0) {
            --this.meleeCD;
         }

         if (this.heartInvulnTicks > 0) {
            --this.heartInvulnTicks;
         }

         ++this.attackTick;
         if (this.comboStep > 0 && this.comboTarget != null && this.comboDelay > 0) {
            --this.comboDelay;
         }

      }

      protected void resetCombatState() {
         this.currentHeart = 1;
         this.attackTick = 0;
         this.introPlayed = false;
         this.heart2Announced = false;
         this.heart3Announced = false;
         this.heart4Announced = false;
         this.heart5Announced = false;
         this.heartInvulnTicks = 0;
         this.earthSlamCD = 0;
         this.boulderCD = 0;
         this.windBulletCD = 0;
         this.windPushCD = 0;
         this.fireballCD = 0;
         this.fireAoeCD = 0;
         this.waterBulletCD = 0;
         this.waterAoeCD = 0;
         this.threadPullCD = 0;
         this.meleeCD = 0;
         this.comboStep = 0;
         this.comboDelay = 0;
         this.comboTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("kakuzuHeart", this.currentHeart);
         compound.setInteger("kakuzuAttackTick", this.attackTick);
         compound.setBoolean("kakuzuIntro", this.introPlayed);
         compound.setBoolean("kakuzuH2", this.heart2Announced);
         compound.setBoolean("kakuzuH3", this.heart3Announced);
         compound.setBoolean("kakuzuH4", this.heart4Announced);
         compound.setBoolean("kakuzuH5", this.heart5Announced);
         compound.setInteger("kakuzuInvuln", this.heartInvulnTicks);
         compound.setInteger("kakuzuEarthCD", this.earthSlamCD);
         compound.setInteger("kakuzuBoulderCD", this.boulderCD);
         compound.setInteger("kakuzuWindBulletCD", this.windBulletCD);
         compound.setInteger("kakuzuWindPushCD", this.windPushCD);
         compound.setInteger("kakuzuFireballCD", this.fireballCD);
         compound.setInteger("kakuzuFireAoeCD", this.fireAoeCD);
         compound.setInteger("kakuzuWaterBulletCD", this.waterBulletCD);
         compound.setInteger("kakuzuWaterAoeCD", this.waterAoeCD);
         compound.setInteger("kakuzuThreadPullCD", this.threadPullCD);
         compound.setInteger("kakuzuMeleeCD", this.meleeCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.currentHeart = compound.hasKey("kakuzuHeart") ? compound.getInteger("kakuzuHeart") : 1;
         this.attackTick = compound.hasKey("kakuzuAttackTick") ? compound.getInteger("kakuzuAttackTick") : 0;
         this.introPlayed = compound.getBoolean("kakuzuIntro");
         this.heart2Announced = compound.getBoolean("kakuzuH2");
         this.heart3Announced = compound.getBoolean("kakuzuH3");
         this.heart4Announced = compound.getBoolean("kakuzuH4");
         this.heart5Announced = compound.getBoolean("kakuzuH5");
         this.heartInvulnTicks = compound.hasKey("kakuzuInvuln") ? compound.getInteger("kakuzuInvuln") : 0;
         this.earthSlamCD = compound.hasKey("kakuzuEarthCD") ? compound.getInteger("kakuzuEarthCD") : 0;
         this.boulderCD = compound.hasKey("kakuzuBoulderCD") ? compound.getInteger("kakuzuBoulderCD") : 0;
         this.windBulletCD = compound.hasKey("kakuzuWindBulletCD") ? compound.getInteger("kakuzuWindBulletCD") : 0;
         this.windPushCD = compound.hasKey("kakuzuWindPushCD") ? compound.getInteger("kakuzuWindPushCD") : 0;
         this.fireballCD = compound.hasKey("kakuzuFireballCD") ? compound.getInteger("kakuzuFireballCD") : 0;
         this.fireAoeCD = compound.hasKey("kakuzuFireAoeCD") ? compound.getInteger("kakuzuFireAoeCD") : 0;
         this.waterBulletCD = compound.hasKey("kakuzuWaterBulletCD") ? compound.getInteger("kakuzuWaterBulletCD") : 0;
         this.waterAoeCD = compound.hasKey("kakuzuWaterAoeCD") ? compound.getInteger("kakuzuWaterAoeCD") : 0;
         this.threadPullCD = compound.hasKey("kakuzuThreadPullCD") ? compound.getInteger("kakuzuThreadPullCD") : 0;
         this.meleeCD = compound.hasKey("kakuzuMeleeCD") ? compound.getInteger("kakuzuMeleeCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.world.isRemote) {
            return amount;
         } else if (this.heartInvulnTicks > 0) {
            return -1.0F;
         } else {
            this.checkHeartDestruction();
            return amount;
         }
      }

      protected void onCombatDeath() {
         this.kakuzuSay("§7Impossible... all five hearts...");
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      private void checkHeartDestruction() {
         float healthPercent = this.getHealth() / this.getMaxHealth();
         if (this.currentHeart == 1 && healthPercent <= 0.8F) {
            this.destroyHeart(2, "§6Earth Grudge Fear... you destroyed one heart.");
         } else if (this.currentHeart == 2 && healthPercent <= 0.6F) {
            this.destroyHeart(3, "§bThree hearts remain... you'll regret that.");
         } else if (this.currentHeart == 3 && healthPercent <= 0.4F) {
            this.destroyHeart(4, "§cTwo hearts left... I'm not done yet!");
         } else if (this.currentHeart == 4 && healthPercent <= 0.2F) {
            this.destroyHeart(5, "§8§lOne heart... You think this is over?!");
         }

      }

      private void destroyHeart(int nextHeart, String dialogue) {
         switch (nextHeart) {
            case 2:
               if (this.heart2Announced) {
                  return;
               }

               this.heart2Announced = true;
               break;
            case 3:
               if (this.heart3Announced) {
                  return;
               }

               this.heart3Announced = true;
               break;
            case 4:
               if (this.heart4Announced) {
                  return;
               }

               this.heart4Announced = true;
               break;
            case 5:
               if (this.heart5Announced) {
                  return;
               }

               this.heart5Announced = true;
         }

         this.currentHeart = nextHeart;
         this.heartInvulnTicks = 10;
         int heartsRemaining = 6 - nextHeart;
         this.kakuzuSay(dialogue);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
            p.sendMessage(new TextComponentString("§c[" + heartsRemaining + "] hearts remain"));
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 0.5F);
         if (!this.world.isRemote) {
            switch (this.currentHeart - 1) {
               case 1:
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.1, 0.15, 0.1, new int[]{-7838157, 30});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)1.5F, (double)1.5F, (double)1.5F, 0.05, 0.1, 0.05, new int[]{-11189214, 25});
                  break;
               case 2:
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)2.0F, (double)2.0F, (double)2.0F, 0.1, 0.15, 0.1, new int[]{-1428295937, 22});
                  break;
               case 3:
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)2.0F, (double)2.0F, (double)2.0F, 0.1, 0.15, 0.1, new int[]{-49152, 30});
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)1.5F, (double)1.5F, (double)1.5F, 0.02, 0.05, 0.02, new int[]{-60160, 25});
                  break;
               case 4:
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)2.0F, (double)2.0F, (double)2.0F, 0.15, 0.2, 0.15, new int[]{-13408598, 25});
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.5F, (double)1.5F, (double)1.5F, 0.05, 0.05, 0.05, new int[]{-13421773, 30});
         }

         float speed = 0.4F + (float)(this.currentHeart - 1) * 0.025F;
         if (this.currentHeart == 5) {
            speed = 0.5F;
         }

         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)speed);
         float atk = 36.0F + (float)(this.currentHeart - 1) * 2.0F;
         if (this.currentHeart == 5) {
            atk = 44.0F;
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)atk);
         float armor = 12.0F + (float)(this.currentHeart - 1) * 1.0F;
         if (this.currentHeart == 5) {
            armor = 16.0F;
         }

         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)armor);
      }

      private void tickAmbientParticles() {
         if (!this.world.isRemote) {
            if (this.ticksExisted % 4 == 0) {
               switch (this.currentHeart) {
                  case 1:
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, this.posY + 0.3 + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, 2, 0.2, 0.1, 0.2, 0.02, 0.01, 0.02, new int[]{-7838157, 20});
                     break;
                  case 2:
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * 1.2, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * 1.2, 1, 0.1, 0.2, 0.1, 0.01, 0.01, 0.01, new int[]{-1428295937, 18});
                     break;
                  case 3:
                     Particles.spawnParticle(this.world, Types.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + (double)0.5F + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 2, 0.1, 0.2, 0.1, 0.01, 0.02, 0.01, new int[]{-49152, 20});
                     break;
                  case 4:
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, 2, 0.2, 0.3, 0.2, (double)0.0F, -0.02, (double)0.0F, new int[]{-13408598, 20});
                     break;
                  case 5:
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, this.posY + 0.3 + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, 3, 0.2, 0.3, 0.2, 0.01, 0.01, 0.01, new int[]{-13421773, 22});
               }

            }
         }
      }

      private void processEarthPhase(EntityLivingBase target, double dist, float baseDmg, double dmgMul) {
         if (this.boulderCD <= 0 && dist >= (double)8.0F && dist <= (double)20.0F) {
            this.boulderCD = 60;
            if (!this.world.isRemote) {
               this.kakuzuSay("§6Earth Style... Weighted Boulder!");
               EntityWeightedBoulder.EntityCustom boulder = new EntityWeightedBoulder.EntityCustom(this.world, this, target.posX, target.posY, target.posZ, 1.0F);
               this.world.spawnEntity(boulder);
               this.swingArm(EnumHand.MAIN_HAND);
            }

         } else if (this.earthSlamCD <= 0 && dist <= (double)6.0F) {
            this.earthSlamCD = 50;
            this.performEarthSlam(target, baseDmg, dmgMul);
         } else if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)3.0F) {
            this.performStandardMelee(target, baseDmg, dmgMul);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void performEarthSlam(EntityLivingBase target, float baseDmg, double dmgMul) {
         if (!this.world.isRemote) {
            this.kakuzuSay("§6Earth Style... Iron Skin!");
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.6F);
            float normalDmg = baseDmg * 1.2F * (float)dmgMul;
            float trueDmg = baseDmg * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)5.0F))) {
               if (e != this && !(e instanceof QuestNpcBase)) {
                  double eDist = (double)this.getDistance(e);
                  float falloff = (float)Math.max(0.3, (double)1.0F - eDist / (double)6.0F);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
               }
            }

            for(int i = 0; i < 40; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = this.rand.nextDouble() * (double)5.0F;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(angle) * r, this.posY + 0.1 + this.rand.nextDouble() * (double)0.5F, this.posZ + Math.sin(angle) * r, 2, 0.2, 0.3, 0.2, 0.05, 0.08, 0.05, new int[]{-7838157, 25});
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 8, (double)1.0F, 0.3, (double)1.0F, 0.05, 0.1, 0.05, new int[]{-11189214, 35});
            this.swingArm(EnumHand.MAIN_HAND);
         }
      }

      private void processWindPhase(EntityLivingBase target, double dist, float baseDmg, double dmgMul) {
         if (this.windBulletCD <= 0 && dist >= (double)6.0F && dist <= (double)25.0F) {
            this.windBulletCD = 40;
            float normalDmg = baseDmg * 0.8F * (float)dmgMul;
            EntityWindCutter.EntityCustom cutter = new EntityWindCutter.EntityCustom(this.world, this);
            cutter.setDamage(normalDmg);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            cutter.shoot(dx, dy, dz, 1.5F, 1.5F);
            this.world.spawnEntity(cutter);
            this.swingArm(EnumHand.MAIN_HAND);
            this.kakuzuSay("§bWind Style... Wind Cutter!");
         } else if (this.windPushCD <= 0 && dist <= (double)8.0F) {
            this.windPushCD = 60;
            this.performWindPush(target, baseDmg, dmgMul);
         } else if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)3.0F) {
            this.performStandardMelee(target, baseDmg, dmgMul);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void performWindPush(EntityLivingBase target, float baseDmg, double dmgMul) {
         if (!this.world.isRemote) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.2F, 1.3F);
            float normalDmg = baseDmg * 0.6F * (float)dmgMul;
            float trueDmg = baseDmg * 0.2F;

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)6.0F))) {
               if (e != this && !(e instanceof QuestNpcBase)) {
                  double eDist = (double)this.getDistance(e);
                  float falloff = (float)Math.max(0.3, (double)1.0F - eDist / (double)7.0F);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  double kbx = e.posX - this.posX;
                  double kbz = e.posZ - this.posZ;
                  double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                  if (kblen > 0.1) {
                     e.motionX += kbx / kblen * (double)1.5F;
                     e.motionY += 0.35;
                     e.motionZ += kbz / kblen * (double)1.5F;
                     e.velocityChanged = true;
                  }
               }
            }

            for(int i = 0; i < 40; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = this.rand.nextDouble() * (double)6.0F;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(angle) * r, this.posY + (double)0.5F + this.rand.nextDouble() * (double)1.5F, this.posZ + Math.sin(angle) * r, 1, 0.2, 0.2, 0.2, 0.05, 0.03, 0.05, new int[]{-1428295937, 22});
            }

         }
      }

      private void processFirePhase(EntityLivingBase target, double dist, float baseDmg, double dmgMul) {
         if (this.fireballCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.fireballCD = 35;
            float normalDmg = baseDmg * 0.9F * (float)dmgMul;
            float trueDmg = baseDmg * 0.4F;
            EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, normalDmg, trueDmg);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            fireball.shoot(dx, dy + horizDist * 0.05, dz, 1.3F, 1.0F);
            this.world.spawnEntity(fireball);
            this.swingArm(EnumHand.MAIN_HAND);
            this.kakuzuSay("§cFire Style... Searing Migraine!");
         } else if (this.fireAoeCD <= 0 && dist <= (double)6.0F) {
            this.fireAoeCD = 50;
            this.performFireAoe(target, baseDmg, dmgMul);
         } else if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)3.0F) {
            this.performStandardMelee(target, baseDmg, dmgMul);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void performFireAoe(EntityLivingBase target, float baseDmg, double dmgMul) {
         if (!this.world.isRemote) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F);
            float normalDmg = baseDmg * 1.0F * (float)dmgMul;
            float trueDmg = baseDmg * 0.4F;

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)5.0F))) {
               if (e != this && !(e instanceof QuestNpcBase)) {
                  double eDist = (double)this.getDistance(e);
                  float falloff = (float)Math.max(0.3, (double)1.0F - eDist / (double)6.0F);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  e.setFire(4);
               }
            }

            for(int i = 0; i < 50; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = this.rand.nextDouble() * (double)5.0F;
               Particles.spawnParticle(this.world, Types.FLAME, this.posX + Math.cos(angle) * r, this.posY + 0.3 + this.rand.nextDouble() * (double)1.5F, this.posZ + Math.sin(angle) * r, 1, 0.1, 0.2, 0.1, 0.03, 0.04, 0.03, new int[]{-49152, 25});
            }

            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)1.5F, (double)0.5F, (double)1.5F, 0.02, 0.05, 0.02, new int[]{-60160, 30});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 10, (double)1.5F, (double)0.5F, (double)1.5F, 0.01, 0.03, 0.01, new int[]{-870704614, 25});
         }
      }

      private void processWaterPhase(EntityLivingBase target, double dist, float baseDmg, double dmgMul) {
         if (this.waterBulletCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.waterBulletCD = 35;
            float normalDmg = baseDmg * 0.8F * (float)dmgMul;
            float trueDmg = baseDmg * 0.35F;
            EntitySuitonBullet.EntityCustom bullet = new EntitySuitonBullet.EntityCustom(this.world, this, normalDmg, trueDmg);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            bullet.shoot(dx, dy, dz, 1.4F, 1.5F);
            this.world.spawnEntity(bullet);
            this.swingArm(EnumHand.MAIN_HAND);
            this.kakuzuSay("§9Water Style... Water Encampment!");
         } else if (this.waterAoeCD <= 0 && dist <= (double)6.0F) {
            this.waterAoeCD = 50;
            this.performWaterAoe(target, baseDmg, dmgMul);
         } else if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)3.0F) {
            this.performStandardMelee(target, baseDmg, dmgMul);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void performWaterAoe(EntityLivingBase target, float baseDmg, double dmgMul) {
         if (!this.world.isRemote) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.8F);
            float normalDmg = baseDmg * 1.0F * (float)dmgMul;
            float trueDmg = baseDmg * 0.4F;

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)5.0F))) {
               if (e != this && !(e instanceof QuestNpcBase)) {
                  double eDist = (double)this.getDistance(e);
                  float falloff = (float)Math.max(0.3, (double)1.0F - eDist / (double)6.0F);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 0));
               }
            }

            for(int i = 0; i < 50; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = this.rand.nextDouble() * (double)5.0F;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(angle) * r, this.posY + 0.3 + this.rand.nextDouble() * (double)1.0F, this.posZ + Math.sin(angle) * r, 1, 0.2, 0.2, 0.2, 0.1, 0.05, 0.1, new int[]{-13408598, 22});
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)1.5F, (double)1.0F, (double)1.5F, (double)0.0F, -0.03, (double)0.0F, new int[]{-14531397, 20});
         }
      }

      private void processThreadPhase(EntityLivingBase target, double dist, float baseDmg, double dmgMul) {
         if (this.threadPullCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.threadPullCD = 30;
            this.performThreadPull(target, baseDmg, dmgMul);
         } else if (this.meleeCD <= 0 && dist <= (double)5.0F && this.rand.nextFloat() < 0.3F) {
            this.meleeCD = 40;
            this.startThreadCombo(target);
         } else if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)5.0F) {
            this.performStandardMelee(target, baseDmg, dmgMul);
         } else {
            if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
               this.performMeleeSwing(target);
            }

         }
      }

      private void performThreadPull(EntityLivingBase target, float baseDmg, double dmgMul) {
         if (!this.world.isRemote) {
            this.kakuzuSay("§8Get over here!");
            double dx = this.posX - target.posX;
            double dz = this.posZ - target.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.1) {
               double pullStrength = 1.8;
               target.motionX += dx / len * pullStrength;
               target.motionY += 0.3;
               target.motionZ += dz / len * pullStrength;
               target.velocityChanged = true;
            }

            float pullDmg = baseDmg * 0.3F * (float)dmgMul;
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), pullDmg);
            double tdx = target.posX - this.posX;
            double tdy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)1.0F);
            double tdz = target.posZ - this.posZ;

            for(int i = 0; i < 15; ++i) {
               double frac = (double)i / (double)15.0F;
               Particles.spawnParticle(this.world, Types.FLAME, this.posX + tdx * frac, this.posY + (double)1.0F + tdy * frac, this.posZ + tdz * frac, 1, 0.05, 0.05, 0.05, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-15658735, 20});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }
      }

      private void startThreadCombo(EntityLivingBase target) {
         this.comboStep = 1;
         this.comboDelay = COMBO_DELAYS[0];
         this.comboTarget = target;
         this.executeComboHit(target, 1);
         this.kakuzuSay("§8Earth Grudge Fear!");
      }

      private void processThreadCombo() {
         if (this.comboTarget != null && this.comboTarget.isEntityAlive() && !((double)this.getDistance(this.comboTarget) > (double)7.0F)) {
            this.getLookHelper().setLookPositionWithEntity(this.comboTarget, 30.0F, 30.0F);
            --this.comboDelay;
            if (this.comboDelay <= 0) {
               ++this.comboStep;
               if (this.comboStep > 6) {
                  this.comboStep = 0;
                  this.comboDelay = 0;
                  this.comboTarget = null;
                  return;
               }

               this.comboDelay = COMBO_DELAYS[this.comboStep - 1];
               this.executeComboHit(this.comboTarget, this.comboStep);
            }

         } else {
            this.comboStep = 0;
            this.comboDelay = 0;
            this.comboTarget = null;
         }
      }

      private void executeComboHit(EntityLivingBase target, int step) {
         if (!this.world.isRemote) {
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float stepMul = 0.5F + (float)step * 0.1F;
            if (step == 6) {
               stepMul = 1.3F;
            }

            float normalDmg = baseDmg * stepMul * (float)dmgMul;
            float trueDmg = 5.0F + (float)step * 1.5F;
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            if (step == 6) {
               double kx = target.posX - this.posX;
               double kz = target.posZ - this.posZ;
               double klen = Math.sqrt(kx * kx + kz * kz);
               if (klen > 0.1) {
                  target.motionX += kx / klen * 1.8;
                  target.motionY += 0.4;
                  target.motionZ += kz / klen * 1.8;
                  target.velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
            } else {
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.9F + (float)step * 0.05F);
            }

            this.swingArm(step % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.3, 0.3, 0.3, 0.02, 0.02, 0.02, new int[]{-13421773, 22});
         }
      }

      private void performStandardMelee(EntityLivingBase target, float baseDmg, double dmgMul) {
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
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.9F);
         int[] cd = this.getCooldownRange(25, 40);
         this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
      }

      private void kakuzuSay(String msg) {
         if (this.chatCooldown <= 0) {
            String name = this.getCustomNameTag();
            if (name == null || name.isEmpty()) {
               name = "Kakuzu";
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
               p.sendMessage(new TextComponentString("§8" + name + ": " + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
