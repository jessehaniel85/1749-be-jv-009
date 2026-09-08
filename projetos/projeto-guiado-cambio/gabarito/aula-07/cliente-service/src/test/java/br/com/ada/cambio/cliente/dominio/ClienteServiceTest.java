package br.com.ada.cambio.cliente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.cliente.infra.ClienteRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Base da piramide: teste unitario, repositorio mockado, sem Spring. */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repositorio;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente novoCliente() {
        return new Cliente("Ana Souza", "43488428095", LocalDate.of(1990, 5, 10),
                EstadoCivil.SOLTEIRO, Sexo.FEMININO);
    }

    @Test
    @DisplayName("cadastra o cliente quando o CPF ainda nao existe")
    void cadastraQuandoCpfLivre() {
        Cliente cliente = novoCliente();
        when(repositorio.existsByCpf("43488428095")).thenReturn(false);
        when(repositorio.save(cliente)).thenReturn(cliente);

        Cliente salvo = clienteService.cadastrar(cliente);

        assertThat(salvo.getCpf()).isEqualTo("43488428095");
        verify(repositorio).save(cliente);
    }

    @Test
    @DisplayName("recusa cadastro com CPF ja usado")
    void recusaCpfDuplicado() {
        Cliente cliente = novoCliente();
        when(repositorio.existsByCpf("43488428095")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(cliente))
                .isInstanceOf(CpfDuplicadoException.class)
                .hasMessageContaining("43488428095");

        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("encontra o cliente pelo CPF")
    void buscaPorCpf() {
        when(repositorio.findByCpf("43488428095")).thenReturn(Optional.of(novoCliente()));

        Cliente encontrado = clienteService.buscarPorCpf("43488428095");

        assertThat(encontrado.getNome()).isEqualTo("Ana Souza");
    }

    @Test
    @DisplayName("estoura ClienteNaoEncontradoException para CPF inexistente")
    void naoEncontraPorCpf() {
        when(repositorio.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorCpf("00000000000"))
                .isInstanceOf(ClienteNaoEncontradoException.class);
    }
}
