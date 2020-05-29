package com.we.quartzrrule.jobs;

import com.we.recurr.iter.RecurrenceIterator;
import org.quartz.Job;
import org.quartz.JobDataMap;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;

public abstract class RecurringRuleJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(RecurringRuleJob.class);

    private static final String JOB_CLASS = "jobClass";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_GROUP = "jobGroup";

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
        Class jobClass = (Class) map.remove(JOB_CLASS);
        String jobName = (String) map.remove(JOB_NAME);
        String jobGroup = (String) map.remove(JOB_GROUP);

        String rrule = map.get("rrule").toString();

        Date nextFire = getNextFire(rrule);
        if (nextFire == null) {
            return;
        }

        logger.info("Job : " + ctx.getJobDetail().getKey() + " Next fire : " + nextFire);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", jobGroup + "Trigger")
                .startAt(nextFire)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.rescheduleJob(ctx.getTrigger().getKey(), trigger);
    }

    protected abstract void doExecute(JobExecutionContext ctx) throws JobExecutionException;

    protected void chainJob(JobExecutionContext context,
                            Class jobClass,
                            String jobName,
                            String jobGroup) {
        JobDataMap map = context.getJobDetail().getJobDataMap();

        map.put(JOB_CLASS, jobClass);
        map.put(JOB_NAME, jobName);
        map.put(JOB_GROUP, jobGroup);
    }

    protected Date getNextFire(String rrule) {
        Iterator<LocalDateTime> itr = new RecurrenceIterator(rrule, null);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = null;

        while (itr.hasNext()) {
            next = itr.next();
            if (next.isAfter(now)) {
                break;
            } else {
                next = null;
            }
        }

        return next != null ? Date.from(next.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}
