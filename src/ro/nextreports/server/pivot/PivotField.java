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
package ro.nextreports.server.pivot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.convert.IConverter;

/**
 * @author Decebal Suiu
 */
public class PivotField implements Serializable, Comparable<PivotField> {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String title;
	private Area area;
	private int index;
	private int areaIndex;
	private Class<?> type;
	private Aggregator aggregator;
	private IConverter<?> converter;
	
	public PivotField(String name, int index) {
		this.name = name;
		this.index = index;
		
		aggregator = Aggregator.get(Aggregator.SUM);
	}
	
	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public String getTitle() {
		return title;
	}

	public PivotField setTitle(String title) {
		this.title = title;
		return this;
	}

	public Area getArea() {
		return area;
	}

	public PivotField setArea(Area area) {
		this.area = area;
		return this;
	}

	public int getAreaIndex() {
		return areaIndex;
	}

	public PivotField setAreaIndex(int areaIndex) {
		this.areaIndex = areaIndex;
		return this;
	}

	public Class<?> getType() {
		return type;
	}

	public PivotField setType(Class<?> valueType) {
		this.type = valueType;
		return this;
	}

	public Aggregator getAggregator() {
		return aggregator;
	}

	public void setAggregator(Aggregator aggregator) {
		this.aggregator = aggregator;
	}

	public boolean isNumber() {
		return Number.class.isAssignableFrom(type);
	}
	
	public IConverter<?> getConverter() {
		return converter;
	}

	public void setConverter(IConverter<?> converter) {
		this.converter = converter;
	}

	@Override
	public int compareTo(PivotField o) {
		return areaIndex - o.areaIndex;
	}

	@Override
	public String toString() {
		return "PivotField [name=" + name + ", title=" + title + ", area="
				+ area + ", index=" + index + ", areaIndex=" + areaIndex + "]";
	}

	/**
	 *Area of pivot field, which indicates whether the field is a column field, row field, or data field.
	 */
	public enum Area {
	
		ROW("row"),
		COLUMN("column"),
		DATA("data"),
		UNUSED("unused");
		
		private String name;
		
		private Area(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		public static List<Area> getValues() {
			List<Area> values = new ArrayList<Area>();
			values.add(ROW);
			values.add(COLUMN);
			values.add(DATA);
			values.add(UNUSED);
			
			return values;
		}
		
		public static Area getValue(String name) {
			for (Area area : getValues()) {
				if (area.name.equals(name)) {
					return area;
				}
			}
			
			return null;
		}
		
	}
	
}
