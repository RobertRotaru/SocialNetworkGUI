package ro.ubbcluj.map.socialnetworkgui.service.dbservices;

import ro.ubbcluj.map.socialnetworkgui.domain.GroupMessage;
import ro.ubbcluj.map.socialnetworkgui.domain.Message;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.MessageValidator;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;
import ro.ubbcluj.map.socialnetworkgui.repository.dbrepos.MessageDBRepository;
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

import static ro.ubbcluj.map.socialnetworkgui.service.dbservices.UserDBService.*;

public class MessageDBService implements Service<Long, Message> {
    private final MessageDBRepository repo;

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    private int size = 0;
    private int page = 1;

    public MessageDBService(String url, String user, String password) {
        this.repo = new MessageDBRepository(
                url, user, password,
                new MessageValidator()
        );
    }

    public static MessageDBService makeMessageDBService(Service<Long, Message> service) {
        return (MessageDBService) service;
    }

    @Override
    public Optional<Message> add(Message message) {
        return repo.save(message);
    }

    @Override
    public Optional<Message> delete(Long longLongTuple) throws FileNotFoundException {
        return repo.delete(longLongTuple);
    }

    @Override
    public Optional<Message> update(Message message) {
        return repo.update(message);
    }

    @Override
    public Iterable<Message> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Message> getOne(Long longLongTuple) {
        return repo.findOne(longLongTuple);
    }

    @Override
    public Long getLastID() {
        return null;
    }
    public PreparedStatement setConnection(String sqlCommand) throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection.prepareStatement(sqlCommand);
    }

    public static Optional<GroupMessage> addGroupMessage(GroupMessage message) {
        return MessageDBRepository.saveGroupMessage(message, url, user, password);
    }

    public Iterable<Message> executeSQLQuery(String sqlCommand) {
        Set<Message> messages = new HashSet<>();
        try(var statement = setConnection(sqlCommand)) {

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

    public void setPageSize(int size) {
        this.size = size;
    }

    public Iterable<Message> getNextMessages() {
        this.page++;
        return getMessageOnPage(this.page);
    }

    public Iterable<Message> getMessageOnPage(int page) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.size);
        return repo.findAll(pageable).getContent().collect(Collectors.toList());
    }
}
