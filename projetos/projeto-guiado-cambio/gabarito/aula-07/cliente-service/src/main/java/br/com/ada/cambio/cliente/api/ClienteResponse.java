package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;
import br.com.ada.cambio.cliente.dominio.EstadoCivil;
import br.com.ada.cambio.cliente.dominio.Sexo;
import java.time.LocalDate;

/** Corpo de saida das operacoes de cliente. */
public record ClienteResponse(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        EstadoCivil estadoCivil,
        Sexo sexo) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getDataNascimento(),
                cliente.getEstadoCivil(),
                cliente.getSexo());
    }
}
