package duoc.sumativa.transportes.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COLA_PRINCIPAL = "guia_cola_principal";
    public static final String COLA_DLQ = "guia_cola_dlq";
    public static final String EXCHANGE = "guia_exchange";
    public static final String ROUTING_KEY_PRINCIPAL = "guia_routing_key";
    public static final String ROUTING_KEY_DLQ = "guia_dlq_routing_key";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue colaPrincipal() {
        return QueueBuilder.durable(COLA_PRINCIPAL)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ)
                .build();
    }

    @Bean
    public Queue colaDLQ() {
        return QueueBuilder.durable(COLA_DLQ).build();
    }

    @Bean
    public Binding bindingPrincipal(Queue colaPrincipal, DirectExchange exchange) {
        return BindingBuilder.bind(colaPrincipal).to(exchange).with(ROUTING_KEY_PRINCIPAL);
    }

    @Bean
    public Binding bindingDLQ(Queue colaDLQ, DirectExchange exchange) {
        return BindingBuilder.bind(colaDLQ).to(exchange).with(ROUTING_KEY_DLQ);
    }

    // =======================================================
    // CONFIGURACIÓN DE REINTENTOS AUTOMÁTICOS PARA EL CONSUMIDOS DE MENSAJES
    // =======================================================
    
    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory = new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // Aplica la política de reintentos a los consumidores
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    @Bean
    public org.springframework.retry.interceptor.RetryOperationsInterceptor retryInterceptor() {
        org.springframework.retry.RetryPolicy retryPolicy = new org.springframework.retry.policy.SimpleRetryPolicy(3); // Reintentos
        return org.springframework.amqp.rabbit.config.RetryInterceptorBuilder.stateless()
                .retryPolicy(retryPolicy)
                .recoverer(new org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer())
                .build();
    }
}
