package edu.clarkson.serl.critic.reporter;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

import edu.clarkson.serl.critic.CriticPlugin;

public class MarkerUpdater implements IMarkerUpdater {
	public String[] getAttribute() {
		// We will make it update all attributes of the marker.
	      return null;
	}

	public String getMarkerType() {
		// Because there are three different markers that this updater updates
		// we will not return null meaning, this is responsible for updating
		// any marker
		return null;
	}

	public boolean updateMarker(IMarker marker, IDocument doc, Position position) {
		try {
			if(!marker.isSubtypeOf(CriticMarkerFactory.MARKER))
				return false;
			int start = position.getOffset();
			int end = position.getOffset() + position.getLength();
			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, end);
			return true;
		} catch (CoreException e) {
			CriticPlugin.log(Status.ERROR, e.getMessage(), e);
			return false;
		}
	}
}
