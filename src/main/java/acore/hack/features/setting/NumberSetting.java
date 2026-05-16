package acore.hack.features.setting;

public class NumberSetting {
    private final String name;
    private double value;
    private final double min;
    private final double max;
    private final double increment;
    
    public NumberSetting(String name, double defaultValue, double min, double max, double increment) {
        this.name = name;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }
    
    public String getName() {
        return name;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        double clamped = Math.max(min, Math.min(max, value));
        this.value = Math.round(clamped / increment) * increment;
    }
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
    
    public double getIncrement() {
        return increment;
    }
                                  }
