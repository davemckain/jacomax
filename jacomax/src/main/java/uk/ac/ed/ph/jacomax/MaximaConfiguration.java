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
 * @author David McKain
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
     * as described in {@link Runtime#exec(String[], String[])} as <code>envp</code>.
     * <p>
     * This may be null (or empty) if you don't need to specify anything here.
     */
    private String[] maximaRuntimeEnvironment;

    /**
     * Character encoding used by Maxima when running a {@link MaximaInteractiveProcess},
     * which is usually a property of the underlying Lisp platform it's running on.
     * <p>
     * If null, we will use a default value of <code>US-ASCII</code>, which is probably fine
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
        if (source!=null) {
            this.maximaExecutablePath = source.maximaExecutablePath;
            this.maximaCommandArguments = safeClone(source.maximaCommandArguments);
            this.maximaRuntimeEnvironment = safeClone(source.maximaRuntimeEnvironment);
            this.maximaCharset = source.maximaCharset;
            this.defaultCallTimeout = source.defaultCallTimeout;
            this.defaultBatchTimeout = source.defaultBatchTimeout;
        }
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
