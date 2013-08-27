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
package ro.nextreports.server.pivot.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class Node implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Node parent;
    private Object data;
    private List<Node> children;

    public Node() {
        children = new ArrayList<Node>();
    }

    public Node(Object data) {
    	this.data = data;
        children = new ArrayList<Node>();
    }

    public Node getParent() {
		return parent;
	}

    public boolean isRoot() {
    	return parent == null;
    }
    
    public int getLevel() {
    	int level = 0;
    	Node p = parent;
    	while (p != null) {
    		++level;
    		p = p.parent;
    	}
    	
        return level;
    }
    
    public boolean isLeaf() {
    	return children.isEmpty();
    }

	public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<Node> getChildren() {
//        return new ArrayList<Node>(children); /* return a shallow copy */
    	return children;
    }

    public void addChild(Node child) {
    	child.parent = this;
        children.add(child);
    }
    
    public void insert(Object data) {
        addChild(new Node(data));
    }
    
	public List<Object> getPathValues() {
		if (isRoot()) {
			return Collections.emptyList();
		}
		
		List<Object> pathValues = new ArrayList<Object>();
		pathValues.add(getData());
		Node parent = getParent();
    	while (parent != null) {
    		pathValues.add(parent.getData());
    		parent = parent.getParent();
    	}
    	pathValues.remove(pathValues.size() - 1); // remove root
    	Collections.reverse(pathValues);
			
		return pathValues;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Node other = (Node) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return (data == null) ? "null" : data.toString();
    }
    
}
