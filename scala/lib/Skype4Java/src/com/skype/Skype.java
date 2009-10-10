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
 * Bart Lamot - good ideas for API and initial javadoc
 ******************************************************************************/
package com.skype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

/**
 * Main model (not view) class of the Skype Java API.
 * Use this class staticly to do model actions (send messages, SMS messages or calls, etc).
 * @see SkypeClient
 * @author Koji Hisano
 */
public final class Skype {
    /** library version. **/
    public static final String LIBRARY_VERSION = "1.0.0.0";

    /** contactList instance. */
    private static ContactList contactList;
    
    /** Profile instance for this Skype session. */
    private static Profile profile;

    /** chatMessageListener lock. */
    private static Object chatMessageListenerMutex = new Object();
    /** CHATMESSAGE listener. */
    private static ConnectorListener chatMessageListener;
    /** Collection of listeners. */
    private static List<ChatMessageListener> chatMessageListeners = Collections.synchronizedList(new ArrayList<ChatMessageListener>());

    /** callListener lock object. */
    private static Object callListenerMutex = new Object();
    /** CALL listener. */
    private static ConnectorListener callListener;
    /** Collection of all CALL listeners. */
    private static List<CallListener> callListeners = Collections.synchronizedList(new ArrayList<CallListener>());

    /** voiceMailListener lock object. */
    private static Object voiceMailListenerMutex = new Object();
    /** VOICEMAIL listener. */
    private static ConnectorListener voiceMailListener;
    /** Collection of all VOICEMAIL listeners. */
    private static List<VoiceMailListener> voiceMailListeners = Collections.synchronizedList(new ArrayList<VoiceMailListener>());

    /** User thread. */
    private static Thread userThread;
    /** User threading lock object. */
    private static Object userThreadFieldMutex = new Object();

    /** General exception handler. */
    private static SkypeExceptionHandler defaultExceptionHandler = new SkypeExceptionHandler() {
        /** Print the non caught exceptions. */
    	public void uncaughtExceptionHappened(Throwable e) {
            e.printStackTrace();
        }
    };
    /** refrence to the default exception handler. */
    private static SkypeExceptionHandler exceptionHandler = defaultExceptionHandler;

    /**
     * Make the main thread of this API a deamon thread.
     * @see Thread
     * @param on if true the main thread will be a deamon thread.
     */
    public static void setDeamon(boolean on) {
        synchronized (userThreadFieldMutex) {
            if (!on && userThread == null) {
                userThread = new Thread("SkypeUserThread") {
                    public void run() {
                        Object wait = new Object();
                        synchronized (wait) {
                            try {
                                wait.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                };
                userThread.start();
            } else if (on && userThread != null) {
                userThread.interrupt();
                userThread = null;
            }
        }
    }

    /**
     * Enable debug logging.
     * @param on if true debug logging will be sent to the console.
     * @throws SkypeException when the connection has gone bad.
     */
    public static void setDebug(boolean on) throws SkypeException {
        try {
            Connector.getInstance().setDebug(on);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Return the version of the Skype client (not this API).
     * @return String with version.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static String getVersion() throws SkypeException {
        return Utils.getProperty("SKYPEVERSION");
    }

    /**
     * Check if Skype client is installed on this computer.
     * WARNING, does not work for all platforms yet.
     * @return true if Skype client is installed.
     */
    public static boolean isInstalled() {
        return getInstalledPath() != null;
    }

    /**
     * Find the install path of the Skype client.
     * WARNING, does not work for all platforms yet.
     * @return String with the full path to Skype client.
     */
    public static String getInstalledPath() {
        return Connector.getInstance().getInstalledPath();
    }

    /**
     * Check if Skype client is running.
     * WARNING, does not work for all platforms.
     * @return true if Skype client is running.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static boolean isRunning() throws SkypeException {
        try {
            return Connector.getInstance().isRunning();
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    /**
     * Get the contactlist instance of this Skype session.
     * @return contactlist singleton.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static ContactList getContactList() throws SkypeException {
        if (contactList == null) {
            contactList = new ContactList();
        }
        return contactList;
    }

    /**
     * Make a Skype CALL to multiple users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to call.
     * @return The started Call.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Call call(String... skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return call(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Make a Skype CALL to one single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to call.
     * @return The new call object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Call call(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeId);
        try {
            String responseHeader = "CALL ";
            String response = Connector.getInstance().executeWithId("CALL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return Call.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Start a chat with multiple Skype users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to start a chat with.
     * @return The new chat object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Chat chat(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return chat(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Start a chat with a single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to start the with.
     * @return The new chat.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Chat chat(String skypeId) throws SkypeException {
        try {
            String responseHeader = "CHAT ";
            String response = Connector.getInstance().executeWithId("CHAT CREATE " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return Chat.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param numbers the cell phone numbers to validate.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String[] numbers) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        return submitConfirmationCode(Utils.convertToCommaSeparatedString(numbers));
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param number the cell phone numbers to validate.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String number) throws SkypeException {
        SMS message = createSMS(number, SMS.Type.CONFIRMATION_CODE_REQUEST);
        message.send();
        return message;
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param numbers the cell phone numbers to validate.
     * @param code the validation code to send.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String[] numbers, String code) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        Utils.checkNotNull("code", code);
        return submitConfirmationCode(Utils.convertToCommaSeparatedString(numbers), code);
    }
    
    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param number the cell phone numbers to validate.
     * @param code the validation code to send.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String number, String code) throws SkypeException {
        Utils.checkNotNull("number", number);
        Utils.checkNotNull("code", code);
        SMS message = createSMS(number, SMS.Type.CONFIRMATION_CODE_REQUEST);
        message.setContent(code);
        message.send();
        return message;
    }

    /**
     * Send an SMS to one or more cell phone numbers.
     * @param numbers the cell phone numbers to send to.
     * @param content the message to send.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS sendSMS(String[] numbers, String content) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        return sendSMS(Utils.convertToCommaSeparatedString(numbers), content);
    }

    /**
     * Send an SMS to one cell phone number.
     * @param number the cell phone numbers to send to.
     * @param content the message to send.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */    
    public static SMS sendSMS(String number, String content) throws SkypeException {
        Utils.checkNotNull("number", number);
        Utils.checkNotNull("content", content);
        SMS message = createSMS(number, SMS.Type.OUTGOING);
        message.setContent(content);
        message.send();
        return message;
    }

    /**
     * Create a new SMS object to send later using SMS.send .
     * @param number Cell phone number to send it to.
     * @param type The type of SMS message.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    private static SMS createSMS(String number, SMS.Type type) throws SkypeException {
        try {
            String responseHeader = "SMS ";
            String response = Connector.getInstance().executeWithId("CREATE SMS " + type + " " + number, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return SMS.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Find all SMS messages.
     * @return Array of SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public SMS[] getAllSMSs() throws SkypeException {
        return getAllSMSs("SMSS");
    }

    /**
     * Find all missed SMS messages.
     * @return Array of SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public SMS[] getAllMissedSMSs() throws SkypeException {
        return getAllSMSs("MISSEDSMSS");
    }

    /**
     * Find all SMS message of a certain type.
     * @param type The type to search for.
     * @return Array of found SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    private SMS[] getAllSMSs(String type) throws SkypeException {
        try {
            String command = "SEARCH " + type;
            String responseHeader = "SMSS ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            SMS[] smss = new SMS[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                smss[i] = SMS.getInstance(ids[i]);
            }
            return smss;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Leave a voicemail in a other Skype users voicemailbox.
     * @param skypeId The Skype user to leave a voicemail.
     * @return The new Voicemail object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static VoiceMail voiceMail(String skypeId) throws SkypeException {
        try {
            String responseHeader = "VOICEMAIL ";
            String response = Connector.getInstance().executeWithId("VOICEMAIL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(' ', responseHeader.length()));
            return VoiceMail.getInstance(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Gets the all voice mails.
     * @return The all voice mails
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static VoiceMail[] getAllVoiceMails() throws SkypeException {
        try {
            String command = "SEARCH VOICEMAILS";
            String responseHeader = "VOICEMAILS ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            VoiceMail[] voiceMails = new VoiceMail[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                voiceMails[i] = VoiceMail.getInstance(ids[i]);
            }
            return voiceMails;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Add an AP2AP capable application.
     * @param name The name of the AP2AP application.
     * @return Application object reference.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Application addApplication(String name) throws SkypeException {
        return Application.getInstance(name);
    }

    /**
     * Gets the current audio input device of this Skype.
     * 
     * @return the audio input device name of this Skype, or <code>null</code>
     *         if the device is the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #setAudioInputDevice(String)
     */
    public static String getAudioInputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_IN"));
    }

    /**
     * Gets the current audio output device of this Skype.
     * 
     * @return the audio output device name of this Skype, or <code>null</code>
     *         if the device is the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #setAudioOutputDevice(String)
     */
    public static String getAudioOutputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_OUT"));
    }

    /**
     * Get the current video input device used by the Skype Client.
     * @return String with the device name or null if there isn't one.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static String getVideoDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("VIDEO_IN"));
    }

    /**
     * Return null if the default device is used.
     * @param deviceName Name of the device to check.
     * @return <code>null</code> if device is default else devicename.
     */
    private static String convertDefaultDeviceToNull(String deviceName) {
        if (isDefaultDevice(deviceName)) {
            return null;
        } else {
            return deviceName;
        }
    }

    /**
     * Compare the devicename to the default value.
     * @param deviceName the string to compare.
     * @return true if devicename is equal to defaultname.
     */
    private static boolean isDefaultDevice(String deviceName) {
        return "".equals(deviceName);
    }

    /**
     * Sets the current audio input device of this Skype.
     * 
     * @param deviceName
     *            the audio input device name. A <code>null</code> value means
     *            the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #getAudioInputDevice()
     */
    public static void setAudioInputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Sets the current audio output device of this Skype.
     * 
     * @param deviceName
     *            the audio output device name. A <code>null</code> value
     *            means the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #getAudioOutputDevice()
     */
    public static void setAudioOutputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_OUT", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Set the video device used by the Skype client.
     * @param deviceName name of the device to set.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void setVideoDevice(String deviceName) throws SkypeException {
        Utils.setProperty("VIDEO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Convert a <code>null</code> to the default device.
     * @param deviceName String to convert.
     * @return Default device string.
     */
    private static String convertNullToDefaultDevice(String deviceName) {
        if (deviceName == null) {
            return "";
        } else {
            return deviceName;
        }
    }

    /**
     * Get the singleton instance of the users profile.
     * @return Profile.
     */
    public static synchronized Profile getProfile() {
        if (profile == null) {
            profile = new Profile();
        }
        return profile;
    }
    
    /**
     * Gets the all chats.
     *
     * @return The all chats
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllChats() throws SkypeException {
        return getAllChats("CHATS");
    }

    /**
     * Gets the all chats which are open in the windows.
     *
     * @return The all chats which are open in the windows
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllActiveChats() throws SkypeException {
        return getAllChats("ACTIVECHATS");
    }

    /**
     * Gets the all chats which include unread messages
     *
     * @return The all chats which include unread messages
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllMissedChats() throws SkypeException {
        return getAllChats("MISSEDCHATS");
    }

    /**
     * Gets the all recent chats in the locally-cached history.
     *
     * @return The all recent chats in the locally-cached history
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllRecentChats() throws SkypeException {
        return getAllChats("RECENTCHATS");
    }

    /**
     * Gets the all bookmarked chats.
     *
     * @return The all bookmarked chats
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getAllBookmarkedChats() throws SkypeException {
        return getAllChats("BOOKMARKEDCHATS");
    }

    /**
     * Gets the all chats by the type.
     *
     * @return The all chats by the type
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    private static Chat[] getAllChats(String type) throws SkypeException {
        try {
            String command = "SEARCH " + type;
            String responseHeader = "CHATS ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Chat[] chats = new Chat[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chats[i] = Chat.getInstance(ids[i]);
            }
            return chats;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }
    
    /**
     * Return User based on ID.
     * @param id ID of the User.
     * @return The user found.
     */
    public static User getUser(String id) {
        return User.getInstance(id);
    }

    /**
     * Add a listener for CHATMESSAGE events received from the Skype API.
     * @see ChatMessageListener
     * @param listener the Listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void addChatMessageListener(ChatMessageListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.add(listener);
            if (chatMessageListener == null) {
                chatMessageListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("CHATMESSAGE ")) {
                            String data = message.substring("CHATMESSAGE ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("STATUS".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                ChatMessageListener[] listeners = chatMessageListeners.toArray(new ChatMessageListener[0]);
                                ChatMessage chatMessage = ChatMessage.getInstance(id);
                                if ("SENT".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                        try {
                                            listener.chatMessageSent(chatMessage);
                                        } catch (Throwable e) {
                                            handleUncaughtException(e);
                                        }
                                    }
                                } else if ("RECEIVED".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                        try {
                                            listener.chatMessageReceived(chatMessage);
                                        } catch (Throwable e) {
                                            handleUncaughtException(e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(chatMessageListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    /**
     * Remove a listener for CHATMESSAGE events.
     * If the listener is already removed nothing happens.
     * @param listener The listener to remove.
     */
    public static void removeChatMessageListener(ChatMessageListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.remove(listener);
            if (chatMessageListeners.isEmpty()) {
                Connector.getInstance().removeConnectorListener(chatMessageListener);
                chatMessageListener = null;
            }
        }
    }

    /**
     * Add a listener for CALL events received from the Skype API.
     * @see CallListener
     * @param listener the listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void addCallListener(CallListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
            callListeners.add(listener);
            if (callListener == null) {
                callListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("CALL ")) {
                            String data = message.substring("CALL ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("STATUS".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                Call.Status status = Call.Status.valueOf(propertyValue);
                                Call call = Call.getInstance(id);
                                EXIT: if (status == Call.Status.RINGING) {
                                    synchronized(call) {
                                        if (call.isCallListenerEventFired()) {
                                            break EXIT;
                                        }
                                        call.setCallListenerEventFired(true);
                                        CallListener[] listeners = callListeners.toArray(new CallListener[0]);
                                        try {
                                            switch (call.getType()) {
                                                case OUTGOING_P2P:
                                                case OUTGOING_PSTN:
                                                    for (CallListener listener : listeners) {
                                                        try {
                                                            listener.callMaked(call);
                                                        } catch (Throwable e) {
                                                            handleUncaughtException(e);
                                                        }
                                                    }
                                                    break;
                                                case INCOMING_P2P:
                                                case INCOMING_PSTN:
                                                    for (CallListener listener : listeners) {
                                                        try {
                                                            listener.callReceived(call);
                                                        } catch (Throwable e) {
                                                            handleUncaughtException(e);
                                                        }
                                                    }
                                                    break;
                                                default: 
                                                	//Should an exception be thrown?
                                                	break;
                                            }
                                        } catch (Throwable e) {
                                            handleUncaughtException(e);
                                        }
                                    }
                                }
                                call.fireStatusChanged(status);
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(callListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    /**
     * Remove a listener for CALL events.
     * If listener is already removed nothing happens.
     * @param listener The listener to add.
     */
    public static void removeCallListener(CallListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
            callListeners.remove(listener);
            if (callListeners.isEmpty()) {
                Connector.getInstance().removeConnectorListener(callListener);
                callListener = null;
            }
        }
    }

    /**
     * Adds a listener for voice mail events received from the Skype API.
     * @param listener the added listener
     * @throws SkypeException if connection is bad or error is returned
     * @see VoicemaListener
     */
    public static void addVoiceMailListener(VoiceMailListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (voiceMailListenerMutex) {
            voiceMailListeners.add(listener);
            if (voiceMailListener == null) {
                voiceMailListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("VOICEMAIL ")) {
                            String data = message.substring("VOICEMAIL ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("TYPE".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                VoiceMail.Type type = VoiceMail.Type.valueOf(propertyValue);
                                VoiceMail voiceMail = VoiceMail.getInstance(id);
                                VoiceMailListener[] listeners = voiceMailListeners.toArray(new VoiceMailListener[0]);
                                switch (type) {
                                    case OUTGOING:
                                        for (VoiceMailListener listener : listeners) {
                                            try {
                                                listener.voiceMailMade(voiceMail);
                                            } catch (Throwable e) {
                                                handleUncaughtException(e);
                                            }
                                        }
                                        break;
                                    case INCOMING:
                                        for (VoiceMailListener listener : listeners) {
                                            try {
                                                listener.voiceMailReceived(voiceMail);
                                            } catch (Throwable e) {
                                                handleUncaughtException(e);
                                            }
                                        }
                                        break;
                                    case DEFAULT_GREETING:
                                    case CUSTOM_GREETING:
                                    case UNKNOWN:
                                    default:
                                        // do nothing
                                        break;
                                }
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(voiceMailListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    /**
     * Remove a listener for VOICEMAIL events.
     * If listener is already removed nothing happens.
     * @param listener The listener to add.
     */
    public static void removeVoiceMailListener(VoiceMailListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (voiceMailListenerMutex) {
            voiceMailListeners.remove(listener);
            if (voiceMailListeners.isEmpty()) {
                Connector.getInstance().removeConnectorListener(voiceMailListener);
                voiceMailListener = null;
            }
        }
    }

    /**
     * Use another exceptionhandler then the default one.
     * @see SkypeExceptionHandler
     * @param handler the handler to use.
     */
    public static void setSkypeExceptionHandler(SkypeExceptionHandler handler) {
        if (handler == null) {
            handler = defaultExceptionHandler;
        }
        exceptionHandler = handler;
    }

    /**
     * Handle uncaught exceptions in a default way.
     * @param e the uncaught exception.
     */
    static void handleUncaughtException(Throwable e) {
        exceptionHandler.uncaughtExceptionHappened(e);
    }

    /** 
     * Private constructor.
     * Please use this object staticly.
     */
    private Skype() {
    }
}
