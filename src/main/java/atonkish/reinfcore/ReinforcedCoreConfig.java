package atonkish.reinfcore;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import atonkish.reinfcore.util.ReinforcedStorageScreenType;

@Config(name = ReinforcedCoreMod.MOD_ID)
public class ReinforcedCoreConfig implements ConfigData {
  public ReinforcedStorageScreenType screenType = ReinforcedStorageScreenType.SINGLE;
  public ScrollScreen scrollScreen = new ScrollScreen();

  public static class ScrollScreen {
    public int rows = 6;
  }
}
