package br.com.ada.cambio.ordem.infra;

import br.com.ada.cambio.ordem.dominio.ConsultaCotacao;
import br.com.ada.cambio.ordem.dominio.CotacaoVigente;
import br.com.ada.cambio.ordem.dominio.Moeda;
import br.com.ada.cambio.ordem.dominio.MoedaNaoSuportadaException;
import br.com.ada.cambio.ordem.dominio.ServicoIndisponivelException;
import feign.FeignException;
import org.springframework.stereotype.Component;

/** ADAPTER + DIP: liga {@link ConsultaCotacao} ao {@link CotacaoFeignClient}. */
@Component
public class CotacaoClientAdapter implements ConsultaCotacao {

    private static final String NOME_DO_SERVICO = "cotacao-service";

    private final CotacaoFeignClient feignClient;

    public CotacaoClientAdapter(CotacaoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public CotacaoVigente vigente(Moeda moeda) {
        try {
            return converter(moeda, feignClient.consultar(moeda));
        } catch (FeignException.UnprocessableEntity excecao) {
            throw new MoedaNaoSuportadaException(moeda.name());
        } catch (FeignException | IllegalStateException excecao) {
            throw new ServicoIndisponivelException(NOME_DO_SERVICO, excecao);
        }
    }

    private CotacaoVigente converter(Moeda moeda, CotacaoResumoJson json) {
        return new CotacaoVigente(moeda, json.valorCotacao(), json.dataHora());
    }
}
