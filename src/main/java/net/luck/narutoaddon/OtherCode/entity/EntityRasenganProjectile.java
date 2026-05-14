
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityRasenganProjectile extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 228;

   public EntityRasenganProjectile(ElementsInfTsukAddon instance) {
      super(instance, 228);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "rasengan_projectile"), 228).name("rasengan_projectile").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderRasengan::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float normalDmg = 10.0F;
      private float trueDmg = 5.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.5F, 0.5F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.5F, 0.5F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.5F, 0.5F);
         this.normalDmg = normalDmg;
         this.trueDmg = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower && result.entityHit instanceof EntityLivingBase) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.normalDmg);
               if (this.trueDmg > 0.0F) {
                  result.entityHit.hurtResistantTime = 0;
                  result.entityHit.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), this.trueDmg);
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + 0.3, this.posZ, 25, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + 0.3, this.posZ, 15, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.8F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 100) {
            this.setDead();
         } else {
            if (this.world.isRemote) {
               for(int i = 0; i < 3; ++i) {
                  double angle = ((double)this.lifetime + (double)i * 0.33) * 0.8;
                  double px = this.posX + Math.cos(angle) * (double)0.25F;
                  double pz = this.posZ + Math.sin(angle) * (double)0.25F;
                  this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, px, this.posY + (double)0.25F, pz, 0.16, 0.82, (double)1.0F, new int[0]);
               }
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("rasenNormDmg", this.normalDmg);
         compound.setFloat("rasenTrueDmg", this.trueDmg);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("rasenNormDmg")) {
            this.normalDmg = compound.getFloat("rasenNormDmg");
         }

         if (compound.hasKey("rasenTrueDmg")) {
            this.trueDmg = compound.getFloat("rasenTrueDmg");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class RenderRasengan extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("inftsukaddon:textures/rasengan_inner.png");
      private final ModelRasenganInnerBall model = new ModelRasenganInnerBall();

      public RenderRasengan(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float age = (float)entity.ticksExisted + partialTicks;
         float pulse = 0.5F + 0.02F * (float)Math.sin((double)age * (double)0.5F);
         GlStateManager.translate(x, y + (double)0.25F, z);
         GlStateManager.scale(pulse, pulse, pulse);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.disableLighting();
         GlStateManager.depthMask(false);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(0.2F, 0.6F, 1.0F, 0.9F);
         this.model.ball.rotateAngleY = -age * 5.0F * 0.0174533F;
         this.model.ball.rotateAngleX = age * 0.6F * 0.0174533F;
         this.model.flaps.rotateAngleY = age * 0.8F * 0.0174533F;
         GlStateManager.disableTexture2D();
         this.model.render(entity, 0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F);
         GlStateManager.scale(1.15F, 1.15F, 1.15F);
         GlStateManager.color(0.4F, 0.8F, 1.0F, 0.4F);
         this.model.render(entity, 0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F);
         GlStateManager.enableTexture2D();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.depthMask(true);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelRasenganInnerBall extends ModelBase {
      public final ModelRenderer flaps;
      public final ModelRenderer ball;

      public ModelRasenganInnerBall() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         this.flaps = new ModelRenderer(this);
         this.flaps.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.addFlapPanel(this.flaps, 0.0F, 0.0F, 0.0F);
         this.addFlapPanel(this.flaps, 0.0F, 1.5708F, 0.0F);
         this.addFlapPanel(this.flaps, 0.0F, 0.7854F, 0.0F);
         this.addFlapPanel(this.flaps, 0.0F, -0.7854F, 0.0F);
         this.addFlapPanel(this.flaps, 1.5708F, 0.0F, 0.0F);
         this.addFlapPanel(this.flaps, 1.5708F, 0.0F, 1.5708F);
         this.addFlapPanel(this.flaps, 0.7854F, 0.0F, 0.0F);
         this.addFlapPanel(this.flaps, -0.7854F, 0.0F, 0.0F);
         this.addFlapPanel(this.flaps, 0.0F, 0.0F, 1.5708F);
         this.addFlapPanel(this.flaps, 0.0F, 0.0F, 0.7854F);
         this.addFlapPanel(this.flaps, 0.0F, 0.0F, -0.7854F);
         this.addFlapPanel(this.flaps, 0.7854F, 0.0F, 0.7854F);
         this.ball = new ModelRenderer(this);
         this.ball.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.addHexRing(this.ball, 0.0F, 0.0F, 0.0F);
         this.addHexRing(this.ball, 0.0F, 0.3927F, 0.0F);
         this.addHexRing(this.ball, 0.0F, 0.7854F, 0.0F);
         this.addHexRing(this.ball, 0.0F, 1.1781F, 0.0F);
         this.addHexRing(this.ball, 1.5708F, 0.0F, 0.0F);
         this.addHexRing(this.ball, 1.5708F, 0.0F, 0.3927F);
         this.addHexRing(this.ball, 1.5708F, 0.0F, 0.7854F);
         this.addHexRing(this.ball, 1.5708F, 0.0F, 1.1781F);
      }

      private void addFlapPanel(ModelRenderer parent, float rx, float ry, float rz) {
         ModelRenderer panel = new ModelRenderer(this);
         panel.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.setRotationAngle(panel, rx, ry, rz);
         panel.cubeList.add(new ModelBox(panel, 0, 0, 0.0F, -8.0F, -5.0F, 0, 16, 10, 0.0F, false));
         parent.addChild(panel);
      }

      private void addHexRing(ModelRenderer parent, float rx, float ry, float rz) {
         ModelRenderer ring = new ModelRenderer(this);
         ring.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.setRotationAngle(ring, rx, ry, rz);
         float radius = 4.0F;

         for(int i = 0; i < 6; ++i) {
            float angle = (float)i * 0.5236F;
            ModelRenderer seg = new ModelRenderer(this);
            seg.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.setRotationAngle(seg, 0.0F, 0.0F, angle);
            seg.cubeList.add(new ModelBox(seg, 0, 16, -1.0F, -radius, -1.0F, 2, 2, 2, 0.0F, false));
            seg.cubeList.add(new ModelBox(seg, 0, 16, -1.0F, radius - 2.0F, -1.0F, 2, 2, 2, 0.0F, false));
            ring.addChild(seg);
         }

         parent.addChild(ring);
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.flaps.render(f5);
         this.ball.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }
}
