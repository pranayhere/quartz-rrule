package com.we.quartzrrule.controllers;

import com.we.quartzrrule.services.JobSchedulerService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobSchedulerController {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerController.class);

    @Autowired
    private JobSchedulerService jobSchedulerService;

    @PostMapping("/schedule")
    public String scheduleSimpleJob() throws SchedulerException {
        jobSchedulerService.scheduleSimpleJob();
        return "Success";
    }

    @PostMapping("/scheduleRecurringJob")
    public String scheduleRecurringJob() throws SchedulerException {
        jobSchedulerService.scheduleRecurringJob();
        return "Success";
    }
}
