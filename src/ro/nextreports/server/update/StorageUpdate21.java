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
package ro.nextreports.server.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.util.ISO9075;

import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.server.StorageConstants;

/**
 * @author Decebal Suiu
 * 
 * Rename class names for each entity node.
 */
public class StorageUpdate21 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		convertOldIdNameClass();
	}

	private void convertOldIdNameClass() throws RepositoryException {
		String searchRoot = "/jcr:root" + ISO9075.encodePath(StorageConstants.DASHBOARDS_ROOT);
		String searchPropertyName = "className";
		String searchPropertyValue = "com.asf.nextserver.domain.ParameterValue";
		String statement = searchRoot + "//*[@" + searchPropertyName + "='" + searchPropertyValue + "']";
        QueryResult queryResult = getTemplate().query(statement);

        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() +  " parameter value nodes");
        while (nodes.hasNext()) {
        	Node node = nodes.nextNode();
        	Property property = node.getProperty("value");
        	try {
	        	Object value = deserialize(property.getBinary().getStream());
	        	if (value instanceof Object[]) {
	        		Object[] values = (Object[]) value;
	        		Object[] convertedValues = new Object[values.length];
		        	boolean converted = false;
	        		for (int i = 0; i < values.length; i++) {
	        			Object tmp = values[i];
	        			if (tmp instanceof com.asf.nextreports.engine.queryexec.IdName) {
	        				com.asf.nextreports.engine.queryexec.IdName oldIdName = (com.asf.nextreports.engine.queryexec.IdName) tmp;
	        				IdName idName = new IdName();
	        				idName.setId(oldIdName.getId());
	        				idName.setName(oldIdName.getName());
	        				convertedValues[i] = idName;
	        				converted = true;
	        			} else {
	        				convertedValues[i] = tmp;
	        			}
	        		}
	        		
	        		if (converted) {
	        			ValueFactory valueFactory = node.getSession().getValueFactory();
	                    Binary binary = valueFactory.createBinary(new ByteArrayInputStream(serialize(convertedValues)));
	        			property.setValue(binary);
	        		}
	        	} else if (value instanceof com.asf.nextreports.engine.queryexec.IdName) {
    				com.asf.nextreports.engine.queryexec.IdName oldIdName = (com.asf.nextreports.engine.queryexec.IdName) value;
    				IdName idName = new IdName();
    				idName.setId(oldIdName.getId());
    				idName.setName(oldIdName.getName());

        			ValueFactory valueFactory = node.getSession().getValueFactory();
                    Binary binary = valueFactory.createBinary(new ByteArrayInputStream(serialize(idName)));
        			property.setValue(binary);
	        	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
		
        getTemplate().save();
	}
	
    /*
     * Serialize an object to a byte array.
     */
    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        try {
            out.writeObject(object);
        } finally {
            out.close();
        }

        return bos.toByteArray();
    }

    /*
     * Deserialize an object from a byte array.
     */
    private Object deserialize(InputStream byteStream) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(byteStream);
        try {
            return in.readObject();
        } finally {
            in.close();
        }
    }
    
}
