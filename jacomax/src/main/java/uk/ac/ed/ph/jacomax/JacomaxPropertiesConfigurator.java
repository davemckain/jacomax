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

import uk.ac.ed.ph.jacomax.internal.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class populates a {@link MaximaConfiguration} Object from a Java {@link Properties} file
 * or Object, with convenience constructors for searching for a {@link Properties} file in a number
 * of useful locations.
 *
 * <h2>Construction</h2>
 *
 * Use one of the following constructors as appropriate:
 *
 * <ul>
 *   <li>
 *     The default no argument constructor will look for a File called
 *     {@link #DEFAULT_PROPERTIES_RESOURCE_NAME} by searching (in order):
 *     the current working directory, your home directory, the ClassPath.
 *     If nothing is found, a {@link JacomaxConfigurationException} will be thrown.
 *   </li>
 *   <li>
 *     Use {@link #JacomaxPropertiesConfigurator(String, PropertiesSearchLocation...)}
 *     to search for a {@link Properties} file of the given name in the given
 *     {@link PropertiesSearchLocation}s. The first match wins.
 *     If nothing is found, a {@link JacomaxConfigurationException} will be thrown.
 *   </li>
 *   <li>
 *     Use the {@link File} or {@link Properties} constructor if you want to explicitly use
 *     the given {@link File} or {@link Properties} Object to supply configuration information.
 *   </li>
 * </ul>
 *
 * <h2>Properties File Format</h2>
 *
 * See <code>jacomax.properties.sample</code> for an example of the required/supported property
 * names and values.
 *
 * @see MaximaConfiguration
 * @see JacomaxAutoConfigurator
 * @see JacomaxSimpleConfigurator
 *
 * @author David McKain
 */
public final class JacomaxPropertiesConfigurator {

    private static final Logger logger = LoggerFactory.getLogger(JacomaxPropertiesConfigurator.class);

    /** Enumerates the various locations to search in. */
    public enum PropertiesSearchLocation {
        CURRENT_DIRECTORY,
        USER_HOME_DIRECTORY,
        CLASSPATH,
        SYSTEM,
    }

    /** Default properties resource name, used if nothing explicit stated */
    public static final String DEFAULT_PROPERTIES_RESOURCE_NAME = "jacomax.properties";

    /** Name of property specifying {@link MaximaConfiguration#getMaximaExecutablePath()} */
    public static final String MAXIMA_EXECUTABLE_PATH_PROPERTY_NAME = "jacomax.maxima.path";

    /** Base name of properties specifying {@link MaximaConfiguration#getMaximaCommandArguments()} */
    public static final String MAXIMA_COMMAND_ARGUMENTS_PROPERTY_BASE_NAME = "jacomax.maxima.arg";

    /** Base name of properties specifying {@link MaximaConfiguration#getMaximaRuntimeEnvironment()} */
    public static final String MAXIMA_ENVIRONMENT_PROPERTY_BASE_NAME = "jacomax.maxima.env";

    /** Name of property specifying {@link MaximaConfiguration#getMaximaCharset()} */
    public static final String MAXIMA_CHARSET_PROPERTY_NAME = "jacomax.maxima.charset";

    /** Name of property specifying {@link MaximaConfiguration#getDefaultCallTimeout()} */
    public static final String DEFAULT_CALL_TIMEOUT_PROPERTY_NAME = "jacomax.default.call.timeout";

    /** Name of property specifying {@link MaximaConfiguration#getDefaultBatchTimeout()} */
    public static final String DEFAULT_BATCH_TIMEOUT_PROPERTY_NAME = "jacomax.default.batch.timeout";

    /** Resolved Properties */
    private final Properties properties;

    /** Description of where {@link #properties} was resolved from */
    private final String propertiesSourceDescription;

    //----------------------------------------------------------------

    /**
     * This constructor looks for a resource called {@link #DEFAULT_PROPERTIES_RESOURCE_NAME}
     * by searching the current directory, your home directory and finally the ClassPath.
     */
    public JacomaxPropertiesConfigurator() {
        this(DEFAULT_PROPERTIES_RESOURCE_NAME,
                PropertiesSearchLocation.CURRENT_DIRECTORY,
                PropertiesSearchLocation.USER_HOME_DIRECTORY,
                PropertiesSearchLocation.CLASSPATH);
    }

    /**
     * This constructor looks for a resource called propertiesName, searching in the locations
     * specified in the order specified.
     * <p>
     * Note that {@link PropertiesSearchLocation#SYSTEM} will always "win" over anything appearing
     * after it.
     */
    public JacomaxPropertiesConfigurator(final String propertiesName, final PropertiesSearchLocation... propertiesSearchPath) {
        Assert.notNull(propertiesName, "propertiesName");
        Properties theProperties = null;
        String thePropertiesSourceDescription = null;
        File tryFile;
        SEARCH: for (final PropertiesSearchLocation location : propertiesSearchPath) {
            switch (location) {
                case CURRENT_DIRECTORY:
                    tryFile = new File(System.getProperty("user.dir"), propertiesName);
                    theProperties = tryPropertiesFile(tryFile);
                    if (theProperties!=null) {
                        logger.debug("Creating Maxima configuration from properties file {} found in current directory", tryFile.getPath());
                        thePropertiesSourceDescription = "File " + tryFile.getPath() + " (found in current directory)";
                        break SEARCH;
                    }
                    continue SEARCH;

                case USER_HOME_DIRECTORY:
                    tryFile = new File(System.getProperty("user.home"), propertiesName);
                    theProperties = tryPropertiesFile(tryFile);
                    if (theProperties!=null) {
                        logger.debug("Creating Maxima configuration from properties file {} found in user home directory", tryFile.getPath());
                        thePropertiesSourceDescription = "File " + tryFile.getPath() + " (found in user home directory)";
                        break SEARCH;
                    }
                    continue SEARCH;

                case CLASSPATH:
                    final InputStream propertiesStream = JacomaxPropertiesConfigurator.class.getClassLoader().getResourceAsStream(propertiesName);
                    if (propertiesStream!=null) {
                        theProperties = readProperties(propertiesStream, "ClassPath resource " + propertiesName);
                        logger.debug("Creating Maxima configuration using properties file {} found in ClassPath", propertiesStream);
                        thePropertiesSourceDescription = "ClassPath resource " + propertiesName;
                        break SEARCH;
                    }
                    continue SEARCH;

                case SYSTEM:
                    theProperties = System.getProperties();
                    logger.debug("Creating Maxima configuration from System properties");
                    thePropertiesSourceDescription = "System properties";
                    break SEARCH;

                default:
                    throw new JacomaxLogicException("Unexpected switch fall-through on " + location);
            }
        }
        if (theProperties==null) {
            throw new JacomaxConfigurationException("Could not load properties file/resource " + propertiesName
                    + " using search path "
                    + Arrays.toString(propertiesSearchPath));
        }
        this.properties = theProperties;
        this.propertiesSourceDescription = thePropertiesSourceDescription;
    }

    /**
     * This constructor uses the provided {@link Properties} Object as a source of
     * configuration information.
     */
    public JacomaxPropertiesConfigurator(final Properties maximaProperties) {
        this.properties = maximaProperties;
        this.propertiesSourceDescription = "Properties Object " + maximaProperties;
    }

    /**
     * This constructor uses the provided {@link File} as a source of configuration information.
     * configuration information.
     */
    public JacomaxPropertiesConfigurator(final File propertiesFile) throws FileNotFoundException {
        final FileInputStream fileInputStream = new FileInputStream(propertiesFile);
        try {
            this.properties = readProperties(fileInputStream, "File " + propertiesFile.getPath());
        }
        finally {
            ensureClose(fileInputStream);
        }
        this.propertiesSourceDescription = "Explicitly specified File " + propertiesFile.getPath();
    }

    /**
     * This constructor uses the provided {@link File} as a source of configuration information.
     * configuration information.
     */
    public JacomaxPropertiesConfigurator(final InputStream inputStream) {
        this.properties = readProperties(inputStream, "Stream " + inputStream.toString());
        this.propertiesSourceDescription = "Explicitly specified InputStream " + inputStream.toString();
    }

    private Properties tryPropertiesFile(final File file) {
        InputStream propertiesStream = null;
        logger.trace("Checking for existence of Jacomax properties file at {}", file.getPath());
        try {
            propertiesStream = new FileInputStream(file);
            logger.trace("Found {}", file.getPath());
            return readProperties(propertiesStream, "File " + file.getPath());
        }
        catch (final FileNotFoundException e) {
            logger.trace("Did not find {}", file.getPath());
            return null;
        }
        finally {
            ensureClose(propertiesStream);
        }
    }

    private void ensureClose(final InputStream inputStream) {
        if (inputStream!=null) {
            try {
                inputStream.close();
            }
            catch (final IOException e) {
                throw new JacomaxRuntimeException("Unexpected Exception closing " + inputStream, e);
            }
        }
    }

    private Properties readProperties(final InputStream inputStream, final String inputDescription) {
        final Properties result = new Properties();
        try {
            result.load(inputStream);
        }
        catch (final IOException e) {
            throw new JacomaxConfigurationException("IOException occurred when reading Maxima properties from "
                    + inputDescription, e);
        }
        return result;
    }

    //----------------------------------------------------------------

    public Properties getProperties() {
        return properties;
    }

    public String getPropertiesSourceDescription() {
        return propertiesSourceDescription;
    }

    //----------------------------------------------------------------

    public MaximaConfiguration configure() {
        final MaximaConfiguration result = new MaximaConfiguration();
        configure(result);
        return result;
    }

    public void configure(final MaximaConfiguration config) {
        config.setMaximaExecutablePath(getRequiredProperty(MAXIMA_EXECUTABLE_PATH_PROPERTY_NAME));
        config.setMaximaCommandArguments(getIndexedProperty(MAXIMA_COMMAND_ARGUMENTS_PROPERTY_BASE_NAME));
        config.setMaximaRuntimeEnvironment(getIndexedProperty(MAXIMA_ENVIRONMENT_PROPERTY_BASE_NAME));
        config.setMaximaCharset(getProperty(MAXIMA_CHARSET_PROPERTY_NAME));
        config.setDefaultCallTimeout(getIntegerProperty(DEFAULT_CALL_TIMEOUT_PROPERTY_NAME));
        config.setDefaultBatchTimeout(getIntegerProperty(DEFAULT_BATCH_TIMEOUT_PROPERTY_NAME));
    }

    //----------------------------------------------------------------

    public String getProperty(final String propertyName) {
        return properties.getProperty(propertyName);
    }

    public String getRequiredProperty(final String propertyName) {
        final String result = getProperty(propertyName);
        if (result==null) {
            throw new JacomaxConfigurationException("Required property " + propertyName
                    + " not specified in " + propertiesSourceDescription);
        }
        return result;
    }

    public String[] getIndexedProperty(final String propertyNameBase) {
        final List<String> resultList = new ArrayList<String>();
        String indexedValue;
        for (int i=0; ;i++) { /* (Keep reading until we get a null or empty property) */
            indexedValue = getProperty(propertyNameBase + i);
            if (indexedValue==null || indexedValue.trim().length()==0) {
                break;
            }
            resultList.add(indexedValue);
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    public int getIntegerProperty(final String propertyName) {
        Integer result;
        final String valueString = getProperty(propertyName);
        if (valueString!=null) {
            try {
                result = Integer.valueOf(valueString);
            }
            catch (final NumberFormatException e) {
                throw new JacomaxConfigurationException("Default timeout " + valueString + " must be an integer");
            }
        }
        else {
            result = null;
        }
        return result!=null ? result.intValue() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + propertiesSourceDescription + "]";
    }
}
