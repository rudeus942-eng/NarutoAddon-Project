
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityBarrierGuardNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 264;

   public EntityBarrierGuardNPC(ElementsInfTsukAddon instance) {
      super(instance, 264);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "barrierguard"), 264).name("barrierguard").tracker(64, 3, true).egg(-1, -1).build());
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
      private int barrierShieldCD = 0;
      private boolean barrierShieldActive = false;
      private int barrierShieldTicks = 0;
      private int barrierCounterCD = 0;
      private int barrierEarthWallCD = 0;
      private int barrierMeleeCD = 0;
      private int barrierKunaiCD = 0;
      private int barrierJutsuCD = 0;
      private static final int BARRIER_SHIELD_CD_BASE = 120;
      private static final int BARRIER_SHIELD_DURATION = 60;
      private static final float BARRIER_SHIELD_DR = 0.4F;
      private static final int BARRIER_COUNTER_CD_BASE = 80;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (this.barrierJutsuCD <= 0 && this.rand.nextFloat() < 0.15F) {
            this.barrierJutsuCD = 40 + this.rand.nextInt(40);
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float total = baseDmg * 1.5F * (float)dmgMul;
            float nDmg = this.trueDamageSplit > 0.0F ? total * (1.0F - this.trueDamageSplit) : total;
            float tDmg = this.trueDamageSplit > 0.0F ? total * this.trueDamageSplit * this.trueDamageMultiplier : 6.0F;
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            int jutsuType = this.rand.nextInt(3);
            if (jutsuType == 0) {
               EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(this.world, this, nDmg, tDmg);
               fb.shoot(dx, dy, dz, 1.2F, 2.0F);
               this.world.spawnEntity(fb);
            } else if (jutsuType == 1) {
               EntityWaterDragonJutsu.EntityCustom wd = new EntityWaterDragonJutsu.EntityCustom(this.world, this, nDmg, tDmg);
               wd.shoot(dx, dy, dz, 1.0F, 1.5F);
               this.world.spawnEntity(wd);
            } else {
               EntityRaitonBeam.EntityCustom rb = new EntityRaitonBeam.EntityCustom(this.world, this, nDmg, tDmg);
               rb.shoot(dx, dy, dz, 1.4F, 1.5F);
               this.world.spawnEntity(rb);
            }

            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 1.2, this.posZ, 10, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 0.8F, 1.5F);
         } else {
            if (this.barrierMeleeCD <= 0 && dist <= (double)3.0F) {
               this.performMeleeSwing(target);
               this.barrierMeleeCD = 14 + this.rand.nextInt(8);
               this.meleeCooldown = this.barrierMeleeCD;
            }

            if (dist > (double)3.0F && dist < (double)20.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, 1.1);
            }

            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
         }
      }

      protected void tickStyleCooldowns() {
         if (this.barrierShieldCD > 0) {
            --this.barrierShieldCD;
         }

         if (this.barrierCounterCD > 0) {
            --this.barrierCounterCD;
         }

         if (this.barrierEarthWallCD > 0) {
            --this.barrierEarthWallCD;
         }

         if (this.barrierMeleeCD > 0) {
            --this.barrierMeleeCD;
         }

         if (this.barrierKunaiCD > 0) {
            --this.barrierKunaiCD;
         }

         if (this.barrierJutsuCD > 0) {
            --this.barrierJutsuCD;
         }

         if (this.barrierShieldTicks > 0) {
            --this.barrierShieldTicks;
            if (this.barrierShieldTicks <= 0) {
               this.barrierShieldActive = false;
            }
         }

      }

      protected void resetCombatState() {
         this.barrierShieldActive = false;
         this.barrierKunaiCD = 0;
         this.barrierShieldCD = 0;
         this.barrierCounterCD = 0;
         this.barrierEarthWallCD = 0;
         this.barrierMeleeCD = 0;
         this.barrierJutsuCD = 0;
         this.barrierShieldTicks = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("barrierShieldActive", this.barrierShieldActive);
         compound.setInteger("barrierShieldTicks", this.barrierShieldTicks);
         compound.setInteger("barrierShieldCD", this.barrierShieldCD);
         compound.setInteger("barrierCounterCD", this.barrierCounterCD);
         compound.setInteger("barrierEarthWallCD", this.barrierEarthWallCD);
         compound.setInteger("barrierMeleeCD", this.barrierMeleeCD);
         compound.setInteger("barrierKunaiCD", this.barrierKunaiCD);
         compound.setInteger("barrierJutsuCD", this.barrierJutsuCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.barrierShieldActive = compound.getBoolean("barrierShieldActive");
         this.barrierShieldTicks = compound.hasKey("barrierShieldTicks") ? compound.getInteger("barrierShieldTicks") : 0;
         this.barrierShieldCD = compound.hasKey("barrierShieldCD") ? compound.getInteger("barrierShieldCD") : 0;
         this.barrierCounterCD = compound.hasKey("barrierCounterCD") ? compound.getInteger("barrierCounterCD") : 0;
         this.barrierEarthWallCD = compound.hasKey("barrierEarthWallCD") ? compound.getInteger("barrierEarthWallCD") : 0;
         this.barrierMeleeCD = compound.hasKey("barrierMeleeCD") ? compound.getInteger("barrierMeleeCD") : 0;
         this.barrierKunaiCD = compound.hasKey("barrierKunaiCD") ? compound.getInteger("barrierKunaiCD") : 0;
         this.barrierJutsuCD = compound.hasKey("barrierJutsuCD") ? compound.getInteger("barrierJutsuCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.barrierShieldActive) {
            amount *= 0.4F;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)1.0F, this.posZ, 6, 0.3, 0.3, 0.3, (double)0.0F, new int[]{Block.getStateId(Blocks.IRON_BLOCK.getDefaultState())});
            }

            Entity trueSource = source.getTrueSource();
            if (this.barrierCounterCD <= 0 && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) <= (double)4.0F) {
               this.barrierCounterCD = this.cdMul(80);
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float counterDmg;
               float counterTrue;
               if (this.trueDamageSplit > 0.0F) {
                  counterDmg = baseDmg * 1.5F * (1.0F - this.trueDamageSplit);
                  counterTrue = baseDmg * 1.5F * this.trueDamageSplit * this.trueDamageMultiplier;
               } else {
                  counterDmg = baseDmg * 1.5F;
                  counterTrue = baseDmg * 0.3F;
               }

               ((EntityLivingBase)trueSource).attackEntityFrom(DamageSource.causeMobDamage(this), counterDmg);
               ((EntityLivingBase)trueSource).hurtResistantTime = 0;
               ((EntityLivingBase)trueSource).attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), counterTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 0.6F, 0.8F);
            }
         }

         return amount;
      }
   }
}
