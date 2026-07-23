package com.vzap.trytons.dao.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.vzap.trytons.dao.shared.BaseDAO;

public class DatabaseTestDAO extends BaseDAO {

    public List<String> getTableNames() throws SQLException {
        List<String> tables = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {

            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        }

        return tables;
    }
}
