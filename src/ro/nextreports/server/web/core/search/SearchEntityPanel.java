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
package ro.nextreports.server.web.core.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.search.AlarmSearchEntry;
import ro.nextreports.server.search.DescriptionSearchEntry;
import ro.nextreports.server.search.DrillDownSearchEntry;
import ro.nextreports.server.search.IndicatorSearchEntry;
import ro.nextreports.server.search.InvalidSqlSearchEntry;
import ro.nextreports.server.search.NameSearchEntry;
import ro.nextreports.server.search.SearchEntry;
import ro.nextreports.server.search.SqlSearchEntry;
import ro.nextreports.server.search.TableSearchEntry;
import ro.nextreports.server.search.Tristate;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.chart.ChartSection;
import ro.nextreports.server.web.common.behavior.AlertBehavior;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.common.event.AjaxUpdateEvent;
import ro.nextreports.server.web.common.event.AjaxUpdateListener;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;
import ro.nextreports.server.web.common.panel.AbstractImageAjaxLinkPanel;
import ro.nextreports.server.web.common.panel.AbstractImageLabelPanel;
import ro.nextreports.server.web.common.table.AjaxCheckTablePanel;
import ro.nextreports.server.web.common.table.BooleanImagePropertyColumn;
import ro.nextreports.server.web.common.table.SortableDataAdapter;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.EntityListDataProvider;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.event.SelectEntityEvent;
import ro.nextreports.server.web.core.menu.EntityPopupMenuPanel;
import ro.nextreports.server.web.core.section.EntitySection;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.core.table.ActivePropertyColumn;
import ro.nextreports.server.web.core.table.CreatedByColumn;
import ro.nextreports.server.web.core.table.CreationDateColumn;
import ro.nextreports.server.web.core.table.NameColumn;
import ro.nextreports.server.web.core.table.NextRunDateColumn;
import ro.nextreports.server.web.core.table.TypeColumn;
import ro.nextreports.server.web.report.ReportSection;
import ro.nextreports.server.web.schedule.SchedulerSection;

public class SearchEntityPanel extends GenericPanel<Entity> implements AjaxUpdateListener {

	private static final long serialVersionUID = 1L;

	@SpringBean
    private SectionManager sectionManager;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private SecurityService securityService;

    private String path;

    private String nameContains;
    private String descContains;
    private Boolean caseSensitive;
    private Integer alarm = -1;
    private Integer indicator = -1;
    private Integer table = -1;
    private Integer drill = -1;
    private Integer invalidSql = -1;
    private String sqlContains;    

    private MultiLineLabel resultsLabel;
    private AjaxSubmitConfirmLink submitLink;
    private WebMarkupContainer container;
    private EntityListDataProvider provider;

    protected MenuPanel bulkMenuPanel;
    protected MenuPanel menuPanel;
    protected String sectionId;

    private FeedbackPanel feedbackPanel;

    public SearchEntityPanel(String id, final String path) {
        super(id);

        this.sectionId = ((EntitySection) sectionManager.getSelectedSection()).getId();
        this.path = path;
        SearchContext searchContext = NextServerSession.get().getSearchContext();
        if (searchContext != null) {
            this.path = searchContext.getPath();
        }
        provider = new EntityListDataProvider();

        AdvancedForm form = new SearchForm("form");

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        feedbackPanel.setEscapeModelStrings(false);

        List<SearchEntry> searchEntries = Collections.emptyList();
        if (searchContext != null) {
            searchEntries = searchContext.getSearchEntries();
        }
        if (searchEntries.size() > 0) {
            try {
                Entity[] entities = storageService.search(searchEntries, null);
                provider.setList(Arrays.asList(entities));
            } catch (Exception e) {
                e.printStackTrace();
                error(e.getMessage());
            }
        }

        add(new Label("title", getString("ActionContributor.Search.name") + " " + getString("Section." + sectionManager.getSelectedSection().getTitle() + ".name")));

        resultsLabel = new MultiLineLabel("resultsLabel", new Model() {

            @Override
            public Serializable getObject() {
                return getSearchString();
            }

        });
        resultsLabel.setOutputMarkupId(true);
        resultsLabel.setVisible(false);
        form.add(resultsLabel);

        container = new WebMarkupContainer("table-container");
        container.setOutputMarkupId(true);
        container.setVisible(false);
        form.add(container);

        final AjaxCheckTablePanel<Entity> tablePanel = createTablePanel(provider);
        container.add(tablePanel);

        form.add(feedbackPanel);

        submitLink = new AjaxSubmitConfirmLink("deleteLink", getString("deleteEntities")) {

			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (StorageUtil.isCommonPath(tablePanel.getSelected())) {
                    error(getString("deleteEntitiesAmbiguous"));
                    target.add(feedbackPanel);
                } else {
                    for (Entity h : tablePanel.getSelected()) {
                        try {
                            if (!StorageUtil.isSystemPath(h.getPath()) && 
                            		securityService.hasPermissionsById(ServerUtil.getUsername(),
                                    PermissionUtil.getDelete(), h.getId())) {
                                storageService.removeEntityById(h.getId());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            add(new AlertBehavior(e.getMessage()));
                            target.add(this);
                        }
                    }
                    if (tablePanel.getSelected().size() > 0) {
                        tablePanel.unselectAll();
                        refresh(target);
                    }
                }
            }

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);
			}

        };
        submitLink.setVisible(false);
        form.add(submitLink);
        add(form);                

        setOutputMarkupId(true);
    }

    protected AjaxCheckTablePanel<Entity> createTablePanel(EntityListDataProvider dataProvider) {
        SortableDataProvider<Entity, String> sortableDataProvider = new SortableDataAdapter<Entity>(dataProvider);
        return new AjaxCheckTablePanel<Entity>("table", createTableColumns(), sortableDataProvider, 10) {
        	
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isCheckable(IModel<Entity> rowModel) {
            	return !StorageUtil.isSystemPath(rowModel.getObject().getPath());
            }
			
        };
    }

    protected List<IColumn<Entity, String>> createTableColumns() {
        List<IColumn<Entity, String>> columns = new ArrayList<IColumn<Entity, String>>();
        columns.add(new EntityNameColumn());
        columns.add(new ActionsColumn());

        if (sectionManager.getSelectedSection() instanceof ReportSection) {
            columns.add(new TypeColumn());
        }

        //columns.add(new EntityPathColumn());

        if (sectionManager.getSelectedSection() instanceof SchedulerSection) {
            columns.add(new ActivePropertyColumn());
            columns.add(new BooleanImagePropertyColumn<Entity>(new Model<String>(getString("ActionContributor.Search.entityRun")), "isRunning", "isRunning"));
            columns.add(new NextRunDateColumn<Entity>());
        }

        columns.add(new CreatedByColumn());
        columns.add(new CreationDateColumn());
        return columns;
    }


    public void onAjaxUpdate(AjaxUpdateEvent event) {
        System.out.println("SearchEntityPanel.onAjaxUpdate()");
        System.out.println(event);
        if (event instanceof SelectEntityEvent) {
            SelectEntityEvent selectEntityEvent = (SelectEntityEvent) event;
            refresh(event.getTarget());
        }        
    }

    private void refresh(AjaxRequestTarget target) {
        Entity[] entities = new Entity[0];
        try {
            SearchContext searchContext = NextServerSession.get().getSearchContext();
            if (searchContext == null) {
                searchContext = new SearchContext();
                NextServerSession.get().setSearchContext(searchContext);
            }
            List<SearchEntry> searchEntries = new ArrayList<SearchEntry>();
            searchContext.setPath(path);
            searchContext.setSearchEntries(searchEntries);

            if (nameContains != null) {
                NameSearchEntry se = new NameSearchEntry();
                se.setFromPath(path);
                se.setName(nameContains);
                se.setIgnoredCase(!caseSensitive);
                searchEntries.add(se);                
            }

            if (descContains != null) {
                DescriptionSearchEntry se = new DescriptionSearchEntry();
                se.setFromPath(path);
                se.setDescription(descContains);
                se.setIgnoredCase(!caseSensitive);
                searchEntries.add(se);
            }
                                    
            if (ReportSection.ID.equals(sectionManager.getSelectedSectionId())) {
            	AlarmSearchEntry ae = new AlarmSearchEntry();
            	ae.setFromPath(path);
            	ae.setAlarm(Tristate.getTristate(alarm.intValue()));
            	searchEntries.add(ae);
            }
            
            if (ReportSection.ID.equals(sectionManager.getSelectedSectionId())) {
            	IndicatorSearchEntry ie = new IndicatorSearchEntry();
            	ie.setFromPath(path);
            	ie.setIndicator(Tristate.getTristate(indicator.intValue()));
            	searchEntries.add(ie);
            }
            
            if (ReportSection.ID.equals(sectionManager.getSelectedSectionId())) {
            	TableSearchEntry te = new TableSearchEntry();
            	te.setFromPath(path);
            	te.setTable(Tristate.getTristate(table.intValue()));
            	searchEntries.add(te);
            }
                      
            if (ReportSection.ID.equals(sectionManager.getSelectedSectionId()) || 
            	ChartSection.ID.equals(sectionManager.getSelectedSectionId())	) {
            	
            	if (sqlContains != null) {
                    SqlSearchEntry se = new SqlSearchEntry();
                    se.setFromPath(path);
                    se.setText(sqlContains);
                    se.setIgnoredCase(!caseSensitive);
                    searchEntries.add(se);                
                }
            	
            	DrillDownSearchEntry dde = new DrillDownSearchEntry();
            	dde.setFromPath(path);
            	dde.setDrill(Tristate.getTristate(drill.intValue()));
            	searchEntries.add(dde);
            	
            	InvalidSqlSearchEntry ise = new InvalidSqlSearchEntry();
            	ise.setFromPath(path);
            	ise.setInvalid(Tristate.getTristate(invalidSql.intValue()));
            	searchEntries.add(ise);
            }
            
            entities = storageService.search(searchEntries, null);            

            provider.setList(Arrays.asList(entities));

            resultsLabel.setVisible(true);
            container.setVisible(entities.length  > 0);
            submitLink.setVisible(entities.length  > 0);

            target.add(this);
            
        } catch (Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }
    }

    private void back(AjaxRequestTarget target) {
        //setResponsePage(HomePage.class);
        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        panel.backwardWorkspace(target);
    }

    private String getSearchString() {
        StringBuilder sb = new StringBuilder();
        sb.append(provider.getList().size()).append(" ").append(getString("ActionContributor.Search.results")).append(" : ");

        SearchContext searchContext = NextServerSession.get().getSearchContext();
        List<SearchEntry> searchEntries = Collections.emptyList();
        if (searchContext != null) {
            searchEntries = searchContext.getSearchEntries();
        }
        for (SearchEntry se : searchEntries) {
            sb.append("\n").append(se.getMessage());
        }

        return sb.toString();
    }

    private class SearchForm extends AdvancedForm {

        public SearchForm(String id) {
            super(id);
            setOutputMarkupId(true);

            add(new Label("info", StorageUtil.getPathWithoutRoot(path)));

            add(new Label("name", getString("ActionContributor.Search.nameContains")));

            final TextField<String> name = new TextField<String>("nameText", new PropertyModel<String>(this, "nameContains"));
            name.setLabel(new Model<String>(getString("ActionContributor.Search.nameContains")));
            add(name);

            Label descLabel = new Label("description", getString("ActionContributor.Search.descriptionContains"));
            descLabel.setVisible(false);
            add(descLabel);

            final TextField<String> description = new TextField<String>("descriptionText", new PropertyModel<String>(this, "descContains"));
            description.setLabel(new Model<String>(getString("ActionContributor.Search.descriptionContains")));
            description.setVisible(false);
            add(description);
            
            Label sqlLabel = new Label("sql", getString("ActionContributor.Search.sqlContains"));
            sqlLabel.setVisible(false);
            add(sqlLabel);

            final TextField<String> sql = new TextField<String>("sqlText", new PropertyModel<String>(this, "sqlContains"));
            sql.setLabel(new Model<String>(getString("ActionContributor.Search.sqlContains")));
            sql.setVisible(false);
            add(sql);
            
            Label alarmLabel = new Label("alarm", getString("Alarm"));
            alarmLabel.setVisible(false);
            add(alarmLabel);
            
            List<Integer> alarms = Arrays.asList(new Integer[] {-1, 0, 1});
            final DropDownChoice<Integer> alarmChoice = new DropDownChoice<Integer>("alarms",
                    new PropertyModel<Integer>(this, "alarm"), alarms, new ChoiceRenderer<Integer>() {
            	public Object getDisplayValue(Integer object) {
            		return Tristate.getTristate(object).getName();
            	}
            });
            alarmChoice.setRequired(true);            
            alarmChoice.setLabel(new Model<String>(getString("Alarm")));
            alarmChoice.setVisible(false);
            add(alarmChoice);
            
            Label indicatorLabel = new Label("indicator", getString("Indicator"));
            indicatorLabel.setVisible(false);
            add(indicatorLabel);
            
            List<Integer> indicators = Arrays.asList(new Integer[] {-1, 0, 1});
            final DropDownChoice<Integer> indicatorChoice = new DropDownChoice<Integer>("indicators",
                    new PropertyModel<Integer>(this, "indicator"), indicators, new ChoiceRenderer<Integer>() {
            	public Object getDisplayValue(Integer object) {
            		return Tristate.getTristate(object).getName();
            	}
            });
            indicatorChoice.setRequired(true);            
            indicatorChoice.setLabel(new Model<String>(getString("Indicator")));
            indicatorChoice.setVisible(false);
            add(indicatorChoice);
            
            Label tableLabel = new Label("table", getString("Table"));
            tableLabel.setVisible(false);
            add(tableLabel);
            
            List<Integer> tables = Arrays.asList(new Integer[] {-1, 0, 1});
            final DropDownChoice<Integer> tableChoice = new DropDownChoice<Integer>("tables",
                    new PropertyModel<Integer>(this, "table"), tables, new ChoiceRenderer<Integer>() {
            	public Object getDisplayValue(Integer object) {
            		return Tristate.getTristate(object).getName();
            	}
            });
            tableChoice.setRequired(true);            
            tableChoice.setLabel(new Model<String>(getString("Table")));
            tableChoice.setVisible(false);
            add(tableChoice);
            
            Label drillLabel = new Label("drill", getString("DrillDown"));
            drillLabel.setVisible(false);
            add(drillLabel);
            
            List<Integer> drills = Arrays.asList(new Integer[] {-1, 0, 1});
            final DropDownChoice<Integer> drillChoice = new DropDownChoice<Integer>("drills",
                    new PropertyModel<Integer>(this, "drill"), drills, new ChoiceRenderer<Integer>() {
            	public Object getDisplayValue(Integer object) {
            		return Tristate.getTristate(object).getName();
            	}
            });
            drillChoice.setRequired(true);            
            drillChoice.setLabel(new Model<String>(getString("DrillDown")));
            drillChoice.setVisible(false);
            add(drillChoice);
            
            Label invalidSqlLabel = new Label("invalidSql", getString("ActionContributor.Search.invalidSql"));
            invalidSqlLabel.setVisible(false);
            add(invalidSqlLabel);
            
            List<Integer> invalidSqls = Arrays.asList(new Integer[] {-1, 0, 1});
            final DropDownChoice<Integer> invalidSqlChoice = new DropDownChoice<Integer>("invalidSqls",
                    new PropertyModel<Integer>(this, "invalidSql"), invalidSqls, new ChoiceRenderer<Integer>() {
            	public Object getDisplayValue(Integer object) {
            		return Tristate.getTristate(object).getName();
            	}
            });
            invalidSqlChoice.setRequired(true);            
            invalidSqlChoice.setLabel(new Model<String>(getString("ActionContributor.Search.invalidSql")));
            invalidSqlChoice.setVisible(false);
            add(invalidSqlChoice);

            // for  reports section : show description, alarm, ...
            if (ReportSection.ID.equals(sectionManager.getSelectedSectionId())) {
                makeSearchComponentVisible(descLabel, true);
                makeSearchComponentVisible(description, false);
                makeSearchComponentVisible(alarmLabel, true);
                makeSearchComponentVisible(alarmChoice, false);
                makeSearchComponentVisible(indicatorLabel, true);
                makeSearchComponentVisible(indicatorChoice, false);
                makeSearchComponentVisible(tableLabel, true);
                makeSearchComponentVisible(tableChoice, false);
                makeSearchComponentVisible(drillLabel, true);
                makeSearchComponentVisible(drillChoice, false);
                makeSearchComponentVisible(invalidSqlLabel, true);
                makeSearchComponentVisible(invalidSqlChoice, false);
                makeSearchComponentVisible(sqlLabel, true);
                makeSearchComponentVisible(sql, false);              
            }
            
            // for chart section show drill-down, ...
            if (ChartSection.ID.equals(sectionManager.getSelectedSectionId())) {            	
            	makeSearchComponentVisible(drillLabel, true);
                makeSearchComponentVisible(drillChoice, false);
                makeSearchComponentVisible(invalidSqlLabel, true);
                makeSearchComponentVisible(invalidSqlChoice, false);
                makeSearchComponentVisible(sqlLabel, true);
                makeSearchComponentVisible(sql, false);                
            }

            add(new Label("ignore", getString("ActionContributor.Search.caseSensitive")));
            CheckBox chkBox = new CheckBox("chkBox", new PropertyModel<Boolean>(this, "caseSensitive"));
            add(chkBox);

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    back(target);
                }

            });

            AjaxButton searchButton = new AjaxButton("search") {

                @Override
                public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    refresh(target);
                }

                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(feedbackPanel);
                }

            };
            setDefaultButton(searchButton);
            add(searchButton);

        }

        public String getNameContains() {
            return nameContains;
        }

        public String getDescContains() {
            return descContains;
        }
        
        public String getSqlContains() {
        	return sqlContains;
        }

        public Boolean getCaseSensitive() {
            return caseSensitive;
        }
               
        public Integer getAlarm() {
        	return alarm;
        }
        
        public Integer getIndicator() {
        	return indicator;
        }
        
        public Integer getTable() {
        	return table;
        }
        
        public Integer getDrill() {
        	return drill;
        }
        
        public void setNameContains(String nameContains) {
            SearchEntityPanel.this.nameContains = nameContains;
        }

        public void setDescContains(String descContains) {
            SearchEntityPanel.this.descContains = descContains;
        }
        
        public void setSqlContains(String sqlContains) {
            SearchEntityPanel.this.sqlContains = sqlContains;
        }

        public void setCaseSensitive(Boolean caseSensitive) {
            SearchEntityPanel.this.caseSensitive = caseSensitive;
        }
        
        public void setAlarm(Integer alarm) {
        	SearchEntityPanel.this.alarm = alarm;
        }
        
        public void setIndicator(Integer indicator) {
        	SearchEntityPanel.this.indicator = indicator;
        }
        
        public void setTable(Integer table) {
        	SearchEntityPanel.this.table = table;
        }
        
        public void setDrill(Integer drill) {
        	SearchEntityPanel.this.drill = drill;
        }
        
        public Integer getInvalidSql() {
        	return invalidSql;
        }
        
        public void setInvalidSql(Integer invalidSql) {
        	SearchEntityPanel.this.invalidSql = invalidSql;
        }

    }

    private class EntityNameColumn extends PropertyColumn<Entity, String> {

        public EntityNameColumn() {
            super(new Model<String>(getString("ActionContributor.Search.entityName")), "name", "name");
        }

        public void populateItem(Item<ICellPopulator<Entity>> item, String componentId, IModel<Entity> rowModel) {
            final Entity entity = rowModel.getObject();
            final String name = entity.getName();

            Component component;
            if (StorageUtil.isFolder(entity)) {
                component = new AbstractImageAjaxLinkPanel(componentId) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectEntity(entity, target);
                    }

                    @Override
                    public String getDisplayString() {
                        return entity.getName();
                    }

                    @Override
                    public String getImageName() {
                        return NameColumn.getImage(entity);
                    }

                };
            } else {
                component = new AbstractImageLabelPanel(componentId) {
                    @Override
                    public String getDisplayString() {
                        return name;
                    }

                    @Override
                    public String getImageName() {
                        return NameColumn.getImage(entity);
                    }

                };
            }
            item.add(new SimpleTooltipBehavior(StorageUtil.getPathWithoutRoot(entity.getPath())));
            item.add(component);
        }

        private void selectEntity(Entity entity, AjaxRequestTarget target) {
            sectionManager.setSelectedSectionId(ReportSection.ID);
            SectionContextUtil.setCurrentPath(ReportSection.ID, entity.getPath());
            setResponsePage(HomePage.class);
        }
        
    }

    private class EntityPathColumn extends AbstractColumn<Entity, String> {

		private static final long serialVersionUID = 1L;

		public EntityPathColumn() {
            super(new Model<String>(getString("ActionContributor.Search.entityPath")));
        }

        public void populateItem(Item<ICellPopulator<Entity>> item, String componentId, IModel<Entity> rowModel) {
            Entity entity = rowModel.getObject();
            String path = entity.getPath();
            item.add(new Label(componentId, path));
        }
    }

    private class ActionsColumn extends AbstractColumn<Entity, String> {

        private static final long serialVersionUID = 1L;

        public ActionsColumn() {
            super(new Model<String>(getString("ActionContributor.Search.entityActions")));
        }

        @Override
        public String getCssClass() {
            return "actions-col";
        }

        public void populateItem(Item<ICellPopulator<Entity>> cellItem, String componentId, IModel<Entity> model) {
            cellItem.add(new ActionPanel(componentId, model));
            cellItem.add(AttributeModifier.replace("class", "actions-col"));
        }

    }

    private class ActionPanel extends Panel {

        private static final long serialVersionUID = 1L;

        public ActionPanel(String id, final IModel<Entity> model) {
            super(id, model);
            setRenderBodyOnly(true);
            add(new EntityPopupMenuPanel("menuPanel", model, sectionId));
        }

    }
    
    private void makeSearchComponentVisible(Component component, boolean isRowLabel) {
    	component.setVisible(true);
    	if (isRowLabel) {
    		component.add(AttributeModifier.replace("class", "row-label row-bottom"));
    	} else {
    		component.add(AttributeModifier.replace("class", "row-bottom"));
    	}
    }

}
