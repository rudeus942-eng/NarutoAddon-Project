package net.luck.narutoaddon.OtherCode.territory.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class TerritorySyncMessage implements IMessage {
   private boolean fullSync;
   private boolean playerToggleState;
   private List<ZoneData> zones;
   private List<VillageScore> weeklyScores;
   private int playerKills;
   private int playerCaptures;
   private int playerPoints;
   private int playerRank;
   private String playerVillage;
   private String deltaZoneId;
   private String deltaNewOwner;
   private String deltaCaptureAttacker;
   private float deltaCaptureProgress;
   private boolean deltaContested;
   private int deltaWallLevel;
   private int deltaGarrisonLevel;
   private int deltaTrainingLevel;
   private int deltaWatchtowerLevel;
   private int deltaSpecialistLevel;

   public TerritorySyncMessage() {
      this.playerVillage = "";
      this.deltaCaptureAttacker = "";
      this.zones = new ArrayList();
      this.weeklyScores = new ArrayList();
      this.deltaZoneId = "";
      this.deltaNewOwner = "";
   }

   public TerritorySyncMessage(List<ZoneData> zones, boolean playerToggleState, List<VillageScore> weeklyScores) {
      this();
   }

   public TerritorySyncMessage(List<ZoneData> zones, boolean playerToggleState, List<VillageScore> weeklyScores, int playerKills, int playerCaptures, int playerPoints, int playerRank) {
      this();
   }

   public TerritorySyncMessage(List<ZoneData> zones, boolean playerToggleState, List<VillageScore> weeklyScores, int playerKills, int playerCaptures, int playerPoints, int playerRank, String playerVillage) {
      this();
   }

   public TerritorySyncMessage(String zoneId, String newOwner, float captureProgress, boolean playerToggleState) {
      this();
   }

   public TerritorySyncMessage(String zoneId, String newOwner, float captureProgress, boolean contested, boolean playerToggleState) {
      this();
   }

   public TerritorySyncMessage(String zoneId, String newOwner, String captureAttacker, float captureProgress, boolean contested, boolean playerToggleState, int wallLevel, int garrisonLevel, int trainingLevel, int watchtowerLevel, int specialistLevel) {
      this();
   }

   public void toBytes(ByteBuf buf) {
   }

   public void fromBytes(ByteBuf buf) {
      this.zones = new ArrayList();
      this.weeklyScores = new ArrayList();
      this.deltaZoneId = "";
      this.deltaNewOwner = "";
   }

   public boolean isFullSync() {
      return this.fullSync;
   }

   public boolean isPlayerToggleState() {
      return this.playerToggleState;
   }

   public List<ZoneData> getZones() {
      return this.zones;
   }

   public List<VillageScore> getWeeklyScores() {
      return this.weeklyScores;
   }

   public String getDeltaZoneId() {
      return this.deltaZoneId;
   }

   public String getDeltaNewOwner() {
      return this.deltaNewOwner;
   }

   public String getDeltaCaptureAttacker() {
      return this.deltaCaptureAttacker != null ? this.deltaCaptureAttacker : "";
   }

   public float getDeltaCaptureProgress() {
      return this.deltaCaptureProgress;
   }

   public boolean isDeltaContested() {
      return this.deltaContested;
   }

   public int getDeltaWallLevel() {
      return this.deltaWallLevel;
   }

   public int getDeltaGarrisonLevel() {
      return this.deltaGarrisonLevel;
   }

   public int getDeltaTrainingLevel() {
      return this.deltaTrainingLevel;
   }

   public int getDeltaWatchtowerLevel() {
      return this.deltaWatchtowerLevel;
   }

   public int getDeltaSpecialistLevel() {
      return this.deltaSpecialistLevel;
   }

   public int getPlayerKills() {
      return this.playerKills;
   }

   public int getPlayerCaptures() {
      return this.playerCaptures;
   }

   public int getPlayerPoints() {
      return this.playerPoints;
   }

   public int getPlayerRank() {
      return this.playerRank;
   }

   public String getPlayerVillage() {
      return this.playerVillage != null ? this.playerVillage : "";
   }

   public static class ZoneData {
      public String zoneId;
      public String displayName;
      public int centerX;
      public int centerY;
      public int centerZ;
      public int radius;
      public String ownerVillage;
      public String captureAttackerVillage = "";
      public float captureProgress;
      public int strategicValue;
      public boolean contested;
      public int wallLevel;
      public int garrisonLevel;
      public int trainingLevel;
      public int watchtowerLevel;
      public int specialistLevel;
   }

   public static class VillageScore {
      public String village;
      public int score;
   }

   public static class Handler implements IMessageHandler<TerritorySyncMessage, IMessage> {
      public IMessage onMessage(TerritorySyncMessage msg, MessageContext ctx) {
         return null;
      }
   }
}
