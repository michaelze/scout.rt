/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.io.Serializable;

public class NodePageState extends AbstractPageState implements Serializable {
  private static final long serialVersionUID = 1L;

  public NodePageState() {
    super();
  }

  protected NodePageState(NodePageState state) {
    super(state);
  }

  @Override
  public NodePageState copy() {
    return new NodePageState(this);
  }
}
