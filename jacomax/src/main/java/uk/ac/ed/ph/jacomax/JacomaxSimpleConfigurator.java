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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This configurator uses a mix of {@link JacomaxAutoConfigurator} and
 * {@link JacomaxPropertiesConfigurator} and is probably the simplest way of getting a useful
 * {@link MaximaConfiguration} without requiring much effort.
 *
 * @author David McKain
 */
public final class JacomaxSimpleConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(JacomaxSimpleConfigurator.class);

    /** Enumerate the methods we'll use to obtain a MaximaConfiguration here */
    public static enum ConfigMethod {

        /** Use {@link JacomaxAutoConfigurator} */
        AUTO,

        /** Use {@link JacomaxPropertiesConfigurator} to search for properties in the default locations */
        PROPERTIES_SEARCH,
        ;
    }

    /**
     * Tries to obtain a {@link MaximaConfiguration} by first using {@link JacomaxPropertiesConfigurator}
     * to look for an appropriate Properties file, then using {@link JacomaxAutoConfigurator} if
     * that doesn't work.
     *
     * @return resulting {@link MaximaConfiguration}
     *
     * @throws JacomaxConfigurationException if this process didn't yield anything useful.
     */
    public static MaximaConfiguration configure() {
        return configure(ConfigMethod.PROPERTIES_SEARCH, ConfigMethod.AUTO);
    }

    /**
     * Tries to obtain a {@link MaximaConfiguration} using the given methods in order.
     *
     * @param configMethods methods to try in order to obtain a {@link MaximaConfiguration}
     *
     * @return resulting {@link MaximaConfiguration}
     *
     * @throws JacomaxConfigurationException if this process didn't yield anything useful.
     */
    public static MaximaConfiguration configure(final ConfigMethod... configMethods) {
        MaximaConfiguration result = null;
        for (final ConfigMethod method : configMethods) {
            switch (method) {
                case AUTO:
                    logger.trace("Trying automatic configuration");
                    result = JacomaxAutoConfigurator.guessMaximaConfiguration();
                    if (result==null) {
                        logger.trace("Automatic configuration attempt did not succeed");
                    }
                    break;

                case PROPERTIES_SEARCH:
                    logger.trace("Trying configuration via properties search in default locations");
                    JacomaxPropertiesConfigurator propertiesConfigurator;
                    try {
                        propertiesConfigurator = new JacomaxPropertiesConfigurator();
                    }
                    catch (final JacomaxConfigurationException e) {
                        logger.trace("Properties search did not succeed");
                        break;
                    }
                    result = propertiesConfigurator.configure();
                    break;

                default:
                    throw new JacomaxLogicException("Unexpected switch case " + method);
            }
            if (result!=null) {
                break;
            }
        }
        if (result==null) {
            logger.warn("Configuration did not yield anything");
            throw new JacomaxConfigurationException("Could not obtain a MaximaConfiguration after attempting configuration methods "
                    + Arrays.toString(configMethods));
        }
        return result;
    }

}
