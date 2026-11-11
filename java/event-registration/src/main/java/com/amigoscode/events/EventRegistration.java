package com.amigoscode.events;

/*
registerParticipant returns 0 for a successful registration, a positive one-based waitlist position, or -1 for failure.
findRegisteredEvents includes only confirmed registrations, never waitlisted entries.

Story:

Create events with capacity and requirements, then register eligible participants until the event reaches capacity.
 */

import java.util.*;

/**
 * Story 1 starts here. Keep confirmed registrations and the waitlist separate.
 */
public final class EventRegistration {
    // storage of events and participants

    private final Set<Event> events = new HashSet<>();
    private final Set<Participant> participants = new HashSet<>();

    // events participants and waitlist

    private final Map<Event, Set<Participant>> eventsParticipants = new HashMap<>();
    private final Map<Event, Deque<Participant>> eventsWaitLists = new HashMap<>();

    public boolean addEvent(String eventId, String eventType, int capacity, String requirements) {
        Optional<Event> optionalEvent = Event.create(
                eventId,
                eventType,
                capacity,
                requirements
        );

        if (optionalEvent.isEmpty()) {
            return false;
        }

        return events.add(optionalEvent.get());
    }

    public int registerParticipant(String eventId, String participantId, String email, String skillLevel) {
        Optional<Participant> optionalParticipant = Participant.create(
                eventId,
                participantId,
                email,
                skillLevel
        );

        if (optionalParticipant.isEmpty()) {
            return -1;
        }
        Participant newParticipant = optionalParticipant.get();

        // save new participant in case it didn't exist yet

        if (participants.contains(newParticipant)) {
            // if this check passes, it means the participant is brand new and therefore is not contained neither in events
            // list nor wait lists

            return -1;
        }
        participants.add(newParticipant);

        // get the event

        Optional<Event> optionalEvent = getEventById(eventId);
        if (!optionalEvent.isPresent()) {
            return -1;
        }

        // add participant with checks on duplication and capacity

        return optionalEvent.get().addParticipant(newParticipant);
    }

    public boolean cancelRegistration(String eventId, String participantId) {
        if (eventId.isBlank() || participantId.isBlank()) {
            return false;
        }

        // find event and participant

        Optional<Event> optionalEvent = getEventById(eventId);
        if (!optionalEvent.isPresent()) {
            return false;
        }
        Event event = optionalEvent.get();

        Optional<Participant> optionalParticipant = getParticipantById(participantId);
        if (!optionalParticipant.isPresent()) {
            return false;
        }
        Participant participant = optionalParticipant.get();

        // check if participant is within event list or waitlist

        Deque<Participant> waitList = eventsWaitLists.get(event);
        if (waitList.removeFirstOccurrence(participant)) {
            return true;
        }

        Set<Participant> participants = eventsParticipants.get(event);
        if (!participants.remove(participant)) {
            return false;
        }

        Optional<Participant> firstWaitListedParticipant = Optional.ofNullable(waitList.peek());

        if (firstWaitListedParticipant.isEmpty()) {
            return false;
        }

        return participants.add(waitList.pop());
    }

    public String[] findRegisteredEvents(String participantId) {
        throw new UnsupportedOperationException("story 4");
    }

    // helper methods

    private Optional<Event> getEventById(String eventId) {
        return events.stream()
                .filter(e -> e.getEventId().equals(eventId))
                .findFirst();
    }

    private Optional<Participant> getParticipantById(String participantId) {
        return participants.stream()
                .filter(p -> p.getParticipantId().equals(participantId))
                .findFirst();
    }
}
