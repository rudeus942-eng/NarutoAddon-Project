package net.luck.narutoaddon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiButtonColor extends GuiButton {
    private final int buttonColor;

    public GuiButtonColor(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, int color) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.buttonColor = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            // Controlla se il mouse è sopra il bottone
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            // Disegna il rettangolo colorato (leggermente più chiaro se hoverato)
            int renderColor = this.hovered ? (buttonColor | 0x44444444) : buttonColor;

            // Sfondo nero per il bordo e poi il colore scelto
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
            drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, renderColor);

            this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, 14737632);
        }
    }
}