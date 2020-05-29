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
        @SuppressWarnings("unchecked")
        Class jobClass = (Class) map.remove(CHAIN_JOB_CLASS);
        String jobName = (String) map.remove(CHAIN_JOB_NAME);
        String jobGroup = (String) map.remove(CHAIN_JOB_GROUP);

        logger.info("Chaining Job : " + jobName);
        int count = (int) map.get("count");
        count--;

        if (count == 0) {
            return;
        }

        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .usingJobData(map)
                .usingJobData("count", count)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", jobGroup + "Trigger")
                .startAt(new Date(System.currentTimeMillis() + 5000L))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();


        scheduler.scheduleJob(job, trigger);
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
