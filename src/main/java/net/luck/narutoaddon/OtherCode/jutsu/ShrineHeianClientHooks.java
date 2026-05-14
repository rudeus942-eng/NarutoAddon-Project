package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.luckAddonAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.PlayerRender;
import net.narutomod.item.ItemJutsu;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.procedure.ProcedureUtils.Vec2f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class ShrineHeianClientHooks {
   private static final ShrineModelHeianEraBody BODY_MODEL = new ShrineModelHeianEraBody();
   private static final ShrineModelHeianEraBody FIRST_PERSON_ARM_MODEL = new ShrineModelHeianEraBody();
   private static final Map<Integer, Integer> LAST_PARTICLE_TICK = new HashMap<>();
   private static final Set<Integer> SCALED_RENDERERS = new HashSet<>();

   private static boolean jumpHeld;
   private static boolean jumpedFromGround;
   private static boolean jumpReleasedMidair;
   private static boolean wasOnGround;
   private static boolean useHeld;

   public static ModelBiped getBodyModel(ModelBiped defaultModel) {
      BODY_MODEL.setModelAttributes(defaultModel);
      BODY_MODEL.restoreFullVisibility();
      return BODY_MODEL;
   }

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase != Phase.END) return;

      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null && mc.world != null) {
         mc.player.eyeHeight = EntityShrineHeianEraTransformation.isActive(mc.player) ?
                 mc.player.getDefaultEyeHeight() * 1.5F : mc.player.getDefaultEyeHeight();

         if (EntityShrineHeianEraTransformation.isActive(mc.player)) {
            // FIX: Rimosso il parametro "2" che causava errore (image_a0c2a5.png)
            PlayerRender.hideBodyPart(mc.player, EntityShrineHeianEraTransformation.HIDDEN_PARTS);
         }

         // Logica salto...
         handleJumpLogic(mc);

         // Logica disattivazione...
         handleDeactivation(mc);
      }
   }

   private void handleJumpLogic(Minecraft mc) {
      boolean onGround = mc.player.onGround;
      KeyBinding jump = mc.gameSettings.keyBindJump;
      boolean pressed = jump.isKeyDown();

      if (onGround) {
         jumpedFromGround = pressed;
         jumpReleasedMidair = false;
      } else {
         if (wasOnGround) jumpedFromGround = jumpHeld;
         if (jumpedFromGround && !pressed) jumpReleasedMidair = true;
      }

      if (pressed && !jumpHeld && EntityShrineHeianEraTransformation.isActive(mc.player) && !onGround && jumpedFromGround && jumpReleasedMidair
              && !mc.player.capabilities.isFlying && !mc.player.isInWater() && !mc.player.isOnLadder()) {
         luckAddonAddon.PACKET_HANDLER.sendToServer(new EntityShrineHeianEraTransformation.DoubleJumpMessage());
         jumpReleasedMidair = false;
      }
      jumpHeld = pressed;
      wasOnGround = onGround;
   }

   private void handleDeactivation(Minecraft mc) {
      KeyBinding useItem = mc.gameSettings.keyBindUseItem;
      boolean usePressed = useItem.isKeyDown();
      if ((useItem.isPressed() || (usePressed && !useHeld)) && EntityShrineHeianEraTransformation.isActive(mc.player)
              && mc.player.isSneaking() && isHoldingSelectedHeian(mc.player)) {
         luckAddonAddon.PACKET_HANDLER.sendToServer(new EntityShrineHeianEraTransformation.DeactivateMessage());
      }
      useHeld = usePressed;
   }

   private static boolean isHoldingSelectedHeian(EntityPlayer player) {
      return isSelectedHeian(player.getHeldItemMainhand()) || isSelectedHeian(player.getHeldItemOffhand());
   }

   private static boolean isSelectedHeian(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof ItemShrineRelease.RangedItem) {
         try {
            // FIX: Risoluzione "protected access" tramite Reflection (image_a0be87.png)
            java.lang.reflect.Method method = ItemJutsu.Base.class.getDeclaredMethod("getCurrentJutsu", ItemStack.class);
            method.setAccessible(true);
            ItemJutsu.JutsuEnum current = (ItemJutsu.JutsuEnum) method.invoke(stack.getItem(), stack);
            return current != null && current.index == ItemShrineRelease.HEIAN_ERA_TRANSFORMATION.index;
         } catch (Exception e) {
            return false;
         }
      }
      return false;
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
      EntityPlayer player = event.getEntityPlayer();
      if (EntityShrineHeianEraTransformation.isActive(player) && !player.isSpectator()) {
         PlayerRender.hideBodyPart(player, EntityShrineHeianEraTransformation.HIDDEN_PARTS);
         setVanillaPlayerModelVisible(event.getRenderer(), false);

         GlStateManager.pushMatrix();
         GlStateManager.translate(event.getX(), event.getY(), event.getZ());
         GlStateManager.scale(1.5F, 1.5F, 1.5F);
         GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
         SCALED_RENDERERS.add(player.getEntityId());
      }
   }

   @SubscribeEvent
   public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
      if (SCALED_RENDERERS.remove(event.getEntityPlayer().getEntityId())) {
         try {
            this.spawnRenderFistParticles(event, event.getEntityPlayer());
         } finally {
            GlStateManager.popMatrix();
            setVanillaPlayerModelVisible(event.getRenderer(), true);
         }
      }
   }

   @SubscribeEvent
   public void onRenderSpecificHand(RenderSpecificHandEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player instanceof AbstractClientPlayer && mc.gameSettings.thirdPersonView == 0) {
         if (EntityShrineHeianEraTransformation.isActive(mc.player)) {
            if (event.getHand() == EnumHand.MAIN_HAND) {
               Render<?> renderer = mc.getRenderManager().getEntityRenderObject(mc.player);
               if (renderer instanceof RenderPlayer) {
                  renderFirstPersonHeianArm(mc.player, (RenderPlayer)renderer, mc.player.getPrimaryHand(), event.getEquipProgress(), event.getSwingProgress(), event.getPartialTicks());
                  event.setCanceled(true);
               }
            } else {
               event.setCanceled(true);
            }
         }
      }
   }

   private static void setVanillaPlayerModelVisible(RenderPlayer renderer, boolean visible) {
      ModelPlayer model = renderer.getMainModel();
      model.bipedHead.showModel = visible;
      model.bipedBody.showModel = visible;
      model.bipedRightArm.showModel = visible;
      model.bipedLeftArm.showModel = visible;
      model.bipedRightLeg.showModel = visible;
      model.bipedLeftLeg.showModel = visible;
   }

   private static void renderFirstPersonHeianArm(EntityPlayerSP player, RenderPlayer renderer, EnumHandSide side, float equip, float swing, float partial) {
      GlStateManager.pushMatrix();
      applyFirstPersonArmTransform(player, side, equip, swing, partial);
      Minecraft.getMinecraft().getTextureManager().bindTexture(EntityShrineHeianEraTransformation.BODY_TEXTURE);
      FIRST_PERSON_ARM_MODEL.renderFirstPersonArm(player, renderer.getMainModel(), side, 0.0625F);
      GlStateManager.popMatrix();
   }

   private static void applyFirstPersonArmTransform(EntityPlayerSP player, EnumHandSide side, float equip, float swing, float partial) {
      float f = side == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      float f1 = MathHelper.sqrt(swing);
      GlStateManager.translate(f * ( -0.3F * MathHelper.sin(f1 * (float)Math.PI) + 0.64F), 0.4F * MathHelper.sin(f1 * (float)Math.PI * 2.0F) - 0.6F + equip * -0.6F, -0.72F);
      GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(f * MathHelper.sin(swing * swing * (float)Math.PI) * -20.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(f * MathHelper.sin(f1 * (float)Math.PI) * -80.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);
   }

   private void spawnRenderFistParticles(RenderPlayerEvent.Post event, EntityPlayer player) {
      // FIX: Risoluzione Incompatible Types (image_a0c2a5.png)
      Render<?> renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);
      if (renderer instanceof RenderLivingBase && ((RenderLivingBase<?>)renderer).getMainModel() instanceof ModelBiped) {
         ModelBiped model = (ModelBiped)((RenderLivingBase<?>)renderer).getMainModel();
         // ... logica particelle
      }
   }

   private float interpolateRotation(float prev, float curr, float partial) {
      return prev + Vec2f.wrapDegrees(curr - prev) * partial;
   }
}