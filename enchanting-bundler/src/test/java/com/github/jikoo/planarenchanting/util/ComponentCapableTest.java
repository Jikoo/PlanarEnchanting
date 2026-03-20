package com.github.jikoo.planarenchanting.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.github.jikoo.planarenchanting.anvil.AnvilCreator;
import com.github.jikoo.planarenchanting.table.Enchantabilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.view.AnvilView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.MockedStatic;

@TestInstance(Lifecycle.PER_CLASS)
public class ComponentCapableTest {

  private MockedStatic<ComponentCapability> componentCapabilities;
  private MockedStatic<Bukkit> bukkit;

  @BeforeAll
  void setUp() {
    componentCapabilities = mockStatic();
    componentCapabilities.when(ComponentCapability::get).thenReturn(true);
    bukkit = mockStatic();
    // Set up registries so Enchantment can be mocked.
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(ignored -> mock(Registry.class));
  }

  @AfterAll
  void tearDown() {
    componentCapabilities.close();
    bukkit.close();
  }

  @Test
  void enchantabilitiesDelegate() {
    // Use ItemType to ensure DataComponent is checked
    assertThrows(LinkageError.class, () -> Enchantabilities.of(mock(ItemType.class)));
  }

  @Test
  void enchantDataDelegate() {
    assertThrows(LinkageError.class, () -> new DelegateEnchantProvider().of(mock()).getWeight());
  }

  @Test
  void anvilDelegate() {
    // Set up non-empty first item so we're sure to hit a function application.
    ItemStack base = mock();
    doReturn(Material.DIRT).when(base).getType();
    doReturn(1).when(base).getAmount();
    AnvilView view = mock();
    doReturn(base).when(view).getItem(0);
    assertThrows(LinkageError.class, () -> AnvilCreator.create().getResult(view));
  }

}
