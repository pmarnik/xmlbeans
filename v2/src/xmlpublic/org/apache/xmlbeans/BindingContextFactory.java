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

package org.apache.xmlbeans;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * BindingContextFactory is used to create BindingContext objects
 * from a binding configuration file.
 */
public abstract class BindingContextFactory
{
    /**
     * Creates a BindingContext from a set of tylars located at the given URI.
     * The order in which tylars appear in the array determines their precedence
     * for loading types.
     *
     * @param tylarUris An array of URIs which identify the tylars to be used
     * in the BindingContext.
     * @return The BindingContext
     * @throws IOException if a problem occurs while opening or parsing the
     * contents of the tylars.
     */
    public abstract BindingContext createBindingContext(URI[] tylarUris)
        throws IOException, XmlException;

  /**
   * Creates a BindingContext from a tylar located at the given URI.
   * The order in which tylars appear in the array determines their precedence
   * for loading types.
   *
   * @param tylarUri A URIs to the tylar to be used in the BindingContext.
   * @return The BindingContext
   * @throws IOException if a problem occurs while opening or parsing the
   * contents of the tylars.
   */
  public abstract BindingContext createBindingContext(URI tylarUri)
      throws IOException, XmlException;


    /**
     * Create a BindingContext that only knows about builtin types
     *
     * @return a BindingContext object for builtin types
     */
    public abstract BindingContext createBindingContext();

    /**
     * Create a BindingContext from a binding config xml file
     *
     * @param bindingConfig
     * @return
     * @throws IOException
     * @throws XmlException
     *
     * @deprecated we are not exposing the binding file directly anymore.
     * use one of the uri-based methods above.
     */
//    public abstract BindingContext createBindingContext(InputStream bindingConfig)
//        throws IOException, XmlException;

    /**
     * Create a BindingContext from a binding config xml file
     *
     * @param bindingConfig
     * @return
     * @throws IOException
     * @throws XmlException
     *
     * @deprecated we are not exposing the binding file directly anymore.
     * use one of the tylar-based methods above.
     */
    public abstract BindingContext createBindingContextFromConfig(File bindingConfig)
        throws IOException, XmlException;


    protected final static String DEFAULT_IMPL =
        "org.apache.xmlbeans.impl.marshal.BindingContextFactoryImpl";

    public static BindingContextFactory newInstance()
    {
        try {
            Class default_impl = Class.forName(DEFAULT_IMPL);
            final BindingContextFactory factory =
                (BindingContextFactory)default_impl.newInstance();
            return factory;
        }
        catch (ClassNotFoundException e) {
            throw new XmlRuntimeException(e);
        }
        catch (InstantiationException e) {
            throw new XmlRuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new XmlRuntimeException(e);
        }
    }

}
