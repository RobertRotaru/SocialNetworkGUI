package ro.ubbcluj.map.socialnetworkgui.service.dbservices;

import ro.ubbcluj.map.socialnetworkgui.domain.Request;
import ro.ubbcluj.map.socialnetworkgui.domain.RequestStatus;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.RequestValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;
import ro.ubbcluj.map.socialnetworkgui.repository.dbrepos.RequestDBRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PageableImplementation;
import ro.ubbcluj.map.socialnetworkgui.service.Service;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestDBService implements Service<Long, Request> {

    private final String url;
    private final String user;
    private final String password;

    private final RequestDBRepository repo;

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    private int size = 0;
    private int page = 1;

    public RequestDBService(String url, String user, String password) {
        this.repo = new RequestDBRepository(
                url, user, password,
                new RequestValidator()
        );
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Optional<Request> add(Request request) {
        return repo.save(request);
    }

    @Override
    public Optional<Request> delete(Long aLong) throws FileNotFoundException {
        return repo.delete(aLong);
    }

    @Override
    public Optional<Request> update(Request request) {
        return repo.update(request);
    }

    @Override
    public Iterable<Request> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Request> getOne(Long aLong) {
        return repo.findOne(aLong);
    }

    public Long getID(Long idUser1, Long idUser2) {
        var result = executeSQLSelect("select * from requests r where r.id_from=" + idUser1 +
                " and r.id_to=" + idUser2);

        if(result.iterator().hasNext()) {
            return result.iterator().next().getId();
        }
        return -1L;
    }

    @Override
    public Long getLastID() {
        return null;
    }

    public static RequestDBService makeDBService(Service<Long, Request> service) {
        return (RequestDBService) service;
    }

    /**
     * Sets the connection with the DB, using an SQL command
     * to prepare a statement
     * @param sqlCommand - the SQL command
     * @return a PreparedStatement ready to use
     * @throws SQLException if the connection fails
     */
    public PreparedStatement setConnection(String sqlCommand) throws SQLException {
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
    public Iterable<Request> executeSQLSelect(String sqlCommand) {
        Set<Request> requests = new HashSet<>();
        try(var statement = setConnection(sqlCommand)) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                var temp1 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_from"));
                var temp2 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_to"));

                Utilizator from = temp1.iterator().next();
                Utilizator to = temp2.iterator().next();

                Request request = new Request(from, to);
                request.setStatus(RequestStatus.fromString(resultSet.getString("status")));
                request.setDate(resultSet.getObject("date", OffsetDateTime.class));
                request.setId(resultSet.getLong("id"));

                requests.add(request);
            }

            return requests;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPageSize(int size) {
        this.size = size;
    }

    public Iterable<Request> getNextRequests() {
        this.page++;
        return getRequestsOnPage(this.page);
    }

    public Iterable<Request> getRequestsOnPage(int page) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.size);
        return repo.findAll(pageable).getContent().collect(Collectors.toList());
    }
}
