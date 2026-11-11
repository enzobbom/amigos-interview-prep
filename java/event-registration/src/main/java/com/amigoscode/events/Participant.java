package com.amigoscode.events;

import java.util.Objects;
import java.util.Optional;

public class Participant {
    private final String eventId;
    private final String participantId;
    private final String email;
    private final String skillLevel;

    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+\\x2f=?^_`\\x7b-\\x7d~\\x2d]+(?:\\.[a-z0-9!#$%&'*+\\x2f" +
            "=?^_`\\x7b-\\x7d~\\x2d]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-" +
            "\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9\\x2d]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9\\x2d]*[a-z0-9])?|" +
            "\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9" +
            "\\x2d]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])\n" +
            "\n";

    private Participant(String eventId, String participantId, String email, String skillLevel) {
        this.eventId = eventId;
        this.participantId = participantId;
        this.email = email;
        this.skillLevel = skillLevel;
    }

    public static Optional<Participant> create(String eventId, String participantId, String email, String skillLevel) {
        if (eventId.isBlank() || participantId.isBlank() || !isValidEmail(email) || skillLevel.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(
                new Participant(eventId,
                        participantId,
                        email,
                        skillLevel)
        );
    }

    public String getEventId() {
        return eventId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public String getEmail() {
        return email;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(participantId, that.participantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, participantId);
    }

    // validation

    private static boolean isValidEmail(String email) {
        return !email.isBlank() && email.matches(EMAIL_REGEX);
    }
}
