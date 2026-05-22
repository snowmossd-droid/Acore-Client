package acore.hack.event;

import meteordevelopment.orbit.IEventBus;
import java.lang.invoke.MethodHandles;

public class EventBus {
    private static final IEventBus EVENT_BUS = new meteordevelopment.orbit.EventBus();

    static {
        EVENT_BUS.registerLambdaFactory("acore.hack.event.impl", (lookupInMethod, klass) ->
            (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }

    public void subscribe(Object object) {
        EVENT_BUS.subscribe(object);
    }

    public void unsubscribe(Object object) {
        EVENT_BUS.unsubscribe(object);
    }

    public void post(Object event) {
        EVENT_BUS.post(event);
    }
}
