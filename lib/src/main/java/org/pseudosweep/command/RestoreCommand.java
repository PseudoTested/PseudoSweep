package org.pseudosweep.command;

import com.beust.jcommander.Parameters;
import org.pseudosweep.Instrumenter;

@Parameters(commandDescription = " >>> restores Java source files back to their original versions following instrumentation")
public class RestoreCommand extends InstrumentCommand {

    @Override
    public String getName() {
        return "restore";
    }

    @Override
    void run() {
        Instrumenter instrumenter = new Instrumenter(statementDeletion, extremeMutation, skipTrivial);
        for (String fileName : getJavaFileNames()) {
            instrumenter.restore(fileName);
        }
    }
}
