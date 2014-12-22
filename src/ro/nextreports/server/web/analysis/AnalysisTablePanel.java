package ro.nextreports.server.web.analysis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.IModel;

import ro.nextreports.engine.exporter.util.StyleFormatConstants;
import ro.nextreports.engine.util.HtmlUtil;
import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.common.table.BaseTable;

public class AnalysisTablePanel extends GenericPanel<Analysis> {
	
	private AnalysisDataProvider dataProvider;
	
	public AnalysisTablePanel(String id, AnalysisDataProvider dataProvider) {
		super(id);
		this.dataProvider = dataProvider;
		add(createTable(dataProvider));
	}

	private BaseTable createTable(AnalysisDataProvider dataProvider ) {    		
		BaseTable<AnalysisRow> table = new BaseTable<AnalysisRow>("table", getPropertyColumns(dataProvider.getHeader()), dataProvider, dataProvider.getRowsPerPage());		
		table.setOutputMarkupId(true);
        return table;
    }
	
	private List<IColumn<AnalysisRow, String>> getPropertyColumns(List<String> header) {
		List<IColumn<AnalysisRow, String>> columns = new ArrayList<IColumn<AnalysisRow, String>>();
		int columnCount = header.size();		
		
		for (int i = 0; i < columnCount; i++) {
			final int j = i;
			columns.add(new PropertyColumn<AnalysisRow, String>(new Model<String>(header.get(i)), header.get(i), "cellValues." + i) {
				public void populateItem(Item cellItem, String componentId, IModel rowModel) {
					setCellStyle(cellItem, rowModel, j);
					super.populateItem(cellItem, componentId, rowModel);
				}
			});
		}		
		return columns;
    }    
	
	private void setCellStyle(Item cellItem, IModel rowModel, int rowIndex) {
    	List<Map<String, Object>> styles = ((AnalysisRow)rowModel.getObject()).getStyles();
		if (styles.size() > rowIndex) {
			Map<String, Object> style = styles.get(rowIndex);
			Color val = (Color) style.get(StyleFormatConstants.FONT_COLOR);
			if (val != null) {
				String text = HtmlUtil.getCssCode(null, style, false);
				cellItem.add(new AttributeAppender("style", Model.of(text)));
			}
		}
    }

}
