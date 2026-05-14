package net.luck.narutoaddon.OtherCode.raid.boss;

import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

public interface IRaidBoss {
   String getBossId();

   String getBossDisplayName();

   ResourceLocation getBossBarTexture();

   void setDifficulty(RaidDifficulty var1);

   RaidDifficulty getDifficulty();

   void initPhases();

   void onPhaseTransition(int var1, int var2, BossPhase var3, BossPhase var4);

   BossPhaseController getPhaseController();

   RaidInstance getRaidInstance();

   void setRaidInstance(RaidInstance var1);

   void setDamageImmune(boolean var1, String var2);

   boolean isDamageImmune();

   String getImmunityReason();

   void setEnrageLevel(int var1);

   int getEnrageLevel();

   BossInfoServer getBossInfo();

   void addPlayerToBossBar(EntityPlayer var1);

   void removePlayerFromBossBar(EntityPlayer var1);

   void clearBossBar();

   World getWorld();

   float getHealth();

   float getMaxHealth();
}
