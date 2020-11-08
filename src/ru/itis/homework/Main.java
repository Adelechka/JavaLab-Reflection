package ru.itis.homework;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Main {
    public static void main(String[] args) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setUsername("postgres");
        hikariConfig.setPassword("h8mfru6r");
        hikariConfig.setMaximumPoolSize(10);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        EntityManager entityManager = new EntityManager(dataSource);
        entityManager.createTable("table1", User.class);
        User user = new User(1L, "firstnameValue", "lastnameValue", true);
        entityManager.save("table1", user);
        User user1 = entityManager.findById("table1", User.class, Long.class, 1L);
        System.out.println("user from method: " + user1.toString());
    }
}
