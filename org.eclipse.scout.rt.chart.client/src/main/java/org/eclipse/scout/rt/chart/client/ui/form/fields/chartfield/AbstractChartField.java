/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.client.ui.form.fields.chartfield;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.chart.client.ui.basic.chart.AbstractChart;
import org.eclipse.scout.rt.chart.client.ui.basic.chart.IChart;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * @since 5.2
 */
@ClassId("cee88505-5685-438d-a87d-591c54efe8d7")
public abstract class AbstractChartField<T extends IChart> extends AbstractFormField implements IChartField<T> {

  private T m_chart;

  private IContextMenu m_contextMenu;

  public AbstractChartField() {
    super(true);
  }

  public AbstractChartField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setChartInternal(createChart());

    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      menus.addOrdered(ConfigurationUtility.newInnerInstance(this, menuClazz));
    }
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    m_contextMenu = new FormFieldContextMenu<>(this, menus.getOrderedList());
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to add and/or remove menus<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  @Override
  public IContextMenu getContextMenu() {
    return m_contextMenu;
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <M extends IMenu> M getMenuByClass(Class<M> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  protected Class<? extends IChart> getConfiguredChart() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IChart>> fc = ConfigurationUtility.filterClasses(dca, IChart.class);
    if (fc.size() == 1) {
      return CollectionUtility.firstElement(fc);
    }
    else {
      for (Class<? extends IChart> c : fc) {
        if (c.getDeclaringClass() != AbstractChartField.class) {
          return c;
        }
      }
      return null;
    }
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getChart()));
  }

  @SuppressWarnings("unchecked")
  protected T createChart() {
    List<IChart> contributedCharts = m_contributionHolder.getContributionsByClass(IChart.class);
    IChart result = CollectionUtility.firstElement(contributedCharts);
    if (result != null) {
      return (T) result;
    }
    Class<? extends IChart> configuredChart = getConfiguredChart();
    if (configuredChart != null) {
      return (T) ConfigurationUtility.newInnerInstance(this, configuredChart);
    }
    return null;
  }

  @Override
  public final T getChart() {
    return m_chart;
  }

  @Override
  public void setChart(T chart) {
    setChartInternal(chart);
  }

  protected void setChartInternal(T chart) {
    if (m_chart == chart) {
      return;
    }
    if (m_chart != null) {
      m_chart.setParentInternal(null);
    }
    m_chart = chart;
    if (m_chart != null) {
      m_chart.setParentInternal(this);
      m_chart.setEnabled(isEnabled());
    }
    boolean changed = propertySupport.setProperty(PROP_CHART, m_chart);
    if (changed) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
      updateKeyStrokes();
    }
  }

  // TODO [15.4] bsh: export import, interception

  @ClassId("a6e76b7c-f42c-43b2-bcb8-55bbbc534b80")
  public class Chart extends AbstractChart {

  }
}
