package ro.ubbcluj.map.socialnetworkgui.repository.dbrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.UtilizatorValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.*;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class UsersDBRepository extends AbstractDBRepository<Long, Utilizator> {

    /**
     * Constructor for the Repo
     * @param url the url of the DB
     * @param user the username to login into the DB
     * @param password the password to login into the DB
     */
    public UsersDBRepository(String url, String user, String password) {
        super(url, user, password, new UtilizatorValidator());
    }

    /**
     * Sets the connection with the DB, using an SQL command
     * to prepare a statement
     * @param sqlCommand - the SQL command
     * @return a PreparedStatement ready to use
     * @throws SQLException if the connection fails
     */
    public PreparedStatement setConnection(String sqlCommand) throws SQLException {
        Connection connection = DriverManager.getConnection(super.url, super.username, super.password);
        return connection.prepareStatement(sqlCommand);
    }

    @Override
    public Optional<Utilizator> findOne(Long aLong) {
        try(var statement = setConnection("select * from users u where u.id=" + aLong)) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Utilizator utilizator = new Utilizator();
                utilizator.setId(resultSet.getLong("id"));
                utilizator.setFirstName(resultSet.getString("first_name"));
                utilizator.setLastName(resultSet.getString("last_name"));
                return Optional.of(utilizator);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();
        try(var statement = setConnection("select * from users")) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Utilizator utilizator = new Utilizator();
                utilizator.setId(resultSet.getLong("id"));
                utilizator.setFirstName(resultSet.getString("first_name"));
                utilizator.setLastName(resultSet.getString("last_name"));
                users.add(utilizator);
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves a user into the DB in the
     * 'users' table
     * @param entity - user to be saved
     * entity must be not null
     * @return an Optional<Utilizator> with null
     *          if the operation took place with success,
     *          with its value of the
     *          user otherwise
     */
    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        validator.validate(entity);
        String sqlCommand = "insert into users(first_name, last_name) values (?, ?)";

        try(var statement = setConnection(sqlCommand)) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.executeUpdate();

            return Optional.of(entity);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a user with the given ID from the
     * DB, and from the table 'users'
     * @param aLong - the ID
     * id must be not null
     * @return an Optional<Utilizator> with its value of the saved
     *         user if the operation took place with success,
     *         with null otherwise
     */
    @Override
    public Optional<Utilizator> delete(Long aLong) {
        if(aLong == null) {
            throw new IllegalArgumentException("ID can't be null!");
        }

        String sqlCommand = "delete from users where id=?";

        Optional<Utilizator> foundUser = findOne(aLong);

        if(foundUser.isPresent()) {
            try(var statement = setConnection(sqlCommand)) {
                statement.setLong(1, aLong);
                statement.executeUpdate();

                return foundUser;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return foundUser;
    }

    /**
     * Updates the user with the ID of the user 'entity'
     * with its first_name and last_name
     * @param entity - the 'new' user
     * entity must not be null
     * @return an Optional<Utilizator> with its value of the saved
     *         user if the operation did not take place with success,
     *         with null if it did
     */
    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        validator.validate(entity);
        String sqlCommand = "update users set first_name=?, last_name=? where id=?";

        try(var statement = setConnection(sqlCommand)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());

            statement.executeUpdate();

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Utilizator> findAll(Pageable pageable) {
        Set<Utilizator> users = new HashSet<>();
        try(var statement = setConnection("select * from users limit " +
                pageable.getPageSize() + " offset " +
                pageable.getPageSize() * (pageable.getPageNumber() - 1))) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Utilizator utilizator = new Utilizator();
                utilizator.setId(resultSet.getLong("id"));
                utilizator.setFirstName(resultSet.getString("first_name"));
                utilizator.setLastName(resultSet.getString("last_name"));
                users.add(utilizator);
            }

            return new PageImplementation<>(pageable, users.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
