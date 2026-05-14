
package net.luck.narutoaddon.OtherCode.keybind;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.quest.gui.BeginnerGui;
import net.luck.narutoaddon.OtherCode.quest.gui.QuestLogGui;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsInfTsukAddon.ModElement.Tag
public class KeyBindingQuest extends ElementsInfTsukAddon.ModElement {
   private KeyBinding keys;

   public KeyBindingQuest(ElementsInfTsukAddon instance) {
      super(instance, 30);
   }

   public void preInit(FMLPreInitializationEvent event) {
      this.elements.addNetworkMessage(KeyBindingPressedMessageHandler.class, KeyBindingPressedMessage.class, Side.SERVER);
   }

   @SideOnly(Side.CLIENT)
   public void init(FMLInitializationEvent event) {
      this.keys = new KeyBinding("key.mcreator.quest", 35, "key.categories.misc");
      ClientRegistry.registerKeyBinding(this.keys);
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void onKeyInput(InputEvent.KeyInputEvent event) {
      if (this.keys.isPressed()) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.player != null && mc.currentScreen == null) {
            if (QuestClientData.isBeginnerComplete()) {
               QuestLogGui.open();
            } else {
               BeginnerGui.open();
            }
         }
      }

   }

   public static class KeyBindingPressedMessageHandler implements IMessageHandler<KeyBindingPressedMessage, IMessage> {
      public IMessage onMessage(KeyBindingPressedMessage message, MessageContext context) {
         EntityPlayerMP entity = context.getServerHandler().player;
         entity.getServerWorld().addScheduledTask(() -> {
         });
         return null;
      }
   }

   public static class KeyBindingPressedMessage implements IMessage {
      public void toBytes(ByteBuf buf) {
      }

      public void fromBytes(ByteBuf buf) {
      }
   }
}
