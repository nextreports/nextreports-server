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
package ro.nextreports.server.api;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.api.client.ErrorCodes;
import ro.nextreports.server.api.client.RunReportMetaData;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntime;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import com.sun.jersey.api.core.InjectParam;

/**
 * @author Decebal Suiu
 */
@Path("report")
public class ReportWebService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportWebService.class);

    @InjectParam
    private ReportService reportService;

    @InjectParam
    private StorageService storageService;

    /**
     * Run a report with parameters.
     * Path is relative to nextserver root ('/nextServer'). Ex: '/reports/test' <=> '/nextServer/reports/test'
     *
     * @param runReportMetaData runReportMetaData
     * @return documentUrl
     */
    @POST
    @Path("runReport")
    public String runReport(RunReportMetaData runReportMetaData) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Run report web service");
            LOG.debug("reportId = " + runReportMetaData.getReportId());
            LOG.debug("reportFormat = " + runReportMetaData.getFormat());
            LOG.debug("parametersValues = " + ReportUtil.getDebugParameters(runReportMetaData.getParametersValues()));
        }

        Report report = null;
        Date startDate = new Date();
        boolean error = false;
        String message = "Ok";
        String url = "";
        try {

            report = (Report) storageService.getEntityById(runReportMetaData.getReportId());

            ReportRuntime runtime = new ReportRuntime();
            runtime.setOutputType(runReportMetaData.getFormat());
            if (runReportMetaData.getParametersValues() == null) {
                runReportMetaData.setParametersValues(new HashMap<String, Object>());
            }
            runtime.setParametersValues(runReportMetaData.getParametersValues(), null);

            String[] result = reportService.reportToURL(report, runtime, "webservice", UUID.randomUUID().toString());
            if (result == null) {
            	return "";
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("url = " + result[1]);
            }
            url = result[1];
            return url;

        } catch (NotFoundException e) {
        	error = true;
        	message = e.getMessage();
            LOG.error(message, e);
            throw new WebApplicationException(new Exception(e.getMessage()), ErrorCodes.NOT_FOUND);
        } catch (NoDataFoundException e) {
        	error = false;
            message = "No Data";
            throw new WebApplicationException(new Exception(e.getMessage()), ErrorCodes.NO_DATA_FOUND);
        } catch (Exception e) {
        	error = true;
        	message = e.getMessage();
            LOG.error(message, e);
            throw new WebApplicationException(new Exception(e.getMessage()), ErrorCodes.RUN_ERROR);
        } finally {
        	
        	// save history
			if (report != null) {

				User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();				
				String runnerId = user.getId();

				RunReportHistory runHistory = new RunReportHistory();
				String name = UUID.randomUUID().toString();
				runHistory.setName(name);

				String path = report.getPath() + StorageConstants.PATH_SEPARATOR + "runHistory"
						+ StorageConstants.PATH_SEPARATOR + name;
				runHistory.setPath(path);

				if (LOG.isDebugEnabled()) {
					LOG.debug("Create run history '" + runHistory.getPath()	+ "'");
				}

				runHistory.setRunnerId(runnerId);
				runHistory.setRunnerType(RunReportHistory.WEB_SERVICE);
				runHistory.setStartDate(startDate);
				runHistory.setEndDate(new Date());
				runHistory.setDuration(Seconds.secondsBetween(
						new DateTime(runHistory.getStartDate()),
						new DateTime(runHistory.getEndDate())).getSeconds());
				runHistory.setError(error);
				runHistory.setMessage(message);
				runHistory.setUrl(url);
				runHistory.setParametersValues(runReportMetaData.getParametersValues(), null);

				try {
					storageService.addEntity(runHistory);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e.getMessage(), e);
				}
			}
        	
        }
	}
    	
}
