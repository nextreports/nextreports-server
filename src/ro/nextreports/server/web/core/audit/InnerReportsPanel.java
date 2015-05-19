package ro.nextreports.server.web.core.audit;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.audit.list.ListPanel;
import ro.nextreports.server.web.core.audit.rights.RightsPanel;
import ro.nextreports.server.web.core.audit.run.RunPanel;

public class InnerReportsPanel extends Panel {

	@SpringBean
    private StorageService storageService;
	
	@SpringBean
    private SecurityService securityService;
	
	@SpringBean
    private ReportService reportService;
		
	private static final Logger LOG = LoggerFactory.getLogger(InnerReportsPanel.class);
	
	private Panel tablePanel;
	
	public InnerReportsPanel(String id) {
		super(id);
		
		ListView<InnerReport> listReports = new ListView<InnerReport>("listReports", Arrays.asList(InnerReport.values())) {
			@Override
			protected void populateItem(ListItem<InnerReport> item) {
				item.add(createLink("report", item.getModel()));
				item.add(new Label("description", getString("Section.Audit.innerReports." + item.getModelObject().getDescription() + ".desc")));
			}			
		};		
		
		add(listReports);	
				
		tablePanel = new EmptyPanel("panel");
		tablePanel.setOutputMarkupPlaceholderTag(true);
		add(tablePanel);
		
		setOutputMarkupId(true);
	}
	
	private AjaxLink<String> createLink(String id, final IModel<InnerReport> model) {

		AjaxLink<String> link = new AjaxLink<String>(id) {

			@Override
			public void onClick(AjaxRequestTarget target) {								
				click(model.getObject(), target);
			}
		};
		link.add(new Label("label", getString("Section.Audit.innerReports." + model.getObject().toString())));		
		return link;
	}
	
	private void click(InnerReport ir, AjaxRequestTarget target) {
		final ModalWindow dialog = findParent(BasePage.class).getDialog();
        dialog.setTitle(getString("Section.Audit.innerReports." + ir.toString()));
        int width = 500;                       
        
        FormContentPanel panel;
        if (ir.toString().equals(InnerReport.RIGHTS.toString())) {
			panel = new RightsPanel() {
				@Override
				public void onOk(AjaxRequestTarget target) {											
					TableData data = getResults(getAuditRights());					
					displayResults(dialog, InnerReport.RIGHTS.toString(), data, getLinkColumns(), getTitle(), target);
				}
			};
        } else if (ir.toString().equals(InnerReport.RUN.toString())) {
        		width = 350;
				panel = new RunPanel() {
					@Override
					public void onOk(AjaxRequestTarget target) {												
						TableData data = getResults(getAuditRun());							
						displayResults(dialog, InnerReport.RUN.toString(), data, getLinkColumns(), getTitle(), target);
					}
				};	
        } else if (ir.toString().equals(InnerReport.LIST.toString())) {
        	width = 250;
			panel = new ListPanel() {
				@Override
				public void onOk(AjaxRequestTarget target) {											
					TableData data = getResults(getAuditList());						
					displayResults(dialog, InnerReport.LIST.toString(), data, null, getTitle(), target);
				}
			};			
        } else {
        	panel = new FormContentPanel(FormPanel.CONTENT_ID);
        }
        dialog.setInitialWidth(width);
        dialog.setUseInitialHeight(false);
        dialog.setContent(new FormPanel(dialog.getContentId(), panel, true));        
        dialog.show(target);

	}
	
	private void displayResults(ModalWindow dialog, String type, TableData data, ArrayList<Integer> links, String title, AjaxRequestTarget target) {
		Panel newPanel;
		if (data.getHeader().size() > 0) {
			newPanel = new AuditTableRendererPanel("panel", type, new Model(data), new Model(links), new Model(title));						
		} else {
			newPanel = new EmptyPanel("panel");
		}
		tablePanel.replaceWith(newPanel);
		tablePanel = newPanel;
		dialog.close(target);	
		target.add(InnerReportsPanel.this);
	}
		
}
