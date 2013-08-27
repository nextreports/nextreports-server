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
package ro.nextreports.server.util;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Apr 22, 2008
 * Time: 10:37:20 AM
 */
public class SearchManager {

    private static ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private SearchManager(){
    }

    // map for searches
    // -> unique key for search
    // -> boolean rawValue for "stop" state 
    private static HashMap<String, Boolean> searches = new HashMap<String, Boolean>();

    public static void addSearch(String key) {
        searches.put(key, Boolean.FALSE);
    }

    public static void removeSearch(String key) {
        searches.remove(key);
    }

    public static boolean containsSearch(String key) {
        return searches.containsKey(key);
    }

    public static void stopSearch(String key) {
        if (containsSearch(key)) {
            rwLock.writeLock().lock();
            searches.put(key, Boolean.TRUE);
            rwLock.writeLock().unlock();
        }
    }
    
    public static boolean wasStopped(String key) {
        rwLock.readLock().lock();
        try {
            Boolean stop = searches.get(key);
            return Boolean.TRUE.equals(stop) ? true : false;
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
