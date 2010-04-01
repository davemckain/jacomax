/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the batch mode aspects {@link MaximaProcessLauncher} class.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaBatchProcessTest extends MaximaProcessLauncherTestBase {
    
    @Before
    public void setup() {
        super.init();
    }
    
    @Test
    public void testBatchWorks() throws MaximaTimeoutException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        maximaProcessLauncher.runBatchProcess(in, out);
    }
}
