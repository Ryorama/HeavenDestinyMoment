package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MomentInstanceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MomentInstanceBuilder.class);
    private final IMoment moment;
    private AutomationContext context;
    private Consumer<MomentInstance> modifier;
    private boolean checkConditions = true;
    private List<ICondition> specialConditions = new ArrayList<>();
    
    MomentInstanceBuilder(IMoment moment, AutomationContext context) {
        this.moment = Objects.requireNonNull(moment, "Moment cannot be null");
        this.context = Objects.requireNonNull(context, "context cannot be null");
    }
    
    public static MomentInstanceBuilder builder(IMoment moment, AutomationContext context) {
        return new MomentInstanceBuilder(moment,context);
    }

    public MomentInstanceBuilder skipConditions() {
        this.checkConditions = false;
        return this;
    }
    
    public MomentInstanceBuilder addSpecialCondition(ICondition condition) {
        if (condition != null) {
            this.specialConditions.add(condition);
        }
        return this;
    }
    
    public MomentInstanceBuilder addSpecialConditions(ICondition... conditions) {
        this.checkConditions = false;
        for (ICondition condition : conditions) {
            if (condition != null) {
                this.specialConditions.add(condition);
            }
        }
        return this;
    }

    //MomentInstance
    public void setMomentInstance(AutomationContext context) {
        this.context = context;
    }

    public MomentInstanceBuilder modify(Consumer<MomentInstance> modifier) {
        this.modifier = modifier;
        return this;
    }

    public AutomationContext getContext() {
        return context;
    }

    public MomentInstance buildRun() {
        MomentInstanceManager manager = MomentInstanceManager.of(context.getLevel());
        return manager.createMomentInstanceRun(this);
    }
    public MomentInstance build() {
        MomentInstanceManager manager = MomentInstanceManager.of(context.getLevel());
        return manager.createMomentInstance(this);
    }

    public IMoment getMoment() { return moment; }
    public Consumer<MomentInstance> getModifier() { return modifier; }
    public boolean isCheckConditions() { return checkConditions; }
    public List<ICondition> getSpecialConditions() { return new ArrayList<>(specialConditions); }

    public static MomentInstance createRun(IMoment moment, AutomationContext context) {
        return builder(moment,context).buildRun();
    }

    public static MomentInstance skipConditionsExceptRun(IMoment moment, AutomationContext context, ICondition... specialConditions) {
        return builder(moment,context).addSpecialConditions(specialConditions).buildRun();
    }

    public static MomentInstance create(IMoment moment, AutomationContext context) {
        return builder(moment,context).build();
    }
}