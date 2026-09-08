package br.com.ada.cambio.cliente.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cliente habilitado a operar câmbio.
 *
 * <p>Esqueleto: só {@code id} e {@code nome} estão mapeados. Os demais campos entram no
 * live coding da Aula 1.</p>
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // TODO (Aula 1): cpf — String, chave de negócio: @Column(nullable = false, unique = true, length = 11)
    // TODO (Aula 1): dataNascimento — LocalDate, @Column(name = "data_nascimento", nullable = false)
    // TODO (Aula 1): estadoCivil — EstadoCivil, @Enumerated(EnumType.STRING), coluna "estado_civil"
    // TODO (Aula 1): sexo — Sexo, @Enumerated(EnumType.STRING)

    /** Construtor exigido pelo JPA. Não use no código de aplicação. */
    protected Cliente() {
    }

    public Cliente(String nome) {
        // TODO (Aula 1): receber também cpf, dataNascimento, estadoCivil e sexo
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    // TODO (Aula 1): getters dos campos novos (a API precisa deles para montar a resposta)
}
