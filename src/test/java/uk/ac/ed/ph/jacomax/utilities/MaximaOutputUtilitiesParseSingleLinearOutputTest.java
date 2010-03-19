/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.utilities;

import uk.ac.ed.ph.jacomax.utilities.MaximaOutputUtilities;
import uk.ac.ed.ph.jacomax.utilities.SingleLinearOutput;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the {@link MaximaOutputUtilities#parseSingleLinearOutput(String)} method.
 *
 * @author  David McKain
 * @version $Revision$
 */
@RunWith(Parameterized.class)
public class MaximaOutputUtilitiesParseSingleLinearOutputTest {
    
    public static Collection<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
    		/* (Real MathML output example) */
            { "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"> <mn>1</mn> </math>\n(%o2)                                false\n",
        	  "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"> <mn>1</mn> </math>\n",
        	  "(%o2)",
        	  "false"
            }
    });
    
    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return TEST_DATA;
    }
    
    private final String rawOutput;
    private final String expectedOutput;
    private final String expectedOutputPrompt;
    private final String expectedResult;
    
    public MaximaOutputUtilitiesParseSingleLinearOutputTest(String rawOutput, String expectedOutput, String expectedOutputPrompt, String expectedResut) {
        this.rawOutput = rawOutput;
        this.expectedOutput = expectedOutput;
        this.expectedOutputPrompt = expectedOutputPrompt;
        this.expectedResult = expectedResut;
    }
    
    @Test
    public void runTest() {
        SingleLinearOutput parsed = MaximaOutputUtilities.parseSingleLinearOutput(rawOutput);
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
