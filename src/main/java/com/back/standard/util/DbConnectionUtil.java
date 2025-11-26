package com.back.standard.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//docker exec -it mysql-1 mysql -u USERNAME -pPASSWORD
public class DbConnectionUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/wise_saying";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "lldj123414";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver 로딩 실패", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("DB 연결 실패", e);
        }
    }
}
