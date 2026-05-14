
package net.luck.narutoaddon.OtherCode.item;

import com.google.common.collect.Multimap;
import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.EntityThrownShuriken;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemGiantShuriken extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:giantshuriken")
   public static final Item block = null;

   public ItemGiantShuriken(ElementsInfTsukAddon instance) {
      super(instance, 28);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> ((Item)(new ItemSword(EnumHelper.addToolMaterial("GIANTSHURIKEN", 1, 0, 4.0F, 12.0F, 0)) {
            public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
               Multimap multimap = super.getItemAttributeModifiers(slot);
               if (slot == EntityEquipmentSlot.MAINHAND) {
                  multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.getAttackDamage(), 0));
                  multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -1.8, 0));
               }

               return multimap;
            }

            public Set<String> getToolClasses(ItemStack stack) {
               HashMap ret = new HashMap();
               ret.put("sword", 1);
               return ret.keySet();
            }

            public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
               ItemStack stack = player.getHeldItem(hand);
               if (player.getCooldownTracker().hasCooldown(this)) {
                  return new ActionResult(EnumActionResult.FAIL, stack);
               } else {
                  world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 1.0F, 0.8F);
                  if (!world.isRemote) {
                     EntityThrownShuriken.EntityCustom shuriken = new EntityThrownShuriken.EntityCustom(world, player);
                     shuriken.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.0F, 0.0F);
                     world.spawnEntity(shuriken);
                  }

                  player.getCooldownTracker().setCooldown(this, 10);
                  return new ActionResult(EnumActionResult.SUCCESS, stack);
               }
            }
         }).setTranslationKey("giantshuriken").setRegistryName("giantshuriken")).setCreativeTab(CreativeTabs.COMBAT));
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("inftsukaddon:giantshuriken", "inventory"));
   }
}
