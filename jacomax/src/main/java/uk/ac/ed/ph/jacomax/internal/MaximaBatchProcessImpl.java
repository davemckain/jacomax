/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
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
 * @author  David McKain
 * @version $Revision$
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
        logger.info("Running Maxima process in batch mode");
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
                    batchOutputStream.close();
                }
                catch (final IOException e) {
                    throw new JacomaxRuntimeException("Could not close batchOutputStream", e);
                }
            }
        }
        return returnCode;
    }
}
