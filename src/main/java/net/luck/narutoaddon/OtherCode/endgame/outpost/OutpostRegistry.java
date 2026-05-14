package net.luck.narutoaddon.OtherCode.endgame.outpost;

import net.minecraft.util.math.BlockPos;

import java.util.*;

public class OutpostRegistry {
   private static final Map<String, OutpostDefinition> OUTPOSTS = new LinkedHashMap();
   private static final List<EncounterGroup> ENCOUNTER_GROUPS = new ArrayList();

   public static void init() {
      OUTPOSTS.clear();
      ENCOUNTER_GROUPS.clear();
      registerLocation(new OutpostDefinition("eastern_hideout", "Eastern Hideout", new BlockPos(800, 65, -900), "A concealed cave network east of the Land of Fire, used by rogue shinobi as a base of operations.", 50, 7200000L, 4));
      registerLocation(new OutpostDefinition("wind_rain_border", "Wind-Rain Border Cave", new BlockPos(-2100, 58, -200), "A hidden lair carved into the cliffs where the Land of Wind meets the Land of Rain.", 50, 7200000L, 4));
      registerLocation(new OutpostDefinition("river_country_camp", "River Country Camp", new BlockPos(-1400, 60, 500), "An abandoned encampment in the Land of Rivers, now a staging ground for dangerous criminals.", 50, 7200000L, 4));
      registerLocation(new OutpostDefinition("northern_watchtower", "Northern Watchtower", new BlockPos(-1050, 65, -2050), "A crumbling watchtower on the northern frontier, occupied by hostile forces.", 50, 7200000L, 4));
      registerLocation(new OutpostDefinition("forest_outpost", "Forest Outpost", new BlockPos(-741, 62, -1750), "A fortified position hidden deep within the forest, guarded by powerful enemies.", 50, 7200000L, 4));
      registerLocation(new OutpostDefinition("southern_ruins", "Southern Ruins", new BlockPos(-400, 60, -1600), "Ancient ruins repurposed as a stronghold by dangerous rogue shinobi.", 50, 7200000L, 4));
      registerEncounter(new EncounterGroup("itachi_kisame", "Itachi & Kisame", new String[]{"itachi", "kisame"}, "The Akatsuki duo — a master of Sharingan genjutsu and the Monster of the Mist."));
      registerEncounter(new EncounterGroup("deidara_sasori", "Deidara & Sasori", new String[]{"deidara", "sasori"}, "Art is an explosion! The Akatsuki artist duo brings clay bombs and deadly puppets."));
      registerEncounter(new EncounterGroup("hidan_kakuzu", "Hidan & Kakuzu", new String[]{"hidan", "kakuzu"}, "The immortal duo — a Jashinist zealot and the five-hearted bounty hunter."));
      registerEncounter(new EncounterGroup("jirobo_kidomaru", "Jirobo & Kidomaru", new String[]{"jirobo", "kidomaru"}, "Sound Four members — a brute-force brawler and a six-armed spider archer."));
      registerEncounter(new EncounterGroup("sakon_tayuya", "Sakon & Tayuya", new String[]{"sakon", "tayuya"}, "Sound Four members — a body-splitting berserker and a genjutsu flautist."));
      registerEncounter(new EncounterGroup("kimimaro_solo", "Kimimaro", new String[]{"kimimaro"}, "Orochimaru's most loyal servant — the last of the Kaguya clan, wielding bone as weapons."));
   }

   private static void registerLocation(OutpostDefinition def) {
      OUTPOSTS.put(def.getOutpostId(), def);
   }

   private static void registerEncounter(EncounterGroup group) {
      ENCOUNTER_GROUPS.add(group);
   }

   public static OutpostDefinition get(String id) {
      return (OutpostDefinition)OUTPOSTS.get(id);
   }

   public static Collection<OutpostDefinition> getAll() {
      return OUTPOSTS.values();
   }

   public static List<EncounterGroup> getAllEncounterGroups() {
      return ENCOUNTER_GROUPS;
   }

   public static EncounterGroup getEncounterGroup(String groupId) {
      for(EncounterGroup g : ENCOUNTER_GROUPS) {
         if (g.getGroupId().equals(groupId)) {
            return g;
         }
      }

      return null;
   }

   public static int getLocationCount() {
      return OUTPOSTS.size();
   }

   public static int getEncounterGroupCount() {
      return ENCOUNTER_GROUPS.size();
   }

   public static class EncounterGroup {
      private final String groupId;
      private final String displayName;
      private final String[] bossConfigIdPrefixes;
      private final String loreDescription;

      public EncounterGroup(String groupId, String displayName, String[] bossConfigIdPrefixes, String loreDescription) {
         this.groupId = groupId;
         this.displayName = displayName;
         this.bossConfigIdPrefixes = bossConfigIdPrefixes;
         this.loreDescription = loreDescription;
      }

      public String getGroupId() {
         return this.groupId;
      }

      public String getDisplayName() {
         return this.displayName;
      }

      public String[] getBossConfigIdPrefixes() {
         return this.bossConfigIdPrefixes;
      }

      public String getLoreDescription() {
         return this.loreDescription;
      }

      public String[] getBossConfigIds(OutpostDifficultyTier tier) {
         String[] configIds = new String[this.bossConfigIdPrefixes.length];

         for(int i = 0; i < this.bossConfigIdPrefixes.length; ++i) {
            configIds[i] = this.bossConfigIdPrefixes[i] + "_" + tier.getConfigSuffix();
         }

         return configIds;
      }

      public int getBossCount() {
         return this.bossConfigIdPrefixes.length;
      }
   }
}
