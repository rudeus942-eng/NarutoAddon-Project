package net.luck.narutoaddon.handlers;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Fondamentale: registra la classe automaticamente nel bus degli eventi di Forge
@Mod.EventBusSubscriber(modid = "narutoaddon")
public class JintonHandler {

    // 5.0F = 2.5 cuori di danno. Se colpisce a ripetizione, il danno sarà bilanciato.
    private static final float FIXED_NERF_DAMAGE = 5.0F;

    @SubscribeEvent
    public static void onDustDamage(LivingHurtEvent event) {
        // Controllo di sicurezza per evitare NullPointerException
        if (event.getSource() == null || event.getSource().getDamageType() == null) return;

        // Controllo 1: Il danno del Jinton originale è "ninjutsu"
        if (event.getSource().getDamageType().equalsIgnoreCase("ninjutsu")) {

            // Controllo 2: Verifichiamo l'entità che ha causato il danno (Cubo o Raggio)
            if (event.getSource().getImmediateSource() != null) {
                String entityName = event.getSource().getImmediateSource().getClass().getSimpleName().toLowerCase();

                // Usiamo "contains" per essere più flessibili con i nomi delle classi della mod originale
                if (entityName.contains("cube") || entityName.contains("beam")) {

                    // Applichiamo il nerf al danno
                    event.setAmount(FIXED_NERF_DAMAGE);
                }
            }
        }
    }
}