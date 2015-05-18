package ro.nextreports.server.web.core.audit;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.util.TableData;
import ro.nextreports.engine.util.ObjectCloner;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.web.common.misc.SimpleLink;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.dashboard.table.TableResource;

public class AuditTableRendererPanel extends GenericPanel<TableData> {
	
	@SpringBean
	private ReportService reportService;
		
	private static final Logger LOG = LoggerFactory.getLogger(AuditTableRendererPanel.class);	
	
	private static DateFormat DATE_FORMAT= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	private List<Integer> links;
		
	public AuditTableRendererPanel(String id, IModel<TableData> model, IModel<List<Integer>> linksModel) {
		super(id, model);		
		links = linksModel.getObject();
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
			if ((links != null) && links.contains(i)) {
				final int index = i;
				columns.add(new AbstractColumn<List<Object>, String>(new Model<String>(getString("Url"))) {

		            public void populateItem(Item<ICellPopulator<List<Object>>> item, String componentId,
		                                     final IModel<List<Object>> rowModel) {
		                String url = (String)rowModel.getObject().get(index);
		                if ((url == null) || url.equals("")) {
		                    item.add(new Label(componentId));
		                    return;
		                } else if (url.equals(ReportConstants.ETL_FORMAT)) {
		                	item.add(new Label(componentId, Model.of(url)));
		                    return;
		                }

		                // dynamic url
		                String fileName = url.substring(url.lastIndexOf("/") + 1);
		                String dynamicUrl = reportService.getReportURL(fileName);
		                item.add(new SimpleLink(componentId, dynamicUrl, getString("view"), true));
		            }

		        });
			} else {
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
		}
		return columns;
	}
	
	private Link<TableResource> createSaveToExcelLink(final IModel<TableData> model) {
		ByteArrayResource download = new TableResource(excludeColumns(model.getObject()), "audit.xls");		
		ResourceLink resourceLink =  new ResourceLink<TableResource>("download", download);
		// see busy-indicator.js
		// we do not want a busy indicator in this situation
		resourceLink.add(new AttributeAppender("class", new Model<String>("noBusyIndicator"), " "));
		return resourceLink;
	}
	
	private TableData excludeColumns(TableData td) {
		if ((links == null) || links.isEmpty()) {
			return td;
		}		
		List<String> header = new ArrayList<String>();
		for (int i=0, size=td.getHeader().size(); i<size; i++) {
			if (!links.contains(i)) {
				header.add(td.getHeader().get(i));
			}
		}	
		
		List<List<Object>> data = new ArrayList<List<Object>>();
		for (int i=0, size=td.getData().size(); i<size; i++) {
			List<Object> row = td.getData().get(i);
			List<Object> newRow = new ArrayList<Object>();
			for (int j=0, cols=row.size(); j<cols; j++) {
				if (!links.contains(j)) {
					newRow.add(row.get(j));					
				}
			}
			data.add(newRow);
		}	
		TableData result = new TableData();
		result.setHeader(header);
		result.setData(data);
		return result;
	}
       
	
	
        
}
