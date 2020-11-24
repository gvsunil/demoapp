package in.gore;

import in.gore.util.MetricIdAppIdPair;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Use properties file in src/main/resources/DataGeneratorV2.properties to configure host details
 * This main class populates data in metricdata_min, metricdata_min_agg and metricdata_min_agg_app
 * for last specified months. Default is 6 months.
 */
public class DataGeneratorV3 {

    public static void main(String args[]) throws Exception {

        System.out.println("Start time: " + System.currentTimeMillis());

        Properties dataGenProps = new Properties();
        dataGenProps.load(ClassLoader.getSystemResourceAsStream("DataGeneratorV3.properties"));

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection localConn = DriverManager.getConnection(
                "jdbc:mysql://" + dataGenProps.getProperty("host") + ":" + dataGenProps.getProperty("DBPort") +
                        "/controller?user=" + dataGenProps.getProperty("DBUser") + "&password=" + dataGenProps.getProperty("DBPassword") + "&port=3306&allowUrlInLocalInfile=true");

        long timeInMilli = System.currentTimeMillis();
        long minutesRound = timeInMilli / (1000 * 60);
        long startTimeMinRound = minutesRound - (60 * Integer.parseInt(dataGenProps.getProperty("HoursToGeneratePastMindataFor")));
        long startTimeHrRound = minutesRound - (24 * 60 * Integer.parseInt(dataGenProps.getProperty("DaysToGeneratePastHourdataFor")));
        startTimeHrRound = (startTimeHrRound / 60) * 60;

        int applicationId = Integer.parseInt(dataGenProps.getProperty("ApplicationID"));
        Statement appStmt = localConn.createStatement();
        ResultSet rs = appStmt.executeQuery("select * from metric where name in" +
                " ('Hardware Resources|CPU|%Busy','Hardware Resources|Memory|Used %') and application_id="
                + applicationId);

        File metricdata_min_file = new File("metricdata_min.txt");
        if (metricdata_min_file.exists()) FileUtils.forceDelete(metricdata_min_file);
        FileWriter metricdataMinWriter = new FileWriter("metricdata_min.txt");
        BufferedWriter metricdataMinBwr = new BufferedWriter(metricdataMinWriter);

        File metricdata_hour_file = new File("metricdata_hour.txt");
        if (metricdata_hour_file.exists()) FileUtils.forceDelete(metricdata_hour_file);
        FileWriter metricdataHourWriter = new FileWriter("metricdata_hour.txt");
        BufferedWriter metricdataHourBwr = new BufferedWriter(metricdataHourWriter);

        Statement insertStmt = localConn.createStatement();

        HashMap<MetricIdAppIdPair, HashMap<Integer, HashSet<Integer>>> validEntryPairs = new HashMap<>();

        while (rs.next()) {

            int metricId = rs.getInt("id");
            //int applicationId = rs.getInt("application_id");

            Statement metricDropStmt = localConn.createStatement();
            int dropCount = metricDropStmt.executeUpdate("delete from metricdata_min where metric_id = " + metricId);

            Statement metricDropStmt1 = localConn.createStatement();
            int dropCount1 = metricDropStmt1.executeUpdate("delete from metricdata_hour where metric_id = " + metricId);

            Statement tierStmt = localConn.createStatement();
            ResultSet tier_id_result = tierStmt.executeQuery("select id from application_component where application_id = " + applicationId);

            HashMap<Integer, HashSet<Integer>> tierNodeMap = new HashMap<>();
            while (tier_id_result.next()) {
                int tierId = tier_id_result.getInt("id");

                Statement nodeStmt = localConn.createStatement();
                ResultSet nodeResult = nodeStmt.executeQuery("select id from application_component_node where application_component_id = " + tierId);

                HashSet<Integer> nodeIdSet = new HashSet<>();
                while (nodeResult.next()) {
                    int nodeId = nodeResult.getInt("id");
                    nodeIdSet.add(nodeId);
                }
                tierNodeMap.put(tierId, nodeIdSet);
            }
            validEntryPairs.put(new MetricIdAppIdPair(metricId, applicationId), tierNodeMap);
        }


        Random r = new Random();


        for (MetricIdAppIdPair metricIdAppIdPair : validEntryPairs.keySet()) {

            int cluster_rollup_type = 0;// 0 or 1 from MetricClusterRollupType
            int rollup_type = 0;// 0 , 1 or 2 from MetricTimeRollupType

            int metricId = metricIdAppIdPair.getMetricId();
            HashMap<Integer, HashSet<Integer>> tierMap = validEntryPairs.get(metricIdAppIdPair);
            for (Integer tierId : tierMap.keySet()) {
                HashSet<Integer> nodeSet = tierMap.get(tierId);
                for (Integer nodeId : nodeSet) {
                    int minWindow = 0;
                    int valueWindow = 0;
                    int maxValue = 0;
                    int minValue = 0;
                    long hrSum = 0;
                    long hrSqrSum = 0;
                    int hrMax = -1;
                    int hrMin = Integer.MAX_VALUE;
                    for (long tmp_currmin = startTimeHrRound + 1; tmp_currmin <= minutesRound; tmp_currmin++) {

                        if (minWindow == 0) {
                            minWindow = getRandomNumberInRange(r, 5, 9);
                            valueWindow = valueWindow + 1 + getRandomNumberInRange(r, 0, 1);
                            valueWindow %= 3;
                            switch (valueWindow) {
                                case 0:
                                    minValue = 5;
                                    maxValue = 45;
                                    break;
                                case 1:
                                    minValue = 55;
                                    maxValue = 65;
                                    break;
                                case 2:
                                    minValue = 75;
                                    maxValue = 95;
                            }
                        } else {
                            minWindow--;
                        }


                        java.util.Date dt = new java.util.Date(tmp_currmin * 60 * 1000);
                        //Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        Calendar cl = Calendar.getInstance();
                        cl.setTime(dt);
                        int hrOfDay = cl.get(Calendar.HOUR_OF_DAY);
                        int dayOfWeek = cl.get(Calendar.DAY_OF_WEEK) - 1;
                        int hrOfWeek = dayOfWeek * 24 + hrOfDay;
                        int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH) - 1;
                        int hrOfMonth = dayOfMonth * 24 + hrOfDay;

                        int metricValue = getRandomNumberInRange(r, minValue, maxValue),
                                sum_val = metricValue,
                                count_val = 1,
                                min_val = metricValue,
                                max_val = metricValue,
                                cur_val = metricValue;

                        hrSum += metricValue;
                        hrSqrSum += metricValue * metricValue;
                        if (hrMin > metricValue) {
                            hrMin = metricValue;
                        }
                        if (hrMax < metricValue) {
                            hrMax = metricValue;
                        }
                        if (tmp_currmin % 60 == 0) {
                            metricdataHourBwr.write(tmp_currmin + "," + metricId + "," + rollup_type + "," + cluster_rollup_type + "," + nodeId + "," + tierId + "," + applicationId
                                    + "," + hrSum + "," + 60 + "," + hrMin
                                    + "," + hrMax + "," + cur_val + "," + hrSqrSum + "," + hrSum
                                    + "," + hrOfDay + "," + hrOfWeek + "," + hrOfMonth);
                            metricdataHourBwr.write("\n");

                            hrSum = 0;
                            hrSqrSum = 0;
                            hrMax = -1;
                            hrMin = Integer.MAX_VALUE;
                        }

                        // populate metricdata_min for current combination of metricid, nodeid, tierid and applicationid combination.
                        if (tmp_currmin >= startTimeMinRound) {
                            metricdataMinBwr.write(tmp_currmin + "," + metricId + "," + rollup_type + "," + cluster_rollup_type + "," + nodeId + "," + tierId + "," + applicationId
                                    + "," + count_val + "," + sum_val + "," + min_val
                                    + "," + max_val + "," + cur_val);
                            metricdataMinBwr.write("\n");
                        }
                    }
                }
            }

        }

        metricdataMinBwr.close();
        metricdataHourBwr.close();

        String sql = "LOAD DATA CONCURRENT LOCAL INFILE 'metricdata_min.txt'  " + "INTO TABLE " + "metricdata_min" + " "
                + "FIELDS TERMINATED BY ',' "
                + "(ts_min, metric_id, rollup_type, cluster_rollup_type, node_id, application_component_instance_id, " +
                "application_id, count_val, sum_val, min_val, max_val, cur_val)";

        insertStmt.executeUpdate(sql);

        sql = "LOAD DATA CONCURRENT LOCAL INFILE 'metricdata_hour.txt'  " + "INTO TABLE " + "metricdata_hour" + " "
                + "FIELDS TERMINATED BY ',' "
                + "(ts_min, metric_id, rollup_type, cluster_rollup_type, node_id, application_component_instance_id, " +
                "application_id, sum_val, count_val, min_val, max_val, cur_val , weight_value_square, weight_value, hr_of_day, hr_of_week, hr_of_month)";

        insertStmt.executeUpdate(sql);

        insertStmt.close();
        localConn.close();
        System.out.println("End time: " + System.currentTimeMillis());

    }

    private static int getRandomNumberInRange(Random r, int min, int max) {

        return r.ints(min, max + 1).findFirst().getAsInt();

    }
}


