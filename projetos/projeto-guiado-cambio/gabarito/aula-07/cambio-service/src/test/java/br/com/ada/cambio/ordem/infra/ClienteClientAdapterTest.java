package br.com.ada.cambio.ordem.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.ordem.FeignErros;
import br.com.ada.cambio.ordem.dominio.ClienteEncontrado;
import br.com.ada.cambio.ordem.dominio.ClienteNaoEncontradoException;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * O Adapter e a fronteira. Este e o UNICO teste do cambio-service que ainda
 * precisa saber o que e uma {@code FeignException} — exatamente como so o
 * Adapter, no codigo de producao, ainda importa {@code feign}.
 */
@ExtendWith(MockitoExtension.class)
class ClienteClientAdapterTest {

    @Mock
    private ClienteFeignClient feignClient;

    @InjectMocks
    private ClienteClientAdapter adapter;

    @Test
    @DisplayName("traduz o JSON do vizinho para o modelo do dominio")
    void traduzOJson() {
        when(feignClient.buscarPorCpf("43488428095"))
                .thenReturn(new ClienteResumoJson(1L, "Ana Souza", "43488428095"));

        ClienteEncontrado cliente = adapter.porCpf("43488428095");

        assertThat(cliente.id()).isEqualTo(1L);
        assertThat(cliente.cpf()).isEqualTo("43488428095");
        assertThat(cliente.nome()).isEqualTo("Ana Souza");
    }

    @Test
    @DisplayName("404 do vizinho vira ClienteNaoEncontradoException")
    void traduzNotFound() {
        when(feignClient.buscarPorCpf("00000000000"))
                .thenThrow(FeignErros.naoEncontrado("/clientes/00000000000"));

        assertThatThrownBy(() -> adapter.porCpf("00000000000"))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessageContaining("00000000000");
    }

    @Test
    @DisplayName("vizinho sem atender vira ServicoIndisponivelException")
    void traduzForaDoAr() {
        when(feignClient.buscarPorCpf("43488428095"))
                .thenThrow(FeignErros.foraDoAr("/clientes/43488428095"));

        assertThatThrownBy(() -> adapter.porCpf("43488428095"))
                .isInstanceOf(ServicoIndisponivelException.class)
                .hasMessageContaining("cliente-service");
    }

    @Test
    @DisplayName("nenhuma instancia no Eureka tambem vira ServicoIndisponivelException")
    void traduzSemInstancia() {
        when(feignClient.buscarPorCpf("43488428095"))
                .thenThrow(FeignErros.semInstanciaRegistrada("cliente-service"));

        assertThatThrownBy(() -> adapter.porCpf("43488428095"))
                .isInstanceOf(ServicoIndisponivelException.class);
    }
}
