package org.docbook.interfaces;

import java.util.List;

public interface IService<T> {
    void create(T t) throws Exception;
    
    void add(T t) throws Exception;

    T readById(Integer id) throws Exception;

    List<T> readAll() throws Exception;

    List<T> getAll() throws Exception;

    T getById(int id) throws Exception;

    void update(T t) throws Exception;

    void delete(Integer id) throws Exception;

    void delete(int id) throws Exception;
}