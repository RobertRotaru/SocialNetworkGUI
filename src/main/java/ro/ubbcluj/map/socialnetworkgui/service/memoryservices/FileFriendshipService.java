package ro.ubbcluj.map.socialnetworkgui.service.memoryservices;

import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos.PrietenieFileRepository;
import ro.ubbcluj.map.socialnetworkgui.service.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;

public class FileFriendshipService implements Service<Tuple<Long, Long>, Prietenie> {

    private PrietenieFileRepository repo;

    /**
     * Constructor with a given repo
     * @param _repo PrietenieFileRepository
     */
    public FileFriendshipService(PrietenieFileRepository _repo) {
        this.repo = _repo;
    }

    /**
     * Saves the friendship in the repository
     * @param prietenie - Entity saved
     * @return null- if the given entity is saved
     *        otherwise returns the entity (id already exists)
     * @throws IllegalArgumentException if the entity is null
     */
    @Override
    public Optional<Prietenie> add(Prietenie prietenie) {
        prietenie.conv();
        return repo.save(prietenie);
    }

    /**
     * Deletes the entity with the given Tuple of IDs
     * @param longLongTuple - the pair of IDs given of the
     *           friendship to be deleted
     * @return - the deleted friendship or null if there is not one
     *          with the given pair of IDs
     * @throws IllegalArgumentException if longLongTuple is null
     */
    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> longLongTuple) throws FileNotFoundException {
        try{
            var friendship = repo.delete(longLongTuple);
            if(friendship.isEmpty()) {
                return Optional.empty();
            }

            repo.extractEntity(new ArrayList<String>(){{
                add(friendship.get().getId().getLeft().toString());
                add(friendship.get().getId().getRight().toString());
            }});
            return friendship;
        } catch(FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }
    }

    /**
     * Updates the friendship with the pair of IDs of the given entity
     * with this new given friendship
     * @param prietenie - Prietenie
     *           prietenie must not be null
     * @return null - if the friendship is updated,
     *                otherwise  returns the friendship  - (e.g. id does not exist).
     * @throws IllegalArgumentException
     *             if the given friendship is null.
     * @throws ValidationException
     *             if the friendship is not valid.
     */
    @Override
    public Optional<Prietenie> update(Prietenie prietenie) {
        return repo.update(prietenie);
    }

    /**
     * Return all freindships, present in the service, under an Iterable
     * variable (e.g. ArrayList, Map)
     * @return - null if there is not any friendship present or the
     *           "list" of friendships
     */
    @Override
    public Iterable<Prietenie> getAll() {
        return repo.findAll();
    }

    /**
     * Gets the friendship with the given pair of IDs
     * @param longLongTuple - the ID which we are looking for
     * @return - the friendship if is found, null otherwise
     * @throws IllegalArgumentException if tuple is null
     */
    @Override
    public Optional<Prietenie> getOne(Tuple<Long, Long> longLongTuple) {
        return repo.findOne(longLongTuple);
    }

    /**
     * Won't be used!
     * @return - null
     */
    @Override
    public Tuple<Long, Long> getLastID() {
        return null;
    }
}
