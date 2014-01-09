package de.zeos.zen2.app;

import java.util.Date;

import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.app.model.ScriptHandlerConsoleEntry;
import de.zeos.zen2.db.InternalDBAccessor;

public class ScriptHandlerConsole {
    private ScriptHandler handler;
    private InternalDBAccessor internalDBAccessor;

    public ScriptHandlerConsole(ScriptHandler handler, InternalDBAccessor internalDBAccessor) {
        this.handler = handler;
        this.internalDBAccessor = internalDBAccessor;
    }

    public void log(String msg) {
        this.handler.getConsoleEntries().add(new ScriptHandlerConsoleEntry(new Date(), msg));
        this.internalDBAccessor.updateScriptHandler(this.handler);
    }
}
