
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import net.narutomod.item.ItemPoisonSenbon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySasoriShippudenNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 265;

   public EntitySasoriShippudenNPC(ElementsInfTsukAddon instance) {
      super(instance, 265);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sasorishippuden"), 265).name("sasorishippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CustomRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CustomRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/sasori.png");

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
      private int sasoriPhase = 1;
      private boolean sasoriIntroPlayed = false;
      private int sasoriTailStabCD = 0;
      private int sasoriSenbonCD = 0;
      private int sasoriDashCD = 0;
      private int sasoriIronSandCD = 0;
      private int sasoriIronSandWallCD = 0;
      private int sasoriPoisonGasCD = 0;
      private int sasoriStringCD = 0;
      private int sasoriPuppetSpawnCD = 0;
      private int sasoriMeleeCD = 0;
      private int sasoriShieldCD = 0;
      private int sasoriPuppetResummons = 0;
      private List<Entity> sasoriSpawnedPuppets = new ArrayList();
      private static final float SASORI_PHASE_2 = 0.7F;
      private static final float SASORI_PHASE_3 = 0.3F;
      private static final int SASORI_MAX_PUPPET_RESUMMONS = 2;
      private static final int SASORI_TAIL_STAB_CD_BASE = 40;
      private static final int SASORI_SENBON_CD_BASE = 60;
      private static final int SASORI_DASH_CD_BASE = 120;
      private static final int SASORI_IRON_SAND_CD_BASE = 80;
      private static final int SASORI_IRON_SAND_WALL_CD_BASE = 200;
      private static final int SASORI_POISON_GAS_CD_BASE = 160;
      private static final int SASORI_STRING_CD_BASE = 100;
      private static final int SASORI_PUPPET_SPAWN_CD_BASE = 140;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         float hpFrac = this.getHealth() / this.getMaxHealth();
         if (this.sasoriPhase == 1 && hpFrac <= 0.7F) {
            this.sasoriPhase = 2;
            this.broadcastChat((double)40.0F, "§7[§cSasori§7] §fHiruko is merely a shell... Let me show you the 3rd Kazekage.");
            this.sasoriKillPuppets();
            this.sasoriPuppetResummons = 0;
            this.sasoriSpawnPuppets(2);
         } else if (this.sasoriPhase == 2 && hpFrac <= 0.3F) {
            this.sasoriPhase = 3;
            this.broadcastChat((double)50.0F, "§7[§cSasori§7] §c§lRed Secret Technique: Performance of a Hundred Puppets!");
            this.sasoriKillPuppets();
            this.sasoriPuppetResummons = 0;
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double curSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpeed * 1.3);
            }

            this.setTextureOverride("inftsukaddon:textures/sasoripuppetform.png");
            this.sasoriSpawnHundredPuppets(target);
         }

         if (!this.sasoriIntroPlayed && target instanceof EntityPlayerMP) {
            this.sasoriIntroPlayed = true;
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§7[§cSasori§7] §fI detest waiting... and making others wait."));
         }

         this.sasoriCheckPuppets();
         this.sasoriMovement(target, dist);
         if (this.sasoriPhase == 1) {
            this.sasoriPhase1Combat(target, dist);
         } else if (this.sasoriPhase == 2) {
            this.sasoriPhase2Combat(target, dist);
         } else {
            this.sasoriPhase3Combat(target, dist);
         }

      }

      protected void tickStyleCooldowns() {
         if (this.sasoriTailStabCD > 0) {
            --this.sasoriTailStabCD;
         }

         if (this.sasoriSenbonCD > 0) {
            --this.sasoriSenbonCD;
         }

         if (this.sasoriDashCD > 0) {
            --this.sasoriDashCD;
         }

         if (this.sasoriIronSandCD > 0) {
            --this.sasoriIronSandCD;
         }

         if (this.sasoriIronSandWallCD > 0) {
            --this.sasoriIronSandWallCD;
         }

         if (this.sasoriPoisonGasCD > 0) {
            --this.sasoriPoisonGasCD;
         }

         if (this.sasoriStringCD > 0) {
            --this.sasoriStringCD;
         }

         if (this.sasoriPuppetSpawnCD > 0) {
            --this.sasoriPuppetSpawnCD;
         }

         if (this.sasoriMeleeCD > 0) {
            --this.sasoriMeleeCD;
         }

         if (this.sasoriShieldCD > 0) {
            --this.sasoriShieldCD;
         }

      }

      protected void resetCombatState() {
         this.sasoriPhase = 1;
         this.sasoriIntroPlayed = false;
         this.sasoriPuppetResummons = 0;
         this.sasoriSpawnedPuppets.clear();
         this.sasoriTailStabCD = 0;
         this.sasoriSenbonCD = 0;
         this.sasoriDashCD = 0;
         this.sasoriIronSandCD = 0;
         this.sasoriIronSandWallCD = 0;
         this.sasoriPoisonGasCD = 0;
         this.sasoriStringCD = 0;
         this.sasoriPuppetSpawnCD = 0;
         this.sasoriMeleeCD = 0;
         this.sasoriShieldCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("sasoriPhase", this.sasoriPhase);
         compound.setBoolean("sasoriIntroPlayed", this.sasoriIntroPlayed);
         compound.setInteger("sasoriTailStabCD", this.sasoriTailStabCD);
         compound.setInteger("sasoriSenbonCD", this.sasoriSenbonCD);
         compound.setInteger("sasoriDashCD", this.sasoriDashCD);
         compound.setInteger("sasoriIronSandCD", this.sasoriIronSandCD);
         compound.setInteger("sasoriIronSandWallCD", this.sasoriIronSandWallCD);
         compound.setInteger("sasoriPoisonGasCD", this.sasoriPoisonGasCD);
         compound.setInteger("sasoriStringCD", this.sasoriStringCD);
         compound.setInteger("sasoriPuppetSpawnCD", this.sasoriPuppetSpawnCD);
         compound.setInteger("sasoriMeleeCD", this.sasoriMeleeCD);
         compound.setInteger("sasoriShieldCD", this.sasoriShieldCD);
         compound.setInteger("sasoriPuppetResummons", this.sasoriPuppetResummons);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.sasoriPhase = compound.hasKey("sasoriPhase") ? compound.getInteger("sasoriPhase") : 1;
         this.sasoriIntroPlayed = compound.getBoolean("sasoriIntroPlayed");
         this.sasoriTailStabCD = compound.hasKey("sasoriTailStabCD") ? compound.getInteger("sasoriTailStabCD") : 0;
         this.sasoriSenbonCD = compound.hasKey("sasoriSenbonCD") ? compound.getInteger("sasoriSenbonCD") : 0;
         this.sasoriDashCD = compound.hasKey("sasoriDashCD") ? compound.getInteger("sasoriDashCD") : 0;
         this.sasoriIronSandCD = compound.hasKey("sasoriIronSandCD") ? compound.getInteger("sasoriIronSandCD") : 0;
         this.sasoriIronSandWallCD = compound.hasKey("sasoriIronSandWallCD") ? compound.getInteger("sasoriIronSandWallCD") : 0;
         this.sasoriPoisonGasCD = compound.hasKey("sasoriPoisonGasCD") ? compound.getInteger("sasoriPoisonGasCD") : 0;
         this.sasoriStringCD = compound.hasKey("sasoriStringCD") ? compound.getInteger("sasoriStringCD") : 0;
         this.sasoriPuppetSpawnCD = compound.hasKey("sasoriPuppetSpawnCD") ? compound.getInteger("sasoriPuppetSpawnCD") : 0;
         this.sasoriMeleeCD = compound.hasKey("sasoriMeleeCD") ? compound.getInteger("sasoriMeleeCD") : 0;
         this.sasoriShieldCD = compound.hasKey("sasoriShieldCD") ? compound.getInteger("sasoriShieldCD") : 0;
         this.sasoriPuppetResummons = compound.hasKey("sasoriPuppetResummons") ? compound.getInteger("sasoriPuppetResummons") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote) {
            if (this.sasoriPhase == 1) {
               amount *= 0.6F;
            }

            if (this.sasoriPhase == 3) {
               amount *= 1.25F;
            }

            if (this.sasoriShieldCD <= 0 && trueSource instanceof EntityLivingBase && this.rand.nextFloat() < 0.15F) {
               this.sasoriShieldCD = this.cdMul(120);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, 0.8, (double)0.5F, 0.02, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.HOSTILE, 1.2F, 0.8F);

               try {
                  ItemPoisonSenbon.spawnArrow(this, new Vec3d(trueSource.posX, trueSource.posY + (double)trueSource.getEyeHeight(), trueSource.posZ));
               } catch (Exception e) {
                  System.err.println("[InfTsuk] SasoriShippuden failed to spawn counter senbon: " + e.getMessage());
               }

               return -1.0F;
            }
         }

         return amount;
      }

      protected void onCombatDeath() {
         this.sasoriKillPuppets();
      }

      private void sasoriMovement(EntityLivingBase target, double dist) {
         if (this.sasoriPhase <= 2) {
            if (dist > (double)18.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
            } else if (dist < (double)5.0F) {
               double dx = this.posX - target.posX;
               double dz = this.posZ - target.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  this.getNavigator().tryMoveToXYZ(this.posX + dx / d * (double)6.0F, this.posY, this.posZ + dz / d * (double)6.0F, (double)1.0F);
               }
            }
         } else if (dist > (double)6.0F) {
            this.getNavigator().tryMoveToEntityLiving(target, 1.3);
         } else if (dist > (double)3.0F) {
            this.getNavigator().tryMoveToEntityLiving(target, 1.1);
         }

         this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
      }

      private void sasoriPhase1Combat(EntityLivingBase target, double dist) {
         if (this.sasoriTailStabCD <= 0 && dist <= (double)4.0F) {
            this.sasoriTailStab(target);
         } else if (this.sasoriSenbonCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.sasoriSenbon(target);
         } else if (this.sasoriDashCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.sasoriPuppetStringDash(target);
         }
      }

      private void sasoriPhase2Combat(EntityLivingBase target, double dist) {
         if (this.sasoriIronSandCD <= 0 && dist >= (double)5.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.15F) {
            this.sasoriIronSand(target);
         } else if (this.sasoriIronSandWallCD <= 0 && dist <= (double)8.0F && this.rand.nextFloat() < 0.05F) {
            this.sasoriIronSandWall(target);
         } else if (this.sasoriDashCD <= 0 && dist >= (double)10.0F && dist <= (double)25.0F) {
            this.sasoriPuppetStringDash(target);
         } else if (this.sasoriSenbonCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.sasoriSenbon(target);
         } else if (this.sasoriStringCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.08F) {
            this.sasoriStringPull(target);
         }
      }

      private void sasoriPhase3Combat(EntityLivingBase target, double dist) {
         if (this.sasoriPoisonGasCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.08F) {
            this.sasoriPoisonGas(target);
         } else if (this.sasoriIronSandCD <= 0 && dist >= (double)5.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.2F) {
            this.sasoriIronSand(target);
         } else if (this.sasoriSenbonCD <= 0 && dist >= (double)3.0F && dist <= (double)18.0F) {
            this.sasoriSenbon(target);
         } else if (this.sasoriMeleeCD <= 0 && dist <= (double)3.5F) {
            this.sasoriRapidMelee(target);
         } else if (this.sasoriDashCD <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
            this.sasoriPuppetStringDash(target);
         }
      }

      private void sasoriTailStab(EntityLivingBase target) {
         this.sasoriTailStabCD = this.cdMul(40);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float tailTotal = baseDmg * 1.2F * (float)dmgMul;
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            float tailSplit = Math.min(1.0F, this.trueDamageSplit + 0.1F);
            normalDmg = tailTotal * (1.0F - tailSplit);
            trueDmg = tailTotal * tailSplit * this.trueDamageMultiplier;
         } else {
            normalDmg = tailTotal;
            trueDmg = 4.0F * this.trueDamageMultiplier;
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), trueDmg);
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.3, 0.4, 0.3, 0.02, new int[0]);
         }

      }

      private void sasoriSenbon(EntityLivingBase target) {
         this.sasoriSenbonCD = this.cdMul(60);
         int count = this.sasoriPhase == 3 ? 5 : (this.sasoriPhase == 2 ? 4 : 3);

         try {
            for(int i = 0; i < count; ++i) {
               double spread = ((double)i - (double)count / (double)2.0F) * 0.35;
               Vec3d targetVec = new Vec3d(target.posX + spread, target.posY + (double)target.getEyeHeight(), target.posZ + spread);
               ItemPoisonSenbon.spawnArrow(this, targetVec);
            }
         } catch (Exception e) {
            System.err.println("[InfTsuk] SasoriShippuden failed to spawn senbon barrage: " + e.getMessage());
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + 1.2, this.posZ, 12, 0.3, 0.3, 0.3, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 0.8F, 1.5F);
      }

      private void sasoriPuppetStringDash(EntityLivingBase target) {
         this.sasoriDashCD = this.cdMul(120);
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double dDist = Math.sqrt(dx * dx + dz * dz);
         if (!(dDist < 0.1)) {
            double speed = 1.6;
            this.isDashing = true;
            this.dashVelX = dx / dDist * speed;
            this.dashVelZ = dz / dDist * speed;
            this.dashTicksRemaining = Math.min(8, (int)(dDist / speed));
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
            }

         }
      }

      private void sasoriIronSand(EntityLivingBase target) {
         this.sasoriIronSandCD = this.cdMul(80);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float ironTotal = baseDmg * 1.8F * (float)dmgMul;
         int projCount = this.sasoriPhase >= 3 ? 4 : 3;

         for(int i = 0; i < projCount; ++i) {
            float normDmg;
            float trueDmg;
            if (this.trueDamageSplit > 0.0F) {
               normDmg = ironTotal * (1.0F - this.trueDamageSplit);
               trueDmg = ironTotal * this.trueDamageSplit * this.trueDamageMultiplier;
            } else {
               normDmg = ironTotal;
               trueDmg = 5.0F * this.trueDamageMultiplier;
            }

            EntityClayBomb.EntityCustom proj = new EntityClayBomb.EntityCustom(this.world, this, normDmg, trueDmg, 2.5F);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            double spreadAngle = ((double)i - (double)projCount / (double)2.0F) * 0.18;
            double sx = dx * Math.cos(spreadAngle) - dz * Math.sin(spreadAngle);
            double sz = dx * Math.sin(spreadAngle) + dz * Math.cos(spreadAngle);
            double hdist = Math.sqrt(sx * sx + sz * sz);
            proj.shoot(sx, dy + hdist * 0.04, sz, 1.8F, 0.6F);
            this.world.spawnEntity(proj);
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)1.5F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[]{Block.getStateId(Blocks.IRON_BLOCK.getDefaultState())});

            for(int i = 0; i < 20; ++i) {
               ws.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)1.0F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, 0, 0.01, 0.01, 0.01, (double)0.0F, new int[0]);
            }
         }

      }

      private void sasoriIronSandWall(EntityLivingBase target) {
         this.sasoriIronSandWallCD = this.cdMul(200);
         this.broadcastChat((double)30.0F, "§7[§cSasori§7] §8Iron Sand: World Order.");
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 40; ++i) {
               double angle = (double)i / (double)40.0F * Math.PI * (double)2.0F;
               double radius = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
               ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + Math.cos(angle) * radius, this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(angle) * radius, 2, 0.1, 0.1, 0.1, (double)0.0F, new int[]{Block.getStateId(Blocks.IRON_BLOCK.getDefaultState())});
            }
         }

         List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)5.0F), (px) -> px != null && px.isEntityAlive() && !px.isSpectator() && !px.isCreative());
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float wallTotal = baseDmg * 1.0F * (float)dmgMul;

         for(EntityPlayer p : nearby) {
            if (this.trueDamageSplit > 0.0F) {
               float wallNorm = wallTotal * (1.0F - this.trueDamageSplit);
               float wallTrue = wallTotal * this.trueDamageSplit * this.trueDamageMultiplier;
               p.attackEntityFrom(DamageSource.causeMobDamage(this), wallNorm);
               p.hurtResistantTime = 0;
               p.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), wallTrue);
            } else {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), wallTotal);
            }

            p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
         }

      }

      private void sasoriStringPull(EntityLivingBase target) {
         this.sasoriStringCD = this.cdMul(100);
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > (double)0.0F && target instanceof EntityPlayer) {
            target.motionX += dx / d * 0.8;
            target.motionZ += dz / d * 0.8;
            target.velocityChanged = true;
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, (this.posX + target.posX) / (double)2.0F, (this.posY + target.posY) / (double)2.0F + (double)1.0F, (this.posZ + target.posZ) / (double)2.0F, 15, (double)1.0F, (double)0.5F, (double)1.0F, 0.01, new int[0]);
         }

      }

      private void sasoriPoisonGas(EntityLivingBase target) {
         this.sasoriPoisonGasCD = this.cdMul(160);

         try {
            EntityPoisonMist.EC mist = new EntityPoisonMist.EC(this, 20.0F);
            mist.setPosition(target.posX, target.posY, target.posZ);
            this.world.spawnEntity(mist);
         } catch (Exception e) {
            System.err.println("[InfTsuk] SasoriShippuden failed to spawn poison mist: " + e.getMessage());
         }

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, target.getEntityBoundingBox().grow((double)5.0F), (px) -> px != null && px.isEntityAlive() && !px.isSpectator())) {
            p.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
            p.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 30; ++i) {
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, target.posY + this.rand.nextDouble() * (double)2.0F, target.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, 2, 0.1, 0.1, 0.1, 0.02, new int[0]);
            }
         }

         this.broadcastChat((double)30.0F, "§7[§cSasori§7] §aPoison cloud...");
      }

      private void sasoriRapidMelee(EntityLivingBase target) {
         this.sasoriMeleeCD = 10 + this.rand.nextInt(8);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float rapidTotal = baseDmg * (float)dmgMul;
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = rapidTotal * (1.0F - this.trueDamageSplit);
            trueDmg = rapidTotal * this.trueDamageSplit * this.trueDamageMultiplier;
         } else {
            normalDmg = rapidTotal;
            trueDmg = 2.0F * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         this.meleeCooldown = this.sasoriMeleeCD;
      }

      private void sasoriSpawnPuppets(int count) {
         if (!this.world.isRemote) {
            EntityLivingBase ct = this.getAttackTarget();
            boolean spawnKazekage = this.sasoriPhase >= 2;
            int karasuCount = spawnKazekage ? Math.max(1, count - 1) : count;
            if (spawnKazekage) {
               double sx = this.posX + (double)4.0F;
               double sz = this.posZ;
               double sy = this.sasoriGroundY(sx, this.posY, sz);
               Entity puppet = this.spawnSasoriPuppet("sasori_kazekage", sx, sy, sz, ct);
               if (puppet != null) {
                  this.sasoriSpawnedPuppets.add(puppet);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, sy + (double)1.0F, sz, 15, (double)0.75F, (double)1.0F, (double)0.75F, 0.02, new int[0]);
               }
            }

            for(int i = 0; i < karasuCount; ++i) {
               double a = (Math.PI * 2D) / (double)count * (double)(spawnKazekage ? i + 1 : i);
               double sx = this.posX + Math.cos(a) * (double)4.0F;
               double sz = this.posZ + Math.sin(a) * (double)4.0F;
               double sy = this.sasoriGroundY(sx, this.posY, sz);
               Entity puppet = this.spawnSasoriPuppet("sasori_karasu", sx, sy, sz, ct);
               if (puppet != null) {
                  this.sasoriSpawnedPuppets.add(puppet);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, sy + (double)1.0F, sz, 15, (double)0.75F, (double)1.0F, (double)0.75F, 0.02, new int[0]);
               }
            }

            this.sasoriPuppetSpawnCD = this.cdMul(140);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.2F, 0.6F);
         }
      }

      private Entity spawnSasoriPuppet(String configId, double sx, double sy, double sz, EntityLivingBase target) {
         EntityCreature puppet;
         if ("sasori_kazekage".equals(configId)) {
            Entity raw = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "puppetkaze"), this.world);
            puppet = raw instanceof EntityCreature ? (EntityCreature)raw : null;
         } else if ("sasori_hundred".equals(configId)) {
            Entity raw = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "puppethundred"), this.world);
            puppet = raw instanceof EntityCreature ? (EntityCreature)raw : null;
         } else {
            Entity raw = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "questpuppet"), this.world);
            puppet = raw instanceof EntityCreature ? (EntityCreature)raw : null;
         }

         if (puppet == null) {
            return null;
         } else {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null && puppet instanceof INpcConfigurable) {
               ((INpcConfigurable)puppet).applyNpcConfig(config);
            }

            puppet.setPosition(sx, sy, sz);
            if (target != null) {
               puppet.setAttackTarget(target);
            }

            puppet.enablePersistence();
            NBTTagCompound nbt = puppet.getEntityData();
            nbt.setBoolean("sasoriPuppet", true);
            NBTTagCompound parentNbt = this.getEntityData();
            if (parentNbt.getBoolean("questEntity")) {
               nbt.setBoolean("questEntity", true);
               String owner = parentNbt.getString("ownerUUID");
               if (owner != null && !owner.isEmpty()) {
                  nbt.setString("ownerUUID", owner);
               }

               String sqid = parentNbt.getString("sharedQuestId");
               if (sqid != null && !sqid.isEmpty()) {
                  nbt.setString("sharedQuestId", sqid);
               }
            }

            this.world.spawnEntity(puppet);
            return puppet;
         }
      }

      private void sasoriSpawnHundredPuppets(EntityLivingBase target) {
         if (!this.world.isRemote) {
            int puppetCount = 10;

            for(int i = 0; i < puppetCount; ++i) {
               double angle = (Math.PI * 2D) / (double)puppetCount * (double)i;
               double radius = (double)5.0F + this.rand.nextDouble() * (double)3.0F;
               double sx = this.posX + Math.cos(angle) * radius;
               double sz = this.posZ + Math.sin(angle) * radius;
               double sy = this.sasoriGroundY(sx, this.posY, sz);
               Entity puppet = this.spawnSasoriPuppet("sasori_hundred", sx, sy, sz, target);
               if (puppet != null) {
                  this.sasoriSpawnedPuppets.add(puppet);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, sx, sy + (double)1.0F, sz, 10, (double)0.5F, 0.8, (double)0.5F, 0.03, new int[0]);
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 60; ++i) {
                  double angle = (double)i / (double)60.0F * Math.PI * (double)2.0F;
                  double radius = (double)3.0F + this.rand.nextDouble() * (double)5.0F;
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + Math.cos(angle) * radius, this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(angle) * radius, 2, 0.1, 0.2, 0.1, 0.02, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.8F);
            this.sasoriPuppetSpawnCD = this.cdMul(140);
         }
      }

      private void sasoriKillPuppets() {
         for(Entity p : this.sasoriSpawnedPuppets) {
            if (p != null && p.isEntityAlive()) {
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, p.posX, p.posY + (double)1.0F, p.posZ, 10, (double)0.5F, (double)1.0F, (double)0.5F, 0.02, new int[0]);
               }

               p.setDead();
            }
         }

         this.sasoriSpawnedPuppets.clear();
      }

      private void sasoriCheckPuppets() {
         Iterator<Entity> it = this.sasoriSpawnedPuppets.iterator();

         while(it.hasNext()) {
            Entity p = (Entity)it.next();
            if (p == null || p.isDead || !p.isEntityAlive()) {
               it.remove();
            }
         }

         if (this.sasoriSpawnedPuppets.isEmpty() && this.sasoriPuppetResummons < 2 && this.sasoriPuppetSpawnCD <= 0 && this.sasoriPhase >= 2) {
            ++this.sasoriPuppetResummons;
            this.sasoriSpawnPuppets(this.sasoriPhase == 2 ? 2 + this.rand.nextInt(2) : 4);
         }

      }

      private double sasoriGroundY(double x, double baseY, double z) {
         BlockPos pos = new BlockPos(x, baseY, z);

         for(int i = 0; i < 10; ++i) {
            if (this.world.getBlockState(pos.down()).getMaterial().isSolid()) {
               return (double)pos.getY();
            }

            pos = pos.down();
         }

         return baseY;
      }
   }
}
