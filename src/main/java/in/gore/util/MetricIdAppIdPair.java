package in.gore.util;

/**
 * Created by samarth.gupta on 13/10/17.
 */
public class MetricIdAppIdPair {

    int metricId;
    int applicationId ;

    public int getApplicationId() {
        return applicationId;
    }

    public int getMetricId() {
        return metricId;
    }


    public MetricIdAppIdPair(int metriccId, int applicationId) {
        this.metricId = metriccId;
        this.applicationId = applicationId;
    }

    @Override
    public boolean equals(Object metricAppPair) {
        return metricAppPair != null
                && metricAppPair instanceof MetricIdAppIdPair
                && (((MetricIdAppIdPair) metricAppPair).getApplicationId() == this.getApplicationId()
                && ((MetricIdAppIdPair) metricAppPair).getMetricId() == this.getMetricId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89*hash + this.getApplicationId();
        hash = 89*hash + this.getMetricId();
        return hash;
    }
}

