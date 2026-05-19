package acore.hack.event;

import meteordevelopment.orbit.EventBus as OrbitEventBus;

public class EventBus {
    private static final OrbitEventBus EVENT_BUS = new OrbitEventBus();
    
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
