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
package ro.nextreports.server.web.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.text.Collator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.common.panel.AbstractImageLabelPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.BooleanImagePropertyColumn;
import ro.nextreports.server.web.core.StackPanel;
import ro.nextreports.server.web.security.AclEntryDataProvider;
import ro.nextreports.server.web.security.GroupAclEntryPanel;
import ro.nextreports.server.web.security.UserAclEntryPanel;


/**
 * @author Decebal Suiu
 */
public class SecurityPanel extends Panel {

    private DataTable<AclEntry> table;
    private ModalWindow dialog;

    @SpringBean
    private SecurityService securityService;

    public SecurityPanel(String id, final Entity entity) {
        super(id, new Model<Entity>(entity));

        add(new Label("path", StorageUtil.getPathWithoutRoot(entity.getPath())));
        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        List<IColumn<AclEntry>> columns = new ArrayList<IColumn<AclEntry>>();
        columns.add(new NameColumn());
        columns.add(new ActionsColumn());
        columns.add(new TypeColumn());
        columns.add(new BooleanImagePropertyColumn<AclEntry>(new Model<String>(getString("Permission.read")), "read", "read"));
        columns.add(new BooleanImagePropertyColumn<AclEntry>(new Model<String>(getString("Permission.execute")), "execute", "execute"));
        columns.add(new BooleanImagePropertyColumn<AclEntry>(new Model<String>(getString("Permission.write")), "write", "write"));
        columns.add(new BooleanImagePropertyColumn<AclEntry>(new Model<String>(getString("Permission.delete")), "delete", "delete"));
        columns.add(new BooleanImagePropertyColumn<AclEntry>(new Model<String>(getString("Permission.security")), "security", "security"));

        AclEntryDataProvider dataProvider = new AclEntryDataProvider(entity.getId());
        table = new BaseTable<AclEntry>("table", columns, dataProvider, 300);
        table.setOutputMarkupId(true);
        add(table);

        dialog = new ModalWindow("dialog");
        add(dialog);

        add(new AjaxLink("addUser") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                AclEntry aclEntry = new AclEntry(AclEntry.USER_TYPE);
                List<String> notGrantedUsers;
                try {
                    notGrantedUsers = getNotGrantedUsersById(entity.getId(), aclEntry);
                } catch (Exception e) {
                    // TODO
                    e.printStackTrace();
                    error(e.getMessage());
                    return;
                }

                if (notGrantedUsers.isEmpty()) {
                    error(getString("SecurityPanel.allUsersGranted"));
                    target.add(feedbackPanel);
                    return;
                }

                dialog.setTitle(getString("SecurityPanel.addUser"));                
                dialog.setInitialWidth(300);
                dialog.setUseInitialHeight(false);
                // TODO duplication code with modify user
                dialog.setContent(new UserAclEntryPanel(dialog.getContentId(), entity, aclEntry, notGrantedUsers) {

                    @Override
                    public void onUserAclEntry(AjaxRequestTarget target, AclEntry aclEntry, boolean recursive) {
                        try {
                            ModalWindow.closeCurrent(target);
                            securityService.grantUser(entity.getPath(), aclEntry.getName(), aclEntry.getPermissions(), recursive);
                            target.add(table);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            error(e.getMessage());
                        }
                    }

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                });
                dialog.show(target);
            }

        });

        add(new AjaxLink("addGroup") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                AclEntry aclEntry = new AclEntry(AclEntry.GROUP_TYPE);
                List<String> notGrantedGroups;
                try {
                    notGrantedGroups = getNotGrantedGroups(entity.getPath(), aclEntry);
                } catch (Exception e) {
                    // TODO
                    e.printStackTrace();
                    error(e.getMessage());
                    return;
                }

                if (notGrantedGroups.isEmpty()) {
                    error(getString("SecurityPanel.allGroupsGranted"));
                    target.add(feedbackPanel);
                    return;
                }

                dialog.setTitle(getString("SecurityPanel.addGroup"));                
                dialog.setInitialWidth(300);
                dialog.setUseInitialHeight(false);
                // TODO duplication code with modify group
                dialog.setContent(new GroupAclEntryPanel(dialog.getContentId(), entity, aclEntry, notGrantedGroups) {

                    @Override
                    public void onGroupAclEntry(AjaxRequestTarget target, AclEntry aclEntry, boolean recursive) {
                        try {
                            ModalWindow.closeCurrent(target);
                            securityService.grantGroup(entity.getPath(), aclEntry.getName(), aclEntry.getPermissions(), recursive);
                            target.add(table);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            error(e.getMessage());
                        }
                    }

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                });
                dialog.show(target);
            }

        });

        add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onCancel(target);
            }
        });
    }

    protected void onCancel(AjaxRequestTarget target) {
        StackPanel panel = findParent(StackPanel.class);
        panel.backwardWorkspace(target);
    }

    private List<String> getNotGrantedUsersById(String entityId, AclEntry aclEntry) throws Exception {
        AclEntry[] entries = securityService.getGrantedUsersById(entityId);
        User[] users = securityService.getUsers();
        List<String> result = new ArrayList<String>();
        for (User user : users) {
            String name = user.getName();
            boolean found = false;
            for (AclEntry entry : entries) {
                if (entry.equals(aclEntry)) {
                    continue;
                }
                if (entry.getName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(name);
            }
        }
        Collections.sort(result, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return Collator.getInstance().compare(o1, o2);
            }
        });

        return result;
    }

    private List<String> getNotGrantedGroups(String path, AclEntry aclEntry) throws Exception {
        AclEntry[] entries = securityService.getGrantedGroups(path);
        Group[] groups = securityService.getGroups();
        List<String> result = new ArrayList<String>();
        for (Group group : groups) {
            String name = group.getName();
            boolean found = false;
            for (AclEntry entry : entries) {
                if (entry.equals(aclEntry)) {
                    continue;
                }
                if (entry.getName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(name);
            }
        }
        Collections.sort(result, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return Collator.getInstance().compare(o1, o2);
            }
        });

        return result;
    }

    class NameColumn extends AbstractColumn<AclEntry> {

        public NameColumn() {
            super(new Model<String>(getString("NameColumn.name")));
        }

        @Override
        public String getCssClass() {
            return "name-col";
        }

        public void populateItem(Item<ICellPopulator<AclEntry>> cellItem, String componentId, final IModel<AclEntry> rowModel) {
            Component component = new AbstractImageLabelPanel(componentId) {

                @Override
                public String getDisplayString() {
                    return rowModel.getObject().getName();
                }

                @Override
                public String getImageName() {
                    if (rowModel.getObject().getType() == AclEntry.USER_TYPE) {
                        return "images/user.png";
                    } else if (rowModel.getObject().getType() == AclEntry.GROUP_TYPE) {
                        return "images/group.png";
                    }

                    // TODO
                    return null; // return "blank.png"
                }

            };
            cellItem.add(component);
            cellItem.add(new SimpleAttributeModifier("class", "name-col"));
        }

    }

    class TypeColumn extends AbstractColumn<AclEntry> {

        public TypeColumn() {
            super(new Model<String>(getString("TypeColumn.name")));
        }

        public void populateItem(Item<ICellPopulator<AclEntry>> item, String componentId, IModel<AclEntry> rowModel) {
            if (rowModel.getObject().getType() == AclEntry.USER_TYPE) {
                item.add(new Label(componentId, getString("AclEntryPanel.user")));
            } else if (rowModel.getObject().getType() == AclEntry.GROUP_TYPE) {
                item.add(new Label(componentId, getString("AclEntryPanel.group")));
            }
        }

    }

    class ActionsColumn extends AbstractColumn<AclEntry> {

        public ActionsColumn() {
            super(new Model<String>(getString("ActionsColumn.name")));
        }

        @Override
        public String getCssClass() {
            return "actions-col";
        }

        public void populateItem(Item<ICellPopulator<AclEntry>> cellItem, String componentId, IModel<AclEntry> model) {
            cellItem.add(new ActionPanel(componentId, model));
            cellItem.add(new SimpleAttributeModifier("class", "actions-col"));
        }

    }

    class ActionPanel extends Panel {

        public ActionPanel(String id, final IModel<AclEntry> model) {
            super(id, model);

            setRenderBodyOnly(true);

            MenuPanel menuPanel = new MenuPanel("menuPanel");
            add(menuPanel);

            MenuItem menuItem = new MenuItem("images/actions.png", null);
            menuPanel.addMenuItem(menuItem);

            AjaxLink<AclEntry> modifyLink = new AjaxLink<AclEntry>(MenuPanel.LINK_ID, model) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    final Entity entity = (Entity) SecurityPanel.this.getDefaultModelObject();
                    AclEntry aclEntry = getModelObject();
                    List<String> notGrantedUsers = Arrays.asList(new String[]{aclEntry.getName()});

                    String title;
                    if (aclEntry.getType() == AclEntry.USER_TYPE) {
                        title = getString("SecurityPanel.modifyUser");
                    } else {
                        title = getString("SecurityPanel.modifyGroup");
                    }
                    dialog.setTitle(title);                    
                    dialog.setInitialWidth(300);
                    dialog.setUseInitialHeight(false);

                    Panel panel;
                    if (aclEntry.getType() == AclEntry.USER_TYPE) {
                        panel = new UserAclEntryPanel(dialog.getContentId(), entity, aclEntry, notGrantedUsers) {

                            @Override
                            public void onUserAclEntry(AjaxRequestTarget target, AclEntry aclEntry, boolean recursive) {
                                try {
                                    ModalWindow.closeCurrent(target);
                                    securityService.grantUser(entity.getPath(), aclEntry.getName(), aclEntry.getPermissions(), recursive);
                                    target.add(table);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    error(e.getMessage());
                                }
                            }

                            @Override
                            public void onCancel(AjaxRequestTarget target) {
                                ModalWindow.closeCurrent(target);
                            }
                        };
                    } else {
                        panel = new GroupAclEntryPanel(dialog.getContentId(), entity, aclEntry, notGrantedUsers) {

                            @Override
                            public void onGroupAclEntry(AjaxRequestTarget target, AclEntry aclEntry, boolean recursive) {
                                try {
                                    ModalWindow.closeCurrent(target);
                                    securityService.grantGroup(entity.getPath(), aclEntry.getName(), aclEntry.getPermissions(), recursive);
                                    target.add(table);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    error(e.getMessage());
                                }
                            }

                            @Override
                            public void onCancel(AjaxRequestTarget target) {
                                ModalWindow.closeCurrent(target);
                            }
                        };
                    }
                    dialog.setContent(panel);
                    dialog.show(target);
                }

            };
            menuItem.addMenuItem(new MenuItem(modifyLink, getString("SecurityPanel.modify"), "images/update.png"));

            AjaxLink<AclEntry> deleteLink = new AjaxConfirmLink<AclEntry>(MenuPanel.LINK_ID, model) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    Entity entity = (Entity) SecurityPanel.this.getDefaultModelObject();
//                    AclEntry aclEntry = (AclEntry) ActionPanel.this.getDefaultModelObject();
                    AclEntry aclEntry = getModelObject();
                    try {
                        if (aclEntry.getType() == AclEntry.USER_TYPE) {
                            securityService.revokeUser(entity.getPath(), aclEntry.getName(), aclEntry.getPermissions());
                        } else {
                            securityService.revokeGroup(entity.getPath(), aclEntry.getName(), aclEntry.getPermissions());
                        }
                        target.add(table);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        error(e.getMessage());
                    }
                }

                @Override
                public String getMessage() {
                    return new StringResourceModel("SecurityPanel.deleteAsk", this, null, new Object[] {getModelObject().getName()}).getString();                    	
                }

            };
            menuItem.addMenuItem(new MenuItem(deleteLink, getString("SecurityPanel.delete"), "images/delete.gif"));
        }

    }

}
