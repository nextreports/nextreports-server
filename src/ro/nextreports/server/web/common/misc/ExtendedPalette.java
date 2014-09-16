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
package ro.nextreports.server.web.common.misc;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * User: mihai.panaitescu
 * Date: 11-May-2010
 * Time: 10:14:51
 */
public class ExtendedPalette<T> extends Palette<T> {

	private static final long serialVersionUID = 1L;

//    private static final String SELECTED_HEADER_ID = "selectedHeader";
//    private static final String AVAILABLE_HEADER_ID = "availableHeader";

	private final IModel<? extends Collection<? extends T>> choicesModel;
    private final IChoiceRenderer<T> choiceRenderer;
    private final int rows;

    /**
     * if reordering of selected items is allowed in
     */
    private final boolean allowOrder;

    /**
     * if add all and remove all are allowed
     */
    private final boolean allowMoveAll;


    public ExtendedPalette(String id, IModel<List<T>> model, IModel<? extends Collection<? extends T>> choicesModel, IChoiceRenderer<T> choiceRenderer,
                           int rows, boolean allowOrder) {
        this(id, model, choicesModel, choiceRenderer, rows, allowOrder, false);
    }


    /**
     * @param id             Component id
     * @param choicesModel   Model representing collection of all available choices
     * @param choiceRenderer Render used to render choices. This must use unique IDs for the objects, not the
     *                       index.
     * @param rows           Number of choices to be visible on the screen with out scrolling
     * @param allowOrder     Allow user to move selections up and down
     * @param allowMoveAll   Allow user to add or remove all items at once
     */
    public ExtendedPalette(String id, IModel<? extends Collection<? extends T>> choicesModel, IChoiceRenderer<T> choiceRenderer, int rows,
                           boolean allowOrder, boolean allowMoveAll) {
        this(id, null, choicesModel, choiceRenderer, rows, allowOrder, allowMoveAll);
    }

    /**
     * @param id             Component id
     * @param model          Model representing collection of user's selections
     * @param choicesModel   Model representing collection of all available choices
     * @param choiceRenderer Render used to render choices. This must use unique IDs for the objects, not the
     *                       index.
     * @param rows           Number of choices to be visible on the screen with out scrolling
     * @param allowOrder     Allow user to move selections up and down
     * @param allowMoveAll   Allow user to add or remove all items at once
     */
	public ExtendedPalette(String id, IModel<List<T>> model,
                           IModel<? extends Collection<? extends T>> choicesModel,
                           IChoiceRenderer<T> choiceRenderer,
                           int rows, boolean allowOrder, boolean allowMoveAll) {
        super(id, model, choicesModel, choiceRenderer, rows, allowOrder, allowMoveAll);

        this.choicesModel = choicesModel;
        this.choiceRenderer = choiceRenderer;
        this.rows = rows;
        this.allowOrder = allowOrder;
        this.allowMoveAll = allowMoveAll;
    }


    /**
     * factory method for the addAll component
     *
     * @return addAll component
     */
    protected Component newAddAllComponent() {
        return new PaletteButton("addAllButton") {
            private static final long serialVersionUID = 1L;

            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.getAttributes().put("onclick", getAddAllOnClickJS());
            }
        };
    }

    /**
     * factory method for the removeAll component
     *
     * @return removeAll component
     */
    protected Component newRemoveAllComponent() {
        return new PaletteButton("removeAllButton") {
            private static final long serialVersionUID = 1L;

            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.getAttributes().put("onclick", getRemoveAllOnClickJS());
            }
        };
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(ExtendedPalette.class, "extendedpalette.js")));
	}

	/**
     * @return addAll action javascript handler
     */
    public String getAddAllOnClickJS() {
        return buildJSCall("Wicket.Palette.addAll");
    }

    /**
     * @return removeAll action javascript handler
     */
    public String getRemoveAllOnClickJS() {
        return buildJSCall("Wicket.Palette.removeAll");
    }

    private class PaletteButton extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;

        public PaletteButton(String id) {
            super(id);
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            if (!isPaletteEnabled()) {
                tag.getAttributes().put("disabled", "disabled");
            }
        }
    }

    public Collection<? extends T> getChoices() {
        return choicesModel.getObject();
    }

    @Override
    protected void onDetach() {
        // we need to manually detach the choices model since it is not attached
        // to a common
        // an alternative might be to attach it to one of the subcomponents
        choicesModel.detach();
        super.onDetach();
    }

    public IChoiceRenderer<T> getChoiceRenderer() {
        return choiceRenderer;
    }

    public int getRows() {
        return rows;
    }

    /**
     * factory method for the remove component
     *
     * @return remove component
     */
    protected Component newRemoveComponent() {
        return new PaletteButton("removeButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.getAttributes().put("onclick", getRemoveOnClickJS());
            }
        };
    }

    /**
     * factory method for the addcomponent
     *
     * @return add component
     */
    protected Component newAddComponent() {
        return new PaletteButton("addButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.getAttributes().put("onclick", getAddOnClickJS());
            }
        };
    }


}
