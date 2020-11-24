package in.gore;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

/**
 Account - select id, name from account;

 Account -> Applications; select account_id, id, name from application group by account_id;

 Account -> Machines (SIM_nodes); select account_id, id, name from machine_instance group by account_id;

 Application -> Tiers; select application_id, id, name from application_component group by application_id;

 Tier -> Nodes; select application_component_id, id, name from application_component_node group by application_component_id;

 Machine -> Nodes; select machine_instance_id, id, name from application_component_node group by machine_instance_id;

 Application -> BTs; select application_id, id, name from abusiness_transaction group by application_id;

 Tier->Backend: Unresolved_backend_call_info:

 Tier -> BT_Summary (Vertex - Tier, Edges - BT <-> Tier, where Tier participates in a BT)
 BT -> BT_Segments:
 ----

 for (bt_id in bt_ids_of_one_app) {
 metrics = format("select * from metric where name like 'BTM|BTs|BT:%d|%'", bt_id);
 for (metric in metrics) {
 parse(metric_name)
 }
 }

 parse (metric_name) {
 bt_id -> participating tiers (bt_summary)
 bt_id -> bt_segment
 bt_segment -> bt_segment_node (comes from tier->nodes)

 also, bin the metric for all the above 3 objects.
 }

 BT_Segment_Node
 ----
 Sub-group of node-level metrics with BTM treeRootType

 ---
 Optional:
 db_server_backend_mapping (db_server_id -> backend_id -> id in unresolved_backend_call_info)

 =======

 Metric Binning (mainly based on metric_config_map)
 -----

 For BT, BT_Segment, BT_Summary, BT_Segment_node:
 Use metric name from metric table
 For BT_Summary, BT_Segment_Node - combination of above + below

 For application, tier, node:
 group by, config_entity_type + tree_root_type. each config_entity_id -> metric_ids
 If config_entity_type = APPLICATION_COMPONENT, metric_ids are automatically applicable to Nodes
 */
public class AppDTopologyForPerspica {

    final static String GET_ALL_ACCOUNTS = "SELECT id, name FROM account";

    final static String GET_ALL_APPLICATIONS_BY_ACCOUNT = "SELECT id, name FROM application WHERE " +
            "account_id = ";

    final static String GET_ALL_MACHINES_BY_ACCOUNT = "SELECT id, name FROM machine_instance WHERE " +
            "account_id = ";

    final static String GET_ALL_TIERS_BY_APPLICATION = "SELECT id, name FROM application_component " +
            "WHERE application_id = ";

    final static String GET_ALL_BACKENDS_BY_APPLICATION = "SELECT id, display_name FROM unresolved_backend_call_info " +
            "WHERE application_id = ";

    final static String GET_ALL_BTS_BY_APPLICATION = "SELECT id, name FROM abusiness_transaction " +
            "WHERE application_id = ";

    final static String GET_ALL_NODES_BY_TIER = "SELECT id, name FROM application_component_node " +
            "WHERE application_component_id = ";

    final static String GET_ALL_NODES_BY_MACHINE = "SELECT id, name FROM application_component_node " +
            "WHERE machine_instance_id = ";

    final static String GET_ALL_METRICS_BY_BT_ID = "SELECT id, name FROM metric WHERE name like 'BTM|BTs|BT:<bt_id>|%'";

    static HashMap<Long, String> fetchIdVsNameMapForEntity(Connection connection, String SQL) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(SQL);
        HashMap<Long, String> entities = new HashMap<>();
        while (rs.next()) {
            long entityId = rs.getInt(1);
            String entityName = rs.getString(2);
            entities.put(entityId, entityName);
        }
        return entities;
    }

    static void log(String msg) {
        System.out.println(msg);
    }

    static void fetchAllMetaInfoFromDB(Connection connection) throws SQLException {

        // get all the accounts
        HashMap<Long, String> accounts = fetchIdVsNameMapForEntity(connection, GET_ALL_ACCOUNTS);

        // account -> applications
        for (long accountId : accounts.keySet()) {
            log(String.format("=== Start Account %d -> Applications ===", accountId));
            HashMap<Long, String> applications = fetchIdVsNameMapForEntity(connection,
                    GET_ALL_APPLICATIONS_BY_ACCOUNT + accountId);
            for (long appId : applications.keySet()) {
                // application -> tiers
                log(String.format("=== Start Application %d -> Tiers, BTs ===", appId));
                HashMap<Long, String> tiers = fetchIdVsNameMapForEntity(connection,
                        GET_ALL_TIERS_BY_APPLICATION + appId);
                log(String.format("Found %d tiers in App %d", tiers.size(), appId));
                for (long tierId : tiers.keySet()) {
                    // tier -> nodes
                    log(String.format("=== Start Tier %d -> Nodes ===", tierId));
                    HashMap<Long, String> nodes = fetchIdVsNameMapForEntity(connection, GET_ALL_NODES_BY_TIER +
                            tierId);
                    log(String.format("Found %d nodes in Tier %d", nodes.size(), tierId));
                    log(String.format("=== End Tier %d -> Nodes ===", tierId));
                }

                // application -> bts
                HashMap<Long, String> bts = fetchIdVsNameMapForEntity(connection, GET_ALL_BTS_BY_APPLICATION + appId);
                log(String.format("Found %d BTs in App %d", bts.size(), appId));
                for (long btId : bts.keySet()) {
                    log(String.format("     === Start BT %d -> bt_summary, bt_segment ===", btId));
                    HashMap<Long, String> btMetrics = fetchIdVsNameMapForEntity(connection,
                            GET_ALL_METRICS_BY_BT_ID.replace("<bt_id>", "" + btId));
                    // bt -> bt_summary
                    // bt -> bt_segment
                    log(String.format("Found %d metrics for BT %d", btMetrics.size(), btId));
                    for (String metricName : btMetrics.values()) {
                        log(String.format("                 === Start processing %s ===", metricName));
                        log(String.format("                 === End processing %s ===", metricName));
                    }
                    log(String.format("     === End BT %d -> bt_summary, bt_segment ===", appId));
                }

                // application -> backends
                HashMap<Long, String> backends = fetchIdVsNameMapForEntity(connection, GET_ALL_BACKENDS_BY_APPLICATION +
                        appId);
                log(String.format("Found %d backends in App %d", backends.size(), appId));
                for (long backendId : backends.keySet()) {
                    // tier -> backend or node -> backend
                }

                log(String.format("=== End Application %d -> Tiers, BTs ===", appId));
            }
            log(String.format("=== End Account %d -> Applications ===", accountId));
        }

        // account -> machines
        for (long accountId : accounts.keySet()) {
            log(String.format("=== Start Account %d -> Machines ===", accountId));
            HashMap<Long, String> machines = fetchIdVsNameMapForEntity(connection, GET_ALL_MACHINES_BY_ACCOUNT + accountId);
            for (long machineId : machines.keySet()) {
                // machine -> nodes
                HashMap<Long, String> nodes = fetchIdVsNameMapForEntity(connection, GET_ALL_NODES_BY_MACHINE +
                        machineId);
            }
            log(String.format("=== End Account %d -> Machines ===", accountId));
        }
    }

    public static void main(String args[]) throws Exception {

        System.out.println("Start time: " + System.currentTimeMillis());
        Properties dataGenProps = new Properties();
        dataGenProps.load(ClassLoader.getSystemResourceAsStream("mysql_connection.properties"));

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://" + dataGenProps.getProperty("host") + ":" + dataGenProps.getProperty("DBPort") +
                            "/controller?user=" + dataGenProps.getProperty("DBUser") + "&password=" + dataGenProps
                            .getProperty("DBPassword") + "&port=3306&allowUrlInLocalInfile=true")) {
            fetchAllMetaInfoFromDB(connection);
        }
        System.out.println("End time: " + System.currentTimeMillis());

    }
}
