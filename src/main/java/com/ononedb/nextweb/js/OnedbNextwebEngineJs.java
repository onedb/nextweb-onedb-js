package com.ononedb.nextweb.js;

import delight.concurrency.gwt.ConcurrencyGwt;
import delight.gwt.console.Console;
import delight.rpc.DeprecatedRemoteConnection;
import delight.rpc.RemoteConnection;

import com.appjangle.api.Client;
import com.appjangle.api.common.ClientConfiguration;
import com.appjangle.api.engine.AppjangleClientEngine;
import com.appjangle.api.engine.AppjangleClientEngineInternal;
import com.appjangle.api.engine.AppjangleGlobal;
import com.appjangle.api.engine.Factory;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.ononedb.nextweb.OnedbNextwebEngine;
import com.ononedb.nextweb.common.H;
import com.ononedb.nextweb.common.OnedbFactory;
import com.ononedb.nextweb.js.internal.OnedbJsFactory;
import com.ononedb.nextweb.local.LocalServerManager;
import com.ononedb.nextweb.plugins.DefaultPluginFactory;

import de.mxro.client.BasicClient;
import de.mxro.client.ClientsCommon;
import io.nextweb.js.engine.JsFactory;
import io.nextweb.js.engine.NextwebEngineJs;
import io.nextweb.promise.exceptions.ExceptionListener;
import io.nextweb.promise.exceptions.ExceptionResult;
import io.nextweb.promise.exceptions.NextwebExceptionManager;
import io.nextweb.promise.js.exceptions.ExceptionUtils;
import nx.client.gwt.services.GwtRemoteService;
import nx.client.gwt.services.GwtRemoteServiceAsync;
import nx.remote.RemoteConnectionDecorator;
import nx.remote.connections.StringConnection;
import nx.rpcclientgwt.GwtConnectionConfiguration;
import nx.rpcclientgwt.GwtRpc;
import one.client.gwt.OneGwt;
import one.common.One;
import one.core.domain.BackgroundListener;
import one.core.dsl.CoreDsl;

/**
 * <p>
 * The onedb implementation of a {@link AppjangleClientEngine}.
 * 
 * @author <a href="http://www.mxro.de">Max Rohde</a>
 *
 */
public class OnedbNextwebEngineJs implements OnedbNextwebEngine, NextwebEngineJs {

    private final CoreDsl dsl;

    private final JsFactory jsFactory;

    protected BasicClient client;
    protected NextwebExceptionManager exceptionManager;

    @Override
    public Client createClient() {

        final CoreDsl dsl = this.dsl;

        return getOnedbFactory().createSession(this, dsl.createClient(), null);
    }

    @Override
    public Client createClient(final ClientConfiguration configuration) {
        final CoreDsl dsl = this.dsl;

        return getOnedbFactory().createSession(this, dsl.createClient(configuration), configuration);
    }

    @Override
    public Client createClient(final RemoteConnection connection) {
        if (connection == null) {
            throw new NullPointerException("connection must not be null.");
        }

        final ClientConfiguration configuration = new ClientConfiguration() {

            @Override
            public RemoteConnection remoteConnection() {
                return connection;
            }

        };
        return getOnedbFactory().createSession(this, dsl.createClient(configuration), configuration);
    }

    @Override
    public Client createClient(final StringConnection stringConnection) {

        return createClient(GwtRpc.createLocalConnection(new GwtConnectionConfiguration() {
        }, stringConnection));
    }

    @Override
    public NextwebExceptionManager getExceptionManager() {

        return exceptionManager;
    }

    @Override
    public OnedbFactory getOnedbFactory() {
        return new OnedbJsFactory();
    }

    @Override
    public Factory factory() {
        return new OnedbJsFactory();
    }

    public OnedbNextwebEngineJs() {
        this(null);
    }

    @Override
    public DefaultPluginFactory plugin() {
        return H.onedbDefaultPluginFactory();
    }

    @Override
    public JsFactory jsFactory() {
        return jsFactory;
    }

    @Override
    public void runSafe(final Client forSession, final Runnable task) {
        task.run(); // no multi-threading in JS assured.
    }

    @Override
    public CoreDsl getDsl() {
        return dsl;
    }

    @Override
    public void addConnectionDecorator(final RemoteConnectionDecorator decorator) {
        OneGwt.getSettings().addConnectionDecorator(decorator);
    }

    @Override
    public void removeConnectionDecorator(final RemoteConnectionDecorator decorator) {
        OneGwt.getSettings().removeConnectionDecorator(decorator);
    }

    @Override
    public DeprecatedRemoteConnection createRemoteConnection() {
        return OneGwt.createRemoteConnection();
    }

    @Override
    public LocalServerManager localServers() {
        return localServers;
    }

    private static GwtRemoteServiceAsync gwtService = null;

    private static GwtRemoteServiceAsync assertGwtService() {
        if (gwtService != null) {
            return gwtService;
        }

        gwtService = GWT.create(GwtRemoteService.class);

        ((ServiceDefTarget) gwtService).setServiceEntryPoint("/servlets/v01/gwtrpc");

        return gwtService;
    }

    private final CoreDsl createDsl(final RemoteConnection internalConnection) {
        CoreDsl res;
        assert dsl == null;

        res = OneGwt.createDsl(assertGwtService(), "", internalConnection);

        if (!One.isDslInitialized()) {
            One.setDsl(res);
        }
        res.getDefaults().getSettings().setDefaultBackgroundListener(new BackgroundListener() {

            @Override
            public void onBackgroundException(final Object operation, final Throwable t, final Throwable origin) {
                String originTrace;
                if (origin == null) {
                    originTrace = "Origin is null.";
                } else {
                    originTrace = ExceptionUtils.getStacktrace(origin);
                }

                throw new RuntimeException("Uncaught background exception: " + t.getMessage() + " for operation: ["
                        + operation + "] originating from: [" + origin + "]. " + ExceptionUtils.getStacktrace(t)
                        + " Origin Trace: " + originTrace, t);
            }
        });

        return res;
    }

    /**
     * 
     * @param internalConnection
     *            The connection to be used for all sessions created with this
     *            engine.
     */
    public OnedbNextwebEngineJs(final RemoteConnection internalConnection) {
        super();
        this.exceptionManager = getOnedbFactory().createExceptionManager(null);
        this.exceptionManager.catchExceptions(new ExceptionListener() {

            @Override
            public void onFailure(final ExceptionResult r) {
                if (r == null) {
                    throw new IllegalArgumentException("onFailure called with ExceptionResult null.");
                }
                Console.log("Unhandled background exception: " + r.exception().getMessage() + " from " + r.origin());
                Console.log(ExceptionUtils.getStacktrace(r.exception()));
                throw new RuntimeException(r.exception());
            }
        });
        this.jsFactory = new JsFactory(this);

        this.dsl = createDsl(internalConnection);

        if (AppjangleGlobal.getStartServerCapability() != null) {
            this.startServerCapability = AppjangleGlobal.getStartServerCapability();
        }
    }

    @Override
    public OnedbNextwebEngine fork(final RemoteConnection internalConnection) {
        final OnedbNextwebEngineJs forkedEngine = new OnedbNextwebEngineJs(internalConnection);

        forkedEngine.client = client;
        forkedEngine.internal = internal;
        forkedEngine.exceptionManager = exceptionManager;

        return forkedEngine;
    }

    @Override
    public BasicClient client() {
        if (this.client == null) {
            this.client = ClientsCommon.createPortable();

            this.client.factories().register(ConcurrencyGwt.createFactory());
        }
        return this.client;
    }

    @Override
    public void setAddressMapping(final String nodeUri, final String serverUri) {
        throw new RuntimeException("Operation not suppored on JavaScript.");
    }

    OnedbNextwebEngineJsInternal internal = null;

    @Override
    public AppjangleClientEngineInternal internal() {
        if (internal == null) {
            internal = new OnedbNextwebEngineJsInternal();
        }
        return internal;
    }

}
