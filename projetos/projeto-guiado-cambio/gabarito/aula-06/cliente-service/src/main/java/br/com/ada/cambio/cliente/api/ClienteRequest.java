package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Corpo de entrada de {@code POST /clientes}. */
public record ClienteRequest(

        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter no maximo 120 caracteres")
        String nome,

        @NotBlank(message = "cpf e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "cpf deve conter exatamente 11 digitos")
        String cpf,

        @NotNull(message = "dataNascimento e obrigatoria")
        @Past(message = "dataNascimento deve estar no passado")
        LocalDate dataNascimento,

        @NotNull(message = "estadoCivil e obrigatorio")
        EstadoCivil estadoCivil,

        @NotNull(message = "sexo e obrigatorio")
        Sexo sexo) {

    public Cliente paraEntidade() {
        return new Cliente(nome, cpf, dataNascimento, estadoCivil, sexo);
    }
}
