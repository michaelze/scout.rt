/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Group = function() {
  scout.Group.parent.call(this);
  this.bodyAnimating = false;
  this.collapsed = false;
  this.collapsible = true;
  this.title = null;
  this.titleSuffix = null;
  this.headerVisible = true;
  this.body = null;

  this.$container = null;
  this.$header = null;
  this.$title = null;
  this.$collapseIcon = null;
  this.collapseStyle = scout.Group.CollapseStyle.LEFT;
  this.htmlComp = null;
  this.htmlHeader = null;
  this.htmlBody = null;
  this.iconId = null;
  this.icon = null;
  this._addWidgetProperties(['body']);
};
scout.inherits(scout.Group, scout.Widget);

scout.Group.CollapseStyle = {
  LEFT: 'left',
  RIGHT: 'right'
};

scout.Group.prototype._init = function(model) {
  scout.Group.parent.prototype._init.call(this, model);
  this.resolveIconIds(['iconId']);
  this._setBody(this.body);
};

/**
 * @override
 */
scout.Group.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.Group.prototype._initKeyStrokeContext = function() {
  scout.Group.parent.prototype._initKeyStrokeContext.call(this);

  // Key stroke should only work when header is focused
  this.keyStrokeContext.$bindTarget = function() {
    return this.$header;
  }.bind(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.GroupToggleCollapseKeyStroke(this)
  ]);
};

scout.Group.prototype._render = function() {
  this.$container = this.$parent.appendDiv('group');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.GroupLayout(this));

  this.$header = this.$container.prependDiv('group-header')
    .on('mousedown', this._onHeaderMouseDown.bind(this))
    .unfocusable()
    .addClass('prevent-initial-focus');
  this.htmlHeader = scout.HtmlComponent.install(this.$header, this.session);
  this.$title = this.$header.appendDiv('group-title');
  this.$titleSuffix = this.$header.appendDiv('group-title-suffix');
  this.$collapseIcon = this.$header.appendDiv('group-collapse-icon');

  scout.tooltips.installForEllipsis(this.$title, {
    parent: this
  });
};

scout.Group.prototype._renderProperties = function() {
  scout.Group.parent.prototype._renderProperties.call(this);
  this._renderIconId();
  this._renderTitle();
  this._renderTitleSuffix();
  this._renderHeaderVisible();
  this._renderCollapsed();
  this._renderCollapseStyle();
  this._renderCollapsible();
};

scout.Group.prototype._remove = function() {
  this.$header = null;
  this.$title = null;
  this.$titleSuffix = null;
  this.$collapseIcon = null;
  this._removeIconId();
  scout.Group.parent.prototype._remove.call(this);
};

scout.Group.prototype._renderEnabled = function() {
  scout.Group.parent.prototype._renderEnabled.call(this);

  this.$header.setTabbable(this.enabled);
};

scout.Group.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

/**
 * Adds an image or font-based icon to the group header by adding either an IMG or SPAN element.
 */
scout.Group.prototype._renderIconId = function() {
  var iconId = this.iconId || '';
  // If the icon is an image (and not a font icon), the scout.Icon class will invalidate the layout when the image has loaded
  if (!iconId) {
    this._removeIconId();
    this._updateIconStyle();
    return;
  }
  if (this.icon) {
    this.icon.setIconDesc(iconId);
    this._updateIconStyle();
    return;
  }
  this.icon = scout.create('Icon', {
    parent: this,
    iconDesc: iconId,
    prepend: true
  });
  this.icon.one('destroy', function() {
    this.icon = null;
  }.bind(this));
  this.icon.render(this.$header);
  this._updateIconStyle();
};

scout.Group.prototype._updateIconStyle = function() {
  var hasTitle = !!this.title;
  this.get$Icon().toggleClass('with-title', hasTitle);
  this.get$Icon().addClass('group-icon');
  this._renderCollapseStyle();
};

scout.Group.prototype.get$Icon = function() {
  if (this.icon) {
    return this.icon.$container;
  }
  return $();
};

scout.Group.prototype._removeIconId = function() {
  if (this.icon) {
    this.icon.destroy();
  }
};

scout.Group.prototype.setTitle = function(title) {
  this.setProperty('title', title);
};

scout.Group.prototype._renderTitle = function() {
  this.$title.textOrNbsp(this.title);
  this._updateIconStyle();
};

scout.Group.prototype.setTitleSuffix = function(titleSuffix) {
  this.setProperty('titleSuffix', titleSuffix);
};

scout.Group.prototype._renderTitleSuffix = function() {
  this.$titleSuffix.text(this.titleSuffix || '');
};

scout.Group.prototype.setHeaderVisible = function(headerVisible) {
  this.setProperty('headerVisible', headerVisible);
};

scout.Group.prototype._renderHeaderVisible = function() {
  this.$header.setVisible(this.headerVisible);
  this.invalidateLayoutTree();
};

scout.Group.prototype.setBody = function(body) {
  this.setProperty('body', body);
};

scout.Group.prototype._setBody = function(body) {
  if (!body) {
    // Create empty body if no body was provided
    body = scout.create('Widget', {
      parent: this,
      _render: function() {
        this.$container = this.$parent.appendDiv('group');
        this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
      }
    });
  }
  this._setProperty('body', body);
};

scout.Group.prototype._renderBody = function() {
  this.body.render();
  this.body.$container.addClass('group-body');
  this.body.invalidateLayoutTree();
};

/**
 * @override
 */
scout.Group.prototype.getFocusableElement = function() {
  if (!this.rendered) {
    return false;
  }
  return this.$header;
};

scout.Group.prototype.toggleCollapse = function() {
  this.setCollapsed(!this.collapsed && this.collapsible);
};

scout.Group.prototype.setCollapsed = function(collapsed) {
  this.setProperty('collapsed', collapsed);
};

scout.Group.prototype._renderCollapsed = function() {
  this.$container.toggleClass('collapsed', this.collapsed);
  this.$collapseIcon.toggleClass('collapsed', this.collapsed);
  if (!this.collapsed && !this.bodyAnimating) {
    this._renderBody();
  }
  if (this.rendered) {
    this.resizeBody();
  } else if (this.collapsed) {
    // Body will be removed after the animation, if there is no animation, remove it now
    this.body.remove();
  }
};

scout.Group.prototype.setCollapsible = function(collapsible) {
  this.setProperty('collapsible', collapsible);
};

scout.Group.prototype._renderCollapsible = function() {
  this.$header.toggleClass('disabled', !this.collapsible);
  this.$collapseIcon.toggleClass('hidden', !this.collapsible);
};

scout.Group.prototype.setCollapseStyle = function(collapseStyle) {
  this.setProperty('collapseStyle', collapseStyle);
};

scout.Group.prototype._renderCollapseStyle = function() {
  this.$header.toggleClass('collapse-right', this.collapseStyle === scout.Group.CollapseStyle.RIGHT);
  if (this.collapseStyle === scout.Group.CollapseStyle.RIGHT) {
    this.$collapseIcon.appendTo(this.$header);
  } else {
    this.$collapseIcon.prependTo(this.$header);
  }
};

scout.Group.prototype._onHeaderMouseDown = function(event) {
  this.setCollapsed(!this.collapsed && this.collapsible);
};

/**
 * Resizes the body to its preferred size by animating the height.
 */
scout.Group.prototype.resizeBody = function() {
  this.animateToggleCollapse().done(function() {
    if (this.bodyAnimating) {
      // Another animation has been started in the mean time -> ignore done event
      return;
    }
    if (this.collapsed) {
      this.body.remove();
    }
    this.invalidateLayoutTree();
  }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.Group.prototype.animateToggleCollapse = function(options) {
  var currentHeight = this.body.$container.cssHeight();
  var currentMargins = scout.graphics.margins(this.body.$container);
  var currentPaddings = scout.graphics.paddings(this.body.$container);
  var targetHeight, targetMargins, targetPaddings;

  if (this.collapsed) {
    // Collapsing
    // Set target values to 0 when collapsing
    targetHeight = 0;
    targetMargins = new scout.Insets();
    targetPaddings = new scout.Insets();
  } else {
    // Expanding
    // Expand to preferred size of the body
    targetHeight = this.body.htmlComp.prefSize().height;

    // Make sure body is layouted correctly before starting the animation (with the target size)
    // Use setSize to explicitly call its layout (this might even be necessary during the animation, see GroupLayout.invalidate)
    this.body.htmlComp.setSize(new scout.Dimension(this.body.$container.outerWidth(), targetHeight));

    if (this.bodyAnimating) {
      // The group may be expanded while being collapsed or vice verca.
      // In that case, use the current values of the inline style as starting values

      // Clear current insets to read target insets from CSS anew
      this.body.$container
        .cssMarginY('')
        .cssPaddingY('');
      targetMargins = scout.graphics.margins(this.body.$container);
      targetPaddings = scout.graphics.paddings(this.body.$container);
    } else {
      // If toggling is not already in progress, start expanding from 0
      currentHeight = 0;
      currentMargins = new scout.Insets();
      currentPaddings = new scout.Insets();
      targetMargins = scout.graphics.margins(this.body.$container);
      targetPaddings = scout.graphics.paddings(this.body.$container);
    }
  }

  this.bodyAnimating = true;
  if (this.collapsed) {
    this.$container.addClass('collapsing');
  }
  return this.body.$container
    .stop(true)
    .cssHeight(currentHeight)
    .cssMarginTop(currentMargins.top)
    .cssMarginBottom(currentMargins.bottom)
    .cssPaddingTop(currentPaddings.top)
    .cssPaddingBottom(currentPaddings.bottom)
    .animate({
      height: targetHeight,
      marginTop: targetMargins.top,
      marginBottom: targetMargins.bottom,
      paddingTop: targetPaddings.top,
      paddingBottom: targetPaddings.bottom
    }, {
      duration: 350,
      progress: function() {
        this.trigger('bodyHeightChange');
        this.revalidateLayoutTree();
      }.bind(this),
      complete: function() {
        this.bodyAnimating = false;
        if (this.body.rendered) {
          // Remove inline styles when finished
          this.body.$container.cssMarginY('');
          this.body.$container.cssPaddingY('');
        }
        if (this.rendered) {
          this.$container.removeClass('collapsing');
        }
        this.trigger('bodyHeightChangeDone');
      }.bind(this)
    })
    .promise();
};
