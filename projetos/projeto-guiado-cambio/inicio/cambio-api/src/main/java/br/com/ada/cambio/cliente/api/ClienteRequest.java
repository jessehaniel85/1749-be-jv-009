package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import java.time.LocalDate;

/**
 * Corpo do {@code POST /clientes}.
 *
 * <p>A validação de entrada mora aqui, e não na entidade: a borda da API é o lugar de
 * recusar dado ruim antes que ele chegue ao domínio.</p>
 */
public record ClienteRequest(

        // TODO (Aula 1): @NotBlank(message = "nome é obrigatório")
        String nome,

        // TODO (Aula 1): @NotBlank + @Pattern(regexp = "\\d{11}") — CPF sem máscara, 11 dígitos
        String cpf,

        // TODO (Aula 1): @NotNull + @Past — não se cadastra quem nasce amanhã
        LocalDate dataNascimento,

        // TODO (Aula 1): @NotNull
        EstadoCivil estadoCivil,

        // TODO (Aula 1): @NotNull
        Sexo sexo) {

    /** Converte o DTO de entrada na entidade de domínio. */
    public Cliente paraEntidade() {
        // TODO (Aula 1): repassar todos os campos assim que o construtor de Cliente crescer
        return new Cliente(nome);
    }
}
