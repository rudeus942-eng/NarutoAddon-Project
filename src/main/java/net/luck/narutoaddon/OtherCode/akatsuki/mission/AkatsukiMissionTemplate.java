package net.luck.narutoaddon.OtherCode.akatsuki.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AkatsukiMissionTemplate {
   public static final List<Template> DAILY_TEMPLATES;
   public static final List<Template> WEEKLY_TEMPLATES;

   public static Template getRandomDaily(Random rand) {
      return (Template)DAILY_TEMPLATES.get(rand.nextInt(DAILY_TEMPLATES.size()));
   }

   public static Template getRandomWeekly(Random rand) {
      return (Template)WEEKLY_TEMPLATES.get(rand.nextInt(WEEKLY_TEMPLATES.size()));
   }

   public static Template getById(String id) {
      for(Template t : DAILY_TEMPLATES) {
         if (t.id.equals(id)) {
            return t;
         }
      }

      for(Template t : WEEKLY_TEMPLATES) {
         if (t.id.equals(id)) {
            return t;
         }
      }

      return null;
   }

   static {
      List<Template> daily = new ArrayList();
      daily.add(new Template("jinchuriki_intel", "Jinchuriki Intel", "A Jinchuriki sighting has been reported. Infiltrate the surveillance zone and eliminate the intelligence operatives.", "Eliminate the intelligence agents and their captain.", Category.DAILY, 15, 45, 400, new MissionStep[]{MissionStep.travel("Travel to the surveillance point", 0, 0), MissionStep.combat("Eliminate the perimeter scouts", new String[]{"ak_patrol_grunt", "ak_rain_agent"}, 300, 600), MissionStep.travel("Move to the intelligence compound", 200, 400), MissionStep.combat("Defeat the intelligence officers and their captain", new String[]{"ak_rain_agent", "ak_patrol_grunt", "ak_patrol_captain"}, 100, 200)}, 600, 1100, 80));
      daily.add(new Template("village_infiltration", "Village Infiltration", "Infiltrate a hidden village. Break through the ANBU response team and extract safely.", "Defeat the village patrols and ANBU response team.", Category.DAILY, 18, 55, 450, new MissionStep[]{MissionStep.travel("Approach the village perimeter", 0, 0), MissionStep.combat("Eliminate the gate patrol", new String[]{"ak_patrol_grunt", "ak_patrol_captain"}, 300, 600), MissionStep.combat("Fight through the ANBU response team", new String[]{"ak_leaf_anbu", "ak_patrol_captain", "ak_patrol_grunt"}, 200, 400), MissionStep.travel("Extract from the village", 300, 500)}, 700, 1200, 90));
      daily.add(new Template("resource_raid", "Resource Raid", "A supply caravan is passing through hostile territory. Ambush the convoy and its reinforcements.", "Defeat the supply convoy guards and reinforcements.", Category.DAILY, 15, 45, 400, new MissionStep[]{MissionStep.travel("Travel to the supply convoy route", 0, 0), MissionStep.combat("Ambush the convoy escort", new String[]{"ak_stone_guard", "ak_bounty_hunter", "ak_patrol_grunt"}, 300, 600), MissionStep.combat("Defeat the reinforcements", new String[]{"ak_sand_guard", "ak_patrol_captain", "ak_bounty_hunter"}, 200, 400)}, 650, 1100, 85));
      daily.add(new Template("assassination", "Assassination", "A high-value target has been identified. Observe their movements, then eliminate them and their bodyguards.", "Locate, observe, and eliminate the target.", Category.DAILY, 20, 60, 500, new MissionStep[]{MissionStep.travel("Travel to the intelligence dead drop", 0, 0), MissionStep.scout("Observe the target's movements", 300, 200, 400), MissionStep.combat("Eliminate the bodyguards", new String[]{"ak_bounty_hunter", "ak_patrol_captain"}, 100, 200), MissionStep.combat("Execute the target", new String[]{"ak_bounty_elite", "ak_cloud_sentinel"}, 50, 100)}, 800, 1300, 95));
      daily.add(new Template("reconnaissance", "Reconnaissance", "Scout an enemy outpost and eliminate any forces that detect your presence.", "Scout the outpost and defeat the detection squads.", Category.DAILY, 25, 40, 350, new MissionStep[]{MissionStep.travel("Infiltrate the enemy outpost perimeter", 0, 0), MissionStep.scout("Scout the outpost defenses", 300, 200, 400), MissionStep.combat("Eliminate the detection squad", new String[]{"ak_sand_guard", "ak_patrol_grunt", "ak_patrol_grunt"}, 150, 300), MissionStep.combat("Defeat the outpost captain", new String[]{"ak_patrol_captain", "ak_leaf_anbu"}, 100, 200)}, 600, 1000, 75));
      daily.add(new Template("supply_interception", "Supply Interception", "Intercept a military supply shipment. Destroy the advance guard and overwhelm the main escort.", "Defeat the supply shipment's military escort.", Category.DAILY, 16, 50, 400, new MissionStep[]{MissionStep.travel("Set up at the ambush point", 0, 0), MissionStep.combat("Destroy the advance guard", new String[]{"ak_cloud_sentinel", "ak_patrol_grunt", "ak_patrol_grunt"}, 300, 600), MissionStep.combat("Overwhelm the main escort", new String[]{"ak_mist_hunter", "ak_stone_guard", "ak_bounty_hunter"}, 200, 400)}, 650, 1100, 85));
      DAILY_TEMPLATES = Collections.unmodifiableList(daily);
      List<Template> weekly = new ArrayList();
      weekly.add(new Template("high_value_assassination", "High-Value Assassination", "A powerful shinobi has been marked for elimination. Infiltrate their compound, break through the guard detail, and execute the targets.", "Defeat the compound guards and eliminate the high-value targets.", Category.WEEKLY, 70, 220, 1200, new MissionStep[]{MissionStep.travel("Meet the intelligence contact", 0, 0), MissionStep.scout("Observe the compound layout", 400, 400, 800), MissionStep.combat("Eliminate the outer guard", new String[]{"ak_patrol_grunt", "ak_patrol_captain", "ak_bounty_hunter"}, 200, 400), MissionStep.travel("Advance to the inner compound", 200, 400), MissionStep.combat("Defeat the elite bodyguard detail", new String[]{"ak_bounty_elite", "ak_leaf_anbu", "ak_cloud_sentinel"}, 100, 200), MissionStep.combat("Eliminate the high-value targets", new String[]{"ak_anbu_captain", "ak_village_commander"}, 50, 100)}, 3000, 5000, 200));
      weekly.add(new Template("pains_directive", "Pain's Directive", "Carry out a classified directive from Pain. Breach and destroy an enemy fortress.", "Break through the fortress defenses and destroy the commander.", Category.WEEKLY, 60, 180, 1000, new MissionStep[]{MissionStep.travel("Report to the briefing coordinates", 0, 0), MissionStep.combat("Clear the perimeter scouts", new String[]{"ak_patrol_grunt", "ak_patrol_grunt", "ak_rain_agent"}, 500, 900), MissionStep.travel("Advance to the enemy fortress", 400, 700), MissionStep.combat("Breach the fortress defenses", new String[]{"ak_stone_guard", "ak_bounty_hunter", "ak_patrol_captain", "ak_sand_guard"}, 200, 400), MissionStep.combat("Destroy the Fortress Commander", new String[]{"ak_fortress_lord", "ak_anbu_captain"}, 100, 200)}, 2500, 4500, 180));
      weekly.add(new Template("sabotage_mission", "Sabotage Mission", "Sabotage a village's defenses. Eliminate the sentries, fight through the garrison, and destroy the base commanders.", "Eliminate the garrison and defeat the base commanders.", Category.WEEKLY, 65, 200, 1100, new MissionStep[]{MissionStep.travel("Travel to the supply depot", 0, 0), MissionStep.combat("Eliminate the depot sentries", new String[]{"ak_patrol_grunt", "ak_patrol_grunt", "ak_sand_guard"}, 400, 800), MissionStep.travel("Move to the command center", 300, 600), MissionStep.combat("Fight through the garrison", new String[]{"ak_bounty_hunter", "ak_cloud_sentinel", "ak_patrol_captain", "ak_mist_hunter"}, 200, 400), MissionStep.combat("Defeat the base commanders", new String[]{"ak_hunter_captain", "ak_village_commander"}, 100, 200)}, 2800, 4800, 190));
      weekly.add(new Template("deep_cover", "Deep Cover", "Establish deep cover in enemy territory. Survive ambushes, eliminate pursuit squads, and extract at the rendezvous.", "Survive all encounters and extract safely.", Category.WEEKLY, 55, 160, 900, new MissionStep[]{MissionStep.travel("Travel to the first contact point", 0, 0), MissionStep.combat("Survive the ambush", new String[]{"ak_bounty_hunter", "ak_bounty_hunter", "ak_patrol_captain"}, 400, 800), MissionStep.travel("Move to the second contact", 300, 600), MissionStep.combat("Eliminate the pursuit squad", new String[]{"ak_leaf_anbu", "ak_cloud_sentinel", "ak_mist_hunter"}, 200, 400), MissionStep.scout("Wait at the extraction point", 400, 300, 500), MissionStep.combat("Defeat the final interceptors", new String[]{"ak_anbu_captain", "ak_bounty_elite"}, 100, 200)}, 2200, 4000, 170));
      WEEKLY_TEMPLATES = Collections.unmodifiableList(weekly);
   }

   public static enum Category {
      DAILY,
      WEEKLY,
      SPECIAL;
   }

   public static class MissionStep {
      public final StepType type;
      public final String objective;
      public final String[] npcConfigIds;
      public final int scoutDurationTicks;
      public final int posOffsetMin;
      public final int posOffsetMax;

      public MissionStep(StepType type, String objective, String[] npcConfigIds, int scoutDurationTicks, int posOffsetMin, int posOffsetMax) {
         this.type = type;
         this.objective = objective;
         this.npcConfigIds = npcConfigIds;
         this.scoutDurationTicks = scoutDurationTicks;
         this.posOffsetMin = posOffsetMin;
         this.posOffsetMax = posOffsetMax;
      }

      public static MissionStep travel(String objective, int offsetMin, int offsetMax) {
         return new MissionStep(StepType.TRAVEL, objective, (String[])null, 0, offsetMin, offsetMax);
      }

      public static MissionStep combat(String objective, String[] npcConfigIds, int offsetMin, int offsetMax) {
         return new MissionStep(StepType.COMBAT, objective, npcConfigIds, 0, offsetMin, offsetMax);
      }

      public static MissionStep scout(String objective, int durationTicks, int offsetMin, int offsetMax) {
         return new MissionStep(StepType.SCOUT, objective, (String[])null, durationTicks, offsetMin, offsetMax);
      }

      public static enum StepType {
         TRAVEL,
         COMBAT,
         SCOUT;
      }
   }

   public static class Template {
      public final String id;
      public final String name;
      public final String description;
      public final String objectiveText;
      public final Category category;
      public final int tokenReward;
      public final int repReward;
      public final int xpReward;
      public final MissionStep[] steps;
      public final int ryoMin;
      public final int ryoMax;
      public final int pveXp;

      public Template(String id, String name, String description, String objectiveText, Category category, int tokenReward, int repReward, int xpReward, MissionStep[] steps, int ryoMin, int ryoMax, int pveXp) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.objectiveText = objectiveText;
         this.category = category;
         this.tokenReward = tokenReward;
         this.repReward = repReward;
         this.xpReward = xpReward;
         this.steps = steps;
         this.ryoMin = ryoMin;
         this.ryoMax = ryoMax;
         this.pveXp = pveXp;
      }

      public int getRandomRyo(Random rand) {
         return this.ryoMin + rand.nextInt(this.ryoMax - this.ryoMin + 1);
      }
   }
}
