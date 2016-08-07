package cvforge;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Filtered node. Checks if node (or its String representation) contains the filter term.
 * Only hides leaves. Overwrite determineVisibility() to alter this behavior.
 * UserObjects are cast to string before applying the filter.
 */
public class FilteredTreeNode extends DefaultMutableTreeNode{
	// filter to use
	protected String filter;
	// internal visibility status
	protected boolean visible;
	
	/**
	 * Construct empty node.
	 */
	public FilteredTreeNode() {
		this(null);
	}

	/**
	 * Construct node with given userObject. 
	 * @param userObject
	 */
	public FilteredTreeNode(Object userObject) {
		this(userObject, true, "");
	}
	
	/**
	 * Construct node with userObject and filter term.
	 * @param userObject UserObject contained in node.
	 * @param allowsChildren Set true, if this is a leaf.
	 * @param filter Filter term.
	 */
	public FilteredTreeNode(Object userObject, boolean allowsChildren, String filter) {
		super(userObject, allowsChildren);
		this.setFilter(filter);
  	}

	/**
	 * Get child at given index.
	 * Excludes invisible nodes.
	 * @param index Index of node.
	 */
	public TreeNode getChildAt(int index) {
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
	    }

	    int realIndex = -1;		// actual node index in child list
	    int visibleIndex = -1;	// index of visible nodes
	    Enumeration e = children.elements();
	    while (e.hasMoreElements()) {
    		FilteredTreeNode node = (FilteredTreeNode) e.nextElement();
	    	if(node.isVisible()) {
	    		visibleIndex++;
	      	}
	      	realIndex++;
	      	if(visibleIndex == index) {
	      		return (TreeNode) children.elementAt(realIndex);
      		}
	    }
	    throw new ArrayIndexOutOfBoundsException("index unmatched");
	}

	/**
	 * Count number of children. 
	 * Excludes invisible nodes.
	 */
	public int getChildCount() {
	    if(children == null) {
	    	return 0;
	    }
	    int count = 0;
	    Enumeration e = children.elements();
	    while(e.hasMoreElements()) {
	    	FilteredTreeNode node = (FilteredTreeNode)e.nextElement();
	    	if (node.isVisible()) {
	    		++count;
	    	}
	    }
	    return count;
	}

	/**
	 * Sets String for filtering.
	 * Iterates over children and sets their filter.
	 * @param filter String for filtering node userObject.
	 */
	public void setFilter(String filter){
		this.filter = filter;
		Enumeration<TreeNode> e = this.children();
		while(e.hasMoreElements()){
			FilteredTreeNode node = (FilteredTreeNode)e.nextElement();
			node.setFilter(filter);
		}		
		determineVisibilty();
	}

	/**
	 * Check if node is filtered and set visibility status.
	 */
	protected void determineVisibilty(){
		if(!this.isLeaf() || filter.isEmpty()){
			this.visible = true;
			return;
		}
		String label = userObject.toString().toLowerCase();
		visible = label.contains(filter);
	}
	
	/**
	 * Check if node is supposed to be shown.
	 * @return true, if node is not leaf, filter does not exist or filter string contained in object.
	 */
	protected boolean isVisible() {	
		return this.visible;
	}
}
