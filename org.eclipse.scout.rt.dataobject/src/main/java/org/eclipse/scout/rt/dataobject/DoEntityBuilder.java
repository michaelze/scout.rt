/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Simple builder for ad-hoc {@link IDoEntity} instances.
 */
@Bean
public class DoEntityBuilder {

  protected IDoEntity m_entity;

  @PostConstruct
  protected void init() {
    m_entity = BEANS.get(DoEntity.class);
  }

  /**
   * Adds new value to attribute map of entity.
   */
  public DoEntityBuilder put(String attributeName, Object value) {
    m_entity.put(attributeName, value);
    return this;
  }

  /**
   * Adds new value to attribute map of entity if the value satisfies the given {@code predicate}.
   */
  public DoEntityBuilder putIf(String attributeName, Object value, Predicate<? super Object> predicate) {
    m_entity.putIf(attributeName, value, predicate);
    return this;
  }

  /**
   * Adds new list value to attribute map of entity.
   * <p>
   * If {@code value} is null, an empty list is added.
   */
  public <V> DoEntityBuilder putList(String attributeName, List<V> value) {
    m_entity.putList(attributeName, value);
    return this;
  }

  /**
   * Adds new list value to attribute map of entity if the value satisfies the given {@code predicate}.
   */
  public <V> DoEntityBuilder putListIf(String attributeName, List<V> value, Predicate<? super List<V>> predicate) {
    m_entity.putListIf(attributeName, value, predicate);
    return this;
  }

  /**
   * Adds list of values to attribute map of entity.
   * <p>
   * If {@code value} is null, an empty list is added.
   */
  public <V> DoEntityBuilder putList(String attributeName, @SuppressWarnings("unchecked") V... values) {
    m_entity.putList(attributeName, CollectionUtility.arrayList(values));
    return this;
  }

  /**
   * @return built {@link IDoEntity} instance
   */
  public IDoEntity build() {
    return m_entity;
  }

  /**
   * @return serialized {@link String} representation of builded {@link IDoEntity}
   */
  public String buildString() {
    return BEANS.get(IDataObjectMapper.class).writeValue(m_entity);
  }
}
