package ro.ubbcluj.map.socialnetworkgui.service;

import ro.ubbcluj.map.socialnetworkgui.domain.Entity;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;

import java.io.FileNotFoundException;
import java.util.Optional;

/**
 * All the basic operations available to the client will be found here
 * @param <ID> type of the ID that the entity will have
 * @param <E> entity
 */
public interface Service<ID, E extends Entity<ID>> {

    /**
     * Saves the entity in the repository
     * @param e - Entity saved
     * @return - {@code Optional} which is
     *        null- if the given entity is saved or
     *        the entity (id already exists)
     * @throws IllegalArgumentException if the entity is null
     */
    Optional<E> add(E e);

    /**
     * Deletes the entity with the given ID
     * @param id - the id of the requested
     *           entity to be deleted
     * @return - {@code Optional} which is
     *          the deleted entity or null if there is not one
     *          with the given id
     * @throws IllegalArgumentException if id is null
     */
    Optional<E> delete(ID id) throws FileNotFoundException;

    /**
     * Updates the entity with the id of the given entity
     * with this new given entity
     * @param e - Entity
     *          entity must not be null
     * @return {@code Optional} which is
     *                null - if the entity is updated,
     *                otherwise  returns the entity  - (e.g id does not exist).
     * @throws IllegalArgumentException
     *             if the given entity is null.
     * @throws ValidationException
     *             if the entity is not valid.
     */
    Optional<E> update(E e);

    /**
     * Return all entities, present in the service, under an Iterable
     * variable (e.g. ArrayList, Map)
     * @return - null if there is not any entity present or the
     *           "list" of entities
     */
    Iterable<E> getAll();

    /**
     * Gets the entity with the given ID
     * @param id - the ID which we are looking for
     * @return - {@code Optional} which is
     *              the enitity if is found, null otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<E> getOne(ID id);

    /**
     * Gets the maximum ID present in the Service
     * @return - maximum ID
     */
    ID getLastID();
}
