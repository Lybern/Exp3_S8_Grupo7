package duoc.sumativa.transportes.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQHealthIndicator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        try {
            // Intenta enviar una operacion basica a RabbitMQ para ver si responde
            String version = rabbitTemplate.execute(channel -> channel.getConnection().getServerProperties().get("version").toString());
            
            return Health.up()
                    .withDetail("RabbitMQ Status", "Disponible")
                    .withDetail("RabbitMQ Version", version)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("RabbitMQ Status", "No Disponible")
                    .withDetail("Error", e.getMessage())
                    .build();
        }
    }
}
