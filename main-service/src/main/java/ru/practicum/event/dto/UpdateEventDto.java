package ru.practicum.event.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateEventDto extends UpdateEventBase {
    private StateAction stateAction;
}
