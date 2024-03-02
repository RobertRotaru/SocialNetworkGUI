package ro.ubbcluj.map.socialnetworkgui.service.dbservices;

import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;
import ro.ubbcluj.map.socialnetworkgui.repository.dbrepos.FriendshipDBRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PageableImplementation;
import ro.ubbcluj.map.socialnetworkgui.service.Service;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendshipDBService implements Service<Tuple<Long, Long>, Prietenie> {
    private final FriendshipDBRepository repo;
    private final String url;
    private final String password;
    private final String user;

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    private int size = 0;
    private int page = 1;


    public FriendshipDBService(String url, String user, String password) {
        this.repo = new FriendshipDBRepository(
                url, user, password
        );
        this.url = url;
        this.password = password;
        this.user = user;
    }

    /**
     * Makes from a Service a FriendshipDBService
     * (upcasting)
     * @param service - the service
     * @return a FriendshipDBService
     */
    public static FriendshipDBService makeFriendshipDBService(
            Service<Tuple<Long, Long>, Prietenie> service
    ) {
        return (FriendshipDBService) service;
    }

    @Override
    public Optional<Prietenie> add(Prietenie prietenie) {
        prietenie.conv();
        return repo.save(prietenie);
    }

    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> longLongTuple) throws FileNotFoundException {
        return repo.delete(longLongTuple);
    }

    @Override
    public Optional<Prietenie> update(Prietenie prietenie) {
        return repo.update(prietenie);
    }

    @Override
    public Iterable<Prietenie> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Prietenie> getOne(Tuple<Long, Long> longLongTuple) {
        return repo.findOne(longLongTuple);
    }

    @Override
    public Tuple<Long, Long> getLastID() {
        return null;
    }

    /**
     * Sets the connection with the DB, using an SQL command
     * to prepare a statement
     * @param sqlCommand - the SQL command
     * @return a PreparedStatement ready to use
     * @throws SQLException if the connection fails
     */
    private PreparedStatement setConnection(String sqlCommand) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection.prepareStatement(sqlCommand);
    }

    /**
     * Executes a 'Select' command in SQL in
     * the 'friendships' table
     * @param sqlCommand the 'Select' command
     * @return list of friendships, according to the
     *          'Select' command
     */
    public Iterable<Prietenie> executeSQLSelect(String sqlCommand) {
        Set<Prietenie> friendships = new HashSet<>();

        try(var statement = setConnection(sqlCommand)) {

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

    public Long getNoPages() {
        String sqlCommand = "select ( count(*) + ? - 1) / ? from friendships";
        try(var statement = setConnection(sqlCommand)) {
            statement.setInt(1, size);
            statement.setInt(2, size);

            long sol = 0L;

            var result = statement.executeQuery();
            while(result.next()) {
                sol = result.getLong(1);
            }
            return sol;
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPageSize(int size) {
        this.size = size;
    }

    public Iterable<Prietenie> getNextFriendships() {
        this.page++;
        return getFriendshipsOnPage(this.page);
    }

    public Iterable<Prietenie> getFriendshipsOnPage(int page) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.size);
        return repo.findAll(pageable).getContent().collect(Collectors.toList());
    }
}
