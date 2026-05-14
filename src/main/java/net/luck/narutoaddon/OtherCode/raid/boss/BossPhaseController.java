
package net.luck.narutoaddon.OtherCode.raid.boss;

import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossPhaseController {
   private final EntityLivingBase bossEntity;
   private final IRaidBoss boss;
   private final List<BossPhase> phases = new ArrayList();
   private int currentPhaseIndex = 0;
   private long phaseStartTime;
   private int phaseTicks = 0;
   private BossMechanic currentMechanic;
   private int mechanicIndex = 0;
   private int mechanicWarningTicks = 0;
   private int mechanicActiveTicks = 0;
   private MechanicState mechanicState;
   private int lightMechanicsSinceHeavy;
   private BossMechanic lastMechanic;
   private int mechanicCooldown;
   private static final int BASE_MIN_COOLDOWN = 60;
   private static final int BASE_MAX_COOLDOWN = 100;
   private static final int BASE_MIN_LIGHT_BEFORE_HEAVY = 2;

   private int getMinMechanicCooldown() {
      if (this.boss != null && this.boss.getRaidInstance() != null) {
         int diffOrdinal = this.boss.getRaidInstance().getDifficulty().ordinal();
         return Math.max(20, 60 - diffOrdinal * 20);
      } else {
         return 60;
      }
   }

   private int getMaxMechanicCooldown() {
      if (this.boss != null && this.boss.getRaidInstance() != null) {
         int diffOrdinal = this.boss.getRaidInstance().getDifficulty().ordinal();
         return Math.max(40, 100 - diffOrdinal * 30);
      } else {
         return 100;
      }
   }

   public BossPhaseController(EntityRaidBoss boss) {
      this.mechanicState = MechanicState.IDLE;
      this.lightMechanicsSinceHeavy = 0;
      this.lastMechanic = null;
      this.mechanicCooldown = 0;
      this.bossEntity = boss;
      this.boss = boss;
      this.phaseStartTime = System.currentTimeMillis();
   }

   public BossPhaseController(EntityLivingBase entity) {
      this.mechanicState = MechanicState.IDLE;
      this.lightMechanicsSinceHeavy = 0;
      this.lastMechanic = null;
      this.mechanicCooldown = 0;
      this.bossEntity = entity;
      if (entity instanceof IRaidBoss) {
         this.boss = (IRaidBoss)entity;
         this.phaseStartTime = System.currentTimeMillis();
      } else {
         throw new IllegalArgumentException("Entity must implement IRaidBoss");
      }
   }

   public void addPhase(BossPhase phase) {
      this.phases.add(phase);
      this.phases.sort((a, b) -> Float.compare(b.getHealthThresholdPercent(), a.getHealthThresholdPercent()));
   }

   public void clearPhases() {
      this.phases.clear();
      this.currentPhaseIndex = 0;
   }

   public void tick(RaidInstance raid) {
      if (!this.phases.isEmpty()) {
         ++this.phaseTicks;
         this.checkPhaseTransition();
         BossPhase currentPhase = this.getCurrentPhase();
         if (currentPhase.hasDuration() && this.phaseTicks >= currentPhase.getDuration()) {
            this.advancePhase(raid);
         }

         this.tickMechanics(raid);
      }
   }

   private void checkPhaseTransition() {
      float healthPercent = this.bossEntity.getHealth() / this.bossEntity.getMaxHealth() * 100.0F;
      int targetPhase = this.currentPhaseIndex;

      for(int i = this.currentPhaseIndex; i < this.phases.size(); ++i) {
         BossPhase phase = (BossPhase)this.phases.get(i);
         if (healthPercent <= phase.getHealthThresholdPercent()) {
            targetPhase = i;
         }
      }

      if (targetPhase > this.currentPhaseIndex) {
         this.transitionToPhase(targetPhase);
      }

   }

   private void transitionToPhase(int newIndex) {
      if (newIndex >= 0 && newIndex < this.phases.size()) {
         System.out.println("[BossPhaseController] Transitioning from phase " + (this.currentPhaseIndex + 1) + " to phase " + (newIndex + 1) + " (health: " + this.bossEntity.getHealth() / this.bossEntity.getMaxHealth() * 100.0F + "%)");
         BossPhase oldPhase = this.getCurrentPhase();
         int oldIndex = this.currentPhaseIndex;
         if (oldPhase != null) {
            oldPhase.onPhaseEnd();
         }

         if (this.currentMechanic != null && this.mechanicState == MechanicState.ACTIVE) {
            this.currentMechanic.onInterrupt(this.bossEntity, this.boss.getRaidInstance());
         }

         this.currentMechanic = null;
         this.mechanicState = MechanicState.IDLE;
         this.mechanicIndex = 0;
         this.mechanicCooldown = this.getMinMechanicCooldown();
         this.currentPhaseIndex = newIndex;
         BossPhase newPhase = this.getCurrentPhase();
         this.phaseTicks = 0;
         this.phaseStartTime = System.currentTimeMillis();
         if (newPhase != null) {
            newPhase.onPhaseStart();
            this.boss.onPhaseTransition(oldIndex + 1, newIndex + 1, oldPhase, newPhase);
         }

      }
   }

   private void advancePhase(RaidInstance raid) {
      if (this.currentPhaseIndex + 1 < this.phases.size()) {
         this.transitionToPhase(this.currentPhaseIndex + 1);
      }

   }

   public void forcePhase(int phaseNumber) {
      if (phaseNumber >= 1 && phaseNumber <= this.phases.size()) {
         this.transitionToPhase(phaseNumber - 1);
      }

   }

   private void tickMechanics(RaidInstance raid) {
      BossPhase currentPhase = this.getCurrentPhase();
      if (currentPhase != null && !currentPhase.getMechanics().isEmpty()) {
         int enrageLevel = raid != null ? raid.getEnrageLevel() : 0;
         switch (this.mechanicState) {
            case IDLE:
               this.tickIdleState(currentPhase, raid, enrageLevel);
               break;
            case WARNING:
               this.tickWarningState(raid, enrageLevel);
               break;
            case ACTIVE:
               this.tickActiveState(raid, enrageLevel);
               break;
            case ENDING:
               this.tickEndingState(raid);
         }

      }
   }

   private void tickIdleState(BossPhase currentPhase, RaidInstance raid, int enrageLevel) {
      if (this.mechanicCooldown > 0) {
         --this.mechanicCooldown;
      } else {
         List<BossMechanic> mechanics = currentPhase.getMechanics();
         if (!mechanics.isEmpty()) {
            if (currentPhase.getType() == BossPhase.PhaseType.IMMUNITY) {
               System.out.println("[BossPhaseController] IMMUNITY phase - selecting mechanic from " + mechanics.size() + " mechanics. Phase: " + currentPhase.getName());
            }

            this.currentMechanic = this.selectWeightedMechanic(mechanics, raid);
            if (this.currentMechanic != null) {
               System.out.println("[BossPhaseController] Selected mechanic: " + this.currentMechanic.getName() + " (heavy=" + this.currentMechanic.isHeavyMechanic() + ", phase type=" + currentPhase.getType() + ")");
               if (this.currentMechanic.isHeavyMechanic()) {
                  this.lightMechanicsSinceHeavy = 0;
               } else {
                  ++this.lightMechanicsSinceHeavy;
               }

               this.lastMechanic = this.currentMechanic;
               int baseWarning = this.currentMechanic.getWarningTicks();
               int reduction = this.currentMechanic.getEnrageWarningReduction(enrageLevel);
               this.mechanicWarningTicks = Math.max(20, baseWarning - reduction);
               this.mechanicState = MechanicState.WARNING;
               if (raid != null) {
                  raid.broadcastMessage("§e§l[WARNING] " + this.currentMechanic.getWarningMessage());
               }
            }
         }

      }
   }

   private BossMechanic selectWeightedMechanic(List<BossMechanic> mechanics, RaidInstance raid) {
      if (mechanics.isEmpty()) {
         return null;
      } else if (mechanics.size() == 1) {
         return (BossMechanic)mechanics.get(0);
      } else {
         Random rand = this.bossEntity.world.rand;
         BossPhase currentPhase = this.getCurrentPhase();
         boolean isImmunityPhase = currentPhase != null && currentPhase.getType() == BossPhase.PhaseType.IMMUNITY;
         int minLightBeforeHeavy = 2;
         if (raid != null && raid.getDifficulty() != null) {
            minLightBeforeHeavy += raid.getDifficulty().ordinal();
         }

         List<BossMechanic> eligibleMechanics = new ArrayList();
         List<Integer> weights = new ArrayList();
         int totalWeight = 0;

         for(BossMechanic mechanic : mechanics) {
            if ((mechanic != this.lastMechanic || mechanics.size() <= 1) && (!mechanic.isHeavyMechanic() || isImmunityPhase || this.lightMechanicsSinceHeavy >= minLightBeforeHeavy)) {
               int weight = mechanic.getSelectionWeight();
               if (!mechanic.isHeavyMechanic() && raid != null && raid.getDifficulty() != null) {
                  weight = (int)((float)weight * (1.0F + (float)raid.getDifficulty().ordinal() * 0.25F));
               }

               eligibleMechanics.add(mechanic);
               weights.add(weight);
               totalWeight += weight;
            }
         }

         if (eligibleMechanics.isEmpty()) {
            eligibleMechanics.addAll(mechanics);

            for(BossMechanic mechanic : mechanics) {
               int weight = mechanic.getSelectionWeight();
               weights.add(weight);
               totalWeight += weight;
            }
         }

         if (totalWeight <= 0) {
            return (BossMechanic)mechanics.get(rand.nextInt(mechanics.size()));
         } else {
            int roll = rand.nextInt(totalWeight);
            int cumulative = 0;

            for(int i = 0; i < eligibleMechanics.size(); ++i) {
               cumulative += (Integer)weights.get(i);
               if (roll < cumulative) {
                  return (BossMechanic)eligibleMechanics.get(i);
               }
            }

            return (BossMechanic)eligibleMechanics.get(eligibleMechanics.size() - 1);
         }
      }
   }

   private void tickWarningState(RaidInstance raid, int enrageLevel) {
      --this.mechanicWarningTicks;
      if (this.mechanicWarningTicks <= 0) {
         this.mechanicState = MechanicState.ACTIVE;
         this.mechanicActiveTicks = 0;
         if (this.currentMechanic != null) {
            this.currentMechanic.onStart(this.bossEntity, raid);
            if (!this.currentMechanic.allowsDamage()) {
               this.boss.setDamageImmune(true, this.currentMechanic.getDisplayName());
            }
         }
      }

   }

   private void tickActiveState(RaidInstance raid, int enrageLevel) {
      if (this.currentMechanic == null) {
         this.mechanicState = MechanicState.IDLE;
      } else {
         this.currentMechanic.onTick(this.bossEntity, raid, this.mechanicActiveTicks);
         ++this.mechanicActiveTicks;
         int duration = this.currentMechanic.getDurationTicks();
         boolean done = duration >= 0 && this.mechanicActiveTicks >= duration;
         if (this.currentMechanic.isPuzzleMechanic() && this.currentMechanic.isPuzzleComplete(this.bossEntity, raid)) {
            done = true;
            System.out.println("[BossPhaseController] Puzzle mechanic complete! Transitioning to ENDING state.");
            if (raid != null) {
               raid.broadcastMessage("§a§l[PUZZLE COMPLETE] Damage window open!");
            }

            this.boss.setDamageImmune(false, (String)null);
            System.out.println("[BossPhaseController] Cleared boss damage immunity.");
         }

         if (done) {
            this.mechanicState = MechanicState.ENDING;
         }

      }
   }

   private void tickEndingState(RaidInstance raid) {
      if (this.currentMechanic != null) {
         this.currentMechanic.onEnd(this.bossEntity, raid);
         this.boss.setDamageImmune(false, (String)null);
      }

      this.currentMechanic = null;
      this.mechanicState = MechanicState.IDLE;
      Random rand = this.bossEntity.world.rand;
      int minCooldown = this.getMinMechanicCooldown();
      int maxCooldown = this.getMaxMechanicCooldown();
      this.mechanicCooldown = minCooldown + rand.nextInt(maxCooldown - minCooldown);
   }

   public BossPhase getCurrentPhase() {
      return this.currentPhaseIndex >= 0 && this.currentPhaseIndex < this.phases.size() ? (BossPhase)this.phases.get(this.currentPhaseIndex) : null;
   }

   public int getCurrentPhaseNumber() {
      return this.currentPhaseIndex + 1;
   }

   public String getCurrentPhaseName() {
      BossPhase phase = this.getCurrentPhase();
      return phase != null ? phase.getName() : "Unknown";
   }

   public int getPhaseCount() {
      return this.phases.size();
   }

   public int getTotalPhases() {
      return this.phases.size();
   }

   public BossMechanic getCurrentMechanic() {
      return this.currentMechanic;
   }

   public MechanicState getMechanicState() {
      return this.mechanicState;
   }

   public boolean isBossImmune() {
      BossPhase phase = this.getCurrentPhase();
      if (phase != null && phase.isBossImmune()) {
         return true;
      } else if (this.currentMechanic != null && this.mechanicState == MechanicState.ACTIVE) {
         return !this.currentMechanic.allowsDamage();
      } else {
         return false;
      }
   }

   public int getPhaseTicks() {
      return this.phaseTicks;
   }

   public List<BossPhase> getPhases() {
      return this.phases;
   }

   public static enum MechanicState {
      IDLE,
      WARNING,
      ACTIVE,
      ENDING;
   }
}
