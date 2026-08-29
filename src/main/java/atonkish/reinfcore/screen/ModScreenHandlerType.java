package atonkish.reinfcore.screen;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import atonkish.reinfcore.ReinforcedCoreMod;
import atonkish.reinfcore.util.ReinforcingMaterial;

public final class ModScreenHandlerType {
  public static final Map<ReinforcingMaterial, MenuType<ReinforcedStorageScreenHandler>>
      REINFORCED_SHULKER_BOX_MAP = new LinkedHashMap<>();

  private ModScreenHandlerType() {}

  public static MenuType<ReinforcedStorageScreenHandler> registerMaterialShulkerBox(
      ReinforcingMaterial material) {
    if (REINFORCED_SHULKER_BOX_MAP.containsKey(material)) {
      return REINFORCED_SHULKER_BOX_MAP.get(material);
    }

    Identifier id =
        Identifier.fromNamespaceAndPath(
            ReinforcedCoreMod.MOD_ID, material.getName() + "_shulker_box");

    MenuType<ReinforcedStorageScreenHandler> type =
        new MenuType<>(
            (syncId, playerInventory) ->
                ReinforcedStorageScreenHandler.createShulkerBoxScreen(
                    material, syncId, playerInventory),
            FeatureFlags.VANILLA_SET);

    Registry.register(BuiltInRegistries.MENU, id, type);
    REINFORCED_SHULKER_BOX_MAP.put(material, type);
    return type;
  }
}
