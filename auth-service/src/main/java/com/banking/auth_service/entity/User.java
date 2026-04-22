package com.banking.auth_service.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")  // email duplicate nahi hogi
    }
)
@Data                  // Lombok: getters, setters, toString auto-generate
@Builder               // Lombok: builder pattern — User.builder().name("x").build()
@NoArgsConstructor     // Lombok: empty constructor
@AllArgsConstructor    // Lombok: all fields constructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment ID
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Column(nullable = false)
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt encrypted password store hoga — plain text kabhi nahi
    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;

    // ROLE_USER ya ROLE_ADMIN
    @Column(nullable = false)
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}