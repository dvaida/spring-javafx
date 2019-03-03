package com.rm.springjavafx.table;

import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author rmarquez
 */

public class TableViewColumn implements InitializingBean{
  private String label;
  private int columnIndex;
  private String propertyName;
  private String rendererType;
  private RenderType renderType;

  public RenderType getRenderType() {
    return renderType;
  }
  
  public void setRenderType(RenderType renderType) {
    
    this.renderType = renderType;
  }
  
  
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public void setColumnIndex(int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public String getRendererType() {
    return rendererType;
  }

  public void setRendererType(String rendererType) {
    this.rendererType = rendererType;
  }
  
  @Override
  public String toString() {
    return "TableViewRenderer{" + "name=" + label + ", column=" + columnIndex + ", rendererType=" + rendererType + '}';
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    System.out.println(this);
  }
  
 
  
}
