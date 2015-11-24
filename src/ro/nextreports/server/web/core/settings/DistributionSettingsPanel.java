package ro.nextreports.server.web.core.settings;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.core.validation.JcrNameValidator;
import ro.nextreports.server.web.core.validation.MailServerValidator;

public class DistributionSettingsPanel extends AbstractSettingsPanel {
	
	@SpringBean
	private StorageService storageService;
	
	private String oldMailIp;
	private Integer oldMailPort;

	public DistributionSettingsPanel(String id) {
		super(id);
	}

	@Override
	protected void addComponents(Form<Settings> form) {

		final TextField<String> mailServerIpField = new TextField<String>("mailServer.ip");
        form.add(mailServerIpField);
        final TextField<Integer> mailServerPortField = new TextField<Integer>("mailServer.port");
        form.add(mailServerPortField);
        final TextField<String> mailServerSenderField = new TextField<String>("mailServer.from");
        form.add(mailServerSenderField);
        final TextField<String> mailServerUsernameField = new TextField<String>("mailServer.username");
        form.add(mailServerUsernameField);
        final PasswordTextField mailServerPasswordField = new PasswordTextField("mailServer.password");
        mailServerPasswordField.setResetPassword(false);
        mailServerPasswordField.setRequired(false);
        form.add(mailServerPasswordField);
        final CheckBox tlsCheckField = new CheckBox("mailServer.enableTls");
        form.add(tlsCheckField);        
        form.add(new MailServerValidator(new FormComponent[] {mailServerIpField, mailServerPortField, mailServerSenderField}));
        
        final TextField<String> distributorDatePatternField = new TextField<String>("distributor.datePattern");
        distributorDatePatternField.add(new JcrNameValidator());
        form.add(distributorDatePatternField);
        final TextField<String> distributorTimePatternField = new TextField<String>("distributor.timePattern");
        distributorTimePatternField.add(new JcrNameValidator());
        form.add(distributorTimePatternField);
        
        Settings settings = storageService.getSettings();
        oldMailPort = settings.getMailServer().getPort();
        oldMailIp = settings.getMailServer().getIp();
        
	}

	protected void afterChange(Form form, AjaxRequestTarget target) {
		Settings settings = (Settings) form.getModelObject();
		JavaMailSenderImpl mailSender = (JavaMailSenderImpl) NextServerApplication.get().getSpringBean("mailSender");
    		
		if (!oldMailIp.equals(settings.getMailServer().getIp()) || 
	    	!oldMailPort.equals(settings.getMailServer().getPort())) {    		
	            mailSender.setHost(settings.getMailServer().getIp());
	            mailSender.setPort(settings.getMailServer().getPort());
	    }
    	    	
   		mailSender.setPassword(settings.getMailServer().getPassword());
   		mailSender.setUsername(settings.getMailServer().getUsername());
		mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", settings.getMailServer().getEnableTls());
	}
}
