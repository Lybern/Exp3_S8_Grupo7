package duoc.sumativa.transportes;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import duoc.sumativa.transportes.config.RabbitMQConfig;

@SpringBootTest
public class RabbitMQIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage() {
        // Se envia el mensaje usando el Exchange y el Routing Key que configuramos previamente
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_PRINCIPAL, "RutaS3_Validacion_Desde_Test");
        System.out.println("Mensaje enviado exitosamente a la cola mediante JUnit Test.");
    }
}
