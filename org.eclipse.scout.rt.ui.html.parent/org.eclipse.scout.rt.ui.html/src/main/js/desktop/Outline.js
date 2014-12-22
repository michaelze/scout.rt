scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._addAdapterProperties('defaultDetailForm');
  this._navigateUp = false;
};
scout.inherits(scout.Outline, scout.Tree);

/**
 * @Override
 */
scout.Outline.prototype._render = function($parent) {
  scout.Outline.parent.prototype._render.call(this, $parent);
  if (this.selectedNodeIds.length === 0) {
    this._showDefaultDetailForm();
  }
};

/**
 * @Override
 */
scout.Outline.prototype._initTreeNode = function(parentNode, node) {
  scout.Outline.parent.prototype._initTreeNode.call(this, parentNode, node);

  if (node.detailTable) {
    node.detailTable = this.session.getOrCreateModelAdapter(node.detailTable, this);
  }

  if (node.detailForm) {
    node.detailForm = this.session.getOrCreateModelAdapter(node.detailForm, this);
  }
};

scout.Outline.prototype._onNodeDeleted = function(node) {
  // Destroy table, which is attached at the root adapter. Form gets destroyed by form close event
  if (node.detailTable) {
    node.detailTable.destroy();
    node.detailTable = null;
  }
};

scout.Outline.prototype._renderSelection = function($nodes) {
  scout.Outline.parent.prototype._renderSelection.call(this, $nodes);

  if (!$nodes) {
    //Outline does not support multi selection -> [0]
    $nodes = [this.$nodeById(this.selectedNodeIds[0])];
  }

  if ($nodes.length === 0) {
    return;
  }

  var node = $nodes[0].data('node');
  if (node) {
    this._updateOutlineTab(node);
  }
};


// FIXME AWE: (page) unit-test setNodesSelected
scout.Outline.prototype.setNodesSelected = function(nodes, $nodes) {
  scout.Outline.parent.prototype.setNodesSelected.call(this, nodes, $nodes);
  if (this._navigateUp) {
   this._navigateUp = false;
  } else {
    nodes = scout.arrays.ensure(nodes);
    if (nodes.length === 1) {
      nodes[0].detailFormVisible = true;
    }
  }
};

scout.Outline.prototype._showDefaultDetailForm = function() {
  var form = this.defaultDetailForm;
  if (form) {
    this.session.desktop.updateOutlineTab(form, form.title);
    this.events.trigger('outlineUpdated', {});
  }
};

scout.Outline.prototype._updateOutlineTab = function(node) {
  // FIXME AWE: (outline) remove these errors if error never occurs in application
  if (!node) {
    throw new Error('called updateOutlineTab without node, should call showDefaultDetailForm instead?');
  }
  if (this.session.desktop.outline !== this) {
    throw new Error('called updateOutlineTab but event affects another outline');
  }

  // Unlink detail form if it was closed.
  // May happen in the following case:
  // The form gets closed on execPageDeactivated.
  // No detailFormChanged event will be fired because the deactivated page is not selected anymore
  var content, parentText, nodeText, title, subTitle;
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  if (node.detailForm && node.detailFormVisible) {
    content = node.detailForm;
  } else {
    content = node.detailTable;
  }

  if (node.parentNode && node.parentNode.text) {
    parentText = node.parentNode.text;
  }
  if (node.detailForm && node.detailForm.title) {
    nodeText = node.detailForm.title;
  } else {
    nodeText = node.text;
  }

  if (parentText && nodeText) {
    title = parentText;
    subTitle = nodeText;
  } else if (parentText) {
    title = parentText;
  } else if (nodeText) {
    title = nodeText;
  }
  this.session.desktop.updateOutlineTab(content, title, subTitle);
  this.events.trigger('outlineUpdated', {node: node});
};

/* event handling */

scout.Outline.prototype.onFormChanged = function(nodeId, detailForm) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    node.detailForm = this.session.getOrCreateModelAdapter(detailForm, this);
//    node.detailFormVisible = true; // FIXME AWE
    // If the following condition is false, the selection state is not synchronized yet which
    // means there is a selection event in the queue which will be processed right afterwards.
    if (this.selectedNodeIds.indexOf(node.id) >= 0) {
      this._updateOutlineTab(node);
    }
  }
  else {
    this.defaultDetailForm = this.session.getOrCreateModelAdapter(detailForm, this);
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype.onTableChanged = function(nodeId, detailTable) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    node.detailTable = this.session.getOrCreateModelAdapter(detailTable, this);
    // If the following condition is false, the selection state is not synchronized yet which means
    // there is a selection event in the queue which will be processed right afterwards.
    if (this.selectedNodeIds.indexOf(node.id) >= 0) {
      this._updateOutlineTab(node);
    }
  }
  else {
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype.onPageChanged = function(nodeId, detailFormVisible) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    if (node.detailFormVisible !== detailFormVisible) {
      node.detailFormVisible = detailFormVisible;
      this._updateOutlineTab(node);
    }
  }
};

scout.Outline.prototype.setDetailFormVisible = function(nodeId, visible) {
  var node;
  if (nodeId >= 0) {
    node = this._nodeMap[nodeId];
    if (node.detailFormVisible !== visible) {
      node.detailFormVisible = visible;
      node.session.send('pageChanged', this.id, {nodeId: nodeId, detailFormVisible: visible});
    }
  }
};

scout.Outline.prototype.onModelAction = function(event) {
  if (event.type === 'detailFormChanged') {
    this.onFormChanged(event.nodeId, event.detailForm);
  } else if (event.type === 'detailTableChanged') {
    this.onTableChanged(event.nodeId, event.detailTable);
  } else if (event.type === 'pageChanged') {
    this.onPageChanged(event.nodeId, event.detailFormVisible);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};

/**
 * Returns the selected row or null when no row is selected. When multiple rows are selected
 * the first selected row is returned.
 */
scout.Outline.prototype.selectedRow = function() {
  var table, rowId, node, nodes = this.selectedNodes();
  if (nodes.length === 0) {
    return null;
  }
  node = nodes[0];
  if (!node.detailTable) {
    return null;
  }
  table = node.detailTable;
  if (!table.selectedRowIds || table.selectedRowIds.length === 0) {
    return null;
  }
  rowId = table.selectedRowIds[0];
  return table.rowById(rowId);
};
