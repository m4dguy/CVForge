package cvforge;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Stupid configloader.
 */
public class ConfigIO {
    /**
     * Strips config files of whitespace and tabs, then reads them into a HashMap.
     * Reads config files of structure:
     * # this line is a comment
     * number = 512
     * text   = a very long text
     * Lines starting with # are ignored (use these for comments).
     *
     * @param path Path to file.
     * @param sep Separator to be used; e.g. "=" in example above.
     * @return HashMap containing parameters.
     */
    public static HashMap<String, String> loadConfig(String path, String sep){
        HashMap<String, String> config = new HashMap<String, String>();
        try{
            FileInputStream stream = new FileInputStream(new File(path));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while(true){
                line = reader.readLine();
                if(line == null) {
                    break;
                }

                // skip comments
                if(line.startsWith("#"))
                    continue;

                // split and read key/value pair
                int split = line.indexOf(sep);
                String key = line.substring(0, split).trim();
                String value = line.substring(split+1, line.length()).trim();
                config.put(key, value);
            }
            reader.close();
        }catch(Exception e){}
        return config;
    }

    public static HashMap<String, String> loadConfig(String path){
        return loadConfig(path, "=");
    }

    /**
     * Write HashMap to config file.
     * @param params HashMAp with keys and values for parameters.
     * @param path Path to target file.
     * @param sep Separator like "="
     */
    public static void writeConfig(HashMap<String, String> params, String path, String sep){
        try{
            FileWriter writer = new FileWriter(new File(path));
            for(Map.Entry<String, String> entry: params.entrySet()){
                writer.write(entry.getKey() + "\t=\t" + entry.getValue() + System.lineSeparator());
            }
            writer.close();
        }catch(Exception e){}
    }

    public static void writeConfig(HashMap<String, String> params, String path){ writeConfig(params, path, "=");}
}
