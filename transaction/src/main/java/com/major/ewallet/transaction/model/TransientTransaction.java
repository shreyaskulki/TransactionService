package com.major.ewallet.transaction.model;

import lombok.Data;

@Data
public class TransientTransaction {

    Long id;
    Double amount;
    Long senderId;
    Long receiverId;
}
