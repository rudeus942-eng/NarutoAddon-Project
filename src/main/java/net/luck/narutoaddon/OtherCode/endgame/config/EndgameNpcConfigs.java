package net.luck.narutoaddon.OtherCode.endgame.config;

import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;

public class EndgameNpcConfigs {
   public static void init() {
      initAkatsuki();
      initSound();
      initBingo();
      initWave();
      initWorldBoss();
   }

   private static void initAkatsuki() {
      registerTiered("itachi", "Itachi Uchiha", "inftsukaddon", "textures/itachi1.png", (double)12000.0F, (double)7.5F, (double)8.0F, 0.38, 4, "ITACHI", 1, true, true, 0.0F, 1.0F, 1.0F, -3407872, -10092544, false, "inftsukaddon:itachistyle");
      registerTiered("kisame", "Kisame Hoshigaki", "inftsukaddon", "textures/kisame1.png", (double)15000.0F, (double)10.0F, (double)10.0F, 0.42, 4, "FERAL", 3, false, true, 0.35F, 1.3F, 1.25F, -14531414, -15654315, true, "narutomod:samehada", 1.3F);
      registerTiered("deidara", "Deidara", "inftsukaddon", "textures/deidara.png", (double)10000.0F, (double)6.0F, (double)5.0F, 0.35, 3, "DEIDARA", 1, true, true, 0.0F, 1.0F, 1.0F, -2250240, -7838208, false, "inftsukaddon:deidaraboss");
      registerTiered("sasori", "Sasori", "inftsukaddon", "textures/sasori.png", (double)11000.0F, (double)8.0F, (double)6.0F, 0.33, 4, "SASORI", 0, false, true, 0.0F, 1.0F, 1.0F, -5622989, -10088175, false, "inftsukaddon:sasoriboss");
      registerTiered("hidan", "Hidan", "inftsukaddon", "textures/hidan.png", (double)13000.0F, (double)9.0F, (double)4.0F, 0.45, 4, "HIDAN", 0, false, true, 0.4F, 1.4F, 1.3F, -7864150, -12320683, false, "narutomod:scythe_hidan", 1.0F, "inftsukaddon:hidanboss");
      registerTiered("kakuzu", "Kakuzu", "inftsukaddon", "textures/kakuzu.png", (double)18000.0F, (double)8.5F, (double)14.0F, 0.3, 4, "KAKUZU", 1, false, true, 0.0F, 1.0F, 1.0F, -13408717, -15125735, false, "inftsukaddon:kakuzustyle");
      NpcConfigRegistry.register(NpcConfig.builder("kakuzu_heart_fire", "inftsukaddon:outpostenemy").displayName("Kakuzu's Heart - Fire").texture("inftsukaddon", "textures/spirit_fire.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)3000.0F).attackDamage((double)5.0F).movementSpeed((double)0.5F).combatTier(2).combatStyle("STANDARD").natureType(1).knockbackImmune(false).themeColor(-13408717).accentColor(-15125735).build());
      NpcConfigRegistry.register(NpcConfig.builder("kakuzu_heart_wind", "inftsukaddon:outpostenemy").displayName("Kakuzu's Heart - Wind").texture("inftsukaddon", "textures/spirit_wind.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)3000.0F).attackDamage((double)5.0F).movementSpeed((double)0.5F).combatTier(2).combatStyle("STANDARD").natureType(2).knockbackImmune(false).themeColor(-13408717).accentColor(-15125735).build());
      NpcConfigRegistry.register(NpcConfig.builder("kakuzu_heart_lightning", "inftsukaddon:outpostenemy").displayName("Kakuzu's Heart - Lightning").texture("inftsukaddon", "textures/spirit_lightning.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)3000.0F).attackDamage((double)5.0F).movementSpeed((double)0.5F).combatTier(2).combatStyle("STANDARD").natureType(4).knockbackImmune(false).themeColor(-13408717).accentColor(-15125735).build());
      NpcConfigRegistry.register(NpcConfig.builder("kakuzu_heart_earth", "inftsukaddon:outpostenemy").displayName("Kakuzu's Heart - Earth").texture("inftsukaddon", "textures/spirit_earth.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)3000.0F).attackDamage((double)4.0F).armor((double)12.0F).movementSpeed(0.45).combatTier(2).combatStyle("STANDARD").natureType(0).knockbackImmune(false).themeColor(-13408717).accentColor(-15125735).build());
   }

   private static void initSound() {
      registerTiered("jirobo", "Jirobo", "inftsukaddon", "textures/jirobo.png", (double)13000.0F, (double)8.0F, (double)12.0F, 0.35, 4, "JIROBO", 0, false, true, 0.3F, 1.3F, 1.2F, -7838140, -12307678, false, "inftsukaddon:jiroboboss");
      registerTiered("kidomaru", "Kidomaru", "inftsukaddon", "textures/kidomaru.png", (double)9000.0F, (double)6.5F, (double)4.0F, 0.38, 3, "KIDOMARU", 0, true, false, 0.0F, 1.0F, 1.0F, -10057677, -13417455, false, "inftsukaddon:kidomaruboss");
      registerTiered("sakon", "Sakon", "inftsukaddon", "textures/sakon.png", (double)11000.0F, (double)8.5F, (double)6.0F, 0.4, 4, "SAKON", 0, false, true, 0.5F, 1.25F, 1.35F, -11184726, -13421722, false, "inftsukaddon:sakonboss");
      registerTiered("tayuya", "Tayuya", "inftsukaddon", "textures/tayuya.png", (double)9500.0F, (double)5.5F, (double)5.0F, 0.33, 3, "TAYUYA", 0, true, true, 0.0F, 1.0F, 1.0F, -5618586, -10083789, false, "inftsukaddon:tayuyaboss");
      NpcConfigRegistry.register(NpcConfig.builder("doki_tank", "inftsukaddon:dailymissionnpc").displayName("Doki").texture("inftsukaddon", "textures/doki_1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)3000.0F).attackDamage((double)12.0F).armor((double)10.0F).movementSpeed(0.28).combatTier(2).combatStyle("STANDARD").natureType(0).hasRangedAttack(false).knockbackImmune(true).trueDamageSplit(0.4F).themeColor(-11180493).accentColor(-13417455).renderScale(1.3F).build());
      NpcConfigRegistry.register(NpcConfig.builder("doki_claw", "inftsukaddon:dailymissionnpc").displayName("Doki").texture("inftsukaddon", "textures/doki_2.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)2000.0F).attackDamage((double)7.0F).armor((double)4.0F).movementSpeed(0.38).combatTier(3).combatStyle("STANDARD").natureType(0).hasRangedAttack(false).knockbackImmune(true).trueDamageSplit(0.4F).themeColor(-11180493).accentColor(-13417455).renderScale(1.15F).build());
      NpcConfigRegistry.register(NpcConfig.builder("doki_worm", "inftsukaddon:dailymissionnpc").displayName("Doki").texture("inftsukaddon", "textures/doki_3.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)2000.0F).attackDamage((double)5.0F).armor((double)4.0F).movementSpeed(0.33).combatTier(2).combatStyle("STANDARD").natureType(0).hasRangedAttack(true).knockbackImmune(true).trueDamageSplit(0.4F).themeColor(-11180493).accentColor(-13417455).renderScale(1.15F).build());
      registerTiered("kimimaro", "Kimimaro", "inftsukaddon", "textures/kimimaro.png", (double)16000.0F, (double)11.0F, (double)10.0F, 0.4, 4, "KIMIMARO", 0, true, true, 0.3F, 1.5F, 1.4F, -2236963, -7829368, false, "narutomod:bone_sword", 1.0F, "inftsukaddon:kimimaroboss");
      NpcConfigRegistry.register(NpcConfig.builder("doki_ghost_1", "inftsukaddon:dailymissionnpc").displayName("Doki Spirit").texture("inftsukaddon", "textures/sound_shinobi.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)2000.0F).attackDamage((double)4.0F).movementSpeed(0.35).combatTier(1).combatStyle("STANDARD").ghostAlpha(0.5F).build());
      NpcConfigRegistry.register(NpcConfig.builder("doki_ghost_2", "inftsukaddon:dailymissionnpc").displayName("Doki Spirit").texture("inftsukaddon", "textures/sound_shinobi.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)2000.0F).attackDamage((double)4.5F).movementSpeed(0.38).combatTier(1).combatStyle("STANDARD").ghostAlpha(0.5F).build());
      NpcConfigRegistry.register(NpcConfig.builder("doki_ghost_3", "inftsukaddon:dailymissionnpc").displayName("Doki Spirit").texture("inftsukaddon", "textures/sound_shinobi.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)2500.0F).attackDamage((double)5.0F).movementSpeed(0.4).combatTier(2).combatStyle("STANDARD").ghostAlpha(0.5F).build());
      NpcConfigRegistry.register(NpcConfig.builder("sakon_split", "inftsukaddon:questnpc1").displayName("Ukon").texture("inftsukaddon", "textures/sakon.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)3000.0F).attackDamage((double)7.0F).movementSpeed(0.42).combatTier(3).combatStyle("FERAL").build());
      NpcConfigRegistry.register(NpcConfig.builder("puppet_add", "inftsukaddon:outpostenemy").displayName("Puppet").texture("inftsukaddon", "textures/bandit1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)2000.0F).attackDamage((double)3.5F).armor((double)8.0F).movementSpeed(0.3).combatTier(1).combatStyle("STANDARD").build());
   }

   private static void initBingo() {
      registerFlat("bingo_chunin_1", "Rogue Genin", "textures/leaf1.png", (double)8000.0F, (double)6.0F, (double)4.0F, 0.33, 2, "STANDARD", 1, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_chunin_2", "Bandit Captain", "textures/bandit2.png", (double)8500.0F, (double)6.5F, (double)5.0F, 0.34, 2, "FERAL", 0, false, false);
      registerFlat("bingo_chunin_3", "Missing-Nin Scout", "textures/sand1.png", (double)9000.0F, (double)6.5F, (double)4.0F, 0.37, 2, "STANDARD", 2, true, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_chunin_4", "Hired Mercenary", "textures/bandit4.png", (double)9000.0F, (double)7.0F, (double)5.0F, 0.35, 2, "STANDARD", 1, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_chunin_5", "River Pirate", "textures/bandit5.png", (double)9500.0F, (double)7.0F, (double)5.0F, 0.36, 2, "FERAL", 3, false, false);
      registerFlat("bingo_chunin_6", "Desert Raider", "textures/sandbandit1.png", (double)10000.0F, (double)7.5F, (double)6.0F, 0.35, 3, "FERAL", 0, false, false);
      registerFlat("bingo_chunin_7", "Mountain Bandit", "textures/bandit6.png", (double)10500.0F, (double)7.5F, (double)6.0F, 0.34, 3, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_chunin_8", "Forest Stalker", "textures/femaleleaf1.png", (double)11000.0F, (double)8.0F, (double)5.0F, 0.38, 3, "STANDARD", 2, true, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_chunin_9", "Road Thief", "textures/bandit8.png", (double)11500.0F, (double)8.5F, (double)6.0F, 0.37, 3, "FERAL", 0, false, false);
      registerFlat("bingo_chunin_10", "Escaped Prisoner", "textures/cloud2.png", (double)12000.0F, (double)9.0F, (double)7.0F, 0.38, 3, "FERAL", 4, true, false);
      registerFlat("bingo_jonin_1", "Rogue Chunin", "textures/leaf3.png", (double)15000.0F, (double)10.0F, (double)6.0F, 0.35, 3, "STANDARD", 1, true, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_jonin_2", "Shadow Broker", "textures/the_shadow.png", (double)16000.0F, (double)11.0F, (double)7.0F, 0.38, 3, "SHADOW", 0, true, false, "inftsukaddon:shadowboss");
      registerFlat("bingo_jonin_3", "Poison Master", "textures/femalesand1.png", (double)17000.0F, (double)11.0F, (double)7.0F, 0.36, 3, "STANDARD", 3, true, false, "inftsukaddon:bingotarget");
      NpcConfigRegistry.register(NpcConfig.builder("bingo_jonin_4", "inftsukaddon:questnpc1").displayName("Blade Dancer").texture("inftsukaddon", "textures/sand3.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)19000.0F).attackDamage((double)13.0F).armor((double)7.0F).movementSpeed(0.42).combatTier(4).combatStyle("FERAL").natureType(2).hasRangedAttack(false).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(1.3F).rageSpeedMultiplier(1.2F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_jonin_5", "inftsukaddon:questnpc1").displayName("Curse Seal Bearer").texture("inftsukaddon", "textures/sound_shinobi.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)20000.0F).attackDamage((double)14.0F).armor((double)8.0F).movementSpeed(0.4).combatTier(4).combatStyle("FERAL").natureType(0).hasRangedAttack(false).knockbackImmune(true).rageThreshold(0.35F).rageDamageMultiplier(1.4F).rageSpeedMultiplier(1.3F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_jonin_6", "inftsukaddon:questnpc1").displayName("Storm Raider").texture("inftsukaddon", "textures/cloud3.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)22000.0F).attackDamage((double)14.0F).armor((double)8.0F).movementSpeed(0.42).combatTier(4).combatStyle("FERAL").natureType(4).hasRangedAttack(true).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(1.3F).rageSpeedMultiplier(1.2F).build());
      registerFlat("bingo_jonin_7", "Silent Assassin", "textures/assassin1.png", (double)18000.0F, (double)12.0F, (double)7.0F, 0.4, 4, "SHADOW", 3, true, true, "inftsukaddon:shadowboss");
      registerFlat("bingo_jonin_8", "Weapons Smuggler", "textures/bandit5.png", (double)17000.0F, (double)12.0F, (double)9.0F, 0.36, 3, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_jonin_9", "Puppet Master", "textures/sand4.png", (double)21000.0F, (double)13.0F, (double)10.0F, 0.37, 4, "STANDARD", 0, true, true, "inftsukaddon:bingotarget");
      NpcConfigRegistry.register(NpcConfig.builder("bingo_jonin_10", "inftsukaddon:shadowboss").displayName("Genjutsu Specialist").texture("inftsukaddon", "textures/the_shadow.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)25000.0F).attackDamage((double)16.0F).armor((double)9.0F).movementSpeed(0.4).combatTier(4).combatStyle("SHADOW").natureType(0).hasRangedAttack(true).knockbackImmune(true).rageThreshold(0.25F).rageDamageMultiplier(1.3F).rageSpeedMultiplier(1.2F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_kage_1", "inftsukaddon:shadowboss").displayName("Akatsuki Sympathizer").texture("inftsukaddon", "textures/the_shadow.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)35000.0F).attackDamage((double)18.0F).armor((double)10.0F).movementSpeed(0.38).combatTier(4).combatStyle("SHADOW").natureType(1).hasRangedAttack(true).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(1.3F).rageSpeedMultiplier(1.2F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_kage_2", "inftsukaddon:questnpc1").displayName("Tailed Beast Hunter").texture("inftsukaddon", "textures/femaleleaf2.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)40000.0F).attackDamage((double)20.0F).armor((double)11.0F).movementSpeed(0.42).combatTier(4).combatStyle("FERAL").natureType(2).hasRangedAttack(false).knockbackImmune(true).rageThreshold(0.25F).rageDamageMultiplier(1.4F).rageSpeedMultiplier(1.3F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_kage_3", "inftsukaddon:questnpc1").displayName("Forbidden Jutsu Scholar").texture("inftsukaddon", "textures/femalestone1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)42000.0F).attackDamage((double)21.0F).armor((double)12.0F).movementSpeed(0.4).combatTier(4).combatStyle("FERAL").natureType(4).hasRangedAttack(true).knockbackImmune(true).rageThreshold(0.28F).rageDamageMultiplier(1.4F).rageSpeedMultiplier(1.3F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_kage_4", "inftsukaddon:questnpc1").displayName("Legendary Swordsman").texture("inftsukaddon", "textures/mist1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)45000.0F).attackDamage((double)22.0F).armor((double)13.0F).movementSpeed(0.43).combatTier(4).combatStyle("FERAL").natureType(3).hasRangedAttack(false).knockbackImmune(true).rageThreshold(0.22F).rageDamageMultiplier(1.5F).rageSpeedMultiplier(1.4F).build());
      NpcConfigRegistry.register(NpcConfig.builder("bingo_kage_5", "inftsukaddon:shadowboss").displayName("War Criminal").texture("inftsukaddon", "textures/the_shadow.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)50000.0F).attackDamage((double)24.0F).armor((double)14.0F).movementSpeed(0.45).combatTier(4).combatStyle("SHADOW").natureType(1).hasRangedAttack(true).knockbackImmune(true).rageThreshold(0.2F).rageDamageMultiplier(1.5F).rageSpeedMultiplier(1.4F).build());
      registerFlat("bingo_decoy_1", "Decoy", "textures/bandit4.png", (double)4000.0F, (double)5.0F, (double)3.0F, 0.33, 1, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_decoy_2", "Decoy", "textures/bandit5.png", (double)5000.0F, (double)5.5F, (double)3.0F, 0.35, 1, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_decoy_3", "Decoy", "textures/bandit6.png", (double)6000.0F, (double)6.5F, (double)4.0F, 0.34, 2, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_decoy_4", "Decoy", "textures/bandit7.png", (double)8000.0F, (double)8.0F, (double)5.0F, 0.36, 2, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_decoy_5", "Decoy", "textures/bandit7.png", (double)6000.0F, (double)8.0F, (double)6.0F, 0.35, 3, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_decoy_6", "Decoy", "textures/bandit8.png", (double)7000.0F, (double)9.0F, (double)7.0F, 0.36, 3, "STANDARD", 0, false, false, "inftsukaddon:bingotarget");
      registerFlat("bingo_ambush_1", "Ambusher", "textures/assassin1.png", (double)5000.0F, (double)7.0F, (double)4.0F, 0.4, 2, "FERAL", 0, false, false);
      registerFlat("bingo_ambush_2", "Ambusher", "textures/assassin2.png", (double)7000.0F, (double)9.0F, (double)5.0F, 0.42, 2, "FERAL", 0, false, false);
      registerFlat("bingo_ambush_3", "Ambusher", "textures/bandit2.png", (double)9000.0F, (double)11.0F, (double)6.0F, 0.44, 3, "FERAL", 0, true, false);
      registerFlat("bingo_ambush_4", "Ambusher", "textures/bandit8.png", (double)12000.0F, (double)13.0F, (double)7.0F, 0.44, 3, "FERAL", 1, true, false);
   }

   private static void initWave() {
      registerFlat("wave_grunt_1", "Rogue Genin", "textures/leaf1.png", (double)1500.0F, (double)3.0F, (double)2.0F, 0.32, 1, "STANDARD", 0, false, false, "inftsukaddon:defensewave");
      registerFlat("wave_grunt_2", "Bandit", "textures/bandit2.png", (double)2000.0F, (double)3.5F, (double)2.0F, 0.33, 1, "STANDARD", 0, false, false, "inftsukaddon:defensewave");
      registerFlat("wave_grunt_3", "Sound Scout", "textures/sound_shinobi.png", (double)2000.0F, (double)4.0F, (double)3.0F, 0.35, 1, "STANDARD", 0, true, false, "inftsukaddon:defensewave");
      registerFlat("wave_grunt_4", "Zetsu Clone", "textures/bandit3.png", (double)2500.0F, (double)4.0F, (double)2.0F, 0.36, 2, "STANDARD", 0, false, false, "inftsukaddon:defensewave");
      registerFlat("wave_grunt_5", "Reanimation", "textures/bandit4.png", (double)3000.0F, (double)5.0F, (double)3.0F, 0.34, 2, "STANDARD", 0, false, false, "inftsukaddon:defensewave");
      registerFlat("wave_captain_1", "Rogue Chunin Captain", "textures/femaleleaf1.png", (double)5000.0F, (double)6.0F, (double)5.0F, 0.35, 2, "STANDARD", 0, true, false, "inftsukaddon:defensewave");
      registerFlat("wave_captain_2", "Bandit Lord", "textures/bandit5.png", (double)6000.0F, (double)7.0F, (double)6.0F, 0.34, 2, "FERAL", 0, false, false);
      registerFlat("wave_captain_3", "Sound Lieutenant", "textures/sound_shinobi.png", (double)6500.0F, (double)7.5F, (double)5.0F, 0.37, 3, "STANDARD", 1, true, false, "inftsukaddon:defensewave");
      registerFlat("wave_captain_4", "Akatsuki Scout", "textures/the_shadow.png", (double)7000.0F, (double)8.0F, (double)6.0F, 0.38, 3, "STANDARD", 0, true, false, "inftsukaddon:defensewave");
      registerFlat("wave_captain_5", "Edo Warrior", "textures/mist1.png", (double)8000.0F, (double)9.0F, (double)7.0F, 0.36, 3, "FERAL", 3, false, false);
      registerFlat("wave_elite_1", "Rogue Jonin", "textures/sand4.png", (double)8000.0F, (double)9.0F, (double)7.0F, 0.38, 3, "STANDARD", 2, true, true, "inftsukaddon:defensewave");
      registerFlat("wave_elite_2", "Bandit Warlord", "textures/bandit7.png", (double)10000.0F, (double)10.0F, (double)8.0F, 0.37, 3, "FERAL", 0, false, true);
      registerFlat("wave_elite_3", "Sound Commander", "textures/sound_shinobi.png", (double)11000.0F, (double)11.0F, (double)8.0F, 0.4, 4, "STANDARD", 1, true, true, "inftsukaddon:defensewave");
      registerFlat("wave_elite_4", "Akatsuki Agent", "textures/the_shadow.png", (double)13000.0F, (double)12.0F, (double)9.0F, 0.42, 4, "SHADOW", 4, true, true, "inftsukaddon:shadowboss");
      registerFlat("wave_elite_5", "Edo Champion", "textures/femaleleaf2.png", (double)15000.0F, (double)13.0F, (double)10.0F, 0.4, 4, "FERAL", 3, true, true);
   }

   private static void initWorldBoss() {
      NpcConfigRegistry.register(NpcConfig.builder("world_boss_sasori", "inftsukaddon:sasorishippuden").displayName("§c§lSasori the Eternal").texture("inftsukaddon", "textures/sasori.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)100000.0F).attackDamage((double)63.0F).armor((double)20.0F).movementSpeed(0.4).combatTier(4).combatStyle("SASORI_SHIPPUDEN").trueDamageSplit(0.65F).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(1.6F).rageSpeedMultiplier(1.5F).themeColor(-2285022).accentColor(-8974063).build());
      NpcConfigRegistry.register(NpcConfig.builder("world_boss_deidara", "inftsukaddon:deidarashippuden").displayName("§e§lDeidara the Mad Bomber").texture("inftsukaddon", "textures/deidara.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)90000.0F).attackDamage((double)60.0F).armor((double)12.0F).movementSpeed(0.46).combatTier(4).combatStyle("DEIDARA_SHIPPUDEN").trueDamageSplit(0.65F).knockbackImmune(true).rageThreshold(0.35F).rageDamageMultiplier(1.5F).rageSpeedMultiplier(1.4F).themeColor(-1127356).accentColor(-7833822).build());
      NpcConfigRegistry.register(NpcConfig.builder("wb_kokuo", "inftsukaddon:worldbosskokuo").displayName("§5§lKokuo - The Five-Tails").texture("inftsukaddon", "textures/iraya1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)150000.0F).attackDamage((double)130.0F).armor((double)25.0F).movementSpeed(0.45).combatTier(4).combatStyle("STANDARD").trueDamageSplit(0.75F).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(2.0F).rageSpeedMultiplier(1.8F).themeColor(-5614081).accentColor(-11197782).build());
      NpcConfigRegistry.register(NpcConfig.builder("wb_saiken", "inftsukaddon:worldbosssaiken").displayName("§2§lSaiken - The Six-Tails").texture("inftsukaddon", "textures/iraya1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)150000.0F).attackDamage((double)125.0F).armor((double)25.0F).movementSpeed(0.45).combatTier(4).combatStyle("STANDARD").trueDamageSplit(0.75F).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(2.0F).rageSpeedMultiplier(1.8F).themeColor(-12268476).accentColor(-14522846).build());
      NpcConfigRegistry.register(NpcConfig.builder("wb_gyuki", "inftsukaddon:worldbossgyuki").displayName("§8§lGyuki - The Eight-Tails").texture("inftsukaddon", "textures/iraya1.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)175000.0F).attackDamage((double)150.0F).armor((double)30.0F).movementSpeed(0.42).combatTier(4).combatStyle("STANDARD").trueDamageSplit(0.8F).knockbackImmune(true).rageThreshold(0.3F).rageDamageMultiplier(2.2F).rageSpeedMultiplier(1.8F).themeColor(-7829368).accentColor(-12303292).build());
      NpcConfigRegistry.register(NpcConfig.builder("wb_hidan", "inftsukaddon:worldbosshidan").displayName("§4§lHidan the Immortal").texture("inftsukaddon", "textures/hidan.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)100000.0F).attackDamage((double)60.0F).armor((double)14.0F).movementSpeed((double)0.5F).combatTier(4).combatStyle("HIDAN_WORLD_BOSS").trueDamageSplit(0.55F).knockbackImmune(true).weaponItemId("narutomod:scythe_hidan").themeColor(-7864320).accentColor(-12320768).build());
      NpcConfigRegistry.register(NpcConfig.builder("wb_zabuza", "inftsukaddon:worldbosszabuza").displayName("§7§lZabuza, Demon of the Mist").texture("inftsukaddon", "textures/zabuza.png").behavior(NpcConfig.Behavior.HOSTILE).maxHealth((double)95000.0F).attackDamage((double)55.0F).armor((double)12.0F).movementSpeed(0.46).combatTier(4).combatStyle("ZABUZA_WORLD_BOSS").trueDamageSplit(0.5F).knockbackImmune(true).weaponItemId("narutomod:zabuza_sword").themeColor(-13417353).accentColor(-15062467).build());
   }

   private static void registerFlat(String configId, String displayName, String texture, double hp, double dmg, double armor, double speed, int combatTier, String combatStyle, int natureType, boolean hasRanged, boolean knockbackImmune) {
      registerFlat(configId, displayName, texture, hp, dmg, armor, speed, combatTier, combatStyle, natureType, hasRanged, knockbackImmune, "inftsukaddon:questnpc1");
   }

   private static void registerFlat(String configId, String displayName, String texture, double hp, double dmg, double armor, double speed, int combatTier, String combatStyle, int natureType, boolean hasRanged, boolean knockbackImmune, String entityType) {
      NpcConfigRegistry.register(NpcConfig.builder(configId, entityType).displayName(displayName).texture("inftsukaddon", texture).behavior(NpcConfig.Behavior.HOSTILE).maxHealth(hp).attackDamage(dmg).armor(armor).movementSpeed(speed).combatTier(combatTier).combatStyle(combatStyle).natureType(natureType).hasRangedAttack(hasRanged).knockbackImmune(knockbackImmune).build());
   }

   private static void registerBingoAnbu(String configId, String displayName, String texture, double hp, double dmg, double armor, double speed, int natureType, float rageThreshold, float rageDmgMult, float rageSpdMult) {
      NpcConfigRegistry.register(NpcConfig.builder(configId, "inftsukaddon:shadowboss").displayName(displayName).texture("inftsukaddon", texture).behavior(NpcConfig.Behavior.HOSTILE).maxHealth(hp).attackDamage(dmg).armor(armor).movementSpeed(speed).combatTier(4).combatStyle("SHADOW").natureType(natureType).knockbackImmune(true).rageThreshold(rageThreshold).rageDamageMultiplier(rageDmgMult).rageSpeedMultiplier(rageSpdMult).build());
   }

   private static void registerTiered(String prefix, String displayName, String texDomain, String texPath, double baseHp, double baseDmg, double armor, double speed, int combatTier, String combatStyle, int natureType, boolean hasRanged, boolean knockbackImmune, float rageThreshold, float rageDmgMult, float rageSpdMult, int themeColor, int accentColor, boolean hasWaterDragon) {
      registerTiered(prefix, displayName, texDomain, texPath, baseHp, baseDmg, armor, speed, combatTier, combatStyle, natureType, hasRanged, knockbackImmune, rageThreshold, rageDmgMult, rageSpdMult, themeColor, accentColor, hasWaterDragon, (String)null, 1.0F);
   }

   private static void registerTiered(String prefix, String displayName, String texDomain, String texPath, double baseHp, double baseDmg, double armor, double speed, int combatTier, String combatStyle, int natureType, boolean hasRanged, boolean knockbackImmune, float rageThreshold, float rageDmgMult, float rageSpdMult, int themeColor, int accentColor, boolean hasWaterDragon, String entityType) {
      registerTiered(prefix, displayName, texDomain, texPath, baseHp, baseDmg, armor, speed, combatTier, combatStyle, natureType, hasRanged, knockbackImmune, rageThreshold, rageDmgMult, rageSpdMult, themeColor, accentColor, hasWaterDragon, (String)null, 1.0F, entityType);
   }

   private static void registerTiered(String prefix, String displayName, String texDomain, String texPath, double baseHp, double baseDmg, double armor, double speed, int combatTier, String combatStyle, int natureType, boolean hasRanged, boolean knockbackImmune, float rageThreshold, float rageDmgMult, float rageSpdMult, int themeColor, int accentColor, boolean hasWaterDragon, String weaponItemId, float renderScale) {
      registerTiered(prefix, displayName, texDomain, texPath, baseHp, baseDmg, armor, speed, combatTier, combatStyle, natureType, hasRanged, knockbackImmune, rageThreshold, rageDmgMult, rageSpdMult, themeColor, accentColor, hasWaterDragon, weaponItemId, renderScale, "inftsukaddon:questnpc1");
   }

   private static void registerTiered(String prefix, String displayName, String texDomain, String texPath, double baseHp, double baseDmg, double armor, double speed, int combatTier, String combatStyle, int natureType, boolean hasRanged, boolean knockbackImmune, float rageThreshold, float rageDmgMult, float rageSpdMult, int themeColor, int accentColor, boolean hasWaterDragon, String weaponItemId, float renderScale, String entityType) {
      for(OutpostDifficultyTier tier : OutpostDifficultyTier.values()) {
         NpcConfig.Builder b = NpcConfig.builder(prefix + "_" + tier.getConfigSuffix(), entityType).displayName(displayName).texture(texDomain, texPath).behavior(NpcConfig.Behavior.HOSTILE).maxHealth(baseHp * tier.getHpMultiplier()).attackDamage(baseDmg * tier.getDmgMultiplier()).armor(armor).movementSpeed(speed).knockbackImmune(knockbackImmune).combatTier(combatTier).combatStyle(combatStyle).natureType(natureType).hasRangedAttack(hasRanged).themeColor(themeColor).accentColor(accentColor);
         if (rageThreshold > 0.0F) {
            b.rageThreshold(rageThreshold).rageDamageMultiplier(rageDmgMult).rageSpeedMultiplier(rageSpdMult);
         }

         if (hasWaterDragon) {
            b.hasWaterDragon(true).waterDragonPower(2.0F);
         }

         if (weaponItemId != null) {
            b.weaponItemId(weaponItemId);
         }

         if (renderScale != 1.0F) {
            b.renderScale(renderScale);
         }

         NpcConfigRegistry.register(b.build());
      }

   }
}
