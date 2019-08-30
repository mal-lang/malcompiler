package com.foreseeti.corelib;

import com.foreseeti.corelib.math.FDistribution;

public interface ModelElement {
  public void setEvidenceDistribution(FDistribution<?> dist);

  public FClass getContainerFClass();
}
