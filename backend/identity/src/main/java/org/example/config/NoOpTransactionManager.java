package org.example.config;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class NoOpTransactionManager extends AbstractPlatformTransactionManager {

    @Override
    protected Object doGetTransaction() {
        return new Object();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) { }

    @Override
    protected Object doSuspend(Object transaction) {
        return null;
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) { }

    @Override
    protected void doCommit(DefaultTransactionStatus status) { }

    @Override
    protected void doRollback(DefaultTransactionStatus status) { }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) { }
}
