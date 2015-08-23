package com.ononedb.nextweb.js;

import com.appjangle.api.common.LocalServer;
import com.appjangle.api.engine.AppjangleClientEngineInternal;
import com.appjangle.api.engine.Capability;
import com.appjangle.api.engine.StartServerCapability;
import com.ononedb.nextweb.local.LocalServerManager;

public class OnedbNextwebEngineJsInternal implements AppjangleClientEngineInternal {
    protected StartServerCapability startServerCapability;

    protected LocalServerManager localServers;

    @Override
    public boolean hasStartServerCapability() {
        return startServerCapability != null;
    }

    @Override
    public void injectCapability(final Capability capability) {
        if (capability instanceof StartServerCapability) {
            startServerCapability = (StartServerCapability) capability;
            return;
        }

        throw new IllegalArgumentException(
                "This engine cannot recognize the capability: [" + capability.getClass() + "]");
    }

    @Override
    public LocalServer startServer(final String domain) {
        if (startServerCapability == null) {
            throw new IllegalStateException("Please inject a StartServerCapability first.");
        }

        return startServerCapability.startServer(domain);
    }

    @Override
    public boolean hasPersistedReplicationCapability() {

        return true;
    }
}
