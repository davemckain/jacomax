/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.utilities;

import uk.ac.ed.ph.jacomax.utilities.MaximaOutputUtilities;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Some tests for {@link MaximaOutputUtilities#parseLinearResult(String)}
 *
 * @author  David McKain
 * @version $Revision$
 */
@RunWith(Parameterized.class)
public class MaximaOutputUtilitiesParseLinearResultTest {
    
    public static Collection<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
            { "1", "1" },
            { "\"1\"", "\"1\"" },
            { "1\\\n2", "12" },
            { "\"1\\\n2\"", "\"1\n2\"" },
            { "1\\2", "12" }
    });
    
    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return TEST_DATA;
    }
    
    private final String rawResult;
    private final String expected;
    
    public MaximaOutputUtilitiesParseLinearResultTest(String rawResult, String expected) {
        this.rawResult = rawResult;
        this.expected = expected;
    }
    
    @Test
    public void runTest() {
        String result = MaximaOutputUtilities.parseLinearResult(rawResult);
        Assert.assertEquals(expected, result);
    }
}
