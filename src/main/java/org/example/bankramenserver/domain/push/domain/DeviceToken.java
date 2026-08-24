package org.example.bankramenserver.domain.push.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.bankramenserver.global.common.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Table(name = "device_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID memberId;

    @Column(nullable = false, unique = true)
    private String token;

    @Builder
    public DeviceToken(UUID memberId, String token) {
        this.memberId = memberId;
        this.token = token;
    }

    public void updateMember(UUID memberId) {
        this.memberId = memberId;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}