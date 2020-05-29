package com.we.quartzrrule.jobs;

import com.we.quartzrrule.controllers.JobSchedulerController;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public abstract class RecurringJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(RecurringJob.class);

    private static final String CHAIN_JOB_CLASS = "chainedJobClass";
    private static final String CHAIN_JOB_NAME = "chainedJobName";
    private static final String CHAIN_JOB_GROUP = "chainedJobGroup";

    @Autowired
    private Scheduler scheduler;

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        doExecute(ctx);

        try {
            chain(ctx);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void chain(JobExecutionContext ctx) throws SchedulerException {
        JobDataMap map = ctx.getJobDetail().getJobDataMap();
        JobDataMap triggerMap = ctx.getTrigger().getJobDataMap();

        @SuppressWarnings("unchecked")
        Class jobClass = (Class) map.remove(CHAIN_JOB_CLASS);
        String jobName = (String) map.remove(CHAIN_JOB_NAME);
        String jobGroup = (String) map.remove(CHAIN_JOB_GROUP);

        int count = (int) triggerMap.get("count");
        count--;
        logger.info("Chaining Job : " + jobName + " count : " + count);

        if (count == 0) {
            return;
        }

        Trigger oldTrigger = ctx.getTrigger();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", jobGroup + "Trigger")
                .usingJobData("count", count)
                .startAt(new Date(System.currentTimeMillis() + 5000L))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.rescheduleJob(oldTrigger.getKey(), trigger);
    }

    protected abstract void doExecute(JobExecutionContext ctx) throws JobExecutionException;

    protected void chainJob(JobExecutionContext context,
                            Class jobClass,
                            String jobName,
                            String jobGroup) {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        map.put(CHAIN_JOB_CLASS, jobClass);
        map.put(CHAIN_JOB_NAME, jobName);
        map.put(CHAIN_JOB_GROUP, jobGroup);
    }
}
