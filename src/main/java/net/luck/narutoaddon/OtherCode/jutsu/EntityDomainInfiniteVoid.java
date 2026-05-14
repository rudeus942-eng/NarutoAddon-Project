
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.jutsu.domain.DomainInstance;
import net.luck.narutoaddon.OtherCode.jutsu.domain.DomainRegistry;
import net.luck.narutoaddon.OtherCode.jutsu.domain.DomainType;
import net.luck.narutoaddon.OtherCode.jutsu.domain.InfiniteVoidEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDomainInfiniteVoid extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 331;
   public static final int DOMAIN_RADIUS = 20;

   public EntityDomainInfiniteVoid(ElementsInfTsukAddon instance) {
      super(instance, 953);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "domain_infinite_void"), 331).name("inftsuk_domain_infinite_void").tracker(128, 20, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private DomainInstance domain;
      private UUID shooterUUID;
      private int totalLifetime;
      private boolean domainBuilt;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, EntityLivingBase caster) {
         this(world);
         this.shooterUUID = caster.getUniqueID();
         this.setPosition(caster.posX, caster.posY, caster.posZ);
      }

      public Type getJutsuType() {
         return Type.SHAKUTON;
      }

      protected void entityInit() {
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)64.0F);
      }

      public boolean isInvisible() {
         return true;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.totalLifetime;
         if (!this.world.isRemote) {
            if (this.world instanceof WorldServer) {
               WorldServer server = (WorldServer)this.world;
               if (!this.domainBuilt) {
                  this.domainBuilt = true;
                  BlockPos center = new BlockPos(Math.floor(this.posX), Math.floor(this.posY), Math.floor(this.posZ));
                  this.domain = new DomainInstance(this.shooterUUID != null ? this.shooterUUID : this.getUniqueID(), DomainType.INFINITE_VOID, center, 20, server.getTotalWorldTime(), 1200);
                  this.domain.activate(server);
                  DomainRegistry.forWorld(server).add(this.domain);
                  this.domain.setPhase(DomainInstance.DomainPhase.ACTIVE);
               } else if (this.domain == null) {
                  this.setDead();
               } else {
                  long now = server.getTotalWorldTime();
                  InfiniteVoidEffects.tickEffects(server, this.domain, now);
                  if (this.domain.isExpired(now) || this.totalLifetime > 1220) {
                     this.domain.setPhase(DomainInstance.DomainPhase.ENDED);
                     this.domain.cleanup(server);
                     DomainRegistry.forWorld(server).remove(this.domain.getId());
                     this.setDead();
                  }

               }
            }
         }
      }

      public void setDead() {
         if (!this.isDead && !this.world.isRemote && this.world instanceof WorldServer && this.domain != null && this.domain.getPhase() != DomainInstance.DomainPhase.ENDED) {
            try {
               WorldServer server = (WorldServer)this.world;
               this.domain.cleanup(server);
               DomainRegistry.forWorld(server).remove(this.domain.getId());
            } catch (Throwable t) {
               t.printStackTrace();
            }
         }

         super.setDead();
      }

      protected void readEntityFromNBT(NBTTagCompound c) {
         this.totalLifetime = c.getInteger("dvLifetime");
         this.domainBuilt = c.getBoolean("dvBuilt");
         if (c.hasKey("dvShooter")) {
            try {
               this.shooterUUID = UUID.fromString(c.getString("dvShooter"));
            } catch (Exception var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound c) {
         c.setInteger("dvLifetime", this.totalLifetime);
         c.setBoolean("dvBuilt", this.domainBuilt);
         if (this.shooterUUID != null) {
            c.setString("dvShooter", this.shooterUUID.toString());
         }

      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 1200;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.5F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 1200L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               EntityCustom dom = new EntityCustom(entity.world, entity);
               dom.setPosition(entity.posX, entity.posY, entity.posZ);
               entity.world.spawnEntity(dom);
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 60.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }
   }
}
