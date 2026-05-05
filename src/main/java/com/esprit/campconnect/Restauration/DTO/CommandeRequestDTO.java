package com.esprit.campconnect.Restauration.DTO;
import lombok.Data;
import java.util.List;

@Data
public class CommandeRequestDTO {

    private List<LigneCommandeDTO> lignes;

}
