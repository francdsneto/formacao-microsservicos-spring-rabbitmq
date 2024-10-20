package estudo.alura.alurafood.pagamentos.controller;

import estudo.alura.alurafood.pagamentos.dto.PagamentoDTO;
import estudo.alura.alurafood.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping
    public ResponseEntity<Page<PagamentoDTO>> listar(@PageableDefault(size = 3) Pageable pageable) {
        return ResponseEntity.ok(service.obterTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDTO> detalhar(@PathVariable @NotNull Long id) {
        return ResponseEntity.ok(service.obterPorId(id));
    }

    @Transactional
    @PostMapping
    public ResponseEntity<PagamentoDTO> cadastrar(@RequestBody @Valid PagamentoDTO dados, UriComponentsBuilder uriBuilder) {
        var pagamentoDTO = service.criarPagamento(dados);
        URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamentoDTO.getId()).toUri();
//        Message message = new Message(("Criei um pagamento com o id " + pagamentoDTO.getId()).getBytes());
//        rabbitTemplate.convertAndSend("pagamento.concluido", pagamentoDTO);
        rabbitTemplate.convertAndSend("pagamentos.ex","", pagamentoDTO);
        return ResponseEntity.created(endereco).body(pagamentoDTO);
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDTO> atualizar(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDTO dados) {
        return ResponseEntity.ok(service.atualizrPagamento(id,dados));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable @NotNull Long id) {
        service.excluirPagamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = "atualizaPedido", fallbackMethod = "pagamentoAutorizadoComIntegracaoPendente")
    public void confirmarPagamento(@PathVariable @NotNull Long id) {
        service.confirmarPagamento(id);
    }

    public void pagamentoAutorizadoComIntegracaoPendente(Long id, Throwable throwable) {
        service.alteraStatusComoConfirmadoSemIntegracao(id);
    }

}
