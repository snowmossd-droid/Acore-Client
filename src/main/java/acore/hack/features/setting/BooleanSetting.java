package acore.hack.features.setting;

public class BooleanSetting {
    private final String name;
    private boolean enabled;
    
    public BooleanSetting(String name, boolean defaultValue) {
        this.name = name;
        this.enabled = defaultValue;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean getValue() {
        return enabled;
    }
    
    public void toggle() {
        this.enabled = !this.enabled;
    }
}
