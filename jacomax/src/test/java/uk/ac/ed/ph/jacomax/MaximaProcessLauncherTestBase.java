/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

/**
 * Base class for integration tests of the {@link MaximaProcessLauncher} class.
 *
 * @author  David McKain
 * @version $Revision$
 */
public abstract class MaximaProcessLauncherTestBase {

    protected MaximaProcessLauncher maximaProcessLauncher;

    public void init() {
        final MaximaConfiguration configuration = IntegrationTestUtilities.getMaximaConfiguration();
        maximaProcessLauncher = new MaximaProcessLauncher(configuration);
    }
}
