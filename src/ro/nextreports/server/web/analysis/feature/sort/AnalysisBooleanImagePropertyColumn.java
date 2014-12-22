package ro.nextreports.server.web.analysis.feature.sort;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import ro.nextreports.server.web.common.panel.AbstractImagePanel;

public class AnalysisBooleanImagePropertyColumn<T> extends PropertyColumn<T, String> {		
    
	private static final long serialVersionUID = 1L;

	public AnalysisBooleanImagePropertyColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
		Injector.get().inject(this);
	}

	public AnalysisBooleanImagePropertyColumn(IModel<String> displayModel, String propertyExpression) {
		super(displayModel, propertyExpression);
		Injector.get().inject(this);
	}

	public void populateItem(Item<ICellPopulator<T>> item, String componentId, final IModel<T> rowModel) {
        item.add(new AbstractImagePanel(componentId) {

            @Override
            public String getImageName() {
                if ((Boolean) PropertyResolver.getValue(getPropertyExpression(), rowModel.getObject())) {                	             	
                    return "images/sortascend.png";
                } else {
                    return "images/sortdescend.png";
                }
            }
        });
    }		

    @Override
    public String getCssClass() {
        return "boolean";
    }

}

