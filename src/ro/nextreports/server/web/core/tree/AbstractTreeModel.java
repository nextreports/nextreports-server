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
package ro.nextreports.server.web.core.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.wicket.markup.html.tree.AbstractTree;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractTreeModel implements TreeModel, Serializable {
	
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>(0);
	
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	public Object getChild(Object parent, int index) {
		List<?> children = ((TreeNode) parent).getChildren();
		return children != null ? children.get(index) : null;
	}

	public int getChildCount(Object parent) {
		List<?> children = ((TreeNode) parent).getChildren();
		return children != null ? children.size() : 0;
	}

	public int getIndexOfChild(Object parent, Object child) {
		List<?> children = ((TreeNode) parent).getChildren();
		return children != null ? children.indexOf(child) : -1;
	}

	public boolean isLeaf(Object node) {
		return ((TreeNode) node).isLeaf();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	public Object getParent(AbstractTree tree, Object node) {
		return tree.getParentNode(node);
	}

	private TreePath pathFromNode(AbstractTree tree, TreeNode node) {
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		for (TreeNode n = node; n != null; n = (TreeNode) getParent(tree, n)) {
			nodes.add(0, n);
		}
		
		return new TreePath(nodes.toArray(new TreeNode[nodes.size()]));
	}

	/**
	 * Notifies the tree that the given node has been changed while it's
	 * children remained unchanged.
	 * 
	 * @param tree
	 * @param node
	 */
	public void nodeChanged(AbstractTree tree, TreeNode node) {
		TreeNode parent = (TreeNode) getParent(tree, node);
		if (parent != null) {
			int index = parent.getChildren().indexOf(node);
			if (index != -1) {
				TreeModelEvent event = new TreeModelEvent(this, pathFromNode(tree, parent), 
						new int[] { index }, new Object[] { node });
				for (TreeModelListener listener : listeners) {
					listener.treeNodesChanged(event);
				}
			}
		}
	}

	/**
	 * Notifies the tree that the given node has been added.
	 * 
	 * @param tree
	 * @param node
	 */
	public void nodeInserted(AbstractTree tree, TreeNode parent, TreeNode node) {
		int index = parent.getChildren().indexOf(node);
		if (index != -1) {
			TreeModelEvent event = new TreeModelEvent(this, pathFromNode(tree, parent), 
					new int[] { index }, new Object[] { node });
			for (TreeModelListener listener : listeners) {
				listener.treeNodesInserted(event);
			}
		}
	}

	/**
	 * Notifies the tree that the given node will be removed. This method must
	 * be called <b>before</b> the node is actually deleted.
	 * 
	 * @param tree
	 * @param node
	 */
	public void nodeDeleted(AbstractTree tree, TreeNode node) {
		TreeNode parent = (TreeNode) getParent(tree, node);
		if (parent != null) {
			int index = parent.getChildren().indexOf(node);
			if (index != -1) {
				TreeModelEvent event = new TreeModelEvent(this, pathFromNode(tree, parent), 
						new int[] { index }, new Object[] { node });
				for (TreeModelListener listener : listeners) {
					listener.treeNodesRemoved(event);
				}
			}
		}
	}

	/**
	 * Notifies the tree that the children of given node have been changed.
	 * 
	 * @param tree
	 * @param node
	 */
	public void nodeChildrenChanged(AbstractTree tree, TreeNode node) {
		TreeModelEvent event = new TreeModelEvent(this, pathFromNode(tree, node));
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}
	
}
