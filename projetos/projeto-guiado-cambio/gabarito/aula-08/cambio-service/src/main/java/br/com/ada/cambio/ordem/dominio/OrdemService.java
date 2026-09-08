package br.com.ada.cambio.ordem.dominio;

import br.com.ada.cambio.ordem.infra.OrdemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistencia de ordens.
 *
 * <p>Na Aula 8 esta classe ENCOLHEU: a orquestracao foi para {@link CambioFacade}
 * e aqui sobrou o que sempre foi a sua unica razao de existir — guardar e
 * recuperar ordens. SRP levado a serio costuma terminar assim: classes menores
 * do que a gente esperava.</p>
 */
@Service
public class OrdemService {

    private final OrdemRepository repositorio;

    public OrdemService(OrdemRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public OrdemDeCompra salvar(OrdemDeCompra ordem) {
        return repositorio.save(ordem);
    }

    @Transactional(readOnly = true)
    public OrdemDeCompra buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new OrdemNaoEncontradaException(id));
    }
}
