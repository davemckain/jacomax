/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This simple configurer tries to build a {@link MaximaConfiguration} by looking for Maxima
 * in standard/default locations for the OS being run on.
 * <p>
 * This might be useful if you have an application that needs to run on multiple platforms
 * with no configuration. But this approach probably won't cater for people with non-standard Maxima
 * installs.
 * <p>
 * (Thanks for Paul Neve and Hans-Petter Ulven for kindly contributing code snippets for this.)
 *
 * @see MaximaConfiguration
 * @see JacomaxSimpleConfigurator
 * @see JacomaxPropertiesConfigurator
 *
 * @author  David McKain
 * @author  Paul Neve
 * @author  Hans-Petter Ulven
 * @version $Revision$
 */
public final class JacomaxAutoConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(JacomaxAutoConfigurator.class);

    private static final String[] UNIX_EXECUTABLE_PATHS = {
        "/usr/bin/maxima",
        "/usr/local/bin/maxima"
    };

    private static final String[] MAC_OS_X_EXECUTABLE_PATHS = {
    	"/Applications/Maxima.app/Contents/Resources/maxima.sh", /* (Newer releases of Maxima) */
        "/Applications/Maxima.app/Contents/Resources/bin/maxima", /* (Older releases of Maxima) */
        "/opt/local/bin/maxima",
        "/usr/bin/maxima",
        "/usr/local/bin/maxima"
    };

    /**
     * Tries to create a {@link MaximaConfiguration} by searching for Maxima in standard/default
     * locations appropriate to the OS you are running on.
     * <p>
     * Returns null if this approach didn't yield anything useful.
     * <p>
     * (This generates some hopefully useful logging messages at DEBUG level.)
     *
     * @return hopefully usable {@link MaximaConfiguration}, null if this approach didn't
     *   succceed.
     */
    public static MaximaConfiguration guessMaximaConfiguration() {
        final MaximaConfiguration result = new MaximaConfiguration();

        final String osName = System.getProperty("os.name");
        if (osName!=null && osName.startsWith("Windows")) {
            final File programFilesFolder = findWindowsProgramFiles();
            if (programFilesFolder!=null) {
                logger.debug("Looking in Windows Program Files Folder ({}) for Maxima installs", programFilesFolder);
                final File maximaFolder = chooseBestWindowsMaximaFolder(programFilesFolder);
                if (maximaFolder!=null) {
                    logger.debug("Found Maxima with highest version number at {}", maximaFolder);
                    findWindowsMaximaExecutable(result, maximaFolder);
                }
                else {
                    logger.debug("Did not find any folders of the form Maxima-n.n.n, so giving up.");
                }
            }
        }
        else if ("Mac OS X".equals(osName)) {
            logger.debug("This is Mac OS X, looking for Maxima at {}", Arrays.toString(MAC_OS_X_EXECUTABLE_PATHS));
            final String executablePath = findExecutable(MAC_OS_X_EXECUTABLE_PATHS);
            if (executablePath!=null && executablePath.equals("/opt/local/bin/maxima")) {
                /* For MacPorts install, I found that I needed to set the PATH in this case */
                result.setMaximaRuntimeEnvironment(new String[] { "PATH=/opt/local/bin:/bin:/usr/bin" });
            }
            result.setMaximaExecutablePath(executablePath);
        }
        else {
            /* We'll try common paths for Unix-like systems */
            logger.debug("Looking for Maxima at the following Unixy locations: {}", Arrays.toString(UNIX_EXECUTABLE_PATHS));
            result.setMaximaExecutablePath(findExecutable(UNIX_EXECUTABLE_PATHS));
        }

        if (result.getMaximaExecutablePath()==null) {
            logger.warn("Could not guess an appropriate MaximaConfiguration");
            return null;
        }
        logger.info("Automatic configuration yielded (hopefully usable) result: {}", result);
        return result;
    }

    private static String findExecutable(final String[] searchLocations) {
        for (final String location : searchLocations) {
            if (new File(location).exists()) {
                logger.debug("Found file at {}, which will be assumed to be the Maxima executable", location);
                return location;
            }
            logger.debug("No potential Maxima executable found at {}");
        }
        return null;
    }

    private static File findWindowsProgramFiles() {
        final String[] searchLocations = new String[] {
                System.getenv("ProgramFiles"),
                System.getenv("ProgramFiles(x86)"),
                System.getenv("ProgramW6432"),
                "C:\\Program Files",
                "C:\\Program Files (x86)"
        };
        File locationFile;
        for (final String location : searchLocations) {
            if (location!=null) {
                locationFile = new File(location);
                if (locationFile.isDirectory()) {
                    return locationFile;
                }
            }
        }
        return null;
    }

    private static final String WINDOWS_MAXIMA_FOLDER_PREFIX = "Maxima-";

    private static FilenameFilter windowsMaximaFolderFilter = new FilenameFilter() {
        public boolean accept(final File dir, final String name) {
            return name.startsWith(WINDOWS_MAXIMA_FOLDER_PREFIX);
        }
    };

    private static File chooseBestWindowsMaximaFolder(final File programFilesFolder) {
        File result = null;
        String highestVersion = null;
        for (final File folder : programFilesFolder.listFiles(windowsMaximaFolderFilter)) {
            final String versionString = folder.getName().substring(WINDOWS_MAXIMA_FOLDER_PREFIX.length());
            if (highestVersion==null || versionString.compareTo(highestVersion) > 0) {
                result = folder;
                highestVersion = versionString;
            }
        }
        return result;
    }

    private static void findWindowsMaximaExecutable(final MaximaConfiguration target, final File maximaFolder) {
        final String maximaFolderPath = maximaFolder.getAbsolutePath();
        final Pattern windowsMagicPattern = Pattern.compile("^(.+?\\\\Maxima-([\\d.]+))");
        final Matcher windowsMagicMatcher = windowsMagicPattern.matcher(maximaFolderPath);
        if (windowsMagicMatcher.matches()) {
            /* (We are actually going to directly call the underlying GCL binary that's bundled with
             * the Windows Maxima EXE, which is a bit of a cheat. The reason we do this is so
             * that the Maxima process can be killed if there's a timeout. Otherwise, we'd just
             * be killing the maxima.bat script, which doesn't actually kill the child process on
             * Windows, leaving an orphaned process causing havoc.
             *
             * If you don't want to use GCL here, you'll need to specify the exact Lisp runtime
             * you want and the appropriate command line arguments and environment variables.
             * This information can be gleaned from the maxima.bat script itself.)
             */
            final String basePath = windowsMagicMatcher.group(1);
            final String versionString = windowsMagicMatcher.group(2);

            final File gclExecutable = new File(basePath + "\\lib\\maxima\\" + versionString + "\\binary-gcl\\maxima.exe");
            if (gclExecutable.isFile()) {
                logger.debug("Found standard GCL binary inside Maxima. Creating a configuration that will use this directly");
                target.setMaximaExecutablePath(gclExecutable.getAbsolutePath());
                target.setMaximaCommandArguments(new String[] { "-eval", "(cl-user::run)", "-f" });
                target.setMaximaRuntimeEnvironment(new String[] { "MAXIMA_PREFIX=" + basePath });
            }
            else {
                logger.warn("Found Windows Maxima folder at " + maximaFolderPath + " but it does not appear to contain the standard GCL binary. You will need to configure this manually");
            }
        }
    }
}