/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.tree.as;

import org.apache.flex.compiler.internal.definitions.NamespaceDefinition;

/**
 * Represents a config directive definition
 */
public class ConfigNamespaceNode extends NamespaceNode
{
    /**
     * Constructor
     */
    public ConfigNamespaceNode(IdentifierNode name)
    {
        super(name);
    }
    
    //
    // BaseDefinitionNode overrides
    //

    @Override
    public NamespaceDefinition getDefinition()
    {
        if (definition == null)
        {
            // CONFIG namespaces can't be declared in a class
            // so we know it's not a static namespace.
            buildDefinition(false);
        }

        return super.getDefinition();
    }
}
