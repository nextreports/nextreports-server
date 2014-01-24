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
package ro.nextreports.server.web.pivot;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import ro.nextreports.server.pivot.DefaultPivotModel;
import ro.nextreports.server.pivot.PivotDataSource;
import ro.nextreports.server.pivot.PivotField;
import ro.nextreports.server.pivot.PivotModel;

/**
 * @author Decebal Suiu
 */
public class PivotPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer areasContainer;
	private PivotModel pivotModel;
	private PivotTable pivotTable;
	private AjaxLink<Void> computeLink;

	public PivotPanel(String id, PivotDataSource pivotDataSource) {
		super(id);
		
		// create a pivot model
		pivotModel = createPivotModel(pivotDataSource);
		
		pivotModel.calculate();
//		System.out.println(pivotModel);
				
		areasContainer = new WebMarkupContainer("areas");
		areasContainer.setOutputMarkupId(true);
		add(areasContainer);
		
		RepeatingView areaRepeater = new RepeatingView("area");
		areasContainer.add(areaRepeater);
		List<PivotField.Area> areas = PivotField.Area.getValues();
		for (PivotField.Area area : areas) {
			areaRepeater.add(new PivotAreaPanel(areaRepeater.newChildId(), area));
		}
		
		pivotTable = createPivotTabel("pivotTable", pivotModel);
		add(pivotTable);
		
		AjaxCheckBox showGrandTotalForColumnCheckBox = new AjaxCheckBox("showGrandTotalForColumn", new PropertyModel<Boolean>(this, "pivotModel.showGrandTotalForColumn")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {			
				target.add(pivotTable);
			}
			
		};
		add(showGrandTotalForColumnCheckBox);

		AjaxCheckBox showGrandTotalForRowCheckBox = new AjaxCheckBox("showGrandTotalForRow", new PropertyModel<Boolean>(this, "pivotModel.showGrandTotalForRow")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {				
				target.add(pivotTable);
			}
			
		};
		add(showGrandTotalForRowCheckBox);

		computeLink = new AjaxLink<Void>("compute") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (!verify()) {
					return;
				}
								
				pivotModel.calculate();
				afterCompute(pivotModel, target);
				PivotTable newPivotTable = new PivotTable("pivotTable", pivotModel);
				pivotTable.replaceWith(newPivotTable);
				pivotTable = newPivotTable;
				target.add(pivotTable);				
			}

			@Override
			public boolean isEnabled() {
				return verify();
			}
			
		};
		computeLink.setOutputMarkupId(true);
		add(computeLink);
	}

	public PivotModel getPivotModel() {
		return pivotModel;
	}

	 @Override
	 public void onEvent(IEvent<?> event) {
        if (event.getPayload() instanceof AreaChangedEvent) {
        	AjaxRequestTarget target = ((AreaChangedEvent) event.getPayload()).getAjaxRequestTarget();
       	 	target.add(areasContainer);
       	 	target.add(computeLink);
        }
	}
	 
    @Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(PivotPanel.class, "pivot.js")));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(PivotPanel.class, "pivot.css")));
	}

    protected PivotModel createPivotModel(PivotDataSource pivotDataSource) {
		PivotModel pivotModel = new DefaultPivotModel(pivotDataSource);
		
		// debug
		/*
		Tree columnsHeaderTree =  pivotModel.getColumnsHeaderTree();
		System.out.println("### Columns Header Tree ###");
		TreeHelper.printTree(columnsHeaderTree.getRoot());
		TreeHelper.printLeafValues(columnsHeaderTree.getRoot());

		Tree rowsHeaderTree =  pivotModel.getRowsHeaderTree();
		System.out.println("### Rows Header Tree ### ");
		TreeHelper.printTree(rowsHeaderTree.getRoot());
		TreeHelper.printLeafValues(rowsHeaderTree.getRoot());
		*/

		return pivotModel;
    }
    
	protected PivotTable createPivotTabel(String id, PivotModel pivotModel) {
		PivotTable pivotTable = new PivotTable(id, pivotModel);
		pivotTable.setOutputMarkupPlaceholderTag(true);
		pivotTable.setVisible(false);
		
		return pivotTable;
	}
    	
	protected void afterCompute(PivotModel pivotModel, AjaxRequestTarget target) {		
	}

	private boolean verify() {
		return !pivotModel.getFields(PivotField.Area.DATA).isEmpty() && (!pivotModel.getFields(PivotField.Area.COLUMN).isEmpty() ||	
			!pivotModel.getFields(PivotField.Area.ROW).isEmpty());	
	}

}
