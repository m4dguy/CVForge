package cvforge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComboBox;

import ij.IJ;
import ij.WindowManager;
import ij.process.ImageProcessor;

/**
 * Cache for objects created e.g. by the CVForgeConstructor or method calls. 
 */
public class CVForgeCache {
	// cached objects with names associated to them.
	protected static HashMap<String, Object> cache = new HashMap<String, Object>();
	// listeners notified if cache changes
	protected static LinkedList<CacheListener> listeners = new LinkedList<CacheListener>();
	
	
	/**
	 * Create a JComboBox containing all cached Objects of specified class. 
	 * @param classType Type of objects to display in ComboBox. Enter Object.class to list entire cache.
	 * @return Constructed JComboBox.
	 */
	public static JComboBox<String> createComboBox(Class classType){
		ArrayList<String> validKeys = new ArrayList<String>();
		for(Entry<String, Object> entry: cache.entrySet()){
			if(entry.getValue().getClass() == classType){
				validKeys.add(entry.getKey());
			}
		}
	
		String[] conv = new String[validKeys.size()];
		for(int i=0; i<validKeys.size(); ++i)
			conv[i] = validKeys.get(i);
		
		JComboBox<String> cacheBox;
		if(!validKeys.isEmpty())
			cacheBox = new JComboBox<String>(conv);
		else
			cacheBox = new JComboBox<String>();
			
		cacheBox.setEditable(false);
		cacheBox.setMaximumRowCount(5);
		return cacheBox;
	}
	
	/**
	 * Add ImageProcessor to cache.
	 * @param ip ImageProcessor to add.
	 */
	public void addImageProcessor(String name){
		ImageProcessor ip = WindowManager.getImage(name).getProcessor();
        if(ip == null){
        	add(name, ip);
        }
        notifyListeners();
	}
	
	/**
	 * Adds the current ImageProcessor to the cache.
	 */
	public void addActiveImageProcessor(){
		String name = WindowManager.getCurrentImage().getTitle();
		ImageProcessor ip = WindowManager.getCurrentImage().getProcessor();
        if(ip == null){
        	add(name, ip);
        }
        notifyListeners();
	}
	
	/**
	 * Adds Object to the cache and associates it to name.
	 * @param name The name to associate.
	 * @param obj Object to add to cache.
	 */
	public static void add(String name, Object obj){		
		if(cache.containsKey(name)){
			IJ.showStatus("cached object \"" + name + "\" of type " + obj.getClass().getSimpleName() + " updated");
			cache.replace(name, obj);
		} else{
			IJ.showStatus("named object \"" + name + "\" of type " + obj.getClass().getSimpleName() + " added to cache");
			cache.put(name, obj);
		}
		notifyListeners();
	}
	
	/**
	 * Remove all objects from cache.
	 */
	public static void clear(){
		IJ.showStatus("cache cleared");
		cache.clear();
		notifyListeners();
	}
	
	/**
	 * Get Set of entries in cache.
	 * @return Set of cache entries. 
	 */
	public static Set<Entry<String, Object>> getEntries(){
		return cache.entrySet();
	}
	
	/**
	 * Get number of elements in cache.
	 * @return Size of cache.
	 */
	public static int size(){
		return cache.size();
	}
	
	/**
	 * Get entry from cache.
	 * @param key Key/ name of entry.
	 * @return Cached entry if found, null else.
	 */
	public static Object get(String key){
		return cache.get(key);
	}
	
	/**
	 * Check if item with given key exists in cache.
	 * @param key Key to check for.
	 * @return true, if cached object with such key exists.
	 */
	public static boolean contains(String key){
		return cache.containsKey(key);
	}
	
	/**
	 * Remove entry denoted by key from cache.
	 * @param key Key identifying object to be removed.
	 */
	public static void remove(String key){
		cache.remove(key);
		notifyListeners();
	}
	
	/**
	 * Update the given key-value pair.
	 * @param key Key for update.
	 * @param value Object to be reassigned to key.
	 */
	public static void update(String key, Object value){
		cache.replace(key, value);
		notifyListeners();
	}
	
	/**
	 * Check if cache is empty.
	 * @return true, if cache is emtpy.
	 */
	public static boolean isEmpty(){
		return cache.isEmpty();
	}
	
	/**
	 * Register given listener s.t. it gets notified if the cache changes.
	 * @param listener CacheListener to register.
	 */
	public static void addListener(CacheListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Notify all registered CacheListeners.
	 */
	protected static void notifyListeners(){
		for(CacheListener cl: listeners){
			cl.cacheChanged();	
		}
	}
}
