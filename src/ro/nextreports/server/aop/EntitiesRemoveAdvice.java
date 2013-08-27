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
package ro.nextreports.server.aop;

import java.util.List;

import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Decebal Suiu
 */
public abstract class EntitiesRemoveAdvice {

	@Pointcut("target(ro.nextreports.server.service.StorageService)")
	public void inStorageService() {
	}

	@Pointcut("execution(* removeEntity(..))")
	public void isRemoveEntity() {
	}

	@Pointcut("args(path, ..)")
	public void withPath(String path) {
	}
	
	@Pointcut("inStorageService() && isRemoveEntity() && withPath(path)")
	public void removeEntity(String path) {
	}

	@Pointcut("execution(* removeEntityById(..))")
	public void isRemoveEntityById() {
	}

	@Pointcut("args(id, ..)")
	public void withId(String id) {
	}
	
	@Pointcut("inStorageService() && isRemoveEntityById() && withId(id)")
	public void removeEntityById(String id) {
	}

	@Pointcut("execution(* removeEntitiesById(..))")
	public void isRemoveEntitiesById() {
	}

	@Pointcut("args(ids, ..)")
	public void withIds(List<String> ids) {
	}

	@Pointcut("inStorageService() && isRemoveEntitiesById() && withIds(ids)")
	public void removeEntitiesById(List<String> ids) {
	}

}
