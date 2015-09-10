package com.ononedb.nextweb.js.internal;

import delight.async.Operation;
import delight.async.callbacks.ValueCallback;
import delight.functional.Closure;
import delight.promise.Promise;
import delight.promise.PromisesCommon;

import com.appjangle.api.Client;
import com.ononedb.nextweb.common.DataPromiseImpl;
import com.ononedb.nextweb.common.OnedbFactory;

import io.nextweb.promise.Fn;
import io.nextweb.promise.DataOperation;
import io.nextweb.promise.DataPromise;
import io.nextweb.promise.exceptions.NextwebExceptionManager;
import io.nextweb.promise.utils.CallbackUtils;

public class OnedbJsFactory extends OnedbFactory {

    @Override
    public <ResultType> DataPromise<ResultType> createPromise(final NextwebExceptionManager exceptionManager,
            final Client session, final DataOperation<ResultType> asyncResult) {

        final Promise<ResultType> promise = createPromiseNew(exceptionManager, session, asyncResult);

        return new DataPromiseImpl<ResultType>(asyncResult, promise, exceptionManager, session);

    }

    private <ResultType> Promise<ResultType> createPromiseNew(final NextwebExceptionManager exceptionManager,
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

        promise.addExceptionFallback(new Closure<Throwable>() {

            @Override
            public void apply(final Throwable o) {
                exceptionManager.onFailure(Fn.exception(this, o));
            }
        });

        return promise;
    }
}
