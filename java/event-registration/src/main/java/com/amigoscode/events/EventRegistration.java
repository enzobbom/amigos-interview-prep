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
    private final Map<Event, Deque<Participant>> eventsWaitLists = new HashMap<>();

    public boolean addEvent(String eventId, String eventType, int capacity, String requirements) {
        Optional<Event> optionalEvent = Event.create(
                eventId,
                eventType,
                capacity,
                requirements
        );

        if (!optionalEvent.isPresent()) {
            return false;
        }

        return events.add(optionalEvent.get());
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

        Optional<Event> optionalEvent = events.stream()
                .filter(e -> e.getEventId().equals(eventId))
                .findFirst();

        if (!optionalEvent.isPresent()) {
            return -1;
        }

        Event event = optionalEvent.get();

        // check if participant is allowed in this event

        if (!event.isParticipantAllowed(email, skillLevel)) {
            return -1;
        }

        // get current participants of the event and check for availability, put in the waiting list if full

        eventsParticipants.putIfAbsent(event, new HashSet<>());
        Set<Participant> eventParticipants = eventsParticipants.get(event);

        if (eventParticipants.size() >= event.getCapacity()) {
            eventsWaitLists.putIfAbsent(event, new ArrayDeque<>());
            Queue<Participant> eventWaitList = eventsWaitLists.get(event);

            eventWaitList.add(newParticipant);
            return eventWaitList.size();
        }

        // if all checks pass, simply register new participant to

        eventParticipants.add(newParticipant);
        return 0;
    }

    public boolean cancelRegistration(String eventId, String participantId) {
        if (eventId.isBlank() || participantId.isBlank()) {
            return false;
        }

        // find event and participant

        Optional<Event> optionalEvent = events.stream()
                .filter(e -> e.getEventId().equals(eventId))
                .findFirst();

        if (!optionalEvent.isPresent()) {
            return false;
        }
        Event event = optionalEvent.get();

        Optional<Participant> optionalParticipant = participants.stream()
                .filter(p -> p.getParticipantId().equals(participantId))
                .findFirst();

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
}
