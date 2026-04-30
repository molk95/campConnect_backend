package com.esprit.campconnect.Formation.service.system;

import com.esprit.campconnect.Formation.dto.system.FormationDatasourceInfoDto;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class FormationDatasourceInspectorService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public FormationDatasourceInspectorService(DataSource dataSource,
                                               JdbcTemplate jdbcTemplate,
                                               Environment environment) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    public FormationDatasourceInfoDto inspect() {
        FormationDatasourceInfoDto dto = new FormationDatasourceInfoDto();
        dto.setActiveProfiles(Arrays.asList(environment.getActiveProfiles()));

        try (Connection connection = dataSource.getConnection()) {
            dto.setJdbcUrl(connection.getMetaData().getURL());
        } catch (Exception exception) {
            dto.setJdbcUrl("unavailable: " + exception.getMessage());
        }

        Map<String, Object> dbInfo = jdbcTemplate.queryForMap(
                "SELECT DATABASE() AS dbName, @@hostname AS dbHost, @@port AS dbPort"
        );
        dto.setCurrentDatabase(stringValue(dbInfo.get("dbName")));
        dto.setDbHost(stringValue(dbInfo.get("dbHost")));
        dto.setDbPort(stringValue(dbInfo.get("dbPort")));

        Integer formationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM formation",
                Integer.class
        );
        dto.setFormationCount(formationCount != null ? formationCount : 0);

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                """
                SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'formation'
                ORDER BY ORDINAL_POSITION
                """
        );

        List<Map<String, String>> normalizedColumns = columns.stream()
                .map(row -> Map.of(
                        "column", stringValue(row.get("COLUMN_NAME")),
                        "type", stringValue(row.get("DATA_TYPE")),
                        "nullable", stringValue(row.get("IS_NULLABLE"))
                ))
                .toList();
        dto.setFormationColumns(normalizedColumns);

        return dto;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
