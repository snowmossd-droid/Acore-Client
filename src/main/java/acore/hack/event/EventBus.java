package acore.hack.event;

import meteordevelopment.orbit.EventBus;

public class EventBus {
    private static final EventBus EVENT_BUS = new EventBus();
    
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
