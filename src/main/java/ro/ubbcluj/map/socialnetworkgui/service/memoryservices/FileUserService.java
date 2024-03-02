package ro.ubbcluj.map.socialnetworkgui.service.memoryservices;

import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos.UtilizatorFileRepository;
import ro.ubbcluj.map.socialnetworkgui.service.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

/**
 * Service for in-memory type of operations for users
 */
public class FileUserService implements Service<Long, Utilizator> {

    //Private repo for saving in memory
    private UtilizatorFileRepository repo;

    /**
     * Constructor for a given repo
     * @param _repo InMemoryRepository
     */
    public FileUserService(UtilizatorFileRepository _repo) {
        this.repo = _repo;
    }


    /**
     * Adds the entity in memory
     * @param e - Entity saved
     * @return null- if the given entity is saved
     *          otherwise returns the entity (id already exists)
     * @throws ValidationException
     *            if the entity is not valid
     * @throws IllegalArgumentException
     *             if the given entity is null.
     */
    @Override
    public Optional<Utilizator> add(Utilizator e) {
        return repo.save(e);
    }

    /**
     * Deletes the entity with the given id
     * @param id - the id of the requested
     *           entity to be deleted
     * @return the removed entity or null if there is no entity with the given id
     *      * @throws IllegalArgumentException
     *      *                   if the given id is null.
     */
    @Override
    public Optional<Utilizator> delete(Long id) throws FileNotFoundException {
        try{
            var user = repo.delete(id);
            if(user.isEmpty()) {
                return Optional.empty();
            }

            repo.extractEntity(new ArrayList<String>(){{
                add(user.get().getId().toString());
                add(user.get().getFirstName());
                add(user.get().getLastName());
            }});

            return user;
        } catch(FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }
    }

    /**
     * Updates the entity with the id of the given entity
     * @param e - entity must not be null
     * @return null - if the entity is updated,
     *      *                otherwise  returns the entity  - (e.g id does not exist).
     *      * @throws IllegalArgumentException
     *      *             if the given entity is null.
     *      * @throws ValidationException
     *      *             if the entity is not valid.
     */
    @Override
    public Optional<Utilizator> update(Utilizator e) {
        return repo.update(e);
    }

    /**
     * Returns the list of all Users in the service, or null
     * @return - null if there is not any User in the service,
     *           the list with all Users otherwise
     */
    @Override
    public Iterable<Utilizator> getAll() {
        return repo.findAll();
    }

    /**
     * Gets the user with the given ID
     * @param aLong - the ID which we are looking for
     * @return - the user if is found, null otherwise
     * @throws IllegalArgumentException if aLong is null
     */
    @Override
    public Optional<Utilizator> getOne(Long aLong) {
        return repo.findOne(aLong);
    }

    /**
     * Gets the maximum ID present in the User Service
     * @return - maximum ID
     */
    @Override
    public Long getLastID() {
//        Long sol = 0l;
//        if(repo.findAll() == null) {
//            return sol;
//        }
//        for (var x : repo.findAll()) {
//            sol = Math.max(sol, x.getId());
//        }
        //Equivalent
        AtomicLong sol = new AtomicLong(0);
        if(repo.findAll() == null) {
            return sol.get();
        }
        StreamSupport.stream(repo.findAll().spliterator(), false)
                .forEach(x -> {
                    sol.accumulateAndGet(x.getId(), Math::max);
                });
        return sol.get();
    }
}
