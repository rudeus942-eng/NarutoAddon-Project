package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;

import java.util.*;

public class PvpMissionTemplate {
   private final String id;
   private final String namePattern;
   private final String descriptionPattern;
   private final PvpObjective.ObjectiveType objectiveType;
   private final int minKills;
   private final int maxKills;
   private final Category category;
   private final QuestDefinition.QuestRank minRank;
   private final int baseNinjaXp;
   private final int basePvpXp;
   private final int timeLimitTicks;
   private final boolean leadershipOnly;
   private final boolean personalOnly;
   private final boolean operationTiered;
   private final Map<String, String> loreOverrides;
   private static final List<PvpMissionTemplate> ALL_TEMPLATES = new ArrayList();
   private static final Map<String, PvpMissionTemplate> BY_ID = new HashMap();
   private static final List<PvpMissionTemplate> DAILY_TEMPLATES = new ArrayList();
   private static final List<PvpMissionTemplate> WEEKLY_TEMPLATES = new ArrayList();
   private static final List<PvpMissionTemplate> RANDOM_TEMPLATES = new ArrayList();
   private static final List<PvpMissionTemplate> LEADERSHIP_TEMPLATES = new ArrayList();

   private PvpMissionTemplate(String id, String namePattern, String descriptionPattern, PvpObjective.ObjectiveType objectiveType, int minKills, int maxKills, Category category, QuestDefinition.QuestRank minRank, int baseNinjaXp, int basePvpXp, int timeLimitTicks, boolean leadershipOnly, boolean personalOnly, boolean operationTiered, Map<String, String> loreOverrides) {
      this.id = id;
      this.namePattern = namePattern;
      this.descriptionPattern = descriptionPattern;
      this.objectiveType = objectiveType;
      this.minKills = minKills;
      this.maxKills = maxKills;
      this.category = category;
      this.minRank = minRank;
      this.baseNinjaXp = baseNinjaXp;
      this.basePvpXp = basePvpXp;
      this.timeLimitTicks = timeLimitTicks;
      this.leadershipOnly = leadershipOnly;
      this.personalOnly = personalOnly;
      this.operationTiered = operationTiered;
      this.loreOverrides = loreOverrides;
   }

   public String getId() {
      return this.id;
   }

   public String getNamePattern() {
      return this.namePattern;
   }

   public String getDescriptionPattern() {
      return this.descriptionPattern;
   }

   public PvpObjective.ObjectiveType getObjectiveType() {
      return this.objectiveType;
   }

   public int getMinKills() {
      return this.minKills;
   }

   public int getMaxKills() {
      return this.maxKills;
   }

   public Category getCategory() {
      return this.category;
   }

   public QuestDefinition.QuestRank getMinRank() {
      return this.minRank;
   }

   public int getBaseNinjaXp() {
      return this.baseNinjaXp;
   }

   public int getBasePvpXp() {
      return this.basePvpXp;
   }

   public int getTimeLimitTicks() {
      return this.timeLimitTicks;
   }

   public boolean isLeadershipOnly() {
      return this.leadershipOnly;
   }

   public boolean isPersonalOnly() {
      return this.personalOnly;
   }

   public boolean isOperationTiered() {
      return this.operationTiered;
   }

   public boolean isAssignable() {
      return this.category == Category.LEADERSHIP && !this.personalOnly;
   }

   public String getLore(String ownerVillage, String targetVillage) {
      if (targetVillage != null) {
         String key = ownerVillage + ">" + targetVillage;
         if (this.loreOverrides.containsKey(key)) {
            return (String)this.loreOverrides.get(key);
         }
      }

      return this.loreOverrides.containsKey(ownerVillage) ? (String)this.loreOverrides.get(ownerVillage) : this.descriptionPattern;
   }

   public static PvpMissionTemplate getById(String id) {
      return (PvpMissionTemplate)BY_ID.get(id);
   }

   public static List<PvpMissionTemplate> getAll() {
      return Collections.unmodifiableList(ALL_TEMPLATES);
   }

   public static List<PvpMissionTemplate> getDaily() {
      return Collections.unmodifiableList(DAILY_TEMPLATES);
   }

   public static List<PvpMissionTemplate> getWeekly() {
      return Collections.unmodifiableList(WEEKLY_TEMPLATES);
   }

   public static List<PvpMissionTemplate> getRandom() {
      return Collections.unmodifiableList(RANDOM_TEMPLATES);
   }

   public static List<PvpMissionTemplate> getLeadership() {
      return Collections.unmodifiableList(LEADERSHIP_TEMPLATES);
   }

   public static PvpMissionTemplate getLeadershipById(String id) {
      PvpMissionTemplate t = (PvpMissionTemplate)BY_ID.get(id);
      return t != null && t.category == Category.LEADERSHIP ? t : null;
   }

   public static List<PvpMissionTemplate> getAssignable() {
      List<PvpMissionTemplate> result = new ArrayList();

      for(PvpMissionTemplate t : LEADERSHIP_TEMPLATES) {
         if (t.isAssignable()) {
            result.add(t);
         }
      }

      return result;
   }

   private static void register(PvpMissionTemplate t) {
      ALL_TEMPLATES.add(t);
      BY_ID.put(t.id, t);
      switch (t.category) {
         case DAILY:
            DAILY_TEMPLATES.add(t);
            break;
         case WEEKLY:
            WEEKLY_TEMPLATES.add(t);
            break;
         case RANDOM:
            RANDOM_TEMPLATES.add(t);
            break;
         case LEADERSHIP:
            LEADERSHIP_TEMPLATES.add(t);
      }

   }

   private static Builder builder(String id) {
      return new Builder(id);
   }

   static {
      register(builder("village_hunt").name("Village Hunt: {targetVillage}").desc("Eliminate {kills} {targetVillage} shinobi. Show no mercy to the enemies of {village}.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(5, 8).cat(Category.DAILY).rank(QuestDefinition.QuestRank.D).ninjaXp(60).pvpXp(15).lore("Leaf>Sand", "The Sand's treachery during the Konoha Crush will not be forgotten. Hunt their shinobi.").lore("Leaf>Cloud", "The Cloud's attempt to steal the Byakugan demands retribution. Find their shinobi.").lore("Sand>Leaf", "The Leaf grows complacent behind their walls. Remind them of the desert's fury.").lore("Cloud>Leaf", "The Leaf hides behind their Will of Fire. Extinguish it.").lore("Mist>Leaf", "The Leaf knows nothing of true hardship. Show them the Blood Mist's resolve.").lore("Stone>Leaf", "The Yellow Flash is gone. The Leaf's era of dominance ends now.").lore("Rain>Leaf", "Konoha has trampled Amegakure's sovereignty for generations. Hunt their shinobi and remind them the rain never forgets.").lore("Rain>Sand", "The Sand believes the desert is harsh. They have never endured Amegakure's storms. Hunt them.").lore("Rain>Cloud", "The Cloud hides in their mountains above the clouds. Drag them down into the rain.").build());
      register(builder("nature_strike").name("Nature Strike: {nature} Release").desc("Defeat {kills} enemies using {nature} Release techniques. Prove your elemental mastery.").type(PvpObjective.ObjectiveType.KILL_WITH_NATURE).kills(3, 5).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(80).pvpXp(20).lore("Leaf", "Channel the Will of Fire through your chakra nature. Let your jutsu speak for you.").lore("Mist", "The Silent Killing technique begins with mastery of Water Release. Perfect your art.").lore("Cloud", "Lightning Release is the pride of Kumogakure. Strike down your foes like the Raikage.").lore("Stone", "Earth Release built Iwagakure's fortress. Now turn that power against our enemies.").lore("Sand", "Wind Release cuts through all defenses. Let the desert winds carry your fury.").lore("Rain", "The rain carries chakra naturally. Let your nature release merge with the downpour and overwhelm your targets.").build());
      register(builder("border_patrol").name("Border Patrol").desc("Eliminate {kills} enemy shinobi near the border of {landName}. Defend our territory.").type(PvpObjective.ObjectiveType.KILL_IN_REGION).kills(4, 6).cat(Category.DAILY).rank(QuestDefinition.QuestRank.D).ninjaXp(50).pvpXp(12).lore("Leaf", "The forests surrounding Konoha are our first line of defense. Patrol and eliminate threats.").lore("Sand", "The desert border is vast. Enemy scouts must not reach Sunagakure.").lore("Cloud", "The mountain passes are the gateway to Kumogakure. No enemy passes unchallenged.").lore("Stone", "The rocky canyons of our homeland are natural fortresses. Keep them clear of intruders.").lore("Mist", "The coastal approaches to Kirigakure must be guarded. No outsider sets foot here.").lore("Rain", "The flooded border roads of Amegakure are treacherous. Patrol them and drown any intruder before they reach the village.").build());
      register(builder("underdog_challenge").name("Underdog Challenge").desc("Defeat {kills} shinobi who have more Ninja XP than you. Only kills on stronger players will count.").type(PvpObjective.ObjectiveType.KILL_HIGHER_LEVEL).kills(3).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(100).pvpXp(30).lore("Leaf", "Naruto proved that an underdog can surpass any genius. Follow his example.").lore("Sand", "Gaara rose from being feared to being respected. Strength comes from within.").lore("Cloud", "Killer Bee mastered the Eight-Tails against all odds. Surpass your limits.").lore("Rain", "Amegakure survives by cunning, not raw power. Defeat those who outclass you and prove the rain erodes even the strongest stone.").build());
      register(builder("mutual_hunt").name("Mutual Hunt").desc("You and a {targetVillage} shinobi have been marked for mutual elimination. Hunt or be hunted.").type(PvpObjective.ObjectiveType.MUTUAL_HUNT).kills(1).cat(Category.DAILY).rank(QuestDefinition.QuestRank.B).ninjaXp(120).pvpXp(40).lore("Leaf>Sand", "Intelligence reports a Sand assassin headed your way. This is a kill-or-be-killed situation.").lore("Leaf>Mist", "A Mist hunter-nin has marked you. Turn the tables.").lore("Cloud>Leaf", "A Leaf ANBU has been tracking you. Eliminate them before they strike.").lore("Rain>Leaf", "A Leaf tracker has found your trail through the rain. End them before they report back.").lore("Sand>Rain", "An Amegakure operative has been tailing you across the border. This ends now.").lore("Mist>Rain", "A Rain shinobi has been shadowing you through the coastal fog. Hunt or be hunted.").build());
      register(builder("village_patrol").name("Village Defense Patrol").desc("Defend {village} territory - eliminate {kills} intruders who dare trespass on our land.").type(PvpObjective.ObjectiveType.KILL_IN_REGION).kills(4, 6).cat(Category.DAILY).rank(QuestDefinition.QuestRank.D).ninjaXp(55).pvpXp(12).lore("Leaf", "Intruders have been spotted in the Land of Fire. Protect the village at all costs.").lore("Sand", "Enemy shinobi have crossed into Wind Country. Drive them back to the dunes.").lore("Cloud", "Lightning Country's borders are sacred. Eliminate every intruder.").lore("Stone", "Earth Country's mountains hide many threats. Root them out.").lore("Mist", "Water Country's islands are under threat. Defend every shore.").lore("Rain", "The rain falls endlessly on our territory. Any enemy who enters will find that the water itself turns against them.").build());
      register(builder("dojutsu_hunter").name("Dojutsu Hunter").desc("Eliminate {kills} dojutsu users. Their eyes are powerful, but not invincible.").type(PvpObjective.ObjectiveType.KILL_DOJUTSU_USER).kills(3, 4).cat(Category.DAILY).rank(QuestDefinition.QuestRank.B).ninjaXp(130).pvpXp(35).lore("Leaf", "The Sharingan and Byakugan are Konoha's pride, but in enemy hands they are a threat. Hunt down those who wield stolen eyes.").lore("Cloud", "The Hyuga Affair taught us the value of dojutsu. Claim what you can from the battlefield.").lore("Mist", "Ao proved that even the Byakugan can be taken. Hunt those who rely on their eyes.").lore("Sand", "Dojutsu users think themselves above normal shinobi. Prove them wrong.").lore("Stone", "The Sharingan cost us dearly in the last war. Make them pay.").lore("Rain", "In the rain, eyes blur and dojutsu falter. Hunt the eye-users where the downpour strips away their advantage.").build());
      register(builder("avenger").name("Avenger: Strike at {targetVillage}").desc("Hunt down {kills} {targetVillage} shinobi in retaliation for their aggression against {village}.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(3, 5).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(75).pvpXp(20).lore("Leaf>Stone", "Iwagakure's aggression in the Third War cost us many comrades. Avenge them.").lore("Leaf>Cloud", "The Cloud's kidnapping attempts will not go unanswered.").lore("Sand>Leaf", "The Leaf humiliated us. Strike back and restore Suna's honor.").lore("Mist>Stone", "The Stone's interference in Water Country ends now.").lore("Rain>Leaf", "The Leaf has used Amegakure as a battleground for decades. They owe us blood.").lore("Rain>Sand", "The Sand thought our rain country was weak enough to raid. Repay them tenfold.").lore("Stone>Rain", "The Rain village sits on our southern flank. Their insolence demands retaliation.").lore("Cloud>Sand", "The Sand's puppet masters think themselves clever. Show them that lightning shatters wood and wire.").build());
      register(builder("skirmish_daily").name("Daily Skirmish").desc("Engage in combat and defeat {kills} enemy shinobi. Hone your skills through battle.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(5, 8).cat(Category.DAILY).rank(QuestDefinition.QuestRank.D).ninjaXp(50).pvpXp(10).lore("Leaf", "A shinobi who avoids conflict grows dull. Seek out battle and sharpen your blade.").lore("Sand", "The desert breeds killers, not cowards. Find your enemies and prove Suna's mettle.").lore("Cloud", "Kumogakure's strength is forged in constant training. Treat the battlefield as your dojo.").lore("Mist", "In the Bloody Mist, hesitation meant death. Carry that lesson into every fight.").lore("Stone", "Iwagakure's shinobi are forged in the mountains' pressure. Get out there and fight.").lore("Rain", "The rain never stops, and neither do Amegakure's shinobi. Find battle and prove your endurance.").build());
      register(builder("night_ops").name("Night Operations").desc("Eliminate {kills} enemies under cover of darkness. Strike silently, leave no trace.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(3, 5).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(85).pvpXp(22).lore("Mist", "The Hidden Mist Jutsu is most effective at night. Use the darkness as your ally.").lore("Leaf", "ANBU operates in the shadows. Channel that discipline.").lore("Sand", "The desert is coldest and darkest at night. Use it to your advantage.").lore("Cloud", "Above the clouds, the night is absolute. Strike from that darkness like a thunderbolt.").lore("Stone", "The deep mountain caverns know no light. Carry that darkness with you into the field.").lore("Rain", "The rain falls harder at night, drowning out all sound. In that blindness, Amegakure's shinobi thrive.").build());
      register(builder("ambush_intel").name("Ambush: Enemy Scouts").desc("Ambush and defeat {kills} enemy scouts gathering intelligence near {landName}.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(3, 4).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(75).pvpXp(18).lore("Leaf", "Enemy scouts have been spotted near the village. Intercept them before they report back.").lore("Sand", "Scouts have been seen at the edge of the dunes. Ambush them before they map our defenses.").lore("Stone", "Our surveillance teams detected infiltrators. Neutralize them.").lore("Cloud", "Someone is mapping our mountain passes. Stop them permanently.").lore("Mist", "Foreign operatives are charting our island approaches. Silence them beneath the waves.").lore("Rain", "Spies have been spotted trying to navigate the flooded approaches to Amegakure. Drown their intelligence gathering.").build());
      register(builder("first_blood").name("First Blood").desc("Draw first blood - defeat {kills} enemies to prove your combat readiness.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(3).cat(Category.DAILY).rank(QuestDefinition.QuestRank.D).ninjaXp(40).pvpXp(8).lore("Leaf", "Every shinobi remembers their first real battle. Make yours count.").lore("Sand", "The desert demands strength. Show you have it.").lore("Mist", "In the Mist, your first kill defines your path. Make it clean.").lore("Stone", "A Stone shinobi's first battle is their foundation. Build on it.").lore("Cloud", "The first bolt of lightning announces the storm. Be that first strike.").lore("Rain", "Every Amegakure shinobi was born in the rain. Your first battle proves whether you can fight through it.").build());
      register(builder("retaliation").name("Retaliation: {targetVillage}").desc("Strike back against {targetVillage} - eliminate {kills} of their shinobi. They struck first.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(4, 6).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(80).pvpXp(22).lore("Leaf>Rain", "The Rain village has been harboring enemies. Show them the Leaf's wrath.").lore("Sand>Stone", "Iwagakure has been encroaching on our borders. Push them back.").lore("Cloud>Mist", "The Mist's provocations demand a response. Give them one.").lore("Rain>Stone", "The Stone has been testing our borders. Retaliate with the full force of Amegakure's vengeance.").lore("Rain>Cloud", "The Cloud thinks their altitude keeps them safe from the rain. Prove them wrong.").lore("Mist>Sand", "Sand shinobi have been raiding our trade routes. Drown their ambitions.").lore("Stone>Cloud", "The Cloud's expansion threatens our northern passes. Strike back.").build());
      register(builder("outskirt_sweep").name("Outskirt Sweep").desc("Clear {kills} enemies from the outskirts of {village} territory. Keep the perimeter secure.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(3, 5).cat(Category.DAILY).rank(QuestDefinition.QuestRank.D).ninjaXp(45).pvpXp(10).lore("Leaf", "The outskirts of the Land of Fire are crawling with hostiles. Clear them out.").lore("Sand", "The desert's edge is where enemies test our resolve. Sweep them back into the wastes.").lore("Mist", "The outer islands report enemy activity. Sweep and secure.").lore("Stone", "The foothills below our mountain fortress have been infiltrated. Crush them.").lore("Cloud", "The lower mountain trails are vulnerable. Patrol and eliminate all hostiles.").lore("Rain", "The outskirts of Amegakure's territory are soggy wastelands where enemies think they can hide. Show them the rain reaches everywhere.").build());
      register(builder("chakra_dominance").name("Chakra Dominance").desc("Assert your chakra superiority - defeat {kills} enemies using ninjutsu techniques.").type(PvpObjective.ObjectiveType.KILL_WITH_NATURE).kills(3, 5).cat(Category.DAILY).rank(QuestDefinition.QuestRank.C).ninjaXp(90).pvpXp(25).lore("Leaf", "True shinobi blend all elements into their fighting style. Show your mastery.").lore("Sand", "Wind chakra cuts through all defenses. Dominate with elemental superiority.").lore("Cloud", "The Raikage's Lightning Armor technique inspired generations. Follow suit.").lore("Mist", "Water Release is in the Mist's blood. Overwhelm them with the force of the ocean.").lore("Stone", "Earth Release makes the ground itself your weapon. Show them Iwagakure's foundation.").lore("Rain", "In Amegakure, the rain amplifies water chakra naturally. Let your jutsu flow with the storm's power.").build());
      register(builder("rapid_strike").name("Rapid Strike").desc("Eliminate {kills} enemies within the time limit. Speed and precision are paramount.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(3).cat(Category.DAILY).rank(QuestDefinition.QuestRank.A).ninjaXp(150).pvpXp(45).timer(12000).lore("Leaf", "The Yellow Flash ended battles in seconds. Channel that speed.").lore("Sand", "Sandstorms are sudden and lethal. Strike with that same blinding speed.").lore("Cloud", "Lightning strikes fast and without warning. Be the lightning.").lore("Mist", "The Mist's assassins kill before you see the blade. Move with that lethality.").lore("Stone", "An avalanche gives no warning. Be the mountain's fury unleashed.").lore("Rain", "A cloudburst hits without warning and floods everything. Strike with that sudden, overwhelming force.").build());
      register(builder("multi_village_hunt").name("Cross-Border Offensive").desc("Strike across borders - defeat {kills} shinobi from at least 3 different villages. Wage war on all fronts.").type(PvpObjective.ObjectiveType.KILL_MULTI_VILLAGE).kills(30).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.B).ninjaXp(300).pvpXp(100).lore("Leaf", "The First Hokage dreamed of peace between villages. Until that day comes, we fight on all fronts.").lore("Sand", "The desert wind recognizes no borders. Suna's reach extends to every corner of the shinobi world.").lore("Cloud", "Kumogakure answers to no one. Strike every village that dares challenge us.").lore("Mist", "The ocean touches every shore. Kirigakure's operatives will be felt in every nation.").lore("Stone", "The Tsuchikage's orders are clear: no village is our ally. Fight them all.").lore("Rain", "Caught between the great nations, Amegakure has enemies on every border. Strike them all -- the rain falls on everyone equally.").build());
      register(builder("anbu_operation").name("ANBU Black Ops").desc("ANBU-level operation: Eliminate {kills} enemy shinobi across multiple phases. Failure is not an option.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(30).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.A).ninjaXp(400).pvpXp(150).lore("Leaf", "Danzo's ROOT may be disbanded, but ANBU endures. This mission is classified S-rank.").lore("Mist", "The Hunter-nin division has sanctioned this operation. Eliminate all targets.").lore("Sand", "The Puppet Brigade's intelligence unit has identified priority targets. Eliminate them.").lore("Cloud", "The Raikage's personal guard has sanctioned a black operation. Leave no survivors, no evidence.").lore("Stone", "Iwagakure's Explosion Corps has been deployed for a covert elimination campaign. Total annihilation.").lore("Rain", "Amegakure's intelligence network is unmatched -- we see through the rain. Every target has been identified. Execute them all.").build());
      register(builder("bingo_book").name("Bingo Book Target").desc("A {targetVillage} shinobi has been added to the Bingo Book. Confirm the kill.").type(PvpObjective.ObjectiveType.KILL_SPECIFIC_PLAYER).kills(1).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.A).ninjaXp(350).pvpXp(120).lore("Leaf>Mist", "A Mist rogue has been terrorizing our borders. Their name is in the Bingo Book. End them.").lore("Cloud>Leaf", "Leaf intelligence has been too effective lately. Remove their top operative.").lore("Mist>Sand", "A Sand puppet user has been interfering with our operations. Eliminate them.").lore("Sand>Mist", "A Mist swordsman has been carving a path through our border patrols. The Bingo Book demands their head.").lore("Stone>Cloud", "A Cloud jonin has been disrupting our mining operations. They're in the Bingo Book. Collect.").lore("Rain>Leaf", "A Leaf operative has been mapping Amegakure's defenses. The Bingo Book marks them for death. Find them in the rain.").lore("Rain>Stone", "A Stone shinobi has been sabotaging our supply routes. Their name is written in blood in our Bingo Book.").build());
      register(builder("border_war").name("Border War").desc("The border is contested. Eliminate {kills} enemies in the disputed territory between {village} and {targetVillage}.").type(PvpObjective.ObjectiveType.KILL_IN_REGION).kills(35).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.B).ninjaXp(300).pvpXp(110).lore("Leaf>Stone", "The Valley of the End marks our border with the Stone. Defend it as Hashirama once did.").lore("Sand>Stone", "The desert meets the mountains at our disputed border. Hold the line.").lore("Cloud>Stone", "The northern passes are ours by right. Fight for every inch.").lore("Rain>Leaf", "The border between Rain and Fire Country has been soaked in blood for generations. Hold it at any cost.").lore("Rain>Stone", "The rocky terrain where Rain meets Earth Country is contested ground. Fight for every muddy inch.").lore("Mist>Cloud", "The coastal border where fog meets mountain is our contested territory. Defend the shore.").lore("Stone>Sand", "Where the mountains crumble into desert, our border lies. Defend every stone.").build());
      register(builder("kg_hunter").name("Kekkei Genkai Hunter").desc("Eliminate {kills} kekkei genkai or dojutsu users. Their bloodline limits end here.").type(PvpObjective.ObjectiveType.KILL_DOJUTSU_USER).kills(25).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.B).ninjaXp(300).pvpXp(105).lore("Leaf", "Konoha shelters many bloodline users, but enemy kekkei genkai threaten our own. Eliminate them systematically.").lore("Sand", "Suna relies on technique over bloodline. Prove that hard-won skill surpasses inherited power.").lore("Cloud", "After the Hyuga Affair, we learned the true value of bloodline limits. Collect them.").lore("Mist", "The Bloodline Purges taught us one thing: those with special eyes are dangerous. Eliminate them.").lore("Stone", "Deidara betrayed us with his Explosion Release. No kekkei genkai user can be trusted.").lore("Rain", "Amegakure has no great bloodlines -- only iron will. Prove that raw determination shatters inherited power.").build());
      register(builder("nature_supremacy").name("Elemental Supremacy").desc("Prove absolute mastery of {nature} Release. Defeat {kills} enemies using your chakra nature.").type(PvpObjective.ObjectiveType.KILL_WITH_NATURE).kills(30).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.C).ninjaXp(350).pvpXp(80).lore("Leaf", "The Will of Fire burns through every jutsu. Let your chakra nature blaze with conviction.").lore("Cloud", "The Third Raikage's Hell Stab was pure Lightning Release. Aspire to that mastery.").lore("Mist", "Zabuza needed only water and killing intent. Follow his example.").lore("Sand", "Temari's Wind Release devastated battlefields. Channel that power.").lore("Stone", "The Fence Sitter's Dust Release combined three natures. Begin with mastering one.").lore("Rain", "The rain itself is chakra in Amegakure. Channel the storm through your jutsu and achieve absolute elemental supremacy.").build());
      register(builder("village_defense_weekly").name("Village Defense: Repel {targetVillage}").desc("Repel the {targetVillage} threat - eliminate {kills} of their shinobi who have infiltrated our lands.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(40).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.C).ninjaXp(350).pvpXp(85).lore("Leaf>Sand", "The Sand's invasion during the Chunin Exams nearly destroyed us. Never again.").lore("Sand>Leaf", "Konoha's shadow looms over every nation. Defend our homeland from their influence.").lore("Cloud>Stone", "Iwagakure's aggression against Lightning Country will be met with thunder.").lore("Mist>Cloud", "Cloud scouts have been spotted near our shores. Drown them.").lore("Rain>Leaf", "Leaf shinobi treat our territory as a highway. Repel every last one from our rain-soaked borders.").lore("Rain>Sand", "Sand raiders think the rain will slow us. It only makes Amegakure's defense more lethal.").lore("Stone>Leaf", "The Leaf's spies keep probing our borders. Bury them under the mountain.").build());
      register(builder("ronin_hunt").name("Total War").desc("Hunt down {kills} enemies from any village. No discrimination, no mercy. A shinobi's duty.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(50).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.B).ninjaXp(350).pvpXp(120).lore("Leaf", "The battlefield knows no allegiance. Every enemy is a threat to be eliminated.").lore("Sand", "In the desert, there are no allies -- only prey. Hunt everything that moves.").lore("Cloud", "Kumogakure's warriors fight everyone. We bow to no village.").lore("Mist", "The Blood Mist taught us: trust no one, kill everyone. Total war is the only war.").lore("Stone", "In the Stone, we fight until our fists break the earth. Keep fighting.").lore("Rain", "Amegakure is surrounded by enemies on all sides. Fight them all without discrimination.").build());
      register(builder("stealth_ops").name("Stealth Operations").desc("Eliminate {kills} enemies without dying. A true shinobi completes the mission and returns alive.").type(PvpObjective.ObjectiveType.KILL_STREAK).kills(25).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.A).ninjaXp(400).pvpXp(140).lore("Leaf", "Kakashi's ANBU career was defined by flawless execution. Emulate the Copy Ninja.").lore("Mist", "The Seven Swordsmen never left survivors. Neither should you.").lore("Sand", "Sasori's puppets killed silently and efficiently. Be just as precise.").lore("Cloud", "Move like lightning -- fast enough that they never touch you. That is Kumogakure's stealth.").lore("Stone", "The Camouflage Technique was perfected in Iwagakure. Become the stone itself and strike without being seen.").lore("Rain", "In the endless rain, no one hears the killing blow. Amegakure's assassins are ghosts in the downpour.").build());
      register(builder("territorial").name("Territorial Supremacy").desc("Defend the homeland - eliminate {kills} enemies within {village} territory. This is our land.").type(PvpObjective.ObjectiveType.KILL_IN_REGION).kills(30).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.C).ninjaXp(320).pvpXp(75).lore("Leaf", "The forests of the Land of Fire have sheltered Konoha for generations. Protect them.").lore("Sand", "Every grain of sand in Wind Country answers to Sunagakure. Remind intruders of that.").lore("Cloud", "These mountains are sacred ground. Trespassers will be struck down like lightning.").lore("Mist", "Kirigakure's islands are sovereign territory. Every shore, every inlet -- ours by blood and right.").lore("Stone", "Earth Country's mountains are our fortress. Every trespasser is buried beneath them.").lore("Rain", "The flooded territory of Rain Country belongs to Amegakure alone. Any enemy who enters will drown in our domain.").build());
      register(builder("warlord").name("Warlord").desc("Dominate the battlefield - eliminate {kills} shinobi from any village. Become a legend of war.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(50).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.S).ninjaXp(500).pvpXp(200).lore("Leaf", "Madara Uchiha once stood against entire armies alone. Walk his path, but for the village.").lore("Sand", "The Third Kazekage was the strongest in Suna's history. Become a warlord worthy of that legacy.").lore("Cloud", "The Third Raikage fought ten thousand shinobi alone and survived. Be that legend.").lore("Stone", "Onoki's Dust Release leveled battlefields. Your body count should be just as devastating.").lore("Mist", "The Bloody Mist era produced the deadliest shinobi. Channel that era's ferocity.").lore("Rain", "Hanzo the Salamander stood alone against the Sannin and won. Become a warlord of that caliber -- let the rain carry tales of your devastation.").build());
      register(builder("assassination").name("Assassination Contract").desc("Assassinate {kills} shinobi who have more Ninja XP than you. Only kills on players ranked higher count toward this contract.").type(PvpObjective.ObjectiveType.KILL_HIGHER_LEVEL).kills(25).cat(Category.WEEKLY).rank(QuestDefinition.QuestRank.S).ninjaXp(450).pvpXp(180).lore("Leaf", "The Foundation taught that emotion is weakness. Strike with cold precision.").lore("Sand", "Chiyo's era taught that poison and cunning overcome brute strength. Assassinate with technique, not power.").lore("Mist", "In the Academy of the Bloody Mist, the weak killed the strong to graduate. Repeat history.").lore("Cloud", "Killer Bee overcame opponents stronger than himself. Channel his unpredictable style.").lore("Stone", "Iwagakure's assassins have toppled leaders twice their strength. Precision beats power.").lore("Rain", "Hanzo earned the title 'Demigod' by killing those far stronger than himself. In the rain, even giants fall.").build());
      register(builder("quick_kill").name("Quick Elimination").desc("A quick mission - eliminate {kills} enemy shinobi. Clean and efficient.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(1, 2).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.D).ninjaXp(25).pvpXp(5).lore("Leaf", "A quick kill is a merciful kill. The Will of Fire demands efficiency.").lore("Sand", "In the desert, you strike once and vanish. No wasted movement.").lore("Mist", "One clean cut. That's all a Mist shinobi needs.").lore("Stone", "Quick as a rockslide. One moment of force, then silence.").lore("Cloud", "A single bolt from the sky. Quick, clean, decisive.").lore("Rain", "The rain washes away all traces. Kill quickly and let the water do the rest.").build());
      register(builder("survive_the_hunt").name("Survive the Hunt").desc("You have been marked by enemy villages. Survive for the duration without dying.").type(PvpObjective.ObjectiveType.SURVIVE_DURATION).kills(0).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.B).ninjaXp(150).pvpXp(50).timer(18000).lore("Leaf", "Enemy ANBU are converging on your position. Survive until backup arrives.").lore("Sand", "You are stranded in hostile territory with no water. Survive the desert and its predators.").lore("Mist", "The Hunter-nin are after you. Evade them in the mist.").lore("Stone", "Trapped in the mountain passes with enemies closing in. Hold your ground until relief arrives.").lore("Cloud", "You have been cut off from retreat. Hold out until the storm passes.").lore("Rain", "The rain reveals your every movement to those who know how to read it. Survive until the storm covers your escape.").build());
      register(builder("bingo_book_hit").name("Bingo Book Hit").desc("A {targetVillage} shinobi has been marked in the Bingo Book. Eliminate them within the time limit.").type(PvpObjective.ObjectiveType.KILL_SPECIFIC_PLAYER).kills(1).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.A).ninjaXp(100).pvpXp(60).timer(12000).lore("Leaf", "The Bingo Book has a fresh entry. Time-sensitive - move fast.").lore("Sand", "A wind-carried Bingo Book update has arrived. The target won't linger long in the heat.").lore("Cloud", "The Raikage's office has issued an urgent Bingo Book entry. Don't let the target escape.").lore("Mist", "A hunter-nin contract has surfaced. Track them through the fog before the trail goes cold.").lore("Stone", "An urgent Bingo Book entry from the Tsuchikage's desk. Strike before they disappear into the mountains.").lore("Rain", "A Bingo Book page, waterlogged but legible, marks a target passing through our territory. Catch them before the rain washes away their trail.").build());
      register(builder("duelist").name("Duelist").desc("Win a one-on-one engagement. Prove your skill in single combat.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(1).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.D).ninjaXp(30).pvpXp(8).lore("Leaf", "The Final Valley saw the greatest duel in history. Write your own chapter.").lore("Sand", "Gaara's fight against Lee in the Chunin Exams defined a generation. Fight with that intensity.").lore("Mist", "The Mist trains killers in one-on-one combat from childhood. Show them what graduation means.").lore("Stone", "In the Stone, duels settle disputes between clans. Fight with that weight on your shoulders.").lore("Cloud", "Killer Bee versus Sasuke -- that is the standard. Meet it.").lore("Rain", "Hanzo dueled the three Sannin at once and earned his legend. One opponent should be nothing to you.").build());
      register(builder("opportunist").name("Opportunist").desc("Seize the moment - eliminate 1 enemy shinobi. Any target, any method.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(1).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.D).ninjaXp(20).pvpXp(5).lore("Leaf", "A shinobi must seize every opportunity. Strike when the moment presents itself.").lore("Sand", "The desert scorpion waits for the perfect moment, then strikes without hesitation.").lore("Mist", "In the mist, opportunity appears and vanishes like fog. Seize it.").lore("Stone", "Patience is a Stone shinobi's greatest weapon. Wait for the crack, then shatter them.").lore("Cloud", "Lightning waits for no one. When you see an opening, take it with everything you have.").lore("Rain", "Opportunity drips through Amegakure like the rain -- constant and everywhere. Take the next one you see.").build());
      register(builder("scout_kill").name("Scout Elimination").desc("Eliminate 1 shinobi from {targetVillage}. A single kill can change the tide of a conflict.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(1).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.D).ninjaXp(25).pvpXp(6).lore("Leaf>Sand", "A Sand scout has been spotted in Fire Country. One kill is all it takes.").lore("Cloud>Leaf", "A Leaf shinobi is gathering intel on our defenses. Silence them.").lore("Rain>Leaf", "A Leaf scout has been spotted navigating our flooded roads. One kill ends their reconnaissance.").lore("Sand>Mist", "A Mist operative was spotted at the desert's edge. Eliminate them before they report.").lore("Mist>Stone", "A Stone scout has been charting our island defenses. Silence them beneath the waves.").lore("Stone>Rain", "A Rain spy has infiltrated the mountain passes. One quick kill, and our secrets stay buried.").build());
      register(builder("nature_quick").name("Nature Release: Quick Strike").desc("Defeat 1 enemy using {nature} Release. A single jutsu, a single kill.").type(PvpObjective.ObjectiveType.KILL_WITH_NATURE).kills(1).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.C).ninjaXp(40).pvpXp(12).lore("Leaf", "One fireball. One kill. That is the way of the Uchiha's signature jutsu.").lore("Sand", "One gust of Wind Release. One kill. The desert wind is all you need.").lore("Cloud", "A single lightning bolt from the sky. That's all a Kumo shinobi needs.").lore("Mist", "One Water Dragon Jutsu to drag them under. Clean.").lore("Stone", "One Earth Release technique to bury them. The mountain gives, the mountain takes.").lore("Rain", "One water jutsu, amplified by the rain. A single technique becomes a tidal wave in Amegakure's domain.").build());
      register(builder("rival_strike").name("Rival Strike").desc("Eliminate 1 shinobi from {targetVillage}, your village's primary rival. Fan the flames of rivalry.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(1).cat(Category.RANDOM).rank(QuestDefinition.QuestRank.D).ninjaXp(30).pvpXp(8).lore("Leaf>Cloud", "The Cloud has always coveted our bloodline limits. Remind them why they fail.").lore("Leaf>Stone", "The Stone never forgave us for the Fourth Hokage. Keep the grudge alive.").lore("Sand>Leaf", "The Leaf thinks they are the center of the shinobi world. Prove otherwise.").lore("Cloud>Leaf", "The Leaf's arrogance is their weakness. Exploit it.").lore("Mist>Cloud", "The Cloud's lightning cannot pierce the mist. Show them.").lore("Stone>Sand", "The desert rats think their wind can erode our mountains. Crush them.").lore("Rain>Leaf", "The Leaf's so-called Will of Fire gutters and dies in Amegakure's rain. Strike your rival.").lore("Rain>Sand", "The Sand thinks the desert is the harshest land. Amegakure's storms say otherwise.").lore("Rain>Stone", "The Stone hides behind mountains, but rain erodes even the tallest peak. Strike your rival.").build());
      register(builder("kage_assassination").name("Kage Assassination: {targetVillage}").desc("Intelligence has located the {targetVillage} Kage. Eliminate them to cripple enemy leadership. This is an S-rank operation.").type(PvpObjective.ObjectiveType.KILL_SPECIFIC_PLAYER).kills(1, 1).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.A).ninjaXp(500).pvpXp(300).leadershipOnly().personalOnly().timer(36000).lore("Leaf", "The Third Hokage gave his life to protect the Will of Fire. Now bring that resolve to end their leadership.").lore("Sand", "The Kazekage has always been a target. This time, we are the hunters. Strike before the sandstorm clears.").lore("Mist", "The Mizukage was once manipulated from the shadows. Free them from their position -- permanently. The Mist answers to no one.").lore("Stone", "The Tsuchikage's stubbornness must end. Strike at the heart of Iwagakure's command, as our ancestors struck at mountains.").lore("Cloud", "When Kumogakure strikes at the head, the body falls. Target their Kage. The Raikage's fist shows no hesitation.").lore("Rain", "Amegakure has survived by striking from the shadows. Their Kage will never see the blow coming through the rain.").build());
      register(builder("shadow_decree").name("The Kage's Shadow Decree").desc("As the leader of {village}, personally demonstrate your power. Eliminate {kills} enemies across the battlefield. Your reputation must precede you.").type(PvpObjective.ObjectiveType.KILL_ANY_ENEMY).kills(20, 25).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.S).ninjaXp(550).pvpXp(300).leadershipOnly().personalOnly().lore("Leaf", "The Hokage's Will of Fire must burn brightest when the village is threatened. Step onto the battlefield and remind the world why Konoha endures.").lore("Sand", "The Kazekage is the desert's wrath given form. When you move, the sands swallow armies whole. Let them tremble at your approach.").lore("Mist", "The Mizukage commands the tides of war. When you enter battle, the waters run red. No shore is safe from your reach.").lore("Stone", "The Tsuchikage is the mountain made flesh. When you fight, the very earth shakes. Let them know that Iwagakure's foundation is unbreakable.").lore("Cloud", "The Raikage is the storm incarnate. Your lightning leaves no survivors. Show every nation what happens when the clouds darken over their village.").lore("Rain", "The leader of Amegakure is a god among shinobi. When you descend upon the battlefield, the rain becomes a funeral dirge for your enemies.").build());
      register(builder("iron_fist").name("Iron Fist Protocol").desc("Demonstrate absolute combat supremacy. Eliminate {kills} enemies without being defeated. A true leader does not fall.").type(PvpObjective.ObjectiveType.KILL_STREAK).kills(10, 15).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.A).ninjaXp(450).pvpXp(250).leadershipOnly().personalOnly().lore("Leaf", "Hashirama fought Madara and the Nine-Tails without faltering. The Hokage does not kneel. Let your unbroken streak speak louder than any speech.").lore("Sand", "The Third Kazekage's Iron Sand crushed all who opposed him. Channel that unstoppable force -- fall, and the desert swallows Suna's honor with you.").lore("Mist", "Yagura ruled through absolute power -- none could touch him. The Mizukage's authority is carved in blood. Do not give them the satisfaction of seeing you fall.").lore("Stone", "Onoki's Dust Release erased enemies from existence without a scratch in return. The Tsuchikage is carved from stone. Stone does not break.").lore("Cloud", "The Third Raikage fought ten thousand shinobi to a standstill. His body bore no mortal wound. Carry that legacy -- become untouchable.").lore("Rain", "Pain's Six Paths laid waste to entire nations without a single defeat. As Amegakure's leader, your invincibility is your village's shield.").build());
      register(builder("covert_incursion").name("Covert Incursion").desc("Infiltrate enemy territory and eliminate {kills} shinobi on their home ground. Prove that no village is safe from your reach.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(8, 12).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.A).ninjaXp(400).pvpXp(220).leadershipOnly().personalOnly().timer(72000).lore("Leaf", "The Fourth Hokage's Flying Thunder God carried him behind enemy lines in an instant. You may lack his speed, but not his audacity. Strike deep into {targetVillage}.").lore("Sand", "Sasori infiltrated nations for decades as a spy. The Kazekage's reach extends far beyond the desert. Bring death to {targetVillage}'s doorstep.").lore("Mist", "The Mist's Silent Killing technique was born for operations like this. Slip into {targetVillage} territory like fog, and leave nothing but corpses behind.").lore("Stone", "Iwagakure's Infiltration Corps has toppled governments from within. Walk into {targetVillage}'s lands and show them their walls mean nothing.").lore("Cloud", "The Cloud's history of covert operations -- from the Hyuga kidnapping to the Gold and Silver Brothers -- shows our willingness to strike anywhere. {targetVillage} is next.").lore("Rain", "Amegakure sits between the great nations. You have watched them all, learned their weaknesses. Now exploit them. {targetVillage} cannot hide from the rain.").build());
      register(builder("wrath_of_the_kage").name("Wrath of the Kage").desc("Unleash your full elemental power. Defeat {kills} enemies using ninjutsu. Let them witness the Kage-level jutsu that defines {village}.").type(PvpObjective.ObjectiveType.KILL_WITH_NATURE).kills(10, 15).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.B).ninjaXp(350).pvpXp(180).leadershipOnly().personalOnly().lore("Leaf", "The Hokage's jutsu arsenal defines an era. The First had Wood Release, the Fourth had the Rasengan. Your jutsu will be spoken of in the same breath.").lore("Sand", "The Kazekage's mastery over Wind and Sand is Sunagakure's greatest weapon. When the desert storm descends, nothing survives. Unleash that fury.").lore("Mist", "The Mizukage's water jutsu can drown entire armies. The seas obey your command -- let your enemies drown in the tides of your power.").lore("Stone", "Earth Release shaped the very mountains of Stone Country. The Tsuchikage's jutsu reshapes the battlefield itself. Show them Iwagakure's geological might.").lore("Cloud", "The Raikage's Lightning Release Chakra Mode makes them untouchable. Your jutsu strikes faster than thought. Let them see the lightning before the thunder kills them.").lore("Rain", "Amegakure's leaders have always wielded jutsu of terrifying power. Your techniques fall like rain -- endless, inescapable, and fatal.").build());
      register(builder("village_purge").name("Village Purge: {targetVillage}").desc("All shinobi of {village}: by order of leadership, focus your attacks on {targetVillage}. Eliminate {kills} of their forces. This is a direct order.").type(PvpObjective.ObjectiveType.KILL_VILLAGE).kills(15, 20).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.B).ninjaXp(400).pvpXp(200).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} shinobi from {targetVillage}. The Hokage has deemed {targetVillage} a threat to the Land of Fire. By the Will of Fire, every Leaf shinobi must answer this call.").lore("Sand", "OBJECTIVE: Kill {kills} shinobi from {targetVillage}. The Kazekage commands it: {targetVillage} has crossed Sunagakure. Every Sand shinobi will carry out this decree.").lore("Mist", "OBJECTIVE: Kill {kills} shinobi from {targetVillage}. The Mizukage's orders are absolute. {targetVillage} has provoked the wrath of the Blood Mist -- eliminate them without mercy.").lore("Stone", "OBJECTIVE: Kill {kills} shinobi from {targetVillage}. By decree of the Tsuchikage: {targetVillage} threatens Earth Country's sovereignty. Engage with extreme prejudice.").lore("Cloud", "OBJECTIVE: Kill {kills} shinobi from {targetVillage}. The Raikage's thunderous command: destroy {targetVillage}'s fighting force. Every Cloud shinobi carries this order.").lore("Rain", "OBJECTIVE: Kill {kills} shinobi from {targetVillage}. Amegakure's leader has spoken. The rain will wash away {targetVillage}'s presence. Carry out this cleansing.").build());
      register(builder("leadership_defense").name("Defend the Homeland").desc("By leadership decree: all shinobi of {village} are to eliminate {kills} enemies within our territory. The homeland must be protected at all costs.").type(PvpObjective.ObjectiveType.KILL_IN_REGION).kills(10, 15).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.C).ninjaXp(300).pvpXp(150).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} enemies inside Leaf village territory. The forests of the Land of Fire are Konoha's cradle. The Hokage commands: drive out the invaders.").lore("Sand", "OBJECTIVE: Kill {kills} enemies inside Sand village territory. The desert is Sunagakure's fortress. By the Kazekage's order, no enemy walks our sands and lives.").lore("Mist", "OBJECTIVE: Kill {kills} enemies inside Mist village territory. Kirigakure's islands are sovereign. The Mizukage declares: any outsider found within our mists dies on sight.").lore("Stone", "OBJECTIVE: Kill {kills} enemies inside Stone village territory. Iwagakure is carved into the mountains. The Tsuchikage orders: defend our stone walls.").lore("Cloud", "OBJECTIVE: Kill {kills} enemies inside Cloud village territory. The mountain stronghold is impregnable. The Raikage orders: fight on home soil.").lore("Rain", "OBJECTIVE: Kill {kills} enemies inside Rain village territory. The endless rain is our shield and sword. By our leader's command, trespassers will be destroyed.").build());
      register(builder("elite_hunt").name("Elite Elimination").desc("Eliminate {kills} enemy shinobi who have more Ninja XP than you. Only kills on stronger players count. All capable shinobi are ordered to engage.").type(PvpObjective.ObjectiveType.KILL_HIGHER_LEVEL).kills(5, 8).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.A).ninjaXp(450).pvpXp(250).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} enemies who have MORE Ninja XP than you. The Hokage's intelligence division has identified elite threats. Rise to face their strongest.").lore("Sand", "OBJECTIVE: Kill {kills} enemies who have MORE Ninja XP than you. The Kazekage commands: prove that Suna breeds shinobi who can overcome any foe.").lore("Mist", "OBJECTIVE: Kill {kills} enemies who have MORE Ninja XP than you. The Mizukage orders: cut down their strongest and watch the rest crumble.").lore("Stone", "OBJECTIVE: Kill {kills} enemies who have MORE Ninja XP than you. The Tsuchikage orders: engage their elites head-on.").lore("Cloud", "OBJECTIVE: Kill {kills} enemies who have MORE Ninja XP than you. The Raikage orders: face their strongest. Kumogakure's lightning humbles giants.").lore("Rain", "OBJECTIVE: Kill {kills} enemies who have MORE Ninja XP than you. Our leader commands: target their elites. Amegakure drowns giants.").build());
      register(builder("multi_front_assault").name("Multi-Front Assault").desc("By order of {village} leadership: demonstrate our village's dominance. All shinobi are to engage enemies from at least 3 different villages. Eliminate {kills} total.").type(PvpObjective.ObjectiveType.KILL_MULTI_VILLAGE).kills(20, 20).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.S).ninjaXp(600).pvpXp(350).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} enemies from at least 3 DIFFERENT enemy villages. Show every nation that the Leaf's will extends to all corners of the world.").lore("Sand", "OBJECTIVE: Kill {kills} enemies from at least 3 DIFFERENT enemy villages. The Kazekage commands: prove Sunagakure can wage war on all fronts simultaneously.").lore("Mist", "OBJECTIVE: Kill {kills} enemies from at least 3 DIFFERENT enemy villages. The Mizukage orders a show of force on every front. Let them remember the Mist.").lore("Stone", "OBJECTIVE: Kill {kills} enemies from at least 3 DIFFERENT enemy villages. The Tsuchikage commands: fight on every border. The mountains weather all storms.").lore("Cloud", "OBJECTIVE: Kill {kills} enemies from at least 3 DIFFERENT enemy villages. The Raikage orders: hit them all, hit them hard. No village is beyond our reach.").lore("Rain", "OBJECTIVE: Kill {kills} enemies from at least 3 DIFFERENT enemy villages. Our leader commands total war on every front. The rain falls everywhere.").build());
      register(builder("dojutsu_purge").name("Dojutsu Purge").desc("By order of {village} leadership: {kills} dojutsu users are to be eliminated. Their bloodline advantages threaten our tactical superiority.").type(PvpObjective.ObjectiveType.KILL_DOJUTSU_USER).kills(5, 8).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.B).ninjaXp(350).pvpXp(200).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} enemies wearing Sharingan, Byakugan, or Rinnegan helmets. The Hokage orders: neutralize enemy dojutsu users before they turn the battle against Konoha.").lore("Sand", "OBJECTIVE: Kill {kills} enemies wearing Sharingan, Byakugan, or Rinnegan helmets. The Kazekage commands: strip the enemy of their eyes' advantage.").lore("Mist", "OBJECTIVE: Kill {kills} enemies wearing Sharingan, Byakugan, or Rinnegan helmets. The Mizukage orders: enemy dojutsu users will find no haven from the Mist.").lore("Stone", "OBJECTIVE: Kill {kills} enemies wearing Sharingan, Byakugan, or Rinnegan helmets. The Tsuchikage orders: prove them wrong. Our fists break what eyes cannot predict.").lore("Cloud", "OBJECTIVE: Kill {kills} enemies wearing Sharingan, Byakugan, or Rinnegan helmets. The Raikage commands: engage and eliminate all dojutsu users on sight.").lore("Rain", "OBJECTIVE: Kill {kills} enemies wearing Sharingan, Byakugan, or Rinnegan helmets. Our leader commands: hunt them where the rain blinds their precious eyes.").build());
      register(builder("coordinated_strike").name("Coordinated Elemental Strike").desc("Eliminate {kills} enemies using a specific nature release jutsu assigned to this operation.").type(PvpObjective.ObjectiveType.KILL_WITH_NATURE).kills(8, 12).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.C).ninjaXp(300).pvpXp(160).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} enemies using Fire Release (Katon) jutsu. The Will of Fire burns in every jutsu a Leaf shinobi casts. The Hokage orders a coordinated ninjutsu offensive.").lore("Sand", "OBJECTIVE: Kill {kills} enemies using Wind Release (Futon) jutsu. Wind Release is the birthright of every Sand shinobi. The Kazekage commands: unleash the desert's fury.").lore("Mist", "OBJECTIVE: Kill {kills} enemies using Water Release (Suiton) jutsu. Water Release defines the Mist. The Mizukage orders all shinobi to drown the enemy in jutsu.").lore("Stone", "OBJECTIVE: Kill {kills} enemies using Earth Release (Doton) jutsu. Earth Release built Iwagakure from raw stone. The Tsuchikage commands: bury them under our jutsu.").lore("Cloud", "OBJECTIVE: Kill {kills} enemies using Lightning Release (Raiton) jutsu. Lightning Release is Kumogakure's pride and power. The Raikage orders: electrify the battlefield.").lore("Rain", "OBJECTIVE: Kill {kills} enemies using Water Release (Suiton) jutsu. In Amegakure, water jutsu falls from the sky itself. Our leader commands: turn every raindrop into a weapon.").build());
      register(builder("supply_line_raid").name("Supply Line Raid").desc("Leadership intelligence reports enemy movement near our borders. All available shinobi: intercept and eliminate {kills} enemies in the contested zone.").type(PvpObjective.ObjectiveType.KILL_IN_REGION).kills(8, 12).cat(Category.LEADERSHIP).rank(QuestDefinition.QuestRank.B).ninjaXp(320).pvpXp(170).leadershipOnly().operationTiered().lore("Leaf", "OBJECTIVE: Kill {kills} enemies near your village borders (edge of Leaf territory). Enemy forces spotted near Konoha. The Hokage orders: intercept and cut off their supply lines.").lore("Sand", "OBJECTIVE: Kill {kills} enemies near your village borders (edge of Sand territory). Caravans detected crossing the desert's edge. The Kazekage commands: ambush them.").lore("Mist", "OBJECTIVE: Kill {kills} enemies near your village borders (edge of Mist territory). Foreign ships spotted near our waters. The Mizukage orders: sink them before they reach shore.").lore("Stone", "OBJECTIVE: Kill {kills} enemies near your village borders (edge of Stone territory). The Tsuchikage orders: collapse those routes on any enemy in the mountain passes.").lore("Cloud", "OBJECTIVE: Kill {kills} enemies near your village borders (edge of Cloud territory). The Raikage commands: use the mountain chokepoints. Let lightning and altitude do our work.").lore("Rain", "OBJECTIVE: Kill {kills} enemies near your village borders (edge of Rain territory). Our leader commands: set ambushes along every approach to Amegakure.").build());
   }

   public static enum Category {
      DAILY,
      WEEKLY,
      RANDOM,
      LEADERSHIP;
   }

   private static class Builder {
      private final String id;
      private String namePattern = "";
      private String descriptionPattern = "";
      private PvpObjective.ObjectiveType objectiveType;
      private int minKills;
      private int maxKills;
      private Category category;
      private QuestDefinition.QuestRank minRank;
      private int baseNinjaXp;
      private int basePvpXp;
      private int timeLimitTicks;
      private boolean leadershipOnly;
      private boolean personalOnly;
      private boolean operationTiered;
      private final Map<String, String> loreOverrides;

      Builder(String id) {
         this.objectiveType = PvpObjective.ObjectiveType.KILL_ANY_ENEMY;
         this.minKills = 1;
         this.maxKills = 1;
         this.category = Category.DAILY;
         this.minRank = QuestDefinition.QuestRank.D;
         this.baseNinjaXp = 50;
         this.basePvpXp = 10;
         this.timeLimitTicks = 0;
         this.leadershipOnly = false;
         this.personalOnly = false;
         this.operationTiered = false;
         this.loreOverrides = new HashMap();
         this.id = id;
      }

      Builder name(String p) {
         this.namePattern = p;
         return this;
      }

      Builder desc(String p) {
         this.descriptionPattern = p;
         return this;
      }

      Builder type(PvpObjective.ObjectiveType t) {
         this.objectiveType = t;
         return this;
      }

      Builder kills(int min, int max) {
         this.minKills = min;
         this.maxKills = max;
         return this;
      }

      Builder kills(int count) {
         this.minKills = count;
         this.maxKills = count;
         return this;
      }

      Builder cat(Category c) {
         this.category = c;
         return this;
      }

      Builder rank(QuestDefinition.QuestRank r) {
         this.minRank = r;
         return this;
      }

      Builder ninjaXp(int xp) {
         this.baseNinjaXp = xp;
         return this;
      }

      Builder pvpXp(int xp) {
         this.basePvpXp = xp;
         return this;
      }

      Builder timer(int ticks) {
         this.timeLimitTicks = ticks;
         return this;
      }

      Builder leadershipOnly() {
         this.leadershipOnly = true;
         return this;
      }

      Builder personalOnly() {
         this.personalOnly = true;
         return this;
      }

      Builder operationTiered() {
         this.operationTiered = true;
         return this;
      }

      Builder lore(String key, String text) {
         this.loreOverrides.put(key, text);
         return this;
      }

      PvpMissionTemplate build() {
         return new PvpMissionTemplate(this.id, this.namePattern, this.descriptionPattern, this.objectiveType, this.minKills, this.maxKills, this.category, this.minRank, this.baseNinjaXp, this.basePvpXp, this.timeLimitTicks, this.leadershipOnly, this.personalOnly, this.operationTiered, Collections.unmodifiableMap(new HashMap(this.loreOverrides)));
      }
   }
}
