package ro.nextreports.server.web.action.schedule;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.util.ObjectCloner;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.schedule.ScheduleWizard;

public class CloneActionLink extends ActionAjaxLink {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(CloneActionLink.class);
	
	@SpringBean
	private StorageService storageService;
	
	public CloneActionLink(ActionContext actionContext) {
		super(actionContext);
	}

    public void executeAction(AjaxRequestTarget target) {
        Entity entity = getActionContext().getEntity();
        try {                        
        	
        	Entity clonedEntity = ObjectCloner.silenceDeepCopy(entity);        	
            String cloneName = clonedEntity.getName() + "_clone";            
            clonedEntity.setName(cloneName);                        
            String id = storageService.addEntity(clonedEntity);
            
            SchedulerJob job =  (SchedulerJob)storageService.getEntityById(id);

            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
            ScheduleWizard wizard = new ScheduleWizard("work", job);
            wizard.add(AttributeModifier.append("class", "wizardScheduler"));
            panel.forwardWorkspace(wizard  , target);
            
            //setResponsePage(new ScheduleWizardPage(job));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
