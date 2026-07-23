package com.vzap.trytons.dto.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadValidationResultDTO{
    @Builder.Default
    private List<SquadValidationErrorDTO> errors = new ArrayList<>();

    public boolean isValid(){
        return errors.isEmpty();
    }

    public void addError(String code, String message, String field){
        SquadValidationErrorDTO newError = SquadValidationErrorDTO.builder().code(code)
                .message(message)
                .field(field)
                .build();
        errors.add(newError);
    }
}
