package com.we.quartzrrule.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class PrintJob extends RecurringRuleJob {
    private static final Logger logger = LoggerFactory.getLogger(PrintJob.class);

    @Override
    protected void doExecute(JobExecutionContext ctx) throws JobExecutionException {
        JobDataMap map = ctx.getJobDetail().getJobDataMap();
        JobDataMap triggerMap = ctx.getTrigger().getJobDataMap();

        logger.info("Executing " + ctx.getJobDetail().getKey().getName() + " with " + new LinkedHashMap<>(map) + " triggerMap : " + new LinkedHashMap<>(triggerMap));

        chainJob(ctx, PrintJob.class, ctx.getJobDetail().getKey().getName(), ctx.getJobDetail().getKey().getGroup());
    }
}
