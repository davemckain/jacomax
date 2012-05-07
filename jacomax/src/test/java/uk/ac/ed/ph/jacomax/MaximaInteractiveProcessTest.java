/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic tests of {@link MaximaInteractiveProcess} to make sure that calls run successfully.
 *
 * @see MaximaInteractiveProcessOutputTest
 *
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaInteractiveProcessTest extends MaximaProcessLauncherTestBase {

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

    @Test(expected=IllegalArgumentException.class)
    public void testCallNull() throws Exception {
        maximaInteractiveProcess.executeCall(null);
    }

    @Test
    public void testSimpleCall() throws Exception {
        maximaInteractiveProcess.executeCall("1;");
    }

    @Test
    public void testSlowCall() throws Exception {
        maximaInteractiveProcess.executeCall("sum((-2)^n/n!,n,1,1500);", 180);
    }

    @Test
    public void testLargeCall() throws Exception {
        final StringBuilder callBuilder = new StringBuilder();
        for (int i=0; i<10000; i++) {
            callBuilder.append(i).append("; \"This is a big long string to help fill up the input buffer\";");
        }
        maximaInteractiveProcess.executeCall(callBuilder.toString(), 180);
    }

    @Test
    public void testDoubleCall() throws Exception {
        maximaInteractiveProcess.executeCall("1;2;");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testUnterminatedInput() throws Exception {
        maximaInteractiveProcess.executeCall("0");
    }

    @Test(expected=MaximaTimeoutException.class)
    public void testTimeout() throws Exception {
        maximaInteractiveProcess.executeCall("for i: 1 while true do 1;", 1);
    }

    /**
     * Makes sure we can do a Lisp call successfully.
     */
    @Test
    public void testLisp() throws Exception {
    	maximaInteractiveProcess.executeCall(":lisp (princ 1)");
    }

    /**
     * This tests making a call that doesn't emit a newline at the end of output, followed
     * by no response. Older versions of the mathml.lisp module used to do something like
     * this.
     */
    @Test
    public void testNoNewlineCall() throws Exception {
    	maximaInteractiveProcess.executeCallDiscardOutput(":lisp (defun $f () (write-char #\\a))");
    	maximaInteractiveProcess.executeCall("f()$");
	}

    /**
     * Regression test for the fix introduced in revision 18. This makes sure the required call
     * terminator is still output if earlier code causes an error.
     */
    @Test
    public void testBadCommand() throws Exception {
        maximaInteractiveProcess.executeCall("+;");
    }
}
