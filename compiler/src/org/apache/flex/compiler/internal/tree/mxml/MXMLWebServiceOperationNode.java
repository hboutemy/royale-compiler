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

package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.mxml.MXMLTagData;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MXMLEmptyAttributeProblem;
import org.apache.flex.compiler.problems.MXMLRequiredAttributeProblem;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;

import static org.apache.flex.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Implementation of the {@link IMXMLWebServiceOperationNode} interface.
 */
class MXMLWebServiceOperationNode extends MXMLInstanceNode implements IMXMLWebServiceOperationNode
{
    /**
     * Create an AST node.
     * 
     * @param parent Parent node.
     */
    MXMLWebServiceOperationNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * {@code name} property value of this tag. This field is initialized in
     * {@link #initializationComplete()} method.
     */
    private String operationName;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLWebServiceOperationID;
    }

    /**
     * Such special tag doesn't have a ID.
     */
    @Override
    public final String getEffectiveID()
    {
        return null;
    }

    /**
     * Check for required {@code name} property.
     */
    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, MXMLTagData tag, MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        // Find 'name' property node.
        final IMXMLPropertySpecifierNode namePropertyNode = getPropertySpecifierNode(ATTRIBUTE_NAME);
        if (namePropertyNode != null)
        {
            final IMXMLInstanceNode namePropertyValueNode = namePropertyNode.getInstanceNode();
            if (namePropertyValueNode instanceof IMXMLStringNode)
            {
                final IMXMLStringNode stringNode = (IMXMLStringNode)namePropertyValueNode;
                operationName = stringNode.getValue();
            }
        }

        if (operationName == null)
        {
            ICompilerProblem problem = new MXMLRequiredAttributeProblem(tag, ATTRIBUTE_NAME);
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
        else if (operationName.isEmpty())
        {
            ICompilerProblem problem = new MXMLEmptyAttributeProblem(tag.getTagAttributeData(ATTRIBUTE_NAME));
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
    }

    /**
     * @return Value of the <code>name</code> attribute.
     */
    @Override
    public final String getOperationName()
    {
        return operationName;
    }
}
