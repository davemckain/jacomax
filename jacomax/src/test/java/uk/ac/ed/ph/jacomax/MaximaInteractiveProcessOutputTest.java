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

import uk.ac.ed.ph.jacomax.utilities.MaximaOutputUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Additional tests for {@link MaximaInteractiveProcess} that checks the resulting
 * outputs.
 *
 * @author David McKain
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

    protected void doSingleOutputCall(final String maximaExpression, final String expectedResult) throws Exception {
        final String result = maximaInteractiveProcess.executeCall(maximaExpression + ";");
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

// Maxima 5.21 and above outputs "" for string(""), which is consistent but doesn't give us the
// output wanted for this test. I can't think of any functions which now give an empty output, so
// have commented this test out.
//    @Test
//    public void testEmptyStringOutput() throws Exception {
//        /* (Maxima returns a blank output here, rather than "", which is what you might normally expect!) */
//        doSingleOutputCall("string(\"\")", "");
//    }

    @Test
    public void testSplitSingleLine() throws Exception {
        /* (Maxima splits the raw output, which gets rejoined by our code) */
        doSingleOutputCall("60!", "8320987112741390144276341183223364380754172606361245952449277696409600000000000000");
    }
}
