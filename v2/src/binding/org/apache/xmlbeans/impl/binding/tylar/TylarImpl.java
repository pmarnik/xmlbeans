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
package org.apache.xmlbeans.impl.binding.tylar;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Simple implementation of Tylar.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class TylarImpl extends BaseTylarImpl implements Tylar, TylarWriter {

  // ========================================================================
  // Variables

  private URI mSourceURI;
  private BindingFile mBindingFile = null;
  private Collection mSchemas = null;

  // ========================================================================
  // Constructors

  public TylarImpl() {}

  public TylarImpl(URI sourceUri,
                   BindingFile bf,
                   Collection schemas)
  {
    mSourceURI = sourceUri;
    mBindingFile = bf;
    mSchemas = schemas;
  }

  // ========================================================================
  // Tylar implementation

  public BindingFile[] getBindingFiles() {
    return new BindingFile[] {mBindingFile};
  }

  public SchemaDocument[] getSchemas() {
    if (mSchemas == null) return new SchemaDocument[0];
    SchemaDocument[] out = new SchemaDocument[mSchemas.size()];
    mSchemas.toArray(out);
    return out;
  }

  public URI getLocation() {
    return mSourceURI;
  }

  public ClassLoader createClassLoader(ClassLoader parent) {
    try {
      return new URLClassLoader(new URL[] {mSourceURI.toURL()},parent);
    } catch(MalformedURLException mue){
      throw new RuntimeException(mue); //FIXME this is bad
    }
  }

  // ========================================================================
  // TylarWriter implementation
  //
  //  This is useful in the case where we want to build up an in-memory tylar
  //  that does not need to be persisted.

  public void writeBindingFile(BindingFile bf) {
    mBindingFile = bf;
  }

  public void writeSchema(SchemaDocument xsd, String path) {
    if (mSchemas == null) mSchemas = new ArrayList();
    mSchemas.add(xsd);
  }

  public JavaOutputStream getJavaOutputStream() {
    throw new UnsupportedOperationException
            ("In-memory tylar does not support java code generation.");
  }

  public void close() {}
}