package duoc.sumativa.transportes.repository;

import duoc.sumativa.transportes.model.GuiaMensajeDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuiaMensajeRepository extends JpaRepository<GuiaMensajeDB, Long> {
}
