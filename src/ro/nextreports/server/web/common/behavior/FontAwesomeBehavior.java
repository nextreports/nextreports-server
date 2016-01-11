package ro.nextreports.server.web.common.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

/**
 * @author Decebal Suiu
 */
public class FontAwesomeBehavior extends Behavior {

    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        response.render(CssReferenceHeaderItem.forUrl("css/font-awesome.css"));
    }

}
