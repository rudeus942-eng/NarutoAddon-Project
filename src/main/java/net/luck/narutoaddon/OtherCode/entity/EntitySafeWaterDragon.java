
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.narutomod.entity.EntityWaterDragon;
import net.narutomod.item.ItemJutsu;
import net.narutomod.procedure.ProcedureAoeCommand;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySafeWaterDragon extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 213;

   public EntitySafeWaterDragon(ElementsInfTsukAddon instance) {
      super(instance, 41);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "safe_water_dragon"), 213).name("inftsuk_safe_water_dragon").tracker(64, 3, true).build());
   }

   public static class EntityCustom extends EntityWaterDragon.EC {
      public EntityCustom(World world) {
         super(world);
      }

      public EntityCustom(EntityLivingBase shooter, float power) {
         super(shooter, power);
      }

      public EntityCustom(EntityLivingBase shooter, double x, double y, double z, float power) {
         super(shooter, x, y, z, power);
      }

      protected void onImpact(RayTraceResult result) {
         if (result.entityHit == null || !result.entityHit.equals(this.shootingEntity)) {
            if (!this.world.isRemote) {
               float size = this.getEntityScale();
               ProcedureAoeCommand.set(this, (double)0.0F, (double)3.0F).exclude(this.shootingEntity).damageEntities(ItemJutsu.causeJutsuDamage(this, this.shootingEntity), 20.0F * size);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ, 100, (double)2.0F, (double)1.0F, (double)2.0F, (double)0.0F, new int[0]);
               }

               this.setDead();
            }

         }
      }
   }
}
