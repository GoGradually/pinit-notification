package me.pinitnotification.infrastructure.batch;

import me.pinitnotification.domain.shared.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("backfill-public-id")
@Component
public class PublicIdBackfillRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PublicIdBackfillRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final IdGenerator idGenerator;
    private final int batchSize;

    public PublicIdBackfillRunner(JdbcTemplate jdbcTemplate,
                                  IdGenerator idGenerator,
                                  @Value("${backfill.public-id.batch-size:500}") int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.idGenerator = idGenerator;
        this.batchSize = batchSize;
    }

    @Override
    public void run(String... args) {
        backfillTable("upcoming_schedule_notification");
        backfillTable("push_subscription");
    }

    private void backfillTable(String table) {
        int total = 0;
        while (true) {
            List<Long> ids = jdbcTemplate.query(
                    "select id from " + table + " where public_id is null limit ?",
                    ps -> ps.setInt(1, batchSize),
                    (rs, rowNum) -> rs.getLong(1)
            );

            if (ids.isEmpty()) {
                break;
            }

            for (Long id : ids) {
                int updated = jdbcTemplate.update(
                        "update " + table + " set public_id = ? where id = ? and public_id is null",
                        idGenerator.generate().toString(),
                        id
                );
                total += updated;
            }
        }

        log.info("Backfill completed for table={}, updatedRows={}", table, total);
    }
}
