package com.rm.panzoomcanvas.layers.points;

import com.rm.panzoomcanvas.FxCanvas;
import com.rm.panzoomcanvas.LayerMouseEvent;
import com.rm.panzoomcanvas.core.FxPoint;
import com.rm.panzoomcanvas.core.ScreenEnvelope;
import com.rm.panzoomcanvas.core.ScreenPoint;
import com.rm.panzoomcanvas.core.SpatialRef;
import com.rm.panzoomcanvas.core.VirtualEnvelope;
import com.rm.panzoomcanvas.core.VirtualPoint;
import com.rm.panzoomcanvas.layers.BaseLayer;
import com.rm.panzoomcanvas.layers.DrawArgs;
import com.rm.panzoomcanvas.projections.Projector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;

/**
 *
 * @author rmarquez
 * @param <T> A user object type.
 */
public class PointsLayer<T> extends BaseLayer {

  private final PointSymbology symbology;
  private final PointsSource<T> source;
  private final ListProperty<PointMarker<T>> selected = new SimpleListProperty<>(FXCollections.emptyObservableList());
  final Property<HoveredPointMarkers<T>> hovered = new SimpleObjectProperty<>();
  private final PointLayerCursorHelper hoveredActionsHelper;
  private PointsTooltip<T> pointsTooltip;

  /**
   *
   * @param name
   * @param symbology
   * @param source
   */
  public PointsLayer(String name, PointSymbology symbology, PointsSource<T> source) {
    super(name, source);
    this.source = source;
    if (symbology == null) {
      throw new NullPointerException("Symbology cannot be null");
    }
    this.symbology = symbology;
    this.hoveredActionsHelper = new PointLayerCursorHelper(this);
  }

  /**
   *
   */
  Node getNode() {
    return this.getLayerCanvas();
  }

  /**
   *
   * @return
   */
  public ReadOnlyProperty<HoveredPointMarkers<T>> hoveredMarkersProperty() {
    return this.hovered;
  }

  /**
   *
   * @return
   */
  public ReadOnlyListProperty<PointMarker<T>> selectedMarkersProperty() {
    return this.selected;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @Override
  protected Node createLayerCanvas(double width, double height) {
    return new Canvas(width, height);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @Override
  protected ScreenEnvelope onGetScreenEnvelope(FxCanvas canvas) {
    VirtualEnvelope virtualEnv = canvas
            .virtualEnvelopeProperty()
            .getValue();
    ScreenEnvelope screenEnv = canvas.screenEnvelopeProperty().getValue();
    ScreenEnvelope result = canvas.getProjector()
            .projectVirtualToScreen(virtualEnv, screenEnv);
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @Override
  protected void onDraw(DrawArgs args) {
    Projector projector = args.getCanvas().getProjector();
    int numPoints = this.source.getNumPoints();
    for (int i = 0; i < numPoints; i++) {
      PointMarker marker = this.source.getFxPoint(i);
      FxPoint point = marker.getPoint();
      ScreenPoint screenPoint = projector.projectGeoToScreen(point, args.getScreenEnv());
      this.symbology.apply(this, marker, args, screenPoint);
    }
  }

  /**
   *
   * @param pointsTooltipBuilder
   */
  public void setTooltip(PointsTooltip.Builder pointsTooltipBuilder) {
    if (this.pointsTooltip != null) {
      this.pointsTooltip.destroy();
    }
    this.pointsTooltip = pointsTooltipBuilder.build(this);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @Override
  public void onMouseHovered(LayerMouseEvent e) {
    ObservableList<PointMarker<T>> newVal = this.getMouseEvtList(e);
    HoveredPointMarkers<T> oldHOvered = this.hovered.getValue();
    HoveredPointMarkers<T> result = new HoveredPointMarkers<>(e, newVal);
    this.hovered.setValue(result);
    List<PointMarker<T>> oldList = oldHOvered == null ? Collections.EMPTY_LIST : oldHOvered.markers;
    this.repaintIfHoveredListChanged(oldList, newVal);
  }

  /**
   *
   * @param newVal
   */
  private void repaintIfHoveredListChanged(List<PointMarker<T>> oldVal, List<PointMarker<T>> newVal) {
    boolean changed = !listEqualsIgnoreOrder(newVal, oldVal);
    if (changed) {
      this.repaint();
    }
  }

  public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
    return new HashSet<>(list1).equals(new HashSet<>(list2));
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @Override
  public void onMouseClicked(LayerMouseEvent e) {
    List<PointMarker<T>> newVal = this.getMouseEvtList(e);
    List<PointMarker<T>> a = newVal.stream().filter((m)->{
      return !selected.getValue().contains(m);
    }).collect(Collectors.toList());
    this.selected.setValue(FXCollections.observableArrayList(a));
    this.repaint();
  }

  /**
   *
   * @param e
   * @return
   */
  private ObservableList<PointMarker<T>> getMouseEvtList(LayerMouseEvent e) {
    double eX = e.mouseEvt.getX();
    double eY = e.mouseEvt.getY();
    ScreenPoint scrnPt = new ScreenPoint(eX, eY);
    ScreenEnvelope env = e.screenEnv;
    VirtualPoint virtual = e.projector.projectScreenToVirtual(scrnPt, env);
    List<PointMarker<T>> result = new ArrayList<>();
    for (int i = 0; i < this.source.getNumPoints(); i++) {
      PointMarker<T> marker = this.source.getFxPoint(i);
      SpatialRef spatialRef = marker.getPoint().getSpatialRef();
      FxPoint refPoint = e.projector.projectVirtualToGeo(virtual.asPoint(), spatialRef);
      FxPoint currPoint = marker.getPoint();
      boolean pointsIntersect = this.source.intersects(refPoint, currPoint);
      if (pointsIntersect) {
        result.add(marker);
      }
    }
    ObservableList<PointMarker<T>> newVal = FXCollections.observableArrayList(result);
    return newVal;
  }

}
