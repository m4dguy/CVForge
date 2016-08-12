package cvforge;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import reflectiontools.ClassInspector;
import reflectiontools.JarInspector;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 
 */

public class LibTreeBuilder {
	/**
	 * Generate a tree of methods in library of given path.
	 * Methods are listed by subpackages and only included if they are public static void.
	 * Each node of the tree contains the name of the method/ package/ class as a string. 
	 * @param path Path to jar file.
	 * @return JTree representing the library.
	 */
    public static JTree generateLibTree(String path, ClassLoader loader, boolean shardsOnly) throws Exception{
        String rootName = getLibName(path);
        HashMap<String, FilteredTreeNode> cache = new HashMap<String, FilteredTreeNode>();
        FilteredTreeNode root = new FilteredTreeNode(rootName);        
        List<Class> classes = JarInspector.loadClassesFromJar(path, loader);
        for(Class c: classes){
        	// skip non-shards if shardOnly-mode
        	if(shardsOnly){
        		boolean isShard = false;
        		Class[] interfaces = c.getInterfaces();
        		for(Class inter: interfaces){
        			isShard |= inter.getSimpleName().equals("CVForgeShard");
        		}
        		if(!isShard){
        			continue;
        		}
        	}

            // hook package into lib definition tree
            Package pack = c.getPackage();
            FilteredTreeNode packNode = cache.get(pack.toString());
            if(packNode == null){
                packNode = new FilteredTreeNode(pack.getName());
                cache.put(pack.toString(), packNode);
                root.add(packNode);
            }

            // add class to package node
            FilteredTreeNode classNode = new FilteredTreeNode(c.getSimpleName());
            Method[] methods = c.getMethods();

            // add suitable methods
            boolean compatible = false;
            for(Method m: methods){
            	boolean publicStaticVoid = ClassInspector.isPublic(m) && ClassInspector.isStatic(m) && ClassInspector.isVoid(m);
            	boolean publicStatic = ClassInspector.isPublic(m) && ClassInspector.isStatic(m);
                if(publicStaticVoid || publicStatic){
                	FilteredTreeNode methodNode = new FilteredTreeNode(ClassInspector.infoString(m));
                    classNode.add(methodNode);
                    compatible = true;
                }
            }
            if(compatible)
            	packNode.add(classNode);
        }
        return new JTree(new FilteredTreeModel(root));
    }
    
    
    
    public static JTree generateLibTree(String path, ClassLoader loader) throws Exception{
    	return generateLibTree(path, loader, false);
    }

	/**
	 * Find childnode of root with fitting name. Uses breadth-first search.
	 * @param root Root node for starting search.
	 * @param childName String containing name of node.
	 * @return Matching node if any, null else.
	 */
    protected static DefaultMutableTreeNode findChild(DefaultMutableTreeNode root, String childName){
        Stack<DefaultMutableTreeNode> unchecked = new Stack<DefaultMutableTreeNode>();
        DefaultMutableTreeNode currentNode = root;
        do{
            Enumeration<DefaultMutableTreeNode> children = currentNode.children();
            while(children.hasMoreElements())
                unchecked.push(children.nextElement());

            currentNode = unchecked.pop();
            if(currentNode.getUserObject() == childName)
                return currentNode;

        }while(!unchecked.empty());
        return null;
    }

    /**
     * Returns name of library.
     * Basically just looks up what's in between the last occurring "/" and "." symbols.
     * @param libPath Path to java library file.  
     * @return Name of the library.
     */
    public static String getLibName(String libPath){
        return libPath.substring(libPath.lastIndexOf(CVForge.SEP)+1, libPath.lastIndexOf("."));
    }
}
