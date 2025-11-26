package com.back.say.controller;

import com.back.say.repository.DbSayRepository;
import com.back.say.repository.SayRepository;
import com.back.standard.util.DbConnectionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbSayControllerTest extends AbstractSayControllerTest {

    @Override
    protected SayRepository createRepository() {
        return new DbSayRepository();
    }
    @BeforeAll
    static void beforeAll() {
        try (Connection conn = DbConnectionUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS say");
            stmt.execute("""
                    CREATE TABLE say (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        content VARCHAR(255) NOT NULL,
                        author VARCHAR(255) NOT NULL
                    )
                    """);

            } catch (SQLException e) {
            throw new RuntimeException("테스트 DB 초기화 실패", e);
        }
    }

    @BeforeEach
    void clearDbBeforeEach() {
        clearTable();
    }

    @AfterEach
    void clearDbAfterEach() {
        clearTable();
    }

    private void clearTable() {
        try (Connection conn = DbConnectionUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("TRUNCATE TABLE say");
            stmt.execute("ALTER TABLE say AUTO_INCREMENT = 1");

        } catch (SQLException e) {
            throw new RuntimeException("clearTable 테스트용 DB 초기화 실패", e);
        }
    }
}
