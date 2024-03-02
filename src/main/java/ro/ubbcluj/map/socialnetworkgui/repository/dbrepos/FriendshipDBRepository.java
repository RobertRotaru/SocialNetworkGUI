package ro.ubbcluj.map.socialnetworkgui.repository.dbrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.PrietenieValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Page;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PageImplementation;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.sql.*;

/**
 * Repository for friendships in DB
 */
public class FriendshipDBRepository extends AbstractDBRepository<Tuple<Long, Long>, Prietenie> {

    /**
     * Constructor for the Repo
     * @param url the url of the DB
     * @param username the username to login into the DB
     * @param password the password to login into the DB
     */
    public FriendshipDBRepository(String url, String username, String password) {
        super(url, username, password, new PrietenieValidator());
    }

    public PreparedStatement setConnection(String sqlCommand) throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        return connection.prepareStatement(sqlCommand);
    }

    /**
     * Saves a friendship into the DB in the
     * 'friendships' table
     * @param entity - friendships to be saved
     * entity must be not null
     * @return an Optional<Prietenie> with its value of the saved
     *          friendship if the operation took place with success,
     *          with null otherwise
     */
    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        String sqlCommand = "insert into friendships(id1, id2) values (?, ?)";

        try(var statement = setConnection(sqlCommand)) {
            statement.setLong(1, entity.getId().getLeft());
            statement.setLong(2, entity.getId().getRight());
            statement.executeUpdate();

            return Optional.of(entity);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a friendship with the given IDs from the
     * DB, and from the table 'friendships'
     * @param longLongTuple - the IDs
     * id must be not null
     * @return an Optional<Prietenie> with its value of the saved
     *         friendship if the operation took place with success,
     *         with null otherwise
     */
    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> longLongTuple){
        if(longLongTuple == null) {
            throw new IllegalArgumentException("IDs can't be null!");
        }

        String sqlCommand = "delete from friendships where id1=? and id2=?";

        Optional<Prietenie> foundFriendship = findOne(longLongTuple);

        if(foundFriendship.isPresent()) {
            try(var statement = setConnection(sqlCommand)) {
                statement.setLong(1, longLongTuple.getLeft());
                statement.setLong(2, longLongTuple.getRight());
                statement.executeUpdate();

                return foundFriendship;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return foundFriendship;
    }



    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        return Optional.empty();
    }

    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> longLongTuple) {
        try(var statement = setConnection("select * from friendships fr where (fr.id1=" + longLongTuple.getLeft() +
                " and fr.id2=" + longLongTuple.getRight() + ") or " +
                "(fr.id1=" + longLongTuple.getRight() + " and fr.id2=" + longLongTuple.getLeft() + ")")) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Prietenie friendship = new Prietenie();
                friendship.setId(new Tuple<>(
                        resultSet.getLong("id1"),
                        resultSet.getLong("id2")
                ));
                friendship.setDate(
                        LocalDate.parse(resultSet.getString("friends_from"))
                );
                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Prietenie> findAll() {
        Set<Prietenie> friendships = new HashSet<>();
        try(var statement = setConnection("select * from friendships")) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Prietenie friendship = new Prietenie();
                friendship.setId(new Tuple<>(
                        resultSet.getLong("id1"),
                        resultSet.getLong("id2")
                ));
                friendship.setDate(
                        LocalDate.parse(resultSet.getString("friends_from"))
                );
                friendships.add(friendship);
            }

            return friendships;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Prietenie> findAll(Pageable pageable) {
        Set<Prietenie> friendships = new HashSet<>();
        try(var statement = setConnection("select * from friendships limit " +
                pageable.getPageSize() + " offset " +
                pageable.getPageSize() * (pageable.getPageNumber() - 1))) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Prietenie friendship = new Prietenie();
                friendship.setId(new Tuple<>(
                        resultSet.getLong("id1"),
                        resultSet.getLong("id2")
                ));
                friendship.setDate(
                        LocalDate.parse(resultSet.getString("friends_from"))
                );
                friendships.add(friendship);
            }

            return new PageImplementation<>(pageable, friendships.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
