package ro.nextreports.server.web.schedule.destination;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.CopyDestination;
import ro.nextreports.server.web.core.validation.JcrNameValidator;

public class CopyPanel extends AbstractDestinationPanel {

	private static final long serialVersionUID = 1L;

	public CopyPanel(String id, CopyDestination copyDestination) {
        super(id, copyDestination);
    }

    protected void initComponents() {    	
        add(new Label("fileName", getString("ActionContributor.Run.destination.fileName")));
        TextField<String> fileNameField = new TextField<String>("fileNameField",
                new PropertyModel<String>(destination, "fileName"));
        fileNameField.setLabel(new Model<String>(getString("ActionContributor.Run.destination.fileName")));
        fileNameField.setRequired(true);
        fileNameField.add(new JcrNameValidator());
        add(fileNameField);       
    }

}
