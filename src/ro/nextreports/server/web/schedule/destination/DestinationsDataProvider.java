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
package ro.nextreports.server.web.schedule.destination;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.domain.SchedulerJob;

import java.util.Iterator;
import java.util.List;

/**
 * User: mihai.panaitescu
 * Date: 24-Sep-2010
 * Time: 11:32:20
 */
public class DestinationsDataProvider extends SortableDataProvider<Destination> {

    private SchedulerJob schedulerJob;

    public DestinationsDataProvider(SchedulerJob schedulerJob) {
        super();
        this.schedulerJob = schedulerJob;
    }

    public Iterator<? extends Destination> iterator(int first, int count) {

        return schedulerJob.getDestinations().iterator();
    }

    public int size() {
        return schedulerJob.getDestinations().size();
    }

    public IModel<Destination> model(Destination destination) {
        return new Model<Destination>(destination);
    }

    public List<Destination> getDestinations() {
        return schedulerJob.getDestinations();
    }

}
