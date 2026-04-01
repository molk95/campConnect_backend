package com.esprit.campconnect.Reservation.DTO;

import com.esprit.campconnect.Reservation.Enum.PaymentStatus;
import com.esprit.campconnect.Reservation.Enum.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequestDTO {

    @NotNull(message = "L'ID de l'utilisateur est requis")
    private Long utilisateurId;

    @NotNull(message = "L'ID de l'événement est requis")
    private Long eventId;

    @NotNull(message = "Le nombre de participants est requis")
    @Min(value = 1, message = "Au minimum 1 participant est requis")
    @Max(value = 100, message = "Maximum 100 participants par réservation")
    private Integer nombreParticipants;

    @Size(max = 500, message = "Les remarques ne doivent pas dépasser 500 caractères")
    private String remarques;
}
