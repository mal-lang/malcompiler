package com.foreseeti.simulator;

import com.foreseeti.corelib.FClass;

public abstract class MultiParentAsset extends FClass implements Asset {
  public MultiParentAsset() {
    super();
  }

  public MultiParentAsset(MultiParentAsset other) {
    super(other);
  }

  @Override
  public void fillElementMap() {}

  public void clearGraphCache() {}
}
