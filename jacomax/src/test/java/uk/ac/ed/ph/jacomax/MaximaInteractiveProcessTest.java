/* Copyright (c) 2010 - 2012, The University of Edinburgh.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 * 
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * @author David McKain
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
