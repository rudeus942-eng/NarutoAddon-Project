package net.luck.narutoaddon.client.gui;


//import net.luck.narutoaddon.OtherCode.gui.*;
//import net.luck.narutoaddon.OtherCode.jutsu.EntityShrineHeianEraTransformation;
import net.luck.narutoaddon.StatMenu.NinjaStatsUtils;
import net.luck.narutoaddon.StatMenu.NinjaStatsUtils.NinjaData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.init.SoundEvents;

import java.io.IOException;

public class GuiStatMenu extends GuiScreen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("narutoaddon", "textures/menu/menu_bg.png");
    private static final ResourceLocation SECONDARY_TEXTURE = new ResourceLocation("narutoaddon", "textures/menu/kagecraft_ui_32bit.png");

    private int currentTab = -1;
    private final int xSize = 400;
    private final int ySize = 230;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int guiLeft = (this.width - xSize) / 2;
        int guiTop = (this.height - ySize) / 2;

        this.buttonList.add(new GuiInvisibleButton(10, guiLeft + 6, guiTop + 4, 55, 18));

        int btnW = 122;
        int btnH = 58;
        int gapX = 130;
        int gapY = 62;
        int startX = guiLeft + 8;
        int startY = guiTop + 28;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int id = col + (row * 3);
                this.buttonList.add(new GuiInvisibleButton(id, startX + (col * gapX), startY + (row * gapY), btnW, btnH));
            }
        }
    }

    /*@Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 10) {
            currentTab = (currentTab != -1) ? -1 : 10;
        }
        else if (currentTab == -1) {
            switch (button.id) {
                case 0: this.mc.displayGuiScreen(new GuiRankedMenu()); break;
                case 1: this.mc.displayGuiScreen(new GuiRankedLeaderboard()); break;
                case 2: this.mc.displayGuiScreen(new GuiRankedStats()); break;
                case 3: this.mc.displayGuiScreen(new GuiRankedTitles()); break;
                //case 4: this.activateSukuna(); break; // Ora Sukuna è collegato al tasto ID 4
                default: this.currentTab = button.id; break;
            }
        }
        this.mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F);
    }*/

    /*public void activateSukuna() {
        if (EntityShrineHeianEraTransformation.isActive(this.mc.player)) {
            luckAddonAddon.PACKET_HANDLER.sendToServer(new EntityShrineHeianEraTransformation.DeactivateMessage());
        } else {
            luckAddonAddon.PACKET_HANDLER.sendToServer(new EntityShrineHeianEraTransformation.ActivateMessage());
        }
        this.mc.displayGuiScreen(null); // Chiude la GUI per vedere l'effetto
    }
     */

    //public void openSeasonMenu() { this.mc.displayGuiScreen(new GuiRankedSeason()); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int guiLeft = (this.width - xSize) / 2;
        int guiTop = (this.height - ySize) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawFullImageStretch(guiLeft, guiTop, xSize, ySize);

        if (currentTab == -1) {
            this.fontRenderer.drawStringWithShadow("RANKED MENU", guiLeft + 35, guiTop + 55, 0xFFFFFF);
            this.fontRenderer.drawStringWithShadow("LEADERBOARD", guiLeft + 165, guiTop + 55, 0xFFFFFF);
            this.fontRenderer.drawStringWithShadow("MY RANKED", guiLeft + 300, guiTop + 55, 0xFFFFFF);
            this.fontRenderer.drawStringWithShadow("TITLES", guiLeft + 50, guiTop + 115, 0xFFFFFF);
            this.fontRenderer.drawStringWithShadow("SUKUNA MODE", guiLeft + 170, guiTop + 115, 0xFFFFFF); // Etichetta Sukuna
        }

        for (GuiButton button : this.buttonList) {
            button.visible = (button.id == 10 || currentTab == -1);
        }

        if (currentTab == 10) {
            this.mc.getTextureManager().bindTexture(SECONDARY_TEXTURE);
            drawFullImageStretch(guiLeft, guiTop, xSize, ySize);
            renderStatsContent(guiLeft, guiTop);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderStatsContent(int x, int y) {
        NinjaData data = NinjaStatsUtils.scanPlayer(this.mc.player);
        int gray = 0xAAAAAA;
        int white = 0xFFFFFF;
        int gold = 0xFFD400;

        int currentYLeft = y + 35;
        int col1 = x + 25;

        currentYLeft = drawDynamicSection("Releases", data.relText, col1, currentYLeft, gray, white);
        currentYLeft += 6;
        currentYLeft = drawDynamicSection("Kekkei", data.kekText, col1, currentYLeft, gray, white);
        currentYLeft += 6;
        currentYLeft = drawDynamicSection("Dojutsu", data.dojText, col1, currentYLeft, gray, white);

        int col2 = x + 215;
        int currentYRight = y + 35;
        int spacing = 15;

        this.fontRenderer.drawStringWithShadow("Strength: ", col2, currentYRight, gray);
        this.fontRenderer.drawString(String.valueOf(data.totalStr), col2 + 70, currentYRight, 0xFF5555);
        currentYRight += spacing;
        this.fontRenderer.drawStringWithShadow("Agility: ", col2, currentYRight, gray);
        this.fontRenderer.drawString(String.valueOf(data.totalAgi), col2 + 70, currentYRight, 0x55FF55);
        currentYRight += spacing;
        this.fontRenderer.drawStringWithShadow("Taijutsu: ", col2, currentYRight, gray);
        this.fontRenderer.drawString(data.taiMode, col2 + 70, currentYRight, white);
        currentYRight += spacing;
        this.fontRenderer.drawStringWithShadow("Senjutsu: ", col2, currentYRight, gray);
        this.fontRenderer.drawString(data.senMode, col2 + 70, currentYRight, 0x55FF55);
        currentYRight += spacing;
        this.fontRenderer.drawStringWithShadow("Vitals: ", col2, currentYRight, gray);
        this.fontRenderer.drawString((int)data.hp + " / " + (int)data.maxHp, col2 + 70, currentYRight, 0xFF5555);
        currentYRight += spacing;

        int finalY = Math.max(currentYLeft, currentYRight) + 10;
        drawRect(x + 20, finalY, x + xSize - 20, finalY + 1, 0x33FFFFFF);

        int statusY = finalY + 10;
        this.fontRenderer.drawStringWithShadow("Medical: ", x + 25, statusY, gray);
        this.fontRenderer.drawString(data.medicalState, x + 85, statusY, 0x55FFFF);
        this.fontRenderer.drawStringWithShadow("Six Paths: ", x + 150, statusY, gray);
        this.fontRenderer.drawString(data.sixPathsState, x + 210, statusY, gold);
        this.fontRenderer.drawStringWithShadow("Summon: ", x + 275, statusY, gray);
        this.fontRenderer.drawString(data.sumMode, x + 330, statusY, white);
    }

    private int drawDynamicSection(String label, String content, int x, int y, int labelColor, int contentColor) {
        this.fontRenderer.drawStringWithShadow(label + ":", x, y, labelColor);
        String[] parts = content.split(", ");
        int currentX = x + 15;
        int currentY = y + 10;
        for (int i = 0; i < parts.length; i++) {
            String element = parts[i] + (i < parts.length - 1 ? ", " : "");
            if (currentX + this.fontRenderer.getStringWidth(element) > (x + 180)) {
                currentX = x + 15;
                currentY += 10;
            }
            this.fontRenderer.drawString(element, currentX, currentY, contentColor);
            currentX += this.fontRenderer.getStringWidth(element);
        }
        return currentY + 12;
    }

    public void drawFullImageStretch(int x, int y, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)x, (double)(y + height), (double)this.zLevel).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)(x + width), (double)y, (double)this.zLevel).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos((double)x, (double)y, (double)this.zLevel).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private static class GuiInvisibleButton extends GuiButton {
        public GuiInvisibleButton(int id, int x, int y, int w, int h) { super(id, x, y, w, h, ""); }
        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, float partialTicks) {}
    }
}