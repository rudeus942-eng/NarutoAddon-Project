package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class TournamentGuiHandler implements IGuiHandler {
   public static final int GUI_REWARD_BASE = 100;

   public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
      if (id >= 100 && id <= 102) {
         int place = id - 100 + 1;
         if (player instanceof EntityPlayerMP) {
            String villageName = VillageHelper.getVillage((EntityPlayerMP)player).teamName;
            return new TournamentRewardContainer(player, villageName, place);
         }
      }

      return null;
   }

   public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
      if (id >= 100 && id <= 102) {
         int place = id - 100 + 1;
         return new TournamentRewardGui(new TournamentRewardContainer(player, "", place), place);
      } else {
         return null;
      }
   }
}
