package com.we.quartzrrule.services;

import com.we.quartzrrule.jobs.PrintJob;
import com.we.quartzrrule.jobs.SimpleJob;
import com.we.quartzrrule.triggers.RecurrenceRuleScheduleBuilder;
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

        JobDetail job = JobBuilder.newJob(PrintJob.class)
                .withIdentity("RecurringJob", "RecurringJobGroup")
                .usingJobData("rrule", "RRULE:FREQ=DAILY;DTSTART=20200530T91520Z;UNTIL=20200603T183000Z")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("RecurringJobTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    public void scheduleRecurringRuleWithTrigger() throws SchedulerException {
        logger.info("Scheduling Recurring job with Custom RRULE trigger");

        JobDetail job = JobBuilder.newJob(SimpleJob.class)
                .withIdentity("SimpleJob", "SimpleJobGroup")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("CustomRecurringRuleTrigger")
                .startNow()
                .withSchedule(RecurrenceRuleScheduleBuilder.recurrenceRuleSchedule("RRULE:FREQ=SECONDLY;INTERVAL=30;DTSTART=20200606T91520Z;UNTIL=20200609T183000Z"))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}