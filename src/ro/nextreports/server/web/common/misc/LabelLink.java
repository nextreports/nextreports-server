package ro.nextreports.server.web.common.misc;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

public abstract class LabelLink extends Link {
	private IModel labelModel;

	public LabelLink(String id, IModel linkModel, IModel labelModel) {
		super(id, linkModel);
		this.labelModel = labelModel;
		add(AttributeModifier.replace("class", "link"));
	}

    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		replaceComponentTagBody(markupStream, openTag, labelModel.getObject()
				.toString());
	}
}
