package org.eclipse.ant.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import junit.framework.TestCase;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.ant.tests.core.testplugin.AntFileRunner;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;


 
/**
 * Tests for Ant core
 */
public abstract class AbstractAntTest extends TestCase {
	
	/**
	 * Returns the 'AntTests' project.
	 * 
	 * @return the test project
	 */
	protected IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject("AntTests");
	}

	public static final int DEFAULT_TIMEOUT = 30000;
	
	
	public static IProject project;
	
	
	public AbstractAntTest(String name) {
		super(name);
	}
	
	protected IFile getBuildFile(String buildFileName) {
		IFile file = getProject().getFolder("scripts").getFile(buildFileName);
		assertTrue("Could not find script file named: " + buildFileName, file.exists());
		return file;
	}
	
	public void run(String buildFileName) throws CoreException {
		run(buildFileName, null);
	}
	
	public void run(String buildFileName, String[] args) throws CoreException {
		IFile buildFile= getBuildFile(buildFileName);
		AntFileRunner runner= new AntFileRunner();
		runner.run(buildFile, getTargetNames(buildFileName), args, "", true);
	}
	
	protected TargetInfo[] getTargets(String buildFileName) throws CoreException {
		IFile buildFile= getBuildFile(buildFileName);
		
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(buildFile.getLocation().toFile().getAbsolutePath());
	 	return runner.getAvailableTargets();
	}
	
	protected String[] getTargetNames(String buildFileName) throws CoreException {
		TargetInfo[] infos= getTargets(buildFileName);
		String[] names= new String[infos.length];
		for (int i = 0; i < infos.length; i++) {
			TargetInfo info = infos[i];
			names[i]= info.getName();
		}
		return names;
	}
	
	protected String[] getTargetDescriptions(String buildFileName) throws CoreException {
		TargetInfo[] infos= getTargets(buildFileName);
		String[] descriptions= new String[infos.length];
		for (int i = 0; i < infos.length; i++) {
			TargetInfo info = infos[i];
			descriptions[i]= info.getDescription();
		}
		return descriptions;
	}
}

