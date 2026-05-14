
package net.luck.narutoaddon.OtherCode.endgame.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoBoard;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoInstance;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoTarget;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoTargetRegistry;
import net.luck.narutoaddon.OtherCode.endgame.gui.EndgameClientData;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndgameSyncMessage implements IMessage {
   public static final byte SYNC_FULL = 0;
   public static final byte SYNC_OUTPOST_COOLDOWNS = 1;
   public static final byte SYNC_BINGO = 2;
   public static final byte SYNC_INCURSION = 3;
   public static final byte SYNC_DEFENSE = 4;
   public static final byte SYNC_LEADERBOARD = 5;
   public static final byte SYNC_COMBAT_BARS = 6;
   public static final byte SYNC_RAID_COOLDOWN = 7;
   public static final byte SYNC_OUTPOST_ACTIVE = 8;
   public static final byte SYNC_PVE_RANK = 9;
   private byte syncType;
   private Map<String, Map<Integer, Long>> outpostCooldowns;
   private List<BingoSlotData> bingoSlots;
   private boolean bingoComplete;
   private long bingoResetTime;
   private int activeHuntSlot;
   private double huntWaypointX;
   private double huntWaypointZ;
   private double huntSearchRadius;
   private String boardTierName;
   private boolean incursionActive;
   private boolean hasJoinedIncursion;
   private String incursionName;
   private String incursionStateName;
   private int incursionCountdown;
   private double incursionX;
   private double incursionZ;
   private int incursionWave;
   private int incursionMaxWaves;
   private int incursionEnemiesRemaining;
   private boolean defenseActive;
   private String defenseVillage;
   private int defenseWave;
   private int defenseMaxWaves;
   private int defenseVillageHP;
   private int defenseMaxHP;
   private List<CombatBarInfo> combatBars;
   private String leaderboardCategory;
   private List<LeaderboardEntryData> leaderboardEntries;
   private long raidCooldownEnd;
   public boolean outpostActive;
   public String outpostLocationName;
   public String outpostEncounterName;
   public String outpostState;
   private int pveXp;
   private String pveRankName;
   private int pveXpToNext;

   public EndgameSyncMessage() {
      this.incursionStateName = "";
      this.incursionCountdown = -1;
      this.outpostLocationName = "";
      this.outpostEncounterName = "";
      this.outpostState = "";
      this.outpostCooldowns = new HashMap();
      this.bingoSlots = new ArrayList();
      this.combatBars = new ArrayList();
      this.leaderboardEntries = new ArrayList();
      this.incursionName = "";
      this.defenseVillage = "";
      this.leaderboardCategory = "";
      this.boardTierName = "";
      this.activeHuntSlot = -1;
      this.pveRankName = "";
   }

   public EndgameSyncMessage(byte syncType) {
      this();
      this.syncType = syncType;
   }

   public EndgameSyncMessage(byte syncType, String leaderboardCategory) {
      this();
      this.syncType = syncType;
      this.leaderboardCategory = leaderboardCategory != null ? leaderboardCategory : "";
   }

   public static EndgameSyncMessage outpostCooldowns(Map<String, Map<Integer, Long>> cooldowns) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 1;
      msg.outpostCooldowns = (Map<String, Map<Integer, Long>>)(cooldowns != null ? cooldowns : new HashMap());
      return msg;
   }

   public static EndgameSyncMessage bingo(List<BingoSlotData> slots, boolean complete, long resetTime, int activeSlot, double wpX, double wpZ, double searchRadius, String boardTierName) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 2;
      msg.bingoSlots = (List<BingoSlotData>)(slots != null ? slots : new ArrayList());
      msg.bingoComplete = complete;
      msg.bingoResetTime = resetTime;
      msg.activeHuntSlot = activeSlot;
      msg.huntWaypointX = wpX;
      msg.huntWaypointZ = wpZ;
      msg.huntSearchRadius = searchRadius;
      msg.boardTierName = boardTierName != null ? boardTierName : "";
      return msg;
   }

   public static EndgameSyncMessage incursion(boolean active, String name, double x, double z, int wave, int maxWaves, int enemiesRemaining, boolean hasJoined, String stateName, int countdown) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 3;
      msg.incursionActive = active;
      msg.hasJoinedIncursion = hasJoined;
      msg.incursionName = name != null ? name : "";
      msg.incursionStateName = stateName != null ? stateName : "";
      msg.incursionCountdown = countdown;
      msg.incursionX = x;
      msg.incursionZ = z;
      msg.incursionWave = wave;
      msg.incursionMaxWaves = maxWaves;
      msg.incursionEnemiesRemaining = enemiesRemaining;
      return msg;
   }

   public static EndgameSyncMessage defense(boolean active, String village, int wave, int maxWaves, int villageHP, int maxHP) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 4;
      msg.defenseActive = active;
      msg.defenseVillage = village != null ? village : "";
      msg.defenseWave = wave;
      msg.defenseMaxWaves = maxWaves;
      msg.defenseVillageHP = villageHP;
      msg.defenseMaxHP = maxHP;
      return msg;
   }

   public static EndgameSyncMessage leaderboard(String category, List<LeaderboardEntryData> entries) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 5;
      msg.leaderboardCategory = category != null ? category : "";
      msg.leaderboardEntries = (List<LeaderboardEntryData>)(entries != null ? entries : new ArrayList());
      return msg;
   }

   public static EndgameSyncMessage combatBars(List<CombatBarInfo> bars) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 6;
      msg.combatBars = (List<CombatBarInfo>)(bars != null ? bars : new ArrayList());
      return msg;
   }

   public static EndgameSyncMessage raidCooldown(long cooldownEnd) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 7;
      msg.raidCooldownEnd = cooldownEnd;
      return msg;
   }

   public static EndgameSyncMessage pveRank(int xp, String rankName, int xpToNext) {
      EndgameSyncMessage msg = new EndgameSyncMessage();
      msg.syncType = 9;
      msg.pveXp = xp;
      msg.pveRankName = rankName != null ? rankName : "";
      msg.pveXpToNext = xpToNext;
      return msg;
   }

   public void setBingoData(BingoBoard board, BingoInstance activeHunt, long nextResetTime) {
      this.bingoSlots = new ArrayList();
      this.activeHuntSlot = -1;
      this.bingoComplete = false;
      this.bingoResetTime = nextResetTime;
      this.huntWaypointX = (double)0.0F;
      this.huntWaypointZ = (double)0.0F;
      this.huntSearchRadius = (double)0.0F;
      this.boardTierName = "";
      if (board != null) {
         this.bingoComplete = board.isAllComplete();
         this.boardTierName = board.getTierName() != null ? board.getTierName() : "";
         List<BingoBoard.BingoSlot> slots = board.getSlots();

         for(int i = 0; i < slots.size(); ++i) {
            BingoBoard.BingoSlot slot = (BingoBoard.BingoSlot)slots.get(i);
            BingoSlotData sd = new BingoSlotData();
            BingoTarget target = BingoTargetRegistry.get(slot.getTargetId());
            sd.targetName = target != null ? target.getDisplayName() : slot.getTargetId();
            sd.loreHint = target != null ? target.getLoreHint() : "";
            sd.regionName = target != null ? target.getTier().getDisplayName() : "";
            sd.ryoReward = slot.getRyoReward();
            sd.completed = slot.isCompleted();
            sd.active = slot.isActive();
            this.bingoSlots.add(sd);
         }
      }

      if (activeHunt != null && board != null) {
         List<BingoBoard.BingoSlot> slots = board.getSlots();

         for(int i = 0; i < slots.size(); ++i) {
            if (((BingoBoard.BingoSlot)slots.get(i)).isActive()) {
               this.activeHuntSlot = i;
               break;
            }
         }

         this.huntWaypointX = (double)activeHunt.getSpawnLocation().getX();
         this.huntWaypointZ = (double)activeHunt.getSpawnLocation().getZ();
         this.huntSearchRadius = activeHunt.getSearchRadius();
      }

   }

   public void setIncursionData(boolean active, String name, double x, double z, int wave, int maxWaves, int enemiesRemaining, boolean hasJoined, String stateName, int countdown) {
      this.incursionActive = active;
      this.hasJoinedIncursion = hasJoined;
      this.incursionName = name != null ? name : "";
      this.incursionStateName = stateName != null ? stateName : "";
      this.incursionCountdown = countdown;
      this.incursionX = x;
      this.incursionZ = z;
      this.incursionWave = wave;
      this.incursionMaxWaves = maxWaves;
      this.incursionEnemiesRemaining = enemiesRemaining;
   }

   public void setDefenseData(boolean active, String village, int wave, int maxWaves, int villageHP, int maxHP) {
      this.defenseActive = active;
      this.defenseVillage = village != null ? village : "";
      this.defenseWave = wave;
      this.defenseMaxWaves = maxWaves;
      this.defenseVillageHP = villageHP;
      this.defenseMaxHP = maxHP;
   }

   public void setPveRankData(int xp, String rankName, int xpToNext) {
      this.pveXp = xp;
      this.pveRankName = rankName != null ? rankName : "";
      this.pveXpToNext = xpToNext;
   }

   public byte getSyncType() {
      return this.syncType;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.syncType);
      if (this.syncType == 0 || this.syncType == 1) {
         this.writeOutpostCooldowns(buf);
      }

      if (this.syncType == 0 || this.syncType == 2) {
         this.writeBingo(buf);
      }

      if (this.syncType == 0 || this.syncType == 3) {
         this.writeIncursion(buf);
      }

      if (this.syncType == 0 || this.syncType == 4) {
         this.writeDefense(buf);
      }

      if (this.syncType == 0 || this.syncType == 6) {
         this.writeCombatBars(buf);
      }

      if (this.syncType == 0 || this.syncType == 5) {
         this.writeLeaderboard(buf);
      }

      if (this.syncType == 0 || this.syncType == 7) {
         this.writeRaidCooldown(buf);
      }

      if (this.syncType == 0 || this.syncType == 9) {
         this.writePveRank(buf);
      }

      if (this.syncType == 0 || this.syncType == 8) {
         this.writeOutpostActive(buf);
      }

   }

   public void fromBytes(ByteBuf buf) {
      this.syncType = buf.readByte();
      if (this.syncType == 0 || this.syncType == 1) {
         this.readOutpostCooldowns(buf);
      }

      if (this.syncType == 0 || this.syncType == 2) {
         this.readBingo(buf);
      }

      if (this.syncType == 0 || this.syncType == 3) {
         this.readIncursion(buf);
      }

      if (this.syncType == 0 || this.syncType == 4) {
         this.readDefense(buf);
      }

      if (this.syncType == 0 || this.syncType == 6) {
         this.readCombatBars(buf);
      }

      if (this.syncType == 0 || this.syncType == 5) {
         this.readLeaderboard(buf);
      }

      if (this.syncType == 0 || this.syncType == 7) {
         this.readRaidCooldown(buf);
      }

      if (this.syncType == 0 || this.syncType == 9) {
         this.readPveRank(buf);
      }

      if (this.syncType == 0 || this.syncType == 8) {
         this.readOutpostActive(buf);
      }

   }

   private void writeOutpostCooldowns(ByteBuf buf) {
      buf.writeShort(this.outpostCooldowns.size());

      for(Map.Entry<String, Map<Integer, Long>> entry : this.outpostCooldowns.entrySet()) {
         ByteBufUtils.writeUTF8String(buf, (String)entry.getKey());
         Map<Integer, Long> tierMap = (Map)entry.getValue();
         buf.writeByte(tierMap.size());

         for(Map.Entry<Integer, Long> tierEntry : tierMap.entrySet()) {
            buf.writeByte((Integer)tierEntry.getKey());
            buf.writeLong((Long)tierEntry.getValue());
         }
      }

   }

   private void readOutpostCooldowns(ByteBuf buf) {
      int count = buf.readShort() & '\uffff';
      this.outpostCooldowns = new HashMap();

      for(int i = 0; i < count; ++i) {
         String outpostId = ByteBufUtils.readUTF8String(buf);
         int tierCount = buf.readByte() & 255;
         Map<Integer, Long> tierMap = new HashMap();

         for(int t = 0; t < tierCount; ++t) {
            int tier = buf.readByte() & 255;
            long cooldownEnd = buf.readLong();
            tierMap.put(tier, cooldownEnd);
         }

         this.outpostCooldowns.put(outpostId, tierMap);
      }

   }

   private void writeBingo(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.boardTierName != null ? this.boardTierName : "");
      buf.writeByte(this.bingoSlots.size());

      for(BingoSlotData slot : this.bingoSlots) {
         ByteBufUtils.writeUTF8String(buf, slot.targetName != null ? slot.targetName : "");
         ByteBufUtils.writeUTF8String(buf, slot.loreHint != null ? slot.loreHint : "");
         ByteBufUtils.writeUTF8String(buf, slot.regionName != null ? slot.regionName : "");
         buf.writeInt(slot.ryoReward);
         buf.writeBoolean(slot.completed);
         buf.writeBoolean(slot.active);
      }

      buf.writeBoolean(this.bingoComplete);
      buf.writeLong(this.bingoResetTime);
      buf.writeByte(this.activeHuntSlot);
      buf.writeDouble(this.huntWaypointX);
      buf.writeDouble(this.huntWaypointZ);
      buf.writeDouble(this.huntSearchRadius);
   }

   private void readBingo(ByteBuf buf) {
      this.boardTierName = ByteBufUtils.readUTF8String(buf);
      int slotCount = buf.readByte() & 255;
      this.bingoSlots = new ArrayList();

      for(int i = 0; i < slotCount; ++i) {
         BingoSlotData slot = new BingoSlotData();
         slot.targetName = ByteBufUtils.readUTF8String(buf);
         slot.loreHint = ByteBufUtils.readUTF8String(buf);
         slot.regionName = ByteBufUtils.readUTF8String(buf);
         slot.ryoReward = buf.readInt();
         slot.completed = buf.readBoolean();
         slot.active = buf.readBoolean();
         this.bingoSlots.add(slot);
      }

      this.bingoComplete = buf.readBoolean();
      this.bingoResetTime = buf.readLong();
      this.activeHuntSlot = buf.readByte();
      this.huntWaypointX = buf.readDouble();
      this.huntWaypointZ = buf.readDouble();
      this.huntSearchRadius = buf.readDouble();
   }

   private void writeIncursion(ByteBuf buf) {
      buf.writeBoolean(this.incursionActive);
      buf.writeBoolean(this.hasJoinedIncursion);
      ByteBufUtils.writeUTF8String(buf, this.incursionName != null ? this.incursionName : "");
      ByteBufUtils.writeUTF8String(buf, this.incursionStateName != null ? this.incursionStateName : "");
      buf.writeInt(this.incursionCountdown);
      buf.writeDouble(this.incursionX);
      buf.writeDouble(this.incursionZ);
      buf.writeInt(this.incursionWave);
      buf.writeInt(this.incursionMaxWaves);
      buf.writeInt(this.incursionEnemiesRemaining);
   }

   private void readIncursion(ByteBuf buf) {
      this.incursionActive = buf.readBoolean();
      this.hasJoinedIncursion = buf.readBoolean();
      this.incursionName = ByteBufUtils.readUTF8String(buf);
      this.incursionStateName = ByteBufUtils.readUTF8String(buf);
      this.incursionCountdown = buf.readInt();
      this.incursionX = buf.readDouble();
      this.incursionZ = buf.readDouble();
      this.incursionWave = buf.readInt();
      this.incursionMaxWaves = buf.readInt();
      this.incursionEnemiesRemaining = buf.readInt();
   }

   private void writeDefense(ByteBuf buf) {
      buf.writeBoolean(this.defenseActive);
      ByteBufUtils.writeUTF8String(buf, this.defenseVillage != null ? this.defenseVillage : "");
      buf.writeInt(this.defenseWave);
      buf.writeInt(this.defenseMaxWaves);
      buf.writeInt(this.defenseVillageHP);
      buf.writeInt(this.defenseMaxHP);
   }

   private void readDefense(ByteBuf buf) {
      this.defenseActive = buf.readBoolean();
      this.defenseVillage = ByteBufUtils.readUTF8String(buf);
      this.defenseWave = buf.readInt();
      this.defenseMaxWaves = buf.readInt();
      this.defenseVillageHP = buf.readInt();
      this.defenseMaxHP = buf.readInt();
   }

   private void writeCombatBars(ByteBuf buf) {
      buf.writeByte(this.combatBars.size());

      for(CombatBarInfo bar : this.combatBars) {
         buf.writeInt(bar.entityId);
         ByteBufUtils.writeUTF8String(buf, bar.entityName != null ? bar.entityName : "");
         buf.writeFloat(bar.currentHP);
         buf.writeFloat(bar.maxHP);
         buf.writeByte(bar.phase);
      }

   }

   private void readCombatBars(ByteBuf buf) {
      int count = buf.readByte() & 255;
      this.combatBars = new ArrayList();

      for(int i = 0; i < count; ++i) {
         CombatBarInfo bar = new CombatBarInfo();
         bar.entityId = buf.readInt();
         bar.entityName = ByteBufUtils.readUTF8String(buf);
         bar.currentHP = buf.readFloat();
         bar.maxHP = buf.readFloat();
         bar.phase = buf.readByte() & 255;
         this.combatBars.add(bar);
      }

   }

   private void writeLeaderboard(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.leaderboardCategory != null ? this.leaderboardCategory : "");
      buf.writeShort(this.leaderboardEntries.size());

      for(LeaderboardEntryData entry : this.leaderboardEntries) {
         ByteBufUtils.writeUTF8String(buf, entry.playerName != null ? entry.playerName : "");
         buf.writeInt(entry.value);
      }

   }

   private void readLeaderboard(ByteBuf buf) {
      this.leaderboardCategory = ByteBufUtils.readUTF8String(buf);
      int count = buf.readShort() & '\uffff';
      this.leaderboardEntries = new ArrayList();

      for(int i = 0; i < count; ++i) {
         LeaderboardEntryData entry = new LeaderboardEntryData();
         entry.playerName = ByteBufUtils.readUTF8String(buf);
         entry.value = buf.readInt();
         this.leaderboardEntries.add(entry);
      }

   }

   private void writeRaidCooldown(ByteBuf buf) {
      buf.writeLong(this.raidCooldownEnd);
   }

   private void readRaidCooldown(ByteBuf buf) {
      this.raidCooldownEnd = buf.readLong();
   }

   private void writePveRank(ByteBuf buf) {
      buf.writeInt(this.pveXp);
      ByteBufUtils.writeUTF8String(buf, this.pveRankName != null ? this.pveRankName : "");
      buf.writeInt(this.pveXpToNext);
   }

   private void readPveRank(ByteBuf buf) {
      this.pveXp = buf.readInt();
      this.pveRankName = ByteBufUtils.readUTF8String(buf);
      this.pveXpToNext = buf.readInt();
   }

   private void writeOutpostActive(ByteBuf buf) {
      buf.writeBoolean(this.outpostActive);
      ByteBufUtils.writeUTF8String(buf, this.outpostLocationName != null ? this.outpostLocationName : "");
      ByteBufUtils.writeUTF8String(buf, this.outpostEncounterName != null ? this.outpostEncounterName : "");
      ByteBufUtils.writeUTF8String(buf, this.outpostState != null ? this.outpostState : "");
   }

   private void readOutpostActive(ByteBuf buf) {
      this.outpostActive = buf.readBoolean();
      this.outpostLocationName = ByteBufUtils.readUTF8String(buf);
      this.outpostEncounterName = ByteBufUtils.readUTF8String(buf);
      this.outpostState = ByteBufUtils.readUTF8String(buf);
   }

   public static class BingoSlotData {
      public String targetName;
      public String loreHint;
      public String regionName;
      public int ryoReward;
      public boolean completed;
      public boolean active;
   }

   public static class CombatBarInfo {
      public int entityId;
      public String entityName;
      public float currentHP;
      public float maxHP;
      public int phase;
   }

   public static class LeaderboardEntryData {
      public String playerName;
      public int value;
   }

   public static class Handler implements IMessageHandler<EndgameSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(EndgameSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            EndgameClientData data = EndgameClientData.getInstance();
            byte type = msg.getSyncType();
            if (type == 0 || type == 1) {
               data.updateOutpostCooldowns(msg.outpostCooldowns);
            }

            if (type == 0 || type == 2) {
               List<EndgameClientData.BingoSlotClient> clientSlots = new ArrayList();

               for(BingoSlotData sd : msg.bingoSlots) {
                  EndgameClientData.BingoSlotClient cs = new EndgameClientData.BingoSlotClient();
                  cs.targetName = sd.targetName;
                  cs.loreHint = sd.loreHint;
                  cs.regionName = sd.regionName;
                  cs.ryoReward = sd.ryoReward;
                  cs.completed = sd.completed;
                  cs.active = sd.active;
                  clientSlots.add(cs);
               }

               data.updateBingo(clientSlots, msg.bingoComplete, msg.bingoResetTime, msg.activeHuntSlot, msg.huntWaypointX, msg.huntWaypointZ, msg.huntSearchRadius, msg.boardTierName);
               if (msg.activeHuntSlot >= 0 && msg.activeHuntSlot < msg.bingoSlots.size()) {
                  BingoSlotData activeSlot = (BingoSlotData)msg.bingoSlots.get(msg.activeHuntSlot);
                  QuestClientData.setBingoHunt(activeSlot.targetName != null ? activeSlot.targetName : "Bingo Target", activeSlot.loreHint != null ? activeSlot.loreHint : "Track down the target");
               } else {
                  QuestClientData.clearBingoHunt();
               }
            }

            if (type == 0 || type == 3) {
               data.updateIncursion(msg.incursionActive, msg.incursionName, msg.incursionX, msg.incursionZ, msg.incursionWave, msg.incursionMaxWaves, msg.incursionEnemiesRemaining, msg.hasJoinedIncursion, msg.incursionStateName, msg.incursionCountdown);
               if (msg.incursionActive) {
                  QuestClientData.setIncursionWaypoint(msg.incursionName, msg.incursionX, msg.incursionZ);
               } else {
                  QuestClientData.clearIncursionWaypoint();
               }
            }

            if (type == 0 || type == 4) {
               data.updateDefense(msg.defenseActive, msg.defenseVillage, msg.defenseWave, msg.defenseMaxWaves, msg.defenseVillageHP, msg.defenseMaxHP);
            }

            if (type == 0 || type == 6) {
               List<EndgameClientData.CombatBarData> clientBars = new ArrayList();
               long tick = Minecraft.getMinecraft().world != null ? Minecraft.getMinecraft().world.getTotalWorldTime() : 0L;

               for(CombatBarInfo bi : msg.combatBars) {
                  EndgameClientData.CombatBarData cb = new EndgameClientData.CombatBarData();
                  cb.entityId = bi.entityId;
                  cb.entityName = bi.entityName;
                  cb.currentHP = bi.currentHP;
                  cb.maxHP = bi.maxHP;
                  cb.phase = bi.phase;
                  cb.lastUpdateTick = tick;
                  clientBars.add(cb);
               }

               data.updateCombatBars(clientBars);
            }

            if (type == 0 || type == 5) {
               List<EndgameClientData.LeaderboardEntry> clientEntries = new ArrayList();

               for(LeaderboardEntryData ed : msg.leaderboardEntries) {
                  clientEntries.add(new EndgameClientData.LeaderboardEntry(ed.playerName, ed.value));
               }

               data.updateLeaderboard(msg.leaderboardCategory, clientEntries);
            }

            if (type == 0 || type == 7) {
               data.updateRaidCooldown(msg.raidCooldownEnd);
            }

            if (type == 0 || type == 9) {
               data.updatePveRank(msg.pveXp, msg.pveRankName, msg.pveXpToNext);
            }

            if (type == 0 || type == 8) {
               if (msg.outpostActive) {
                  QuestClientData.setOutpostMission(msg.outpostLocationName, msg.outpostEncounterName, msg.outpostState);
               } else {
                  QuestClientData.clearOutpostMission();
               }
            }

         });
         return null;
      }
   }
}
