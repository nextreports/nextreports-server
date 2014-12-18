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
package ro.nextreports.server.etl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DefaultProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(DefaultProcessor.class);

    private Extractor extractor;
    private List<Transformer> transformers;
    private Loader loader;

    public DefaultProcessor(Extractor extractor, Loader loader) {
        this(extractor, Collections.EMPTY_LIST, loader);
    }

    public DefaultProcessor(Extractor extractor, List<Transformer> transformers, Loader loader) {
        this.extractor = extractor;
        this.transformers = transformers;
        this.loader = loader;
    }

    @Override
    public void init() {
        log.debug("Init processor");
        extractor.init();
        for (Transformer transformer : transformers) {
            transformer.init();
        }
        loader.init();
    }

    @Override
    public void process() {
        log.debug("Start processing");
        long time = System.currentTimeMillis();
        long counter = 0;
        Iterator<Row> iterator = extractor.extract();
        while (iterator.hasNext()) {
            Row row = iterator.next();
//            System.out.println("row = " + row);
            for (Transformer transformer : transformers) {
                row = transformer.transform(row);
//                System.out.println("transformer = " + transformer + " > " + "row = " + row);

            }
            loader.load(row);
            counter++;
        }
        time = System.currentTimeMillis() - time;
        log.info("Processed {} rows in {} ms", counter, time);
    }

    @Override
    public void destroy() {
        log.debug("Destroy processor");
        loader.destroy();
        List<Transformer> transformers = new ArrayList<Transformer>(this.transformers);
        Collections.reverse(transformers);
        for (Transformer transformer : transformers) {
            transformer.destroy();
        }
        extractor.destroy();
    }

}
