/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.internal.*;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A <code>CompareConfiguration</code> object
 * controls various UI aspects of compare/merge viewers like
 * title labels and images, or whether a side of a merge viewer is editable.
 * In addition to these fixed properties <code>ICompareConfiguration</code> provides
 * API for an open ended set of properties. Different viewers which share the same
 * configuration can communicate via this mechanism. E.g. if a compare editor
 * has a button for controlling whether compare viewers ignore white space,
 * the button would trigger a change of the boolean <code>IGNORE_WHITESPACE</code> property
 * and all interested viewers would receive notification.
 * <p>
 * Suitable default labels are provided (without images); both the left and right sides
 * are editable.
 * </p>
 * <p>
 * Clients may use this class as is, or subclass to add new state and behavior.
 * </p>
 */
public class CompareConfiguration {

	/**
	 * Name of the ignore whitespace property (value <code>"IGNORE_WHITESPACE"</code>).
	 */
	public static final String IGNORE_WHITESPACE= "IGNORE_WHITESPACE"; //$NON-NLS-1$
	/**
	 * Name of the show pseudo conflicts property (value <code>"SHOW_PSEUDO_CONFLICTS"</code>).
	 */
	public static final String SHOW_PSEUDO_CONFLICTS= "SHOW_PSEUDO_CONFLICTS"; //$NON-NLS-1$
	/**
	 * Name of the use outline view property (value <code>"USE_OUTLINE_VIEW"</code>).
	 * @since 3.0
	 */
	public static final String USE_OUTLINE_VIEW= "USE_OUTLINE_VIEW"; //$NON-NLS-1$
	
	private static ImageDescriptor[] fgImages= new ImageDescriptor[16];
	private static Object fgDummy= new Object();
	private static HashMap fgMap= new HashMap(20);
	private static boolean fLeftIsLocal= true;

	static {
		if (fLeftIsLocal) {
			fgImages[Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.LEFT + Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/r_inadd_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.RIGHT + Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/r_outadd_ov.gif"); //$NON-NLS-1$

			fgImages[Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.LEFT + Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/r_indel_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.RIGHT + Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/r_outdel_ov.gif"); //$NON-NLS-1$

			fgImages[Differencer.LEFT + Differencer.CHANGE]= CompareUIPlugin.getImageDescriptor("ovr16/r_inchg_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.RIGHT + Differencer.CHANGE]= CompareUIPlugin.getImageDescriptor("ovr16/r_outchg_ov.gif"); //$NON-NLS-1$
		} else {
			fgImages[Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.LEFT + Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/inadd_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.RIGHT + Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/outadd_ov.gif"); //$NON-NLS-1$

			fgImages[Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.LEFT + Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/indel_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.RIGHT + Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/outdel_ov.gif"); //$NON-NLS-1$

			fgImages[Differencer.LEFT + Differencer.CHANGE]= CompareUIPlugin.getImageDescriptor("ovr16/inchg_ov.gif"); //$NON-NLS-1$
			fgImages[Differencer.RIGHT + Differencer.CHANGE]= CompareUIPlugin.getImageDescriptor("ovr16/outchg_ov.gif"); //$NON-NLS-1$
		}

		fgImages[Differencer.CONFLICTING + Differencer.ADDITION]= CompareUIPlugin.getImageDescriptor("ovr16/confadd_ov.gif"); //$NON-NLS-1$
		fgImages[Differencer.CONFLICTING + Differencer.DELETION]= CompareUIPlugin.getImageDescriptor("ovr16/confdel_ov.gif"); //$NON-NLS-1$
		fgImages[Differencer.CONFLICTING + Differencer.CHANGE]= CompareUIPlugin.getImageDescriptor("ovr16/confchg_ov.gif"); //$NON-NLS-1$
	}

	private IPreferenceStore fPreferenceStore;
	private ListenerList fListeners= new ListenerList();
	private HashMap fProperties= new HashMap();
	private boolean fLeftEditable= true;
	private boolean fRightEditable= true;
	private String fAncestorLabel;
	private String fLeftLabel;
	private String fRightLabel;
	private Image fAncestorImage;
	private Image fRightImage;
	private Image fLeftImage;
	private Image[] fImages= new Image[16];
	private boolean fCalculateDiffs = true;
	private ICompareContainer fContainer;
	private DefaultLabelProvider labelProvider = new DefaultLabelProvider();
	
	private class DefaultLabelProvider extends LabelProvider implements ICompareInputLabelProvider {
		private Map labelProviders = new HashMap();
		public Image getAncestorImage(Object input) {
			ICompareInputLabelProvider provider = getLabelProvider(input);
			if (provider != null) {
				Image image = provider.getAncestorImage(input);
				if (image != null)
					return image;
			}
			return fAncestorImage;
		}
		public String getAncestorLabel(Object input) {
			ICompareInputLabelProvider provider = getLabelProvider(input);
			if (provider != null) {
				String label = provider.getAncestorLabel(input);
				if (label != null)
					return label;
			}
			return fAncestorLabel;
		}
		public Image getLeftImage(Object input) {
			ICompareInputLabelProvider provider = getLabelProvider(input);
			if (provider != null) {
				Image image = provider.getLeftImage(input);
				if (image != null)
					return image;
			}
			return fLeftImage;
		}
		public String getLeftLabel(Object input) {
			ICompareInputLabelProvider provider = getLabelProvider(input);
			if (provider != null) {
				String label = provider.getLeftLabel(input);
				if (label != null)
					return label;
			}
			return fLeftLabel;
		}
		public Image getRightImage(Object input) {
			ICompareInputLabelProvider provider = getLabelProvider(input);
			if (provider != null) {
				Image image = provider.getRightImage(input);
				if (image != null)
					return image;
			}
			return fRightImage;
		}
		public String getRightLabel(Object input) {
			ICompareInputLabelProvider provider = getLabelProvider(input);
			if (provider != null) {
				String label = provider.getRightLabel(input);
				if (label != null)
					return label;
			}
			return fRightLabel;
		}
		public ICompareInputLabelProvider getLabelProvider(Object input) {
			return (ICompareInputLabelProvider)labelProviders.get(input);
		}
		public void setLabelProvider(ICompareInput input, ICompareInputLabelProvider labelProvider) {
			labelProviders.put(input, labelProvider);
		}
		public Image getImage(Object element) {
			ICompareInputLabelProvider provider = getLabelProvider(element);
			if (provider != null) {
				Image image = provider.getImage(element);
				if (image != null)
					return image;
			}
			if (element instanceof ICompareInput) {
				ICompareInput ci = (ICompareInput) element;
				Image image = ci.getImage();
				if (image != null)
					return image;
			}
			return super.getImage(element);
		}
		public String getText(Object element) {
			ICompareInputLabelProvider provider = getLabelProvider(element);
			if (provider != null) {
				String label = provider.getText(element);
				if (label != null)
					return label;
			}
			if (element instanceof ICompareInput) {
				ICompareInput ci = (ICompareInput) element;
				String label = ci.getName();
				if (label != null)
					return label;
			}
			return super.getText(element);
		}
	}

	/**
	 * Creates a new configuration with editable left and right sides,
	 * suitable default labels, and no images.
	 * The given preference store is used to connect this configuration
	 * with the Compare preference page properties <code>ComparePreferencePage.INITIALLY_SHOW_ANCESTOR_PANE</code>,
	 * and <code>CompareConfiguration.IGNORE_WHITESPACE</code>.
	 * 
	 * @param prefStore the preference store which this configuration holds onto.
	 * @since 2.0
	 */
	public CompareConfiguration(IPreferenceStore prefStore) {
		
		setProperty("LEFT_IS_LOCAL", new Boolean(fLeftIsLocal)); //$NON-NLS-1$
		
		fPreferenceStore= prefStore;
		if (fPreferenceStore != null) {
			boolean b= fPreferenceStore.getBoolean(ComparePreferencePage.INITIALLY_SHOW_ANCESTOR_PANE);
			setProperty(ICompareUIConstants.PROP_ANCESTOR_VISIBLE, new Boolean(b));
			
			b= fPreferenceStore.getBoolean(ComparePreferencePage.IGNORE_WHITESPACE);
			setProperty(CompareConfiguration.IGNORE_WHITESPACE, new Boolean(b));
		}
	}	
	
	/**
	 * Creates a new configuration with editable left and right sides,
	 * suitable default labels, and no images.
	 * This configuration uses the preference store of the Compare plug-in
	 * (<code>CompareUIPlugin.getDefault().getPreferenceStore()</code>).
	 */
	public CompareConfiguration() {
		this(CompareUIPlugin.getDefault().getPreferenceStore());
	}
	
	/**
	 * Returns the preference store of this configuration.
	 * @return the preference store of this configuration.
	 * @since 2.0
	 */
	public IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}
	
	/**
	 * Returns an image showing the specified change kind.
	 * The different kind of changes are defined in the <code>Differencer</code>.
	 * Newly created images are remembered by this class and
	 * disposed when the <code>dispose</code> method is called.
	 *
	 * @param kind the kind of change as defined in <code>Differencer</code>.
	 * @return an modification of the base image reflecting the kind of change.
	 * @see org.eclipse.compare.structuremergeviewer.Differencer
	 * @since 2.0
	 */
	public Image getImage(int kind) {
		Image image= fImages[kind & 15];
		if (image == null) {
			ImageDescriptor id= fgImages[kind & 15];
			if (id != null)				
				image= id.createImage();
			fImages[kind & 15]= image;
		}
		return image;
	}
	
	/**
	 * Returns an image showing the specified change kind applied to a
	 * given base image. The different kind of changes are defined in the <code>Differencer</code>.
	 * Typically an implementation would build a composite image 
	 * from the given base image and an image representing the change kind.
	 * Newly created images are remembered by this class and
	 * disposed when the <code>dispose</code> method is called.
	 *
	 * @param base the image which is modified to reflect the kind of change
	 * @param kind the kind of change as defined in <code>Differencer</code>.
	 * @return an modification of the base image reflecting the kind of change.
	 * @see org.eclipse.compare.structuremergeviewer.Differencer
	 */
	public Image getImage(Image base, int kind) {

		Object key= base;
		if (key == null)
			key= fgDummy;

		kind &= 15;

		Image[] a= (Image[]) fgMap.get(key);
		if (a == null) {
			a= new Image[16];
			fgMap.put(key, a);
		}
		Image b= a[kind];
		if (b == null) {
			b= new DiffImage(base, fgImages[kind], ICompareUIConstants.COMPARE_IMAGE_WIDTH, !fLeftIsLocal).createImage();
			CompareUI.disposeOnShutdown(b);
			a[kind]= b;
		}
		return b;
	}
	
	/**
	 * Dispose of this compare configuration.
	 * This method is called if the compare configuration is no longer used.
	 * An implementation must dispose of all resources.
	 */
	public void dispose() {
		if (fImages != null) {
			for (int i= 0; i < fImages.length; i++){
				Image image= fImages[i];
				if (image != null && !image.isDisposed())
					image.dispose();
			}
		}
		fImages= null;
		labelProvider.dispose();
	}

	/**
	 * Fires a <code>PropertyChangeEvent</code> to registered listeners.
	 *
	 * @param propertyName the name of the property that has changed
	 * @param oldValue the property's old value
	 * @param newValue the property's new value
	 */
	private void fireChange(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeEvent event= null;
		Object[] listeners= fListeners.getListeners();
		if (listeners != null) {
			for (int i= 0; i < listeners.length; i++) {
				IPropertyChangeListener l= (IPropertyChangeListener) listeners[i];
				if (event == null)
					event= new PropertyChangeEvent(this, propertyName, oldValue, newValue);
				l.propertyChange(event);
			}
		}
	}

	/* (non javadoc)
	 * see IPropertyChangeNotifier.addListener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}

	/* (non javadoc)
	 * see IPropertyChangeNotifier.removeListener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Sets the property with the given name.
	 * If the new value differs from the old a <code>PropertyChangeEvent</code>
	 * is sent to registered listeners.
	 *
	 * @param key the name of the property to set
	 * @param newValue the new value of the property
	 */
	public void setProperty(String key, Object newValue) {
		Object oldValue= fProperties.get(key);
		fProperties.put(key, newValue);
		if (oldValue == null || !oldValue.equals(newValue))
			fireChange(key, oldValue, newValue);
	}

	/**
	 * Returns the property with the given name, or <code>null</code>
	 * if no such property exists.
	 *
	 * @param key the name of the property to retrieve
	 * @return the property with the given name, or <code>null</code> if not found
	 */
	public Object getProperty(String key) {
		return fProperties.get(key);
	}

	//---- ancestor
	
	/**
	 * Sets the label to use for the ancestor of compare/merge viewers.
	 * This label will be used if the element for which a label
	 * is requested does not have an ancestor or the element does not have
	 * a registered label provider or the label provider returns <code>null</code>
	 * as the label.
	 *
	 * @param label the new label for the ancestor of compare/merge viewers
	 */
	public void setAncestorLabel(String label) {
		fAncestorLabel= label;
	}

	/**
	 * Returns the label for the ancestor side of compare/merge viewers.
	 * This label is typically shown in the title of the ancestor area in a compare viewer.
	 *
	 * @param element the input object of a compare/merge viewer or <code>null</code>
	 * @return the label for the ancestor side or <code>null</code>
	 */
	public String getAncestorLabel(Object element) {
		return labelProvider.getAncestorLabel(element);
	}

	/**
	 * Sets the image to use for the ancestor of compare/merge viewers.
	 * The CompareConfiguration does not automatically dispose the old image.
	 * This image will be used if the element for which a image
	 * is requested does not have an ancestor or the element does not have
	 * a registered label provider or the label provider returns <code>null</code>
	 * as the image.
	 *
	 * @param image the new image for the ancestor of compare/merge viewers
	 */
	public void setAncestorImage(Image image) {
		fAncestorImage= image;
	}

	/**
	 * Returns the image for the ancestor side of compare/merge viewers.
	 * This image is typically shown in the title of the ancestor area in a compare viewer.
	 *
	 * @param element the input object of a compare/merge viewer or <code>null</code>
	 * @return the image for the ancestor side or <code>null</code>
	 */	
	public Image getAncestorImage(Object element) {
		return labelProvider.getAncestorImage(element);
	}

	//---- left side
	
	/**
	 * Controls whether the left side of a merge viewer is editable.
	 *
	 * @param editable if the value is <code>true</code> left side is editable
	 */
	public void setLeftEditable(boolean editable) {
		fLeftEditable= editable;
	}
	
	/**
	 * Returns whether the left hand side of a merge viewer is editable.
	 * 
	 * @return <code>true</code> if the left hand side is editable
	 */
	public boolean isLeftEditable() {
		return fLeftEditable;
	}

	/**
	 * Sets the label to use for the left side of compare/merge viewers.
	 * This label will be used if the element for which a label
	 * is requested does not have a left contributor or the element does not have
	 * a registered label provider or the label provider returns <code>null</code>
	 * as the label.
	 *
	 * @param label the new label for the left side of compare/merge viewers
	 */
	public void setLeftLabel(String label) {
		fLeftLabel= label;
	}
	
	/**
	 * Returns the label for the left hand side of compare/merge viewers.
	 * This label is typically shown in the title of the left side of a compare viewer.
	 *
	 * @param element the input object of a compare/merge viewer or <code>null</code>
	 * @return the label for the left hand side or <code>null</code>
	 */
	public String getLeftLabel(Object element) {
		return labelProvider.getLeftLabel(element);
	}

	/**
	 * Sets the image to use for the left side of compare/merge viewers.
	 * The compare configuration does not automatically dispose the old image.
	 * This image will be used if the element for which a image
	 * is requested does not have an left contributor or the element does not have
	 * a registered label provider or the label provider returns <code>null</code>
	 * as the image.
	 *
	 * @param image the new image for the left side of compare/merge viewers
	 */
	public void setLeftImage(Image image) {
		fLeftImage= image;
	}

	/**
	 * Returns the image for the left hand side of compare/merge viewers.
	 * This image is typically shown in the title of the left side of a compare viewer.
	 *
	 * @param element the input object of a compare/merge viewer or <code>null</code>
	 * @return the image for the left hand side or <code>null</code>
	 */	
	public Image getLeftImage(Object element) {
		return labelProvider.getLeftImage(element);
	}
	
	//---- right side

	/**
	 * Controls whether the right side of a merge viewer is editable.
	 *
	 * @param editable if the value is <code>true</code> right side is editable
	 */
	public void setRightEditable(boolean editable) {
		fRightEditable= editable;
	}
	
	/**
	 * Returns whether the right hand side of a merge viewer is editable.
	 * 
	 * @return <code>true</code> if the right hand side is editable
	 */
	public boolean isRightEditable() {
		return fRightEditable;
	}

	/**
	 * Sets the label to use for the right side of compare/merge viewers.
	 * This label will be used if the element for which a label
	 * is requested does not have an right contributor or the element does not have
	 * a registered label provider or the label provider returns <code>null</code>
	 * as the label.
	 *
	 * @param label the new label for the right side of compare/merge viewers
	 */
	public void setRightLabel(String label) {
		fRightLabel= label;
	}

	/**
	 * Returns the label for the right hand side of compare/merge viewers.
	 * This label is typically shown in the title of the right side of a compare viewer.
	 *
	 * @param element the input object of a compare/merge viewer or <code>null</code>
	 * @return the label for the right hand side or <code>null</code>
	 */
	public String getRightLabel(Object element) {
		return labelProvider.getRightLabel(element);
	}

	/**
	 * Sets the image to use for the right side of compare/merge viewers.
	 * The compare configuration does not automatically dispose the old image.
	 * This image will be used if the element for which a image
	 * is requested does not have an right contributor or the element does not have
	 * a registered label provider or the label provider returns <code>null</code>
	 * as the image.
	 *
	 * @param image the new image for the right side of compare/merge viewers
	 */
	public void setRightImage(Image image) {
		fRightImage= image;
	}

	/**
	 * Returns the image for the right hand side of compare/merge viewers.
	 * This image is typically shown in the title of the right side of a compare viewer.
	 *
	 * @param element the input object of a compare/merge viewer or <code>null</code>
	 * @return the image for the right hand side or <code>null</code>
	 */
	public Image getRightImage(Object element) {
		return labelProvider.getRightImage(element);
	}

	/**
	 * Returns whether a compare/merge viewer should calculate diffs
	 * @return <code>true</code> if diffs are to be calculate, <code>false</code> otherwise
	 * @since 3.3 
	 */
	public boolean getCalculateDiffs() {
		return fCalculateDiffs;
	}

	/**
	 * Used to specify if the compare/merge viewers should calculate the diffs between the
	 * left and right sides. By default, it is initialized to <code>true</code>.
	 * @param calculateDiffs
	 * @since 3.3
	 */
	public void setCalculateDiffs(boolean calculateDiffs) {
		fCalculateDiffs = calculateDiffs;
	}

	/**
	 * Return the container of the compare associated with this configuration.
	 * @return the container of the compare associated with this configuration
	 */
	public ICompareContainer getContainer() {
		if (fContainer == null) {
			// Create a default container in case one is not provided
			fContainer= new CompareContainer();
		}
		return fContainer;
	}

	/**
	 * Set the container of the compare associated with this configuration.
	 * @param container the container of the compare associated with this configuration.
	 */
	public void setContainer(ICompareContainer container) {
		fContainer = container;
	}
	
	/**
	 * Return the label provider for the given compare input or
	 * <code>null</code> if there is no label provider. If there is
	 * a label provider for an element, it is consulted to determine
	 * the ancestor left and right labels and images.
	 * 
	 * @param input the compare input
	 * @return the label provider for the given compare input
	 */
	public ICompareInputLabelProvider getLabelProvider(Object input) {
		return labelProvider.getLabelProvider(input);
	}
	
	/**
	 * Set the label provider for the given compare input. The compare configuration
	 * will not dispose of the label provider when the configuration is disposed.
	 * It is up to the provider of the label provider to ensure that it is 
	 * disposed when it is no longer needed.
	 * @param input the compare input
	 * @param labelProvider the label provider for the compare input
	 */
	public void setLabelProvider(ICompareInput input, ICompareInputLabelProvider labelProvider) {
		this.labelProvider.setLabelProvider(input, labelProvider);
	}
	
}

