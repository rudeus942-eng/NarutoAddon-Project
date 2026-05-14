
package net.luck.narutoaddon.OtherCode.stat.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public class IronBodyRefundLogger {
   private static final IronBodyRefundLogger INSTANCE = new IronBodyRefundLogger();
   private static final String CSV_NAME = "iron_body_refund.csv";
   private static final String CSV_HEADER = "uuid,player_name,charges,timestamp_iso\n";

   public static void register() {
      MinecraftForge.EVENT_BUS.register(INSTANCE);
   }

   @SubscribeEvent
   public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.player instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         World world = player.getServerWorld();
         if (world != null && !world.isRemote) {
            StatSavedData saved = StatSavedData.get(world);
            if (saved != null) {
               PlayerStatData data = saved.getOrCreate(player.getUniqueID());
               int charges = data.getIronBodyCharges();
               if (charges > 0) {
                  try {
                     this.appendRow(player.getUniqueID().toString(), player.getName(), charges);
                     data.clearIronBodyCharges();
                     saved.markDirty();
                     System.out.println("[IronBodyRefund] Logged " + charges + " charges for " + player.getName() + " and cleared field.");
                  } catch (IOException ex) {
                     System.err.println("[IronBodyRefund] Failed to write refund row for " + player.getName() + ": " + ex.getMessage());
                  }

               }
            }
         }
      }
   }

   private void appendRow(String uuid, String name, int charges) throws IOException {
      File csv = this.getCsvFile();
      if (csv != null) {
         boolean writeHeader = !csv.exists() || csv.length() == 0L;
         FileWriter w = new FileWriter(csv, true);
         Throwable var7 = null;

         try {
            if (writeHeader) {
               w.write("uuid,player_name,charges,timestamp_iso\n");
            }

            w.write(uuid);
            w.write(44);
            w.write(csvEscape(name));
            w.write(44);
            w.write(Integer.toString(charges));
            w.write(44);
            w.write(Instant.now().toString());
            w.write(10);
         } catch (Throwable var16) {
            var7 = var16;
            throw var16;
         } finally {
            if (w != null) {
               if (var7 != null) {
                  try {
                     w.close();
                  } catch (Throwable var15) {
                     var7.addSuppressed(var15);
                  }
               } else {
                  w.close();
               }
            }

         }

      }
   }

   private File getCsvFile() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null) {
         return null;
      } else {
         File worldDir = server.getWorld(0).getSaveHandler().getWorldDirectory();
         return new File(worldDir, "iron_body_refund.csv");
      }
   }

   private static String csvEscape(String s) {
      if (s == null) {
         return "";
      } else {
         return s.indexOf(44) < 0 && s.indexOf(34) < 0 && s.indexOf(10) < 0 ? s : "\"" + s.replace("\"", "\"\"") + "\"";
      }
   }
}
