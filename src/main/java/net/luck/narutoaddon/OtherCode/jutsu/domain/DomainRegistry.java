
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.*;

public class DomainRegistry {
   private static final Map<Integer, DomainRegistry> PER_DIMENSION = new HashMap();
   private final Map<UUID, DomainInstance> active = new HashMap();

   public static DomainRegistry forWorld(WorldServer world) {
      return (DomainRegistry)PER_DIMENSION.computeIfAbsent(world.provider.getDimension(), (k) -> new DomainRegistry());
   }

   public synchronized void add(DomainInstance domain) {
      this.active.put(domain.getId(), domain);
   }

   public synchronized void remove(UUID id) {
      this.active.remove(id);
   }

   public synchronized DomainInstance get(UUID id) {
      return (DomainInstance)this.active.get(id);
   }

   public synchronized Collection<DomainInstance> all() {
      return new ArrayList(this.active.values());
   }

   public synchronized DomainInstance findAt(Vec3d pos) {
      for(DomainInstance d : this.active.values()) {
         if (d.isInsideDomain(pos)) {
            return d;
         }
      }

      return null;
   }
}
