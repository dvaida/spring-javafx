package com.rm.springjavafx.charts.category.datasets;

import com.rm.springjavafx.charts.category.CategoryValues;
import com.rm.springjavafx.charts.category.JFreeCategoryDataSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;

/**
 *
 * @author Ricardo Marquez
 */
public class JFreeMultiIntervalCategoryDataSet extends AbstractDataset implements CategoryDataset, JFreeCategoryDataSet {

  public final ObservableList<CategoryValues> datasetProperty = FXCollections.observableArrayList();

  public final List<String> categories = new ArrayList<>();

  public JFreeMultiIntervalCategoryDataSet() {
    this.datasetProperty.addListener((ListChangeListener.Change<? extends CategoryValues> c) -> {
      if (c.next()) {
        fireDatasetChanged();
      }
    });
  }

  @Override
  public void setCategories(Set<String> categories) {
    this.categories.clear();
    this.categories.addAll(categories);
  }

  /**
   *
   * @param values
   */
  @Override
  public void addOrUpdate(CategoryValues values) {
    Objects.requireNonNull(values, "Values cannot be null");
    int indexOf = this.getSeriesIndex((String) values.getKey());
    if (indexOf < 0) {
      this.datasetProperty.add(values);
    } else {
      this.datasetProperty.set(indexOf, values);
    }
  }

  /**
   *
   * @param key
   * @return
   */
  @Override
  public int getSeriesIndex(String key) {
    return this.getRowIndex(key);
  }

  /**
   *
   * @return
   */
  public ObservableList<CategoryValues> datasetProperty() {
    return this.datasetProperty;
  }

  @Override
  public Comparable getColumnKey(int i) {
    return this.categories.get(i);
  }

  /**
   *
   * @param cmprbl
   * @return
   */
  @Override
  public int getColumnIndex(Comparable cmprbl) {
    return this.categories.indexOf(cmprbl);
  }

  /**
   *
   * @return
   */
  @Override
  public List getColumnKeys() {
    return new ArrayList<>(this.categories);
  }

  @Override
  public Comparable getRowKey(int i) {
    Comparable result;
    if (0 <= i && i < this.getRowCount()) {
      result = this.datasetProperty.get(i).getKey();
    } else {
      result = null;
    }
    return result;
  }

  /**
   *
   * @param key
   * @return
   */
  @Override
  public int getRowIndex(Comparable key) {
    int result = -1;
    int index = -1;
    for (CategoryValues xYValues : this.datasetProperty) {
      index++;
      if (Objects.equals(xYValues.getKey(), key)) {
        result = index;
        break;
      }
    }
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public List getRowKeys() {
    List<Comparable> result = this.datasetProperty.stream()
      .map(e -> e.getKey())
      .collect(Collectors.toList());
    return result;
  }

  /**
   *
   * @param cmprbl
   * @param cmprbl1
   * @return
   */
  @Override
  public Number getValue(Comparable cmprbl, Comparable cmprbl1) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @return
   */
  @Override
  public int getColumnCount() {
    int result = this.categories.size();
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public int getRowCount() {
    int result = this.datasetProperty.size();
    return result;
  }

  /**
   *
   * @param seriesIndex
   * @param recordIndex
   * @return
   */
  @Override
  public Number getValue(int seriesIndex, int recordIndex) {
    Number result;
    if (0 <= seriesIndex && seriesIndex < this.getRowCount()) {
      CategoryValues values = this.datasetProperty.get(seriesIndex);
      if (recordIndex < values.size()) {
        result = values.getY(this.categories.get(recordIndex));
      } else {
        result = 0;
      }

    } else {
      result = null;
    }
    return result;
  }

  /**
   *
   * @param row
   * @param column
   * @return
   */
  public Number getEndValue(int seriesIndex, int recordIndex, int i) {
    Number result;
    if (0 <= seriesIndex && seriesIndex < this.getRowCount()) {
      CategoryValues values = this.datasetProperty.get(seriesIndex);
      if (recordIndex < values.size()) {
        Object userObj = values.getUserObj(this.categories.get(recordIndex));
        if (userObj instanceof HasRanges) {
          List<Range<Number>> range = ((HasRanges<Number>) userObj).getRange();
          result = range.get(i).getMaximum();
        } else {
          throw new IllegalStateException(
            String.format("User object does not implement '%s'", HasRanges.class));
        }
      } else {
        result = 0;
      }
    } else {
      result = null;
    }
    return result;
  }

  /**
   *
   * @param row
   * @param column
   * @return
   */
  public Number getStartValue(int seriesIndex, int recordIndex, int i) {
    Number result;
    if (0 <= seriesIndex && seriesIndex < this.getRowCount()) {
      CategoryValues values = this.datasetProperty.get(seriesIndex);
      if (recordIndex < values.size()) {
        Object userObj = values.getUserObj(this.categories.get(recordIndex));
        if (userObj instanceof HasRanges) {
          List<Range<Number>> range = ((HasRanges<Number>) userObj).getRange();
          result = range.get(i).getMinimum();
        } else {
          throw new IllegalStateException();
        }
      } else {
        result = 0;
      }
    } else {
      result = null;
    }
    return result;
  }

  /**
   *
   * @param row
   * @param column
   * @return
   */
  int getNumIntervals(int seriesIndex, int recordIndex) {
    int result;
    if (0 <= seriesIndex && seriesIndex < this.getRowCount()) {
      CategoryValues values = this.datasetProperty.get(seriesIndex);
      if (recordIndex < values.size()) {
        Object userObj = values.getUserObj(this.categories.get(recordIndex));
        if (userObj instanceof HasRanges) {
          List<Range<Number>> range = ((HasRanges<Number>) userObj).getRange();
          result = range.size();
        } else {
          result = 0;
        }
      } else {
        result = 0;
      }
    } else {
      result = 0;
    }
    return result;
  }

}
