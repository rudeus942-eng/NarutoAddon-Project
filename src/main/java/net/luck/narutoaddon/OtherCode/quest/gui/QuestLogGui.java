
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.akatsuki.contract.ContractActionMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiModInit;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiRank;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiRing;
import net.luck.narutoaddon.OtherCode.akatsuki.core.RingUpgradeConstants;
import net.luck.narutoaddon.OtherCode.akatsuki.market.BlackMarketActionMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.market.BlackMarketItem;
import net.luck.narutoaddon.OtherCode.akatsuki.market.BlackMarketRegistry;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.AkatsukiMissionActionMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiClientData;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiInviteResponseMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiRingUpgradeMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.network.LeaderActionMessage;
import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.contract.ContractClientData;
import net.luck.narutoaddon.OtherCode.endgame.contract.ContractManager;
import net.luck.narutoaddon.OtherCode.endgame.contract.ContractNetworkMessage;
import net.luck.narutoaddon.OtherCode.endgame.gui.EndgameClientData;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameActionMessage;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDefinition;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostRegistry;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedMenu;
import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.QuestModInit;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.network.QuestActionMessage;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.luck.narutoaddon.OtherCode.quest.pvp.OperationTierTable;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpClientData;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpMissionTemplate;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpModInit;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.LeadershipActionMessage;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpActionMessage;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpSyncMessage;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.TournamentActionMessage;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarActionMessage;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarMode;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarRosterRequestMessage;
import net.luck.narutoaddon.OtherCode.stat.core.StatCategory;
import net.luck.narutoaddon.OtherCode.stat.core.StatConstants;
import net.luck.narutoaddon.OtherCode.stat.core.StatElement;
import net.luck.narutoaddon.OtherCode.stat.core.StatModInit;
import net.luck.narutoaddon.OtherCode.stat.network.StatAllocateMessage;
import net.luck.narutoaddon.OtherCode.stat.network.StatClientData;
import net.luck.narutoaddon.OtherCode.territory.core.TerritoryConstants;
import net.luck.narutoaddon.OtherCode.territory.network.TerritoryClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class QuestLogGui extends GuiScreen {
   private static final int GUI_WIDTH = 360;
   private static final int GUI_HEIGHT = 280;
   private static final int COLOR_BORDER_OUTER = -7706054;
   private static final int COLOR_BORDER_INNER = -9809872;
   private static final int COLOR_BG_TOP = -298833888;
   private static final int COLOR_BG_BOT = -300280816;
   private static final int COLOR_TITLE = -1521552;
   private static final int COLOR_PANEL_TOP = -868600792;
   private static final int COLOR_PANEL_BOT = -870442476;
   private static final int COLOR_SEPARATOR = -9807296;
   private static final int COLOR_TEXT_HEADER = -1521552;
   private static final int COLOR_TEXT_BODY = -2832216;
   private static final int COLOR_TEXT_DIM = -7700886;
   private static final int COLOR_WAR_SKIRMISH = -30720;
   private static final int COLOR_WAR_DIVISION = -52429;
   private static final int COLOR_WAR_DOMINATION = -13399809;
   private static final int COLOR_WAR_RUSH = -13386957;
   private static final int COLOR_WAR_FOREST = -14514142;
   private static final int COLOR_WAR_INFILTRATION = -3403503;
   private static final int BUTTON_CLOSE = 0;
   private static final int BUTTON_ACCEPT = 1;
   private static final int BUTTON_ABANDON = 2;
   private static final int BUTTON_TAB_AVAILABLE = 3;
   private static final int BUTTON_TAB_ACTIVE = 4;
   private static final int BUTTON_TAB_COMPLETED = 5;
   private static final int BUTTON_CAT_STORY = 6;
   private static final int BUTTON_CAT_PVE = 7;
   private static final int BUTTON_CAT_PVP = 8;
   private static final int BUTTON_CAT_WAR = 9;
   private static final int BUTTON_REROLL = 11;
   private static final int BUTTON_ACCEPT_OFFER = 12;
   private static final int BUTTON_SUBSLOT_BASE = 20;
   private static final int BUTTON_QUEST_BASE = 400;
   private static final int BUTTON_STORYLINE_BASE = 300;
   private static final int BUTTON_PVE_DAILY = 13;
   private static final int BUTTON_PVE_WEEKLY = 14;
   private static final int BUTTON_PVE_RANDOM = 15;
   private static final int BUTTON_PVP_DAILY = 16;
   private static final int BUTTON_PVP_WEEKLY = 17;
   private static final int BUTTON_PVP_RANDOM = 18;
   private static final int BUTTON_PVP_BINGO = 19;
   private static final int PVP_ACCEPT = 30;
   private static final int PVP_REROLL = 31;
   private static final int PVP_ABANDON = 32;
   private static final int BINGO_ACCEPT = 33;
   private static final int PVP_SUBSLOT_BASE = 35;
   private static final int WAR_DECLARE = 50;
   private static final int WAR_SURRENDER = 51;
   private static final int WAR_MODE_SKIRMISH = 52;
   private static final int WAR_MODE_DIVISION = 53;
   private static final int WAR_MODE_DOMINATION = 54;
   private static final int WAR_MODE_RUSH = 55;
   private static final int WAR_MODE_FOREST = 56;
   private static final int WAR_MODE_INFILTRATION = 57;
   private static final int WAR_TARGET_BASE = 60;
   private static final int WAR_CONFIRM = 66;
   private static final int WAR_LOBBY_LOCK = 67;
   private static final int WAR_LOBBY_CANCEL = 68;
   private static final int WAR_LOBBY_START = 69;
   private static final int WAR_LOBBY_ACCEPT = 70;
   private static final int WAR_LOBBY_DECLINE = 71;
   private static final int WAR_SELECT_ROSTER = 72;
   private static final int WAR_ROSTER_BASE = 200;
   private static final int BUTTON_PVP_LEADERSHIP = 75;
   private static final int LEADERSHIP_ACCEPT = 76;
   private static final int LEADERSHIP_REROLL = 77;
   private static final int LEADERSHIP_ABANDON = 78;
   private static final int LEADERSHIP_ORDER_VILLAGE = 80;
   private static final int LEADERSHIP_CANCEL_ORDER_BASE = 81;
   private static final int LEADERSHIP_TARGET_BASE = 85;
   private static final int LEADERSHIP_CANCEL_FLOW = 92;
   private static final int LEADERSHIP_ISSUE_NEW = 93;
   private static final int LEADERSHIP_ASSIGN_TEMPLATE_BASE = 180;
   private static final int LEADERSHIP_ACCEPT_ASSIGNED_BASE = 170;
   private static final int BUTTON_CAT_SHOP = 10;
   private static final int BUTTON_CAT_VILLAGE = 40;
   private static final int BUTTON_CAT_RANKED = 41;
   private static final int BUTTON_CAT_STATS = 42;
   private static final int BUTTON_CAT_ENDGAME = 47;
   private static final int BUTTON_CAT_TERRITORY = 48;
   private static final int BUTTON_CAT_FACTION = 49;
   private static final int FACTION_SUB_BASE = 700;
   private static final int FACTION_INVITE_ACCEPT = 710;
   private static final int FACTION_INVITE_DECLINE = 711;
   private static final int FACTION_RING_UPGRADE_BASE = 720;
   private static final int FACTION_LEADER_START_RAID = 740;
   private static final int FACTION_LEADER_CANCEL_RAID = 741;
   private static final int FACTION_LEADER_ASSIGN_PARTNER = 742;
   private static final int FACTION_LEADER_VILLAGE_BASE = 746;
   private static final int FACTION_LEADER_MEMBER_BASE = 760;
   private static final int FACTION_LEADER_MISSION_TYPE_CYCLE = 770;
   private static final int FACTION_LEADER_MISSION_ASSIGN = 771;
   private static final int FACTION_LEADER_MISSION_REMOVE = 772;
   private static final int FACTION_LEADER_MISSION_TOKENS_DOWN = 773;
   private static final int FACTION_LEADER_MISSION_TOKENS_UP = 774;
   private static final int FACTION_LEADER_MISSION_REP_DOWN = 775;
   private static final int FACTION_LEADER_MISSION_REP_UP = 776;
   private static final int FACTION_LEADER_BOUNTY_ADD = 780;
   private static final int FACTION_LEADER_BOUNTY_AMOUNT_DOWN = 781;
   private static final int FACTION_LEADER_BOUNTY_AMOUNT_UP = 782;
   private static final int FACTION_LEADER_MODE_RAID = 785;
   private static final int FACTION_LEADER_MODE_MISSION = 786;
   private static final int FACTION_LEADER_MODE_BOUNTY = 787;
   private static final int FACTION_CONTRACT_TYPE_CYCLE = 730;
   private static final int FACTION_CONTRACT_RYO_DOWN = 731;
   private static final int FACTION_CONTRACT_RYO_UP = 732;
   private static final int FACTION_CONTRACT_POST = 733;
   private static final int FACTION_CONTRACT_CANCEL = 734;
   private static final int FACTION_CONTRACT_ACCEPT_BASE = 735;
   private static final int CONTRACT_TARGET_PREV = 800;
   private static final int CONTRACT_TARGET_NEXT = 801;
   private static final int CONTRACT_ZONE_PREV = 802;
   private static final int CONTRACT_ZONE_NEXT = 803;
   private static final int BLACK_MARKET_BUY_BASE = 810;
   private static final int BLACK_MARKET_ZONE_BASE = 830;
   private static final int BLACK_MARKET_ZONE_BACK = 829;
   private static final int FACTION_MISSION_ACCEPT_BASE = 790;
   private static final int FACTION_MISSION_ABANDON_BASE = 795;
   private static final int TERRITORY_TOGGLE = 600;
   private static final int STAT_SUB_OFFENSE = 43;
   private static final int STAT_SUB_DEFENSE = 44;
   private static final int STAT_SUB_OVERVIEW = 45;
   private static final int STAT_SUB_CHAKRA = 47;
   private static final int STAT_RESPEC = 46;
   private static final int STAT_ALLOCATE_BASE = 1200;
   private static final int ENDGAME_SUB_OUTPOSTS = 500;
   private static final int ENDGAME_SUB_BINGO = 501;
   private static final int ENDGAME_SUB_EVENTS = 502;
   private static final int ENDGAME_SUB_LEADERBOARD = 503;
   private static final int BUTTON_PVE_CONTRACTS = 504;
   private static final int CONTRACT_ABANDON_BTN = 559;
   private static final int CONTRACT_ACCEPT_BASE = 560;
   private static final int ENDGAME_OUTPOST_BASE = 510;
   private static final int ENDGAME_JOIN_OUTPOST = 523;
   private static final int ENDGAME_LEAVE_OUTPOST = 524;
   private static final int ENDGAME_BINGO_BASE = 530;
   private static final int ENDGAME_BINGO_HUNT = 535;
   private static final int ENDGAME_JOIN_INCURSION = 540;
   private static final int ENDGAME_JOIN_DEFENSE = 541;
   private static final int ENDGAME_LB_CAT_BASE = 550;
   private static final int LEADERSHIP_VIEW_OVERVIEW = 150;
   private static final int LEADERSHIP_VIEW_MISSION = 151;
   private static final int LEADERSHIP_VIEW_ORDER = 152;
   private static final int LEADERSHIP_SCROLL_UP = 160;
   private static final int LEADERSHIP_SCROLL_DOWN = 161;
   private static final int BUTTON_PVP_TOURNAMENT = 79;
   private static final int TOURNAMENT_SIGNUP = 130;
   private static final int TOURNAMENT_WITHDRAW = 131;
   private static final int TOURNAMENT_CREATE = 132;
   private static final int TOURNAMENT_FINALIZE = 133;
   private static final int TOURNAMENT_CANCEL = 134;
   private static final int TOURNAMENT_REWARDS_1ST = 135;
   private static final int TOURNAMENT_REWARDS_2ND = 136;
   private static final int TOURNAMENT_REWARDS_3RD = 137;
   private static final int TOURNAMENT_MIN_RANK = 138;
   private static final int TOURNAMENT_MAX_RANK = 139;
   private static final int TOURNAMENT_VILLAGE_ONLY = 140;
   private static final int[] RANK_COLORS = new int[]{-11141291, -11184641, -5635926, -22016, -43691, -43521};
   private static final String[] RANK_NAMES = new String[]{"D", "C", "B", "A", "S", "S+"};
   private int guiLeft;
   private int guiTop;
   private int selectedCategory = 0;
   private int selectedTab = 0;
   private int selectedQuestIndex = -1;
   private int selectedSubSlotIndex = 0;
   private int scrollOffset = 0;
   private int storyTotalRows = 0;
   private int selectedStorylineIndex = 0;
   private boolean dataRequested = false;
   private float animationTick = 0.0F;
   private int pveSubTab = 0;
   private int boardContractScrollOffset = 0;
   private int pvpSubTab = 0;
   private int pvpSubSlotIndex = 0;
   private int bingoScrollOffset = 0;
   private int warDeclareStep = 0;
   private int warSelectedMode = -1;
   private String warSelectedTarget = null;
   private int leadershipSubView = 0;
   private int leadershipStep = 0;
   private int assignTemplateScrollOffset = 0;
   private int ordersOverviewScrollOffset = 0;
   private int missionScrollOffset = 0;
   private int tournamentMinRank = -1;
   private int tournamentMaxRank = -1;
   private float bracketPanX = 0.0F;
   private float bracketPanY = 0.0F;
   private float bracketZoom = 1.0F;
   private boolean bracketDragging = false;
   private int bracketDragStartX = 0;
   private int bracketDragStartY = 0;
   private float bracketDragStartPanX = 0.0F;
   private float bracketDragStartPanY = 0.0F;
   private static final float BRACKET_ZOOM_MIN = 0.4F;
   private static final float BRACKET_ZOOM_MAX = 2.0F;
   private static final String[] PVP_RANK_DISPLAY = new String[]{"Genin", "Chunin", "Toku Jonin", "Jonin", "ANBU", "ANBU Cpt"};
   private float mapZoom = 1.0F;
   private float mapOffsetX = 0.0F;
   private float mapOffsetZ = 0.0F;
   private boolean mapDragging = false;
   private int mapDragStartX = 0;
   private int mapDragStartY = 0;
   private float mapDragStartOffsetX = 0.0F;
   private float mapDragStartOffsetZ = 0.0F;
   private String selectedZoneId = null;
   private final int[][] fortifyBtnBounds = new int[5][4];
   private int fortifyBtnCount = 0;
   private int fortifyScrollOffset = 0;
   private int territoryScoreboardScrollOffset = 0;
   private int fortifyBackBtnX = -1;
   private int fortifyBackBtnY = -1;
   private int fortifyBackBtnW = 0;
   private int fortifyBackBtnH = 0;
   private static final ResourceLocation TERRITORY_MAP_TEXTURE = new ResourceLocation("inftsukaddon", "textures/map.png");
   private static final int MAP_TEX_W = 1145;
   private static final int MAP_TEX_H = 1140;
   private static final float MAP_ZOOM_MIN = 1.0F;
   private static final float MAP_ZOOM_MAX = 8.0F;
   private static final float WORLD_MIN = -6000.0F;
   private static final float WORLD_MAX = 6000.0F;
   private static final float WORLD_SIZE = 12000.0F;
   private static final int[][] VILLAGE_CENTERS = new int[][]{{-947, -843}, {-2734, 520}, {3860, -2155}, {-2500, -2655}, {1912, -3066}, {-2249, -828}, {2307, -4025}};
   private int statSubTab = 0;
   private int statScrollOffset = 0;
   public static int chakraEnhancementCharges = 0;
   public static int chakraEnhancementCap = 0;
   private int endgameSubTab = 0;
   private int endgameBingoIndex = 0;
   private int endgameLbCategory = 0;
   private boolean endgameDataRequested = false;
   private int factionSubTab = 0;
   private int akatsukiAssignScrollOffset = 0;
   private int akatsukiAssignMaxScroll = 0;
   private int contractScrollOffset = 0;
   private String selectedBlackMarketItemId = null;
   private int blackMarketZoneScrollOffset = 0;
   private int blackMarketItemScrollOffset = 0;
   private int contractTypeIndex = 0;
   private int contractRyoAmount = 500;
   private int contractTargetPlayerIndex = 0;
   private int contractTargetZoneIndex = 0;
   private boolean leaderRaidSelecting = false;
   private int leaderSelectedMember1 = -1;
   private int leaderSelectedMember2 = -1;
   private int leaderMode = 0;
   private int leaderMissionTypeIndex = 0;
   private int leaderMissionTokens = 50;
   private int leaderMissionRep = 100;
   private int leaderBountyAmount = 500;
   private static final int LEFT_PANEL_W = 155;
   private static final String[] PVE_SUB_TAB_NAMES = new String[]{"Daily", "Weekly", "Random", "Outposts", "Contracts", "Bingo Book", "Events", "Leaders"};
   private static final int[] PVE_SUB_TAB_IDS = new int[]{13, 14, 15, 500, 504, 501, 502, 503};
   private static final int[][] PVE_SUB_TAB_COLORS = new int[][]{{-12948854, -13620192, -15054230, -14671848}, {-9811318, -13620192, -11916694, -14671848}, {-9807312, -13620192, -11912680, -14671848}, {-7718342, -13620192, -9823718, -14671848}, {-7718294, -13620192, -9823670, -14671848}, {-7706054, -13620192, -9811416, -14671848}, {-12948854, -13620192, -15054230, -14671848}, {-10855878, -13620192, -12961254, -14671848}};
   private static final int CONTRACT_ROW_HEIGHT = 36;
   private static final int CONTRACT_VISIBLE_ROWS = 4;
   private static final int CONTRACT_ACCEPT_BTN_W = 54;
   private static final int CONTRACT_RIGHT_PAD = 8;
   private static final String[] TERRITORY_VILLAGE_ORDER = new String[]{"leaf", "sand", "mist", "stone", "cloud", "rain", "akatsuki"};
   private static final String[] TERRITORY_VILLAGE_DISPLAY = new String[]{"Leaf", "Sand", "Mist", "Stone", "Cloud", "Rain", "Akatsuki"};
   private static final int[][] TERRAIN_LAND = new int[][]{{-2000, -1700, 1800, 900, -14796258}, {-1000, 900, 1200, 1600, -14005718}, {-2000, -1800, -1700, -1500, -14270940}, {1800, -2200, 2200, -1700, -14534112}, {-2900, -1500, -1800, -300, -14009814}, {-3600, -300, -1700, 2200, -10859990}, {-3400, -3400, -1700, -1800, -12962512}, {700, -3900, 2700, -2200, -14009808}, {400, -3600, 900, -2600, -12957094}, {3400, -2600, 4400, -1300, -14796240}, {3100, -2300, 3400, -2000, -14796760}, {4400, -2100, 4700, -1800, -14796760}, {3600, -1300, 3900, -1100, -14796760}, {1200, 100, 2000, 800, -14001606}, {-1500, 400, 200, 1200, -14008272}};
   private static final int[][] TERRAIN_DETAIL = new int[][]{{-1400, -1200, -1000, -900, -15323114}, {-600, -800, -200, -500, -15323114}, {200, -400, 600, -100, -15323114}, {-800, 0, -400, 300, -15323114}, {600, -1200, 1000, -900, -15323114}, {-200, -1500, 200, -1200, -15323114}, {800, 100, 1100, 400, -15323114}, {-3200, 200, -2800, 600, -9807302}, {-2600, 800, -2200, 1200, -9807302}, {-3000, 1400, -2600, 1800, -9807302}, {-2200, 0, -1900, 400, -9807302}, {-3200, -3200, -2800, -2800, -14015200}, {-2600, -2600, -2200, -2200, -14015200}, {-3000, -2600, -2600, -2400, -14015200}, {-2200, -3000, -1900, -2600, -14015200}, {1200, -3600, 1600, -3200, -14799324}, {2000, -3400, 2400, -3000, -14799324}, {-2700, -1200, -2400, -900, -14799330}, {-2400, -700, -2100, -500, -14799330}};
   private static final int SHORE_COLOR = -12955552;
   private static final int[][] RIVER_SEGMENTS = new int[][]{{-1000, -1600, -800, -1200}, {-800, -1200, -600, -600}, {-600, -600, -500, 0}, {-500, 0, -400, 600}, {-400, 600, -200, 1200}, {-2200, -1200, -2400, -600}, {-2400, -600, -2500, 0}, {-2500, 0, -2400, 200}};
   private static final String[] ENDGAME_LB_CATEGORY_NAMES = new String[]{"Outposts", "Bingo", "Incursions", "Defense"};

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      mc.displayGuiScreen(new QuestLogGui());
   }

   public static void openToTournament() {
      Minecraft mc = Minecraft.getMinecraft();
      QuestLogGui gui = new QuestLogGui();
      gui.selectedCategory = 2;
      gui.pvpSubTab = 5;
      mc.displayGuiScreen(gui);
   }

   public void initGui() {
      this.guiLeft = (this.width - 360) / 2;
      this.guiTop = (this.height - 280) / 2;
      if (!this.dataRequested) {
         this.dataRequested = true;
         QuestModInit.NETWORK.sendToServer(new QuestActionMessage(4));
         PvpModInit.NETWORK.sendToServer(new PvpActionMessage(5, (byte)0));
      }

      this.rebuildButtons();
   }

   private String getPveSubSlot() {
      switch (this.pveSubTab) {
         case 0:
            return "daily_" + this.selectedSubSlotIndex;
         case 1:
            return "weekly_" + this.selectedSubSlotIndex;
         case 2:
            return "random";
         default:
            return "daily_0";
      }
   }

   private List<String> getPveSubSlots() {
      switch (this.pveSubTab) {
         case 0:
            return QuestClientData.getDailySubSlots();
         case 1:
            return QuestClientData.getWeeklySubSlots();
         default:
            return null;
      }
   }

   private boolean isPveMultiSlot() {
      return this.pveSubTab == 0 || this.pveSubTab == 1;
   }

   private String getPvpSubSlot() {
      switch (this.pvpSubTab) {
         case 0:
            return PvpClientData.PVP_DAILY_SLOTS[this.pvpSubSlotIndex];
         case 1:
            return PvpClientData.PVP_WEEKLY_SLOTS[this.pvpSubSlotIndex];
         case 2:
            return "pvp_random";
         default:
            return PvpClientData.PVP_DAILY_SLOTS[0];
      }
   }

   private byte getPvpSubSlotByte() {
      return PvpSyncMessage.pvpSubSlotToByte(this.getPvpSubSlot());
   }

   private boolean isPvpMultiSlot() {
      return this.pvpSubTab == 0 || this.pvpSubTab == 1;
   }

   public void rebuildButtons() {
      this.buttonList.clear();
      boolean showFactionTab = AkatsukiClientData.akatsukiExists || AkatsukiClientData.hasPendingInvite;
      int catY = this.guiTop + 28;
      int catCount = showFactionTab ? 9 : 8;
      int catW = showFactionTab ? 37 : 42;
      int catSpacing = 3;
      int totalCatW = catW * catCount + catSpacing * (catCount - 1);
      int catStartX = this.guiLeft + (360 - totalCatW) / 2;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(6, catStartX, catY, catW, 16, this.selectedCategory == 0 ? "§fStory" : "§7Story", this.selectedCategory == 0 ? -7706054 : -11912160, this.selectedCategory == 0 ? -9811416 : -13095912));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(7, catStartX + catW + catSpacing, catY, catW, 16, this.selectedCategory == 1 ? "§fPvE" : "§7PvE", this.selectedCategory == 1 ? -12948854 : -14403512, this.selectedCategory == 1 ? -15054230 : -15062472));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(8, catStartX + (catW + catSpacing) * 2, catY, catW, 16, this.selectedCategory == 2 ? "§fPvP" : "§7PvP", this.selectedCategory == 2 ? -7718342 : -12049372, this.selectedCategory == 2 ? -9823718 : -13100518));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(9, catStartX + (catW + catSpacing) * 3, catY, catW, 16, this.selectedCategory == 3 ? "§fWar" : "§7War", this.selectedCategory == 3 ? -7710176 : -12046312, this.selectedCategory == 3 ? -9815536 : -13097968));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(10, catStartX + (catW + catSpacing) * 4, catY, catW, 16, this.selectedCategory == 4 ? "§fShop" : "§7Shop", this.selectedCategory == 4 ? -12944838 : -14403548, this.selectedCategory == 4 ? -15050214 : -15061990));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(40, catStartX + (catW + catSpacing) * 5, catY, catW, 16, "§7Village", -12953030, -14008280));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(41, catStartX + (catW + catSpacing) * 6, catY, catW, 16, "§7Ranked", -9815520, -11916776));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(42, catStartX + (catW + catSpacing) * 7, catY, catW, 16, this.selectedCategory == 5 ? "§fStats" : "§7Stats", this.selectedCategory == 5 ? -10863990 : -13097912, this.selectedCategory == 5 ? -12969366 : -14017992));
      if (showFactionTab) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(49, catStartX + (catW + catSpacing) * 8, catY, catW, 16, this.selectedCategory == 7 ? "§fFaction" : "§7Faction", this.selectedCategory == 7 ? -7725024 : -12050400, this.selectedCategory == 7 ? -9826288 : -13101032));
      }

      switch (this.selectedCategory) {
         case 0:
            this.rebuildStoryButtons();
            break;
         case 1:
            this.rebuildPveButtons();
            break;
         case 2:
            this.rebuildPvpButtons();
            break;
         case 3:
            this.rebuildWarButtons();
         case 4:
         case 6:
         default:
            break;
         case 5:
            this.rebuildStatButtons();
            break;
         case 7:
            this.rebuildFactionButtons();
      }

      int bottomY = this.guiTop + 280 - 30;
      if (this.selectedCategory != 3) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, this.guiLeft + 8, bottomY, 50, 22, "Close", -9809872, -11913184));
      }

   }

   private static String getStorylineDisplayName(String storyline) {
      switch (storyline) {
         case "leaf_story":
            return "Leaf Story";
         case "shinobi_way":
            return "Your Shinobi Way";
         default:
            return storyline;
      }
   }

   private List<String> getStorylinesForCurrentTab() {
      LinkedHashSet<String> storylines = new LinkedHashSet();
      if (this.selectedTab == 0) {
         for(QuestClientData.QuestListEntry e : QuestClientData.getAvailableQuests()) {
            if (e.category == 0 && e.available) {
               storylines.add(e.storyline);
            }
         }
      } else if (this.selectedTab == 1) {
         for(Map.Entry<String, QuestClientData.ActiveQuestState> entry : QuestClientData.getAllActiveQuests().entrySet()) {
            if (QuestClientData.isStorySlot((String)entry.getKey())) {
               String sl = ((String)entry.getKey()).substring("story:".length());
               storylines.add(sl);
            }
         }
      } else if (this.selectedTab == 2) {
         for(String id : QuestClientData.getCompletedQuestIds()) {
            for(QuestClientData.QuestListEntry entry : QuestClientData.getAvailableQuests()) {
               if (entry.id.equals(id) && entry.category == 0) {
                  storylines.add(entry.storyline);
                  break;
               }
            }
         }

         for(String id : QuestClientData.getCompletedQuestIds()) {
            boolean found = false;

            for(QuestClientData.QuestListEntry entry : QuestClientData.getAvailableQuests()) {
               if (entry.id.equals(id) && entry.category == 0) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               storylines.add("leaf_story");
            }
         }
      }

      return new ArrayList(storylines);
   }

   private void rebuildStoryButtons() {
      int subTabY = this.guiTop + 48;
      int tabW0 = 48;
      int tabW = 48;
      int tabSpacing = 2;
      int tabStartX = this.guiLeft + 8;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, tabStartX, subTabY, tabW0, 14, this.selectedTab == 0 ? "§fQuests" : "§8Quests", this.selectedTab == 0 ? -7704528 : -13620192, this.selectedTab == 0 ? -9809888 : -14671848));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(4, tabStartX + tabW0 + tabSpacing, subTabY, tabW, 14, this.selectedTab == 1 ? "§fActive" : "§8Active", this.selectedTab == 1 ? -11896272 : -13620192, this.selectedTab == 1 ? -14001640 : -14671848));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(5, tabStartX + tabW0 + tabSpacing + tabW + tabSpacing, subTabY, tabW + 4, 14, this.selectedTab == 2 ? "§fCompleted" : "§8Completed", this.selectedTab == 2 ? -8762774 : -13620192, this.selectedTab == 2 ? -10868150 : -14671848));
      List<String> storylines = this.getStorylinesForCurrentTab();
      int listY = this.guiTop + 66;
      int listHeight = 18;
      int headerHeight = 16;
      int yOffset = 0;
      int questFlatIndex = 0;
      int visualRow = 0;
      this.storyTotalRows = 0;

      for(int si = 0; si < storylines.size(); ++si) {
         ++this.storyTotalRows;
         if (si == this.selectedStorylineIndex) {
            this.storyTotalRows += this.getQuestsForStoryline((String)storylines.get(si)).size();
         }
      }

      for(int si = 0; si < storylines.size(); ++si) {
         String storyline = (String)storylines.get(si);
         boolean expanded = si == this.selectedStorylineIndex;
         String arrow = expanded ? "▼ " : "▶ ";
         String headerLabel = arrow + getStorylineDisplayName(storyline);
         if (visualRow >= this.scrollOffset) {
            int headerY = listY + yOffset;
            if (headerY + headerHeight > this.guiTop + 280 - 38) {
               break;
            }

            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(300 + si, this.guiLeft + 8, headerY, 143, headerHeight, headerLabel, expanded ? -8754630 : -11911120, expanded ? -10859990 : -12963808));
            yOffset += headerHeight + 2;
         }

         ++visualRow;
         if (expanded) {
            List<QuestClientData.QuestListEntry> quests = this.getQuestsForStoryline(storyline);

            for(int qi = 0; qi < quests.size(); ++qi) {
               if (visualRow >= this.scrollOffset) {
                  int btnY = listY + yOffset;
                  if (btnY + listHeight > this.guiTop + 280 - 38) {
                     break;
                  }

                  QuestClientData.QuestListEntry entry = (QuestClientData.QuestListEntry)quests.get(qi);
                  boolean isSelected = questFlatIndex == this.selectedQuestIndex;
                  int topColor;
                  int botColor;
                  if (isSelected) {
                     topColor = -9807302;
                     botColor = -11912662;
                  } else if (!entry.available) {
                     topColor = -14014432;
                     botColor = -15067120;
                  } else {
                     topColor = -12962776;
                     botColor = -14015464;
                  }

                  String label = entry.name;
                  if (label.length() > 22) {
                     label = label.substring(0, 20) + "..";
                  }

                  if (this.selectedTab == 2) {
                     label = "✓ " + label;
                  }

                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(400 + questFlatIndex, this.guiLeft + 16, btnY, 133, listHeight, label, topColor, botColor));
                  yOffset += listHeight + 1;
               }

               ++visualRow;
               ++questFlatIndex;
            }
         } else {
            List<QuestClientData.QuestListEntry> quests = this.getQuestsForStoryline(storyline);
            questFlatIndex += quests.size();
         }
      }

      int bottomY = this.guiTop + 280 - 30;
      int actionAreaX = this.guiLeft + 155 + 10;
      int actionAreaW = 189;
      if (this.selectedQuestIndex >= 0) {
         List<QuestClientData.QuestListEntry> list = this.getQuestsForCurrentView();
         if (this.selectedQuestIndex < list.size() && ((QuestClientData.QuestListEntry)list.get(this.selectedQuestIndex)).available && this.selectedTab == 0) {
            int btnW = 90;
            int btnX = actionAreaX + (actionAreaW - btnW) / 2;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1, btnX, bottomY, btnW, 22, "⚔ Accept", -12944838, -14788066));
         }
      }

      if (QuestClientData.hasActiveQuestInSlot("story")) {
         int btnW = 90;
         int btnX = actionAreaX + (actionAreaW - btnW) / 2;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(2, btnX, bottomY, btnW, 22, "Abandon", -7718358, -9819622));
      }

   }

   private int endgameSubTabForPveTab(int pveTab) {
      switch (pveTab) {
         case 3:
            return 0;
         case 4:
         default:
            return -1;
         case 5:
            return 1;
         case 6:
            return 2;
         case 7:
            return 3;
      }
   }

   private void rebuildPveButtons() {
      int subTabY = this.guiTop + 48;
      int tabW = 40;
      int tabSpacing = 1;
      int totalSubW = tabW * 8 + tabSpacing * 7;
      int tabStartX = this.guiLeft + (360 - totalSubW) / 2;

      for(int i = 0; i < 8; ++i) {
         boolean sel = this.pveSubTab == i;
         GuiRankedMenu.GuiButtonGradient btn = new GuiRankedMenu.GuiButtonGradient(PVE_SUB_TAB_IDS[i], tabStartX + i * (tabW + tabSpacing), subTabY, tabW, 14, (sel ? "§f" : "§8") + PVE_SUB_TAB_NAMES[i], sel ? PVE_SUB_TAB_COLORS[i][0] : PVE_SUB_TAB_COLORS[i][1], sel ? PVE_SUB_TAB_COLORS[i][2] : PVE_SUB_TAB_COLORS[i][3]);
         if (i == 3 || i == 4 || i == 5) {
            btn.textScale = 0.7F;
         }

         this.buttonList.add(btn);
      }

      if (this.pveSubTab <= 2) {
         this.rebuildPveMissionButtons();
      } else if (this.pveSubTab == 4) {
         this.rebuildContractBoardButtons();
      } else {
         this.endgameSubTab = this.endgameSubTabForPveTab(this.pveSubTab);
         switch (this.endgameSubTab) {
            case 0:
               this.rebuildEndgameOutpostButtons();
               break;
            case 1:
               this.rebuildEndgameBingoButtons();
               break;
            case 2:
               this.rebuildEndgameEventsButtons();
               break;
            case 3:
               this.rebuildEndgameLeaderboardButtons();
         }
      }

   }

   private void rebuildPveMissionButtons() {
      if (this.isPveMultiSlot()) {
         List<String> subSlots = this.getPveSubSlots();
         if (subSlots != null) {
            int listY = this.guiTop + 66;
            int listHeight = 18;
            int listSpacing = 1;

            for(int i = 0; i < subSlots.size(); ++i) {
               String subSlot = (String)subSlots.get(i);
               int btnY = listY + i * (listHeight + listSpacing);
               boolean isSelected = i == this.selectedSubSlotIndex;
               String label = this.getPveSubSlotLabel(subSlot, i);
               int topColor;
               int botColor;
               if (isSelected) {
                  topColor = -9807302;
                  botColor = -11912662;
               } else {
                  topColor = -12962776;
                  botColor = -14015464;
               }

               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(20 + i, this.guiLeft + 8, btnY, 151, listHeight, label, topColor, botColor));
            }
         }
      }

      int bottomY = this.guiTop + 280 - 30;
      boolean hasLeftPanel = this.isPveMultiSlot();
      int actionAreaX = hasLeftPanel ? this.guiLeft + 155 + 10 : this.guiLeft + 68;
      int actionAreaW = hasLeftPanel ? 189 : 276;
      String currentSlot = this.getPveSubSlot();
      if (!QuestClientData.hasActiveQuestInSlot(currentSlot)) {
         QuestClientData.OfferState offer = QuestClientData.getOffer(currentSlot);
         if (offer != null && offer.cooldownRemaining <= 0L && offer.name != null && !offer.name.isEmpty()) {
            int btnW = 80;
            int gap = 6;
            int totalW = btnW * 2 + gap;
            int startX = actionAreaX + (actionAreaW - totalW) / 2;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(12, startX, bottomY, btnW, 22, "⚔ Accept", -12944838, -14788066));
            String baseCategory = QuestClientData.getBaseCategory(currentSlot);
            boolean canReroll;
            String rerollLabel;
            if ("random".equals(baseCategory)) {
               rerollLabel = "Reroll (∞)";
               canReroll = true;
            } else {
               int remaining = offer.rerollsRemaining;
               rerollLabel = "Reroll (" + remaining + "/5)";
               canReroll = remaining > 0;
            }

            int rerollTopColor = canReroll ? -10855798 : -12961222;
            int rerollBotColor = canReroll ? -12961174 : -14013910;
            GuiRankedMenu.GuiButtonGradient rerollBtn = new GuiRankedMenu.GuiButtonGradient(11, startX + btnW + gap, bottomY, btnW, 22, rerollLabel, rerollTopColor, rerollBotColor);
            rerollBtn.enabled = canReroll;
            this.buttonList.add(rerollBtn);
         }
      } else {
         int btnW = 90;
         int btnX = actionAreaX + (actionAreaW - btnW) / 2;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(2, btnX, bottomY, btnW, 22, "Abandon", -7718358, -9819622));
      }

   }

   private void rebuildPvpButtons() {
      int subTabY = this.guiTop + 48;
      int tabW = 44;
      int tabSpacing = 2;
      int tabCount = 6;
      int tabsTotalW = tabCount * tabW + (tabCount - 1) * tabSpacing;
      int tabStartX = this.guiLeft + (360 - tabsTotalW) / 2;
      int idx = 0;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(16, tabStartX + idx * (tabW + tabSpacing), subTabY, tabW, 14, this.pvpSubTab == 0 ? "§fDaily" : "§8Daily", this.pvpSubTab == 0 ? -9807312 : -13620192, this.pvpSubTab == 0 ? -11912678 : -14671848));
      ++idx;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(17, tabStartX + idx * (tabW + tabSpacing), subTabY, tabW, 14, this.pvpSubTab == 1 ? "§fWeekly" : "§8Weekly", this.pvpSubTab == 1 ? -9807312 : -13620192, this.pvpSubTab == 1 ? -11912678 : -14671848));
      ++idx;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(18, tabStartX + idx * (tabW + tabSpacing), subTabY, tabW, 14, this.pvpSubTab == 2 ? "§fRandom" : "§8Random", this.pvpSubTab == 2 ? -9807312 : -13620192, this.pvpSubTab == 2 ? -11912678 : -14671848));
      ++idx;
      GuiRankedMenu.GuiButtonGradient bingoTab = new GuiRankedMenu.GuiButtonGradient(19, tabStartX + idx * (tabW + tabSpacing), subTabY, tabW, 14, this.pvpSubTab == 3 ? "§fBingo Book" : "§8Bingo Book", this.pvpSubTab == 3 ? -9807312 : -13620192, this.pvpSubTab == 3 ? -11912678 : -14671848);
      bingoTab.textScale = 0.7F;
      this.buttonList.add(bingoTab);
      ++idx;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(75, tabStartX + idx * (tabW + tabSpacing), subTabY, tabW, 14, this.pvpSubTab == 4 ? "§fOrders" : "§8Orders", this.pvpSubTab == 4 ? -9807312 : -13620192, this.pvpSubTab == 4 ? -11912678 : -14671848));
      ++idx;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(79, tabStartX + idx * (tabW + tabSpacing), subTabY, tabW, 14, this.pvpSubTab == 5 ? "§fTourney" : "§8Tourney", this.pvpSubTab == 5 ? -9807312 : -13620192, this.pvpSubTab == 5 ? -11912678 : -14671848));
      int bottomY = this.guiTop + 280 - 30;
      if (this.pvpSubTab == 5) {
         this.rebuildTournamentButtons(bottomY);
      } else if (this.pvpSubTab == 4) {
         this.rebuildLeadershipButtons(bottomY);
      } else if (this.pvpSubTab == 3) {
         List<PvpClientData.BingoInfo> entries = PvpClientData.getBingoEntries();
         int listY = this.guiTop + 66;
         int listHeight = 18;
         int maxVisible = 9;

         for(int i = this.bingoScrollOffset; i < Math.min(entries.size(), this.bingoScrollOffset + maxVisible); ++i) {
            PvpClientData.BingoInfo entry = (PvpClientData.BingoInfo)entries.get(i);
            int btnY = listY + (i - this.bingoScrollOffset) * (listHeight + 1);
            String label = entry.targetName;
            if (label.length() > 18) {
               label = label.substring(0, 16) + "..";
            }

            if (entry.claimed) {
               label = "§m" + label;
            } else {
               label = "§c⚔ " + label;
            }

            int topColor = entry.claimed ? -14014432 : -11917272;
            int botColor = entry.claimed ? -15067120 : -12969960;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(400 + i, this.guiLeft + 8, btnY, 151, listHeight, label, topColor, botColor));
         }

         if (this.selectedQuestIndex >= 0 && this.selectedQuestIndex < entries.size()) {
            PvpClientData.BingoInfo b = (PvpClientData.BingoInfo)entries.get(this.selectedQuestIndex);
            if (!b.claimed) {
               int btnW = 90;
               int actionAreaX = this.guiLeft + 155 + 10;
               int actionAreaW = 189;
               int btnX = actionAreaX + (actionAreaW - btnW) / 2;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(33, btnX, bottomY, btnW, 22, "⚔ Claim", -8766918, -10872294));
            }
         }
      } else {
         boolean weeklyLocked = this.pvpSubTab == 1 && PvpClientData.playerPvpRankOrdinal == 0;
         if (this.isPvpMultiSlot() && !weeklyLocked) {
            String[] slots = this.pvpSubTab == 0 ? PvpClientData.PVP_DAILY_SLOTS : PvpClientData.PVP_WEEKLY_SLOTS;
            int listY = this.guiTop + 66;
            int listHeight = 18;
            int listSpacing = 1;

            for(int i = 0; i < slots.length; ++i) {
               String subSlot = slots[i];
               int btnY = listY + i * (listHeight + listSpacing);
               boolean isSelected = i == this.pvpSubSlotIndex;
               String label = this.getPvpSubSlotLabel(subSlot, i);
               int topColor;
               int botColor;
               if (isSelected) {
                  topColor = -9815494;
                  botColor = -11916758;
               } else {
                  topColor = -12965848;
                  botColor = -14018536;
               }

               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(35 + i, this.guiLeft + 8, btnY, 151, listHeight, label, topColor, botColor));
            }
         }

         if (weeklyLocked) {
            return;
         }

         boolean hasLeftPanel = this.isPvpMultiSlot();
         int actionAreaX = hasLeftPanel ? this.guiLeft + 155 + 10 : this.guiLeft + 68;
         int actionAreaW = hasLeftPanel ? 189 : 276;
         String currentSlot = this.getPvpSubSlot();
         if (!PvpClientData.hasActiveMission(currentSlot)) {
            PvpClientData.PvpOfferInfo offer = PvpClientData.getOffer(currentSlot);
            if (offer != null && !offer.isOnCooldown() && offer.name != null && !offer.name.isEmpty()) {
               int btnW = 80;
               int gap = 6;
               int totalW = btnW * 2 + gap;
               int startX = actionAreaX + (actionAreaW - totalW) / 2;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(30, startX, bottomY, btnW, 22, "⚔ Accept", -8766918, -10872294));
               String baseCategory = PvpClientData.getBaseCategory(currentSlot);
               String rerollLabel;
               boolean canReroll;
               if ("pvp_random".equals(baseCategory)) {
                  rerollLabel = "Reroll (∞)";
                  canReroll = true;
               } else {
                  int remaining = offer.rerollsRemaining;
                  rerollLabel = "Reroll (" + remaining + "/5)";
                  canReroll = remaining > 0;
               }

               int rerollTopColor = canReroll ? -10859926 : -12961222;
               int rerollBotColor = canReroll ? -12965302 : -14013910;
               GuiRankedMenu.GuiButtonGradient rerollBtn = new GuiRankedMenu.GuiButtonGradient(31, startX + btnW + gap, bottomY, btnW, 22, rerollLabel, rerollTopColor, rerollBotColor);
               rerollBtn.enabled = canReroll;
               this.buttonList.add(rerollBtn);
            }
         } else {
            int btnW = 90;
            int btnX = actionAreaX + (actionAreaW - btnW) / 2;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(32, btnX, bottomY, btnW, 22, "Abandon", -7718358, -9819622));
         }
      }

   }

   private void rebuildWarButtons() {
      int bottomY = this.guiTop + 280 - 30;
      int panelMidX = this.guiLeft + 180;
      if (PvpClientData.isKage || PvpClientData.hasWarAuthority) {
         List<PvpClientData.WarClientInfo> wars = PvpClientData.getActiveWars();
         PvpClientData.WarClientInfo myWar = null;

         for(PvpClientData.WarClientInfo w : wars) {
            if (w.involvesVillage(PvpClientData.kageVillage)) {
               myWar = w;
               break;
            }
         }

         if (myWar != null) {
            this.warDeclareStep = 0;
            int surrenderY = this.guiTop + 280 - 112;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(51, this.guiLeft + 360 - 100, surrenderY, 80, 16, "☠ Surrender", -7722454, -9823718));
            if (myWar.lobbyState >= 1 && myWar.lobbyState <= 3) {
               boolean myRosterLocked = myWar.isMyRosterLocked(PvpClientData.kageVillage);
               if (!myRosterLocked) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(72, panelMidX - 70, bottomY - 24, 140, 20, "✎ Select Roster", -11904454, -14009830));
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(67, panelMidX - 75, bottomY, 60, 20, "Lock", -10855798, -12961174));
               }

               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(68, panelMidX + 15, bottomY, 60, 20, "Cancel", -9811398, -11916774));
               if (myWar.lobbyState == 4) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(69, panelMidX - 45, bottomY - 24, 90, 20, "⚔ Start War", -12944838, -14788066));
               }
            }
         } else if (this.warDeclareStep == 0) {
            int declareBtnY = this.guiTop + 280 - 70;
            if (PvpClientData.warCooldownRemainingMs > 0L) {
               String cdStr = formatCooldown(PvpClientData.warCooldownRemainingMs);
               GuiRankedMenu.GuiButtonGradient btn = new GuiRankedMenu.GuiButtonGradient(50, panelMidX - 70, declareBtnY, 140, 22, "Cooldown: " + cdStr, -12961222, -14013910);
               btn.enabled = false;
               this.buttonList.add(btn);
            } else {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(50, panelMidX - 70, declareBtnY, 140, 22, "⚔ Declare War", -7714262, -9819622));
            }
         } else if (this.warDeclareStep == 1) {
            int modeW = 120;
            int modeStartX = this.guiLeft + 16;
            int modeStartY = this.guiTop + 70;
            int modeSpacing = 20;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(52, modeStartX, modeStartY, modeW, 16, "Skirmish", -10859984, -12963816));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(53, modeStartX, modeStartY + modeSpacing, modeW, 16, "10v10", -10864070, -12967904));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(54, modeStartX, modeStartY + modeSpacing * 2, modeW, 16, "Domination", -12957094, -14669254));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(55, modeStartX, modeStartY + modeSpacing * 3, modeW, 16, "Rush", -12953030, -14663648));
            int nextModeSlot = 4;
            if ("Akatsuki".equalsIgnoreCase(PvpClientData.kageVillage)) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(57, modeStartX, modeStartY + modeSpacing * nextModeSlot, modeW, 16, "§4Infiltration", -10872294, -12973558));
               ++nextModeSlot;
            }

            int cancelY = modeStartY + modeSpacing * nextModeSlot;
            int maxCancelY = this.guiTop + 280 - 114 - 18;
            if (cancelY > maxCancelY) {
               cancelY = maxCancelY;
            }

            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(68, modeStartX, cancelY, 60, 16, "Cancel", -9811398, -11916774));
         } else if (this.warDeclareStep == 2) {
            VillageHelper.Village[] villages = VillageHelper.Village.values();
            int targetY = this.guiTop + 120;
            int btnW = 70;
            int btnSpacing = 4;
            int col = 0;
            int row = 0;

            for(int i = 0; i < villages.length; ++i) {
               VillageHelper.Village v = villages[i];
               if (v != VillageHelper.Village.UNKNOWN && !v.villageName.equals(PvpClientData.kageVillage) && (v != VillageHelper.Village.AKATSUKI || "Akatsuki".equalsIgnoreCase(PvpClientData.kageVillage))) {
                  int bx = this.guiLeft + 20 + col * (btnW + btnSpacing);
                  int by = targetY + row * 22;
                  boolean isTargetSelected = v.teamName.equals(this.warSelectedTarget);
                  int[] vColors = getVillageColors(v);
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(60 + i, bx, by, btnW, 18, isTargetSelected ? "§f" + v.villageName : "§7" + v.villageName, isTargetSelected ? -9815494 : vColors[0], isTargetSelected ? -11920870 : vColors[1]));
                  ++col;
                  if (col >= 4) {
                     col = 0;
                     ++row;
                  }
               }
            }

            if (this.warSelectedTarget != null) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(66, panelMidX - 45, this.guiTop + 210, 90, 22, "Confirm", -12944838, -14788066));
            }

            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(68, panelMidX + 50, this.guiTop + 210, 60, 22, "Cancel", -9811398, -11916774));
         }

      }
   }

   private void rebuildTerritoryButtons() {
      int bottomY = this.guiTop + 280 - 30;
      TerritoryClientData tData = TerritoryClientData.getInstance();
      String toggleLabel = tData.isPlayerToggleState() ? "§aTerritory Mode: ON" : "§7Territory Mode: OFF";
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(600, this.guiLeft + 180 - 70, bottomY, 140, 18, toggleLabel, -12953030, -14008280));
   }

   private void rebuildLeadershipButtons(int bottomY) {
      int actionAreaX = this.guiLeft + 16;
      int actionAreaW = 328;
      boolean hasAuthority = PvpClientData.isKage || PvpClientData.isAdvisor || PvpClientData.hasWarAuthority;
      int svTabW = 62;
      int svTabSpacing = 3;
      int svTabCount = hasAuthority ? 3 : 2;
      int svTotalW = svTabCount * svTabW + (svTabCount - 1) * svTabSpacing;
      int svStartX = this.guiLeft + (360 - svTotalW) / 2;
      int svTabY = this.guiTop + 63;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(150, svStartX, svTabY, svTabW, 13, this.leadershipSubView == 0 ? "§fOverview" : "§8Overview", this.leadershipSubView == 0 ? -9807312 : -13620192, this.leadershipSubView == 0 ? -11912678 : -14671848));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(151, svStartX + svTabW + svTabSpacing, svTabY, svTabW, 13, this.leadershipSubView == 1 ? "§fMy Mission" : "§8My Mission", this.leadershipSubView == 1 ? -9807312 : -13620192, this.leadershipSubView == 1 ? -11912678 : -14671848));
      if (hasAuthority) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(152, svStartX + (svTabW + svTabSpacing) * 2, svTabY, svTabW, 13, this.leadershipSubView == 2 ? "§fIssue Order" : "§8Issue Order", this.leadershipSubView == 2 ? -9807312 : -13620192, this.leadershipSubView == 2 ? -11912678 : -14671848));
      }

      switch (this.leadershipSubView) {
         case 0:
            this.rebuildLeadershipOverviewButtons(actionAreaX, actionAreaW, bottomY, hasAuthority);
            break;
         case 1:
            this.rebuildLeadershipMissionButtons(actionAreaX, actionAreaW, bottomY);
            break;
         case 2:
            this.rebuildLeadershipOrderFlowButtons(actionAreaX, actionAreaW, bottomY);
      }

   }

   private void rebuildLeadershipOverviewButtons(int actionAreaX, int actionAreaW, int bottomY, boolean hasAuthority) {
      if (hasAuthority) {
         List<PvpClientData.OrderClientInfo> orders = PvpClientData.getActiveOrders();
         int orderY = this.guiTop + 145 - this.ordersOverviewScrollOffset;

         for(int i = 0; i < Math.min(orders.size(), 2); ++i) {
            boolean visible = orderY >= this.guiTop + 80 && orderY + 14 <= this.guiTop + 280 - 38;
            if (visible) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(81 + i, this.guiLeft + 360 - 75, orderY, 44, 14, "Cancel", -9815494, -11916758));
            }

            PvpClientData.OrderClientInfo order = (PvpClientData.OrderClientInfo)orders.get(i);
            if (order.isAssignedMission()) {
               PvpClientData.OperationProgressInfo opProgress = PvpClientData.getOperationProgressForOrder(order.orderId);
               orderY += 23;
               if (opProgress != null) {
                  orderY += 86;
               }
            } else {
               orderY += 36;
            }
         }

      }
   }

   private void rebuildLeadershipMissionButtons(int actionAreaX, int actionAreaW, int bottomY) {
      boolean isLeader = PvpClientData.isKage || PvpClientData.isAdvisor || PvpClientData.hasWarAuthority;
      PvpClientData.ActivePvpMissionInfo assignedMission = PvpClientData.getActiveMission("pvp_assigned");
      List<PvpClientData.OrderClientInfo> assignOrders = new ArrayList();

      for(PvpClientData.OrderClientInfo o : PvpClientData.getActiveOrders()) {
         if (o.isAssignedMission()) {
            assignOrders.add(o);
         }
      }

      boolean hasPendingOrders = !assignOrders.isEmpty() && assignedMission == null;
      if (assignedMission != null) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(32, actionAreaX + (actionAreaW - 130) / 2, bottomY, 130, 20, "§c☆ Abandon Village Op", -7718358, -9819622));
         if (isLeader && PvpClientData.leadershipMission != null) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(78, actionAreaX + (actionAreaW - 140) / 2, bottomY - 24, 140, 20, "§c✦ Abandon Leader", -7718358, -9819622));
         }

      } else if (isLeader && PvpClientData.leadershipMission != null) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(78, actionAreaX + (actionAreaW - 140) / 2, bottomY, 140, 20, "§c✦ Abandon Leader Mission", -7718358, -9819622));
      } else {
         int gap = 3;
         int curX = this.guiLeft + 62;
         if (isLeader && PvpClientData.leadershipOffer != null) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(76, curX, bottomY, 86, 20, "✦ Accept Leader", -12944838, -14788066));
            curX += 86 + gap;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(77, curX, bottomY, 55, 20, "Reroll", -10859926, -12965302));
            curX += 55 + gap;
         }

         if (hasPendingOrders) {
            int orderIdx = 0;

            for(PvpClientData.OrderClientInfo order : assignOrders) {
               if (orderIdx >= 3) {
                  break;
               }

               String label = assignOrders.size() > 1 ? "☆ Order " + (orderIdx + 1) : "☆ Accept Order";
               int w = assignOrders.size() > 1 ? 65 : 85;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(170 + orderIdx, curX, bottomY, w, 20, label, -14001558, -15058358));
               curX += w + gap;
               ++orderIdx;
            }
         }

      }
   }

   private void rebuildLeadershipOrderFlowButtons(int actionAreaX, int actionAreaW, int bottomY) {
      if (this.leadershipStep == 0) {
         int btnW2 = 110;
         int gap2 = 10;
         int startX2 = actionAreaX + (actionAreaW - btnW2 * 2 - gap2) / 2;
         int btnY = this.guiTop + 118;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(93, startX2, btnY, btnW2, 18, "Target Order", -10851782, -12957158));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(80, startX2 + btnW2 + gap2, btnY, btnW2, 18, "Assign Mission", -12952982, -15058358));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(92, actionAreaX + (actionAreaW - 60) / 2, bottomY, 60, 22, "Back", -9811398, -11916774));
      } else if (this.leadershipStep == 1) {
         VillageHelper.Village[] villages = VillageHelper.Village.values();
         int targetY = this.guiTop + 170;
         int btnW = 70;
         int btnSpacing = 4;
         int col = 0;
         int row = 0;

         for(int i = 0; i < villages.length; ++i) {
            VillageHelper.Village v = villages[i];
            if (v != VillageHelper.Village.UNKNOWN && !v.villageName.equals(PvpClientData.kageVillage) && !v.villageName.equals(PvpClientData.advisorVillage)) {
               int bx = this.guiLeft + 20 + col * (btnW + btnSpacing);
               int by = targetY + row * 22;
               int[] vColors = getVillageColors(v);
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(85 + i, bx, by, btnW, 18, "§7" + v.villageName, vColors[0], vColors[1]));
               ++col;
               if (col >= 4) {
                  col = 0;
                  ++row;
               }
            }
         }

         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(92, actionAreaX + (actionAreaW - 60) / 2, bottomY, 60, 22, "Cancel", -9811398, -11916774));
      } else if (this.leadershipStep == 2) {
         List<PvpMissionTemplate> templates = PvpMissionTemplate.getAssignable();
         int templateY = this.guiTop + 123;
         int btnW = actionAreaW - 30;
         int maxVisible = 4;
         if (this.assignTemplateScrollOffset > Math.max(0, templates.size() - maxVisible)) {
            this.assignTemplateScrollOffset = Math.max(0, templates.size() - maxVisible);
         }

         int[][] templateColors = new int[][]{{-11912662, -12965350}, {-14009782, -15062470}, {-12957126, -14009814}, {-11912630, -12965318}, {-11908566, -12961254}, {-12961206, -14013894}, {-11916742, -12969430}};

         for(int i = this.assignTemplateScrollOffset; i < Math.min(templates.size(), this.assignTemplateScrollOffset + maxVisible); ++i) {
            int colorIdx = i % templateColors.length;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(180 + i, actionAreaX + 5, templateY, btnW, 14, "§b" + ((PvpMissionTemplate)templates.get(i)).getNamePattern(), templateColors[colorIdx][0], templateColors[colorIdx][1]));
            templateY += 28;
         }

         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(92, actionAreaX + (actionAreaW - 60) / 2, bottomY, 60, 22, "Cancel", -9811398, -11916774));
      }

   }

   private void rebuildTournamentButtons(int bottomY) {
      PvpClientData.TournamentClientInfo info = PvpClientData.tournamentInfo;
      if (info == null) {
         boolean isOp = this.mc.player != null && this.mc.player.canUseCommand(2, "");
         if (PvpClientData.isKage || PvpClientData.isAdvisor || isOp) {
            String minLabel = "Min: " + (this.tournamentMinRank < 0 ? "Any" : PVP_RANK_DISPLAY[this.tournamentMinRank]);
            String maxLabel = "Max: " + (this.tournamentMaxRank < 0 ? "Any" : PVP_RANK_DISPLAY[this.tournamentMaxRank]);
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(138, this.guiLeft + 180 - 90, this.guiTop + 120, 85, 16, minLabel, -11908550, -12961238));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(139, this.guiLeft + 180 + 5, this.guiTop + 120, 85, 16, maxLabel, -11908550, -12961238));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(132, this.guiLeft + 180 - 60, this.guiTop + 145, 120, 22, "⚔ Create Tournament", -12944838, -14788066));
         }

      } else {
         byte state = info.state;
         if (state != 0 || !PvpClientData.isKage && !PvpClientData.isAdvisor) {
            if (state == 1) {
               if (!info.isPlayerSignedUp) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(130, this.guiLeft + 180 - 50, bottomY, 100, 22, "⚔ Sign Up", -12944838, -14788066));
               } else {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(131, this.guiLeft + 180 - 50, bottomY, 100, 22, "Withdraw", -7718358, -9819622));
               }

               if (PvpClientData.isKage || PvpClientData.isAdvisor) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(134, this.guiLeft + 360 - 70, bottomY, 55, 22, "Cancel", -7718358, -9819622));
               }
            } else if ((state == 3 || state == 2) && (PvpClientData.isKage || PvpClientData.isAdvisor)) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(134, this.guiLeft + 360 - 70, bottomY, 55, 22, "Cancel", -7718358, -9819622));
            }
         } else {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(135, this.guiLeft + 20, bottomY - 30, 80, 18, "1st Rewards", -5601280, -7838208));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(136, this.guiLeft + 105, bottomY - 30, 80, 18, "2nd Rewards", -7829368, -10066330));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(137, this.guiLeft + 190, bottomY - 30, 80, 18, "3rd Rewards", -7710160, -9815528));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(133, this.guiLeft + 180 - 50, bottomY, 100, 22, "Open Signups", -12944838, -14788066));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(134, this.guiLeft + 360 - 70, bottomY, 55, 22, "Cancel", -7718358, -9819622));
            String voLabel = "Village Only: " + (info.villageOnly ? "ON" : "OFF");
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(140, this.guiLeft + 20, bottomY - 55, 100, 18, voLabel, -11908550, -12961238));
         }

      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      float glowIntensity = (float)(0.2 + 0.08 * Math.sin((double)this.animationTick));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 4, this.guiTop - 4, this.guiLeft + 360 + 4, this.guiTop + 280 + 4, glowAlpha << 24 | 9071162);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 360, this.guiTop + 280, -298833888, -300280816);
      this.drawBorderWithShading(this.guiLeft, this.guiTop, 360, 280, -7706054, 2);
      this.drawBorderWithShading(this.guiLeft + 4, this.guiTop + 4, 352, 272, -9809872, 1);
      this.drawScrollCorners();
      String title = "§lIceeRamen: Infinite Tsukuyomi Chapter 2";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawString(title, this.guiLeft + (360 - titleWidth) / 2 + 1, this.guiTop + 10 + 1, 1709072);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (360 - titleWidth) / 2), (float)(this.guiTop + 10), -1521552);
      this.drawHorizontalLine(this.guiLeft + 20, this.guiLeft + 180 - 10, this.guiTop + 24, -9807296);
      this.drawHorizontalLine(this.guiLeft + 180 + 10, this.guiLeft + 360 - 20, this.guiTop + 24, -9807296);
      this.drawDiamond(this.guiLeft + 180, this.guiTop + 24, 5, -7706054);
      switch (this.selectedCategory) {
         case 0:
            this.drawStoryContent(mouseX, mouseY);
            break;
         case 1:
            this.drawPveContent(mouseX, mouseY);
            break;
         case 2:
            this.drawPvpContent(mouseX, mouseY);
            break;
         case 3:
            this.drawWarContent(mouseX, mouseY);
         case 4:
         case 6:
         default:
            break;
         case 5:
            this.drawStatContent(mouseX, mouseY);
            break;
         case 7:
            this.drawFactionContent(mouseX, mouseY);
      }

      this.drawHorizontalLine(this.guiLeft + 20, this.guiLeft + 360 - 20, this.guiTop + 280 - 36, -9807296);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawStoryContent(int mouseX, int mouseY) {
      this.drawPanelWithLighting(this.guiLeft + 6, this.guiTop + 63, 155, this.guiTop + 280 - 38 - (this.guiTop + 63));
      int storyMaxVis = 9;
      if (this.storyTotalRows > storyMaxVis) {
         this.drawScrollBar(this.guiLeft + 6 + 155 - 6, this.guiTop + 66, storyMaxVis * 19, this.storyTotalRows, storyMaxVis, this.scrollOffset, Math.max(1, this.storyTotalRows - storyMaxVis));
      }

      int rightPanelX = this.guiLeft + 155 + 10;
      int rightPanelW = 189;
      this.drawPanelWithLighting(rightPanelX, this.guiTop + 63, rightPanelW, 180);
      this.drawStoryDetails(rightPanelX + 8, this.guiTop + 68, rightPanelW - 16);
   }

   private void drawStoryDetails(int detailX, int detailY, int maxWidth) {
      boolean showingActive = this.selectedTab == 1;
      if (showingActive) {
         QuestClientData.ActiveQuestState aq = QuestClientData.getActiveQuestInSlot("story");
         if (aq != null) {
            this.drawActiveQuestDetails(detailX, detailY, maxWidth, aq);
            return;
         }
      }

      List<QuestClientData.QuestListEntry> quests = this.getQuestsForCurrentView();
      if (this.selectedQuestIndex >= 0 && this.selectedQuestIndex < quests.size()) {
         QuestClientData.QuestListEntry entry = (QuestClientData.QuestListEntry)quests.get(this.selectedQuestIndex);
         this.fontRenderer.drawStringWithShadow(entry.name, (float)detailX, (float)detailY, -1521552);
         detailY += 14;
         String catLabel = getArcDisplayName(entry.arc);
         this.fontRenderer.drawStringWithShadow(catLabel, (float)detailX, (float)detailY, -7700886);
         detailY += 14;
         this.drawHorizontalLine(detailX, detailX + maxWidth, detailY - 3, -11910088);
         if (entry.description != null && !entry.description.isEmpty()) {
            for(String line : this.fontRenderer.listFormattedStringToWidth(entry.description, maxWidth)) {
               this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -2832216);
               detailY += 11;
            }
         }

         detailY += 10;
         if (entry.available) {
            this.fontRenderer.drawStringWithShadow("✓ Available", (float)detailX, (float)detailY, -10831270);
         } else if (this.selectedTab == 2) {
            this.fontRenderer.drawStringWithShadow("✓ Completed", (float)detailX, (float)detailY, -10831270);
         } else {
            this.fontRenderer.drawStringWithShadow("✗ Prerequisites not met", (float)detailX, (float)detailY, -4564390);
         }

         if (entry.rewardSummary != null && !entry.rewardSummary.isEmpty()) {
            detailY += 14;
            this.fontRenderer.drawStringWithShadow("Reward: " + entry.rewardSummary, (float)detailX, (float)detailY, -7700886);
         }
      } else {
         this.drawCategoryHint(detailX, detailY, maxWidth, "Select a quest to view details.");
      }

   }

   private void drawPveContent(int mouseX, int mouseY) {
      if (this.pveSubTab == 4) {
         this.drawContractBoardContent(mouseX, mouseY);
      } else if (this.pveSubTab >= 3) {
         this.endgameSubTab = this.endgameSubTabForPveTab(this.pveSubTab);
         this.drawEndgameContent(mouseX, mouseY);
      } else {
         boolean showLeftPanel = this.isPveMultiSlot();
         if (showLeftPanel) {
            this.drawPanelWithLighting(this.guiLeft + 6, this.guiTop + 63, 155, this.guiTop + 280 - 38 - (this.guiTop + 63));
            List<QuestClientData.QuestListEntry> pveQuests = this.getQuestsForCurrentView();
            int pveMaxVis = 9;
            if (pveQuests.size() > pveMaxVis) {
               this.drawScrollBar(this.guiLeft + 6 + 155 - 6, this.guiTop + 66, pveMaxVis * 19, pveQuests.size(), pveMaxVis, this.scrollOffset, Math.max(1, pveQuests.size() - pveMaxVis));
            }
         }

         int rightPanelX = showLeftPanel ? this.guiLeft + 155 + 10 : this.guiLeft + 8;
         int rightPanelW = showLeftPanel ? 189 : 344;
         this.drawPanelWithLighting(rightPanelX, this.guiTop + 63, rightPanelW, 180);
         this.drawPveRankBadge(rightPanelX + 8, this.guiTop + 66, rightPanelW - 16);
         this.drawPveDetails(rightPanelX + 8, this.guiTop + 80, rightPanelW - 16);
      }
   }

   private void drawPveDetails(int detailX, int detailY, int maxWidth) {
      String currentSlot = this.getPveSubSlot();
      if (QuestClientData.hasActiveQuestInSlot(currentSlot)) {
         QuestClientData.ActiveQuestState aq = QuestClientData.getActiveQuestInSlot(currentSlot);
         if (aq != null) {
            this.drawActiveQuestDetails(detailX, detailY, maxWidth, aq);
            return;
         }
      }

      QuestClientData.OfferState offer = QuestClientData.getOffer(currentSlot);
      if (offer != null) {
         if (offer.cooldownRemaining > 0L) {
            String timeStr = formatCooldown(offer.cooldownRemaining);
            int hintY = detailY + 40;
            String msg = "Quest completed! Next available in:";
            int w = this.fontRenderer.getStringWidth(msg);
            this.fontRenderer.drawStringWithShadow(msg, (float)(detailX + (maxWidth - w) / 2), (float)hintY, -7700886);
            hintY += 16;
            String timeDisplay = "§e" + timeStr;
            w = this.fontRenderer.getStringWidth(timeDisplay);
            this.fontRenderer.drawStringWithShadow(timeDisplay, (float)(detailX + (maxWidth - w) / 2), (float)hintY, -2839996);
         } else if (offer.name != null && !offer.name.isEmpty()) {
            this.drawPveOfferDetails(detailX, detailY, maxWidth, offer, currentSlot);
         } else {
            this.drawPveHint(detailX, detailY, maxWidth);
         }
      } else {
         this.drawPveHint(detailX, detailY, maxWidth);
      }

   }

   private void drawPveOfferDetails(int detailX, int detailY, int maxWidth, QuestClientData.OfferState offer, String slot) {
      this.fontRenderer.drawStringWithShadow("Mission Offer", (float)detailX, (float)detailY, -1521552);
      detailY += 16;
      String rankBadge = "";
      if (offer.rank >= 0 && offer.rank < RANK_NAMES.length) {
         rankBadge = getRankColorCode(offer.rank) + "[" + RANK_NAMES[offer.rank] + "] §r";
      }

      this.fontRenderer.drawStringWithShadow(rankBadge + offer.name, (float)detailX, (float)detailY, -1521552);
      detailY += 14;
      this.drawHorizontalLine(detailX, detailX + maxWidth, detailY - 2, -11910088);
      detailY += 4;
      if (offer.description != null && !offer.description.isEmpty()) {
         for(String line : this.fontRenderer.listFormattedStringToWidth(offer.description, maxWidth)) {
            this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -2832216);
            detailY += 11;
         }
      }

      detailY += 8;
      this.fontRenderer.drawStringWithShadow("Enemies: §f" + offer.killCount, (float)detailX, (float)detailY, -7700886);
      detailY += 12;
      String rewardLine = "Reward: §e" + offer.xpReward + " Ninja XP";
      if (offer.ryoReward > 0) {
         rewardLine = rewardLine + "§7, §6" + offer.ryoReward + " Ryo";
      }

      int offerSP = getSPForRankOrdinal(offer.rank);
      if (offerSP > 0) {
         rewardLine = rewardLine + "§7, §d" + offerSP + " SP";
      }

      int availableWidth = maxWidth - 8;
      int pveXpReward = getPveXpForRankOrdinal(offer.rank);
      if (pveXpReward > 0) {
         String pveXpStr = "§b+" + pveXpReward + " PvE XP";
         if (this.fontRenderer.getStringWidth(rewardLine + ", " + pveXpStr) > availableWidth) {
            this.fontRenderer.drawStringWithShadow(rewardLine, (float)detailX, (float)detailY, -7700886);
            detailY += 10;
            this.fontRenderer.drawStringWithShadow(pveXpStr, (float)detailX, (float)detailY, -7700886);
         } else {
            rewardLine = rewardLine + "§7, " + pveXpStr;
            this.fontRenderer.drawStringWithShadow(rewardLine, (float)detailX, (float)detailY, -7700886);
         }

         detailY += 12;
      } else {
         this.fontRenderer.drawStringWithShadow(rewardLine, (float)detailX, (float)detailY, -7700886);
         detailY += 12;
      }

      detailY += 4;
      String baseCategory = QuestClientData.getBaseCategory(slot);
      String rerollInfo;
      if ("random".equals(baseCategory)) {
         rerollInfo = "Rerolls: §a∞";
      } else {
         int remaining = offer.rerollsRemaining;
         String color = remaining > 0 ? "§a" : "§c";
         rerollInfo = "Rerolls: " + color + remaining + "/5";
      }

      this.fontRenderer.drawStringWithShadow(rerollInfo, (float)detailX, (float)detailY, -7700886);
   }

   private void drawPveHint(int detailX, int detailY, int maxWidth) {
      String hint;
      switch (this.pveSubTab) {
         case 0:
            hint = "Complete up to 3 daily missions. Each resets every 24 hours. Select a mission on the left.";
            break;
         case 1:
            hint = "Complete up to 3 weekly missions. Each resets every 7 days. Select a mission on the left.";
            break;
         case 2:
            hint = "Random missions of varying difficulty. Unlimited rerolls available.";
            break;
         default:
            hint = "Select a mission to view details.";
      }

      this.drawCategoryHint(detailX, detailY, maxWidth, hint);
   }

   private int contractRowLeft() {
      return this.guiLeft + 14;
   }

   private int contractRowRight() {
      return this.guiLeft + 360 - 14;
   }

   private int contractListTop() {
      return this.guiTop + 94;
   }

   private void rebuildContractBoardButtons() {
      List<ContractNetworkMessage.ContractClientEntry> list = ContractClientData.entries;
      String acceptedId = ContractClientData.acceptedId;
      boolean hasActive = acceptedId != null && !acceptedId.isEmpty();
      int maxScroll = Math.max(0, list.size() - 4);
      if (this.boardContractScrollOffset > maxScroll) {
         this.boardContractScrollOffset = maxScroll;
      }

      if (this.boardContractScrollOffset < 0) {
         this.boardContractScrollOffset = 0;
      }

      int listTop = this.contractListTop();
      int rows = Math.min(4, Math.max(0, list.size() - this.boardContractScrollOffset));
      int acceptX = this.contractRowRight() - 54 - 8;

      for(int i = 0; i < rows; ++i) {
         int idx = this.boardContractScrollOffset + i;
         ContractNetworkMessage.ContractClientEntry e = (ContractNetworkMessage.ContractClientEntry)list.get(idx);
         boolean isAccepted = e.id.equals(acceptedId);
         int btnY = listTop + i * 36 + 10;
         String label = isAccepted ? "§fActive" : "§fAccept";
         int top = isAccepted ? -10855878 : -12944838;
         int bot = isAccepted ? -12961254 : -14788066;
         GuiRankedMenu.GuiButtonGradient btn = new GuiRankedMenu.GuiButtonGradient(560 + i, acceptX, btnY, 54, 16, label, top, bot);
         btn.enabled = !isAccepted && !hasActive;
         this.buttonList.add(btn);
      }

      if (hasActive) {
         int bottomY = this.guiTop + 280 - 30;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(559, this.contractRowRight() - 92, bottomY, 88, 18, "§fAbandon Contract", -7718342, -9823718));
      }

   }

   private void drawContractBoardContent(int mouseX, int mouseY) {
      int panelX = this.guiLeft + 8;
      int panelW = 344;
      this.drawPanelWithLighting(panelX, this.guiTop + 63, panelW, 180);
      this.fontRenderer.drawStringWithShadow("§l§6Bounty Contract Board", (float)(panelX + 8), (float)(this.guiTop + 66), -1521552);
      this.fontRenderer.drawStringWithShadow(this.buildContractCapsLine(), (float)this.contractRowLeft(), (float)(this.guiTop + 77), -7700886);
      int headerY = this.guiTop + 84;
      this.fontRenderer.drawStringWithShadow("§8Rank  Target", (float)this.contractRowLeft(), (float)headerY, -7700886);
      this.fontRenderer.drawStringWithShadow("§8Reward", (float)(this.contractRowLeft() + 200), (float)headerY, -7700886);
      this.drawHorizontalLine(this.contractRowLeft(), this.contractRowRight(), headerY + 8, -9807296);
      List<ContractNetworkMessage.ContractClientEntry> list = ContractClientData.entries;
      String acceptedId = ContractClientData.acceptedId;
      if (list.isEmpty()) {
         String empty = "§7No contracts available. The board refills automatically.";
         int w = this.fontRenderer.getStringWidth(empty);
         this.fontRenderer.drawStringWithShadow(empty, (float)(panelX + (panelW - w) / 2), (float)(this.guiTop + 120), -7700886);
      } else {
         int listTop = this.contractListTop();
         int rows = Math.min(4, Math.max(0, list.size() - this.boardContractScrollOffset));
         int acceptBtnLeft = this.contractRowRight() - 54 - 8;

         for(int i = 0; i < rows; ++i) {
            int idx = this.boardContractScrollOffset + i;
            ContractNetworkMessage.ContractClientEntry e = (ContractNetworkMessage.ContractClientEntry)list.get(idx);
            boolean isAccepted = e.id.equals(acceptedId);
            int rowTop = listTop + i * 36;
            int rowBg = isAccepted ? 1077570106 : ((i & 1) == 0 ? 538976280 : 269488136);
            drawRect(this.contractRowLeft(), rowTop, this.contractRowRight(), rowTop + 36 - 2, rowBg);
            String rankColor = this.colorForContractRank(e.rank);
            String targetLine = rankColor + "[" + e.rank + "] §f§l" + this.truncateString(e.targetName, 30);
            this.fontRenderer.drawStringWithShadow(targetLine, (float)(this.contractRowLeft() + 4), (float)(rowTop + 3), -2832216);
            this.fontRenderer.drawStringWithShadow("§8" + this.truncateString(e.locationHint, 50), (float)(this.contractRowLeft() + 4), (float)(rowTop + 14), -7700886);
            String reward = this.contractRewardLabel(e.rank);
            this.fontRenderer.drawStringWithShadow("§e" + reward, (float)(this.contractRowLeft() + 4), (float)(rowTop + 25), -8875);
         }

         int footerY = listTop + 144 + 4;
         if (list.size() > 4) {
            String scr = "[" + (this.boardContractScrollOffset + 1) + "-" + Math.min(list.size(), this.boardContractScrollOffset + 4) + " / " + list.size() + "]";
            int w = this.fontRenderer.getStringWidth(scr);
            this.fontRenderer.drawStringWithShadow(scr, (float)(this.contractRowRight() - w), (float)footerY, -7700886);
         }

      }
   }

   private String buildContractCapsLine() {
      int[] used = ContractClientData.perRankUsedToday;
      if (used == null || used.length < 5) {
         used = new int[5];
      }

      int[] caps = ContractManager.RANK_DAILY_CAP;
      String[] names = new String[]{"D", "C", "B", "A", "S"};
      StringBuilder sb = new StringBuilder("§7Today: ");
      boolean first = true;

      for(int i = 2; i < 5; ++i) {
         if (!first) {
            sb.append(" §7| ");
         }

         first = false;
         int cap = caps[i];
         if (cap <= 0) {
            sb.append("§7").append(names[i]).append(" §8∞");
         } else {
            int u = used[i];
            String color = u >= cap ? "§e" : "§a";
            sb.append("§7").append(names[i]).append(" ").append(color).append(u).append("/").append(cap);
         }
      }

      return sb.toString();
   }

   private String colorForContractRank(String rank) {
      if (rank == null) {
         return "§7";
      } else {
         switch (rank) {
            case "S":
               return "§d";
            case "A":
               return "§c";
            case "B":
               return "§6";
            case "C":
               return "§e";
            default:
               return "§7";
         }
      }
   }

   private String contractRewardLabel(String rank) {
      if (rank == null) {
         return "";
      } else {
         switch (rank) {
            case "S":
               return "§640,000 Ryo";
            case "A":
               return "§620,000 Ryo";
            case "B":
               return "§610,000 Ryo";
            default:
               return "§6-- Ryo";
         }
      }
   }

   private String truncateString(String s, int max) {
      if (s == null) {
         return "";
      } else {
         return s.length() <= max ? s : s.substring(0, Math.max(0, max - 2)) + "..";
      }
   }

   private void drawPvpContent(int mouseX, int mouseY) {
      if (this.pvpSubTab == 5) {
         this.drawPanelWithLighting(this.guiLeft + 8, this.guiTop + 63, 344, 180);
         this.drawTournamentContent(this.guiLeft + 16, this.guiTop + 68, 328);
      } else if (this.pvpSubTab == 4) {
         this.drawPanelWithLighting(this.guiLeft + 8, this.guiTop + 63, 344, 180);
         this.drawLeadershipContent(this.guiLeft + 16, this.guiTop + 68, 328);
      } else {
         boolean showLeftPanel = this.pvpSubTab != 3 ? this.isPvpMultiSlot() : true;
         if (showLeftPanel) {
            int leftTop = this.guiTop + 63;
            this.drawPanelWithLighting(this.guiLeft + 6, leftTop, 155, this.guiTop + 280 - 38 - leftTop);
            int pvpListMaxVis = 9;
            if (this.pvpSubTab == 3) {
               List<PvpClientData.BingoInfo> entries = PvpClientData.getBingoEntries();
               if (entries.size() > pvpListMaxVis) {
                  this.drawScrollBar(this.guiLeft + 6 + 155 - 6, this.guiTop + 66, pvpListMaxVis * 19, entries.size(), pvpListMaxVis, this.bingoScrollOffset, Math.max(1, entries.size() - pvpListMaxVis));
               }
            } else {
               List<QuestClientData.QuestListEntry> pvpQuests = this.getQuestsForCurrentView();
               if (pvpQuests.size() > pvpListMaxVis) {
                  this.drawScrollBar(this.guiLeft + 6 + 155 - 6, this.guiTop + 66, pvpListMaxVis * 19, pvpQuests.size(), pvpListMaxVis, this.scrollOffset, Math.max(1, pvpQuests.size() - pvpListMaxVis));
               }
            }
         }

         int rightPanelX = showLeftPanel ? this.guiLeft + 155 + 10 : this.guiLeft + 8;
         int rightPanelW = showLeftPanel ? 189 : 344;
         this.drawPanelWithLighting(rightPanelX, this.guiTop + 63, rightPanelW, 180);
         int rpx = rightPanelX + 8;
         int rpy = this.guiTop + 66;
         int rpw = rightPanelW - 16;
         String pvpRankName = PvpClientData.getPvpRankName();
         int pvpRankColor = PvpClientData.getPvpRankColor();
         String rankLabel = "§l[" + pvpRankName + "]";
         this.fontRenderer.drawStringWithShadow(rankLabel, (float)rpx, (float)rpy, pvpRankColor);
         long pvpXp = PvpClientData.playerPvpXp;
         int pvpRankOrd = PvpClientData.playerPvpRankOrdinal;
         long pvpCurThreshold = PvpClientData.getPvpRankThreshold(pvpRankOrd);
         long pvpNextThreshold = PvpClientData.getPvpRankThreshold(pvpRankOrd + 1);
         long pvpProgress = pvpXp - pvpCurThreshold;
         long pvpNeeded = pvpNextThreshold > pvpCurThreshold ? pvpNextThreshold - pvpCurThreshold : 0L;
         int rankLabelWidth = this.fontRenderer.getStringWidth(rankLabel);
         int barX = rpx + rankLabelWidth + 6;
         int barW = Math.min(rpw - rankLabelWidth - 50, 140);
         int barY = rpy + 1;
         drawRect(barX, barY, barX + barW, barY + 6, -15068144);
         if (pvpNeeded > 0L) {
            int fillW = (int)((float)pvpProgress / (float)pvpNeeded * (float)barW);
            fillW = Math.min(fillW, barW);
            drawRect(barX + 1, barY + 1, barX + 1 + fillW, barY + 5, pvpRankColor & -1426063361);
         }

         String xpText = pvpNeeded > 0L ? pvpProgress + "/" + pvpNeeded : "MAX";
         this.fontRenderer.drawStringWithShadow(xpText, (float)(barX + barW + 4), (float)(barY - 1), -7700886);
         int detailY = rpy + 14;
         if (this.pvpSubTab == 3) {
            this.drawBingoDetails(rpx, detailY, rpw);
         } else {
            this.drawPvpMissionDetails(rpx, detailY, rpw);
         }

      }
   }

   private void drawPvpMissionDetails(int detailX, int detailY, int maxWidth) {
      if (this.pvpSubTab == 1 && PvpClientData.playerPvpRankOrdinal == 0) {
         String lockMsg = "Become A Chunin for Weekly Quests!";
         int msgW = this.fontRenderer.getStringWidth(lockMsg);
         int centerX = detailX + (maxWidth - msgW) / 2;
         int centerY = detailY + 40;
         this.fontRenderer.drawStringWithShadow(lockMsg, (float)centerX, (float)centerY, -22016);
      } else {
         String currentSlot = this.getPvpSubSlot();
         if (PvpClientData.hasActiveMission(currentSlot)) {
            PvpClientData.ActivePvpMissionInfo mission = PvpClientData.getActiveMission(currentSlot);
            if (mission != null) {
               this.drawActivePvpMission(detailX, detailY, maxWidth, mission);
               return;
            }
         }

         PvpClientData.PvpOfferInfo offer = PvpClientData.getOffer(currentSlot);
         if (offer != null) {
            if (offer.isOnCooldown()) {
               String timeStr = formatCooldown(offer.cooldownRemainingMs);
               int hintY = detailY + 30;
               String msg = "Mission completed! Next available in:";
               int w = this.fontRenderer.getStringWidth(msg);
               this.fontRenderer.drawStringWithShadow(msg, (float)(detailX + (maxWidth - w) / 2), (float)hintY, -7700886);
               hintY += 16;
               String timeDisplay = "§e" + timeStr;
               w = this.fontRenderer.getStringWidth(timeDisplay);
               this.fontRenderer.drawStringWithShadow(timeDisplay, (float)(detailX + (maxWidth - w) / 2), (float)hintY, -2839996);
            } else if (offer.name != null && !offer.name.isEmpty()) {
               this.drawPvpOfferDetails(detailX, detailY, maxWidth, offer, currentSlot);
            } else {
               this.drawPvpHint(detailX, detailY, maxWidth);
            }
         } else {
            this.drawPvpHint(detailX, detailY, maxWidth);
         }

      }
   }

   private void drawActivePvpMission(int detailX, int detailY, int maxWidth, PvpClientData.ActivePvpMissionInfo mission) {
      this.fontRenderer.drawStringWithShadow("§c⚔ " + mission.name, (float)detailX, (float)detailY, -1521552);
      detailY += 14;
      String rankBadge = getRankColorCode((byte)mission.rankOrdinal) + "[" + mission.getRankName() + "]";
      this.fontRenderer.drawStringWithShadow(rankBadge + " §fPvP Mission", (float)detailX, (float)detailY, -7700886);
      detailY += 16;
      this.drawHorizontalLine(detailX, detailX + maxWidth, detailY - 3, -11910088);
      if (mission.description != null) {
         for(String line : this.fontRenderer.listFormattedStringToWidth(mission.description, maxWidth)) {
            this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -2832216);
            detailY += 11;
         }
      }

      if (mission.killsRequired > 0) {
         int var13 = detailY + 8;
         this.fontRenderer.drawStringWithShadow("Progress:", (float)detailX, (float)var13, -1521552);
         detailY = var13 + 12;
         int barW = maxWidth - 40;
         drawRect(detailX, detailY, detailX + barW, detailY + 8, -15068144);
         int fillW = (int)((float)mission.progress / (float)mission.killsRequired * (float)barW);
         fillW = Math.min(fillW, barW);
         drawRect(detailX + 1, detailY + 1, detailX + 1 + fillW, detailY + 7, -7724510);
         drawRect(detailX, detailY, detailX + barW, detailY + 1, -11910088);
         drawRect(detailX, detailY + 7, detailX + barW, detailY + 8, -15068144);
         String killText = mission.progress + "/" + mission.killsRequired;
         this.fontRenderer.drawStringWithShadow(killText, (float)(detailX + barW + 4), (float)detailY, -2832216);
      }

      if (mission.isTimeLimited()) {
         detailY += 14;
         long elapsed = System.currentTimeMillis() - mission.startTimeMs;
         long remaining = mission.timeLimitMs - elapsed;
         if (remaining < 0L) {
            remaining = 0L;
         }

         String timeStr = formatCooldown(remaining);
         this.fontRenderer.drawStringWithShadow("Time Left: §e" + timeStr, (float)detailX, (float)detailY, -7700886);
      }

   }

   private void drawPvpOfferDetails(int detailX, int detailY, int maxWidth, PvpClientData.PvpOfferInfo offer, String slot) {
      this.fontRenderer.drawStringWithShadow("§cPvP Mission Offer", (float)detailX, (float)detailY, -1521552);
      detailY += 16;
      String rankBadge = "";
      if (offer.rankOrdinal >= 0 && offer.rankOrdinal < RANK_NAMES.length) {
         rankBadge = getRankColorCode((byte)offer.rankOrdinal) + "[" + RANK_NAMES[offer.rankOrdinal] + "] §r";
      }

      this.fontRenderer.drawStringWithShadow(rankBadge + offer.name, (float)detailX, (float)detailY, -1521552);
      detailY += 14;
      this.drawHorizontalLine(detailX, detailX + maxWidth, detailY - 2, -11910088);
      detailY += 4;
      if (offer.description != null && !offer.description.isEmpty()) {
         for(String line : this.fontRenderer.listFormattedStringToWidth(offer.description, maxWidth)) {
            this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -2832216);
            detailY += 11;
         }
      }

      detailY += 8;
      this.fontRenderer.drawStringWithShadow("Target Kills: §f" + offer.killCount, (float)detailX, (float)detailY, -7700886);
      detailY += 12;
      String xpLine = "Ninja XP: §e" + offer.ninjaXpReward;
      if (offer.ryoReward > 0) {
         xpLine = xpLine + "§7, §6" + offer.ryoReward + " Ryo";
      }

      this.fontRenderer.drawStringWithShadow(xpLine, (float)detailX, (float)detailY, -7700886);
      detailY += 12;
      this.fontRenderer.drawStringWithShadow("PvP XP: §c" + offer.pvpXpReward, (float)detailX, (float)detailY, -7700886);
      detailY += 12;
      detailY += 4;
      String baseCategory = PvpClientData.getBaseCategory(slot);
      String rerollInfo;
      if ("pvp_random".equals(baseCategory)) {
         rerollInfo = "Rerolls: §a∞";
      } else {
         int remaining = offer.rerollsRemaining;
         String color = remaining > 0 ? "§a" : "§c";
         rerollInfo = "Rerolls: " + color + remaining + "/5";
      }

      this.fontRenderer.drawStringWithShadow(rerollInfo, (float)detailX, (float)detailY, -7700886);
   }

   private void drawPvpHint(int detailX, int detailY, int maxWidth) {
      String hint;
      switch (this.pvpSubTab) {
         case 0:
            hint = "PvP daily missions reset every 24 hours. Hunt enemy village players for PvP XP.";
            break;
         case 1:
            if (PvpClientData.playerPvpRankOrdinal < 1) {
               hint = "Weekly PvP missions require Chunin rank (C) or higher. Keep completing daily missions to rank up!";
            } else {
               hint = "PvP weekly missions reset every 7 days. Larger objectives with greater rewards.";
            }
            break;
         case 2:
            hint = "Random PvP missions. Unlimited rerolls available.";
            break;
         default:
            hint = "Select a mission to view details.";
      }

      this.drawCategoryHint(detailX, detailY, maxWidth, hint);
   }

   private void drawLeadershipContent(int contentX, int contentY, int contentW) {
      int detailY = contentY + 14;
      if (this.leadershipSubView != 0 && this.leadershipSubView != 1) {
         this.drawLeadershipOrderFlow(contentX, detailY, contentW);
      } else {
         ScaledResolution sr = new ScaledResolution(this.mc);
         int scaleFactor = sr.getScaleFactor();
         int scissorTop = this.guiTop + 80;
         int scissorBottom = this.guiTop + 280 - 38;
         int glX = this.guiLeft * scaleFactor;
         int glY = this.mc.displayHeight - scissorBottom * scaleFactor;
         int glW = 360 * scaleFactor;
         int glH = (scissorBottom - scissorTop) * scaleFactor;
         GL11.glEnable(3089);
         GL11.glScissor(glX, glY, glW, glH);
         int scrollOffset = this.leadershipSubView == 0 ? this.ordersOverviewScrollOffset : this.missionScrollOffset;
         int scrolledY = detailY - scrollOffset;
         if (this.leadershipSubView == 0) {
            this.drawLeadershipOverview(contentX, scrolledY, contentW);
         } else {
            this.drawLeadershipMyMission(contentX, scrolledY, contentW);
         }

         GL11.glDisable(3089);
         int maxScrollVal = 200;
         int visibleH = scissorBottom - scissorTop;
         int totalH = visibleH + maxScrollVal;
         this.drawScrollBar(this.guiLeft + 360 - 22, scissorTop, visibleH, totalH, visibleH, scrollOffset, maxScrollVal);
      }

   }

   private void drawLeadershipOverview(int contentX, int detailY, int contentW) {
      this.fontRenderer.drawStringWithShadow("§6✦ Village Leadership", (float)contentX, (float)detailY, 16777215);
      detailY += 14;
      this.fontRenderer.drawStringWithShadow("§7Kage: §f" + PvpClientData.kageName, (float)contentX, (float)detailY, 16777215);
      detailY += 11;
      String advisorStr = PvpClientData.getAdvisorNames().isEmpty() ? "None" : String.join(", ", PvpClientData.getAdvisorNames());
      this.fontRenderer.drawStringWithShadow("§7Advisors: §f" + advisorStr, (float)contentX, (float)detailY, 16777215);
      detailY += 11;
      if (PvpClientData.hasWarAuthority) {
         this.fontRenderer.drawStringWithShadow("§a✔ War Authority Active", (float)contentX, (float)detailY, 16777215);
      } else if (PvpClientData.isKage) {
         this.fontRenderer.drawStringWithShadow("§a✔ Kage", (float)contentX, (float)detailY, 16777215);
      } else if (PvpClientData.isAdvisor) {
         this.fontRenderer.drawStringWithShadow("§e✔ Advisor", (float)contentX, (float)detailY, 16777215);
      } else {
         int rankOrd = PvpClientData.playerPvpRankOrdinal;
         String rankName = rankOrd >= 0 && rankOrd < PVP_RANK_DISPLAY.length ? PVP_RANK_DISPLAY[rankOrd] : "Genin";
         this.fontRenderer.drawStringWithShadow("§7Rank: " + getRankColorCode((byte)rankOrd) + rankName, (float)contentX, (float)detailY, 16777215);
      }

      detailY += 14;
      this.drawHorizontalLine(contentX, contentX + contentW, detailY - 3, -11910088);
      List<PvpClientData.OrderClientInfo> orders = PvpClientData.getActiveOrders();
      this.fontRenderer.drawStringWithShadow("§6Active Village Orders (" + orders.size() + "/2)", (float)contentX, (float)detailY, 16777215);
      detailY += 13;
      if (orders.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No active orders.", (float)contentX, (float)detailY, 16777215);
         detailY += 14;
      } else {
         for(int i = 0; i < orders.size(); ++i) {
            PvpClientData.OrderClientInfo order = (PvpClientData.OrderClientInfo)orders.get(i);
            String typeIcon;
            switch (order.orderType) {
               case 0:
                  typeIcon = "⚔";
                  break;
               case 2:
                  typeIcon = "☆";
                  break;
               default:
                  typeIcon = "✠";
            }

            String timeStr = formatTimeRemaining(order.timeRemainingMs);
            if (!order.isAssignedMission()) {
               this.fontRenderer.drawStringWithShadow("§e" + typeIcon + " " + order.targetName + " §7- " + timeStr, (float)contentX, (float)detailY, 16777215);
               detailY += 11;
               this.fontRenderer.drawStringWithShadow("§8  Issued by: " + order.issuerName + " | §a+" + order.bonusPvpXp + " PvP XP/kill", (float)contentX, (float)detailY, 16777215);
               detailY += 11;
               this.fontRenderer.drawStringWithShadow("§7  Bonus: Kill §f" + order.targetName + "§7 shinobi for §a+" + order.bonusPvpXp + " PvP XP§7 per kill", (float)contentX, (float)detailY, 16777215);
               detailY += 14;
            } else {
               this.fontRenderer.drawStringWithShadow("§b" + typeIcon + " " + order.targetName + " §7- " + timeStr, (float)contentX, (float)detailY, 16777215);
               detailY += 11;
               this.fontRenderer.drawStringWithShadow("§8  Issued by: " + order.issuerName, (float)contentX, (float)detailY, 16777215);
               detailY += 12;
               PvpClientData.OperationProgressInfo opProgress = PvpClientData.getOperationProgressForOrder(order.orderId);
               if (opProgress != null) {
                  OperationTierTable tierTable = OperationTierTable.get(opProgress.templateId);
                  QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
                  String[] rankColors = new String[]{"§a", "§e", "§6", "§c", "§5", "§d"};
                  int gridX = contentX + 6;

                  for(int r = 0; r < Math.min(ranks.length, 6); ++r) {
                     boolean complete = opProgress.isTierComplete(r);
                     String rankName = ranks[r].displayName;
                     String roleTitle = tierTable != null ? tierTable.getRoleTitle(ranks[r]) : "";
                     int killCount = tierTable != null ? tierTable.getKillCount(ranks[r]) : 0;
                     String status = complete ? "§a✓" : "§8-";
                     String line = rankColors[r] + rankName + " §7" + roleTitle + " §8(" + killCount + " kills) " + status;
                     this.fontRenderer.drawStringWithShadow(line, (float)gridX, (float)detailY, 16777215);
                     detailY += 10;
                  }

                  int completedCount = opProgress.getCompletedTierCount();
                  int bonusPct = completedCount <= 1 ? 0 : (completedCount - 1) * 20;
                  String summary = "§e" + completedCount + "/6 tiers §7(" + bonusPct + "% bonus) §7| " + opProgress.participantCount + " deployed";
                  this.fontRenderer.drawStringWithShadow(summary, (float)gridX, (float)(detailY + 2), 16777215);
                  detailY += 14;
                  int playerRankOrd = PvpClientData.playerPvpRankOrdinal;
                  if (playerRankOrd >= 0 && playerRankOrd < ranks.length && tierTable != null) {
                     QuestDefinition.QuestRank playerQuestRank = ranks[playerRankOrd];
                     String playerRole = tierTable.getRoleTitle(playerQuestRank);
                     int playerKills = tierTable.getKillCount(playerQuestRank);
                     String roleColor = rankColors[Math.min(playerRankOrd, rankColors.length - 1)];
                     this.fontRenderer.drawStringWithShadow("§7Your Role: " + roleColor + playerRole + " §7— " + playerKills + " kills required", (float)gridX, (float)detailY, 16777215);
                     detailY += 12;
                  }
               }
            }
         }
      }

      this.drawHorizontalLine(contentX, contentX + contentW, detailY - 2, -11910088);
      detailY += 4;
      boolean isLeader = PvpClientData.isKage || PvpClientData.isAdvisor || PvpClientData.hasWarAuthority;
      if (isLeader) {
         this.fontRenderer.drawStringWithShadow("§8Use the Issue Order tab to manage village orders.", (float)contentX, (float)detailY, 16777215);
      } else {
         boolean hasAssignMission = false;

         for(PvpClientData.OrderClientInfo o : orders) {
            if (o.isAssignedMission()) {
               hasAssignMission = true;
               break;
            }
         }

         if (hasAssignMission) {
            this.fontRenderer.drawStringWithShadow("§a✦ You have a village mission! Check My Mission tab.", (float)contentX, (float)detailY, 16777215);
            detailY += 12;
         }

         this.fontRenderer.drawStringWithShadow("§8Check the My Mission tab for your assigned missions.", (float)contentX, (float)detailY, 16777215);
      }

   }

   private void drawLeadershipMyMission(int contentX, int detailY, int contentW) {
      boolean isLeader = PvpClientData.isKage || PvpClientData.isAdvisor || PvpClientData.hasWarAuthority;
      PvpClientData.ActivePvpMissionInfo assignedMission = PvpClientData.getActiveMission("pvp_assigned");
      boolean hasAssignOrder = false;

      for(PvpClientData.OrderClientInfo o : PvpClientData.getActiveOrders()) {
         if (o.isAssignedMission()) {
            hasAssignOrder = true;
            break;
         }
      }

      if (assignedMission != null) {
         this.fontRenderer.drawStringWithShadow("§b☆ Village Mission (Active)", (float)contentX, (float)detailY, 16777215);
         detailY += 14;
         String rankBadge = getRankColorCode((byte)assignedMission.rankOrdinal) + "[" + assignedMission.getRankName() + "] §r";
         this.fontRenderer.drawStringWithShadow(rankBadge + "§f§l" + assignedMission.name, (float)contentX, (float)detailY, 16777215);
         detailY += 14;
         if (assignedMission.description != null) {
            for(String line : this.fontRenderer.listFormattedStringToWidth("§7" + assignedMission.description, contentW)) {
               this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
               detailY += 11;
            }
         }

         detailY += 6;
         if (assignedMission.killsRequired > 0) {
            this.fontRenderer.drawStringWithShadow("§eProgress:", (float)contentX, (float)detailY, 16777215);
            detailY += 12;
            int barW = contentW - 60;
            drawRect(contentX, detailY, contentX + barW, detailY + 10, -15068144);
            int fillW = (int)((float)assignedMission.progress / (float)assignedMission.killsRequired * (float)barW);
            fillW = Math.min(fillW, barW);
            drawRect(contentX + 1, detailY + 1, contentX + 1 + fillW, detailY + 9, -12940613);
            String killText = assignedMission.progress + "/" + assignedMission.killsRequired + " kills";
            this.fontRenderer.drawStringWithShadow(killText, (float)(contentX + barW + 4), (float)(detailY + 1), -2832216);
            detailY += 14;
         }

         if (assignedMission.isTimeLimited()) {
            long elapsed = System.currentTimeMillis() - assignedMission.startTimeMs;
            long remaining = assignedMission.timeLimitMs - elapsed;
            if (remaining < 0L) {
               remaining = 0L;
            }

            this.fontRenderer.drawStringWithShadow("Time Left: §e" + formatCooldown(remaining), (float)contentX, (float)detailY, -7700886);
            detailY += 14;
         }
      } else if (hasAssignOrder) {
         this.fontRenderer.drawStringWithShadow("§6☆ Village Mission Available", (float)contentX, (float)detailY, -1521552);
         detailY += 14;
         int orderNum = 0;

         for(PvpClientData.OrderClientInfo order : PvpClientData.getActiveOrders()) {
            if (order.isAssignedMission()) {
               ++orderNum;
               String timeStr = formatTimeRemaining(order.timeRemainingMs);
               this.fontRenderer.drawStringWithShadow("§b☆ " + order.targetName + " §7(" + timeStr + ")", (float)contentX, (float)detailY, 16777215);
               detailY += 11;
               this.fontRenderer.drawStringWithShadow("§8  Issued by: " + order.issuerName, (float)contentX, (float)detailY, 16777215);
               detailY += 12;
               PvpClientData.OperationProgressInfo opInfo = PvpClientData.getOperationProgressForOrder(order.orderId);
               if (opInfo != null && opInfo.templateId != null) {
                  String detail = getAssignTemplateDetailDescription(opInfo.templateId);

                  for(String line : this.fontRenderer.listFormattedStringToWidth("§7" + detail, contentW)) {
                     this.fontRenderer.drawStringWithShadow(line, (float)(contentX + 4), (float)detailY, 16777215);
                     detailY += 10;
                  }
               }

               for(String line : this.fontRenderer.listFormattedStringToWidth("§aAccept below to receive a combat mission scaled to your rank.", contentW)) {
                  this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
                  detailY += 10;
               }

               detailY += 6;
               if (orderNum >= 3) {
                  break;
               }
            }
         }
      } else {
         this.fontRenderer.drawStringWithShadow("§8No village missions assigned.", (float)contentX, (float)detailY, 16777215);
         detailY += 14;
      }

      if (isLeader) {
         detailY += 4;
         this.drawHorizontalLine(contentX, contentX + contentW, detailY - 2, -11910088);
         detailY += 6;
         this.fontRenderer.drawStringWithShadow("§6✦ Leadership Mission", (float)contentX, (float)detailY, 16777215);
         detailY += 16;
         if (PvpClientData.leadershipMission != null) {
            PvpClientData.LeadershipMissionClientInfo m = PvpClientData.leadershipMission;
            String rankBadge = getRankColorCode(m.rank) + "[" + (m.rank < RANK_NAMES.length ? RANK_NAMES[m.rank] : "?") + "] §r";
            this.fontRenderer.drawStringWithShadow(rankBadge + "§f§l" + m.name, (float)contentX, (float)detailY, 16777215);
            detailY += 14;
            if (m.description != null) {
               for(String line : this.fontRenderer.listFormattedStringToWidth("§7" + m.description, contentW)) {
                  this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
                  detailY += 11;
               }
            }

            detailY += 8;
            if (m.killsRequired > 0) {
               this.fontRenderer.drawStringWithShadow("§eProgress:", (float)contentX, (float)detailY, 16777215);
               detailY += 12;
               int barW = contentW - 60;
               drawRect(contentX, detailY, contentX + barW, detailY + 10, -15068144);
               int fillW = (int)((float)m.progress / (float)m.killsRequired * (float)barW);
               fillW = Math.min(fillW, barW);
               drawRect(contentX + 1, detailY + 1, contentX + 1 + fillW, detailY + 9, -9795038);
               String killText = m.progress + "/" + m.killsRequired + " kills";
               this.fontRenderer.drawStringWithShadow(killText, (float)(contentX + barW + 4), (float)(detailY + 1), -2832216);
            }
         } else if (PvpClientData.leadershipOffer != null) {
            PvpClientData.LeadershipOfferClientInfo o = PvpClientData.leadershipOffer;
            this.fontRenderer.drawStringWithShadow("§eMission Available:", (float)contentX, (float)detailY, 16777215);
            detailY += 14;
            String rankBadge = getRankColorCode(o.rank) + "[" + (o.rank < RANK_NAMES.length ? RANK_NAMES[o.rank] : "?") + "] §r";
            this.fontRenderer.drawStringWithShadow(rankBadge + "§f§l" + o.name, (float)contentX, (float)detailY, 16777215);
            detailY += 14;
            if (o.description != null) {
               for(String line : this.fontRenderer.listFormattedStringToWidth("§7" + o.description, contentW)) {
                  this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
                  detailY += 11;
               }
            }

            detailY += 8;
            this.fontRenderer.drawStringWithShadow("§eTarget: §f" + o.killCount + " kills", (float)contentX, (float)detailY, 16777215);
            detailY += 12;
            String rewardText = "§eReward: §a" + o.ninjaXpReward + " Ninja XP§7, §d" + o.pvpXpReward + " PvP XP";
            if (o.ryoReward > 0) {
               rewardText = rewardText + "§7, §6" + o.ryoReward + " Ryo";
            }

            this.fontRenderer.drawStringWithShadow(rewardText, (float)contentX, (float)detailY, 16777215);
         } else {
            this.fontRenderer.drawStringWithShadow("§8No leadership missions available.", (float)contentX, (float)detailY, 16777215);
         }
      }

   }

   private void drawLeadershipOrderFlow(int contentX, int detailY, int contentW) {
      if (this.leadershipStep == 0) {
         this.fontRenderer.drawStringWithShadow("§6✦ Issue Village Order", (float)contentX, (float)detailY, 16777215);
         detailY += 14;

         for(String line : this.fontRenderer.listFormattedStringToWidth("§7Select an order type to issue to your village. You may have up to 2 active orders at once.", contentW)) {
            this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
            detailY += 10;
         }

         int descY = this.guiTop + 138;

         for(String line : this.fontRenderer.listFormattedStringToWidth("§8Issue a kill order targeting an enemy village. All village members earn bonus PvP XP for every kill against that village.", contentW)) {
            this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)descY, 16777215);
            descY += 10;
         }

         descY += 8;

         for(String line : this.fontRenderer.listFormattedStringToWidth("§8Deploy a coordinated village-wide operation. Each member receives a mission scaled to their rank. Bonus rewards when all tiers complete.", contentW)) {
            this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)descY, 16777215);
            descY += 10;
         }
      } else if (this.leadershipStep == 1) {
         this.fontRenderer.drawStringWithShadow("§6✦ Issue Target Order", (float)contentX, (float)detailY, 16777215);
         detailY += 14;

         for(String line : this.fontRenderer.listFormattedStringToWidth("§7Select an enemy village to target. All members of your village will earn bonus PvP XP for every kill against that village's shinobi. Duration: 24 hours.", contentW)) {
            this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
            detailY += 10;
         }
      } else if (this.leadershipStep == 2) {
         this.fontRenderer.drawStringWithShadow("§6✦ Assign Village Mission", (float)contentX, (float)detailY, 16777215);
         detailY += 14;

         for(String line : this.fontRenderer.listFormattedStringToWidth("§7Select a coordinated operation. Each village member receives a mission scaled to their rank.", contentW)) {
            this.fontRenderer.drawStringWithShadow(line, (float)contentX, (float)detailY, 16777215);
            detailY += 10;
         }

         detailY += 4;
         List<PvpMissionTemplate> templates = PvpMissionTemplate.getAssignable();
         int templateY = this.guiTop + 139;
         int maxVisible = 4;

         for(int i = this.assignTemplateScrollOffset; i < Math.min(templates.size(), this.assignTemplateScrollOffset + maxVisible); ++i) {
            String templateDesc = getAssignTemplateDescription(((PvpMissionTemplate)templates.get(i)).getId());
            this.fontRenderer.drawStringWithShadow("§8" + templateDesc, (float)(contentX + 8), (float)templateY, 16777215);
            templateY += 28;
         }

         if (templates.size() > maxVisible) {
            int trackX = this.guiLeft + 360 - 24;
            int trackTop = this.guiTop + 132;
            int trackH = maxVisible * 28;
            this.drawScrollBar(trackX, trackTop, trackH, templates.size(), maxVisible, this.assignTemplateScrollOffset, Math.max(1, templates.size() - maxVisible));
         }
      }

   }

   private static String getAssignTemplateDescription(String templateId) {
      switch (templateId) {
         case "village_purge":
            return "Hunt enemy village shinobi";
         case "leadership_defense":
            return "Defend your village borders";
         case "elite_hunt":
            return "Hunt high-XP enemy shinobi";
         case "multi_front_assault":
            return "Attack multiple enemy villages";
         case "dojutsu_purge":
            return "Hunt enemy dojutsu users";
         case "coordinated_strike":
            return "Coordinated elemental assault";
         case "supply_line_raid":
            return "Raid enemy supply lines";
         default:
            return "Village-wide coordinated operation.";
      }
   }

   private static String getAssignTemplateDetailDescription(String templateId) {
      switch (templateId) {
         case "village_purge":
            return "Hunt and kill shinobi from a specific enemy village. Your target village is assigned on accept. Only kills against that village count. D:3 C:5 B:8 A:11 S:15 S+:21";
         case "leadership_defense":
            return "Eliminate enemy shinobi inside your village territory. Kills only count when the enemy dies INSIDE your village borders. D:2 C:4 B:6 A:9 S:12 S+:17";
         case "elite_hunt":
            return "Target enemies with MORE Ninja XP than you. Only kills against higher-XP opponents count. D:1 C:2 B:4 A:6 S:9 S+:13";
         case "multi_front_assault":
            return "Kill enemies from 3+ different villages. Spread attacks across multiple enemy villages to complete. D:3 C:5 B:8 A:12 S:17 S+:24";
         case "dojutsu_purge":
            return "Hunt enemies with Sharingan, Byakugan, or Rinnegan. Only kills against dojutsu users count. D:1 C:2 B:4 A:6 S:9 S+:13";
         case "coordinated_strike":
            return "Kill enemies using a specific nature release jutsu (randomly assigned). Only kills with the assigned element count. D:2 C:4 B:6 A:9 S:13 S+:18";
         case "supply_line_raid":
            return "Eliminate enemies near your village border regions. Kills only count near your village territory edges. D:2 C:4 B:7 A:10 S:14 S+:20";
         default:
            return "Village-wide coordinated operation.";
      }
   }

   private void drawTournamentContent(int x, int y, int maxWidth) {
      PvpClientData.TournamentClientInfo info = PvpClientData.tournamentInfo;
      if (info == null) {
         this.fontRenderer.drawStringWithShadow("§6✦ Village Tournament", (float)x, (float)y, -1521552);
         y += 16;
         this.fontRenderer.drawStringWithShadow("§7No active tournament.", (float)x, (float)y, -7700886);
         y += 14;
         if (PvpClientData.isKage || PvpClientData.isAdvisor) {
            this.fontRenderer.drawStringWithShadow("§7As village leader, you can create one.", (float)x, (float)y, -7700886);
         }

      } else {
         String stateLabel;
         int stateColor;
         switch (info.state) {
            case 0:
               stateLabel = "SETUP";
               stateColor = -22016;
               break;
            case 1:
               stateLabel = "SIGNUPS OPEN";
               stateColor = -11141291;
               break;
            case 2:
               stateLabel = "BRACKET READY";
               stateColor = -11141121;
               break;
            case 3:
               stateLabel = "IN PROGRESS";
               stateColor = -43691;
               break;
            case 4:
               stateLabel = "COMPLETE";
               stateColor = -5592406;
               break;
            default:
               stateLabel = "CANCELLED";
               stateColor = -7829368;
         }

         this.fontRenderer.drawStringWithShadow("§6✦ Village Tournament", (float)x, (float)y, -1521552);
         this.fontRenderer.drawStringWithShadow(stateLabel, (float)(x + maxWidth - this.fontRenderer.getStringWidth(stateLabel)), (float)y, stateColor);
         y += 14;
         this.fontRenderer.drawStringWithShadow("Host: §e" + info.hostName, (float)x, (float)y, -2832216);
         y += 12;
         this.fontRenderer.drawStringWithShadow("Participants: §e" + info.participantCount, (float)x, (float)y, -2832216);
         y += 12;
         String rankRestriction = "Ranks: ";
         if (info.minRank < 0 && info.maxRank < 0) {
            rankRestriction = rankRestriction + "§aAll ranks";
         } else {
            String minR = info.minRank >= 0 && info.minRank < 6 ? RANK_NAMES[info.minRank] : "Any";
            String maxR = info.maxRank >= 0 && info.maxRank < 6 ? RANK_NAMES[info.maxRank] : "Any";
            rankRestriction = rankRestriction + "§e" + minR + " - " + maxR;
         }

         this.fontRenderer.drawStringWithShadow(rankRestriction, (float)x, (float)y, -2832216);
         y += 12;
         if (info.state == 1) {
            long remaining = Math.max(0L, info.signupEndTime - System.currentTimeMillis());
            long minutes = remaining / 60000L;
            long seconds = remaining / 1000L % 60L;
            this.fontRenderer.drawStringWithShadow("Signup closes in: §e" + minutes + "m " + seconds + "s", (float)x, (float)y, -2832216);
            y += 12;
            if (info.isPlayerSignedUp) {
               this.fontRenderer.drawStringWithShadow("§a✓ You are signed up!", (float)x, (float)y, -11141291);
            } else {
               this.fontRenderer.drawStringWithShadow("§7You have not signed up yet.", (float)x, (float)y, -7700886);
            }

            y += 14;
         }

         if (info.hasBracket && info.matches != null && !info.matches.isEmpty()) {
            this.drawHorizontalLine(x, x + maxWidth, y, -9807296);
            y += 6;
            this.fontRenderer.drawStringWithShadow("§6Bracket (Round " + (info.currentRound + 1) + "/" + info.totalRounds + ")", (float)x, (float)y, -1521552);
            y += 14;
            this.drawBracketGrid(x, y, maxWidth, info);
         }

         if (info.isInMatch) {
            int matchY = this.guiTop + 280 - 75;
            this.fontRenderer.drawStringWithShadow("§c§l⚔ YOUR MATCH IS ACTIVE!", (float)x, (float)matchY, -43691);
            this.fontRenderer.drawStringWithShadow("Opponent: §e" + info.opponentName, (float)x, (float)(matchY + 14), -2832216);
         }

      }
   }

   private void drawBracketGrid(int x, int y, int maxWidth, PvpClientData.TournamentClientInfo info) {
      if (info.matches != null && !info.matches.isEmpty()) {
         int totalRounds = info.totalRounds;
         if (totalRounds <= 0) {
            totalRounds = 1;
         }

         List<List<PvpClientData.MatchClientInfo>> roundMatches = new ArrayList();

         for(int r = 0; r < totalRounds; ++r) {
            roundMatches.add(new ArrayList());
         }

         for(PvpClientData.MatchClientInfo match : info.matches) {
            if (match.round >= 0 && match.round < totalRounds) {
               ((List)roundMatches.get(match.round)).add(match);
            }
         }

         int matchBoxW = 100;
         int matchBoxH = 26;
         int roundSpacing = 30;
         int colWidth = matchBoxW + roundSpacing;
         int firstRoundMatches = roundMatches.isEmpty() ? 1 : Math.max(1, ((List)roundMatches.get(0)).size());
         int var10000 = totalRounds * colWidth;
         var10000 = firstRoundMatches * (matchBoxH + 8);
         int clipX = this.guiLeft + 8;
         int clipY = y - 2;
         int clipW = 344;
         int clipH = this.guiTop + 280 - 50 - clipY;
         this.fontRenderer.drawStringWithShadow("§8Drag to pan, scroll to zoom", (float)clipX, (float)(clipY + clipH + 2), -7700886);
         ScaledResolution sr = new ScaledResolution(this.mc);
         int scaleFactor = sr.getScaleFactor();
         GL11.glEnable(3089);
         GL11.glScissor(clipX * scaleFactor, (sr.getScaledHeight() - clipY - clipH) * scaleFactor, clipW * scaleFactor, clipH * scaleFactor);
         GlStateManager.pushMatrix();
         float centerX = (float)clipX + (float)clipW / 2.0F;
         float centerY = (float)clipY + (float)clipH / 2.0F;
         GlStateManager.translate(centerX, centerY, 0.0F);
         GlStateManager.scale(this.bracketZoom, this.bracketZoom, 1.0F);
         GlStateManager.translate(-centerX + this.bracketPanX, -centerY + this.bracketPanY, 0.0F);

         for(int r = 0; r < totalRounds; ++r) {
            List<PvpClientData.MatchClientInfo> matches = (List)roundMatches.get(r);
            int colX = x + r * colWidth;
            String roundLabel = "R" + (r + 1);
            if (r == totalRounds - 1) {
               roundLabel = "Finals";
            } else if (r == totalRounds - 2 && totalRounds > 2) {
               roundLabel = "Semis";
            }

            this.fontRenderer.drawStringWithShadow("§e" + roundLabel, (float)colX, (float)(y - 12), -22016);
            int matchCount = matches.size();
            if (matchCount != 0) {
               int totalSlots = firstRoundMatches;
               float verticalGap = (float)(firstRoundMatches * (matchBoxH + 8)) / (float)matchCount;

               for(int m = 0; m < matchCount; ++m) {
                  PvpClientData.MatchClientInfo match = (PvpClientData.MatchClientInfo)matches.get(m);
                  int matchY = y + (int)((float)m * verticalGap + verticalGap / 2.0F - (float)(matchBoxH / 2));
                  int boxColor;
                  switch (match.matchState) {
                     case 1:
                        boxColor = -2004318208;
                        break;
                     case 2:
                        boxColor = -2004353024;
                        break;
                     case 3:
                        boxColor = -2013248512;
                        break;
                     case 4:
                        boxColor = -2009910477;
                        break;
                     default:
                        boxColor = -2011028958;
                  }

                  drawRect(colX, matchY, colX + matchBoxW, matchY + matchBoxH, boxColor);
                  this.drawHorizontalLine(colX, colX + matchBoxW - 1, matchY, -11184811);
                  this.drawHorizontalLine(colX, colX + matchBoxW - 1, matchY + matchBoxH - 1, -11184811);
                  this.drawVerticalLine(colX, matchY, matchY + matchBoxH - 1, -11184811);
                  this.drawVerticalLine(colX + matchBoxW - 1, matchY, matchY + matchBoxH - 1, -11184811);
                  String p1 = !match.player1Name.isEmpty() ? match.player1Name : "TBD";
                  String p2 = !match.player2Name.isEmpty() ? match.player2Name : "TBD";
                  boolean p1Won = !match.winnerName.isEmpty() && match.winnerName.equals(match.player1Name);
                  boolean p2Won = !match.winnerName.isEmpty() && match.winnerName.equals(match.player2Name);
                  String p1Color = p1Won ? "§a" : (p2Won ? "§c" : "§f");
                  String p2Color = p2Won ? "§a" : (p1Won ? "§c" : "§f");
                  String p1Display = this.fontRenderer.trimStringToWidth(p1, matchBoxW - 6);
                  String p2Display = this.fontRenderer.trimStringToWidth(p2, matchBoxW - 6);
                  this.fontRenderer.drawStringWithShadow(p1Color + p1Display, (float)(colX + 3), (float)(matchY + 2), -1);
                  this.drawHorizontalLine(colX + 2, colX + matchBoxW - 3, matchY + 12, -12303292);
                  this.fontRenderer.drawStringWithShadow(p2Color + p2Display, (float)(colX + 3), (float)(matchY + 15), -1);
                  if (r < totalRounds - 1) {
                     int nextMatchIdx = m / 2;
                     List<PvpClientData.MatchClientInfo> nextMatches = (List)roundMatches.get(r + 1);
                     if (nextMatchIdx < nextMatches.size()) {
                        float nextVerticalGap = (float)(totalSlots * (matchBoxH + 8)) / (float)nextMatches.size();
                        int nextMatchY = y + (int)((float)nextMatchIdx * nextVerticalGap + nextVerticalGap / 2.0F - (float)(matchBoxH / 2));
                        int lineStartX = colX + matchBoxW;
                        int lineStartY = matchY + matchBoxH / 2;
                        int lineMidX = lineStartX + roundSpacing / 2;
                        int lineEndX = colX + colWidth;
                        int lineEndY = nextMatchY + matchBoxH / 2;
                        this.drawHorizontalLine(lineStartX, lineMidX, lineStartY, -10066330);
                        int minY = Math.min(lineStartY, lineEndY);
                        int maxY = Math.max(lineStartY, lineEndY);
                        if (minY != maxY) {
                           this.drawVerticalLine(lineMidX, minY, maxY, -10066330);
                        }

                        if (m % 2 == 0) {
                           this.drawHorizontalLine(lineMidX, lineEndX, lineEndY, -10066330);
                        }
                     }
                  }
               }
            }
         }

         GlStateManager.popMatrix();
         GL11.glDisable(3089);
      }
   }

   private void drawBingoDetails(int detailX, int detailY, int maxWidth) {
      List<PvpClientData.BingoInfo> entries = PvpClientData.getBingoEntries();
      if (entries.isEmpty()) {
         this.drawCategoryHint(detailX, detailY, maxWidth, "No targets listed. Check back later.");
      } else {
         if (this.selectedQuestIndex >= 0 && this.selectedQuestIndex < entries.size()) {
            PvpClientData.BingoInfo b = (PvpClientData.BingoInfo)entries.get(this.selectedQuestIndex);
            this.fontRenderer.drawStringWithShadow("§cBingo Book Target", (float)detailX, (float)detailY, -1521552);
            detailY += 16;
            this.fontRenderer.drawStringWithShadow("§f" + b.targetName, (float)detailX, (float)detailY, -1521552);
            detailY += 14;
            this.fontRenderer.drawStringWithShadow("Village: §f" + b.targetVillage, (float)detailX, (float)detailY, -7700886);
            detailY += 12;
            String ninjaXpLine = "Ninja XP: §e" + b.ninjaXpReward;
            if (b.ryoReward > 0) {
               ninjaXpLine = ninjaXpLine + "§7, §6" + b.ryoReward + " Ryo";
            }

            this.fontRenderer.drawStringWithShadow(ninjaXpLine, (float)detailX, (float)detailY, -7700886);
            detailY += 12;
            this.fontRenderer.drawStringWithShadow("PvP XP: §c" + b.pvpXpReward, (float)detailX, (float)detailY, -7700886);
            detailY += 12;
            detailY += 4;
            if (b.claimed) {
               this.fontRenderer.drawStringWithShadow("§7✗ Already Claimed", (float)detailX, (float)detailY, -7700886);
            } else {
               this.fontRenderer.drawStringWithShadow("§a✓ Available", (float)detailX, (float)detailY, -10831270);
            }
         } else {
            this.drawCategoryHint(detailX, detailY, maxWidth, "Select a target to view details.");
         }

      }
   }

   private void drawWarContent(int mouseX, int mouseY) {
      this.drawPanelWithLighting(this.guiLeft + 8, this.guiTop + 48, 344, 195);
      int contentX = this.guiLeft + 16;
      int contentY = this.guiTop + 54;
      int contentW = 328;
      List<PvpClientData.WarClientInfo> wars = PvpClientData.getActiveWars();
      if (wars.isEmpty() && this.warDeclareStep == 0) {
         this.drawCategoryHint(contentX, contentY, contentW, "No active wars.");
      } else if (!wars.isEmpty()) {
         for(int i = 0; i < Math.min(wars.size(), 3); ++i) {
            PvpClientData.WarClientInfo war = (PvpClientData.WarClientInfo)wars.get(i);
            this.drawWarPanel(contentX, contentY, contentW, war);
            contentY += 52;
         }
      }

      if (PvpClientData.isKage || PvpClientData.hasWarAuthority) {
         int kageY = this.guiTop + 280 - 110;
         this.drawHorizontalLine(contentX, contentX + contentW, kageY - 4, -9807296);
         boolean isAkatsuki = "Akatsuki".equalsIgnoreCase(PvpClientData.kageVillage);
         String kageLabel = PvpClientData.isKage ? (isAkatsuki ? "§4✦ Leader Control Panel" : "§6✦ Kage Control Panel") : "§6✦ Advisor War Authority";
         this.fontRenderer.drawStringWithShadow(kageLabel, (float)contentX, (float)kageY, -1521552);
         kageY += 14;
         if (this.warDeclareStep == 1) {
            this.fontRenderer.drawStringWithShadow("Select war mode:", (float)contentX, (float)(this.guiTop + 57), -2832216);
            int descX = this.guiLeft + 142;
            int descStartY = this.guiTop + 70;
            int descSpacing = 20;
            int maxDescW = this.guiLeft + 360 - 20 - descX;
            String leaderTitle = isAkatsuki ? "Leader" : "Kage";
            List<String> descList = new ArrayList();
            descList.add("Score kills to win");
            descList.add(leaderTitle + " picks 10 shinobi");
            descList.add("Capture & hold points");
            descList.add("Intense battlefront control");
            if (isAkatsuki) {
               descList.add("Stealth ops, 5v village");
            }

            String[] modeDescs = (String[])descList.toArray(new String[0]);

            for(int d = 0; d < modeDescs.length; ++d) {
               String desc = modeDescs[d];
               if (this.fontRenderer.getStringWidth(desc) > maxDescW) {
                  desc = this.fontRenderer.trimStringToWidth(desc, maxDescW - 6) + "..";
               }

               this.fontRenderer.drawStringWithShadow("§8" + desc, (float)descX, (float)(descStartY + descSpacing * d + 4), -11513776);
            }
         } else if (this.warDeclareStep == 2) {
            String modeName = this.warSelectedMode >= 0 ? WarMode.fromOrdinal(this.warSelectedMode).displayName : "?";
            this.fontRenderer.drawStringWithShadow("Mode: §f" + modeName + " §8- Select target village:", (float)contentX, (float)(this.guiTop + 105), -2832216);
         } else {
            PvpClientData.WarClientInfo myWar = null;

            for(PvpClientData.WarClientInfo w : wars) {
               if (w.involvesVillage(PvpClientData.kageVillage)) {
                  myWar = w;
                  break;
               }
            }

            if (myWar != null) {
               if (myWar.lobbyState >= 1) {
                  String stateLabel;
                  if (myWar.lobbyState <= 2) {
                     stateLabel = "§eSelecting";
                  } else if (myWar.lobbyState == 3) {
                     stateLabel = "§6Locking";
                  } else {
                     stateLabel = "§aReady";
                  }

                  this.fontRenderer.drawStringWithShadow("Lobby: " + stateLabel, (float)contentX, (float)kageY, -2832216);
                  kageY += 12;
                  String r1Label = myWar.village1 + " (" + myWar.roster1Names.size() + "/10" + (myWar.locked1 ? " §a✔" : "") + "§7)";
                  String r2Label = myWar.village2 + " (" + myWar.roster2Names.size() + "/10" + (myWar.locked2 ? " §a✔" : "") + "§7)";
                  this.fontRenderer.drawStringWithShadow(r1Label, (float)contentX, (float)kageY, -7700886);
                  this.fontRenderer.drawStringWithShadow(r2Label, (float)(contentX + contentW / 2), (float)kageY, -7700886);
                  kageY += 11;
                  int maxRoster = 3;

                  for(int j = 0; j < maxRoster; ++j) {
                     String n1 = j < myWar.roster1Names.size() ? (String)myWar.roster1Names.get(j) : "";
                     String n2 = j < myWar.roster2Names.size() ? (String)myWar.roster2Names.get(j) : "";
                     if (!n1.isEmpty()) {
                        this.fontRenderer.drawStringWithShadow("§7" + n1, (float)(contentX + 4), (float)kageY, -7700886);
                     }

                     if (!n2.isEmpty()) {
                        this.fontRenderer.drawStringWithShadow("§7" + n2, (float)(contentX + contentW / 2 + 4), (float)kageY, -7700886);
                     }

                     if (n1.isEmpty() && n2.isEmpty()) {
                        break;
                     }

                     kageY += 10;
                  }

                  int maxNames = Math.max(myWar.roster1Names.size(), myWar.roster2Names.size());
                  if (maxNames > maxRoster) {
                     this.fontRenderer.drawStringWithShadow("§8+" + (maxNames - maxRoster) + " more...", (float)(contentX + 4), (float)kageY, -7700886);
                  }
               } else {
                  this.fontRenderer.drawStringWithShadow("War in progress. Use Surrender to end.", (float)contentX, (float)kageY, -2832216);
               }
            } else if (PvpClientData.warCooldownRemainingMs > 0L) {
               String cd = formatCooldown(PvpClientData.warCooldownRemainingMs);
               this.fontRenderer.drawStringWithShadow("War cooldown: §c" + cd, (float)contentX, (float)kageY, -2832216);
            } else {
               this.fontRenderer.drawStringWithShadow("Ready to declare war on another village.", (float)contentX, (float)kageY, -2832216);
            }
         }
      }

   }

   private void drawWarPanel(int x, int y, int w, PvpClientData.WarClientInfo war) {
      drawRect(x, y, x + w, y + 48, -2011160544);
      drawRect(x, y, x + w, y + 1, -11910088);
      drawRect(x, y + 47, x + w, y + 48, -14015464);
      int modeColor = getWarModeColor(war.modeOrdinal);
      String modeName = war.getModeName();
      String badge = "[" + modeName.toUpperCase() + "]";
      this.fontRenderer.drawStringWithShadow(badge, (float)(x + 4), (float)(y + 4), modeColor);
      String timeStr = formatCooldown(war.timeRemainingMs);
      int timeW = this.fontRenderer.getStringWidth(timeStr);
      this.fontRenderer.drawStringWithShadow(timeStr, (float)(x + w - timeW - 4), (float)(y + 4), -7700886);
      String v1 = war.village1;
      String v2 = war.village2;
      String scoreText = "§f" + v1 + "  §e" + war.score1 + " §7vs §e" + war.score2 + "  §f" + v2;
      int scoreW = this.fontRenderer.getStringWidth(scoreText);
      this.fontRenderer.drawStringWithShadow(scoreText, (float)(x + (w - scoreW) / 2), (float)(y + 16), -2832216);
      int detailY = y + 30;
      if (war.modeOrdinal == WarMode.DIVISION.ordinal()) {
         String roundInfo = "Round " + war.currentRound + " | Won: " + war.roundsWon1 + " - " + war.roundsWon2;
         this.fontRenderer.drawStringWithShadow(roundInfo, (float)(x + 4), (float)detailY, -7700886);
      } else {
         StringBuilder topLine = new StringBuilder();
         if (war.topKillers1 != null && !war.topKillers1.isEmpty()) {
            PvpClientData.TopKillerInfo tk = (PvpClientData.TopKillerInfo)war.topKillers1.get(0);
            topLine.append("§7Top: ").append(tk.playerName).append(" (").append(tk.kills).append(")");
         }

         if (topLine.length() > 0) {
            this.fontRenderer.drawStringWithShadow(topLine.toString(), (float)(x + 4), (float)detailY, -7700886);
         }
      }

   }

   private static float getFortifyBonusForLevel(int level) {
      switch (level) {
         case 1:
            return 25.0F;
         case 2:
            return 50.0F;
         case 3:
            return 75.0F;
         default:
            return 0.0F;
      }
   }

   private int getTerritoryVillageColor(String village) {
      if (village == null) {
         return -7829368;
      } else {
         switch (village) {
            case "leaf":
               return -12272828;
            case "sand":
               return -2241468;
            case "mist":
               return -11167318;
            case "stone":
               return -5605564;
            case "cloud":
               return -4486913;
            case "rain":
               return -14531414;
            case "akatsuki":
               return -3407872;
            default:
               return -7829368;
         }
      }
   }

   private String getWeeklyResetCountdown() {
      long now = System.currentTimeMillis();
      long target;
      if (now < TerritoryConstants.FIRST_SEASON_END_MS) {
         target = TerritoryConstants.FIRST_SEASON_END_MS;
      } else {
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
         cal.setTimeInMillis(now);
         int dayOfWeek = cal.get(7);
         int resetDay = 7;
         int daysUntil = (resetDay - dayOfWeek + 7) % 7;
         cal.add(6, daysUntil);
         cal.set(11, 16);
         cal.set(12, 0);
         cal.set(13, 0);
         cal.set(14, 0);
         if (cal.getTimeInMillis() <= now) {
            cal.add(6, 7);
         }

         target = cal.getTimeInMillis();
      }

      long remaining = target - now;
      if (remaining < 0L) {
         remaining = 0L;
      }

      long totalMinutes = remaining / 60000L;
      long days = totalMinutes / 1440L;
      long hours = totalMinutes % 1440L / 60L;
      long minutes = totalMinutes % 60L;
      return days > 0L ? days + "d " + hours + "h" : hours + "h " + minutes + "m";
   }

   private void drawTerritoryContent(int mouseX, int mouseY) {
      TerritoryClientData tData = TerritoryClientData.getInstance();
      Map<String, TerritoryClientData.ClientZoneData> zones = tData.getZoneStates();
      Map<String, Integer> scores = tData.getWeeklyScores();
      int leftX = this.guiLeft + 6;
      int leftW = 155;
      int leftTop = this.guiTop + 50;
      int leftPanelH = this.guiTop + 280 - 38 - leftTop;
      int toggleBtnRight = this.guiLeft + 180 + 70;
      int toggleBtnY = this.guiTop + 280 - 30;
      String resetStr = "§8Reset: §f" + this.getWeeklyResetCountdown();
      this.fontRenderer.drawStringWithShadow(resetStr, (float)(toggleBtnRight + 6), (float)(toggleBtnY + 5), -7700886);
      this.fortifyBtnCount = 0;
      this.fortifyBackBtnX = -1;
      if (this.selectedZoneId != null && zones.containsKey(this.selectedZoneId)) {
         this.drawTerritoryZoneDetailPanel(leftX, leftTop, leftW, leftPanelH, (TerritoryClientData.ClientZoneData)zones.get(this.selectedZoneId), mouseX, mouseY);
      } else {
         this.fortifyScrollOffset = 0;
         this.drawTerritoryScoreboard(leftX, leftTop, leftW, zones, scores);
      }

      int mapX = this.guiLeft + 155 + 10;
      int mapW = 189;
      int mapTop = this.guiTop + 50;
      int mapH = 190;
      this.drawPanelWithLighting(mapX, mapTop, mapW, mapH);
      this.fontRenderer.drawStringWithShadow("§8Scroll to zoom, drag to pan", (float)(mapX + 4), (float)(mapTop + mapH - 10), -7700886);
      int clipX = mapX + 2;
      int clipY = mapTop + 2;
      int clipW = mapW - 4;
      int clipH = mapH - 14;
      this.drawTerritoryMap(clipX, clipY, clipW, clipH, zones, mouseX, mouseY);
   }

   private void drawTerritoryScoreboard(int leftX, int leftTop, int leftW, Map<String, TerritoryClientData.ClientZoneData> zones, Map<String, Integer> scores) {
      int leftPanelH = this.guiTop + 280 - 38 - leftTop;
      this.drawPanelWithLighting(leftX, leftTop, leftW, leftPanelH);
      int contentTop = leftTop + 3;
      int contentBot = leftTop + leftPanelH - 3;
      ScaledResolution sr = new ScaledResolution(this.mc);
      int sf = sr.getScaleFactor();
      GL11.glEnable(3089);
      GL11.glScissor(leftX * sf, (sr.getScaledHeight() - contentBot) * sf, leftW * sf, (contentBot - contentTop) * sf);
      int ly = leftTop + 4 - this.territoryScoreboardScrollOffset;
      this.fontRenderer.drawStringWithShadow("§l§6Village Scores", (float)(leftX + 8), (float)ly, -1521552);
      ly += 12;
      this.drawHorizontalLine(leftX + 4, leftX + leftW - 4, ly, -9807296);
      ly += 4;
      Map<String, Integer> zoneCounts = new HashMap();

      for(TerritoryClientData.ClientZoneData czd : zones.values()) {
         if (czd.ownerVillage != null && !czd.ownerVillage.isEmpty()) {
            zoneCounts.merge(czd.ownerVillage, 1, Integer::sum);
         }
      }

      int[][] villageRanking = new int[TERRITORY_VILLAGE_ORDER.length][2];

      for(int i = 0; i < TERRITORY_VILLAGE_ORDER.length; ++i) {
         villageRanking[i][0] = i;
         villageRanking[i][1] = (Integer)scores.getOrDefault(TERRITORY_VILLAGE_ORDER[i], 0);
      }

      Arrays.sort(villageRanking, (a, b) -> b[1] - a[1]);

      for(int i = 0; i < TERRITORY_VILLAGE_ORDER.length; ++i) {
         String v = TERRITORY_VILLAGE_ORDER[i];
         String display = TERRITORY_VILLAGE_DISPLAY[i];
         int color = this.getTerritoryVillageColor(v);
         int zoneCount = (Integer)zoneCounts.getOrDefault(v, 0);
         int score = (Integer)scores.getOrDefault(v, 0);
         drawRect(leftX + 6, ly + 1, leftX + 10, ly + 7, color);
         this.fontRenderer.drawStringWithShadow(display, (float)(leftX + 13), (float)ly, color);
         String info = zoneCount + " | " + score + "pts";
         int infoW = this.fontRenderer.getStringWidth(info);
         this.fontRenderer.drawStringWithShadow(info, (float)(leftX + leftW - infoW - 6), (float)ly, -7700886);
         ly += 14;
      }

      ly += 2;
      this.drawHorizontalLine(leftX + 4, leftX + leftW - 4, ly, -9807296);
      ly += 4;
      this.fontRenderer.drawStringWithShadow("§l§eMultipliers", (float)(leftX + 8), (float)ly, -1521552);
      ly += 11;
      String[] medalColors = new String[]{"§6", "§7", "§c"};
      String[] ryoBonus = new String[]{"+25% Ryo", "+12% Ryo", "+5% Ryo"};
      int[] medalCircleColors = new int[]{-13312, -4473925, -3377340};

      for(int r = 0; r < 3 && r < 6; ++r) {
         int vi = villageRanking[r][0];
         String vName = TERRITORY_VILLAGE_DISPLAY[vi];
         int vColor = this.getTerritoryVillageColor(TERRITORY_VILLAGE_ORDER[vi]);
         drawRect(leftX + 6, ly + 1, leftX + 10, ly + 7, medalCircleColors[r]);
         this.fontRenderer.drawStringWithShadow(medalColors[r] + (r + 1) + ". " + vName, (float)(leftX + 13), (float)ly, vColor);
         String bonusStr = "§8" + ryoBonus[r];
         int bonusW = this.fontRenderer.getStringWidth(bonusStr);
         this.fontRenderer.drawStringWithShadow(bonusStr, (float)(leftX + leftW - bonusW - 6), (float)ly, -7700886);
         ly += 11;
      }

      ly += 2;
      this.drawHorizontalLine(leftX + 4, leftX + leftW - 4, ly, -9807296);
      ly += 4;
      this.fontRenderer.drawStringWithShadow("§l§aWeekly Rewards", (float)(leftX + 8), (float)ly, -1521552);
      ly += 11;
      String[] rewardLabels = new String[]{"1st:", "2nd:", "3rd:", "4th:", "5th:", "6th:"};
      String[] rewardValues = new String[]{"200,000", "100,000", "50,000", "25,000", "10,000", "5,000"};
      int[] rewardColors = new int[]{-13312, -4473925, -3377340, -10044570, -11167301, -6715222};

      for(int r = 0; r < 6; ++r) {
         this.fontRenderer.drawStringWithShadow(rewardLabels[r], (float)(leftX + 10), (float)ly, rewardColors[r]);
         String valStr = rewardValues[r] + " Ryo";
         int valW = this.fontRenderer.getStringWidth(valStr);
         this.fontRenderer.drawStringWithShadow(valStr, (float)(leftX + leftW - valW - 6), (float)ly, rewardColors[r]);
         ly += 10;
      }

      ly += 2;
      this.drawHorizontalLine(leftX + 4, leftX + leftW - 4, ly, -9807296);
      ly += 4;
      TerritoryClientData tcd = TerritoryClientData.getInstance();
      int myPts = tcd.getMyPoints();
      int myKills = tcd.getMyKills();
      int myCaps = tcd.getMyCaptures();
      String myTier;
      int myTierColor;
      if (myPts >= 40000) {
         myTier = "Diamond";
         myTierColor = -5574913;
      } else if (myPts >= 28000) {
         myTier = "Gold";
         myTierColor = -13312;
      } else if (myPts >= 16000) {
         myTier = "Silver";
         myTierColor = -4473925;
      } else if (myPts >= 6000) {
         myTier = "Bronze";
         myTierColor = -3377340;
      } else if (myPts >= 1500) {
         myTier = "Iron";
         myTierColor = -12303292;
      } else {
         myTier = "None";
         myTierColor = -7829368;
      }

      this.fontRenderer.drawStringWithShadow("§l§bYour Stats", (float)(leftX + 8), (float)ly, -1521552);
      ly += 11;
      this.fontRenderer.drawStringWithShadow("§7Points: §f" + String.format("%,d", myPts), (float)(leftX + 10), (float)ly, -1);
      ly += 10;
      this.fontRenderer.drawStringWithShadow("§7Kills: §f" + myKills + "  §7Caps: §f" + myCaps, (float)(leftX + 10), (float)ly, -1);
      ly += 10;
      this.fontRenderer.drawStringWithShadow("§7Tier: ", (float)(leftX + 10), (float)ly, -5592406);
      int tierLabelW = this.fontRenderer.getStringWidth("§7Tier: ");
      this.fontRenderer.drawStringWithShadow(myTier, (float)(leftX + 10 + tierLabelW), (float)ly, myTierColor);
      ly += 12;
      ly += 2;
      this.drawHorizontalLine(leftX + 4, leftX + leftW - 4, ly, -9807296);
      ly += 4;
      this.fontRenderer.drawStringWithShadow("§l§dContribution Tiers", (float)(leftX + 8), (float)ly, -1521552);
      ly += 11;
      String[][] tiers = new String[][]{{"Diamond", "40,000", "150%"}, {"Gold", "28,000", "120%"}, {"Silver", "16,000", "90%"}, {"Bronze", "6,000", "60%"}, {"Iron", "1,500", "25%"}};
      int[] tierColors = new int[]{-5574913, -13312, -4473925, -3377340, -12303292};

      for(int t = 0; t < tiers.length; ++t) {
         boolean isMyTier = tiers[t][0].equals(myTier);
         String prefix = isMyTier ? "§l" : "";
         drawRect(leftX + 8, ly + 1, leftX + 12, ly + 7, tierColors[t]);
         this.fontRenderer.drawStringWithShadow(prefix + tiers[t][0], (float)(leftX + 15), (float)ly, tierColors[t]);
         String tierInfo = tiers[t][1] + "pts §8| §a" + tiers[t][2];
         int tiW = this.fontRenderer.getStringWidth(tierInfo);
         this.fontRenderer.drawStringWithShadow(tierInfo, (float)(leftX + leftW - tiW - 6), (float)ly, -5592406);
         ly += 10;
      }

      int totalContentH = ly + this.territoryScoreboardScrollOffset - (leftTop + 4);
      int visibleH = contentBot - contentTop;
      int maxScroll = Math.max(0, totalContentH - visibleH);
      if (this.territoryScoreboardScrollOffset > maxScroll) {
         this.territoryScoreboardScrollOffset = maxScroll;
      }

      GL11.glDisable(3089);
      if (this.territoryScoreboardScrollOffset < maxScroll) {
         String arrow = "§8▼";
         int arrowW = this.fontRenderer.getStringWidth(arrow);
         this.fontRenderer.drawStringWithShadow(arrow, (float)(leftX + (leftW - arrowW) / 2), (float)(contentBot - 9), -7700886);
      }

      if (this.territoryScoreboardScrollOffset > 0) {
         String arrow = "§8▲";
         int arrowW = this.fontRenderer.getStringWidth(arrow);
         this.fontRenderer.drawStringWithShadow(arrow, (float)(leftX + (leftW - arrowW) / 2), (float)(contentTop + 1), -7700886);
      }

   }

   private void drawTerritoryZoneDetailPanel(int leftX, int leftTop, int leftW, int leftPanelH, TerritoryClientData.ClientZoneData sel, int mouseX, int mouseY) {
      this.drawPanelWithLighting(leftX, leftTop, leftW, leftPanelH);
      int contentTop = leftTop + 3;
      int contentBot = leftTop + leftPanelH - 3;
      ScaledResolution sr = new ScaledResolution(this.mc);
      int sf = sr.getScaleFactor();
      GL11.glEnable(3089);
      GL11.glScissor(leftX * sf, (sr.getScaledHeight() - contentBot) * sf, leftW * sf, (contentBot - contentTop) * sf);
      int dy = contentTop - this.fortifyScrollOffset;
      String backText = "§7< Back";
      this.fontRenderer.drawStringWithShadow(backText, (float)(leftX + 6), (float)dy, -2832216);
      this.fortifyBackBtnX = leftX + 6;
      this.fortifyBackBtnY = dy;
      this.fortifyBackBtnW = this.fontRenderer.getStringWidth(backText);
      this.fortifyBackBtnH = 9;
      dy += 13;
      this.fontRenderer.drawStringWithShadow("§l§f" + sel.displayName, (float)(leftX + 6), (float)dy, -1);
      dy += 11;
      String ownerStr = sel.ownerVillage != null && !sel.ownerVillage.isEmpty() ? sel.ownerVillage : "Neutral";
      int ownerColor = this.getTerritoryVillageColor(ownerStr.equals("Neutral") ? null : ownerStr);
      this.fontRenderer.drawStringWithShadow("§8Owner: ", (float)(leftX + 6), (float)dy, -7700886);
      this.fontRenderer.drawStringWithShadow(ownerStr, (float)(leftX + 6 + this.fontRenderer.getStringWidth("Owner: ")), (float)dy, ownerColor);
      dy += 10;
      this.fontRenderer.drawStringWithShadow("§8" + sel.centerX + ", " + sel.centerZ, (float)(leftX + 6), (float)dy, -7700886);
      dy += 10;
      if (this.mc.player != null) {
         double distX = this.mc.player.posX - (double)sel.centerX;
         double distZ = this.mc.player.posZ - (double)sel.centerZ;
         int dist = (int)Math.sqrt(distX * distX + distZ * distZ);
         this.fontRenderer.drawStringWithShadow("§8~" + dist + " blocks away", (float)(leftX + 6), (float)dy, -7700886);
         dy += 10;
      }

      if (sel.captureProgress > 0.0F) {
         float capMax = 75.0F + getFortifyBonusForLevel(sel.wallLevel);
         float capNorm = Math.min(1.0F, sel.captureProgress / capMax);
         if (capNorm < 1.0F) {
            dy += 2;
            int barX = leftX + 6;
            int barW = leftW - 12;
            drawRect(barX, dy, barX + barW, dy + 6, -15068144);
            int fillW = (int)(capNorm * (float)(barW - 2));
            drawRect(barX + 1, dy + 1, barX + 1 + Math.min(fillW, barW - 2), dy + 5, ownerColor);
            String capStr = (int)(capNorm * 100.0F) + "%";
            int capStrW = this.fontRenderer.getStringWidth(capStr);
            this.fontRenderer.drawStringWithShadow(capStr, (float)(barX + (barW - capStrW) / 2), (float)(dy - 1), -1);
            dy += 8;
            if (sel.contested) {
               this.fontRenderer.drawStringWithShadow("§cContested!", (float)(leftX + 6), (float)dy, -48060);
               dy += 10;
            }
         }
      }

      dy += 4;
      this.drawHorizontalLine(leftX + 4, leftX + leftW - 4, dy, -9807296);
      dy += 6;
      this.fontRenderer.drawStringWithShadow("§l§6Fortifications", (float)(leftX + 6), (float)dy, -1521552);
      dy += 12;
      boolean hasOwner = sel.ownerVillage != null && !sel.ownerVillage.isEmpty();
      String localVillage = TerritoryClientData.getInstance().getMyVillage();
      if (localVillage.isEmpty()) {
         localVillage = this.getTerritoryPlayerVillage();
      }

      boolean isOwnVillage = hasOwner && localVillage != null && !localVillage.isEmpty() && sel.ownerVillage.equalsIgnoreCase(localVillage);
      this.fortifyBtnCount = 0;
      if (hasOwner && !isOwnVillage) {
         this.fontRenderer.drawStringWithShadow("§8Enemy fortifications:", (float)(leftX + 6), (float)dy, -7700886);
         dy += 10;
         this.fontRenderer.drawStringWithShadow("§c  Unknown", (float)(leftX + 6), (float)dy, -3390396);
         dy += 14;
      } else {
         String[] upgradeNames = new String[]{"Walls", "Garrison", "Training", "Watchtower", "Specialist"};
         String[] upgradeIcons = new String[]{"▣", "⚔", "☠", "◉", "★"};
         int[] upgradeColors = new int[]{-7820596, -3381692, -10040218, -3355580, -3390260};
         String[] var10000 = new String[]{"wall", "garrison", "training", "watchtower", "specialist"};
         int[] currentLevels = new int[]{sel.wallLevel, sel.garrisonLevel, sel.trainingLevel, sel.watchtowerLevel, sel.specialistLevel};
         int[] maxLevels = new int[]{3, 3, 3, 2, 2};
         boolean isAkatsukiPlayer = AkatsukiClientData.isAkatsuki;
         int[][] costs;
         String currencyName;
         if (isAkatsukiPlayer) {
            costs = new int[][]{TerritoryConstants.AK_WALL_COSTS, TerritoryConstants.AK_GARRISON_COSTS, TerritoryConstants.AK_TRAINING_COSTS, TerritoryConstants.AK_WATCHTOWER_COSTS, TerritoryConstants.AK_SPECIALIST_COSTS};
            currencyName = "Tokens";
         } else {
            costs = new int[][]{TerritoryConstants.WALL_COSTS, TerritoryConstants.GARRISON_COSTS, TerritoryConstants.TRAINING_COSTS, TerritoryConstants.WATCHTOWER_COSTS, TerritoryConstants.SPECIALIST_COSTS};
            currencyName = "Ryo";
         }

         String[][] effects = new String[][]{{"+25 capture pts", "+50 capture pts", "+75 capture pts"}, {"3 defenders/wave", "4 defenders/wave", "5 defenders/wave"}, {"Tier 2 defenders", "Tier 3 defenders", "Tier 4 defenders"}, {"3min waves", "1.5min waves"}, {"+1 specialist NPC", "+2 specialist NPCs"}};

         for(int u = 0; u < 5; ++u) {
            int curLvl = currentLevels[u];
            int maxLvl = maxLevels[u];
            String lvlStr = curLvl >= maxLvl ? "§aMAX" : "Lv " + curLvl + "/" + maxLvl;
            String nameStr = upgradeIcons[u] + " " + upgradeNames[u];
            this.fontRenderer.drawStringWithShadow(nameStr, (float)(leftX + 6), (float)dy, upgradeColors[u]);
            int lvlW = this.fontRenderer.getStringWidth(lvlStr);
            this.fontRenderer.drawStringWithShadow(lvlStr, (float)(leftX + leftW - lvlW - 6), (float)dy, -7700886);
            dy += 10;
            if (curLvl > 0 && curLvl <= effects[u].length) {
               this.fontRenderer.drawStringWithShadow("  §8" + effects[u][curLvl - 1], (float)(leftX + 6), (float)dy, -7700886);
            } else if (curLvl == 0 && effects[u].length > 0) {
               this.fontRenderer.drawStringWithShadow("  §8Next: " + effects[u][0], (float)(leftX + 6), (float)dy, -7700886);
            }

            dy += 10;
            if (!hasOwner) {
               this.fontRenderer.drawStringWithShadow("  §8Capture first", (float)(leftX + 6), (float)dy, -7700886);
            } else if (curLvl < maxLvl) {
               int nextCost = costs[u][curLvl];
               String btnText = "§e[Upgrade: " + nextCost + " " + currencyName + "]";
               int btnTextW = this.fontRenderer.getStringWidth(btnText);
               int btnX = leftX + 8;
               boolean hovered = mouseX >= btnX && mouseX < btnX + btnTextW && mouseY >= dy && mouseY < dy + 9 && mouseY >= contentTop && mouseY < contentBot;
               if (hovered) {
                  this.fontRenderer.drawStringWithShadow("§6[Upgrade: " + nextCost + " " + currencyName + "]", (float)btnX, (float)dy, -13312);
               } else {
                  this.fontRenderer.drawStringWithShadow(btnText, (float)btnX, (float)dy, -22016);
               }

               if (this.fortifyBtnCount < 5) {
                  this.fortifyBtnBounds[this.fortifyBtnCount][0] = btnX;
                  this.fortifyBtnBounds[this.fortifyBtnCount][1] = dy;
                  this.fortifyBtnBounds[this.fortifyBtnCount][2] = btnTextW;
                  this.fortifyBtnBounds[this.fortifyBtnCount][3] = u;
                  ++this.fortifyBtnCount;
               }
            }

            dy += 12;
            dy += 2;
         }
      }

      GL11.glDisable(3089);
      int totalContentH = dy + this.fortifyScrollOffset - contentTop;
      int visibleH = contentBot - contentTop;
      if (totalContentH > visibleH) {
         int trackX = leftX + leftW - 3;
         int trackH = leftPanelH - 6;
         int trackTop = leftTop + 3;
         drawRect(trackX, trackTop, trackX + 2, trackTop + trackH, 1090519039);
         int maxScroll = totalContentH - visibleH;
         float scrollFrac = (float)this.fortifyScrollOffset / (float)maxScroll;
         int thumbH = Math.max(8, trackH * visibleH / totalContentH);
         int thumbY = trackTop + (int)(scrollFrac * (float)(trackH - thumbH));
         drawRect(trackX, thumbY, trackX + 2, thumbY + thumbH, -1593835521);
      }

   }

   private String getTerritoryPlayerVillage() {
      if (this.mc.player == null) {
         return "";
      } else {
         Team team = this.mc.player.getTeam();
         if (team == null) {
            return "";
         } else {
            String teamName = team.getName();
            if (teamName != null && !teamName.isEmpty()) {
               switch (teamName.toLowerCase()) {
                  case "leaf":
                  case "konoha":
                     return "leaf";
                  case "sand":
                  case "sunagakure":
                     return "sand";
                  case "mist":
                  case "kirigakure":
                     return "mist";
                  case "stone":
                  case "iwagakure":
                     return "stone";
                  case "cloud":
                  case "kumogakure":
                     return "cloud";
                  case "rain":
                  case "amegakure":
                  case "ame":
                     return "rain";
                  default:
                     return lower;
               }
            } else {
               return "";
            }
         }
      }
   }

   private void drawTerritoryMap(int clipX, int clipY, int clipW, int clipH, Map<String, TerritoryClientData.ClientZoneData> zones, int mouseX, int mouseY) {
      ScaledResolution sr = new ScaledResolution(this.mc);
      int sf = sr.getScaleFactor();
      GL11.glEnable(3089);
      GL11.glScissor(clipX * sf, (sr.getScaledHeight() - clipY - clipH) * sf, clipW * sf, clipH * sf);
      drawRect(clipX, clipY, clipX + clipW, clipY + clipH, -15853014);
      float viewHalfW = 6000.0F / this.mapZoom;
      float viewHalfH = 6000.0F / this.mapZoom;
      float viewCenterX = this.mapOffsetX;
      float viewCenterZ = this.mapOffsetZ;
      viewCenterX = Math.max(-6000.0F + viewHalfW, Math.min(6000.0F - viewHalfW, viewCenterX));
      viewCenterZ = Math.max(-6000.0F + viewHalfH, Math.min(6000.0F - viewHalfH, viewCenterZ));
      float viewMinX = viewCenterX - viewHalfW;
      float viewMaxX = viewCenterX + viewHalfW;
      float viewMinZ = viewCenterZ - viewHalfH;
      float viewMaxZ = viewCenterZ + viewHalfH;
      this.drawTerrainTexture(clipX, clipY, clipW, clipH, viewMinX, viewMaxX, viewMinZ, viewMaxZ);
      this.drawMapGrid(clipX, clipY, clipW, clipH, viewMinX, viewMaxX, viewMinZ, viewMaxZ);
      long tick = System.currentTimeMillis();
      String hoveredZone = null;
      double bestHoverDist = Double.MAX_VALUE;

      for(TerritoryClientData.ClientZoneData czd : zones.values()) {
         int sx = this.worldToScreenX((float)czd.centerX, clipX, clipW, viewMinX, viewMaxX);
         int sz = this.worldToScreenZ((float)czd.centerZ, clipY, clipH, viewMinZ, viewMaxZ);
         if (sx >= clipX - 20 && sx <= clipX + clipW + 20 && sz >= clipY - 20 && sz <= clipY + clipH + 20) {
            float worldRadius = (float)czd.radius;
            int screenRadius = Math.max(1, (int)(worldRadius / (viewMaxX - viewMinX) * (float)clipW));
            String owner = czd.ownerVillage != null && !czd.ownerVillage.isEmpty() ? czd.ownerVillage : null;
            int color = this.getTerritoryVillageColor(owner);
            boolean isSelected = czd.zoneId.equals(this.selectedZoneId);
            float captureMax = 75.0F + getFortifyBonusForLevel(czd.fortifyLevel);
            float capNorm = Math.min(1.0F, czd.captureProgress / captureMax);
            boolean isContested = czd.captureProgress > 0.0F && capNorm < 1.0F;
            if (screenRadius <= 4) {
               int fillColor = color & 16777215 | -1879048192;
               int r = Math.max(1, screenRadius);
               drawRect(sx - r, sz - r, sx + r + 1, sz + r + 1, fillColor);
               if (isSelected) {
                  drawRect(sx - r - 1, sz - r - 1, sx + r + 2, sz + r + 2, -1056964796);
               }
            } else {
               int fillRadius = screenRadius - 1;
               int alpha = owner != null ? 128 : 64;
               int fillColor = color & 16777215 | alpha << 24;
               this.drawFilledHexagon(sx, sz, fillRadius, fillColor);
               if (isContested && !isSelected) {
                  float pulse = (float)(Math.sin((double)tick / (double)300.0F) * (double)0.5F + (double)0.5F);
                  int pulseAlpha = (int)(128.0F + pulse * 127.0F);
                  int contestedColor = pulseAlpha << 24 | 16729156;
                  this.drawHexagonBorder(sx, sz, fillRadius, contestedColor, 2);
               } else {
                  int borderColor = isSelected ? -188 : color | -16777216;
                  int borderThickness = isSelected ? 2 : 1;
                  this.drawHexagonBorder(sx, sz, fillRadius, borderColor, borderThickness);
               }
            }

            if (this.mapZoom >= 5.0F && screenRadius > 8) {
               String label = czd.displayName;
               int labelW = this.fontRenderer.getStringWidth(label);
               this.fontRenderer.drawStringWithShadow(label, (float)(sx - labelW / 2), (float)(sz + screenRadius + 2), -2832216);
            }

            if (this.mapZoom >= 5.0F && isContested) {
               String pct = (int)(capNorm * 100.0F) + "%";
               int pctW = this.fontRenderer.getStringWidth(pct);
               this.fontRenderer.drawStringWithShadow(pct, (float)(sx - pctW / 2), (float)(sz - 4), -1);
            }

            int hoverRadius = Math.max(screenRadius, 8);
            if (this.isInsideHex(mouseX, mouseY, sx, sz, hoverRadius)) {
               double hdx = (double)(mouseX - sx);
               double hdz = (double)(mouseY - sz);
               double hDist = hdx * hdx + hdz * hdz;
               if (hDist < bestHoverDist) {
                  bestHoverDist = hDist;
                  hoveredZone = czd.zoneId;
               }
            }
         }
      }

      for(int i = 0; i < TERRITORY_VILLAGE_ORDER.length; ++i) {
         int vx = this.worldToScreenX((float)VILLAGE_CENTERS[i][0], clipX, clipW, viewMinX, viewMaxX);
         int vz = this.worldToScreenZ((float)VILLAGE_CENTERS[i][1], clipY, clipH, viewMinZ, viewMaxZ);
         int vColor = this.getTerritoryVillageColor(TERRITORY_VILLAGE_ORDER[i]);
         int ms = 3;
         drawRect(vx - ms - 1, vz - ms - 1, vx + ms + 2, vz + ms + 2, -1);
         drawRect(vx - ms, vz - ms, vx + ms + 1, vz + ms + 1, vColor);
         String vName = TERRITORY_VILLAGE_DISPLAY[i];
         int nameW = this.fontRenderer.getStringWidth(vName);
         drawRect(vx - nameW / 2 - 1, vz + ms + 3, vx + nameW / 2 + 1, vz + ms + 13, -1879048192);
         this.fontRenderer.drawStringWithShadow("§f" + vName, (float)(vx - nameW / 2), (float)(vz + ms + 4), -1);
      }

      if (this.mc.player != null) {
         int px = this.worldToScreenX((float)this.mc.player.posX, clipX, clipW, viewMinX, viewMaxX);
         int pz = this.worldToScreenZ((float)this.mc.player.posZ, clipY, clipH, viewMinZ, viewMaxZ);
         boolean blink = tick / 500L % 2L == 0L;
         if (blink) {
            drawRect(px - 2, pz - 2, px + 3, pz + 3, -1);
            drawRect(px - 3, pz - 3, px + 4, pz + 4, -2130706433);
         } else {
            drawRect(px - 2, pz - 2, px + 3, pz + 3, -3355444);
         }
      }

      int legendX = clipX + clipW - 58;
      int legendY = clipY + clipH - 8 * TERRITORY_VILLAGE_ORDER.length - 4;
      drawRect(legendX - 2, legendY - 2, clipX + clipW, clipY + clipH, -1609560048);

      for(int i = 0; i < TERRITORY_VILLAGE_ORDER.length; ++i) {
         int lc = this.getTerritoryVillageColor(TERRITORY_VILLAGE_ORDER[i]);
         drawRect(legendX, legendY + i * 8, legendX + 5, legendY + i * 8 + 5, lc);
         this.fontRenderer.drawStringWithShadow(TERRITORY_VILLAGE_DISPLAY[i], (float)(legendX + 8), (float)(legendY + i * 8 - 1), -7700886);
      }

      GL11.glDisable(3089);
      if (hoveredZone != null) {
         TerritoryClientData.ClientZoneData hz = TerritoryClientData.getInstance().getZone(hoveredZone);
         if (hz != null) {
            String hOwner = hz.ownerVillage != null && !hz.ownerVillage.isEmpty() ? hz.ownerVillage : "Neutral";
            List<String> tooltip = new ArrayList();
            tooltip.add("§f" + hz.displayName);
            int ownerColor = hOwner.equals("Neutral") ? 7 : 10;
            tooltip.add("§7Owner: §" + Integer.toHexString(ownerColor) + hOwner);
            float hCapMax = 75.0F + getFortifyBonusForLevel(hz.fortifyLevel);
            float hCapNorm = Math.min(1.0F, hz.captureProgress / hCapMax);
            int hCapPct = (int)(hCapNorm * 100.0F);
            if (hz.captureProgress > 0.0F && hCapNorm < 1.0F) {
               tooltip.add("§cContested! " + hCapPct + "%");
            }

            if (hz.fortifyLevel > 0) {
               String[] fNames = new String[]{"", "Basic", "Reinforced", "Fortified"};
               tooltip.add("§e⚔ " + fNames[hz.fortifyLevel] + " Defenses");
            }

            tooltip.add("§8(" + hz.centerX + ", " + hz.centerZ + ")");
            if (this.mc.player != null) {
               double dx = this.mc.player.posX - (double)hz.centerX;
               double dz = this.mc.player.posZ - (double)hz.centerZ;
               int dist = (int)Math.sqrt(dx * dx + dz * dz);
               tooltip.add("§8~" + dist + " blocks");
            }

            this.drawHoveringText(tooltip, mouseX, mouseY);
         }
      }

   }

   private int worldToScreenX(float worldX, int clipX, int clipW, float viewMinX, float viewMaxX) {
      return clipX + (int)((worldX - viewMinX) / (viewMaxX - viewMinX) * (float)clipW);
   }

   private int worldToScreenZ(float worldZ, int clipY, int clipH, float viewMinZ, float viewMaxZ) {
      return clipY + (int)((worldZ - viewMinZ) / (viewMaxZ - viewMinZ) * (float)clipH);
   }

   private void drawMapGrid(int clipX, int clipY, int clipW, int clipH, float viewMinX, float viewMaxX, float viewMinZ, float viewMaxZ) {
      int gridColor = 369098751;
      float gridStep = 2000.0F;
      float startX = (float)(Math.ceil((double)(viewMinX / gridStep)) * (double)gridStep);

      for(float gx = startX; gx <= viewMaxX; gx += gridStep) {
         int sx = this.worldToScreenX(gx, clipX, clipW, viewMinX, viewMaxX);
         if (sx >= clipX && sx < clipX + clipW) {
            drawRect(sx, clipY, sx + 1, clipY + clipH, gridColor);
            String coord = String.valueOf((int)gx);
            int cw = this.fontRenderer.getStringWidth(coord);
            if (sx - cw / 2 > clipX + 2 && sx + cw / 2 < clipX + clipW - 2) {
               this.fontRenderer.drawStringWithShadow("§8" + coord, (float)(sx - cw / 2), (float)(clipY + 2), -11184811);
            }
         }
      }

      float startZ = (float)(Math.ceil((double)(viewMinZ / gridStep)) * (double)gridStep);

      for(float gz = startZ; gz <= viewMaxZ; gz += gridStep) {
         int sz = this.worldToScreenZ(gz, clipY, clipH, viewMinZ, viewMaxZ);
         if (sz >= clipY && sz < clipY + clipH) {
            drawRect(clipX, sz, clipX + clipW, sz + 1, gridColor);
            String coord = String.valueOf((int)gz);
            if (sz > clipY + 10 && sz < clipY + clipH - 10) {
               this.fontRenderer.drawStringWithShadow("§8" + coord, (float)(clipX + 2), (float)(sz + 2), -11184811);
            }
         }
      }

      int bx1 = this.worldToScreenX(-6000.0F, clipX, clipW, viewMinX, viewMaxX);
      int bx2 = this.worldToScreenX(6000.0F, clipX, clipW, viewMinX, viewMaxX);
      int bz1 = this.worldToScreenZ(-6000.0F, clipY, clipH, viewMinZ, viewMaxZ);
      int bz2 = this.worldToScreenZ(6000.0F, clipY, clipH, viewMinZ, viewMaxZ);
      int borderColor = 1090470980;
      drawRect(Math.max(bx1, clipX), Math.max(bz1, clipY), Math.min(bx2, clipX + clipW), Math.max(bz1, clipY) + 1, borderColor);
      drawRect(Math.max(bx1, clipX), Math.min(bz2, clipY + clipH) - 1, Math.min(bx2, clipX + clipW), Math.min(bz2, clipY + clipH), borderColor);
      drawRect(Math.max(bx1, clipX), Math.max(bz1, clipY), Math.max(bx1, clipX) + 1, Math.min(bz2, clipY + clipH), borderColor);
      drawRect(Math.min(bx2, clipX + clipW) - 1, Math.max(bz1, clipY), Math.min(bx2, clipX + clipW), Math.min(bz2, clipY + clipH), borderColor);
   }

   private void drawTerrainTexture(int clipX, int clipY, int clipW, int clipH, float viewMinX, float viewMaxX, float viewMinZ, float viewMaxZ) {
      int screenX1 = this.worldToScreenX(-6000.0F, clipX, clipW, viewMinX, viewMaxX);
      int screenZ1 = this.worldToScreenZ(-6000.0F, clipY, clipH, viewMinZ, viewMaxZ);
      int screenX2 = this.worldToScreenX(6000.0F, clipX, clipW, viewMinX, viewMaxX);
      int screenZ2 = this.worldToScreenZ(6000.0F, clipY, clipH, viewMinZ, viewMaxZ);
      float u1 = 0.0F;
      float v1 = 0.0F;
      float u2 = 1.0F;
      float v2 = 1.0F;
      int fullW = screenX2 - screenX1;
      int fullH = screenZ2 - screenZ1;
      if (fullW > 0 && fullH > 0) {
         if (screenX1 < clipX) {
            u1 = (float)(clipX - screenX1) / (float)fullW;
            screenX1 = clipX;
         }

         if (screenZ1 < clipY) {
            v1 = (float)(clipY - screenZ1) / (float)fullH;
            screenZ1 = clipY;
         }

         if (screenX2 > clipX + clipW) {
            u2 = 1.0F - (float)(screenX2 - (clipX + clipW)) / (float)fullW;
            screenX2 = clipX + clipW;
         }

         if (screenZ2 > clipY + clipH) {
            v2 = 1.0F - (float)(screenZ2 - (clipY + clipH)) / (float)fullH;
            screenZ2 = clipY + clipH;
         }

         if (screenX2 > screenX1 && screenZ2 > screenZ1) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TERRITORY_MAP_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            drawScaledCustomSizeModalRect(screenX1, screenZ1, u1 * 1145.0F, v1 * 1140.0F, (int)((u2 - u1) * 1145.0F), (int)((v2 - v1) * 1140.0F), screenX2 - screenX1, screenZ2 - screenZ1, 1145.0F, 1140.0F);
            GlStateManager.disableBlend();
         }
      }
   }

   private void drawBiomeTerrain(int clipX, int clipY, int clipW, int clipH, float viewMinX, float viewMaxX, float viewMinZ, float viewMaxZ) {
      for(int[] land : TERRAIN_LAND) {
         int shoreExpand = Math.max(1, (int)(80.0F / (viewMaxX - viewMinX) * (float)clipW));
         this.drawTerrainRect(land[0] - 80, land[1] - 80, land[2] + 80, land[3] + 80, -12955552, clipX, clipY, clipW, clipH, viewMinX, viewMaxX, viewMinZ, viewMaxZ);
      }

      for(int[] land : TERRAIN_LAND) {
         this.drawTerrainRect(land[0], land[1], land[2], land[3], land[4], clipX, clipY, clipW, clipH, viewMinX, viewMaxX, viewMinZ, viewMaxZ);
      }

      for(int[] detail : TERRAIN_DETAIL) {
         this.drawTerrainRect(detail[0], detail[1], detail[2], detail[3], detail[4], clipX, clipY, clipW, clipH, viewMinX, viewMaxX, viewMinZ, viewMaxZ);
      }

      if (this.mapZoom >= 2.0F) {
         this.drawBiomeLabels(clipX, clipY, clipW, clipH, viewMinX, viewMaxX, viewMinZ, viewMaxZ);
      }

   }

   private void drawTerrainRect(int wx1, int wz1, int wx2, int wz2, int color, int clipX, int clipY, int clipW, int clipH, float viewMinX, float viewMaxX, float viewMinZ, float viewMaxZ) {
      int sx1 = this.worldToScreenX((float)wx1, clipX, clipW, viewMinX, viewMaxX);
      int sz1 = this.worldToScreenZ((float)wz1, clipY, clipH, viewMinZ, viewMaxZ);
      int sx2 = this.worldToScreenX((float)wx2, clipX, clipW, viewMinX, viewMaxX);
      int sz2 = this.worldToScreenZ((float)wz2, clipY, clipH, viewMinZ, viewMaxZ);
      sx1 = Math.max(sx1, clipX);
      sz1 = Math.max(sz1, clipY);
      sx2 = Math.min(sx2, clipX + clipW);
      sz2 = Math.min(sz2, clipY + clipH);
      if (sx2 > sx1 && sz2 > sz1) {
         drawRect(sx1, sz1, sx2, sz2, color);
      }

   }

   private void drawBiomeLabels(int clipX, int clipY, int clipW, int clipH, float viewMinX, float viewMaxX, float viewMinZ, float viewMaxZ) {
      int[][] labelPositions = new int[][]{{-100, -400}, {-2650, 950}, {-2550, -2600}, {1700, -3050}, {3900, -1950}, {-2350, -900}, {1600, 450}, {-650, 800}, {650, -3100}};
      String[] labelNames = new String[]{"Fire Country", "Wind Country", "Earth Country", "Lightning Country", "Water Country", "Rain Country", "Land of Waves", "Land of Rivers", "Land of Frost"};

      for(int i = 0; i < labelNames.length; ++i) {
         int lx = this.worldToScreenX((float)labelPositions[i][0], clipX, clipW, viewMinX, viewMaxX);
         int lz = this.worldToScreenZ((float)labelPositions[i][1], clipY, clipH, viewMinZ, viewMaxZ);
         if (lx > clipX + 10 && lx < clipX + clipW - 10 && lz > clipY + 5 && lz < clipY + clipH - 5) {
            int nameW = this.fontRenderer.getStringWidth(labelNames[i]);
            drawRect(lx - nameW / 2 - 2, lz - 5, lx + nameW / 2 + 2, lz + 5, Integer.MIN_VALUE);
            this.fontRenderer.drawStringWithShadow("§7" + labelNames[i], (float)(lx - nameW / 2), (float)(lz - 3), -5592406);
         }
      }

   }

   private void drawRivers(int clipX, int clipY, int clipW, int clipH, float viewMinX, float viewMaxX, float viewMinZ, float viewMaxZ) {
      int riverColor = -15853014;
      int riverBank = -15062470;

      for(int[] seg : RIVER_SEGMENTS) {
         int sx1 = this.worldToScreenX((float)seg[0], clipX, clipW, viewMinX, viewMaxX);
         int sz1 = this.worldToScreenZ((float)seg[1], clipY, clipH, viewMinZ, viewMaxZ);
         int sx2 = this.worldToScreenX((float)seg[2], clipX, clipW, viewMinX, viewMaxX);
         int sz2 = this.worldToScreenZ((float)seg[3], clipY, clipH, viewMinZ, viewMaxZ);
         this.drawThickLine(sx1, sz1, sx2, sz2, 3, riverBank, clipX, clipY, clipW, clipH);
         this.drawThickLine(sx1, sz1, sx2, sz2, 1, riverColor, clipX, clipY, clipW, clipH);
      }

   }

   private void drawThickLine(int x1, int y1, int x2, int y2, int thickness, int color, int clipX, int clipY, int clipW, int clipH) {
      int dx = Math.abs(x2 - x1);
      int dz = Math.abs(y2 - y1);
      int sx = x1 < x2 ? 1 : -1;
      int sz = y1 < y2 ? 1 : -1;
      int err = dx - dz;
      int half = thickness / 2;
      int steps = 0;

      for(int maxSteps = dx + dz + 1; steps < maxSteps; ++steps) {
         int rx1 = Math.max(x1 - half, clipX);
         int ry1 = Math.max(y1 - half, clipY);
         int rx2 = Math.min(x1 + half + 1, clipX + clipW);
         int ry2 = Math.min(y1 + half + 1, clipY + clipH);
         if (rx2 > rx1 && ry2 > ry1) {
            drawRect(rx1, ry1, rx2, ry2, color);
         }

         if (x1 == x2 && y1 == y2) {
            break;
         }

         int e2 = 2 * err;
         if (e2 > -dz) {
            err -= dz;
            x1 += sx;
         }

         if (e2 < dx) {
            err += dx;
            y1 += sz;
         }
      }

   }

   private void drawFilledHexagon(int cx, int cy, int radius, int color) {
      if (radius <= 1) {
         drawRect(cx, cy, cx + 1, cy + 1, color);
      } else {
         double hexHalfH = Math.sqrt((double)3.0F) * (double)radius / (double)2.0F;
         int halfHeight = (int)Math.ceil(hexHalfH);

         for(int dy = -halfHeight; dy <= halfHeight; ++dy) {
            double absY = (double)Math.abs(dy);
            if (!(absY > hexHalfH)) {
               int halfW = (int)((double)radius * ((double)1.0F - absY / ((double)2.0F * hexHalfH)));
               if (halfW >= 0) {
                  drawRect(cx - halfW, cy + dy, cx + halfW + 1, cy + dy + 1, color);
               }
            }
         }

      }
   }

   private void drawHexagonBorder(int cx, int cy, int radius, int color, int thickness) {
      if (radius <= 1) {
         drawRect(cx - 1, cy - 1, cx + 2, cy + 2, color);
      } else {
         for(int t = 0; t < thickness; ++t) {
            int r = radius + t;
            int halfH = (int)(Math.sqrt((double)3.0F) * (double)r / (double)2.0F);
            int halfR = r / 2;
            int[] vx = new int[]{cx + r, cx + halfR, cx - halfR, cx - r, cx - halfR, cx + halfR};
            int[] vy = new int[]{cy, cy + halfH, cy + halfH, cy, cy - halfH, cy - halfH};

            for(int i = 0; i < 6; ++i) {
               int next = (i + 1) % 6;
               this.drawPixelLine(vx[i], vy[i], vx[next], vy[next], color);
            }
         }

      }
   }

   private boolean isInsideHex(int px, int py, int cx, int cy, int radius) {
      double dx = (double)(px - cx);
      double dy = (double)(py - cy);
      double q = 0.6666666666666666 * dx / (double)radius;
      double r = (-0.3333333333333333 * dx + Math.sqrt((double)3.0F) / (double)3.0F * dy) / (double)radius;
      double s = -q - r;
      return Math.max(Math.abs(q), Math.max(Math.abs(r), Math.abs(s))) <= (double)1.0F;
   }

   private void drawPixelLine(int x0, int y0, int x1, int y1, int color) {
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx - dy;

      while(true) {
         drawRect(x0, y0, x0 + 1, y0 + 1, color);
         if (x0 == x1 && y0 == y1) {
            return;
         }

         int e2 = 2 * err;
         if (e2 > -dy) {
            err -= dy;
            x0 += sx;
         }

         if (e2 < dx) {
            err += dx;
            y0 += sy;
         }
      }
   }

   private void drawActiveQuestDetails(int detailX, int detailY, int maxWidth, QuestClientData.ActiveQuestState aq) {
      this.fontRenderer.drawStringWithShadow(aq.questName, (float)detailX, (float)detailY, -1521552);
      detailY += 14;
      String stepText = "Step " + (aq.stepIndex + 1) + "/" + aq.totalSteps;
      this.fontRenderer.drawStringWithShadow(stepText, (float)detailX, (float)detailY, -7700886);
      detailY += 16;
      this.drawHorizontalLine(detailX, detailX + maxWidth, detailY - 3, -11910088);
      this.fontRenderer.drawStringWithShadow("Objective:", (float)detailX, (float)detailY, -1521552);
      detailY += 12;
      if (aq.stepDesc != null) {
         for(String line : this.fontRenderer.listFormattedStringToWidth(aq.stepDesc, maxWidth)) {
            this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -2832216);
            detailY += 11;
         }
      }

      if (aq.killsRequired > 0) {
         detailY += 8;
         this.fontRenderer.drawStringWithShadow("Progress:", (float)detailX, (float)detailY, -1521552);
         detailY += 12;
         int barW = maxWidth - 40;
         drawRect(detailX, detailY, detailX + barW, detailY + 8, -15068144);
         int fillW = aq.killsRequired > 0 ? (int)((float)aq.killProgress / (float)aq.killsRequired * (float)barW) : 0;
         fillW = Math.min(fillW, barW);
         drawRect(detailX + 1, detailY + 1, detailX + 1 + fillW, detailY + 7, -9795038);
         drawRect(detailX, detailY, detailX + barW, detailY + 1, -11910088);
         drawRect(detailX, detailY + 7, detailX + barW, detailY + 8, -15068144);
         String killText = aq.killProgress + "/" + aq.killsRequired;
         this.fontRenderer.drawStringWithShadow(killText, (float)(detailX + barW + 4), (float)detailY, -2832216);
         detailY += 12;
      }

      if (aq.rewardSummary != null && !aq.rewardSummary.isEmpty()) {
         detailY += 8;
         this.fontRenderer.drawStringWithShadow("Reward: " + aq.rewardSummary, (float)detailX, (float)detailY, -7700886);
      }

   }

   private void drawCategoryHint(int detailX, int detailY, int maxWidth, String hint) {
      List<String> hintLines = this.fontRenderer.listFormattedStringToWidth(hint, maxWidth);
      int hintY = detailY + 40;

      for(String line : hintLines) {
         int w = this.fontRenderer.getStringWidth(line);
         this.fontRenderer.drawString(line, detailX + (maxWidth - w) / 2, hintY, -7700886);
         hintY += 11;
      }

   }

   private String getPveSubSlotLabel(String subSlot, int index) {
      if (QuestClientData.hasActiveQuestInSlot(subSlot)) {
         QuestClientData.ActiveQuestState aq = QuestClientData.getActiveQuestInSlot(subSlot);
         if (aq != null) {
            String name = aq.questName;
            if (name != null && name.length() > 20) {
               name = name.substring(0, 18) + "..";
            }

            return "§a▶ " + name;
         }
      }

      QuestClientData.OfferState offer = QuestClientData.getOffer(subSlot);
      if (offer != null) {
         if (offer.cooldownRemaining > 0L) {
            return "§7⌚ " + formatCooldown(offer.cooldownRemaining);
         }

         if (offer.name != null && !offer.name.isEmpty()) {
            String rankBadge = "";
            if (offer.rank >= 0 && offer.rank < RANK_NAMES.length) {
               rankBadge = getRankColorCode(offer.rank) + "[" + RANK_NAMES[offer.rank] + "] ";
            }

            String name = offer.name;
            if (name.length() > 18) {
               name = name.substring(0, 16) + "..";
            }

            return rankBadge + "§f" + name;
         }
      }

      return "§8Slot " + (index + 1);
   }

   private String getPvpSubSlotLabel(String subSlot, int index) {
      if (PvpClientData.hasActiveMission(subSlot)) {
         PvpClientData.ActivePvpMissionInfo mission = PvpClientData.getActiveMission(subSlot);
         if (mission != null) {
            String name = mission.name;
            if (name != null && name.length() > 20) {
               name = name.substring(0, 18) + "..";
            }

            return "§c▶ " + name;
         }
      }

      PvpClientData.PvpOfferInfo offer = PvpClientData.getOffer(subSlot);
      if (offer != null) {
         if (offer.isOnCooldown()) {
            return "§7⌚ " + formatCooldown(offer.cooldownRemainingMs);
         }

         if (offer.name != null && !offer.name.isEmpty()) {
            String rankBadge = "";
            if (offer.rankOrdinal >= 0 && offer.rankOrdinal < RANK_NAMES.length) {
               rankBadge = getRankColorCode((byte)offer.rankOrdinal) + "[" + RANK_NAMES[offer.rankOrdinal] + "] ";
            }

            String name = offer.name;
            if (name.length() > 18) {
               name = name.substring(0, 16) + "..";
            }

            return rankBadge + "§f" + name;
         }
      }

      if (subSlot.startsWith("pvp_daily")) {
         return "§8Daily " + (index + 1);
      } else {
         return subSlot.startsWith("pvp_weekly") ? "§8Weekly " + (index + 1) : "§8Slot " + (index + 1);
      }
   }

   private List<QuestClientData.QuestListEntry> getQuestsForStoryline(String storyline) {
      List<QuestClientData.QuestListEntry> all = QuestClientData.getAvailableQuests();
      ArrayList<QuestClientData.QuestListEntry> result = new ArrayList();
      switch (this.selectedTab) {
         case 0:
            for(QuestClientData.QuestListEntry e : all) {
               if (e.category == 0 && e.available && storyline.equals(e.storyline)) {
                  result.add(e);
               }
            }
            break;
         case 1:
            for(Map.Entry<String, QuestClientData.ActiveQuestState> entry : QuestClientData.getAllActiveQuests().entrySet()) {
               if (QuestClientData.isStorySlot((String)entry.getKey())) {
                  String sl = ((String)entry.getKey()).substring("story:".length());
                  if (storyline.equals(sl)) {
                     QuestClientData.ActiveQuestState sq = (QuestClientData.ActiveQuestState)entry.getValue();
                     result.add(new QuestClientData.QuestListEntry(sq.questId, sq.questName, sq.stepDesc, 0, true));
                  }
               }
            }
            break;
         case 2:
            for(String id : QuestClientData.getCompletedQuestIds()) {
               boolean found = false;

               for(QuestClientData.QuestListEntry entry : all) {
                  if (entry.id.equals(id) && entry.category == 0 && storyline.equals(entry.storyline)) {
                     result.add(entry);
                     found = true;
                     break;
                  }
               }

               if (!found && "leaf_story".equals(storyline)) {
                  boolean existsElsewhere = false;

                  for(QuestClientData.QuestListEntry entry : all) {
                     if (entry.id.equals(id) && entry.category == 0) {
                        existsElsewhere = true;
                        break;
                     }
                  }

                  if (!existsElsewhere) {
                     result.add(new QuestClientData.QuestListEntry(id, id, "", 0, false));
                  }
               }
            }
      }

      return result;
   }

   private List<QuestClientData.QuestListEntry> getQuestsForCurrentView() {
      List<String> storylines = this.getStorylinesForCurrentTab();
      ArrayList<QuestClientData.QuestListEntry> result = new ArrayList();
      if (this.selectedCategory == 0) {
         for(String sl : storylines) {
            result.addAll(this.getQuestsForStoryline(sl));
         }
      }

      return result;
   }

   protected void actionPerformed(GuiButton button) {
      if (button.id >= 560 && button.id < 580) {
         int rowIdx = this.boardContractScrollOffset + (button.id - 560);
         List<ContractNetworkMessage.ContractClientEntry> list = ContractClientData.entries;
         if (rowIdx >= 0 && rowIdx < list.size()) {
            String contractId = ((ContractNetworkMessage.ContractClientEntry)list.get(rowIdx)).id;
            EndgameModInit.NETWORK.sendToServer(ContractNetworkMessage.ActionMessage.accept(contractId));
         }

      } else {
         switch (button.id) {
            case 0:
               this.mc.displayGuiScreen((GuiScreen)null);
               break;
            case 1:
               List<QuestClientData.QuestListEntry> quests = this.getQuestsForCurrentView();
               if (this.selectedQuestIndex >= 0 && this.selectedQuestIndex < quests.size()) {
                  QuestModInit.NETWORK.sendToServer(new QuestActionMessage(0, ((QuestClientData.QuestListEntry)quests.get(this.selectedQuestIndex)).id));
               }
               break;
            case 2:
               if (this.selectedCategory == 0) {
                  QuestModInit.NETWORK.sendToServer(new QuestActionMessage(1, "story"));
               } else if (this.selectedCategory == 1) {
                  String slot = this.getPveSubSlot();
                  QuestModInit.NETWORK.sendToServer(new QuestActionMessage(1, slot));
               }
               break;
            case 3:
               this.selectedTab = 0;
               this.selectedQuestIndex = -1;
               this.scrollOffset = 0;
               this.selectedStorylineIndex = 0;
               this.rebuildButtons();
               break;
            case 4:
               this.selectedTab = 1;
               this.selectedQuestIndex = -1;
               this.scrollOffset = 0;
               this.selectedStorylineIndex = 0;
               this.rebuildButtons();
               break;
            case 5:
               this.selectedTab = 2;
               this.selectedQuestIndex = -1;
               this.scrollOffset = 0;
               this.selectedStorylineIndex = 0;
               this.rebuildButtons();
               break;
            case 6:
               this.selectedCategory = 0;
               this.selectedTab = 0;
               this.selectedQuestIndex = -1;
               this.selectedSubSlotIndex = 0;
               this.scrollOffset = 0;
               this.warDeclareStep = 0;
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               this.selectedStorylineIndex = 0;
               this.rebuildButtons();
               break;
            case 7:
               this.selectedCategory = 1;
               this.selectedQuestIndex = -1;
               this.selectedSubSlotIndex = 0;
               this.scrollOffset = 0;
               this.warDeclareStep = 0;
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               if (!this.endgameDataRequested) {
                  this.endgameDataRequested = true;
                  EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)10));
               }

               this.rebuildButtons();
               break;
            case 8:
               this.selectedCategory = 2;
               this.selectedQuestIndex = -1;
               this.pvpSubSlotIndex = 0;
               this.scrollOffset = 0;
               this.bingoScrollOffset = 0;
               this.warDeclareStep = 0;
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               this.rebuildButtons();
               break;
            case 9:
               this.selectedCategory = 3;
               this.warDeclareStep = 0;
               this.warSelectedMode = -1;
               this.warSelectedTarget = null;
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               this.rebuildButtons();
               break;
            case 10:
               RyoShopGui.open();
               break;
            case 11:
               String slot = this.getPveSubSlot();
               QuestModInit.NETWORK.sendToServer(new QuestActionMessage(6, slot));
               break;
            case 12:
               String slot = this.getPveSubSlot();
               QuestModInit.NETWORK.sendToServer(new QuestActionMessage(7, slot));
               break;
            case 13:
               this.pveSubTab = 0;
               this.selectedSubSlotIndex = 0;
               this.rebuildButtons();
               break;
            case 14:
               this.pveSubTab = 1;
               this.selectedSubSlotIndex = 0;
               this.rebuildButtons();
               break;
            case 15:
               this.pveSubTab = 2;
               this.selectedSubSlotIndex = 0;
               this.rebuildButtons();
               break;
            case 16:
               this.pvpSubTab = 0;
               this.pvpSubSlotIndex = 0;
               this.selectedQuestIndex = -1;
               this.rebuildButtons();
               break;
            case 17:
               this.pvpSubTab = 1;
               this.pvpSubSlotIndex = 0;
               this.selectedQuestIndex = -1;
               this.rebuildButtons();
               break;
            case 18:
               this.pvpSubTab = 2;
               this.pvpSubSlotIndex = 0;
               this.selectedQuestIndex = -1;
               this.rebuildButtons();
               break;
            case 19:
               this.pvpSubTab = 3;
               this.selectedQuestIndex = -1;
               this.bingoScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 30:
               byte slotByte = this.getPvpSubSlotByte();
               PvpModInit.NETWORK.sendToServer(new PvpActionMessage(0, slotByte));
               break;
            case 31:
               byte slotByte = this.getPvpSubSlotByte();
               PvpModInit.NETWORK.sendToServer(new PvpActionMessage(1, slotByte));
               break;
            case 32:
               byte slotByte;
               if (this.pvpSubTab == 4 && this.leadershipSubView == 1) {
                  slotByte = PvpSyncMessage.pvpSubSlotToByte("pvp_assigned");
               } else {
                  slotByte = this.getPvpSubSlotByte();
               }

               PvpModInit.NETWORK.sendToServer(new PvpActionMessage(2, slotByte));
               break;
            case 33:
               PvpModInit.NETWORK.sendToServer(new PvpActionMessage(3, (byte)this.selectedQuestIndex));
               break;
            case 40:
               this.mc.displayGuiScreen((GuiScreen)null);
               this.mc.player.sendChatMessage("/village");
               break;
            case 41:
               GuiRankedMenu.open();
               break;
            case 42:
               this.selectedCategory = 5;
               this.statSubTab = 0;
               this.statScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 43:
               this.statSubTab = 0;
               this.statScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 44:
               this.statSubTab = 1;
               this.statScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 45:
               this.statSubTab = 2;
               this.statScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 46:
               StatModInit.NETWORK.sendToServer(StatAllocateMessage.respec());
               break;
            case 47:
               this.statSubTab = 3;
               this.statScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 49:
               this.selectedCategory = 7;
               this.factionSubTab = 0;
               this.scrollOffset = 0;
               this.akatsukiAssignScrollOffset = 0;
               this.warDeclareStep = 0;
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               this.leaderRaidSelecting = false;
               this.leaderSelectedMember1 = -1;
               this.leaderSelectedMember2 = -1;
               this.rebuildButtons();
               break;
            case 50:
               this.warDeclareStep = 1;
               this.warSelectedMode = -1;
               this.warSelectedTarget = null;
               this.rebuildButtons();
               break;
            case 51:
               PvpModInit.NETWORK.sendToServer(new WarActionMessage((byte)1));
               this.rebuildButtons();
               break;
            case 52:
               this.warSelectedMode = WarMode.SKIRMISH.ordinal();
               this.warDeclareStep = 2;
               this.warSelectedTarget = null;
               this.rebuildButtons();
               break;
            case 53:
               this.warSelectedMode = WarMode.DIVISION.ordinal();
               this.warDeclareStep = 2;
               this.warSelectedTarget = null;
               this.rebuildButtons();
               break;
            case 54:
               this.warSelectedMode = WarMode.DOMINATION.ordinal();
               this.warDeclareStep = 2;
               this.warSelectedTarget = null;
               this.rebuildButtons();
               break;
            case 55:
               this.warSelectedMode = WarMode.RUSH.ordinal();
               this.warDeclareStep = 2;
               this.warSelectedTarget = null;
               this.rebuildButtons();
               break;
            case 57:
               this.warSelectedMode = WarMode.INFILTRATION.ordinal();
               this.warDeclareStep = 2;
               this.warSelectedTarget = null;
               this.rebuildButtons();
               break;
            case 66:
               if (this.warSelectedMode >= 0 && this.warSelectedTarget != null) {
                  if (this.warSelectedMode == WarMode.DIVISION.ordinal()) {
                     PvpModInit.NETWORK.sendToServer(new WarRosterRequestMessage((byte)this.warSelectedMode, this.warSelectedTarget));
                  } else {
                     PvpModInit.NETWORK.sendToServer(WarActionMessage.declareWar((byte)this.warSelectedMode, this.warSelectedTarget));
                  }

                  this.warDeclareStep = 0;
               }

               this.rebuildButtons();
               break;
            case 67:
               PvpModInit.NETWORK.sendToServer(new WarActionMessage((byte)4));
               break;
            case 68:
               if (this.warDeclareStep > 0) {
                  this.warDeclareStep = 0;
                  this.warSelectedMode = -1;
                  this.warSelectedTarget = null;
               }

               this.rebuildButtons();
               break;
            case 69:
               PvpModInit.NETWORK.sendToServer(new WarActionMessage((byte)7));
               break;
            case 70:
               PvpModInit.NETWORK.sendToServer(new WarActionMessage((byte)5));
               break;
            case 71:
               PvpModInit.NETWORK.sendToServer(new WarActionMessage((byte)6));
               break;
            case 72:
               PvpModInit.NETWORK.sendToServer(new WarRosterRequestMessage((byte)0, "", true));
               break;
            case 75:
               this.pvpSubTab = 4;
               this.leadershipSubView = !PvpClientData.isKage && !PvpClientData.isAdvisor && !PvpClientData.hasWarAuthority ? 1 : 0;
               this.leadershipStep = 0;
               this.rebuildButtons();
               break;
            case 76:
               PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.acceptMission());
               break;
            case 77:
               PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.rerollMission());
               break;
            case 78:
               PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.abandonMission());
               break;
            case 79:
               this.pvpSubTab = 5;
               this.rebuildButtons();
               break;
            case 80:
               this.leadershipSubView = 2;
               this.leadershipStep = 2;
               this.rebuildButtons();
               break;
            case 92:
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               this.rebuildButtons();
               break;
            case 93:
               this.leadershipSubView = 2;
               this.leadershipStep = 1;
               this.rebuildButtons();
               break;
            case 130:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.signup());
               break;
            case 131:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.withdraw());
               break;
            case 132:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.createTournament(5, 120, (byte)this.tournamentMinRank, (byte)this.tournamentMaxRank));
               break;
            case 133:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.finalize_());
               break;
            case 134:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.cancel());
               break;
            case 135:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.openRewardGui((byte)1));
               break;
            case 136:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.openRewardGui((byte)2));
               break;
            case 137:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.openRewardGui((byte)3));
               break;
            case 138:
               this.tournamentMinRank = cycleRank(this.tournamentMinRank);
               this.rebuildButtons();
               break;
            case 139:
               this.tournamentMaxRank = cycleRank(this.tournamentMaxRank);
               this.rebuildButtons();
               break;
            case 140:
               PvpModInit.NETWORK.sendToServer(TournamentActionMessage.toggleVillageOnly());
               break;
            case 150:
               this.leadershipSubView = 0;
               this.leadershipStep = 0;
               this.ordersOverviewScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 151:
               this.leadershipSubView = 1;
               this.leadershipStep = 0;
               this.missionScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 152:
               this.leadershipSubView = 2;
               this.leadershipStep = 0;
               this.rebuildButtons();
               break;
            case 500:
               this.pveSubTab = 3;
               this.endgameSubTab = 0;
               this.rebuildButtons();
               break;
            case 501:
               this.pveSubTab = 5;
               this.endgameSubTab = 1;
               this.endgameBingoIndex = 0;
               this.rebuildButtons();
               break;
            case 502:
               this.pveSubTab = 6;
               this.endgameSubTab = 2;
               this.rebuildButtons();
               break;
            case 503:
               this.pveSubTab = 7;
               this.endgameSubTab = 3;
               this.endgameLbCategory = 0;
               EndgameModInit.NETWORK.sendToServer(EndgameActionMessage.leaderboardRequest("outposts"));
               this.rebuildButtons();
               break;
            case 504:
               this.pveSubTab = 4;
               this.boardContractScrollOffset = 0;
               EndgameModInit.NETWORK.sendToServer(ContractNetworkMessage.ActionMessage.requestSync());
               this.rebuildButtons();
               break;
            case 523:
               EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)0));
               this.rebuildButtons();
               break;
            case 524:
               EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)1));
               this.rebuildButtons();
               break;
            case 530:
            case 531:
            case 532:
            case 533:
            case 534:
               this.endgameBingoIndex = button.id - 530;
               this.rebuildButtons();
               break;
            case 535:
               EndgameClientData data = EndgameClientData.getInstance();
               if (data.hasActiveHunt()) {
                  EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)3));
               } else {
                  EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)2, (byte)this.endgameBingoIndex));
               }
               break;
            case 540:
               if (EndgameClientData.getInstance().hasJoinedIncursion()) {
                  EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)6));
               } else {
                  EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)4));
               }
               break;
            case 541:
               EndgameModInit.NETWORK.sendToServer(new EndgameActionMessage((byte)5));
               break;
            case 550:
            case 551:
            case 552:
            case 553:
               this.endgameLbCategory = button.id - 550;
               String[] lbCats = new String[]{"outposts", "bingo", "incursions", "defense"};
               EndgameModInit.NETWORK.sendToServer(EndgameActionMessage.leaderboardRequest(lbCats[this.endgameLbCategory]));
               this.rebuildButtons();
               break;
            case 559:
               EndgameModInit.NETWORK.sendToServer(ContractNetworkMessage.ActionMessage.abandon());
               break;
            case 700:
            case 701:
            case 702:
            case 703:
            case 704:
            case 705:
            case 706:
               this.factionSubTab = button.id - 700;
               this.akatsukiAssignScrollOffset = 0;
               this.leaderRaidSelecting = false;
               this.leaderSelectedMember1 = -1;
               this.leaderSelectedMember2 = -1;
               this.leaderMode = 0;
               this.selectedBlackMarketItemId = null;
               this.blackMarketZoneScrollOffset = 0;
               this.blackMarketItemScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 710:
               AkatsukiModInit.NETWORK.sendToServer(new AkatsukiInviteResponseMessage(true));
               break;
            case 711:
               AkatsukiModInit.NETWORK.sendToServer(new AkatsukiInviteResponseMessage(false));
               this.mc.displayGuiScreen((GuiScreen)null);
               break;
            case 720:
            case 721:
            case 722:
            case 723:
            case 724:
               AkatsukiModInit.NETWORK.sendToServer(new AkatsukiRingUpgradeMessage(button.id - 720));
               break;
            case 730:
               this.contractTypeIndex = (this.contractTypeIndex + 1) % 2;
               this.rebuildButtons();
               break;
            case 731:
               this.contractRyoAmount = Math.max(100, this.contractRyoAmount - 100);
               this.rebuildButtons();
               break;
            case 732:
               this.contractRyoAmount = Math.min(10000, this.contractRyoAmount + 100);
               this.rebuildButtons();
               break;
            case 733:
               String postTargetName = "";
               String postTargetZoneId = "";
               if (this.contractTypeIndex == 0) {
                  List<String> onlinePlayers = this.getOnlinePlayerNames();
                  if (this.contractTargetPlayerIndex >= 0 && this.contractTargetPlayerIndex < onlinePlayers.size()) {
                     postTargetName = (String)onlinePlayers.get(this.contractTargetPlayerIndex);
                  }
               } else if (this.contractTypeIndex == 1) {
                  postTargetZoneId = String.valueOf(this.contractTargetZoneIndex + 1);
               }

               AkatsukiModInit.NETWORK.sendToServer(ContractActionMessage.post(this.contractTypeIndex, postTargetName, this.contractRyoAmount, postTargetZoneId));
               break;
            case 734:
               List<AkatsukiClientData.ContractClientEntry> myC = AkatsukiClientData.myContracts;
               if (!myC.isEmpty()) {
                  AkatsukiModInit.NETWORK.sendToServer(ContractActionMessage.cancel(((AkatsukiClientData.ContractClientEntry)myC.get(0)).id));
               }
               break;
            case 735:
            case 736:
            case 737:
            case 738:
            case 739:
               int cIdx = button.id - 735;
               List<AkatsukiClientData.ContractClientEntry> openC = AkatsukiClientData.openContracts;
               if (cIdx < openC.size()) {
                  AkatsukiModInit.NETWORK.sendToServer(ContractActionMessage.accept(((AkatsukiClientData.ContractClientEntry)openC.get(cIdx)).id));
               }

               this.rebuildButtons();
               break;
            case 740:
               this.leaderRaidSelecting = !this.leaderRaidSelecting;
               this.rebuildButtons();
               break;
            case 741:
               AkatsukiModInit.NETWORK.sendToServer(new LeaderActionMessage(1, "", ""));
               this.rebuildButtons();
               break;
            case 742:
               List<AkatsukiClientData.RosterEntry> rosterList = AkatsukiClientData.roster;
               if (this.leaderSelectedMember1 >= 0 && this.leaderSelectedMember1 < rosterList.size() && this.leaderSelectedMember2 >= 0 && this.leaderSelectedMember2 < rosterList.size()) {
                  String n1 = ((AkatsukiClientData.RosterEntry)rosterList.get(this.leaderSelectedMember1)).name;
                  String n2 = ((AkatsukiClientData.RosterEntry)rosterList.get(this.leaderSelectedMember2)).name;
                  AkatsukiModInit.NETWORK.sendToServer(new LeaderActionMessage(2, n1, n2));
                  this.leaderSelectedMember1 = -1;
                  this.leaderSelectedMember2 = -1;
                  this.rebuildButtons();
               }
               break;
            case 746:
            case 747:
            case 748:
            case 749:
            case 750:
            case 751:
               String[] villageNames = new String[]{"leaf", "sand", "mist", "stone", "cloud", "rain"};
               int idx = button.id - 746;
               AkatsukiModInit.NETWORK.sendToServer(new LeaderActionMessage(0, villageNames[idx], ""));
               this.leaderRaidSelecting = false;
               this.rebuildButtons();
               break;
            case 760:
            case 761:
            case 762:
            case 763:
            case 764:
            case 765:
            case 766:
            case 767:
            case 768:
            case 769:
               int memberIdx = button.id - 760;
               if (this.leaderMode == 0) {
                  if (memberIdx == this.leaderSelectedMember1) {
                     this.leaderSelectedMember1 = -1;
                  } else if (memberIdx == this.leaderSelectedMember2) {
                     this.leaderSelectedMember2 = -1;
                  } else if (this.leaderSelectedMember1 < 0) {
                     this.leaderSelectedMember1 = memberIdx;
                  } else if (this.leaderSelectedMember2 < 0) {
                     this.leaderSelectedMember2 = memberIdx;
                  } else {
                     this.leaderSelectedMember1 = memberIdx;
                  }
               } else {
                  if (memberIdx == this.leaderSelectedMember1) {
                     this.leaderSelectedMember1 = -1;
                  } else {
                     this.leaderSelectedMember1 = memberIdx;
                  }

                  this.leaderSelectedMember2 = -1;
               }

               this.rebuildButtons();
               break;
            case 770:
               this.leaderMissionTypeIndex = (this.leaderMissionTypeIndex + 1) % 5;
               this.rebuildButtons();
               break;
            case 771:
               String targetNameM = this.leaderGetSelectedName();
               if (!targetNameM.isEmpty()) {
                  String[] mTypes = new String[]{"assassination", "infiltration", "extraction", "sabotage", "escort"};
                  String mType = this.leaderMissionTypeIndex >= 0 && this.leaderMissionTypeIndex < mTypes.length ? mTypes[this.leaderMissionTypeIndex] : "assassination";
                  AkatsukiModInit.NETWORK.sendToServer(new LeaderActionMessage(3, mType, targetNameM, this.leaderMissionTokens, this.leaderMissionRep));
                  this.leaderSelectedMember1 = -1;
                  this.rebuildButtons();
               }
               break;
            case 772:
               String targetNameR = this.leaderGetSelectedName();
               if (!targetNameR.isEmpty()) {
                  AkatsukiModInit.NETWORK.sendToServer(new LeaderActionMessage(4, targetNameR, "", 0, 0));
                  this.leaderSelectedMember1 = -1;
                  this.rebuildButtons();
               }
               break;
            case 773:
               this.leaderMissionTokens = Math.max(10, this.leaderMissionTokens - 10);
               this.rebuildButtons();
               break;
            case 774:
               this.leaderMissionTokens = Math.min(500, this.leaderMissionTokens + 10);
               this.rebuildButtons();
               break;
            case 775:
               this.leaderMissionRep = Math.max(10, this.leaderMissionRep - 25);
               this.rebuildButtons();
               break;
            case 776:
               this.leaderMissionRep = Math.min(1000, this.leaderMissionRep + 25);
               this.rebuildButtons();
               break;
            case 780:
               String targetNameB = this.leaderGetSelectedName();
               if (!targetNameB.isEmpty()) {
                  AkatsukiModInit.NETWORK.sendToServer(new LeaderActionMessage(5, targetNameB, "", this.leaderBountyAmount, 0));
                  this.leaderSelectedMember1 = -1;
                  this.rebuildButtons();
               }
               break;
            case 781:
               this.leaderBountyAmount = Math.max(100, this.leaderBountyAmount - 100);
               this.rebuildButtons();
               break;
            case 782:
               this.leaderBountyAmount = Math.min(10000, this.leaderBountyAmount + 100);
               this.rebuildButtons();
               break;
            case 785:
               this.leaderMode = 0;
               this.leaderSelectedMember1 = -1;
               this.leaderSelectedMember2 = -1;
               this.rebuildButtons();
               break;
            case 786:
               this.leaderMode = 1;
               this.leaderSelectedMember1 = -1;
               this.leaderSelectedMember2 = -1;
               this.rebuildButtons();
               break;
            case 787:
               this.leaderMode = 2;
               this.leaderSelectedMember1 = -1;
               this.leaderSelectedMember2 = -1;
               this.rebuildButtons();
               break;
            case 790:
            case 791:
            case 792:
            case 793:
            case 794:
               int offerIdx = button.id - 790;
               AkatsukiModInit.NETWORK.sendToServer(AkatsukiMissionActionMessage.acceptOffer(offerIdx));
               this.akatsukiAssignScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 795:
            case 796:
            case 797:
            case 798:
            case 799:
               int mIdx = button.id - 795;
               List<AkatsukiClientData.MissionClientEntry> actList = AkatsukiClientData.activeMissions;
               if (mIdx < actList.size()) {
                  AkatsukiModInit.NETWORK.sendToServer(AkatsukiMissionActionMessage.abandonMission(((AkatsukiClientData.MissionClientEntry)actList.get(mIdx)).templateId));
               }

               this.akatsukiAssignScrollOffset = 0;
               this.rebuildButtons();
               break;
            case 800:
               List<String> players = this.getOnlinePlayerNames();
               if (!players.isEmpty()) {
                  this.contractTargetPlayerIndex = (this.contractTargetPlayerIndex - 1 + players.size()) % players.size();
               }

               this.rebuildButtons();
               break;
            case 801:
               List<String> players = this.getOnlinePlayerNames();
               if (!players.isEmpty()) {
                  this.contractTargetPlayerIndex = (this.contractTargetPlayerIndex + 1) % players.size();
               }

               this.rebuildButtons();
               break;
            case 802:
               this.contractTargetZoneIndex = Math.max(0, this.contractTargetZoneIndex - 1);
               this.rebuildButtons();
               break;
            case 803:
               ++this.contractTargetZoneIndex;
               this.rebuildButtons();
               break;
            case 829:
               this.selectedBlackMarketItemId = null;
               this.blackMarketZoneScrollOffset = 0;
               this.rebuildButtons();
               break;
            default:
               if (button.id >= 810 && button.id < 820) {
                  int itemIdx = button.id - 810 + this.blackMarketItemScrollOffset;
                  List<BlackMarketItem> bmItems = BlackMarketRegistry.getAll();
                  if (itemIdx >= 0 && itemIdx < bmItems.size()) {
                     BlackMarketItem bmItem = (BlackMarketItem)bmItems.get(itemIdx);
                     if (bmItem.isBuff()) {
                        AkatsukiModInit.NETWORK.sendToServer(new BlackMarketActionMessage(bmItem.getId(), ""));
                     } else {
                        this.selectedBlackMarketItemId = bmItem.getId();
                        this.blackMarketZoneScrollOffset = 0;
                        this.rebuildButtons();
                     }
                  }
               } else if (button.id >= 830 && button.id < 866 && this.selectedBlackMarketItemId != null) {
                  int zoneIdx = button.id - 830 + this.blackMarketZoneScrollOffset;
                  BlackMarketItem selItem = BlackMarketRegistry.get(this.selectedBlackMarketItemId);
                  List<TerritoryClientData.ClientZoneData> validZones = new ArrayList();
                  if (selItem != null) {
                     for(TerritoryClientData.ClientZoneData czd : TerritoryClientData.getInstance().getZoneStates().values()) {
                        if (!selItem.isOffensive() && !selItem.isMassDeploy()) {
                           if (selItem.isDefensive()) {
                              if ("akatsuki".equalsIgnoreCase(czd.ownerVillage)) {
                                 validZones.add(czd);
                              }
                           } else if (!"akatsuki".equalsIgnoreCase(czd.ownerVillage)) {
                              validZones.add(czd);
                           }
                        } else if (!"akatsuki".equalsIgnoreCase(czd.ownerVillage)) {
                           validZones.add(czd);
                        }
                     }
                  }

                  validZones.sort((a, b) -> a.displayName.compareToIgnoreCase(b.displayName));
                  if (zoneIdx >= 0 && zoneIdx < validZones.size()) {
                     String targetZoneId = ((TerritoryClientData.ClientZoneData)validZones.get(zoneIdx)).zoneId;
                     AkatsukiModInit.NETWORK.sendToServer(new BlackMarketActionMessage(this.selectedBlackMarketItemId, targetZoneId));
                     this.selectedBlackMarketItemId = null;
                     this.blackMarketZoneScrollOffset = 0;
                     this.rebuildButtons();
                  }
               }

               if (button.id >= 300 && button.id < 320) {
                  int clickedIndex = button.id - 300;
                  if (clickedIndex == this.selectedStorylineIndex) {
                     this.selectedStorylineIndex = -1;
                  } else {
                     this.selectedStorylineIndex = clickedIndex;
                  }

                  this.selectedQuestIndex = -1;
                  this.rebuildButtons();
               } else if (button.id >= 20 && button.id < 23) {
                  this.selectedSubSlotIndex = button.id - 20;
                  this.rebuildButtons();
               } else if (button.id >= 35 && button.id < 38) {
                  this.pvpSubSlotIndex = button.id - 35;
                  this.rebuildButtons();
               } else if (button.id >= 170 && button.id < 172) {
                  int orderIdx = button.id - 170;
                  List<PvpClientData.OrderClientInfo> orders = PvpClientData.getActiveOrders();
                  int assignedIdx = 0;

                  for(PvpClientData.OrderClientInfo order : orders) {
                     if (order.isAssignedMission()) {
                        if (assignedIdx == orderIdx) {
                           PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.acceptAssignedMission(order.orderId));
                           break;
                        }

                        ++assignedIdx;
                     }
                  }
               } else if (button.id >= 180 && button.id < 187) {
                  int templateIdx = button.id - 180;
                  List<PvpMissionTemplate> templates = PvpMissionTemplate.getAssignable();
                  if (templateIdx >= 0 && templateIdx < templates.size()) {
                     PvpMissionTemplate t = (PvpMissionTemplate)templates.get(templateIdx);
                     PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.assignMission(t.getId()));
                     this.leadershipSubView = 0;
                     this.leadershipStep = 0;
                     this.rebuildButtons();
                  }
               } else if (this.selectedCategory == 5 && button.id >= 1200 && button.id < 1250) {
                  int idx = button.id - 1200;
                  StatElement[] elements = this.getStatElementsForCurrentTab();
                  StatCategory cat = this.statSubTab == 0 ? this.getOffenseCategoryForElement(elements, idx) : this.getDefenseCategoryForElement(elements, idx);
                  if (cat != null && idx < elements.length) {
                     StatModInit.NETWORK.sendToServer(StatAllocateMessage.allocate(cat, elements[idx]));
                  }
               } else if (button.id >= 400 && button.id < 1200) {
                  this.selectedQuestIndex = button.id - 400;
                  this.rebuildButtons();
               } else if (button.id >= 60 && button.id < 70) {
                  int villageIdx = button.id - 60;
                  VillageHelper.Village[] villages = VillageHelper.Village.values();
                  if (villageIdx >= 0 && villageIdx < villages.length) {
                     this.warSelectedTarget = villages[villageIdx].teamName;
                  }

                  this.rebuildButtons();
               } else if (button.id >= 81 && button.id < 83) {
                  int orderIdx = button.id - 81;
                  List<PvpClientData.OrderClientInfo> orders = PvpClientData.getActiveOrders();
                  if (orderIdx >= 0 && orderIdx < orders.size()) {
                     PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.cancelOrder(((PvpClientData.OrderClientInfo)orders.get(orderIdx)).orderId));
                  }
               } else if (button.id >= 85 && button.id < 95) {
                  int villageIdx = button.id - 85;
                  VillageHelper.Village[] villages = VillageHelper.Village.values();
                  if (villageIdx >= 0 && villageIdx < villages.length) {
                     PvpModInit.NETWORK.sendToServer(LeadershipActionMessage.issueOrderVillage((byte)villageIdx));
                     this.leadershipSubView = 0;
                     this.leadershipStep = 0;
                     this.rebuildButtons();
                  }
               } else if (this.selectedCategory == 5 && button.id >= 1200 && button.id < 1250) {
                  int idx = button.id - 1200;
                  StatElement[] elements = this.getStatElementsForCurrentTab();
                  StatCategory cat = this.statSubTab == 0 ? this.getOffenseCategoryForElement(elements, idx) : this.getDefenseCategoryForElement(elements, idx);
                  if (cat != null && idx < elements.length) {
                     StatModInit.NETWORK.sendToServer(StatAllocateMessage.allocate(cat, elements[idx]));
                  }
               }
         }

      }
   }

   public void updateScreen() {
      super.updateScreen();
      this.animationTick += 0.1F;
      this.rebuildButtons();
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (mouseButton == 0 && this.selectedCategory == 3 && this.warDeclareStep == 0) {
         int panelMidX = this.guiLeft + 180;
         int btnX = panelMidX - 70;
         int btnY = this.guiTop + 280 - 70;
         if (mouseX >= btnX && mouseX < btnX + 140 && mouseY >= btnY && mouseY < btnY + 22 && (PvpClientData.isKage || PvpClientData.hasWarAuthority) && PvpClientData.warCooldownRemainingMs <= 0L) {
            this.warDeclareStep = 1;
            this.warSelectedMode = -1;
            this.warSelectedTarget = null;
            this.rebuildButtons();
            return;
         }
      }

      if (mouseButton == 0 && this.selectedCategory == 2 && this.pvpSubTab == 5 && this.isBracketVisible()) {
         int bracketAreaX = this.guiLeft + 8;
         int bracketAreaY = this.guiTop + 68;
         int bracketAreaW = 344;
         int bracketAreaH = 195;
         if (mouseX >= bracketAreaX && mouseX < bracketAreaX + bracketAreaW && mouseY >= bracketAreaY && mouseY < bracketAreaY + bracketAreaH) {
            this.bracketDragging = true;
            this.bracketDragStartX = mouseX;
            this.bracketDragStartY = mouseY;
            this.bracketDragStartPanX = this.bracketPanX;
            this.bracketDragStartPanY = this.bracketPanY;
            return;
         }
      }

      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      this.bracketDragging = false;
      this.mapDragging = false;
      super.mouseReleased(mouseX, mouseY, state);
   }

   protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
      if (this.bracketDragging && clickedMouseButton == 0) {
         this.bracketPanX = this.bracketDragStartPanX + (float)(mouseX - this.bracketDragStartX) / this.bracketZoom;
         this.bracketPanY = this.bracketDragStartPanY + (float)(mouseY - this.bracketDragStartY) / this.bracketZoom;
      } else if (this.mapDragging && clickedMouseButton == 0) {
         int mapAreaW = 185;
         int mapAreaH = 176;
         float worldPerPixelX = 12000.0F / this.mapZoom / (float)mapAreaW;
         float worldPerPixelZ = 12000.0F / this.mapZoom / (float)mapAreaH;
         this.mapOffsetX = this.mapDragStartOffsetX - (float)(mouseX - this.mapDragStartX) * worldPerPixelX;
         this.mapOffsetZ = this.mapDragStartOffsetZ - (float)(mouseY - this.mapDragStartY) * worldPerPixelZ;
      } else {
         super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
      }
   }

   private boolean isBracketVisible() {
      PvpClientData.TournamentClientInfo info = this.getTournamentInfo();
      return info != null && info.hasBracket && info.matches != null && !info.matches.isEmpty();
   }

   @Nullable
   private PvpClientData.TournamentClientInfo getTournamentInfo() {
      return PvpClientData.tournamentInfo;
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int scroll = Mouse.getEventDWheel();
      if (scroll != 0) {
         if (this.selectedCategory == 0) {
            int maxVisible = 9;
            if (scroll > 0 && this.scrollOffset > 0) {
               --this.scrollOffset;
            } else if (scroll < 0 && this.scrollOffset < Math.max(0, this.storyTotalRows - maxVisible)) {
               ++this.scrollOffset;
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 2 && this.pvpSubTab == 3) {
            List<PvpClientData.BingoInfo> entries = PvpClientData.getBingoEntries();
            int maxVisible = 9;
            if (scroll > 0 && this.bingoScrollOffset > 0) {
               --this.bingoScrollOffset;
            } else if (scroll < 0 && this.bingoScrollOffset < Math.max(0, entries.size() - maxVisible)) {
               ++this.bingoScrollOffset;
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 1 && this.pveSubTab == 4) {
            List<ContractNetworkMessage.ContractClientEntry> list = ContractClientData.entries;
            int maxScroll = Math.max(0, list.size() - 4);
            if (scroll > 0 && this.boardContractScrollOffset > 0) {
               --this.boardContractScrollOffset;
            } else if (scroll < 0 && this.boardContractScrollOffset < maxScroll) {
               ++this.boardContractScrollOffset;
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 2 && this.pvpSubTab == 4 && this.leadershipSubView == 0) {
            int scrollStep = 12;
            if (scroll > 0 && this.ordersOverviewScrollOffset > 0) {
               this.ordersOverviewScrollOffset = Math.max(0, this.ordersOverviewScrollOffset - scrollStep);
            } else if (scroll < 0) {
               this.ordersOverviewScrollOffset = Math.min(this.ordersOverviewScrollOffset + scrollStep, 200);
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 2 && this.pvpSubTab == 4 && this.leadershipSubView == 1) {
            int scrollStep = 12;
            if (scroll > 0 && this.missionScrollOffset > 0) {
               this.missionScrollOffset = Math.max(0, this.missionScrollOffset - scrollStep);
            } else if (scroll < 0) {
               this.missionScrollOffset = Math.min(this.missionScrollOffset + scrollStep, 200);
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 2 && this.pvpSubTab == 4 && this.leadershipSubView == 2 && this.leadershipStep == 2) {
            List<PvpMissionTemplate> templates = PvpMissionTemplate.getAssignable();
            int maxVisible = 4;
            if (scroll > 0 && this.assignTemplateScrollOffset > 0) {
               --this.assignTemplateScrollOffset;
            } else if (scroll < 0 && this.assignTemplateScrollOffset < Math.max(0, templates.size() - maxVisible)) {
               ++this.assignTemplateScrollOffset;
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 5 && this.statSubTab == 3) {
            int scrollStep = 12;
            if (scroll > 0 && this.statScrollOffset > 0) {
               this.statScrollOffset = Math.max(0, this.statScrollOffset - scrollStep);
            } else if (scroll < 0) {
               this.statScrollOffset = Math.min(this.statScrollOffset + scrollStep, 150);
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 5 && this.statSubTab < 2) {
            int scrollStep = 18;
            StatElement[] elements = this.getStatElementsForCurrentTab();
            int rowH = 18;
            int totalContentH = 14 + elements.length * rowH + 18;
            int clipTop = this.guiTop + 84;
            int clipBot = this.guiTop + 280 - 42;
            int visibleH = clipBot - clipTop;
            int maxScroll = Math.max(0, totalContentH - visibleH);
            if (scroll > 0 && this.statScrollOffset > 0) {
               this.statScrollOffset = Math.max(0, this.statScrollOffset - scrollStep);
            } else if (scroll < 0) {
               this.statScrollOffset = Math.min(this.statScrollOffset + scrollStep, maxScroll);
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 2 && this.pvpSubTab == 5 && this.isBracketVisible()) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            int bracketAreaX = this.guiLeft + 8;
            int bracketAreaY = this.guiTop + 68;
            int bracketAreaW = 344;
            int bracketAreaH = 195;
            if (mouseX >= bracketAreaX && mouseX < bracketAreaX + bracketAreaW && mouseY >= bracketAreaY && mouseY < bracketAreaY + bracketAreaH) {
               float oldZoom = this.bracketZoom;
               if (scroll > 0) {
                  this.bracketZoom = Math.min(2.0F, this.bracketZoom * 1.15F);
               } else {
                  this.bracketZoom = Math.max(0.4F, this.bracketZoom / 1.15F);
               }
            }
         } else if (this.selectedCategory == 7 && this.factionSubTab == 0 && AkatsukiClientData.isAkatsuki) {
            int scrollStep = 12;
            if (scroll > 0 && this.akatsukiAssignScrollOffset > 0) {
               this.akatsukiAssignScrollOffset = Math.max(0, this.akatsukiAssignScrollOffset - scrollStep);
            } else if (scroll < 0) {
               this.akatsukiAssignScrollOffset = Math.min(this.akatsukiAssignScrollOffset + scrollStep, this.akatsukiAssignMaxScroll);
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 7 && this.factionSubTab == 1 && AkatsukiClientData.isAkatsuki) {
            int scrollStep = 18;
            if (scroll > 0 && this.contractScrollOffset > 0) {
               this.contractScrollOffset = Math.max(0, this.contractScrollOffset - scrollStep);
            } else if (scroll < 0) {
               this.contractScrollOffset = Math.min(this.contractScrollOffset + scrollStep, 500);
            }

            this.rebuildButtons();
         } else if (this.selectedCategory == 7 && this.factionSubTab == 5 && AkatsukiClientData.isAkatsuki) {
            if (this.selectedBlackMarketItemId != null) {
               int scrollStep = 1;
               if (scroll > 0 && this.blackMarketZoneScrollOffset > 0) {
                  this.blackMarketZoneScrollOffset = Math.max(0, this.blackMarketZoneScrollOffset - scrollStep);
               } else if (scroll < 0) {
                  this.blackMarketZoneScrollOffset += scrollStep;
               }
            } else {
               int scrollStep = 1;
               int totalItems = BlackMarketRegistry.getAll().size();
               if (scroll > 0 && this.blackMarketItemScrollOffset > 0) {
                  this.blackMarketItemScrollOffset = Math.max(0, this.blackMarketItemScrollOffset - scrollStep);
               } else if (scroll < 0 && this.blackMarketItemScrollOffset < totalItems - 1) {
                  this.blackMarketItemScrollOffset = Math.min(totalItems - 1, this.blackMarketItemScrollOffset + scrollStep);
               }
            }

            this.rebuildButtons();
         }
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public void onGuiClosed() {
      super.onGuiClosed();
   }

   private void drawScrollBar(int trackX, int trackTop, int trackH, int totalItems, int visibleItems, int scrollPos, int maxScroll) {
      if (totalItems > visibleItems && maxScroll > 0) {
         drawRect(trackX, trackTop, trackX + 4, trackTop + trackH, 1073741824);
         int thumbH = Math.max(12, trackH * visibleItems / totalItems);
         int thumbY = trackTop + (trackH - thumbH) * scrollPos / maxScroll;
         drawRect(trackX, thumbY, trackX + 4, thumbY + thumbH, -1433765286);
         drawRect(trackX, thumbY, trackX + 4, thumbY + 1, -1432317856);
         drawRect(trackX, thumbY + thumbH - 1, trackX + 4, thumbY + thumbH, -1436923344);
      }
   }

   private void rebuildStatButtons() {
      if (StatClientData.isStatsUnlocked()) {
         int contentY = this.guiTop + 50;
         int contentX = this.guiLeft + 8;
         int contentW = 344;
         int subTabW = 56;
         int subSpacing = 3;
         int subStartX = contentX + (contentW - (subTabW * 4 + subSpacing * 3)) / 2;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(43, subStartX, contentY, subTabW, 14, this.statSubTab == 0 ? "§fOffense" : "§7Offense", this.statSubTab == 0 ? -7718342 : -12049372, this.statSubTab == 0 ? -9823718 : -13100518));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(44, subStartX + subTabW + subSpacing, contentY, subTabW, 14, this.statSubTab == 1 ? "§fDefense" : "§7Defense", this.statSubTab == 1 ? -12948854 : -14403512, this.statSubTab == 1 ? -15054230 : -15062472));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(45, subStartX + (subTabW + subSpacing) * 2, contentY, subTabW, 14, this.statSubTab == 2 ? "§fOverview" : "§7Overview", this.statSubTab == 2 ? -10855878 : -13092828, this.statSubTab == 2 ? -12961254 : -14013926));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(47, subStartX + (subTabW + subSpacing) * 3, contentY, subTabW, 14, this.statSubTab == 3 ? "§fChakra" : "§7Chakra", this.statSubTab == 3 ? -13997430 : -15058872, this.statSubTab == 3 ? -15050118 : -16111048));
         if (this.statSubTab < 2) {
            StatElement[] elements = this.getStatElementsForCurrentTab();
            int rowY = this.guiTop + 88 + 14;
            int rowH = 18;
            int clipTop = this.guiTop + 84;
            int clipBot = this.guiTop + 280 - 42;

            for(int i = 0; i < elements.length && i < 20; ++i) {
               StatCategory cat = this.statSubTab == 0 ? elements[i].getOffenseCategory() : elements[i].getDefenseCategory();
               if (cat != null) {
                  int level = StatClientData.getLevel(cat, elements[i]);
                  int maxLevel = StatConstants.getMaxLevel(cat);
                  int extraOffset = i > 4 ? 18 : 0;
                  int drawY = rowY + i * rowH + extraOffset - this.statScrollOffset;
                  if (drawY >= clipTop && drawY + rowH <= clipBot && level < maxLevel && StatClientData.canAllocate(cat, elements[i])) {
                     this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1200 + i, this.guiLeft + 360 - 36, drawY, 22, rowH - 2, "§a+", -12948934, -15054310));
                  }
               }
            }
         }

         if (this.statSubTab == 2) {
            int bottomY = this.guiTop + 280 - 50;
            boolean canRespec = StatClientData.isRespecAvailable();
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(46, this.guiLeft + 180 - 45, bottomY, 90, 16, canRespec ? "§fRespec Stats" : "§8Respec Stats", canRespec ? -7710176 : -13094880, canRespec ? -9815536 : -14016488));
         }

      }
   }

   private void drawStatContent(int mouseX, int mouseY) {
      int contentX = this.guiLeft + 8;
      int contentW = 344;
      if (!StatClientData.isStatsUnlocked()) {
         String msg1 = "Stats unlock at Jonin rank";
         String msg2 = "(Complete Konoha Crush)";
         int cx = this.guiLeft + 180;
         int cy = this.guiTop + 140 - 20;
         this.drawCenteredString(this.fontRenderer, "§6§l" + msg1, cx, cy, -1521552);
         this.drawCenteredString(this.fontRenderer, "§7" + msg2, cx, cy + 14, -7700886);
      } else {
         int spBarY = this.guiTop + 68;
         int available = StatClientData.getAvailableSP();
         int earned = StatClientData.getSpEarned();
         int spent = StatClientData.getSpSpent();
         String spText = "§bSP: §f" + available + "§7/" + earned + " §8(" + spent + " spent)";
         this.fontRenderer.drawStringWithShadow(spText, (float)(contentX + 4), (float)spBarY, -2832216);
         int barX = contentX + 4;
         int barW = contentW - 8;
         int barY = spBarY + 11;
         drawRect(barX, barY, barX + barW, barY + 4, -15066604);
         if (earned > 0) {
            int filledW = (int)((float)spent / 120.0F * (float)barW);
            int availW = (int)((float)earned / 120.0F * (float)barW);
            drawRect(barX, barY, barX + availW, barY + 4, -14005654);
            drawRect(barX, barY, barX + filledW, barY + 4, -10843478);
         }

         int clipTop = this.guiTop + 84;
         int clipBot = this.guiTop + 280 - 42;
         ScaledResolution sr = new ScaledResolution(this.mc);
         int sf = sr.getScaleFactor();
         GL11.glEnable(3089);
         GL11.glScissor(this.guiLeft * sf, (sr.getScaledHeight() - clipBot) * sf, 360 * sf, (clipBot - clipTop) * sf);
         switch (this.statSubTab) {
            case 0:
               this.drawStatOffenseTab(contentX, contentW);
               break;
            case 1:
               this.drawStatDefenseTab(contentX, contentW);
               break;
            case 2:
               this.drawStatOverviewTab(contentX, contentW);
               break;
            case 3:
               this.drawStatChakraTab(contentX, contentW);
         }

         GL11.glDisable(3089);
         if (this.statSubTab < 2) {
            StatElement[] elements = this.getStatElementsForCurrentTab();
            int rowH = 18;
            int totalContentH = 14 + elements.length * rowH + 18;
            int visibleH = clipBot - clipTop;
            int maxScroll = Math.max(0, totalContentH - visibleH);
            this.statScrollOffset = Math.min(this.statScrollOffset, maxScroll);
            if (maxScroll > 0) {
               this.drawScrollBar(this.guiLeft + 360 - 12, clipTop, visibleH, totalContentH, visibleH, this.statScrollOffset, maxScroll);
            }
         }

      }
   }

   private void drawStatOffenseTab(int contentX, int contentW) {
      StatElement[] elements = this.getStatElementsForCurrentTab();
      int rowY = this.guiTop + 88;
      int rowH = 18;
      this.fontRenderer.drawStringWithShadow("§e§lNature Mastery", (float)(contentX + 4), (float)(rowY - this.statScrollOffset), -1521552);
      rowY += 14;

      for(int i = 0; i < elements.length; ++i) {
         StatElement el = elements[i];
         StatCategory cat = el.getOffenseCategory();
         if (cat != null) {
            int extraOffset = i > 4 ? 18 : 0;
            int drawY = rowY + i * rowH + extraOffset - this.statScrollOffset;
            int level = StatClientData.getLevel(cat, el);
            int maxLevel = StatConstants.getMaxLevel(cat);
            double bonus = StatClientData.getOffenseBonus(el);
            String color = getElementColor(el);
            this.fontRenderer.drawStringWithShadow(color + el.getDisplayName(), (float)(contentX + 6), (float)(drawY + 2), -2832216);
            int barX = contentX + 70;
            int barW = contentW - 170;
            drawRect(barX, drawY + 4, barX + barW, drawY + 10, -15066604);
            if (level > 0) {
               int filledW = (int)((float)level / (float)maxLevel * (float)barW);
               drawRect(barX, drawY + 4, barX + filledW, drawY + 10, getElementBarColor(el));
            }

            String lvlText = level + "/" + maxLevel;
            if (bonus > (double)0.0F) {
               lvlText = lvlText + " §a+" + String.format("%.0f%%", bonus * (double)100.0F);
            }

            this.fontRenderer.drawStringWithShadow("§7" + lvlText, (float)(barX + barW + 4), (float)(drawY + 2), -7700886);
            if (i == 4) {
               int headerY = rowY + (i + 1) * rowH - this.statScrollOffset;
               this.fontRenderer.drawStringWithShadow("§e§lKG Mastery", (float)(contentX + 4), (float)headerY, -1521552);
            }
         }
      }

   }

   private void drawStatDefenseTab(int contentX, int contentW) {
      StatElement[] elements = this.getStatElementsForCurrentTab();
      int rowY = this.guiTop + 88;
      int rowH = 18;
      this.fontRenderer.drawStringWithShadow("§e§lNature Defense", (float)(contentX + 4), (float)(rowY - this.statScrollOffset), -1521552);
      rowY += 14;

      for(int i = 0; i < elements.length; ++i) {
         StatElement el = elements[i];
         StatCategory cat = el.getDefenseCategory();
         if (cat != null) {
            int extraOffset = i > 4 ? 18 : 0;
            int drawY = rowY + i * rowH + extraOffset - this.statScrollOffset;
            int level = StatClientData.getLevel(cat, el);
            int maxLevel = StatConstants.getMaxLevel(cat);
            double reduction = StatClientData.getDefenseReduction(el);
            String color = getElementColor(el);
            this.fontRenderer.drawStringWithShadow(color + el.getDisplayName(), (float)(contentX + 6), (float)(drawY + 2), -2832216);
            int barX = contentX + 70;
            int barW = contentW - 170;
            drawRect(barX, drawY + 4, barX + barW, drawY + 10, -15066604);
            if (level > 0) {
               int filledW = (int)((float)level / (float)maxLevel * (float)barW);
               drawRect(barX, drawY + 4, barX + filledW, drawY + 10, getElementBarColor(el));
            }

            String lvlText = level + "/" + maxLevel;
            if (reduction > (double)0.0F) {
               lvlText = lvlText + " §9-" + String.format("%.0f%%", reduction * (double)100.0F);
            }

            this.fontRenderer.drawStringWithShadow("§7" + lvlText, (float)(barX + barW + 4), (float)(drawY + 2), -7700886);
            if (i == 4) {
               int headerY = rowY + (i + 1) * rowH - this.statScrollOffset;
               this.fontRenderer.drawStringWithShadow("§e§lKG Defense", (float)(contentX + 4), (float)headerY, -1521552);
            }
         }
      }

   }

   private void drawStatOverviewTab(int contentX, int contentW) {
      int y = this.guiTop + 88;
      int lineH = 12;
      this.fontRenderer.drawStringWithShadow("§e§lOffense Bonuses:", (float)(contentX + 4), (float)y, -1521552);
      y += lineH + 2;

      for(StatElement el : StatElement.getElementsForCategory(StatCategory.NATURE_OFFENSE)) {
         double bonus = StatClientData.getOffenseBonus(el);
         if (bonus > (double)0.0F) {
            this.fontRenderer.drawStringWithShadow(getElementColor(el) + el.getDisplayName() + " §a+" + String.format("%.1f%%", bonus * (double)100.0F), (float)(contentX + 10), (float)y, -2832216);
            y += lineH;
         }
      }

      for(StatElement el : StatElement.getElementsForCategory(StatCategory.KG_OFFENSE)) {
         double bonus = StatClientData.getOffenseBonus(el);
         if (bonus > (double)0.0F) {
            this.fontRenderer.drawStringWithShadow(getElementColor(el) + el.getDisplayName() + " §a+" + String.format("%.1f%%", bonus * (double)100.0F), (float)(contentX + 10), (float)y, -2832216);
            y += lineH;
         }
      }

      y += 6;
      this.fontRenderer.drawStringWithShadow("§e§lDefense Reductions:", (float)(contentX + 4), (float)y, -1521552);
      y += lineH + 2;

      for(StatElement el : StatElement.getElementsForCategory(StatCategory.NATURE_DEFENSE)) {
         double red = StatClientData.getDefenseReduction(el);
         if (red > (double)0.0F) {
            this.fontRenderer.drawStringWithShadow(getElementColor(el) + el.getDisplayName() + " §9-" + String.format("%.1f%%", red * (double)100.0F), (float)(contentX + 10), (float)y, -2832216);
            y += lineH;
         }
      }

      for(StatElement el : StatElement.getElementsForCategory(StatCategory.KG_DEFENSE)) {
         double red = StatClientData.getDefenseReduction(el);
         if (red > (double)0.0F) {
            this.fontRenderer.drawStringWithShadow(getElementColor(el) + el.getDisplayName() + " §9-" + String.format("%.1f%%", red * (double)100.0F), (float)(contentX + 10), (float)y, -2832216);
            y += lineH;
         }
      }

      y += 10;
      this.fontRenderer.drawStringWithShadow("§7Respec Tokens: §f" + StatClientData.getRespecTokens(), (float)(contentX + 4), (float)y, -7700886);
      y += lineH;
      this.fontRenderer.drawStringWithShadow("§7Cost: §f5000 Ryo + 1 Token", (float)(contentX + 4), (float)y, -7700886);
      y += lineH;
      long cd = StatClientData.getRespecCooldownRemaining();
      if (cd > 0L) {
         long days = cd / 86400000L;
         long hours = cd % 86400000L / 3600000L;
         this.fontRenderer.drawStringWithShadow("§cCooldown: " + days + "d " + hours + "h", (float)(contentX + 4), (float)y, -43691);
      } else {
         this.fontRenderer.drawStringWithShadow("§aRespec available", (float)(contentX + 4), (float)y, -11141291);
      }

   }

   private void drawStatChakraTab(int contentX, int contentW) {
      int lineH = 11;
      int y = this.guiTop + 88 - this.statScrollOffset;
      this.fontRenderer.drawStringWithShadow("§b§l✦ Chakra Enhancement", (float)(contentX + 4), (float)y, -11141121);
      y += lineH + 3;
      this.drawHorizontalLine(contentX + 4, contentX + contentW - 4, y, -13399894);
      y += 5;
      if (chakraEnhancementCap <= 0) {
         this.fontRenderer.drawStringWithShadow("§c§lLOCKED", (float)(contentX + 4), (float)y, -43691);
         y += lineH + 4;
         this.fontRenderer.drawStringWithShadow("§7Complete the §eKazekage Rescue§7 arc", (float)(contentX + 4), (float)y, -5592406);
         y += lineH;
         this.fontRenderer.drawStringWithShadow("§7to unlock Chakra Enhancement!", (float)(contentX + 4), (float)y, -5592406);
         y += lineH + 6;
         this.drawHorizontalLine(contentX + 4, contentX + contentW - 4, y, -14007211);
         y += 5;
         this.fontRenderer.drawStringWithShadow("§eWhat is Chakra Enhancement?", (float)(contentX + 4), (float)y, -22016);
         y += lineH + 2;
         this.fontRenderer.drawStringWithShadow("§7Each §bChakra Scroll §7consumed grants", (float)(contentX + 4), (float)y, -5592406);
         y += lineH;
         this.fontRenderer.drawStringWithShadow("§ba permanent +5% Jutsu Damage §7buff.", (float)(contentX + 4), (float)y, -5592406);
         y += lineH + 4;
         this.fontRenderer.drawStringWithShadow("§8• Scrolls drop from the §cWorld Boss", (float)(contentX + 4), (float)y, -7829368);
         y += lineH;
         this.fontRenderer.drawStringWithShadow("§8• World Boss spawns every 30 minutes", (float)(contentX + 4), (float)y, -7829368);
         y += lineH;
         this.fontRenderer.drawStringWithShadow("§8• Cap increases with each story arc", (float)(contentX + 4), (float)y, -7829368);
      } else {
         int dmgBonus = chakraEnhancementCharges * 5;
         this.fontRenderer.drawStringWithShadow("§bCurrent Level:", (float)(contentX + 4), (float)y, -11149825);
         this.fontRenderer.drawStringWithShadow("§f" + chakraEnhancementCharges + " §7/ §f" + chakraEnhancementCap, (float)(contentX + 90), (float)y, -1);
         y += lineH + 3;
         this.fontRenderer.drawStringWithShadow("§aJutsu Damage Bonus:", (float)(contentX + 4), (float)y, -11141291);
         this.fontRenderer.drawStringWithShadow("§a+" + dmgBonus + "%", (float)(contentX + 120), (float)y, -11141291);
         y += lineH + 3;
         this.fontRenderer.drawStringWithShadow("§7Each scroll = §b+5% §7jutsu damage", (float)(contentX + 4), (float)y, -5592406);
         y += lineH + 6;
         this.drawHorizontalLine(contentX + 4, contentX + contentW - 4, y, -13399894);
         y += 5;
         this.fontRenderer.drawStringWithShadow("§eHow to Upgrade:", (float)(contentX + 4), (float)y, -22016);
         y += lineH + 2;
         this.fontRenderer.drawStringWithShadow("§7• Defeat the §cWorld Boss §7(every 30min)", (float)(contentX + 4), (float)y, -5592406);
         y += lineH;
         this.fontRenderer.drawStringWithShadow("§7• Right-click scroll to consume", (float)(contentX + 4), (float)y, -5592406);
         y += lineH;
         this.fontRenderer.drawStringWithShadow("§7• Cap increases with each story arc", (float)(contentX + 4), (float)y, -5592406);
         if (chakraEnhancementCharges >= chakraEnhancementCap) {
            y += lineH + 5;
            this.fontRenderer.drawStringWithShadow("§a✓ MAX for current rank! Complete more arcs to raise cap.", (float)(contentX + 4), (float)y, -11141291);
         }
      }

   }

   private StatElement[] getStatElementsForCurrentTab() {
      StatCategory natCat = this.statSubTab == 0 ? StatCategory.NATURE_OFFENSE : StatCategory.NATURE_DEFENSE;
      StatCategory kgCat = this.statSubTab == 0 ? StatCategory.KG_OFFENSE : StatCategory.KG_DEFENSE;
      StatElement[] natures = StatElement.getElementsForCategory(natCat);
      StatElement[] kgs = StatElement.getElementsForCategory(kgCat);
      StatElement[] combined = new StatElement[natures.length + kgs.length];
      System.arraycopy(natures, 0, combined, 0, natures.length);
      System.arraycopy(kgs, 0, combined, natures.length, kgs.length);
      return combined;
   }

   private StatCategory getOffenseCategoryForElement(StatElement[] elements, int idx) {
      return idx >= 0 && idx < elements.length ? elements[idx].getOffenseCategory() : null;
   }

   private StatCategory getDefenseCategoryForElement(StatElement[] elements, int idx) {
      return idx >= 0 && idx < elements.length ? elements[idx].getDefenseCategory() : null;
   }

   private static String getElementColor(StatElement el) {
      switch (el) {
         case KATON:
            return "§c";
         case FUTON:
            return "§a";
         case SUITON:
            return "§9";
         case RAITON:
            return "§e";
         case DOTON:
            return "§6";
         case MOKUTON:
            return "§2";
         case HYOTON:
            return "§b";
         case RANTON:
            return "§3";
         case YOTON:
            return "§4";
         case BAKUTON:
            return "§c";
         case SHARINGAN:
            return "§c";
         case BYAKUGAN:
            return "§f";
         default:
            return "§7";
      }
   }

   private static int getElementBarColor(StatElement el) {
      switch (el) {
         case KATON:
            return -3390396;
         case FUTON:
            return -12268476;
         case SUITON:
            return -12294452;
         case RAITON:
            return -3355580;
         case DOTON:
            return -3373005;
         case MOKUTON:
            return -13400013;
         case HYOTON:
            return -12277044;
         case RANTON:
         case YOTON:
         case BAKUTON:
         default:
            return -9803158;
         case SHARINGAN:
            return -3399134;
      }
   }

   private static int cycleRank(int current) {
      return current >= 5 ? -1 : current + 1;
   }

   private static String getRankColorCode(byte rank) {
      switch (rank) {
         case 0:
            return "§a";
         case 1:
            return "§9";
         case 2:
            return "§5";
         case 3:
            return "§6";
         case 4:
            return "§c";
         case 5:
            return "§d";
         default:
            return "§7";
      }
   }

   private static String formatCooldown(long ms) {
      long totalSeconds = ms / 1000L;
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      if (hours > 0L) {
         return hours + "h " + minutes + "m";
      } else {
         return minutes > 0L ? minutes + "m" : "< 1m";
      }
   }

   private static String formatTimeRemaining(long ms) {
      if (ms <= 0L) {
         return "Expired";
      } else {
         long seconds = ms / 1000L;
         long minutes = seconds / 60L;
         long hours = minutes / 60L;
         if (hours > 0L) {
            return hours + "h " + minutes % 60L + "m";
         } else {
            return minutes > 0L ? minutes + "m " + seconds % 60L + "s" : seconds + "s";
         }
      }
   }

   private static int[] getVillageColors(VillageHelper.Village v) {
      switch (v) {
         case LEAF:
            return new int[]{-12948934, -15054310};
         case SAND:
            return new int[]{-9803206, -11908582};
         case RAIN:
            return new int[]{-15064491, -16117184};
         case STONE:
            return new int[]{-10859974, -12965350};
         case CLOUD:
            return new int[]{-11912598, -14017974};
         case MIST:
            return new int[]{-14005675, -15060928};
         default:
            return new int[]{-12963808, -14016488};
      }
   }

   private static int getWarModeColor(int modeOrdinal) {
      switch (modeOrdinal) {
         case 0:
            return -30720;
         case 1:
            return -52429;
         case 2:
            return -13399809;
         case 3:
            return -13386957;
         case 4:
            return -14514142;
         default:
            return -7700886;
      }
   }

   private static long getRankXpThreshold(int rankOrdinal) {
      switch (rankOrdinal) {
         case 0:
            return 0L;
         case 1:
            return 500L;
         case 2:
            return 2000L;
         case 3:
            return 6000L;
         case 4:
            return 15000L;
         case 5:
            return 40000L;
         default:
            return 100000L;
      }
   }

   private void drawPanelWithLighting(int x, int y, int w, int h) {
      this.drawGradientRect(x, y, x + w, y + h, -868600792, -870442476);
      drawRect(x + 1, y + 1, x + w - 1, y + 2, 872415231);
      drawRect(x + 1, y + 1, x + 2, y + h - 1, 587202559);
      drawRect(x + 1, y + h - 2, x + w - 1, y + h - 1, 1426063360);
      drawRect(x + w - 2, y + 1, x + w - 1, y + h - 1, 855638016);
      drawRect(x, y, x + w, y + 1, -11910088);
      drawRect(x, y + h - 1, x + w, y + h, -14015464);
      drawRect(x, y, x + 1, y + h, -11910088);
      drawRect(x + w - 1, y, x + w, y + h, -14015464);
   }

   private void drawBorderWithShading(int x, int y, int w, int h, int color, int thickness) {
      int bright = brightenColor(color, 35);
      int dark = darkenColor(color, 35);
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

   private void drawScrollCorners() {
      int size = 10;
      int offset = 8;
      int bright = brightenColor(9071162, 25) | -16777216;
      int dark = darkenColor(9071162, 25) | -16777216;
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + size, this.guiTop + offset + 2, bright);
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + 2, this.guiTop + offset + size, bright);
      drawRect(this.guiLeft + offset + 2, this.guiTop + offset + 2, this.guiLeft + offset + 4, this.guiTop + offset + 4, bright);
      drawRect(this.guiLeft + 360 - offset - size, this.guiTop + offset, this.guiLeft + 360 - offset, this.guiTop + offset + 2, bright);
      drawRect(this.guiLeft + 360 - offset - 2, this.guiTop + offset, this.guiLeft + 360 - offset, this.guiTop + offset + size, dark);
      drawRect(this.guiLeft + offset, this.guiTop + 280 - offset - 2, this.guiLeft + offset + size, this.guiTop + 280 - offset, dark);
      drawRect(this.guiLeft + offset, this.guiTop + 280 - offset - size, this.guiLeft + offset + 2, this.guiTop + 280 - offset, bright);
      drawRect(this.guiLeft + 360 - offset - size, this.guiTop + 280 - offset - 2, this.guiLeft + 360 - offset, this.guiTop + 280 - offset, dark);
      drawRect(this.guiLeft + 360 - offset - 2, this.guiTop + 280 - offset - size, this.guiLeft + 360 - offset, this.guiTop + 280 - offset, dark);
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

   private void rebuildEndgameOutpostButtons() {
      int centerX = this.guiLeft + 180;
      int ry = this.guiTop + 224;
      boolean hasActiveMission = QuestClientData.hasActiveQuestInSlot("outpost_mission");
      if (hasActiveMission) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(524, centerX - 70, ry, 140, 20, "§fLeave Outpost Mission", -7718342, -9823718));
      } else {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(523, centerX - 70, ry, 140, 20, "§fAccept Outpost Mission", -12940742, -15046118));
      }

   }

   private void rebuildEndgameBingoButtons() {
      EndgameClientData data = EndgameClientData.getInstance();
      List<EndgameClientData.BingoSlotClient> slots = data.getBingoSlots();
      int listY = this.guiTop + 86;
      int listX = this.guiLeft + 8;
      int entryH = 22;

      for(int i = 0; i < slots.size() && i < 5; ++i) {
         boolean sel = this.endgameBingoIndex == i;
         EndgameClientData.BingoSlotClient slot = (EndgameClientData.BingoSlotClient)slots.get(i);
         String icon = slot.completed ? "§a✓ " : "§7○ ";
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(530 + i, listX, listY + i * entryH, 151, entryH - 2, icon + (sel ? "§f" : "§7") + slot.targetName, sel ? -10860000 : -12963816, sel ? -12965360 : -14016496));
      }

      if (this.endgameBingoIndex >= 0 && this.endgameBingoIndex < slots.size()) {
         int rx = this.guiLeft + 155 + 14;
         int ry = this.guiTop + 200;
         EndgameClientData.BingoSlotClient slot = (EndgameClientData.BingoSlotClient)slots.get(this.endgameBingoIndex);
         if (!slot.completed) {
            boolean isHunting = data.hasActiveHunt() && data.getActiveHuntSlot() == this.endgameBingoIndex;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(535, rx + 20, ry, 120, 18, isHunting ? "§cAbandon Hunt" : "§fAccept Hunt", isHunting ? -7718342 : -12940742, isHunting ? -9823718 : -15046118));
         }
      }

   }

   private void rebuildEndgameEventsButtons() {
      EndgameClientData data = EndgameClientData.getInstance();
      int contentW = 332;
      if (data.isIncursionActive()) {
         boolean joined = data.hasJoinedIncursion();
         String label = joined ? "§fLeave" : "§fJoin";
         int btnColor1 = joined ? -7718342 : -12948854;
         int btnColor2 = joined ? -9823718 : -15054230;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(540, this.guiLeft + contentW - 30, this.guiTop + 107, 40, 12, label, btnColor1, btnColor2));
      }

      if (data.isDefenseActive()) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(541, this.guiLeft + contentW - 30, this.guiTop + 173, 40, 12, "§fJoin", -7706054, -9811416));
      }

   }

   private void rebuildEndgameLeaderboardButtons() {
      int rx = this.guiLeft + 8;
      int ry = this.guiTop + 86;
      int catW = 75;
      int catSpacing = 4;

      for(int i = 0; i < 4; ++i) {
         boolean sel = this.endgameLbCategory == i;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(550 + i, rx + i * (catW + catSpacing), ry, catW, 14, (sel ? "§f" : "§7") + ENDGAME_LB_CATEGORY_NAMES[i], sel ? -10855878 : -13092828, sel ? -12961254 : -14013926));
      }

   }

   private void drawEndgameContent(int mouseX, int mouseY) {
      int badgeX = this.guiLeft + 14;
      int badgeW = 332;
      this.drawPanelWithLighting(this.guiLeft + 8, this.guiTop + 63, badgeW + 4, 16);
      this.drawPveRankBadge(badgeX, this.guiTop + 66, badgeW);
      switch (this.endgameSubTab) {
         case 0:
            this.drawEndgameOutpostContent(mouseX, mouseY);
            break;
         case 1:
            this.drawEndgameBingoContent(mouseX, mouseY);
            break;
         case 2:
            this.drawEndgameEventsContent(mouseX, mouseY);
            break;
         case 3:
            this.drawEndgameLeaderboardContent(mouseX, mouseY);
      }

   }

   private void drawEndgameOutpostContent(int mouseX, int mouseY) {
      EndgameClientData data = EndgameClientData.getInstance();
      int panelX = this.guiLeft + 8;
      int panelW = 344;
      this.drawPanelWithLighting(panelX, this.guiTop + 82, panelW, 158);
      int detailX = panelX + 8;
      int detailY = this.guiTop + 90;
      int textW = panelW - 16;
      this.fontRenderer.drawStringWithShadow("§l§6Outpost Missions", (float)detailX, (float)detailY, -1521552);
      detailY += 14;
      this.fontRenderer.drawStringWithShadow("§7Random location, enemies, and difficulty.", (float)detailX, (float)detailY, -7700886);
      detailY += 16;
      this.fontRenderer.drawStringWithShadow("§lRewards by Tier:", (float)detailX, (float)detailY, -1521552);
      detailY += 11;
      this.fontRenderer.drawStringWithShadow("  §7Chunin:  §6800-1,200 Ryo  §b+50 PvE XP", (float)detailX, (float)detailY, -2832216);
      detailY += 10;
      this.fontRenderer.drawStringWithShadow("  §7Jonin:   §61,500-2,200 Ryo  §b+100 PvE XP", (float)detailX, (float)detailY, -2832216);
      detailY += 10;
      this.fontRenderer.drawStringWithShadow("  §7ANBU:    §62,500-3,500 Ryo  §b+200 PvE XP", (float)detailX, (float)detailY, -2832216);
      detailY += 14;
      List<OutpostRegistry.EncounterGroup> groups = OutpostRegistry.getAllEncounterGroups();
      this.fontRenderer.drawStringWithShadow("§8" + groups.size() + " encounter groups | 1-2 bosses each", (float)detailX, (float)detailY, -7700886);
      detailY += 14;
      int totalLocations = OutpostRegistry.getLocationCount();
      int availableCount = 0;

      for(OutpostDefinition loc : OutpostRegistry.getAll()) {
         boolean anyTierAvailable = false;

         for(int t = 0; t < 3; ++t) {
            if (!data.isOutpostOnCooldown(loc.getOutpostId(), t)) {
               anyTierAvailable = true;
               break;
            }
         }

         if (anyTierAvailable) {
            ++availableCount;
         }
      }

      String statusColor = availableCount > 0 ? "§a" : "§c";
      this.fontRenderer.drawStringWithShadow("Available: " + statusColor + availableCount + "/" + totalLocations + " locations", (float)detailX, (float)detailY, -2832216);
      detailY += 14;
      if (QuestClientData.hasActiveQuestInSlot("outpost_mission")) {
         QuestClientData.ActiveQuestState aq = QuestClientData.getActiveQuestInSlot("outpost_mission");
         if (aq != null) {
            this.fontRenderer.drawStringWithShadow("§a§lActive: §f" + aq.questName, (float)detailX, (float)detailY, -2832216);
            detailY += 11;
            this.fontRenderer.drawStringWithShadow("§7" + aq.stepDesc, (float)detailX, (float)detailY, -7700886);
         }
      }

   }

   private void drawEndgameBingoContent(int mouseX, int mouseY) {
      EndgameClientData data = EndgameClientData.getInstance();
      List<EndgameClientData.BingoSlotClient> slots = data.getBingoSlots();
      this.drawPanelWithLighting(this.guiLeft + 6, this.guiTop + 82, 155, 114);
      int rx = this.guiLeft + 155 + 10;
      int rpW = 187;
      this.drawPanelWithLighting(rx, this.guiTop + 82, rpW, 158);
      if (slots.isEmpty()) {
         int ey = this.guiTop + 92;
         this.fontRenderer.drawStringWithShadow("§6§lBingo Book", (float)(rx + 10), (float)ey, -1521552);
         ey += 14;

         for(String line : this.fontRenderer.listFormattedStringToWidth("§7Your personal bounty board. Each day you receive 5 targets to hunt across the world. Accept a hunt, travel to the target's location, and eliminate them for Ryo rewards. Complete all 5 for a 2x jackpot bonus!", rpW - 20)) {
            this.fontRenderer.drawStringWithShadow(line, (float)(rx + 10), (float)ey, -7700886);
            ey += 10;
         }

         ey += 6;
         this.fontRenderer.drawStringWithShadow("§eBoard resets daily.", (float)(rx + 10), (float)ey, -2832216);
      } else {
         if (this.endgameBingoIndex >= 0 && this.endgameBingoIndex < slots.size()) {
            EndgameClientData.BingoSlotClient slot = (EndgameClientData.BingoSlotClient)slots.get(this.endgameBingoIndex);
            int detailX = rx + 6;
            int detailY = this.guiTop + 88;
            String nameColor = slot.completed ? "§a" : "§f";
            this.fontRenderer.drawStringWithShadow("§l" + nameColor + slot.targetName, (float)detailX, (float)detailY, -1521552);
            detailY += 14;
            if (!slot.loreHint.isEmpty()) {
               for(String line : this.fontRenderer.listFormattedStringToWidth("§7\"" + slot.loreHint + "\"", rpW - 14)) {
                  this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -7700886);
                  detailY += 10;
               }

               detailY += 4;
            }

            this.fontRenderer.drawStringWithShadow("Target Rank: §e" + slot.regionName, (float)detailX, (float)detailY, -2832216);
            detailY += 12;
            this.fontRenderer.drawStringWithShadow("Bounty: §6" + slot.ryoReward + " Ryo §b+" + 30 + "-" + 100 + " PvE XP", (float)detailX, (float)detailY, -2832216);
            detailY += 12;
            String status = slot.completed ? "§aEliminated" : (data.hasActiveHunt() && data.getActiveHuntSlot() == this.endgameBingoIndex ? "§eTracking..." : "§7Available");
            this.fontRenderer.drawStringWithShadow("Status: " + status, (float)detailX, (float)detailY, -2832216);
            detailY += 16;
            if (!slot.completed && (!data.hasActiveHunt() || data.getActiveHuntSlot() != this.endgameBingoIndex)) {
               for(String line : this.fontRenderer.listFormattedStringToWidth("§8Click 'Accept Hunt' to track this target. A waypoint will guide you to the search area.", rpW - 14)) {
                  this.fontRenderer.drawStringWithShadow(line, (float)detailX, (float)detailY, -7700886);
                  detailY += 10;
               }
            }
         }

         int completedCount = 0;

         for(EndgameClientData.BingoSlotClient s : slots) {
            if (s.completed) {
               ++completedCount;
            }
         }

         String jackpotText = data.isBingoComplete() ? "§6§lJACKPOT! All 5 eliminated — 2x bonus!" : "§eJackpot: Eliminate all 5 for 2x bonus! (" + completedCount + "/5)";
         int jw = this.fontRenderer.getStringWidth(jackpotText);
         this.fontRenderer.drawStringWithShadow(jackpotText, (float)(this.guiLeft + (360 - jw) / 2), (float)(this.guiTop + 280 - 50), -1521552);
      }
   }

   private void drawEndgameEventsContent(int mouseX, int mouseY) {
      EndgameClientData data = EndgameClientData.getInstance();
      int cx = this.guiLeft + 14;
      int cy = this.guiTop + 86;
      int contentW = 332;
      this.drawPanelWithLighting(this.guiLeft + 8, cy - 2, contentW + 4, 58);
      this.fontRenderer.drawStringWithShadow("§l§bIncursion", (float)cx, (float)cy, -1521552);
      cy += 12;
      if (data.isIncursionActive()) {
         this.fontRenderer.drawStringWithShadow("§f" + data.getIncursionName(), (float)(cx + 4), (float)cy, -2832216);
         cy += 10;
         String stateName = data.getIncursionStateName();
         int countdown = data.getIncursionCountdown();
         if ("ANNOUNCED".equals(stateName) && countdown >= 0) {
            this.fontRenderer.drawStringWithShadow("Starting in: §e" + countdown + "s  §7Location: §e" + (int)data.getIncursionX() + ", " + (int)data.getIncursionZ(), (float)(cx + 4), (float)cy, -2832216);
         } else if ("WAITING".equals(stateName)) {
            this.fontRenderer.drawStringWithShadow("§eWaiting for players to arrive...", (float)(cx + 4), (float)cy, -2832216);
         } else if ("WAVE_BREAK".equals(stateName) && countdown >= 0) {
            this.fontRenderer.drawStringWithShadow("Wave: §e" + data.getIncursionWave() + "/" + data.getIncursionMaxWaves() + "  §7Next wave: §e" + countdown + "s", (float)(cx + 4), (float)cy, -2832216);
         } else {
            this.fontRenderer.drawStringWithShadow("Wave: §e" + data.getIncursionWave() + "/" + data.getIncursionMaxWaves() + "  §7Enemies: §c" + data.getIncursionEnemiesRemaining(), (float)(cx + 4), (float)cy, -2832216);
         }

         cy += 10;
         if (!"ANNOUNCED".equals(stateName)) {
            this.fontRenderer.drawStringWithShadow("Location: §e" + (int)data.getIncursionX() + ", " + (int)data.getIncursionZ(), (float)(cx + 4), (float)cy, -7700886);
         }
      } else {
         this.fontRenderer.drawStringWithShadow("§8No active incursion.", (float)(cx + 4), (float)cy, -7700886);
      }

      cy = this.guiTop + 152;
      this.drawPanelWithLighting(this.guiLeft + 8, cy - 2, contentW + 4, 58);
      this.fontRenderer.drawStringWithShadow("§l§6Village Defense", (float)cx, (float)cy, -1521552);
      cy += 12;
      if (data.isDefenseActive()) {
         this.fontRenderer.drawStringWithShadow("§f" + data.getDefenseVillage() + " under attack!", (float)(cx + 4), (float)cy, -2832216);
         cy += 10;
         this.fontRenderer.drawStringWithShadow("Wave: §e" + data.getDefenseWave() + "/" + data.getDefenseMaxWaves(), (float)(cx + 4), (float)cy, -2832216);
         cy += 10;
         int hp = data.getDefenseVillageHP();
         int maxHp = data.getDefenseMaxHP();
         String hpColor = hp > maxHp / 2 ? "§a" : (hp > maxHp / 4 ? "§e" : "§c");
         this.fontRenderer.drawStringWithShadow("Village HP: " + hpColor + hp + "/" + maxHp, (float)(cx + 4), (float)cy, -2832216);
      } else {
         this.fontRenderer.drawStringWithShadow("§8No active defense.", (float)(cx + 4), (float)cy, -7700886);
      }

      cy = this.guiTop + 218;
      this.drawPanelWithLighting(this.guiLeft + 8, cy - 2, contentW + 4, 30);
      this.fontRenderer.drawStringWithShadow("§l§dRaid Cooldown", (float)cx, (float)cy, -1521552);
      cy += 12;
      if (data.isRaidOnCooldown()) {
         long rem = data.getRaidCooldownRemaining();
         long min = rem / 60000L % 60L;
         long hrs = rem / 3600000L;
         this.fontRenderer.drawStringWithShadow("§c" + hrs + "h " + min + "m remaining", (float)(cx + 4), (float)cy, -7700886);
      } else {
         this.fontRenderer.drawStringWithShadow("§aReady!", (float)(cx + 4), (float)cy, -2832216);
      }

   }

   private void drawEndgameLeaderboardContent(int mouseX, int mouseY) {
      EndgameClientData data = EndgameClientData.getInstance();
      String[] lbCatKeys = new String[]{"outposts", "bingo", "incursions", "defense"};
      String catKey = lbCatKeys[this.endgameLbCategory];
      int listY = this.guiTop + 108;
      int listX = this.guiLeft + 14;
      int contentW = 332;
      this.drawPanelWithLighting(this.guiLeft + 8, listY - 4, contentW + 4, 150);
      this.fontRenderer.drawStringWithShadow("§l" + ENDGAME_LB_CATEGORY_NAMES[this.endgameLbCategory] + " Leaderboard", (float)listX, (float)listY, -1521552);
      listY += 14;
      this.fontRenderer.drawStringWithShadow("§nRank", (float)listX, (float)listY, -7700886);
      this.fontRenderer.drawStringWithShadow("§nPlayer", (float)(listX + 35), (float)listY, -7700886);
      this.fontRenderer.drawStringWithShadow("§nScore", (float)(listX + contentW - 50), (float)listY, -7700886);
      listY += 12;
      List<EndgameClientData.LeaderboardEntry> entries = data.getLeaderboard(catKey);
      if (entries.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No data available.", (float)(listX + 4), (float)listY, -7700886);
      } else {
         for(int i = 0; i < entries.size() && i < 10; ++i) {
            EndgameClientData.LeaderboardEntry entry = (EndgameClientData.LeaderboardEntry)entries.get(i);
            String rankColor;
            switch (i) {
               case 0:
                  rankColor = "§6";
                  break;
               case 1:
                  rankColor = "§7";
                  break;
               case 2:
                  rankColor = "§c";
                  break;
               default:
                  rankColor = "§f";
            }

            this.fontRenderer.drawStringWithShadow(rankColor + "#" + (i + 1), (float)listX, (float)listY, -2832216);
            this.fontRenderer.drawStringWithShadow("§f" + entry.playerName, (float)(listX + 35), (float)listY, -2832216);
            this.fontRenderer.drawStringWithShadow("§e" + entry.value, (float)(listX + contentW - 50), (float)listY, -2832216);
            listY += 12;
         }
      }

   }

   private static String getArcDisplayName(int arc) {
      switch (arc) {
         case 1:
            return "Becoming a Genin";
         case 2:
            return "Land of Waves";
         case 3:
            return "Chunin Exams";
         case 4:
            return "Konoha Crush";
         case 5:
            return "Search for Tsunade";
         case 6:
            return "Sasuke Recovery";
         case 7:
            return "Kazekage Rescue";
         case 8:
            return "Tenchi Bridge";
         case 9:
            return "The Immortal Duo";
         case 10:
            return "Hunt for Itachi";
         case 99:
            return "Your Shinobi Way";
         default:
            return "Arc " + arc;
      }
   }

   private static int getSPForRankOrdinal(int rankOrdinal) {
      switch (rankOrdinal) {
         case 2:
            return 2;
         case 3:
            return 4;
         case 4:
            return 7;
         case 5:
            return 10;
         default:
            return 0;
      }
   }

   private static int getPveXpForRankOrdinal(int rankOrdinal) {
      switch (rankOrdinal) {
         case 0:
            return 10;
         case 1:
            return 20;
         case 2:
            return 35;
         case 3:
            return 50;
         case 4:
         case 5:
            return 75;
         default:
            return 10;
      }
   }

   private void drawPveRankBadge(int rpx, int rpy, int rpw) {
      EndgameClientData pveData = EndgameClientData.getInstance();
      String pveRankName = pveData.getPveRankName();
      PveRank currentPveRank = PveRank.byName(pveRankName);
      if (currentPveRank == null) {
         currentPveRank = PveRank.GENIN;
      }

      int pveRankColor = getPveRankColor(currentPveRank);
      String rankLabel = "§l[" + pveRankName + "]";
      this.fontRenderer.drawStringWithShadow(rankLabel, (float)rpx, (float)rpy, pveRankColor);
      int pveXp = pveData.getPveXp();
      int pveCurThreshold = currentPveRank.getXpThreshold();
      PveRank nextPveRank = currentPveRank.getNextRank();
      int pveNextThreshold = nextPveRank != null ? nextPveRank.getXpThreshold() : pveCurThreshold;
      int pveProgress = pveXp - pveCurThreshold;
      int pveNeeded = pveNextThreshold - pveCurThreshold;
      int rankLabelWidth = this.fontRenderer.getStringWidth(rankLabel);
      int barX = rpx + rankLabelWidth + 6;
      int barW = Math.min(rpw - rankLabelWidth - 50, 140);
      int barY = rpy + 1;
      drawRect(barX, barY, barX + barW, barY + 6, -15068144);
      if (pveNeeded > 0) {
         int fillW = (int)((float)pveProgress / (float)pveNeeded * (float)barW);
         fillW = Math.min(fillW, barW);
         drawRect(barX + 1, barY + 1, barX + 1 + fillW, barY + 5, pveRankColor & -1426063361);
      }

      String xpText = nextPveRank != null ? pveProgress + "/" + pveNeeded : "MAX";
      this.fontRenderer.drawStringWithShadow(xpText, (float)(barX + barW + 4), (float)(barY - 1), -7700886);
   }

   private void rebuildFactionButtons() {
      if (AkatsukiClientData.hasPendingInvite && !AkatsukiClientData.isAkatsuki) {
         int centerX = this.guiLeft + 180;
         int btnY = this.guiTop + 180;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(710, centerX - 70, btnY, 60, 18, "§aAccept", -13997526, -15054310));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(711, centerX + 10, btnY, 60, 18, "§cDecline", -9819606, -11920870));
      } else {
         boolean isOp = this.mc.player != null && this.mc.player.canUseCommand(2, "");
         String[] tabNames;
         if (AkatsukiClientData.isAkatsuki) {
            if (isOp) {
               tabNames = new String[]{"Assign", "Bounties", "Ring", "Roster", "Rep", "Shop", "Leader"};
            } else {
               tabNames = new String[]{"Assign", "Bounties", "Ring", "Roster", "Rep", "Shop"};
            }
         } else {
            tabNames = new String[]{"Threats", "Bounties", "Hunter", "Defense", "Rally", "Log"};
         }

         int subTabW = tabNames.length > 6 ? 44 : 50;
         int subTabSpacing = 3;
         int subTabCount = tabNames.length;
         int subTotalW = subTabW * subTabCount + subTabSpacing * (subTabCount - 1);
         int subStartX = this.guiLeft + (360 - subTotalW) / 2;
         int subTabY = this.guiTop + 50;

         for(int i = 0; i < subTabCount; ++i) {
            boolean sel = this.factionSubTab == i;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(700 + i, subStartX + (subTabW + subTabSpacing) * i, subTabY, subTabW, 13, sel ? "§f" + tabNames[i] : "§8" + tabNames[i], sel ? -9822176 : -13623264, sel ? -11923440 : -14673896));
         }

         if (AkatsukiClientData.isAkatsuki && this.factionSubTab == 2) {
            int btnX = this.guiLeft + 360 - 68;
            int btnStartY = this.guiTop + 88;

            for(int i = 0; i < 5; ++i) {
               int level = AkatsukiClientData.ringUpgradeLevels[i];
               boolean maxed = level >= 5;
               int cost = maxed ? 0 : RingUpgradeConstants.getUpgradeCost(i, level);
               boolean canAfford = !maxed && AkatsukiClientData.bountyTokens >= cost;
               String label = maxed ? "§8MAX" : (canAfford ? "§a" + cost + "T" : "§c" + cost + "T");
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(720 + i, btnX, btnStartY + i * 22, 48, 13, label, canAfford ? -13997526 : -11919328, canAfford ? -15054310 : -13627376));
            }
         }

         if (AkatsukiClientData.isAkatsuki && this.factionSubTab == 5) {
            int panelX = this.guiLeft + 10;
            int panelY = this.guiTop + 68;
            int panelW = 340;
            int panelH = 202;
            if (this.selectedBlackMarketItemId != null) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(829, panelX + 5, panelY + 2, 40, 13, "§7< Back", -12967904, -14018536));
               BlackMarketItem selectedItem = BlackMarketRegistry.get(this.selectedBlackMarketItemId);
               List<TerritoryClientData.ClientZoneData> validZones = new ArrayList();
               if (selectedItem != null) {
                  for(TerritoryClientData.ClientZoneData czd : TerritoryClientData.getInstance().getZoneStates().values()) {
                     if (!selectedItem.isOffensive() && !selectedItem.isMassDeploy()) {
                        if (selectedItem.isDefensive()) {
                           if ("akatsuki".equalsIgnoreCase(czd.ownerVillage)) {
                              validZones.add(czd);
                           }
                        } else if (!"akatsuki".equalsIgnoreCase(czd.ownerVillage)) {
                           validZones.add(czd);
                        }
                     } else if (!"akatsuki".equalsIgnoreCase(czd.ownerVillage)) {
                        validZones.add(czd);
                     }
                  }
               }

               validZones.sort((a, b) -> a.displayName.compareToIgnoreCase(b.displayName));
               int zoneY = panelY + 20;
               int zoneSpacing = 16;
               int maxVisible = Math.min(validZones.size() - this.blackMarketZoneScrollOffset, (panelH - 24) / zoneSpacing);
               maxVisible = Math.min(maxVisible, 36);

               for(int i = 0; i < maxVisible; ++i) {
                  int zIdx = i + this.blackMarketZoneScrollOffset;
                  if (zIdx >= validZones.size()) {
                     break;
                  }

                  TerritoryClientData.ClientZoneData czd = (TerritoryClientData.ClientZoneData)validZones.get(zIdx);
                  String ownerTag = czd.ownerVillage.isEmpty() ? "§8Neutral" : "§e" + czd.ownerVillage.substring(0, 1).toUpperCase() + czd.ownerVillage.substring(1);
                  String label = "§f" + czd.displayName + " " + ownerTag;
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(830 + i, panelX + 5, zoneY + i * zoneSpacing, panelW - 10, 14, label, -12965334, -14018022));
               }
            } else {
               List<BlackMarketItem> items = BlackMarketRegistry.getAll();
               int itemY = panelY + 20;
               int itemSpacing = 24;
               int maxVisible = Math.min(items.size() - this.blackMarketItemScrollOffset, (panelH - 24) / itemSpacing);
               maxVisible = Math.min(maxVisible, 10);

               for(int i = 0; i < maxVisible; ++i) {
                  int idx = i + this.blackMarketItemScrollOffset;
                  if (idx >= items.size()) {
                     break;
                  }

                  BlackMarketItem item = (BlackMarketItem)items.get(idx);
                  boolean canAfford = AkatsukiClientData.bountyTokens >= item.getTokenCost();
                  String buyLabel = canAfford ? "§aBUY" : "§cBUY";
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(810 + i, panelX + panelW - 40, itemY + i * itemSpacing, 35, 13, buyLabel, canAfford ? -13997526 : -11919328, canAfford ? -15054310 : -13627376));
               }
            }
         }

         if (AkatsukiClientData.isAkatsuki && this.factionSubTab == 6 && isOp) {
            int panelX = this.guiLeft + 10;
            int panelY = this.guiTop + 68;
            int panelW = 340;
            int modeTabW = 55;
            int modeTabSpacing = 3;
            int modeStartX = panelX + 50;
            int modeTabY = panelY + 4;
            String[] modeNames = new String[]{"Raid", "Mission", "Bounty"};
            int[] modeIds = new int[]{785, 786, 787};

            for(int i = 0; i < 3; ++i) {
               boolean sel = this.leaderMode == i;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(modeIds[i], modeStartX + (modeTabW + modeTabSpacing) * i, modeTabY, modeTabW, 12, sel ? "§f" + modeNames[i] : "§8" + modeNames[i], sel ? -9822176 : -13623264, sel ? -11923440 : -14673896));
            }

            int contentY = modeTabY + 20;
            if (this.leaderMode == 0) {
               if (AkatsukiClientData.raidActive) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(741, panelX + 10, contentY + 24, 80, 14, "§cCancel Raid", -9822176, -11923440));
               } else if (this.leaderRaidSelecting) {
                  String[] villages = new String[]{"Leaf", "Sand", "Mist", "Stone", "Cloud", "Rain"};

                  for(int i = 0; i < 6; ++i) {
                     int col = i % 3;
                     int row = i / 3;
                     this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(746 + i, panelX + 10 + col * 72, contentY + 12 + row * 18, 66, 14, "§f" + villages[i], -11919328, -13627376));
                  }
               } else {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(740, panelX + 10, contentY + 12, 80, 14, "§cStart Raid", -9822176, -11923440));
               }
            } else if (this.leaderMode != 1) {
               if (this.leaderMode == 2) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(781, panelX + 54, contentY, 16, 12, "§c-", -11919328, -13627376));
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(782, panelX + 114, contentY, 16, 12, "§a+", -14005718, -15060966));
                  boolean canAdd = this.leaderSelectedMember1 >= 0;
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(780, panelX + 10, contentY + 30, 90, 14, "§cAdd Bounty", canAdd ? -9822176 : -13623264, canAdd ? -11923440 : -14673896));
               }
            } else {
               String[] mTypes = new String[]{"Assassination", "Infiltration", "Extraction", "Sabotage", "Escort"};
               String mType = this.leaderMissionTypeIndex >= 0 && this.leaderMissionTypeIndex < mTypes.length ? mTypes[this.leaderMissionTypeIndex] : "Unknown";
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(770, panelX + 46, contentY, 112, 12, "§f" + mType, -11915232, -13623280));
               int tkRow = contentY + 16;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(773, panelX + 54, tkRow, 16, 12, "§c-", -11919328, -13627376));
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(774, panelX + 100, tkRow, 16, 12, "§a+", -14005718, -15060966));
               int rpRow = contentY + 30;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(775, panelX + 54, rpRow, 16, 12, "§c-", -11919328, -13627376));
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(776, panelX + 100, rpRow, 16, 12, "§a+", -14005718, -15060966));
               boolean canAssign = this.leaderSelectedMember1 >= 0;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(771, panelX + 10, contentY + 58, 76, 14, "§6Assign", canAssign ? -11912672 : -13623264, canAssign ? -13621232 : -14673896));
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(772, panelX + 90, contentY + 58, 76, 14, "§cRemove", -11919328, -13627376));
            }

            int panelH = 172;
            int memberStartY = panelY + panelH / 2 + 10;
            List<String> selectableNames = this.leaderGetSelectableNames();
            int maxMembers = Math.min(selectableNames.size(), (panelY + panelH - memberStartY - 4) / 13);
            maxMembers = Math.min(maxMembers, 10);

            for(int i = 0; i < maxMembers; ++i) {
               boolean isSel = i == this.leaderSelectedMember1;
               String label;
               if (this.leaderMode == 0) {
                  AkatsukiClientData.RosterEntry r = i < AkatsukiClientData.roster.size() ? (AkatsukiClientData.RosterEntry)AkatsukiClientData.roster.get(i) : null;
                  String ringInfo = r != null ? " §8[" + r.ringKanji + "]" : "";
                  label = (isSel ? "§e> " : "§7") + (String)selectableNames.get(i) + ringInfo;
               } else {
                  label = (isSel ? "§e> " : "§7") + (String)selectableNames.get(i);
               }

               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(760 + i, panelX + 10, memberStartY + i * 13, panelW - 20, 12, label, isSel ? -11908576 : -13623264, isSel ? -12961264 : -14673896));
            }

            if (this.leaderMode == 0 && this.leaderSelectedMember1 >= 0 && this.leaderSelectedMember2 >= 0) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(742, panelX + panelW - 110, panelY + panelH - 18, 100, 14, "§6Assign Partner", -11912672, -13621232));
            }
         }

         if (AkatsukiClientData.isAkatsuki && this.factionSubTab == 0) {
            int panelY = this.guiTop + 68;
            int panelH = 172;
            int abx = this.guiLeft + 10;
            int abw = 340;
            int aty = panelY + 6 + 14;
            aty -= this.akatsukiAssignScrollOffset;
            if (AkatsukiClientData.raidActive) {
               aty += 38;
            }

            if (AkatsukiClientData.hasLeaderMission) {
               aty += 77;
            }

            List<AkatsukiClientData.MissionClientEntry> actMissions = AkatsukiClientData.activeMissions;
            if (!actMissions.isEmpty()) {
               aty += 12;

               for(int i = 0; i < actMissions.size() && i < 5; ++i) {
                  AkatsukiClientData.MissionClientEntry m = (AkatsukiClientData.MissionClientEntry)actMissions.get(i);
                  boolean hasProgress = m.currentStepType == 1 || m.currentStepType == 2;
                  int cardH = hasProgress ? 51 : 42;
                  int btnY = aty + 1;
                  if (btnY >= this.guiTop + 68 && btnY < this.guiTop + 280 - 40) {
                     this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(795 + i, abx + abw - 52, btnY, 40, 12, "§cDrop", -11919328, -13627376));
                  }

                  aty += cardH;
               }
            }

            List<AkatsukiClientData.MissionOfferEntry> offerList = AkatsukiClientData.missionOffers;
            if (!offerList.isEmpty()) {
               aty += 12;

               for(int i = 0; i < offerList.size() && i < 5; ++i) {
                  int cardH = 42;
                  int btnY = aty + 1;
                  if (btnY >= this.guiTop + 68 && btnY < this.guiTop + 280 - 40) {
                     this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(790 + i, abx + abw - 52, btnY, 40, 12, "§aAccept", -14005718, -15060966));
                  }

                  aty += cardH;
               }
            }
         }

         if (!AkatsukiClientData.isAkatsuki && this.factionSubTab == 2) {
            int bx = this.guiLeft + 20;
            int by = this.guiTop + 68;
            boolean hasActive = !AkatsukiClientData.myContracts.isEmpty();
            if (!hasActive) {
               String[] typeNames = new String[]{"Assassination", "Territory Hire"};
               String typeName = this.contractTypeIndex >= 0 && this.contractTypeIndex < typeNames.length ? typeNames[this.contractTypeIndex] : "Unknown";
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(730, bx + 80, by + 54, 120, 15, "§f" + typeName, -11915232, -13623280));
               int targetBtnY = by + 74;
               if (this.contractTypeIndex == 0) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(800, bx + 80, targetBtnY, 20, 15, "§f<", -12963808, -14016496));
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(801, bx + 180, targetBtnY, 20, 15, "§f>", -12963808, -14016496));
               } else {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(802, bx + 80, targetBtnY, 20, 15, "§f<", -12963808, -14016496));
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(803, bx + 180, targetBtnY, 20, 15, "§f>", -12963808, -14016496));
               }

               int ryoBtnY = by + 94;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(731, bx + 80, ryoBtnY, 30, 15, "§c-", -11919328, -13627376));
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(732, bx + 170, ryoBtnY, 30, 15, "§a+", -14005718, -15060966));
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(733, bx + 80, by + 118, 120, 15, "§6Post Contract", -11912672, -13621232));
            } else {
               AkatsukiClientData.ContractClientEntry myc = (AkatsukiClientData.ContractClientEntry)AkatsukiClientData.myContracts.get(0);
               if (myc.statusOrdinal == 0) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(734, bx + 80, by + 100, 120, 15, "§cCancel Contract", -9822176, -11923440));
               }
            }
         }

         if (AkatsukiClientData.isAkatsuki && this.factionSubTab == 1) {
            List<AkatsukiClientData.ContractClientEntry> open = AkatsukiClientData.openContracts;
            List<AkatsukiClientData.ContractClientEntry> assigned = AkatsukiClientData.assignedContracts;
            int bountyCount = Math.min(AkatsukiClientData.bounties.size(), 5);
            int emptyBountyHeight = AkatsukiClientData.bounties.isEmpty() ? 14 : 0;
            int contractStartY = this.guiTop + 68 + 10 + 16 + bountyCount * 22 + emptyBountyHeight + 20;
            contractStartY += assigned.size() * 36;
            contractStartY -= this.contractScrollOffset;
            int panelTop = this.guiTop + 68;
            int panelBot = this.guiTop + 68 + 172;
            int accBtnY = contractStartY;
            int maxContracts = Math.min(open.size(), 5);

            for(int i = 0; i < Math.min(open.size(), 5); ++i) {
               if (accBtnY >= panelTop - 5 && accBtnY < panelBot - 15) {
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(735 + i, this.guiLeft + 360 - 75, accBtnY, 55, 13, "§aAccept", -13997526, -15054310));
               }

               accBtnY += 36;
            }
         }

      }
   }

   private void drawFactionContent(int mouseX, int mouseY) {
      int panelX = this.guiLeft + 10;
      int panelY = this.guiTop + 68;
      int panelW = 340;
      int panelH = 172;
      this.drawPanelWithLighting(panelX, panelY, panelW, panelH);
      int textX = panelX + 10;
      int textY = panelY + 10;
      int maxW = panelW - 20;
      if (AkatsukiClientData.hasPendingInvite && !AkatsukiClientData.isAkatsuki) {
         String inviteTitle = "§c§lAkatsuki Invitation";
         int tw = this.fontRenderer.getStringWidth(inviteTitle);
         this.fontRenderer.drawStringWithShadow(inviteTitle, (float)(this.guiLeft + (360 - tw) / 2), (float)(panelY + 20), -43691);
         String line1 = "You have been invited to join the Akatsuki.";
         int l1w = this.fontRenderer.getStringWidth(line1);
         this.fontRenderer.drawStringWithShadow(line1, (float)(this.guiLeft + (360 - l1w) / 2), (float)(panelY + 45), -2832216);
         String line2 = "§cWarning: §7Joining is permanent. You will leave";
         int l2w = this.fontRenderer.getStringWidth(line2);
         this.fontRenderer.drawStringWithShadow(line2, (float)(this.guiLeft + (360 - l2w) / 2), (float)(panelY + 65), -2832216);
         String line3 = "your village and lose access to village missions.";
         int l3w = this.fontRenderer.getStringWidth(line3);
         this.fontRenderer.drawStringWithShadow(line3, (float)(this.guiLeft + (360 - l3w) / 2), (float)(panelY + 76), -2832216);
      } else if (AkatsukiClientData.isAkatsuki) {
         switch (this.factionSubTab) {
            case 0:
               this.drawAkatsukiAssignments(panelX, panelY, panelW, panelH);
               break;
            case 1:
               this.drawAkatsukiBounties(panelX, panelY, panelW, panelH);
               break;
            case 2:
               this.drawAkatsukiRing(panelX, panelY, panelW, panelH);
               break;
            case 3:
               this.drawAkatsukiRoster(panelX, panelY, panelW, panelH);
               break;
            case 4:
               this.drawAkatsukiReputation(panelX, panelY, panelW, panelH);
               break;
            case 5:
               this.drawAkatsukiShop(panelX, panelY, panelW, panelH);
               break;
            case 6:
               this.drawLeaderPanel(panelX, panelY, panelW, panelH);
         }

      } else {
         switch (this.factionSubTab) {
            case 0:
               this.drawVillageThreats(panelX, panelY, panelW, panelH);
               break;
            case 1:
               this.drawVillageBounties(panelX, panelY, panelW, panelH);
               break;
            case 2:
               this.drawVillageHunter(panelX, panelY, panelW, panelH);
               break;
            case 3:
            case 4:
            default:
               this.drawFactionComingSoon(panelX, panelY, panelW, panelH);
               break;
            case 5:
               this.drawVillageLog(panelX, panelY, panelW, panelH);
         }

      }
   }

   private void drawAkatsukiAssignments(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 6;
      this.fontRenderer.drawStringWithShadow("§c§lAssignments", (float)tx, (float)ty, -43691);
      ty += 14;
      int bottomLimit = py + ph - 4;
      ScaledResolution sr = new ScaledResolution(this.mc);
      int scaleFactor = sr.getScaleFactor();
      int clipX = px * scaleFactor;
      int clipY = (sr.getScaledHeight() - (py + ph)) * scaleFactor;
      int clipW = pw * scaleFactor;
      int clipH = (ph - 20) * scaleFactor;
      GL11.glEnable(3089);
      GL11.glScissor(clipX, clipY, clipW, clipH);
      ty -= this.akatsukiAssignScrollOffset;
      if (AkatsukiClientData.raidActive) {
         drawRect(px + 2, ty - 2, px + pw - 2, ty + 30, -1436155904);
         String villageCap = AkatsukiClientData.raidTargetVillage.isEmpty() ? "?" : AkatsukiClientData.raidTargetVillage.substring(0, 1).toUpperCase() + AkatsukiClientData.raidTargetVillage.substring(1);
         this.fontRenderer.drawStringWithShadow("§c§lRAID: " + villageCap, (float)(tx + 2), (float)ty, -43691);
         ty += 10;
         long elapsed = System.currentTimeMillis() - AkatsukiClientData.raidSyncTime;
         long remain = Math.max(0L, AkatsukiClientData.raidTimeRemainingMs - elapsed);
         long secs = remain / 1000L;
         this.fontRenderer.drawStringWithShadow("§7Kills: §f" + AkatsukiClientData.raidKillsAchieved + "/" + AkatsukiClientData.raidKillsRequired + "  §7Time: §e" + String.format("%d:%02d", secs / 60L, secs % 60L), (float)(tx + 4), (float)ty, -2832216);
         ty += 10;
         this.fontRenderer.drawStringWithShadow("§7Rally: §f(" + AkatsukiClientData.raidRallyX + ", " + AkatsukiClientData.raidRallyZ + ")", (float)(tx + 4), (float)ty, -2832216);
         ty += 14;
      }

      if (AkatsukiClientData.hasLeaderMission) {
         drawRect(px + 2, ty - 1, px + pw - 2, ty + 63, -2143018496);
         this.fontRenderer.drawStringWithShadow("§6§lPain's Order", (float)(tx + 2), (float)ty, -22016);
         String lStatus = AkatsukiClientData.leaderMissionCompleted ? "§a[DONE]" : "§e[ACTIVE]";
         this.fontRenderer.drawStringWithShadow(lStatus, (float)(px + pw - 10 - this.fontRenderer.getStringWidth(lStatus)), (float)ty, -2832216);
         ty += 10;
         String typeLabel = AkatsukiClientData.leaderMissionType;
         if (typeLabel != null && !typeLabel.isEmpty()) {
            typeLabel = typeLabel.substring(0, 1).toUpperCase() + typeLabel.substring(1);
         }

         this.fontRenderer.drawStringWithShadow("§c" + typeLabel + " §8- " + AkatsukiClientData.leaderMissionDesc, (float)(tx + 4), (float)ty, -2832216);
         ty += 10;
         String lObj = AkatsukiClientData.leaderMissionObjective;
         if (lObj != null && !lObj.isEmpty()) {
            if (lObj.length() > 42) {
               lObj = lObj.substring(0, 40) + "..";
            }

            this.fontRenderer.drawStringWithShadow("§e> " + lObj, (float)(tx + 4), (float)ty, -7700886);
         }

         ty += 10;
         this.fontRenderer.drawStringWithShadow("§8Location: §f(" + AkatsukiClientData.leaderMissionX + ", " + AkatsukiClientData.leaderMissionZ + ")", (float)(tx + 4), (float)ty, -7700886);
         ty += 10;
         int lRyo = AkatsukiClientData.leaderMissionTokenReward * 30;
         int lPve = AkatsukiClientData.leaderMissionTokenReward * 2;
         this.fontRenderer.drawStringWithShadow("§6" + lRyo + " Ryo §b" + lPve + " PvE XP §e" + AkatsukiClientData.leaderMissionTokenReward + " Tokens §c" + AkatsukiClientData.leaderMissionRepReward + " Rep", (float)(tx + 4), (float)ty, -7700886);
         ty += 14;
      }

      List<AkatsukiClientData.MissionClientEntry> missions = AkatsukiClientData.activeMissions;
      if (!missions.isEmpty()) {
         this.drawHorizontalLine(px + 4, px + pw - 4, ty - 1, -11919328);
         this.fontRenderer.drawStringWithShadow("§c§lActive Missions", (float)tx, (float)(ty + 1), -3390396);
         ty += 12;

         for(int i = 0; i < missions.size(); ++i) {
            AkatsukiClientData.MissionClientEntry m = (AkatsukiClientData.MissionClientEntry)missions.get(i);
            String catLabel = m.categoryOrdinal == 0 ? "§7Daily" : (m.categoryOrdinal == 1 ? "§dWeekly" : "§6Special");
            boolean hasProgress = m.currentStepType == 1 || m.currentStepType == 2;
            int boxH = hasProgress ? 51 : 42;
            drawRect(px + 3, ty - 1, px + pw - 3, ty + boxH, 1613762576);
            this.fontRenderer.drawStringWithShadow(catLabel + " §f" + m.templateName, (float)(tx + 2), (float)ty, -2832216);
            if (m.totalSteps > 1) {
               String stepLabel = m.getStepLabel();
               this.fontRenderer.drawStringWithShadow("§7" + stepLabel, (float)(px + pw - 8 - this.fontRenderer.getStringWidth(stepLabel)), (float)ty, -7700886);
            } else {
               String stateLabel = m.getMissionStateLabel();
               this.fontRenderer.drawStringWithShadow(stateLabel, (float)(px + pw - 8 - this.fontRenderer.getStringWidth(stateLabel)), (float)ty, -7700886);
            }

            ty += 10;
            String stepTypePrefix = m.getStepTypeLabel();
            String stepObj = m.currentStepObjective != null ? m.currentStepObjective : m.objectiveText;
            if (stepObj == null) {
               stepObj = "";
            }

            String fullStepLine = stepTypePrefix + ": " + stepObj;
            if (this.fontRenderer.getStringWidth(fullStepLine) > pw - 24) {
               while(stepObj.length() > 0 && this.fontRenderer.getStringWidth(stepTypePrefix + ": " + stepObj + "..") > pw - 24) {
                  stepObj = stepObj.substring(0, stepObj.length() - 1);
               }

               fullStepLine = stepTypePrefix + ": " + stepObj + "..";
            }

            this.fontRenderer.drawStringWithShadow(fullStepLine, (float)(tx + 4), (float)ty, -7700886);
            ty += 9;
            if (m.currentStepType == 1) {
               String killText = "§8Targets: §f" + m.killsAchieved + "/" + m.killsRequired;
               if (m.killsRequired > 0 && m.killsAchieved >= m.killsRequired) {
                  killText = "§aAll targets eliminated!";
               }

               this.fontRenderer.drawStringWithShadow(killText, (float)(tx + 4), (float)ty, -7700886);
               ty += 9;
            } else if (m.currentStepType == 2) {
               int scoutSecs = m.scoutProgress / 20;
               int scoutReqSecs = m.scoutRequired / 20;
               this.fontRenderer.drawStringWithShadow("§bScouting... §f" + scoutSecs + "s / " + scoutReqSecs + "s", (float)(tx + 4), (float)ty, -7700886);
               ty += 9;
            }

            this.fontRenderer.drawStringWithShadow("§8Location: §f(" + m.targetX + ", " + m.targetZ + ")", (float)(tx + 4), (float)ty, -7700886);
            ty += 9;
            this.fontRenderer.drawStringWithShadow("§6" + m.ryoReward + " Ryo §b" + m.pveXpReward + " PvE XP §e" + m.tokenReward + " Tokens §c" + m.repReward + " Rep", (float)(tx + 4), (float)ty, -7700886);
            ty += 14;
         }
      }

      List<AkatsukiClientData.MissionOfferEntry> offers = AkatsukiClientData.missionOffers;
      if (!offers.isEmpty()) {
         this.drawHorizontalLine(px + 4, px + pw - 4, ty - 1, -11919328);
         this.fontRenderer.drawStringWithShadow("§e§lAvailable Missions", (float)tx, (float)(ty + 1), -22016);
         ty += 12;

         for(int i = 0; i < offers.size(); ++i) {
            AkatsukiClientData.MissionOfferEntry o = (AkatsukiClientData.MissionOfferEntry)offers.get(i);
            String catLabel = o.categoryOrdinal == 0 ? "§7Daily" : (o.categoryOrdinal == 1 ? "§dWeekly" : "§6Special");
            drawRect(px + 3, ty - 1, px + pw - 3, ty + 40, 1612718096);
            String offerName = o.templateName;
            if (o.totalSteps > 1) {
               offerName = offerName + " §8(" + o.totalSteps + " steps)";
            }

            this.fontRenderer.drawStringWithShadow(catLabel + " §e" + offerName, (float)(tx + 2), (float)ty, -2832216);
            ty += 10;
            if (o.description != null && !o.description.isEmpty()) {
               String desc = o.description;
               if (this.fontRenderer.getStringWidth(desc) > pw - 24) {
                  while(desc.length() > 0 && this.fontRenderer.getStringWidth(desc + "..") > pw - 24) {
                     desc = desc.substring(0, desc.length() - 1);
                  }

                  desc = desc + "..";
               }

               this.fontRenderer.drawStringWithShadow("§8" + desc, (float)(tx + 4), (float)ty, -7700886);
            }

            ty += 9;
            this.fontRenderer.drawStringWithShadow("§8Location: §f(" + o.targetX + ", " + o.targetZ + ")", (float)(tx + 4), (float)ty, -7700886);
            ty += 9;
            this.fontRenderer.drawStringWithShadow("§6" + o.ryoReward + " Ryo §b" + o.pveXpReward + " PvE XP §e" + o.tokenReward + " Tokens §c" + o.repReward + " Rep", (float)(tx + 4), (float)ty, -7700886);
            ty += 14;
         }
      }

      if (missions.isEmpty() && offers.isEmpty() && !AkatsukiClientData.hasLeaderMission && !AkatsukiClientData.raidActive) {
         this.fontRenderer.drawStringWithShadow("§8No assignments available.", (float)tx, (float)ty, -7700886);
         ty += 11;
         this.fontRenderer.drawStringWithShadow("§8Missions refresh daily.", (float)tx, (float)ty, -7700886);
      }

      GL11.glDisable(3089);
      int totalContentH = ty + this.akatsukiAssignScrollOffset - ty;
      int visibleH = bottomLimit - ty;
      this.akatsukiAssignMaxScroll = Math.max(0, totalContentH - visibleH);
      if (this.akatsukiAssignScrollOffset > this.akatsukiAssignMaxScroll) {
         this.akatsukiAssignScrollOffset = this.akatsukiAssignMaxScroll;
      }

      if (this.akatsukiAssignMaxScroll > 0) {
         this.drawScrollBar(px + pw - 6, ty, visibleH, totalContentH, visibleH, this.akatsukiAssignScrollOffset, this.akatsukiAssignMaxScroll);
      }

   }

   private void drawAkatsukiBounties(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8 - this.contractScrollOffset;
      this.fontRenderer.drawStringWithShadow("§c§lBounty Board", (float)tx, (float)ty, -43691);
      ty += 16;
      List<AkatsukiClientData.BountyClientEntry> bounties = AkatsukiClientData.bounties;
      if (bounties.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No active bounties.", (float)tx, (float)ty, -7700886);
         ty += 14;
      } else {
         int maxDisplay = Math.min(bounties.size(), 5);

         for(int i = 0; i < maxDisplay; ++i) {
            AkatsukiClientData.BountyClientEntry b = (AkatsukiClientData.BountyClientEntry)bounties.get(i);
            int rowY = ty + i * 22;
            this.fontRenderer.drawStringWithShadow("§f" + b.targetName, (float)tx, (float)rowY, -2832216);
            String amountStr = "§6" + b.amount + " Ryo";
            this.fontRenderer.drawStringWithShadow(amountStr, (float)(tx + 120), (float)rowY, -22016);
            if (b.lastKnownRegion != null && !b.lastKnownRegion.isEmpty()) {
               this.fontRenderer.drawStringWithShadow("§8" + b.lastKnownRegion, (float)tx, (float)(rowY + 10), -7700886);
            }
         }

         ty += maxDisplay * 22;
      }

      List<AkatsukiClientData.ContractClientEntry> openContracts = AkatsukiClientData.openContracts;
      List<AkatsukiClientData.ContractClientEntry> assignedContracts = AkatsukiClientData.assignedContracts;
      if (!openContracts.isEmpty() || !assignedContracts.isEmpty()) {
         ty += 6;
         this.fontRenderer.drawStringWithShadow("§6§lContracts" + (openContracts.size() + assignedContracts.size() > 0 ? " §8(" + (openContracts.size() + assignedContracts.size()) + ")" : ""), (float)tx, (float)ty, -22016);
         ty += 14;
         int contractCount = assignedContracts.size() + openContracts.size();
         int entryHeight = 36;
         int totalContentHeight = contractCount * entryHeight;
         int visibleHeight = py + ph - (py + 8) - 5;
         int maxScroll = Math.max(0, totalContentHeight + 150 - visibleHeight);
         this.contractScrollOffset = Math.max(0, Math.min(this.contractScrollOffset, maxScroll));
         int scrolledTy = ty;
         int scaleFactor = (new ScaledResolution(this.mc)).getScaleFactor();
         int scissorX = px * scaleFactor;
         int scissorY = this.mc.displayHeight - (py + ph) * scaleFactor;
         int scissorW = pw * scaleFactor;
         int scissorH = (ph - (ty - py)) * scaleFactor;
         GL11.glEnable(3089);
         GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

         for(AkatsukiClientData.ContractClientEntry c : assignedContracts) {
            if (scrolledTy >= ty - 12 && scrolledTy < py + ph) {
               this.fontRenderer.drawStringWithShadow("§a[MINE] §f" + c.getTypeName() + " §8from " + c.posterName + (c.posterVillage.isEmpty() ? "" : " §7(" + c.posterVillage + ")"), (float)tx, (float)scrolledTy, -2832216);
               if (c.typeOrdinal == 0 && c.targetName != null && !c.targetName.isEmpty()) {
                  this.fontRenderer.drawStringWithShadow("  §7Target: §f" + c.targetName, (float)tx, (float)(scrolledTy + 11), -2832216);
               } else if (c.typeOrdinal == 1 && c.targetZoneId != null && !c.targetZoneId.isEmpty()) {
                  this.fontRenderer.drawStringWithShadow("  §7Zone: §f" + c.targetZoneId, (float)tx, (float)(scrolledTy + 11), -2832216);
               }

               this.fontRenderer.drawStringWithShadow("  §6" + c.ryoAmount + " Ryo", (float)tx, (float)(scrolledTy + 22), -22016);
            }

            scrolledTy += entryHeight;
         }

         for(int i = 0; i < openContracts.size(); ++i) {
            AkatsukiClientData.ContractClientEntry c = (AkatsukiClientData.ContractClientEntry)openContracts.get(i);
            if (scrolledTy >= ty - 12 && scrolledTy < py + ph) {
               this.fontRenderer.drawStringWithShadow("§e[OPEN] §f" + c.getTypeName() + " §8from " + c.posterName + (c.posterVillage.isEmpty() ? "" : " §7(" + c.posterVillage + ")"), (float)tx, (float)scrolledTy, -2832216);
               if (c.typeOrdinal == 0 && c.targetName != null && !c.targetName.isEmpty()) {
                  this.fontRenderer.drawStringWithShadow("  §7Target: §f" + c.targetName, (float)tx, (float)(scrolledTy + 11), -2832216);
               } else if (c.typeOrdinal == 1 && c.targetZoneId != null && !c.targetZoneId.isEmpty()) {
                  this.fontRenderer.drawStringWithShadow("  §7Zone: §f" + c.targetZoneId, (float)tx, (float)(scrolledTy + 11), -2832216);
               }

               this.fontRenderer.drawStringWithShadow("  §6" + c.ryoAmount + " Ryo", (float)tx, (float)(scrolledTy + 22), -22016);
            }

            scrolledTy += entryHeight;
         }

         GL11.glDisable(3089);
         if (maxScroll > 0) {
            String scrollHint = "§8↑↓ Scroll (" + (this.contractScrollOffset / entryHeight + 1) + "/" + contractCount + ")";
            this.fontRenderer.drawStringWithShadow(scrollHint, (float)tx, (float)(py + ph - 12), -7700886);
         }
      }

   }

   private void drawAkatsukiRing(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 6;
      AkatsukiRing ring = AkatsukiRing.getByIndex(AkatsukiClientData.ringOrdinal);
      String tokenStr = "§6Tokens: §f" + AkatsukiClientData.bountyTokens;
      int tokenW = this.fontRenderer.getStringWidth(tokenStr);
      this.fontRenderer.drawStringWithShadow(tokenStr, (float)(px + pw - tokenW - 10), (float)(ty + 2), -22016);
      this.fontRenderer.drawStringWithShadow("§c" + ring.kanji + " §7" + ring.romajiName + " §8(" + ring.englishName + ", " + ring.fingerPosition + ")", (float)tx, (float)ty, -2832216);
      ty += 14;

      for(int i = 0; i < 5; ++i) {
         int rowY = ty + i * 22;
         int level = AkatsukiClientData.ringUpgradeLevels[i];
         this.fontRenderer.drawStringWithShadow("§f" + RingUpgradeConstants.UPGRADE_NAMES[i], (float)tx, (float)rowY, -2832216);
         this.fontRenderer.drawStringWithShadow("§8" + RingUpgradeConstants.UPGRADE_DESCRIPTIONS[i], (float)tx, (float)(rowY + 10), -7700886);
         int dotX = tx + 130;

         for(int d = 0; d < 5; ++d) {
            int dotColor = d < level ? -43691 : -12962776;
            drawRect(dotX + d * 10, rowY + 1, dotX + d * 10 + 7, rowY + 8, dotColor);
         }
      }

   }

   private void drawAkatsukiRoster(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8;
      this.fontRenderer.drawStringWithShadow("§c§lRoster", (float)tx, (float)ty, -43691);
      ty += 16;
      List<AkatsukiClientData.RosterEntry> roster = AkatsukiClientData.roster;
      if (roster.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No members.", (float)tx, (float)ty, -7700886);
      } else {
         int maxDisplay = Math.min(roster.size(), (ph - 30) / 14);

         for(int i = 0; i < maxDisplay; ++i) {
            AkatsukiClientData.RosterEntry r = (AkatsukiClientData.RosterEntry)roster.get(i);
            int rowY = ty + i * 14;
            int dotColor = r.online ? -11141291 : -11184811;
            drawRect(tx, rowY + 2, tx + 5, rowY + 7, dotColor);
            this.fontRenderer.drawStringWithShadow("§c" + r.ringKanji, (float)(tx + 8), (float)rowY, -43691);
            boolean isPartner = r.name.equals(AkatsukiClientData.partnerName);
            String nameColor = isPartner ? "§6" : (r.online ? "§f" : "§8");
            this.fontRenderer.drawStringWithShadow(nameColor + r.name, (float)(tx + 22), (float)rowY, -2832216);
            this.fontRenderer.drawStringWithShadow("§8" + r.rankName, (float)(tx + 150), (float)rowY, -7700886);
         }

      }
   }

   private void drawAkatsukiReputation(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8;
      AkatsukiRank rank = AkatsukiRank.fromReputation(AkatsukiClientData.reputation);
      AkatsukiRank nextRank = rank.next();
      boolean isMax = nextRank == rank;
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)tx, (float)ty, 0.0F);
      GlStateManager.scale(1.5F, 1.5F, 1.0F);
      this.fontRenderer.drawStringWithShadow("§c" + rank.displayName, 0.0F, 0.0F, -43691);
      GlStateManager.popMatrix();
      ty += 22;
      int barW = pw - 40;
      int barH = 10;
      drawRect(tx, ty, tx + barW, ty + barH, -15068144);
      drawRect(tx, ty, tx + barW, ty + 1, -11910088);
      drawRect(tx, ty + barH - 1, tx + barW, ty + barH, -14015464);
      if (!isMax) {
         int repInRank = AkatsukiClientData.reputation - rank.repRequired;
         int repNeeded = nextRank.repRequired - rank.repRequired;
         float progress = repNeeded > 0 ? (float)repInRank / (float)repNeeded : 1.0F;
         progress = Math.min(1.0F, Math.max(0.0F, progress));
         int fillW = (int)(progress * (float)(barW - 2));
         drawRect(tx + 1, ty + 1, tx + 1 + fillW, ty + barH - 1, -859032781);
      } else {
         drawRect(tx + 1, ty + 1, tx + barW - 1, ty + barH - 1, -859032781);
      }

      ty += barH + 4;
      if (isMax) {
         this.fontRenderer.drawStringWithShadow("§f" + AkatsukiClientData.reputation + " §8(MAX RANK)", (float)tx, (float)ty, -2832216);
      } else {
         this.fontRenderer.drawStringWithShadow("§f" + AkatsukiClientData.reputation + " §8/ " + nextRank.repRequired + "  §7Next: §c" + nextRank.displayName, (float)tx, (float)ty, -2832216);
      }

      ty += 18;
      this.fontRenderer.drawStringWithShadow("§8Recent Activity:", (float)tx, (float)ty, -7700886);
      ty += 12;
      List<String> log = AkatsukiClientData.activityLog;
      int maxLines = Math.min(log.size(), (py + ph - ty - 4) / 11);

      for(int i = 0; i < maxLines; ++i) {
         this.fontRenderer.drawStringWithShadow("§7" + (String)log.get(i), (float)tx, (float)(ty + i * 11), -7700886);
      }

   }

   private void drawAkatsukiShop(int px, int py, int pw, int ph) {
      this.fontRenderer.drawStringWithShadow("§c§lBlack Market", (float)(px + 10), (float)(py + 4), -43691);
      int tokens = AkatsukiClientData.bountyTokens;
      String balanceStr = "§6Tokens: §e" + tokens;
      this.fontRenderer.drawStringWithShadow(balanceStr, (float)(px + pw - this.fontRenderer.getStringWidth(balanceStr) - 10), (float)(py + 4), -2832216);
      this.drawHorizontalLine(px + 5, px + pw - 5, py + 16, -9807296);
      if (this.selectedBlackMarketItemId != null) {
         BlackMarketItem selItem = BlackMarketRegistry.get(this.selectedBlackMarketItemId);
         String itemName = selItem != null ? selItem.getDisplayName() : this.selectedBlackMarketItemId;
         String header = "§eSelect target zone for: §f" + itemName;
         this.fontRenderer.drawStringWithShadow(header, (float)(px + 50), (float)(py + 20), -2832216);
         String colHeader;
         if (selItem != null && selItem.isDefensive()) {
            colHeader = "§8Showing Akatsuki-owned zones";
         } else {
            colHeader = "§8Showing non-Akatsuki zones";
         }

         this.fontRenderer.drawStringWithShadow(colHeader, (float)(px + 50), (float)(py + 32), -7700886);
         int zoneCount = TerritoryClientData.getInstance().getZoneStates().size();
         if (zoneCount == 0) {
            this.fontRenderer.drawStringWithShadow("§cNo territory zones synced — open Territory tab first,", (float)(px + 10), (float)(py + 50), -7700886);
            this.fontRenderer.drawStringWithShadow("§cor toggle territory mode to load zone data.", (float)(px + 10), (float)(py + 62), -7700886);
         }

      } else {
         List<BlackMarketItem> items = BlackMarketRegistry.getAll();
         int itemY = py + 20;
         int itemSpacing = 24;
         int maxVisible = Math.min(items.size() - this.blackMarketItemScrollOffset, (ph - 24) / itemSpacing);
         maxVisible = Math.min(maxVisible, 10);

         for(int i = 0; i < maxVisible; ++i) {
            int idx = i + this.blackMarketItemScrollOffset;
            if (idx >= items.size()) {
               break;
            }

            BlackMarketItem item = (BlackMarketItem)items.get(idx);
            int iy = itemY + i * itemSpacing;
            String typeColor = item.isOffensive() ? "§c" : (item.isDefensive() ? "§a" : (item.isBuff() ? "§b" : (item.isMassDeploy() ? "§d" : "§6")));
            this.fontRenderer.drawStringWithShadow(typeColor + item.getDisplayName(), (float)(px + 10), (float)iy, -2832216);
            String costStr = "§e" + item.getTokenCost() + " §6T";
            if (!item.isInstant()) {
               costStr = costStr + " §7| " + item.getDurationDisplay();
            }

            if (item.isRoaming()) {
               costStr = costStr + " §a[R]";
            }

            int costW = this.fontRenderer.getStringWidth(costStr);
            this.fontRenderer.drawStringWithShadow(costStr, (float)(px + pw - costW - 48), (float)iy, -7700886);
            String desc = "§8" + item.getDescription();
            if (this.fontRenderer.getStringWidth(desc) > pw - 60) {
               desc = this.fontRenderer.trimStringToWidth(desc, pw - 64) + "..";
            }

            this.fontRenderer.drawStringWithShadow(desc, (float)(px + 10), (float)(iy + 10), -7700886);
         }

         String activeStr = "§7Active deployments shown on Leader Panel";
         this.fontRenderer.drawStringWithShadow(activeStr, (float)(px + 10), (float)(py + ph - 12), -7700886);
      }
   }

   private void drawLeaderPanel(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 4;
      this.fontRenderer.drawStringWithShadow("§4§lLEADER", (float)tx, (float)ty, -5636096);
      ty += 16;
      int contentY = ty + 4;
      if (this.leaderMode == 0) {
         if (AkatsukiClientData.raidActive) {
            String villageCap = AkatsukiClientData.raidTargetVillage.isEmpty() ? "?" : AkatsukiClientData.raidTargetVillage.substring(0, 1).toUpperCase() + AkatsukiClientData.raidTargetVillage.substring(1);
            this.fontRenderer.drawStringWithShadow("§cRaid: §f" + villageCap, (float)tx, (float)contentY, -2832216);
            contentY += 10;
            long elapsed = System.currentTimeMillis() - AkatsukiClientData.raidSyncTime;
            long remain = Math.max(0L, AkatsukiClientData.raidTimeRemainingMs - elapsed);
            long secs = remain / 1000L;
            this.fontRenderer.drawStringWithShadow("§7Kills: §f" + AkatsukiClientData.raidKillsAchieved + "/" + AkatsukiClientData.raidKillsRequired + "  §7Time: §e" + String.format("%d:%02d", secs / 60L, secs % 60L), (float)(tx + 4), (float)contentY, -2832216);
         } else if (this.leaderRaidSelecting) {
            this.fontRenderer.drawStringWithShadow("§eSelect target:", (float)tx, (float)contentY, -1521552);
         } else {
            this.fontRenderer.drawStringWithShadow("§8No active raid.", (float)tx, (float)contentY, -7700886);
         }
      } else if (this.leaderMode == 1) {
         this.fontRenderer.drawStringWithShadow("§7Type:", (float)tx, (float)(contentY + 2), -7700886);
         contentY += 16;
         this.fontRenderer.drawStringWithShadow("§7Tokens:", (float)tx, (float)(contentY + 2), -7700886);
         String tkStr = "" + this.leaderMissionTokens;
         int tkW = this.fontRenderer.getStringWidth(tkStr);
         this.fontRenderer.drawStringWithShadow("§6" + tkStr, (float)(px + 73 + (24 - tkW) / 2), (float)(contentY + 2), -22016);
         contentY += 14;
         this.fontRenderer.drawStringWithShadow("§7Rep:", (float)tx, (float)(contentY + 2), -7700886);
         String rpStr = "" + this.leaderMissionRep;
         int rpW = this.fontRenderer.getStringWidth(rpStr);
         this.fontRenderer.drawStringWithShadow("§c" + rpStr, (float)(px + 73 + (24 - rpW) / 2), (float)(contentY + 2), -43691);
         contentY += 14;
         String targetName = this.leaderSelectedMember1 >= 0 ? this.leaderGetSelectedName() : "§8(select below)";
         this.fontRenderer.drawStringWithShadow("§7To: §e" + targetName, (float)tx, (float)contentY, -2832216);
      } else if (this.leaderMode == 2) {
         this.fontRenderer.drawStringWithShadow("§7Amount:", (float)tx, (float)(contentY + 2), -7700886);
         String amtStr = this.leaderBountyAmount + " ryo";
         int amtW = this.fontRenderer.getStringWidth(amtStr);
         this.fontRenderer.drawStringWithShadow("§6" + amtStr, (float)(px + 73 + (38 - amtW) / 2), (float)(contentY + 2), -22016);
         contentY += 16;
         String targetName = this.leaderSelectedMember1 >= 0 ? this.leaderGetSelectedName() : "§8(select below)";
         this.fontRenderer.drawStringWithShadow("§7Target: §e" + targetName, (float)tx, (float)contentY, -2832216);
      }

      int memberHeaderY = py + ph / 2 - 2;
      this.drawHorizontalLine(px + 4, px + pw - 4, memberHeaderY - 2, -11919328);
      String hdrLabel = this.leaderMode == 0 ? "§8Members" : "§8Online Players";
      this.fontRenderer.drawStringWithShadow(hdrLabel, (float)tx, (float)memberHeaderY, -7700886);
   }

   private List<String> leaderGetSelectableNames() {
      if (this.leaderMode != 0 && this.leaderMode != 1) {
         List<String> names = new ArrayList();
         if (this.mc.getConnection() != null) {
            for(NetworkPlayerInfo info : this.mc.getConnection().getPlayerInfoMap()) {
               if (info.getGameProfile() != null && info.getGameProfile().getName() != null) {
                  names.add(info.getGameProfile().getName());
               }
            }
         }

         Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
         return names;
      } else {
         List<String> names = new ArrayList();

         for(AkatsukiClientData.RosterEntry r : AkatsukiClientData.roster) {
            names.add(r.name);
         }

         return names;
      }
   }

   private String leaderGetSelectedName() {
      List<String> names = this.leaderGetSelectableNames();
      return this.leaderSelectedMember1 >= 0 && this.leaderSelectedMember1 < names.size() ? (String)names.get(this.leaderSelectedMember1) : "";
   }

   private List<String> getOnlinePlayerNames() {
      List<String> names = new ArrayList();
      if (this.mc.getConnection() != null) {
         for(NetworkPlayerInfo info : this.mc.getConnection().getPlayerInfoMap()) {
            if (info.getGameProfile() != null && info.getGameProfile().getName() != null) {
               names.add(info.getGameProfile().getName());
            }
         }
      }

      Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
      return names;
   }

   private void drawVillageThreats(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8;
      if (AkatsukiClientData.raidActive && AkatsukiClientData.raidIsMyVillage) {
         drawRect(px + 2, ty - 2, px + pw - 2, ty + 26, -863502336);
         String raidBanner = "§c§l!! YOUR VILLAGE IS UNDER ATTACK !!";
         int rbw = this.fontRenderer.getStringWidth(raidBanner);
         this.fontRenderer.drawStringWithShadow(raidBanner, (float)(px + (pw - rbw) / 2), (float)ty, -43691);
         ty += 13;
         this.fontRenderer.drawStringWithShadow("§7Rally to defend! The Akatsuki are raiding!", (float)(tx + 4), (float)ty, -2832216);
         ty += 20;
      }

      if (AkatsukiClientData.criticalThreat) {
         String banner = "§c§l!! CRITICAL THREAT !!";
         int bw = this.fontRenderer.getStringWidth(banner);
         drawRect(px + 2, ty - 2, px + pw - 2, ty + 12, -1436155904);
         this.fontRenderer.drawStringWithShadow(banner, (float)(px + (pw - bw) / 2), (float)ty, -43691);
         ty += 18;
      }

      this.fontRenderer.drawStringWithShadow("§e§lThreat Level", (float)tx, (float)ty, -1521552);
      ty += 14;
      int barW = pw - 40;
      int barH = 12;
      drawRect(tx, ty, tx + barW, ty + barH, -15068144);
      drawRect(tx, ty, tx + barW, ty + 1, -11910088);
      int total = AkatsukiClientData.totalZoneCount > 0 ? AkatsukiClientData.totalZoneCount : 36;
      float ratio = total > 0 ? (float)AkatsukiClientData.akatsukiZoneCount / (float)total : 0.0F;
      ratio = Math.min(1.0F, Math.max(0.0F, ratio));
      int fillW = (int)(ratio * (float)(barW - 2));
      int barColor;
      if (ratio < 0.25F) {
         barColor = -869029325;
      } else if (ratio < 0.5F) {
         barColor = -861230541;
      } else if (ratio < 0.75F) {
         barColor = -859019725;
      } else {
         barColor = -859032781;
      }

      if (fillW > 0) {
         drawRect(tx + 1, ty + 1, tx + 1 + fillW, ty + barH - 1, barColor);
      }

      ty += barH + 4;
      this.fontRenderer.drawStringWithShadow("§7Zones controlled: §c" + AkatsukiClientData.akatsukiZoneCount + "§8/" + total, (float)tx, (float)ty, -2832216);
      ty += 18;
      this.fontRenderer.drawStringWithShadow("§8Known Akatsuki Members:", (float)tx, (float)ty, -7700886);
      ty += 12;
      List<AkatsukiClientData.RosterEntry> roster = AkatsukiClientData.roster;
      if (roster.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No intelligence available.", (float)tx, (float)ty, -7700886);
      } else {
         int maxDisplay = Math.min(roster.size(), (py + ph - ty - 4) / 12);

         for(int i = 0; i < maxDisplay; ++i) {
            AkatsukiClientData.RosterEntry r = (AkatsukiClientData.RosterEntry)roster.get(i);
            int dotColor = r.online ? -43691 : -11184811;
            drawRect(tx, ty + 2, tx + 5, ty + 7, dotColor);
            this.fontRenderer.drawStringWithShadow((r.online ? "§c" : "§8") + r.name + " §8[" + r.rankName + "]", (float)(tx + 8), (float)ty, -7700886);
            ty += 12;
         }
      }

   }

   private void drawVillageBounties(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8;
      this.fontRenderer.drawStringWithShadow("§e§lBounty Board", (float)tx, (float)ty, -1521552);
      ty += 16;
      List<AkatsukiClientData.BountyClientEntry> bounties = AkatsukiClientData.bounties;
      if (bounties.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No active bounties on Akatsuki members.", (float)tx, (float)ty, -7700886);
      } else {
         int maxDisplay = Math.min(bounties.size(), (ph - 30) / 22);

         for(int i = 0; i < maxDisplay; ++i) {
            AkatsukiClientData.BountyClientEntry b = (AkatsukiClientData.BountyClientEntry)bounties.get(i);
            int rowY = ty + i * 22;
            this.fontRenderer.drawStringWithShadow("§c" + b.targetName, (float)tx, (float)rowY, -43691);
            this.fontRenderer.drawStringWithShadow("§6" + b.amount + " ryo", (float)(tx + 120), (float)rowY, -22016);
            if (b.lastKnownRegion != null && !b.lastKnownRegion.isEmpty()) {
               this.fontRenderer.drawStringWithShadow("§8Last seen: " + b.lastKnownRegion, (float)tx, (float)(rowY + 10), -7700886);
            }
         }

      }
   }

   private void drawVillageLog(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8;
      this.fontRenderer.drawStringWithShadow("§e§lActivity Log", (float)tx, (float)ty, -1521552);
      ty += 16;
      List<String> log = AkatsukiClientData.activityLog;
      if (log.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§8No recent activity.", (float)tx, (float)ty, -7700886);
      } else {
         int maxLines = Math.min(log.size(), (py + ph - ty - 4) / 11);

         for(int i = 0; i < maxLines; ++i) {
            this.fontRenderer.drawStringWithShadow("§7" + (String)log.get(i), (float)tx, (float)(ty + i * 11), -7700886);
         }

      }
   }

   private void drawVillageHunter(int px, int py, int pw, int ph) {
      int tx = px + 10;
      int ty = py + 8;
      this.fontRenderer.drawStringWithShadow("§6§lHire the Akatsuki", (float)tx, (float)ty, -22016);
      ty += 16;
      List<AkatsukiClientData.ContractClientEntry> myContracts = AkatsukiClientData.myContracts;
      if (myContracts.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§7Post a contract for Akatsuki mercenaries.", (float)tx, (float)ty, -7700886);
         ty += 14;
         this.fontRenderer.drawStringWithShadow("§eType:", (float)tx, (float)(ty + 18), -2832216);
         int targetRowY = ty + 38;
         if (this.contractTypeIndex == 0) {
            this.fontRenderer.drawStringWithShadow("§eTarget:", (float)tx, (float)targetRowY, -2832216);
            List<String> onlinePlayers = this.getOnlinePlayerNames();
            String playerName = "§8(none online)";
            if (!onlinePlayers.isEmpty()) {
               if (this.contractTargetPlayerIndex >= onlinePlayers.size()) {
                  this.contractTargetPlayerIndex = 0;
               }

               playerName = "§f" + (String)onlinePlayers.get(this.contractTargetPlayerIndex);
            }

            int pnW = this.fontRenderer.getStringWidth(playerName);
            int selectorCenterX = tx + 100 + 35;
            this.fontRenderer.drawStringWithShadow(playerName, (float)(selectorCenterX - pnW / 2), (float)(targetRowY + 2), -2832216);
         } else {
            this.fontRenderer.drawStringWithShadow("§eZone:", (float)tx, (float)targetRowY, -2832216);
            String zoneName = "§fZone " + (this.contractTargetZoneIndex + 1);
            int znW = this.fontRenderer.getStringWidth(zoneName);
            int selectorCenterX = tx + 100 + 35;
            this.fontRenderer.drawStringWithShadow(zoneName, (float)(selectorCenterX - znW / 2), (float)(targetRowY + 2), -2832216);
         }

         int ryoRowY = targetRowY + 20;
         this.fontRenderer.drawStringWithShadow("§eRyo:", (float)tx, (float)ryoRowY, -2832216);
         String ryoStr = "§6" + this.contractRyoAmount;
         int ryoW = this.fontRenderer.getStringWidth(ryoStr);
         int ryoCenterX = tx + 80 + 45;
         this.fontRenderer.drawStringWithShadow(ryoStr, (float)(ryoCenterX - ryoW / 2), (float)(ryoRowY + 2), -22016);
         ty = ryoRowY + 36;
         this.fontRenderer.drawStringWithShadow("§8Ryo is held in escrow until completion.", (float)tx, (float)ty, -7700886);
         ty += 11;
         this.fontRenderer.drawStringWithShadow("§8Two Akatsuki members will be assigned.", (float)tx, (float)ty, -7700886);
         ty += 11;
         this.fontRenderer.drawStringWithShadow("§8Contracts expire after 48 hours.", (float)tx, (float)ty, -7700886);
      } else {
         AkatsukiClientData.ContractClientEntry c = (AkatsukiClientData.ContractClientEntry)myContracts.get(0);
         this.fontRenderer.drawStringWithShadow("§eActive Contract", (float)tx, (float)ty, -1521552);
         ty += 14;
         String statusColor = c.statusOrdinal == 0 ? "§e" : "§a";
         this.fontRenderer.drawStringWithShadow("§7Type: §f" + c.getTypeName(), (float)tx, (float)ty, -2832216);
         ty += 11;
         if (c.typeOrdinal == 0 && c.targetName != null && !c.targetName.isEmpty()) {
            this.fontRenderer.drawStringWithShadow("§7Target: §f" + c.targetName, (float)tx, (float)ty, -2832216);
            ty += 11;
         } else if (c.typeOrdinal == 1 && c.targetZoneId != null && !c.targetZoneId.isEmpty()) {
            this.fontRenderer.drawStringWithShadow("§7Zone: §f" + c.targetZoneId, (float)tx, (float)ty, -2832216);
            ty += 11;
         }

         this.fontRenderer.drawStringWithShadow("§7Ryo: §6" + c.ryoAmount, (float)tx, (float)ty, -22016);
         ty += 11;
         this.fontRenderer.drawStringWithShadow("§7Status: " + statusColor + c.getStatusName(), (float)tx, (float)ty, -2832216);
         ty += 14;
         this.fontRenderer.drawStringWithShadow("§7Assigned:", (float)tx, (float)ty, -7700886);
         ty += 11;
         String a1 = c.assignee1 != null && !c.assignee1.isEmpty() ? c.assignee1 : "§8(waiting)";
         String a2 = c.assignee2 != null && !c.assignee2.isEmpty() ? c.assignee2 : "§8(waiting)";
         this.fontRenderer.drawStringWithShadow("  §f" + a1, (float)tx, (float)ty, -2832216);
         ty += 11;
         this.fontRenderer.drawStringWithShadow("  §f" + a2, (float)tx, (float)ty, -2832216);
      }

   }

   private void drawFactionComingSoon(int px, int py, int pw, int ph) {
      String[] villTabNames = new String[]{"Threats", "Bounties", "Hunter", "Defense", "Rally", "Log"};
      String tabName = this.factionSubTab < villTabNames.length ? villTabNames[this.factionSubTab] : "Unknown";
      String text = "§7" + tabName + " §8— Coming Soon";
      int tw = this.fontRenderer.getStringWidth(text);
      this.fontRenderer.drawStringWithShadow(text, (float)(px + (pw - tw) / 2), (float)(py + ph / 2 - 4), -7700886);
   }

   private static int getPveRankColor(PveRank rank) {
      switch (rank) {
         case GENIN:
            return -11141291;
         case CHUNIN:
            return -11141121;
         case JONIN:
            return -171;
         case ANBU:
            return -43521;
         case KAGE:
            return -22016;
         default:
            return -5592406;
      }
   }
}
