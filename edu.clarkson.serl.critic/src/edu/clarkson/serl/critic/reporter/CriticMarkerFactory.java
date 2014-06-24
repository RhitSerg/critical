package edu.clarkson.serl.critic.reporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import edu.clarkson.serl.critic.CriticPlugin;
import edu.clarkson.serl.critic.extension.ICritic;


public class CriticMarkerFactory {
	//Marker ID
	public static final String MARKER = "edu.clarkson.serl.critic.marker";
	public static final String MARKER_CRITICISM = "edu.clarkson.serl.critic.marker.criticism";
	public static final String MARKER_EXPLANATION = "edu.clarkson.serl.critic.marker.explanation";
	public static final String MARKER_RECOMMENDATION = "edu.clarkson.serl.critic.marker.recommendation";
	
	public static final String ANNOTATION_CRITICISM = "edu.clarkson.serl.critic.annotation.criticism";
	public static final String ANNOTATION_EXPLANATION = "edu.clarkson.serl.critic.annotation.explanation";	
	public static final String ANNOTATION_RECOMMENDATION = "edu.clarkson.serl.critic.annotation.recommendation";	
	
	/*
	 * Creates a Marker
	 */
	public static IMarker createMarker(IResource res, ICritic critics)
    throws CoreException {
            IMarker marker = null;
            String type = MARKER_CRITICISM;
            
            if(critics.getType() == ICritic.Type.Explanation)
            	type = MARKER_EXPLANATION;
            else if(critics.getType() == ICritic.Type.Recommendation)
            	type = MARKER_RECOMMENDATION;

            List<IMarker> markers = findMarkers(res, type);
            for(IMarker m : markers) {
            	int line = m.getAttribute(IMarker.LINE_NUMBER, -1); 
            	if(line != critics.getLineNumber())
            		break;
            	
            	int priority = m.getAttribute(IMarker.PRIORITY, -1);
            	if(priority != critics.getPriority().ordinal())
            		break;

            	String desc = m.getAttribute(IMarker.MESSAGE, "");
            	if(!desc.equals(critics.getDescription()))
            		break;
            	
            	// Its a repetition
            	return null;
            }
            
            marker = res.createMarker(type);
            marker.setAttribute(IMarker.LINE_NUMBER, critics.getLineNumber());
            marker.setAttribute(IMarker.MESSAGE, critics.getDescription());
            marker.setAttribute(IMarker.PRIORITY, critics.getPriority().ordinal());
            return marker;
    }
	
	/**
	 * Returns a list of markers of supplied type found in the resource.
	 * 
	 * @param resource The resource file.
	 * @param type The type of Marker. 
	 * @return Returns a list of markers of supplied type.
	 */
	public static List<IMarker> findMarkers(IResource resource, String type) {
		ArrayList<IMarker> list = new ArrayList<IMarker>();
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(type, true, IResource.DEPTH_ZERO)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     return list;
	}	

	/**
	 * Returns a list of all types (explanations, criticisms, and recommendations) for the supplied resource.
	 * @param resource The resource file.
	 * @return A list of {@link IMarker}.
	 */
	public static List<IMarker> findMarkers(IResource resource) {
		ArrayList<IMarker> list = new ArrayList<IMarker>();
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(MARKER_CRITICISM, true, IResource.DEPTH_ZERO)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(MARKER_EXPLANATION, true, IResource.DEPTH_ZERO)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(MARKER_RECOMMENDATION, true, IResource.DEPTH_ZERO)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     return list;
	}
	
	public static void removeMarkers(IResource resource) {
		List<IMarker> markers = findMarkers(resource);
		for(IMarker marker : markers) {
			try {
				marker.delete();
			}
		     catch (CoreException e) {
		    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
		     }
		}
	}
	
	/*
	 * Returns a list of markers that are linked to the resource or any sub resource of the resource
	 */
	public static List<IMarker> findAllMarkers(IResource  resource) {
		ArrayList<IMarker> list = new ArrayList<IMarker>();
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(MARKER_CRITICISM, true, IResource.DEPTH_INFINITE)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(MARKER_EXPLANATION, true, IResource.DEPTH_INFINITE)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     try {
	         list.addAll(Arrays.asList(resource.findMarkers(MARKER_RECOMMENDATION, true, IResource.DEPTH_INFINITE)));
	     } 
	     catch (CoreException e) {
	    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
	     }
	     return list;
    }
	
	public static void removeAllMarkers(IResource resource) {
		List<IMarker> markers = findAllMarkers(resource);
		for(IMarker marker : markers) {
			try {
				marker.delete();
			}
		     catch (CoreException e) {
		    	 CriticPlugin.log(Status.ERROR, e.getMessage(), e);
		     }
		}
	}
	
	
	/*
	 * Returns the selection of the package explorer
	 */
	public static TreeSelection getTreeSelection() {

		ISelection selection = CriticPlugin.getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof TreeSelection) {
			return (TreeSelection)selection;
		}
		return null;
	}

	/*
	 * Returns the selection of the package explorer
	 */
	public static TextSelection getTextSelection() {

		ISelection selection = CriticPlugin.getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof TextSelection) {
			return (TextSelection)selection;
		}
		return null;
	}
	
	//Annotation ID
//	public static final String ANNOTATION = "edu.clarkson.serl.critic.critics";
	
//	public static void addAnnotation(IMarker marker, ITextSelection selection, ITextEditor editor) {
//	      //The DocumentProvider enables to get the document currently loaded in the editor
//	      IDocumentProvider idp = editor.getDocumentProvider();
//
//	      //This is the document we want to connect to. This is taken from the current editor input.
//	      IDocument document = idp.getDocument(editor.getEditorInput());
//
//	      //The IannotationModel enables to add/remove/change annoatation to a Document loaded in an Editor
//	      IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());
//
//	      //Note: The annotation type id specify that you want to create one of your annotations
//	      SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(ANNOTATION, marker);
//
//	      //Finally add the new annotation to the model
//	      iamf.connect(document);
//	      iamf.addAnnotation(ma,new Position(selection.getOffset(),selection.getLength()));
//	      iamf.disconnect(document);
//	}
}
