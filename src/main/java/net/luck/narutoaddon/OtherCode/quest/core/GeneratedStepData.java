
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;

public class GeneratedStepData {
   public QuestStep.StepType type;
   public String description;
   public BlockPos position;
   public String[] spawnEntityIds;
   public int[] spawnCounts;
   public String[] npcConfigIds;
   public String npcName;
   public String[] dialogLines;
   public String[] dialogResponses;
   public String dialogNpcConfigId;

   public static GeneratedStepData travel(String description, BlockPos position) {
      GeneratedStepData data = new GeneratedStepData();
      data.type = QuestStep.StepType.TRAVEL;
      data.description = description;
      data.position = position;
      return data;
   }

   public static GeneratedStepData combat(String description, BlockPos position, String[] npcConfigIds) {
      GeneratedStepData data = new GeneratedStepData();
      data.type = QuestStep.StepType.COMBAT;
      data.description = description;
      data.position = position;
      data.spawnEntityIds = new String[]{"inftsukaddon:dailymissionnpc"};
      data.spawnCounts = new int[]{npcConfigIds.length};
      data.npcConfigIds = npcConfigIds;
      return data;
   }

   public static GeneratedStepData dialog(String description, BlockPos position, String npcName, String[] dialogLines, String[] dialogResponses, String dialogNpcConfigId) {
      GeneratedStepData data = new GeneratedStepData();
      data.type = QuestStep.StepType.DIALOG;
      data.description = description;
      data.position = position;
      data.npcName = npcName;
      data.dialogLines = dialogLines;
      data.dialogResponses = dialogResponses;
      data.dialogNpcConfigId = dialogNpcConfigId;
      return data;
   }

   public QuestStep toQuestStep() {
      switch (this.type) {
         case TRAVEL:
            return QuestStep.travel(this.description, this.position);
         case COMBAT:
            if (this.npcConfigIds != null && this.npcConfigIds.length > 0) {
               return QuestStep.combatWithNpcConfig(this.description, this.position, this.spawnEntityIds, this.spawnCounts, this.npcConfigIds);
            }

            return QuestStep.combat(this.description, this.position, this.spawnEntityIds, this.spawnCounts);
         case DIALOG:
            if (this.dialogNpcConfigId != null) {
               return QuestStep.dialogWithNpc(this.description, this.position, this.npcName, this.dialogLines, this.dialogResponses, this.dialogNpcConfigId);
            }

            return QuestStep.dialog(this.description, this.position, this.npcName, this.dialogLines, this.dialogResponses);
         default:
            return QuestStep.travel(this.description, this.position);
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("type", this.type.ordinal());
      nbt.setString("desc", this.description != null ? this.description : "");
      if (this.position != null) {
         nbt.setInteger("posX", this.position.getX());
         nbt.setInteger("posY", this.position.getY());
         nbt.setInteger("posZ", this.position.getZ());
      }

      if (this.spawnEntityIds != null) {
         NBTTagList entityIdList = new NBTTagList();

         for(String id : this.spawnEntityIds) {
            entityIdList.appendTag(new NBTTagString(id));
         }

         nbt.setTag("spawnEntityIds", entityIdList);
      }

      if (this.spawnCounts != null) {
         nbt.setIntArray("spawnCounts", this.spawnCounts);
      }

      if (this.npcConfigIds != null) {
         NBTTagList configList = new NBTTagList();

         for(String id : this.npcConfigIds) {
            configList.appendTag(new NBTTagString(id));
         }

         nbt.setTag("npcConfigIds", configList);
      }

      if (this.npcName != null) {
         nbt.setString("npcName", this.npcName);
      }

      if (this.dialogLines != null) {
         NBTTagList lineList = new NBTTagList();

         for(String line : this.dialogLines) {
            lineList.appendTag(new NBTTagString(line));
         }

         nbt.setTag("dialogLines", lineList);
      }

      if (this.dialogResponses != null) {
         NBTTagList respList = new NBTTagList();

         for(String resp : this.dialogResponses) {
            respList.appendTag(new NBTTagString(resp));
         }

         nbt.setTag("dialogResponses", respList);
      }

      if (this.dialogNpcConfigId != null) {
         nbt.setString("dialogNpcConfigId", this.dialogNpcConfigId);
      }

      return nbt;
   }

   public static GeneratedStepData readFromNBT(NBTTagCompound nbt) {
      GeneratedStepData data = new GeneratedStepData();
      int typeOrd = nbt.getInteger("type");
      QuestStep.StepType[] types = QuestStep.StepType.values();
      data.type = typeOrd < types.length ? types[typeOrd] : QuestStep.StepType.TRAVEL;
      data.description = nbt.getString("desc");
      if (nbt.hasKey("posX")) {
         data.position = new BlockPos(nbt.getInteger("posX"), nbt.getInteger("posY"), nbt.getInteger("posZ"));
      }

      if (nbt.hasKey("spawnEntityIds")) {
         NBTTagList entityIdList = nbt.getTagList("spawnEntityIds", 8);
         data.spawnEntityIds = new String[entityIdList.tagCount()];

         for(int i = 0; i < entityIdList.tagCount(); ++i) {
            data.spawnEntityIds[i] = entityIdList.getStringTagAt(i);
         }
      }

      if (nbt.hasKey("spawnCounts")) {
         data.spawnCounts = nbt.getIntArray("spawnCounts");
      }

      if (nbt.hasKey("npcConfigIds")) {
         NBTTagList configList = nbt.getTagList("npcConfigIds", 8);
         data.npcConfigIds = new String[configList.tagCount()];

         for(int i = 0; i < configList.tagCount(); ++i) {
            data.npcConfigIds[i] = configList.getStringTagAt(i);
         }
      }

      if (nbt.hasKey("npcName")) {
         data.npcName = nbt.getString("npcName");
      }

      if (nbt.hasKey("dialogLines")) {
         NBTTagList lineList = nbt.getTagList("dialogLines", 8);
         data.dialogLines = new String[lineList.tagCount()];

         for(int i = 0; i < lineList.tagCount(); ++i) {
            data.dialogLines[i] = lineList.getStringTagAt(i);
         }
      }

      if (nbt.hasKey("dialogResponses")) {
         NBTTagList respList = nbt.getTagList("dialogResponses", 8);
         data.dialogResponses = new String[respList.tagCount()];

         for(int i = 0; i < respList.tagCount(); ++i) {
            data.dialogResponses[i] = respList.getStringTagAt(i);
         }
      }

      if (nbt.hasKey("dialogNpcConfigId")) {
         data.dialogNpcConfigId = nbt.getString("dialogNpcConfigId");
      }

      return data;
   }
}
