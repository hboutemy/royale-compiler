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

package org.apache.flex.compiler.problems;

import org.apache.flex.compiler.tree.as.IASNode;

public final class ScopedToDefaultNamespaceProblem extends SemanticWarningProblem
{
    
    public static final String DESCRIPTION =
        "${identifierName} will be scoped to the default namespace: ${className} ${INTERNAL}. It will not be visible outside of this package.";

    public static final int warningCode = 1084;

    public ScopedToDefaultNamespaceProblem(IASNode site, String identifierName, String className)
    {
        super(site);
        this.identifierName = identifierName;
        this.className = (className == null) ? "" : (className + ":");
    }
    
    public final String identifierName;
    public final String className;
    
    public final String INTERNAL = "internal";    
}
