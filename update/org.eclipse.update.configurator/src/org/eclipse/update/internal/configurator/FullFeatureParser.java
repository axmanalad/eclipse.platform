/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;


import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * A more complete feature parser. It adds the plugins listed to the feature.
 */
public class FullFeatureParser extends DefaultHandler implements IConfigurationConstants{

	private SAXParser parser;
	private FeatureEntry feature;
	private URL url;
	private boolean isDescription;
	private StringBuffer description = new StringBuffer();

	private final static SAXParserFactory parserFactory =
		SAXParserFactory.newInstance();

	/**
	 * Constructs a feature parser.
	 */
	public FullFeatureParser(FeatureEntry feature) {
		super();
		this.feature = feature;
		try {
			parserFactory.setNamespaceAware(true);
			this.parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (SAXException e) {
			System.out.println(e);
		}
	}
	/**
	 */
	public void parse(){
		InputStream in = null;
		try {
			if (feature.getSite() == null)
				return;
			this.url = new URL(feature.getSite().getResolvedURL(), feature.getURL() + FEATURE_XML);
			in = url.openStream();
			parser.parse(new InputSource(in), this);
		} catch (SAXException e) {;
		} catch (IOException e) {
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {
					Utils.log(e1.getLocalizedMessage());
				}
		}
	}

	/**
	 * Handle start of element tags
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 * @since 2.0
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		Utils.debug("Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);
		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		if ("plugin".equals(localName)) {
			processPlugin(attributes);
		} else if ("description".equals(localName)){
			isDescription = true;
		}
		
	}

	/*
	 * Process feature information
	 */
	private void processPlugin(Attributes attributes) {

		// identifier and version
		String id = attributes.getValue("id"); //$NON-NLS-1$
		String ver = attributes.getValue("version"); //$NON-NLS-1$

		if (id == null || id.trim().equals("") //$NON-NLS-1$
		|| ver == null || ver.trim().equals("")) { //$NON-NLS-1$
			System.out.println(Messages.getString("FeatureParser.IdOrVersionInvalid", new String[] { id, ver}));
			//$NON-NLS-1$
		} else {
//			String label = attributes.getValue("label"); //$NON-NLS-1$
//			String provider = attributes.getValue("provider-name"); //$NON-NLS-1$
//			String nl = attributes.getValue("nl"); //$NON-NLS-1$
			String os = attributes.getValue("os"); //$NON-NLS-1$
			String ws = attributes.getValue("ws"); //$NON-NLS-1$
			String arch = attributes.getValue("arch"); //$NON-NLS-1$
			if (!Utils.isValidEnvironment(os, ws, arch))
				return;

			PluginEntry plugin = new PluginEntry();
			plugin.setPluginIdentifier(id);
			plugin.setPluginVersion(ver);
			feature.addPlugin(plugin);

			
			Utils.
				debug("End process DefaultFeature tag: id:" +id + " ver:" +ver + " url:" + feature.getURL()); 	
		}
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (!isDescription)
			return;
		description.append(ch, start, length);
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("description".equals(localName)) {
			isDescription = false;
			String d = description.toString().trim();
			ResourceBundle bundle = feature.getResourceBundle();
			feature.setDescription(Utils.getResourceString(bundle, d));
		}
	}
}