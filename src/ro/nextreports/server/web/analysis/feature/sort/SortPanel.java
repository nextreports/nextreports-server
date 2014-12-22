package ro.nextreports.server.web.analysis.feature.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.analysis.util.DatabaseUtil;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.LinkPropertyColumn;

public class SortPanel extends FormContentPanel<Analysis> {
	
	private ArrayList<String> sortProperty;
	private ArrayList<Boolean> ascending;		
	private SortObject sortObject;
	
	private DropDownChoice<String> columnChoice;
	private DropDownChoice<Boolean> orderChoice;
	private Label label;
	private DataTable<SortObject, String> table;
	private SortObjectDataProvider provider;
	private boolean firstSortRemoved = false;
	private boolean changeFirstSortOrder = false;
	private int editIndex = -1;
	private IModel<String> addTextModel;
	
	public SortPanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);				
		
		sortProperty = new ArrayList<String>(model.getObject().getSortProperty());		
		ascending = new ArrayList<Boolean>(model.getObject().getAscending());
		
		sortObject = new SortObject();
		sortObject.setColumn(model.getObject().getSimpleColumns().get(0));
		sortObject.setOrder(Boolean.TRUE);					 
		
		add(new Label("column", new StringResourceModel("SortPanel.column", null, null)));
		columnChoice = new DropDownChoice<String>("columnChoice", 
 				new PropertyModel<String>(this, "sortObject.column"), model.getObject().getSimpleColumns(),
 				new ChoiceRenderer<String>() {
					@Override
					public Object getDisplayValue(String fullColumnName) {
						return DatabaseUtil.getColumnAlias(fullColumnName);
					}
		});
		columnChoice.setOutputMarkupPlaceholderTag(true);
		columnChoice.setRequired(true);
 		add(columnChoice); 
		
		add(new Label("order", new StringResourceModel("SortPanel.order", null, null)));  
		orderChoice = new DropDownChoice<Boolean>("orderChoice", 
 				new PropertyModel<Boolean>(this, "sortObject.order"), Arrays.asList(Boolean.TRUE, Boolean.FALSE));
		orderChoice.setOutputMarkupPlaceholderTag(true);
		orderChoice.setRequired(true);
 		add(orderChoice); 
 		
 		AjaxSubmitLink addLink = new AjaxSubmitLink("addLink") {						
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				if (editIndex != -1) {
 					int index = sortProperty.indexOf(sortObject.getColumn()); 					
 					if ( (index != -1) && (index != editIndex) ) {
 						error(getString("SortPanel.duplicateColumn"));	    
	            		target.add(getFeedbackPanel());
	        			return;
 					}
 					if (editIndex == 0)  {
 						if (sortProperty.get(editIndex).equals(sortObject.getColumn())) {
 							changeFirstSortOrder = true;
 						} else {
 							firstSortRemoved = true;
 						}
 					}
 					sortProperty.set(editIndex, sortObject.getColumn());
 					ascending.set(editIndex, sortObject.getOrder()); 	
 					addTextModel.setObject(getString("add")); 					
 					editIndex = -1;
 					target.add(label);
 				} else {
	 				if (sortProperty.contains(sortObject.getColumn())) {
	 					error(getString("SortPanel.duplicateColumn"));	    
	            		target.add(getFeedbackPanel());
	        			return;
	 				}
	 				sortProperty.add(sortObject.getColumn());
	 				ascending.add(sortObject.getOrder());
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
        List<IColumn<SortObject, String>> columns = new ArrayList<IColumn<SortObject, String>>();
        
        columns.add(new AbstractColumn<SortObject, String>(new Model<String>("")) {

            @Override
            public String getCssClass() {
                return "index";
            }

            public void populateItem(Item<ICellPopulator<SortObject>> item, String componentId, final IModel<SortObject> rowModel) {                 
            	int col=item.getIndex();
            	Item<?> i = (Item<?>) item.getParent().getParent();
            	int row = i.getIndex()+1; 
                item.add(new Label(componentId, new Model<String>(String.valueOf(row))));                
            }
        });
        
        columns.add(new AbstractColumn<SortObject, String>(new StringResourceModel("SortPanel.column", null, null)) {
            public void populateItem(Item<ICellPopulator<SortObject>> item, String componentId, final IModel<SortObject> rowModel) {
                final SortObject sortObject = rowModel.getObject();                
                item.add(new Label(componentId, new Model<String>(sortObject.getColumn())));
            }
        });   
        
        columns.add(new AnalysisBooleanImagePropertyColumn<SortObject>(new StringResourceModel("SortPanel.order", null, null), "order"));
        
        columns.add(new LinkPropertyColumn<SortObject>(new StringResourceModel("edit", null, null), new StringResourceModel("edit", null, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				SortObject sortObject = (SortObject) model.getObject();	
				editIndex = sortProperty.indexOf(sortObject.getColumn());
				SortPanel.this.sortObject = sortObject;	
				addTextModel.setObject(getString("edit"));
				target.add(columnChoice);
				target.add(orderChoice);
				target.add(label);
			}
		});
        
        columns.add(new LinkPropertyColumn<SortObject>(new StringResourceModel("delete", null, null), new StringResourceModel("delete", null, null),  new StringResourceModel("SortPanel.askDelete", null, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				SortObject sortObject = (SortObject) model.getObject();																																
				int index = sortProperty.indexOf(sortObject.getColumn());
				if (index == 0) {
					firstSortRemoved = true;
				}
				sortProperty.remove(index);
				ascending.remove(index);
                target.add(table);     
			}
		});
               
        provider =  new SortObjectDataProvider(new Model<ArrayList<String>>(sortProperty), new Model<ArrayList<Boolean>>(ascending));
        table = new BaseTable<SortObject>("table", columns, provider, 10);
        table.setOutputMarkupId(true);
        add(table);
    }

	public ArrayList<String> getSortProperty() {
		return sortProperty;
	}

	public ArrayList<Boolean> getAscending() {
		return ascending;
	}	
	
	public boolean isFirstSortRemoved() {
		return firstSortRemoved;
	}
	
	public boolean isChangeFirstSortOrder() {
		return changeFirstSortOrder;
	}		
	

}
