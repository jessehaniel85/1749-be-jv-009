package br.com.ada.cambio.ordem.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.ordem.infra.OrdemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * O que sobrou do OrdemService depois da Facade: persistencia.
 * O teste encolheu junto — e isso e uma boa noticia.
 */
@ExtendWith(MockitoExtension.class)
class OrdemServiceTest {

    @Mock
    private OrdemRepository repositorio;

    @InjectMocks
    private OrdemService ordemService;

    private OrdemDeCompra exemplo() {
        return new OrdemDeCompra(1L, "43488428095", LocalDateTime.now(), Moeda.USD,
                new BigDecimal("10.00"), new BigDecimal("5.4321"), new BigDecimal("54.32"), "7057");
    }

    @Test
    @DisplayName("salvar delega ao repositorio")
    void salva() {
        OrdemDeCompra ordem = exemplo();
        when(repositorio.save(ordem)).thenReturn(ordem);

        assertThat(ordemService.salvar(ordem)).isSameAs(ordem);
    }

    @Test
    @DisplayName("busca por id devolve a ordem gravada")
    void buscaPorId() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(exemplo()));

        assertThat(ordemService.buscarPorId(1L).getCpfCliente()).isEqualTo("43488428095");
    }

    @Test
    @DisplayName("id inexistente estoura OrdemNaoEncontradaException")
    void ordemInexistente() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemService.buscarPorId(99L))
                .isInstanceOf(OrdemNaoEncontradaException.class);
    }
}
