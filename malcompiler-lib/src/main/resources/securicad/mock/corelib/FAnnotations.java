package com.foreseeti.corelib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FAnnotations {
  public static enum Category {
    Attacker,
    Communication,
    Container,
    Networking,
    Security,
    System,
    User,
    Zone
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Association {
    public int index();

    public String name();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Display {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface DisplayClass {
    public boolean supportCapexOpex() default true;

    public Category category();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface TypeDescription {
    public String text();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface TypeName {
    public String name();
  }
}
