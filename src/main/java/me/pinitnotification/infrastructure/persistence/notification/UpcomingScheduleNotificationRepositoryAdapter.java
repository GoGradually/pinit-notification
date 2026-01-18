package me.pinitnotification.infrastructure.persistence.notification;

import me.pinitnotification.domain.notification.UpcomingScheduleNotification;
import me.pinitnotification.domain.notification.UpcomingScheduleNotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UpcomingScheduleNotificationRepositoryAdapter implements UpcomingScheduleNotificationRepository {
    private final UpcomingScheduleNotificationJpaRepository jpaRepository;

    public UpcomingScheduleNotificationRepositoryAdapter(UpcomingScheduleNotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<UpcomingScheduleNotification> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<UpcomingScheduleNotification> findByScheduleIdAndOwnerId(Long scheduleId, Long ownerId) {
        return jpaRepository.findByScheduleIdAndOwnerId(scheduleId, ownerId)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByScheduleIdAndOwnerId(Long scheduleId, Long ownerId) {
        return jpaRepository.existsByScheduleIdAndOwnerId(scheduleId, ownerId);
    }

    @Override
    public UpcomingScheduleNotification save(UpcomingScheduleNotification notification) {
        UpcomingScheduleNotificationEntity saved = jpaRepository.save(toEntity(notification));
        return toDomain(saved);
    }

    @Override
    public void deleteByScheduleIdAndOwnerId(Long scheduleId, Long ownerId) {
        jpaRepository.deleteByScheduleIdAndOwnerId(scheduleId, ownerId);
    }

    @Override
    public void deleteAllInBatch(List<UpcomingScheduleNotification> notifications) {
        List<UpcomingScheduleNotificationEntity> entities = notifications.stream()
                .map(this::toEntity)
                .toList();
        jpaRepository.deleteAllInBatch(entities);
    }

    private UpcomingScheduleNotification toDomain(UpcomingScheduleNotificationEntity entity) {
        return new UpcomingScheduleNotification(
                entity.getPublicId(),
                entity.getOwnerId(),
                entity.getScheduleId(),
                entity.getScheduleTitle(),
                entity.getScheduleStartTime(),
                entity.getIdempotentKey()
        );
    }

    private UpcomingScheduleNotificationEntity toEntity(UpcomingScheduleNotification domain) {
        UpcomingScheduleNotificationEntity entity = new UpcomingScheduleNotificationEntity();
        entity.setPublicId(domain.getId());
        entity.setOwnerId(domain.getOwnerId());
        entity.setScheduleId(domain.getScheduleId());
        entity.setScheduleTitle(domain.getScheduleTitle());
        entity.setScheduleStartTime(domain.getScheduleStartTime());
        entity.setIdempotentKey(domain.getIdempotentKey());
        return entity;
    }
}
