package br.com.ada.cambio.cliente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ada.cambio.cliente.infra.ClienteRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * BASE da pirâmide: teste unitário puro.
 *
 * <p>Nenhum contexto Spring, nenhum banco. O repositório é um dublê (mock), então o
 * que está sob teste é só a regra de negócio do serviço. Roda em milissegundos.</p>
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repositorio;

    @InjectMocks
    private ClienteService servico;

    private Cliente clienteValido() {
        return new Cliente("Marina Alcântara", "43488428095",
                LocalDate.of(1991, 4, 17), EstadoCivil.SOLTEIRO, Sexo.FEMININO);
    }

    @Test
    @DisplayName("cadastra o cliente quando o CPF ainda não existe")
    void cadastraQuandoCpfInedito() {
        Cliente novo = clienteValido();
        when(repositorio.existsByCpf("43488428095")).thenReturn(false);
        when(repositorio.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Cliente salvo = servico.cadastrar(novo);

        assertThat(salvo.getCpf()).isEqualTo("43488428095");
        assertThat(salvo.getNome()).isEqualTo("Marina Alcântara");
        verify(repositorio).save(novo);
    }

    @Test
    @DisplayName("repassa ao repositório exatamente os dados recebidos")
    void repassaOsDadosRecebidosAoRepositorio() {
        when(repositorio.existsByCpf(anyString())).thenReturn(false);
        when(repositorio.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));
        ArgumentCaptor<Cliente> capturado = ArgumentCaptor.forClass(Cliente.class);

        servico.cadastrar(clienteValido());

        verify(repositorio).save(capturado.capture());
        assertThat(capturado.getValue().getEstadoCivil()).isEqualTo(EstadoCivil.SOLTEIRO);
        assertThat(capturado.getValue().getSexo()).isEqualTo(Sexo.FEMININO);
        assertThat(capturado.getValue().getDataNascimento()).isEqualTo(LocalDate.of(1991, 4, 17));
    }

    @Test
    @DisplayName("recusa o cadastro quando o CPF já está em uso e nem tenta salvar")
    void recusaCpfDuplicado() {
        Cliente novo = clienteValido();
        when(repositorio.existsByCpf("43488428095")).thenReturn(true);

        assertThatThrownBy(() -> servico.cadastrar(novo))
                .isInstanceOf(CpfDuplicadoException.class)
                .hasMessageContaining("43488428095");

        verify(repositorio, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("busca por CPF devolve o cliente existente")
    void buscaPorCpfExistente() {
        when(repositorio.findByCpf("43488428095")).thenReturn(Optional.of(clienteValido()));

        Cliente encontrado = servico.buscarPorCpf("43488428095");

        assertThat(encontrado.getNome()).isEqualTo("Marina Alcântara");
    }

    @Test
    @DisplayName("busca por CPF inexistente lança ClienteNaoEncontradoException")
    void buscaPorCpfInexistente() {
        when(repositorio.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorCpf("00000000000"))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessageContaining("00000000000");
    }
}
