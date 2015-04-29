package ro.nextreports.server.web.datasource.validator;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.util.lang.Classes;


public class MinMaxPoolSizeValidator extends AbstractFormValidator {

	private final FormComponent[] components;

	public MinMaxPoolSizeValidator(FormComponent formComponent1, FormComponent formComponent2) {
        if (formComponent1 == null) {
			throw new IllegalArgumentException("argument formComponent1 cannot be null");
		}
		if (formComponent2 == null) {
			throw new IllegalArgumentException("argument formComponent2 cannot be null");
		}
		components = new FormComponent[] { formComponent1, formComponent2 };
	}


	public FormComponent[] getDependentFormComponents() {
		return components;
	}


	public void validate(Form form) {
		// we have a choice to validate the type converted values or the raw
		// input values, we validate the raw input
		final FormComponent formComponent1 = components[0];
		final FormComponent formComponent2 = components[1];

        String s1 = formComponent1.getInput();
        String s2 = formComponent2.getInput();

        // disabled components
        if ( (s1 == null) || s1.trim().equals("") || (s2 == null) || s2.trim().equals("")) {
        	error(formComponent2, resourceKey() + "." + "notnull"); 
        }  else  {
        	Integer i1 = Integer.parseInt(s1);
        	Integer i2 = Integer.parseInt(s2);
        	if (i1.intValue() > i2.intValue()) {
        		error(formComponent2, resourceKey() + "." + "invalid");
        	}           
        }
	}

    @Override
    protected String resourceKey() {
        return Classes.simpleName(MinMaxPoolSizeValidator.class);
    }

}
