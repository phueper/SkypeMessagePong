/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.connector.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.skype.connector.ConnectorUtils;

public final class CSVRecorder extends Recorder {
    private PrintWriter writer;
    
    public CSVRecorder(String filePath) throws IOException {
        this(new File(filePath));
    }

    public CSVRecorder(File file) throws IOException {
        this(new FileWriter(file));
    }

    public CSVRecorder(Writer writer) {
        ConnectorUtils.checkNotNull("writer", writer);
        this.writer = new PrintWriter(new BufferedWriter(writer));
    }

    @Override
    protected void recordReceivedMessage(long time, String message) {
        write("received", time, message);
    }

    @Override
    protected void recordSentMessage(long time, String message) {
        write("sent", time, message);
    }

    private synchronized void write(String header, long time, String message) {
        writer.print(header);
        writer.print(',');
        writer.print(time);
        writer.print(',');
        writer.println(message);
    }
    
    public void close() {
        writer.close();
    }
}
