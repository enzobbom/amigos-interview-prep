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
    private final EventRepository eventRepository = new EventRepository();
    private final ParticipantRepository participantRepository = new ParticipantRepository();

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

        Event event = optionalEvent.get();
        if (eventRepository.existsById(eventId)) {
            return false;
        }

        eventRepository.save(event);

        return true;
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

        if (participantRepository.existsByIdAndEventId(participantId, eventId)) {
            return -1;
        }
        participantRepository.save(newParticipant);

        // get the event

        Optional<Event> optionalEvent = eventRepository.findById(eventId);
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

        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (!optionalEvent.isPresent()) {
            return false;
        }
        Event event = optionalEvent.get();

        Optional<Participant> optionalParticipant = participantRepository.findById(participantId);
        if (!optionalParticipant.isPresent()) {
            return false;
        }
        Participant participant = optionalParticipant.get();

        // remove participants with checks and promotion logic (wait list -> participants list)

        return event.removeParticipant(participant);
    }

    public String[] findRegisteredEvents(String participantId) {
        throw new UnsupportedOperationException("story 4");
    }
}
