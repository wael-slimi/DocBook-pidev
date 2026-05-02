package org.docbook.interfaces;

import java.util.List;

public interface IService<T> {
    /**
     * Ajoute une entite.
     */
    void add(T t);

    /**
     * Met a jour une entite existante.
     */
    void update(T t);

    /**
     * Supprime une entite par identifiant.
     */
    void delete(int id);

    /**
     * Retourne toutes les entites.
     */
    List<T> getAll();

    /**
     * Retourne une entite par identifiant.
     */
    T getById(int id);
}


