package atonkish.reinfcore.api;

import net.minecraft.world.item.Item;

import atonkish.reinfcore.screen.ModScreenHandlerType;
import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfcore.util.ReinforcingMaterials;

public final class ReinforcedCoreRegistry {
  private ReinforcedCoreRegistry() {}

  public static ReinforcingMaterial registerReinforcingMaterial(String name, int size, Item item) {
    ReinforcingMaterial material = new ReinforcingMaterial(name, size, item);
    ReinforcingMaterials.MAP.put(name, material);
    return material;
  }

  public static void registerMaterialSingleBlockScreenModel(ReinforcingMaterial material) {
    // No-op in the vendored 26.2 core. The shulker mod renders its own screen.
  }

  public static void registerMaterialShulkerBoxScreenHandler(ReinforcingMaterial material) {
    ModScreenHandlerType.registerMaterialShulkerBox(material);
  }
}
