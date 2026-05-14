
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityPuppetAllyNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 263;

   public EntityPuppetAllyNPC(ElementsInfTsukAddon instance) {
      super(instance, 263);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "puppetally"), 263).name("puppetally").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CustomRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CustomRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/anko.png");

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
      private int puppetAllyHealCD = 0;
      private int puppetAllyMeleeCD = 0;
      private static final int PUPPET_ALLY_HEAL_CD_BASE = 200;

      public EntityCustom(World world) {
         super(world);
      }

      public void setAttackTarget(@Nullable EntityLivingBase target) {
         if (!(target instanceof EntityPlayer)) {
            super.setAttackTarget(target);
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         return trueSource instanceof EntityPlayer ? false : super.attackEntityFrom(source, amount);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processPuppetAllyBehavior();
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote && !this.isPassive && !this.questRetreating) {
            EntityLivingBase currentTarget = this.getAttackTarget();
            if (currentTarget == null || !currentTarget.isEntityAlive()) {
               this.processPuppetAllyBehavior();
            }
         }

      }

      private void processPuppetAllyBehavior() {
         EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, (double)30.0F);
         if (nearestPlayer != null && nearestPlayer.isEntityAlive()) {
            if (this.puppetAllyHealCD <= 0 && nearestPlayer.getHealth() < nearestPlayer.getMaxHealth() * 0.5F && (double)this.getDistance(nearestPlayer) <= (double)10.0F) {
               this.puppetAllyHealCD = this.cdMul(200);
               nearestPlayer.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60, 0));
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, nearestPlayer.posX, nearestPlayer.posY + (double)1.0F, nearestPlayer.posZ, 12, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);
                  double dx = nearestPlayer.posX - this.posX;
                  double dz = nearestPlayer.posZ - this.posZ;

                  for(int i = 0; i < 8; ++i) {
                     double t = (double)i / (double)8.0F;
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + dx * t, this.posY + (double)1.0F + (nearestPlayer.posY - this.posY) * t, this.posZ + dz * t, 1, 0.05, 0.05, 0.05, (double)0.0F, new int[0]);
                  }
               }
            }

            double playerDist = (double)this.getDistance(nearestPlayer);
            if (playerDist > (double)12.0F) {
               this.getNavigator().tryMoveToEntityLiving(nearestPlayer, 1.2);
            }

            List<EntityMob> hostiles = this.world.getEntitiesWithinAABB(EntityMob.class, nearestPlayer.getEntityBoundingBox().grow((double)15.0F), (e) -> e != null && e.isEntityAlive() && !QuestNpcBase.class.isAssignableFrom(e.getClass()) && !e.getClass().getName().contains("EntityQuestNPC"));
            if (!hostiles.isEmpty()) {
               EntityMob closestHostile = null;
               double closestDist = Double.MAX_VALUE;

               for(EntityMob mob : hostiles) {
                  double d = nearestPlayer.getDistanceSq(mob);
                  if (d < closestDist) {
                     closestDist = d;
                     closestHostile = mob;
                  }
               }

               if (closestHostile != null) {
                  double hostileDist = (double)this.getDistance(closestHostile);
                  this.getNavigator().tryMoveToEntityLiving(closestHostile, 1.1);
                  this.getLookHelper().setLookPositionWithEntity(closestHostile, 30.0F, 30.0F);
                  if (this.puppetAllyMeleeCD <= 0 && hostileDist <= (double)3.0F) {
                     float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                     float allyTotal = baseDmg * 0.8F;
                     if (this.trueDamageSplit > 0.0F) {
                        float allyNorm = allyTotal * (1.0F - this.trueDamageSplit);
                        float allyTrue = allyTotal * this.trueDamageSplit * this.trueDamageMultiplier;
                        closestHostile.attackEntityFrom(DamageSource.causeMobDamage(this), allyNorm);
                        closestHostile.hurtResistantTime = 0;
                        closestHostile.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), allyTrue);
                     } else {
                        closestHostile.attackEntityFrom(DamageSource.causeMobDamage(this), allyTotal);
                     }

                     this.swingArm(EnumHand.MAIN_HAND);
                     this.puppetAllyMeleeCD = 15 + this.rand.nextInt(8);
                  }
               }
            } else if (playerDist > (double)5.0F) {
               this.getNavigator().tryMoveToEntityLiving(nearestPlayer, (double)1.0F);
            }

         }
      }

      protected void tickStyleCooldowns() {
         if (this.puppetAllyHealCD > 0) {
            --this.puppetAllyHealCD;
         }

         if (this.puppetAllyMeleeCD > 0) {
            --this.puppetAllyMeleeCD;
         }

      }

      protected void resetCombatState() {
         this.puppetAllyHealCD = 0;
         this.puppetAllyMeleeCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("puppetAllyHealCD", this.puppetAllyHealCD);
         compound.setInteger("puppetAllyMeleeCD", this.puppetAllyMeleeCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.puppetAllyHealCD = compound.hasKey("puppetAllyHealCD") ? compound.getInteger("puppetAllyHealCD") : 0;
         this.puppetAllyMeleeCD = compound.hasKey("puppetAllyMeleeCD") ? compound.getInteger("puppetAllyMeleeCD") : 0;
      }
   }
}
