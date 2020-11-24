package in.gore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;

public class DataGenerator {

    public static void main(String args[]) throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection localConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/controller?user=controller&password=controller&port=3306&allowUrlInLocalInfile=true");
        PreparedStatement pstmt = null;

        Statement stmt = localConn.createStatement();
        int count = 0;

        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_min");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_min_agg");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_min_agg_app");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_ten_min");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_ten_min_agg");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_ten_min_agg_app");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_hour");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_hour_agg");
        count = stmt.executeUpdate("TRUNCATE TABLE metricdata_hour_agg_app");
        stmt.close();

        long currUTCTimeMin = System.currentTimeMillis() / (1000*60);
        // go back 6 months
        long startTimeMin = currUTCTimeMin - 24*60*180;
        String query;
        Random r =new Random();
        int metricId = 1373;
        int applicationId = 10;
        int tierId = 14;
        int nodeId = 13;
        for (long i = startTimeMin; i <= currUTCTimeMin; i++) {
            query = "INSERT INTO metricdata_min (ts_min, metric_id, rollup_type, cluster_rollup_type, node_id, " +
                    "application_component_instance_id, application_id, sum_val, count_val, min_val, max_val, cur_val)  " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?) ";

            // generate a random value between 10 and 100
            int value = r.nextInt(100 - 10) + 10;

            pstmt = localConn.prepareStatement(query);
            pstmt.setLong(1, i);
            pstmt.setInt(2, metricId);
            pstmt.setInt(3, 1);
            pstmt.setInt(4, 1);
            pstmt.setInt(5, nodeId);
            pstmt.setInt(6, tierId);
            pstmt.setInt(7, applicationId);
            pstmt.setInt(8, value);
            pstmt.setInt(9, value);
            pstmt.setInt(10, value);
            pstmt.setInt(11,value);
            pstmt.setInt(12,value);
            pstmt.executeUpdate();
            pstmt.close();
        }

        stmt = localConn.createStatement();

        query =
                "INSERT INTO metricdata_min_agg (ts_min, metric_id, application_component_instance_id, application_id, "
                        +
                        "  group_count_val, sum_val, count_val, min_val, max_val, cur_val)  " +
                        "SELECT  ts_min, metric_id,  application_component_instance_id, MAX(application_id), COUNT(*), SUM(sum_val), "
                        +
                        "  SUM(count_val), " +
                        "  MIN(CASE rollup_type WHEN 0 THEN min_val WHEN 1 THEN sum_val WHEN 2 THEN cur_val ELSE 0 END), "
                        +
                        "  MAX(CASE rollup_type WHEN 0 THEN max_val WHEN 1 THEN sum_val WHEN 2 THEN cur_val ELSE 0 END), "
                        +
                        "  SUM(cur_val) " +
                        "FROM metricdata_min MD " +
                        "GROUP BY  ts_min, metric_id, application_component_instance_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_min_agg");

        query = "INSERT INTO metricdata_min_agg_app (ts_min, metric_id,  application_id, " +
                "  group_count_val, sum_val, count_val, min_val, max_val, cur_val)  " +
                "SELECT  ts_min, metric_id,  MAX(application_id), SUM(group_count_val), SUM(sum_val), " +
                "  SUM(count_val), " +
                "  MIN(min_val), " +
                "  MAX(max_val), " +
                "  SUM(cur_val) " +
                "FROM metricdata_min_agg MD " +
                "GROUP BY  ts_min, metric_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_min_agg_app");


        query = "INSERT IGNORE INTO metricdata_ten_min\n" +
                "(ts_min, metric_id, rollup_type, cluster_rollup_type,node_id, application_component_instance_id, application_id, sum_val, count_val, min_val, max_val, cur_val, weight_value_square, weight_value)  \n" +
                "\n" +
                "SELECT  ((ts_min DIV 10) * 10) min_ts, metric_id, MAX(rollup_type) rollup_type, MAX(cluster_rollup_type) cluster_rollup_type,     node_id, application_component_instance_id, MAX(application_id) application_id,    SUM(sum_val) sum_val, sum(count_val) count_val, min(min_val) min_val, max(max_val) max_val,    \n" +
                "SUBSTRING_INDEX(GROUP_CONCAT(CAST(cur_val AS CHAR) ORDER BY ts_min DESC), ',', 1) as last_val,    \n" +
                "SUM(CASE rollup_type WHEN 0 THEN POW(IFNULL(sum_val / count_val,0),2) * count_val WHEN 1 THEN POW(sum_val,2) * count_val WHEN 2 THEN POW(cur_val,2) * count_val ELSE 0 END) weight_value_square,      \n" +
                "SUM(CASE rollup_type WHEN 0 THEN IFNULL(sum_val / count_val,0) * count_val WHEN 1 THEN sum_val * count_val WHEN 2 THEN cur_val * count_val ELSE 0 END) weight_value   \n" +
                "FROM metricdata_min USE INDEX (PRIMARY)   WHERE metric_id  = %d AND ts_min BETWEEN %d AND %d \n" +
                "GROUP BY metric_id, node_id, min_ts, application_component_instance_id   \n" +
                "ORDER BY metric_id, node_id, min_ts";

        query = String.format(query, metricId, startTimeMin, currUTCTimeMin);
        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_ten_min");


        query =
                "INSERT INTO metricdata_ten_min_agg (ts_min, metric_id, application_component_instance_id,  application_id, "
                        +
                        "  group_count_val, sum_val, count_val, min_val, max_val, cur_val)  " +
                        "SELECT  ts_min, metric_id,  application_component_instance_id, MAX(application_id), COUNT(*), SUM(sum_val), "
                        +
                        "  SUM(count_val), " +
                        "  MIN(CASE rollup_type WHEN 0 THEN min_val WHEN 1 THEN sum_val WHEN 2 THEN cur_val ELSE 0 END), "
                        +
                        "  MAX(CASE rollup_type WHEN 0 THEN max_val WHEN 1 THEN sum_val WHEN 2 THEN cur_val ELSE 0 END), "
                        +
                        "  SUM(cur_val) " +
                        "FROM metricdata_ten_min MD " +
                        "GROUP BY  ts_min, metric_id, application_component_instance_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_ten_min_agg");


        query = "INSERT INTO metricdata_ten_min_agg_app (ts_min, metric_id,  application_id, " +
                "  group_count_val, sum_val, count_val, min_val, max_val, cur_val)  " +
                "SELECT  ts_min, metric_id,  MAX(application_id), SUM(group_count_val), SUM(sum_val), " +
                "  SUM(count_val), " +
                "  MIN(min_val), " +
                "  MAX(max_val), " +
                "  SUM(cur_val) " +
                "FROM metricdata_ten_min_agg MD " +
                "GROUP BY  ts_min, metric_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_ten_min_agg_app");


        query = "INSERT INTO metricdata_hour (ts_min, metric_id, rollup_type, cluster_rollup_type, node_id, " +
                "application_component_instance_id, application_id, sum_val, count_val, min_val, max_val, cur_val , weight_value_square, weight_value, hr_of_day, hr_of_week, hr_of_month)  "
                +
                "SELECT MD.min_ts, MD.metric_id, MD.rollup_type, MD.cluster_rollup_type, MD.node_id, MD.application_component_instance_id, MD.application_id, "
                +
                "MD.sum_val,MD.count_val,MD.min_val,MD.max_val, C.cur_val, MD.weight_value_square, MD.weight_value, MD.hr_of_day, MD.hr_of_week, MD.hr_of_month " +
                "FROM " +
                " metricdata_ten_min C  ,  " +
                " (SELECT @hr_starting_min := ((ts_min DIV 60) * 60) min_ts, MAX(ts_min) max_ts, metric_id, MAX(rollup_type) rollup_type, MAX(cluster_rollup_type) cluster_rollup_type, node_id, application_component_instance_id, MAX(application_id) application_id, "
                +
                "   SUM(sum_val) sum_val, SUM(count_val) count_val, min(min_val) min_val, max(max_val) max_val, SUM(weight_value_square) weight_value_square,  SUM(weight_value) weight_value,  "
                + "@hr := HOUR(@hr_in_default_tz := from_unixtime(@hr_starting_min * 60)) hr_of_day,"
                +  "@hr + (DAYOFWEEK(@hr_in_default_tz) - 1) * 24 hr_of_week,"
                + "@hr + (DAYOFMONTH(@hr_in_default_tz) - 1) * 24 hr_of_month"
                +
                "  FROM metricdata_ten_min " +
                "  GROUP BY  (ts_min DIV 60), metric_id, node_id, application_component_instance_id " +
                " ) MD " +
                "WHERE MD.max_ts = C.ts_min AND MD.metric_id = C.metric_id AND " +
                "  MD.node_id = C.node_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_hour ");

        query =
                "INSERT INTO metricdata_hour_agg (ts_min, metric_id, rollup_type, cluster_rollup_type, application_component_instance_id, application_id, "
                        +
                        "  group_count_val, sum_val, count_val, min_val, max_val, cur_val, weight_value_square, weight_value , hr_of_day, hr_of_week, hr_of_month) "
                        +
                        "SELECT  ts_min, metric_id, max(rollup_type), max(cluster_rollup_type),  application_component_instance_id, MAX(application_id), COUNT(*), SUM(sum_val), "
                        +
                        "  SUM(count_val),  " +
                        "  MIN(CASE rollup_type WHEN 0 THEN min_val WHEN 1 THEN sum_val WHEN 2 THEN cur_val ELSE 0 END), "
                        +
                        "  MAX(CASE rollup_type WHEN 0 THEN max_val WHEN 1 THEN sum_val WHEN 2 THEN cur_val ELSE 0 END), "
                        +
                        "  SUM(cur_val), SUM(weight_value_square), SUM(weight_value), max(hr_of_day), max(hr_of_week), max(hr_of_month)  " +
                        "FROM metricdata_hour MD " +
                        "GROUP BY  ts_min, metric_id, application_component_instance_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_hour_agg ");

        query =
                "INSERT INTO metricdata_hour_agg_app (ts_min, metric_id, rollup_type, cluster_rollup_type,  application_id, " +
                        "  group_count_val, sum_val, count_val, min_val, max_val, cur_val, weight_value_square, weight_value, hr_of_day, hr_of_week, hr_of_month )  "
                        +
                        "SELECT  ts_min, metric_id, max(rollup_type), max(cluster_rollup_type), MAX(application_id), SUM(group_count_val), SUM(sum_val), " +
                        "  SUM(count_val), " +
                        "  MIN(min_val), " +
                        "  MAX(max_val), " +
                        "  SUM(cur_val), SUM(weight_value_square), SUM(weight_value), max(hr_of_day), max(hr_of_week), max(hr_of_month)  " +
                        "FROM metricdata_hour_agg MD " +
                        "GROUP BY  ts_min, metric_id ";

        count = stmt.executeUpdate(query);
        System.out.println("inserting  " + count + " rows into metricdata_hour_agg_app ");

        stmt.close();

        localConn.close();

    }


}
