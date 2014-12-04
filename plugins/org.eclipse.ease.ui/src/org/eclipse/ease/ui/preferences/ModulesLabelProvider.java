package org.eclipse.ease.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ModulesLabelProvider extends LabelProvider {

	private final Map<ImageDescriptor, Image> fImages = new HashMap<ImageDescriptor, Image>();

	@Override
	public String getText(final Object element) {
		if (element instanceof IPath)
			return ((IPath) element).lastSegment();

		if (element instanceof ModuleDefinition)
			return ((ModuleDefinition) element).getName();

		return super.getText(element);
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof IPath)
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/folder.png", true);

		if (element instanceof ModuleDefinition) {
			ImageDescriptor descriptor = ((ModuleDefinition) element).getImageDescriptor();
			if (descriptor != null) {
				if (!fImages.containsKey(descriptor))
					fImages.put(descriptor, descriptor.createImage());

				return fImages.get(descriptor);

			} else
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/module.png", true);
		}

		return super.getImage(element);
	}

	@Override
	public void dispose() {
		for (Image image : fImages.values())
			image.dispose();

		super.dispose();
	}
}
