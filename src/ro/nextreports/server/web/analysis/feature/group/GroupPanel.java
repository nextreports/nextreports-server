package ro.nextreports.server.web.analysis.feature.group;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.LinkPropertyColumn;

public class GroupPanel extends FormContentPanel<Analysis> {
	
	private LinkedList<String> groups;	
	private Label label;
	private String groupObject;	
	private DropDownChoice<String> columnChoice;	
	private DataTable<String, String> table;
	private GroupObjectDataProvider provider;	
	private int editIndex = -1;
	private IModel<String> addTextModel;
	
	public GroupPanel(IModel<Analysis> model) {		
		super(FormPanel.CONTENT_ID);
		
		groups = new LinkedList<String>(model.getObject().getGroups());		
		
		groupObject = model.getObject().getColumns().get(0);							 
		
		add(new Label("column", new StringResourceModel("GroupPanel.column", null, null)));
		columnChoice = new DropDownChoice<String>("columnChoice", 
 				new PropertyModel<String>(this, "groupObject"), model.getObject().getSimpleColumns());
		columnChoice.setOutputMarkupPlaceholderTag(true);
		columnChoice.setRequired(true);
 		add(columnChoice); 
				
 		AjaxSubmitLink addLink = new AjaxSubmitLink("addLink") {						
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				if (editIndex != -1) {
 					int index = groups.indexOf(groupObject); 					
 					if ( (index != -1) && (index != editIndex) ) {
 						error(getString("GroupPanel.duplicateGroup"));	    
	            		target.add(getFeedbackPanel());
	        			return;
 					} 					
 					groups.set(editIndex, groupObject); 					 	
 					addTextModel.setObject(getString("add")); 					
 					editIndex = -1;
 					target.add(label);
 				} else {
	 				if (groups.contains(groupObject)) {
	 					error(getString("GroupPanel.duplicateGroup"));	    
	            		target.add(getFeedbackPanel());
	        			return;
	 				}
	 				groups.add(groupObject);	 				
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
        List<IColumn<String, String>> columns = new ArrayList<IColumn<String, String>>();
        
        columns.add(new AbstractColumn<String, String>(new Model<String>("")) {

            @Override
            public String getCssClass() {
                return "index";
            }

            public void populateItem(Item<ICellPopulator<String>> item, String componentId, final IModel<String> rowModel) {                 
            	int col=item.getIndex();
            	Item<?> i = (Item<?>) item.getParent().getParent();
            	int row = i.getIndex()+1; 
                item.add(new Label(componentId, new Model<String>(String.valueOf(row))));                
            }
        });
        
        columns.add(new AbstractColumn<String, String>(new StringResourceModel("GroupPanel.column", null, null)) {
            public void populateItem(Item<ICellPopulator<String>> item, String componentId, final IModel<String> rowModel) {
                final String groupObject = rowModel.getObject();                
                item.add(new Label(componentId, new Model<String>(groupObject)));
            }
        });                  
        
        columns.add(new LinkPropertyColumn<String>(new StringResourceModel("edit", null, null), new StringResourceModel("edit", null, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				String groupObject = (String) model.getObject();	
				editIndex = groups.indexOf(groupObject);
				GroupPanel.this.groupObject = groupObject;	
				addTextModel.setObject(getString("edit"));
				target.add(columnChoice);				
				target.add(label);
			}
		});
        
        columns.add(new LinkPropertyColumn<String>(new StringResourceModel("delete", null, null), new StringResourceModel("delete", null, null),  new StringResourceModel("GroupPanel.askDelete", null, null)) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Item item, String componentId, IModel model, AjaxRequestTarget target) {										
				String groupObject = (String) model.getObject();																																
				int index = groups.indexOf(groupObject);				
				groups.remove(index);				
                target.add(table);     
			}
		});
               
        provider =  new GroupObjectDataProvider(new Model<LinkedList<String>>(groups));
        table = new BaseTable<String>("table", columns, provider, 10);
        table.setOutputMarkupId(true);
        add(table);
    }

	public LinkedList<String> getGroups() {
		return groups;
	}
	
}
