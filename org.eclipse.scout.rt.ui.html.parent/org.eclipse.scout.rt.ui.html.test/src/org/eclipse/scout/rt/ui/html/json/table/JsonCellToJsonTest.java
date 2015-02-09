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
package org.eclipse.scout.rt.ui.html.json.table;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonSessionMock;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithDateColumn;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithLongColumn;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.TableWithStringColumn;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class JsonCellToJsonTest {
  private JsonSessionMock m_jsonSession;

  @Before
  public void setUp() {
    m_jsonSession = new JsonSessionMock();
  }

  /**
   * Don't generate a cell object if text is the only property.
   */
  @Test
  public void testStringColumn() throws ProcessingException, JSONException {
    TableWithStringColumn table = new TableWithStringColumn();
    table.initTable();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, "A string");
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertTrue(jsonObj instanceof String);
  }

  /**
   * Don't send value and text if they are equal. They may be equal if the date column uses the same pattern as
   * {@link JsonDate}.
   */
  @Test
  public void testLongColumn() throws ProcessingException, JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.initTable();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, 15L);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    Assert.assertEquals("15", jsonObj.get("text"));
    assertNull(jsonObj.opt("value"));
  }

  /**
   * Don't generate a cell object if text is the only property.
   */
  @Test
  public void testLongColumn_leftAlignment() throws ProcessingException, JSONException {
    TableWithLongColumn table = new TableWithLongColumn();
    table.getColumn().setHorizontalAlignment(-1);
    table.initTable();
    ITableRow row = table.addRow(table.createRow());
    table.getColumn().setValue(row, 15L);
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertTrue(jsonObj instanceof String);
  }

  @Test
  public void testDateColumn() throws ProcessingException, JSONException {
    TableWithDateColumn table = new TableWithDateColumn();
    table.initTable();
    ITableRow row = table.addRow(table.createRow());

    table.getColumn().setFormat("dd.MM.yyyy");
    table.getColumn().setValue(row, DateUtility.parse("01.01.2015", "dd.MM.yyyy"));
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    JSONObject jsonObj = (JSONObject) jsonTable.cellToJson(row, table.getColumn());
    Assert.assertEquals("01.01.2015", jsonObj.get("text"));
    Assert.assertEquals("2015-01-01", jsonObj.get("value")); //Pattern used by JsonDate
  }

  /**
   * Don't send value and text if they are equal. They may be equal if the date column uses the same pattern as
   * {@link JsonDate}.
   */
  @Test
  public void testDateColumn_jsonDatePattern() throws ProcessingException, JSONException {
    TableWithDateColumn table = new TableWithDateColumn();
    table.initTable();
    ITableRow row = table.addRow(table.createRow());

    table.getColumn().setFormat(JsonDate.JSON_PATTERN_DATE_ONLY);
    table.getColumn().setValue(row, DateUtility.parse("01.01.2015", "dd.MM.yyyy"));
    JsonTable<ITable> jsonTable = m_jsonSession.newJsonAdapter(table, null, null);

    Object jsonObj = jsonTable.cellToJson(row, table.getColumn());
    assertTrue(jsonObj instanceof String);
  }
}
