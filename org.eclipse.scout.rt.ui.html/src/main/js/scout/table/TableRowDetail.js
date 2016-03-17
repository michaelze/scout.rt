/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableRowDetail = function() {
  scout.TableRowDetail.parent.call(this);
  this.table;
  this.tableRow;
};
scout.inherits(scout.TableRowDetail, scout.Widget);

scout.TableRowDetail.prototype._init = function(model) {
  scout.TableRowDetail.parent.prototype._init.call(this, model);
  this.table = model.table;
  this.tableRow = model.tableRow;
};

scout.TableRowDetail.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('table-row-detail');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this._renderTableRow();
};

scout.TableRowDetail.prototype._renderTableRow = function() {
  this.table.columns.forEach(function(column) {
    var name = column.text;
    var value = this.table.cellText(column, this.tableRow);
    if (scout.strings.empty(value)) {
      return;
    }
    var $field = this.$container.appendDiv('table-row-detail-field');
    // FIXME CGU handle column without text or with icon, handle icon content, html content, bean content
    $field.appendSpan('table-row-detail-name').text(name + ': ');
    $field.appendSpan('table-row-detail-value').text(value);
  }, this);
};
