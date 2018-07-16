package fr.ipgp.earlywarning.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileSearch {
    public static File searchForFile(File root, String name) throws IOException {
        for (File f : root.listFiles())
            if (f.isFile())
                if (f.getName().equalsIgnoreCase(name))
                    return f.getCanonicalFile();

        for (File f : root.listFiles())
            if (f.isDirectory() && (!f.equals(root))) {
                try {
                    File result = searchForFile(f, name);
                    return result.getCanonicalFile();
                } catch (FileNotFoundException ignored) {

                }
            }

        throw new FileNotFoundException();
    }

    public static File searchForFile(String name) throws IOException {
        return searchForFile(new File("."), name);
    }

    public static File searchForFile(String root, String name) throws IOException {
        return searchForFile(new File(root).getCanonicalFile(), name);
    }
}
