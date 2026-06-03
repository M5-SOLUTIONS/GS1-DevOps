-- =========================================
-- ST_T_BASES
-- =========================================
CREATE TABLE st_t_bases (
                            id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            nome VARCHAR2(100) NOT NULL
);

-- =========================================
-- ST_T_SETORES
-- =========================================
CREATE TABLE st_t_setores (
                              id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                              base_id NUMBER NOT NULL,

                              nome VARCHAR2(100) NOT NULL,
                              descricao VARCHAR2(255),

                              CONSTRAINT fk_setor_base
                                  FOREIGN KEY (base_id)
                                      REFERENCES st_t_bases(id)
);

-- =========================================
-- ST_T_USUARIOS (SINGLE TABLE INHERITANCE)
-- =========================================
CREATE TABLE st_t_usuarios (
                               id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               base_id NUMBER NOT NULL,

                               nome VARCHAR2(100) NOT NULL,
                               email VARCHAR2(150) NOT NULL UNIQUE,
                               senha VARCHAR2(100) NOT NULL,

    -- HERANÇA
                               tipo_usuario VARCHAR2(30) NOT NULL,

                               CONSTRAINT fk_usuario_base
                                   FOREIGN KEY (base_id)
                                       REFERENCES st_t_bases(id),

                               CONSTRAINT ck_tipo_usuario
                                   CHECK (tipo_usuario IN ('VIEWER', 'OPERATOR'))
);

-- =========================================
-- ST_T_RECURSOS
-- =========================================
CREATE TABLE st_t_recursos (
                               id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               setor_id NUMBER NOT NULL,

                               nome VARCHAR2(100) NOT NULL,
                               categoria VARCHAR2(50) NOT NULL,

                               quantidade NUMBER NOT NULL,
                               minimo NUMBER NOT NULL,
                               capacidade_maxima NUMBER NOT NULL,

                               critico NUMBER(1) DEFAULT 0,
                               status VARCHAR2(30),
                               ultima_atualizacao TIMESTAMP,

                               CONSTRAINT fk_recurso_setor
                                   FOREIGN KEY (setor_id)
                                       REFERENCES st_t_setores(id)
);

-- =========================================
-- ST_T_MOVIMENTACOES
-- =========================================
CREATE TABLE st_t_movimentacoes (
                                    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    usuario_id NUMBER NOT NULL,
                                    recurso_id NUMBER NOT NULL,
                                    setor_id NUMBER NOT NULL,

                                    tipo_movimentacao VARCHAR2(30) NOT NULL,
                                    quantidade NUMBER NOT NULL,
                                    descricao VARCHAR2(255),
                                    data_movimentacao TIMESTAMP NOT NULL,

                                    CONSTRAINT fk_mov_usuario
                                        FOREIGN KEY (usuario_id)
                                            REFERENCES st_t_usuarios(id),

                                    CONSTRAINT fk_mov_recurso
                                        FOREIGN KEY (recurso_id)
                                            REFERENCES st_t_recursos(id),

                                    CONSTRAINT fk_mov_setor
                                        FOREIGN KEY (setor_id)
                                            REFERENCES st_t_setores(id)
);

-- =========================================
-- ST_T_ALERTAS
-- =========================================
CREATE TABLE st_t_alertas (
                              id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                              recurso_id NUMBER NOT NULL,
                              setor_id NUMBER NOT NULL,

                              mensagem VARCHAR2(255),
                              nivel VARCHAR2(30),
                              resolvido NUMBER(1) DEFAULT 0,
                              data_alerta TIMESTAMP,

                              CONSTRAINT fk_alerta_recurso
                                  FOREIGN KEY (recurso_id)
                                      REFERENCES st_t_recursos(id),

                              CONSTRAINT fk_alerta_setor
                                  FOREIGN KEY (setor_id)
                                      REFERENCES st_t_setores(id)
);