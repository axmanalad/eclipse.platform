package org.eclipse.ant.tests.core.testloggers;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.PrintStream;

import org.apache.tools.ant.*;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.ant.tests.core.testplugin.AntLoggerChecker;
import org.eclipse.core.runtime.OperationCanceledException;

public class TestBuildLogger implements BuildLogger {

	private int messageOutputLevel =Project.MSG_INFO;
	private PrintStream fErr= null;
	private PrintStream fOut= null;
	
	/**
	 * An exception that has already been logged.
	 */
	private Throwable fHandledException= null;
	
	
	public TestBuildLogger() {
	}
	
	/**
	 * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
	 */
	public void setMessageOutputLevel(int level) {
		messageOutputLevel= level;
	}
	
	protected int getMessageOutputLevel() {
		return messageOutputLevel;
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setEmacsMode(boolean)
	 */
	public void setEmacsMode(boolean emacsMode) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		AntLoggerChecker.reset();
//		AntLoggerChecker.getDefault().buildStarted(event);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		handleException(event);
		fHandledException= null;
//		AntLoggerChecker.getDefault().buildFinished(event);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
//		AntLoggerChecker.getDefault().targetStarted(event);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		handleException(event);
//		AntLoggerChecker.getDefault().targetFinished(event);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
//		AntLoggerChecker.getDefault().taskStarted(event);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		handleException(event);
//		AntLoggerChecker.getDefault().targetFinished(event);
	}

	/**
	 * @see BuildListener#messageLogged(BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		logMessage(event.getMessage(), event.getPriority());
		AntLoggerChecker.getDefault().messageLogged(event.getMessage());
	}

	protected PrintStream getErrorPrintStream() {
		return fErr;
	}
	
	protected PrintStream getOutputPrintStream() {
		return fOut;
	}
	
	/**
	 * @see org.apache.tools.ant.BuildLogger#setErrorPrintStream(java.io.PrintStream)
	 */
	public void setErrorPrintStream(PrintStream err) {
		//this build logger logs to "null" unless
		//the user has explicitly set a logfile to use
		if (err == System.err) {
			fErr= null;
		} else {
			fErr= err;
		}
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setOutputPrintStream(java.io.PrintStream)
	 */
	public void setOutputPrintStream(PrintStream output) {
		//this build logger logs to "null" unless
		//the user has explicitly set a logfile to use
		if (output == System.out) {
			fOut= null;
		} else {
			fOut= output;
		}
	}
	
	protected void logMessage(String message, int priority) {
		if (priority > getMessageOutputLevel()) {
			return;
		}
		
		if (priority == Project.MSG_ERR) {
			if (getErrorPrintStream() != null && getErrorPrintStream() != System.err) {
				//user has designated to log to a logfile
				getErrorPrintStream().println(message);
			}
		} else {
			if (getOutputPrintStream() != null && getOutputPrintStream() != System.out) {
				//user has designated to log to a logfile
				getOutputPrintStream().println(message);
			} 
		}
	}
	
	protected void handleException(BuildEvent event) {
		Throwable exception = event.getException();
		if (exception == null || exception == fHandledException
		|| exception instanceof OperationCanceledException
		|| exception instanceof AntSecurityException) {
			return;
		}
		fHandledException= exception;
	}
}
