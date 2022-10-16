package com.major.ewallet.transaction.service;

import com.major.ewallet.transaction.entity.Transaction;
import com.major.ewallet.transaction.entity.TransactionStatus;
import com.major.ewallet.transaction.model.NewWalletRequestModel;
import com.major.ewallet.transaction.model.TransientTransaction;
import com.major.ewallet.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Value("${wallet.default.amount}")
    public Long defaultAmount;

    @Value("${wallet.user.system.id}")
    public Long systemId;

    public Transaction createANewPendingTransaction(NewWalletRequestModel newWalletRequestModel){
        Transaction transaction = newWalletRequestModel.toTransaction();
        transaction.setAmount((double)defaultAmount);
        transaction.setSenderId(systemId);

        return saveOrUpdate(transaction);
    }

    private Transaction saveOrUpdate(Transaction transaction){
        return transactionRepository.save(transaction);
    }

    public Transaction markTransactionStatus(TransientTransaction transientTransaction, TransactionStatus status) {
        Optional<Transaction> transactionById = transactionRepository.findById(transientTransaction.getId());
        if(transactionById.isEmpty()){
            throw new RuntimeException();
        }

        Transaction transaction = transactionById.get();
        transaction.setStatus(status);
        return saveOrUpdate(transaction);
    }
}
