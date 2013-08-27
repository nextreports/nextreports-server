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
package ro.nextreports.server.web.core.migration.tree;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.core.migration.MigrationEntityType;
import ro.nextreports.server.web.core.tree.EntityNode;
import ro.nextreports.server.web.core.tree.EntityTreeModel;

public class MigrationEntityTreeModel extends EntityTreeModel {

	private MigrationEntityType migrationType;

	public MigrationEntityTreeModel(String rootPath, MigrationEntityType widgetType) {
		super(rootPath);
		setMigrationEntityType(widgetType);
	}

	protected EntityNode createEntityNode(Entity entity) {
		return new MigrationEntityTreeNode(entity, migrationType);
	}

	private void setMigrationEntityType(MigrationEntityType migrationType) {
		if (!MigrationEntityType.isDefined(migrationType)) {
			throw new IllegalArgumentException("Invalid widget type : " + migrationType);
		}
		this.migrationType = migrationType;
	}

}
