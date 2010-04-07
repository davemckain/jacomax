/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.pooling;

import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;
import uk.ac.ed.ph.jacomax.internal.ConstraintUtilities;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PoolableObjectFactory} catering in {@link MaximaInteractiveProcess}s.
 *
 * @author  David McKain
 * @version $Revision$
 */
public class PoolableMaximaInteractiveProcessFactory implements PoolableObjectFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(PoolableMaximaInteractiveProcessFactory.class);
    
    private MaximaProcessLauncher maximaProcessLauncher;
    
    public PoolableMaximaInteractiveProcessFactory(MaximaProcessLauncher maximaProcessLauncher) {
        ConstraintUtilities.ensureNotNull(maximaProcessLauncher, "maximaProcessLauncher");
        this.maximaProcessLauncher = maximaProcessLauncher;
    }
    
    //---------------------------------------------------------

    public Object makeObject() {
        logger.debug("Creating new pooled MaximaInteractiveProcess");
        MaximaInteractiveProcess maximaInteractiveProcess = maximaProcessLauncher.launchInteractiveProcess();
        return maximaInteractiveProcess;
    }
    
    public void activateObject(Object obj) {
        /* (Nothing to do here) */
    }
    
    public void passivateObject(Object obj) {
        logger.debug("Resetting MaximaInteractiveProcess and passivating");
        MaximaInteractiveProcess process = (MaximaInteractiveProcess) obj;
        if (process.isTerminated()) {
            throw new IllegalStateException("Expected pool to verify Objects before passivation");
        }
        try {
            process.softReset();
        }
        catch (Exception e) {
            logger.warn("Could not reset process - terminating so that it is no longer considered valid");
            process.terminate();
        }
    }
    
    public boolean validateObject(Object obj) {
        MaximaInteractiveProcess process = (MaximaInteractiveProcess) obj;
        return !process.isTerminated();
    }
    
    public void destroyObject(Object obj) {
        logger.debug("Terminating pooled MaximaInteractiveProcess");
        MaximaInteractiveProcess process = (MaximaInteractiveProcess) obj;
        process.terminate();
    }
}
