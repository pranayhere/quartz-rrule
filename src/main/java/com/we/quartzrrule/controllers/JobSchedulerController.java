package com.we.quartzrrule.controllers;

import com.we.quartzrrule.services.JobSchedulerService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobSchedulerController {

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

    @PostMapping("/customTrigger")
    public String scheduleWithCustomTrigger() throws SchedulerException {
        jobSchedulerService.scheduleRecurringRuleWithTrigger();
        return "Success";
    }
}
