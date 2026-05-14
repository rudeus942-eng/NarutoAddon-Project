
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
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
public class EntityGurenCrystalNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 314;
   public static final int ENTITYID_SPIKE = 315;

   public EntityGurenCrystalNPC(ElementsInfTsukAddon instance) {
      super(instance, 314);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "gurencrystal"), 314).name("gurencrystal").tracker(64, 3, true).egg(-1, -1).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCrystalSpikeProjectile.class).id(new ResourceLocation("inftsukaddon", "crystal_spike_proj"), 315).name("crystal_spike_proj").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, GurenNpcRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntityCrystalSpikeProjectile.class, CrystalSpikeRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class GurenNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public GurenNpcRenderer(RenderManager renderManager) {
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
         float alpha = 1.0F;
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               alpha = config.getGhostAlpha();
            }
         }

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
   }

   @SideOnly(Side.CLIENT)
   public static class CrystalSpikeRenderer extends Render<EntityCrystalSpikeProjectile> {
      private static final ResourceLocation CRYSTAL_TEXTURE = new ResourceLocation("narutomod:textures/crystal_pink.png");
      private final ModelPrism model = new ModelPrism();

      public CrystalSpikeRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCrystalSpikeProjectile entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y + (double)0.25F, z);
         float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(0.35F, 0.35F, 0.35F);
         GlStateManager.enableBlend();
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(1.0F, 0.6F, 0.8F, 0.85F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCrystalSpikeProjectile entity) {
         return CRYSTAL_TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelPrism extends ModelBase {
      private final ModelRenderer prism;
      private final ModelRenderer bone5;
      private final ModelRenderer bone;
      private final ModelRenderer bone4;
      private final ModelRenderer bone3;
      private final ModelRenderer bone2;
      private final ModelRenderer bone6;
      private final ModelRenderer bone7;
      private final ModelRenderer bone8;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;

      public ModelPrism() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         this.prism = new ModelRenderer(this);
         this.prism.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone5 = new ModelRenderer(this);
         this.bone5.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.prism.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.0F, -0.7854F, 0.0F);
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone5.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, 0.0F, -0.5847F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, false));
         this.bone4 = new ModelRenderer(this);
         this.bone4.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone5.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 1.5708F, -0.9861F, -1.5708F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, false));
         this.bone3 = new ModelRenderer(this);
         this.bone3.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone5.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -1.5708F, 0.9861F, -1.5708F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, false));
         this.bone2 = new ModelRenderer(this);
         this.bone2.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone5.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.0F, 0.5847F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, true));
         this.bone6 = new ModelRenderer(this);
         this.bone6.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.prism.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.0F, -0.7854F, -3.1416F);
         this.bone7 = new ModelRenderer(this);
         this.bone7.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone6.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.0F, 0.0F, -0.5847F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, false));
         this.bone8 = new ModelRenderer(this);
         this.bone8.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone6.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 1.5708F, -0.9861F, -1.5708F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, false));
         this.bone9 = new ModelRenderer(this);
         this.bone9.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone6.addChild(this.bone9);
         this.setRotationAngle(this.bone9, -1.5708F, 0.9861F, -1.5708F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, false));
         this.bone10 = new ModelRenderer(this);
         this.bone10.setRotationPoint(0.0F, -15.0F, 0.0F);
         this.bone6.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.0F, 0.0F, 0.5847F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 0, -20, 0.0F, 0.0F, -10.0F, 0, 18, 20, 0.0F, true));
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.prism.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   public static class EntityCrystalSpikeProjectile extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 8.0F;
      private float trueDamage = 2.0F;

      public EntityCrystalSpikeProjectile(World world) {
         super(world);
         this.setSize(0.4F, 0.4F);
      }

      public EntityCrystalSpikeProjectile(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.4F, 0.4F);
      }

      public EntityCrystalSpikeProjectile(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.4F, 0.4F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.thrower), this.damage);
               if (this.trueDamage > 0.0F && result.entityHit instanceof EntityLivingBase) {
                  result.entityHit.hurtResistantTime = 0;
                  result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, result.entityHit.posX, result.entityHit.posY + (double)1.0F, result.entityHit.posZ, 12, 0.3, (double)0.5F, 0.3, 0.05, new int[]{Block.getStateId(Blocks.PACKED_ICE.getDefaultState())});
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY, this.posZ, 8, 0.2, 0.2, 0.2, 0.03, new int[]{Block.getStateId(Blocks.PACKED_ICE.getDefaultState())});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 0.8F, 1.2F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 80) {
            this.setDead();
         }

         if (this.world instanceof WorldServer && this.lifetime % 2 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY, this.posZ, 2, 0.05, 0.05, 0.05, 0.01, new int[0]);
         }

      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("crystalDmg", this.damage);
         compound.setFloat("crystalTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("crystalDmg")) {
            this.damage = compound.getFloat("crystalDmg");
         }

         if (compound.hasKey("crystalTrue")) {
            this.trueDamage = compound.getFloat("crystalTrue");
         }

      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private boolean crystalArmorActive = false;
      private boolean crystalArmorTriggered = false;
      private int crystalPrisonCooldown = 0;
      private int crystalSpikeCooldown = 0;
      private int crystalBladeCooldown = 0;
      private boolean crystalIntroPlayed = false;
      private static final float CRYSTAL_ARMOR_HP_THRESHOLD = 0.5F;
      private static final float CRYSTAL_ARMOR_REDUCTION = 0.3F;
      private static final int CRYSTAL_PRISON_BASE_CD = 180;
      private static final int CRYSTAL_PRISON_SLOWNESS_DURATION = 60;
      private static final int CRYSTAL_PRISON_SLOWNESS_AMP = 3;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processCrystalCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.crystalPrisonCooldown > 0) {
            --this.crystalPrisonCooldown;
         }

         if (this.crystalSpikeCooldown > 0) {
            --this.crystalSpikeCooldown;
         }

         if (this.crystalBladeCooldown > 0) {
            --this.crystalBladeCooldown;
         }

      }

      protected void resetCombatState() {
         this.crystalArmorActive = false;
         this.crystalArmorTriggered = false;
         this.crystalIntroPlayed = false;
         this.crystalPrisonCooldown = 0;
         this.crystalSpikeCooldown = 0;
         this.crystalBladeCooldown = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("crystalArmorActive", this.crystalArmorActive);
         compound.setBoolean("crystalArmorTriggered", this.crystalArmorTriggered);
         compound.setBoolean("crystalIntroPlayed", this.crystalIntroPlayed);
         compound.setInteger("crystalPrisonCooldown", this.crystalPrisonCooldown);
         compound.setInteger("crystalSpikeCooldown", this.crystalSpikeCooldown);
         compound.setInteger("crystalBladeCooldown", this.crystalBladeCooldown);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.crystalArmorActive = compound.getBoolean("crystalArmorActive");
         this.crystalArmorTriggered = compound.getBoolean("crystalArmorTriggered");
         this.crystalIntroPlayed = compound.getBoolean("crystalIntroPlayed");
         this.crystalPrisonCooldown = compound.hasKey("crystalPrisonCooldown") ? compound.getInteger("crystalPrisonCooldown") : 0;
         this.crystalSpikeCooldown = compound.hasKey("crystalSpikeCooldown") ? compound.getInteger("crystalSpikeCooldown") : 0;
         this.crystalBladeCooldown = compound.hasKey("crystalBladeCooldown") ? compound.getInteger("crystalBladeCooldown") : 0;
      }

      protected void onCombatDeath() {
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.6, (double)1.0F, 0.6, 0.1, new int[]{Block.getStateId(Blocks.PACKED_ICE.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.5F, 0.6F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         return this.crystalArmorActive ? amount * 0.7F : amount;
      }

      private void processCrystalCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (!this.crystalIntroPlayed) {
               this.crystalIntroPlayed = true;
               this.crystalBroadcast("§5Guren: §dMy Crystal Style will be your tomb!");
            }

            if (!this.crystalArmorTriggered && hpPercent <= 0.5F) {
               this.crystalArmorTriggered = true;
               this.crystalArmorActive = true;
               this.crystalBroadcast("§5Guren: §dCrystal Armor!");
               if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
                  double curArmor = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
                  this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(curArmor + (double)8.0F);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.08, new int[]{Block.getStateId(Blocks.PACKED_ICE.getDefaultState())});
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.HOSTILE, 1.2F, 0.5F);
            }

            if (this.crystalPrisonCooldown <= 0 && dist <= (double)8.0F && dist >= (double)2.0F && this.rand.nextFloat() < 0.3F) {
               this.crystalPrisonAttack(target);
               this.crystalPrisonCooldown = this.cdMul(180);
            } else if (this.crystalSpikeCooldown <= 0 && dist >= (double)12.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.35F) {
               this.fireCrystalSpike(target);
               this.crystalSpikeCooldown = this.cdMul(this.crystalArmorActive ? 40 : 60);
            } else if (this.crystalSpikeCooldown <= 0 && dist >= (double)6.0F && dist < (double)12.0F && this.rand.nextFloat() < 0.2F) {
               this.fireCrystalSpike(target);
               this.crystalSpikeCooldown = this.cdMul(this.crystalArmorActive ? 50 : 70);
            } else if (!this.isDashing && this.dashCooldown <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.25F) {
               this.crystalLunge(target, dist);
               this.dashCooldown = this.cdMul(this.crystalArmorActive ? 60 : 80);
            } else {
               if (dist > (double)6.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.2);
               } else if (dist > (double)2.5F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
               }

               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                  this.performMeleeSwing(target);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 6, 0.2, 0.3, 0.2, 0.03, new int[0]);
                  }
               }

               if (this.natureType != 0 && this.jutsuCooldown <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.15F) {
                  this.startNatureJutsu(target);
                  this.jutsuCooldown = this.cdMul(this.crystalArmorActive ? 60 : 100);
               }

            }
         }
      }

      private void fireCrystalSpike(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 8.0F * (float)dmgMult;
         float trueDmg = (this.combatTier >= 4 ? 4.0F : (this.combatTier >= 3 ? 3.0F : 2.0F)) * this.trueDamageMultiplier;
         EntityCrystalSpikeProjectile spike = new EntityCrystalSpikeProjectile(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - spike.posY;
         double dz = target.posZ - this.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         spike.shoot(dx, dy + dist * 0.08, dz, 1.8F, 1.5F);
         this.world.spawnEntity(spike);
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 0.7F, 1.5F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.5F, this.posZ, 8, 0.3, 0.3, 0.3, 0.05, new int[0]);
         }

      }

      private void crystalPrisonAttack(EntityLivingBase target) {
         float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float burstDmg = baseDamage * 1.5F * (float)this.getDamageMultiplier();
         float trueDmg = (this.combatTier >= 4 ? 6.0F : (this.combatTier >= 3 ? 4.0F : 3.0F)) * this.trueDamageMultiplier;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), burstDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 3));
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 12; ++i) {
               double angle = (double)i / (double)12.0F * Math.PI * (double)2.0F;
               double px = target.posX + Math.cos(angle) * (double)1.5F;
               double pz = target.posZ + Math.sin(angle) * (double)1.5F;
               ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, px, target.posY + (double)0.5F, pz, 3, 0.1, 0.3, 0.1, 0.02, new int[]{Block.getStateId(Blocks.PACKED_ICE.getDefaultState())});
            }

            for(int j = 0; j < 4; ++j) {
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)j * (double)0.5F, target.posZ, 5, 0.4, 0.1, 0.4, 0.02, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.HOSTILE, 1.5F, 0.4F);
         this.crystalBroadcast("§5Guren: §dCrystal Prison!");
         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§5§o* Crystals encase your body, slowing your movement! *"));
         }

      }

      private void crystalLunge(EntityLivingBase target, double dist) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 8;
            double dashDist = Math.max(horizDist - (double)2.0F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.3;
            this.dashVelZ = nz * velocityPerTick * 1.3;
            this.dashArcSustain = 0.05F;
            this.dashFallAccel = 0.06F;
            this.dashMaxFall = -0.35F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.28;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.3, 0.3, 0.3, 0.08, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.3F);
         }
      }

      private void crystalBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
