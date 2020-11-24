package in.gore;

import in.gore.util.MetricIdAppIdPair;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;

/**
 * Use properties file in src/main/resources/DataGeneratorV2.properties to configure host details
 * This main class populates data in metricdata_hour, metricdata_hour_agg and metricdata_hour_agg_app
 * for last specified months. Default is 6 months.
 */
public class DataGeneratorV2 {

    public static void main(String args[]) throws Exception {

        System.out.println("Start time: " + System.currentTimeMillis());

        Properties dataGenProps = new Properties();
        dataGenProps.load(ClassLoader.getSystemResourceAsStream("DataGeneratorV2.properties"));

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection localConn = DriverManager.getConnection(
                "jdbc:mysql://" + dataGenProps.getProperty("host")+":" + dataGenProps.getProperty("DBPort") +
                        "/controller?user=" + dataGenProps.getProperty("DBUser")+"&password=" + dataGenProps.getProperty("DBPassword")+"&port=3306&allowUrlInLocalInfile=true");

        long timeInMilli = System.currentTimeMillis();
        long minutesRoundToHR = ((timeInMilli / (1000 * 60)) / 60) * 60;
        long sixMonthBackMinutes = minutesRoundToHR - (24 * 60 * Integer.parseInt(dataGenProps.getProperty("MonthsToGeneratePastBaselineFor")) * 30);
        long backtoseconds = minutesRoundToHR * 60;
        long sixMonthBackSeconds = sixMonthBackMinutes * 60;

        Statement stmt = localConn.createStatement();
        stmt.executeUpdate("TRUNCATE TABLE metricdata_hour");
        stmt.executeUpdate("TRUNCATE TABLE metricdata_hour_agg");
        stmt.executeUpdate("TRUNCATE TABLE metricdata_hour_agg_app");
        stmt.close();

        Statement appStmt = localConn.createStatement();
        ResultSet rs = appStmt.executeQuery("select * from metric");

        File metricdata_hour_file = new File("metricdata_hour.txt");
        if(metricdata_hour_file.exists()) FileUtils.forceDelete(metricdata_hour_file);
        File metricdata_hour_agg_file = new File("metricdata_hour_agg.txt");
        if(metricdata_hour_agg_file.exists()) FileUtils.forceDelete(metricdata_hour_agg_file);
        File metricdata_hour_agg_app_file = new File("metricdata_hour_agg_app.txt");
        if(metricdata_hour_agg_app_file.exists()) FileUtils.forceDelete(metricdata_hour_agg_app_file);

        FileWriter metricdataHourWriter = new FileWriter("metricdata_hour.txt");
        BufferedWriter metricdataHourBwr = new BufferedWriter(metricdataHourWriter);

        FileWriter metricdata_hour_aggWriter = new FileWriter("metricdata_hour_agg.txt");
        BufferedWriter metricdata_hour_aggBwr = new BufferedWriter(metricdata_hour_aggWriter);

        FileWriter metricdata_hour_agg_appWriter = new FileWriter("metricdata_hour_agg_app.txt");
        BufferedWriter metricdata_hour_agg_appBwr = new BufferedWriter(metricdata_hour_agg_appWriter);

        String query;
        Statement insertStmt = localConn.createStatement();


        HashMap<MetricIdAppIdPair, HashMap<Integer, HashSet<Integer>>> validEntryPairs = new HashMap<>();

        while (rs.next()) {

            int metricId = rs.getInt("id");
            int applicationId = rs.getInt("application_id");

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


        for (long tmp_currmin = minutesRoundToHR - 60; tmp_currmin >= sixMonthBackMinutes; tmp_currmin = tmp_currmin - 60) {


            java.util.Date dt = new java.util.Date(tmp_currmin * 60 * 1000);
            //Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar cl = Calendar.getInstance();
            cl.setTime(dt);
            int hrOfDay = cl.get(Calendar.HOUR_OF_DAY);
            int dayOfWeek = cl.get(Calendar.DAY_OF_WEEK) - 1;
            int hrOfWeek = dayOfWeek * 24 + hrOfDay;
            int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH) - 1;
            int hrOfMonth = dayOfMonth * 24 + hrOfDay;

            for (MetricIdAppIdPair metricIdAppIdPair : validEntryPairs.keySet()) {

            Random r = new Random();
            int cluster_rollup_type = getRandomNumberInRange(r, 0, 1); // 0 or 1 from MetricClusterRollupType
            int rollup_type = getRandomNumberInRange(r, 0, 2); // 0 , 1 or 2 from MetricTimeRollupType

            boolean hasAppBeenInserted = false;
            int metricId = metricIdAppIdPair.getMetricId();
            int applicationId = metricIdAppIdPair.getApplicationId();
            HashMap<Integer, HashSet<Integer>> tierMap = validEntryPairs.get(metricIdAppIdPair);
            for (Integer tierId : tierMap.keySet()) {
                boolean hasTierBeenInserted = false;
                HashSet<Integer> nodeSet = tierMap.get(tierId);
                for (Integer nodeId : nodeSet) {

                        int group_count_val = getRandomNumberInRange(r, 0, 20000),
                                sum_val = getRandomNumberInRange(r, 0, 20000),
                                count_val = getRandomNumberInRange(r, 0, 20000),
                                min_val = getRandomNumberInRange(r, 0, 20000),
                                max_val = getRandomNumberInRange(r, 0, 20000),
                                cur_val = getRandomNumberInRange(r, 0, 20000),
                                weight_value_square = getRandomNumberInRange(r, 0, 20000),
                                weight_value = getRandomNumberInRange(r, 0, 20000);


                        if (!hasAppBeenInserted) {
                            //populate metricdata_hour_agg_app table for this metricid and applicationid combination
                            metricdata_hour_agg_appBwr.write(tmp_currmin + "," + metricId + "," + rollup_type + "," + cluster_rollup_type + "," + applicationId
                                    + "," + group_count_val + "," + sum_val + "," + count_val + "," + min_val
                                    + "," + max_val + "," + cur_val + "," + weight_value_square + "," + weight_value
                                    + "," + hrOfDay + "," + hrOfWeek + "," + hrOfMonth);
                            metricdata_hour_agg_appBwr.write("\n");

                            hasAppBeenInserted = true;
                        }

                        if (!hasTierBeenInserted) {
                            //populate metricdata_hour_agg with this metricid, tierid and applicationid combination
                            metricdata_hour_aggBwr.write(tmp_currmin + "," + metricId + "," + rollup_type + "," + cluster_rollup_type + "," + tierId + "," + applicationId
                                    + "," + group_count_val + "," + sum_val + "," + count_val + "," + min_val
                                    + "," + max_val + "," + cur_val + "," + weight_value_square + "," + weight_value
                                    + "," + hrOfDay + "," + hrOfWeek + "," + hrOfMonth);
                            metricdata_hour_aggBwr.write("\n");
                            hasTierBeenInserted = true;
                        }

                        // populate metricdata_hour for current combination of metricid, nodeid, tierid and applicationid combination.
                        metricdataHourBwr.write(tmp_currmin + "," + metricId + "," + rollup_type + "," + cluster_rollup_type + "," + nodeId + "," + tierId + "," + applicationId
                                + "," + sum_val + "," + count_val + "," + min_val
                                + "," + max_val + "," + cur_val + "," + weight_value_square + "," + weight_value
                                + "," + hrOfDay + "," + hrOfWeek + "," + hrOfMonth);
                        metricdataHourBwr.write("\n");
                    }
                }
            }

        }

        metricdataHourBwr.close();
        metricdata_hour_aggBwr.close();
        metricdata_hour_agg_appBwr.close();

        String sql = "LOAD DATA CONCURRENT LOCAL INFILE 'metricdata_hour.txt'  " + "INTO TABLE " + "metricdata_hour" + " "
                + "FIELDS TERMINATED BY ',' "
                + "(ts_min, metric_id, rollup_type, cluster_rollup_type, node_id, application_component_instance_id, " +
                "application_id, sum_val, count_val, min_val, max_val, cur_val , weight_value_square, weight_value, hr_of_day, hr_of_week, hr_of_month)";

        insertStmt.executeUpdate(sql);

        sql = "LOAD DATA CONCURRENT LOCAL INFILE 'metricdata_hour_agg.txt'  " + "INTO TABLE " + "metricdata_hour_agg" + " "
                + "FIELDS TERMINATED BY ',' "
                + "(ts_min, metric_id, rollup_type, cluster_rollup_type, application_component_instance_id, application_id, " +
                "group_count_val, sum_val, count_val, min_val, max_val, cur_val, weight_value_square, weight_value , hr_of_day, hr_of_week, hr_of_month)";

        insertStmt.executeUpdate(sql);

        sql = "LOAD DATA CONCURRENT LOCAL INFILE 'metricdata_hour_agg_app.txt'  " + "INTO TABLE " + "metricdata_hour_agg_app" + " "
                + "FIELDS TERMINATED BY ',' "
                + "(ts_min, metric_id, rollup_type, cluster_rollup_type,  application_id, group_count_val, sum_val, " +
                "count_val, min_val, max_val, cur_val, weight_value_square, weight_value, hr_of_day, hr_of_week, hr_of_month)";

        insertStmt.executeUpdate(sql);


        insertStmt.close();
        localConn.close();
        System.out.println("End time: " + System.currentTimeMillis());

    }

    private static int getRandomNumberInRange(Random r, int min, int max) {

        return r.ints(min, max + 1).findFirst().getAsInt();

    }
}


