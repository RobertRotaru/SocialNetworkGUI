package ro.ubbcluj.map.socialnetworkgui.repository.dbrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.Entity;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;
import ro.ubbcluj.map.socialnetworkgui.repository.InMemoryRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PagingRepository;

/**
 * Repository for working with DB
 * @param <ID> the type of the IDs
 * @param <E> the type of entities
 */
public abstract class AbstractDBRepository<ID, E extends Entity<ID>> implements PagingRepository<ID, E> {

    protected String url;
    protected String username;
    protected String password;

    protected Validator<E> validator;

    /**
     * Constructori
     * @param url the url of the database
     * @param username the username to login into the DB
     * @param password the password to login into the DB
     * @param _validator the validator for the entity
     */
    public AbstractDBRepository(String url, String username, String password, Validator<E> _validator) {
        validator = _validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

}
