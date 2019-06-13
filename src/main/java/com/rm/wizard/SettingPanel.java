package com.rm.wizard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Ricardo Marquez
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE)
public @interface SettingPanel {
  public String wizardId();
  public int order();
  public String label();
  public String fxml();
}
