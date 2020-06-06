package com.we.quartzrrule.triggers;

import com.we.recurr.domain.RRule;
import com.we.recurr.iter.RecurrenceIterator;
import com.we.recurr.parser.RRuleParser;
import com.we.recurr.parser.RuleParser;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.triggers.AbstractTrigger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class RecurrenceRuleTriggerImpl extends AbstractTrigger<RecurrenceRuleTrigger> implements RecurrenceRuleTrigger {

    private static final long serialVersionUID = -2658978876664286825L;
    private static final int YEAR_TO_GIVEUP_SCHEDULING_AT = Calendar.getInstance().get(java.util.Calendar.YEAR) + 100;
    private String recurrenceRuleExpression = null;
    private transient RRule recurrenceRule = null;
    private Date nextFireTime = null;
    private Date previousFireTime = null;
    private Date fromDate = null;

    public RecurrenceRuleTriggerImpl() {
        super();
    }

    @Override
    public void triggered(org.quartz.Calendar calendar) {
//        CronTrigger trigger = new CronTriggerImpl();
        previousFireTime = nextFireTime;
        nextFireTime = getFireTimeAfter(nextFireTime);

        while (nextFireTime != null && calendar != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }
    }

    @Override
    public Date computeFirstFireTime(org.quartz.Calendar calendar) {
        nextFireTime = getFireTimeAfter(new Date(getStartTime().getTime() - 1000l), true);

        while (nextFireTime != null && calendar != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime, true);
        }

        return nextFireTime;
    }

    public void setRecurrenceRuleExpression(String rruleExpression) {
        recurrenceRuleExpression = rruleExpression;
        RuleParser rruleParser = new RRuleParser(rruleExpression, null);
        recurrenceRule = rruleParser.parse();
    }

    public RRule getRecurrenceRule() {
        return this.recurrenceRule;
    }

    @Override
    public String getRecurrenceRuleExpression() {
        return this.recurrenceRuleExpression;
    }

    @Override
    public int getRepeatCount() {
        if (getRecurrenceRule() == null) {
            return REPEAT_INDEFINITELY;
        } else {
            return getRecurrenceRule().getCount();
        }
    }

    @Override
    public boolean mayFireAgain() {
        return getNextFireTime() != null;
    }

    @Override
    public Date getStartTime() {
        return fromDate;
    }

    @Override
    public void setStartTime(Date startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        Date eTime = getEndTime();
        if (eTime != null && eTime.before(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        // round off millisecond...
        // Note timeZone is not needed here as parameter for
        // Calendar.getInstance(),
        // since time zone is implicit when using a Date in the setTime method.
        Calendar cl = Calendar.getInstance();
        cl.setTime(startTime);
        cl.set(Calendar.MILLISECOND, 0);

        this.fromDate = cl.getTime();
    }

    @Override
    public void setEndTime(Date endTime) {
        Date sTime = getStartTime();
        if (sTime != null && endTime != null && sTime.after(endTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        // set end time in RRULE
//        if (endTime != null) {
//            setRecurrenceRule(new Recurrence.Builder(this.getRecurrenceRule()).until(endTime).build());
//        }
    }

    public void setRecurrenceRule (RRule rrule, String recurrenceRuleExpression) {
        this.recurrenceRule = rrule;
        this.recurrenceRuleExpression = recurrenceRuleExpression;
    }

    @Override
    public Date getEndTime() {
        if (getRecurrenceRule() == null) {
            return null;
        } else {
            return Date.from(getRecurrenceRule().getUntil().atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    @Override
    public Date getNextFireTime() {
        return this.nextFireTime;
    }

    @Override
    public Date getPreviousFireTime() {
        return this.previousFireTime;
    }

    @Override
    public Date getFireTimeAfter(Date afterTime) {
        return getFireTimeAfter(afterTime, false);
    }

    public Date getFireTimeAfter (Date afterTime, boolean firstTime) {
        Date after;
        if (afterTime == null) {
            after = new Date();
        } else {
            after = (Date) afterTime.clone();
        }

        if (getStartTime().after(after)) {
            after = new Date(getStartTime().getTime() - 1000l);
        }

        if (getEndTime() != null && (after.compareTo(getEndTime()) >= 0)) {
            return null;
        }

        Date pot = getTimeAfter(after, firstTime);
        if (getEndTime() != null && pot != null && pot.after(getEndTime())) {
            return null;
        }

        return pot;
    }

    protected Date getTimeAfter (Date afterTime, boolean firstTime) {
        if (getRecurrenceRule() == null) {
            return null;
        } else {
            String timeZone = null;
            Iterator<LocalDateTime> itr = new RecurrenceIterator(getRecurrenceRuleExpression(), null);

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

            ZoneId zoneId = timeZone != null ? ZoneId.of( timeZone ) : ZoneId.systemDefault();
            return next != null ? Date.from(next.atZone(zoneId).toInstant()) : null;
        }
    }

    @Override
    public Date getFinalFireTime() {
        return Date.from(getRecurrenceRule().getUntil().atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    protected boolean validateMisfireInstruction(int misfireInstruction) {
        if (misfireInstruction < MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
            return false;
        }

        return misfireInstruction <= MISFIRE_INSTRUCTION_DO_NOTHING;
    }

    @Override
    public void updateAfterMisfire(org.quartz.Calendar cal) {
        int instr = getMisfireInstruction();

        if (instr == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
            return;
        }

        if (instr == MISFIRE_INSTRUCTION_SMART_POLICY) {
            instr = MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        }

        if (instr == MISFIRE_INSTRUCTION_DO_NOTHING) {
            Date newFireTime = getFireTimeAfter(new Date());
            while (newFireTime != null && cal != null && !cal.isTimeIncluded(newFireTime.getTime())) {
                newFireTime = getFireTimeAfter(newFireTime);
            }
            setNextFireTime(newFireTime);
        } else if (instr == MISFIRE_INSTRUCTION_FIRE_ONCE_NOW) {
            setNextFireTime(new Date());
        }
    }

    @Override
    public void updateWithNewCalendar(org.quartz.Calendar calendar, long misfireThreshold) {
        nextFireTime = getFireTimeAfter(previousFireTime);

        if (nextFireTime == null || calendar == null) {
            return;
        }

        Date now = new Date();
        while (nextFireTime != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {

            nextFireTime = getFireTimeAfter(nextFireTime);

            if (nextFireTime == null) {
                break;
            }

            // avoid infinite loop
            // Use gregorian only because the constant is based on Gregorian
            java.util.Calendar c = new java.util.GregorianCalendar();
            c.setTime(nextFireTime);
            if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                nextFireTime = null;
            }

            if (nextFireTime != null && nextFireTime.before(now)) {
                long diff = now.getTime() - nextFireTime.getTime();
                if (diff >= misfireThreshold) {
                    nextFireTime = getFireTimeAfter(nextFireTime);
                    continue;
                }
            }
        }
    }

    @Override
    public void setNextFireTime(Date fireTime) {
        this.nextFireTime = fireTime;
    }

    @Override
    public void setPreviousFireTime(Date fireTime) {
        this.previousFireTime = fireTime;
    }

    @Override
    public ScheduleBuilder<RecurrenceRuleTrigger> getScheduleBuilder() {
        return null;
    }
}
