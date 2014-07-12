package edu.clarkson.serl.critic.reporter;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class FileDecorator extends LabelProvider implements ILightweightLabelDecorator {
	
	public static final String ICON = "/icons/CriticALSmall.gif";
//	private static Font font = new Font(null, "Arial", 10, 0);
//	private static Color color = new Color(null, 0, 0, 255);
	
	public void decorate(Object resource, IDecoration decoration) {
		IResource res = (IResource)resource;
		
		if(res == null || res.getProject() == null)
			return;
		
		if(!res.getProject().isOpen())
			return;
		
		int markers = CriticMarkerFactory.findMarkers(res).size();
		if (markers > 0) {
			decoration.addOverlay(ImageDescriptor.createFromFile(FileDecorator.class, ICON), IDecoration.TOP_RIGHT);
//			decoration.addPrefix("<T> ");
//			decoration.addSuffix(" " + markers + " marker(s)");
//			decoration.setFont(font);
//			decoration.setForegroundColor(color);
		}
	}
}
