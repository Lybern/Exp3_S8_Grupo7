package duoc.sumativa.transportes.service;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import duoc.sumativa.transportes.model.GuiaDespacho;
import duoc.sumativa.transportes.model.Pedido;
import duoc.sumativa.transportes.repository.GuiaRepository;
import duoc.sumativa.transportes.repository.PedidoRepository;
import duoc.sumativa.transportes.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GuiaServiceImpl implements GuiaService {

    private final S3Template s3Template;
    private final GuiaRepository guiaRepository;
    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${aws.s3.bucket}")
    private String nombreBucket;

    @Value("${ruta.almacenamiento.temporal}")
    private String rutaEfs;

    public GuiaServiceImpl(S3Template s3Template, GuiaRepository guiaRepository, PedidoRepository pedidoRepository, RabbitTemplate rabbitTemplate) {
        this.s3Template = s3Template;
        this.guiaRepository = guiaRepository;
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public String registrarYSubirGuia(String codigoPedido, String transportista, MultipartFile archivo) throws IOException {
        Pedido pedido = pedidoRepository.findByCodigoPedido(codigoPedido)
                .orElseThrow(() -> new IllegalArgumentException("El codigo de pedido " + codigoPedido + " no existe."));

        String nombreArchivo = archivo.getOriginalFilename();
        String rutaDestinoS3 = generarRutaS3(transportista, nombreArchivo);

        // 1. Requerimiento EFS - Almacenamiento local temporal
        Path directorioEfs = Paths.get(rutaEfs);
        if (!Files.exists(directorioEfs)) {
            Files.createDirectories(directorioEfs);
        }
        Path archivoTemporalEfs = directorioEfs.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), archivoTemporalEfs, StandardCopyOption.REPLACE_EXISTING);

        // 2. Requerimiento S3 - Subida automatica a la nube
        s3Template.upload(nombreBucket, rutaDestinoS3, archivo.getInputStream());

        // 3. Persistencia Oracle - Crear la Guia de Despacho
        GuiaDespacho nuevaGuia = new GuiaDespacho();
        nuevaGuia.setNumeroGuia("GD-" + System.currentTimeMillis());
        nuevaGuia.setTransportista(transportista);
        nuevaGuia.setFechaEmision(LocalDate.now());
        nuevaGuia.setEstado("DESPACHADO");
        nuevaGuia.setRutaS3(rutaDestinoS3);
        
        GuiaDespacho guiaGuardada = guiaRepository.save(nuevaGuia);

        // 4. Vincular Guia con el Pedido
        pedido.setGuiaDespacho(guiaGuardada);
        pedidoRepository.save(pedido);

        // 5. Enviar ruta a RabbitMQ (Cola 1)
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_PRINCIPAL, rutaDestinoS3);

        return rutaDestinoS3;
    }

    @Override
    public InputStream descargarGuia(String rutaS3) throws IOException {
        return s3Template.download(nombreBucket, rutaS3).getInputStream();
    }

    @Override
    public void eliminarGuia(String rutaS3) {
        s3Template.deleteObject(nombreBucket, rutaS3);
    }

    private String generarRutaS3(String transportista, String nombreArchivoGuia) {
        String periodo = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyM")); 
        String transportistaLimpio = transportista.replaceAll("\\s+", ""); 
        return String.format("%s/%s/%s", periodo, transportistaLimpio, nombreArchivoGuia);
    }

    @Override
    public List<String> listarArchivos() throws IOException {
        try {
        // S3Template nos da una lista de objetos S3 Resource. 
        // Usamos streams para extraer solo la "rutaS3" (Key) de cada uno.
        return s3Template.listObjects(nombreBucket, "")
                .stream()
                .map(s3Resource -> s3Resource.getFilename()) // Extrae la ruta/nombre del archivo
                .toList(); // Lo convierte en la List<String> que necesitas
        } catch (Exception e) {
            throw new IOException("Error al conectar con AWS S3 para listar los archivos: " + e.getMessage(), e);
        }
    }
}