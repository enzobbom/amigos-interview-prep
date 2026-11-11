package com.amigoscode.events;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ParticipantRepository {
    private final Set<Participant> participants = new HashSet<>();

    public void save(Participant participant) {
        participants.add(participant);
    }

    public boolean existsByIdAndEventId(String id, String eventId) {
        return participants.stream()
                .anyMatch(p -> p.getParticipantId().equals(id)
                        && p.getEventId().equals(eventId));
    }

    public Optional<Participant> findById(String id) {
        return participants.stream()
                .filter(p -> p.getParticipantId().equals(id))
                .findFirst();
    }
}
