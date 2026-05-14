
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
import net.narutomod.entity.EntityPoisonMist;
import net.narutomod.entity.EntityPuppet3rdKazekage;
import net.narutomod.entity.EntityPuppetKarasu;
import net.narutomod.item.ItemPoisonSenbon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySasoriBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 259;

   public EntitySasoriBoss(ElementsInfTsukAddon instance) {
      super(instance, 259);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sasoriboss"), 259).name("sasoriboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SasoriBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SasoriBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SasoriBossRenderer(RenderManager renderManager) {
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
      private int sasoriPhase = 1;
      private List<Entity> sasori_spawnedPuppets = new ArrayList();
      private int sasori_puppetResummons = 0;
      private static final int SASORI_MAX_PUPPET_RESUMMONS = 2;
      private boolean sasori_phase2Announced = false;
      private boolean sasori_phase3Announced = false;
      private int sasori_strafeTimer = 0;
      private boolean sasori_strafingRight = false;
      private boolean sasori_isDashing = false;
      private int sasori_dashTicksRemaining = 0;
      private double sasori_dashVelX;
      private double sasori_dashVelZ;
      private int sasori_tailStabCD = 0;
      private int sasori_senbonCD = 0;
      private int sasori_stringPullCD = 0;
      private int sasori_puppetSpawnCD = 0;
      private int sasori_ironSandCD = 0;
      private int sasori_poisonGasCD = 0;
      private int sasori_dashCooldown = 0;
      private int sasori_shieldCooldown = 0;
      private int sasori_ironSandWallCooldown = 0;
      private int sasori_meleeCD = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.sasori_shieldCooldown <= 0 && !this.world.isRemote && this.getRNG().nextFloat() < 0.15F) {
            this.sasori_shieldCooldown = this.sasoriCd(100, 160);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, 0.8, (double)0.5F, 0.02, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + 1.2, this.posZ, 8, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.HOSTILE, 1.2F, 0.8F);
            Entity attacker = source.getTrueSource();
            if (attacker instanceof EntityLivingBase) {
               try {
                  ItemPoisonSenbon.spawnArrow(this, new Vec3d(attacker.posX, attacker.posY + (double)attacker.getEyeHeight(), attacker.posZ));
               } catch (Exception e) {
                  System.err.println("[InfTsuk] Sasori failed to spawn counter senbon: " + e.getMessage());
               }
            }

            return -1.0F;
         } else {
            return amount;
         }
      }

      public void onCombatDeath() {
         this.sasoriKillPuppets();
      }

      protected void onDeathUpdate() {
         this.sasoriKillPuppets();
         super.onDeathUpdate();
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            this.sasoriCheckPhase();
            if (this.sasoriPhase >= 2 && this.ticksExisted % 20 == 0) {
               this.sasoriCheckPuppets();
            }

            this.sasoriMove(target, dist);
            switch (this.sasoriPhase) {
               case 1:
                  this.sasoriP1(target, dist);
                  break;
               case 2:
                  this.sasoriP2(target, dist);
                  break;
               case 3:
                  this.sasoriP3(target, dist);
            }

         }
      }

      protected void tickStyleCooldowns() {
         if (this.sasori_tailStabCD > 0) {
            --this.sasori_tailStabCD;
         }

         if (this.sasori_senbonCD > 0) {
            --this.sasori_senbonCD;
         }

         if (this.sasori_stringPullCD > 0) {
            --this.sasori_stringPullCD;
         }

         if (this.sasori_puppetSpawnCD > 0) {
            --this.sasori_puppetSpawnCD;
         }

         if (this.sasori_ironSandCD > 0) {
            --this.sasori_ironSandCD;
         }

         if (this.sasori_poisonGasCD > 0) {
            --this.sasori_poisonGasCD;
         }

         if (this.sasori_dashCooldown > 0) {
            --this.sasori_dashCooldown;
         }

         if (this.sasori_shieldCooldown > 0) {
            --this.sasori_shieldCooldown;
         }

         if (this.sasori_ironSandWallCooldown > 0) {
            --this.sasori_ironSandWallCooldown;
         }

         if (this.sasori_meleeCD > 0) {
            --this.sasori_meleeCD;
         }

         if (this.sasori_isDashing) {
            --this.sasori_dashTicksRemaining;
            if (this.sasori_dashTicksRemaining <= 0) {
               this.sasori_isDashing = false;
               return;
            }

            this.motionX = this.sasori_dashVelX * 0.92;
            this.motionZ = this.sasori_dashVelZ * 0.92;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + 0.8, this.posZ, 3, 0.15, 0.3, 0.15, (double)0.0F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)0.5F, this.posZ, 2, 0.2, 0.2, 0.2, 0.01, new int[0]);
            }
         }

         if (this.sasoriPhase >= 2 && this.ticksExisted % 10 == 0 && !this.sasori_spawnedPuppets.isEmpty() && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(Entity puppet : this.sasori_spawnedPuppets) {
               if (puppet != null && puppet.isEntityAlive()) {
                  double dx = puppet.posX - this.posX;
                  double dy = puppet.posY + (double)1.0F - (this.posY + (double)1.0F);
                  double dz = puppet.posZ - this.posZ;

                  for(int i = 0; i < 8; ++i) {
                     double t = (double)i / (double)8.0F;
                     ws.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + dx * t + (this.rand.nextDouble() - (double)0.5F) * 0.1, this.posY + (double)1.0F + dy * t, this.posZ + dz * t + (this.rand.nextDouble() - (double)0.5F) * 0.1, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }
            }
         }

      }

      protected void resetCombatState() {
         this.sasoriPhase = 1;
         this.sasori_puppetResummons = 0;
         this.sasori_phase2Announced = false;
         this.sasori_phase3Announced = false;
         this.sasori_strafeTimer = 0;
         this.sasori_isDashing = false;
         this.sasori_dashTicksRemaining = 0;
         this.sasori_tailStabCD = 0;
         this.sasori_senbonCD = 0;
         this.sasori_stringPullCD = 0;
         this.sasori_puppetSpawnCD = 0;
         this.sasori_ironSandCD = 0;
         this.sasori_poisonGasCD = 0;
         this.sasori_dashCooldown = 0;
         this.sasori_shieldCooldown = 0;
         this.sasori_ironSandWallCooldown = 0;
         this.sasori_meleeCD = 0;
         this.sasoriKillPuppets();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("sasoriPhase", this.sasoriPhase);
         compound.setBoolean("sasori_phase2Announced", this.sasori_phase2Announced);
         compound.setBoolean("sasori_phase3Announced", this.sasori_phase3Announced);
         compound.setInteger("sasori_puppetResummons", this.sasori_puppetResummons);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.sasoriPhase = compound.hasKey("sasoriPhase") ? compound.getInteger("sasoriPhase") : 1;
         this.sasori_phase2Announced = compound.getBoolean("sasori_phase2Announced");
         this.sasori_phase3Announced = compound.getBoolean("sasori_phase3Announced");
         this.sasori_puppetResummons = compound.hasKey("sasori_puppetResummons") ? compound.getInteger("sasori_puppetResummons") : 0;
      }

      private void sasoriCheckPhase() {
         float hp = this.getHealth() / this.getMaxHealth();
         if (this.sasoriPhase == 1 && hp <= 0.65F) {
            this.sasoriPhase = 2;
            this.sasoriToP2();
         } else if (this.sasoriPhase == 2 && hp <= 0.3F) {
            this.sasoriPhase = 3;
            this.sasoriToP3();
         }

      }

      private void sasoriToP2() {
         if (!this.sasori_phase2Announced) {
            this.sasori_phase2Announced = true;
            this.sasoriMsg("§c§lSasori: §7\"Hiruko was merely a shell... now witness true art.\"");
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(Math.max((double)0.0F, this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue() * (double)0.5F));
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() * 1.15);
            this.sasori_puppetResummons = 0;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.25F, this.posZ, 40, (double)1.5F, (double)1.25F, (double)1.5F, 0.05, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.5F, this.posZ, 25, (double)2.0F, (double)1.5F, (double)2.0F, 0.02, new int[0]);
            }

            this.sasoriSpawn(2 + this.rand.nextInt(2));
         }
      }

      private void sasoriToP3() {
         if (!this.sasori_phase3Announced) {
            this.sasori_phase3Announced = true;
            this.sasoriMsg("§4§lSasori: §7\"Behold the power of the Third Kazekage... Iron Sand!\"");
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(Math.max((double)0.0F, this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue() * 0.6));
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() * 1.12);
            this.sasori_puppetResummons = 0;
            this.sasoriKillPuppets();
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + (double)1.5F, this.posZ, 60, (double)3.0F, (double)2.0F, (double)3.0F, 0.01, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)2.0F, (double)1.0F, (double)2.0F, 0.02, new int[0]);
            }

            this.sasoriSpawn(4);
         }
      }

      private void sasoriSpawn(int count) {
         if (!this.world.isRemote) {
            EntityLivingBase ct = this.getAttackTarget();

            try {
               boolean spawnKazekage = this.sasoriPhase >= 2;
               int karasuCount = spawnKazekage ? count - 1 : count;
               if (spawnKazekage) {
                  double a = (double)0.0F;
                  double sx = this.posX + Math.cos(a) * (double)4.0F;
                  double sz = this.posZ + Math.sin(a) * (double)4.0F;
                  double sy = this.sasoriGround(sx, this.posY, sz);
                  EntityPuppet3rdKazekage.EntityCustom kazekage = new EntityPuppet3rdKazekage.EntityCustom(this, (double)0.0F);
                  kazekage.setPosition(sx, sy, sz);
                  if (ct != null) {
                     kazekage.setAttackTarget(ct);
                  }

                  this.world.spawnEntity(kazekage);
                  this.sasori_spawnedPuppets.add(kazekage);
                  NBTTagCompound puppetNbt = kazekage.getEntityData();
                  puppetNbt.setBoolean("endgameEntity", true);
                  puppetNbt.setBoolean("sasoriPuppet", true);
                  String instKey = this.getEntityData().getString("instanceKey");
                  if (!instKey.isEmpty()) {
                     puppetNbt.setString("instanceKey", instKey);
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, sy + (double)1.0F, sz, 15, (double)0.75F, (double)1.0F, (double)0.75F, 0.02, new int[0]);
                  }
               }

               for(int i = 0; i < karasuCount; ++i) {
                  double a = (Math.PI * 2D) / (double)count * (double)(spawnKazekage ? i + 1 : i);
                  double sx = this.posX + Math.cos(a) * (double)4.0F;
                  double sz = this.posZ + Math.sin(a) * (double)4.0F;
                  double sy = this.sasoriGround(sx, this.posY, sz);
                  EntityPuppetKarasu.EntityCustom karasu = new EntityPuppetKarasu.EntityCustom(this, (double)0.0F);
                  karasu.setPosition(sx, sy, sz);
                  if (ct != null) {
                     karasu.setAttackTarget(ct);
                  }

                  this.world.spawnEntity(karasu);
                  this.sasori_spawnedPuppets.add(karasu);
                  NBTTagCompound karasuNbt = karasu.getEntityData();
                  karasuNbt.setBoolean("endgameEntity", true);
                  karasuNbt.setBoolean("sasoriPuppet", true);
                  String kInstKey = this.getEntityData().getString("instanceKey");
                  if (!kInstKey.isEmpty()) {
                     karasuNbt.setString("instanceKey", kInstKey);
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, sy + (double)1.0F, sz, 15, (double)0.75F, (double)1.0F, (double)0.75F, 0.02, new int[0]);
                  }
               }
            } catch (Exception e) {
               System.err.println("[Sasori] Failed to spawn AHZNB puppets: " + e.getMessage());
            }

            this.sasori_puppetSpawnCD = this.sasoriCd(100, 160);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.2F, 0.6F);
         }
      }

      private void sasoriKillPuppets() {
         for(Entity p : this.sasori_spawnedPuppets) {
            if (p != null && p.isEntityAlive()) {
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, p.posX, p.posY + (double)1.0F, p.posZ, 10, (double)0.5F, (double)1.0F, (double)0.5F, 0.02, new int[0]);
               }

               p.setDead();
            }
         }

         this.sasori_spawnedPuppets.clear();
      }

      private void sasoriCheckPuppets() {
         Iterator<Entity> it = this.sasori_spawnedPuppets.iterator();

         while(it.hasNext()) {
            Entity p = (Entity)it.next();
            if (p == null || p.isDead || !p.isEntityAlive()) {
               it.remove();
            }
         }

         if (this.sasori_spawnedPuppets.isEmpty() && this.sasori_puppetResummons < 2 && this.sasori_puppetSpawnCD <= 0) {
            ++this.sasori_puppetResummons;
            this.sasoriSpawn(this.sasoriPhase == 2 ? 2 + this.rand.nextInt(2) : 4);
         }

      }

      private void sasoriMove(EntityLivingBase target, double dist) {
         if (!this.sasori_isDashing) {
            ++this.sasori_strafeTimer;
            if (this.sasoriPhase == 1) {
               if (dist > (double)12.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
               } else if (dist < (double)5.0F) {
                  double dx = this.posX - target.posX;
                  double dz = this.posZ - target.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  if (d > (double)0.0F) {
                     this.getNavigator().tryMoveToXYZ(this.posX + dx / d * (double)6.0F, this.posY, this.posZ + dz / d * (double)6.0F, (double)1.0F);
                  }
               } else if (dist < (double)8.0F) {
                  if (this.sasori_strafeTimer > 40) {
                     this.sasori_strafingRight = !this.sasori_strafingRight;
                     this.sasori_strafeTimer = 0;
                  }

                  double a = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
                  this.getNavigator().tryMoveToXYZ(this.posX + (double)(this.sasori_strafingRight ? -1 : 1) * Math.sin(a) * (double)4.0F, this.posY, this.posZ + (double)(this.sasori_strafingRight ? 1 : -1) * Math.cos(a) * (double)4.0F, 0.8);
               }
            } else if (this.sasoriPhase == 2) {
               if (dist > (double)20.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
               } else if (dist < (double)10.0F) {
                  double dx = this.posX - target.posX;
                  double dz = this.posZ - target.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  if (d > (double)0.0F) {
                     this.getNavigator().tryMoveToXYZ(this.posX + dx / d * (double)8.0F, this.posY, this.posZ + dz / d * (double)8.0F, 1.1);
                  }
               } else {
                  if (this.sasori_strafeTimer > 35) {
                     this.sasori_strafingRight = !this.sasori_strafingRight;
                     this.sasori_strafeTimer = 0;
                  }

                  double a = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
                  this.getNavigator().tryMoveToXYZ(this.posX + (double)(this.sasori_strafingRight ? -1 : 1) * Math.sin(a) * (double)5.0F, this.posY, this.posZ + (double)(this.sasori_strafingRight ? 1 : -1) * Math.cos(a) * (double)5.0F, 0.9);
               }
            } else if (dist > (double)10.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, 1.4);
            } else if (dist > (double)4.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, 1.2);
            } else {
               this.getNavigator().clearPath();
            }

         }
      }

      private void sasoriP1(EntityLivingBase target, double dist) {
         if (this.sasori_dashCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.sasoriPuppetStringDash(target);
         } else if (this.sasori_senbonCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.sasoriSenbon(target);
         } else if (this.sasori_tailStabCD <= 0 && dist <= (double)4.0F) {
            this.sasoriTailStab(target);
         }
      }

      private void sasoriP2(EntityLivingBase target, double dist) {
         if (this.sasori_spawnedPuppets.isEmpty() && this.sasori_puppetResummons < 2 && this.sasori_puppetSpawnCD <= 0) {
            ++this.sasori_puppetResummons;
            this.sasoriSpawn(2 + this.rand.nextInt(2));
         } else if (this.sasori_dashCooldown <= 0 && dist >= (double)10.0F && dist <= (double)25.0F) {
            this.sasoriPuppetStringDash(target);
         } else if (this.sasori_stringPullCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F) {
            this.sasoriString(target);
         } else if (this.sasori_senbonCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.sasoriSenbon(target);
         }
      }

      private void sasoriP3(EntityLivingBase target, double dist) {
         if (this.sasori_spawnedPuppets.isEmpty() && this.sasori_puppetResummons < 2 && this.sasori_puppetSpawnCD <= 0) {
            ++this.sasori_puppetResummons;
            this.sasoriSpawn(4);
         } else if (this.sasori_ironSandWallCooldown <= 0 && dist <= (double)3.0F) {
            this.sasoriIronSandWall(target);
         } else if (this.sasori_ironSandCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.sasoriIronSand(target);
         } else if (this.sasori_poisonGasCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F) {
            this.sasoriPoisonGas(target);
         } else if (this.sasori_dashCooldown <= 0 && dist >= (double)10.0F && dist <= (double)25.0F) {
            this.sasoriPuppetStringDash(target);
         } else if (this.sasori_senbonCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.sasoriSenbon(target);
         } else if (this.sasori_meleeCD <= 0 && dist <= (double)3.0F) {
            this.sasoriMelee(target);
         }
      }

      private void sasoriTailStab(EntityLivingBase target) {
         this.sasori_tailStabCD = this.sasoriCd(30, 50);
         double dm = this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)10.0F * dm));
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, (float)((double)3.0F * dm));
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;

            for(int i = 0; i < 8; ++i) {
               double t = (double)i / (double)8.0F;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + dx * t, this.posY + (double)1.0F, this.posZ + dz * t, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.7F);
      }

      private void sasoriSenbon(EntityLivingBase target) {
         this.sasori_senbonCD = this.sasoriCd(60, 90);
         int cnt = 3 + this.rand.nextInt(3);

         try {
            Vec3d targetVec = new Vec3d(target.posX, target.posY + (double)target.getEyeHeight(), target.posZ);

            for(int i = 0; i < cnt; ++i) {
               double sa = ((double)i - (double)cnt / (double)2.0F) * 0.8;
               Vec3d spreadVec = targetVec.add(sa, (double)0.0F, sa * (double)0.5F);
               ItemPoisonSenbon.spawnArrow(this, spreadVec);
            }
         } catch (Exception e) {
            System.err.println("[InfTsuk] Sasori failed to spawn senbon barrage: " + e.getMessage());
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + 1.4, this.posZ, 12, (double)0.5F, (double)0.25F, (double)0.5F, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.5F);
      }

      private void sasoriString(EntityLivingBase target) {
         this.sasori_stringPullCD = this.sasoriCd(40, 60);
         double dm = this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)6.0F * dm));
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, (float)((double)2.0F * dm));
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         }

         double px = this.posX - target.posX;
         double pz = this.posZ - target.posZ;
         double pd = Math.sqrt(px * px + pz * pz);
         if (pd > (double)1.0F) {
            target.motionX += px / pd * 0.35;
            target.motionZ += pz / pd * 0.35;
            target.velocityChanged = true;
         }

         double dx = target.posX - this.posX;
         double dy = target.posY + (double)1.0F - (this.posY + (double)1.0F);
         double dz = target.posZ - this.posZ;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 15; ++i) {
               double t = (double)i / (double)15.0F;
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + dx * t, this.posY + (double)1.0F + dy * t, this.posZ + dz * t, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + dx * t + (this.rand.nextDouble() - (double)0.5F) * 0.2, this.posY + (double)1.0F + dy * t, this.posZ + dz * t + (this.rand.nextDouble() - (double)0.5F) * 0.2, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.HOSTILE, 1.0F, 1.8F);
      }

      private void sasoriIronSand(EntityLivingBase target) {
         this.sasori_ironSandCD = this.sasoriCd(50, 80);
         double dm = this.getDamageMultiplier();
         float nd = (float)((double)12.0F * dm);
         float td = (float)((double)4.0F * dm);

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, target.getEntityBoundingBox().grow((double)5.0F))) {
            if (e instanceof EntityPlayer) {
               EntityLivingBase hit = (EntityLivingBase)e;
               hit.hurtResistantTime = 0;
               hit.attackEntityFrom(DamageSource.causeMobDamage(this), nd);
               hit.hurtResistantTime = 0;
               hit.attackEntityFrom(DamageSource.MAGIC, td);
               hit.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
            }
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), nd);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, td);
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.REDSTONE, target.posX, target.posY + (double)1.0F, target.posZ, 50, (double)5.0F, (double)1.5F, (double)5.0F, 0.01, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)0.5F, target.posZ, 20, (double)4.0F, (double)0.5F, (double)4.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.HOSTILE, 1.0F, 0.5F);
         this.sasoriMsg("§8§lSasori: §7\"Iron Sand... Gathering Assault!\"");
      }

      private void sasoriPoisonGas(EntityLivingBase target) {
         this.sasori_poisonGasCD = this.sasoriCd(60, 100);

         try {
            EntityPoisonMist.EC mist = new EntityPoisonMist.EC(this, 30.0F);
            mist.setPosition(target.posX, target.posY, target.posZ);
            this.world.spawnEntity(mist);
         } catch (Exception var3) {
            if (target instanceof EntityLivingBase) {
               target.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
               target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 60, (double)4.0F, (double)1.5F, (double)4.0F, 0.02, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, target.posX, target.posY + (double)0.5F, target.posZ, 30, (double)3.0F, (double)1.0F, (double)3.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_CREEPER_DEATH, SoundCategory.HOSTILE, 1.0F, 1.5F);
      }

      private void sasoriPuppetStringDash(EntityLivingBase target) {
         if (!this.sasori_isDashing) {
            this.sasori_dashCooldown = this.sasoriCd(80, 120);
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist < (double)1.0F)) {
               double speed = (double)1.5F;
               this.sasori_dashVelX = dx / dist * speed;
               this.sasori_dashVelZ = dz / dist * speed;
               this.sasori_isDashing = true;
               this.sasori_dashTicksRemaining = 8;
               this.motionX = this.sasori_dashVelX;
               this.motionY = 0.15;
               this.motionZ = this.sasori_dashVelZ;
               this.velocityChanged = true;
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  double ddx = target.posX - this.posX;
                  double ddy = target.posY + (double)1.0F - (this.posY + (double)1.0F);
                  double ddz = target.posZ - this.posZ;

                  for(int i = 0; i < 12; ++i) {
                     double t = (double)i / (double)12.0F;
                     ws.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + ddx * t, this.posY + (double)1.0F + ddy * t, this.posZ + ddz * t, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }

                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.HOSTILE, 1.2F, 2.0F);
            }
         }
      }

      private void sasoriIronSandWall(EntityLivingBase target) {
         this.sasori_ironSandWallCooldown = this.sasoriCd(60, 100);
         double dm = this.getDamageMultiplier();
         float nd = (float)((double)8.0F * dm);
         float td = (float)((double)2.0F * dm);

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)3.5F))) {
            if (e instanceof EntityPlayer) {
               EntityLivingBase hit = (EntityLivingBase)e;
               hit.hurtResistantTime = 0;
               hit.attackEntityFrom(DamageSource.causeMobDamage(this), nd);
               hit.hurtResistantTime = 0;
               hit.attackEntityFrom(DamageSource.MAGIC, td);
               double kx = e.posX - this.posX;
               double kz = e.posZ - this.posZ;
               double kd = Math.sqrt(kx * kx + kz * kz);
               if (kd > (double)0.0F) {
                  e.motionX += kx / kd * 1.2;
                  e.motionY += 0.4;
                  e.motionZ += kz / kd * 1.2;
                  ((EntityLivingBase)e).velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 16; ++i) {
               double a = (Math.PI / 8D) * (double)i;
               ws.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + Math.cos(a) * (double)3.0F, this.posY + (double)1.0F, this.posZ + Math.sin(a) * (double)3.0F, 5, 0.2, (double)1.0F, 0.2, (double)0.0F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.5F, this.posZ, 15, (double)3.0F, (double)0.5F, (double)3.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1.0F, 0.6F);
      }

      private void sasoriMelee(EntityLivingBase target) {
         this.sasori_meleeCD = this.sasoriCd(15, 25);
         double dm = this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)8.0F * dm));
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, (float)((double)2.0F * dm));
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0));
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 6, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, 1.0F);
      }

      private int sasoriCd(int min, int max) {
         int[] r = this.getCooldownRange(min, max);
         return r[0] + this.rand.nextInt(Math.max(1, r[1] - r[0] + 1));
      }

      private void sasoriMsg(String msg) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString(msg));
         }

      }

      private double sasoriGround(double x, double refY, double z) {
         for(int y = Math.min((int)refY + 10, 255); y >= Math.max((int)refY - 20, 1); --y) {
            if (this.world.getBlockState(new BlockPos(x, (double)(y - 1), z)).getMaterial().isSolid() && !this.world.getBlockState(new BlockPos(x, (double)y, z)).getMaterial().isSolid() && !this.world.getBlockState(new BlockPos(x, (double)(y + 1), z)).getMaterial().isSolid()) {
               return (double)y;
            }
         }

         return this.posY;
      }
   }
}
