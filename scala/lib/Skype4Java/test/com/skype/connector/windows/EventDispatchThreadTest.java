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
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.connector.windows;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.skype.Skype;
import com.skype.SkypeException;

public final class EventDispatchThreadTest extends TestCase {
    public void testEventDispatchThreadInMainMethod() throws Exception {
        final Display display = Display.getDefault();
        final Shell shell = new Shell(display);
        shell.setText("Event Dispatch Thread In Main Method Test");
        shell.setLayout(new FillLayout());
        Button button = new Button(shell, SWT.NONE);
        final String[] version = new String[1];
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    version[0] = Skype.getVersion();
                } catch (SkypeException e) {
                }
                shell.dispose();
            }
        });
        button.setText("Please, click here.");
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
        assertNotNull(version[0]);
    }
}
