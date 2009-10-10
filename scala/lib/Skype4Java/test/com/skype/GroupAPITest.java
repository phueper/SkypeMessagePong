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
package com.skype;

import com.skype.Friend;
import com.skype.Group;
import com.skype.Skype;

import junit.framework.TestCase;

public class GroupAPITest extends TestCase {
    public void testGetGroup() throws Exception {
        TestUtils.showMessageDialog("Please create 'Test' group and add " + TestData.getFriendId() + " to it before closing this dialog.");
        Group group = Skype.getContactList().getGroup("Test");
        assertNotNull(group);
        assertTrue(group.hasFriend(TestData.getFriend()));
    }

    public void testAddAndRemoveGroup() throws Exception {
        String addedGroupName = "GroupAPITest";
        Group added = Skype.getContactList().addGroup(addedGroupName);
        assertNotNull(Skype.getContactList().getGroup(addedGroupName));
        Skype.getContactList().removeGroup(added);
        assertNull(Skype.getContactList().getGroup(addedGroupName));
    }

    public void testAddAndRemoveFriend() throws Exception {
        Group addedGroup = Skype.getContactList().addGroup("GroupAPITest");
        Friend addedFriend = TestData.getFriend();
        addedGroup.addFriend(addedFriend);
        assertTrue(addedGroup.hasFriend(addedFriend));
        addedGroup.removeFriend(addedFriend);
        assertFalse(addedGroup.hasFriend(addedFriend));
        Skype.getContactList().removeGroup(addedGroup);
    }
}
