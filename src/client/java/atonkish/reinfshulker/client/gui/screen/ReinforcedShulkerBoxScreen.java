package atonkish.reinfshulker.client.gui.screen;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import atonkish.reinfcore.ReinforcedCoreMod;
import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
import atonkish.reinfcore.util.ReinforcedStorageScreenType;

public class ReinforcedShulkerBoxScreen
    extends AbstractContainerScreen<ReinforcedStorageScreenHandler> {
  private static final int SLOT_SIZE = 18;
  private static final int CONTAINER_SLOT_BG_X = 7;
  private static final int CONTAINER_SLOT_BG_Y = 17;
  private static final int PLAYER_INVENTORY_GAP = 14;
  private static final int PLAYER_INVENTORY_HEIGHT = 76;
  private static final int HOTBAR_GAP = 4;

  private static final int LEFT_INSET = 7;
  private static final int RIGHT_INSET = 7;
  private static final int SCREEN_MARGIN = 8;

  private static final int SCROLL_COLUMNS = 9;
  private static final int LOW_RES_MAX_SCROLL_ROWS = 6;
  private static final int SCROLLBAR_GAP = 4;
  private static final int SCROLLBAR_WIDTH = 12;
  private static final int SCROLLER_HEIGHT = 15;

  private static final Identifier GENERIC_54_TEXTURE =
      Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

  private final int containerRows;
  private final int containerColumns;
  private int playerInventoryX;
  private int playerInventoryY;

  private float scrollPosition = 0.0F;
  private boolean scrolling = false;

  public ReinforcedShulkerBoxScreen(
      ReinforcedStorageScreenHandler menu, Inventory inventory, Component title) {
    super(menu, inventory, title, getImageWidth(menu), getImageHeight(getVisibleRows(menu)));

    this.containerRows = getVisibleRows(menu);
    this.containerColumns = getLayoutColumns(menu);
    this.playerInventoryX = getPlayerInventoryX(this.containerColumns, this.imageWidth);
    this.playerInventoryY = getPlayerInventoryY(this.containerRows);

    this.titleLabelX = LEFT_INSET + 1;
    this.titleLabelY = CONTAINER_SLOT_BG_Y - 11;

    this.inventoryLabelX = this.playerInventoryX + 1;
    this.inventoryLabelY = this.playerInventoryY - 11;

    scrollContainerSlots();
  }

  private static int getImageWidth(ReinforcedStorageScreenHandler menu) {
    int columns = getLayoutColumns(menu);
    return CONTAINER_SLOT_BG_X * 2
        + columns * SLOT_SIZE
        + (shouldUseScrollbar(menu) ? SCROLLBAR_WIDTH : 0);
  }

  private static int getImageHeight(int rows) {
    return CONTAINER_SLOT_BG_Y
        + rows * SLOT_SIZE
        + PLAYER_INVENTORY_GAP
        + PLAYER_INVENTORY_HEIGHT
        + RIGHT_INSET;
  }

  private static int getPlayerInventoryX(int columns, int imageWidth) {
    int playerWidth = SCROLL_COLUMNS * SLOT_SIZE;

    if (columns == SCROLL_COLUMNS) {
      return CONTAINER_SLOT_BG_X + (SCROLL_COLUMNS * SLOT_SIZE - playerWidth) / 2;
    }

    return Math.max(CONTAINER_SLOT_BG_X, (imageWidth - playerWidth) / 2);
  }

  private static int getPlayerInventoryY(int rows) {
    return CONTAINER_SLOT_BG_Y + rows * SLOT_SIZE + PLAYER_INVENTORY_GAP;
  }

  @Override
  protected void init() {
    super.init();
    updatePlayerInventoryLayout();
    scrollContainerSlots();
  }

  @Override
  public void extractBackground(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractBackground(graphics, mouseX, mouseY, partialTick);

    int x = this.leftPos;
    int y = this.topPos;

    drawPanelBackground(graphics, x, y);
    drawContainerSlots(graphics);
    drawPlayerSlots(graphics);

    if (this.hasScrollbar()) {
      drawScrollbar(graphics, x, y);
    }
  }

  private void drawPanelBackground(GuiGraphicsExtractor graphics, int x, int y) {
    graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF555555);
    graphics.outline(x, y, this.imageWidth, this.imageHeight, 0xFF202020);
  }

  private void drawContainerSlots(GuiGraphicsExtractor graphics) {
    int containerSlots = this.menu.getInventory().getContainerSize();

    for (int index = 0; index < containerSlots && index < this.menu.slots.size(); index++) {
      Slot slot = this.menu.slots.get(index);

      if (slot.y <= -1000) {
        continue;
      }

      drawSlotBackground(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1);
    }
  }

  private void drawPlayerSlots(GuiGraphicsExtractor graphics) {
    int containerSlots = this.menu.getInventory().getContainerSize();

    for (int index = containerSlots; index < this.menu.slots.size(); index++) {
      Slot slot = this.menu.slots.get(index);

      if (slot.y <= -1000) {
        continue;
      }

      drawSlotBackground(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1);
    }
  }

  private void drawScrollbar(GuiGraphicsExtractor graphics, int x, int y) {
    int barX = getScrollbarX();
    int barY = y + CONTAINER_SLOT_BG_Y + 1;
    int trackHeight = this.containerRows * SLOT_SIZE - 2;

    graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + trackHeight, 0xFF8B8B8B);
    graphics.outline(barX, barY, SCROLLBAR_WIDTH, trackHeight, 0xFF404040);

    int thumbTravel = trackHeight - SCROLLER_HEIGHT;
    int thumbY = barY + Math.round(thumbTravel * this.scrollPosition);

    graphics.fill(
        barX + 1, thumbY, barX + SCROLLBAR_WIDTH - 1, thumbY + SCROLLER_HEIGHT, 0xFFC6C6C6);
    graphics.outline(barX + 1, thumbY, SCROLLBAR_WIDTH - 2, SCROLLER_HEIGHT, 0xFF404040);
  }

  private int getScrollbarX() {
    return this.leftPos + CONTAINER_SLOT_BG_X + this.containerColumns * SLOT_SIZE + SCROLLBAR_GAP;
  }

  private boolean hasScrollbar() {
    return shouldUseScrollbar(this.menu);
  }

  private boolean isClickInScrollbar(double mouseX, double mouseY) {
    int x = getScrollbarX();
    int y = this.topPos + CONTAINER_SLOT_BG_Y + 1;
    int height = this.containerRows * SLOT_SIZE - 2;

    return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH && mouseY >= y && mouseY < y + height;
  }

  private int getHiddenRows() {
    int totalRows = getTotalRows(this.menu);
    return Math.max(1, totalRows - this.containerRows);
  }

  private void setScrollPosition(float position) {
    this.scrollPosition = Math.max(0.0F, Math.min(position, 1.0F));
    scrollContainerSlots();
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (!this.hasScrollbar()) {
      return false;
    }

    float amount = (float) (verticalAmount / (double) getHiddenRows());
    setScrollPosition(this.scrollPosition - amount);
    return true;
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
    if (this.hasScrollbar() && event.button() == 0 && isClickInScrollbar(event.x(), event.y())) {
      this.scrolling = true;
      return true;
    }

    return super.mouseClicked(event, doubled);
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    if (event.button() == 0) {
      this.scrolling = false;
    }

    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
    if (this.hasScrollbar() && this.scrolling) {
      int y = this.topPos + CONTAINER_SLOT_BG_Y + 1;
      int height = this.containerRows * SLOT_SIZE - 2;
      float position =
          ((float) event.y() - y - SCROLLER_HEIGHT / 2.0F) / (height - SCROLLER_HEIGHT);

      setScrollPosition(position);
      return true;
    }

    return super.mouseDragged(event, offsetX, offsetY);
  }

  private static void drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y) {
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        GENERIC_54_TEXTURE,
        x,
        y,
        CONTAINER_SLOT_BG_X,
        CONTAINER_SLOT_BG_Y,
        SLOT_SIZE,
        SLOT_SIZE,
        256,
        256);
  }

  private static int getFullColumns(ReinforcedStorageScreenHandler menu) {
    return Math.max(SCROLL_COLUMNS, menu.getColumns());
  }

  private static int getLayoutColumns(ReinforcedStorageScreenHandler menu) {
    return shouldForceScrollLayout(menu) ? SCROLL_COLUMNS : getFullColumns(menu);
  }

  private static boolean shouldForceScrollLayout(ReinforcedStorageScreenHandler menu) {
    return ReinforcedCoreMod.CONFIG.screenType == ReinforcedStorageScreenType.SCROLL
        || isFullLayoutTooLarge(menu);
  }

  private static boolean isFullLayoutTooLarge(ReinforcedStorageScreenHandler menu) {
    int fullColumns = getFullColumns(menu);
    int fullRows = getRows(menu, fullColumns);
    int fullWidth = CONTAINER_SLOT_BG_X * 2 + fullColumns * SLOT_SIZE;
    int fullHeight = getImageHeight(fullRows);

    Minecraft minecraft = Minecraft.getInstance();
    int maxWidth = minecraft.getWindow().getGuiScaledWidth() - SCREEN_MARGIN;
    int maxHeight = minecraft.getWindow().getGuiScaledHeight() - SCREEN_MARGIN;

    return fullWidth > maxWidth || fullHeight > maxHeight;
  }

  private static int getTotalRows(ReinforcedStorageScreenHandler menu) {
    return getRows(menu, getLayoutColumns(menu));
  }

  private static int getRows(ReinforcedStorageScreenHandler menu, int columns) {
    return (int) Math.ceil(menu.getInventory().getContainerSize() / (double) columns);
  }

  private static int getVisibleRows(ReinforcedStorageScreenHandler menu) {
    int totalRows = getTotalRows(menu);

    if (!shouldForceScrollLayout(menu)) {
      return totalRows;
    }

    int configuredRows = ReinforcedCoreMod.CONFIG.scrollScreen.rows;
    int visibleRows = Math.max(1, Math.min(configuredRows, totalRows));
    visibleRows = Math.min(visibleRows, getMaxRowsThatFit(totalRows));

    if (isFullLayoutTooLarge(menu)) {
      visibleRows = Math.min(visibleRows, LOW_RES_MAX_SCROLL_ROWS);
    }

    return Math.max(1, visibleRows);
  }

  private static int getMaxRowsThatFit(int totalRows) {
    Minecraft minecraft = Minecraft.getInstance();
    int availableHeight =
        minecraft.getWindow().getGuiScaledHeight()
            - SCREEN_MARGIN * 2
            - CONTAINER_SLOT_BG_Y
            - PLAYER_INVENTORY_GAP
            - PLAYER_INVENTORY_HEIGHT
            - RIGHT_INSET;

    int rows = availableHeight / SLOT_SIZE;
    return Math.max(1, Math.min(rows, totalRows));
  }

  private static boolean shouldUseScrollbar(ReinforcedStorageScreenHandler menu) {
    return getTotalRows(menu) > getVisibleRows(menu);
  }

  private void scrollContainerSlots() {
    int totalSlots = this.menu.getInventory().getContainerSize();
    int hiddenRows = Math.max(0, getTotalRows(this.menu) - this.containerRows);
    int rowOffset = this.hasScrollbar() ? Math.round(this.scrollPosition * hiddenRows) : 0;

    for (int slotIndex = 0;
        slotIndex < totalSlots && slotIndex < this.menu.slots.size();
        slotIndex++) {
      moveContainerSlot(slotIndex, rowOffset);
    }

    movePlayerInventorySlots(totalSlots);
  }

  private void moveContainerSlot(int slotIndex, int rowOffset) {
    Slot slot = this.menu.slots.get(slotIndex);
    int row = slotIndex / this.containerColumns;
    int column = slotIndex % this.containerColumns;
    int visibleRow = row - rowOffset;

    int x = CONTAINER_SLOT_BG_X + column * SLOT_SIZE;
    int y =
        visibleRow >= 0 && visibleRow < this.containerRows
            ? CONTAINER_SLOT_BG_Y + visibleRow * SLOT_SIZE
            : -2000;

    setSlotPosition(slot, x, y);
  }

  private void updatePlayerInventoryLayout() {
    this.playerInventoryX = getPlayerInventoryX(this.containerColumns, this.imageWidth);
    this.playerInventoryY = this.imageHeight - (3 * SLOT_SIZE + HOTBAR_GAP + SLOT_SIZE) - 8;

    this.inventoryLabelX = this.playerInventoryX + 1;
    this.inventoryLabelY = this.playerInventoryY - 11;
  }

  private void movePlayerInventorySlots(int firstPlayerSlotIndex) {
    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < SCROLL_COLUMNS; column++) {
        int index = firstPlayerSlotIndex + row * SCROLL_COLUMNS + column;

        if (index < this.menu.slots.size()) {
          setSlotPosition(
              this.menu.slots.get(index),
              this.playerInventoryX + column * SLOT_SIZE,
              this.playerInventoryY + row * SLOT_SIZE);
        }
      }
    }

    moveHotbarSlots(firstPlayerSlotIndex);
  }

  private void moveHotbarSlots(int firstPlayerSlotIndex) {
    int hotbarY = this.playerInventoryY + 3 * SLOT_SIZE + HOTBAR_GAP;

    for (int column = 0; column < SCROLL_COLUMNS; column++) {
      int index = firstPlayerSlotIndex + 27 + column;

      if (index < this.menu.slots.size()) {
        setSlotPosition(
            this.menu.slots.get(index), this.playerInventoryX + column * SLOT_SIZE, hotbarY);
      }
    }
  }

  private static final Field SLOT_X_FIELD = getSlotField("x", "field_7873");
  private static final Field SLOT_Y_FIELD = getSlotField("y", "field_7872");

  private static Field getSlotField(String namedField, String intermediaryField) {
    try {
      Field field = Slot.class.getDeclaredField(namedField);
      field.setAccessible(true);
      return field;
    } catch (ReflectiveOperationException namedException) {
      try {
        Field field = Slot.class.getDeclaredField(intermediaryField);
        field.setAccessible(true);
        return field;
      } catch (ReflectiveOperationException intermediaryException) {
        throw new IllegalStateException(
            "Unable to access Slot position field", intermediaryException);
      }
    }
  }

  private static void setSlotPosition(Slot slot, int x, int y) {
    try {
      SLOT_X_FIELD.setInt(slot, x);
      SLOT_Y_FIELD.setInt(slot, y);
    } catch (IllegalAccessException exception) {
      throw new IllegalStateException("Unable to move shulker slot for scrolling", exception);
    }
  }
}
