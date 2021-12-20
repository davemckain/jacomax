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
package uk.ac.ed.ph.jacomax.internal;

import uk.ac.ed.ph.jacomax.JacomaxLogicException;
import uk.ac.ed.ph.jacomax.JacomaxRuntimeException;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessLauncher;
import uk.ac.ed.ph.jacomax.MaximaProcessTerminatedException;
import uk.ac.ed.ph.jacomax.MaximaTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides basic I/O functionality for a Maxima process.
 * <p>
 * {@link MaximaBatchProcessImpl} and {@link MaximaInteractiveProcessImpl} use this
 * to do their work.
 *
 * @author David McKain
 */
public final class MaximaProcessController {

    static final Logger logger = LoggerFactory.getLogger(MaximaProcessController.class);

    /** Size of buffer used to accumulate bytes to send to Maxima STDIN. */
    public static final int INPUT_BUFFER_SIZE = 1024;

    /** Size of buffer used to accumulate bytes to send to Maxima STDOUT. */
    public static final int OUTPUT_BUFFER_SIZE = 1024;

    /** Size of buffer used to accumulate bytes to send to Maxima STDERR. */
    public static final int STDERR_BUFFER_SIZE = 128;

    /**
     * Time to wait after asking Maxima process to terminate before forcibly killing it.
     * (This is needed if Maxima gets locked in a calculation that is either very
     * complex or will never actually finish.)
     */
    private static final int PROCESS_KILL_TIMEOUT = 1;

    /** {@link MaximaProcessLauncher} owning this */
    private final MaximaProcessLauncher launcher;

    /** Helper to manage asynchronous calls to Maxima process thread */
    private final ExecutorService executor;

    /** Maxima {@link Process} encapsulated by this */
    final Process maximaProcess;

    /** Maxima STDIN handle */
    final OutputStream maximaStdin;

    /** Maxima STDOUT handle */
    final InputStream maximaStdout;

    /** Maxima STDERR handle */
    final InputStream maximaStderr;

    /** Buffer for accumulating data to send to Maxima STDIN */
    final byte[] maximaStdinBuffer;

    /** Buffer for accumulating data from Maxima STDOUT */
    final byte[] maximaStdoutBuffer;

    /** Buffer for accumulating data from Maxima STDERR */
    final byte[] maximaStderrBuffer;

    /** Handles Maxima STDERR (may be null) */
    final OutputStream maximaStderrHandler;

    /** Set to true when a call is underway */
    private boolean callRunning;

    /** {@link Future} representing the result of the Thread feeding data to Maxima STDIN */
    private Future<Object> maximaCallInputFuture;

    /** {@link Future} representing the result of the Thread receiving data from Maxima STDOUT*/
    private Future<Object> maximaCallOutputFuture;

    /** Flag set when the underlying process has been terminated */
    private boolean terminated;

    public MaximaProcessController(final MaximaProcessLauncher launcher, final Process maximaProcess, final OutputStream maximaStderrHandler) {
        this.launcher = launcher;
        this.maximaProcess = maximaProcess;
        this.maximaStderrHandler = maximaStderrHandler;
        this.executor = Executors.newFixedThreadPool(3); /* (stdin, stdout, stderr, shutdown) */
        this.maximaStdinBuffer = new byte[INPUT_BUFFER_SIZE];
        this.maximaStdoutBuffer = new byte[OUTPUT_BUFFER_SIZE];
        this.maximaStderrBuffer = new byte[STDERR_BUFFER_SIZE];
        this.maximaStdout = maximaProcess.getInputStream();
        this.maximaStderr = maximaProcess.getErrorStream();
        this.maximaStdin = maximaProcess.getOutputStream();
        this.terminated = false;
        this.maximaCallInputFuture = null;
        this.maximaCallOutputFuture = null;
    }

    public MaximaProcessLauncher getOwner() {
        return launcher;
    }

    public boolean isTerminated() {
        return terminated;
    }

    /**
     * Terminates the underlying Maxima process, forcibly if required. No
     * more calls can be made to this process after this point.
     * <p>
     * Calling this on a process that has already terminated will do nothing.
     *
     * @return underlying exit value from the Maxima process, {@link MaximaInteractiveProcess#PROCESS_ALREADY_TERMINATED}
     *    if the process was already terminated, or {@link MaximaInteractiveProcess#PROCESS_FORCIBLY_DESTROYED}
     *    if the process had to be forcibly destroyed.
     */
    public int terminate() {
        if (terminated) {
            return MaximaInteractiveProcess.PROCESS_ALREADY_TERMINATED;
        }
        /* If there's a call already running, try to get it to cancel */
        cancelCurrentMaximaCall();

        /* Then terminate the Maxima process */
        return terminateMaximaProcess();
    }

    /* (Thread safe) */
    private int terminateMaximaProcess() {
        terminated = true;
        try {
            try {
                /* Ask Maxima to nicely close down by closing its input */
                logger.debug("Attempting to close Maxima nicely");
                synchronized (maximaStdin) {
                    maximaStdin.close();
                }
                final FutureTask<Integer> shutdownTask = new FutureTask<Integer>(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        return maximaProcess.waitFor();
                    }
                });
                executor.execute(shutdownTask);
                return shutdownTask.get(PROCESS_KILL_TIMEOUT, TimeUnit.SECONDS).intValue();
            }
            catch (final Exception e) {
                logger.debug("Maxima process did not terminate naturally, so forcibly terminating", e);
                maximaProcess.destroy();
                return MaximaInteractiveProcess.PROCESS_FORCIBLY_DESTROYED;
            }
        }
        finally {
            executor.shutdown();
            if (maximaStderrHandler!=null) {
                try {
                    maximaStderrHandler.close();
                }
                catch (final IOException e) {
                    throw new JacomaxRuntimeException("Could not close maximaStderrHandler", e);
                }
            }
        }
    }

    public void doMaximaCall(final InputStream callInputStream, final boolean closeOnInputEof,
            final MaximaOutputHandler maximaOutputHandler, final int callTimeout)
            throws MaximaTimeoutException {
        ensureNotTerminated();
        if (callRunning) {
            throw new JacomaxLogicException("Precondition failed - callRunning is currently true");
        }
        callRunning = true;
        final List<Callable<Object>> callables = new ArrayList<Callable<Object>>();
        callables.add(Executors.callable(new MaximaInputTask(callInputStream, closeOnInputEof)));
        callables.add(Executors.callable(new MaximaOutputTask(maximaOutputHandler)));
        try {
            List<Future<Object>> callResults;
            if (callTimeout > 0) {
                /* Wait until timeout */
                logger.trace("Invoking maxima call using timeout {}s", callTimeout);
                callResults = executor.invokeAll(callables, callTimeout, TimeUnit.SECONDS);
            }
            else {
                /* Wait indefinitely (this can be dangerous!) */
                logger.trace("Invoking maxima call without timeout");
                callResults = executor.invokeAll(callables);
            }
            maximaCallInputFuture = callResults.get(0);
            maximaCallOutputFuture = callResults.get(1);
            final boolean hadTimeout = callTimeout>0 && maximaCallInputFuture.isCancelled() || maximaCallOutputFuture.isCancelled();
            if (hadTimeout) {
                logger.debug("Timeout was exceeded communicating with Maxima - terminating the process");
                terminateMaximaProcess();
                throw new MaximaTimeoutException(callTimeout);
            }
            maximaCallInputFuture.get();
            maximaCallOutputFuture.get();
        }
        catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            JacomaxRuntimeException toThrow;
            if (cause instanceof JacomaxRuntimeException) {
                logger.debug("Caught a JacomaxRuntimeException from thread - terminating the process");
                toThrow = (JacomaxRuntimeException) cause;
            }
            else {
                logger.debug("Caught unexpected Exception from thread - terminating the process");
                toThrow = new JacomaxRuntimeException("Unexpected Exception", cause);
            }
            terminateMaximaProcess();
            throw toThrow;
        }
        catch (final InterruptedException e) {
            if (!terminated) {
                logger.debug("Maxima threads interrupted unexpectedly - terminating the process");
                terminateMaximaProcess();
                throw new JacomaxRuntimeException("Maxima thread interrupted unexpectedly");
            }
        }
        finally {
            maximaCallInputFuture = null;
            maximaCallOutputFuture = null;
            callRunning = false;
        }
    }

    private void cancelCurrentMaximaCall() {
        if (callRunning) {
            logger.trace("Instructing current Maxima call to cancel if possible");
            maximaCallInputFuture.cancel(true);
            maximaCallOutputFuture.cancel(true);
            maximaCallInputFuture = null;
            maximaCallOutputFuture = null;
            callRunning = false;
        }
    }

    /* (Thread-safe) */
    void checkMaximaStderr() throws IOException {
        synchronized (maximaStderr) {
            if (maximaStderr.available() > 0) {
                final int bytesReadFromStderr = maximaStderr.read(maximaStderrBuffer);
                if (logger.isTraceEnabled() && bytesReadFromStderr>0) {
                    logger.trace("MAXIMA!!!: {}", new String(maximaStderrBuffer, 0, bytesReadFromStderr, "US-ASCII"));
                }
                if (bytesReadFromStderr>0 && maximaStderrHandler!=null) {
                    maximaStderrHandler.write(maximaStderrBuffer, 0, bytesReadFromStderr);
                    maximaStderrHandler.flush();
                }
                else if (bytesReadFromStderr==-1) {
                    if (maximaStderrHandler!=null) {
                        maximaStderrHandler.close();
                    }
                }
            }
        }
    }

    private void ensureNotTerminated() {
        if (terminated) {
            throw new MaximaProcessTerminatedException();
        }
    }

    /**
     * Task sending call input to Maxima STDIN
     *
     * @author David McKain
     */
    private class MaximaInputTask implements Runnable {

        /** Stream providing call data to send to Maxima STDIN */
        private final InputStream callInputStream;

        /**
         * Flag to indicate whether Maxima STDIN should be closed, rather than just flushed,
         * when there is no more input to send to it.
         */
        private final boolean closeStdinOnEof;

        public MaximaInputTask(final InputStream callInputStream, final boolean closeStdinOnEof) {
            this.callInputStream = callInputStream;
            this.closeStdinOnEof = closeStdinOnEof;
        }

        public void run() {
            try {
                doMaximaWriteLoop();
            }
            catch (final IOException e) {
                throw new JacomaxRuntimeException("An IOException occurred sending input to Maxima", e);
            }
        }

        private void doMaximaWriteLoop() throws IOException {
            if (callInputStream==null) {
                logger.trace("Maxim STDIN loop exiting immediately as callInputStream is null");
                return;
            }
            boolean maximaStdinFinished = false;
            while (!maximaStdinFinished) {
                logger.trace("Maxima STDIN Loop: maximaStdinFinished={},inputAvailable={}",
                        maximaStdinFinished, callInputStream.available());
                checkMaximaStderr();
                logger.trace("Blocking on call input");
                final int bytesReadFromCallInput = callInputStream.read(maximaStdinBuffer);
                synchronized (maximaStdin) {
                    if (bytesReadFromCallInput==-1) {
                        /* Nothing more to send to Maxima */
                        logger.trace("Received EOF from inputStream. {}ing Maxima input and exiting write loop", closeStdinOnEof ? "Clos" : "Flush");
                        if (closeStdinOnEof) {
                            maximaStdin.close();
                        }
                        else {
                            maximaStdin.flush();
                        }
                        maximaStdinFinished = true;
                    }
                    else if (bytesReadFromCallInput>0 /*&& !isSignalledTerminating()*/) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Read {} byte(s) from callInputStream. Passing to Maxima input and flushing", bytesReadFromCallInput);
                            logger.trace("MAXIMA>>>: {}", new String(maximaStdinBuffer, 0, bytesReadFromCallInput, "US-ASCII"));
                        }

                        /* Send stuff to Maxima and try again */
                        maximaStdin.write(maximaStdinBuffer, 0, bytesReadFromCallInput);
                        maximaStdin.flush();
                    }
                    else {
                        throw new JacomaxLogicException("Read 0 input bytes from callInputStream after blocking - not expected");
                    }
                }
            }
            logger.trace("Maxim STDIN loop exiting");
        }
    }

    /**
     * Task that reads from Maxima STDOUT
     *
     * @author David McKain
     */
    private class MaximaOutputTask implements Runnable {

        /** Handler to cope with raw Maxima STDOUT */
        private final MaximaOutputHandler maximaOutputHandler;

        public MaximaOutputTask(final MaximaOutputHandler maximaOutputHandler) {
            this.maximaOutputHandler = maximaOutputHandler;
        }

        public void run() {
            try {
                try {
                    maximaOutputHandler.callStarting();
                    doMaximaReadLoop();
                }
                finally {
                    maximaOutputHandler.callFinished();
                }
            }
            catch (final IOException e) {
                throw new JacomaxRuntimeException("An IOException occurred reading from Maxima", e);
            }
        }

        private void doMaximaReadLoop() throws IOException {
            boolean maximaStdoutFinished = false;
            boolean outputHandlerSaysStop = false;
            while (!maximaStdoutFinished && !outputHandlerSaysStop /*&& !isSignalledTerminating()*/) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Maxima Read Loop: maximaStdoutFinished={},"
                                + "stdoutAvailable={},outputHandlerSaysStop={}",
                            maximaStdoutFinished,
                            maximaStdout.available(),
                            outputHandlerSaysStop
                            );
                }
                checkMaximaStderr();
                final int bytesReadFromMaxima = maximaStdout.read(maximaStdoutBuffer);
                if (bytesReadFromMaxima==-1) {
                    /* Maxima's STDOUT has finished */
                    logger.trace("Received EOF from Maxima STDOUT so stopping reading from it and informing output handler");
                    maximaOutputHandler.callFinished();
                    maximaStdoutFinished = true;
                }
                else if (bytesReadFromMaxima>0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Read {} byte(s) from Maxima. Sending to output handler and checking status", bytesReadFromMaxima);
                        logger.trace("MAXIMA<<<: {}", new String(maximaStdoutBuffer, 0, bytesReadFromMaxima, "US-ASCII"));
                    }
                    outputHandlerSaysStop = maximaOutputHandler.handleOutput(maximaStdoutBuffer, bytesReadFromMaxima, maximaStdoutFinished);
                }
                else if (bytesReadFromMaxima==0) {
                    /* Not expecting this! */
                    throw new JacomaxLogicException("Read 0 input bytes from Maxima STDOUT after blocking - not expected");
                }
            }
            logger.trace("Maxima STDOUT loop exiting");
        }
    }
}
