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
package ro.nextreports.server.report.jasper.util;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.ByteArrayInputStream;

/**
 * User: mihai.panaitescu
 * Date: 14-Apr-2010
 * Time: 11:29:58
 */
public class JasperReportSaxParser extends DefaultHandler {

    private String REPORT_TAG = "jasperReport";
    private String LANGUAGE_TAG = "language";

    private String language;
    private StringBuffer buffer = new StringBuffer();

     public void processFile(String file) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser =  factory.newSAXParser();
        saxParser.parse( new File(file), this );
    }

    public void process(byte[] content) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser =  factory.newSAXParser();
        saxParser.parse( new ByteArrayInputStream(content), this );
    }

    @Override
    public void startElement(String uri, String localName, String qname, Attributes attributes) {
        buffer.setLength(0);
        if (qname.equals(REPORT_TAG)) {
            language = attributes.getValue(LANGUAGE_TAG);
        }
    }
    
    public String getLanguage() {
        return language;
    }
}
