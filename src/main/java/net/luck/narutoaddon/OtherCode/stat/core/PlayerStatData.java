
package net.luck.narutoaddon.OtherCode.stat.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.EnumMap;
import java.util.Map;

public class PlayerStatData {
   private final EnumMap<StatCategory, EnumMap<StatElement, Integer>> allocations = new EnumMap(StatCategory.class);
   private int spEarned;
   private int spSpent;
   private int respecTokens;
   private long lastRespecTime;
   private int chakraEnhancementCharges = 0;
   private int ironBodyCharges = 0;

   public PlayerStatData() {
      for(StatCategory cat : StatCategory.values()) {
         this.allocations.put(cat, new EnumMap(StatElement.class));
      }

      this.spEarned = 0;
      this.spSpent = 0;
      this.respecTokens = 0;
      this.lastRespecTime = 0L;
   }

   public int getSpEarned() {
      return this.spEarned;
   }

   public int getSpSpent() {
      return this.spSpent;
   }

   public int getAvailableSP() {
      return this.spEarned - this.spSpent;
   }

   public int getRespecTokens() {
      return this.respecTokens;
   }

   public long getLastRespecTime() {
      return this.lastRespecTime;
   }

   public int getChakraEnhancementCharges() {
      return this.chakraEnhancementCharges;
   }

   public int getIronBodyCharges() {
      return this.ironBodyCharges;
   }

   public boolean addChakraEnhancementCharge(int maxCap) {
      if (this.chakraEnhancementCharges >= maxCap) {
         return false;
      } else {
         ++this.chakraEnhancementCharges;
         return true;
      }
   }

   public double getChakraEnhancementBonus() {
      return (double)this.chakraEnhancementCharges * 0.05;
   }

   public void clearIronBodyCharges() {
      this.ironBodyCharges = 0;
   }

   public int getLevel(StatCategory category, StatElement element) {
      EnumMap<StatElement, Integer> map = (EnumMap)this.allocations.get(category);
      if (map == null) {
         return 0;
      } else {
         Integer lvl = (Integer)map.get(element);
         return lvl != null ? lvl : 0;
      }
   }

   public EnumMap<StatElement, Integer> getAllocationsForCategory(StatCategory category) {
      return (EnumMap)this.allocations.get(category);
   }

   public boolean allocate(StatCategory category, StatElement element) {
      int currentLevel = this.getLevel(category, element);
      int cost = StatConstants.getCostForNextLevel(category, currentLevel);
      if (cost < 0) {
         return false;
      } else if (this.getAvailableSP() < cost) {
         return false;
      } else {
         ((EnumMap)this.allocations.get(category)).put(element, currentLevel + 1);
         this.spSpent += cost;
         return true;
      }
   }

   public void respecAll() {
      for(StatCategory cat : StatCategory.values()) {
         ((EnumMap)this.allocations.get(cat)).clear();
      }

      this.spSpent = 0;
      this.lastRespecTime = System.currentTimeMillis();
   }

   public void grantSP(int amount) {
      this.spEarned = Math.min(this.spEarned + amount, 120);
   }

   public void setSPEarned(int amount) {
      this.spEarned = amount;
   }

   public void grantRespecTokens(int amount) {
      this.respecTokens += amount;
   }

   public void consumeRespecToken() {
      if (this.respecTokens > 0) {
         --this.respecTokens;
      }

   }

   public double getOffenseBonus(StatElement element) {
      if (element != null && element != StatElement.GENERIC_NINJUTSU && element != StatElement.TAIJUTSU) {
         StatCategory cat = element.getOffenseCategory();
         if (cat == null) {
            return (double)0.0F;
         } else {
            int level = this.getLevel(cat, element);
            if (level == 0) {
               return (double)0.0F;
            } else if (cat == StatCategory.NATURE_OFFENSE) {
               return (double)level * 0.025;
            } else {
               return cat == StatCategory.KG_OFFENSE ? (double)level * 0.04 : (double)0.0F;
            }
         }
      } else {
         return (double)0.0F;
      }
   }

   public double getDefenseReduction(StatElement element) {
      if (element != null && element != StatElement.GENERIC_NINJUTSU && element != StatElement.TAIJUTSU) {
         StatCategory cat = element.getDefenseCategory();
         if (cat == null) {
            return (double)0.0F;
         } else {
            int level = this.getLevel(cat, element);
            if (level == 0) {
               return (double)0.0F;
            } else if (cat == StatCategory.NATURE_DEFENSE) {
               return (double)level * 0.0175;
            } else {
               return cat == StatCategory.KG_DEFENSE ? (double)level * 0.02 : (double)0.0F;
            }
         }
      } else {
         return (double)0.0F;
      }
   }

   public double getAverageDefenseReduction() {
      double total = (double)0.0F;
      int count = 0;

      for(StatElement el : StatElement.values()) {
         if (el.isBaseNature()) {
            double red = this.getDefenseReduction(el);
            if (red > (double)0.0F) {
               total += red;
               ++count;
            }
         }
      }

      return count > 0 ? total / (double)count : (double)0.0F;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setInteger("spEarned", this.spEarned);
      tag.setInteger("spSpent", this.spSpent);
      tag.setInteger("respecTokens", this.respecTokens);
      tag.setLong("lastRespecTime", this.lastRespecTime);
      tag.setInteger("ceCharges", this.chakraEnhancementCharges);
      tag.setInteger("ironBodyCharges", this.ironBodyCharges);

      for(StatCategory cat : StatCategory.values()) {
         EnumMap<StatElement, Integer> map = (EnumMap)this.allocations.get(cat);
         if (map != null && !map.isEmpty()) {
            NBTTagList list = new NBTTagList();

            for(Map.Entry<StatElement, Integer> entry : map.entrySet()) {
               if ((Integer)entry.getValue() > 0) {
                  NBTTagCompound entryTag = new NBTTagCompound();
                  entryTag.setByte("e", (byte)((StatElement)entry.getKey()).getId());
                  entryTag.setByte("l", (byte)(Integer)entry.getValue());
                  list.appendTag(entryTag);
               }
            }

            tag.setTag("cat_" + cat.getId(), list);
         }
      }

      return tag;
   }

   public void readFromNBT(NBTTagCompound tag) {
      this.spEarned = tag.getInteger("spEarned");
      this.spSpent = tag.getInteger("spSpent");
      this.respecTokens = tag.getInteger("respecTokens");
      this.lastRespecTime = tag.getLong("lastRespecTime");
      this.chakraEnhancementCharges = tag.getInteger("ceCharges");
      this.ironBodyCharges = tag.getInteger("ironBodyCharges");

      for(StatCategory cat : StatCategory.values()) {
         EnumMap<StatElement, Integer> map = (EnumMap)this.allocations.get(cat);
         map.clear();
         String key = "cat_" + cat.getId();
         if (tag.hasKey(key)) {
            NBTTagList list = tag.getTagList(key, 10);

            for(int i = 0; i < list.tagCount(); ++i) {
               NBTTagCompound entryTag = list.getCompoundTagAt(i);
               StatElement el = StatElement.fromId(entryTag.getByte("e"));
               int level = entryTag.getByte("l") & 255;
               if (el != null && level > 0) {
                  map.put(el, level);
               }
            }
         }
      }

   }

   public void copyFrom(PlayerStatData other) {
      this.spEarned = other.spEarned;
      this.spSpent = other.spSpent;
      this.respecTokens = other.respecTokens;
      this.lastRespecTime = other.lastRespecTime;
      this.chakraEnhancementCharges = other.chakraEnhancementCharges;
      this.ironBodyCharges = other.ironBodyCharges;

      for(StatCategory cat : StatCategory.values()) {
         ((EnumMap)this.allocations.get(cat)).clear();
         ((EnumMap)this.allocations.get(cat)).putAll((Map)other.allocations.get(cat));
      }

   }
}
