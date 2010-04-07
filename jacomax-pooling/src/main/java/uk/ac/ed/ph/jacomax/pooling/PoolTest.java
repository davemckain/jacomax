/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.pooling;

import uk.ac.ed.ph.jacomax.JacomaxSimpleConfigurator;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;

/**
 * Standalone test class. This should end up as a unit test... bad Dave!
 *
 * @author  David McKain
 * @version $Revision$
 */
public class PoolTest {
    
    public static void main(String[] args) throws Exception {
        MaximaProcessLauncher launcher = new MaximaProcessLauncher(JacomaxSimpleConfigurator.configure());
        PooledMaximaInteractiveProcessManager manager = new PooledMaximaInteractiveProcessManager(launcher);
        
        MaximaInteractiveProcess session = manager.obtainProcess();
        System.out.println("Math 1: " + session.executeCall("1;"));
        System.out.println("Math 2: " + session.executeCall("2;"));
        
        manager.returnProcess(session);
        manager.shutdown();
    }
}
