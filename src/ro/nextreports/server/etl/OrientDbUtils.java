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

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class OrientDbUtils {

    public static long getDatabaseSize(ODatabaseDocument database) {
        return database.getSize();
    }

    public static List<ClassMetadata> getDatabaseClasses(ODatabaseDocument database) {
        Collection<OClass> classes = database.getMetadata().getSchema().getClasses();

        List<ClassMetadata> result = new ArrayList<ClassMetadata>();
        for (OClass clazz : classes) {
            result.add(new ClassMetadata(clazz.getName(), clazz.count(), clazz.getSize()));
        }

        return result;
    }

    public static List<ClassMetadata> getDatabaseClasses(ODatabaseDocument database, String prefix) {
        Collection<OClass> classes = database.getMetadata().getSchema().getClasses();

        List<ClassMetadata> result = new ArrayList<ClassMetadata>();
        for (OClass clazz : classes) {
            if (clazz.getName().startsWith(prefix)) {
                result.add(new ClassMetadata(clazz.getName(), clazz.count(), clazz.getSize()));
            }
        }

        return result;
    }

    public static class ClassMetadata {

        private String name;
        private long records;
        private long size;

        public ClassMetadata(String name, long records, long size) {
            this.name = name;
            this.records = records;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getRecords() {
            return records;
        }

        public long getSize() {
            return size;
        }

    }

}
