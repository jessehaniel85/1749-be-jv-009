package br.com.ada.cambio.cotacao.infra;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ada.cambio.cotacao.dominio.CotacaoProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Mesma aplicação, uma linha de configuração diferente, outra implementação injetada —
 * e nenhuma classe de domínio recompilada. Este teste não faz chamada de rede: ele só
 * verifica <b>qual bean o contêiner escolheu</b>.
 */
@SpringBootTest(properties = "cotacao.provedor=externo")
class SelecaoDoProvedorExternoTest {

    @Autowired
    private CotacaoProvider provedor;

    @Test
    @DisplayName("com cotacao.provedor=externo, o provedor ativo é o externo")
    void provedorExternoQuandoConfigurado() {
        assertThat(provedor).isInstanceOf(CotacaoExternaProvider.class);
    }
}
