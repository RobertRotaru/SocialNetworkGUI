package ro.ubbcluj.map.socialnetworkgui.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A friendship is identified by a unique combination of
 * two IDs of existing users
 */
public class Prietenie extends Entity<Tuple<Long,Long>> {

    LocalDate date;

    public Prietenie() {
        date = LocalDate.now();
    }


    /**
     * Compare two friendships by IDs, member by member
     * @param ot - the second friendship
     * @return true if this frienship is "bigger" than the other one,
     *          false otherwise
     */
    public boolean compare(Prietenie ot) {
        return this.getId().getLeft() > ot.getId().getRight() ||
                (this.getId().getLeft() == ot.getId().getLeft() &&
                        this.getId().getRight() > ot.getId().getRight());
    }

    /**
     * Swaps the IDs, in case they are not in ascending order
     */
    public void conv() {
        if(this.getId().getLeft() > this.getId().getRight()) {
            this.setId(new Tuple<>(this.getId().getRight(), this.getId().getLeft()));
        }
    }

    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return "Between UserID: " + this.getId().getLeft().toString() +
                " and UserID: " + this.getId().getRight().toString() +
                " established at date " + this.getDate().format(timeFormatter).toString();
    }

    /**
     * Sets the date
     * @param date LocalDateTime
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
