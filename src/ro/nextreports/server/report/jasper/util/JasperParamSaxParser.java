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


import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import ro.nextreports.server.report.jasper.JasperParameterSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Mar 4, 2008
 * Time: 1:45:22 PM
 */
public class JasperParamSaxParser extends DefaultHandler {

    private List<JasperParameterSource> parameterSources = new ArrayList<JasperParameterSource>();

    private String PARAM_TAG = "param";
    private String NAME_TAG = "name";
    private String TYPE_TAG = "type";
    private String CLASS_TAG = "valueClassName";
    private String SELECT_TAG = "select";
    private String MANDATORY_TAG = "mandatory";

    private String name;
    private String type;
    private String valueClassName;
    private String select;
    private Boolean mandatory = Boolean.TRUE;
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
        if (qname.equals(PARAM_TAG)) {
            name = null;
            type = null;
            valueClassName = null;
            select = null;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qname) {
        if (qname.equals(NAME_TAG)) {
            name = buffer.toString();
        } else if (qname.equals(TYPE_TAG)) {
            type = buffer.toString();
        } else if (qname.equals(CLASS_TAG)) {
            valueClassName = buffer.toString();
        } else if (qname.equals(SELECT_TAG)) {
            select = buffer.toString();
        } else if (qname.equals(MANDATORY_TAG)) {
            mandatory = Boolean.parseBoolean(buffer.toString());
        } else if (qname.equals(PARAM_TAG)) {
            JasperParameterSource sp = new JasperParameterSource(name);
            sp.setType(type);
            sp.setValueClassName(valueClassName);
            sp.setSelect(select);
            sp.setMandatory(mandatory);
            parameterSources.add(sp);
        }
        buffer.setLength(0); // empty character buffer
    }

    @Override
    public void characters(char[] chars, int start, int length) {
        // collect the characters
        buffer.append(chars, start, length);
    }    

    public List<JasperParameterSource> getParameterSources() {
        return parameterSources;
    }

}
