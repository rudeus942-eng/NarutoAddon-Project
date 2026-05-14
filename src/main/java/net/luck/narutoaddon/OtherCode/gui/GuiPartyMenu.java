
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.raid.core.RaidModInit;
import net.luck.narutoaddon.OtherCode.raid.network.PartyActionMessage;
import net.luck.narutoaddon.OtherCode.raid.network.PartyPlayerListMessage;
import net.luck.narutoaddon.OtherCode.raid.network.RaidClientData;
import net.luck.narutoaddon.OtherCode.raid.network.RaidPartyDataMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class GuiPartyMenu extends GuiScreen {
   private static final int GUI_WIDTH = 320;
   private static final int GUI_HEIGHT = 260;
   private static final int COLOR_BORDER_OUTER = -8960837;
   private static final int COLOR_BORDER_INNER = -11193430;
   private static final int COLOR_BG_TOP = -299755472;
   private static final int COLOR_BG_BOT = -301070312;
   private static final int COLOR_TITLE = -2572048;
   private static final int COLOR_TEXT_BODY = -3358504;
   private static final int COLOR_TEXT_DIM = -7833440;
   private static final int COLOR_TEXT_HEADER = -4677416;
   private static final int COLOR_SEPARATOR = -10860416;
   private static final int COLOR_LEADER_GOLD = -13244;
   private static final int COLOR_READY_GREEN = -10035866;
   private static final int COLOR_NOT_READY = -2267546;
   private static final int COLOR_STATE_QUEUE = -2241468;
   private static final int COLOR_STATE_RAID = -2271915;
   private static final int BUTTON_CLOSE = 0;
   private static final int BUTTON_CREATE = 1;
   private static final int BUTTON_LEAVE = 2;
   private static final int BUTTON_DISBAND = 3;
   private static final int BUTTON_READY = 4;
   private static final int BUTTON_ACCEPT_INVITE = 5;
   private static final int BUTTON_DECLINE_INVITE = 6;
   private static final int BUTTON_REFRESH = 7;
   private static final int BUTTON_TAB_PARTY = 8;
   private static final int BUTTON_TAB_INVITES = 9;
   private static final int BUTTON_INVITE_BASE = 100;
   private static final int BUTTON_KICK_BASE = 200;
   private static final int BUTTON_PROMOTE_BASE = 300;
   private static final int BUTTON_ACCEPT_BASE = 400;
   private static final int BUTTON_DECLINE_BASE = 500;
   private int playerListScroll = 0;
   private static final int MAX_VISIBLE_PLAYERS = 6;
   private int inviteListScroll = 0;
   private static final int MAX_VISIBLE_INVITES = 6;
   private int noPartyTab = 0;
   private int guiLeft;
   private int guiTop;
   private boolean dataRequested = false;
   private float animationTick = 0.0F;

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      mc.displayGuiScreen(new GuiPartyMenu());
   }

   public void initGui() {
      this.guiLeft = (this.width - 320) / 2;
      this.guiTop = (this.height - 260) / 2;
      if (!this.dataRequested) {
         this.dataRequested = true;
         RaidModInit.NETWORK.sendToServer(new PartyActionMessage(9));
      }

      this.rebuildButtons();
   }

   private void rebuildButtons() {
      this.buttonList.clear();
      int centerX = this.guiLeft + 160;
      boolean hasParty = RaidClientData.hasParty();
      boolean isLeader = hasParty && RaidClientData.getPartyLeaderId() != null && Minecraft.getMinecraft().player != null && RaidClientData.getPartyLeaderId().equals(Minecraft.getMinecraft().player.getUniqueID());
      this.addButton(new GuiRankedMenu.GuiButtonGradient(0, this.guiLeft + 320 - 22, this.guiTop + 4, 18, 14, "X", -7720141, -10872808));
      if (!hasParty) {
         int inviteCount = RaidClientData.getPendingInvites().size();
         int tabY = this.guiTop + 28;
         int partyTopC = this.noPartyTab == 0 ? -11193430 : -12967322;
         int partyBotC = this.noPartyTab == 0 ? -13427094 : -14543804;
         this.addButton(new GuiRankedMenu.GuiButtonGradient(8, centerX - 80, tabY, 75, 18, "Party", partyTopC, partyBotC));
         String invTabLabel = inviteCount > 0 ? "Invites (" + inviteCount + ")" : "Invites";
         int invTopC = this.noPartyTab == 1 ? -11193430 : -12967322;
         int invBotC = this.noPartyTab == 1 ? -13427094 : -14543804;
         this.addButton(new GuiRankedMenu.GuiButtonGradient(9, centerX + 5, tabY, 75, 18, invTabLabel, invTopC, invBotC));
         if (this.noPartyTab == 0) {
            this.addButton(new GuiRankedMenu.GuiButtonGradient(1, centerX - 65, this.guiTop + 260 - 38, 130, 24, "✦ Create Party", -11193430, -13427094));
         } else {
            List<RaidClientData.PendingInviteData> invites = RaidClientData.getPendingInvites();
            int contentTop = this.guiTop + 52;
            int startIdx = this.inviteListScroll;
            int endIdx = Math.min(startIdx + 6, invites.size());

            for(int i = startIdx; i < endIdx; ++i) {
               int yPos = contentTop + 22 + (i - startIdx) * 24;
               this.addButton(new GuiRankedMenu.GuiButtonGradient(400 + i, this.guiLeft + 320 - 130, yPos + 2, 50, 18, "Accept", -12944838, -14792162));
               this.addButton(new GuiRankedMenu.GuiButtonGradient(500 + i, this.guiLeft + 320 - 74, yPos + 2, 50, 18, "Decline", -7722968, -10872808));
            }
         }
      } else {
         int bottomY = this.guiTop + 260 - 38;
         boolean amReady = this.isPlayerReady();
         String readyText = amReady ? "Unready" : "Ready";
         int readyTop = amReady ? -7706078 : -12944838;
         int readyBot = amReady ? -10861551 : -14792162;
         this.addButton(new GuiRankedMenu.GuiButtonGradient(4, this.guiLeft + 15, bottomY, 75, 24, readyText, readyTop, readyBot));
         this.addButton(new GuiRankedMenu.GuiButtonGradient(2, centerX - 37, bottomY, 75, 24, "Leave", -9809872, -12965864));
         if (isLeader) {
            this.addButton(new GuiRankedMenu.GuiButtonGradient(3, this.guiLeft + 320 - 90, bottomY, 75, 24, "Disband", -7722968, -10872808));
         }

         if (isLeader) {
            int panelTop = this.guiTop + 30;
            List<PartyPlayerListMessage.PlayerEntry> available = RaidClientData.getAvailablePlayers();
            int startIdx = this.playerListScroll;
            int endIdx = Math.min(startIdx + 6, available.size());

            for(int i = startIdx; i < endIdx; ++i) {
               int yPos = panelTop + 22 + (i - startIdx) * 24;
               PartyPlayerListMessage.PlayerEntry entry = (PartyPlayerListMessage.PlayerEntry)available.get(i);
               if (entry.hasPendingInvite) {
                  this.addButton(new GuiRankedMenu.GuiButtonGradient(100 + i, this.guiLeft + 320 - 58, yPos + 2, 42, 18, "Cancel", -7711454, -10865903));
               } else {
                  this.addButton(new GuiRankedMenu.GuiButtonGradient(100 + i, this.guiLeft + 320 - 58, yPos + 2, 42, 18, "Invite", -11193430, -13427094));
               }
            }

            this.addButton(new GuiRankedMenu.GuiButtonGradient(7, this.guiLeft + 320 - 58, panelTop + 2, 42, 14, "Refresh", -13408632, -15058856));
         }

         if (isLeader) {
            int panelTop = this.guiTop + 30;
            List<RaidPartyDataMessage.PartyMemberData> members = RaidClientData.getPartyMembers();
            UUID myUUID = Minecraft.getMinecraft().player.getUniqueID();

            for(int i = 0; i < members.size(); ++i) {
               RaidPartyDataMessage.PartyMemberData member = (RaidPartyDataMessage.PartyMemberData)members.get(i);
               if (!member.id.equals(myUUID)) {
                  int yPos = panelTop + 22 + i * 24;
                  this.addButton(new GuiRankedMenu.GuiButtonGradient(200 + i, this.guiLeft + 130, yPos + 2, 28, 18, "Kick", -7722968, -10872808));
               }
            }
         }
      }

   }

   private boolean isPlayerReady() {
      if (!RaidClientData.hasParty()) {
         return false;
      } else {
         UUID myUUID = Minecraft.getMinecraft().player.getUniqueID();

         for(RaidPartyDataMessage.PartyMemberData member : RaidClientData.getPartyMembers()) {
            if (member.id.equals(myUUID)) {
               return member.ready;
            }
         }

         return false;
      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      float glowIntensity = (float)(0.2 + 0.1 * Math.sin((double)this.animationTick));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 4, this.guiTop - 4, this.guiLeft + 320 + 4, this.guiTop + 260 + 4, glowAlpha << 24 | 7816379);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 320, this.guiTop + 260, -299755472, -301070312);
      this.drawBorderWithShading(this.guiLeft, this.guiTop, 320, 260, -8960837, 2);
      this.drawBorderWithShading(this.guiLeft + 4, this.guiTop + 4, 312, 252, -11193430, 1);
      this.drawShieldCorners();
      String title = "§l✦ Party ✦";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawString(title, this.guiLeft + (320 - titleWidth) / 2 + 1, this.guiTop + 10 + 1, 919576);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (320 - titleWidth) / 2), (float)(this.guiTop + 10), -2572048);
      this.drawHorizontalLine(this.guiLeft + 20, this.guiLeft + 160 - 10, this.guiTop + 24, -10860416);
      this.drawHorizontalLine(this.guiLeft + 160 + 10, this.guiLeft + 320 - 20, this.guiTop + 24, -10860416);
      this.drawDiamond(this.guiLeft + 160, this.guiTop + 24, 5, -8960837);
      boolean hasParty = RaidClientData.hasParty();
      if (!hasParty) {
         int tabY = this.guiTop + 46;
         int centerX = this.guiLeft + 160;
         if (this.noPartyTab == 0) {
            this.drawHorizontalLine(centerX - 78, centerX - 7, tabY, -8960837);
         } else {
            this.drawHorizontalLine(centerX + 7, centerX + 78, tabY, -8960837);
         }

         if (this.noPartyTab == 0) {
            this.drawNoPartyView();
         } else {
            this.drawInvitesView();
         }
      } else {
         this.drawPartyView();
      }

      this.drawHorizontalLine(this.guiLeft + 20, this.guiLeft + 320 - 20, this.guiTop + 260 - 48, -10860416);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawNoPartyView() {
      int emblemY = this.guiTop + 80;
      this.drawPanelWithLighting(this.guiLeft + 40, emblemY, 240, 80);
      String crest = "✦";
      int crestWidth = this.fontRenderer.getStringWidth(crest);
      GlStateManager.pushMatrix();
      float scale = 3.0F;
      float crestX = (float)(this.guiLeft + 160) - (float)crestWidth * scale / 2.0F;
      float crestY = (float)(emblemY + 8);
      GlStateManager.translate(crestX, crestY, 0.0F);
      GlStateManager.scale(scale, scale, 1.0F);
      this.fontRenderer.drawStringWithShadow(crest, 0.0F, 0.0F, -8960837);
      GlStateManager.popMatrix();
      int textY = emblemY + 40;
      String msg1 = "You are not in a party.";
      String msg2 = "Create a party to invite players";
      String msg3 = "for raids and quests.";
      int w1 = this.fontRenderer.getStringWidth(msg1);
      int w2 = this.fontRenderer.getStringWidth(msg2);
      int w3 = this.fontRenderer.getStringWidth(msg3);
      this.fontRenderer.drawStringWithShadow(msg1, (float)(this.guiLeft + (320 - w1) / 2), (float)textY, -3358504);
      this.fontRenderer.drawStringWithShadow(msg2, (float)(this.guiLeft + (320 - w2) / 2), (float)(textY + 12), -7833440);
      this.fontRenderer.drawStringWithShadow(msg3, (float)(this.guiLeft + (320 - w3) / 2), (float)(textY + 24), -7833440);
   }

   private void drawInvitesView() {
      List<RaidClientData.PendingInviteData> invites = RaidClientData.getPendingInvites();
      int contentTop = this.guiTop + 52;
      int contentBot = this.guiTop + 260 - 52;
      this.drawPanelWithLighting(this.guiLeft + 12, contentTop, 296, contentBot - contentTop);
      if (invites.isEmpty()) {
         String empty1 = "No pending invites.";
         String empty2 = "When someone invites you,";
         String empty3 = "it will appear here.";
         int ew1 = this.fontRenderer.getStringWidth(empty1);
         int ew2 = this.fontRenderer.getStringWidth(empty2);
         int ew3 = this.fontRenderer.getStringWidth(empty3);
         int emptyY = contentTop + 40;
         this.fontRenderer.drawStringWithShadow(empty1, (float)(this.guiLeft + (320 - ew1) / 2), (float)emptyY, -3358504);
         this.fontRenderer.drawStringWithShadow(empty2, (float)(this.guiLeft + (320 - ew2) / 2), (float)(emptyY + 14), -7833440);
         this.fontRenderer.drawStringWithShadow(empty3, (float)(this.guiLeft + (320 - ew3) / 2), (float)(emptyY + 26), -7833440);
      } else {
         String hdr = "§l✉ Pending Invites";
         this.fontRenderer.drawStringWithShadow(hdr, (float)(this.guiLeft + 20), (float)(contentTop + 5), -4677416);
         this.drawHorizontalLine(this.guiLeft + 16, this.guiLeft + 320 - 16, contentTop + 17, -10860416);
         int startIdx = this.inviteListScroll;
         int endIdx = Math.min(startIdx + 6, invites.size());

         for(int i = startIdx; i < endIdx; ++i) {
            RaidClientData.PendingInviteData inv = (RaidClientData.PendingInviteData)invites.get(i);
            int yPos = contentTop + 22 + (i - startIdx) * 24;
            if ((i - startIdx) % 2 == 0) {
               drawRect(this.guiLeft + 15, yPos, this.guiLeft + 320 - 15, yPos + 22, 419430399);
            }

            String inviterText = inv.inviterName;
            int maxW = 140;
            if (this.fontRenderer.getStringWidth(inviterText) > maxW) {
               while(this.fontRenderer.getStringWidth(inviterText + "..") > maxW && inviterText.length() > 1) {
                  inviterText = inviterText.substring(0, inviterText.length() - 1);
               }

               inviterText = inviterText + "..";
            }

            this.fontRenderer.drawStringWithShadow(inviterText, (float)(this.guiLeft + 20), (float)(yPos + 7), -13244);
         }

         if (invites.size() > 6) {
            String scrollText = this.inviteListScroll + 1 + "-" + endIdx + "/" + invites.size();
            int scrollW = this.fontRenderer.getStringWidth(scrollText);
            this.fontRenderer.drawString(scrollText, this.guiLeft + (320 - scrollW) / 2, contentBot - 14, -7833440);
         }

      }
   }

   private void drawPartyView() {
      UUID myUUID = Minecraft.getMinecraft().player.getUniqueID();
      boolean isLeader = RaidClientData.getPartyLeaderId() != null && RaidClientData.getPartyLeaderId().equals(myUUID);
      int leftPanelX = this.guiLeft + 10;
      int leftPanelW = isLeader ? 155 : 300;
      int panelTop = this.guiTop + 30;
      int panelBot = this.guiTop + 260 - 52;
      this.drawPanelWithLighting(leftPanelX, panelTop, leftPanelW, panelBot - panelTop);
      String memberHeader = "§l★ Members (" + RaidClientData.getPartyMembers().size() + "/6)";
      this.fontRenderer.drawStringWithShadow(memberHeader, (float)(leftPanelX + 6), (float)(panelTop + 5), -4677416);
      this.drawHorizontalLine(leftPanelX + 4, leftPanelX + leftPanelW - 4, panelTop + 17, -10860416);
      List<RaidPartyDataMessage.PartyMemberData> members = RaidClientData.getPartyMembers();
      UUID leaderId = RaidClientData.getPartyLeaderId();

      for(int i = 0; i < members.size(); ++i) {
         RaidPartyDataMessage.PartyMemberData member = (RaidPartyDataMessage.PartyMemberData)members.get(i);
         int yPos = panelTop + 22 + i * 24;
         if (i % 2 == 0) {
            drawRect(leftPanelX + 3, yPos, leftPanelX + leftPanelW - 3, yPos + 22, 419430399);
         }

         String readyIcon = member.ready ? "✔" : "✘";
         int iconColor = member.ready ? -10035866 : -2267546;
         this.fontRenderer.drawStringWithShadow(readyIcon, (float)(leftPanelX + 6), (float)(yPos + 7), iconColor);
         String prefix = "";
         int nameColor = -3358504;
         if (member.id.equals(leaderId)) {
            prefix = "§6★ ";
            nameColor = -13244;
         }

         String displayName = prefix + member.name;
         this.fontRenderer.drawStringWithShadow(displayName, (float)(leftPanelX + 15), (float)(yPos + 7), nameColor);
         if (!isLeader || member.id.equals(myUUID)) {
            String statusText = member.ready ? "§aReady" : "§8Waiting";
            int statusW = this.fontRenderer.getStringWidth(statusText);
            this.fontRenderer.drawString(statusText, leftPanelX + leftPanelW - statusW - 8, yPos + 7, 16777215);
         }
      }

      int stateY = panelBot - 16;
      int partyState = RaidClientData.getPartyState();
      String stateText;
      int stateColor;
      switch (partyState) {
         case 1:
            stateText = "✔ All Ready!";
            stateColor = -10035866;
            break;
         case 2:
            stateText = "✦ In Queue...";
            stateColor = -2241468;
            break;
         case 3:
            stateText = "⚔ In Raid";
            stateColor = -2271915;
            break;
         default:
            stateText = "○ Forming Party";
            stateColor = -7833440;
      }

      int stateW = this.fontRenderer.getStringWidth(stateText);
      this.fontRenderer.drawStringWithShadow(stateText, (float)(leftPanelX + (leftPanelW - stateW) / 2), (float)stateY, stateColor);
      if (isLeader) {
         int rightPanelX = this.guiLeft + 170;
         int rightPanelW = 140;
         this.drawPanelWithLighting(rightPanelX, panelTop, rightPanelW, panelBot - panelTop);
         String onlineHeader = "§lOnline";
         this.fontRenderer.drawStringWithShadow(onlineHeader, (float)(rightPanelX + 6), (float)(panelTop + 5), -4677416);
         this.drawHorizontalLine(rightPanelX + 4, rightPanelX + rightPanelW - 4, panelTop + 17, -10860416);
         List<PartyPlayerListMessage.PlayerEntry> available = RaidClientData.getAvailablePlayers();
         if (available.isEmpty()) {
            this.fontRenderer.drawStringWithShadow("No players", (float)(rightPanelX + 6), (float)(panelTop + 28), -7833440);
            this.fontRenderer.drawStringWithShadow("available", (float)(rightPanelX + 6), (float)(panelTop + 40), -7833440);
         } else {
            int startIdx = this.playerListScroll;
            int endIdx = Math.min(startIdx + 6, available.size());

            for(int i = startIdx; i < endIdx; ++i) {
               PartyPlayerListMessage.PlayerEntry entry = (PartyPlayerListMessage.PlayerEntry)available.get(i);
               int yPos = panelTop + 22 + (i - startIdx) * 24;
               if (entry.hasPendingInvite) {
                  drawRect(rightPanelX + 3, yPos, rightPanelX + rightPanelW - 3, yPos + 22, 553617408);
               } else if ((i - startIdx) % 2 == 0) {
                  drawRect(rightPanelX + 3, yPos, rightPanelX + rightPanelW - 3, yPos + 22, 419430399);
               }

               String name = entry.name;
               int maxNameW = rightPanelW - 58;
               if (this.fontRenderer.getStringWidth(name) > maxNameW) {
                  while(this.fontRenderer.getStringWidth(name + "..") > maxNameW && name.length() > 1) {
                     name = name.substring(0, name.length() - 1);
                  }

                  name = name + "..";
               }

               int nameColor = entry.hasPendingInvite ? -3372988 : -3358504;
               this.fontRenderer.drawStringWithShadow(name, (float)(rightPanelX + 6), (float)(yPos + 7), nameColor);
            }

            if (available.size() > 6) {
               String scrollText = this.playerListScroll + 1 + "-" + endIdx + "/" + available.size();
               int scrollW = this.fontRenderer.getStringWidth(scrollText);
               this.fontRenderer.drawString(scrollText, rightPanelX + (rightPanelW - scrollW) / 2, panelBot - 14, -7833440);
            }
         }
      }

   }

   protected void actionPerformed(GuiButton button) {
      switch (button.id) {
         case 0:
            this.mc.displayGuiScreen((GuiScreen)null);
            break;
         case 1:
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(0));
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(9));
            break;
         case 2:
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(5));
            break;
         case 3:
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(8));
            break;
         case 4:
            boolean amReady = this.isPlayerReady();
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(amReady ? 7 : 6));
            break;
         case 5:
            if (RaidClientData.getPendingInvitePartyId() != null) {
               RaidModInit.NETWORK.sendToServer(new PartyActionMessage(2, RaidClientData.getPendingInvitePartyId()));
               RaidClientData.clearInvite();
            }
            break;
         case 6:
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(3));
            RaidClientData.clearInvite();
            break;
         case 7:
            RaidModInit.NETWORK.sendToServer(new PartyActionMessage(9));
            break;
         case 8:
            this.noPartyTab = 0;
            break;
         case 9:
            this.noPartyTab = 1;
            break;
         default:
            if (button.id >= 400 && button.id < 500) {
               int idx = button.id - 400;
               List<RaidClientData.PendingInviteData> invites = RaidClientData.getPendingInvites();
               if (idx >= 0 && idx < invites.size()) {
                  UUID partyId = ((RaidClientData.PendingInviteData)invites.get(idx)).partyId;
                  RaidModInit.NETWORK.sendToServer(new PartyActionMessage(2, partyId));
                  RaidClientData.clearInvite(partyId);
               }
            } else if (button.id >= 500 && button.id < 600) {
               int idx = button.id - 500;
               List<RaidClientData.PendingInviteData> invites = RaidClientData.getPendingInvites();
               if (idx >= 0 && idx < invites.size()) {
                  UUID partyId = ((RaidClientData.PendingInviteData)invites.get(idx)).partyId;
                  RaidModInit.NETWORK.sendToServer(new PartyActionMessage(3, partyId));
                  RaidClientData.clearInvite(partyId);
               }
            } else if (button.id >= 100 && button.id < 200) {
               int idx = button.id - 100;
               List<PartyPlayerListMessage.PlayerEntry> available = RaidClientData.getAvailablePlayers();
               if (idx >= 0 && idx < available.size()) {
                  PartyPlayerListMessage.PlayerEntry entry = (PartyPlayerListMessage.PlayerEntry)available.get(idx);
                  if (entry.hasPendingInvite) {
                     RaidModInit.NETWORK.sendToServer(new PartyActionMessage(11, entry.name));
                  } else {
                     RaidModInit.NETWORK.sendToServer(new PartyActionMessage(1, entry.name));
                  }

                  RaidModInit.NETWORK.sendToServer(new PartyActionMessage(9));
               }
            } else if (button.id >= 200 && button.id < 300) {
               int idx = button.id - 200;
               List<RaidPartyDataMessage.PartyMemberData> members = RaidClientData.getPartyMembers();
               if (idx >= 0 && idx < members.size()) {
                  UUID targetUUID = ((RaidPartyDataMessage.PartyMemberData)members.get(idx)).id;
                  RaidModInit.NETWORK.sendToServer(new PartyActionMessage(4, targetUUID));
               }
            } else if (button.id >= 300) {
               int idx = button.id - 300;
               List<RaidPartyDataMessage.PartyMemberData> members = RaidClientData.getPartyMembers();
               if (idx >= 0 && idx < members.size()) {
                  UUID targetUUID = ((RaidPartyDataMessage.PartyMemberData)members.get(idx)).id;
                  RaidModInit.NETWORK.sendToServer(new PartyActionMessage(10, targetUUID));
               }
            }
      }

   }

   public void updateScreen() {
      super.updateScreen();
      this.animationTick += 0.1F;
      this.rebuildButtons();
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int scroll = Mouse.getEventDWheel();
      if (scroll != 0) {
         if (!RaidClientData.hasParty() && this.noPartyTab == 1) {
            List<RaidClientData.PendingInviteData> invites = RaidClientData.getPendingInvites();
            if (scroll < 0) {
               if (this.inviteListScroll + 6 < invites.size()) {
                  ++this.inviteListScroll;
               }
            } else if (this.inviteListScroll > 0) {
               --this.inviteListScroll;
            }
         } else {
            List<PartyPlayerListMessage.PlayerEntry> available = RaidClientData.getAvailablePlayers();
            if (scroll < 0) {
               if (this.playerListScroll + 6 < available.size()) {
                  ++this.playerListScroll;
               }
            } else if (this.playerListScroll > 0) {
               --this.playerListScroll;
            }
         }
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private void drawPanelWithLighting(int x, int y, int w, int h) {
      this.drawGradientRect(x, y, x + w, y + h, -869391296, -870969314);
      drawRect(x + 1, y + 1, x + w - 1, y + 2, 872415231);
      drawRect(x + 1, y + 1, x + 2, y + h - 1, 587202559);
      drawRect(x + 1, y + h - 2, x + w - 1, y + h - 1, 1426063360);
      drawRect(x + w - 2, y + 1, x + w - 1, y + h - 1, 855638016);
      drawRect(x, y, x + w, y + 1, -11913112);
      drawRect(x, y + h - 1, x + w, y + h, -15069144);
      drawRect(x, y, x + 1, y + h, -11913112);
      drawRect(x + w - 1, y, x + w, y + h, -15069144);
   }

   private void drawBorderWithShading(int x, int y, int w, int h, int color, int thickness) {
      int bright = brightenColor(color, 35) | -16777216;
      int dark = darkenColor(color, 35) | -16777216;
      drawRect(x, y, x + w, y + thickness, bright);
      drawRect(x, y, x + thickness, y + h, bright);
      drawRect(x, y + h - thickness, x + w, y + h, dark);
      drawRect(x + w - thickness, y, x + w, y + h, dark);
   }

   private void drawDiamond(int cx, int cy, int size, int color) {
      drawRect(cx - 1, cy - size, cx + 1, cy - size + 2, color);
      drawRect(cx - 2, cy - size + 2, cx + 2, cy + size - 2, color);
      drawRect(cx - 1, cy + size - 2, cx + 1, cy + size, color);
   }

   private void drawShieldCorners() {
      int size = 10;
      int offset = 8;
      int bright = brightenColor(7816379, 25) | -16777216;
      int dark = darkenColor(7816379, 25) | -16777216;
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + size, this.guiTop + offset + 2, bright);
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + 2, this.guiTop + offset + size, bright);
      drawRect(this.guiLeft + offset + 2, this.guiTop + offset + 2, this.guiLeft + offset + 4, this.guiTop + offset + 4, bright);
      drawRect(this.guiLeft + 320 - offset - size, this.guiTop + offset, this.guiLeft + 320 - offset, this.guiTop + offset + 2, bright);
      drawRect(this.guiLeft + 320 - offset - 2, this.guiTop + offset, this.guiLeft + 320 - offset, this.guiTop + offset + size, dark);
      drawRect(this.guiLeft + 320 - offset - 4, this.guiTop + offset + 2, this.guiLeft + 320 - offset - 2, this.guiTop + offset + 4, bright);
      drawRect(this.guiLeft + offset, this.guiTop + 260 - offset - 2, this.guiLeft + offset + size, this.guiTop + 260 - offset, dark);
      drawRect(this.guiLeft + offset, this.guiTop + 260 - offset - size, this.guiLeft + offset + 2, this.guiTop + 260 - offset, bright);
      drawRect(this.guiLeft + offset + 2, this.guiTop + 260 - offset - 4, this.guiLeft + offset + 4, this.guiTop + 260 - offset - 2, dark);
      drawRect(this.guiLeft + 320 - offset - size, this.guiTop + 260 - offset - 2, this.guiLeft + 320 - offset, this.guiTop + 260 - offset, dark);
      drawRect(this.guiLeft + 320 - offset - 2, this.guiTop + 260 - offset - size, this.guiLeft + 320 - offset, this.guiTop + 260 - offset, dark);
      drawRect(this.guiLeft + 320 - offset - 4, this.guiTop + 260 - offset - 4, this.guiLeft + 320 - offset - 2, this.guiTop + 260 - offset - 2, dark);
   }

   private static int brightenColor(int color, int amount) {
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return r << 16 | g << 8 | b;
   }

   private static int darkenColor(int color, int amount) {
      int r = Math.max(0, (color >> 16 & 255) - amount);
      int g = Math.max(0, (color >> 8 & 255) - amount);
      int b = Math.max(0, (color & 255) - amount);
      return r << 16 | g << 8 | b;
   }
}
