package com.esprit.campconnect.Formation.dto.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormationDatasourceInfoDto {

    private String jdbcUrl;
    private String currentDatabase;
    private String dbHost;
    private String dbPort;
    private List<String> activeProfiles = new ArrayList<>();
    private Integer formationCount;
    private List<Map<String, String>> formationColumns = new ArrayList<>();

    public FormationDatasourceInfoDto() {
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
    }

    public void setActiveProfiles(List<String> activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    public Integer getFormationCount() {
        return formationCount;
    }

    public void setFormationCount(Integer formationCount) {
        this.formationCount = formationCount;
    }

    public List<Map<String, String>> getFormationColumns() {
        return formationColumns;
    }

    public void setFormationColumns(List<Map<String, String>> formationColumns) {
        this.formationColumns = formationColumns;
    }
}
