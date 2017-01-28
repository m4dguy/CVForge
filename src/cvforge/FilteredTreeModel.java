package cvforge;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * TreeModel which dynamically filters its nodes.
 * For use with JTree. Build a tree with FilteredTreeNode objects.
 * Then assign the node to this model.
 * 
 * Sample:
 * 	FilteredTreeNode root = new FilteredTreeNode(rootName);
 * 	FilteredTreeModel model = new FilteredTreeModel(root, "my filter string");
 * 	JTree tree = new JTree(model);
 * 
 */
public class FilteredTreeModel extends DefaultTreeModel {

	/**
	 * String for filtering.
	 * Only nodes containing the filter String are displayed.
	 * Keep filter at "" to disable filtering. 
	 */
	protected String filter;
	
	/**
	 * Create new TreeModel with given node.
	 * @param root Root node for creation.
	 */
	public FilteredTreeModel(TreeNode root) {
		this(root, false);
	}
	
	/**
	 * Create new TreeModel with given node.
	 * @param root Root node for creation.
	 * @param asksAllowsChildren Allow children.
	 */
	public FilteredTreeModel(TreeNode root, boolean asksAllowsChildren) {
	    this(root, false, "");
	}
	
	/**
	 * Create new TreeModel with given node.
	 * @param root Root node for creation.
	 * @param asksAllowsChildren Allow children.
	 * @param filter String for filtering.
	 */
	public FilteredTreeModel(TreeNode root, boolean asksAllowsChildren, String filter) {
    	super(root, asksAllowsChildren);
    	this.filter = filter;
	}
	
	/**
	 * Get child node from parent.
	 * Respects filtering! Filtered nodes are excluded.
	 * @param parent Parent node to retrieve child from.
	 * @param index Index of child of interest.
	 * @return Selected child of parent node.
	 */
	public Object getChild(Object parent, int index) {
		if(filterIsActive()) {
			if(parent instanceof FilteredTreeNode) {
	    		return ((FilteredTreeNode)parent).getChildAt(index);
		  	}
		}
		return ((TreeNode)parent).getChildAt(index);
	}
	
	/**
	 * Count number of children in node.
	 * Respects filtering! Filtered nodes are excluded.
	 * @param parent Parent to ask.
	 * @return Number of children.
	 */
	public int getChildCount(Object parent) {
		if(filterIsActive()) {
			if(parent instanceof FilteredTreeModel) {
				return ((FilteredTreeNode)parent).getChildCount();
		  	}
	  	}
  		return ((TreeNode)parent).getChildCount();
	}
	
	/**
	 * Set new String for filtering nodes.
	 * Will be cast to lowercase!
	 * @param filter 
	 */
	public void setFilter(String filter){
		this.filter = filter.toLowerCase();
		FilteredTreeNode top = (FilteredTreeNode)this.root;
		top.setFilter(this.filter);
	}
	
	/**
	 * Check if filter exists.
	 * @return true, if filter is non-empty.
	 */
	protected boolean filterIsActive(){
		return !filter.isEmpty();
	}
}
	