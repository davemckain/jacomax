/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

/**
 * This simple POJO is used to specify how a {@link MaximaProcessLauncher}
 * should run and interact with Maxima.
 * <p>
 * The {@link JacomaxPropertiesConfigurator} helper class can be used to populate one
 * of these Objects by finding and reading a {@link Properties} Object in various
 * ways, which might be useful in some circumstances.
 *
 * @see JacomaxSimpleConfigurator
 * @see JacomaxAutoConfigurator
 * @see JacomaxPropertiesConfigurator
 *
 * @author  David McKain
 * @version $Revision$
 */
public class MaximaConfiguration implements Serializable, Cloneable {

    private static final long serialVersionUID = -7568972957741246870L;

    /**
     * Full path to your Maxima executable file.
     * <p>
     * This must not be null.
     */
    private String maximaExecutablePath;

    /**
     * Command-line arguments to pass to Maxima executable.
     * <p>
     * This may be null (or empty) if you don't want to specify anything here.
     */
    private String[] maximaCommandArguments;

    /**
     * This allows you to pass any required environment variables to Maxima,
     * as described in {@link Runtime#exec(String[], String[])} as <tt>envp</tt>.
     * <p>
     * This may be null (or empty) if you don't need to specify anything here.
     */
    private String[] maximaRuntimeEnvironment;

    /**
     * Character encoding used by Maxima when running a {@link MaximaInteractiveProcess},
     * which is usually a property of the underlying Lisp platform it's running on.
     * <p>
     * If null, we will use a default value of <tt>US-ASCII</tt>, which is probably fine
     * in most situations.
     */
    private String maximaCharset;

    /**
     * Default time to wait when executing a single call with {@link MaximaInteractiveProcess}
     * before the underlying process gets killed.
     * <p>
     * Set this to zero to use the default value of {@link MaximaProcessLauncher#DEFAULT_CALL_TIMEOUT}.
     * Set this to a negative number to stop this safety feature from happening.
     */
    private int defaultCallTimeout;

    /**
     * Default time to wait when executing a batch operation with {@link MaximaProcessLauncher#runBatchProcess(java.io.InputStream, java.io.OutputStream)}
     * before the underlying process gets killed.
     * <p>
     * Set this to zero to use the default value of {@link MaximaProcessLauncher#DEFAULT_BATCH_TIMEOUT}.
     * Set this to a negative number to stop this safety feature from happening.
     */
    private int defaultBatchTimeout;

    public MaximaConfiguration() {
    }

    /**
     * Convenience copy constructor
     */
    public MaximaConfiguration(final MaximaConfiguration source) {
        this.maximaExecutablePath = source.maximaExecutablePath;
        this.maximaCommandArguments = safeClone(source.maximaCommandArguments);
        this.maximaRuntimeEnvironment = safeClone(source.maximaRuntimeEnvironment);
        this.maximaCharset = source.maximaCharset;
        this.defaultCallTimeout = source.defaultCallTimeout;
        this.defaultBatchTimeout = source.defaultBatchTimeout;
    }

    public String getMaximaExecutablePath() {
        return maximaExecutablePath;
    }

    public void setMaximaExecutablePath(final String maximaExecutablePath) {
        this.maximaExecutablePath = maximaExecutablePath;
    }


    public String[] getMaximaCommandArguments() {
        return safeClone(maximaCommandArguments);
    }

    public void setMaximaCommandArguments(final String[] maximaCommandArguments) {
        this.maximaCommandArguments = safeClone(maximaCommandArguments);
    }


    public String[] getMaximaRuntimeEnvironment() {
        return safeClone(maximaRuntimeEnvironment);
    }

    public void setMaximaRuntimeEnvironment(final String[] maximaRuntimeEnvironment) {
        this.maximaRuntimeEnvironment = safeClone(maximaRuntimeEnvironment);
    }


    public String getMaximaCharset() {
        return maximaCharset;
    }

    public void setMaximaCharset(final String maximaCharset) {
        this.maximaCharset = maximaCharset;
    }


    public int getDefaultCallTimeout() {
        return defaultCallTimeout;
    }

    public void setDefaultCallTimeout(final int defaultCallTimeout) {
        this.defaultCallTimeout = defaultCallTimeout;
    }


    public int getDefaultBatchTimeout() {
        return defaultBatchTimeout;
    }


    public void setDefaultBatchTimeout(final int defaultBatchTimeout) {
        this.defaultBatchTimeout = defaultBatchTimeout;
    }

    //-------------------------------------------------------------------

    private String[] safeClone(final String[] source) {
        return source!=null ? source.clone() : null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
            + "(maximaExecutablePath=" + maximaExecutablePath
            + ",maximaCommandArguments=" + Arrays.toString(maximaCommandArguments)
            + ",maximaRuntimeEnvironment=" + Arrays.toString(maximaRuntimeEnvironment)
            + ",maximaCharset=" + maximaCharset
            + ",defaultCallTimeout=" + defaultCallTimeout
            + ",defaultBatchTimeout=" + defaultBatchTimeout
            + ")";
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (final CloneNotSupportedException e) {
            throw new JacomaxLogicException("Unexpected clone failure", e);
        }
    }
}
