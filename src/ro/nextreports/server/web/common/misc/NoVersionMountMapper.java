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

import org.apache.wicket.core.request.handler.BookmarkableListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;

/**
 * Provides a mount strategy that drops the version number from
 * stateful page urls.
 * 
 * From wicket version 6.13+ ajax did not work from bokmarkablke pages when using this mapper and some changes were added
 * see http://stackoverflow.com/questions/8602489/delete-version-number-in-url
 */
public class NoVersionMountMapper extends MountedMapper {

	public NoVersionMountMapper(final Class<? extends IRequestablePage> pageClass) {
        this("/", pageClass);
    }

    public NoVersionMountMapper(String mountPath, final Class<? extends IRequestablePage> pageClass) {
        super(mountPath, pageClass, new PageParametersEncoder());
    }

    @Override
    protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
        //Does nothing
    }

    @Override
    public Url mapHandler(IRequestHandler requestHandler) {
        if (requestHandler instanceof ListenerInterfaceRequestHandler || 
        	requestHandler instanceof BookmarkableListenerInterfaceRequestHandler) {
            return null;
        } else {
            return super.mapHandler(requestHandler);
        }
    }

}
