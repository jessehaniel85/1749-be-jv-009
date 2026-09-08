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

/**
 * Cliente habilitado a operar câmbio.
 *
 * <p>O CPF é a chave de negócio (única); o {@code id} é apenas a chave técnica gerada
 * pelo banco. Note que a validação de formato NÃO mora aqui — ela vive no DTO de
 * entrada ({@code ClienteRequest}). A entidade guarda o dado já aceito.</p>
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    /** Quantidade de dígitos de um CPF sem máscara. */
    public static final int TAMANHO_CPF = 11;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = TAMANHO_CPF)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    /** Construtor exigido pelo JPA. Não use no código de aplicação. */
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
}
