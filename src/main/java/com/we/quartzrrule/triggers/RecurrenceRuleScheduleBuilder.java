package com.we.quartzrrule.triggers;

import com.we.recurr.domain.RRule;
import com.we.recurr.parser.RRuleParser;
import org.quartz.CronTrigger;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.spi.MutableTrigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.TimeZone;

public class RecurrenceRuleScheduleBuilder extends ScheduleBuilder<RecurrenceRuleTrigger> {

    private RRule recurrenceRule;
    private String recurrenceRuleExpression;
    private TimeZone timeZone;
    private int misfireInstruction = RecurrenceRuleTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;


    protected RecurrenceRuleScheduleBuilder(final RRule rrule, final String rruleExpression) {
        if (rrule == null) {
            throw new NullPointerException("recurrence rule can not be null");
        }
        this.recurrenceRule = rrule;
        this.recurrenceRuleExpression = rruleExpression;
    }

    /**
     * Build the actual Trigger -- NOT intended to be invoked by end users, but will rather be invoked by a TriggerBuilder which this ScheduleBuilder is given to.
     *
     * @return The trigger that has been built.
     * @see TriggerBuilder#withSchedule(ScheduleBuilder)
     */
    @Override
    protected MutableTrigger build() {
        RecurrenceRuleTriggerImpl rrt = new RecurrenceRuleTriggerImpl();

        rrt.setRecurrenceRule(recurrenceRule, recurrenceRuleExpression); // need way to convert RRule to String
        rrt.setMisfireInstruction(misfireInstruction);
        rrt.setStartTime(new Date()); // incorrect, Start time should be user defined.
        rrt.setTimeZone(this.timeZone.toZoneId().toString());
        return rrt;
    }

    public static RecurrenceRuleScheduleBuilder recurrenceRuleSchedule(String recurrenceRuleExpression) {
        RRuleParser rruleParser = new RRuleParser(recurrenceRuleExpression, null);
        return recurrenceRuleSchedule(rruleParser.parse(), recurrenceRuleExpression);
    }

    public static RecurrenceRuleScheduleBuilder recurrenceRuleSchedule(final RRule rrule, final String rruleExpression) {
        return new RecurrenceRuleScheduleBuilder(rrule, rruleExpression);
    }

    /**
     * If the Trigger misfires, use the {@link Trigger#MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY} instruction.
     *
     * @return the updated RecurrenceRuleScheduleBuilder
     * @see Trigger#MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
     */
    public RecurrenceRuleScheduleBuilder withMisfireHandlingInstructionIgnoreMisfires() {
        misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
        return this;
    }

    /**
     * If the Trigger misfires, use the {@link CronTrigger#MISFIRE_INSTRUCTION_DO_NOTHING} instruction.
     *
     * @return the updated RecurrenceRuleScheduleBuilder
     * @see CronTrigger#MISFIRE_INSTRUCTION_DO_NOTHING
     */
    public RecurrenceRuleScheduleBuilder withMisfireHandlingInstructionDoNothing() {
        misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        return this;
    }

    /**
     * If the Trigger misfires, use the {@link CronTrigger#MISFIRE_INSTRUCTION_FIRE_ONCE_NOW} instruction.
     *
     * @return the updated RecurrenceRuleScheduleBuilder
     * @see CronTrigger#MISFIRE_INSTRUCTION_FIRE_ONCE_NOW
     */
    public RecurrenceRuleScheduleBuilder withMisfireHandlingInstructionFireAndProceed() {
        misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        return this;
    }

    public RecurrenceRuleScheduleBuilder inTimeZone(TimeZone timezone) {
        this.timeZone = timezone;
        return this;
    }
}