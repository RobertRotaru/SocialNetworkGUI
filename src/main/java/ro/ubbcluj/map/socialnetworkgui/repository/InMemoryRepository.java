package ro.ubbcluj.map.socialnetworkgui.repository;

import ro.ubbcluj.map.socialnetworkgui.domain.Entity;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID,E> {
    private Validator<E> validator;
    Map<ID,E> entities;

    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities=new HashMap<ID,E>();
    }

    @Override
    public Optional<E> findOne(ID id){
        if (id==null)
            throw new IllegalArgumentException("ID must be not null");
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        if(entities.values().isEmpty()) {
            return null;
        }
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        validator.validate(entity);
        return Optional.ofNullable(entities.putIfAbsent(entity.getId(), entity));
    }

    /**
     * Removes the entity with the given ID if given,
     * returns null if there is no entity with this ID,
     * or throws exception if ID is null
     * @param id
     *      id must be not null
     * @return the deleted entity or null if there is not any
     *          entity with the given ID
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Optional<E> delete(ID id) throws FileNotFoundException {
        if(id == null) {
            throw new IllegalArgumentException("ID can't be null!");
        }
        if(entities.get(id) == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entities.remove(id));
    }

    @Override
    public Optional<E> update(E entity) {
        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(entity);

        entities.put(entity.getId(),entity);

        if(entities.get(entity.getId()) != null) {
            entities.put(entity.getId(),entity);
            return Optional.empty();
        }
        return Optional.of(entity);
    }
}
