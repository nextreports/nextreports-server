package ro.nextreports.server.web.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.web.analysis.util.AnalysisException;

public class AnalysisDataProvider extends SortableDataProvider<AnalysisRow, String> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(AnalysisDataProvider.class);

	private Analysis analysis;	

	@SpringBean
	private AnalysisReader analysisReader;

	
	public AnalysisDataProvider(IModel<Analysis> model) {
		this.analysis = model.getObject();
		if ((analysis != null) && (analysis.getSortProperty() != null) && !analysis.getSortProperty().isEmpty()) {
			SortOrder order = analysis.getAscending().get(0) ? SortOrder.ASCENDING : SortOrder.DESCENDING;
			setSort(analysis.getSortProperty().get(0), order);
		}		
		Injector.get().inject(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<AnalysisRow> iterator(long first, long count) {
		try {			
			SortParam<String> sort = getSort();					
			String property = null;
			boolean asc = true;
			if (sort != null) {
				property = sort.getProperty();
				asc = sort.isAscending();
			}												
			
			// sort from UI, we use that column as first to sort, and keep all others sort columns
			if (analysis.getSortProperty() != null) {
				List<String> sortProperty = new LinkedList<String>(analysis.getSortProperty());				
				List<Boolean> ascList = new LinkedList<Boolean>(analysis.getAscending());
				if ((sortProperty != null) && !sortProperty.isEmpty()) {
					int index = sortProperty.indexOf(property);
					if (index != -1) {
						sortProperty.remove(index);
						ascList.remove(index);
					}
					// if sort property was deleted/edited we do not add it again!
					// and we set the column sort to first sort from sortProperty list
					if (!analysis.isFirstSortRemoved()) {
						if (property != null) {
							sortProperty.add(0, property);
							if (analysis.isChangeFirstSortOrder()) {
								asc = analysis.getAscending().get(0);
								analysis.setChangeFirstSortOrder(false);
								SortOrder order = analysis.getAscending().get(0) ? SortOrder.ASCENDING : SortOrder.DESCENDING;
								setSort(analysis.getSortProperty().get(0), order);
							}
							ascList.add(0, asc);
						} else {
							SortOrder order = analysis.getAscending().get(0) ? SortOrder.ASCENDING : SortOrder.DESCENDING;
							setSort(analysis.getSortProperty().get(0), order);
						}
					} else {
						analysis.setFirstSortRemoved(false);
						SortOrder order = analysis.getAscending().get(0) ? SortOrder.ASCENDING : SortOrder.DESCENDING;
						setSort(analysis.getSortProperty().get(0), order);
					}
				}

				analysis.setAscending(ascList);
				analysis.setSortProperty(sortProperty);
			}
			return analysisReader.iterator(analysis, first, count);
		} catch (AnalysisException e) {
			LOG.error(e.getMessage(), e);
			Session.get().error(e.getMessage());
			return IteratorUtils.EMPTY_ITERATOR;
		}
	}

	@Override
	public IModel<AnalysisRow> model(AnalysisRow object) {
		return new Model<AnalysisRow>(object);
	}

	@Override
	public long size() {
		try {
			return analysisReader.getRowCount(analysis);
		} catch (AnalysisException ex) {
			LOG.error(ex.getMessage(), ex);
			Session.get().error(ex.getMessage());
			return 0;
		}
	}
	
	public List<String> getHeader() {		
		List<String> result = analysisReader.getHeader(analysis);
		if (result.isEmpty()) {
			return result;
		}
		SortParam<String> sort = getSort();							
		if (sort == null) {			
			setSort(result.get(0), SortOrder.ASCENDING);
			List<String> sortProperty = new ArrayList<String>();
			sortProperty.add(result.get(0));
			analysis.setSortProperty(sortProperty);
			List<Boolean> asc = new ArrayList<Boolean>();
			asc.add(true);
			analysis.setAscending(asc);
		}
		return result;
	}
	
	public int getRowsPerPage() {
		if (analysis == null) {
			return 0;
		}
		int rows = analysis.getRowsPerPage();
		if (rows == 0) {
			rows = Integer.MAX_VALUE;
		}
		return rows;
	}
	
	public boolean isEmpty() {
		return (analysis == null);
	}

	@Override
	public void detach() {		
	}	
	
	public void reset() {
		analysisReader.reset();
	}
		

}
