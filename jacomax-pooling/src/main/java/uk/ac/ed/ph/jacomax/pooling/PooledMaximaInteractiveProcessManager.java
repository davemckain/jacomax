/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.pooling;

import uk.ac.ed.ph.jacomax.JacomaxRuntimeException;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;
import uk.ac.ed.ph.jacomax.internal.ConstraintUtilities;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  David McKain
 * @version $Revision$
 */
public class PooledMaximaInteractiveProcessManager implements MaximaInteractiveProcessManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PooledMaximaInteractiveProcessManager.class);
    
    private final GenericObjectPool processPool;
    
    public PooledMaximaInteractiveProcessManager(final MaximaProcessLauncher maximaProcessLauncher) {
        ConstraintUtilities.ensureNotNull(maximaProcessLauncher, "maximaProcessLauncher");
        
        logger.info("Creating MaximaInteractiveProcess Object pool");
        PoolableMaximaInteractiveProcessFactory factory = new PoolableMaximaInteractiveProcessFactory(maximaProcessLauncher);
        processPool = new GenericObjectPool(factory);
        processPool.setTestOnBorrow(true);
        processPool.setTestOnReturn(true);
    }
    
    //---------------------------------------------------------

    public void shutdown() {
        logger.info("Closing MaximaInteractiveProcess Object pool");
        try {
            processPool.close();
        }
        catch (Exception e) {
            throw new JacomaxRuntimeException("Could not shut down process pool", e);
        }
    }
    
    //---------------------------------------------------------
    
    public MaximaInteractiveProcess obtainProcess() {
        try {
            return (MaximaInteractiveProcess) processPool.borrowObject();
        }
        catch (Exception e) {
            throw new JacomaxRuntimeException("Could not obtain MaximaInteractiveProcess from pool", e);
        }
    }
    
    public void returnProcess(MaximaInteractiveProcess process) {
        try {
            processPool.returnObject(process);
        }
        catch (Exception e) {
            throw new JacomaxRuntimeException("Could not return MaximaInteractiveProcess to pool", e);
        }
    }
}
