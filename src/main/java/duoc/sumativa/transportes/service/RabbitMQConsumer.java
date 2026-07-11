package duoc.sumativa.transportes.service;
import java.time.LocalDateTime;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import duoc.sumativa.transportes.config.RabbitMQConfig;
import duoc.sumativa.transportes.model.GuiaMensajeDB;
import duoc.sumativa.transportes.repository.GuiaMensajeRepository;

@Service
public class RabbitMQConsumer {

    @Autowired
    private GuiaMensajeRepository guiaMensajeRepository;

    // Balanceo de carga: Se crean entre 3 a 5 instancias (hilos) del consumidor para procesar mensajes en paralelo
    @RabbitListener(queues = RabbitMQConfig.COLA_PRINCIPAL, concurrency = "3-5")
    public void recibirMensaje(Message mensaje) throws Exception {
        String rutaS3 = new String(mensaje.getBody(), "UTF-8");
        System.out.println("Mensaje recibido de Cola 1: " + rutaS3);
        
        // Simular la lógica de validación de la ruta S3. Si la ruta es inválida, se lanza una excepción para activar los reintentos.
        if (rutaS3 == null || rutaS3.isEmpty() || rutaS3.contains("error")) {
            // AL LANZAR LA EXCEPCIÓN, SE ACTIVA LA POLÍTICA DE REINTENTOS (3 intentos)
            // Si falla 3 veces, el RejectAndDontRequeueRecoverer lo mandará a la DLQ
            throw new RuntimeException("Error procesando ruta invalida: " + rutaS3);
        }
        
        // Guardar mensaje en la base de datos ORACLE
        GuiaMensajeDB msj = new GuiaMensajeDB();
        msj.setRutaS3(rutaS3);
        msj.setFechaProcesamiento(LocalDateTime.now());
        msj.setEstado("PROCESADO_OK");
        
        guiaMensajeRepository.save(msj);
        System.out.println("Mensaje guardado en base de datos correctamente.");
    }
}
