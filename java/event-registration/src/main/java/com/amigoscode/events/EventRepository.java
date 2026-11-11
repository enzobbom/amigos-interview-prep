package com.amigoscode.events;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class EventRepository {
    private final Set<Event> events = new HashSet<>();

    public void save(Event event) {
        events.add(event);
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    public Optional<Event> findById(String id) {
        return events.stream()
                .filter(e -> e.getEventId().equals(id))
                .findFirst();
    }
}
