package fr.ipgp.earlywarning.utilities;

import java.io.File;
import java.io.IOException;

/**
 * A class for common, path-related utilities.
 *
 * @author Thomas Kowalski
 */

public class PathUtilities {
    /**
     * Returns the current Java working directory
     *
     * @return the working directory, given by <code>System.getProperty("user.dir")</code>
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir") + "/";
    }

    /**
     * Builds a canonical path from a relative path and the current workind directory
     *
     * @param relativePath the relative path to build the canonical path from
     * @return the canonical counterpart of <code>relativePath</code>
     */
    public static String buildPath(String relativePath) {
        try {
            return new File(getWorkingDirectory() + relativePath).getCanonicalPath();
        } catch (IOException e) {
            return getWorkingDirectory() + "/" + relativePath;
        }
    }
}
