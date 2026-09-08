package br.com.ada.cambio.ordem.api;

import java.time.LocalDateTime;

/** Corpo padrao de erro devolvido por todos os endpoints do servico. */
public record ErroResposta(int status, String erro, String mensagem, LocalDateTime momento) {

    public static ErroResposta de(int status, String erro, String mensagem) {
        return new ErroResposta(status, erro, mensagem, LocalDateTime.now());
    }
}
