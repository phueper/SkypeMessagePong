/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements Skype CHATMESSAGE object.
 * @see https://developer.skype.com/Docs/ApiDoc/CHATMESSAGE_object
 * Protocol 3. Supersedes the MESSAGE object.
 * @author Koji Hisano
 */
public final class ChatMessage extends SkypeObject {
    /**
     * Collection of ChatMessage objects.
     */
    private static final Map<String, ChatMessage> chatMessages = new HashMap<String, ChatMessage>();
    
    /**
     * Returns the ChatMessage object by the specified id.
     * @param id whose associated ChatMessage object is to be returned.
     * @return ChatMessage object with ID == id.
     */
    static ChatMessage getInstance(final String id) {
        synchronized(chatMessages) {
            if (!chatMessages.containsKey(id)) {
                chatMessages.put(id, new ChatMessage(id));
            }
            return chatMessages.get(id);
        }
    }

    /**
     * Enumeration for type.
     */
	public enum Type {
		/**
		 * SETTOPIC - change of chat topic.
		 * SAID - IM.
		 * ADDEDMEMBERS - invited someone to chat.
		 * SAWMEMBERS - chat participant has seen other members.
		 * CREATEDCHATWITH - chat to multiple people is created.
		 * LEFT - someone left chat; can also be a notification if somebody cannot be added to chat.
		 * UNKNOWN - other.
		 */
        SETTOPIC, SAID, ADDEDMEMBERS, SAWMEMBERS, CREATEDCHATWITH, LEFT, UNKNOWN;
    }

	/**
	 * Enumeration for STATUS of CHATMESSAGE.
	 */
    public enum Status {
    	/**
    	 * SENDING - message is being sent.
    	 * SENT - message was sent.
    	 * RECEIVED - message has been received.
    	 * READ - message has been read.
    	 */
        SENDING, SENT, RECEIVED, READ;
    }

    /**
     * Enumeration for LeaveReason.
     */
    public enum LeaveReason {
    	/**
    	 * USER_NOT_FOUND - user was not found.
    	 * USER_INCAPABLE - user has an older Skype version and cannot join multichat.
    	 * ADDER_MUST_BE_FRIEND - recipient accepts messages from contacts only and sender is not in his/her contact list.
    	 * ADDED_MUST_BE_AUTHORIZED - recipient accepts messages from authorized users only and sender is not authorized.
    	 * UNSUBSCRIBE - participant left chat.
    	 */
        USER_NOT_FOUND, USER_INCAPABLE, ADDER_MUST_BE_FRIEND, ADDED_MUST_BE_AUTHORIZED, UNSUBSCRIBE;
    }

    /**
     * ID of this CHATMESSAGE.
     */
    private final String id;

    /**
     * Constructor.
     * @param newId The ID of this CHATMESSAGE.
     */
    private ChatMessage(String newId) {
        this.id = newId;
    }

    
    /**
     * Returns the hashcode for this object.
     * In this case it's ID.
     * @return ID.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Compare two object to check equalness.
     * @param compared the object to check against.
     * @return true if object ID's are equal.
     */
    public boolean equals(Object compared) {
        if (compared instanceof ChatMessage) {
            ChatMessage comparedChatMessage = (ChatMessage)compared;
            return getId().equals(comparedChatMessage.getId());
        }
        return false;
    }

    /**
     * Return CHATMESSAGE ID.
     * @return ID of this chatmessage.
     */
    public String getId() {
        return id;
    }

    /**
     * Return time when message was sent (UNIX timestamp).
     * @return Date of this chatmessage.
     * @throws SkypeException when connection has gone bad.
     */
    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    /**
     * Return the User who sended this CHATMESSAGE.
     * @return User object of sender.
     * @throws SkypeException when connection has gone bad.
     */
    public User getSender() throws SkypeException {
        return User.getInstance(getSenderId());
    }

    /**
     * Return the handle of the user who has sent this CHATMESSAGE.
     * @return a String with the handle.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getSenderId() throws SkypeException {
        return getProperty("FROM_HANDLE");
    }

    /**
     * Return the displayname of the sender of this CHATMESSAGE.
     * @return a String with the displayname of the sender.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getSenderDisplayName() throws SkypeException {
        return getProperty("FROM_DISPNAME");
    }

    /**
     * Get the type of this CHATMESSAGE.
     * @see Type
     * @return Type of this chatmessage.
     * @throws SkypeException when the connection has gone bad.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    /**
     * Get the status of this CHATMESSAGE.
     * @see Status
     * @return Status of this chatmessage.
     * @throws SkypeException when the connection has gone bad.
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getPropertyWithCommandId("CHATMESSAGE", getId(), "STATUS"));
    }

    /**
     * Get the leave reason.
     * @see LeaveReason
     * @return get the leave reason.
     * @throws SkypeException when the connection has gone bad.
     */
    public LeaveReason getLeaveReason() throws SkypeException {
        return LeaveReason.valueOf(getProperty("LEAVEREASON"));
    }

    /**
     * Get the content of this CHATMESSAGE.
     * @return the content of this chatmessage.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getContent() throws SkypeException {
        return getProperty("BODY");
    }

    /**
     * Get the parent CHAT object for this CHATMESSAGE.
     * @see Chat
     * @return parent CHAT object.
     * @throws SkypeException when the connection has gone bad.
     */
    public Chat getChat() throws SkypeException {
        return Chat.getInstance(getProperty("CHATNAME"));
    }

    /**
     * Return all users added to CHAT.
     * @return Array of users.
     * @throws SkypeException when connection has gone bad.
     */
    public User[] getAllUsers() throws SkypeException {
        String value = getProperty("USERS");
        if ("".equals(value)) {
            return new User[0];
        }
        String[] ids = value.split(" ");
        User[] users = new User[ids.length];
        for (int i = 0; i < ids.length; i++) {
            users[i] = User.getInstance(ids[i]);
        }
        return users;
    }

    /**
     * Get CHATMESSAGE property.
     * @param name of the property.
     * @return value of the property.
     * @throws SkypeException when connection has gone bad or property not found.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("CHATMESSAGE", getId(), name);
    }

    // TODO void setSeen()
    // TODO boolean isSeen()
}
