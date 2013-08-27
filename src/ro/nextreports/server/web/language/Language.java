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
package ro.nextreports.server.web.language;

public class Language {
	
	private String property;
	private String language;
	private String country;
		
	public Language(String language, String country, String property) {
		super();
		this.property = property;
		this.language = language;
		this.country = country;
	}
	
	public String getProperty() {
		return property;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getCountry() {
		return country;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Language other = (Language) obj;
		if (country == null) {
			if (other.country != null) return false;
		} else if (!country.equals(other.country)) return false;
		if (language == null) {
			if (other.language != null) return false;
		} else if (!language.equals(other.language)) return false;
		if (property == null) {
			if (other.property != null) return false;
		} else if (!property.equals(other.property)) return false;
		return true;
	}
	
	
			
}
