package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IValue;

/**
 * Listener interface for notification of value
 * details.
 * 
 * @see IDebugModelPresentation
 */

public interface IValueDetailListener {
	/**
	 * Notifies this listener that the details for the given
	 * value have been computed as the specified result.
	 *  
	 * @param value the value for which the detail is provided
	 * @param result the detailed description of the given value
	 */
	public void detailComputed(IValue value, String result);
}