package ro.nextreports.server.web.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryAjaxEventBehavior;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.sortable.SortableJavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.util.ObjectCloner;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.feature.create.CreatePanel;
import ro.nextreports.server.web.analysis.feature.export.CsvResource;
import ro.nextreports.server.web.analysis.feature.export.XlsResource;
import ro.nextreports.server.web.analysis.feature.export.XlsxResource;
import ro.nextreports.server.web.analysis.feature.filter.FilterPanel;
import ro.nextreports.server.web.analysis.feature.group.GroupPanel;
import ro.nextreports.server.web.analysis.feature.paging.PaginatePanel;
import ro.nextreports.server.web.analysis.feature.select.ColumnsPanel;
import ro.nextreports.server.web.analysis.feature.sort.SortPanel;
import ro.nextreports.server.web.analysis.model.SelectedAnalysisModel;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.jgrowl.JGrowlAjaxBehavior;
import ro.nextreports.server.web.common.util.PreferencesHelper;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.security.SecurityUtil;

public class AnalysisPanel extends GenericPanel<Analysis> {
		
	private static final long serialVersionUID = 1L;
	private AnalysisDataProvider dataProvider;
	private XlsResource xlsResource;
	private XlsxResource xlsxResource;
	private CsvResource csvResource;
	
	@SpringBean
	private AnalysisService analysisService;
	
	@SpringBean
	private SecurityService securityService;
			
	private static final Logger LOG = LoggerFactory.getLogger(AnalysisPanel.class);	
	
	public AnalysisPanel(String id) {
		super(id, new SelectedAnalysisModel());				
		setOutputMarkupId(true);    
		addToolbar();            

		Form<Void> submitForm = new Form<Void>("submitForm");
		add(submitForm);
		
		dataProvider = new AnalysisDataProvider(getModel());
		xlsResource = new XlsResource(dataProvider);
		xlsxResource = new XlsxResource(dataProvider);
		csvResource = new CsvResource(dataProvider);
		submitForm.add(createTablePanel(dataProvider));   
        
        addLinks(submitForm);
    }
	
    @Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);		
		response.render(JavaScriptHeaderItem.forReference(SortableJavaScriptResourceReference.get()));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(AnalysisPanel.class, "analysis.css")));        
	}
    
    private void addToolbar() {
		IModel<String> toggleImageModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
		        String imagePath = "images/left-gray.png";
		        Map<String, String> preferences = NextServerSession.get().getPreferences();
                boolean isHidden = !PreferencesHelper.getBoolean("analysis.navigationToggle", preferences);
		        if (isHidden) {
					imagePath = "images/right-gray.png";
				}		        
		        return imagePath;
			}
        	
        };
		final ContextImage toggle = new ContextImage("toggle", toggleImageModel);
		toggle.add(new WiQueryAjaxEventBehavior(MouseEvent.CLICK) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				Map<String, String> preferences = NextServerSession.get().getPreferences();
				boolean toogle = false;
				if (preferences.containsKey("analysis.navigationToggle")) {
					toogle = Boolean.parseBoolean(preferences.get("analysis.navigationToggle"));
					toogle = !toogle;
				}
				
				preferences.put("analysis.navigationToggle", String.valueOf(toogle));
				NextServerSession.get().setPreferences(preferences);
				
				AnalysisBrowserPanel browserPanel = findParent(AnalysisBrowserPanel.class);
				target.add(browserPanel.getAnalysisNavigationPanel());
				target.add(toggle);
				target.add(AnalysisPanel.this);
			}

			public JsStatement statement() {
				return null;
			}
			
		});
        IModel<String> toggleTooltipModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				String tooltip = getString("DashboardPanel.hide");
		        Map<String, String> preferences = NextServerSession.get().getPreferences();
		        boolean isHidden = !PreferencesHelper.getBoolean("analysis.navigationToggle", preferences);
		        if (isHidden) {
					tooltip = getString("DashboardPanel.show");
				}
		        
		        return tooltip;
			}
        	
        };
		toggle.add(new AttributeModifier("title", toggleTooltipModel));
		add(toggle);
		
		
		add(new Label("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Analysis analysis = getModelObject();
				String title = getString("Analysis.title");
				if (analysis != null) {
					title += " : " + analysis.getName();
				}
				return title;
			}
		}));
    }
        
    private Panel createTablePanel(AnalysisDataProvider dataProvider ) {  
    	if (dataProvider.isEmpty()) {
    		return new EmptyPanel("tablePanel");
    	} else {
    		return new AnalysisTablePanel("tablePanel", dataProvider);
    	}
    }
    
    public void changeDataProvider(IModel<Analysis> model, AjaxRequestTarget target) {
    	dataProvider = new AnalysisDataProvider(model);
    	dataProvider.reset();
		xlsResource.setProvider(dataProvider);
		xlsxResource.setProvider(dataProvider);
		csvResource.setProvider(dataProvider);
		AnalysisPanel.this.get("submitForm:tablePanel").replaceWith(createTablePanel(dataProvider));
        target.add(AnalysisPanel.this);
    }
    
    private void addLinks(Form<Void> submitForm) {
    	submitForm.add(getCreateLink());    
    	submitForm.add(getSelectLink());    	
    	submitForm.add(getSortLink());
    	submitForm.add(getFilterLink());
    	submitForm.add(getGroupLink());
    	submitForm.add(getPaginateLink());
    	
    	//submitForm.add(getCsvLink());
    	//submitForm.add(getXlsLink());
    	submitForm.add(getXlsxLink());
    	submitForm.add(getSaveLink());
    	submitForm.add(getFreezeLink());
    }
    
    private AjaxLink<Analysis> getSelectLink() {
    	return new ToolbarLink<Analysis>("selectColumns", "ColumnsPanel.title", 560) {									
			
			@Override
			protected FormContentPanel<Analysis> createPanel() {
				return new ColumnsPanel(AnalysisPanel.this.getModel()) {
	            	
	                private static final long serialVersionUID = 1L;

	                @Override
	                public void onOk(AjaxRequestTarget target) {	                	
	                	if (getColumns().size() == 0) {	                		
	                		error(getString("ColumnsPanel.selectOne"));	    
	                		target.add(getFeedbackPanel());
	            			return;
	            		}	                	
	                	
	                	ModalWindow.closeCurrent(target);
	                    
	                	List<String> selectedColumns = getColumns();
	                	List<Boolean> sel = new ArrayList<Boolean>();
	                	for (String column : getChoices()) {
	                		sel.add(selectedColumns.contains(column)); 
	                	}	                	
	                	AnalysisPanel.this.getModel().getObject().setSelected(sel);	  
	                	AnalysisPanel.this.getModel().getObject().setColumns(getChoices()); 
	                	
	                	// if some columns are deselected and they were used in order by or group by clause, we have to remove
	                	// them from sorts and groups
	                	DatabaseUtil.removeGroupColumns(selectedColumns, AnalysisPanel.this.getModel().getObject().getGroups());
	                	DatabaseUtil.removeSortColumns(selectedColumns, AnalysisPanel.this.getModel().getObject().getSortProperty(), AnalysisPanel.this.getModel().getObject().getAscending());	                	
	                  
	                	changeDataProvider(AnalysisPanel.this.getModel(), target);	                	
	                }
	            };
			}						
    	};
    }
    
    private AjaxLink<Analysis> getSortLink() {
    	return new ToolbarLink<Analysis>("sortRows", "SortPanel.title", 400) {			
						
			@Override
			protected FormContentPanel<Analysis> createPanel() {
				return new SortPanel(AnalysisPanel.this.getModel()) {
	            	
	                private static final long serialVersionUID = 1L;

	                @Override
	                public void onOk(AjaxRequestTarget target) {	  
	                	if (getSortProperty().size() == 0) {	                		
	                		error(getString("SortPanel.selectOne"));	    
	                		target.add(getFeedbackPanel());
	            			return;
	            		}	 
	                	ModalWindow.closeCurrent(target);
	                    
	                	Analysis analysis = AnalysisPanel.this.getModel().getObject();	                	
	                	analysis.setSortProperty(getSortProperty());
	                	analysis.setAscending(getAscending());
	                	analysis.setFirstSortRemoved(isFirstSortRemoved());
	                	analysis.setChangeFirstSortOrder(isChangeFirstSortOrder());
	                  
	                    target.add(AnalysisPanel.this);
	                }
	            };
			}	
    	};
    }
    
    private AjaxLink<Analysis> getFilterLink() {
    	return new ToolbarLink<Analysis>("filterRows", "FilterPanel.title", 400) {

			@Override
			protected FormContentPanel<Analysis> createPanel() {
				return  new FilterPanel(AnalysisPanel.this.getModel()) {
	            	
	                private static final long serialVersionUID = 1L;

	                @Override
	                public void onOk(AjaxRequestTarget target) {	  	                	
	                	ModalWindow.closeCurrent(target);	                    
	                	Analysis analysis = AnalysisPanel.this.getModel().getObject();
	                	analysis.setFilters(getFilters());
	                	dataProvider.reset();	                  
	                    target.add(AnalysisPanel.this);
	                }
	            };
			}									
    	};
    }
    
    private AjaxLink<Analysis> getCreateLink() {
    	return new ToolbarLink<Analysis>("createColumns", "CreatePanel.title", 500) {

			@Override
			protected FormContentPanel<Analysis> createPanel() {
				return new CreatePanel(ObjectCloner.silenceDeepCopy(AnalysisPanel.this.getModel())) {
	            	
	                private static final long serialVersionUID = 1L;
	                private boolean deleted = false;

	                @Override
	                public void onOk(AjaxRequestTarget target) {	  
	                	
	                	ModalWindow.closeCurrent(target);
	                    
	                	Analysis analysis = getAnalysis();	 
	                	
	                	// header is modified
	                	AnalysisPanel.this.getModel().setObject(analysis);
	                	changeDataProvider(AnalysisPanel.this.getModel(), target);
	                }
	                
	                @Override
	                public void onDelete(Analysis analysis, AjaxRequestTarget target) {	                	
	                	AnalysisPanel.this.getModel().setObject(analysis);	                	                	
	            	}
	            };
			}			
    	};
    }
    
    private AjaxLink<Analysis> getPaginateLink() {
    	return new ToolbarLink<Analysis>("paginate", "PaginatePanel.title", 200) {

			@Override
			protected FormContentPanel<Analysis> createPanel() {
				return new PaginatePanel(AnalysisPanel.this.getModel()) {
	            	
	                private static final long serialVersionUID = 1L;

	                @Override
	                public void onOk(AjaxRequestTarget target) {	                		                	         		                	
	                	ModalWindow.closeCurrent(target);	   
	                	changeDataProvider(AnalysisPanel.this.getModel(), target);	                	
	                }
	            };
			}			
    	};
    }
    
    private AjaxLink<Analysis> getGroupLink() {
    	return new ToolbarLink<Analysis>("groupRows", "GroupPanel.title", 300) {

			@Override
			protected FormContentPanel<Analysis> createPanel() {
				return new GroupPanel(AnalysisPanel.this.getModel()) {
	            	
	                private static final long serialVersionUID = 1L;

	                @Override
	                public void onOk(AjaxRequestTarget target) {	  	                		 
	                	ModalWindow.closeCurrent(target);	                    
	                	Analysis analysis = AnalysisPanel.this.getModel().getObject();	                	
	                	analysis.setGroups(getGroups());
	                	dataProvider.reset();
	                    target.add(AnalysisPanel.this);
	                }
	            };
			}			
    	};
    }
    
    private ResourceLink<CsvResource> getCsvLink() {    	
    	return  new ResourceLink<CsvResource>("csvExport", csvResource) {
    		@Override
			public boolean isVisible() {				
				return !dataProvider.isEmpty();
			}	
    	};
    }
    
    private ResourceLink<XlsResource> getXlsLink() {
    	return  new ResourceLink<XlsResource>("xlsExport", xlsResource) {
    		@Override
			public boolean isVisible() {				
				return !dataProvider.isEmpty();
			}	
    	};
    }
    
    private ResourceLink<XlsxResource> getXlsxLink() {
    	return  new ResourceLink<XlsxResource>("xlsxExport", xlsxResource) {
    		@Override
			public boolean isVisible() {				
				return !dataProvider.isEmpty();
			}	
    	};
    }
    
    private AjaxSubmitLink getFreezeLink() {
    	return new AjaxSubmitLink("freeze") {

    		@Override
    		public void onSubmit(AjaxRequestTarget target, Form form) {
    			Analysis analysis = AnalysisPanel.this.getModel().getObject();	

    			// modify analysis to freeze anyway to disable the button    			
    			analysis.setFreezed(true);					    			
    			analysisService.modifyAnalysis(analysis);
    			    		
    			getSession().getFeedbackMessages().add(new FeedbackMessage(null, getString("freeze.start"), JGrowlAjaxBehavior.INFO_FADE));    			
    	        setResponsePage(HomePage.class);    	            	        
    	        
    	        analysisService.freeze(analysis);
    		}
    		
    		@Override
			public boolean isVisible() {				
				if (dataProvider.isEmpty() || AnalysisPanel.this.getModel().getObject().isFreezed()) {
					return false;
				}
				if (!SecurityUtil.hasPermission(securityService, PermissionUtil.getWrite(), getModelObject().getId())) {
    				return false;
    			}
    			return true;
			}	 
    		
    		@Override
    		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
    		    super.updateAjaxAttributes(attributes);
    		    attributes.getAjaxCallListeners().add(new AjaxCallListener() {
    		        @Override
    		        public CharSequence getBeforeHandler(Component cmpnt) {
    		            return "$(\"#" + cmpnt.getMarkupId() + "\").hide()";    		        	
    		        }
    		    });   
    		}
    		
    	};
    }
    
    private AjaxSubmitLink getSaveLink() {
    	return new AjaxSubmitLink("save") {

    		@Override
    		public void onSubmit(AjaxRequestTarget target, Form form) {
    			Analysis analysis = AnalysisPanel.this.getModel().getObject();	    			
    			analysisService.modifyAnalysis(analysis);
    			getSession().getFeedbackMessages().add(new FeedbackMessage(null, new StringResourceModel("Analysis.saved", null, new Object[] {analysis.getName()}).getString(), JGrowlAjaxBehavior.INFO_FADE));
    			setResponsePage(HomePage.class);
    		}
    		
    		@Override
			public boolean isVisible() {				
    			if (dataProvider.isEmpty()) {
    				return false;
    			}
    			if (!SecurityUtil.hasPermission(securityService, PermissionUtil.getWrite(), getModelObject().getId())) {
    				return false;
    			}
    			return true;
			}	
    		
    	};
    }                
    
    
	private abstract class ToolbarLink<T extends Analysis> extends AjaxLink<Analysis> {
				
		private static final long serialVersionUID = 1L;
		private String title;
		private int dialogWidth;

		public ToolbarLink(String id, String title, int dialogWidth) {
			super(id);
			this.title = title;
			this.dialogWidth = dialogWidth;
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			ModalWindow dialog = findParent(BasePage.class).getDialog();
			dialog.setTitle(getString(title));
			dialog.setInitialWidth(dialogWidth);
			dialog.setUseInitialHeight(false);
			FormContentPanel<Analysis> panel = createPanel();
			FormPanel<Analysis> formPanel = new FormPanel<Analysis>(dialog.getContentId(), panel, true);
			formPanel.add(AttributeModifier.append("class", "analysisForm"));
			dialog.setContent(formPanel);
			dialog.show(target);
		}

		protected abstract FormContentPanel<Analysis> createPanel();

		@Override
		public boolean isVisible() {
			return !dataProvider.isEmpty();
		}
	};

}
