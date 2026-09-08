package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Corpo do {@code POST /clientes}.
 *
 * <p>É aqui que a validação de entrada acontece — a entidade não deve ser exposta na
 * borda da API. Violações viram HTTP 400 via {@code TratadorDeErros}.</p>
 */
public record ClienteRequest(

        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotBlank(message = "cpf é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "cpf deve ter exatamente 11 dígitos numéricos")
        String cpf,

        @NotNull(message = "dataNascimento é obrigatória")
        @Past(message = "dataNascimento deve ser uma data no passado")
        LocalDate dataNascimento,

        @NotNull(message = "estadoCivil é obrigatório")
        EstadoCivil estadoCivil,

        @NotNull(message = "sexo é obrigatório")
        Sexo sexo) {

    /** Converte o DTO de entrada na entidade de domínio. */
    public Cliente paraEntidade() {
        return new Cliente(nome, cpf, dataNascimento, estadoCivil, sexo);
    }
}
