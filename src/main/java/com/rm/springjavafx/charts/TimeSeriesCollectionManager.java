package com.rm.springjavafx.charts;

import common.bindings.RmBindings;
import common.timeseries.TimeStepValue;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 *
 * @author Ricardo Marquez
 */
public final class TimeSeriesCollectionManager {

  /**
   *
   */
  private final XYPlot plot;
  private final TimeSeriesChartPane chart;
  private final Map<String, SpringFxTimeSeries> datasets = new HashMap<>();

  /**
   *
   * @param plot
   */
  public TimeSeriesCollectionManager(TimeSeriesChartPane chart) {
    this.chart = chart;
    this.plot = chart.getPlot();
    this.chart.visibleDatasetsProperty().addListener((obs, old, change) -> {
      this.updateVisibilities();
    });
    this.updateChartDatasetsProperty();
  }
  
  /**
   * 
   * @return 
   */
  public TimeSeriesChartPane getChart() {
    return chart;
  }

  /**
   *
   * @param dataset
   */
  public void addDataSet(SpringFxTimeSeries dataset) {
    if (this.datasets.containsKey(dataset.getKey())) {
      throw new IllegalArgumentException(
        String.format("Data set '%s' cannot be added twice", dataset.getKey()));
    }
    this.datasets.put(dataset.getKey(), dataset);
    int datasetId = dataset.getDatasetId();
    TimeSeriesCollection collection = (TimeSeriesCollection) plot.getDataset(datasetId);
    if (collection == null) {
      throw new IllegalStateException(
        String.format("No timeseries collection for datasetId = '%d'", datasetId));
    }

    RmBindings.bindActionOnAnyChange(() -> this.updateSeries(dataset, collection),
      dataset.valueAccessorProperty(),
      dataset.seriesProperty());
    
    
    
    this.updateSeries(dataset, collection);

  }

  /**
   *
   * @param dataset
   * @param collection
   */
  private void updateSeries(SpringFxTimeSeries dataset, TimeSeriesCollection collection) {
    common.timeseries.TimeSeries<?> series = dataset.getSeries();
    TimeSeries old = collection.getSeries(dataset.getKey());
    if (old != null) {
      int index = collection.indexOf(dataset.getKey());
      collection.removeSeries(index);
    }
    TimeSeries jfcSeries = new TimeSeries(dataset.getKey());
    Function<TimeStepValue<?>, Double> accessor = dataset.getValueAccessor();
    if (accessor == null) {
      Logger.getLogger(TimeSeriesCollectionManager.class.getName()).log(Level.WARNING, 
        String.format("accessor is not defined for dataset '%s'", dataset.getKey()));
    } 
    if (series != null && accessor != null) {
      series.forEach((r) -> {
        ZonedDateTime dateTime = r.getZoneDateTime();
        Date time = Date.from(dateTime.toInstant());
        Double value = accessor.apply(r);
        Second second = new Second(time);
        TimeSeriesDataItem item = new TimeSeriesDataItem(second, value);
        jfcSeries.add(item);
      });
    }
    collection.addSeries(jfcSeries);

    XYItemRenderer renderer = this.plot.getRenderer(dataset.getDatasetId());
    int seriesIndex = collection.getSeriesIndex(dataset.getKey());
    renderer.setSeriesPaint(seriesIndex, dataset.getLineColorAwt());
    renderer.setSeriesStroke(seriesIndex, dataset.getLineStroke());
    renderer.setSeriesShape(seriesIndex, dataset.getShape());
    this.setVisibility(dataset);
    this.updateChartDatasetsProperty();
  }

  /**
   *
   */
  private void updateVisibilities() {
    List<String> value = this.chart.visibleDatasetsProperty().getValue();
    if (value == null) {
      this.allVisible();
    } else {
      for (Map.Entry<String, SpringFxTimeSeries> entry : this.datasets.entrySet()) {
        SpringFxTimeSeries dataset = entry.getValue();
        this.setVisibility(dataset);
      }
    }
  }

  /**
   *
   */
  private void allVisible() {
    this.plot.getDataset();
    for (String key : this.datasets.keySet()) {
      SpringFxTimeSeries dataset = this.datasets.get(key);
      setVisibility(dataset, Boolean.TRUE);
    }
  }

  /**
   *
   * @param dataset
   */
  private void setVisibility(SpringFxTimeSeries dataset) {
    String key = dataset.getKey();
    List<String> value = this.chart.visibleDatasetsProperty().getValue();
    boolean visible = value == null ? true : value.contains(key);
    setVisibility(dataset, visible);
  }

  /**
   *
   * @param dataset
   * @param key
   * @param visible
   */
  private void setVisibility(SpringFxTimeSeries dataset, boolean visible) {
    int datasetId = dataset.getDatasetId();
    String key = dataset.getKey();
    TimeSeriesCollection collection = (TimeSeriesCollection) plot.getDataset(datasetId);
    int seriesIndex = collection.getSeriesIndex(key);
    this.plot.getRenderer(datasetId).setSeriesVisible(seriesIndex, visible, Boolean.TRUE);
  }

  /**
   *
   */
  private void updateChartDatasetsProperty() {
    this.chart.writableDatasetsProperty().setValue(new ArrayList<>(this.datasets.keySet()));
  }
}