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

import uk.ac.ed.ph.jacomax.JacomaxRuntimeException;
import uk.ac.ed.ph.jacomax.MaximaTimeoutException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal implementation of batch process functionality.
 *
 * @author David McKain
 */
public final class MaximaBatchProcessImpl {

    private static final Logger logger = LoggerFactory.getLogger(MaximaBatchProcessImpl.class);

    private final MaximaProcessController maximaProcessController;
    private final InputStream batchInputStream;
    private final OutputStream batchOutputStream;

    public MaximaBatchProcessImpl(final MaximaProcessController maximaProcessController,
            final InputStream batchInputStream, final OutputStream batchOutputStream) {
        this.maximaProcessController = maximaProcessController;
        this.batchInputStream = batchInputStream;
        this.batchOutputStream = batchOutputStream;
    }

    public int run(final int timeout) throws MaximaTimeoutException {
        logger.debug("Running Maxima process in batch mode");
        final BatchOutputHandler writerOutputHandler = new BatchOutputHandler(batchOutputStream);
        int returnCode;
        try {
            maximaProcessController.doMaximaCall(batchInputStream, true, writerOutputHandler, timeout);
        }
        finally {
            try {
                returnCode = maximaProcessController.terminate();
            }
            finally {
                try {
                    batchOutputStream.flush();
                }
                catch (final IOException e) {
                    throw new JacomaxRuntimeException("Could not flush batchOutputStream", e);
                }
            }
        }
        return returnCode;
    }
}
