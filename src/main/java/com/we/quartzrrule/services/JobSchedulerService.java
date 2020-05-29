package com.we.quartzrrule.services;

import com.we.quartzrrule.jobs.PrintRecurringJob;
import com.we.quartzrrule.jobs.RecurringJob;
import com.we.quartzrrule.jobs.SimpleJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);

    @Autowired
    private Scheduler scheduler;

    public void scheduleSimpleJob() throws SchedulerException {
        logger.info("Scheduling job to quartz");
        JobDetail job = JobBuilder.newJob(SimpleJob.class)
                .withIdentity("SimpleJob", "SimpleJobGroup")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("SimpleJobTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    public void scheduleRecurringJob() throws SchedulerException {
        logger.info("Scheduling recurring job to quartz");
        JobDetail job = JobBuilder.newJob(PrintRecurringJob.class)
                .withIdentity("RecurringJob", "RecurringJobGroup")
                .usingJobData("count", 10)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("RecurringJobTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
