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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class TreeIterator implements Iterator<Node> {

    private Iterator<Node> iterator;

    public TreeIterator(Node root) {
        this.iterator = getList(root).iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

	public Node next() {
		return iterator.next();
	}

    public Iterator<Object> getValuesIterator() {
        List<Object> values = new ArrayList<Object>();
        while (hasNext()) {
            values.add(next().getData());
        }

        return values.iterator();
    }

    private List<Node> getList(Node node) {
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);
        Iterator<Node> iterator = node.getChildren().iterator();
        while (iterator.hasNext()) {
        	nodes.addAll(getList(iterator.next()));
        }

        return nodes;
    }

}
