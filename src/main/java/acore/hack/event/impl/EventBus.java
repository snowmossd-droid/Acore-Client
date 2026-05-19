package acore.hack.event;

import meteordevelopment.orbit.EventBus as OrbitEventBus;

public class EventBus {
    private static final OrbitEventBus EVENT_BUS = new OrbitEventBus();
    
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
