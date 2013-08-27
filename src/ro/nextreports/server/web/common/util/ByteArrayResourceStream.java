/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

//
public class ByteArrayResourceStream extends AbstractResourceStream {

	private static final long serialVersionUID = 1L;
	
	private byte[] content = null;
    private String contentType = null;

    public ByteArrayResourceStream(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public void close() throws IOException {
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() throws ResourceStreamNotFoundException {
        return (new ByteArrayInputStream(content));
    }

    @Override
    public Bytes length() {
        return Bytes.bytes(content.length);
    }

}
