/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.yang.parser.builder.api;

import org.opendaylight.controller.yang.common.QName;
import org.opendaylight.controller.yang.model.api.SchemaPath;
import org.opendaylight.controller.yang.model.api.TypeDefinition;

/**
 * Builders of all nodes, which can have 'type' statement must implement this interface.
 * [typedef, type, leaf, leaf-list, deviate]
 */
public interface TypeAwareBuilder extends Builder {

    QName getQName();

    SchemaPath getPath();

    TypeDefinition<?> getType();

    TypeDefinitionBuilder getTypedef();

    void setType(TypeDefinition<?> type);

    void setTypedef(TypeDefinitionBuilder typedef);

}
