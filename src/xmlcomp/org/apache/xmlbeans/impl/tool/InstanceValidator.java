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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;

public class InstanceValidator
{
    public static void main(String[] args)
    {
        CommandLine cl = new CommandLine(args, Collections.EMPTY_SET);
        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }
        if (cl.args().length == 0)
        {
            System.out.println("Validates a schema defintion and instances within the schema.");
            System.out.println("Usage: validate [switches] schema.xsd instance.xml");
            System.out.println("Switches:");
            System.out.println("    -dl    enable network downloads for imports and includes");
            System.out.println("    -nopvr disable particle valid (restriction) rule");
            System.out.println("    -noupa diable unique particle attributeion rule");
            System.out.println("    -license prints license information");
            return;
        }
        
        boolean dl = (cl.getOpt("dl") != null);
        boolean nopvr = (cl.getOpt("nopvr") != null);
        boolean noupa = (cl.getOpt("noupa") != null);
        
        File[] schemaFiles = cl.filesEndingWith(".xsd");
        File[] instanceFiles = cl.filesEndingWith(".xml");
        
        List sdocs = new ArrayList();
        
        
        for (int i = 0; i < schemaFiles.length; i++)
        {
            try
            {
                sdocs.add(
                    XmlObject.Factory.parse(
                        schemaFiles[i], (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest()));
            }
            catch (Exception e)
            {
                System.err.println( schemaFiles[i] + " not loadable: " + e );
            }
        }

        XmlObject[] schemas = (XmlObject[])sdocs.toArray(new XmlObject[0]);

        SchemaTypeLoader sLoader;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        if (dl)
            schemaOptions.setCompileDownloadUrls();
        if (nopvr)
            schemaOptions.setCompileNoPvrRule();
        if (noupa)
            schemaOptions.setCompileNoUpaRule();
        
        try
        {
            sLoader = XmlBeans.loadXsd(schemas, schemaOptions);
        }
        catch (Exception e)
        {
            if (compErrors.isEmpty() || !(e instanceof XmlException))
            {
                e.printStackTrace(System.err);
            }
            System.out.println("Schema invalid");
            for (Iterator i = compErrors.iterator(); i.hasNext(); )
                System.out.println(i.next());
            return;
        }
        
        for (int i = 0; i < instanceFiles.length; i++)
        {
            XmlObject xobj;
            
            try
            {
                xobj =
                    sLoader.parse( instanceFiles[i], null, (new XmlOptions()).setLoadLineNumbers() );
            }
            catch (Exception e)
            {
                System.err.println(instanceFiles[i] + " not loadable: " + e);
                e.printStackTrace(System.err);
                continue;
            }

            Collection errors = new ArrayList();

            if (xobj.schemaType() == XmlObject.type)
            {
                System.out.println(instanceFiles[i] + " NOT valid.  ");
                System.out.println("  Document type not found." );
            }
            else if (xobj.validate(new XmlOptions().setErrorListener(errors)))
                System.out.println(instanceFiles[i] + " valid.");
            else
            {
                System.out.println(instanceFiles[i] + " NOT valid.");
                for (Iterator it = errors.iterator(); it.hasNext(); )
                {
                    System.out.println(it.next());
                }
            }
        }
    }
}