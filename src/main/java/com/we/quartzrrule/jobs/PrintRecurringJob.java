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
        System.out.println("Executing " + ctx.getJobDetail().getKey().getName() + " with " + new LinkedHashMap<>(map));

        map.put("jobTime", new Date().toString());
        map.put("jobValue", new Random().nextLong());

        COUNT++;
        chainJob(ctx, PrintRecurringJob.class, ctx.getJobDetail().getKey().getName() + "-" +COUNT, ctx.getJobDetail().getKey().getGroup());
    }
}
