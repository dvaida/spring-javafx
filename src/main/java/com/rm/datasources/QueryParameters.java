package com.rm.datasources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author rmarquez
 */
public final class QueryParameters implements Iterable<QueryParameter>, InitializingBean {

  private final MapProperty<String, QueryParameter> queryParams = new SimpleMapProperty<>();
  private Invoker invoker;
  /**
   *
   * @param queryParam
   */
  public QueryParameters(List<QueryParameter> queryParam) {
    this.setQueryParameters(queryParam);
  }

  public Invoker getInvoker() {
    return invoker;
  }

  public void setInvoker(Invoker invoker) {
    this.invoker = invoker;
  }

  
  /**
   *
   * @param listener
   */
  public void addListener(ChangeListener<? super ObservableMap<String, QueryParameter>> listener) {
    this.queryParams.addListener(listener);
  }

  /**
   *
   * @param queryParam
   */
  public void setQueryParameters(List<QueryParameter> queryParam) {
    Map<String, QueryParameter> map = new HashMap<>();
    for (QueryParameter queryParameter : queryParam) {
      String name = queryParameter.getName();
      map.put(name, queryParameter);
    }
    this.queryParams.setValue(FXCollections.observableMap(map));
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.invoker != null) {
      this.invoker.addListener((e)->{
        List<QueryParameter> newVal = this.queryParams.values().stream().collect(Collectors.toList());
        this.setQueryParameters(newVal);
      }); 
    }
  }
  
  /**
   *
   * @param stnId
   * @return
   */
  public QueryParameter get(String stnId) {
    QueryParameter result = this.queryParams.get(stnId);
    return result;
  }

  /**
   *
   * @return
   */
  public int size() {
    return this.queryParams.size();
  }
  
  /**
   * 
   * @return 
   */
  @Override
  public Iterator<QueryParameter> iterator() {
    return this.queryParams.values().iterator();
  }

  @Override
  public String toString() {
    return "QueryParameters{" + "queryParam=" + queryParams + '}';
  }
  
}
