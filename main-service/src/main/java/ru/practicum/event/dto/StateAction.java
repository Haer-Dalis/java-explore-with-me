package ru.practicum.event.dto;

import ru.practicum.exception.NotFoundException;

public enum StateAction {
    SEND_TO_REVIEW,
    PUBLISH_EVENT,
    CANCEL_REVIEW,
    REJECT_EVENT;

    public static StateAction throwException(String stateAction) {
        try {
            return StateAction.valueOf(stateAction.toUpperCase());
        } catch (Exception e) {
            throw new NotFoundException("Неизвестное значение  StateAction" + stateAction);
        }
    }
}
