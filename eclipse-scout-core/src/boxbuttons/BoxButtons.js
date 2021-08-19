/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, scout, Widget} from '../index';

/**
 * Button utility class for a set of buttons, where each button has an option value.
 *
 * Constructor arguments:
 *
 * @param $parent
 *          required
 * @param onClickHandler
 *          optional, global onClickHandler to attach to each button without a specific clickHandler
 */
export default class BoxButtons extends Widget {

  constructor() {
    super();
    this._addWidgetProperties(['buttons']);
    this._addPreserveOnPropertyChangeProperties(['defaultButtonIndex']);

    this.$container = null;
    this.buttons = [];
    this.defaultButtonIndex = 0;
  }

  /**
   * @override
   */
  _render() {
    this.$container = this.$parent.appendDiv('box-buttons');
  }

  /**
   * @override
   */
  _renderProperties() {
    super._renderProperties();
    this._renderButtons();
    this._renderDefaultButtonIndex();
  }

  _renderButtons() {
    this.buttons.forEach(function(button) {
      if (!button.rendered) {
        button.render(this.$container);
        button.$container.unfocusable().addClass('button box-button');
      }
    }, this);
  }

  _renderDefaultButtonIndex() {
    for (let i = 0; i < this.buttons.length; i++) {
      let button = this.buttons[i];
      button.setSelected(button.isVisible() && button.enabledComputed && this.isDefaultButtonIndex(i));
    }
  }

  isDefaultButtonIndex(index) {
    return index === this.defaultButtonIndex;
  }

  addButton(model, options) {
    model = model || {};
    model.parent = this;
    model.tabbable = true;
    model.actionStyle = Action.ActionStyle.BUTTON;
    model.preventDoubleClick = true;
    let button = scout.create('Action', model, options);
    this.buttons.push(button);
    return button;
  }

  setDefaultButtonIndex(index) {
    this.setProperty('defaultButtonIndex', index);
  }

  buttonCount() {
    return this.buttons.length;
  }
}
