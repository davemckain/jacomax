/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.samples;

import uk.ac.ed.ph.jacomax.JacomaxSimpleConfigurator;
import uk.ac.ed.ph.jacomax.MaximaConfiguration;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;
import uk.ac.ed.ph.jacomax.utilities.MaximaOutputUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example that does a trivial Maxima call with exceptionally verbose logging.
 * <p>
 * (Might be useful for diagnosing and submitting bug reports.)
 *
 * @author  David McKain
 * @version $Revision$
 */
public class DiagnosticExample {
    
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticExample.class);
    
    public static void main(String[] args) throws Exception {
        setupLog4J();
        try {
            MaximaConfiguration configuration = JacomaxSimpleConfigurator.configure();
            MaximaProcessLauncher launcher = new MaximaProcessLauncher(configuration);
            MaximaInteractiveProcess process = launcher.launchInteractiveProcess();
            String result = process.executeCall("1;", 10);
            String parsed = MaximaOutputUtilities.parseSingleLinearOutputResult(result);
            process.terminate();
            if ("1".equals(parsed)) {
                logger.info("DiagnosticExample ran successfully");
            }
            else {
                logger.error("DiagnosticExample did not work as expected!");
                logger.info("Raw result was: " + result);
            }
        }
        catch (Exception e) {
            logger.error("DiagnosticExample throw an Exception when running", e);
        }
    }
    
    private static void setupLog4J() throws Exception {
        /* We configure Log4J to be exceptionally verbose here, but without requiring
         * any config files to get loaded. We would normally do this directly 
         * 
         * BasicConfigurator.configure();
         * org.apache.log4j.Logger.getRootLogger().setLevel(Level.TRACE);
         * 
         * but I want to avoid a compile-time dependency, hence the following mucky
         * equivalent using reflection:
         */
        Class.forName("org.apache.log4j.BasicConfigurator").getMethod("configure").invoke(null);
        Object rootLogger = Class.forName("org.apache.log4j.Logger").getMethod("getRootLogger").invoke(null);
        Class<?> levelClass = Class.forName("org.apache.log4j.Level");
        Object traceLevel = levelClass.getDeclaredField("TRACE").get(levelClass);
        rootLogger.getClass().getMethod("setLevel", levelClass).invoke(rootLogger, traceLevel);

    }
}
