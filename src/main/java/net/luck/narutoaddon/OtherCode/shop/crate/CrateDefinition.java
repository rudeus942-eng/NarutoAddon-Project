
package net.luck.narutoaddon.OtherCode.shop.crate;

import net.luck.narutoaddon.OtherCode.shop.core.ItemRarity;
import net.luck.narutoaddon.OtherCode.shop.core.ShopCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CrateDefinition {
   private final String crateId;
   private final String displayName;
   private final String description;
   private final ShopCategory category;
   private final long price;
   private final String iconItemId;
   private final int iconItemMeta;
   private final List<CrateLootEntry> lootTable;
   private final double totalWeight;
   private int sSoftPity;
   private int sHardPity;
   private double sEscalation;
   private int sPlusSoftPity;
   private int sPlusHardPity;
   private double sPlusEscalation;
   private int tenRollGuaranteedMinTier;
   private int crystalPrice;
   private boolean directPurchase;

   public CrateDefinition(String crateId, String displayName, String description, ShopCategory category, long price, String iconItemId, int iconItemMeta, List<CrateLootEntry> lootTable) {
      this.crateId = crateId;
      this.displayName = displayName;
      this.description = description;
      this.category = category;
      this.price = price;
      this.iconItemId = iconItemId;
      this.iconItemMeta = iconItemMeta;
      this.lootTable = new ArrayList(lootTable);
      double w = (double)0.0F;

      for(CrateLootEntry entry : this.lootTable) {
         w += entry.getWeight();
      }

      this.totalWeight = w;
      this.sSoftPity = 0;
      this.sHardPity = 0;
      this.sEscalation = (double)0.0F;
      this.sPlusSoftPity = 0;
      this.sPlusHardPity = 0;
      this.sPlusEscalation = (double)0.0F;
      this.tenRollGuaranteedMinTier = 2;
      this.crystalPrice = 0;
      this.directPurchase = false;
   }

   public CrateDefinition withSPity(int softPity, int hardPity, double escalation) {
      this.sSoftPity = softPity;
      this.sHardPity = hardPity;
      this.sEscalation = escalation;
      return this;
   }

   public CrateDefinition withSPlusPity(int softPity, int hardPity, double escalation) {
      this.sPlusSoftPity = softPity;
      this.sPlusHardPity = hardPity;
      this.sPlusEscalation = escalation;
      return this;
   }

   public CrateDefinition withTenRollMinTier(int minTierOrdinal) {
      this.tenRollGuaranteedMinTier = minTierOrdinal;
      return this;
   }

   public CrateDefinition withCrystalPrice(int crystalPrice) {
      this.crystalPrice = crystalPrice;
      return this;
   }

   public CrateDefinition withDirectPurchase(boolean directPurchase) {
      this.directPurchase = directPurchase;
      return this;
   }

   public int getSSoftPity() {
      return this.sSoftPity;
   }

   public int getSHardPity() {
      return this.sHardPity;
   }

   public double getSEscalation() {
      return this.sEscalation;
   }

   public int getSPlusSoftPity() {
      return this.sPlusSoftPity;
   }

   public int getSPlusHardPity() {
      return this.sPlusHardPity;
   }

   public double getSPlusEscalation() {
      return this.sPlusEscalation;
   }

   public int getTenRollGuaranteedMinTier() {
      return this.tenRollGuaranteedMinTier;
   }

   public boolean hasSPity() {
      return this.sSoftPity > 0 && this.sHardPity > 0;
   }

   public boolean hasSPlusPity() {
      return this.sPlusSoftPity > 0 && this.sPlusHardPity > 0;
   }

   public int getCrystalPrice() {
      return this.crystalPrice;
   }

   public boolean usesCrystals() {
      return this.crystalPrice > 0;
   }

   public boolean isDirectPurchase() {
      return this.directPurchase;
   }

   public String getCrateId() {
      return this.crateId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   public ShopCategory getCategory() {
      return this.category;
   }

   public long getPrice() {
      return this.price;
   }

   public String getIconItemId() {
      return this.iconItemId;
   }

   public int getIconItemMeta() {
      return this.iconItemMeta;
   }

   public double getTotalWeight() {
      return this.totalWeight;
   }

   public List<CrateLootEntry> getEntries() {
      return Collections.unmodifiableList(this.lootTable);
   }

   public float getEntryChance(CrateLootEntry entry) {
      return (float)(entry.getWeight() / this.totalWeight * (double)100.0F);
   }

   public CrateLootEntry rollReward(Random rng) {
      double roll = rng.nextDouble() * this.totalWeight;
      double accumulated = (double)0.0F;

      for(CrateLootEntry entry : this.lootTable) {
         accumulated += entry.getWeight();
         if (roll < accumulated) {
            return entry;
         }
      }

      return (CrateLootEntry)this.lootTable.get(this.lootTable.size() - 1);
   }

   public CrateLootEntry rollRewardWithPity(Random rng, int rollsSinceSRank, int rollsSinceSPlusRank) {
      if (this.hasSPlusPity() && rollsSinceSPlusRank >= this.sPlusHardPity) {
         return this.rollFromTierOrHigher(rng, ItemRarity.S_PLUS_RANK);
      } else if (this.hasSPity() && rollsSinceSRank >= this.sHardPity) {
         return this.rollFromTierOrHigher(rng, ItemRarity.S_RANK);
      } else {
         double[] adjustedWeights = new double[this.lootTable.size()];
         double sBonus = (double)0.0F;
         double sPlusBonus = (double)0.0F;
         if (this.hasSPity() && rollsSinceSRank > this.sSoftPity) {
            sBonus = (double)(rollsSinceSRank - this.sSoftPity) * this.sEscalation * this.totalWeight;
         }

         if (this.hasSPlusPity() && rollsSinceSPlusRank > this.sPlusSoftPity) {
            sPlusBonus = (double)(rollsSinceSPlusRank - this.sPlusSoftPity) * this.sPlusEscalation * this.totalWeight;
         }

         double totalBonusWeight = sBonus + sPlusBonus;
         double adjustedTotal = (double)0.0F;

         for(int i = 0; i < this.lootTable.size(); ++i) {
            CrateLootEntry entry = (CrateLootEntry)this.lootTable.get(i);
            double w = entry.getWeight();
            if (entry.getRarity() == ItemRarity.S_RANK && sBonus > (double)0.0F) {
               w += sBonus;
            } else if (entry.getRarity() == ItemRarity.S_PLUS_RANK && sPlusBonus > (double)0.0F) {
               w += sPlusBonus;
            } else if (totalBonusWeight > (double)0.0F && entry.getRarity().id < ItemRarity.S_RANK.id) {
               double proportion = entry.getWeight() / this.getLowerTierWeight();
               w = Math.max(0.01, w - totalBonusWeight * proportion);
            }

            adjustedWeights[i] = w;
            adjustedTotal += w;
         }

         double roll = rng.nextDouble() * adjustedTotal;
         double accumulated = (double)0.0F;

         for(int i = 0; i < adjustedWeights.length; ++i) {
            accumulated += adjustedWeights[i];
            if (roll < accumulated) {
               return (CrateLootEntry)this.lootTable.get(i);
            }
         }

         return (CrateLootEntry)this.lootTable.get(this.lootTable.size() - 1);
      }
   }

   public CrateLootEntry rollRewardWithCashPity(Random rng, int rollsSinceNonFiller) {
      if (!this.hasSPity()) {
         return this.rollReward(rng);
      } else if (rollsSinceNonFiller >= this.sHardPity) {
         return this.rollCashHardPity(rng);
      } else if (rollsSinceNonFiller > this.sSoftPity) {
         double[] adjustedWeights = new double[this.lootTable.size()];
         int stepsIntoSoftPity = rollsSinceNonFiller - this.sSoftPity;
         int stepsToHardPity = this.sHardPity - this.sSoftPity;
         double fillerReduction = Math.min((double)0.5F, (double)stepsIntoSoftPity / (double)stepsToHardPity * (double)0.5F);
         double fillerWeightTotal = (double)0.0F;
         double nonFillerWeightTotal = (double)0.0F;

         for(CrateLootEntry entry : this.lootTable) {
            if (entry.getRarity().id <= ItemRarity.C_RANK.id) {
               fillerWeightTotal += entry.getWeight();
            } else {
               nonFillerWeightTotal += entry.getWeight();
            }
         }

         double weightToRedistribute = fillerWeightTotal * fillerReduction;
         double adjustedTotal = (double)0.0F;

         for(int i = 0; i < this.lootTable.size(); ++i) {
            CrateLootEntry entry = (CrateLootEntry)this.lootTable.get(i);
            double w = entry.getWeight();
            if (entry.getRarity().id <= ItemRarity.C_RANK.id) {
               double proportion = entry.getWeight() / fillerWeightTotal;
               w -= weightToRedistribute * proportion;
               w = Math.max(0.01, w);
            } else if (nonFillerWeightTotal > (double)0.0F) {
               double proportion = entry.getWeight() / nonFillerWeightTotal;
               w += weightToRedistribute * proportion;
            }

            adjustedWeights[i] = w;
            adjustedTotal += w;
         }

         double roll = rng.nextDouble() * adjustedTotal;
         double accumulated = (double)0.0F;

         for(int i = 0; i < adjustedWeights.length; ++i) {
            accumulated += adjustedWeights[i];
            if (roll < accumulated) {
               return (CrateLootEntry)this.lootTable.get(i);
            }
         }

         return (CrateLootEntry)this.lootTable.get(this.lootTable.size() - 1);
      } else {
         return this.rollReward(rng);
      }
   }

   private CrateLootEntry rollCashHardPity(Random rng) {
      List<CrateLootEntry> eligible = new ArrayList();
      List<Double> pityWeights = new ArrayList();

      for(CrateLootEntry entry : this.lootTable) {
         if (entry.getRarity().id > ItemRarity.C_RANK.id) {
            double pityWeight;
            switch (entry.getRarity()) {
               case B_RANK:
                  pityWeight = (double)15.0F;
                  break;
               case A_RANK:
                  pityWeight = (double)8.0F;
                  break;
               case S_RANK:
                  pityWeight = (double)3.0F;
                  break;
               case S_PLUS_RANK:
                  pityWeight = (double)1.5F;
                  break;
               default:
                  pityWeight = (double)1.0F;
            }

            eligible.add(entry);
            pityWeights.add(pityWeight);
         }
      }

      if (eligible.isEmpty()) {
         return this.rollReward(rng);
      } else {
         double totalPityWeight = (double)0.0F;

         for(double w : pityWeights) {
            totalPityWeight += w;
         }

         double roll = rng.nextDouble() * totalPityWeight;
         double accumulated = (double)0.0F;

         for(int i = 0; i < eligible.size(); ++i) {
            accumulated += (Double)pityWeights.get(i);
            if (roll < accumulated) {
               return (CrateLootEntry)eligible.get(i);
            }
         }

         return (CrateLootEntry)eligible.get(eligible.size() - 1);
      }
   }

   private double getLowerTierWeight() {
      double w = (double)0.0F;

      for(CrateLootEntry entry : this.lootTable) {
         if (entry.getRarity().id < ItemRarity.S_RANK.id) {
            w += entry.getWeight();
         }
      }

      return w > (double)0.0F ? w : (double)1.0F;
   }

   private CrateLootEntry rollFromTierOrHigher(Random rng, ItemRarity minTier) {
      List<CrateLootEntry> eligible = new ArrayList();
      double eligibleWeight = (double)0.0F;

      for(CrateLootEntry entry : this.lootTable) {
         if (entry.getRarity().id >= minTier.id) {
            eligible.add(entry);
            eligibleWeight += entry.getWeight();
         }
      }

      if (eligible.isEmpty()) {
         return this.rollReward(rng);
      } else {
         double roll = rng.nextDouble() * eligibleWeight;
         double accumulated = (double)0.0F;

         for(CrateLootEntry entry : eligible) {
            accumulated += entry.getWeight();
            if (roll < accumulated) {
               return entry;
            }
         }

         return (CrateLootEntry)eligible.get(eligible.size() - 1);
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("crateId", this.crateId);
      nbt.setString("displayName", this.displayName);
      nbt.setString("description", this.description);
      nbt.setByte("category", (byte)this.category.id);
      nbt.setLong("price", this.price);
      nbt.setString("iconItemId", this.iconItemId);
      nbt.setInteger("iconItemMeta", this.iconItemMeta);
      nbt.setInteger("sSoftPity", this.sSoftPity);
      nbt.setInteger("sHardPity", this.sHardPity);
      nbt.setDouble("sEscalation", this.sEscalation);
      nbt.setInteger("sPlusSoftPity", this.sPlusSoftPity);
      nbt.setInteger("sPlusHardPity", this.sPlusHardPity);
      nbt.setDouble("sPlusEscalation", this.sPlusEscalation);
      nbt.setInteger("tenRollMinTier", this.tenRollGuaranteedMinTier);
      nbt.setInteger("crystalPrice", this.crystalPrice);
      nbt.setBoolean("directPurchase", this.directPurchase);
      NBTTagList lootList = new NBTTagList();

      for(CrateLootEntry entry : this.lootTable) {
         lootList.appendTag(entry.writeToNBT());
      }

      nbt.setTag("lootTable", lootList);
      return nbt;
   }

   public static CrateDefinition readFromNBT(NBTTagCompound nbt) {
      String crateId = nbt.getString("crateId");
      String displayName = nbt.getString("displayName");
      String description = nbt.getString("description");
      ShopCategory category = ShopCategory.fromId(nbt.getByte("category"));
      long price = nbt.getLong("price");
      String iconItemId = nbt.getString("iconItemId");
      int iconItemMeta = nbt.getInteger("iconItemMeta");
      NBTTagList lootList = nbt.getTagList("lootTable", 10);
      List<CrateLootEntry> lootTable = new ArrayList();

      for(int i = 0; i < lootList.tagCount(); ++i) {
         lootTable.add(CrateLootEntry.readFromNBT(lootList.getCompoundTagAt(i)));
      }

      CrateDefinition def = new CrateDefinition(crateId, displayName, description, category, price, iconItemId, iconItemMeta, lootTable);
      if (nbt.hasKey("sSoftPity")) {
         def.withSPity(nbt.getInteger("sSoftPity"), nbt.getInteger("sHardPity"), nbt.getDouble("sEscalation"));
      }

      if (nbt.hasKey("sPlusSoftPity")) {
         def.withSPlusPity(nbt.getInteger("sPlusSoftPity"), nbt.getInteger("sPlusHardPity"), nbt.getDouble("sPlusEscalation"));
      }

      if (nbt.hasKey("tenRollMinTier")) {
         def.withTenRollMinTier(nbt.getInteger("tenRollMinTier"));
      }

      if (nbt.hasKey("crystalPrice")) {
         def.withCrystalPrice(nbt.getInteger("crystalPrice"));
      }

      if (nbt.hasKey("directPurchase")) {
         def.withDirectPurchase(nbt.getBoolean("directPurchase"));
      }

      return def;
   }
}
