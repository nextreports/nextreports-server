package ro.nextreports.server.web.core.audit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.analysis.AnalysisSection;
import ro.nextreports.server.web.chart.ChartSection;
import ro.nextreports.server.web.common.misc.LabelLink;
import ro.nextreports.server.web.common.misc.SimpleLink;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.audit.rights.AuditRights;
import ro.nextreports.server.web.core.audit.rights.RightsPanel;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.dashboard.DashboardSection;
import ro.nextreports.server.web.dashboard.table.TableResource;
import ro.nextreports.server.web.datasource.DataSourceSection;
import ro.nextreports.server.web.report.ReportSection;
import ro.nextreports.server.web.schedule.SchedulerSection;


public class AuditTableRendererPanel extends GenericPanel<TableData> {
	
	@SpringBean
	private ReportService reportService;
	
	@SpringBean
	private StorageService storageService;
	
	@SpringBean
    private SectionManager sectionManager;
		
	private static final Logger LOG = LoggerFactory.getLogger(AuditTableRendererPanel.class);	
	
	private static DateFormat DATE_FORMAT= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	private List<Integer> links;
	private String type;
		
	public AuditTableRendererPanel(String id, String type, IModel<TableData> model, IModel<List<Integer>> linksModel, IModel<String> title) {
		super(id, model);		
		this.type = type;
		links = linksModel.getObject();
		
		add(new Label("title", title.getObject()));
		
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
		                
		                if (InnerReport.RUN.toString().equals(type)) {
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
		                } else  if (InnerReport.RIGHTS.toString().equals(type)) {
		                	
		                	LabelLink link = new LabelLink(componentId, Model.of(""), Model.of(getString("WidgetPopupMenu.gotoEntity"))) {

		            			private static final long serialVersionUID = 1L;

		            			@Override
		            			public void onClick() {
		            				
		            				String sectionId = getSectionId();
		            				String entityPath = (String)rowModel.getObject().get(RightsPanel.PATH_COLUMN);
		            				try {
		            					Entity entity = storageService.getEntity(entityPath);					
		            					sectionManager.setSelectedSectionId(sectionId);
		            					SectionContextUtil.setCurrentPath(sectionId, StorageUtil.getParentPath(entity.getPath()));
		            					SectionContextUtil.setSelectedEntityPath(sectionId,	entity.getPath());
		            					setResponsePage(HomePage.class);
		            				} catch (Exception e) {
		            					e.printStackTrace();
		            				}
		            			}		            			
		            			
		            			private String getSectionId() {
		            				String category = (String)rowModel.getObject().get(RightsPanel.CATEGORY_COLUMN);		            				
		            				String sectionId = null;			            			
		            				if (category.equals(getString("Section.Audit.Entity." + AuditRights.ENTITY_DASHBOARDS))) {
		            					sectionId = DashboardSection.ID;
		            				} else if (category.equals(getString("Section.Audit.Entity." + AuditRights.ENTITY_REPORTS))) {
		            					sectionId = ReportSection.ID;
		            				} else if (category.equals(getString("Section.Audit.Entity." + AuditRights.ENTITY_CHARTS))) {
		            					sectionId = ChartSection.ID;
		            				} else if (category.equals(getString("Section.Audit.Entity." + AuditRights.ENTITY_DATA_SOURCES))) {
		            					sectionId = DataSourceSection.ID;
		            				} else if (category.equals(getString("Section.Audit.Entity." + AuditRights.ENTITY_SCHEDULERS))) {
		            					sectionId = SchedulerSection.ID;
		            				} else if (category.equals(getString("Section.Audit.Entity." + AuditRights.ENTITY_ANALYSIS))) {
		            					sectionId = AnalysisSection.ID;
		            				} 		            				
		            				return sectionId;
		            			}
		            		};
		            		item.add(link);

		                }
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");
		String fileName = "audit-" +  type.toLowerCase() + "-" + sdf.format(new Date()) + ".xls";
		ByteArrayResource download = new TableResource(excludeColumns(model.getObject()), fileName);		
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
		td.setExcludedColumns(links);
		return td;
	}
       
	
	
        
}
