package atonkish.reinfcore.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import atonkish.reinfcore.util.ReinforcingMaterial;

public class ReinforcedStorageScreenHandler extends AbstractContainerMenu {
  private static final int SLOT_SIZE = 18;

  private final ReinforcingMaterial material;
  private final Container inventory;
  private final int rows;
  private final int columns;

  private ReinforcedStorageScreenHandler(
      MenuType<ReinforcedStorageScreenHandler> type,
      ReinforcingMaterial material,
      int syncId,
      Inventory playerInventory,
      Container inventory) {
    super(type, syncId);

    checkContainerSize(inventory, material.getSize());

    this.material = material;
    this.inventory = inventory;
    this.columns = getColumnCount(material);
    this.rows = (int) Math.ceil(inventory.getContainerSize() / (double) this.columns);

    inventory.startOpen(playerInventory.player);

    addContainerSlots();
    addPlayerSlots(playerInventory);
  }

  public static ReinforcedStorageScreenHandler createShulkerBoxScreen(
      ReinforcingMaterial material, int syncId, Inventory playerInventory) {
    return createShulkerBoxScreen(
        material, syncId, playerInventory, new SimpleContainer(material.getSize()));
  }

  public static ReinforcedStorageScreenHandler createShulkerBoxScreen(
      ReinforcingMaterial material, int syncId, Inventory playerInventory, Container inventory) {
    MenuType<ReinforcedStorageScreenHandler> type =
        ModScreenHandlerType.REINFORCED_SHULKER_BOX_MAP.get(material);

    if (type == null) {
      type = ModScreenHandlerType.registerMaterialShulkerBox(material);
    }

    return new ReinforcedStorageScreenHandler(type, material, syncId, playerInventory, inventory);
  }

  private static int getColumnCount(ReinforcingMaterial material) {
    int size = material.getSize();

    if (size <= 81) {
      return 9;
    }

    return Math.max(9, size / 9);
  }

  private void addContainerSlots() {
    int size = this.inventory.getContainerSize();

    for (int index = 0; index < size; index++) {
      int row = index / this.columns;
      int column = index % this.columns;
      this.addSlot(
          new ShulkerBoxSlot(this.inventory, index, 8 + column * SLOT_SIZE, 18 + row * SLOT_SIZE));
    }
  }

  private void addPlayerSlots(Inventory playerInventory) {
    int playerInventoryY = 18 + this.rows * SLOT_SIZE + 14;

    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 9; column++) {
        this.addSlot(
            new Slot(
                playerInventory,
                column + row * 9 + 9,
                8 + column * SLOT_SIZE,
                playerInventoryY + row * SLOT_SIZE));
      }
    }

    int hotbarY = playerInventoryY + 58;
    for (int column = 0; column < 9; column++) {
      this.addSlot(new Slot(playerInventory, column, 8 + column * SLOT_SIZE, hotbarY));
    }
  }

  public ReinforcingMaterial getMaterial() {
    return this.material;
  }

  public Container getInventory() {
    return this.inventory;
  }

  public int getRows() {
    return this.rows;
  }

  public int getColumns() {
    return this.columns;
  }

  @Override
  public boolean stillValid(Player player) {
    return this.inventory.stillValid(player);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack originalStack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot != null && slot.hasItem()) {
      ItemStack stack = slot.getItem();
      originalStack = stack.copy();

      int containerSlots = this.inventory.getContainerSize();

      if (index < containerSlots) {
        if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(stack, 0, containerSlots, false)) {
        return ItemStack.EMPTY;
      }

      if (stack.isEmpty()) {
        slot.setByPlayer(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }

      if (stack.getCount() == originalStack.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(player, stack);
    }

    return originalStack;
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    this.inventory.stopOpen(player);
  }
}
