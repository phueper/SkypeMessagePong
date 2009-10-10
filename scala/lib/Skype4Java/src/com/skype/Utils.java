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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * Util class used by this API to store common used helper methods.
 */
final class Utils {
	
	/**
	 * Convert a connector exception to a SkypeException.
	 * @param e the connectorException to convert.
	 * @throws SkypeException the converted connectorException.
	 */
    static void convertToSkypeException(ConnectorException e) throws SkypeException {
        SkypeException r;
        if (e instanceof com.skype.connector.NotAttachedException) {
            r = new NotAttachedException();
        } else if (e instanceof com.skype.connector.TimeOutException) {
            r = new TimeOutException(e.getMessage());
        } else {
            r = new SkypeException(e.getMessage());
        }
        r.initCause(e);
        throw r;
    }

    /**
     * Check the reply string if it contains ERROR string, if so throw a exception.
     * @param response the reply string to check.
     * @throws SkypeException when the response contains ERROR.
     */
    static void checkError(String response) throws SkypeException {
        if (response == null) {
            return;
        }
        if (response.startsWith("ERROR ")) {
            throw new CommandFailedException(response);
        }
    }

    /**
     * Get a Skype object property based on a type, an id and a name.
     * @param type the Skype object type.
     * @param id the Skype object id.
     * @param name the property name.
     * @return the property value.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static String getPropertyWithCommandId(String type, String id, String name) throws SkypeException {
        try {
            String command = "GET " + type + " " + id + " " + name;
            String responseHeader = type + " " + id + " " + name + " ";
            String response = Connector.getInstance().executeWithId(command, responseHeader);
            checkError(response);
            return response.substring((responseHeader).length());
        } catch (ConnectorException e) {
            convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Get a Skype object property based on a type, an id and a name.
     * @param type the Skype object type.
     * @param id the Skype object id.
     * @param name the property name.
     * @return the property value.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static String getProperty(String type, String id, String name) throws SkypeException {
        try {
            String command = "GET " + type + " " + id + " " + name;
            String responseHeader = type + " " + id + " " + name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
            return response.substring((responseHeader).length());
        } catch (ConnectorException e) {
            convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Get a Skype object property based on a type and a name.
     * @param type the Skype object type.
     * @param name the property name.
     * @return the property value.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static String getProperty(String type, String name) throws SkypeException {
        try {
            String command = "GET " + type + " " + name;
            String responseHeader = type + " " + name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            return response.substring((responseHeader).length());
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Get a Skype object property based on a name.
     * @param name the property name.
     * @return the property value.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static String getProperty(String name) throws SkypeException {
        try {
            String command = "GET " + name + " ";
            String responseHeader = name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
            return response.substring(responseHeader.length());
        } catch (ConnectorException e) {
            convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Set the value of a property of a Skype object.
     * @param type the Skype object.
     * @param id the id of the Skype object.
     * @param name name of the property.
     * @param value value to set to property to.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static void setProperty(String type, String id, String name, String value) throws SkypeException {
        try {
            String command = "SET " + type + " " + id + " " + name + " " + value;
            String responseHeader = type + " " + id + " " + name + " " + value;
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    /**
     * Set the value of a property of a Skype object.
     * @param type the type of Skype object.
     * @param name name of the property.
     * @param value value to set to property to.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static void setProperty(String type, String name, String value) throws SkypeException {
        try {
            String command = "SET " + type + " " + name + " " + value;
            String responseHeader = type + " " + name + " " + value;
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    /**
     * Set the value of a property of a Skype object.
     * @param name name of the property.
     * @param value value to set to property to.
     * @throws SkypeException when connection to Skype client has gone bad or reply contains ERROR.
     */
    static void setProperty(String name, String value) throws SkypeException {
        try {
            String command = "SET " + name + " " + value;
            String responseHeader = name + " " + value;
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    /**
     * Send a SKYPE message and check the reply for ERROR.
     * @param command the command to send to the Skype client.
     * @throws SkypeException  when connection to Skype client has gone bad or reply contains ERROR.
     */
    static void executeWithErrorCheck(String command) throws SkypeException {
        try {
            String response = Connector.getInstance().execute(command);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    /**
     * Check if a object isn't null, if it is Throw an NULLPOINTER exception.
     * @param name name of the objectm used in the exception message.
     * @param value the object to check.
     */
    static void checkNotNull(String name, Object value) {
        if (value == null) {
            throw new NullPointerException("The " + name + " must not be null.");
        }
    }

    /**
     * Convert a comma seperated string to an array.
     * @param listString The string to convert.
     * @return Array.
     */
    static String[] convertToArray(String listString) {
        if ("".equals(listString)) {
            return new String[0];
        }
        return listString.split(", ");
    }

    /**
     * Convert an array to a comma seperated string.
     * @param array Array to convert.
     * @return comma seperated string.
     */
    static String convertToCommaSeparatedString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(array[i]);
        }
        return builder.toString();
    }

    /**
     * Parse UNIX timestring to a Data object.
     * @param time The timestring to parse.
     * @return Date object with the unix time.
     */
    static Date parseUnixTime(String time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(Long.parseLong(time) * 1000);
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar.getTime();
    }

    /**
     * Uncaught exception handler.
     * @param e The uncaught exception.
     * @param exceptionHandler the handler to set as uncaught exception handler.
     */
    static void handleUncaughtException(Throwable e, SkypeExceptionHandler exceptionHandler) {
        if (exceptionHandler != null) {
            exceptionHandler.uncaughtExceptionHappened(e);
            return;
        }
        Skype.handleUncaughtException(e);
    }

    /**
     * Private constructor.
     * Methods should be used staticly.
     */
    private Utils() {
    }
}
