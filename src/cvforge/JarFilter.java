package cvforge;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class JarFilter extends FileFilter{	
	@Override
	public boolean accept(File f) {
	    return (f.getName().endsWith(".jar")) || (f.isDirectory());
	}
	//The description of this filter
    @Override
	public String getDescription() {
        return "Jar files";
    }
}
