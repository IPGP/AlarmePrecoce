package fr.ipgp.earlywarning.utilities;

import java.io.File;
import java.io.IOException;

public class PathUtilities {
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir") + "/";
    }

    public static String buildPath(String relativePath) {
        try {
            return new File(getWorkingDirectory() + relativePath).getCanonicalPath();
        } catch (IOException e) {
            return getWorkingDirectory() + "/" + relativePath;
        }
    }
}
