package org.docbook.interfaces;

import java.util.List;

public interface ICrud<T> {
    void create(T t);
    T read(int id);
    List<T> getAll();
    void update(T t);
    void delete(int id);
}