package estudo.alura.alurafood.pagamentos.repository;

import estudo.alura.alurafood.pagamentos.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
