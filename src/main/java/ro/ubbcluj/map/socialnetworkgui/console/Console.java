package ro.ubbcluj.map.socialnetworkgui.console;

import ro.ubbcluj.map.socialnetworkgui.domain.Entity;

/**
 * Console for GUI with user
 * @param <ID> type of the ID's of the entities
 * @param <E> type of entities
 */
public interface Console<ID, E extends Entity<ID>> {

    /**
     * Show the GUI
     */
    void showUI();
}
