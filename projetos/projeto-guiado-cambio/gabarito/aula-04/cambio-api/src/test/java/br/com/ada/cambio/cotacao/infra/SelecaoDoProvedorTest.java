package br.com.ada.cambio.cotacao.infra;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ada.cambio.cotacao.dominio.CotacaoProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prova que a chave de configuração realmente troca a implementação do contrato.
 *
 * <p>Sem a propriedade, vale {@code matchIfMissing = true}: o provedor local. É o piso
 * seguro — a aplicação sobe e funciona mesmo sem acesso à internet.</p>
 */
@SpringBootTest
class SelecaoDoProvedorTest {

    @Autowired
    private CotacaoProvider provedor;

    @Test
    @DisplayName("sem configuração, o provedor ativo é o local")
    void provedorPadraoEhOLocal() {
        assertThat(provedor).isInstanceOf(CotacaoLocalProvider.class);
    }
}
