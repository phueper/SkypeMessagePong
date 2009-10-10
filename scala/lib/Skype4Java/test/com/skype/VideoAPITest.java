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
package com.skype;

import junit.framework.TestCase;

public class VideoAPITest extends TestCase {
    public void testGetVideoDevice() throws Exception {
        String name = Skype.getVideoDevice();
        if (name == null) {
            name = "Default video device";
        }
        TestUtils.showCheckDialog("Webcam is '" + name + "'?");
    }

    public void testOpenVideoTestWindow() throws Exception {
        SkypeClient.openVideoTestWindow();
        TestUtils.showCheckDialog("Webcam test window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testOpenVideoOptionsWindow() throws Exception {
        SkypeClient.openVideoOptionsWindow();
        TestUtils.showCheckDialog("Options window with selectiong 'Video' page is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }
}
