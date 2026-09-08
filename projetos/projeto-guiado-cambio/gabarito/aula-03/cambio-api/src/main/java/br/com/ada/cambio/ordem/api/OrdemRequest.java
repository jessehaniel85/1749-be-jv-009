package br.com.ada.cambio.ordem.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Corpo do {@code POST /ordens}.
 *
 * <p>Repare no que <b>não</b> está validado aqui: {@code moeda} e {@code numeroAgenciaRetirada}
 * só têm {@code @NotBlank}. O formato deles é regra de negócio, e o contrato do módulo manda
 * responder <b>422</b> (não 400) quando a moeda não é operada ou a agência não tem 4 dígitos —
 * então quem valida é o {@code OrdemService}, que sabe lançar a exceção certa.</p>
 */
public record OrdemRequest(

        @NotBlank(message = "cpf é obrigatório")
        String cpf,

        @NotBlank(message = "moeda é obrigatória")
        String moeda,

        @NotNull(message = "valorMoedaEstrangeira é obrigatório")
        @Positive(message = "valorMoedaEstrangeira deve ser maior que zero")
        BigDecimal valorMoedaEstrangeira,

        @NotBlank(message = "numeroAgenciaRetirada é obrigatório")
        String numeroAgenciaRetirada) {
}
