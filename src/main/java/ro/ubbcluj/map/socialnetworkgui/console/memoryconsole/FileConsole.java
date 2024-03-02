package ro.ubbcluj.map.socialnetworkgui.console.memoryconsole;

import ro.ubbcluj.map.socialnetworkgui.console.Console;
import ro.ubbcluj.map.socialnetworkgui.domain.Entity;
import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.FriendshipAlreadyExistsException;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.PrietenieValidator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.UtilizatorValidator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.factories.UserFactory;
import ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos.PrietenieFileRepository;
import ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos.UtilizatorFileRepository;
import ro.ubbcluj.map.socialnetworkgui.service.SocialNetworkService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.MessageDBService;
import ro.ubbcluj.map.socialnetworkgui.service.dbservices.RequestDBService;
import ro.ubbcluj.map.socialnetworkgui.service.memoryservices.FileFriendshipService;
import ro.ubbcluj.map.socialnetworkgui.service.memoryservices.FileUserService;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Implements the console to save data in memory
 * @param <ID> type of the ID's of the entities
 * @param <E> type of entities
 */
public class FileConsole<ID, E extends Entity<ID>> implements Console<ID, E> {

    private final SocialNetworkService service;

    /**
     * Default constructor
     */
    public FileConsole(String usersFileName, String friendshipFileName){
        service = new SocialNetworkService(
                new FileUserService(new UtilizatorFileRepository(
                        usersFileName,
                        new UtilizatorValidator())),
                new FileFriendshipService(new PrietenieFileRepository(
                        friendshipFileName,
                        new PrietenieValidator())),
                new MessageDBService(
                        "ala", "bala", "portocala"
                ),
                new RequestDBService(
                        "ala", "bala", "portocala"
                ),
                "ala", "bala", "portocala"
        );
    }

    /**
     * Prints all users
     */
    private void printAllUsers() {
        if(service.getAllUsers() == null) {
            System.out.println("There are currently no users!");
            return;
        }
        System.out.println();
        System.out.println("Users: ");
        service.getAllUsers().forEach(System.out::println);
    }

    /**
     * Print all friendships
     */
    private void printAllFriendships() {
        if(service.getAllFreindships() == null) {
            System.out.println("There are currently no friendships!");
            return;
        }
        System.out.println();
        System.out.println("Friendships: ");
        service.getAllFreindships().forEach(System.out::println);
    }

    /**
     * Execute the first option - Add a User
     * @param in - Scanner used
     */
    private void firstOption(Scanner in) {
        System.out.println("Enter first name: ");
        String firstName = in.nextLine();
        System.out.println("Enter last name:");
        String lastName = in.nextLine();

        Utilizator utilizator = new Utilizator(firstName, lastName);
        UserFactory.getInstance().setID(utilizator,
                service.getLastIDUsers() + 1); //sets an id for the user

        try {
            service.addUser(utilizator);
            System.out.println("The user with " + utilizator
                    + " has been added successfully!");
        }catch(ValidationException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Execute second operation - Delete a User
     * @param in - Scanner used
     */
    private void secondOption(Scanner in) {
        System.out.println("You can choose between these users: ");
        printAllUsers();
        System.out.println("Enter the ID of the user you want to be deleted: ");
        Long ID = in.nextLong();

        try {
            var user = service.deleteUser(ID);
            if(user.isPresent()) {
                System.out.println("The user with: " + user.get()
                        + " has been removed successfully!");
            }
            else {
                System.out.println("There is no user with the ID " + ID);
            }
        } catch(IllegalArgumentException | FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Execute the third operation - establish a friendship
     * @param in - Scanner used
     */
    private void thirdOption(Scanner in) {
        if(service.getNoUsers() < 2) {
            System.out.println("A friendships can't be established" +
                    " (not enough users)");
            return;
        }
        System.out.println("You can choose to establish a friendship" +
                " between two of these users");
        printAllUsers();

        System.out.println("Enter the ID of the first user: ");
        Long id1 = in.nextLong();
        System.out.println("Enter the ID of the second user: ");
        Long id2 = in.nextLong();

        try {
            Prietenie prietenie = new Prietenie();
            prietenie.setId(new Tuple<>(id1, id2));

            service.addFriendship(prietenie);

            if(service.getUser(id1).isPresent() &&
                service.getUser(id2).isPresent()) {
                var user1 = service.getUser(id1).get();
                var user2 = service.getUser(id2).get();

                System.out.println("The friendship between ");
                System.out.println(user1.getFirstName() + " " + user1.getLastName()
                        + " and " +
                        user2.getFirstName() + " " + user2.getLastName());
                System.out.println("Has been established successfully!");
            }
        } catch(IllegalArgumentException |
               ValidationException |
                FriendshipAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Execute the forth oprion - Delete a friendship
     * @param in - Scanner used
     */
    private void forthOption(Scanner in) {
        System.out.println("These are the");
        printAllFriendships();

        System.out.println("Please enter the IDs of the users whose friendship you " +
                "want to be deleted, seperated by comma:");
        String string = in.nextLine();

        var ids = Arrays.asList(string.split(","));
        Tuple<Long, Long> tuple = new Tuple<>(
                Long.parseLong(ids.get(0)),
                Long.parseLong(ids.get(1))
        );

        try {
            var friendship = service.deleteFriendship(tuple);

            if(friendship.isEmpty()) {
                throw new ValidationException("IDs can't be found!");
            }

            if(service.getUser(tuple.getLeft()).isPresent() &&
                    service.getUser(tuple.getRight()).isPresent()) {
                var user1 = service.getUser(tuple.getLeft()).get();
                var user2 = service.getUser(tuple.getRight()).get();

                System.out.println("The friendship between ");
                System.out.println(user1.getFirstName() + " " + user1.getLastName()
                        + " and " +
                        user2.getFirstName() + " " + user2.getLastName());
                System.out.println("Has been removed successfully!");
            }
        } catch (FileNotFoundException |
                 IllegalArgumentException |
                 ValidationException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Execute the fifth task - Number of communities
     */
    private void fifthOption() {
        int noCommunities = service.getCommunities();
        System.out.println("The number of the communities in the social" +
                " network is " + noCommunities);
    }

    /**
     * Execute the sixth task - Largest community
     */
    private void sixthOption() {
        var largestCommunity = service.getLargestCommunity();
        if(largestCommunity == null) {
            System.out.println("There is no comunity in the social network");
            return;
        }

        System.out.println("The largest community memebers: ");
        largestCommunity.forEach(System.out::println);
    }

    /**
     * Execute the ninth option
     * @param in - Scanner used
     */
    private void ninthOption(Scanner in) {
        System.out.println("Provide the number of friends: ");
        int n = in.nextInt();
        var usersList = service.getUsersWithMinimumNFriends(n);
        if(usersList.isEmpty()) {
            System.out.println("There are no users with minimum of "
                + n + " friends");
        }
        else {
            System.out.println();
            System.out.println("Users at least with " + n + " friendships: ");
            usersList.forEach(user ->
                    System.out.println(user + " No friendships: " + user.getFriends().size()));
        }
    }

    @Override
    public void showUI() {
        Scanner in = new Scanner(System.in);
        while(true) {
            System.out.println("1. Add an user");
            System.out.println("2. Delete an user");
            System.out.println("3. Establish an friendship");
            System.out.println("4. Delete an friendship");
            System.out.println("5. Get number of communities");
            System.out.println("6. Get the largest community");
            System.out.println("7. Show all users");
            System.out.println("8. Show all friendships");
            System.out.println("9. Show all users with at least N friends");
            System.out.println("10. Exit");
            System.out.println();
            System.out.println("Enter your option: ");
            int option = in.nextInt();
            in.nextLine();
            if(option == 1) {
                firstOption(in);
            }
            else if(option == 2) {
                secondOption(in);
            }
            else if(option == 3) {
                thirdOption(in);
            }
            else if(option == 4) {
                forthOption(in);
            }
            else if(option == 5) {
                fifthOption();
            }
            else if(option == 6) {
                sixthOption();
            }
            else if(option == 7) {
                printAllUsers();
            }
            else if(option == 8) {
                printAllFriendships();
            }
            else if(option == 9) {
                ninthOption(in);
            }
            else if(option == 10) {
                break;
            }
            else {
                System.out.println("Wrong option!");
            }
        }
    }
}
