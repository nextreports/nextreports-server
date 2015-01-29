package ro.nextreports.server.web.analysis.feature.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;

import ro.nextreports.engine.querybuilder.sql.Operator;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.AnalysisFilter;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.LinkPropertyColumn;

public class FilterPanel extends FormContentPanel<Analysis> {
	
	private AnalysisFilter filterObject;
	private IModel<Analysis> model;
	private ArrayList<AnalysisFilter> filters;
	private Label label;
	private DataTable<AnalysisFilter, String> table;
	private FilterObjectDataProvider provider;
	private DropDownChoice<String> columnChoice;
	private DropDownChoice<String> operatorChoice;
	private TextField valueText;
	private int editIndex = -1;
	private IModel<String> addTextModel;
		
	public FilterPanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);	
		this.model = model;
		
		filterObject = new AnalysisFilter();
		filterObject.setColumn(model.getObject().getSimpleColumns().get(0));
		filterObject.setOperator(Operator.LIKE);	
		filterObject.setName(UUID.randomUUID().toString());
		
		filters = new ArrayList<AnalysisFilter>();
		filters.addAll(model.getObject().getFilters());
		
		add(new Label("info", new StringResourceModel("FilterPanel.info", this, null)));
		
		add(new Label("column", new StringResourceModel("FilterPanel.column", this, null)));
		columnChoice = new DropDownChoice<String>("columnChoice", 
				new PropertyModel<String>(this, "filterObject.column"), 
				model.getObject().getSimpleColumns(),				
				new ChoiceRenderer<String>() {
					@Override
					public Object getDisplayValue(String fullColumnName) {
						return DatabaseUtil.getColumnAlias(fullColumnName);
					}
				}
		);
		columnChoice.setOutputMarkupPlaceholderTag(true);
		columnChoice.setRequired(true);
 		add(columnChoice); 
 		
 		add(new Label("operator", new StringResourceModel("FilterPanel.operator", this, null)));
		operatorChoice = new DropDownChoice<String>("operatorChoice", 
				new PropertyModel<String>(this, "filterObject.operator"), Arrays.asList(Operator.operators));
		operatorChoice.setOutputMarkupPlaceholderTag(true);
		operatorChoice.setRequired(true);
 		add(operatorChoice); 
 		
 		add(new Label("value", new StringResourceModel("FilterPanel.value", this, null)));
 		valueText = new TextField<String>("valueText", new PropertyModel<String>(this, "filterObject.value"));
 		valueText.setOutputMarkupPlaceholderTag(true);
 		add(valueText);
 		
 		AjaxSubmitLink addLink = new AjaxSubmitLink("addLink") {						
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) { 				
 				if (editIndex != -1) {
 					int index = filters.indexOf(filterObject); 					
 					if ( (index != -1) && (index != editIndex) ) {
 						error(getString("FilterPanel.duplicateFilter"));	    
	            		target.add(getFeedbackPanel());
	        			return;
 					}
 					
 					filters.set(editIndex, filterObject); 					 	
 					addTextModel.setObject(getString("add")); 					
 					editIndex = -1;
 					target.add(label);
 				} else { 	 					
	 				if (filters.contains(filterObject)) {
	 					error(getString("FilterPanel.duplicateFilter"));	    
	            		target.add(getFeedbackPanel());
	        			return;
	 				}
	 				filters.add(filterObject.clone());	
	 			}
 				target.add(table);
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
	
	private void addTable() {
        List<IColumn<AnalysisFilter, String>> columns = new ArrayList<IColumn<AnalysisFilter, String>>();
        
        columns.add(new AbstractColumn<AnalysisFilter, String>(new Model<String>("")) {

            @Override
            public String getCssClass() {
                return "index";
            }

            public void populateItem(Item<ICellPopulator<AnalysisFilter>> item, String componentId, final IModel<AnalysisFilter> rowModel) {                 
            	int col=item.getIndex();
            	Item<?> i = (Item<?>) item.getParent().getParent();
            	int row = i.getIndex()+1; 
                item.add(new Label(componentId, new Model<String>(String.valueOf(row))));                
            }
        });
        
        columns.add(new AbstractColumn<AnalysisFilter, String>(new StringResourceModel("FilterPanel.column", FilterPanel.this, null)) {
            public void populateItem(Item<ICellPopulator<AnalysisFilter>> item, String componentId, final IModel<AnalysisFilter> rowModel) {
                final AnalysisFilter filterObject = rowModel.getObject();                
                item.add(new Label(componentId, new Model<String>(filterObject.getColumn())));
            }
        });   
        
        columns.add(new AbstractColumn<AnalysisFilter, String>(new StringResourceModel("FilterPanel.operator", FilterPanel.this, null)) {
            public void populateItem(Item<ICellPopulator<AnalysisFilter>> item, String componentId, final IModel<AnalysisFilter> rowModel) {
                final AnalysisFilter filterObject = rowModel.getObject();                
                item.add(new Label(componentId, new Model<String>(filterObject.getOperator())));
            }
        }); 
        
        columns.add(new AbstractColumn<AnalysisFilter, String>(new StringResourceModel("FilterPanel.value", FilterPanel.this, null)) {
            public void populateItem(Item<ICellPopulator<AnalysisFilter>> item, String componentId, final IModel<AnalysisFilter> rowModel) {
                final AnalysisFilter filterObject = rowModel.getObject();   
                Serializable s = filterObject.getValue();
                String value = (s == null) ? "" : s.toString();
                item.add(new Label(componentId, new Model<String>(value)));
            }
        }); 
        
        columns.add(new LinkPropertyColumn<AnalysisFilter>(new StringResourceModel("edit", FilterPanel.this, null), new StringResourceModel("edit", FilterPanel.this, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				AnalysisFilter filterObject = (AnalysisFilter) model.getObject();	
				editIndex = filters.indexOf(filterObject);
				FilterPanel.this.filterObject = filterObject;	
				addTextModel.setObject(getString("edit"));
				target.add(columnChoice);
				target.add(operatorChoice);
				target.add(valueText);
				target.add(label);
			}
		});
        
        columns.add(new LinkPropertyColumn<AnalysisFilter>(new StringResourceModel("delete", FilterPanel.this, null), new StringResourceModel("delete", FilterPanel.this, null),  new StringResourceModel("FilterPanel.askDelete", FilterPanel.this, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				AnalysisFilter filterObject = (AnalysisFilter) model.getObject();																																
				int index = filters.indexOf(filterObject);				
				filters.remove(index);				
                target.add(table);     
			}
		});
               
        provider =  new FilterObjectDataProvider(new Model(filters));
        table = new BaseTable<AnalysisFilter>("table", columns, provider, 10);
        table.setOutputMarkupId(true);
        add(table);
    }
	
	public AnalysisFilter getFilterObject() {
		return filterObject;
	}

	public List<AnalysisFilter> getFilters() {
		return filters;
	}				

}
