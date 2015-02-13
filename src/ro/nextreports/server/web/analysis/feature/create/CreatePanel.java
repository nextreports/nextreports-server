package ro.nextreports.server.web.analysis.feature.create;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.AnalysisDeclaredColumn;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.AnalysisUtil;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.LinkPropertyColumn;
import ro.nextreports.server.web.core.validation.AnalysisNameValidator;

public class CreatePanel extends FormContentPanel<Analysis> {

	private AnalysisDeclaredColumn declaredColumnObject;
	private IModel<Analysis> model;
	private Label label;
	private DataTable<AnalysisDeclaredColumn, String> table;
	private DeclaredColumnObjectDataProvider provider;
	private TextField<String> nameText;		
	private TextArea<String> expressionText;
	private DropDownChoice<String> typeChoice;	
	private String selectedColumn;
	private String selectedAgg;
	
	private int editIndex = -1;
	private int oldColumnIndex = -1;
	private int oldFilterIndex = -1;
	private int oldSortIndex = -1;
	private int oldGroupIndex = -1;
	private IModel<String> addTextModel;
	
	@SpringBean
    private StorageService storageService;   
		
	public CreatePanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);	
		this.model = model;		
				
		ContextImage urlImage = new ContextImage("infoImage","images/information.png");        
        urlImage.add(new SimpleTooltipBehavior(AnalysisUtil.getAnalysisInfo(model.getObject(), 5, storageService.getSettings())));
        add(urlImage);
		
		declaredColumnObject = new AnalysisDeclaredColumn();
		declaredColumnObject.setType("java.lang.String");	
		declaredColumnObject.setName(UUID.randomUUID().toString());
		selectedColumn = model.getObject().getColumns().get(0);	
						
		add(new Label("name",  new StringResourceModel("CreatePanel.name", this, null)));
		nameText = new TextField<String>("nameText", new PropertyModel<String>(this, "declaredColumnObject.columnName"));
		nameText.add(new AnalysisNameValidator(getString("JcrNameValidator")));
		nameText.setOutputMarkupPlaceholderTag(true);
 		add(nameText);
 		
 		DropDownChoice<String> columnChoice = new DropDownChoice<String>("columnChoice", new PropertyModel<String>(this, "selectedColumn"), model.getObject().getColumns());
		columnChoice.setOutputMarkupPlaceholderTag(true);
		columnChoice.setNullValid(false);		
 		add(columnChoice); 
 		
 		final DropDownChoice<String> aggChoice = new DropDownChoice<String>("aggChoice", new PropertyModel<String>(this, "selectedAgg"), DatabaseUtil.aggregates);
 		aggChoice.setOutputMarkupPlaceholderTag(true);
 		aggChoice.setNullValid(true);	
 		// needed for addColumn AjaxLink
 		aggChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
 		      @Override
 		      protected void onUpdate(AjaxRequestTarget target) {
 		    	 selectedAgg = (String) getFormComponent().getConvertedInput();
 		      }
 		 });
 		add(aggChoice); 
 		
 		AjaxLink addColumn = new AjaxLink("addColumn") {			
			@Override
			public void onClick(AjaxRequestTarget target) {
				String newText = declaredColumnObject.getExpression();
				if (newText == null) {
					newText = "";
				}							
				if (selectedAgg == null) {
					newText = newText + " " + selectedColumn;
				} else {
					newText = newText + " " + selectedAgg + "(" + selectedColumn + ")";
				}				
				declaredColumnObject.setExpression(newText);	
				target.add(expressionText);				
			} 			
 		};
 		add(addColumn);
 		
 		add(new Label("expression", new StringResourceModel("CreatePanel.expression", this, null)));
		expressionText = new TextArea<String>("expressionText", new PropertyModel<String>(this, "declaredColumnObject.expression"));
		//expressionText.add(new JcrNameValidator(getString("JcrNameValidator")));
		expressionText.setOutputMarkupPlaceholderTag(true);			
 		add(expressionText); 
 		
 		add(new Label("type", new StringResourceModel("CreatePanel.type", this, null)));
		typeChoice = new DropDownChoice<String>("typeChoice", 
				new PropertyModel<String>(this, "declaredColumnObject.type"),
				DatabaseUtil.getJavaTypes(), 
				new ChoiceRenderer<String>() {
					@Override
					public Object getDisplayValue(String name) {
						int index = name.lastIndexOf(".");
						return name.substring(index+1);
					}
				}	
		);
		typeChoice.setOutputMarkupPlaceholderTag(true);
		typeChoice.setNullValid(false);		
 		add(typeChoice);  		 		
 		
 		AjaxSubmitLink addLink = new AjaxSubmitLink("addLink") {						
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) { 				
 				if ((declaredColumnObject.getColumnName() == null) || "".equals(declaredColumnObject.getColumnName().trim())) {
 					error(getString("CreatePanel.askName"));
 					target.add(getFeedbackPanel());
        			return;
 				}
 				if ((declaredColumnObject.getExpression() == null) || "".equals(declaredColumnObject.getExpression().trim())) {
 					error(getString("CreatePanel.askExpression"));
 					target.add(getFeedbackPanel());
        			return;
 				}
 				if (editIndex != -1) {
 					int index =  findDeclaredColumnByName(CreatePanel.this.model.getObject().getDeclaredColumns(), declaredColumnObject.getColumnName()); 					
 					if ( (index != -1) && (index != editIndex) ) {
 						error(getString("CreatePanel.duplicateColumn"));	    
	            		target.add(getFeedbackPanel());
	        			return;
 					}
 					 					
 					CreatePanel.this.model.getObject().editDeclaredColumn(editIndex, oldColumnIndex, oldFilterIndex, oldSortIndex, oldGroupIndex, declaredColumnObject);
 					resetEdit(target);
 				} else { 	
 					int index = findDeclaredColumnByName(CreatePanel.this.model.getObject().getDeclaredColumns(), declaredColumnObject.getColumnName());
	 				if (index != -1) {
	 					error(getString("CreatePanel.duplicateColumn"));	    
	            		target.add(getFeedbackPanel());
	        			return;
	 				}
	 				CreatePanel.this.model.getObject().addDeclaredColumn(declaredColumnObject.clone());
	 			} 				
 				target.add(getFeedbackPanel());
 				target.add(table);
 			} 
 			
 			@Override
 			protected void onError(AjaxRequestTarget target, Form<?> form) { 				
 				target.add(getFeedbackPanel());
 			}
 			
 	    };
 	    
 	    addTextModel = Model.of(""); 	    
 	    label = new Label("addMessage", addTextModel);
 	    label.setOutputMarkupPlaceholderTag(true);
 	    addLink.add(label);
 	    add(addLink);
 	    
 	    addTable();
	}	
	
	protected void onConfigure()  {
		super.onConfigure();
		addTextModel.setObject(getString("add"));
    }
	
	private void resetEdit(AjaxRequestTarget target) {
		addTextModel.setObject(getString("add"));
		editIndex = -1;
		oldColumnIndex = -1;
		oldFilterIndex = -1;
		oldSortIndex = -1;
		oldGroupIndex = -1;
		target.add(label);
	}
	
	private void addTable() {
        List<IColumn<AnalysisDeclaredColumn, String>> columns = new ArrayList<IColumn<AnalysisDeclaredColumn, String>>();
        
        columns.add(new AbstractColumn<AnalysisDeclaredColumn, String>(new Model<String>("")) {

            @Override
            public String getCssClass() {
                return "index";
            }

            public void populateItem(Item<ICellPopulator<AnalysisDeclaredColumn>> item, String componentId, final IModel<AnalysisDeclaredColumn> rowModel) {                 
            	int col=item.getIndex();
            	Item<?> i = (Item<?>) item.getParent().getParent();
            	int row = i.getIndex()+1; 
                item.add(new Label(componentId, new Model<String>(String.valueOf(row))));                
            }
        });
        
        columns.add(new AbstractColumn<AnalysisDeclaredColumn, String>(new StringResourceModel("CreatePanel.name", CreatePanel.this, null)) {
            public void populateItem(Item<ICellPopulator<AnalysisDeclaredColumn>> item, String componentId, final IModel<AnalysisDeclaredColumn> rowModel) {
                final AnalysisDeclaredColumn createObject = rowModel.getObject();                
                item.add(new Label(componentId, new Model<String>(createObject.getColumnName())));
            }
        });   
        
        columns.add(new AbstractColumn<AnalysisDeclaredColumn, String>(new StringResourceModel("CreatePanel.expression", CreatePanel.this, null)) {
            public void populateItem(Item<ICellPopulator<AnalysisDeclaredColumn>> item, String componentId, final IModel<AnalysisDeclaredColumn> rowModel) {
                final AnalysisDeclaredColumn createObject = rowModel.getObject();                
                item.add(new Label(componentId, new Model<String>(createObject.getExpression())));
            }
        });   
        
        columns.add(new AbstractColumn<AnalysisDeclaredColumn, String>(new StringResourceModel("CreatePanel.type", CreatePanel.this, null)) {
            public void populateItem(Item<ICellPopulator<AnalysisDeclaredColumn>> item, String componentId, final IModel<AnalysisDeclaredColumn> rowModel) {
                final AnalysisDeclaredColumn createObject = rowModel.getObject();    
                String type = createObject.getType();
                int index = type.lastIndexOf(".");				
                item.add(new Label(componentId, new Model<String>(type.substring(index+1))));
            }
        });                 
        
        columns.add(new LinkPropertyColumn<AnalysisDeclaredColumn>(new StringResourceModel("edit", CreatePanel.this, null), new StringResourceModel("edit", CreatePanel.this, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				AnalysisDeclaredColumn declaredColumnObject = (AnalysisDeclaredColumn) model.getObject();	
				editIndex = CreatePanel.this.model.getObject().getDeclaredColumns().indexOf(declaredColumnObject);
				oldColumnIndex = CreatePanel.this.model.getObject().getColumnIndexByName(declaredColumnObject.getExpression() + DatabaseUtil.AS + declaredColumnObject.getColumnName());
				oldFilterIndex = CreatePanel.this.model.getObject().getFilterIndexByName(declaredColumnObject.getColumnName());
				oldSortIndex = CreatePanel.this.model.getObject().getSortIndexByName(declaredColumnObject.getColumnName());
				oldGroupIndex = CreatePanel.this.model.getObject().getGroupIndexByName(declaredColumnObject.getColumnName());
				CreatePanel.this.declaredColumnObject = declaredColumnObject;	
				addTextModel.setObject(getString("edit"));
				target.add(nameText);
				target.add(expressionText);
				target.add(typeChoice);				
				target.add(label);
			}
		});
        
        columns.add(new LinkPropertyColumn<AnalysisDeclaredColumn>(new StringResourceModel("delete", CreatePanel.this, null), new StringResourceModel("delete", CreatePanel.this, null),  new StringResourceModel("CreatePanel.askDelete", CreatePanel.this, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				AnalysisDeclaredColumn declaredColumnObject = (AnalysisDeclaredColumn) model.getObject();																																								
				CreatePanel.this.model.getObject().deleteDeclaredColumn(declaredColumnObject);
				resetEdit(target);
				target.add(table);
				onDelete(getAnalysis(), target);                
			}
		});
               
        provider =  new DeclaredColumnObjectDataProvider(model);
        table = new BaseTable<AnalysisDeclaredColumn>("table", columns, provider, 10);
        table.setOutputMarkupId(true);
        add(table);
    }
	
	public AnalysisDeclaredColumn getDeclaredColumnObject() {
		return declaredColumnObject;
	}		
	
	public Analysis getAnalysis() {
		return CreatePanel.this.model.getObject();
	}
	
	public void onDelete(Analysis analysis, AjaxRequestTarget target) {		
	}
	
	private int findDeclaredColumnByName(List<AnalysisDeclaredColumn> list, String columnName) {
		for (int i=0, size=list.size(); i<size; i++) {
			if (list.get(i).getColumnName().equals(columnName)) {
				return i;
			}
		}
		return -1;
	}

}
