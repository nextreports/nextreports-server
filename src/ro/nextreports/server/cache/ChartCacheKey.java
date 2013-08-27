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
package ro.nextreports.server.cache;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Decebal Suiu
 */
public class ChartCacheKey extends QueryCacheKey {

	private byte chartType;

	public ChartCacheKey(Map<String, Object> parameterValues, byte chartType) {
		super(parameterValues);
		
		this.chartType = chartType;
	}

    @Override
	public int hashCode() {
    	HashCodeBuilder builder = new HashCodeBuilder(19, 39);
    	builder.append(parameterValuesAsString);
    	builder.append(chartType);
    	return builder.toHashCode();
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || !(object instanceof ChartCacheKey)) {
			return false;
		}
		
		ChartCacheKey otherCacheKey = (ChartCacheKey) object;
		
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(parameterValuesAsString, otherCacheKey.parameterValuesAsString);
		builder.append(chartType, otherCacheKey.chartType);
		return builder.isEquals();
	}

}
