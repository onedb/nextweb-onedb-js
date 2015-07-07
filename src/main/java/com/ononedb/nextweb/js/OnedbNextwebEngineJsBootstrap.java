package com.ononedb.nextweb.js;

import com.appjangle.api.engine.NextwebGlobal;

import one.common.One;

public class OnedbNextwebEngineJsBootstrap {

    public static OnedbNextwebEngineJs init() {
        final OnedbNextwebEngineJs engine = new OnedbNextwebEngineJs();
        NextwebGlobal.injectEngine(engine);

        One.setDsl(engine.getDsl());
        return engine;
    }

}
