/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package drtcases;

import com.mytest.MyClass;
import com.mytest.MySubClass;
import com.mytest.MySubSubClass;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.BindingContextFactory;
import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.XmlReaderToWriter;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.tool.PrettyPrinter;
import org.apache.xmlbeans.impl.marshal.BindingContextFactoryImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


public class MarshalTests extends TestCase
{
    private static final boolean VERBOSE = false;

    public MarshalTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(MarshalTests.class);
    }

    //does not test any xmlbeans code, but rather a quick sanity check
    //of the current jsr 173 impl
    public void testAStream()
        throws Exception
    {
        String doc = "<a x='y'>food</a>";
        StringReader sr = new StringReader(doc);
        final XMLStreamReader reader =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);

        dumpReader(reader);
    }

    public void testManySimpleTypesUnmarshall()
        throws Exception
    {
        testSimpleTypeUnmarshal(Boolean.TRUE, "boolean");
        testSimpleTypeUnmarshal(new Byte((byte)125), "byte");
        testSimpleTypeUnmarshal(new Short((short)5543), "short");
        testSimpleTypeUnmarshal(new Integer(55434535), "int");
        testSimpleTypeUnmarshal(new Long(554345354445555555L), "long");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "integer");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "positiveInteger");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "nonNegativeInteger");
        testSimpleTypeUnmarshal(new BigInteger("-55434535443332323245555555"), "negativeInteger");
        testSimpleTypeUnmarshal(new BigInteger("-55434535443332323245555555"), "nonPositiveInteger");
        testSimpleTypeUnmarshal(new BigInteger("5543453555"), "unsignedLong");
        testSimpleTypeUnmarshal(new Long("5543453555"), "unsignedInt");
        testSimpleTypeUnmarshal(new Integer("62121"), "unsignedShort");
        testSimpleTypeUnmarshal(new Short("254"), "unsignedByte");
        testSimpleTypeUnmarshal(new BigDecimal("43434343342.233434342"), "decimal");
        testSimpleTypeUnmarshal(new Float(54.5423f), "float");
        testSimpleTypeUnmarshal(new Double(23432.43234), "double");

        testStringTypeUnmarshal("string");
        testStringTypeUnmarshal("normalizedString");
        testStringTypeUnmarshal("token");
        testStringTypeUnmarshal("language");
        testStringTypeUnmarshal("Name");
        testStringTypeUnmarshal("NCName");
        testStringTypeUnmarshal("NMTOKEN");
        testStringTypeUnmarshal("ID");
        testStringTypeUnmarshal("IDREF");
        testStringTypeUnmarshal("ENTITY");
        testStringTypeUnmarshal("anyURI");

        Calendar c = Calendar.getInstance();

        testSimpleTypeUnmarshal(c, "2002-03-06T08:04:39.265Z", "dateTime");

    }

    private void testStringTypeUnmarshal(String xsd_type)
        throws Exception
    {
        testSimpleTypeUnmarshal("test_" + xsd_type, xsd_type);
    }


    public void testManySimpleTypesMarshall()
        throws Exception
    {
        testSimpleTypeMarshal(Boolean.TRUE, "boolean");
        testSimpleTypeMarshal(new Byte((byte)125), "byte");
        testSimpleTypeMarshal(new Short((short)5543), "short");
        testSimpleTypeMarshal(new Integer(55434535), "int");
        testSimpleTypeMarshal(new Long(554345354445555555L), "long");
        testSimpleTypeMarshal(new BigInteger("55434535443332323245555555"), "integer");
        testSimpleTypeMarshal(new BigDecimal("43434343342.233434342"), "decimal");
        testSimpleTypeMarshal(new Float(5555.5555f), "float");
        testSimpleTypeMarshal(new Double(1231.444), "double");
        testSimpleTypeMarshal("some text here", "string");
        testSimpleTypeMarshal("aToken", "token");
        testSimpleTypeMarshal("       ", "string");

        testSimpleTypeMarshal(new QName("someuri", "somelname"), "QName");
    }


    //only works for values where .toString() is equivalent to marshalling
    public void testSimpleTypeUnmarshal(Object expected, String xsd_type)
        throws Exception
    {
        testSimpleTypeUnmarshal(expected, expected.toString(), xsd_type);
    }

    public void testSimpleTypeUnmarshal(Object expected,
                                        String lexval,
                                        String xsd_type)
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        String xmldoc = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
            " xmlns:xs='http://www.w3.org/2001/XMLSchema' xsi:type='xs:" +
            xsd_type + "' >" + lexval + "</a>";

        StringReader stringReader = new StringReader(xmldoc);
        XMLStreamReader xrdr =
            XMLInputFactory.newInstance().createXMLStreamReader(stringReader);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller umctx =
            bindingContext.createUnmarshaller();

        Object obj = umctx.unmarshal(xrdr, options);


        //special case date/time tests.
        //we really need more robust testing here.
        if (expected instanceof Calendar) {
            XmlCalendar got = (XmlCalendar)obj;
            String got_lex = got.toString();
            Assert.assertEquals(lexval, got_lex);
        } else {
            Assert.assertEquals(expected, obj);
        }

        Assert.assertTrue(errors.isEmpty());


//        inform("OK for " + expected);
    }


    public void testSimpleTypeMarshal(Object orig, String xsd_type)
        throws Exception
    {
        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext();

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(orig,
//                           new QName("uri", "lname"),
                            new QName("lname"),
                            new QName("http://www.w3.org/2001/XMLSchema", xsd_type),
                            orig.getClass().getName(), options);


        inform("==================OBJ: " + orig);
        dumpReader(reader);

        if (!errors.isEmpty()) {
            for (Iterator itr = errors.iterator(); itr.hasNext();) {
                Object err = itr.next();
                inform("Error: " + err);
            }
        }

        Assert.assertTrue(errors.isEmpty());
    }

    public void testSimplePolymorphicTypeMarshal()
        throws Exception
    {
        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext();

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);

        String our_obj = "hello";

        final QName schemaType = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
        final String javaType = Object.class.getName();
        final XMLStreamReader reader =
            ctx.marshalType(our_obj,
                            new QName("lname"),
                            schemaType,
                            javaType, options);


        inform("==================POLYOBJ: " + our_obj);

        final boolean dump = false;
        if (dump) {
            dumpReader(reader);
        } else {
            Unmarshaller um =
                bindingContext.createUnmarshaller();
            Assert.assertNotNull(um);

            Object out = um.unmarshalType(reader, schemaType, javaType, options);
            Assert.assertEquals(our_obj, out);


            if (!errors.isEmpty()) {
                for (Iterator itr = errors.iterator(); itr.hasNext();) {
                    Object err = itr.next();
                    inform("Error: " + err);
                }
            }

            Assert.assertTrue(errors.isEmpty());
        }
    }


    public void testByNameMarshal()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());


        MySubClass sub = new MySubClass();
        sub.setBigInt(new BigInteger("23522352235223522352"));
        myelt.setMySubClass(sub);
//        sub.setIsSetBigInt(false); //TESTING;
//        sub.setBigInt(null);
//        sub.setIsSetBigInt(true); //TESTING;


        myelt.setMyClass(sub);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});

        myelt.setMyClassArray(new MyClass[]{sub, new MyClass(),
                                            //this type is not in our binding file,
                                            //but we should then treat is as its the parent type
                                            new MySubSubClass(),
                                            sub});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();

        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(mc, new QName("java:com.mytest", "load"),
                            new QName("java:com.mytest", "MyClass"),
                            mc.getClass().getName(), options);

//
//        final XMLStreamReader reader =
//            ctx.marshalType(sub, new QName("java:com.mytest", "sub-test"),
//                            new QName("java:com.mytest", "MySubClass"),
//                            "MyClass", null);

        inform("=======IN-OBJ: " + mc);

        dumpReader(reader);
        Assert.assertTrue(errors.isEmpty());
    }


    public void testByNameMarshalViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        myelt.setMyClass(null);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);


        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);


        ctx.marshalType(w, mc, new QName("java:com.mytest", "load"),
                        new QName("java:com.mytest", "MyClass"),
                        mc.getClass().getName(), options);

        inform("=======IN-OBJ: " + mc);
        inform("=======OUT-XML: " + PrettyPrinter.indent(sw.getBuffer().toString()));
        Assert.assertTrue(errors.isEmpty());
    }

    public void testByNameDocMarshalViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        myelt.setMyClass(null);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});


        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);
        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);
        ctx.marshal(w, mc, options);

        //now unmarshall from String and compare objects...
        StringReader sr = new StringReader(sw.getBuffer().toString());
        XMLStreamReader rdr =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        Object out_obj = umctx.unmarshal(rdr, options);
        Assert.assertEquals(mc, out_obj);
        Assert.assertTrue(errors.isEmpty());
    }


    public void testByNameDocMarshalViaOutputStream()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        myelt.setMyClass(null);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final String encoding = "UTF-16";

        final XmlOptions options = new XmlOptions();
        Collection errors = new LinkedList();
        options.setErrorListener(errors);
        options.setCharacterEncoding(encoding);
        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);

        ctx.marshal(baos, mc, options);
        baos.close();
        final byte[] buf = baos.toByteArray();
        inform("16Doc=" + new String(buf, encoding));

        //now unmarshall from String and compare objects...
        Unmarshaller umctx = bindingContext.createUnmarshaller();
        final ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        Object out_obj = umctx.unmarshal(bais, options);
        Assert.assertEquals(mc, out_obj);
        Assert.assertTrue(errors.isEmpty());
    }


    public void testRoundtripPerf()
        throws Exception
    {
        //crank up these numbers to see real perf testing
        //the test still has some value aside from perf
        //in that it can test large stack depths.
        final int trials = 1;
        final int depth = 5;
        final int boolean_array_size = 5;

        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();

        com.mytest.MyClass curr = top_obj;

        boolean[] bools = createRandomBooleanArray(rnd, boolean_array_size);

        for (int i = 0; i < depth; i++) {
            com.mytest.YourClass myelt = new com.mytest.YourClass();
            myelt.setAttrib(rnd.nextFloat());
            myelt.setMyFloat(rnd.nextFloat());
            myelt.setBooleanArray(bools);
            final com.mytest.MyClass my_c = new com.mytest.MyClass();
            myelt.setMyClass(my_c);
            curr.setMyelt(myelt);
            curr.setMyatt("STR" + rnd.nextInt());
            curr = my_c;
        }

        //inform("top_obj = " + top_obj);

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");
        final QName elem_name = new QName("java:com.mytest", "load");
        final String class_name = top_obj.getClass().getName();

        Object out_obj = null;
        final long before_millis = System.currentTimeMillis();
        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        for (int i = 0; i < trials; i++) {
            errors.clear();

            Marshaller ctx =
                bindingContext.createMarshaller();
            Assert.assertNotNull(ctx);


            final XMLStreamReader reader =
                ctx.marshalType(top_obj, elem_name,
                                schemaType,
                                class_name, options);


//            //DEBUG!!!
//            if (System.currentTimeMillis() > 1) {
//                dumpReader(reader);
//                return;
//            }

            Unmarshaller umctx = bindingContext.createUnmarshaller();
            out_obj = umctx.unmarshalType(reader, schemaType, javaType, options);
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
//        inform(" perf_out_obj = " + top_obj);
        Assert.assertTrue(errors.isEmpty());
        Assert.assertEquals(top_obj, out_obj);
        inform("milliseconds: " + diff + " trials: " + trials);
        inform("milliseconds PER trial: " + (diff / (double)trials));
    }


    public void testThreadedRoundtripPerf()
        throws Exception
    {
        //crank up these numbers to see real perf testing
        //the test still has some value aside from perf
        //in that it can test large stack depths.
        final int trials = 50;
        final int depth = 3;
        final int thread_cnt = 5;
        final int boolean_array_size = 3;

        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();

        com.mytest.MyClass curr = top_obj;

        boolean[] bools = createRandomBooleanArray(rnd, boolean_array_size);

        for (int i = 0; i < depth; i++) {
            com.mytest.YourClass myelt = new com.mytest.YourClass();
            myelt.setAttrib(rnd.nextFloat());
            myelt.setMyFloat(rnd.nextFloat());
            myelt.setBooleanArray(bools);
            final com.mytest.MyClass my_c = new com.mytest.MyClass();
            myelt.setMyClass(my_c);
            curr.setMyelt(myelt);
            curr.setMyatt("STR" + rnd.nextInt());
            curr = my_c;
        }

        //inform("top_obj = " + top_obj);

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");
        final QName elem_name = new QName("java:com.mytest", "load");
        final String class_name = top_obj.getClass().getName();

        final Marshaller msh = bindingContext.createMarshaller();
        Assert.assertNotNull(msh);
        final Unmarshaller umsh = bindingContext.createUnmarshaller();
        Assert.assertNotNull(umsh);

        Object out_obj = null;
        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        final long before_millis = System.currentTimeMillis();

        RoundTripRunner[] runners = new RoundTripRunner[thread_cnt];
        for(int i = 0 ; i < thread_cnt ; i++) {
            runners[i] = new RoundTripRunner(top_obj, msh, umsh, elem_name,
                                             schemaType, class_name, javaType, options, trials);
        }

        inform("starting " + thread_cnt + " threads...");

        for(int i = 0 ; i < thread_cnt ; i++) {
            runners[i].start();
        }

        inform("trials=" + trials +  "\tjoining " + thread_cnt + " threads...");

        for(int i = 0 ; i < thread_cnt ; i++) {
            runners[i].join();
        }

        inform("joined " + thread_cnt + " threads.");



        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
//        inform(" perf_out_obj = " + top_obj);
        Assert.assertTrue(errors.isEmpty());
        //Assert.assertEquals(top_obj, out_obj);
        inform("milliseconds: " + diff + " trials: " + trials +
               " threads=" + thread_cnt);
        inform("milliseconds PER trial: " + (diff / (double)trials));
        inform("milliseconds PER roundtrip: " + (diff / ((double)trials*thread_cnt)));
    }

    private static Object doRoundTrip(MyClass top_obj,
                                      final Marshaller msh,
                                      final Unmarshaller umsh,
                                      final QName elem_name,
                                      final QName schemaType,
                                      final String class_name,
                                      final String javaType,
                                      final XmlOptions options)
        throws XmlException
    {
        Object out_obj;
        final XMLStreamReader reader =
            msh.marshalType(top_obj, elem_name,
                            schemaType,
                            class_name, options);

        out_obj = umsh.unmarshalType(reader, schemaType, javaType);
        return out_obj;
    }

    private static class RoundTripRunner extends Thread
    {
        private final MyClass top_obj;
        private final Marshaller msh;
        private final Unmarshaller umsh;
        private final QName elem_name;
        private final QName schemaType;
        private final String class_name;
        private final String javaType;
        private final XmlOptions options;
        private final int trials;


        public RoundTripRunner(MyClass top_obj,
                               Marshaller msh,
                               Unmarshaller umsh,
                               QName elem_name,
                               QName schemaType,
                               String class_name,
                               String javaType,
                               XmlOptions options,
                               int trials)
        {
            this.top_obj = top_obj;
            this.msh = msh;
            this.umsh = umsh;
            this.elem_name = elem_name;
            this.schemaType = schemaType;
            this.class_name = class_name;
            this.javaType = javaType;
            this.options = options;
            this.trials = trials;
        }

        public void run()
        {
            final int t = trials;
            try {
                Object out_obj = null;
                for (int i = 0; i < t; i++) {
                    out_obj = doRoundTrip(top_obj, msh,
                                                 umsh, elem_name,
                                                 schemaType, class_name,
                                                 javaType, options);
                }
                Assert.assertEquals(top_obj, out_obj);
            }
            catch (XmlException xe) {
                throw new AssertionError(xe);
            }
        }
    }

    private boolean[] createRandomBooleanArray(Random rnd, int size)
    {
        boolean[] a = new boolean[size];
        for (int i = 0; i < size; i++) {
            a[i] = rnd.nextBoolean();
        }
        return a;
    }


    public void testJavaToSchemaToJava()
        throws Exception
    {
        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(rnd.nextFloat());
        myelt.setMyFloat(rnd.nextFloat());
        final com.mytest.MyClass my_c = new com.mytest.MyClass();
//        myelt.setMyClass(my_c);
        myelt.setMyClass(null);
        top_obj.setMyelt(myelt);
//        curr.setMyatt("STR" + rnd.nextInt());
        top_obj.setMyatt(null);
//        top_obj.setMyatt("someVALUE");


        inform("top_obj = " + top_obj);

        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());


        //TODO: remove hard coded values
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");
        final QName elem_name = new QName("java:com.mytest", "load");
        final String class_name = top_obj.getClass().getName();

        Object out_obj = null;

        final XmlOptions options = new XmlOptions();
        final ArrayList errors = new ArrayList();
        options.setErrorListener(errors);

        Marshaller ctx =
            bindingContext.createMarshaller();
        Assert.assertNotNull(ctx);


        final XMLStreamReader reader =
            ctx.marshalType(top_obj, elem_name,
                            schemaType,
                            class_name, options);

        Unmarshaller umctx =
            bindingContext.createUnmarshaller();
        out_obj = umctx.unmarshalType(reader, schemaType, javaType, options);
        inform(" out_obj = " + top_obj);
        Assert.assertEquals(top_obj, out_obj);
        Assert.assertTrue(errors.isEmpty());
    }

    private static void dumpReader(final XMLStreamReader reader)
        throws XMLStreamException, XmlException, IOException
    {
        final boolean write_doc = true;
        if (write_doc) {
            StringWriter sw = new StringWriter();

            XMLStreamWriter xsw =
                XMLOutputFactory.newInstance().createXMLStreamWriter(sw);


            XmlReaderToWriter.writeAll(reader, xsw);

            xsw.close();

            if (VERBOSE) {
                final String xmldoc = sw.getBuffer().toString();
                inform("DOC:");
                inform(PrettyPrinter.indent(xmldoc));
                inform((xmldoc));
            }
        } else {
            int i = 0;
            if (VERBOSE)
                inform((i++) + "\tSTATE: " +
                                   XmlStreamUtils.printEvent(reader));
            while (reader.hasNext()) {
                final int state = reader.next();
                if (VERBOSE)
                    inform((i++) + "\tSTATE: " +
                                       XmlStreamUtils.printEvent(reader));
            }
        }
    }

    public void testByNameBeanUnmarshal()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        File doc = TestEnv.xbeanCase("marshal/doc2.xml");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller um_ctx =
            bindingContext.createUnmarshaller();
        Object obj = um_ctx.unmarshal(xrdr, options);

        inform("doc2-obj = " + obj);

        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            XmlError xmlError = (XmlError)itr.next();
            inform("doc2-ERROR: " + xmlError);
        }

        Assert.assertTrue(errors.isEmpty());

    }

    public void testByNameBeanUnmarshalErrors()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        File doc = TestEnv.xbeanCase("marshal/doc3.xml");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller um_ctx =
            bindingContext.createUnmarshaller();
        Object obj = um_ctx.unmarshal(xrdr, options);

        //even with some errors, we should get an object
        Assert.assertTrue(obj != null);

        inform("doc3-obj = " + obj);

        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            XmlError xmlError = (XmlError)itr.next();
            inform("doc3-ERROR: " + xmlError);
        }

        Assert.assertTrue(errors.size() > 0);

    }

    public void testByNameBeanUnmarshalFromInputStream()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        File doc = TestEnv.xbeanCase("marshal/doc2.xml");

        final XmlOptions options = new XmlOptions();
        final LinkedList errors = new LinkedList();
        options.setErrorListener(errors);

        Unmarshaller um_ctx =
            bindingContext.createUnmarshaller();
        Object obj = um_ctx.unmarshal(new FileInputStream(doc), options);

        inform("doc2-obj = " + obj);

        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            XmlError xmlError = (XmlError)itr.next();
            inform("doc2-ERROR: " + xmlError);
        }

        Assert.assertTrue(errors.isEmpty());

    }


    public void testByNameBeanUnmarshalType()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        final File doc = TestEnv.xbeanCase("marshal/doc.xml");
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        final XmlOptions xmlOptions = new XmlOptions();
        Collection errors = new LinkedList();
        xmlOptions.setErrorListener(errors);
        Unmarshaller ctx = bindingContext.createUnmarshaller();

        //this is not very safe but it should work...
        while (!xrdr.isStartElement()) {
            xrdr.next();
        }

        Object obj = ctx.unmarshalType(xrdr, schemaType, javaType, xmlOptions);
        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            inform("ERROR: " + itr.next());
        }
        inform("+++++TYPE obj = " + obj);

        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            XmlError xmlError = (XmlError)itr.next();
            inform("doc-ERROR: " + xmlError);
        }

        Assert.assertTrue(errors.isEmpty());
    }

    public void testPerfByNameBeanUnmarshall()
        throws Exception
    {
        BindingContext bindingContext = getBindingContext(getBindingConfigDocument());

        //File doc = TestEnv.xbeanCase("marshal/doc2.xml");
        File doc = TestEnv.xbeanCase("marshal/bigdoc.xml");
        final FileReader fileReader = new FileReader(doc);
        CharArrayWriter cw = new CharArrayWriter();

        bufferedStreamCopy(fileReader, cw);
        final char[] chars = cw.toCharArray();
        final CharArrayReader cr = new CharArrayReader(chars);

        final int trials = 5;

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        final XmlOptions xmlOptions = new XmlOptions();

        final long before_millis = System.currentTimeMillis();
        for (int i = 0; i < trials; i++) {
            cr.reset();
            XMLStreamReader xrdr =
                xmlInputFactory.createXMLStreamReader(cr);
            Unmarshaller umctx =
                bindingContext.createUnmarshaller();

            Object obj = umctx.unmarshal(xrdr, xmlOptions);

            if ((i % 1000) == 0) {
                String s = obj.toString().substring(0, 70);
                inform("i=" + i + "\tobj = " + s + "...");
            }
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
        inform("milliseconds: " + diff + " trials: " + trials);
        inform("milliseconds PER trial: " + (diff / (double)trials));
    }

    protected static void bufferedStreamCopy(Reader in, Writer out)
        throws IOException
    {
        int charsRead;
        char[] buf = new char[1024];

        while ((charsRead = in.read(buf)) != -1) {
            out.write(buf, 0, charsRead);
        }
    }

    private File getBindingConfigDocument()
    {
        File loc = TestEnv.xbeanCase("marshal/example_config.xml");
        return loc;
    }

    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }

    private static BindingContext getBindingContext(File bcdoc)
        throws XmlException, IOException
    {
        return ((BindingContextFactoryImpl)BindingContextFactory.newInstance()).
            createBindingContextFromConfig(bcdoc);
    }

    private static void  inform(String msg) {
        if (VERBOSE) System.out.println(msg);
    }
}
