package me.pinitnotification.infrastructure.events.listener;

import me.pinitnotification.application.notification.ScheduleNotificationService;
import me.pinitnotification.application.notification.command.ScheduleDeletedCommand;
import me.pinitnotification.application.notification.command.ScheduleStateChangedCommand;
import me.pinitnotification.application.notification.command.UpcomingUpdatedCommand;
import me.pinitnotification.infrastructure.events.payload.ScheduleDeletedMessage;
import me.pinitnotification.infrastructure.events.payload.ScheduleStateChangedMessage;
import me.pinitnotification.infrastructure.events.payload.ScheduleTimeUpcomingUpdatedMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ScheduleEventListener {
    private static final String EXCHANGE_NAME = "task.schedule.direct";
    private final ScheduleNotificationService scheduleNotificationService;

    public ScheduleEventListener(ScheduleNotificationService scheduleNotificationService) {
        this.scheduleNotificationService = scheduleNotificationService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "schedule.time.upcoming.updated.notification", durable = "true"),
            exchange = @Exchange(value = EXCHANGE_NAME, type = ExchangeTypes.DIRECT, durable = "true"),
            key = "schedule.time.upcoming.updated"
    ))
    public void onScheduleTimeUpcomingUpdated(ScheduleTimeUpcomingUpdatedMessage message) {
        scheduleNotificationService.handleUpcomingUpdated(
                new UpcomingUpdatedCommand(
                        message.ownerId(),
                        message.scheduleId(),
                        message.newUpcomingTime(),
                        message.idempotentKey()
                )
        );
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "schedule.deleted.notification", durable = "true"),
            exchange = @Exchange(value = EXCHANGE_NAME, type = ExchangeTypes.DIRECT, durable = "true"),
            key = "schedule.deleted"
    ))
    public void onScheduleDeleted(ScheduleDeletedMessage message) {
        scheduleNotificationService.handleScheduleDeleted(
                new ScheduleDeletedCommand(
                        message.ownerId(),
                        message.scheduleId(),
                        message.idempotentKey()
                )
        );
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "schedule.state.started.notification", durable = "true"),
            exchange = @Exchange(value = EXCHANGE_NAME, type = ExchangeTypes.DIRECT, durable = "true"),
            key = "schedule.state.started"
    ))
    public void onScheduleStarted(ScheduleStateChangedMessage message) {
        scheduleNotificationService.handleScheduleStarted(
                new ScheduleStateChangedCommand(
                        message.ownerId(),
                        message.scheduleId(),
                        message.beforeState(),
                        message.idempotentKey()
                )
        );
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "schedule.state.canceled.notification", durable = "true"),
            exchange = @Exchange(value = EXCHANGE_NAME, type = ExchangeTypes.DIRECT, durable = "true"),
            key = "schedule.state.canceled"
    ))
    public void onScheduleCanceled(ScheduleStateChangedMessage message) {
        scheduleNotificationService.handleScheduleCanceled(
                new ScheduleStateChangedCommand(
                        message.ownerId(),
                        message.scheduleId(),
                        message.beforeState(),
                        message.idempotentKey()
                )
        );
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "schedule.state.completed.notification", durable = "true"),
            exchange = @Exchange(value = EXCHANGE_NAME, type = ExchangeTypes.DIRECT, durable = "true"),
            key = "schedule.state.completed"
    ))
    public void onScheduleCompleted(ScheduleStateChangedMessage message) {
        scheduleNotificationService.handleScheduleCompleted(
                new ScheduleStateChangedCommand(
                        message.ownerId(),
                        message.scheduleId(),
                        message.beforeState(),
                        message.idempotentKey()
                )
        );
    }
}
