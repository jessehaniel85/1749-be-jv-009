package br.com.ada.cambio.cliente.api;

import br.com.ada.cambio.cliente.dominio.Cliente;

/** Representação do cliente devolvida pela API. */
public record ClienteResponse(Long id, String nome) {

    // TODO (Aula 1): incluir cpf, dataNascimento, estadoCivil e sexo no contrato de saída

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome());
    }
}
