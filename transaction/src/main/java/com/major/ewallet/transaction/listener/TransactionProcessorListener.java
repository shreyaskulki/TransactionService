package com.major.ewallet.transaction.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.major.ewallet.transaction.entity.Transaction;
import com.major.ewallet.transaction.entity.TransactionStatus;
import com.major.ewallet.transaction.model.TransientTransaction;
import com.major.ewallet.transaction.service.TransactionService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDeleteAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class TransactionProcessorListener {

    private static final String TOPUP_SUCCESS="TOPUP_SUCCESS";

    private static final String TOPUP_FAILURE="TOPUP_FAILURE";

    private static final String TRANSACTION_SUCCESS="TRANSACTION_SUCCESS";

    private static final String TRANSACTION_FAILURE="TRANSACTION_FAILURE";

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TransactionService transactionService;

    @SneakyThrows
    @KafkaListener(topics = {TOPUP_SUCCESS},groupId = "transaction_group")
    public void processSuccessTransaction(@Payload String message){
        objectMapper.registerModule(new JavaTimeModule());
        TransientTransaction transientTransaction = objectMapper.readValue(message,TransientTransaction.class);
        Transaction transaction = transactionService.markTransactionStatus(transientTransaction, TransactionStatus.SUCCESS);

        String sendMessage = objectMapper.writeValueAsString(transaction);
        log.info("*** SENDING MESSAGE TO TRANSACTION_SUCCESS ***");
        addCallBack(sendMessage,kafkaTemplate.send(TRANSACTION_SUCCESS,sendMessage));
    }

    @SneakyThrows
    @KafkaListener(topics = {TOPUP_FAILURE},groupId = "transaction_group")
    public void processFailureTransaction(@Payload String message){
        TransientTransaction transientTransaction = objectMapper.readValue(message,TransientTransaction.class);
        Transaction transaction = transactionService.markTransactionStatus(transientTransaction, TransactionStatus.FAILURE);

        String sendMessage = objectMapper.writeValueAsString(transaction);
        log.info("*** SENDING MESSAGE TO TRANSACTION_FAILURE ***");
        addCallBack(sendMessage,kafkaTemplate.send(TRANSACTION_FAILURE,sendMessage));
    }

    public void addCallBack(String message, ListenableFuture<SendResult<String, String>> send){
        send.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("****** FAILURE TO SEND MESSAGE "+message);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("****** SUCCESS TO SEND MESSAGE "+message+" WITH PARTITION "+result.getRecordMetadata().partition()
                        +" WITH OFFSET "+result.getRecordMetadata().offset());
            }
        });
    }
}
