package ro.nextreports.server.web.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import ro.nextreports.server.etl.OrientDbUtils;
import ro.nextreports.server.etl.OrientDbUtils.ClassMetadata;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.AnalysisUtil;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.security.SecurityUtil;

public class AddAnalysisPanel extends FormContentPanel {

	private static final long serialVersionUID = 1L;

	@SpringBean
	private StorageService storageService;

	@SpringBean
	private AnalysisService analysisService;

	private String selectedTable;
	
	private static final Logger LOG = LoggerFactory.getLogger(AddAnalysisPanel.class);

	public AddAnalysisPanel() {
		super(FormPanel.CONTENT_ID);

		DropDownChoice<String> selectedTable = new DropDownChoice<String>("tableChoice", new PropertyModel<String>(this, "selectedTable"), new TablesModel());
		selectedTable.setOutputMarkupPlaceholderTag(true);
		selectedTable.setNullValid(false);		
		selectedTable.setRequired(true);
		selectedTable.setLabel(new StringResourceModel("Table", null));
 		add(selectedTable);  		
	}

	public String getSelectedTable() {
		return selectedTable;
	}

	private class TablesModel extends LoadableDetachableModel<List<String>> {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<String> load() {
			ODatabaseDocumentTx db = null;
			try {
				String prefix = SecurityUtil.getLoggedUsername() + "-";
				try {
					db = new ODatabaseDocumentTx(analysisService.getDatabasePath(), false).open("admin", "admin");
				} catch (Throwable t) {
					// critical case					
					t.printStackTrace();
					LOG.error(t.getMessage(), t);
					return new ArrayList<String>();
				}
				List<ClassMetadata> result = OrientDbUtils.getDatabaseClasses(db, prefix);								
				List<String> names = new ArrayList<String>();
				for (ClassMetadata c : result) {
					if (!c.getName().contains(AnalysisUtil.FREEZE_MARKUP)) {
						names.add(c.getName().substring(prefix.length()));
					}
				}
				return names;
			} finally {
				if (db != null) {
					db.close();
				}
			}
		}

	}

}
