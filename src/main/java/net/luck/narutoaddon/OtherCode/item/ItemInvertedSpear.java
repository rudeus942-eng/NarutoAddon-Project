
package net.luck.narutoaddon.OtherCode.item;

import com.google.common.collect.Multimap;
import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
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
public class ItemInvertedSpear extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:invertedspear")
   public static final Item block = null;

   public ItemInvertedSpear(ElementsInfTsukAddon instance) {
      super(instance, 26);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> ((Item)(new ItemSword(EnumHelper.addToolMaterial("INVERTEDSPEAR", 1, 0, 4.0F, 20.0F, 0)) {
            public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
               Multimap multimap = super.getItemAttributeModifiers(slot);
               if (slot == EntityEquipmentSlot.MAINHAND) {
                  multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.getAttackDamage(), 0));
                  multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4, 0));
               }

               return multimap;
            }

            public Set<String> getToolClasses(ItemStack stack) {
               HashMap ret = new HashMap();
               ret.put("sword", 1);
               return ret.keySet();
            }

            public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
               DamageSource trueDamage = (new DamageSource("invertedSpear")).setDamageBypassesArmor().setDamageIsAbsolute();
               target.hurtResistantTime = 0;
               target.setAbsorptionAmount(0.0F);
               target.attackEntityFrom(trueDamage, 8.0F);
               return true;
            }
         }).setTranslationKey("invertedspear").setRegistryName("invertedspear")).setCreativeTab(CreativeTabs.COMBAT));
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("inftsukaddon:invertedspear", "inventory"));
   }
}
