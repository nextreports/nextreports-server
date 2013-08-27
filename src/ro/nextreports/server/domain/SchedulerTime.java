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
package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.schedule.ScheduleConstants;

import java.util.Date;

import ro.nextreports.engine.util.DateUtil;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: May 20, 2008
 * Time: 12:10:11 PM
 */
public class SchedulerTime extends EntityFragment {

	private static final long serialVersionUID = 1L;

	@JcrProperty
    private Date startActivationDate;

    @JcrProperty
    private Date endActivationDate;

    @JcrProperty
    private String type;

    // ONCE TYPE
    @JcrProperty
    private Date runDate;

    // MINUTELY & HOURLY TYPE
    @JcrProperty
    private String hours;

    @JcrProperty
    private int gap;

    @JcrProperty
    private String days;

    @JcrProperty
    private String months;

    @JcrProperty
    private int minute;

    // DAILY
    @JcrProperty
    private String daysOfWeek;

    // MONTHLY
    @JcrProperty
    private int dayNo;

    @JcrProperty
    private int monthlyType;

    @JcrProperty
    private boolean advanced;

    public SchedulerTime() {
        super();
        
        setName("time");
    }

    public Date getStartActivationDate() {
        return startActivationDate;
    }

    public void setStartActivationDate(Date startActivationDate) {
        this.startActivationDate = startActivationDate;
    }

    public Date getEndActivationDate() {
        return endActivationDate;
    }

    public void setEndActivationDate(Date endActivationDate) {
        this.endActivationDate = endActivationDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getMonths() {
        return months;
    }

    public void setMonths(String months) {
        this.months = months;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getDayNo() {
        return dayNo;
    }

    public void setDayNo(int dayNo) {
        this.dayNo = dayNo;
    }

    public int getMonthlyType() {
        return monthlyType;
    }

    public void setMonthlyType(int monthlyType) {
        this.monthlyType = monthlyType;
    }

    public boolean getAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public String getCronEntry() {
        if (ScheduleConstants.ONCE_TYPE.equals(type)) {
            return createCron("0",
                    String.valueOf(DateUtil.getMinute(runDate)),
                    String.valueOf(DateUtil.getHour(runDate)),
                    String.valueOf(DateUtil.getDayOfMonth(runDate)),
                    String.valueOf(DateUtil.getMonth(runDate) + 1),
                    "?");
        } else if (ScheduleConstants.MINUTELY_TYPE.equals(type)) {
            String minute = "*";
            if (gap > 1) {
                minute = minute + "/" + gap;
            }
            
            return createCron("0", minute, hours, days, months, "?");
        } else if (ScheduleConstants.HOURLY_TYPE.equals(type)) {
            String hour = hours;
            if (gap > 1) {
                hour = hours + "/" + gap;
            }
            
            return createCron("0", String.valueOf(minute), hour, days, months, "?");
        } else if (ScheduleConstants.DAILY_TYPE.equals(type)) {
            String dow = daysOfWeek;
            String d = days;
            if ("*".equals(days) && "*".equals(daysOfWeek)) {
                dow = "?";
            }
            if (!"*".equals(days)) {
                dow = "?";
            }
            if (!"*".equals(daysOfWeek) && !"?".equals(daysOfWeek)) {
                d = "?";
            }
            
            return createCron("0", String.valueOf(minute), hours, d, "*", dow);
        } else if (ScheduleConstants.WEEKLY_TYPE.equals(type)) {
            String dow = daysOfWeek;
            String d = days;
            if ("*".equals(days) && "*".equals(daysOfWeek)) {
                dow = "?";
            }
            if (!"*".equals(days)) {
                dow = "?";
            }
            if (!"*".equals(daysOfWeek) && !"?".equals(daysOfWeek)) {
                d = "?";
            }
            
            return createCron("0", String.valueOf(minute), hours, d, "*", dow);
        } else if (ScheduleConstants.MONTHLY_TYPE.equals(type)) {
            if (monthlyType == ScheduleConstants.MONTHLY_LAST_DAY_TYPE) {
                return createCron("0", String.valueOf(minute), hours, "L", "*", "?");
            } else if (monthlyType == ScheduleConstants.MONTHLY_DAY_OF_WEEK_TYPE) {
                String dayOfWeek = daysOfWeek;
                if (dayNo == 5) {
                    dayOfWeek = dayOfWeek + "L";
                } else {
                    dayOfWeek = dayOfWeek + "#" + dayNo;
                }
                
                return createCron("0", String.valueOf(minute), hours, "?", "*", dayOfWeek);
            } else if (monthlyType == ScheduleConstants.MONTHLY_GENERAL_TYPE) {
                String dow = daysOfWeek;
                String d = days;
                if ("*".equals(days) && "*".equals(daysOfWeek)) {
                    dow = "?";
                }
                if (!"*".equals(days)) {
                    dow = "?";
                }
                if (!"*".equals(daysOfWeek) && !"?".equals(daysOfWeek)) {
                    d = "?";
                }
                
                return createCron("0", String.valueOf(minute), hours, d, months, dow);
            }
        }

        return null;
    }

    @Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("SchedulerTime[");
        buffer.append("name = ").append(name);
        buffer.append(", advanced = ").append(advanced);
        buffer.append(", path = ").append(path);
    	buffer.append(", dayNo = ").append(dayNo);
    	buffer.append(", days = ").append(days);
    	buffer.append(", daysOfWeek = ").append(daysOfWeek);
    	buffer.append(", endActivationDate = ").append(endActivationDate);
    	buffer.append(", gap = ").append(gap);
    	buffer.append(", hours = ").append(hours);
    	buffer.append(", minute = ").append(minute);
    	buffer.append(", monthlyType = ").append(monthlyType);
    	buffer.append(", months = ").append(months);
    	buffer.append(", runDate = ").append(runDate);
    	buffer.append(", startActivationDate = ").append(startActivationDate);
    	buffer.append(", type = ").append(type);
    	buffer.append("]");
    	return buffer.toString();
    }    

    private String createCron(String second, String minute, String hour, String dayOfMonth, String month, String dayOfWeek) {
        StringBuilder sb = new StringBuilder();
        sb.append(second).append(" ").
                append(minute).append(" ").
                append(hour).append(" ").
                append(dayOfMonth).append(" ").
                append(month).append(" ").
                append(dayOfWeek);
        return sb.toString();
    }

}
