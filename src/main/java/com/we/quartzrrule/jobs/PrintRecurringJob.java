package com.we.quartzrrule.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Random;

public class PrintRecurringJob extends RecurringJob {
    static int COUNT = 0;

    @Override
    protected void doExecute(JobExecutionContext ctx) throws JobExecutionException {
        JobDataMap map = ctx.getJobDetail().getJobDataMap();
        JobDataMap triggerMap = ctx.getTrigger().getJobDataMap();
        System.out.println("Executing " + ctx.getJobDetail().getKey().getName() + " with " + new LinkedHashMap<>(map) + " triggerMap : " + new LinkedHashMap<>(triggerMap));

        map.put("jobTime", new Date().toString());
        map.put("jobValue", new Random().nextLong());

        COUNT++;
        chainJob(ctx, PrintRecurringJob.class, ctx.getJobDetail().getKey().getName(), ctx.getJobDetail().getKey().getGroup());
    }
}
