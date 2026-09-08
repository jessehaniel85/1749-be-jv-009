package br.com.ada.cambio.cliente.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Pessoa habilitada a comprar moeda estrangeira.
 *
 * <p>O CPF e a identidade de negocio (unico); o {@code id} e apenas a chave tecnica.</p>
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = RegrasDeCliente.TAMANHO_MAXIMO_NOME)
    private String nome;

    @Column(nullable = false, unique = true, length = RegrasDeCliente.TAMANHO_CPF)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 20)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sexo sexo;

    /** Construtor exigido pelo JPA. */
    protected Cliente() {
    }

    public Cliente(String nome, String cpf, LocalDate dataNascimento, EstadoCivil estadoCivil, Sexo sexo) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.estadoCivil = estadoCivil;
        this.sexo = sexo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public EstadoCivil getEstadoCivil() {
        return estadoCivil;
    }

    public Sexo getSexo() {
        return sexo;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Cliente cliente)) {
            return false;
        }
        return id != null && id.equals(cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
