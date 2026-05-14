package net.luck.narutoaddon.OtherCode.endgame.bingo;

import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;

import java.util.*;

public class BingoTargetRegistry {
   private static final Map<String, BingoTarget> targets = new LinkedHashMap();
   private static final List<BingoTarget> chuninTargets = new ArrayList();
   private static final List<BingoTarget> joninTargets = new ArrayList();
   private static final List<BingoTarget> anbuTargets = new ArrayList();
   private static boolean initialized = false;

   public static void init() {
      if (!initialized) {
         initialized = true;
         String[] chuninDecoys = new String[]{"bingo_decoy_1", "bingo_decoy_2"};
         String[] joninDecoys = new String[]{"bingo_decoy_3", "bingo_decoy_4"};
         String[] anbuDecoys = new String[]{"bingo_decoy_5", "bingo_decoy_6"};
         String[] chuninAmbush = new String[]{"bingo_ambush_1"};
         String[] joninAmbush = new String[]{"bingo_ambush_2"};
         String[] anbuAmbush = new String[]{"bingo_ambush_3"};
         register(new BingoTarget("bingo_chunin_1", "Rogue Genin", "Lurking in Fire Country forests", "bingo_chunin_1", OutpostDifficultyTier.CHUNIN, -1200, -600, -400, 200, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 200, 400));
         register(new BingoTarget("bingo_chunin_2", "Bandit Captain", "Leading raids on trade routes", "bingo_chunin_2", OutpostDifficultyTier.CHUNIN, -800, -200, 0, 600, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 250, 400));
         register(new BingoTarget("bingo_chunin_3", "Missing-Nin Scout", "Spotted near the Sound border", "bingo_chunin_3", OutpostDifficultyTier.CHUNIN, -1100, -500, -2100, -1600, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 200, 350));
         register(new BingoTarget("bingo_chunin_4", "Hired Mercenary", "Hired by Rain country criminals", "bingo_chunin_4", OutpostDifficultyTier.CHUNIN, -2400, -1800, -200, 400, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 250, 400));
         register(new BingoTarget("bingo_chunin_5", "River Pirate", "Attacks boats on the river", "bingo_chunin_5", OutpostDifficultyTier.CHUNIN, -1600, -1000, 200, 800, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 200, 350));
         register(new BingoTarget("bingo_chunin_6", "Desert Raider", "Raids caravans near Sunagakure", "bingo_chunin_6", OutpostDifficultyTier.CHUNIN, -3200, -2400, 200, 1000, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 250, 400));
         register(new BingoTarget("bingo_chunin_7", "Mountain Bandit", "Hides in Earth Country caves", "bingo_chunin_7", OutpostDifficultyTier.CHUNIN, -2800, -2200, -2800, -2200, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 200, 350));
         register(new BingoTarget("bingo_chunin_8", "Forest Stalker", "Hunts travelers in dense woods", "bingo_chunin_8", OutpostDifficultyTier.CHUNIN, -1400, -800, -1600, -1000, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 250, 400));
         register(new BingoTarget("bingo_chunin_9", "Road Thief", "Ambushes on the main road", "bingo_chunin_9", OutpostDifficultyTier.CHUNIN, 0, 600, -600, 0, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 200, 350));
         register(new BingoTarget("bingo_chunin_10", "Escaped Prisoner", "Escaped from Cloud's prison", "bingo_chunin_10", OutpostDifficultyTier.CHUNIN, 1400, 2000, -2000, -1400, chuninDecoys, chuninAmbush, 0.6, (double)0.25F, 250, 400));
         register(new BingoTarget("bingo_jonin_1", "Rogue Chunin", "Defected after a failed mission in Fire Country", "bingo_jonin_1", OutpostDifficultyTier.JONIN, -1400, -600, -1200, -400, joninDecoys, joninAmbush, 0.6, (double)0.25F, 400, 600));
         register(new BingoTarget("bingo_jonin_2", "Shadow Broker", "Selling secrets near the Rain border", "bingo_jonin_2", OutpostDifficultyTier.JONIN, -2500, -1900, -800, -200, joninDecoys, joninAmbush, 0.6, (double)0.25F, 450, 650));
         register(new BingoTarget("bingo_jonin_3", "Poison Master", "Operates a hidden lab in Wind Country", "bingo_jonin_3", OutpostDifficultyTier.JONIN, -3000, -2300, 400, 1100, joninDecoys, joninAmbush, 0.6, (double)0.25F, 400, 600));
         register(new BingoTarget("bingo_jonin_4", "Blade Dancer", "Terrorizing villages near the Stone border", "bingo_jonin_4", OutpostDifficultyTier.JONIN, -2700, -2100, -2600, -2000, joninDecoys, joninAmbush, 0.6, (double)0.25F, 450, 700));
         register(new BingoTarget("bingo_jonin_5", "Curse Seal Bearer", "A former Sound experiment gone rogue", "bingo_jonin_5", OutpostDifficultyTier.JONIN, -1200, -400, -2200, -1500, joninDecoys, joninAmbush, 0.6, (double)0.25F, 400, 650));
         register(new BingoTarget("bingo_jonin_6", "Storm Raider", "Strikes during thunderstorms near Cloud", "bingo_jonin_6", OutpostDifficultyTier.JONIN, 1500, 2200, -3200, -2600, joninDecoys, joninAmbush, 0.6, (double)0.25F, 450, 700));
         register(new BingoTarget("bingo_jonin_7", "Silent Assassin", "Eliminates targets in Mist territory", "bingo_jonin_7", OutpostDifficultyTier.JONIN, 3500, 4100, -2400, -1900, joninDecoys, joninAmbush, 0.6, (double)0.25F, 400, 650));
         register(new BingoTarget("bingo_jonin_8", "Weapons Smuggler", "Running arms through neutral territory", "bingo_jonin_8", OutpostDifficultyTier.JONIN, -600, 0, -2000, -1400, joninDecoys, joninAmbush, 0.6, (double)0.25F, 450, 650));
         register(new BingoTarget("bingo_jonin_9", "Puppet Master", "Uses traps and puppets near Sand's outskirts", "bingo_jonin_9", OutpostDifficultyTier.JONIN, -3200, -2500, -100, 500, joninDecoys, joninAmbush, 0.6, (double)0.25F, 400, 700));
         register(new BingoTarget("bingo_jonin_10", "Genjutsu Specialist", "Lures victims into illusions in the forest", "bingo_jonin_10", OutpostDifficultyTier.JONIN, -1200, -500, -800, -200, joninDecoys, joninAmbush, 0.6, (double)0.25F, 450, 700));
         register(new BingoTarget("bingo_kage_1", "Akatsuki Sympathizer", "A former Kage guard turned traitor", "bingo_kage_1", OutpostDifficultyTier.ANBU, -2000, -1400, -1000, -400, anbuDecoys, anbuAmbush, 0.6, (double)0.25F, 800, 1200));
         register(new BingoTarget("bingo_kage_2", "Tailed Beast Hunter", "Searching for jinchuriki across the nations", "bingo_kage_2", OutpostDifficultyTier.ANBU, -1000, -200, -2400, -1800, anbuDecoys, anbuAmbush, 0.6, (double)0.25F, 900, 1200));
         register(new BingoTarget("bingo_kage_3", "Forbidden Jutsu Scholar", "Stole forbidden scrolls from multiple villages", "bingo_kage_3", OutpostDifficultyTier.ANBU, 1600, 2400, -3000, -2400, anbuDecoys, anbuAmbush, 0.6, (double)0.25F, 800, 1100));
         register(new BingoTarget("bingo_kage_4", "Legendary Swordsman", "One of the Seven Swords wielders gone rogue", "bingo_kage_4", OutpostDifficultyTier.ANBU, 3600, 4200, -2200, -1600, anbuDecoys, anbuAmbush, 0.6, (double)0.25F, 900, 1200));
         register(new BingoTarget("bingo_kage_5", "War Criminal", "Committed atrocities during the last war", "bingo_kage_5", OutpostDifficultyTier.ANBU, -2800, -2200, -2600, -2000, anbuDecoys, anbuAmbush, 0.6, (double)0.25F, 800, 1200));
      }
   }

   private static void register(BingoTarget target) {
      targets.put(target.getTargetId(), target);
      switch (target.getTier()) {
         case CHUNIN:
            chuninTargets.add(target);
            break;
         case JONIN:
            joninTargets.add(target);
            break;
         case ANBU:
            anbuTargets.add(target);
      }

   }

   public static BingoTarget get(String targetId) {
      if (!initialized) {
         init();
      }

      return (BingoTarget)targets.get(targetId);
   }

   public static List<BingoTarget> getByTier(OutpostDifficultyTier tier) {
      if (!initialized) {
         init();
      }

      switch (tier) {
         case CHUNIN:
            return Collections.unmodifiableList(chuninTargets);
         case JONIN:
            return Collections.unmodifiableList(joninTargets);
         case ANBU:
            return Collections.unmodifiableList(anbuTargets);
         default:
            return Collections.emptyList();
      }
   }

   public static List<BingoTarget> getRandomTargets(OutpostDifficultyTier tier, int count, Random rand) {
      List<BingoTarget> pool = new ArrayList();
      pool.addAll(chuninTargets);
      if (tier.ordinal() >= OutpostDifficultyTier.JONIN.ordinal()) {
         pool.addAll(joninTargets);
      }

      if (tier.ordinal() >= OutpostDifficultyTier.ANBU.ordinal()) {
         pool.addAll(anbuTargets);
      }

      if (pool.isEmpty()) {
         return Collections.emptyList();
      } else {
         Collections.shuffle(pool, rand);
         int actual = Math.min(count, pool.size());
         return new ArrayList(pool.subList(0, actual));
      }
   }
}
