/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.diagnostics;

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
public class JacomaxDiagnostic {
    
    private static final Logger logger = LoggerFactory.getLogger(JacomaxDiagnostic.class);
    
    public static void main(String[] args) throws Exception {
        try {
            MaximaConfiguration configuration = JacomaxSimpleConfigurator.configure();
            MaximaProcessLauncher launcher = new MaximaProcessLauncher(configuration);
            MaximaInteractiveProcess process = launcher.launchInteractiveProcess();
            String result = process.executeCall("1;", 10);
            String parsed = MaximaOutputUtilities.parseSingleLinearOutputResult(result);
            process.terminate();
            if ("1".equals(parsed)) {
                logger.info("JacomaxDiagnostic ran successfully");
            }
            else {
                logger.error("JacomaxDiagnostic did not work as expected!");
                logger.info("Raw result was: " + result);
            }
        }
        catch (Exception e) {
            logger.error("JacomaxDiagnostic throw an Exception when running", e);
        }
    }
}
