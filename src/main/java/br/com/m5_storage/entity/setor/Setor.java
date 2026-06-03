package br.com.m5_storage.entity.setor;

import br.com.m5_storage.entity.base.Base;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "st_t_setores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @Embedded
    private SetorInfo info;
}