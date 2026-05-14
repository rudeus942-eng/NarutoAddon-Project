
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RaidBossDataMessage implements IMessage {
   public static final int ACTION_BOSS_SPAWN = 0;
   public static final int ACTION_HEALTH_UPDATE = 1;
   public static final int ACTION_PHASE_CHANGE = 2;
   public static final int ACTION_IMMUNITY_UPDATE = 3;
   public static final int ACTION_ENRAGE_UPDATE = 4;
   public static final int ACTION_BOSS_DEATH = 5;
   public static final int ACTION_BOSS_DESPAWN = 6;
   private int action;
   private int raidId;
   private String bossId;
   private String bossDisplayName;
   private int entityId;
   private float currentHealth;
   private float maxHealth;
   private float healthPercent;
   private int currentPhase;
   private int totalPhases;
   private String phaseName;
   private boolean isImmune;
   private String immunityReason;
   private int enrageLevel;
   private long raidTimeElapsed;
   private int difficulty;

   public static RaidBossDataMessage createSpawnMessage(int raidId, String bossId, String displayName, int entityId, float maxHealth, int totalPhases, int difficulty) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 0;
      msg.raidId = raidId;
      msg.bossId = bossId;
      msg.bossDisplayName = displayName;
      msg.entityId = entityId;
      msg.maxHealth = maxHealth;
      msg.currentHealth = maxHealth;
      msg.healthPercent = 1.0F;
      msg.totalPhases = totalPhases;
      msg.currentPhase = 1;
      msg.difficulty = difficulty;
      return msg;
   }

   public static RaidBossDataMessage createHealthUpdate(int raidId, float currentHealth, float maxHealth) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 1;
      msg.raidId = raidId;
      msg.currentHealth = currentHealth;
      msg.maxHealth = maxHealth;
      msg.healthPercent = maxHealth > 0.0F ? currentHealth / maxHealth : 0.0F;
      return msg;
   }

   public static RaidBossDataMessage createPhaseChange(int raidId, int currentPhase, int totalPhases, String phaseName) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 2;
      msg.raidId = raidId;
      msg.currentPhase = currentPhase;
      msg.totalPhases = totalPhases;
      msg.phaseName = phaseName;
      return msg;
   }

   public static RaidBossDataMessage createImmunityUpdate(int raidId, boolean isImmune, String reason) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 3;
      msg.raidId = raidId;
      msg.isImmune = isImmune;
      msg.immunityReason = reason;
      return msg;
   }

   public static RaidBossDataMessage createEnrageUpdate(int raidId, int enrageLevel, long timeElapsed) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 4;
      msg.raidId = raidId;
      msg.enrageLevel = enrageLevel;
      msg.raidTimeElapsed = timeElapsed;
      return msg;
   }

   public static RaidBossDataMessage createDeathMessage(int raidId) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 5;
      msg.raidId = raidId;
      return msg;
   }

   public static RaidBossDataMessage createDespawnMessage(int raidId) {
      RaidBossDataMessage msg = new RaidBossDataMessage();
      msg.action = 6;
      msg.raidId = raidId;
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readInt();
      this.raidId = buf.readInt();
      switch (this.action) {
         case 0:
            this.bossId = ByteBufUtils.readUTF8String(buf);
            this.bossDisplayName = ByteBufUtils.readUTF8String(buf);
            this.entityId = buf.readInt();
            this.currentHealth = buf.readFloat();
            this.maxHealth = buf.readFloat();
            this.healthPercent = buf.readFloat();
            this.currentPhase = buf.readInt();
            this.totalPhases = buf.readInt();
            this.difficulty = buf.readInt();
            break;
         case 1:
            this.currentHealth = buf.readFloat();
            this.maxHealth = buf.readFloat();
            this.healthPercent = buf.readFloat();
            break;
         case 2:
            this.currentPhase = buf.readInt();
            this.totalPhases = buf.readInt();
            this.phaseName = ByteBufUtils.readUTF8String(buf);
            break;
         case 3:
            this.isImmune = buf.readBoolean();
            this.immunityReason = ByteBufUtils.readUTF8String(buf);
            break;
         case 4:
            this.enrageLevel = buf.readInt();
            this.raidTimeElapsed = buf.readLong();
         case 5:
         case 6:
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.action);
      buf.writeInt(this.raidId);
      switch (this.action) {
         case 0:
            ByteBufUtils.writeUTF8String(buf, this.bossId != null ? this.bossId : "");
            ByteBufUtils.writeUTF8String(buf, this.bossDisplayName != null ? this.bossDisplayName : "");
            buf.writeInt(this.entityId);
            buf.writeFloat(this.currentHealth);
            buf.writeFloat(this.maxHealth);
            buf.writeFloat(this.healthPercent);
            buf.writeInt(this.currentPhase);
            buf.writeInt(this.totalPhases);
            buf.writeInt(this.difficulty);
            break;
         case 1:
            buf.writeFloat(this.currentHealth);
            buf.writeFloat(this.maxHealth);
            buf.writeFloat(this.healthPercent);
            break;
         case 2:
            buf.writeInt(this.currentPhase);
            buf.writeInt(this.totalPhases);
            ByteBufUtils.writeUTF8String(buf, this.phaseName != null ? this.phaseName : "");
            break;
         case 3:
            buf.writeBoolean(this.isImmune);
            ByteBufUtils.writeUTF8String(buf, this.immunityReason != null ? this.immunityReason : "");
            break;
         case 4:
            buf.writeInt(this.enrageLevel);
            buf.writeLong(this.raidTimeElapsed);
         case 5:
         case 6:
      }

   }

   public int getAction() {
      return this.action;
   }

   public int getRaidId() {
      return this.raidId;
   }

   public String getBossId() {
      return this.bossId;
   }

   public String getBossDisplayName() {
      return this.bossDisplayName;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public float getCurrentHealth() {
      return this.currentHealth;
   }

   public float getMaxHealth() {
      return this.maxHealth;
   }

   public float getHealthPercent() {
      return this.healthPercent;
   }

   public int getCurrentPhase() {
      return this.currentPhase;
   }

   public int getTotalPhases() {
      return this.totalPhases;
   }

   public String getPhaseName() {
      return this.phaseName;
   }

   public boolean isImmune() {
      return this.isImmune;
   }

   public String getImmunityReason() {
      return this.immunityReason;
   }

   public int getEnrageLevel() {
      return this.enrageLevel;
   }

   public long getRaidTimeElapsed() {
      return this.raidTimeElapsed;
   }

   public int getDifficulty() {
      return this.difficulty;
   }

   public static class Handler implements IMessageHandler<RaidBossDataMessage, IMessage> {
      public IMessage onMessage(RaidBossDataMessage message, MessageContext ctx) {
         if (ctx.side == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(() -> this.handleClient(message));
         }

         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClient(RaidBossDataMessage message) {
         RaidClientData.handleBossUpdate(message);
      }
   }
}
