package me.pinitnotification.infrastructure.persistence.notification;

import me.pinitnotification.application.notification.NotificationDispatchItem;
import me.pinitnotification.application.notification.NotificationDispatchQueryRepository;
import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public class NotificationDispatchQueryRepositoryAdapter implements NotificationDispatchQueryRepository {
    private static final String FIND_DUE_WITH_TOKENS_SQL = """
            SELECT n.public_id,
                   n.owner_id,
                   n.schedule_id,
                   n.schedule_title,
                   n.schedule_start_time,
                   n.idempotent_key,
                   ps.token
            FROM upcoming_schedule_notification n
                     LEFT JOIN push_subscription ps ON ps.member_id = n.owner_id
            WHERE n.schedule_start_time IS NOT NULL
              AND n.schedule_start_time <= ?
            """;

    private final JdbcClient jdbcClient;

    public NotificationDispatchQueryRepositoryAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<NotificationDispatchItem> findDueNotificationsWithTokens(Instant now) {
        List<DispatchRow> rows = jdbcClient.sql(FIND_DUE_WITH_TOKENS_SQL)
                .param(now.toString())
                .query((rs, rowNum) -> new DispatchRow(
                        UUID.fromString(rs.getString("public_id")),
                        rs.getLong("owner_id"),
                        rs.getLong("schedule_id"),
                        rs.getString("schedule_title"),
                        rs.getString("schedule_start_time"),
                        rs.getString("idempotent_key"),
                        rs.getString("token")
                ))
                .list();

        if (rows.isEmpty()) {
            return List.of();
        }

        Map<UUID, DispatchAccumulator> aggregated = new LinkedHashMap<>();
        for (DispatchRow row : rows) {
            DispatchAccumulator accumulator = aggregated.computeIfAbsent(
                    row.notificationId,
                    id -> new DispatchAccumulator(
                            toDomain(row),
                            new ArrayList<>()
                    )
            );
            if (row.token != null) {
                accumulator.tokens.add(row.token);
            }
        }

        return aggregated.values().stream()
                .map(accumulator -> new NotificationDispatchItem(accumulator.notification, List.copyOf(accumulator.tokens)))
                .toList();
    }

    private UpcomingScheduleNotification toDomain(DispatchRow row) {
        return new UpcomingScheduleNotification(
                row.notificationId,
                row.ownerId,
                row.scheduleId,
                row.scheduleTitle,
                row.scheduleStartTime,
                row.idempotentKey
        );
    }

    private record DispatchRow(
            UUID notificationId,
            Long ownerId,
            Long scheduleId,
            String scheduleTitle,
            String scheduleStartTime,
            String idempotentKey,
            String token
    ) {
    }

    private static class DispatchAccumulator {
        private final UpcomingScheduleNotification notification;
        private final List<String> tokens;

        private DispatchAccumulator(UpcomingScheduleNotification notification, List<String> tokens) {
            this.notification = notification;
            this.tokens = tokens;
        }
    }
}
