package in.gore.util;

public class ValueGenerator {

    private final int initialValue;
    private final int maxValue;
    private final int step;
    private int currentValue;
    public ValueGenerator(int initialValue, int maxValue, int step) {
        this.initialValue = initialValue;
        this.maxValue = maxValue;
        this.step = step;
        this.currentValue = initialValue;
    }

    public int getValue() {
        int retVal = currentValue;
        currentValue += step;
        if (currentValue > maxValue) {
            currentValue = initialValue;
        }
        return retVal;
    }
}
