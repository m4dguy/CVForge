package cvforge;

/**
 * Interface for notifying classes depending on CVForgeCache.
 * Registers classes implementing this interface in CVForgeCache to make them aware of cache cahnges.
 */
public interface CacheListener {
	/**
	 * Called when CVForgeCache experiences additions or removals of elements.
	 */
	public void cacheChanged();
}
