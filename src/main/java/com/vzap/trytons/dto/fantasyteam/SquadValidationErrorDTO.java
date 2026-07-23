package com.vzap.trytons.dto.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadValidationErrorDTO {
    private String code;
    private String message;
    private String field;
}
