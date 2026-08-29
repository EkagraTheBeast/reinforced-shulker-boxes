package atonkish.reinfshulker.block.entity;

import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;
import atonkish.reinfshulker.mixin.BlockEntityAccessor;

public class ReinforcedShulkerBoxBlockEntity extends ShulkerBoxBlockEntity {
  private final ReinforcingMaterial cachedMaterial;
  private final @Nullable DyeColor cachedColor;

  public ReinforcedShulkerBoxBlockEntity(
      ReinforcingMaterial material, @Nullable DyeColor color, BlockPos pos, BlockState state) {
    super(color, pos, state);
    ((BlockEntityAccessor) this)
        .setType(ModBlockEntityType.REINFORCED_SHULKER_BOX_MAP.get(material));
    this.setItems(NonNullList.withSize(material.getSize(), ItemStack.EMPTY));
    this.cachedMaterial = material;
    this.cachedColor = color;
  }

  public ReinforcedShulkerBoxBlockEntity(
      ReinforcingMaterial material, BlockPos pos, BlockState state) {
    this(material, getColorFromBlockState(state), pos, state);
  }

  private static @Nullable DyeColor getColorFromBlockState(BlockState state) {
    Block block = state.getBlock();

    if (block instanceof ReinforcedShulkerBoxBlock reinforcedShulkerBoxBlock) {
      return reinforcedShulkerBoxBlock.getColor();
    }

    return null;
  }

  @Override
  protected Component getDefaultName() {
    String namespace = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()).getNamespace();
    String name = this.cachedMaterial.getName() + "_shulker_box";

    if (this.cachedColor != null) {
      name = this.cachedColor.getName() + "_" + name;
    }

    return Component.translatable("block." + namespace + "." + name);
  }

  @Override
  public int[] getSlotsForFace(Direction side) {
    return IntStream.range(0, this.getContainerSize()).toArray();
  }

  public ReinforcingMaterial getMaterial() {
    return this.cachedMaterial;
  }

  @Override
  protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
    return ReinforcedStorageScreenHandler.createShulkerBoxScreen(
        this.cachedMaterial, syncId, playerInventory, this);
  }
}
