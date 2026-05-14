
package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TournamentInstance {
   private final String tournamentId;
   private TournamentState state;
   private final String villageName;
   private final UUID hostId;
   private final String hostName;
   private final long creationTime;
   private long signupEndTime;
   private int minRankOrdinal;
   private int maxRankOrdinal;
   private int matchTimeoutSeconds;
   private final List<UUID> participants;
   private final List<String> participantNames;
   @Nullable
   private TournamentBracket bracket;
   private ItemStack[] rewards1st;
   private ItemStack[] rewards2nd;
   private ItemStack[] rewards3rd;
   private int currentRound;
   private boolean villageOnly = false;

   public TournamentInstance(String tournamentId, TournamentState state, String villageName, UUID hostId, String hostName, long creationTime, long signupEndTime, int minRankOrdinal, int maxRankOrdinal, int matchTimeoutSeconds) {
      this.tournamentId = tournamentId;
      this.state = state;
      this.villageName = villageName;
      this.hostId = hostId;
      this.hostName = hostName;
      this.creationTime = creationTime;
      this.signupEndTime = signupEndTime;
      this.minRankOrdinal = minRankOrdinal;
      this.maxRankOrdinal = maxRankOrdinal;
      this.matchTimeoutSeconds = matchTimeoutSeconds;
      this.participants = new ArrayList();
      this.participantNames = new ArrayList();
      this.bracket = null;
      this.rewards1st = createEmptyRewardArray();
      this.rewards2nd = createEmptyRewardArray();
      this.rewards3rd = createEmptyRewardArray();
      this.currentRound = 0;
   }

   private static ItemStack[] createEmptyRewardArray() {
      ItemStack[] arr = new ItemStack[9];

      for(int i = 0; i < 9; ++i) {
         arr[i] = ItemStack.EMPTY;
      }

      return arr;
   }

   public boolean isPlayerEligible(QuestDefinition.QuestRank playerRank) {
      int rankOrd = playerRank.ordinal();
      if (this.minRankOrdinal >= 0 && rankOrd < this.minRankOrdinal) {
         return false;
      } else {
         return this.maxRankOrdinal < 0 || rankOrd <= this.maxRankOrdinal;
      }
   }

   public boolean isSignupOpen() {
      return this.state == TournamentState.SIGNUP && System.currentTimeMillis() < this.signupEndTime;
   }

   public void addParticipant(UUID id, String name) {
      if (!this.participants.contains(id)) {
         this.participants.add(id);
         this.participantNames.add(name);
      }

   }

   public void removeParticipant(UUID id) {
      int idx = this.participants.indexOf(id);
      if (idx >= 0) {
         this.participants.remove(idx);
         this.participantNames.remove(idx);
      }

   }

   public boolean hasParticipant(UUID id) {
      return this.participants.contains(id);
   }

   public void generateBracket() {
      this.bracket = TournamentBracket.generate(this.participants, this.participantNames);
   }

   @Nullable
   public TournamentMatch getActiveMatch() {
      if (this.bracket == null) {
         return null;
      } else {
         for(TournamentMatch match : this.bracket.getMatches()) {
            if (match.getMatchState() == TournamentMatch.MatchState.ACTIVE || match.getMatchState() == TournamentMatch.MatchState.COUNTDOWN) {
               return match;
            }
         }

         return null;
      }
   }

   @Nullable
   public TournamentMatch getMatchForPlayer(UUID playerId) {
      if (this.bracket == null) {
         return null;
      } else {
         for(TournamentMatch match : this.bracket.getMatches()) {
            TournamentMatch.MatchState ms = match.getMatchState();
            if ((ms == TournamentMatch.MatchState.PENDING || ms == TournamentMatch.MatchState.COUNTDOWN || ms == TournamentMatch.MatchState.ACTIVE) && match.involvesPlayer(playerId)) {
               return match;
            }
         }

         return null;
      }
   }

   public String getTournamentId() {
      return this.tournamentId;
   }

   public TournamentState getState() {
      return this.state;
   }

   public void setState(TournamentState state) {
      this.state = state;
   }

   public String getVillageName() {
      return this.villageName;
   }

   public UUID getHostId() {
      return this.hostId;
   }

   public String getHostName() {
      return this.hostName;
   }

   public long getCreationTime() {
      return this.creationTime;
   }

   public long getSignupEndTime() {
      return this.signupEndTime;
   }

   public void setSignupEndTime(long signupEndTime) {
      this.signupEndTime = signupEndTime;
   }

   public int getMinRankOrdinal() {
      return this.minRankOrdinal;
   }

   public void setMinRankOrdinal(int minRankOrdinal) {
      this.minRankOrdinal = minRankOrdinal;
   }

   public int getMaxRankOrdinal() {
      return this.maxRankOrdinal;
   }

   public void setMaxRankOrdinal(int maxRankOrdinal) {
      this.maxRankOrdinal = maxRankOrdinal;
   }

   public int getMatchTimeoutSeconds() {
      return this.matchTimeoutSeconds;
   }

   public void setMatchTimeoutSeconds(int matchTimeoutSeconds) {
      this.matchTimeoutSeconds = matchTimeoutSeconds;
   }

   public List<UUID> getParticipants() {
      return this.participants;
   }

   public List<String> getParticipantNames() {
      return this.participantNames;
   }

   @Nullable
   public TournamentBracket getBracket() {
      return this.bracket;
   }

   public void setBracket(@Nullable TournamentBracket bracket) {
      this.bracket = bracket;
   }

   public ItemStack[] getRewards1st() {
      return this.rewards1st;
   }

   public ItemStack[] getRewards2nd() {
      return this.rewards2nd;
   }

   public ItemStack[] getRewards3rd() {
      return this.rewards3rd;
   }

   public int getCurrentRound() {
      return this.currentRound;
   }

   public void setCurrentRound(int currentRound) {
      this.currentRound = currentRound;
   }

   public int getParticipantCount() {
      return this.participants.size();
   }

   public boolean isVillageOnly() {
      return this.villageOnly;
   }

   public void setVillageOnly(boolean villageOnly) {
      this.villageOnly = villageOnly;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("tournamentId", this.tournamentId);
      nbt.setByte("state", (byte)this.state.ordinal());
      nbt.setString("villageName", this.villageName);
      nbt.setString("hostId", this.hostId.toString());
      nbt.setString("hostName", this.hostName);
      nbt.setLong("creationTime", this.creationTime);
      nbt.setLong("signupEndTime", this.signupEndTime);
      nbt.setInteger("minRankOrdinal", this.minRankOrdinal);
      nbt.setInteger("maxRankOrdinal", this.maxRankOrdinal);
      nbt.setInteger("matchTimeoutSeconds", this.matchTimeoutSeconds);
      nbt.setInteger("currentRound", this.currentRound);
      nbt.setBoolean("villageOnly", this.villageOnly);
      NBTTagList idList = new NBTTagList();

      for(UUID id : this.participants) {
         idList.appendTag(new NBTTagString(id.toString()));
      }

      nbt.setTag("participants", idList);
      NBTTagList nameList = new NBTTagList();

      for(String name : this.participantNames) {
         nameList.appendTag(new NBTTagString(name));
      }

      nbt.setTag("participantNames", nameList);
      if (this.bracket != null) {
         nbt.setTag("bracket", this.bracket.writeToNBT());
      }

      nbt.setTag("rewards1st", writeRewardArray(this.rewards1st));
      nbt.setTag("rewards2nd", writeRewardArray(this.rewards2nd));
      nbt.setTag("rewards3rd", writeRewardArray(this.rewards3rd));
      return nbt;
   }

   public static TournamentInstance readFromNBT(NBTTagCompound nbt) {
      String tournamentId = nbt.getString("tournamentId");
      TournamentState state = TournamentState.fromOrdinal(nbt.getByte("state"));
      String villageName = nbt.getString("villageName");
      UUID hostId = UUID.fromString(nbt.getString("hostId"));
      String hostName = nbt.getString("hostName");
      long creationTime = nbt.getLong("creationTime");
      long signupEndTime = nbt.getLong("signupEndTime");
      int minRankOrdinal = nbt.getInteger("minRankOrdinal");
      int maxRankOrdinal = nbt.getInteger("maxRankOrdinal");
      int matchTimeoutSeconds = nbt.getInteger("matchTimeoutSeconds");
      TournamentInstance instance = new TournamentInstance(tournamentId, state, villageName, hostId, hostName, creationTime, signupEndTime, minRankOrdinal, maxRankOrdinal, matchTimeoutSeconds);
      instance.currentRound = nbt.getInteger("currentRound");
      instance.villageOnly = nbt.getBoolean("villageOnly");
      NBTTagList idList = nbt.getTagList("participants", 8);

      for(int i = 0; i < idList.tagCount(); ++i) {
         instance.participants.add(UUID.fromString(idList.getStringTagAt(i)));
      }

      NBTTagList nameList = nbt.getTagList("participantNames", 8);

      for(int i = 0; i < nameList.tagCount(); ++i) {
         instance.participantNames.add(nameList.getStringTagAt(i));
      }

      if (nbt.hasKey("bracket")) {
         instance.bracket = TournamentBracket.readFromNBT(nbt.getCompoundTag("bracket"));
      }

      instance.rewards1st = readRewardArray(nbt.getTagList("rewards1st", 10));
      instance.rewards2nd = readRewardArray(nbt.getTagList("rewards2nd", 10));
      instance.rewards3rd = readRewardArray(nbt.getTagList("rewards3rd", 10));
      return instance;
   }

   private static NBTTagList writeRewardArray(ItemStack[] rewards) {
      NBTTagList list = new NBTTagList();

      for(ItemStack stack : rewards) {
         if (!stack.isEmpty()) {
            NBTTagCompound stackTag = stack.writeToNBT(new NBTTagCompound());
            list.appendTag(stackTag);
         } else {
            NBTTagCompound emptyTag = new NBTTagCompound();
            emptyTag.setBoolean("empty", true);
            list.appendTag(emptyTag);
         }
      }

      return list;
   }

   private static ItemStack[] readRewardArray(NBTTagList list) {
      ItemStack[] rewards = createEmptyRewardArray();

      for(int i = 0; i < list.tagCount() && i < 9; ++i) {
         NBTTagCompound tag = list.getCompoundTagAt(i);
         if (!tag.hasKey("empty")) {
            rewards[i] = new ItemStack(tag);
         }
      }

      return rewards;
   }
}
