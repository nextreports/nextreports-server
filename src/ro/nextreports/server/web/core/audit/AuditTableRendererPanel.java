package ro.nextreports.server.web.core.audit;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.dashboard.table.TableResource;

public class AuditTableRendererPanel extends GenericPanel<TableData> {
		
	private static final Logger LOG = LoggerFactory.getLogger(AuditTableRendererPanel.class);	
	
	private static DateFormat DATE_FORMAT= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
	public AuditTableRendererPanel(String id, IModel<TableData> model) {
		super(id, model);		
		
		add(createSaveToExcelLink(model));
		
		AuditDataProvider dataProvider = new AuditDataProvider(model.getObject());
		WebMarkupContainer container = new WebMarkupContainer("tableContainer");
		container.add(getCurrentTable(dataProvider));		
        add(container);        
	}	
	
	private BaseTable getCurrentTable(AuditDataProvider dataProvider) {    	        
        List<String> tableHeader = dataProvider.getHeader();				
		BaseTable<List<Object>> table = new BaseTable<List<Object>>("table", getPropertyColumns(tableHeader), dataProvider, 100);					
        return table;
    }

	private List<IColumn<List<Object>, String>> getPropertyColumns(List<String> header) {
		List<IColumn<List<Object>, String>> columns = new ArrayList<IColumn<List<Object>, String>>();
		int columnCount = header.size();
		for (int i = 0; i < columnCount; i++) {			
			columns.add(new PropertyColumn<List<Object>, String>(new Model<String>(header.get(i)), header.get(i), String.valueOf(i)) {
				@Override				    
				public IModel<Object> getDataModel(IModel rowModel) {
					IModel<Object> model = super.getDataModel(rowModel);						
					if ((model.getObject() instanceof Date) && (model.getObject() != null)) {
						return new Model(DATE_FORMAT.format((Date)model.getObject()));
					}
					return model;
				}
			});
		}
		return columns;
	}
	
	private Link<TableResource> createSaveToExcelLink(final IModel<TableData> model) {
		ByteArrayResource download = new TableResource(model.getObject(), "audit.xls");		
		ResourceLink resourceLink =  new ResourceLink<TableResource>("download", download);
		// see busy-indicator.js
		// we do not want a busy indicator in this situation
		resourceLink.add(new AttributeAppender("class", new Model<String>("noBusyIndicator"), " "));
		return resourceLink;
	}
	
        
}
