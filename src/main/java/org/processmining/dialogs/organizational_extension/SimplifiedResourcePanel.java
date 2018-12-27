package org.processmining.dialogs.organizational_extension;

import org.processmining.models.organizational_extension.Resource;
import ru.hse.pais.shugurov.widgets.elements.ElementPanel;
import ru.hse.pais.shugurov.widgets.panels.EditableListPanel;

import java.util.List;

/**
 * Created by Ivan Shugurov on 21.04.2014.
 *
 * @author Ivan Shugurov
 *         Created  21.04.2014
 */
public class SimplifiedResourcePanel extends EditableListPanel<Resource>
{
    private List<Resource> simplifiedResources;
    private boolean isUsingDelaysBetweenActions;

    public SimplifiedResourcePanel(List<Resource> simplifiedResources, List<Resource> allResources, boolean isUsingDelaysBetweenActions)
    {
        super(allResources);
        this.simplifiedResources = simplifiedResources;
        this.isUsingDelaysBetweenActions = isUsingDelaysBetweenActions;
    }

    @Override
    protected void removeElement(Resource resource, ElementPanel panel)
    {
        super.removeElement(resource, panel);
        resource.removeResource();
        simplifiedResources.remove(resource);
    }

    @Override
    protected Resource createNewElement()
    {
        BaseResourceCreationDialog dialog = new SimplifiedResourceDialog();
        dialog.setVisible(true);
        Resource resource = dialog.getResource();
        if (resource != null)
        {
            simplifiedResources.add(resource);
        }
        return resource;
    }

    @Override
    protected boolean editElement(Resource resource, ElementPanel panel)
    {
        BaseResourceCreationDialog dialog = new SimplifiedResourceDialog(resource);
        dialog.setVisible(true);
        return dialog.getResource() != null;
    }

    private class SimplifiedResourceDialog extends BaseResourceCreationDialog
    {

        public SimplifiedResourceDialog()
        {
            super(isUsingDelaysBetweenActions);
        }

        public SimplifiedResourceDialog(Resource resource)
        {
            super(resource, isUsingDelaysBetweenActions);
        }

        @Override
        protected boolean verifyInput()
        {
            return true;
        }

        @Override
        protected void createResource(String resourceName)
        {
            resource = new Resource(resourceName);
        }
    }
}
