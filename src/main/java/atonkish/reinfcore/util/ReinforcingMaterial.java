package atonkish.reinfcore.util;

import net.minecraft.world.item.Item;

public record ReinforcingMaterial(String name, int size, Item item) {
  public String getName() {
    return this.name;
  }

  public int getSize() {
    return this.size;
  }

  public Item getItem() {
    return this.item;
  }
}
