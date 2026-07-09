package duoc.sumativa.transportes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "TST_PEDIDOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PEDIDO")
    private Long id;

    @Column(name = "CODIGO_PEDIDO", nullable = false, unique = true)
    private String codigoPedido;

    @Column(name = "CLIENTE", nullable = false)
    private String cliente;

    @Column(name = "DIRECCION_DESTINO", nullable = false)
    private String direccionDestino;

    @Column(name = "FECHA_PEDIDO", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "TOTAL_ARTICULOS", nullable = false)
    private Integer totalArticulos;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_GUIA_FK", referencedColumnName = "ID_GUIA")
    private GuiaDespacho guiaDespacho;
}