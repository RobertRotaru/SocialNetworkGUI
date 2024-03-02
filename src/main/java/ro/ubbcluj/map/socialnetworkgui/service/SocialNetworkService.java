package ro.ubbcluj.map.socialnetworkgui.service;

import ro.ubbcluj.map.socialnetworkgui.domain.*;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.FriendshipAlreadyExistsException;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.repository.paging.Pageable;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.FriendshipDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.MessageDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.RequestDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.UserDBService;
import ro.ubbcluj.map.socialnetworkgui.utils.events.UsersChangedEvent;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observable;
import ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer;

import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class SocialNetworkService implements Observable<UsersChangedEvent> {

    private final Service<Long, Utilizator> userService;
    private final Service<Tuple<Long, Long>, Prietenie> friendshipService;
    private final Service<Long, Message> messageService;
    private final Service<Long, Request> requestService;

    private String url;
    private String user;
    private String password;

    private final List<Observer<UsersChangedEvent>> observers = new ArrayList<>();


    public SocialNetworkService(Service<Long, Utilizator> userService,
                                Service<Tuple<Long, Long>, Prietenie> friendshipService,
                                Service<Long, Message> messageService,
                                Service<Long, Request> requestService,
                                String url, String user, String password) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.requestService = requestService;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Calculates the number of users in the social network
     * @return - number of the users
     */
    public String getNoPagesUsers() {
        AtomicInteger nr = new AtomicInteger(0);
//        for(var x : userService.getAll()) {
//            nr++;
//        }
        //Equivalent
        var userdb = UserDBService.makeUserDBService(userService);
        return userdb.getNoPages().toString();
    }

    public int getUsersPage() {
        return UserDBService.makeUserDBService(userService).getPage();
    }

    public int getFriendshipsPage() {
        return FriendshipDBService.makeFriendshipDBService(friendshipService).getPage();
    }

    public int getNoFriendships() {
        AtomicInteger nr = new AtomicInteger(0);
        friendshipService.getAll().forEach(user -> nr.addAndGet(1));
        return nr.get();
    }

    public int getNoUsers() {
        AtomicInteger nr = new AtomicInteger(0);
        userService.getAll().forEach(user -> nr.addAndGet(1));
        return nr.get();
    }

    /**
     * Sets the page size
     * @param size - size of one page
     */
    public void setPageSizeUsers(int size) {
        var usersDB = UserDBService.makeUserDBService(userService);
        usersDB.setPageSize(size);
    }

    /**
     * Gets the users on a specified page
     * @param page - specified page
     * @return - list of users
     */
    public Iterable<Utilizator> getUsersOnPage(int page) {
        var usersDB = UserDBService.makeUserDBService(userService);
        return usersDB.getUsersOnPage(page);
    }

    /**
     * Calculates the number of users in the social network
     * @return - number of the users
     */
    public String getNoPagesFriendships() {
        AtomicInteger nr = new AtomicInteger(0);
//        for(var x : userService.getAll()) {
//            nr++;
//        }
        //Equivalent
        var friendshipsDB = FriendshipDBService.makeFriendshipDBService(friendshipService);
        return friendshipsDB.getNoPages().toString();
    }

    /**
     * Sets the page size
     * @param size - size of one page
     */
    public void setPageSizeFriendships(int size) {
        var friendshipsDB = FriendshipDBService.makeFriendshipDBService(friendshipService);
        friendshipsDB.setPageSize(size);
    }

    /**
     * Gets the users on a specified page
     * @param page - specified page
     * @return - list of users
     */
    public Iterable<Prietenie> getFriendshipsOnPage(int page) {
        var friendshipsDB = FriendshipDBService.makeFriendshipDBService(friendshipService);
        return friendshipsDB.getFriendshipsOnPage(page);
    }

    /**
     * Adds a message to the DB
     * @param message - Message to be added
     * @return Optional which is null if the given entity
     *          was saved, with the value of the entity otherwise
     */
    public Optional<Message> addMessage(Message message) {
        var taskResult = messageService.add(message);
        if(taskResult.isEmpty()) {
            notifyObservers(new UsersChangedEvent());
        }
        return taskResult;
    }

    public Optional<GroupMessage> addGroupMessage(GroupMessage message) {
        var taskResult = MessageDBService.addGroupMessage(message);
        if(taskResult.isEmpty()) {
            notifyObservers(new UsersChangedEvent());
        }
        return taskResult;
    }

    public Optional<Prietenie> getFriendship(Long id1, Long id2) {
        return friendshipService.getOne(new Tuple<>(id1, id2));
    }

    public Optional<Group> makeGroup(Object[] users) {
        String sqlCommand = "insert into groups_users(members) values (?)";

        try(Connection connection = DriverManager.getConnection(url, user, password);
            var statement = connection.prepareStatement(sqlCommand);) {

            Array array = connection.createArrayOf("long", users);

            statement.setArray(1, array);
            statement.executeUpdate();

            var newStatement = connection.prepareStatement("select * from groups_users where members=(?)");
            newStatement.setArray(1, array);
            var resultset = newStatement.executeQuery();
            if(resultset.next()) {
                var obj = resultset.getArray("members").getArray();
                var ids = Arrays.asList((Object[])obj);
                ArrayList<Utilizator> usersList = new ArrayList<>();
                ids.forEach(user -> {
                    var foundUser = UserDBService.executeSQLSelect("select * from users u " +
                            "where u.id=" + user);
                    usersList.add(foundUser.iterator().next());
                });
                Group group = new Group(usersList);
                group.setId(resultset.getLong("id"));

                return Optional.of(group);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Group> getGroupsOf(Utilizator utilizator) {
        Set<Group> groups = new HashSet<>();
        String sqlCommand = "select * from groups_users where " + utilizator.getId() +
                " = any(members)";

        try(Connection connection = DriverManager.getConnection(url, user, password);
            var statement = connection.prepareStatement(sqlCommand);) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                var obj = resultSet.getArray("members").getArray();
                var ids = Arrays.asList((Object[])obj);
                ArrayList<Utilizator> users = new ArrayList<>();
                ids.forEach(user -> {
                        var foundUser = UserDBService.executeSQLSelect("select * from users u " +
                                "where u.id=" + user);
                        users.add(foundUser.iterator().next());
                });
                Group group = new Group(users);
                group.setId(resultSet.getLong("id"));
                groups.add(group);
            }

            return groups;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<GroupMessage> getMessagesGroup(Group group) {
        String sqlCommand = "select * from messages_groups mg where mg.id_group=" + group.getId();

        Set<GroupMessage> messages = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, user, password);
            var statement = connection.prepareStatement(sqlCommand);) {

            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                GroupMessage groupMessage = new GroupMessage();
                groupMessage.setId(resultSet.getLong("id"));
                groupMessage.setIdGroup(resultSet.getLong("id_group"));
                groupMessage.setDate(resultSet.getObject("date", OffsetDateTime.class));
                groupMessage.setText(resultSet.getString("text"));

                var temp = UserDBService.executeSQLSelect("select * from users u where " +
                        "u.id=" + resultSet.getLong("id_from"));
                groupMessage.setFrom(temp.iterator().next());

                messages.add(groupMessage);
            }

            return messages;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the messages between user1 and user2, chronologically ordered
     * @param user1 - first user
     * @param user2 - second user
     * @return - map with texts and dates of the messages
     */
    public Iterable<Message> getMessages(Utilizator user1, Utilizator user2) {
        var messagesDB = MessageDBService.makeMessageDBService(messageService);

//        List<Message> finalList = new ArrayList<>();

        return messagesDB.executeSQLQuery("select * from messages m" +
                " where (m.id_user1=" + user1.getId() + " and m.id_user2=" + user2.getId() +  " ) or" +
                " (m.id_user1=" + user2.getId() + " and m.id_user2=" + user1.getId() + ")" +
                " order by m.date");
//        Iterable<Message> list2 = messagesDB.executeSQLQuery("select * from messages m" +
//                " where m.id_user1=" + user2.getId() + " and m.id_user2=" + user1.getId());
//
//        list1.forEach(finalList::add);
//        list2.forEach(finalList::add);
//
//        return finalList.stream().sorted(
//                new Comparator<Message>() {
//                    @Override
//                    public int compare(Message o1, Message o2) {
//                        if(o1.getDate().isBefore(o2.getDate())) {
//                            return -1;
//                        }
//                        else if(o1.getDate().isEqual(o2.getDate())) {
//                            return 0;
//                        }
//                        else {
//                            return 1;
//                        }
//                    }
//                }
//        ).collect(Collectors.toList());

    }

    /**
     * Adds request from first user to second user
     * @param from - first user
     * @param to - second user
     */
    public void addRequest(Utilizator from, Utilizator to) {
        if(friendshipService.getOne(new Tuple<>(from.getId(), to.getId())).isPresent()) {
            throw new FriendshipAlreadyExistsException();
        }

        var requestDB = RequestDBService.makeDBService(requestService);
        Long id1 = requestDB.getID(from.getId(), to.getId());
        Long id2 = requestDB.getID(to.getId(), from.getId());
        if(id1 != -1 || id2 != -1) {
            throw new ValidationException("Request is already pending!");
        }

        if(Objects.equals(from.getId(), to.getId())) {
            throw new ValidationException("Can't make request between same users!");
        }
        Request request = new Request(from, to);
        requestService.add(request);
    }

    /**
     * Accept request from first user to second user
     * @param from - first user
     * @param to - second user
     */
    public void acceptRequest(Utilizator from, Utilizator to) {
        var requestDB = RequestDBService.makeDBService(requestService);
        Optional<Request> foundRequest = requestDB.getOne(
                requestDB.getID(from.getId(), to.getId())
        );

        if(foundRequest.isPresent()) {
            Request request = new Request(from, to);
            request.setStatus(RequestStatus.ACCEPTED);
            request.setId(foundRequest.get().getId());
            requestDB.update(request);

            Prietenie newFriendwship = new Prietenie();
            newFriendwship.setId(new Tuple<>(
                    from.getId(), to.getId()
            ));
            addFriendship(newFriendwship);

            notifyObservers(new UsersChangedEvent());

            return;
        }

        throw new ValidationException("Can't find the request!");
    }

    public void rejectRequest(Utilizator from, Utilizator to) throws FileNotFoundException {
        var requestDB = RequestDBService.makeDBService(requestService);
        Optional<Request> foundRequest = requestDB.getOne(
                requestDB.getID(from.getId(), to.getId())
        );

        if(foundRequest.isPresent()) {
            Request request = new Request(from, to);
            request.setStatus(RequestStatus.REJECTED);
            request.setId(foundRequest.get().getId());
            requestDB.delete(request.getId());
            notifyObservers(new UsersChangedEvent());

            return;
        }

        throw new ValidationException("Can't find the request!");
    }

    /**
     * Get pending requests of a user
     * @param to - the user
     * @return - List<Request> sorted by date
     */
    public List<Request> getRequests(Utilizator to) {
        var requestsDB = RequestDBService.makeDBService(requestService);

        Iterable<Request> requests = requestsDB.executeSQLSelect(
                "select * from requests r where r.id_to=" + to.getId() +
                        " and r.status like '%pending%'"
        );

        List<Request> requestsList = new ArrayList<>();
        requests.forEach(requestsList::add);

        requestsList.sort(new Comparator<Request>() {
            @Override
            public int compare(Request o1, Request o2) {
                if(o1.getDate().isBefore(o2.getDate())) {
                    return -1;
                }
                else if(o1.getDate().isEqual(o2.getDate())) {
                    return 0;
                }
                else {
                    return 1;
                }
            }
        });

        return requestsList;
    }

    /**
     * Gets the users with whon user 'user' is not friend with
     * @param utilizator - user
     * @return - list of users
     */
    public List<Utilizator> getPotentialFriends(Utilizator utilizator) {
        var allUsers = userService.getAll();

        List<Utilizator> potentialFriends = new ArrayList<>();
        for(Utilizator potFr : allUsers) {
            if(!Objects.equals(potFr, utilizator) &&
                    friendshipService.getOne(new Tuple<>(potFr.getId(), utilizator.getId())).isEmpty() &&
                    friendshipService.getOne(new Tuple<>(utilizator.getId(), potFr.getId())).isEmpty()) {
                potentialFriends.add(potFr);
            }
        }

        return potentialFriends;
    }

    /**
     * Gets all friends of a user
     * @param user - the user
     * @return - List of users
     */
    public List<Utilizator> getFriends(Utilizator user) {
        return (List<Utilizator>) UserDBService.executeSQLSelect(
                "select distinct u.id, u.first_name, u.last_name from users u, friendships fr where " +
                        "(fr.id1=" + user.getId() + " and fr.id2=u.id) or " +
                        "(fr.id2=" + user.getId() + " and fr.id1=u.id)"
        );
    }

    /**
     * Gets friends with which the user has a chat
     * @param user - the user
     * @return - List of users
     */
    public List<Utilizator> getFriendsWithChat(Utilizator user) {
        return  (List<Utilizator>) UserDBService.executeSQLSelect(
                "select distinct u.id, u.first_name, u.last_name from users u, messages m where " +
                        "(m.id_user1=" + user.getId() + " and m.id_user2=u.id) or " +
                        "(m.id_user2=" + user.getId() + " and m.id_user1=u.id)"
        );
    }

    /**
     * Adds the user in file
     * @param e - User saved
     * @return null- if the given user is saved
     *          otherwise returns the user (id already exists)
     * @throws ValidationException
     *            if the user is not valid
     * @throws IllegalArgumentException
     *             if the given user is null.
     */

    public Optional<Utilizator> addUser(Utilizator e) {
        var task =  userService.add(e);
        if(!task.isEmpty()) {
            notifyObservers(new UsersChangedEvent());
        }
        return task;
    }

    /**
     * Deletes the user with the given id
     * @param id - the id of the requested
     *           user to be deleted
     * @return the removed user or null if there is no user with the given id
     *      * @throws IllegalArgumentException
     *      *                   if the given id is null.
     */
    public Optional<Utilizator> deleteUser(Long id) throws FileNotFoundException{
        var task =  userService.delete(id);
        if(task.isPresent()) {
            notifyObservers(new UsersChangedEvent());
        }
        return task;
    }

    /**
     * Updates the user with the id of the given user
     * @param user - user must not be null
     * @return null - if the user is updated,
     *      *                otherwise returns the user  - (e.g id does not exist).
     *      * @throws IllegalArgumentException
     *      *             if the given user is null.
     *      * @throws ValidationException
     *      *             if the user is not valid.
     */
    public Optional<Utilizator> updateUser(Utilizator user) {
        var task = userService.update(user);
        if(task.isEmpty()) {
            notifyObservers(new UsersChangedEvent());
        }
        return task;
    }

    /**
     * Returns the list of all Users in the service, or null
     * @return - null if there is not any User in the service,
     *           the list with all Users otherwise
     */
    public Iterable<Utilizator> getAllUsers() {
        return userService.getAll();
    }

    /**
     * Gets the user with the given ID
     * @param id - the ID which we are looking for
     * @return - the user if is found, null otherwise
     * @throws IllegalArgumentException if aLong is null
     */
    public Optional<Utilizator> getUser(Long id) {
        return userService.getOne(id);
    }

    /**
     * Gets the maximum ID present in the User Service
     * @return - maximum ID
     */
    public Long getLastIDUsers() {
        return userService.getLastID();
    }

    /**
     * Saves the friendship in the repository
     *
     * @param prietenie - Entity saved
     * @throws IllegalArgumentException if the entity is null
     * @throws ValidationException      if the IDs can't be found or if the friendship
     *                                  already exists
     */
    public Optional<Prietenie> addFriendship(Prietenie prietenie) {
        if(friendshipService.getOne(prietenie.getId()).isPresent()){
            throw new FriendshipAlreadyExistsException("Friendship already exists!");
        }
        if(userService.getOne(prietenie.getId().getLeft()).isPresent() &&
                userService.getOne(prietenie.getId().getRight()).isPresent()) {

            var user1 = userService.getOne(prietenie.getId().getLeft()).get();
            var user2 = userService.getOne(prietenie.getId().getRight()).get();
            user1.addFriend(user2);
            user2.addFriend(user1);

            return friendshipService.add(prietenie);
        }
        throw new ValidationException("The IDs given are not valid!");
    }

    /**
     * Deletes the friendship with the given Tuple of IDs
     * @param longLongTuple - the pair of IDs given of the
     *           friendship to be deleted
     * @return - the deleted friendship or null if there is not one
     *          with the given pair of IDs
     * @throws IllegalArgumentException if longLongTuple is null
     */
    public Optional<Prietenie> deleteFriendship(Tuple<Long, Long> longLongTuple) throws FileNotFoundException {
        return friendshipService.delete(longLongTuple);
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
    public Optional<Prietenie> updateFriendship(Prietenie prietenie) {
        return friendshipService.update(prietenie);
    }

    /**
     * Return all freindships, present in the service, under an Iterable
     * variable (e.g. ArrayList, Map)
     * @return - null if there is not any friendship present or the
     *           "list" of friendships
     */
    public Iterable<Prietenie> getAllFreindships() {
        return friendshipService.getAll();
    }

    /**
     * Gets the friendship with the given pair of IDs
     * @param longLongTuple - the ID which we are looking for
     * @return - the friendship if is found, null otherwise
     * @throws IllegalArgumentException if tuple is null
     */
    public Optional<Prietenie> getOne(Tuple<Long, Long> longLongTuple) {
        return friendshipService.getOne(longLongTuple);
    }

    /**
     * Gets the maximum ID present in the Friendship Service
     * @return - maximum ID
     */
    public Tuple<Long, Long> getLastIDFriendship() {
        return friendshipService.getLastID();
    }

    /**
     * Utility method for DFS from a starting user to all the users
     * which are in the same community with our starting user
     * @param startUser - the starting user
     * @param viz - list with all the already "marked" users
     * @param community - the number of the community
     */
    private void DFS(Utilizator startUser, List<Utilizator> viz, int community) {
        viz.add(startUser);
        startUser.setCommunity(community);
        if (startUser.getFriends() != null) {
//            for (var friend : startUser.getFriends()) {
//                if (!viz.contains(friend)) {
//                    DFS(friend, viz, community);
//                }
//            }
            //Equivalent
            startUser.getFriends().stream()
                    .filter(friend -> !viz.contains(friend))
                    .forEach(friend -> DFS(friend, viz, community));
        }
    }

    /**
     * Utility method which sets the communities for each user present
     * in the social network
     */
    private void setCommunities() {
        //Resets the lists of friends
        StreamSupport.stream(getAllUsers().spliterator(), false)
                .forEach(Utilizator::deleteFriends);

//        Establish the lists of friends
//        for(var friendship : getAllFriendships()) {
//            var user1 = userService.getOne(friendship.getId().getLeft()).get();
//            var user2 = userService.getOne(friendship.getId().getRight()).get();
//
//            if(!user1.getFriends().contains(user2)) {
//                user1.addFriend(user2);
//            }
//            if(!user2.getFriends().contains(user1)) {
//                user2.addFriend(user1);
//            }
//        }
        //Equivalent
        StreamSupport.stream(getAllFreindships().spliterator(), false)
                .map(friendship -> {
                    if (userService.getOne(friendship.getId().getLeft()).isPresent() &&
                            userService.getOne(friendship.getId().getRight()).isPresent()) {
                        var user1 = userService.getOne(friendship.getId().getLeft()).get();
                        var user2 = userService.getOne(friendship.getId().getRight()).get();

                        AbstractMap.SimpleEntry<Utilizator, Utilizator> pair =
                                new AbstractMap.SimpleEntry<>(user1, user2);

                        return pair;
                    }
                    return null;
                }).filter(Objects::nonNull)
                .forEach(pair -> {
                    var user1 = pair.getKey();
                    var user2 = pair.getValue();
                    if(!user1.getFriends().contains(user2)) {
                        user1.addFriend(user2);
                    }
                    if(!user2.getFriends().contains(user1)) {
                        user2.addFriend(user1);
                    }
                });

        AtomicInteger communities = new AtomicInteger(1);
        List<Utilizator> viz = new ArrayList<>();

        //If the user is not marked, we go into its community
//        for(var user : getAllUsers()) {
//            if(!viz.contains(user)) {
//                DFS(user, viz, communities);
//                communities++;
//            }
//        }
        //Equivalent
        StreamSupport.stream(getAllUsers().spliterator(), false)
                .filter(user -> !viz.contains(user))
                .forEach(user -> {
                    DFS(user, viz, communities.get());
                    communities.getAndIncrement();
                });
    }

    /**
     * Returns the number of communities in the social network
     * @return the number of the communities, including the ones
     *         with one member
     */
    public int getNoCommunities() {
        AtomicInteger noCommunities = new AtomicInteger(0);
//        for(var user : getAllUsers()) { //get the maximum community number, because
//                                        //we know that those numbers are consecutive
//            noCommunities = Math.max(noCommunities, user.getCommunity());
//        }
        //Equivalent
        StreamSupport.stream(getAllUsers().spliterator(), false)
                .forEach(user->
                        noCommunities.getAndAccumulate(user.getCommunity(), Math::max)
                );
        return noCommunities.get();
    }

    /**
     * Returns the number of communities with more than one member
     * @return - number of communities with more than one member
     */
    public int getCommunities() {
        //Frequencies of the communities
        int noCommunities = getNoCommunities();
        int[] ap = new int[noCommunities+1];

//        for(var user : getAllUsers()) {
//            ap[user.getCommunity()]++;
//        }
        //Equivalent
        StreamSupport.stream(getAllUsers().spliterator(), false)
                .forEach(user -> ap[user.getCommunity()]++);

//        int communities = 0;
//        for(int i = 1; i <= noCommunities; i++) {
//            if(ap[i] > 1) {
//                communities++;
//            }
//        }
        //Equivalent
        AtomicInteger communities = new AtomicInteger(0);
        IntStream.rangeClosed(1, noCommunities)
                .filter(i -> ap[i] > 1)
                .forEach(i -> communities.getAndIncrement());

        return communities.get();
    }

    /**
     * Returns the most sociable community (the one with the most members)
     * or null if there is not such a community
     * @return list of users in the largest community, or null if there is not one
     */
    public List<Utilizator> getLargestCommunity() {
        List<Utilizator> largestCommunity = new ArrayList<>();

        //Frequencies of the communities
        int noCommunities = getNoCommunities();
        int[] ap = new int[noCommunities+1];

//        for(var user : getAllUsers()) {
//            ap[user.getCommunity()]++;
//        }
        //Equivalent
        StreamSupport.stream(getAllUsers().spliterator(), false)
                .forEach(user -> ap[user.getCommunity()]++);


        //Gets the community with most members
        final int[] maxCommunity = {0};
        final int[] maxAp = {0};
//        for(int i = 1; i <= noCommunities; ++i) {
//            if(ap[i] > maxAp) {
//                maxAp = ap[i];
//                maxCommunity = i;
//            }
//        }
        //Equivalent
        IntStream.rangeClosed(1, noCommunities)
                .forEach(i -> {
                    if(ap[i] > maxAp[0]) {
                        maxAp[0] = ap[i];
                        maxCommunity[0] = i;
                    }
                });

        if(maxAp[0] == 1) { //we don't have friendships
            return null;
        }

        //Compute the largestCommunity list
//        for(var user : getAllUsers()) {
//            if(user.getCommunity() == maxCommunity[0]) {
//                largestCommunity.add(user);
//            }
//        }
        StreamSupport.stream(getAllUsers().spliterator(), false)
                .filter(user -> user.getCommunity() == maxCommunity[0])
                .forEach(largestCommunity::add);
        return largestCommunity;
    }

    /**
     * Gets the users with minimum of n firendships
     * @param n - the number of friendships
     * @return - list of users
     */
    public List<Utilizator> getUsersWithMinimumNFriends(int n) {
        return StreamSupport.stream(userService.getAll().spliterator(), false)
                .filter(user -> user.getFriends().size() >= n)
                .collect(Collectors.toList());
        //.toList();
    }

    /**
     * Gets the friends from a certian month of a certain user
     * @param u - the user
     * @param month - the month
     * @return Map<Utilizator, LocalDate> whose keys represent
     *          the friends found, and the values are the dates
     *          from which user and friend are friends from
     */
    public Map<Utilizator, LocalDate> getFriendsFromMonth(Utilizator u, int month) {
        var users = getUsers(u, month);

        var friendshipDB = FriendshipDBService.makeFriendshipDBService(friendshipService);

        Map<Utilizator, LocalDate> friends = new HashMap<>();

        users.forEach((user) -> {
            String sqlFrCommand = "select * from " +
                    "friendships fr where " +
                    "(fr.id1=" + u.getId().toString() + " and " +
                    "fr.id2=" + user.getId() + ") or " +
                    "(fr.id2=" + u.getId().toString() + " and " +
                    "fr.id1=" + user.getId() + ")";
            var friendships = friendshipDB.executeSQLSelect(sqlFrCommand);

            friendships.forEach((friendship) -> friends.put(user,friendship.getDate()));
        });

        return friends;
    }

    /**
     * Gets the friends of a certain user from a certain month
     * @param u - the user
     * @param month - the month
     * @return list of users according to the 'Select' query
     */
    private Iterable<Utilizator> getUsers(Utilizator u, int month) {
        var usersDB = UserDBService.makeUserDBService(userService);

        //SQL Version
        String sqlCommand = "select u.id, u.first_name, u.last_name from " +
                "users u, friendships fr where " +
                "EXTRACT(MONTH from fr.friends_from)=" + month + " and " +
                "(fr.id1=" + u.getId().toString() + " and " +
                "fr.id2=u.id) or " +
                "(fr.id2=" + u.getId().toString() + " and " +
                "fr.id1=u.id)";
        return UserDBService.executeSQLSelect(sqlCommand);
    }

    /**
     * Adds an observer to observers' list
     * @param obs - Observer
     */
    @Override
    public void addObserver(ro.ubbcluj.map.socialnetworkgui.utils.observer.Observer<UsersChangedEvent> obs) {
        observers.add(obs);
    }

    /**
     * Deletes an observer from observers' list
     * @param obs - Observer
     */
    @Override
    public void removeObserver(Observer<UsersChangedEvent> obs) {
        observers.add(obs);
    }

    /**
     * Notify all observers if there is an event
     * @param usersChangedEvent - event
     */
    @Override
    public void notifyObservers(UsersChangedEvent usersChangedEvent) {
        observers.forEach((obs) -> obs.update(usersChangedEvent));
    }

    private String encryptPassword(String pass) {
        String encryptedpassword = null;
        try
        {
            /* MessageDigest instance for MD5. */
            MessageDigest m = MessageDigest.getInstance("MD5");

            m.update(pass.getBytes());

            byte[] bytes = m.digest();

            StringBuilder s = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            encryptedpassword = s.toString();

            return encryptedpassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void encryptPasswords() {
        String sqlCommand = "select * from login_details";

        try(Connection connection = DriverManager.getConnection(url, user, password);
            var preparedStatement = connection.prepareStatement(sqlCommand)) {

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String userLogin = resultSet.getString("username");
                String passLogin = resultSet.getString("pass");
                Long id_user = resultSet.getLong("id_user");

                String updateSQLCommand = "update login_details set pass=? where username=? and id_user=?";

                try(var preparedStatement1 = connection.prepareStatement(updateSQLCommand)) {
                    String encryptedPassword = encryptPassword(passLogin);

                    preparedStatement1.setString(1, encryptedPassword);
                    preparedStatement1.setString(2, userLogin);
                    preparedStatement1.setLong(3, id_user);

                    preparedStatement1.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Utilizator> validateCredentials(String username, String pass) {
        String encryptedPassword = encryptPassword(pass);

        String sqlCommand = "select * from login_details where username=? and pass=?";

        try(Connection connection = DriverManager.getConnection(url, user, password);
            var preparedStatement = connection.prepareStatement(sqlCommand)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptedPassword);

            var result = preparedStatement.executeQuery();
            if(result.next()) {
                return userService.getOne(result.getLong("id_user"));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addNewUserCredentials(Utilizator utilizator, String userAdded, String userPass) {
        String encryptedPassword = encryptPassword(userPass);

        Long id = 0l;

        String sqlCommand = "select id from users where first_name=? and last_name=?";

        try(Connection connection = DriverManager.getConnection(url, user, password);
            var preparedStatement = connection.prepareStatement(sqlCommand)) {

            preparedStatement.setString(1, utilizator.getFirstName());
            preparedStatement.setString(2, utilizator.getLastName());

            var result_set = preparedStatement.executeQuery();

            if(result_set.next()) {
                id = result_set.getLong(1);
            }

            sqlCommand = "insert into login_details(username, pass, id_user) values (?, ?, ?)";

            try(Connection newconnection = DriverManager.getConnection(url, user, password);
                var newpreparedStatement = newconnection.prepareStatement(sqlCommand)) {

                newpreparedStatement.setString(1, userAdded);
                newpreparedStatement.setString(2, encryptedPassword);
                newpreparedStatement.setLong(3, id);

                newpreparedStatement.executeUpdate();

                return true;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
