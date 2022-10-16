package com.major.ewallet.transaction.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.major.ewallet.transaction.entity.Transaction;
import lombok.Data;



import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(value = {"createdAt","updatedAt"})
public class NewWalletRequestModel {


    private Long id;
    private Long userId;

    private Double balance;


    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public Transaction toTransaction(){
        return Transaction.builder()
                .receiverId(userId)
                .build();
    }
}
