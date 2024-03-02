package ro.ubbcluj.map.socialnetworkgui.service.dbservices;

import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;
import ro.ubbcluj.map.socialnetworkgui.repository.dbrepos.UsersDBRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Page;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PageableImplementation;
import ro.ubbcluj.map.socialnetworkgui.service.Service;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserDBService implements Service<Long, Utilizator>{

    private final UsersDBRepository repo;

    public static String user = "";
    public static String url = "";
    public static String password = "";

    public int getPage() {
        return page;
    }

    private int page = 0;

    public int getSize() {
        return size;
    }

    private int size = 1;

    public UserDBService(String url, String user, String password) {
        this.repo = new UsersDBRepository(
                url, user, password
        );
        UserDBService.url = url;
        UserDBService.user = user;
        UserDBService.password = password;
    }

    /**
     * Makes a UserDBService from Service
     * (upcasting)
     *
     * @param service - the service
     * @return - UserDBService
     */
    public static UserDBService makeUserDBService(Service<Long, Utilizator> service) {
        return (UserDBService) service;
    }

    @Override
    public Optional<Utilizator> add(Utilizator utilizator) {
        return repo.save(utilizator);
    }

    @Override
    public Optional<Utilizator> delete(Long aLong) throws FileNotFoundException {
        return repo.delete(aLong);
    }

    @Override
    public Optional<Utilizator> update(Utilizator utilizator) {
       return repo.update(utilizator);
    }

    @Override
    public Iterable<Utilizator> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Utilizator> getOne(Long aLong) {
        return repo.findOne(aLong);
    }

    @Override
    public Long getLastID() {
        return null;
    }

    /**
     * Sets the connection with the DB, using an SQL command
     * to prepare a statement
     * @param sqlCommand - the SQL command
     * @return a PreparedStatement ready to use
     * @throws SQLException if the connection fails
     */
    public static PreparedStatement setConnection(String sqlCommand) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection.prepareStatement(sqlCommand);
    }

    /**
     * Executes a 'Select' command in SQL in
     * the 'users' table
     * @param sqlCommand the 'Select' command
     * @return list of users, according to the
     *          'Select' command
     */
    public static Iterable<Utilizator> executeSQLSelect(String sqlCommand) {
        List<Utilizator> users = new ArrayList<>();

        try (var statement = setConnection(sqlCommand)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Utilizator user = new Utilizator(
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name")
                );
                user.setId(resultSet.getLong("id"));
                users.add(user);
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getNoPages() {
        String sqlCommand = "select ( count(*) + ? - 1) / ? from users";
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

    public Iterable<Utilizator> getNextUsers() {
        this.page++;
        return getUsersOnPage(this.page);
    }

    public Iterable<Utilizator> getUsersOnPage(int page) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.size);
        return repo.findAll(pageable).getContent().collect(Collectors.toList());
    }
}
