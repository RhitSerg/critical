package edu.clarkson.serl.critic.swing;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SwingCriticPlugin implements BundleActivator {
	// The plug-in ID
	public static final String PLUGIN_ID = "edu.clarkson.serl.critic.swing"; //$NON-NLS-1$
	

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		SwingCriticPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		SwingCriticPlugin.context = null;
	}

}
