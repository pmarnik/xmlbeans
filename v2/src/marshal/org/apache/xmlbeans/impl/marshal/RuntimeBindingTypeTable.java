/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Table of TypeMarshaller and TypeUnmarshaller objects keyed by BindingType
 */
final class RuntimeBindingTypeTable
{
    //key is BindingType, value is TTEntry
    private final Map typeMap = new HashMap();

    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private static final Map BUILTIN_TYPE_MAP = createBuiltinTypeMap();


    public static RuntimeBindingTypeTable createRuntimeBindingTypeTable()
    {
        //this pseudo-clone is to ensure that we only have one instance of
        //all builtin (un)marshallers per JVM,
        //without having to make each builtin a singleton.

        final RuntimeBindingTypeTable tbl = new RuntimeBindingTypeTable();
        final Map tbl_map = tbl.typeMap;
        final Set entries = BUILTIN_TYPE_MAP.entrySet();
        for (Iterator itr = entries.iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry)itr.next();
            final TTEntry value = (TTEntry)entry.getValue();
            tbl_map.put(entry.getKey(), value.shallowCopy());
        }
        return tbl;
    }


    private static Map createBuiltinTypeMap()
    {
        RuntimeBindingTypeTable builtins = new RuntimeBindingTypeTable();
        builtins.addBuiltins();
        return builtins.typeMap;
    }

    private RuntimeBindingTypeTable()
    {
    }

    TypeUnmarshaller getTypeUnmarshaller(BindingType key)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) return null;
        return e.typeUnmarshaller;
    }

    TypeMarshaller getTypeMarshaller(BindingType key)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) return null;
        return e.typeMarshaller;
    }

    void putTypeUnmarshaller(BindingType key, TypeUnmarshaller um)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) {
            e = new TTEntry();
            typeMap.put(key, e);
        }
        e.typeUnmarshaller = um;
    }

    void putTypeMarshaller(BindingType key, TypeMarshaller m)
    {
        TTEntry e = (TTEntry)typeMap.get(key);
        if (e == null) {
            e = new TTEntry();
            typeMap.put(key, e);
        }
        e.typeMarshaller = m;
    }

    public void initUnmarshallers(BindingLoader loader)
    {
        for (Iterator iterator = typeMap.values().iterator(); iterator.hasNext();) {
            TTEntry entry = (TTEntry)iterator.next();
            entry.typeUnmarshaller.initialize(this, loader);
        }
    }

    protected void addXsdBuiltin(String xsdType, Class javaClass, TypeConverter converter)
    {
        final BindingLoader bindingLoader = BuiltinBindingLoader.getInstance();

        QName xml_type = new QName(XSD_NS, xsdType);
        JavaTypeName jName = JavaTypeName.forString(javaClass.getName());
        XmlTypeName xName = XmlTypeName.forTypeNamed(xml_type);
        BindingType btype = bindingLoader.getBindingType(BindingTypeName.forPair(jName, xName));
        if (btype == null) {
            throw new AssertionError("failed to find builtin for java:" + jName +
                                     " - xsd:" + xName);
        }
        putTypeMarshaller(btype, converter);
        putTypeUnmarshaller(btype, converter);

        assert getTypeMarshaller(btype) == converter;
        assert getTypeUnmarshaller(btype) == converter;
    }


    private void addBuiltins()
    {
        final FloatTypeConverter float_conv = new FloatTypeConverter();
        addXsdBuiltin("float", float.class, float_conv);
        addXsdBuiltin("float", Float.class, float_conv);

        final DoubleTypeConverter double_conv = new DoubleTypeConverter();
        addXsdBuiltin("double", double.class, double_conv);
        addXsdBuiltin("double", Double.class, double_conv);

        final IntegerTypeConverter integer_conv = new IntegerTypeConverter();
        final Class bigint = java.math.BigInteger.class;
        addXsdBuiltin("integer", bigint, integer_conv);
        addXsdBuiltin("nonPositiveInteger", bigint, integer_conv);
        addXsdBuiltin("negativeInteger", bigint, integer_conv);
        addXsdBuiltin("nonNegativeInteger", bigint, integer_conv);
        addXsdBuiltin("positiveInteger", bigint, integer_conv);
        addXsdBuiltin("unsignedLong", bigint, integer_conv);

        addXsdBuiltin("decimal", java.math.BigDecimal.class,
                      new DecimalTypeConverter());

        final LongTypeConverter long_conv = new LongTypeConverter();
        addXsdBuiltin("long", long.class, long_conv);
        addXsdBuiltin("long", Long.class, long_conv);
        addXsdBuiltin("unsignedInt", long.class, long_conv);
        addXsdBuiltin("unsignedInt", Long.class, long_conv);

        final IntTypeConverter int_conv = new IntTypeConverter();
        addXsdBuiltin("int", int.class, int_conv);
        addXsdBuiltin("int", Integer.class, int_conv);
        addXsdBuiltin("unsignedShort", int.class, int_conv);
        addXsdBuiltin("unsignedShort", Integer.class, int_conv);

        final ShortTypeConverter short_conv = new ShortTypeConverter();
        addXsdBuiltin("short", short.class, short_conv);
        addXsdBuiltin("short", Short.class, short_conv);
        addXsdBuiltin("unsignedByte", short.class, short_conv);
        addXsdBuiltin("unsignedByte", Short.class, short_conv);

        final ByteTypeConverter byte_conv = new ByteTypeConverter();
        addXsdBuiltin("byte", byte.class, byte_conv);
        addXsdBuiltin("byte", Byte.class, byte_conv);

        final BooleanTypeConverter boolean_conv = new BooleanTypeConverter();
        addXsdBuiltin("boolean", boolean.class, boolean_conv);
        addXsdBuiltin("boolean", Boolean.class, boolean_conv);

        final StringTypeConverter string_conv = new StringTypeConverter();
        final Class str = String.class;
        addXsdBuiltin("string", str, string_conv);
        addXsdBuiltin("normalizedString", str, string_conv);
        addXsdBuiltin("token", str, string_conv);
        addXsdBuiltin("language", str, string_conv);
        addXsdBuiltin("Name", str, string_conv);
        addXsdBuiltin("NCName", str, string_conv);
        addXsdBuiltin("NMTOKEN", str, string_conv);
        addXsdBuiltin("ID", str, string_conv);
        addXsdBuiltin("IDREF", str, string_conv);
        addXsdBuiltin("ENTITY", str, string_conv);

        addXsdBuiltin("anyURI",
                      str,
                      new AnyUriToStringTypeConverter());

        addXsdBuiltin("dateTime",
                      java.util.Calendar.class,
                      new DateTimeTypeConverter());

        addXsdBuiltin("QName",
                      javax.xml.namespace.QName.class,
                      new QNameTypeConverter());
    }


    private static class TTEntry
    {
        TypeMarshaller typeMarshaller;
        TypeUnmarshaller typeUnmarshaller;

        TTEntry()
        {
        }

        TTEntry(TypeMarshaller typeMarshaller,
                TypeUnmarshaller typeUnmarshaller)
        {
            this.typeMarshaller = typeMarshaller;
            this.typeUnmarshaller = typeUnmarshaller;
        }

        TTEntry shallowCopy()
        {
            return new TTEntry(typeMarshaller, typeUnmarshaller);
        }
    }


}