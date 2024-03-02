package ro.ubbcluj.map.socialnetworkgui.utils.observer;

import ro.ubbcluj.map.socialnetworkgui.utils.events.Event;

public interface Observable<E extends Event> {
    void addObserver(Observer<E> obs);
    void removeObserver(Observer<E> obs);
    void notifyObservers(E e);
}
