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

/**
 * Exception which is thrown when the connection the SKYPE Client isn't in place.
 * @author Koji Hisano
 */
public class NotAttachedException extends SkypeException {
    /**
	 * Serial UID needed for all serialisation objects. 
	 */
	private static final long serialVersionUID = -4191519326969230719L;

	/**
	 * Constructor.
	 *
	 */
	NotAttachedException() {
    }
}
