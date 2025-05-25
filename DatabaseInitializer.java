package com.trailtales.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseInitializer {

  private final DataSource dataSource;

  public DatabaseInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
    this.dataSource = dataSource;
  }

  public void initialize() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      connection.setAutoCommit(false);

      System.out.println("Виконання скрипту ddl.sql...");
      executeScript(connection, "ddl.sql");

      System.out.println("Виконання скрипту dml.sql...");
      executeScript(connection, "dml.sql");

      connection.commit();
      System.out.println("База даних успішно ініціалізована та дані зафіксовано.");

    } catch (Exception e) {
      if (connection != null) {
        try {
          connection.rollback();
          System.err.println("Транзакцію ініціалізації бази даних відкочено.");
        } catch (SQLException ex) {
          System.err.println("Помилка відкочування транзакції: " + ex.getMessage());
        }
      }
      System.err.println("Помилка ініціалізації бази даних: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Помилка ініціалізації бази даних", e);
    } finally {
      if (connection != null) {
        try {
          connection.setAutoCommit(true);
          connection.close();
        } catch (SQLException e) {
          System.err.println("Помилка закриття з'єднання: " + e.getMessage());
        }
      }
    }
  }

  private void executeScript(Connection connection, String scriptFileName) throws SQLException, IOException {
    Resource resource = new ClassPathResource(scriptFileName);
    if (scriptFileName.equals("ddl.sql")) {
      String scriptContent;
      try (BufferedReader reader =
                   new BufferedReader(
                           new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
        scriptContent = reader.lines().collect(Collectors.joining("\n"));
      }
      try (Statement statement = connection.createStatement()) {
        statement.execute(scriptContent);
      }
    } else {
      ScriptUtils.executeSqlScript(connection, resource);
    }
    System.out.println("Скрипт " + scriptFileName + " успішно виконано.");
  }
}