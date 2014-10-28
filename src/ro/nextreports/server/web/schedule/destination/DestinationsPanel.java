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
import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.DestinationType;
import ro.nextreports.server.domain.CopyDestination;
import ro.nextreports.server.domain.FtpDestination;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SftpDestination;
import ro.nextreports.server.domain.SmbDestination;
import ro.nextreports.server.domain.SmtpAlertDestination;
import ro.nextreports.server.domain.SmtpDestination;
import ro.nextreports.server.domain.WebdavDestination;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.table.BaseTable;

/**
 * User: mihai.panaitescu
 * Date: 23-Sep-2010
 * Time: 17:49:18
 */
public class DestinationsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private SchedulerJob schedulerJob;
    private DataTable<Destination, String> table;
    private DestinationsDataProvider dataProvider;
    private WebMarkupContainer container;
    private DropDownChoice<String> typeChoice;

    public DestinationsPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        this.schedulerJob = schedulerJob;
        init();
    }

    private void init() {
        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        addTable();
        addDestinationType();

        container.add(new EmptyPanel("destinationPanel"));
    }

    private void addTable() {
        List<IColumn<Destination, String>> columns = new ArrayList<IColumn<Destination, String>>();
        columns.add(new AbstractColumn<Destination, String>(new Model<String>(getString("type"))) {

			private static final long serialVersionUID = 1L;

			@Override
            public String getCssClass() {
                return "name-col";
            }

            public void populateItem(Item<ICellPopulator<Destination>> item, String componentId,
                                     final IModel<Destination> rowModel) {
                final Destination destination = rowModel.getObject();
                item.add(new Label(componentId, new Model<String>(destination.getType())));
                item.add(AttributeModifier.replace("class", "name-col"));
            }
            
        });

        columns.add(new ActionsColumn());

        columns.add(new AbstractColumn<Destination, String>(new Model<String>(getString("name"))) {

			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<Destination>> item, String componentId,
                                     final IModel<Destination> rowModel) {
                final Destination destination = rowModel.getObject();
                item.add(new Label(componentId, new Model<String>(destination.getName())));
            }
			
        });

        dataProvider = new DestinationsDataProvider(schedulerJob);
        table = new BaseTable<Destination>("table", columns, dataProvider, 300);
        table.setOutputMarkupId(true);
        add(table);
    }

    private void addDestinationType() {
        Label type = new Label("type", getString("ActionContributor.Run.destination.type"));
        add(type);

        List<String> types = new ArrayList<String>();
        boolean supportsAlert = schedulerJob.getReport().isAlarmType() || schedulerJob.getReport().isIndicatorType() || schedulerJob.getReport().isDisplayType();
        if (supportsAlert) {
        	types.add(DestinationType.ALERT.toString());
        } else {
        	for (DestinationType dt : DestinationType.values()) {
        		if (!DestinationType.ALERT.toString().equals(dt.toString())) {
        			types.add(dt.toString());
        		}
        	}
        }
        typeChoice = new DropDownChoice<String>("typeChoice", new Model<String>(), types) {
        	
			private static final long serialVersionUID = 1L;

			@Override
			protected CharSequence getDefaultChoice(String selectedValue) {
                return "<option value=\"\">Choose one</option>";
			}
            
        };
        add(typeChoice);
        typeChoice.setNullValid(true);
        typeChoice.setOutputMarkupId(true);
        typeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        	
			private static final long serialVersionUID = 1L;

			protected void onUpdate(AjaxRequestTarget target) {
                showNewDestination(target);
            }
            
        });
    }

    private class ActionsColumn extends AbstractColumn<Destination, String> {

		private static final long serialVersionUID = 1L;

		public ActionsColumn() {
            super(new Model<String>(getString("actions")));
        }

        @Override
        public String getCssClass() {
            return "actions-col";
        }

        public void populateItem(Item<ICellPopulator<Destination>> cellItem, String componentId, IModel<Destination> model) {
            cellItem.add(new ActionPanel(componentId, model));
            cellItem.add(AttributeModifier.replace("class", "actions-col"));
        }

    }

    private class ActionPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public ActionPanel(String id, final IModel<Destination> model) {
            super(id, model);

            setRenderBodyOnly(true);

            MenuPanel menuPanel = new MenuPanel("menuPanel");
            add(menuPanel);

            MenuItem mi = new MenuItem("images/actions.png", null);
            menuPanel.addMenuItem(mi);

            AjaxLink<Destination> editLink = new AjaxLink<Destination>(MenuPanel.LINK_ID, model) {

				private static final long serialVersionUID = 1L;

				@Override
                public void onClick(AjaxRequestTarget target) {
                    Destination destination = model.getObject();
                    showEditDestination(destination, target);                    
                }
				
            };
            mi.addMenuItem(new MenuItem(editLink, "Edit", "images/edit.png"));

            AjaxLink<Destination> deleteLink = new AjaxLink<Destination>(MenuPanel.LINK_ID, model) {

				private static final long serialVersionUID = 1L;

				@Override
                public void onClick(AjaxRequestTarget target) {
                    Destination destination = model.getObject();
                    schedulerJob.getDestinations().remove(destination);
                    clearContainer(target);
                    target.add(table);
                }
				
            };
            mi.addMenuItem(new MenuItem(deleteLink, "Delete", "images/delete.gif"));
        }
		
    }

    public List<Destination> getDestinations() {
        return dataProvider.getDestinations();
    }

    private void addDestination(Destination destination, AjaxRequestTarget target) {
        List<Destination> destinations = DestinationsPanel.this.schedulerJob.getDestinations();
        destinations.add(destination);
        clearContainer(target);
        target.add(table);
    }

    private void editDestination(AjaxRequestTarget target) {
        typeChoice.setModelObject(null);
        container.replace(new EmptyPanel("destinationPanel"));
        target.add(typeChoice);
        target.add(table);
        target.add(container);
    }

    private void clearContainer(AjaxRequestTarget target) {
        typeChoice.setModelObject(null);
        container.replace(new EmptyPanel("destinationPanel"));
        target.add(typeChoice);
        target.add(container);
    }

    private void showNewDestination(AjaxRequestTarget target) {
        String type = typeChoice.getModelObject();
        if (DestinationType.SMTP.toString().equals(type)) {
            final SmtpDestination destination = new SmtpDestination();
            setTemporaryDestinationPath(destination);
            MailPanel mailPanel = new MailPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    if (provider.size() == 0) {
                    	getForm().error("Mail destination must contain at least a mail address.");
                    	target.add(getFeedbackPanel());
                    } else {
                        super.onSave(target);
                        addDestination(destination, target);
                    }
                }

				protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", mailPanel, destination));
        } else if (DestinationType.ALERT.toString().equals(type)) {
            final SmtpAlertDestination destination = new SmtpAlertDestination();
            destination.setOperator("<");
            setTemporaryDestinationPath(destination);
            MailAlertPanel mailPanel = new MailAlertPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    if (provider.size() == 0) {
                    	getForm().error("Mail destination must contain at least a mail address.");
                    	target.add(getFeedbackPanel());
                    } else {
                        super.onSave(target);
                        addDestination(destination, target);
                    }
                }

				protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", mailPanel, destination));    
        } else if (DestinationType.FTP.toString().equals(type)) {
            final FtpDestination destination = new FtpDestination();
            setTemporaryDestinationPath(destination);
            FtpPanel ftpPanel = new FtpPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    addDestination(destination, target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", ftpPanel, destination));
        } else if (DestinationType.SFTP.toString().equals(type)) {
            final SftpDestination destination = new SftpDestination();
            setTemporaryDestinationPath(destination);
            SftpPanel sftpPanel = new SftpPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    addDestination(destination, target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", sftpPanel, destination));
        } else if (DestinationType.Samba.toString().equals(type)) {
            final SmbDestination destination = new SmbDestination();
            setTemporaryDestinationPath(destination);
            SmbPanel smbPanel = new SmbPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    addDestination(destination, target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", smbPanel, destination));
        } else if (DestinationType.WebDAV.toString().equals(type)) {
            final WebdavDestination destination = new WebdavDestination();
            setTemporaryDestinationPath(destination);
            WebdavPanel webdavPanel = new WebdavPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    addDestination(destination, target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", webdavPanel, destination));
        } else if (DestinationType.COPY.toString().equals(type)) {
            final CopyDestination destination = new CopyDestination();
            setTemporaryDestinationPath(destination);
            CopyPanel copyPanel = new CopyPanel(FormPanel.CONTENT_ID, destination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    addDestination(destination, target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            container.replace(new DestinationFormPanel("destinationPanel", copyPanel, destination));                        
        } else {
            container.replace(new EmptyPanel("destinationPanel"));
        }
        target.add(container);
    }

    private void showEditDestination(Destination destination, AjaxRequestTarget target) {    	
        if (DestinationType.SMTP.toString().equals(destination.getType())) {
            final SmtpDestination smtpDestination = (SmtpDestination) destination;
            MailPanel mailPanel = new MailPanel(FormPanel.CONTENT_ID, smtpDestination) {
            	
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    editDestination(target);
                }

				@Override
                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", mailPanel, destination));
        } else if (DestinationType.ALERT.toString().equals(destination.getType())) {
            final SmtpAlertDestination smtpDestination = (SmtpAlertDestination) destination;
            MailAlertPanel mailPanel = new MailAlertPanel(FormPanel.CONTENT_ID, smtpDestination) {
            	
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    editDestination(target);
                }

				@Override
                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", mailPanel, destination));    
        } else if (DestinationType.FTP.toString().equals(destination.getType())) {
            final FtpDestination ftpDestination = (FtpDestination) destination;
            FtpPanel ftpPanel = new FtpPanel(FormPanel.CONTENT_ID, ftpDestination) {
            	
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    editDestination(target);
                }

				@Override
                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", ftpPanel, destination));
        } else if (DestinationType.SFTP.toString().equals(destination.getType())) {
            final SftpDestination sftpDestination = (SftpDestination) destination;
            SftpPanel sftpPanel = new SftpPanel(FormPanel.CONTENT_ID, sftpDestination) {
            	
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    editDestination(target);
                }

				@Override
                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", sftpPanel, destination));
        } else if (DestinationType.Samba.toString().equals(destination.getType())) {
            final SmbDestination smbDestination = (SmbDestination) destination;
            SmbPanel smbPanel = new SmbPanel(FormPanel.CONTENT_ID, smbDestination) {
            	
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);
                    editDestination(target);
                }

				@Override
                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", smbPanel, destination));
        } else if (DestinationType.WebDAV.toString().equals(destination.getType())) {
            final WebdavDestination webdavDestination = (WebdavDestination) destination;
            WebdavPanel webdavPanel = new WebdavPanel(FormPanel.CONTENT_ID, webdavDestination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);                    
                    editDestination(target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", webdavPanel, destination));            
        } else if (DestinationType.COPY.toString().equals(destination.getType())) {
            final CopyDestination copyDestination = (CopyDestination) destination;
            CopyPanel copyPanel = new CopyPanel(FormPanel.CONTENT_ID, copyDestination) {
            	
				private static final long serialVersionUID = 1L;

				protected void onSave(AjaxRequestTarget target) {
                    super.onSave(target);                    
                    editDestination(target);
                }

                protected void onClose(AjaxRequestTarget target) {
                    clearContainer(target);
                }
                
            };
            typeChoice.setModelObject(destination.getType());
            container.replace(new DestinationFormPanel("destinationPanel", copyPanel, destination));            
        }
        target.add(typeChoice);
        target.add(container);
    }

    // Use this method to have a temporary path (to know which destination is editted before save)
    // destinations real path are set in ScheduleWizard.onFinish method 
    private void setTemporaryDestinationPath(Destination destination) {
        destination.setPath(UUID.randomUUID().toString());                   
    }

}
