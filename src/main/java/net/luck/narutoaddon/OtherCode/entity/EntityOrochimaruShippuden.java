
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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityOrochimaruShippuden extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 268;

   public EntityOrochimaruShippuden(ElementsInfTsukAddon instance) {
      super(instance, 268);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "orochimarushippuden"), 268).name("orochimarushippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, OrochimaruShippudenRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class OrochimaruShippudenRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/orochimaru.png");

      public OrochimaruShippudenRenderer(RenderManager renderManager) {
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
         boolean phaseGlow = entity.phase >= 2;
         if (!(alpha < 1.0F) && !phaseGlow) {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
         } else {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            if (phaseGlow) {
               float glowIntensity = entity.phase == 3 ? 0.9F : 0.7F;
               float r = 0.6F + (entity.phase == 3 ? 0.15F : 0.0F);
               float g = 0.4F;
               float b = 0.85F + (entity.phase == 3 ? 0.1F : 0.0F);
               GlStateManager.color(r, g, b, alpha < 1.0F ? alpha : glowIntensity);
            } else {
               GlStateManager.color(0.7F, 0.85F, 1.0F, alpha);
            }

            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
      int phase = 1;
      private int attackTick = 0;
      private boolean phase4Played = false;
      private boolean phase5Played = false;
      private int meleeCD = 0;
      private int kusanagiCD = 0;
      private int snakeSummonCD = 0;
      private int windCD = 0;
      private int rashmonCD = 0;
      private int bodyReplacementCD = 0;
      private int healCD = 0;
      private List<Entity> spawnedSnakes = new ArrayList();
      private boolean rashmonActive = false;
      private int rashmonTicks = 0;
      private int comboStep = 0;
      private int comboDelay = 0;
      private EntityLivingBase comboTarget = null;
      private static final int[] COMBO_DELAYS = new int[]{0, 5, 5, 8, 12};
      private boolean introPlayed = false;
      private boolean phase2Played = false;
      private boolean phase3Played = false;
      private boolean bodyReplacementAnnounced = false;
      private static final ResourceLocation SNAKE_REGISTRY = new ResourceLocation("inftsukaddon", "orochimaru_snake");
      private boolean mandaSpawned = false;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.updatePhase();
         this.processOrochimaruCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         this.tickOrochimaruCooldowns();
      }

      protected void resetCombatState() {
         this.phase = 1;
         this.attackTick = 0;
         this.meleeCD = 0;
         this.kusanagiCD = 0;
         this.snakeSummonCD = 0;
         this.windCD = 0;
         this.rashmonCD = 0;
         this.bodyReplacementCD = 0;
         this.healCD = 0;
         this.rashmonActive = false;
         this.rashmonTicks = 0;
         this.comboStep = 0;
         this.comboDelay = 0;
         this.comboTarget = null;
         this.introPlayed = false;
         this.phase2Played = false;
         this.phase3Played = false;
         this.phase4Played = false;
         this.phase5Played = false;
         this.mandaSpawned = false;
         this.bodyReplacementAnnounced = false;
         this.cleanupSnakes();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("oroPhase", this.phase);
         compound.setInteger("oroMeleeCD", this.meleeCD);
         compound.setInteger("oroKusanagiCD", this.kusanagiCD);
         compound.setInteger("oroSnakeSummonCD", this.snakeSummonCD);
         compound.setInteger("oroWindCD", this.windCD);
         compound.setInteger("oroRashmonCD", this.rashmonCD);
         compound.setInteger("oroBodyRepCD", this.bodyReplacementCD);
         compound.setInteger("oroHealCD", this.healCD);
         compound.setBoolean("oroRashmonActive", this.rashmonActive);
         compound.setInteger("oroRashmonTicks", this.rashmonTicks);
         compound.setBoolean("oroIntroPlayed", this.introPlayed);
         compound.setBoolean("oroPhase2Played", this.phase2Played);
         compound.setBoolean("oroPhase3Played", this.phase3Played);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.phase = compound.hasKey("oroPhase") ? compound.getInteger("oroPhase") : 1;
         this.meleeCD = compound.hasKey("oroMeleeCD") ? compound.getInteger("oroMeleeCD") : 0;
         this.kusanagiCD = compound.hasKey("oroKusanagiCD") ? compound.getInteger("oroKusanagiCD") : 0;
         this.snakeSummonCD = compound.hasKey("oroSnakeSummonCD") ? compound.getInteger("oroSnakeSummonCD") : 0;
         this.windCD = compound.hasKey("oroWindCD") ? compound.getInteger("oroWindCD") : 0;
         this.rashmonCD = compound.hasKey("oroRashmonCD") ? compound.getInteger("oroRashmonCD") : 0;
         this.bodyReplacementCD = compound.hasKey("oroBodyRepCD") ? compound.getInteger("oroBodyRepCD") : 0;
         this.healCD = compound.hasKey("oroHealCD") ? compound.getInteger("oroHealCD") : 0;
         this.rashmonActive = compound.getBoolean("oroRashmonActive");
         this.rashmonTicks = compound.hasKey("oroRashmonTicks") ? compound.getInteger("oroRashmonTicks") : 0;
         this.introPlayed = compound.getBoolean("oroIntroPlayed");
         this.phase2Played = compound.getBoolean("oroPhase2Played");
         this.phase3Played = compound.getBoolean("oroPhase3Played");
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.world.isRemote) {
            return amount;
         } else {
            if (this.rashmonActive) {
               amount *= 0.5F;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, 0.8, (double)0.5F, 0.3, new int[0]);
               }
            }

            float dodgeChance = this.phase >= 5 ? 0.5F : (this.phase >= 4 ? 0.4F : 0.3F);
            if (this.phase >= 3 && this.bodyReplacementCD <= 0 && source.getTrueSource() instanceof EntityLivingBase && this.rand.nextFloat() < dodgeChance) {
               EntityLivingBase attacker = (EntityLivingBase)source.getTrueSource();
               this.performBodyReplacement(attacker);
               return -1.0F;
            } else {
               return amount;
            }
         }
      }

      protected void onCombatDeath() {
         this.cleanupSnakes();
         this.orochimaruSay("§7Kukuku... Until we meet again.");
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      private void updatePhase() {
         if (!this.world.isRemote) {
            float healthPercent = this.getHealth() / this.getMaxHealth();
            if (this.phase == 1 && healthPercent < 0.65F) {
               this.phase = 2;
               if (!this.phase2Played) {
                  this.phase2Played = true;
                  this.orochimaruSay("§dYou've pushed me this far... How delightful.");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.5F, this.posZ, 50, (double)1.5F, (double)1.5F, (double)1.5F, (double)0.5F, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.6F);
               }
            } else if (this.phase == 2 && healthPercent < 0.35F) {
               this.phase = 3;
               if (!this.phase3Played) {
                  this.phase3Played = true;
                  this.orochimaruSay("§4This body is but a vessel... and vessels can be replaced!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.5F, this.posZ, 80, (double)2.0F, (double)2.0F, (double)2.0F, 0.8, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.3, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 0.5F);
               }
            } else if (this.phase == 3 && healthPercent < 0.2F) {
               this.phase = 4;
               if (!this.phase4Played) {
                  this.phase4Played = true;
                  this.orochimaruSay("§4§lYou think this is enough to kill me?! I am IMMORTAL!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.5F, this.posZ, 100, (double)2.5F, (double)2.5F, (double)2.5F, (double)1.0F, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.5F, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.5F, 0.4F);
                  this.setHealth(Math.min(this.getHealth() + this.getMaxHealth() * 0.05F, this.getMaxHealth()));
               }
            } else if (this.phase == 4 && healthPercent < 0.1F) {
               this.phase = 5;
               if (!this.phase5Played) {
                  this.phase5Played = true;
                  this.orochimaruSay("§4§lFormation of Ten Thousand Snakes!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.5F, this.posZ, 150, (double)3.0F, (double)3.0F, (double)3.0F, (double)1.5F, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 2.0F, 0.5F);
               }
            }

         }
      }

      private void tickOrochimaruCooldowns() {
         if (this.meleeCD > 0) {
            --this.meleeCD;
         }

         if (this.kusanagiCD > 0) {
            --this.kusanagiCD;
         }

         if (this.snakeSummonCD > 0) {
            --this.snakeSummonCD;
         }

         if (this.windCD > 0) {
            --this.windCD;
         }

         if (this.rashmonCD > 0) {
            --this.rashmonCD;
         }

         if (this.bodyReplacementCD > 0) {
            --this.bodyReplacementCD;
         }

         if (this.healCD > 0) {
            --this.healCD;
         }

         ++this.attackTick;
         if (this.rashmonActive) {
            ++this.rashmonTicks;
            if (this.rashmonTicks >= 100) {
               this.rashmonActive = false;
               this.rashmonTicks = 0;
            }
         }

         this.cleanDeadSnakes();
      }

      private float phaseCdMul() {
         if (this.phase >= 5) {
            return 0.25F;
         } else if (this.phase >= 4) {
            return 0.35F;
         } else if (this.phase == 3) {
            return 0.5F;
         } else {
            return this.phase == 2 ? 0.7F : 1.0F;
         }
      }

      private int maxSnakes() {
         if (this.phase >= 4) {
            return 3;
         } else if (this.phase >= 3) {
            return 3;
         } else {
            return this.phase >= 2 ? 2 : 1;
         }
      }

      private void processOrochimaruCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.introPlayed) {
               this.introPlayed = true;
               this.orochimaruSay("§dKukuku... Let's see what you're made of.");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.5F, new int[0]);
               }
            }

            if (this.ticksExisted % 4 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ, 2, 0.3, (double)0.5F, 0.3, 0.1, new int[0]);
            }

            if (this.phase >= 3 && this.ticksExisted % 6 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + 0.3 + this.rand.nextDouble() * (double)1.5F, this.posZ, 3, 0.4, 0.6, 0.4, 0.15, new int[0]);
            }

            if (this.rashmonActive && this.ticksExisted % 3 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ, 5, 0.6, (double)1.0F, 0.6, 0.2, new int[0]);
            }

            if (this.comboStep > 0 && this.comboTarget != null) {
               this.processKusanagiCombo();
            } else {
               double dmgMul = this.getDamageMultiplier();
               float cdMul = this.phaseCdMul();
               if (this.phase >= 3 && this.healCD <= 0 && this.attackTick % 20 == 0) {
                  int healInterval = (int)(400.0F * cdMul);
                  if (this.attackTick % healInterval < 2) {
                     this.performOralRebirth();
                     return;
                  }
               }

               if (this.phase >= 4 && this.ticksExisted % 40 == 0) {
                  for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)5.0F))) {
                     if (p.isEntityAlive() && !p.isSpectator()) {
                        p.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, this.phase >= 5 ? 1 : 0));
                        if (this.phase >= 5) {
                           p.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
                        }
                     }
                  }
               }

               if (this.phase >= 4 && !this.mandaSpawned && this.snakeSummonCD <= 0) {
                  this.snakeSummonCD = 200;
                  this.spawnManda(target);
               } else if (this.phase >= 3 && this.snakeSummonCD <= 0 && dist <= (double)20.0F && this.countAliveSnakes() < this.maxSnakes()) {
                  int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(180.0F * cdMul));
                  this.snakeSummonCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.spawnSnakes(1, target);
                  this.orochimaruSay("§dStriking Shadow Snakes!");
               } else if (this.phase >= 2 && this.windCD <= 0 && dist <= (double)12.0F && dist >= (double)3.0F && this.rand.nextFloat() < 0.18F) {
                  int[] cd = this.getCooldownRange((int)(200.0F * cdMul), (int)(280.0F * cdMul));
                  this.windCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.performWindBreakthrough(target, dmgMul);
               } else if (this.phase >= 2 && this.rashmonCD <= 0 && !this.rashmonActive && this.rand.nextFloat() < 0.08F) {
                  int[] cd = this.getCooldownRange((int)(300.0F * cdMul), (int)(400.0F * cdMul));
                  this.rashmonCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.activateRashmon();
               } else if (this.phase == 2 && this.snakeSummonCD <= 0 && dist <= (double)20.0F && this.rand.nextFloat() < 0.15F && this.countAliveSnakes() < this.maxSnakes()) {
                  int[] cd = this.getCooldownRange((int)(140.0F * cdMul), (int)(200.0F * cdMul));
                  this.snakeSummonCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.spawnSnakes(1, target);
                  this.orochimaruSay("§dStriking Shadow Snakes!");
               } else if (this.kusanagiCD <= 0 && dist <= (double)5.0F && this.rand.nextFloat() < 0.15F) {
                  int[] cd = this.getCooldownRange((int)(100.0F * cdMul), (int)(140.0F * cdMul));
                  this.kusanagiCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.startKusanagiCombo(target);
                  this.orochimaruSay("§dKusanagi... Longsword of the Sky!");
               } else if (this.meleeCD <= 0 && dist <= (double)5.0F && dist > (double)0.5F) {
                  int[] cd = this.getCooldownRange((int)(40.0F * cdMul), (int)(55.0F * cdMul));
                  this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.performKusanagiSlash(target, dmgMul);
               } else if (this.phase == 1 && this.snakeSummonCD <= 0 && dist <= (double)20.0F && dist >= (double)3.0F && this.rand.nextFloat() < 0.12F && this.countAliveSnakes() < this.maxSnakes()) {
                  int[] cd = this.getCooldownRange((int)(200.0F * cdMul), (int)(260.0F * cdMul));
                  this.snakeSummonCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.spawnSnakes(1, target);
                  this.orochimaruSay("§dStriking Shadow Snakes!");
               } else if (dist > (double)10.0F && this.kunaiCooldown <= 0) {
                  float normalDmg = this.jutsuPower * 0.25F * (float)dmgMul;
                  float trueDmg = 3.0F;
                  EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, normalDmg, trueDmg);
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY;
                  double dz = target.posZ - this.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  kunai.shoot(dx, dy + d * 0.1, dz, this.kunaiSpeed, this.kunaiInaccuracy);
                  this.world.spawnEntity(kunai);
                  this.swingArm(EnumHand.MAIN_HAND);
                  int[] cd = this.getCooldownRange(30, 50);
                  this.kunaiCooldown = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
               } else {
                  if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     this.performMeleeSwing(target);
                  }

               }
            }
         }
      }

      private void performKusanagiSlash(EntityLivingBase target, double dmgMul) {
         float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float normalDmg = baseDamage * 1.2F * (float)dmgMul;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            trueDmg = baseDamage * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            trueDmg = 8.0F * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2F, 0.9F);
         if (this.world instanceof WorldServer) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.1) {
               for(int i = 0; i < 6; ++i) {
                  double frac = (double)(i + 1) / (double)7.0F;
                  double px = this.posX + dx * frac;
                  double pz = this.posZ + dz * frac;
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, px, this.posY + 1.2, pz, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
               }

               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX + dx * (double)0.5F, this.posY + (double)1.5F, this.posZ + dz * (double)0.5F, 5, 0.3, 0.3, 0.3, 0.02, new int[0]);
            }
         }

      }

      private void startKusanagiCombo(EntityLivingBase target) {
         this.comboStep = 1;
         this.comboDelay = COMBO_DELAYS[0];
         this.comboTarget = target;
         this.executeComboHit(target, 1);
      }

      private void processKusanagiCombo() {
         if (this.comboTarget != null && this.comboTarget.isEntityAlive() && !((double)this.getDistance(this.comboTarget) > (double)7.0F)) {
            this.getLookHelper().setLookPositionWithEntity(this.comboTarget, 30.0F, 30.0F);
            --this.comboDelay;
            if (this.comboDelay <= 0) {
               ++this.comboStep;
               if (this.comboStep > 5) {
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
            float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float stepMul = 0.7F + (float)step * 0.12F;
            float normalDmg = baseDamage * stepMul * (float)dmgMul;
            float trueDmg = 4.0F + (float)step;
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            if (step == 5) {
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.1) {
                  target.motionX += dx / len * (double)1.5F;
                  target.motionY += 0.35;
                  target.motionZ += dz / len * (double)1.5F;
                  target.velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.8F);
            } else {
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.0F + (float)step * 0.05F);
            }

            this.swingArm(step % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 2, 0.3, 0.2, 0.3, (double)0.0F, new int[0]);
            }

         }
      }

      private void spawnSnakes(int count, EntityLivingBase target) {
         if (!this.world.isRemote) {
            for(int i = 0; i < count; ++i) {
               double offsetX = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
               double offsetZ = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
               double spawnX = this.posX + offsetX;
               double spawnZ = this.posZ + offsetZ;
               Entity snake = EntityList.createEntityByIDFromName(SNAKE_REGISTRY, this.world);
               if (snake != null) {
                  snake.setPosition(spawnX, this.posY, spawnZ);
                  snake.getEntityData().setBoolean("questEntity", true);
                  snake.getEntityData().setString("ownerUUID", this.getUniqueID().toString());
                  if (snake instanceof EntityOrochimaruSnake.EntityCustom) {
                     ((EntityOrochimaruSnake.EntityCustom)snake).setOwnerUUID(this.getUniqueID().toString());
                  }

                  if (snake instanceof EntityLivingBase) {
                     EntityLivingBase living = (EntityLivingBase)snake;
                     if (this.phase <= 2) {
                        living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(1000 + this.phase * 250));
                        living.setHealth((float)(1000 + this.phase * 250));
                        living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)8.0F);
                     } else if (this.phase == 3) {
                        living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)2000.0F);
                        living.setHealth(2000.0F);
                        living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)12.0F);
                     }
                  }

                  this.world.spawnEntity(snake);
                  this.spawnedSnakes.add(snake);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, spawnX, this.posY + (double)0.5F, spawnZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.3, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, spawnX, this.posY + 0.3, spawnZ, 8, 0.3, 0.3, 0.3, 0.05, new int[0]);
                  }
               }
            }

         }
      }

      private void spawnManda(EntityLivingBase target) {
         if (!this.world.isRemote && !this.mandaSpawned) {
            this.mandaSpawned = true;
            Entity manda = EntityList.createEntityByIDFromName(SNAKE_REGISTRY, this.world);
            if (manda != null) {
               double spawnX = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F;
               double spawnZ = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F;
               manda.setPosition(spawnX, this.posY, spawnZ);
               manda.getEntityData().setBoolean("questEntity", true);
               manda.getEntityData().setString("ownerUUID", this.getUniqueID().toString());
               if (manda instanceof EntityOrochimaruSnake.EntityCustom) {
                  ((EntityOrochimaruSnake.EntityCustom)manda).setOwnerUUID(this.getUniqueID().toString());
               }

               if (manda instanceof EntityLivingBase) {
                  EntityLivingBase living = (EntityLivingBase)manda;
                  living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)12000.0F);
                  living.setHealth(12000.0F);
                  living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)35.0F);
                  if (manda.hasCustomName()) {
                     manda.setCustomNameTag("Manda");
                  } else {
                     manda.setCustomNameTag("Manda");
                  }
               }

               this.world.spawnEntity(manda);
               this.spawnedSnakes.add(manda);
               this.orochimaruSay("§d§lSummoning Jutsu... Manda!");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, spawnX, this.posY + (double)1.0F, spawnZ, 40, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.5F, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, spawnX, this.posY + (double)1.0F, spawnZ, 3, (double)1.5F, (double)1.5F, (double)1.5F, (double)0.0F, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, spawnX, this.posY, spawnZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 2.0F, 0.5F);
            }

         }
      }

      private int countAliveSnakes() {
         int count = 0;

         for(Entity e : this.spawnedSnakes) {
            if (e != null && e.isEntityAlive() && !e.isDead) {
               ++count;
            }
         }

         return count;
      }

      private void cleanDeadSnakes() {
         Iterator<Entity> it = this.spawnedSnakes.iterator();

         while(it.hasNext()) {
            Entity e = (Entity)it.next();
            if (e == null || !e.isEntityAlive() || e.isDead) {
               it.remove();
            }
         }

      }

      private void cleanupSnakes() {
         for(Entity e : this.spawnedSnakes) {
            if (e != null && e.isEntityAlive()) {
               e.setDead();
            }
         }

         this.spawnedSnakes.clear();
      }

      private void performWindBreakthrough(EntityLivingBase target, double dmgMul) {
         if (!this.world.isRemote) {
            this.orochimaruSay("§fWind Release... Great Breakthrough!");
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
            float normalDmg = 15.0F * (float)dmgMul;
            float trueDmg = 8.0F;

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)8.0F))) {
               if (e != this && !this.spawnedSnakes.contains(e) && !(e instanceof QuestNpcBase)) {
                  double eDist = (double)this.getDistance(e);
                  float falloff = (float)Math.max(0.3, (double)1.0F - eDist / (double)9.0F);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  double kbx = e.posX - this.posX;
                  double kbz = e.posZ - this.posZ;
                  double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                  if (kblen > 0.1) {
                     e.motionX += kbx / kblen * 1.8;
                     e.motionY += 0.4;
                     e.motionZ += kbz / kblen * 1.8;
                     e.velocityChanged = true;
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 60; ++i) {
                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double r = this.rand.nextDouble() * (double)8.0F;
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + Math.cos(angle) * r, this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(angle) * r, 1, 0.2, 0.2, 0.2, 0.05, new int[0]);
               }

               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 3, (double)1.0F, (double)0.5F, (double)1.0F, (double)0.0F, new int[0]);
            }

         }
      }

      private void activateRashmon() {
         if (!this.world.isRemote) {
            this.rashmonActive = true;
            this.rashmonTicks = 0;
            this.orochimaruSay("§8Rashomon!");
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.HOSTILE, 1.5F, 0.5F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.0F, (double)1.5F, (double)1.0F, (double)0.5F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.8, (double)1.0F, 0.8, 0.05, new int[0]);
            }

         }
      }

      private void performBodyReplacement(EntityLivingBase attacker) {
         if (!this.world.isRemote) {
            if (!this.bodyReplacementAnnounced) {
               this.bodyReplacementAnnounced = true;
               this.orochimaruSay("§dBody Replacement Technique!");
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 25, (double)0.5F, 0.6, (double)0.5F, 0.08, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 15, 0.4, 0.4, 0.4, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 0.7F);
            double teleportDist = (double)5.0F + this.rand.nextDouble() * (double)3.0F;
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double teleX = attacker.posX + Math.cos(angle) * teleportDist;
            double teleZ = attacker.posZ + Math.sin(angle) * teleportDist;
            double teleY = attacker.posY;
            if (this.world.isBlockLoaded(new BlockPos(teleX, teleY, teleZ)) && this.isPositionSafe(teleX, teleY, teleZ)) {
               this.internalReposition = true;
               this.setPositionAndUpdate(teleX, teleY, teleZ);
               this.internalReposition = false;
            } else {
               float aYaw = attacker.rotationYaw * ((float)Math.PI / 180F);
               double behindX = attacker.posX + Math.sin((double)aYaw) * (double)5.0F;
               double behindZ = attacker.posZ - Math.cos((double)aYaw) * (double)5.0F;
               if (this.world.isBlockLoaded(new BlockPos(behindX, teleY, behindZ)) && this.isPositionSafe(behindX, teleY, behindZ)) {
                  this.internalReposition = true;
                  this.setPositionAndUpdate(behindX, teleY, behindZ);
                  this.internalReposition = false;
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.3, new int[0]);
            }

            this.bodyReplacementCD = 80;
            this.comboStep = 0;
            this.comboDelay = 0;
            this.comboTarget = null;
         }
      }

      private void performOralRebirth() {
         if (!this.world.isRemote) {
            float healAmount = this.getMaxHealth() * 0.04F;
            this.heal(healAmount);
            int[] cd = this.getCooldownRange((int)(500.0F * this.phaseCdMul()), (int)(650.0F * this.phaseCdMul()));
            this.healCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.orochimaruSay("§dOral Rebirth... You can't kill me that easily.");
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 1.0F, 0.6F);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)1.0F, (double)0.5F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.8, (double)0.5F, 0.3, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + 1.2, this.posZ, 10, 0.4, 0.6, 0.4, 0.1, new int[0]);
            }

         }
      }

      private void orochimaruSay(String msg) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§5" + this.getCustomNameTag() + ": " + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
