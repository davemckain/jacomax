/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.pooling;

import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;
import uk.ac.ed.ph.jacomax.internal.ConstraintUtilities;

/**
 * Simplest possible implementation of {@link MaximaInteractiveProcessManager}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public class DefaultMaximaInteractiveProcessManager implements MaximaInteractiveProcessManager {
    
    private final MaximaProcessLauncher maximaProcessLauncher;
    
    public DefaultMaximaInteractiveProcessManager(MaximaProcessLauncher maximaProcessLauncher) {
        ConstraintUtilities.ensureNotNull(maximaProcessLauncher, "maximaProcessLauncher");
        this.maximaProcessLauncher = maximaProcessLauncher;
    }
    
    public MaximaInteractiveProcess obtainProcess() {
        return maximaProcessLauncher.launchInteractiveProcess();
    }
    
    public void returnProcess(MaximaInteractiveProcess process) {
        process.terminate();
    }
}
