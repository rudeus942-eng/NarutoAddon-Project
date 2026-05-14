
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class DomainSavedData extends WorldSavedData {
   private static final String DATA_NAME = "inftsukaddon_domains";
   private final List<NBTTagCompound> orphanedDomainData = new ArrayList();
   private final Map<UUID, DomainInstance> trackedDomains = new HashMap();

   public DomainSavedData() {
      super("inftsukaddon_domains");
   }

   public DomainSavedData(String name) {
      super(name);
   }

   public static DomainSavedData get(WorldServer world) {
      MapStorage storage = world.getPerWorldStorage();
      DomainSavedData data = (DomainSavedData)storage.getOrLoadData(DomainSavedData.class, "inftsukaddon_domains");
      if (data == null) {
         data = new DomainSavedData();
         storage.setData("inftsukaddon_domains", data);
      }

      return data;
   }

   public void addDomain(DomainInstance domain) {
      this.trackedDomains.put(domain.getId(), domain);
      this.markDirty();
   }

   public void removeDomain(UUID domainId) {
      this.trackedDomains.remove(domainId);
      this.markDirty();
   }

   public Map<UUID, DomainInstance> getTrackedDomains() {
      return this.trackedDomains;
   }

   public List<NBTTagCompound> takeOrphanedData() {
      List<NBTTagCompound> copy = new ArrayList(this.orphanedDomainData);
      this.orphanedDomainData.clear();
      return copy;
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagList list = new NBTTagList();

      for(DomainInstance d : this.trackedDomains.values()) {
         list.appendTag(d.serializeNBT());
      }

      nbt.setTag("domains", list);
      nbt.setLong("saveTime", System.currentTimeMillis());
      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.orphanedDomainData.clear();
      if (nbt.hasKey("domains")) {
         NBTTagList list = nbt.getTagList("domains", 10);

         for(int i = 0; i < list.tagCount(); ++i) {
            this.orphanedDomainData.add(list.getCompoundTagAt(i));
         }

         System.out.println("[InfTsukAddon/Domain] Loaded " + this.orphanedDomainData.size() + " orphaned domain(s) from WorldSavedData (pending cleanup check)");
      }

   }
}
