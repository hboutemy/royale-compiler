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

package org.apache.flex.compiler.internal.fxg.sax;

import static org.apache.flex.compiler.fxg.FXGConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.flex.compiler.fxg.FXGVersion;
import org.apache.flex.compiler.fxg.dom.IFXGNode;
import org.apache.flex.compiler.fxg.flex.FlexGraphicNode;
import org.apache.flex.compiler.fxg.flex.FlexRichTextNode;
import org.apache.flex.compiler.internal.fxg.dom.BitmapGraphicNode;
import org.apache.flex.compiler.internal.fxg.dom.ContentPropertyNode;
import org.apache.flex.compiler.internal.fxg.dom.DefinitionNode;
import org.apache.flex.compiler.internal.fxg.dom.DelegateNode;
import org.apache.flex.compiler.internal.fxg.dom.EllipseNode;
import org.apache.flex.compiler.internal.fxg.dom.GradientEntryNode;
import org.apache.flex.compiler.internal.fxg.dom.GroupDefinitionNode;
import org.apache.flex.compiler.internal.fxg.dom.GroupNode;
import org.apache.flex.compiler.internal.fxg.dom.LibraryNode;
import org.apache.flex.compiler.internal.fxg.dom.LineNode;
import org.apache.flex.compiler.internal.fxg.dom.MaskPropertyNode;
import org.apache.flex.compiler.internal.fxg.dom.PathNode;
import org.apache.flex.compiler.internal.fxg.dom.RectNode;
import org.apache.flex.compiler.internal.fxg.dom.fills.BitmapFillNode;
import org.apache.flex.compiler.internal.fxg.dom.fills.LinearGradientFillNode;
import org.apache.flex.compiler.internal.fxg.dom.fills.RadialGradientFillNode;
import org.apache.flex.compiler.internal.fxg.dom.fills.SolidColorFillNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.BevelFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.BlurFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.ColorMatrixFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.DropShadowFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.GlowFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.GradientBevelFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.filters.GradientGlowFilterNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.BRNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.DivNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.ImgNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.LinkNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.ParagraphNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.SpanNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.TCYNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.TabNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.TextLayoutFormatNode;
import org.apache.flex.compiler.internal.fxg.dom.richtext.TextPropertyNode;
import org.apache.flex.compiler.internal.fxg.dom.strokes.LinearGradientStrokeNode;
import org.apache.flex.compiler.internal.fxg.dom.strokes.RadialGradientStrokeNode;
import org.apache.flex.compiler.internal.fxg.dom.strokes.SolidColorStrokeNode;
import org.apache.flex.compiler.internal.fxg.dom.transforms.ColorTransformNode;
import org.apache.flex.compiler.internal.fxg.dom.transforms.MatrixNode;

/**
 * IFXGVersionHandler for FXG 2.0
 */
public class FXG_v2_0_Handler extends AbstractFXGVersionHandler
{
    
    private boolean initialized = false;

    protected FXG_v2_0_Handler()
    {
        super();
        handlerVersion = FXGVersion.v2_0;
    }

    /**
     * initializes the version handler with FXG 2.0 specific information
     */
    @Override
    protected void init()
    {
        if (initialized)
            return;

        Map<String, Class<? extends IFXGNode>> elementNodes = new HashMap<String, Class<? extends IFXGNode>>(DEFAULT_FXG_2_0_NODES.size() + 4);
        elementNodes.putAll(DEFAULT_FXG_2_0_NODES);
        elementNodesByURI = new HashMap<String, Map<String, Class<? extends IFXGNode>>>(1);
        elementNodesByURI.put(FXG_NAMESPACE, elementNodes);

        // Skip <Private> by default for FXG 2.0
        HashSet<String> skippedElements = new HashSet<String>(1);
        skippedElements.add(FXG_PRIVATE_ELEMENT);
        skippedElementsByURI = new HashMap<String, Set<String>>(1);
        skippedElementsByURI.put(FXG_NAMESPACE, skippedElements);
        initialized = true;
    }

    /**
     * The default IFXGNode Classes to handle elements in the FXG 2.0
     */
    public static Map<String, Class<? extends IFXGNode>> DEFAULT_FXG_2_0_NODES = new HashMap<String, Class<? extends IFXGNode>>();
    static
    {
        // RichText relevant nodes
        DEFAULT_FXG_2_0_NODES.put(FXG_BR_ELEMENT, BRNode.class);        
        DEFAULT_FXG_2_0_NODES.put(FXG_DIV_ELEMENT, DivNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_IMG_ELEMENT, ImgNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_A_ELEMENT, LinkNode.class);        
        DEFAULT_FXG_2_0_NODES.put(FXG_P_ELEMENT, ParagraphNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_RICHTEXT_ELEMENT, FlexRichTextNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_SPAN_ELEMENT, SpanNode.class); 
        DEFAULT_FXG_2_0_NODES.put(FXG_TAB_ELEMENT, TabNode.class); 
        DEFAULT_FXG_2_0_NODES.put(FXG_TCY_ELEMENT, TCYNode.class); 
        DEFAULT_FXG_2_0_NODES.put(FXG_TEXTLAYOUTFORMAT_ELEMENT, TextLayoutFormatNode.class); 
        
        DEFAULT_FXG_2_0_NODES.put(FXG_GRAPHIC_ELEMENT, FlexGraphicNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_DEFINITION_ELEMENT, DefinitionNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LIBRARY_ELEMENT, LibraryNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_BEVELFILTER_ELEMENT, BevelFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_BITMAPFILL_ELEMENT, BitmapFillNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_BITMAPIMAGE_ELEMENT, BitmapGraphicNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_BLURFILTER_ELEMENT, BlurFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_COLORMATRIXFILTER_ELEMENT, ColorMatrixFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_COLORTRANSFORM_ELEMENT, ColorTransformNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_DROPSHADOWFILTER_ELEMENT, DropShadowFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_ELLIPSE_ELEMENT, EllipseNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_GLOWFILTER_ELEMENT, GlowFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_GRADIENTENTRY_ELEMENT, GradientEntryNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_GRADIENTBEVELFILTER_ELEMENT, GradientBevelFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_GRADIENTGLOWFILTER_ELEMENT, GradientGlowFilterNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_GROUP_ELEMENT, GroupNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LINE_ELEMENT, LineNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LINEARGRADIENT_ELEMENT, LinearGradientFillNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LINEARGRADIENTSTROKE_ELEMENT, LinearGradientStrokeNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_MATRIX_ELEMENT, MatrixNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_PATH_ELEMENT, PathNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_RADIALGRADIENT_ELEMENT, RadialGradientFillNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_RADIALGRADIENTSTROKE_ELEMENT, RadialGradientStrokeNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_RECT_ELEMENT, RectNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_SOLIDCOLOR_ELEMENT, SolidColorFillNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_SOLIDCOLORSTROKE_ELEMENT, SolidColorStrokeNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_TRANSFORM_ELEMENT, DelegateNode.class);

        // Special delegate property nodes
        DEFAULT_FXG_2_0_NODES.put(FXG_COLORTRANSFORM_PROPERTY_ELEMENT, DelegateNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_CONTENT_PROPERTY_ELEMENT, ContentPropertyNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_FILL_PROPERTY_ELEMENT, DelegateNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_FILTERS_PROPERTY_ELEMENT, DelegateNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_MASK_PROPERTY_ELEMENT, MaskPropertyNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_MATRIX_PROPERTY_ELEMENT, DelegateNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_STROKE_PROPERTY_ELEMENT, DelegateNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_TRANSFORM_PROPERTY_ELEMENT, DelegateNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LINKACTIVEFORMAT_PROPERTY_ELEMENT, TextPropertyNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LINKHOVERFORMAT_PROPERTY_ELEMENT, TextPropertyNode.class);
        DEFAULT_FXG_2_0_NODES.put(FXG_LINKNORMALFORMAT_PROPERTY_ELEMENT, TextPropertyNode.class);

        // Special nodes
        DEFAULT_FXG_2_0_NODES.put(FXG_GROUP_DEFINITION_ELEMENT, GroupDefinitionNode.class);
    }
}
