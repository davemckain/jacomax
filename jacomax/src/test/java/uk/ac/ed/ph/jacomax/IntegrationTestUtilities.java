/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some trivial utility methods for the test suite.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class IntegrationTestUtilities {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestUtilities.class);

    public static MaximaConfiguration getMaximaConfiguration() {
        MaximaConfiguration result = null;
        try {
            result = JacomaxSimpleConfigurator.configure();
        }
        catch (final JacomaxConfigurationException e) {
            final String failureMessage = "Could not create a MaximaConfiguration using " + JacomaxSimpleConfigurator.class.getSimpleName()
                + ". You will either have to create an explicit jacomax.properties file (see " + JacomaxPropertiesConfigurator.class.getSimpleName()
                + "), or we might be able to tweak " + JacomaxAutoConfigurator.class.getSimpleName()
                + " to handle your OS/Maxima setup better...";
            logger.error(failureMessage);
            Assert.fail(failureMessage);
        }
        return result;
    }
}
