package br.com.m5_storage.entity.alerta;

import br.com.m5_storage.entity.recurso.Recurso;
import br.com.m5_storage.entity.setor.Setor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "st_t_alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(name = "mensagem", length = 255)
    private String mensagem;

    @Column(name = "nivel", length = 30)
    private String nivel;

    @Column(name = "resolvido", nullable = false)
    private Boolean resolvido = false;

    @Column(name = "data_alerta")
    private LocalDateTime dataAlerta;
}