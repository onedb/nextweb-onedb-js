package com.ononedb.nextweb.js.internal;

import delight.async.Operation;
import delight.async.callbacks.ValueCallback;
import delight.promise.Promise;
import delight.promise.PromisesCommon;

import com.appjangle.api.Client;
import com.ononedb.nextweb.common.DataPromiseImplWithClient;
import com.ononedb.nextweb.common.OnedbFactory;

import io.nextweb.promise.DataOperation;
import io.nextweb.promise.DataPromise;
import io.nextweb.promise.exceptions.DataExceptionManager;
import io.nextweb.promise.utils.CallbackUtils;

public class OnedbJsFactory extends OnedbFactory {

    @Override
    public <ResultType> DataPromise<ResultType> createPromise(final DataExceptionManager fallbackExceptionManager,
            final Client client, final DataOperation<ResultType> asyncResult) {

        final DataExceptionManager exceptionManager = createExceptionManager(fallbackExceptionManager);
        final Promise<ResultType> promise = createPromiseNew(exceptionManager, client, asyncResult);

        return new DataPromiseImplWithClient<ResultType>(asyncResult, promise, exceptionManager, client);

    }

    private <ResultType> Promise<ResultType> createPromiseNew(final DataExceptionManager exceptionManager,
            final Client session, final DataOperation<ResultType> operation) {

        final Promise<ResultType> promise = PromisesCommon.createUnsafe(new Operation<ResultType>() {

            @Override
            public String toString() {
                return "[(" + operation + ") wrapped by (" + super.toString() + ")]";
            }

            @Override
            public void apply(final ValueCallback<ResultType> callback) {
                operation.apply(CallbackUtils.asDataCallback(exceptionManager, callback));

            }
        });

        return promise;
    }
}
