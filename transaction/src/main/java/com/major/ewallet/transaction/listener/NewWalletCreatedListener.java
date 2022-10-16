package com.major.ewallet.transaction.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.major.ewallet.transaction.entity.Transaction;
import com.major.ewallet.transaction.model.NewWalletRequestModel;
import com.major.ewallet.transaction.service.TransactionService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
public class NewWalletCreatedListener {

    private static final String NEW_WALLET_CREATED = "NEW_WALLET_CREATED";

    private static final String TOPUP_WALLET = "TOPUP_WALLET";

    @Autowired
    KafkaTemplate<String,String > kafkaTemplate;

    @Autowired
    TransactionService transactionService;

    @Autowired
    ObjectMapper objectMapper;


    @SneakyThrows
    @KafkaListener(topics = {NEW_WALLET_CREATED}, groupId = "transaction_group")
    public void receiveMessage(@Payload String message){
        /*
         * Read from New Wallet Created Kafka Queue > Create a new pending Transaction > Persist in Db > Send message to Top Up Wallet  Kafka Queue*/
        log.info("*** INSIDE NEW WALLET CREATED LISTENER RECEIVE MESSAGE IN TRANSACTION SERVICE");
        objectMapper.registerModule(new JavaTimeModule());
        NewWalletRequestModel walletUser = objectMapper.readValue(message, NewWalletRequestModel.class);
        Transaction pendingTransaction = transactionService.createANewPendingTransaction(walletUser);
        String sendMessage = objectMapper.writeValueAsString(pendingTransaction);
        log.info("*** SENDING MESSAGE TO TOPUP_WALLET ***");
        addCallBack(sendMessage,kafkaTemplate.send(TOPUP_WALLET,sendMessage));
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
