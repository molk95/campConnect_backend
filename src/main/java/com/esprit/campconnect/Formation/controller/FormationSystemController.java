package com.esprit.campconnect.Formation.controller;

import com.esprit.campconnect.Formation.dto.system.FormationDatasourceInfoDto;
import com.esprit.campconnect.Formation.service.system.FormationDatasourceInspectorService;
import com.esprit.campconnect.User.Entity.Role;
import com.esprit.campconnect.User.Entity.Utilisateur;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/formations/system")
@CrossOrigin(origins = "http://localhost:4200")
public class FormationSystemController {

    private final FormationDatasourceInspectorService inspectorService;

    public FormationSystemController(FormationDatasourceInspectorService inspectorService) {
        this.inspectorService = inspectorService;
    }

    @GetMapping("/datasource")
    public FormationDatasourceInfoDto inspectDatasource(Authentication authentication) {
        Utilisateur user = requireAuthenticatedUser(authentication);
        assertGuideOrAdminRole(user);
        return inspectorService.inspect();
    }

    private Utilisateur requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Utilisateur user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        return user;
    }

    private void assertGuideOrAdminRole(Utilisateur user) {
        if (user.getRole() == Role.GUIDE || user.getRole() == Role.ADMINISTRATEUR) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action reservee aux guides et admins");
    }
}
