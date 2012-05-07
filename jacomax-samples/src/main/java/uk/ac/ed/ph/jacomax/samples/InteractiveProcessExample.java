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

/**
 * Example of using a {@link MaximaInteractiveProcess}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public class InteractiveProcessExample {

    public static void main(final String[] args) throws Exception {
        final MaximaConfiguration configuration = JacomaxSimpleConfigurator.configure();
        final MaximaProcessLauncher launcher = new MaximaProcessLauncher(configuration);
        final MaximaInteractiveProcess process = launcher.launchInteractiveProcess();
        final String result = process.executeCall("1+2;", 10);

        System.out.println("Result is: " + MaximaOutputUtilities.parseSingleLinearOutputResult(result));

        process.terminate();
    }

}
