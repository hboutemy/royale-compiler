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

import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.parsing.IASToken;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;

/**
 * Final subclass of {@link BinaryOperatorNodeBase} for the '<code>,</code>' operator.
 */
public final class BinaryOperatorCommaNode extends BinaryOperatorNodeBase
{
    /**
     * Constructor.
     */
    public BinaryOperatorCommaNode(IASToken operatorToken,
                                   ExpressionNodeBase leftOperand,
                                   ExpressionNodeBase rightOperand)
    {
        super(operatorToken, leftOperand, rightOperand);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected BinaryOperatorCommaNode(BinaryOperatorCommaNode other)
    {
        super(other);
    }

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.Op_CommaID;
    }
    
    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // The type of (a, b) is the type of b.
        return getRightOperandNode().resolveType(project);
    }

    @Override
    protected BinaryOperatorCommaNode copy()
    {
        return new BinaryOperatorCommaNode(this);
    }

    //
    // OperatorNodeBase overrides
    //

    @Override
    public OperatorType getOperator()
    {
        return OperatorType.COMMA;
    }
}
