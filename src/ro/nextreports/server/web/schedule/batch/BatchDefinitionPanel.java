package ro.nextreports.server.web.schedule.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;

public class BatchDefinitionPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
    private StorageService storageService;   
	
	private SchedulerJob schedulerJob;    
    private DropDownChoice<String> parameterChoice;

    public BatchDefinitionPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        this.schedulerJob = schedulerJob;
        init();
    }

    private void init() {
    	Label parameter = new Label("parameter", getString("ActionContributor.Run.batch.parameter"));
        add(parameter);
                
        ro.nextreports.engine.Report report = NextUtil.getNextReport(storageService.getSettings(), schedulerJob.getReport());
        Map<String, QueryParameter> paramMap = ParameterUtil.getUsedNotHiddenParametersMap(report);
        List<String> parameters = new ArrayList<String>();
        for (QueryParameter qp : paramMap.values()) {
        	if (qp.getSelection().equals(QueryParameter.SINGLE_SELECTION) && (qp.getSource() != null)) {
        		parameters.add(qp.getName());
        	}
        }               
        
        parameterChoice = new DropDownChoice<String>("parameterChoice", 
        		new PropertyModel<String>(schedulerJob, "batchDefinition.parameter"), parameters);
        parameterChoice.setNullValid(true);
        add(parameterChoice);
        
        add(new Label("dataQuery", getString("ActionContributor.Run.batch.dataQuery")));

		TextArea<String> dataQueryArea = new TextArea<String>("dataQueryArea", new PropertyModel<String>(schedulerJob, "batchDefinition.dataQuery"));
		dataQueryArea.setLabel(new Model<String>(getString("ActionContributor.Run.batch.dataQuery")));
		add(dataQueryArea);		
		
		add(new Label("infoDynamic", getString("ActionContributor.Run.batch.dynamic")));
		add(new Label("infoDependent", getString("ActionContributor.Run.batch.dependent")));
    }


}
