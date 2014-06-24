/*
 * CriticImageProvider.java
 * Sep 15, 2011
 *
 * CriticAL: A Critic for APIs and Libraries
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti & Daqing Hou, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti <rupakhcr@clarkson.edu> 
 * Daqing Hou <dhou@clarkson.edu>
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * USA
 * http://critical.sf.net
 */
 
package edu.clarkson.serl.critic.reporter;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import edu.clarkson.serl.critic.CriticPlugin;

/**
 * 
 * @author <a href="http://clarkson.edu/~rupakhcr">Chandan R. Rupakheti</a> (rupakhcr@clarkson.edu)
 */
public class CriticImageProvider implements IAnnotationImageProvider {
	public static final String ICON_CRITICISM = "/icons/Criticism.gif"; 
	public static final String ICON_EXPLANATION = "/icons/Explanation.gif"; 
	public static final String ICON_RECOMMENDATION = "/icons/Recommendation.gif"; 

	private static Image criticismImage;
	private static Image explanationImage;
	private static Image recommendationImage;
	

	/**
	 * Creates an image provider for annotation.
	 * 
	 */
	public CriticImageProvider() {
	}

	public Image getManagedImage(Annotation annotation) {
		String type = annotation.getType();
		Image image = null;
		if(type.equals(CriticMarkerFactory.ANNOTATION_CRITICISM)) {
			if(criticismImage == null) {
				ImageDescriptor desc = this.getImageDescriptor(ICON_CRITICISM);
				if(desc != null)
					criticismImage = desc.createImage();
			}
			image = criticismImage;
		}
		if(type.equals(CriticMarkerFactory.ANNOTATION_EXPLANATION)) {
			if(explanationImage == null) {
				ImageDescriptor desc = this.getImageDescriptor(ICON_EXPLANATION);
				if(desc != null)
					explanationImage = desc.createImage();
			}
			image = explanationImage;
		}
		if(type.equals(CriticMarkerFactory.ANNOTATION_RECOMMENDATION)) {
			if(recommendationImage == null) {
				ImageDescriptor desc = this.getImageDescriptor(ICON_RECOMMENDATION);
				if(desc != null)
					recommendationImage = desc.createImage();
			}
			image = recommendationImage;
		}
		return image;
	}
	
	public String getImageDescriptorId(Annotation annotation) {
		String type = annotation.getType();
		if(type.equals(CriticMarkerFactory.ANNOTATION_CRITICISM))
			return ICON_CRITICISM;
		if(type.equals(CriticMarkerFactory.ANNOTATION_EXPLANATION))
			return ICON_EXPLANATION;
		if(type.equals(CriticMarkerFactory.ANNOTATION_RECOMMENDATION))
			return ICON_RECOMMENDATION;
		return null;
	}

	public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
		return CriticPlugin.getImageDescriptor(imageDescriptorId);
	}
}
