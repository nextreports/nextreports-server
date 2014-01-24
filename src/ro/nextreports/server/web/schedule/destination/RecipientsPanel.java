/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.schedule.destination;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;
import ro.nextreports.server.web.common.panel.AbstractImageLabelPanel;
import ro.nextreports.server.web.common.table.BaseTable;

/**
 * @author Decebal Suiu
 */
public  class RecipientsPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
	private RecipientDataProvider provider;
	private DataTable<Recipient, String> table;
	private transient List<Recipient> marked = new ArrayList<Recipient>();
	private CheckGroup<Recipient> group;

	public RecipientsPanel(String id, RecipientDataProvider provider) {
		super(id);
		
		this.provider = provider;
		
		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);
		
        List<IColumn<Recipient, String>> columns = new ArrayList<IColumn<Recipient, String>>();
        columns.add(new AbstractColumn<Recipient, String>(new Model<String>(getString("select"))) {

            public void populateItem(Item<ICellPopulator<Recipient>> item, String componentId, IModel<Recipient> rowModel) {
                try {
                    item.add(new CheckBoxPanel(componentId, rowModel, item));
                    item.add(AttributeModifier.replace("width", "30px"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public Component getHeader(String s) {
                return new CheckBoxHeaderPanel(s);
            }

        });
        columns.add(new RecipientColumn());

        group = new CheckGroup<Recipient>("groupCheck", marked);
        group.setRenderBodyOnly(false);
        table = new BaseTable<Recipient>("table", columns, provider, 300);
        group.add(table);
        table.setOutputMarkupId(true);        

		add(group);
        add(new AjaxSubmitConfirmLink<Recipient>("deleteLink", getString("ActionContributor.Run.destination.recipient.remove")) {

            public void onSubmit(AjaxRequestTarget target, Form<?> form) {                
                for (Recipient recipient : marked) {
                    RecipientsPanel.this.provider.removeRecipient(recipient);
                }
                if (marked.size() > 0) {
                    target.add(RecipientsPanel.this);
                }
            }

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				// TODO Auto-generated method stub
			}
            
        });
	}
	
	@Override
	public boolean isVisible() {
		return provider.size() != 0;
	}
	
    private class CheckBoxPanel extends Panel {

        public CheckBoxPanel(String id, IModel<Recipient> model, final Item<ICellPopulator<Recipient>> item) {
            super(id, model);
            add(new Check<Recipient>("select", model));
        }

    }

    private class CheckBoxHeaderPanel extends Panel {

        public CheckBoxHeaderPanel(String id) {
            super(id);
            
            CheckGroupSelector selector = new CheckGroupSelector("groupselector");
            group.add(selector);
            add(selector);
        }

    }
    
    private class RecipientColumn extends AbstractColumn<Recipient, String> {

        public RecipientColumn() {
            super(new Model<String>(getString("ActionContributor.Run.destination.recipient")));
        }

        public void populateItem(Item<ICellPopulator<Recipient>> item, String componentId, IModel<Recipient> rowModel) {
            final Recipient recipient = rowModel.getObject();
            final String name = recipient.getName();

            Component component = new AbstractImageLabelPanel(componentId) {

                @Override
                public String getDisplayString() {
                    return name;
                }

                @Override
                public String getImageName() {
                    if (recipient.getType() == Recipient.EMAIL_TYPE) {
                        return "images/email.png";
                    } else if (recipient.getType() == Recipient.USER_TYPE) {
                        return "images/user.png";
                    } else if (recipient.getType() == Recipient.GROUP_TYPE) {
                        return "images/group.png";
                    }
                    // TODO
                    return null; // return "blank.png"
                }

            };

            item.add(component);
        }
    }
    
}
