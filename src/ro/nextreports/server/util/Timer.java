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

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:12:28 AM
 */
public class Timer {

    public static final int MILLISECONDS = 0;
    public static final int SECONDS = 1;
    public static final int MINUTES = 2;

    private long start;
    private long end;

    public Timer() {
        reset();
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void end() {
        end = System.currentTimeMillis();
    }

    public long duration() {
        return (end - start);
    }

    public long duration(int time_units) {
        switch (time_units) {
            case Timer.MILLISECONDS:
                return (end - start);
            case Timer.SECONDS:
                return (end - start) / 1000;
            case Timer.MINUTES:
                return (end - start) / (60 * 1000);
        }

        return -1;
    }

    public void reset() {
        start = 0;
        end = 0;
    }

    public void restart() {
        start();
    }

}
