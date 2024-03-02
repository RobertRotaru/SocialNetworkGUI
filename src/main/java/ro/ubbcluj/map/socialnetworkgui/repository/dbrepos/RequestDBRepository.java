package ro.ubbcluj.map.socialnetworkgui.repository.dbrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Request;
import ro.ubbcluj.map.socialnetworkgui.domain.RequestStatus;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Page;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PageImplementation;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.UserDBService;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RequestDBRepository extends AbstractDBRepository<Long, Request> {


    /**
     * Constructori
     *
     * @param url        the url of the database
     * @param username   the username to login into the DB
     * @param password   the password to login into the DB
     * @param _validator the validator for the entity
     */
    public RequestDBRepository(String url, String username, String password, Validator<Request> _validator) {
        super(url, username, password, _validator);
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
    public Optional<Request> findOne(Long aLong) {
        try(var statement = setConnection("select * from requests r where r.id=" + aLong)) {

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

                return Optional.of(request);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Request> findAll() {
        Set<Request> requests = new HashSet<>();
        try(var statement = setConnection("select * from request")) {

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

    @Override
    public Optional<Request> save(Request entity) {
        String sqlCommand = "insert into requests(id_from, id_to, status, date) values (?, ?, ?, ?)";

        try(var statement = setConnection(sqlCommand)) {

            statement.setLong(1, entity.getFrom().getId());
            statement.setLong(2, entity.getTo().getId());
            statement.setString(3, entity.getStatus().toString());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate().toLocalDateTime()));
            statement.executeUpdate();

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Request> delete(Long aLong) throws FileNotFoundException {
        if(aLong == null) {
            throw new IllegalArgumentException("ID can't be null!");
        }

        String sqlCommand = "delete from requests where id=?";

        Optional<Request> foundRequest = findOne(aLong);

        if(foundRequest.isPresent()) {
            try(var statement = setConnection(sqlCommand)) {
                statement.setLong(1, aLong);
                statement.executeUpdate();

                return foundRequest;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return foundRequest;
    }

    @Override
    public Optional<Request> update(Request entity) {
        String sqlCommand = "update requests set id_from=?, id_to=?, date=?, status=? where id=?";

        try(var statement = setConnection(sqlCommand)) {
            statement.setLong(1, entity.getFrom().getId());
            statement.setLong(2, entity.getTo().getId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate().toLocalDateTime()));
            statement.setString(4, entity.getStatus().toString());
            statement.setLong(5, entity.getId());

            statement.executeUpdate();

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Request> findAll(Pageable pageable) {
        Set<Request> requests = new HashSet<>();
        try(var statement = setConnection("select * from requests limit " +
                pageable.getPageSize() + " offset " +
                pageable.getPageSize() * (pageable.getPageNumber() - 1))) {

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

            return new PageImplementation<>(pageable, requests.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
