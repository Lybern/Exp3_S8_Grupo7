package duoc.sumativa.transportes.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
//
public interface GuiaService {
    String registrarYSubirGuia(String codigoPedido, String transportista, MultipartFile archivo) throws IOException;
    InputStream descargarGuia(String rutaS3) throws IOException;
    void eliminarGuia(String rutaS3);
    List<String> listarArchivos() throws IOException;

}