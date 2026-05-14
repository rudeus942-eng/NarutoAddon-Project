package net.luck.narutoaddon.OtherCode.territory.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TerritoryManager {
   private static TerritoryManager INSTANCE;
   private TerritorySavedData savedData;

   private TerritoryManager() {
   }

   public static TerritoryManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new TerritoryManager();
      }

      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = null;
   }

   public void init(MinecraftServer server) {
   }

   public void togglePlayer(EntityPlayerMP player) {
   }

   public boolean isPlayerEnabled(UUID playerId) {
      return false;
   }

   public void onPlayerLogout(EntityPlayerMP player) {
   }

   public void removePlayerFromZone(UUID playerId) {
   }

   public void tickZones(MinecraftServer server) {
   }

   public void onPvpKill(EntityPlayerMP killer, EntityPlayerMP victim, TerritoryZone zone) {
   }

   public void onPveKill(EntityPlayerMP player, TerritoryZone zone) {
   }

   public String getPlayerVillage(EntityPlayerMP player) {
      return "";
   }

   public void checkWeeklyReset() {
   }

   public void weeklyReset() {
   }

   public void claimPendingWeeklyReward(EntityPlayerMP player) {
   }

   public void cachePlayerVillage(EntityPlayerMP player) {
   }

   public List<Map.Entry<String, Integer>> getLeaderboard() {
      return Collections.emptyList();
   }

   public void syncToPlayer(EntityPlayerMP player) {
   }

   public void syncDelta(TerritoryZone zone) {
   }

   public TerritoryZone findZoneForPlayer(EntityPlayerMP player) {
      return null;
   }

   public TerritorySavedData getSavedData() {
      return this.savedData;
   }
}
