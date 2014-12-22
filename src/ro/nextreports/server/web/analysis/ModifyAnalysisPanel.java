package ro.nextreports.server.web.analysis;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.core.validation.JcrNameValidator;

public class ModifyAnalysisPanel extends Panel {

	private static final long serialVersionUID = 1L;
		
	private String title;	

	public ModifyAnalysisPanel(String id, Model<Analysis> model) {
		super(id);
				
		title = model.getObject().getName();
		
        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

		Form<Analysis> form = new Form<Analysis>("form");
		add(form);		
		
		TextField<String> titleText = new TextField<String>("title", new PropertyModel<String>(this, "title"));		
		titleText.add(new JcrNameValidator());        
		titleText.setRequired(true);     		
        form.add(titleText);                
        
        form.add(new AjaxSubmitLink("modify") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (!onVerify(target)) {
					onError(target, form);
				} else {
					onModify(target);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);				
			}

		});
	}    	
	
	public String getTitle() {
		return title;
	}

	public void onModify(AjaxRequestTarget target) {
		// override
	}
	
	public boolean onVerify(AjaxRequestTarget target) {
		// override
		return true;
	}
    
	public void onCancel(AjaxRequestTarget target) {
		// override
	}

}
