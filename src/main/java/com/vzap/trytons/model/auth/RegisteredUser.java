package com.vzap.trytons.model.auth;

import com.vzap.trytons.enums.RegistrationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RegisteredUser extends User {
    private RegistrationStatus registrationStatus;
}