package Utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */

public class FileUtilities {
    
    public static List<String> getFileLines(String filePath) throws Exception {
        final List<String> lines = new ArrayList<String>();

        final BufferedReader reader = new BufferedReader(new FileReader(filePath));
        for(String line; (line = reader.readLine()) != null; ) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }
}