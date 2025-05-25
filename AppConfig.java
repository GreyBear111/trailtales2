package com.trailtales.config;

import com.trailtales.repository.*;
import com.trailtales.util.DatabaseInitializer;
import com.trailtales.util.EmailService;
import com.trailtales.util.PasswordHasher;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.trailtales") // Скануємо всі компоненти в com.trailtales
@EnableTransactionManagement
@PropertySource("classpath:application.properties") // Завантажуємо application.properties
public class AppConfig {

  @Value("${spring.datasource.url}")
  private String datasourceUrl;

  @Value("${spring.datasource.username}")
  private String datasourceUsername;

  @Value("${spring.datasource.password}")
  private String datasourcePassword;

  @Bean
  public DataSource dataSource() {

    // НОВИЙ КОД для HikariCP:
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(datasourceUrl);
    config.setUsername(datasourceUsername);
    config.setPassword(datasourcePassword);
    // Додаткові налаштування для продуктивності (опційно, але рекомендовано)
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    return new HikariDataSource(config);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public PlatformTransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public PasswordHasher passwordHasher() {
    return new PasswordHasher();
  }

  @Bean
  public EmailService emailService() {
    return new EmailService();
  }

  @Bean
  public Validator validator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      return factory.getValidator();
    }
  }

  // Repositories
  @Bean
  public UserRepository userRepository(JdbcTemplate jdbcTemplate, RoleRepository roleRepository) {
    return new UserRepository(jdbcTemplate, roleRepository);
  }

  @Bean
  public RoleRepository roleRepository(JdbcTemplate jdbcTemplate) {
    return new RoleRepository(jdbcTemplate);
  }

  @Bean
  public JourneyRepository journeyRepository(
      JdbcTemplate jdbcTemplate,
      LocationRepository locationRepository,
      UserRepository userRepository,
      TagRepository tagRepository,
      EventRepository eventRepository,
      PhotoRepository photoRepository) {
    return new JourneyRepository(
        jdbcTemplate,
        locationRepository,
        userRepository,
        tagRepository,
        eventRepository,
        photoRepository);
  }

  @Bean
  public EventRepository eventRepository(JdbcTemplate jdbcTemplate) {
    return new EventRepository(jdbcTemplate);
  }

  @Bean
  public LocationRepository locationRepository(JdbcTemplate jdbcTemplate) {
    return new LocationRepository(jdbcTemplate);
  }

  @Bean
  public PhotoRepository photoRepository(JdbcTemplate jdbcTemplate) {
    return new PhotoRepository(jdbcTemplate);
  }

  @Bean
  public TagRepository tagRepository(JdbcTemplate jdbcTemplate) {
    return new TagRepository(jdbcTemplate);
  }

  @Bean
  public DatabaseInitializer databaseInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
    return new DatabaseInitializer(dataSource, jdbcTemplate);
  }
}
