package net.luck.narutoaddon.StatMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;

public class NinjaStatsHUD extends Gui {

    private final Minecraft mc = Minecraft.getMinecraft();
    public static boolean isVisible = false;

    public NinjaStatsHUD() {}

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!isVisible || event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;

        if (mc.player == null) return;
        INinjaStats stats = mc.player.getCapability(NinjaStatsProvider.NINJA_STATS, null);
        if (stats == null) return;

        // --- UI CONFIG ---
        String title = "--- NINJA STATUS ---";
        int boxWidth = mc.fontRenderer.getStringWidth(title) + 10;
        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight();
        int x = width - boxWidth - 10;
        int y = (int)(height * 0.30);

        // --- STATS CALCULATION ---
        int totalStr = stats.getStrength() + (mc.player.isPotionActive(MobEffects.STRENGTH) ? mc.player.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier() + 1 : 0);
        int totalAgi = stats.getAgility() + (mc.player.isPotionActive(MobEffects.SPEED) ? mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1 : 0);

        // --- STORAGE ---
        StringBuilder relSB = new StringBuilder(); int relC = 0;
        StringBuilder dojSB = new StringBuilder(); int dojC = 0;
        StringBuilder kekSB = new StringBuilder(); int kekC = 0;
        String taiMode = "None", senMode = "None", sumMode = "None";
        boolean hasSixPaths = false;
        String baseSage = "";

        List<String> foundKekkei = new ArrayList<>();
        List<String> foundReleases = new ArrayList<>();

        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            String name = TextFormatting.getTextWithoutFormattingCodes(stack.getDisplayName()).toLowerCase();

            // 1. SIX PATHS CHECK
            if (name.contains("six path") || name.contains("rikudo")) hasSixPaths = true;

            List<String> fullTip = new ArrayList<>();
            stack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL).forEach(l ->
                    fullTip.add(TextFormatting.getTextWithoutFormattingCodes(l).toLowerCase()));

            // 2. RELEASES & KEKKEI
            if (!name.contains("six path") && !name.contains("rikudo")) {

                // RELEASES (Purple Lightning -> "P Light ")
                String[] rT = {"fire","water","earth","wind","lightning","purple lightning","yin","yang"};
                String[] rD = {"Fire ","Water ","Earth ","Wind ","Light ","P Light ","Yin ","Yang "};
                for(int j=0; j<rT.length; j++) {
                    if (name.contains(rT[j]) && !foundReleases.contains(rD[j])) {
                        relSB.append(rD[j]); foundReleases.add(rD[j]); relC++;
                    }
                }

                // KEKKEI GENGAI (Rimosso Purple Lightning/Shiden da qui)
                String[] kT = {"mokuton","wood","hyoton","ice","yoton","lava","futton","boil","shakuton","scorch","jinton","dust","magnet","shoton","crystal","bakuton","explosion","ranton","storm","shikotsumyaku","bone","shadow"};
                String[] kD = {"Wood ","Wood ","Ice ","Ice ","Lava ","Lava ","Boil ","Boil ","Scorch ","Scorch ","Dust ","Dust ","Magn ","Cryst ","Cryst ","Expl ","Expl ","Storm ","Storm ","Bone ","Bone ","Shadow "};
                for(int k=0; k<kT.length; k++) {
                    if(name.contains(kT[k]) && !foundKekkei.contains(kD[k])) {
                        kekSB.append(kD[k]); foundKekkei.add(kD[k]); kekC++;
                    }
                }
            }

            // 3. TAIJUTSU
            if (name.contains("gate") || name.contains("hachimon")) taiMode = "8 Gates";

            // 4. SUMMONING
            if (name.contains("summon") || name.contains("contract")) {
                for (String s : fullTip) {
                    if (s.contains(">")) {
                        if (s.contains("toad")) sumMode = "Toad";
                        else if (s.contains("snake")) sumMode = "Snake";
                        else if (s.contains("katsuyu") || s.contains("slug")) sumMode = "Katsuyu";
                        else if (s.contains("monkey") || s.contains("enma")) sumMode = "Monkey King";
                        else if (s.contains("ninken") || s.contains("dog")) sumMode = "Dogs";
                        else if (s.contains("crow")) sumMode = "Crow";
                        if (!sumMode.equals("None")) break;
                    }
                }
            }

            // 5. DOJUTSU
            String dEntry = "";
            if (name.contains("sharingan") || fullTip.stream().anyMatch(s -> s.contains("sharingan"))) {
                if (name.contains("eternal") || fullTip.stream().anyMatch(s -> s.contains("eternal"))) dEntry = "EMS ";
                else if (name.contains("mangekyo") || fullTip.stream().anyMatch(s -> s.contains("mangekyo"))) {
                    if (name.contains("kamui") || fullTip.stream().anyMatch(s -> s.contains("kamui"))) dEntry = "Kamui-MS ";
                    else if (name.contains("amaterasu") || fullTip.stream().anyMatch(s -> s.contains("amaterasu"))) dEntry = "Amat-MS ";
                    else dEntry = "MS ";
                } else dEntry = "Shar ";
            }
            else if (name.contains("rinnegan") || fullTip.stream().anyMatch(s -> s.contains("rinnegan"))) dEntry = "Rinn ";
            else if (name.contains("tenseigan") || fullTip.stream().anyMatch(s -> s.contains("tenseigan"))) {
                dEntry = (name.contains("mode") || name.contains("chakra")) ? "Tens-CM " : "Tens ";
            }
            else if (name.contains("byakugan") || fullTip.stream().anyMatch(s -> s.contains("byakugan"))) dEntry = "Byak ";

            if (!dEntry.isEmpty() && !dojSB.toString().contains(dEntry)) { dojSB.append(dEntry); dojC++; }

            // 6. SENJUTSU
            if (name.contains("senjutsu") || name.contains("sage mode")) {
                for (String s : fullTip) {
                    if (s.contains("slug") || s.contains("katsuyu")) baseSage = "Slug";
                    else if (s.contains("snake")) baseSage = "Snake";
                    else if (s.contains("toad")) baseSage = "Toad";
                    else if (s.contains("wood") || s.contains("hashirama")) baseSage = "Wood";
                    if (!baseSage.isEmpty()) break;
                }
            }
        }

        // --- FINAL SENJUTSU LOGIC ---
        if (!baseSage.isEmpty() && hasSixPaths) senMode = baseSage + " / Six Paths";
        else if (!baseSage.isEmpty()) senMode = baseSage + " Sage";
        else if (hasSixPaths) senMode = "Six Paths";

        // --- RENDERING ---
        int wrapWidth = boxWidth - 4;
        String relText = "Rel: " + TextFormatting.AQUA + (relC > 0 ? relSB.toString().trim() : "None");
        String kekText = "KG: " + TextFormatting.GOLD + (kekC > 0 ? kekSB.toString().trim() : "None");
        String dojText = "Doj: " + TextFormatting.LIGHT_PURPLE + (dojC > 0 ? dojSB.toString().trim() : "None");

        int relLines = mc.fontRenderer.listFormattedStringToWidth(relText, wrapWidth).size();
        int kekLines = mc.fontRenderer.listFormattedStringToWidth(kekText, wrapWidth).size();
        int dojLines = mc.fontRenderer.listFormattedStringToWidth(dojText, wrapWidth).size();

        int dynamicHeight = 75 + (relLines * 10) + (kekLines * 10) + (dojLines * 10) + 10;
        drawRect(x - 5, y - 5, x + boxWidth + 5, y + dynamicHeight, 0xAA000000);

        mc.fontRenderer.drawStringWithShadow(TextFormatting.GOLD + title, x + 5, y, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("STR: " + totalStr + "  AGI: " + totalAgi, x + 2, y + 15, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("Tai: " + TextFormatting.GREEN + taiMode, x + 2, y + 30, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("Sen: " + TextFormatting.YELLOW + senMode, x + 2, y + 40, 0xFFFFFF);
        mc.fontRenderer.drawStringWithShadow("Sum: " + TextFormatting.WHITE + sumMode, x + 2, y + 50, 0xFFFFFF);

        int currentY = y + 65;
        mc.fontRenderer.drawSplitString(relText, x + 2, currentY, wrapWidth, 0xFFFFFF);
        currentY += (relLines * 10) + 2;
        mc.fontRenderer.drawSplitString(kekText, x + 2, currentY, wrapWidth, 0xFFFFFF);
        currentY += (kekLines * 10) + 2;
        mc.fontRenderer.drawSplitString(dojText, x + 2, currentY, wrapWidth, 0xFFFFFF);
        currentY += (dojLines * 10) + 2;

        mc.fontRenderer.drawStringWithShadow("HP: " + TextFormatting.RED + (int)mc.player.getHealth() + "/" + (int)mc.player.getMaxHealth(), x + 2, currentY + 3, 0xFFFFFF);
    }
}