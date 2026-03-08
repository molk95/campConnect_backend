package com.esprit.campconnect.User.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ProfilDTO {
    String adresse;

    String photo;

    String biographie;
}
