package ru.itis.homework;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class EntityManager {
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public EntityManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // createTable("account", User.class);
    public <T> void createTable(String tableName, Class<T> entityClass) {
        // сгенерировать CREATE TABLE на основе класса
        // create table account ( id integer, firstName varchar(255), ...))
        try {
            StringBuilder createdTable = new StringBuilder();
            createdTable.append("create table ").append(tableName).append("( ");

            Class<?> aClass = Class.forName(entityClass.getName());
            Field[] fields = aClass.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                String type;

                switch (fields[i].getType().getSimpleName()) {
                    case "Long":
                        type = "bigint";
                        break;
                    case "String":
                        type = "VARCHAR(45)";
                        break;
                    default:
                        type = fields[i].getType().getSimpleName();
                        break;
                }

                if (i != fields.length - 1) {
                    createdTable.append(fields[i].getName().toLowerCase()).append(" ").append(type).append(", ");
                } else {
                    createdTable.append(fields[i].getName().toLowerCase()).append(" ").append(type).append(" ");
                }
            }

            createdTable.append(");");
            System.out.println(createdTable);
            jdbcTemplate.execute(String.valueOf(createdTable));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //insert into student (first_name, last_name, age, group_number) values ('Марсель', 'Сидиков', 26, 915);
    public void save(String tableName, Object entity) {
        try {
            Class<?> classOfEntity = entity.getClass();

            StringBuilder saveValues = new StringBuilder();
            saveValues.append("INSERT INTO ").append(tableName).append(" (");
            StringBuilder values = new StringBuilder();

            Field[] fields = classOfEntity.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (i != fields.length - 1) {
                    saveValues.append(fields[i].getName().toLowerCase()).append(", ");
                    for (Method method : classOfEntity.getDeclaredMethods()) {
                        if (method.getReturnType().getName().equals(fields[i].getType().getName())) {
                            if (method.getName().toLowerCase().contains(fields[i].getName().toLowerCase())) {
                                Object result = method.invoke(entity);
                                if (method.getReturnType().getSimpleName().equals("String")) {
                                    values.append("'").append(result).append("'");
                                } else {
                                    values.append(result);
                                }
                                values.append(", ");
                            }
                        }
                    }
                } else {
                    saveValues.append(fields[i].getName().toLowerCase());
                    for (Method method : classOfEntity.getDeclaredMethods()) {
                        if (method.getName().toLowerCase().contains(fields[i].getName().toLowerCase())) {
                            Object result = method.invoke(entity);
                            if (method.getReturnType().getSimpleName().equals("String")) {
                                values.append("'").append(result).append("'");
                            } else {
                                values.append(result);
                            }
                        }
                    }
                }
            }
            saveValues.append(") ").append("values").append(" (");
            saveValues.append(values.toString());
            saveValues.append(");");

            System.out.println(saveValues);
            jdbcTemplate.execute(String.valueOf(saveValues));

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // User user = entityManager.findById("account", User.class, Long.class, 10L);
    public <T, ID> T findById(String tableName, Class<T> resultType, Class<ID> idType, ID idValue) {
        // сгенеририровать select
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            StringBuilder selectById = new StringBuilder();
            selectById.append("SELECT * FROM ").append(tableName).append(" WHERE id = ").append(idValue).append(";");

            Class<?> aClass = Class.forName(resultType.getName());

            Class[] fieldsClasses = new Class[aClass.getDeclaredFields().length];
            Field[] fields = aClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fieldsClasses[i] = fields[i].getType();
            }
            Constructor constructor = aClass.getConstructor(fieldsClasses);

            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(String.valueOf(selectById));

            if (resultSet.next()) {
                Object[] args1 = new Object[fields.length];
                for (int i = 0; i < args1.length; i++) {
                    args1[i] = resultSet.getObject(i + 1);
                }
                return (T) constructor.newInstance(args1);
                } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
