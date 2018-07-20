package fr.ipgp.earlywarning.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Methods for searching files
 *
 * @author Thomas Kowalski
 */
public class FileSearch {
    /**
     * Searches for a file given by its name in a directory and its children
     *
     * @param root the directory to search the file in
     * @param name the name of the file to search
     * @return the found {@link File}
     * @throws IOException if the file does not exist
     */
    public static File searchForFile(File root, String name) throws IOException {
        if (root == null)
            throw new FileNotFoundException();

        File[] files = root.listFiles();
        if (files == null)
            throw new FileNotFoundException();

        for (File f : files)
            if (f.isFile())
                if (f.getName().equalsIgnoreCase(name))
                    return f.getCanonicalFile();

        for (File f : files)
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
