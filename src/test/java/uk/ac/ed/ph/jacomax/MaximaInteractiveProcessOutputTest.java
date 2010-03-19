/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

import uk.ac.ed.ph.jacomax.utilities.MaximaOutputUtilities;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Additional tests for {@link MaximaInteractiveProcess} that checks the resulting
 * outputs.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaInteractiveProcessOutputTest extends MaximaProcessLauncherTestBase {
    
    protected MaximaInteractiveProcess maximaInteractiveProcess;
    
    @Before
    public void setup() {
        super.init();
        
        /* Start process */
        maximaInteractiveProcess = maximaProcessLauncher.launchInteractiveProcess();
    }
    
    @After
    public void cleanup() {
        /* Kill process so we have a clean slate each time */
        if (maximaInteractiveProcess!=null) {
            maximaInteractiveProcess.terminate();
            maximaInteractiveProcess = null;
        }
    }
    
    protected void doSingleOutputCall(String maximaExpression, String expectedResult) throws Exception {
        String result = maximaInteractiveProcess.executeCall(maximaExpression + ";");
        Assert.assertEquals(expectedResult, MaximaOutputUtilities.parseSingleLinearOutputResult(result));
    }
    
    @Test
    public void testMultipleCalls() throws Exception {
        for (int i=0; i<1000; i++) {
            doSingleOutputCall(String.valueOf(i), String.valueOf(i));
        }
    }
    
    @Test
    public void testSimpleSingleLine() throws Exception {
        doSingleOutputCall("1", "1");
    }
    
    @Test
    public void testLessSimpleSingleLine() throws Exception {
        doSingleOutputCall("simp:false$ string(1+x)", "1+x");
    }
    
    @Test
    public void testEmptyStringOutput() throws Exception {
        /* (Maxima returns a blank output here, rather than "", which is what you might normally expect!) */
        doSingleOutputCall("string(\"\")", "");
    }
    
    @Test
    public void testSplitSingleLine() throws Exception {
        /* (Maxima splits the raw output, which gets rejoined by our code) */
        doSingleOutputCall("60!", "8320987112741390144276341183223364380754172606361245952449277696409600000000000000");
    }
}
