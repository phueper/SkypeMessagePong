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

import java.util.Date;

import junit.framework.TestCase;

public class CallAPITest extends TestCase {
    public void testCallAndFinish() throws Exception {
        TestUtils.showMessageDialog("Please, check " + TestData.getFriendId() + " will receive a call and it will be finished after two seconds?");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(2000);
        call.finish();
        TestUtils.showCheckDialog(TestData.getFriendId() + " has received a call and it was finished after two seconds?");
    }

    public void testFinishEndedCall() throws Exception {
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        call.finish();
        Thread.sleep(1000);
        try {
            call.finish();
        } catch (CommandFailedException e) {
            assertEquals(24, e.getCode());
            assertEquals("Cannot hangup inactive call", e.getMessage());
        }
    }

    public void testHoldAndResume() throws Exception {
        TestUtils.showMessageDialog("Please, start a talking with " + TestData.getFriendId() + " in ten seconds after starting a call.");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(10000);
        TestUtils.showMessageDialog("Please, check 2 seconds suspending, 2 seconds talking and finishing.");
        call.hold();
        Thread.sleep(2000);
        call.resume();
        Thread.sleep(2000);
        call.finish();
        TestUtils.showCheckDialog("You have gotton a 2 seconds suspending, and 2 seconds talking?");
    }

    public void testCallProperty() throws Exception {
        Date startTime = new Date();
        TestUtils.showMessageDialog("Please, start a talking with " + TestData.getFriendId() + " and finish in ten seconds after starting a call.");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(10000);
        Date endTime = new Date();
        assertTrue(call.getStartTime().getTime() - startTime.getTime() <= endTime.getTime() - startTime.getTime());
        assertTrue(call.getDuration() <= endTime.getTime() - startTime.getTime());
        assertEquals(TestData.getFriendId(), call.getPartnerId());
        assertEquals(TestData.getFriendDisplayName(), call.getPartnerDisplayName());
    }

    public void testCallReceived() throws Exception {
        final Object wait = new Object();
        final Call[] result = new Call[1];
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callReceived(Call call) throws SkypeException {
                synchronized(wait) {
                    wait.notify();
                }
                result[0] = call;
            }
        });
        TestUtils.showMessageDialog("Please, request " + TestData.getFriendId() + " to call me, receive the call, and finish.");
        synchronized(wait) {
            wait.wait();
        }
        assertEquals(TestData.getFriendId(), result[0].getPartnerId());
    }
}
