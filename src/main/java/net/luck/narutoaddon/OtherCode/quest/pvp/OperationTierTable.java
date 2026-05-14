package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;

import java.util.HashMap;
import java.util.Map;

public class OperationTierTable {
   private static final Map<String, OperationTierTable> REGISTRY = new HashMap();
   private final TierEntry[] tiers;

   private OperationTierTable(TierEntry[] tiers) {
      if (tiers.length != 6) {
         throw new IllegalArgumentException("OperationTierTable requires exactly 6 tier entries");
      } else {
         this.tiers = tiers;
      }
   }

   public static OperationTierTable get(String operationId) {
      return (OperationTierTable)REGISTRY.get(operationId);
   }

   public TierEntry getTier(QuestDefinition.QuestRank rank) {
      int ordinal = rank.ordinal();
      return ordinal >= 0 && ordinal < this.tiers.length ? this.tiers[ordinal] : this.tiers[0];
   }

   public int getKillCount(QuestDefinition.QuestRank rank) {
      return this.getTier(rank).killCount;
   }

   public String getRoleTitle(QuestDefinition.QuestRank rank) {
      return this.getTier(rank).roleTitle;
   }

   public String getRoleDescription(QuestDefinition.QuestRank rank) {
      return this.getTier(rank).roleDescription;
   }

   static {
      REGISTRY.put("village_purge", new OperationTierTable(new TierEntry[]{new TierEntry(3, "Genin Scout", "Scout enemy movements and engage targets of opportunity"), new TierEntry(5, "Chunin Operative", "Lead your squad into hostile territory"), new TierEntry(8, "Tokubetsu Jonin", "Execute tactical strikes on priority positions"), new TierEntry(11, "Jonin Squad Leader", "Coordinate multi-squad offensive operations"), new TierEntry(15, "ANBU Operative", "Eliminate high-priority targets behind enemy lines"), new TierEntry(21, "ANBU Captain", "Command the full assault as field commander")}));
      REGISTRY.put("leadership_defense", new OperationTierTable(new TierEntry[]{new TierEntry(2, "Gate Guard", "Hold your post at the village perimeter"), new TierEntry(4, "Barrier Corps", "Patrol and reinforce the defense line"), new TierEntry(6, "Chunin Interceptor", "Respond to perimeter breaches with force"), new TierEntry(9, "Jonin Sentinel", "Command a defense sector of the homeland"), new TierEntry(12, "ANBU Patrol Leader", "Hunt and eliminate all infiltrators"), new TierEntry(17, "Guardian Shinobi", "Serve as the village's unbreakable shield")}));
      REGISTRY.put("elite_hunt", new OperationTierTable(new TierEntry[]{new TierEntry(1, "Genin Tracker", "Shadow and locate one high-value target"), new TierEntry(2, "Chunin Hunter", "Engage veteran enemy shinobi"), new TierEntry(4, "Hunter-nin", "Track and eliminate priority targets"), new TierEntry(6, "Jonin Assassin", "Execute surgical strikes on enemy leadership"), new TierEntry(9, "ANBU Black Ops", "Erase their command structure from the shadows"), new TierEntry(13, "Bingo Book Enforcer", "Execute every marked target without exception")}));
      REGISTRY.put("multi_front_assault", new OperationTierTable(new TierEntry[]{new TierEntry(3, "Genin Messenger", "Carry out strikes across multiple borders"), new TierEntry(5, "Chunin Vanguard", "Lead probing attacks on multiple fronts"), new TierEntry(8, "Surprise Attack Specialist", "Strike where the enemy least expects"), new TierEntry(12, "Jonin Tactician", "Coordinate attacks across multiple theaters"), new TierEntry(17, "Division Commander", "Lead an entire front of the war effort"), new TierEntry(24, "Supreme War Commander", "Bring every hostile nation to submission")}));
      REGISTRY.put("dojutsu_purge", new OperationTierTable(new TierEntry[]{new TierEntry(1, "Genin Spotter", "Identify and mark one dojutsu wielder"), new TierEntry(2, "Sensor-type Chunin", "Detect and engage bloodline users"), new TierEntry(4, "Sealing Corps Specialist", "Neutralize their ocular prowess"), new TierEntry(6, "Jonin Eye Hunter", "Systematically eliminate all dojutsu wielders"), new TierEntry(9, "ANBU Purge Operative", "Eradicate kekkei genkai from the field"), new TierEntry(13, "Bloodline Extinguisher", "End their dojutsu dominance permanently")}));
      REGISTRY.put("coordinated_strike", new OperationTierTable(new TierEntry[]{new TierEntry(2, "Genin Ninjutsu User", "Channel your element against the enemy"), new TierEntry(4, "Chunin Ninjutsu Specialist", "Lead coordinated elemental volleys"), new TierEntry(6, "Ninjutsu Combat Specialist", "Overwhelm targets with elemental force"), new TierEntry(9, "Jonin Nature Master", "Turn the entire battlefield into your domain"), new TierEntry(13, "Ninjutsu Division Captain", "Command the village's elemental offensive"), new TierEntry(18, "Ninjutsu War Commander", "Unleash devastation beyond comprehension")}));
      REGISTRY.put("supply_line_raid", new OperationTierTable(new TierEntry[]{new TierEntry(2, "Genin Saboteur", "Disrupt enemy supply movements near the border"), new TierEntry(4, "Chunin Raider", "Ambush enemy logistics convoys"), new TierEntry(7, "Infiltration Specialist", "Devastate enemy supply infrastructure"), new TierEntry(10, "Jonin Blockade Captain", "Cut off enemy reinforcement routes"), new TierEntry(14, "ANBU Operations Leader", "Choke their entire war effort"), new TierEntry(20, "War Operations Commander", "Annihilate all enemy operations in the sector")}));
   }

   public static class TierEntry {
      public final int killCount;
      public final String roleTitle;
      public final String roleDescription;

      public TierEntry(int killCount, String roleTitle, String roleDescription) {
         this.killCount = killCount;
         this.roleTitle = roleTitle;
         this.roleDescription = roleDescription;
      }
   }
}
