package ro.ubbcluj.map.socialnetworkgui.factories;


import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;

/**
 * Factory which is implemented using Singleton Design Pattern and sets
 * an ID for a given User
 */
public class UserFactory {
    private static UserFactory instance;

    private static Long NumID = 1l;

    private UserFactory(){}

    public static UserFactory getInstance() {
        if(instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    /**
     * Sets the ID for the user u with startingID + 1
     * @param u - the user whose ID is being set
     * @param startingID - the maximum ID present in the social network
     */
    public void setID(Utilizator u, Long startingID) {
        NumID = startingID;
        u.setId(NumID);
        NumID++;
    }
}
