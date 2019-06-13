package com.rm.springjavafx.tabs;

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
public @interface TabItem {
  
  /**
   * 
   * @return 
   */
  String tabGroupId();
  
  /**
   * 
   * @return 
   */
  String fxml();
  
  /**
   * 
   * @return 
   */
  public String label();
}
