package net.luck.narutoaddon.OtherCode.akatsuki.mission;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiClientData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class AkatsukiMissionSyncMessage implements IMessage {
   private List<MissionEntry> missions = new ArrayList();
   private List<MissionEntry> offerEntries = new ArrayList();

   public AkatsukiMissionSyncMessage() {
   }

   public AkatsukiMissionSyncMessage(List<AkatsukiMission> activeMissions, List<AkatsukiMission> offers) {
      for(AkatsukiMission m : activeMissions) {
         String description = m.getDescription();
         String objectiveText = "";
         AkatsukiMissionTemplate.Template tmpl = AkatsukiMissionTemplate.getById(m.getTemplateId());
         if (tmpl != null) {
            if (description == null || description.isEmpty()) {
               description = tmpl.description;
            }

            objectiveText = tmpl.objectiveText;
         }

         int curStep = m.getCurrentStepIndex();
         int totSteps = m.getTotalSteps();
         String stepObj = "";
         int stepType = 0;
         int scoutProg = 0;
         int scoutReq = 0;
         AkatsukiMission.StepInstance step = m.getCurrentStep();
         if (step != null) {
            stepObj = step.getObjective();
            stepType = step.getType().ordinal();
            scoutReq = step.getScoutDurationTicks();
            scoutProg = m.getScoutProgress();
         }

         this.missions.add(new MissionEntry(m.getTemplateId(), m.getTemplateName(), description, objectiveText, m.getTargetPos().getX(), m.getTargetPos().getY(), m.getTargetPos().getZ(), m.getStartTime(), m.isCompleted(), m.getCategory().ordinal(), tmpl != null ? tmpl.tokenReward : 0, tmpl != null ? tmpl.repReward : 0, tmpl != null ? tmpl.xpReward : 0, m.getKillsAchieved(), m.getKillsRequired(), m.getMissionState().ordinal(), m.getRyoReward(), m.getPveXpReward(), curStep, totSteps, stepObj, stepType, scoutProg, scoutReq));
      }

      for(AkatsukiMission m : offers) {
         String description = m.getDescription();
         String objectiveText = "";
         AkatsukiMissionTemplate.Template tmpl = AkatsukiMissionTemplate.getById(m.getTemplateId());
         if (tmpl != null) {
            if (description == null || description.isEmpty()) {
               description = tmpl.description;
            }

            objectiveText = tmpl.objectiveText;
         }

         this.offerEntries.add(new MissionEntry(m.getTemplateId(), m.getTemplateName(), description, objectiveText, m.getTargetPos().getX(), m.getTargetPos().getY(), m.getTargetPos().getZ(), m.getStartTime(), m.isCompleted(), m.getCategory().ordinal(), tmpl != null ? tmpl.tokenReward : 0, tmpl != null ? tmpl.repReward : 0, tmpl != null ? tmpl.xpReward : 0, 0, 0, 0, m.getRyoReward(), m.getPveXpReward(), 0, 1, "", 0, 0, 0));
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.missions.size());

      for(MissionEntry entry : this.missions) {
         writeEntry(buf, entry);
      }

      buf.writeInt(this.offerEntries.size());

      for(MissionEntry entry : this.offerEntries) {
         writeEntry(buf, entry);
      }

   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readInt();
      this.missions = new ArrayList(count);

      for(int i = 0; i < count; ++i) {
         this.missions.add(readEntry(buf));
      }

      int offerCount = buf.readInt();
      this.offerEntries = new ArrayList(offerCount);

      for(int i = 0; i < offerCount; ++i) {
         this.offerEntries.add(readEntry(buf));
      }

   }

   private static void writeEntry(ByteBuf buf, MissionEntry entry) {
      ByteBufUtils.writeUTF8String(buf, entry.templateId);
      ByteBufUtils.writeUTF8String(buf, entry.templateName);
      ByteBufUtils.writeUTF8String(buf, entry.description);
      ByteBufUtils.writeUTF8String(buf, entry.objectiveText);
      buf.writeInt(entry.targetX);
      buf.writeInt(entry.targetY);
      buf.writeInt(entry.targetZ);
      buf.writeLong(entry.startTime);
      buf.writeBoolean(entry.completed);
      buf.writeInt(entry.categoryOrdinal);
      buf.writeInt(entry.tokenReward);
      buf.writeInt(entry.repReward);
      buf.writeInt(entry.xpReward);
      buf.writeInt(entry.killsAchieved);
      buf.writeInt(entry.killsRequired);
      buf.writeInt(entry.missionStateOrdinal);
      buf.writeInt(entry.ryoReward);
      buf.writeInt(entry.pveXpReward);
      buf.writeInt(entry.currentStepIndex);
      buf.writeInt(entry.totalSteps);
      ByteBufUtils.writeUTF8String(buf, entry.currentStepObjective);
      buf.writeInt(entry.currentStepType);
      buf.writeInt(entry.scoutProgress);
      buf.writeInt(entry.scoutRequired);
   }

   private static MissionEntry readEntry(ByteBuf buf) {
      String templateId = ByteBufUtils.readUTF8String(buf);
      String templateName = ByteBufUtils.readUTF8String(buf);
      String description = ByteBufUtils.readUTF8String(buf);
      String objectiveText = ByteBufUtils.readUTF8String(buf);
      int x = buf.readInt();
      int y = buf.readInt();
      int z = buf.readInt();
      long startTime = buf.readLong();
      boolean completed = buf.readBoolean();
      int categoryOrdinal = buf.readInt();
      int tokenReward = buf.readInt();
      int repReward = buf.readInt();
      int xpReward = buf.readInt();
      int killsAchieved = buf.readInt();
      int killsRequired = buf.readInt();
      int missionStateOrdinal = buf.readInt();
      int ryoReward = buf.readInt();
      int pveXpReward = buf.readInt();
      int currentStepIndex = buf.readInt();
      int totalSteps = buf.readInt();
      String currentStepObjective = ByteBufUtils.readUTF8String(buf);
      int currentStepType = buf.readInt();
      int scoutProgress = buf.readInt();
      int scoutRequired = buf.readInt();
      return new MissionEntry(templateId, templateName, description, objectiveText, x, y, z, startTime, completed, categoryOrdinal, tokenReward, repReward, xpReward, killsAchieved, killsRequired, missionStateOrdinal, ryoReward, pveXpReward, currentStepIndex, totalSteps, currentStepObjective, currentStepType, scoutProgress, scoutRequired);
   }

   private static class MissionEntry {
      final String templateId;
      final String templateName;
      final String description;
      final String objectiveText;
      final int targetX;
      final int targetY;
      final int targetZ;
      final long startTime;
      final boolean completed;
      final int categoryOrdinal;
      final int tokenReward;
      final int repReward;
      final int xpReward;
      final int killsAchieved;
      final int killsRequired;
      final int missionStateOrdinal;
      final int ryoReward;
      final int pveXpReward;
      final int currentStepIndex;
      final int totalSteps;
      final String currentStepObjective;
      final int currentStepType;
      final int scoutProgress;
      final int scoutRequired;

      MissionEntry(String templateId, String templateName, String description, String objectiveText, int targetX, int targetY, int targetZ, long startTime, boolean completed, int categoryOrdinal, int tokenReward, int repReward, int xpReward, int killsAchieved, int killsRequired, int missionStateOrdinal, int ryoReward, int pveXpReward, int currentStepIndex, int totalSteps, String currentStepObjective, int currentStepType, int scoutProgress, int scoutRequired) {
         this.templateId = templateId;
         this.templateName = templateName;
         this.description = description;
         this.objectiveText = objectiveText;
         this.targetX = targetX;
         this.targetY = targetY;
         this.targetZ = targetZ;
         this.startTime = startTime;
         this.completed = completed;
         this.categoryOrdinal = categoryOrdinal;
         this.tokenReward = tokenReward;
         this.repReward = repReward;
         this.xpReward = xpReward;
         this.killsAchieved = killsAchieved;
         this.killsRequired = killsRequired;
         this.missionStateOrdinal = missionStateOrdinal;
         this.ryoReward = ryoReward;
         this.pveXpReward = pveXpReward;
         this.currentStepIndex = currentStepIndex;
         this.totalSteps = totalSteps;
         this.currentStepObjective = currentStepObjective;
         this.currentStepType = currentStepType;
         this.scoutProgress = scoutProgress;
         this.scoutRequired = scoutRequired;
      }
   }

   public static class Handler implements IMessageHandler<AkatsukiMissionSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(final AkatsukiMissionSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            public void run() {
               AkatsukiClientData.activeMissions.clear();

               for(MissionEntry entry : msg.missions) {
                  AkatsukiClientData.activeMissions.add(new AkatsukiClientData.MissionClientEntry(entry.templateId, entry.templateName, entry.description, entry.objectiveText, entry.targetX, entry.targetY, entry.targetZ, entry.startTime, entry.completed, entry.categoryOrdinal, entry.tokenReward, entry.repReward, entry.xpReward, entry.killsAchieved, entry.killsRequired, entry.missionStateOrdinal, entry.ryoReward, entry.pveXpReward, entry.currentStepIndex, entry.totalSteps, entry.currentStepObjective, entry.currentStepType, entry.scoutProgress, entry.scoutRequired));
               }

               AkatsukiClientData.missionOffers.clear();

               for(MissionEntry entry : msg.offerEntries) {
                  AkatsukiClientData.missionOffers.add(new AkatsukiClientData.MissionOfferEntry(entry.templateId, entry.templateName, entry.description, entry.objectiveText, entry.targetX, entry.targetY, entry.targetZ, entry.categoryOrdinal, entry.tokenReward, entry.repReward, entry.xpReward, entry.ryoReward, entry.pveXpReward, entry.totalSteps));
               }

            }
         });
         return null;
      }
   }
}
