package com.foreseeti.simulator;

import com.foreseeti.corelib.FClass;

public abstract class SingleParentAsset extends FClass implements Asset {
  public SingleParentAsset() {
    super();
  }

  public SingleParentAsset(SingleParentAsset other) {
    super(other);
  }

  @Override
  public void fillElementMap() {}
}
