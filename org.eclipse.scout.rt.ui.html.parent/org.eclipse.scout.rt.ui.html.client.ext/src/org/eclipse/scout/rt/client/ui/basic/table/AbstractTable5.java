/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.menus.TableOrganizeMenu;
import org.eclipse.scout.rt.extension.client.ui.basic.table.AbstractExtensibleTable;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.ui.UiLayer2;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.service.SERVICES;

public class AbstractTable5 extends AbstractExtensibleTable implements ITable5 {
  private List<ITableControl> m_tableControls;
  private IReloadHandler m_reloadHandler;

  public AbstractTable5() {
    this(true);
  }

  public void setReloadHandler(IReloadHandler reloadHandler) {
    m_reloadHandler = reloadHandler;
  }

  @Override
  protected void execCreateHeaderMenus(List<IMenu> menuList) {
    //FIXME how to distinguish between html ui and others? better create service?
    if (UserAgentUtility.getCurrentUiLayer().equals(UiLayer2.HTML)) {
      menuList.add(new TableOrganizeMenu(this));
    }
    else {
      super.execCreateHeaderMenus(menuList);
    }
  }

  public AbstractTable5(boolean callInitializer) {
    super(false);

    m_tableControls = new LinkedList<ITableControl>();

    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public List<ITableControl> getControls() {
    return m_tableControls;
  }

  @Override
  public void fireTableReloadFromUI() {
    try {
      if (m_reloadHandler != null) {
        m_reloadHandler.reload();
      }
    }
    catch (ProcessingException se) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(se);
    }
  }

  @Override
  public void fireSortColumnRemovedFromUI(IColumn<?> column) {
    getColumnSet().removeSortColumn(column);
    sort();
  }

  @Override
  public Class<? extends IMenu> getDefaultMenu() {
    return super.getDefaultMenuInternal();
  }

}
