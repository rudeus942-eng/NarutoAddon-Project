
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

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySerpentBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 233;

   public EntitySerpentBoss(ElementsInfTsukAddon instance) {
      super(instance, 37);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "serpentboss"), 233).name("serpentboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SerpentBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SerpentBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SerpentBossRenderer(RenderManager renderManager) {
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
      private boolean serpentPhase2 = false;
      private int serpentSnakesCD = 0;
      private int serpentPullCD = 0;
      private int serpentIntentCD = 0;
      private int serpentWindCD = 0;
      private int serpentGazeCD = 0;
      private int serpentFlickerCD = 0;
      private int serpentWindup = 0;
      private int serpentWindupType = 0;
      private boolean serpentIntroPlayed = false;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processSerpentCooldowns();
         this.processSerpentCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
      }

      protected void resetCombatState() {
         this.serpentPhase2 = false;
         this.serpentSnakesCD = 0;
         this.serpentPullCD = 0;
         this.serpentIntentCD = 0;
         this.serpentWindCD = 0;
         this.serpentGazeCD = 0;
         this.serpentFlickerCD = 0;
         this.serpentWindup = 0;
         this.serpentWindupType = 0;
         this.serpentIntroPlayed = false;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("serpentPhase2", this.serpentPhase2);
         compound.setInteger("serpentSnakesCD", this.serpentSnakesCD);
         compound.setInteger("serpentPullCD", this.serpentPullCD);
         compound.setInteger("serpentIntentCD", this.serpentIntentCD);
         compound.setInteger("serpentWindCD", this.serpentWindCD);
         compound.setInteger("serpentGazeCD", this.serpentGazeCD);
         compound.setInteger("serpentFlickerCD", this.serpentFlickerCD);
         compound.setBoolean("serpentIntroPlayed", this.serpentIntroPlayed);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.serpentPhase2 = compound.getBoolean("serpentPhase2");
         this.serpentSnakesCD = compound.hasKey("serpentSnakesCD") ? compound.getInteger("serpentSnakesCD") : 0;
         this.serpentPullCD = compound.hasKey("serpentPullCD") ? compound.getInteger("serpentPullCD") : 0;
         this.serpentIntentCD = compound.hasKey("serpentIntentCD") ? compound.getInteger("serpentIntentCD") : 0;
         this.serpentWindCD = compound.hasKey("serpentWindCD") ? compound.getInteger("serpentWindCD") : 0;
         this.serpentGazeCD = compound.hasKey("serpentGazeCD") ? compound.getInteger("serpentGazeCD") : 0;
         this.serpentFlickerCD = compound.hasKey("serpentFlickerCD") ? compound.getInteger("serpentFlickerCD") : 0;
         this.serpentIntroPlayed = compound.getBoolean("serpentIntroPlayed");
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (!this.world.isRemote && !this.serpentPhase2 && this.getHealth() / this.getMaxHealth() < 0.4F) {
            this.serpentPhase2 = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.5F, this.posZ, 40, (double)1.0F, (double)1.0F, (double)1.0F, 0.3, new int[0]);
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§5" + this.getCustomNameTag() + ": §dKukuku... you're more interesting than I thought..."));
            }
         }

         return amount;
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      private void processSerpentCooldowns() {
         if (this.serpentSnakesCD > 0) {
            --this.serpentSnakesCD;
         }

         if (this.serpentPullCD > 0) {
            --this.serpentPullCD;
         }

         if (this.serpentIntentCD > 0) {
            --this.serpentIntentCD;
         }

         if (this.serpentWindCD > 0) {
            --this.serpentWindCD;
         }

         if (this.serpentGazeCD > 0) {
            --this.serpentGazeCD;
         }

         if (this.serpentFlickerCD > 0) {
            --this.serpentFlickerCD;
         }

      }

      private void processSerpentCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.serpentIntroPlayed) {
               this.serpentIntroPlayed = true;
               this.serpentSay("Interesting prey...");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.5F, new int[0]);
               }
            }

            if (this.ticksExisted % 4 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ, 2, 0.3, (double)0.5F, 0.3, 0.1, new int[0]);
            }

            if (this.serpentWindup > 0) {
               --this.serpentWindup;
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  if (this.serpentWindupType == 1) {
                     ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)0.5F, this.posZ, 8, 0.8, (double)1.5F, 0.8, (double)0.5F, new int[0]);
                  }
               }

               if (this.serpentWindup <= 0) {
                  this.executeSerpentWindup(target);
               }

            } else {
               double dmgMul = this.getDamageMultiplier();
               float cdMul = this.serpentPhase2 ? 0.7F : 1.0F;
               if (this.serpentGazeCD <= 0 && dist <= (double)5.0F && this.rand.nextFloat() < 0.2F) {
                  int[] cd = this.getCooldownRange((int)(100.0F * cdMul), (int)(160.0F * cdMul));
                  this.serpentGazeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 9));
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, 3.0F);
                  this.serpentSay("Look into my eyes...");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, target.posX, target.posY + (double)target.getEyeHeight(), target.posZ, 15, 0.3, 0.3, 0.3, 0.2, new int[0]);
                  }

               } else if (this.serpentIntentCD <= 0 && dist <= (double)15.0F && this.rand.nextFloat() < 0.12F) {
                  int[] cd = this.getCooldownRange((int)(160.0F * cdMul), (int)(240.0F * cdMul));
                  this.serpentIntentCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.serpentWindup = 20;
                  this.serpentWindupType = 1;
                  this.serpentSay("You cannot hope to survive...");
               } else if (this.serpentPullCD <= 0 && dist <= (double)10.0F && dist >= (double)3.0F && this.rand.nextFloat() < 0.18F) {
                  int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(180.0F * cdMul));
                  this.serpentPullCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  double pullDx = this.posX - target.posX;
                  double pullDz = this.posZ - target.posZ;
                  double pullLen = Math.sqrt(pullDx * pullDx + pullDz * pullDz);
                  if (pullLen > 0.1) {
                     target.motionX += pullDx / pullLen * 1.2;
                     target.motionY += 0.15;
                     target.motionZ += pullDz / pullLen * 1.2;
                     target.velocityChanged = true;
                  }

                  target.attackEntityFrom(DamageSource.causeMobDamage(this), 1.2F * (float)dmgMul);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, 3.0F);
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;

                     for(int i = 0; i < 8; ++i) {
                        double t = (double)i / (double)8.0F;
                        double px = target.posX + (this.posX - target.posX) * t;
                        double pz = target.posZ + (this.posZ - target.posZ) * t;
                        ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, this.posY + 0.8, pz, 3, 0.2, 0.2, 0.2, 0.05, new int[0]);
                     }
                  }

                  this.serpentSay("Hidden Shadow Snake Hands!");
               } else if (this.serpentSnakesCD <= 0 && dist <= (double)12.0F && dist >= (double)3.0F && this.rand.nextFloat() < 0.22F) {
                  int[] cd = this.getCooldownRange((int)(60.0F * cdMul), (int)(100.0F * cdMul));
                  this.serpentSnakesCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float normalDmg = this.jutsuPower * 0.35F * (float)dmgMul;
                  float trueDmg = 3.0F;

                  for(int i = 0; i < 3; ++i) {
                     EntityKunaiProjectile.EntityCustom snake = new EntityKunaiProjectile.EntityCustom(this.world, this, normalDmg, trueDmg);
                     double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F;
                     double dy = target.posY + (double)target.getEyeHeight() - 0.1 - snake.posY;
                     double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F;
                     double d = Math.sqrt(dx * dx + dz * dz);
                     snake.shoot(dx, dy + d * 0.1, dz, 1.4F, 2.0F);
                     this.world.spawnEntity(snake);
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + 1.2, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.3, new int[0]);
                  }

                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.serpentWindCD <= 0 && dist <= (double)15.0F && dist >= (double)4.0F && this.rand.nextFloat() < 0.18F) {
                  int[] cd = this.getCooldownRange((int)(80.0F * cdMul), (int)(130.0F * cdMul));
                  this.serpentWindCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float normalDmg = this.jutsuPower * 0.4F * (float)dmgMul;
                  float trueDmg = 5.0F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  target.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 1));

                  for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, target.getEntityBoundingBox().grow((double)5.0F))) {
                     if (e != this) {
                        double kbx = e.posX - this.posX;
                        double kbz = e.posZ - this.posZ;
                        double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                        if (kblen > 0.1) {
                           e.motionX += kbx / kblen * 1.2;
                           e.motionY += (double)0.25F;
                           e.motionZ += kbz / kblen * 1.2;
                           e.velocityChanged = true;
                        }
                     }
                  }

                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;

                     for(int i = 0; i < 12; ++i) {
                        double frac = (double)i / (double)12.0F;
                        double px = this.posX + (target.posX - this.posX) * frac;
                        double pz = this.posZ + (target.posZ - this.posZ) * frac;
                        double py = this.posY + (double)this.getEyeHeight() + (target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight())) * frac;
                        ws.spawnParticle(EnumParticleTypes.SPELL_MOB, px, py, pz, 3, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
                     }

                     ws.spawnParticle(EnumParticleTypes.SPELL_MOB, target.posX, target.posY + (double)1.0F, target.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);
                  }

                  this.serpentSay("Take this venom!");
                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.serpentFlickerCD <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.15F) {
                  int[] cd = this.getCooldownRange((int)(130.0F * cdMul), (int)(190.0F * cdMul));
                  this.serpentFlickerCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float tYaw = target.rotationYaw * ((float)Math.PI / 180F);
                  double behindX = target.posX + Math.sin((double)tYaw) * (double)3.0F;
                  double behindZ = target.posZ - Math.cos((double)tYaw) * (double)3.0F;
                  if (this.world.isBlockLoaded(new BlockPos(behindX, target.posY, behindZ)) && this.isPositionSafe(behindX, target.posY, behindZ)) {
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
                     }

                     this.internalReposition = true;
                     this.setPositionAndUpdate(behindX, target.posY, behindZ);
                     this.internalReposition = false;
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.3, new int[0]);
                     }

                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 0.7F);
                     this.performMeleeSwing(target);
                  }

               } else {
                  if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     this.performMeleeSwing(target);
                  }

               }
            }
         }
      }

      private void executeSerpentWindup(EntityLivingBase target) {
         if (!this.world.isRemote) {
            if (this.serpentWindupType == 1) {
               for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)10.0F))) {
                  if (e != this) {
                     e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                     e.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
                     e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 40, 0));
                     e.hurtResistantTime = 0;
                     e.attackEntityFrom(DamageSource.MAGIC, 3.0F);
                  }
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)8.0F, (double)3.0F, (double)8.0F, 0.8, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
            }

            this.serpentWindupType = 0;
         }
      }

      private void serpentSay(String msg) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§5" + this.getCustomNameTag() + ": §d" + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
