package com.ceqb.SistemaVentaDispositivos2025.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Long id;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "clave")
    private String clave;

    @Column(name = "correo")
    private String correo;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    @Column(name = "expiracion_codigo")
    private LocalDateTime expiracionCodigo; //Sirve para verificar email, debloquear cuenta

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado ;

    @Column(name = "codigo_desbloqueo")
    private String codigoDesbloqueo;

    @ManyToOne
    @JoinColumn(name = "idcargo")
    private Cargo cargo;

    /*VERIFICACION DE EMAIL*/

    @Column(name = "verificado")
    private Boolean verificado = false;

    @Column(name = "token_verificacion")
    private String tokenVerificacion;

    /*RESET PASSWORD*/
    @Column(name = "token_reset_password")
    private String tokenResetPassword;
    @Column(name = "expiracion_reset_pass")
    private LocalDateTime expiracionResetPass;

}
