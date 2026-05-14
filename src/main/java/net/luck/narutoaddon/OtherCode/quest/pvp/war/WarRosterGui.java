
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.gui.GuiRankedMenu;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpModInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class WarRosterGui extends GuiScreen {
   private static final int GUI_WIDTH = 300;
   private static final int GUI_HEIGHT = 260;
   private static final int COLOR_BG_TOP = -298833888;
   private static final int COLOR_BG_BOT = -300280816;
   private static final int COLOR_BORDER_OUTER = -7706054;
   private static final int COLOR_TITLE = -1521552;
   private static final int COLOR_TEXT_BODY = -2832216;
   private static final int COLOR_TEXT_DIM = -7700886;
   private static final int COLOR_SEPARATOR = -9807296;
   private static final int COLOR_SELECTED = -11141291;
   private static final int COLOR_UNSELECTED = -5592406;
   private static final int MAX_ROSTER = 10;
   private static final int ENTRIES_PER_PAGE = 10;
   private static final int BUTTON_CONFIRM = 1;
   private static final int BUTTON_CANCEL = 2;
   private static final int BUTTON_SCROLL_UP = 3;
   private static final int BUTTON_SCROLL_DOWN = 4;
   private static final int BUTTON_PLAYER_BASE = 100;
   private int guiLeft;
   private int guiTop;
   private int scrollOffset;
   private final byte warMode;
   private final String targetVillageTeamName;
   private final boolean defenderMode;
   private final List<PlayerEntry> villagePlayers;
   private final Set<UUID> selectedPlayers;
   private List<PlayerEntry> serverProvidedPlayers;

   public WarRosterGui(byte warMode, String targetVillageTeamName) {
      this.scrollOffset = 0;
      this.villagePlayers = new ArrayList();
      this.selectedPlayers = new HashSet();
      this.warMode = warMode;
      this.targetVillageTeamName = targetVillageTeamName;
      this.defenderMode = false;
   }

   public WarRosterGui(byte warMode, String targetVillageTeamName, List<UUID> playerUUIDs, List<String> playerNames) {
      this(warMode, targetVillageTeamName, playerUUIDs, playerNames, false);
   }

   public WarRosterGui(byte warMode, String targetVillageTeamName, List<UUID> playerUUIDs, List<String> playerNames, boolean defenderMode) {
      this.scrollOffset = 0;
      this.villagePlayers = new ArrayList();
      this.selectedPlayers = new HashSet();
      this.warMode = warMode;
      this.targetVillageTeamName = targetVillageTeamName;
      this.defenderMode = defenderMode;
      this.serverProvidedPlayers = new ArrayList();

      for(int i = 0; i < playerUUIDs.size(); ++i) {
         this.serverProvidedPlayers.add(new PlayerEntry((UUID)playerUUIDs.get(i), (String)playerNames.get(i)));
      }

   }

   public void initGui() {
      this.guiLeft = (this.width - 300) / 2;
      this.guiTop = (this.height - 260) / 2;
      this.buildPlayerList();
      this.rebuildButtons();
   }

   private void buildPlayerList() {
      this.villagePlayers.clear();
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null && mc.world != null) {
         UUID selfUUID = mc.player.getUniqueID();
         if (this.serverProvidedPlayers != null && !this.serverProvidedPlayers.isEmpty()) {
            this.villagePlayers.addAll(this.serverProvidedPlayers);
            this.villagePlayers.sort((a, b) -> {
               if (a.uuid.equals(selfUUID)) {
                  return -1;
               } else {
                  return b.uuid.equals(selfUUID) ? 1 : a.name.compareToIgnoreCase(b.name);
               }
            });
         } else {
            NetHandlerPlayClient connection = mc.getConnection();
            if (connection != null) {
               Scoreboard scoreboard = mc.world.getScoreboard();
               ScorePlayerTeam myTeam = scoreboard.getPlayersTeam(mc.player.getName());
               if (myTeam != null) {
                  String myTeamName = myTeam.getName();
                  if (myTeamName != null && !myTeamName.isEmpty()) {
                     String myVillageName = null;

                     for(VillageHelper.Village v : VillageHelper.Village.values()) {
                        if (v != VillageHelper.Village.UNKNOWN && (v.teamName.equalsIgnoreCase(myTeamName) || v.villageName.equalsIgnoreCase(myTeamName))) {
                           myTeamName = v.teamName;
                           myVillageName = v.villageName;
                           break;
                        }
                     }

                     for(NetworkPlayerInfo info : connection.getPlayerInfoMap()) {
                        UUID playerUUID = info.getGameProfile().getId();
                        String playerName = info.getGameProfile().getName();
                        if (playerUUID != null && playerName != null) {
                           ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);
                           if (team != null) {
                              String teamName = team.getName();
                              if (teamName != null && (teamName.equalsIgnoreCase(myTeamName) || myVillageName != null && teamName.equalsIgnoreCase(myVillageName))) {
                                 this.villagePlayers.add(new PlayerEntry(playerUUID, playerName));
                              }
                           }
                        }
                     }

                     this.villagePlayers.sort((a, b) -> {
                        if (a.uuid.equals(selfUUID)) {
                           return -1;
                        } else {
                           return b.uuid.equals(selfUUID) ? 1 : a.name.compareToIgnoreCase(b.name);
                        }
                     });
                  }
               }
            }
         }
      }
   }

   private void rebuildButtons() {
      this.buttonList.clear();
      int listX = this.guiLeft + 12;
      int listY = this.guiTop + 44;
      int entryH = 18;
      int entryW = 276;
      int maxVisible = Math.min(10, this.villagePlayers.size() - this.scrollOffset);

      for(int i = 0; i < maxVisible; ++i) {
         int idx = this.scrollOffset + i;
         if (idx >= this.villagePlayers.size()) {
            break;
         }

         PlayerEntry entry = (PlayerEntry)this.villagePlayers.get(idx);
         boolean isSelected = this.selectedPlayers.contains(entry.uuid);
         String label = (isSelected ? "✔ " : "○ ") + entry.name;
         int topColor = isSelected ? -14001622 : -12962776;
         int botColor = isSelected ? -15058406 : -14015464;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(100 + i, listX, listY + i * (entryH + 1), entryW, entryH, label, topColor, botColor));
      }

      int scrollBtnX = this.guiLeft + 300 - 28;
      if (this.scrollOffset > 0) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, scrollBtnX, this.guiTop + 30, 16, 14, "▲", -11910088, -14015464));
      }

      if (this.scrollOffset + 10 < this.villagePlayers.size()) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(4, scrollBtnX, this.guiTop + 260 - 50, 16, 14, "▼", -11910088, -14015464));
      }

      int bottomY = this.guiTop + 260 - 30;
      int btnW = 80;
      int gap = 10;
      boolean canConfirm = !this.selectedPlayers.isEmpty();
      int confirmTop = canConfirm ? -12944838 : -12961222;
      int confirmBot = canConfirm ? -14788066 : -14013910;
      GuiRankedMenu.GuiButtonGradient confirmBtn = new GuiRankedMenu.GuiButtonGradient(1, this.guiLeft + 150 - btnW - gap / 2, bottomY, btnW, 22, "Confirm (" + this.selectedPlayers.size() + "/" + 10 + ")", confirmTop, confirmBot);
      confirmBtn.enabled = canConfirm;
      this.buttonList.add(confirmBtn);
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(2, this.guiLeft + 150 + gap / 2, bottomY, btnW, 22, "Cancel", -9811398, -11916774));
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 260, -298833888, -300280816);
      drawRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 2, -7706054);
      drawRect(this.guiLeft, this.guiTop + 260 - 2, this.guiLeft + 300, this.guiTop + 260, -7706054);
      drawRect(this.guiLeft, this.guiTop, this.guiLeft + 2, this.guiTop + 260, -7706054);
      drawRect(this.guiLeft + 300 - 2, this.guiTop, this.guiLeft + 300, this.guiTop + 260, -7706054);
      String title = "§l⚔ Select Roster (10v10)";
      int titleW = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleW) / 2), (float)(this.guiTop + 8), -1521552);
      String subtitle = this.selectedPlayers.size() + "/" + 10 + " warriors selected";
      int subColor = this.selectedPlayers.size() >= 10 ? -11141291 : -2832216;
      int subW = this.fontRenderer.getStringWidth(subtitle);
      this.fontRenderer.drawStringWithShadow(subtitle, (float)(this.guiLeft + (300 - subW) / 2), (float)(this.guiTop + 22), subColor);
      this.drawHorizontalLine(this.guiLeft + 10, this.guiLeft + 300 - 10, this.guiTop + 34, -9807296);
      if (this.villagePlayers.isEmpty()) {
         String noPlayers = "No village members online.";
         int npW = this.fontRenderer.getStringWidth(noPlayers);
         this.fontRenderer.drawStringWithShadow(noPlayers, (float)(this.guiLeft + (300 - npW) / 2), (float)(this.guiTop + 100), -7700886);
      }

      this.drawHorizontalLine(this.guiLeft + 10, this.guiLeft + 300 - 10, this.guiTop + 260 - 38, -9807296);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   protected void actionPerformed(GuiButton button) {
      switch (button.id) {
         case 1:
            if (!this.selectedPlayers.isEmpty()) {
               if (this.defenderMode) {
                  PvpModInit.NETWORK.sendToServer(WarRosterSubmitMessage.create(this.selectedPlayers));
               } else {
                  PvpModInit.NETWORK.sendToServer(WarRosterMessage.create(this.warMode, this.targetVillageTeamName, this.selectedPlayers));
               }

               this.mc.displayGuiScreen((GuiScreen)null);
            }
            break;
         case 2:
            this.mc.displayGuiScreen((GuiScreen)null);
            break;
         case 3:
            if (this.scrollOffset > 0) {
               --this.scrollOffset;
               this.rebuildButtons();
            }
            break;
         case 4:
            if (this.scrollOffset + 10 < this.villagePlayers.size()) {
               ++this.scrollOffset;
               this.rebuildButtons();
            }
            break;
         default:
            if (button.id >= 100 && button.id < 110) {
               int visibleIdx = button.id - 100;
               int idx = this.scrollOffset + visibleIdx;
               if (idx >= 0 && idx < this.villagePlayers.size()) {
                  PlayerEntry entry = (PlayerEntry)this.villagePlayers.get(idx);
                  if (this.selectedPlayers.contains(entry.uuid)) {
                     this.selectedPlayers.remove(entry.uuid);
                  } else if (this.selectedPlayers.size() < 10) {
                     this.selectedPlayers.add(entry.uuid);
                  }

                  this.rebuildButtons();
               }
            }
      }

   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int scroll = Mouse.getEventDWheel();
      if (scroll != 0) {
         if (scroll > 0 && this.scrollOffset > 0) {
            --this.scrollOffset;
            this.rebuildButtons();
         } else if (scroll < 0 && this.scrollOffset + 10 < this.villagePlayers.size()) {
            ++this.scrollOffset;
            this.rebuildButtons();
         }
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private static class PlayerEntry {
      final UUID uuid;
      final String name;

      PlayerEntry(UUID uuid, String name) {
         this.uuid = uuid;
         this.name = name;
      }
   }
}
