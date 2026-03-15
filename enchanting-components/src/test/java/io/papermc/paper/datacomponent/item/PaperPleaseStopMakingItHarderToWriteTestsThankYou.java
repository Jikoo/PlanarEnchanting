package io.papermc.paper.datacomponent.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datacomponent.item.ChargedProjectiles.Builder;
import io.papermc.paper.datacomponent.item.KineticWeapon.Condition;
import io.papermc.paper.datacomponent.item.MapDecorations.DecorationEntry;
import io.papermc.paper.datacomponent.item.ResolvableProfile.SkinPatch;
import io.papermc.paper.datacomponent.item.ResolvableProfile.SkinPatchBuilder;
import io.papermc.paper.datacomponent.item.Tool.Rule;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.text.Filtered;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.JukeboxSong;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.map.MapCursor.Type;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NonExtendableApiUsage")
@NullMarked
public class PaperPleaseStopMakingItHarderToWriteTestsThankYou implements ItemComponentTypesBridge {

  @Override
  public Builder chargedProjectiles() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PotDecorations.Builder potDecorations() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ItemLore.Builder lore() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ItemEnchantments.Builder enchantments() {
    return new ItemEnchantments.Builder() {
      private final Map<Enchantment, Integer> enchantments = new HashMap<>();
      @Override
      public ItemEnchantments.Builder add(Enchantment enchantment, @IntRange(from = 1L, to = 255L) int level) {
        this.enchantments.put(enchantment, level);
        return this;
      }

      @Override
      public ItemEnchantments.Builder addAll(
          Map<Enchantment, @IntRange(from = 1L, to = 255L) Integer> enchantments) {
        this.enchantments.putAll(enchantments);
        return this;
      }

      @Override
      public ItemEnchantments build() {
        return new ItemEnchantments() {
          private final Map<Enchantment, Integer> enchants = Map.copyOf(enchantments);
          @Override
          public @Unmodifiable Map<Enchantment, @IntRange(from = 1L, to = 255L) Integer> enchantments() {
             return enchants;
          }
        };
      }
    };
  }

  @Override
  public ItemAttributeModifiers.Builder modifiers() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FoodProperties.Builder food() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DyedItemColor.Builder dyedItemColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PotionContents.Builder potionContents() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BundleContents.Builder bundleContents() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SuspiciousStewEffects.Builder suspiciousStewEffects() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MapItemColor.Builder mapItemColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MapDecorations.Builder mapDecorations() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DecorationEntry decorationEntry(Type type, double x, double z, float rotation) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SeededContainerLoot.Builder seededContainerLoot(Key lootTableKey) {
    throw new UnsupportedOperationException();
  }

  @Override
  public WrittenBookContent.Builder writtenBookContent(Filtered<String> title, String author) {
    throw new UnsupportedOperationException();
  }

  @Override
  public WritableBookContent.Builder writeableBookContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ItemArmorTrim.Builder itemArmorTrim(ArmorTrim armorTrim) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LodestoneTracker.Builder lodestoneTracker() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Fireworks.Builder fireworks() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResolvableProfile.Builder resolvableProfile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SkinPatchBuilder skinPatch() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SkinPatch emptySkinPatch() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResolvableProfile resolvableProfile(PlayerProfile profile) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BannerPatternLayers.Builder bannerPatternLayers() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BlockItemDataProperties.Builder blockItemStateProperties() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ItemContainerContents.Builder itemContainerContents() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JukeboxPlayable.Builder jukeboxPlayable(JukeboxSong song) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tool.Builder tool() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Rule rule(RegistryKeySet<BlockType> blocks, @Nullable Float speed,
      TriState correctForDrops) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ItemAdventurePredicate.Builder itemAdventurePredicate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CustomModelData.Builder customModelData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MapId mapId(int id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UseRemainder useRemainder(ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Consumable.Builder consumable() {
    throw new UnsupportedOperationException();
  }

  @Override
  public UseCooldown.Builder useCooldown(float seconds) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DamageResistant damageResistant(TagKey<DamageType> types) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enchantable enchantable(int level) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Repairable repairable(RegistryKeySet<ItemType> types) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Equippable.Builder equippable(EquipmentSlot slot) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeathProtection.Builder deathProtection() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OminousBottleAmplifier ominousBottleAmplifier(int amplifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BlocksAttacks.Builder blocksAttacks() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TooltipDisplay.Builder tooltipDisplay() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Weapon.Builder weapon() {
    throw new UnsupportedOperationException();
  }

  @Override
  public KineticWeapon.Builder kineticWeapon() {
    throw new UnsupportedOperationException();
  }

  @Override
  public UseEffects.Builder useEffects() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PiercingWeapon.Builder piercingWeapon() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AttackRange.Builder attackRange() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SwingAnimation.Builder swingAnimation() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Condition kineticWeaponCondition(int maxDurationTicks, float minSpeed,
      float minRelativeSpeed) {
    throw new UnsupportedOperationException();
  }
}
