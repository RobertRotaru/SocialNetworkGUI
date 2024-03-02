package ro.ubbcluj.map.socialnetworkgui.repository.dbrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.*;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Page;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.PageImplementation;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.UserDBService;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MessageDBRepository extends AbstractDBRepository<Long, Message> {

    /**
     * Constructori
     *
     * @param url        the url of the database
     * @param username   the username to login into the DB
     * @param password   the password to login into the DB
     * @param _validator the validator for the entity
     */
    public MessageDBRepository(String url, String username, String password, Validator<Message> _validator) {
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
    public Optional<Message> findOne(Long aLong) {
        try(var statement = setConnection("select * from messages m where m.id=" + aLong)) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Message message = new Message();
                message.setId(resultSet.getLong("id"));
                message.setText(resultSet.getString("text"));
                message.setDate(resultSet.getObject("date", OffsetDateTime.class));
                var temp1 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_user1"));
                var temp2 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_user2"));

                Utilizator from = temp1.iterator().next();
                Utilizator to = temp2.iterator().next();

                message.setFrom(from);
                message.setTo(to);

                return Optional.of(message);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();
        try(var statement = setConnection("select * from messages")) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Message message = new Message();
                message.setId(resultSet.getLong("id"));
                message.setText(resultSet.getString("text"));
                message.setDate(resultSet.getObject("date", OffsetDateTime.class));
                var temp1 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_user1"));
                var temp2 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_user2"));

                Utilizator from = temp1.iterator().next();
                Utilizator to = temp2.iterator().next();

                message.setFrom(from);
                message.setTo(to);

                messages.add(message);
            }

            return messages;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<GroupMessage> saveGroupMessage(GroupMessage groupMessage,
                                                          String url, String username, String password) {
        String sqlCommand = "insert into messages_groups " +
                "(id_group, id_from, text, date) values (?, ?, ?, ?)";

        try(Connection connection = DriverManager.getConnection(url, username, password);
            var statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, groupMessage.getIdGroup());
            statement.setLong(2, groupMessage.getFrom().getId());
            statement.setString(3, groupMessage.getText());
            statement.setTimestamp(4, Timestamp.valueOf(groupMessage.getDate().toLocalDateTime()));
            statement.executeUpdate();

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> save(Message entity) {
        String sqlCommand = "insert into messages(id_user1, id_user2, text, date) values (?, ?, ?, ?)";

        try(var statement = setConnection(sqlCommand)) {

            statement.setLong(1, entity.getFrom().getId());
            statement.setLong(2, entity.getTo().getId());
            statement.setString(3, entity.getText());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate().toLocalDateTime()));
            statement.executeUpdate();

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long along) throws FileNotFoundException {
        if(along == null) {
            throw new IllegalArgumentException("IDs can't be null!");
        }

        String sqlCommand = "delete from messages where id=?";

        Optional<Message> foundMessage = findOne(along);

        if(foundMessage.isPresent()) {
            try(var statement = setConnection(sqlCommand)) {
                statement.setLong(1, along);
                statement.executeUpdate();

                return foundMessage;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return foundMessage;
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }

    @Override
    public Page<Message> findAll(Pageable pageable) {
        Set<Message> messages = new HashSet<>();
        try(var statement = setConnection("select * from messages limit " +
                pageable.getPageSize() + " offset " +
                pageable.getPageSize() * (pageable.getPageNumber() - 1))) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Message message = new Message();
                message.setId(resultSet.getLong("id"));
                message.setText(resultSet.getString("text"));
                message.setDate(resultSet.getObject("date", OffsetDateTime.class));
                var temp1 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_user1"));
                var temp2 = UserDBService.executeSQLSelect("select * from users u where u.id=" +
                        resultSet.getLong("id_user2"));

                Utilizator from = temp1.iterator().next();
                Utilizator to = temp2.iterator().next();

                message.setFrom(from);
                message.setTo(to);

                messages.add(message);
            }

            return new PageImplementation<>(pageable, messages.stream());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
