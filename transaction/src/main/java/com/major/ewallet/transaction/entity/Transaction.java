package com.major.ewallet.transaction.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Enumerated(value = EnumType.STRING)
    private TransactionStatus status;

    private Double amount;

    private Long senderId;

    private Long receiverId;

    @PrePersist
    public void defaultTransactionUpdate(){
        this.status = TransactionStatus.PENDING;
    }
}
