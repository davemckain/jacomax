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
import uk.ac.ed.ph.jacomax.internal.MaximaBatchProcessImpl;
import uk.ac.ed.ph.jacomax.internal.MaximaInteractiveProcessImpl;
import uk.ac.ed.ph.jacomax.internal.MaximaProcessController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main entry point into Jacomax.
 * <p>
 * Create one of these Objects using a {@link MaximaConfiguration} to indicate how to run
 * and interact with Maxima. You can then:
 * <ul>
 *   <li>
 *     Use {@link #launchInteractiveProcess()} to create a Maxima process that you can use
 *     in a simple "interactive" mode by sending a number of calls to Maxima and getting
 *     results back.
 *   </li>
 *   <li>
 *     Use {@link #runBatchProcess(InputStream, OutputStream)} to run a Maxima process in
 *     a simple batch mode
 *   </li>
 * </ul>
 * An instance of this class is thread-safe, provided that the {@link MaximaConfiguration}
 * it was created with is not modified.
 *
 * @author David McKain
 */
public final class MaximaProcessLauncher {

    private static final Logger logger = LoggerFactory.getLogger(MaximaProcessLauncher.class);

    /** Default value for {@link MaximaConfiguration#getDefaultCallTimeout()} */
    public static final int DEFAULT_CALL_TIMEOUT = 60;

    /** Default value for {@link MaximaConfiguration#getDefaultBatchTimeout()} */
    public static final int DEFAULT_BATCH_TIMEOUT = 180;

    /** Default value for {@link MaximaConfiguration#getMaximaCharset()} */
    public static final String DEFAULT_MAXIMA_CHARSET = "US-ASCII";

    /** Underlying {@link MaximaConfiguration} used by this launcher */
    private final MaximaConfiguration maximaConfiguration;

    /**
     * Creates a new Maxima process launcher, using the given {@link MaximaConfiguration}
     * to specify how to run and connect to Maxima.
     */
    public MaximaProcessLauncher(final MaximaConfiguration maximaConfiguration) {
        Assert.notNull(maximaConfiguration, "MaximaConfiguration");
        this.maximaConfiguration = maximaConfiguration;
    }

    /**
     * Launches a new {@link MaximaInteractiveProcess} that you can send individual calls
     * to.
     */
    public MaximaInteractiveProcess launchInteractiveProcess() {
        return launchInteractiveProcess(null);
    }

    /**
     * Launches a new {@link MaximaInteractiveProcess} that you can send individual calls
     * to.
     *
     * @param maximaStderrHandler optional OutputStram that will receive any STDERR output
     *   from Maxima. This may be null, which will result in this output being discarded.
     *   The caller is reponsible for closing this stream afterwards.
     */
    public MaximaInteractiveProcess launchInteractiveProcess(final OutputStream maximaStderrHandler) {
        final MaximaInteractiveProcessImpl process = new MaximaInteractiveProcessImpl(newMaximaProcessController(maximaStderrHandler),
                computeDefaultTimeout(maximaConfiguration.getDefaultCallTimeout(), DEFAULT_CALL_TIMEOUT),
                computeMaximaCharset());
        process.advanceToFirstInputPrompt();
        logger.debug("Maxima interactive process started and ready for communication");
        return process;
    }

    /**
     * Runs a Maxima process in a kind of "batch" mode, feeding it data from the given
     * batchInputStream and sending the resulting output to batchOutputStream.
     * <p>
     * The default timeout specified in {@link MaximaConfiguration} is used here.
     * <p>
     * On completion, the batchOutputStream will be flushed. The caller is however responsible for
     * closing all streams provided.
     *
     * @param batchInputStream batch input stream, which must not be null
     * @param batchOutputStream batch output stream, which must not be null
     *
     * @return underlying exit value from the Maxima process
     *
     * @throws MaximaTimeoutException if the process exceeded its timeout and had to be killed.
     */
    public int runBatchProcess(final InputStream batchInputStream, final OutputStream batchOutputStream)
            throws MaximaTimeoutException {
        Assert.notNull(batchInputStream, "batchInputStream");
        Assert.notNull(batchOutputStream, "batchOutputStream");
        return doRunBatchProcess(batchInputStream, batchOutputStream, null,
                computeDefaultTimeout(maximaConfiguration.getDefaultBatchTimeout(), DEFAULT_BATCH_TIMEOUT));
    }

    /**
     * Runs a Maxima process in a kind of "batch" mode, feeding it data from the given
     * batchInputStream and sending the resulting output to batchOutputStream.
     * <p>
     * The default timeout specified in {@link MaximaConfiguration} is used here.
     * <p>
     * An additional optional OutputStream will receive the STDERR from Maxima
     * <p>
     * On completion, the batchOutputStream will be flushed. The caller is however responsible for
     * closing all streams provided.
     *
     * @param batchInputStream batch input stream, which must not be null
     * @param batchOutputStream batch output stream, which must not be null
     * @param batchErrorStream optional output stream to receive Maxima STDERR. If null, then
     *   STDERR output will be discarded.
     *
     * @return underlying exit value from the Maxima process
     *
     * @throws MaximaTimeoutException if the process exceeded its timeout and had to be killed.
     */
    public int runBatchProcess(final InputStream batchInputStream, final OutputStream batchOutputStream,
            final OutputStream batchErrorStream)
            throws MaximaTimeoutException {
        Assert.notNull(batchInputStream, "batchInputStream");
        Assert.notNull(batchOutputStream, "batchOutputStream");
        return doRunBatchProcess(batchInputStream, batchOutputStream, batchErrorStream,
                computeDefaultTimeout(maximaConfiguration.getDefaultBatchTimeout(), DEFAULT_BATCH_TIMEOUT));
    }

    /**
     * Runs a Maxima process in a kind of "batch" mode, feeding it data from the given
     * batchInputStream and sending the resulting output to batchOutputStream.
     * <p>
     * The specified timeout (in seconds) is used here. If this value is positive and
     * the batch process has not completed by this time, then the proces is killed and a
     * {@link MaximaTimeoutException} is thrown.
     * <p>
     * On completion, the batchOutputStream will be flushed. The caller is however responsible for
     * closing all streams provided.
     *
     * @param batchInputStream batch input stream, which must not be null
     * @param batchOutputStream batch output stream, which must not be null
     * @param timeout timeout to use. Zero or less indicates that no timeout should be
     *   applied
     *
     * @return underlying exit value from the Maxima process
     *
     * @throws MaximaTimeoutException if the process exceeded its timeout and had to be killed.
     */
    public int runBatchProcess(final InputStream batchInputStream, final OutputStream batchOutputStream,
            final int timeout)
            throws MaximaTimeoutException {
        Assert.notNull(batchInputStream, "batchInputStream");
        Assert.notNull(batchOutputStream, "batchOutputStream");
        return doRunBatchProcess(batchInputStream, batchOutputStream, null, timeout);
    }

    /**
     * Runs a Maxima process in a kind of "batch" mode, feeding it data from the given
     * batchInputStream and sending the resulting output to batchOutputStream.
     * <p>
     * The specified timeout (in seconds) is used here. If this value is positive and
     * the batch process has not completed by this time, then the proces is killed and a
     * {@link MaximaTimeoutException} is thrown.
     * <p>
     * An additional optional OutputStream will receive the STDERR from Maxima
     * <p>
     * On completion, the batchOutputStream will be flushed. The caller is however responsible for
     * closing all streams provided.
     *
     * @param batchInputStream batch input stream, which must not be null
     * @param batchOutputStream batch output stream, which must not be null
     * @param batchErrorStream optional output stream to receive Maxima STDERR. If null, then
     *   STDERR output will be discarded.
     * @param timeout timeout to use. Zero or less indicates that no timeout should be
     *   applied
     *
     * @return underlying exit value from the Maxima process
     *
     * @throws MaximaTimeoutException if the process exceeded its timeout and had to be killed.
     */
    public int runBatchProcess(final InputStream batchInputStream, final OutputStream batchOutputStream,
            final OutputStream batchErrorStream, final int timeout)
            throws MaximaTimeoutException {
        Assert.notNull(batchInputStream, "batchInputStream");
        Assert.notNull(batchOutputStream, "batchOutputStream");
        return doRunBatchProcess(batchInputStream, batchOutputStream, batchErrorStream, timeout);
    }

    private int doRunBatchProcess(final InputStream batchInputStream, final OutputStream batchOutputStream,
            final OutputStream batchErrorStream, final int timeout)
            throws MaximaTimeoutException {
        final MaximaBatchProcessImpl batchProcess = new MaximaBatchProcessImpl(newMaximaProcessController(batchErrorStream), batchInputStream, batchOutputStream);
        return batchProcess.run(timeout);
    }

    //------------------------------------------------------------------------

    private Charset computeMaximaCharset() {
        String charset = maximaConfiguration.getMaximaCharset();
        if (charset==null) {
            charset = DEFAULT_MAXIMA_CHARSET;
        }
        try {
            return Charset.forName(charset);
        }
        catch (final IllegalCharsetNameException e) {
            throw new JacomaxConfigurationException("Unknown character set " + charset, e);
        }
    }

    private int computeDefaultTimeout(final int configured, final int defaultValue) {
        if (configured > 0) {
            return configured;
        }
        else if (configured==0) {
            return defaultValue;
        }
        else {
            return 0;
        }
    }

    private MaximaProcessController newMaximaProcessController(final OutputStream maximaStderrHandler) {
        return new MaximaProcessController(this, launchMaximaProcess(), maximaStderrHandler);
    }

    private Process launchMaximaProcess() {
        /* Extract relevant configuration required to get Maxima running */
        final String maximaExecutablePath = maximaConfiguration.getMaximaExecutablePath();
        final String[] maximaCommandArguments = maximaConfiguration.getMaximaCommandArguments();
        String[] maximaRuntimeEnvironment = maximaConfiguration.getMaximaRuntimeEnvironment();
        if (maximaExecutablePath==null) {
            throw new JacomaxConfigurationException("maximaExecutablePath must not be null");
        }

        /* Build up the resulting command that we will execute */
        final List<String> maximaCommandArray = new ArrayList<String>();
        final Pattern windowsMagicPattern = Pattern.compile("^(.+?\\\\Maxima-([\\d.]+))\\\\bin\\\\maxima.bat$");
        final Matcher windowsMagicMatcher = windowsMagicPattern.matcher(maximaExecutablePath);
        if (windowsMagicMatcher.matches()) {
            /* (We are actually going to directly call the underlying GCL binary that's bundled with
             * the Windows Maxima EXE, which is a bit of a cheat. The reason we do this is so
             * that the Maxima process can be killed if there's a timeout. Otherwise, we'd just
             * be killing the maxima.bat script, which doesn't actually kill the child process on
             * Windows, leaving an orphaned process causing havoc.
             *
             * If you don't want to use GCL here, you'll need to specify the exact Lisp runtime
             * you want and the appropriate command line arguments and environment variables.
             * This information can be gleaned from the maxima.bat script itself.)
             */
            logger.debug("Replacing configured call to Windows Maxima batch file with call to "
                    + "the underlying GCL binary that comes with vanilla Windows Maxima installs. "
                    + "If you don't want this, please adjust your configuration!");
            final String basePath = windowsMagicMatcher.group(1);
            final String versionString = windowsMagicMatcher.group(2);

            maximaCommandArray.add(basePath + "\\lib\\maxima\\" + versionString + "\\binary-gcl\\maxima.exe");
            maximaCommandArray.add("-eval");
            maximaCommandArray.add("(cl-user::run)");
            maximaCommandArray.add("-f");
            if (maximaCommandArguments.length>0) {
                maximaCommandArray.add("--");
            }
            if (maximaRuntimeEnvironment==null || maximaRuntimeEnvironment.length==0) {
                /* (This makes sure Maxima can find modules and suchlike) */
                maximaRuntimeEnvironment = new String[] { "MAXIMA_PREFIX=" + basePath };
            }
            else {
                logger.warn("I have replaced the maximaExecutablePath in order to invoke the underlying GCL binary."
                        + " I would normally update your maximaRuntimeEnvironment to set MAXIMA_PREFIX"
                        + " but you have already set this, so I'm going with your decision. You may find you"
                        + " need to add a MAXIMA_PREFIX setting to the environment (if you haven't done so already)"
                        + " so that Maxima can find any modules you want to load");
            }
        }
        else {
            maximaCommandArray.add(maximaExecutablePath);
        }
        if (maximaCommandArguments!=null) {
            for (final String arg : maximaCommandArguments) {
                maximaCommandArray.add(arg);
            }
        }

        /* Now start the process up */
        Process result;
        try {
            if (logger.isInfoEnabled()) {
                logger.debug("Starting Maxima cmdarray {} with environment {}",
                        maximaCommandArray, Arrays.toString(maximaRuntimeEnvironment));
            }
            result = Runtime.getRuntime().exec(maximaCommandArray.toArray(new String[maximaCommandArray.size()]), maximaRuntimeEnvironment);
            logger.trace("Maxima process started");
        }
        catch (final IOException e) {
            throw new JacomaxRuntimeException("Could not launch Maxima process", e);
        }
        return result;
    }
}
