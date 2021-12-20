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
package uk.ac.ed.ph.jacomax.utilities;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the {@link MaximaOutputUtilities#parseSingleLinearOutput(String)} method.
 *
 * @author David McKain
 */
@RunWith(Parameterized.class)
public class MaximaOutputUtilitiesParseSingleLinearOutputTest {

    public static final Collection<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
            /* (Real MathML output example) */
            { "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"> <mn>1</mn> </math>\n(%o2)                                false\n",
              "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"> <mn>1</mn> </math>\n",
              "(%o2)",
              "false"
            }
    });

    @Parameters
    public static Collection<Object[]> data() {
        return TEST_DATA;
    }

    private final String rawOutput;
    private final String expectedOutput;
    private final String expectedOutputPrompt;
    private final String expectedResult;

    public MaximaOutputUtilitiesParseSingleLinearOutputTest(final String rawOutput, final String expectedOutput, final String expectedOutputPrompt, final String expectedResut) {
        this.rawOutput = rawOutput;
        this.expectedOutput = expectedOutput;
        this.expectedOutputPrompt = expectedOutputPrompt;
        this.expectedResult = expectedResut;
    }

    @Test
    public void runTest() {
        final SingleLinearOutput parsed = MaximaOutputUtilities.parseSingleLinearOutput(rawOutput);
        if (expectedOutput==null) {
            Assert.assertNull(parsed);
        }
        else {
            Assert.assertNotNull(parsed);
            Assert.assertEquals(expectedOutput, parsed.getOutput());
            Assert.assertEquals(expectedOutputPrompt, parsed.getOutputPrompt());
            Assert.assertEquals(expectedResult, parsed.getResult());
        }
    }
}
