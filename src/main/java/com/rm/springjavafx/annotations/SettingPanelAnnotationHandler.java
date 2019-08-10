package com.rm.springjavafx.annotations;

import com.rm.springjavafx.AnnotationHandler;
import com.rm.springjavafx.FxmlInitializer;
import com.rm.wizard.SettingPanel;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ricardo Marquez
 */
@Component
public class SettingPanelAnnotationHandler implements InitializingBean, AnnotationHandler {
  @Autowired
  private FxmlInitializer fxmlInitializer;
  @Autowired
  private ApplicationContext appContext;
  
  /**
   * 
   * @throws Exception 
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    this.fxmlInitializer.addAnnotationHandler(this);
  }

  /**
   *
   */
  @Override
  public void readyFxmls() {
    Map<String, Object> beans = appContext.getBeansWithAnnotation(SettingPanel.class);
    for (Object value : beans.values()) {
      String fxml = value.getClass().getDeclaredAnnotation(SettingPanel.class).fxml();
      this.addFxml(fxml);
    }
  }
  
  /**
   * 
   * @param fxml
   * @throws RuntimeException
   * @throws IllegalStateException 
   */
  private void addFxml(String fxml) {
    if (this.getClass().getClassLoader().getResource(fxml) == null) {
      throw new IllegalStateException("Fxml does not exist: '" + fxml + "'");
    }
    if (!FilenameUtils.getExtension(fxml).endsWith("fxml")) {
      throw new RuntimeException("File does not have .fxml extension: '" + fxml + "'");
    }
    this.fxmlInitializer.addFxml(fxml);
    System.out.println("added fxml: " + fxml);
  }

  /**
   *
   * @throws BeansException
   * @throws RuntimeException
   */
  @Override
  public void setNodes() {
  }
    
}