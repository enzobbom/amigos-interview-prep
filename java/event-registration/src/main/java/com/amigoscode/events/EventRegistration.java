package com.amigoscode.events;

/*
registerParticipant returns 0 for a successful registration, a positive one-based waitlist position, or -1 for failure.
findRegisteredEvents includes only confirmed registrations, never waitlisted entries.

Story:

Create events with capacity and requirements, then register eligible participants until the event reaches capacity.
 */

import java.util.*;

/** Story 1 starts here. Keep confirmed registrations and the waitlist separate. */
public final class EventRegistration {
    // storage of events and participants

    private final Set<Event> events = new HashSet<>();
    private final Set<Participant> participants = new HashSet<>();

    // events participants and waitlist

    private final Map<Event, Set<Participant>> eventsParticipants = new HashMap<>();
    private final Map<Event, Set<Participant>> eventsWaitLists = new HashMap<>();

    public boolean addEvent(String eventId, String eventType, int capacity, String requirements) {
        Optional<Event> newEvent = Event.create(
                eventId,
                eventType,
                capacity,
                requirements
        );

        if (newEvent.isEmpty()) {
            return false;
        }

        if (events.contains(newEvent.get())) {
            return false;
        }
        events.add(newEvent.get());

        return true;
    }

    public int registerParticipant(String eventId, String participantId, String email, String skillLevel) {
        if (eventId.isBlank() || participantId.isBlank() || email.isBlank() || skillLevel.isBlank()) {
            return -1;
        }

        // create and add participant to participants storage, if not exists

        Participant newParticipant = new Participant(
                eventId,
                participantId,
                email,
                skillLevel
        );

        if (participants.contains(newParticipant)) {
            // if this check passes, it means the participant is brand new and therefore is not contained neither in the
            // eventsParticipants nor eventsWaitLists sets

            return -1;
        }

        participants.add(newParticipant);

        // get the event

        Event event = events.stream()
                .filter(e -> e.getEventId().equals(eventId))
                .findFirst()
                .orElse(null);

        if (event == null) {
            return -1;
        }

        // get current participants of the event and check for availability, Put in the waiting list if full

        eventsParticipants.putIfAbsent(event, new HashSet<>());
        Set<Participant> eventParticipants = eventsParticipants.get(event);

        if (eventParticipants.size() >= event.getCapacity()) {
            eventsWaitLists.putIfAbsent(event, new HashSet<>());
            Set<Participant> eventWaitList = eventsWaitLists.get(event);

            eventWaitList.add(newParticipant);
            return eventWaitList.size();
        }

        // if all checks pass, simply register new participant to

        eventParticipants.add(newParticipant);
        return 0;
    }

    public boolean cancelRegistration(String eventId, String participantId) {
        throw new UnsupportedOperationException("story 3");
    }

    public String[] findRegisteredEvents(String participantId) {
        throw new UnsupportedOperationException("story 4");
    }
}
