package net.luck.narutoaddon.StatMenu;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NinjaStatsUtils {

    public static class NinjaData {
        public int totalStr, totalAgi;
        public String relText, kekText, dojText;
        public String taiMode, senMode, sumMode;
        public String sixPathsState;
        public String medicalState;
        public float hp, maxHp;
    }

    public static NinjaData scanPlayer(EntityPlayer player) {
        NinjaData data = new NinjaData();
        INinjaStats stats = player.getCapability(NinjaStatsProvider.NINJA_STATS, null);
        if (stats == null) return data;

        data.totalStr = stats.getStrength() + (player.isPotionActive(MobEffects.STRENGTH) ? player.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier() + 1 : 0);
        data.totalAgi = stats.getAgility() + (player.isPotionActive(MobEffects.SPEED) ? player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1 : 0);
        data.hp = player.getHealth();
        data.maxHp = player.getMaxHealth();

        Set<String> foundReleases = new LinkedHashSet<>();
        Set<String> foundKekkei = new LinkedHashSet<>();
        Set<String> foundDojutsu = new LinkedHashSet<>();

        data.taiMode = "None";
        data.sumMode = "None";
        data.sixPathsState = "No";
        data.medicalState = "No";

        boolean hasSixPaths = false;
        boolean hasEightGates = false; // Semplificato: rileva solo presenza
        String baseSage = "";

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            ResourceLocation res = stack.getItem().getRegistryName();
            if (res == null) continue;
            String id = res.getResourcePath().toLowerCase();

            List<String> rawTooltip = stack.getTooltip(player, ITooltipFlag.TooltipFlags.NORMAL);
            List<String> safeTooltip = new ArrayList<>();
            if (rawTooltip.size() > 1) {
                for (int j = 1; j < rawTooltip.size(); j++) {
                    safeTooltip.add(TextFormatting.getTextWithoutFormattingCodes(rawTooltip.get(j)).toLowerCase());
                }
            }

            // 1. RELEASES
            if (id.contains("purple_lightning")) foundReleases.add("P. Light");
            else if (id.contains("raiton") || id.contains("lightning_rel")) foundReleases.add("Light");
            if (id.contains("katon") || id.contains("fire_rel")) foundReleases.add("Fire");
            if (id.contains("suiton") || id.contains("water_rel")) foundReleases.add("Water");
            if (id.contains("doton") || id.contains("earth_rel")) foundReleases.add("Earth");
            if (id.contains("fuuton") || id.contains("wind_rel")) foundReleases.add("Wind");

            if (id.contains("inyon") || id.contains("yin_yang")) foundReleases.add("Yin-Yang");
            else if (id.contains("inton") || id.contains("yin_rel")) foundReleases.add("Yin");
            else if (id.contains("yoton") && !id.contains("yooton")) foundReleases.add("Yang");

            // 2. KEKKEI GENKAI
            if (id.contains("yooton") || id.contains("lava_")) foundKekkei.add("Lava");
            if (id.contains("nara") || id.contains("shadow")) foundKekkei.add("Shadow");
            if (id.contains("mokuton") || id.contains("wood_")) foundKekkei.add("Wood");
            if (id.contains("hyoton") || id.contains("ice_")) foundKekkei.add("Ice");
            if (id.contains("futton") || id.contains("boil_")) foundKekkei.add("Boil");
            if (id.contains("shakuton") || id.contains("scorch_")) foundKekkei.add("Scorch");
            if (id.contains("jinton") && !id.contains("swift")) foundKekkei.add("Dust");
            if (id.contains("ranton") || id.contains("storm_")) foundKekkei.add("Storm");
            if (id.contains("bakuton") || id.contains("explosion_")) foundKekkei.add("Explosion");
            if (id.contains("jidon") || id.contains("magnet_")) foundKekkei.add("Magnet");
            if (id.contains("shoton") || id.contains("crystal_")) foundKekkei.add("Crystal");
            if (id.contains("deiton") || id.contains("mud_")) foundKekkei.add("Mud");
            if (id.contains("kinton") || id.contains("steel_")) foundKekkei.add("Steel");
            if (id.contains("meiton") || id.contains("dark_")) foundKekkei.add("Dark");
            if (id.contains("jinton_swift") || id.contains("swift_")) foundKekkei.add("Swift");

            // 3. DOJUTSU
            if (id.contains("sharingan") || id.contains("mangekyo")) {
                String owner = extractOwner(id);
                if (id.contains("eternal") || id.contains("ems")) foundDojutsu.add("EMS" + owner);
                else foundDojutsu.add("MS" + owner);
            } else if (id.contains("rinnegan")) {
                foundDojutsu.add(id.contains("sasuke") ? "Rinne-Sharingan" : "Rinnegan");
            } else if (id.contains("tenseigan")) {
                foundDojutsu.add(id.contains("chakra_mode") ? "Tenseigan CM" : "Tenseigan");
            } else if (id.contains("byakugan")) foundDojutsu.add("Byakugan");
            else if (id.contains("jougan")) foundDojutsu.add("Jougan");
            else if (id.contains("ketsuryugan")) foundDojutsu.add("Ketsuryugan");

            // 4. TAIJUTSU & 8 GATES (Solo rilevamento presenza)
            if (id.contains("gate_") || id.contains("hachimon") || id.contains("eightgates")) {
                hasEightGates = true;
            }

            // 5. SIX PATHS & MEDICAL
            if (id.contains("six_path_senjutsu") || id.contains("rikudo") || id.contains("hagoromo") || id.contains("truth_seeking")) {
                hasSixPaths = true;
                data.sixPathsState = "Yes";
            }
            if (id.contains("medical") || id.contains("iryo") || id.contains("byakugou") || id.contains("mystical_palm")) {
                data.medicalState = "Yes";
            }

            // 6. SENJUTSU
            if (id.contains("sage") || id.contains("senjutsu") || id.contains("sennin")) {
                String foundType = scanForType(id, safeTooltip);
                if (!foundType.equals("None")) baseSage = foundType;
            }

            // 7. SUMMONS
            if (id.contains("summon") || id.contains("contract")) {
                String foundType = scanForType(id, safeTooltip);
                if (!foundType.equals("None")) data.sumMode = foundType;
            }
        }

        // Logica richiesta: Taijutsu mostra 8 Gates se presenti e se NON è in Six Paths
        if (hasEightGates && !hasSixPaths) {
            data.taiMode = "8 Gates";
        } else if (hasEightGates && hasSixPaths) {
            data.taiMode = "8 Gates"; // Lo scriviamo comunque se presente
        }

        data.senMode = hasSixPaths ? (baseSage.isEmpty() ? "Six Paths" : "6P " + baseSage) : (baseSage.isEmpty() ? "None" : baseSage);
        data.relText = foundReleases.isEmpty() ? "None" : String.join(", ", foundReleases);
        data.kekText = foundKekkei.isEmpty() ? "None" : String.join(", ", foundKekkei);
        data.dojText = foundDojutsu.isEmpty() ? "None" : String.join(", ", foundDojutsu);

        return data;
    }

    private static String scanForType(String id, List<String> safeTooltip) {
        String[] keys = {"toad", "snake", "slug", "katsuyu", "monkey", "enma", "crow", "hashirama", "wood", "rashomon", "dog"};
        String[] results = {"Toad", "Snake", "Slug", "Slug", "Monkey", "Monkey", "Crow", "Wood", "Wood", "Rashomon", "Dog"};
        for (int i = 0; i < keys.length; i++) {
            if (id.contains(keys[i])) return results[i];
            for (String line : safeTooltip) { if (line.contains(keys[i])) return results[i]; }
        }
        return "None";
    }

    private static String extractOwner(String id) {
        if (id.contains("itachi")) return " (Ita)";
        if (id.contains("obito") || id.contains("kamui")) return " (Obi)";
        if (id.contains("sasuke")) return " (Sas)";
        if (id.contains("shisui")) return " (Shi)";
        if (id.contains("madara")) return " (Mad)";
        return "";
    }
}