/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields;

import java.util.ArrayList;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.core.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.core.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.rap.core.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

public abstract class RwtScoutFieldComposite<T extends IFormField> extends RwtScoutComposite<T> implements IRwtScoutFormField<T> {
  private ILabelComposite m_label;
  private IRwtKeyStroke[] m_keyStrokes;

  private Color m_mandatoryFieldBackgroundColor;
  private OnFieldLabelDecorator m_onFieldLabelDecorator;

  @Override
  public ILabelComposite getUiLabel() {
    return m_label;
  }

  public OnFieldLabelDecorator getOnFieldLabelDecorator() {
    return m_onFieldLabelDecorator;
  }

  protected void setUiLabel(ILabelComposite label) {
    m_label = label;
    if (m_label != null && label.getLayoutData() == null) {
      LogicalGridData statusLabelGridData = null;
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
        statusLabelGridData = LogicalGridDataBuilder.createLabelOnTop(getScoutObject().getGridData());
      }
      else {
        statusLabelGridData = LogicalGridDataBuilder.createLabel(getScoutObject().getGridData());
      }

      m_label.setLayoutData(statusLabelGridData);
    }
  }

  public Color getMandatoryFieldBackgroundColor() {
    return m_mandatoryFieldBackgroundColor;
  }

  protected void setErrorStatusFromScout(IProcessingStatus s) {
    if (getUiLabel() != null) {
      getUiLabel().setStatus(s);
      getUiContainer().layout(true, true);
    }
  }

  public void setMandatoryFieldBackgroundColor(Color mandatoryFieldBackgroundColor) {
    m_mandatoryFieldBackgroundColor = mandatoryFieldBackgroundColor;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() != null) {
      setBackgroundFromScout(getScoutObject().getBackgroundColor());
      setForegroundFromScout(getScoutObject().getForegroundColor());
      setVisibleFromScout(getScoutObject().isVisible());
      setEnabledFromScout(getScoutObject().isEnabled());
      // bsh 2010-10-01: The "mandatory" state of a SequenceBoxes is always derived from the
      // inner fields. Don't use the model value (it will always be false).
      if (!(getScoutObject() instanceof ISequenceBox)) {
        setMandatoryFromScout(getScoutObject().isMandatory());
      }
      setErrorStatusFromScout(getScoutObject().getErrorStatus());
      setLabelFromScout(getScoutObject().getLabel());
      setLabelWidthInPixelFromScout();
      setLabelVisibleFromScout();
      setLabelPositionFromScout();
      setLabelHorizontalAlignmentFromScout();
      setTooltipTextFromScout(getScoutObject().getTooltipText());
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD && getScoutObject().getLabel() != null && getScoutObject().getTooltipText() == null) {
        setTooltipTextFromScout(getScoutObject().getLabel());
      }
      setFontFromScout(getScoutObject().getFont());
      setSaveNeededFromScout(getScoutObject().isSaveNeeded());
      setFocusableFromScout(getScoutObject().isFocusable());
      setFocusRequestedFromScout(getScoutObject().fetchFocusRequested());
      updateKeyStrokesFromScout();
    }
  }

  protected void setVisibleFromScout(boolean b) {
    boolean updateLayout = false;
    if (getUiContainer() != null && getUiContainer().getVisible() != b) {
      updateLayout = true;
      getUiContainer().setVisible(b);
    }
    else if (getUiField() != null && getUiField().getVisible() != b) {
      updateLayout = true;
      getUiField().setVisible(b);
    }
    if (updateLayout && isCreated()) {
      RwtLayoutUtility.invalidateLayout(getUiEnvironment(), getUiContainer());
    }
  }

  protected void setEnabledFromScout(boolean b) {
    boolean updateLayout = false;
    Control field = getUiField();
    if (field != null) {
      updateLayout = true;
      setFieldEnabled(field, b);
      if (b) {
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
      else {
        setForegroundFromScout(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
      }
    }
    if (getUiLabel() != null) {
      if (getUiLabel().getEnabled() != b) {
        updateLayout = true;
        getUiLabel().setEnabled(b);
      }
    }
    if (updateLayout && isCreated()) {
      RwtLayoutUtility.invalidateLayout(getUiEnvironment(), getUiContainer());
    }
  }

  /**
   * used to change enabled into read only
   * 
   * @param field
   * @param enabled
   */
  protected void setFieldEnabled(Control field, boolean enabled) {
    field.setEnabled(enabled);
  }

  protected void setMandatoryFromScout(boolean b) {
    String fieldBackgroundColorString = UiDecorationExtensionPoint.getLookAndFeel().getMandatoryFieldBackgroundColor();
    if (fieldBackgroundColorString != null) {
      Color color = null;
      if (b) {
        color = getUiEnvironment().getColor(fieldBackgroundColorString);
      }
      else {
        color = null;
      }
      if (getMandatoryFieldBackgroundColor() != color) {
        setMandatoryFieldBackgroundColor(color);
        setBackgroundFromScout(getScoutObject().getBackgroundColor());
      }
    }
    if (getUiLabel() != null) {
      if (getUiLabel().setMandatory(b)) {
        if (isCreated()) {
          RwtLayoutUtility.invalidateLayout(getUiEnvironment(), getUiContainer());
        }
      }

      //In case of on field labels it is necessary to recompute the label visibility if the mandatory status changes.
      setLabelVisibleFromScout();
    }
  }

  protected void setLabelPositionFromScout() {
    if (getUiField() != null) {
      if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
        if (m_onFieldLabelDecorator == null) {
          m_onFieldLabelDecorator = new OnFieldLabelDecorator(getUiField(), getScoutObject().isMandatory(), this);
          m_onFieldLabelDecorator.setLabel(getScoutObject().getLabel());
        }
        m_onFieldLabelDecorator.attach(getUiField());
      }
      else {
        if (m_onFieldLabelDecorator != null) {
          m_onFieldLabelDecorator.detach(getUiField());
        }
      }
      getUiField().redraw();
    }
    setLabelVisibleFromScout();
    setLabelFromScout(getScoutObject().getLabel());
  }

  protected void setLabelWidthInPixelFromScout() {
    if (getUiLabel() == null) {
      return;
    }

    int w = getScoutObject().getLabelWidthInPixel();
    if (w > 0) {
      getUiLabel().setLayoutWidthHint(w);
    }
    else if (w == IFormField.LABEL_WIDTH_DEFAULT) {
      getUiLabel().setLayoutWidthHint(UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelWidth());
    }
    else if (w == IFormField.LABEL_WIDTH_UI) {
      getUiLabel().setLayoutWidthHint(0);
    }
  }

  protected void setLabelHorizontalAlignmentFromScout() {
    // XXX not supported by swt to dynamically change style of a widget
  }

  protected void setLabelFromScout(String s) {
    if (m_onFieldLabelDecorator != null) {
      m_onFieldLabelDecorator.setLabel(s);
    }
    else {
      if (m_label != null && s != null) {
        m_label.setText(s);
      }
    }
  }

  protected void setLabelVisibleFromScout() {
    if (m_label == null) {
      return;
    }

    boolean visible = getScoutObject().isLabelVisible();
    if (getScoutObject().getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
      //Make the label as small as possible in order to show the mandatory marker (*).
      m_label.setText(null);
      m_label.setLayoutWidthHint(0);

      //Unfortunately the label can't be removed completely by only setting the width hint to 0.
      //So it is necessary to make it invisible if it is really not necessary.
      if (!getScoutObject().isMandatory()) {
        visible = false;
      }
    }
    m_label.setVisible(visible);
    if (getUiContainer() != null) {
      getUiContainer().layout(true, true);
    }
  }

  protected void setTooltipTextFromScout(String s) {
    if (getUiField() != null) {
      getUiField().setToolTipText(s);
    }
  }

  protected void setBackgroundFromScout(String scoutColor) {
    if (getUiField() != null) {
      Control fld = getUiField();
      Color initCol = (Color) fld.getData(CLIENT_PROP_INITIAL_BACKGROUND);
      //only set color if scoutColor is not null/empty or if color was set one time
      if (StringUtility.hasText(scoutColor) || initCol != null) {
        if (fld.getData(CLIENT_PROP_INITIAL_BACKGROUND) == null) {
          initCol = fld.getBackground();
          fld.setData(CLIENT_PROP_INITIAL_BACKGROUND, initCol);
        }
        Color c = getUiEnvironment().getColor(scoutColor);
        if (getMandatoryFieldBackgroundColor() != null) {
          c = getMandatoryFieldBackgroundColor();
        }
        if (c == null) {
          c = initCol;
          //if field is reseted to init color data can be reseted to null
          fld.setData(CLIENT_PROP_INITIAL_BACKGROUND, null);
        }
        fld.setBackground(c);
      }
    }
  }

  protected void setForegroundFromScout(String scoutColor) {
    if (getUiField() != null) {
      Control fld = getUiField();
      Color initCol = (Color) fld.getData(CLIENT_PROP_INITIAL_FOREGROUND);
      //only set color if scoutColor is not null/empty or if color was set one time
      if (StringUtility.hasText(scoutColor) || initCol != null) {
        if (initCol == null) {
          initCol = fld.getForeground();
          fld.setData(CLIENT_PROP_INITIAL_FOREGROUND, initCol);
        }
        Color c = getUiEnvironment().getColor(scoutColor);
        if (c == null) {
          c = initCol;
          //if field is reseted to init color data can be reseted to null
          fld.setData(CLIENT_PROP_INITIAL_FOREGROUND, null);
        }
        fld.setForeground(c);
      }
    }
  }

  protected void setFontFromScout(FontSpec scoutFont) {
    if (getUiField() != null) {
      Control fld = getUiField();
      Font initFont = (Font) fld.getData(CLIENT_PROP_INITIAL_FONT);
      //only set color if scoutColor is not null/empty or if color was set one time
      if (scoutFont != null || initFont != null) {
        if (fld.getData(CLIENT_PROP_INITIAL_FONT) == null) {
          initFont = fld.getFont();
          fld.setData(CLIENT_PROP_INITIAL_FONT, initFont);
        }
        Font f = getUiEnvironment().getFont(scoutFont, initFont);
        if (f == null) {
          f = initFont;
        }
        if (initFont == null || !initFont.equals(f)) {
          // only set the new font if it is different to the current one
          fld.setFont(f);
        }
      }
      if (isCreated()) {
        RwtLayoutUtility.invalidateLayout(getUiEnvironment(), getUiContainer());
      }
    }
  }

  protected void setSaveNeededFromScout(boolean b) {
  }

  protected void setFocusableFromScout(boolean b) {
  }

  protected void updateEmptyFromScout() {
  }

  protected void updateKeyStrokesFromScout() {
    // key strokes
    Control widget = getUiContainer();
    if (widget == null) {
      widget = getUiField();
    }
    if (widget != null) {

      // remove old
      if (m_keyStrokes != null) {
        for (IRwtKeyStroke uiKeyStroke : m_keyStrokes) {
          getUiEnvironment().removeKeyStroke(widget, uiKeyStroke);
        }
      }

      ArrayList<IRwtKeyStroke> newUiKeyStrokes = new ArrayList<IRwtKeyStroke>();
      IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKeyStroke : scoutKeyStrokes) {
        IRwtKeyStroke[] uiStrokes = RwtUtility.getKeyStrokes(scoutKeyStroke, getUiEnvironment());
        for (IRwtKeyStroke uiStroke : uiStrokes) {
          getUiEnvironment().addKeyStroke(widget, uiStroke, false);
          newUiKeyStrokes.add(uiStroke);
        }
      }
      m_keyStrokes = newUiKeyStrokes.toArray(new IRwtKeyStroke[newUiKeyStrokes.size()]);
    }
  }

  protected void setFocusRequestedFromScout(boolean b) {
    if (getUiField() != null) {
      if (getUiField().isVisible()) {
        if (b) {
          getUiField().setFocus();
        }
      }
    }
  }

  //runs in scout job
  @Override
  protected boolean isHandleScoutPropertyChange(final String name, final Object newValue) {
    if (name.equals(IFormField.PROP_ENABLED) || name.equals(IFormField.PROP_VISIBLE)) {
      //add immediate change to rwt environment to support TAB traversal to component that changes from disabled to enabled.
      getUiEnvironment().postImmediateUiJob(new Runnable() {
        @Override
        public void run() {
          handleScoutPropertyChange(name, newValue);
        }
      });
    }
    return super.isHandleScoutPropertyChange(name, newValue);
  }

  //runs in gui thread
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFormField.PROP_ENABLED)) {
      setEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_FOCUSABLE)) {
      setFocusableFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_FOCUS_REQUESTED)) {
      getScoutObject().fetchFocusRequested();
      setFocusRequestedFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_LABEL)) {
      setLabelFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_LABEL_VISIBLE)) {
      setLabelVisibleFromScout();
    }
    else if (name.equals(IFormField.PROP_TOOLTIP_TEXT)) {
      setTooltipTextFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_VISIBLE)) {
      setVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_MANDATORY)) {
      setMandatoryFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_ERROR_STATUS)) {
      setErrorStatusFromScout((IProcessingStatus) newValue);
    }
    else if (name.equals(IFormField.PROP_FOREGROUND_COLOR)) {
      setForegroundFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_BACKGROUND_COLOR)) {
      setBackgroundFromScout((String) newValue);
    }
    else if (name.equals(IFormField.PROP_FONT)) {
      setFontFromScout((FontSpec) newValue);
    }
    else if (name.equals(IFormField.PROP_SAVE_NEEDED)) {
      setSaveNeededFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_EMPTY)) {
      updateEmptyFromScout();
    }
    else if (name.equals(IFormField.PROP_KEY_STROKES)) {
      updateKeyStrokesFromScout();
    }
  }

}
