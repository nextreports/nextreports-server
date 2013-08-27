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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.service.StorageService;


/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Jun 2, 2008
 * Time: 4:20:33 PM
 */
@Aspect
public class ReportRemovedAdvice extends EntitiesRemoveAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(ReportRemovedAdvice.class);
	
    private StorageService storageService;

    @Before("removeEntity(path)")
	public void beforeReportRemoved(String path) {
        if (isInvalidPath(path)) {
            return;
        }
        try {
            Entity entity = storageService.getEntity(path);
            if (entity instanceof Report) {
                Report report = (Report) entity;
                if (ReportConstants.JASPER.equals(report.getType())) {
                	LOG.info("Delete jasper compiled files for report '" + path + "'");
                	JasperReportsUtil.deleteJasperCompiledFiles(storageService, report);
                }
            }
        } catch (Exception e) {
        	LOG.error(e.getMessage(), e);
        	throw new RuntimeException(e);
        }
    }

	@Before("removeEntityById(id)")
	public void beforeReportRemovedById(String id) {
		try {
			String path = storageService.getEntityPath(id);
			beforeReportRemoved(path);
		} catch (NotFoundException e) {
			// do nothing
		}
	}

	@Before("removeEntitiesById(ids)")
	public void beforeReportRemovedById(List<String> ids) {
		for (String id : ids) {
			beforeReportRemovedById(id);
		}
	}

    @Required
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    private boolean isInvalidPath(String path) {
        return !path.startsWith(StorageConstants.REPORTS_ROOT);
    }

}
