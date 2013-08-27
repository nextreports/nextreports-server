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
package ro.nextreports.server.domain;

import java.util.ArrayList;
import java.util.List;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.engine.util.TimeShortcutType;

public class ShortcutType extends EntityFragment {
	
	private static final long serialVersionUID = 1L;
	
	public static ShortcutType NONE = new ShortcutType(TimeShortcutType.NONE);

	@JcrProperty
	private int type;
	
	@JcrProperty
	private int timeType;
	
	@JcrProperty
	private int timeUnits;
	
	public ShortcutType() {
    	super();    	
    	setName("shortcutType");    	
	}
	
	public ShortcutType(TimeShortcutType timeShortcutType) {
    	super();    	
    	setName("shortcutType");
    	type = timeShortcutType.getType();
    	timeType = timeShortcutType.getTimeType();
    	timeUnits = timeShortcutType.getTimeUnits();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTimeType() {
		return timeType;
	}

	public void setTimeType(int timeType) {
		this.timeType = timeType;
	}

	public int getTimeUnits() {
		return timeUnits;
	}

	public void setTimeUnits(int timeUnits) {
		this.timeUnits = timeUnits;
	}
	
	public TimeShortcutType getTimeShortcutType() {
		if (type == -1) {
			return new TimeShortcutType(timeType, timeUnits);
		} else {
			return new TimeShortcutType(type);
		}
	}
	
	public static String getName(int type) {
		return TimeShortcutType.getName(type);
	}
	
	public static List<ShortcutType> getTypes() {
		List<ShortcutType> result = new ArrayList<ShortcutType>();
		for (TimeShortcutType tt : TimeShortcutType.getTypes()) {
			result.add(new ShortcutType(tt));
		}
		return result;
	}

	@Override
	public String toString() {
		return "ShortcutType [type=" + type + ", timeType=" + timeType + ", timeUnits=" + timeUnits + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;		
		int result = 31;		
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		// ! make equality test only for type property in this class 
		// (because our list of ShotcutType in combobox does not have
		// properties from superclass like name, path)		
		if (getClass() != obj.getClass())
			return false;
		ShortcutType other = (ShortcutType) obj;
		if (type != other.type)
			return false;		
		
		return true;
	}
	
	
		

}
