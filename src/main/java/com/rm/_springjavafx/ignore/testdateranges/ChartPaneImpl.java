package com.rm._springjavafx.ignore.testdateranges;

import com.rm.springjavafx.FxmlInitializer;
import com.rm.springjavafx.annotations.FxAttach;
import com.rm.springjavafx.annotations.FxController;
import com.rm.springjavafx.annotations.childnodes.ChildNode;
import com.rm.springjavafx.charts.FxPlotOrientation;
import com.rm.springjavafx.charts.category.CategoryChart;
import com.rm.springjavafx.charts.category.CategoryChartPane;
import com.rm.springjavafx.charts.category.CategoryFxDataSetGroup;
import com.rm.springjavafx.charts.category.PlotType;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import org.controlsfx.control.CheckListView;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 * @author Ricardo Marquez
 */
@Component
@Lazy(false)
@FxController(fxml = "fxml/charts.fxml")
@CategoryChart(
  id = "testchart",
  node = "chart",
  datasetgroups = {
    @CategoryFxDataSetGroup(
      plotType = PlotType.ITEM_VALUES,
      barwidth = 1
    )
  },
  orientation = FxPlotOrientation.HORIZONTAL,
  categories = "categories"
)
@FxAttach(fxml = "fxml/Main.fxml", id = "theAnchorPane")
public class ChartPaneImpl extends CategoryChartPane {

  @Autowired
  private FxmlInitializer fxmlInitializer;
  @ChildNode(id = "listview")
  private CheckListView<String> listview;

  @Bean("testchart_datasets")
  public Property<String> getDatasets() {
    return new SimpleObjectProperty<>();
  }
  
  /**
   * 
   * @return 
   */
  @Override
  protected ValueAxis getRangeAxis() {
    DateAxis result = new DateAxis();
    result.setAutoRange(true);
    return result;
  }

  /**
   *
   */
  @Override
  protected void postInit() {
    this.fxmlInitializer.addListener((i) -> {
      super.datasetsProperty().addListener((obs, old, change) -> {
        this.updateListViewItems();
      });
      this.updateListViewItems();

      super.visibleDatasetsProperty().addListener((obs, old, change) -> {
        this.updateListViewItemsCheckedValues();
      });
      this.updateListViewItemsCheckedValues();
      this.listview.getCheckModel().getCheckedItems().addListener(
        (ListChangeListener.Change<? extends String> c) -> {
          if (c.next()) {
            if (c.wasAdded()) {
              List<? extends String> added = c.getAddedSubList();
              this.setVisible((List<String>) added, true);
            } else if (c.wasRemoved()) {
              List<? extends String> removed = c.getRemoved();
              this.setVisible((List<String>) removed, false);
            }
          }
        });
    });
  }

  private void updateListViewItemsCheckedValues() {
    List<String> change = this.visibleDatasetsProperty().getValue();
    if (change != null) {
      for (String string : change) {
        this.listview.getCheckModel().check(string);
      }
    }
  }

  /**
   *
   */
  private void updateListViewItems() {
    List<String> datasets = this.datasetsProperty().getValue();
    if (datasets != null) {
      for (String string : datasets) {
        if (!this.listview.getItems().contains(string)) {
          this.listview.getItems().add(string);
        }
      }
      List<String> copy = new ArrayList<>(listview.getItems());
      List<String> toRemove = new ArrayList<>();
      for (String item : copy) {
        if (!datasets.contains(item)) {
          toRemove.add(item);
        }
      }
      listview.getItems().removeAll(toRemove);
    }
  }
}
