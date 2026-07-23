package com.vzap.trytons.dao.shared;

import com.vzap.trytons.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DBConnectionManager.getConnection();
    }
}
