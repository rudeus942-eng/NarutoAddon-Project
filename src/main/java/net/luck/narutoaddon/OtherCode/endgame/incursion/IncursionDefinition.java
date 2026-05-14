package net.luck.narutoaddon.OtherCode.endgame.incursion;

import java.util.*;

public class IncursionDefinition {
   private static final Map<String, IncursionDefinition> TEMPLATES = new LinkedHashMap();
   private final String templateId;
   private final String displayName;
   private final String announcement;
   private final List<IncursionWave> waves;

   public IncursionDefinition(String templateId, String displayName, String announcement, List<IncursionWave> waves) {
      this.templateId = templateId;
      this.displayName = displayName;
      this.announcement = announcement;
      this.waves = waves;
   }

   public String getTemplateId() {
      return this.templateId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getAnnouncement() {
      return this.announcement;
   }

   public List<IncursionWave> getWaves() {
      return this.waves;
   }

   public static void init() {
      TEMPLATES.clear();
      register(new IncursionDefinition("rogue_ninja_raid", "Rogue Ninja Raid", "§c§l[INCURSION] §eRogue ninja have been spotted gathering nearby! Defend the area!", Arrays.asList(new IncursionWave(1, new String[]{"wave_grunt_1"}, new int[]{4}, 1200), new IncursionWave(2, new String[]{"wave_grunt_2", "wave_captain_1"}, new int[]{4, 1}, 1200), new IncursionWave(3, new String[]{"wave_captain_2", "wave_grunt_3"}, new int[]{3, 2}, 1200), new IncursionWave(4, new String[]{"wave_elite_1", "wave_captain_1"}, new int[]{1, 2}, 1200))));
      register(new IncursionDefinition("bandit_horde", "Bandit Horde", "§c§l[INCURSION] §eA massive bandit horde is approaching! Rally to defend!", Arrays.asList(new IncursionWave(1, new String[]{"wave_grunt_1"}, new int[]{4}, 1200), new IncursionWave(2, new String[]{"wave_grunt_1", "wave_grunt_2"}, new int[]{3, 3}, 1200), new IncursionWave(3, new String[]{"wave_grunt_2", "wave_grunt_3"}, new int[]{4, 4}, 1200), new IncursionWave(4, new String[]{"wave_grunt_3", "wave_grunt_4"}, new int[]{5, 5}, 1200), new IncursionWave(5, new String[]{"wave_grunt_4", "wave_grunt_5", "wave_elite_2"}, new int[]{6, 5, 1}, 1200))));
      register(new IncursionDefinition("akatsuki_scout", "Akatsuki Scout Force", "§c§l[INCURSION] §4Akatsuki scouts have been detected! Intercept them immediately!", Arrays.asList(new IncursionWave(1, new String[]{"wave_grunt_4"}, new int[]{6}, 1800), new IncursionWave(2, new String[]{"wave_captain_4", "wave_grunt_4"}, new int[]{4, 2}, 1800), new IncursionWave(3, new String[]{"wave_elite_4", "wave_captain_4"}, new int[]{1, 3}, 1800))));
      register(new IncursionDefinition("sound_invasion", "Sound Invasion", "§c§l[INCURSION] §5Sound ninja are launching an invasion! Stop them!", Arrays.asList(new IncursionWave(1, new String[]{"wave_grunt_3"}, new int[]{5}, 1200), new IncursionWave(2, new String[]{"wave_captain_3", "wave_grunt_3"}, new int[]{3, 3}, 1200), new IncursionWave(3, new String[]{"wave_elite_3", "wave_captain_3"}, new int[]{1, 2}, 1200))));
      register(new IncursionDefinition("reanimation_uprising", "Reanimation Uprising", "§c§l[INCURSION] §8Reanimated shinobi are rising from the dead! Seal them away!", Arrays.asList(new IncursionWave(1, new String[]{"wave_grunt_5"}, new int[]{5}, 1800), new IncursionWave(2, new String[]{"wave_grunt_5", "wave_captain_5"}, new int[]{4, 2}, 1800), new IncursionWave(3, new String[]{"wave_captain_5", "wave_elite_5"}, new int[]{3, 1}, 1800), new IncursionWave(4, new String[]{"wave_elite_5", "wave_grunt_5"}, new int[]{2, 4}, 1800))));
   }

   private static void register(IncursionDefinition def) {
      TEMPLATES.put(def.getTemplateId(), def);
   }

   public static IncursionDefinition get(String id) {
      return (IncursionDefinition)TEMPLATES.get(id);
   }

   public static Collection<IncursionDefinition> getAll() {
      return TEMPLATES.values();
   }
}
