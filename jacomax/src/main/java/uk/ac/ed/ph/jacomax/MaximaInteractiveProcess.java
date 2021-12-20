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

/**
 * Handle on an "interactive" Maxima process, as created using
 * {@link MaximaProcessLauncher#launchInteractiveProcess()}.
 * <p>
 * Use this if you want to send a series of commands (or calls) to Maxima
 * for it to evaluate and (possibly) return results.
 * <p>
 * Call {@link #terminate()} to terminate and clean up
 * the Maxima process once you have finished using it. Any further calls
 * made will result in a {@link MaximaProcessTerminatedException} being thrown.
 * <p>
 * Calls are executed with a timeout (specified in seconds), which can be passed
 * explicitly of defaulted in various ways. If the timeout is greater than zero and
 * the call has not completed in the allotted time, the underlying Maxima process is
 * terminated and a {@link MaximaTimeoutException} is thrown. Any further calls made
 * will generate a {@link MaximaProcessTerminatedException}. If the timeout is zero
 * or less, then calls are allowed to run indefinitely. (This should be used
 * with caution!)
 * <p>
 * An instance of this class should only be used by one thread at a time.
 *
 * @author David McKain
 */
public interface MaximaInteractiveProcess {

    int PROCESS_ALREADY_TERMINATED = -1;
    int PROCESS_FORCIBLY_DESTROYED = -2;

    /**
     * Returns the default call timeout for this process.
     */
    int getDefaultCallTimeout();

    /**
     * Sets the default call timeout for this process.
     * @param defaultCallTimeout
     */
    void setDefaultCallTimeout(int defaultCallTimeout);

    /**
     * Executes the given Maxima code, waiting for Maxima to finish evaluating
     * it and returning the raw output.
     * <p>
     * If the call times out, then {@link #terminate()} will be called and a
     * {@link MaximaTimeoutException} is thrown.
     *
     * @param maximaInput Maxima code to call. This should include any required
     *   terminator characters (e.g. <code>;</code> or <code>$</code>) as expected by Maxima.
     *   We also support calls containing <code>:lisp</code>, which are expected to end
     *   with a <code>)</code> character.
     *
     * @return raw Maxima output
     *
     * @throws IllegalArgumentException if maximaInput is null or does not appear to
     *   end with a terminator that can be safely handled.
     * @throws MaximaTimeoutException if the default timeout is positive and Maxima
     *   did not return a result within this time.
     * @throws MaximaProcessTerminatedException if the Maxima process has already
     *   been terminated, either by calling {@link #terminate()}, because a
     *   {@link MaximaTimeoutException} previously occurred or because a previous
     *   call failed to execute.
     */
    String executeCall(String maximaInput)
        throws MaximaTimeoutException;

    /**
     * Version of {@link #executeCall(String)} that uses the given timeout instead
     * of the current default.
     *
     * @param maximaInput
     * @param callTimeout
     *
     * @throws IllegalArgumentException
     * @throws MaximaTimeoutException
     * @throws MaximaProcessTerminatedException
     */
    String executeCall(String maximaInput, int callTimeout)
        throws MaximaTimeoutException;

    /**
     * Version of {@link #executeCall(String)} that throws away the output from Maxima.
     * (This is marginally more efficient than calling {@link #executeCall(String)} and
     * then throwing away the output yourself.)
     *
     * @param maximaInput
     *
     * @throws IllegalArgumentException
     * @throws MaximaTimeoutException
     * @throws MaximaProcessTerminatedException
     */
    void executeCallDiscardOutput(String maximaInput)
        throws MaximaTimeoutException;

    /**
     * Version of {@link #executeCallDiscardOutput(String)} that takes a custom timeout.
     *
     * @param maximaInput
     * @param callTimeout
     *
     * @throws IllegalArgumentException
     * @throws MaximaTimeoutException
     * @throws MaximaProcessTerminatedException
     */
    void executeCallDiscardOutput(String maximaInput, int callTimeout)
        throws MaximaTimeoutException;

    /**
     * Performs a "soft reset" of the process by calling
     * <code>[kill(all),reset()];</code>, which has the effect of clearing up
     * <em>most</em> things that you might have done. Consult the Maxima documentation
     * for more details.
     *
     * @throws MaximaTimeoutException
     * @throws MaximaProcessTerminatedException
     */
    void softReset()
        throws MaximaTimeoutException;

    /**
     * Returns whether or not this process has been terminated due to a call to
     * {@link #terminate()}, or because of a timeout, or due to a previous call
     * failing.
     *
     * @return true if the process has been terminated, false otherwise.
     */
    boolean isTerminated();

    /**
     * Terminates the underlying Maxima process, forcibly if required. No
     * more calls can be made to this process after this point.
     * <p>
     * Calling this on a process that has already terminated will do nothing.
     *
     * @return underlying exit value from the Maxima process, {@link #PROCESS_ALREADY_TERMINATED}
     *    if the process was already terminated, or {@link #PROCESS_FORCIBLY_DESTROYED}
     *    if the process had to be forcibly destroyed.
     */
    int terminate();

}
