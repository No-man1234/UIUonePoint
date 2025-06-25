package com.example.uiuonepoint.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DAO<T> {
    Optional<T> get(int id) throws SQLException;
    List<T> getAll() throws SQLException;
    int save(T t) throws SQLException;
    void update(T t) throws SQLException;
    void delete(int id) throws SQLException;
} 