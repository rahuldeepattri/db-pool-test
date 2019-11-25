package com.company.db_test.srv;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class TestController {

    @Autowired
    DataSource dataSource;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static String SCHEMA;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_READ_CONNECTION_URL;

    static {
        try {
            String DB_USERNAME = "";
            String DB_PASSWORD = "";
            String DB_HOST = "";
            String DB_PORT = "";


            JSONObject obj = new JSONObject(System.getenv("VCAP_SERVICES"));
            JSONArray arr = obj.getJSONArray("hana");
            DB_USERNAME = arr.getJSONObject(0).getJSONObject("credentials").getString("user");
            DB_PASSWORD = arr.getJSONObject(0).getJSONObject("credentials").getString("password");
            DB_HOST = arr.getJSONObject(0).getJSONObject("credentials").getString("host").split(",")[0];
            DB_PORT = arr.getJSONObject(0).getJSONObject("credentials").getString("port");


            TestController.SCHEMA = arr.getJSONObject(0).getJSONObject("credentials").getString("schema");
            TestController.DB_USERNAME = DB_USERNAME;
            TestController.DB_PASSWORD = DB_PASSWORD;
            TestController.DB_READ_CONNECTION_URL = "jdbc:sap://" + DB_HOST + ":" + DB_PORT;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/plain")
        @ResponseBody
        String home() {
            StringBuilder builder = new StringBuilder();
            builder.append("Hello World !!");

            builder.append("\n\nJDBC connection available: ");
            try (Connection conn = getConnection();) {

                if (conn != null) {
                    builder.append("yes");
                    builder.append("\n\nCurrent Hana DB user:\n");
                    String userName = getCurrentUser(conn);
                    builder.append(userName);
                    builder.append("\n\nCurrent Hana schema:\n");
                    builder.append(getCurrentSchema(conn));
                } else {
                    builder.append("no");
                }
            } catch (SQLException e) {
                builder.append("no");
            }

            return builder.toString();
        }

        @RequestMapping(value = "/loaderio-933c4ad366f9c3b9209b96fd7e687d02", method = RequestMethod.GET, produces = "text/plain")
        @ResponseBody
        String loader() {
            return "loaderio-933c4ad366f9c3b9209b96fd7e687d02";
        }
    */
    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    String test() {
        StringBuilder builder = new StringBuilder();
        try (Connection conn = getConnectionFromDataSource();) {

            if (conn != null) {
                List<Integer> tenIds = getTenIds();
                insertTenTimes(conn, tenIds);
                selectTenTimes(conn, tenIds);
                updateTenTimes(conn, tenIds);
                deleteTenTimes(conn, tenIds);
                builder.append("ok");
            } else {
                builder.append("no");
            }
        } catch (SQLException e) {
            builder.append(e.getMessage());
            builder.append("\nno");
        }

        return builder.toString();
    }

    @RequestMapping(value = "/insertRandomData", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    String insertRandomData() {
        StringBuilder builder = new StringBuilder();
        try (Connection conn = getConnection();) {

            if (conn != null) {
                for (int i = 0; i < 1000; i++) {

                    List<Integer> tenIds = getTenIds();
                    insertTenTimes(conn, tenIds);
                }
                builder.append("ok");
            } else {
                builder.append("no");
            }
        } catch (SQLException e) {
            builder.append(e.getMessage());
            builder.append("\nno");
        }

        return builder.toString();
    }

    private void updateTenTimes(Connection conn, List<Integer> tenIds) throws SQLException {
        ThreadLocalRandom threadRandom = ThreadLocalRandom.current();

        for (int i = 0; i < 10; i++) {

            try (PreparedStatement prepareStatement = conn.prepareStatement("update " + SCHEMA + "." + "\"db_test.db::TestTable\" set MEASURE1 = ? where ID = ?;");) {
                prepareStatement.setInt(1, threadRandom.nextInt());
                prepareStatement.setInt(2, tenIds.get(i));
                prepareStatement.executeUpdate();
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private List<Integer> getTenIds() {
        ThreadLocalRandom threadRandom = ThreadLocalRandom.current();
        return IntStream.range(0, 10).map(operand -> threadRandom.nextInt()).boxed().collect(Collectors.toList());
    }

    private void deleteTenTimes(Connection conn, List<Integer> tenIds) throws SQLException {
        for (int i = 0; i < 10; i++) {

            try (PreparedStatement prepareStatement = conn.prepareStatement("delete from " + SCHEMA + "." + "\"db_test.db::TestTable\" where ID = ?;");) {

                prepareStatement.setInt(1, tenIds.get(i));
                prepareStatement.executeUpdate();
            } catch (Exception e) {
                throw e;
            }
        }

    }

    private void selectTenTimes(Connection conn, List<Integer> tenIds) throws SQLException {
        for (int i = 0; i < 10; i++) {

            try (PreparedStatement prepareStatement = conn.prepareStatement("select  * from " + SCHEMA + "." + "\"db_test.db::TestTable\" where ID = ?");) {
                prepareStatement.setInt(1, tenIds.get(i));
                try (ResultSet resultSet = prepareStatement.executeQuery();) {
                }
            }
        }
    }

    private void insertTenTimes(Connection conn, List<Integer> tenIds) throws SQLException {
        ThreadLocalRandom threadRandom = ThreadLocalRandom.current();

        for (int i = 0; i < 10; i++) {

            try (PreparedStatement prepareStatement = conn.prepareStatement("insert into " + SCHEMA + "." + "\"db_test.db::TestTable\" values (?, ?, ?, ?, ?, ?, ?);");) {

                prepareStatement.setInt(1, tenIds.get(i));
                prepareStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
                prepareStatement.setString(3, randomString(1));
                prepareStatement.setString(4, randomString(16));
                prepareStatement.setString(5, randomString(4));
                prepareStatement.setInt(6, threadRandom.nextInt());
                prepareStatement.setDouble(7, threadRandom.nextDouble());

                prepareStatement.executeUpdate();
            } catch (Exception e) {
                throw e;
            }
        }

    }

    private String randomString(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

/*
    private String getCurrentUser(Connection conn) throws SQLException {
        String currentUser = "";
        PreparedStatement prepareStatement = conn.prepareStatement("SELECT CURRENT_USER \"current_user\" FROM DUMMY;");
        ResultSet resultSet = prepareStatement.executeQuery();
        int column = resultSet.findColumn("current_user");
        while (resultSet.next()) {
            currentUser += resultSet.getString(column);
        }
        return currentUser;
    }

    private String getCurrentSchema(Connection conn) throws SQLException {
        String currentSchema = "";
        PreparedStatement prepareStatement = conn.prepareStatement("SELECT CURRENT_SCHEMA \"current_schema\" FROM DUMMY;");
        ResultSet resultSet = prepareStatement.executeQuery();
        int column = resultSet.findColumn("current_schema");
        while (resultSet.next()) {
            currentSchema += resultSet.getString(column);
        }
        return currentSchema;
    }

 */

    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_READ_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    private Connection getConnectionFromDataSource() {
        try {

            return dataSource.getConnection();
//            return getConnection();

        } catch (Exception connection) {
            connection.printStackTrace();
            return null;
        }
    }

}
