
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityTayuyaBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 243;

   public EntityTayuyaBoss(ElementsInfTsukAddon instance) {
      super(instance, 243);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "tayuyaboss"), 243).name("tayuyaboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, TayuyaBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class TayuyaBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public TayuyaBossRenderer(RenderManager renderManager) {
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
      private boolean tayuyaDokiSummoned = false;
      private List<Entity> tayuyaDokiEntities = new ArrayList();
      private int tayuyaFluteCooldown = 0;
      private int tayuyaGenjutsuCooldown = 0;
      private int tayuyaCommandCooldown = 0;
      private boolean tayuyaCurseMark1 = false;
      private boolean tayuyaCurseMark2 = false;
      private boolean tayuyaIntroPlayed = false;
      private static final float TAYUYA_PHASE_2 = 0.65F;
      private static final float TAYUYA_PHASE_3 = 0.3F;
      private static final int TAYUYA_RETREAT_RANGE = 12;
      private static final double TAYUYA_BASE_HP = (double)9500.0F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processTayuyaCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.tayuyaFluteCooldown > 0) {
            --this.tayuyaFluteCooldown;
         }

         if (this.tayuyaGenjutsuCooldown > 0) {
            --this.tayuyaGenjutsuCooldown;
         }

         if (this.tayuyaCommandCooldown > 0) {
            --this.tayuyaCommandCooldown;
         }

         for(int i = this.tayuyaDokiEntities.size() - 1; i >= 0; --i) {
            Entity doki = (Entity)this.tayuyaDokiEntities.get(i);
            if (doki == null || doki.isDead) {
               this.tayuyaDokiEntities.remove(i);
            }
         }

      }

      protected void resetCombatState() {
         this.tayuyaDokiSummoned = false;
         this.tayuyaDokiEntities.clear();
         this.tayuyaCurseMark1 = false;
         this.tayuyaCurseMark2 = false;
         this.tayuyaIntroPlayed = false;
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      protected void onCombatDeath() {
         this.tayuyaOnDeath();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("tayuyaDokiSummoned", this.tayuyaDokiSummoned);
         compound.setInteger("tayuyaFluteCooldown", this.tayuyaFluteCooldown);
         compound.setInteger("tayuyaGenjutsuCooldown", this.tayuyaGenjutsuCooldown);
         compound.setInteger("tayuyaCommandCooldown", this.tayuyaCommandCooldown);
         compound.setBoolean("tayuyaCurseMark1", this.tayuyaCurseMark1);
         compound.setBoolean("tayuyaCurseMark2", this.tayuyaCurseMark2);
         compound.setBoolean("tayuyaIntroPlayed", this.tayuyaIntroPlayed);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.tayuyaDokiSummoned = compound.getBoolean("tayuyaDokiSummoned");
         this.tayuyaFluteCooldown = compound.hasKey("tayuyaFluteCooldown") ? compound.getInteger("tayuyaFluteCooldown") : 0;
         this.tayuyaGenjutsuCooldown = compound.hasKey("tayuyaGenjutsuCooldown") ? compound.getInteger("tayuyaGenjutsuCooldown") : 0;
         this.tayuyaCommandCooldown = compound.hasKey("tayuyaCommandCooldown") ? compound.getInteger("tayuyaCommandCooldown") : 0;
         this.tayuyaCurseMark1 = compound.getBoolean("tayuyaCurseMark1");
         this.tayuyaCurseMark2 = compound.getBoolean("tayuyaCurseMark2");
         this.tayuyaIntroPlayed = compound.getBoolean("tayuyaIntroPlayed");
      }

      private void processTayuyaCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            this.tayuyaCheckPhaseTransition(hpPercent);
            if (!this.tayuyaDokiSummoned) {
               this.tayuyaSummonDoki(target);
               this.tayuyaDokiSummoned = true;
            } else {
               if (!this.tayuyaIntroPlayed) {
                  this.tayuyaIntroPlayed = true;
                  this.tayuyaBroadcast("§5Tayuya: §dYou think you can take me? My melody will be your death march!");
               }

               if (dist < (double)12.0F) {
                  this.tayuyaRetreat(target);
               } else if (dist > (double)30.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.2);
               } else {
                  this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                  double strafeX = this.posX + (this.rand.nextBoolean() ? (double)3.0F : (double)-3.0F);
                  double strafeZ = this.posZ + (this.rand.nextBoolean() ? (double)3.0F : (double)-3.0F);
                  this.getNavigator().tryMoveToXYZ(strafeX, this.posY, strafeZ, (double)1.0F);
               }

               if (this.tayuyaCommandCooldown <= 0 && !this.tayuyaDokiEntities.isEmpty()) {
                  this.tayuyaCommandDoki(target);
                  this.tayuyaCommandCooldown = 60;
               }

               int fluteCDBase = this.tayuyaCurseMark2 ? 80 : (this.tayuyaCurseMark1 ? 120 : 160);
               if (this.tayuyaFluteCooldown <= 0 && dist <= (double)25.0F && dist >= (double)4.0F) {
                  this.tayuyaCastPhantomSoundChains(target);
                  this.tayuyaFluteCooldown = (int)((float)fluteCDBase * this.configCooldownMultiplier);
               } else {
                  int genjutsuCDBase = this.tayuyaCurseMark2 ? 40 : (this.tayuyaCurseMark1 ? 60 : 80);
                  if (this.tayuyaGenjutsuCooldown <= 0 && dist <= (double)20.0F && dist >= (double)3.0F) {
                     this.tayuyaCastFluteMelody(target);
                     this.tayuyaGenjutsuCooldown = (int)((float)genjutsuCDBase * this.configCooldownMultiplier);
                  }

               }
            }
         }
      }

      private void tayuyaCheckPhaseTransition(float hpPercent) {
         if (!this.tayuyaCurseMark1 && hpPercent <= 0.65F) {
            this.tayuyaCurseMark1 = true;
            this.tayuyaBroadcast("§5Tayuya: §dThe curse mark... you'll regret pushing me!");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.38);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }

         if (!this.tayuyaCurseMark2 && hpPercent <= 0.3F) {
            this.tayuyaCurseMark2 = true;
            this.tayuyaBroadcast("§4§lTayuya: CURSE MARK SECOND STATE! §cMy melody will be your requiem!");
            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
               double currentArmor = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(currentArmor + (double)10.0F);
            }

            this.tayuyaBoostDoki(0.4F, 0.15F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.6, (double)1.0F, 0.6, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 20, 0.4, 0.6, 0.4, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.7F);
         }

      }

      private void tayuyaSummonDoki(EntityLivingBase target) {
         String[] dokiConfigs = new String[]{"doki_tank", "doki_claw", "doki_worm"};
         double tierRatio = (double)this.getMaxHealth() / (double)9500.0F;
         if (tierRatio < 0.1) {
            tierRatio = 0.1;
         }

         for(int i = 0; i < dokiConfigs.length; ++i) {
            NpcConfig config = NpcConfigRegistry.get(dokiConfigs[i]);
            if (config != null) {
               Entity dokiEntity = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "questnpc1"), this.world);
               if (dokiEntity != null && dokiEntity instanceof EntityCreature) {
                  EntityCreature doki = (EntityCreature)dokiEntity;
                  double angle = (double)i * (double)2.0F * Math.PI / (double)3.0F + this.rand.nextDouble() * (double)0.5F;
                  double radius = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
                  double spawnX = this.posX + Math.cos(angle) * radius;
                  double spawnZ = this.posZ + Math.sin(angle) * radius;
                  double spawnY = this.findSafeY(spawnX, this.posY, spawnZ);
                  doki.setPosition(spawnX, spawnY, spawnZ);
                  if (doki instanceof INpcConfigurable) {
                     ((INpcConfigurable)doki).applyNpcConfig(config);
                  }

                  if (doki.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
                     double scaledHP = config.getMaxHealth() * tierRatio;
                     doki.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(scaledHP);
                     doki.setHealth((float)scaledHP);
                  }

                  if (doki.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                     double scaledDmg = config.getAttackDamage() * tierRatio;
                     doki.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(scaledDmg);
                  }

                  doki.setAttackTarget(target);
                  doki.enablePersistence();
                  NBTTagCompound nbt = doki.getEntityData();
                  nbt.setBoolean("tayuyaDoki", true);
                  if (this.getEntityData().getBoolean("questEntity")) {
                     nbt.setBoolean("questEntity", true);
                     nbt.setString("questId", this.getEntityData().getString("questId"));
                     String parentOwner = this.getEntityData().getString("ownerUUID");
                     if (parentOwner != null && !parentOwner.isEmpty()) {
                        nbt.setString("ownerUUID", parentOwner);
                     }

                     String parentSharedQuestId = this.getEntityData().getString("sharedQuestId");
                     if (parentSharedQuestId != null && !parentSharedQuestId.isEmpty()) {
                        nbt.setString("sharedQuestId", parentSharedQuestId);
                     }
                  }

                  if (this.getEntityData().getBoolean("endgameEntity")) {
                     nbt.setBoolean("endgameEntity", true);
                     nbt.setString("instanceKey", this.getEntityData().getString("instanceKey"));
                  }

                  if (this.getEntityData().getBoolean("akatsukiMissionEntity")) {
                     nbt.setBoolean("akatsukiMissionEntity", true);
                     nbt.setString("missionOwnerId", this.getEntityData().getString("missionOwnerId"));
                     nbt.setString("missionTemplateId", this.getEntityData().getString("missionTemplateId"));
                  }

                  doki.getEntityData().setDouble("dokiOriginX", this.posX);
                  doki.getEntityData().setDouble("dokiOriginZ", this.posZ);
                  this.world.spawnEntity(doki);
                  this.tayuyaDokiEntities.add(doki);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, spawnX, spawnY + (double)1.0F, spawnZ, 20, 0.4, 0.6, 0.4, 0.08, new int[0]);
                  }
               }
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 0.8F);
         this.tayuyaBroadcast("§5Tayuya: §dCome forth, my Doki!");
      }

      private void tayuyaCommandDoki(EntityLivingBase target) {
         for(Entity e : this.tayuyaDokiEntities) {
            if (e instanceof EntityCreature && e.isEntityAlive()) {
               ((EntityCreature)e).setAttackTarget(target);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.HOSTILE, 1.0F, 1.5F);
      }

      private void tayuyaCastPhantomSoundChains(EntityLivingBase target) {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.HOSTILE, 1.5F, 0.4F);
         if (this.tayuyaCurseMark1) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 1));
            target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 0));
         } else {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 0));
            target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
         }

         if (this.world instanceof WorldServer) {
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > (double)0.0F) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.NOTE, this.posX + dx * 0.3, this.posY + (double)1.5F + dy * 0.3, this.posZ + dz * 0.3, 8, 0.3, 0.3, 0.3, (double)0.5F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.NOTE, this.posX + dx * 0.6, this.posY + (double)1.5F + dy * 0.6, this.posZ + dz * 0.6, 8, 0.3, 0.3, 0.3, (double)0.5F, new int[0]);
            }

            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 15, 0.4, 0.6, 0.4, 0.05, new int[0]);
         }

         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§5§o* Tayuya's melody pierces your mind... *"));
         }

      }

      private void tayuyaCastFluteMelody(EntityLivingBase target) {
         float atkDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmgScale = this.tayuyaCurseMark2 ? 0.8F : (this.tayuyaCurseMark1 ? 0.6F : 0.4F);
         float damage = atkDmg * dmgScale * (float)this.getDamageMultiplier();
         target.attackEntityFrom(DamageSource.MAGIC, damage);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.HOSTILE, 1.3F, 1.2F);
         if (this.world instanceof WorldServer) {
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;

            for(int step = 1; step <= 5; ++step) {
               double t = (double)step / (double)5.0F;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.NOTE, this.posX + dx * t, this.posY + (double)1.5F + dy * t, this.posZ + dz * t, 3, 0.1, 0.1, 0.1, (double)0.5F, new int[0]);
            }
         }

      }

      private void tayuyaRetreat(EntityLivingBase target) {
         double distFromSpawn = this.getDistance(this.spawnOriginX, this.spawnOriginY, this.spawnOriginZ);
         if (!Double.isNaN(this.spawnOriginX) && distFromSpawn > (double)40.0F) {
            this.getNavigator().clearPath();
            this.setAttackTarget(target);
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
         } else {
            double fleeX = this.posX + (this.posX - target.posX) * 0.8;
            double fleeZ = this.posZ + (this.posZ - target.posZ) * 0.8;
            this.getNavigator().tryMoveToXYZ(fleeX, this.posY, fleeZ, (double)1.5F);
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
         }

      }

      private void tayuyaBoostDoki(float damageBoostPercent, float speedBoostPercent) {
         for(Entity e : this.tayuyaDokiEntities) {
            if (e instanceof EntityLivingBase && e.isEntityAlive()) {
               EntityLivingBase doki = (EntityLivingBase)e;
               if (doki.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                  double curDmg = doki.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                  doki.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(curDmg * ((double)1.0F + (double)damageBoostPercent));
               }

               if (doki.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
                  double curSpd = doki.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
                  doki.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpd * ((double)1.0F + (double)speedBoostPercent));
               }
            }
         }

      }

      private void tayuyaOnDeath() {
         if (!this.tayuyaDokiEntities.isEmpty()) {
            this.tayuyaBoostDoki(0.5F, 0.2F);
            this.tayuyaBroadcast("§4§lThe Doki spirits rage uncontrollably!");

            for(Entity e : this.tayuyaDokiEntities) {
               if (e instanceof EntityLivingBase && e.isEntityAlive() && this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, e.posX, e.posY + (double)1.0F, e.posZ, 10, 0.3, 0.3, 0.3, 0.05, new int[0]);
               }
            }

         }
      }

      private void tayuyaBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
