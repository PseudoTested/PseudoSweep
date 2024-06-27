package org.pseudosweep.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.pseudosweep.DataManager;
import org.pseudosweep.LaunchException;

public abstract class Command {

    @Parameter(names = {"-sdl", "--statementdeletion"}, description = "Execute statement deletion operator instrumentation")
    boolean statementDeletion = false;

    @Parameter(names = {"-xmt", "--extrememutation"}, description = "Execute extreme mutation operator instrumentation")
    boolean extremeMutation = false;

    @Parameter(names = {"-d", "--dataDir"}, description = "The directory to report data from this execution (relative to the current directory)")
    String dataPath = "PS-data";

    public abstract String getName();

    public abstract void checkParams() throws ParameterException;

    public void launch() throws LaunchException {
        DataManager.createDataDirectory(dataPath);
        DataManager.configureLogging(getName());
        run();
    }

    abstract void run();
}

