package net.luck.narutoaddon.OtherCode.quest.npc;

import net.minecraft.util.ResourceLocation;

public class NpcConfig {
   private final String configId;
   private final String entityRegistryId;
   private final String displayName;
   private final ResourceLocation texture;
   private final Behavior behavior;
   private final double maxHealth;
   private final double attackDamage;
   private final double armor;
   private final double movementSpeed;
   private final float renderScale;
   private final boolean knockbackImmune;
   private final Float spawnYaw;
   private final String weaponItemId;
   private final String offhandItemId;
   private final String helmetItemId;
   private final String chestplateItemId;
   private final String leggingsItemId;
   private final boolean hasRangedAttack;
   private final int combatTier;
   private final int natureType;
   private final float jutsuPower;
   private final float kunaiSpeed;
   private final float kunaiInaccuracy;
   private final String teamRole;
   private final int spawnDelayTicks;
   private final boolean hasHeadhunter;
   private final NpcPose pose;
   private final boolean useIceNeedles;
   private final boolean hasIceDomePhase;
   private final float iceDomeHealthThreshold;
   private final boolean hasWaterDragon;
   private final float waterDragonPower;
   private final float ghostAlpha;
   private final int themeColor;
   private final int accentColor;
   private final boolean hasSwordCombo;
   private final boolean hasHiddenMist;
   private final float hiddenMistThreshold;
   private final boolean hasWaterPrison;
   private final String combatStyle;
   private final float rageThreshold;
   private final float rageDamageMultiplier;
   private final float rageSpeedMultiplier;
   private final float cooldownMultiplier;
   private final float damageMultiplier;
   private final float trueDamageSplit;
   private final double leashRange;
   private final double leashRangeX;
   public static final int NATURE_NONE = 0;
   public static final int NATURE_FIRE = 1;
   public static final int NATURE_WIND = 2;
   public static final int NATURE_WATER = 3;
   public static final int NATURE_LIGHTNING = 4;
   public static final int NATURE_ICE = 5;
   public static final int NATURE_EARTH = 6;

   private NpcConfig(Builder builder) {
      this.configId = builder.configId;
      this.entityRegistryId = builder.entityRegistryId;
      this.displayName = builder.displayName;
      this.texture = builder.texture;
      this.behavior = builder.behavior;
      this.maxHealth = builder.maxHealth;
      this.attackDamage = builder.attackDamage;
      this.armor = builder.armor;
      this.movementSpeed = builder.movementSpeed;
      this.renderScale = builder.renderScale;
      this.knockbackImmune = builder.knockbackImmune;
      this.spawnYaw = builder.spawnYaw;
      this.weaponItemId = builder.weaponItemId;
      this.offhandItemId = builder.offhandItemId;
      this.helmetItemId = builder.helmetItemId;
      this.chestplateItemId = builder.chestplateItemId;
      this.leggingsItemId = builder.leggingsItemId;
      this.hasRangedAttack = builder.hasRangedAttack;
      this.combatTier = builder.combatTier;
      this.natureType = builder.natureType;
      this.jutsuPower = builder.jutsuPower;
      this.kunaiSpeed = builder.kunaiSpeed;
      this.kunaiInaccuracy = builder.kunaiInaccuracy;
      this.teamRole = builder.teamRole;
      this.spawnDelayTicks = builder.spawnDelayTicks;
      this.hasHeadhunter = builder.hasHeadhunter;
      this.pose = builder.pose;
      this.useIceNeedles = builder.useIceNeedles;
      this.hasIceDomePhase = builder.hasIceDomePhase;
      this.iceDomeHealthThreshold = builder.iceDomeHealthThreshold;
      this.hasWaterDragon = builder.hasWaterDragon;
      this.waterDragonPower = builder.waterDragonPower;
      this.ghostAlpha = builder.ghostAlpha;
      this.themeColor = builder.themeColor;
      this.accentColor = builder.accentColor;
      this.hasSwordCombo = builder.hasSwordCombo;
      this.hasHiddenMist = builder.hasHiddenMist;
      this.hiddenMistThreshold = builder.hiddenMistThreshold;
      this.hasWaterPrison = builder.hasWaterPrison;
      this.combatStyle = builder.combatStyle;
      this.rageThreshold = builder.rageThreshold;
      this.rageDamageMultiplier = builder.rageDamageMultiplier;
      this.rageSpeedMultiplier = builder.rageSpeedMultiplier;
      this.cooldownMultiplier = builder.cooldownMultiplier;
      this.damageMultiplier = builder.damageMultiplier;
      this.trueDamageSplit = builder.trueDamageSplit;
      this.leashRange = builder.leashRange;
      this.leashRangeX = builder.leashRangeX;
   }

   public String getConfigId() {
      return this.configId;
   }

   public String getEntityRegistryId() {
      return this.entityRegistryId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }

   public Behavior getBehavior() {
      return this.behavior;
   }

   public double getMaxHealth() {
      return this.maxHealth;
   }

   public double getAttackDamage() {
      return this.attackDamage;
   }

   public double getArmor() {
      return this.armor;
   }

   public double getMovementSpeed() {
      return this.movementSpeed;
   }

   public float getRenderScale() {
      return this.renderScale;
   }

   public boolean isKnockbackImmune() {
      return this.knockbackImmune;
   }

   public Float getSpawnYaw() {
      return this.spawnYaw;
   }

   public boolean hasFixedSpawnYaw() {
      return this.spawnYaw != null;
   }

   public boolean isPassive() {
      return this.behavior == Behavior.PASSIVE;
   }

   public String getWeaponItemId() {
      return this.weaponItemId;
   }

   public String getOffhandItemId() {
      return this.offhandItemId;
   }

   public String getHelmetItemId() {
      return this.helmetItemId;
   }

   public String getChestplateItemId() {
      return this.chestplateItemId;
   }

   public String getLeggingsItemId() {
      return this.leggingsItemId;
   }

   public boolean hasRangedAttack() {
      return this.hasRangedAttack;
   }

   public int getCombatTier() {
      return this.combatTier;
   }

   public int getNatureType() {
      return this.natureType;
   }

   public boolean hasNatureJutsu() {
      return this.natureType != 0;
   }

   public float getJutsuPower() {
      return this.jutsuPower;
   }

   public float getKunaiSpeed() {
      return this.kunaiSpeed;
   }

   public float getKunaiInaccuracy() {
      return this.kunaiInaccuracy;
   }

   public String getTeamRole() {
      return this.teamRole;
   }

   public boolean isRangedRole() {
      return "RANGED".equals(this.teamRole);
   }

   public boolean isFlankerRole() {
      return "FLANKER".equals(this.teamRole);
   }

   public boolean isSupportRole() {
      return "SUPPORT".equals(this.teamRole);
   }

   public int getSpawnDelayTicks() {
      return this.spawnDelayTicks;
   }

   public boolean hasHeadhunter() {
      return this.hasHeadhunter;
   }

   public NpcPose getPose() {
      return this.pose;
   }

   public boolean isUseIceNeedles() {
      return this.useIceNeedles;
   }

   public boolean isHasIceDomePhase() {
      return this.hasIceDomePhase;
   }

   public float getIceDomeHealthThreshold() {
      return this.iceDomeHealthThreshold;
   }

   public boolean isHasWaterDragon() {
      return this.hasWaterDragon;
   }

   public float getWaterDragonPower() {
      return this.waterDragonPower;
   }

   public float getGhostAlpha() {
      return this.ghostAlpha;
   }

   public boolean isGhost() {
      return this.ghostAlpha < 1.0F;
   }

   public int getThemeColor() {
      return this.themeColor;
   }

   public int getAccentColor() {
      return this.accentColor;
   }

   public boolean hasHealthBarColors() {
      return this.themeColor != 0;
   }

   public boolean hasSwordCombo() {
      return this.hasSwordCombo;
   }

   public boolean hasHiddenMist() {
      return this.hasHiddenMist;
   }

   public float getHiddenMistThreshold() {
      return this.hiddenMistThreshold;
   }

   public boolean hasWaterPrison() {
      return this.hasWaterPrison;
   }

   public String getCombatStyle() {
      return this.combatStyle;
   }

   public float getRageThreshold() {
      return this.rageThreshold;
   }

   public float getRageDamageMultiplier() {
      return this.rageDamageMultiplier;
   }

   public float getRageSpeedMultiplier() {
      return this.rageSpeedMultiplier;
   }

   public float getCooldownMultiplier() {
      return this.cooldownMultiplier;
   }

   public float getDamageMultiplier() {
      return this.damageMultiplier;
   }

   public float getTrueDamageSplit() {
      return this.trueDamageSplit;
   }

   public double getLeashRange() {
      return this.leashRange;
   }

   public double getLeashRangeX() {
      return this.leashRangeX;
   }

   public static Builder builder(String configId, String entityRegistryId) {
      return new Builder(configId, entityRegistryId);
   }

   public static enum Behavior {
      PASSIVE,
      HOSTILE;
   }

   public static class Builder {
      private final String configId;
      private final String entityRegistryId;
      private String displayName;
      private ResourceLocation texture;
      private Behavior behavior;
      private double maxHealth;
      private double attackDamage;
      private double armor;
      private double movementSpeed;
      private float renderScale;
      private boolean knockbackImmune;
      private Float spawnYaw;
      private String weaponItemId;
      private String offhandItemId;
      private String helmetItemId;
      private String chestplateItemId;
      private String leggingsItemId;
      private boolean hasRangedAttack;
      private int combatTier;
      private int natureType;
      private float jutsuPower;
      private float kunaiSpeed;
      private float kunaiInaccuracy;
      private String teamRole;
      private int spawnDelayTicks;
      private boolean hasHeadhunter;
      private NpcPose pose;
      private boolean useIceNeedles;
      private boolean hasIceDomePhase;
      private float iceDomeHealthThreshold;
      private boolean hasWaterDragon;
      private float waterDragonPower;
      private float ghostAlpha;
      private int themeColor;
      private int accentColor;
      private boolean hasSwordCombo;
      private boolean hasHiddenMist;
      private float hiddenMistThreshold;
      private boolean hasWaterPrison;
      private String combatStyle;
      private float rageThreshold;
      private float rageDamageMultiplier;
      private float rageSpeedMultiplier;
      private float cooldownMultiplier;
      private float damageMultiplier;
      private float trueDamageSplit;
      private double leashRange;
      private double leashRangeX;

      private Builder(String configId, String entityRegistryId) {
         this.displayName = "NPC";
         this.behavior = Behavior.PASSIVE;
         this.maxHealth = (double)20.0F;
         this.attackDamage = (double)3.0F;
         this.armor = (double)0.0F;
         this.movementSpeed = 0.3;
         this.renderScale = 1.0F;
         this.knockbackImmune = false;
         this.spawnYaw = null;
         this.weaponItemId = null;
         this.offhandItemId = null;
         this.helmetItemId = null;
         this.chestplateItemId = null;
         this.leggingsItemId = null;
         this.hasRangedAttack = false;
         this.combatTier = 0;
         this.natureType = 0;
         this.jutsuPower = 0.0F;
         this.kunaiSpeed = 1.6F;
         this.kunaiInaccuracy = 2.0F;
         this.teamRole = null;
         this.spawnDelayTicks = 0;
         this.hasHeadhunter = false;
         this.pose = NpcPose.STANDING;
         this.useIceNeedles = false;
         this.hasIceDomePhase = false;
         this.iceDomeHealthThreshold = 0.3F;
         this.hasWaterDragon = false;
         this.waterDragonPower = 1.5F;
         this.ghostAlpha = 1.0F;
         this.themeColor = 0;
         this.accentColor = 0;
         this.hasSwordCombo = false;
         this.hasHiddenMist = false;
         this.hiddenMistThreshold = 0.6F;
         this.hasWaterPrison = false;
         this.combatStyle = "STANDARD";
         this.rageThreshold = 0.0F;
         this.rageDamageMultiplier = 1.0F;
         this.rageSpeedMultiplier = 1.0F;
         this.cooldownMultiplier = 1.0F;
         this.damageMultiplier = 1.0F;
         this.trueDamageSplit = 0.0F;
         this.leashRange = (double)0.0F;
         this.leashRangeX = (double)0.0F;
         this.configId = configId;
         this.entityRegistryId = entityRegistryId;
      }

      public Builder displayName(String name) {
         this.displayName = name;
         return this;
      }

      public Builder texture(ResourceLocation tex) {
         this.texture = tex;
         return this;
      }

      public Builder texture(String domain, String path) {
         this.texture = new ResourceLocation(domain, path);
         return this;
      }

      public Builder behavior(Behavior b) {
         this.behavior = b;
         return this;
      }

      public Builder maxHealth(double hp) {
         this.maxHealth = hp;
         return this;
      }

      public Builder attackDamage(double dmg) {
         this.attackDamage = dmg;
         return this;
      }

      public Builder armor(double arm) {
         this.armor = arm;
         return this;
      }

      public Builder movementSpeed(double spd) {
         this.movementSpeed = spd;
         return this;
      }

      public Builder renderScale(float scale) {
         this.renderScale = scale;
         return this;
      }

      public Builder knockbackImmune(boolean immune) {
         this.knockbackImmune = immune;
         return this;
      }

      public Builder spawnYaw(float yaw) {
         this.spawnYaw = yaw;
         return this;
      }

      public Builder weaponItemId(String id) {
         this.weaponItemId = id;
         return this;
      }

      public Builder offhandItemId(String id) {
         this.offhandItemId = id;
         return this;
      }

      public Builder helmetItemId(String id) {
         this.helmetItemId = id;
         return this;
      }

      public Builder chestplateItemId(String id) {
         this.chestplateItemId = id;
         return this;
      }

      public Builder leggingsItemId(String id) {
         this.leggingsItemId = id;
         return this;
      }

      public Builder hasRangedAttack(boolean ranged) {
         this.hasRangedAttack = ranged;
         return this;
      }

      public Builder combatTier(int tier) {
         this.combatTier = tier;
         return this;
      }

      public Builder natureType(int nature) {
         this.natureType = nature;
         return this;
      }

      public Builder jutsuPower(float power) {
         this.jutsuPower = power;
         return this;
      }

      public Builder kunaiSpeed(float speed) {
         this.kunaiSpeed = speed;
         return this;
      }

      public Builder kunaiInaccuracy(float inaccuracy) {
         this.kunaiInaccuracy = inaccuracy;
         return this;
      }

      public Builder teamRole(String role) {
         this.teamRole = role;
         return this;
      }

      public Builder spawnDelayTicks(int ticks) {
         this.spawnDelayTicks = ticks;
         return this;
      }

      public Builder hasHeadhunter(boolean hh) {
         this.hasHeadhunter = hh;
         return this;
      }

      public Builder pose(NpcPose p) {
         this.pose = p;
         return this;
      }

      public Builder useIceNeedles(boolean ice) {
         this.useIceNeedles = ice;
         return this;
      }

      public Builder hasIceDomePhase(boolean dome) {
         this.hasIceDomePhase = dome;
         return this;
      }

      public Builder iceDomeHealthThreshold(float threshold) {
         this.iceDomeHealthThreshold = threshold;
         return this;
      }

      public Builder hasWaterDragon(boolean wd) {
         this.hasWaterDragon = wd;
         return this;
      }

      public Builder waterDragonPower(float power) {
         this.waterDragonPower = power;
         return this;
      }

      public Builder ghostAlpha(float alpha) {
         this.ghostAlpha = alpha;
         return this;
      }

      public Builder themeColor(int color) {
         this.themeColor = color;
         return this;
      }

      public Builder accentColor(int color) {
         this.accentColor = color;
         return this;
      }

      public Builder healthBarColors(int theme, int accent) {
         this.themeColor = theme;
         this.accentColor = accent;
         return this;
      }

      public Builder hasSwordCombo(boolean combo) {
         this.hasSwordCombo = combo;
         return this;
      }

      public Builder hasHiddenMist(boolean mist) {
         this.hasHiddenMist = mist;
         return this;
      }

      public Builder hiddenMistThreshold(float threshold) {
         this.hiddenMistThreshold = threshold;
         return this;
      }

      public Builder hasWaterPrison(boolean prison) {
         this.hasWaterPrison = prison;
         return this;
      }

      public Builder combatStyle(String style) {
         this.combatStyle = style;
         return this;
      }

      public Builder rageThreshold(float threshold) {
         this.rageThreshold = threshold;
         return this;
      }

      public Builder rageDamageMultiplier(float mul) {
         this.rageDamageMultiplier = mul;
         return this;
      }

      public Builder rageSpeedMultiplier(float mul) {
         this.rageSpeedMultiplier = mul;
         return this;
      }

      public Builder cooldownMultiplier(float mul) {
         this.cooldownMultiplier = mul;
         return this;
      }

      public Builder damageMultiplier(float mul) {
         this.damageMultiplier = mul;
         return this;
      }

      public Builder trueDamageSplit(float split) {
         this.trueDamageSplit = Math.max(0.0F, Math.min(1.0F, split));
         return this;
      }

      public Builder leashRange(double range) {
         this.leashRange = range;
         return this;
      }

      public Builder leashRangeX(double range) {
         this.leashRangeX = range;
         return this;
      }

      public NpcConfig build() {
         if (this.combatTier < 0 || this.combatTier > 4) {
            System.err.println("[InfTsuk] NpcConfig '" + this.configId + "': combatTier " + this.combatTier + " out of range 0-4, clamping");
            this.combatTier = Math.max(0, Math.min(4, this.combatTier));
         }

         if (this.natureType < 0 || this.natureType > 6) {
            System.err.println("[InfTsuk] NpcConfig '" + this.configId + "': natureType " + this.natureType + " out of range 0-6, clamping");
            this.natureType = Math.max(0, Math.min(6, this.natureType));
         }

         if (this.iceDomeHealthThreshold < 0.0F || this.iceDomeHealthThreshold > 1.0F) {
            System.err.println("[InfTsuk] NpcConfig '" + this.configId + "': iceDomeHealthThreshold " + this.iceDomeHealthThreshold + " out of range, clamping");
            this.iceDomeHealthThreshold = Math.max(0.0F, Math.min(1.0F, this.iceDomeHealthThreshold));
         }

         if (this.texture == null) {
            this.texture = new ResourceLocation("inftsukaddon", "textures/iraya1.png");
         }

         return new NpcConfig(this);
      }
   }
}
