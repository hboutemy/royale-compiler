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

package org.apache.royale.compiler.internal.codegen.js.royale;

import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleEmitter;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.ImplicitBindableImplementation;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.jx.AccessorEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.AsIsEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.DefinePropertyFunctionEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ForEachEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.InterfaceEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.LiteralEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.MethodEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ObjectDefinePropertyEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.SelfReferenceEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.VarDeclarationEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyaleEmitter;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.embedding.EmbedMIMEType;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.FlexProject;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.problems.EmbedUnableToReadSourceProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IEmbedNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IFunctionObjectNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.ITypedExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.utils.ASNodeUtils;

import com.google.common.base.Joiner;
import org.apache.royale.compiler.utils.NativeUtils;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Concrete implementation of the 'Royale' JavaScript production.
 *
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSRoyaleEmitter extends JSGoogEmitter implements IJSRoyaleEmitter
{

    private JSRoyaleDocEmitter docEmitter = null;

    private PackageHeaderEmitter packageHeaderEmitter;
    public PackageFooterEmitter packageFooterEmitter;

    private BindableEmitter bindableEmitter;

    private ClassEmitter classEmitter;
    private InterfaceEmitter interfaceEmitter;

    private FieldEmitter fieldEmitter;
    public VarDeclarationEmitter varDeclarationEmitter;
    public AccessorEmitter accessorEmitter;
    public MethodEmitter methodEmitter;

    private FunctionCallEmitter functionCallEmitter;
    private SuperCallEmitter superCallEmitter;
    private ForEachEmitter forEachEmitter;
    private MemberAccessEmitter memberAccessEmitter;
    private BinaryOperatorEmitter binaryOperatorEmitter;
    private IdentifierEmitter identifierEmitter;
    private LiteralEmitter literalEmitter;

    private AsIsEmitter asIsEmitter;
    private SelfReferenceEmitter selfReferenceEmitter;
    private ObjectDefinePropertyEmitter objectDefinePropertyEmitter;
    private DefinePropertyFunctionEmitter definePropertyFunctionEmitter;

    public ArrayList<String> usedNames = new ArrayList<String>();
    public ArrayList<String> staticUsedNames = new ArrayList<String>();
    private boolean needNamespace;

    @Override
    public String postProcess(String output)
    {
        output = super.postProcess(output);

    	String[] lines = output.split("\n");
    	ArrayList<String> finalLines = new ArrayList<String>();
        boolean foundLanguage = false;
        boolean foundXML = false;
        boolean foundNamespace = false;
        boolean sawRequires = false;
    	boolean stillSearching = true;
        int addIndex = -1;
        int provideIndex = -1;
        int len = lines.length;
    	for (int i = 0; i < len; i++)
    	{
            String line = lines[i];
    		if (stillSearching)
    		{
                int c = line.indexOf(JSGoogEmitterTokens.GOOG_PROVIDE.getToken());
                if (c != -1)
                {
                    // if zero requires are found, require Language after the
                    // call to goog.provide
                    provideIndex = addIndex = i + 1;
                }
	            c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            if (c != -1)
	            {
                    // we found other requires, so we'll just add Language at
                    // the end of the list
                    addIndex = -1;
	                int c2 = line.indexOf(")");
	                String s = line.substring(c + 14, c2 - 1);
                    if (s.equals(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken()))
                    {
                        foundLanguage = true;
                    }
                    else if (s.equals(IASLanguageConstants.XML))
                    {
                        foundXML = true;
                    }
                    else if (s.equals(IASLanguageConstants.Namespace))
                    {
                        foundNamespace = true;
                    }
	    			sawRequires = true;
	    			if (!usedNames.contains(s))
                    {
                        removeLineFromMappings(i);
                        continue;
                    }
	    		}
	    		else if (sawRequires || i == len - 1)
                {
                    stillSearching = false;

                    //when we emitted the requires based on the imports, we may
                    //not have known if Language was needed yet because the
                    //imports are at the beginning of the file. other code,
                    //later in the file, may require Language.
                    ICompilerProject project = getWalker().getProject();
                    if (project instanceof RoyaleProject)
                    {
                        RoyaleProject flexJSProject = (RoyaleProject) project;
                        boolean needLanguage = getModel().needLanguage;
                        if (needLanguage && !foundLanguage)
                        {
                            StringBuilder appendString = new StringBuilder();
                            appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
                            appendString.append(ASEmitterTokens.SEMICOLON.getToken());
                            if(addIndex != -1)
                            {
                                // if we didn't find other requires, this index
                                // points to the line after goog.provide
                                finalLines.add(addIndex, appendString.toString());
                                addLineToMappings(addIndex);
                            }
                            else
                            {
                                finalLines.add(appendString.toString());
                                addLineToMappings(i);
                            }
                        }
                        boolean needXML = flexJSProject.needXML;
                        if (needXML && !foundXML)
                        {
                            StringBuilder appendString = new StringBuilder();
                            appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(IASLanguageConstants.XML);
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
                            appendString.append(ASEmitterTokens.SEMICOLON.getToken());
                            if(addIndex != -1)
                            {
                                // if we didn't find other requires, this index
                                // points to the line after goog.provide
                                finalLines.add(addIndex, appendString.toString());
                                addLineToMappings(addIndex);
                            }
                            else
                            {
                                finalLines.add(appendString.toString());
                                addLineToMappings(i);
                            }
                        }
                        if (needNamespace && !foundNamespace)
                        {
                            StringBuilder appendString = new StringBuilder();
                            appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(IASLanguageConstants.Namespace);
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
                            appendString.append(ASEmitterTokens.SEMICOLON.getToken());
                            if(addIndex != -1)
                            {
                                // if we didn't find other requires, this index
                                // points to the line after goog.provide
                                finalLines.add(addIndex, appendString.toString());
                                addLineToMappings(addIndex);
                            }
                            else
                            {
                                finalLines.add(appendString.toString());
                                addLineToMappings(i);
                            }
                        }
                    }
                }
    		}
    		finalLines.add(line);
    	}
		if (staticUsedNames.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(JSGoogEmitterTokens.FLEXJS_STATIC_DEPENDENCY_LIST.getToken());
			boolean firstDependency = true;
			for (String staticName : staticUsedNames)
			{
				if (!firstDependency)
					sb.append(",");
				firstDependency = false;
				sb.append(staticName);
			}
			sb.append("*/");
			finalLines.add(provideIndex, sb.toString());
		}

    	return Joiner.on("\n").join(finalLines);
    }

    public BindableEmitter getBindableEmitter()
    {
        return bindableEmitter;
    }

    public FieldEmitter getFieldEmitter()
    {
        return fieldEmitter;
    }

    public ClassEmitter getClassEmitter()
    {
        return classEmitter;
    }

    public AccessorEmitter getAccessorEmitter()
    {
        return accessorEmitter;
    }

    public PackageFooterEmitter getPackageFooterEmitter()
    {
        return packageFooterEmitter;
    }

    // TODO (mschmalle) Fix; this is not using the backend doc strategy for replacement
    @Override
    public IJSGoogDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
            docEmitter = new JSRoyaleDocEmitter(this);
        return docEmitter;
    }

    public JSRoyaleEmitter(FilterWriter out)
    {
        super(out);

        packageHeaderEmitter = new PackageHeaderEmitter(this);
        packageFooterEmitter = new PackageFooterEmitter(this);

        bindableEmitter = new BindableEmitter(this);

        classEmitter = new ClassEmitter(this);
        interfaceEmitter = new InterfaceEmitter(this);

        fieldEmitter = new FieldEmitter(this);
        varDeclarationEmitter = new VarDeclarationEmitter(this);
        accessorEmitter = new AccessorEmitter(this);
        methodEmitter = new MethodEmitter(this);

        functionCallEmitter = new FunctionCallEmitter(this);
        superCallEmitter = new SuperCallEmitter(this);
        forEachEmitter = new ForEachEmitter(this);
        memberAccessEmitter = new MemberAccessEmitter(this);
        binaryOperatorEmitter = new BinaryOperatorEmitter(this);
        identifierEmitter = new IdentifierEmitter(this);
        literalEmitter = new LiteralEmitter(this);

        asIsEmitter = new AsIsEmitter(this);
        selfReferenceEmitter = new SelfReferenceEmitter(this);
        objectDefinePropertyEmitter = new ObjectDefinePropertyEmitter(this);
        definePropertyFunctionEmitter = new DefinePropertyFunctionEmitter(this);

    }

    @Override
    protected void writeIndent()
    {
        write(JSRoyaleEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSRoyaleEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    @Override
    public void emitLocalNamedFunction(IFunctionNode node)
    {
		IFunctionNode fnNode = (IFunctionNode)node.getAncestorOfType(IFunctionNode.class);
    	if (fnNode.getEmittingLocalFunctions())
    	{
    		super.emitLocalNamedFunction(node);
    	}
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
    	node.setEmittingLocalFunctions(true);
    	super.emitFunctionBlockHeader(node);
    	if (node.isConstructor())
    	{
            IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);
            if (cnode.getDefinition().needsEventDispatcher(getWalker().getProject())) {
                //add whatever variant of the implementation is necessary inside the constructor
                if (getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.IMPLEMENTS)
                    bindableEmitter.emitBindableImplementsConstructorCode();
                else if (getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.EXTENDS)
                    bindableEmitter.emitBindableExtendsConstructorCode(cnode.getDefinition().getQualifiedName(),false);
            }
            emitComplexInitializers(cnode);

    	}
        if (node.containsLocalFunctions())
        {
            List<IFunctionNode> anonFns = node.getLocalFunctions();
            int n = anonFns.size();
            for (int i = 0; i < n; i++)
            {
                IFunctionNode anonFn = anonFns.get(i);
                if (anonFn.getParent().getNodeID() == ASTNodeID.AnonymousFunctionID)
                {
                    write("var /** @type {Function} */ __localFn" + Integer.toString(i) + "__ = ");
                	getWalker().walk(anonFn.getParent());
                }
                else
                {
                	getWalker().walk(anonFn);
                	write(ASEmitterTokens.SEMICOLON);
                }
                this.writeNewline();
            }
        }
    	node.setEmittingLocalFunctions(false);
    }

    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
		IFunctionNode fnNode = (IFunctionNode)node.getAncestorOfType(IFunctionNode.class);
    	if (fnNode == null || fnNode.getEmittingLocalFunctions())
    	{
    		super.emitFunctionObject(node);
    	}
    	else
    	{
            List<IFunctionNode> anonFns = fnNode.getLocalFunctions();
            int i = anonFns.indexOf(node.getFunctionNode());
            if (i < 0)
            	System.out.println("missing index for " + node.toString());
            else
            	write("__localFn" + Integer.toString(i) + "__");
    	}
    }

    @Override
    public void emitNamespace(INamespaceNode node)
    {
    	needNamespace = true;
        write(formatQualifiedName(node.getQualifiedName()));
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        writeToken(ASEmitterTokens.NEW);
        write(IASLanguageConstants.Namespace);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getNamespaceURINode());
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }

    public boolean isCustomNamespace(FunctionNode node)
    {
		INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
		if (ns != null)
		{
            String nsName = node.getNamespace();
            if (!(nsName == IASKeywordConstants.PRIVATE ||
                nsName == IASKeywordConstants.PROTECTED ||
                nsName == IASKeywordConstants.INTERNAL ||
                nsName == INamespaceConstants.AS3URI ||
                nsName == IASKeywordConstants.PUBLIC))
            {
            	return true;
            }
		}
		return false;
    }

    public boolean isCustomNamespace(FunctionDefinition def)
    {
		INamespaceDefinition nsDef = def.getNamespaceReference().resolveNamespaceReference(getWalker().getProject());
		String uri = nsDef.getURI();
		if (!def.getNamespaceReference().isLanguageNamespace() && !uri.equals(INamespaceConstants.AS3URI) && 
						!nsDef.getBaseName().equals(ASEmitterTokens.PRIVATE.getToken()))
			return true;
		return false;
    }

    @Override
    public void emitMemberName(IDefinitionNode node)
    {
    	if (node.getNodeID() == ASTNodeID.FunctionID)
    	{
    		FunctionNode fn = (FunctionNode)node;
    		if (isCustomNamespace(fn))
    		{
    			INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
                ICompilerProject project = getWalker().getProject();
    			INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
    			formatQualifiedName(nsDef.getQualifiedName()); // register with used names
    			String s = nsDef.getURI();
    			write("[\"" + s + "::" + node.getName() + "\"]");
    			return;
    		}
    	}
        write(node.getName());
    }

    @Override
    public String formatQualifiedName(String name)
    {
        return formatQualifiedName(name, false);
    }

    public MXMLRoyaleEmitter mxmlEmitter = null;

    public String formatQualifiedName(String name, boolean isDoc)
    {
    	if (mxmlEmitter != null)
    		name = mxmlEmitter.formatQualifiedName(name);
        /*
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        */
    	if (getModel().isInternalClass(name))
    		return getModel().getInternalClasses().get(name);
        if (NativeUtils.isJSNative(name)) return name;
    	if (name.startsWith("window."))
    		name = name.substring(7);
    	else if (!isDoc)
    	{
        	if (getModel().inStaticInitializer)
        		if (!staticUsedNames.contains(name) && !NativeUtils.isJSNative(name))
        			staticUsedNames.add(name);
    		
    		if (!usedNames.contains(name) && !isExternal(name))
    			usedNames.add(name);
    	}
        return name;
    }

    public String convertASTypeToJS(String name)
    {
        String result = name;

        if (name.equals(""))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants.Class))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants._int)
                || name.equals(IASLanguageConstants.uint))
            result = IASLanguageConstants.Number;

        boolean isBuiltinFunction = name.matches("Vector\\.<.*>");
        if (isBuiltinFunction)
        {
        	result = IASLanguageConstants.Array;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // Package Level
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
    	IPackageNode packageNode = definition.getNode();
    	IFileNode fileNode = (IFileNode) packageNode.getAncestorOfType(IFileNode.class);
        int nodeCount = fileNode.getChildCount();
        String mainClassName = null;
        for (int i = 0; i < nodeCount; i++)
        {
	        IASNode pnode = fileNode.getChild(i);

	        if (pnode instanceof IPackageNode)
	        {
	        	IScopedNode snode = ((IPackageNode)pnode).getScopedNode();
	            int snodeCount = snode.getChildCount();
	            for (int j = 0; j < snodeCount; j++)
	            {
	    	        IASNode cnode = snode.getChild(j);
	    	        if (cnode instanceof IClassNode)
	    	        {
	    	        	mainClassName = ((IClassNode)cnode).getQualifiedName();
	    	        	break;
	    	        }
	    	        else if (j == 0)
	    	        {
	    	            if (cnode instanceof IFunctionNode)
	    	            {
	    	                mainClassName = ((IFunctionNode)cnode).getQualifiedName();
	    	            }
	    	            else if (cnode instanceof INamespaceNode)
	    	            {
	    	            	mainClassName = ((INamespaceNode)cnode).getQualifiedName();
	    	            }
	    	            else if (cnode instanceof IVariableNode)
	    	            {
	    	            	mainClassName = ((IVariableNode)cnode).getQualifiedName();
	    	            }
	    	        	
	    	        }
	            }
	        }
	        else if (pnode instanceof IClassNode)
	        {
	        	String className = ((IClassNode)pnode).getQualifiedName();
	        	getModel().getInternalClasses().put(className, mainClassName + "." + className);
	        }
	        else if (pnode instanceof IInterfaceNode)
	        {
	        	String className = ((IInterfaceNode)pnode).getQualifiedName();
	        	getModel().getInternalClasses().put(className, mainClassName + "." + className);
	        }
            else if (pnode instanceof IFunctionNode)
            {
                String className = ((IFunctionNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
            }
            else if (pnode instanceof INamespaceNode)
            {
                String className = ((INamespaceNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
            }
            else if (pnode instanceof IVariableNode)
            {
                String className = ((IVariableNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
            }
        }

        packageHeaderEmitter.emit(definition);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        packageHeaderEmitter.emitContents(definition);
        usedNames.clear();
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        packageFooterEmitter.emit(definition);
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        classEmitter.emit(node);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        interfaceEmitter.emit(node);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        fieldEmitter.emit(node);
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        varDeclarationEmitter.emit(node);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
        accessorEmitter.emit(node);
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        accessorEmitter.emitGet(node);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        accessorEmitter.emitSet(node);
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        methodEmitter.emit(node);
    }

    public void emitComplexInitializers(IClassNode node)
    {
    	classEmitter.emitComplexInitializers(node);
    }

    //--------------------------------------------------------------------------
    // Statements
    //--------------------------------------------------------------------------

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        functionCallEmitter.emit(node);
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        forEachEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Expressions
    //--------------------------------------------------------------------------

    @Override
    public void emitSuperCall(IASNode node, String type)
    {
        superCallEmitter.emit(node, type);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        memberAccessEmitter.emit(node);
    }

    @Override
    public void emitArguments(IContainerNode node)
    {
        IContainerNode newNode = node;
        int len = node.getChildCount();
    	if (len == 2)
    	{
            ICompilerProject project = getWalker().getProject();;
    		IFunctionCallNode fcNode = (IFunctionCallNode) node.getParent();
    		IExpressionNode nameNode = fcNode.getNameNode();
            IDefinition def = nameNode.resolve(project);
        	if (def != null && def.getBaseName().equals("insertAt"))
        	{
        		if (def.getParent() != null &&
            		def.getParent().getQualifiedName().equals("Array"))
        		{
            		if (nameNode instanceof MemberAccessExpressionNode)
            		{
                        newNode = EmitterUtils.insertArgumentsAt(node, 1, new NumericLiteralNode("0"));
            		}
    			}
    		}
    	}
        if (len == 1)
        {
            ICompilerProject project = getWalker().getProject();;
            IFunctionCallNode fcNode = (IFunctionCallNode) node.getParent();
            IExpressionNode nameNode = fcNode.getNameNode();
            IDefinition def = nameNode.resolve(project);
            if (def != null && def.getBaseName().equals("removeAt"))
            {
                if (def.getParent() != null &&
                        def.getParent().getQualifiedName().equals("Array"))
                {
                    if (nameNode instanceof MemberAccessExpressionNode)
                    {
                        newNode = EmitterUtils.insertArgumentsAfter(node, new NumericLiteralNode("1"));
                    }
                }
            }
            else if (def != null && def.getBaseName().equals("parseInt"))
            {
                IDefinition parentDef = def.getParent();
                if (parentDef == null)
                {
                    if (nameNode instanceof IdentifierNode)
                    {
                        //see FLEX-35283
                        LiteralNode appendedArgument = new NumericLiteralNode("undefined");
                        appendedArgument.setSynthetic(true);
                        newNode = EmitterUtils.insertArgumentsAfter(node, appendedArgument);
                    }
                }
            }
        }
        super.emitArguments(newNode);
    }

    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
    	getModel().inE4xFilter = true;
    	getWalker().walk(node.getLeftOperandNode());
    	write(".filter(function(node){return (node.");
    	String s = stringifyNode(node.getRightOperandNode());
    	if (s.startsWith("(") && s.endsWith(")"))
    		s = s.substring(1, s.length() - 1);
    	write(s);
    	write(")})");
    	getModel().inE4xFilter = false;
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperatorEmitter.emit(node);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        identifierEmitter.emit(node);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        literalEmitter.emit(node);
    }

    @Override
    public void emitEmbed(IEmbedNode node)
    {
    	// if the embed is text/plain, return the actual text from the file.
    	// this assumes the variable being initialized is of type String.
    	// Embed node seems to not have location, so use parent.
        EmbedData data = new EmbedData(node.getParent().getSourcePath(), null);
        boolean hadError = false;
        for (IMetaTagAttribute attribute : node.getAttributes())
        {
            String key = attribute.getKey();
            String value = attribute.getValue();
            if (data.addAttribute((CompilerProject) project, node.getParent(), key, value, getProblems()))
            {
                hadError = true;
            }
        }
        if (hadError)
        {
        	write("");
        	return;
        }
    	String source = (String) data.getAttribute(EmbedAttribute.SOURCE);
    	EmbedMIMEType mimeType = (EmbedMIMEType) data.getAttribute(EmbedAttribute.MIME_TYPE);
        if (mimeType != null && mimeType.toString().equals(EmbedMIMEType.TEXT.toString()) && source != null)
        {
            File file = new File(FilenameNormalization.normalize(source));
    		try {
    	        String newlineReplacement = "\\\\n";
				String s = FileUtils.readFileToString(file);
	            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
	            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
	            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
	            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
	            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
	            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
	            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
	            s = s.replaceAll("\"", "\\\\\"");
	            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
	            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
	            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
	            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
	            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
	            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
	            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", newlineReplacement);
				write("\"" + s + "\"");
			} catch (IOException e) {
	            getProblems().add(new EmbedUnableToReadSourceProblem(e, file.getPath()));
			}
        }
    }


    //--------------------------------------------------------------------------
    // Specific
    //--------------------------------------------------------------------------

    public void emitIsAs(IExpressionNode node, IExpressionNode left, IExpressionNode right, ASTNodeID id, boolean coercion)
    {
        asIsEmitter.emitIsAs(node, left, right, id, coercion);
    }

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        selfReferenceEmitter.emit(node);
    }

    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        objectDefinePropertyEmitter.emit(node);
    }

    @Override
    public void emitDefinePropertyFunction(IAccessorNode node)
    {
        definePropertyFunctionEmitter.emit(node);
    }

    public String stringifyDefineProperties(IClassDefinition cdef)
    {
    	setBufferWrite(true);
    	accessorEmitter.emit(cdef);
        String result = getBuilder().toString();
        getBuilder().setLength(0);
        setBufferWrite(false);
        return result;
    }

    @Override
	public void emitClosureStart()
    {
        ICompilerProject project = getWalker().getProject();;
        if (project instanceof RoyaleProject)
        	((RoyaleProject)project).needLanguage = true;
        getModel().needLanguage = true;
        write(JSRoyaleEmitterTokens.CLOSURE_FUNCTION_NAME);
        write(ASEmitterTokens.PAREN_OPEN);
    }

    @Override
	public void emitClosureEnd(IASNode node, IDefinition nodeDef)
    {
    	write(ASEmitterTokens.COMMA);
    	write(ASEmitterTokens.SPACE);
    	write(ASEmitterTokens.SINGLE_QUOTE);
    	if (node.getNodeID() == ASTNodeID.IdentifierID)
    	{
        	if (nodeDef instanceof FunctionDefinition &&
        			isCustomNamespace((FunctionDefinition)nodeDef))
        	{
            	String ns = ((FunctionDefinition)nodeDef).getNamespaceReference().resolveAETNamespace(getWalker().getProject()).getName();
            	write(ns + "::");
        	}
    		write(((IIdentifierNode)node).getName());
    	}
    	else if (node.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    		writeChainName(node);
    	else
    		System.out.println("unexpected node in emitClosureEnd");
    	write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitStatement(IASNode node)
    {
    	// don't emit named local functions as statements
    	// they are emitted as part of the function block header
    	if (node.getNodeID() == ASTNodeID.FunctionID)
    	{
    		return;
    	}
    	super.emitStatement(node);
    }
    private void writeChainName(IASNode node)
    {
    	while (node.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    	{
    		node = ((IMemberAccessExpressionNode)node).getRightOperandNode();
    	}
    	if (node.getNodeID() == ASTNodeID.IdentifierID)
    		write(((IdentifierNode)node).getName());
    	else
    		System.out.println("unexpected node in emitClosureEnd");
    }

    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        if (node.getNodeID() == ASTNodeID.Op_DeleteID)
        {
        	if (node.getChild(0).getNodeID() == ASTNodeID.ArrayIndexExpressionID)
        	{
        		if (node.getChild(0).getChild(0).getNodeID() == ASTNodeID.MemberAccessExpressionID)
        		{
        			MemberAccessExpressionNode obj = (MemberAccessExpressionNode)(node.getChild(0).getChild(0));
        			if (isXMLList(obj))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);

        	            getWalker().walk(obj);
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".removeChildAt(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        		}
        		else if (node.getChild(0).getChild(0).getNodeID() == ASTNodeID.IdentifierID)
        		{
        			if (isXML((IdentifierNode)(node.getChild(0).getChild(0))))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);

        	            getWalker().walk(node.getChild(0).getChild(0));
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".removeChild(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        			else if (isProxy((IdentifierNode)(node.getChild(0).getChild(0))))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);

        	            getWalker().walk(node.getChild(0).getChild(0));
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".deleteProperty(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        		}

        	}
        	else if (node.getChild(0).getNodeID() == ASTNodeID.MemberAccessExpressionID)
    		{
    			MemberAccessExpressionNode obj = (MemberAccessExpressionNode)(node.getChild(0));
    			if (isXMLList(obj))
    			{
    		        if (ASNodeUtils.hasParenOpen(node))
    		            write(ASEmitterTokens.PAREN_OPEN);

    	            String s = stringifyNode(obj.getLeftOperandNode());
    	            write(s);
    	            write(".removeChild('");
    	            s = stringifyNode(obj.getRightOperandNode());
    	            write(s);
    	            write("')");
    		        if (ASNodeUtils.hasParenClose(node))
    		            write(ASEmitterTokens.PAREN_CLOSE);
    		        return;
    			}
    		}

        }
        else if (node.getNodeID() == ASTNodeID.Op_AtID)
        {
        	IASNode op = node.getOperandNode();
        	if (op != null)
        	{
            	write("attribute('");
        		getWalker().walk(node.getOperandNode());
            	write("')");
        	}
        	else if (node.getParent().getNodeID() == ASTNodeID.ArrayIndexExpressionID)
        	{
        		DynamicAccessNode parentNode = (DynamicAccessNode)node.getParent();
            	write("attribute(");
        		getWalker().walk(parentNode.getRightOperandNode());        		
            	write(")");
        	}
        		
        	return;
        }

        super.emitUnaryOperator(node);

    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * We want to know not just whether the node is of type XML,
     * but whether it is a property of a property of type XML.
     * For example, this.foo might be XML or XMLList, but since
     * 'this' isn't also XML, we return false.  That's because
     * assignment to this.foo shouldn't use setChild() but
     * just do an assignment.
     * @param obj
     * @return
     */
    public boolean isXMLList(MemberAccessExpressionNode obj)
    {
    	IExpressionNode leftNode = obj.getLeftOperandNode();
    	IExpressionNode rightNode = obj.getRightOperandNode();
    	ASTNodeID rightID = rightNode.getNodeID();
		if (rightID == ASTNodeID.IdentifierID)
		{
			IDefinition rightDef = rightNode.resolveType(getWalker().getProject());
			if (rightDef != null)
			{
				if (IdentifierNode.isXMLish(rightDef, getWalker().getProject()))
				{
					return isLeftNodeXMLish(leftNode);
				}
				return false;
			}
			return isLeftNodeXMLish(leftNode);
		}
		else if (rightID == ASTNodeID.Op_AtID)
			return true;
		return false;
    }

    public boolean isLeftNodeXMLish(IExpressionNode leftNode)
    {
    	ASTNodeID leftID = leftNode.getNodeID();
		if (leftID == ASTNodeID.IdentifierID)
		{
			IDefinition leftDef = leftNode.resolveType(getWalker().getProject());
			if (leftDef != null)
				return IdentifierNode.isXMLish(leftDef, getWalker().getProject());
		}
		else if (leftID == ASTNodeID.MemberAccessExpressionID)
		{
			MemberAccessExpressionNode maen = (MemberAccessExpressionNode)leftNode;
	    	IExpressionNode rightNode = maen.getRightOperandNode();
	    	ASTNodeID rightID = rightNode.getNodeID();
			if (rightID == ASTNodeID.IdentifierID)
			{
				IDefinition rightDef = rightNode.resolveType(getWalker().getProject());
				if (rightDef != null)
				{
					return IdentifierNode.isXMLish(rightDef, getWalker().getProject());
				}
			}
			leftNode = maen.getLeftOperandNode();
			return isLeftNodeXMLish(leftNode);
		}
		else if (leftID == ASTNodeID.FunctionCallID)
		{
			FunctionCallNode fcn = (FunctionCallNode)leftNode;
			String fname = fcn.getFunctionName();
			if (fname.equals("XML") || fname.equals("XMLList"))
				return true;
		}
		else if (leftID == ASTNodeID.Op_AsID)
		{
			BinaryOperatorAsNode boan = (BinaryOperatorAsNode)leftNode;
			String fname = ((IdentifierNode)boan.getChild(1)).getName();
			if (fname.equals("XML") || fname.equals("XMLList"))
				return true;
		}
		else if (leftID == ASTNodeID.ArrayIndexExpressionID)
		{
			leftNode = (IExpressionNode)(leftNode.getChild(0));
			IDefinition leftDef = leftNode.resolveType(getWalker().getProject());
			if (leftDef != null)
				return IdentifierNode.isXMLish(leftDef, getWalker().getProject());

		}
		else if (leftID == ASTNodeID.E4XFilterID)
			return true;
    	return false;
    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isProxy(IExpressionNode obj)
    {
		FlexProject project = (FlexProject)getWalker().getProject();
		// See if it is Proxy
		ITypeDefinition leftDef = obj.resolveType(project);
		if (leftDef == null)
		{
			if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
			{
				IExpressionNode leftNode = ((MemberAccessExpressionNode)obj).getLeftOperandNode();
				leftDef = leftNode.resolveType(project);
				if (leftDef != null && leftDef.isInstanceOf(project.getProxyBaseClass(), project))
					return true;
				while (leftNode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
				{
					// walk up chain looking for a proxy
					leftNode = ((MemberAccessExpressionNode)leftNode).getLeftOperandNode();
					leftDef = leftNode.resolveType(project);
					if (leftDef != null && leftDef.isInstanceOf(project.getProxyBaseClass(), project))
						return true;
				}
			}
			return false;
		}
		return leftDef.isInstanceOf(project.getProxyBaseClass(), project);
    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isDateProperty(IExpressionNode obj)
    {
		FlexProject project = (FlexProject)getWalker().getProject();
		if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
		{
			IDefinition leftDef;
			IExpressionNode leftNode = ((MemberAccessExpressionNode)obj).getLeftOperandNode();
			IExpressionNode rightNode = ((MemberAccessExpressionNode)obj).getRightOperandNode();
			leftDef = leftNode.resolveType(project);
			IDefinition rightDef = rightNode.resolve(project);
			if (leftDef != null && leftDef.getQualifiedName().equals("Date") && rightDef instanceof AccessorDefinition)
			{
				return true;
			}
		}
		return false;
	}

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isXML(IExpressionNode obj)
    {
		// See if the left side is XML or XMLList
		IDefinition leftDef = obj.resolveType(getWalker().getProject());
		return IdentifierNode.isXMLish(leftDef, getWalker().getProject());
    }

    public MemberAccessExpressionNode getLastMAEInChain(MemberAccessExpressionNode node)
    {
    	while (node.getRightOperandNode() instanceof MemberAccessExpressionNode)
    		node = (MemberAccessExpressionNode)node.getRightOperandNode();
    	return node;
    }

    @Override
    public void emitLabelStatement(LabeledStatementNode node)
    {
    	BlockNode innerBlock = node.getLabeledStatement();
    	if (innerBlock.getChildCount() == 1 && innerBlock.getChild(0).getNodeID() == ASTNodeID.ForEachLoopID)
    	{
    		getWalker().walk(node.getLabeledStatement());
    		return; // for each emitter will emit label in the right spot
    	}
    	super.emitLabelStatement(node);
    }
    
    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSRoyaleEmitterTokens.VECTOR);
    }
    
	boolean isExternal(String className)
	{
        ICompilerProject project = getWalker().getProject();
		ICompilationUnit cu = project.resolveQNameToCompilationUnit(className);
		if (cu == null) return false; // unit testing
		
		return ((RoyaleProject)project).isExternalLinkage(cu);
	}

}