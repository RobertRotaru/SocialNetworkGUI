package ro.ubbcluj.map.socialnetworkgui.domain;

public enum RequestStatus {
    ACCEPTED, PENDING, REJECTED;

    @Override
    public String toString() {
        return toString(this);
    }

    public String toString(RequestStatus requestStatus) {
        switch (requestStatus){
            case PENDING -> {
                return "pending";
            }
            case ACCEPTED -> {
                return "accepted";
            }
            case REJECTED -> {
                return "rejected";
            }
            default -> {
                return null;
            }
        }
    }

    public static RequestStatus fromString(String s) {
        switch (s) {
            case "pending" -> {
                return PENDING;
            }
            case "accepted" -> {
                return ACCEPTED;
            }
            case "rejected" -> {
                return REJECTED;
            }
            default -> {
                return null;
            }
        }
    }
}
