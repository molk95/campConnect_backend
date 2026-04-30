package com.esprit.campconnect.Formation.service.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FormationSchemaAlignmentRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            alignGuideStepCompletionForeignKeys();
        } catch (Exception ex) {
            log.error("Schema alignment for Formation/Guide failed: {}", ex.getMessage(), ex);
        }
    }

    private void alignGuideStepCompletionForeignKeys() {
        if (!tableExists("guide_step_completion")) {
            return;
        }

        normalizeForeignKeys(
                "guide_step_completion",
                "progress_id",
                "guide_progress_user",
                "fk_gsc_progress_user"
        );

        normalizeForeignKeys(
                "guide_step_completion",
                "step_id",
                "guide_interactif_step",
                "fk_gsc_step_interactif"
        );
    }

    private void normalizeForeignKeys(String tableName,
                                      String columnName,
                                      String expectedReferencedTable,
                                      String desiredConstraintName) {
        List<FkInfo> fkInfos = jdbcTemplate.query(
                """
                SELECT CONSTRAINT_NAME, REFERENCED_TABLE_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                  AND REFERENCED_TABLE_NAME IS NOT NULL
                """,
                (rs, rowNum) -> new FkInfo(
                        rs.getString("CONSTRAINT_NAME"),
                        rs.getString("REFERENCED_TABLE_NAME")
                ),
                tableName,
                columnName
        );

        List<String> expectedFkNames = new ArrayList<>();
        for (FkInfo fkInfo : fkInfos) {
            if (expectedReferencedTable.equalsIgnoreCase(fkInfo.referencedTableName())) {
                expectedFkNames.add(fkInfo.constraintName());
                continue;
            }

            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + fkInfo.constraintName());
            log.info("Dropped legacy FK {} on {}.{} referencing {}", fkInfo.constraintName(), tableName, columnName, fkInfo.referencedTableName());
        }

        if (expectedFkNames.size() > 1) {
            for (int i = 1; i < expectedFkNames.size(); i++) {
                String duplicateFk = expectedFkNames.get(i);
                jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + duplicateFk);
                log.info("Dropped duplicate FK {} on {}.{}", duplicateFk, tableName, columnName);
            }
            expectedFkNames = expectedFkNames.subList(0, 1);
        }

        if (!expectedFkNames.isEmpty()) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE " + tableName
                        + " ADD CONSTRAINT " + desiredConstraintName
                        + " FOREIGN KEY (" + columnName + ") REFERENCES " + expectedReferencedTable + "(id)"
        );

        log.info("Added FK {} on {}.{} -> {}.id", desiredConstraintName, tableName, columnName, expectedReferencedTable);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """,
                Integer.class,
                tableName
        );

        return count != null && count > 0;
    }

    private record FkInfo(String constraintName, String referencedTableName) {
    }
}
