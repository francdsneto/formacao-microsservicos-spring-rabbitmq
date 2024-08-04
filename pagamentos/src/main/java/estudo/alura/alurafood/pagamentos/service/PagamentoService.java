package estudo.alura.alurafood.pagamentos.service;

import estudo.alura.alurafood.pagamentos.dto.PagamentoDTO;
import estudo.alura.alurafood.pagamentos.http.PedidoClient;
import estudo.alura.alurafood.pagamentos.model.Pagamento;
import estudo.alura.alurafood.pagamentos.model.Status;
import estudo.alura.alurafood.pagamentos.repository.PagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository repository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PedidoClient pedido;

    public Page<PagamentoDTO> obterTodos(Pageable paginacao) {
        return repository
                .findAll(paginacao)
                .map(p -> modelMapper.map(p, PagamentoDTO.class));
    }

    public PagamentoDTO obterPorId(Long id) {
        Pagamento pagamento = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public PagamentoDTO criarPagamento(PagamentoDTO dados) {
        Pagamento pagamento = modelMapper.map(dados, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        pagamento = repository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public PagamentoDTO atualizrPagamento(Long id, PagamentoDTO dados) {
        Pagamento pagamento = modelMapper.map(dados, Pagamento.class);
        pagamento.setId(id);
        pagamento = repository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public void excluirPagamento(Long id) {
        repository.deleteById(id);
    }

    public void confirmarPagamento(Long id) {

        Optional<Pagamento> pagamento = repository.findById(id);

        if(!pagamento.isPresent())
        {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        pedido.atualizaPagamento(pagamento.get().getPedidoId());

    }

    public void alteraStatusComoConfirmadoSemIntegracao(Long id) {
        System.out.println("Chamando pedidoClient.atualizaPagamento com id: " + id);

        Optional<Pagamento> pagamento = repository.findById(id);

        if(!pagamento.isPresent())
        {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        repository.save(pagamento.get());
    }
}
