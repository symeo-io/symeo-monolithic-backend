package io.symeo.monolithic.backend.infrastructure.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@EnableAutoConfiguration
public class SetupConfiguration extends BaseTestConfiguration {

  @Bean
  public DataSource hikariDataSource() {
    HikariConfig hikari = new HikariConfig();
    hikari.setJdbcUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
    hikari.setPassword("sa");
    hikari.setUsername("sa");
    hikari.setAutoCommit(true);
    hikari.setDriverClassName("org.h2.Driver");
    return new HikariDataSource(hikari);
  }

}
