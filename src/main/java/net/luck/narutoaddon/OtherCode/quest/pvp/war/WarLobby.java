
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;

public class WarLobby {
   public static final int MAX_ROSTER_SIZE = 10;
   public static final long CHALLENGE_EXPIRY_MS = 900000L;
   private VillageHelper.Village village1;
   private VillageHelper.Village village2;
   private UUID kage1;
   private UUID kage2;
   private final List<UUID> roster1;
   private final List<UUID> roster2;
   private final Set<UUID> accepted1;
   private final Set<UUID> accepted2;
   private boolean locked1;
   private boolean locked2;
   private long challengeTimeMs;
   private LobbyState state;

   public WarLobby(VillageHelper.Village village1, VillageHelper.Village village2, UUID kage1, UUID kage2) {
      this.village1 = village1;
      this.village2 = village2;
      this.kage1 = kage1;
      this.kage2 = kage2;
      this.roster1 = new ArrayList();
      this.roster2 = new ArrayList();
      this.accepted1 = new HashSet();
      this.accepted2 = new HashSet();
      this.locked1 = false;
      this.locked2 = false;
      this.challengeTimeMs = System.currentTimeMillis();
      this.state = LobbyState.SELECTING;
   }

   private WarLobby(VillageHelper.Village village1, VillageHelper.Village village2, UUID kage1, UUID kage2, List<UUID> roster1, List<UUID> roster2, Set<UUID> accepted1, Set<UUID> accepted2, boolean locked1, boolean locked2, long challengeTimeMs, LobbyState state) {
      this.village1 = village1;
      this.village2 = village2;
      this.kage1 = kage1;
      this.kage2 = kage2;
      this.roster1 = roster1;
      this.roster2 = roster2;
      this.accepted1 = accepted1;
      this.accepted2 = accepted2;
      this.locked1 = locked1;
      this.locked2 = locked2;
      this.challengeTimeMs = challengeTimeMs;
      this.state = state;
   }

   public VillageHelper.Village getVillage1() {
      return this.village1;
   }

   public VillageHelper.Village getVillage2() {
      return this.village2;
   }

   public UUID getKage1() {
      return this.kage1;
   }

   public UUID getKage2() {
      return this.kage2;
   }

   public List<UUID> getRoster1() {
      return this.roster1;
   }

   public List<UUID> getRoster2() {
      return this.roster2;
   }

   public Set<UUID> getAccepted1() {
      return this.accepted1;
   }

   public Set<UUID> getAccepted2() {
      return this.accepted2;
   }

   public boolean isLocked1() {
      return this.locked1;
   }

   public boolean isLocked2() {
      return this.locked2;
   }

   public long getChallengeTimeMs() {
      return this.challengeTimeMs;
   }

   public LobbyState getState() {
      return this.state;
   }

   public void setState(LobbyState state) {
      this.state = state;
   }

   public boolean selectPlayer(VillageHelper.Village village, UUID playerUUID) {
      if (this.state != LobbyState.SELECTING && this.state != LobbyState.LOCKED_WAITING) {
         return false;
      } else if (village == this.village1) {
         if (!this.locked1 && this.roster1.size() < 10) {
            if (this.roster1.contains(playerUUID)) {
               return false;
            } else {
               this.roster1.add(playerUUID);
               return true;
            }
         } else {
            return false;
         }
      } else if (village == this.village2) {
         if (!this.locked2 && this.roster2.size() < 10) {
            if (this.roster2.contains(playerUUID)) {
               return false;
            } else {
               this.roster2.add(playerUUID);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean removePlayer(VillageHelper.Village village, UUID playerUUID) {
      if (this.state != LobbyState.SELECTING && this.state != LobbyState.LOCKED_WAITING) {
         return false;
      } else if (village == this.village1) {
         if (this.locked1) {
            return false;
         } else {
            this.accepted1.remove(playerUUID);
            return this.roster1.remove(playerUUID);
         }
      } else if (village == this.village2) {
         if (this.locked2) {
            return false;
         } else {
            this.accepted2.remove(playerUUID);
            return this.roster2.remove(playerUUID);
         }
      } else {
         return false;
      }
   }

   public boolean playerAccept(UUID playerUUID) {
      if (this.roster1.contains(playerUUID)) {
         this.accepted1.add(playerUUID);
         return true;
      } else if (this.roster2.contains(playerUUID)) {
         this.accepted2.add(playerUUID);
         return true;
      } else {
         return false;
      }
   }

   public boolean lockRoster(VillageHelper.Village village, UUID kageUUID) {
      if (this.state != LobbyState.SELECTING && this.state != LobbyState.LOCKED_WAITING) {
         return false;
      } else {
         if (village == this.village1 && kageUUID.equals(this.kage1)) {
            if (this.roster1.isEmpty()) {
               return false;
            }

            this.locked1 = true;
         } else {
            if (village != this.village2 || !kageUUID.equals(this.kage2)) {
               return false;
            }

            if (this.roster2.isEmpty()) {
               return false;
            }

            this.locked2 = true;
         }

         if (this.locked1 && this.locked2) {
            this.state = this.isReady() ? LobbyState.READY : LobbyState.LOCKED_WAITING;
         } else {
            this.state = LobbyState.LOCKED_WAITING;
         }

         return true;
      }
   }

   public boolean isReady() {
      if (this.locked1 && this.locked2) {
         if (!this.roster1.isEmpty() && !this.roster2.isEmpty()) {
            return this.accepted1.containsAll(this.roster1) && this.accepted2.containsAll(this.roster2);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isExpired() {
      return System.currentTimeMillis() - this.challengeTimeMs > 900000L;
   }

   public int getPlayerSide(UUID playerUUID) {
      if (this.roster1.contains(playerUUID)) {
         return 1;
      } else {
         return this.roster2.contains(playerUUID) ? 2 : 0;
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("village1", this.village1.ordinal());
      nbt.setInteger("village2", this.village2.ordinal());
      nbt.setString("kage1", this.kage1.toString());
      nbt.setString("kage2", this.kage2.toString());
      nbt.setBoolean("locked1", this.locked1);
      nbt.setBoolean("locked2", this.locked2);
      nbt.setLong("challengeTimeMs", this.challengeTimeMs);
      nbt.setInteger("state", this.state.ordinal());
      nbt.setTag("roster1", writeUUIDList(this.roster1));
      nbt.setTag("roster2", writeUUIDList(this.roster2));
      nbt.setTag("accepted1", writeUUIDSet(this.accepted1));
      nbt.setTag("accepted2", writeUUIDSet(this.accepted2));
      return nbt;
   }

   public static WarLobby readFromNBT(NBTTagCompound nbt) {
      VillageHelper.Village[] villages = VillageHelper.Village.values();
      int v1Ord = nbt.getInteger("village1");
      int v2Ord = nbt.getInteger("village2");
      VillageHelper.Village v1 = v1Ord >= 0 && v1Ord < villages.length ? villages[v1Ord] : VillageHelper.Village.UNKNOWN;
      VillageHelper.Village v2 = v2Ord >= 0 && v2Ord < villages.length ? villages[v2Ord] : VillageHelper.Village.UNKNOWN;
      UUID kage1 = parseUUID(nbt.getString("kage1"));
      UUID kage2 = parseUUID(nbt.getString("kage2"));
      List<UUID> roster1 = readUUIDList(nbt.getTagList("roster1", 8));
      List<UUID> roster2 = readUUIDList(nbt.getTagList("roster2", 8));
      Set<UUID> accepted1 = readUUIDSet(nbt.getTagList("accepted1", 8));
      Set<UUID> accepted2 = readUUIDSet(nbt.getTagList("accepted2", 8));
      boolean locked1 = nbt.getBoolean("locked1");
      boolean locked2 = nbt.getBoolean("locked2");
      long challengeTimeMs = nbt.getLong("challengeTimeMs");
      int stateOrd = nbt.getInteger("state");
      LobbyState[] states = LobbyState.values();
      LobbyState state = stateOrd >= 0 && stateOrd < states.length ? states[stateOrd] : LobbyState.EXPIRED;
      return new WarLobby(v1, v2, kage1, kage2, roster1, roster2, accepted1, accepted2, locked1, locked2, challengeTimeMs, state);
   }

   private static NBTTagList writeUUIDList(List<UUID> list) {
      NBTTagList tagList = new NBTTagList();

      for(UUID uuid : list) {
         tagList.appendTag(new NBTTagString(uuid.toString()));
      }

      return tagList;
   }

   private static NBTTagList writeUUIDSet(Set<UUID> set) {
      NBTTagList tagList = new NBTTagList();

      for(UUID uuid : set) {
         tagList.appendTag(new NBTTagString(uuid.toString()));
      }

      return tagList;
   }

   private static List<UUID> readUUIDList(NBTTagList tagList) {
      List<UUID> list = new ArrayList();

      for(int i = 0; i < tagList.tagCount(); ++i) {
         UUID uuid = parseUUID(tagList.getStringTagAt(i));
         if (uuid != null) {
            list.add(uuid);
         }
      }

      return list;
   }

   private static Set<UUID> readUUIDSet(NBTTagList tagList) {
      Set<UUID> set = new HashSet();

      for(int i = 0; i < tagList.tagCount(); ++i) {
         UUID uuid = parseUUID(tagList.getStringTagAt(i));
         if (uuid != null) {
            set.add(uuid);
         }
      }

      return set;
   }

   private static UUID parseUUID(String str) {
      if (str != null && !str.isEmpty()) {
         try {
            return UUID.fromString(str);
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static enum LobbyState {
      CHALLENGE_SENT,
      SELECTING,
      LOCKED_WAITING,
      READY,
      EXPIRED;
   }
}
