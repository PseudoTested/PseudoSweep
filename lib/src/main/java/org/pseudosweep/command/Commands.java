package org.pseudosweep.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.util.Set;

import static org.pseudosweep.util.StringUtils.encloseQuotes;

public class Commands {

    static Set<Command> commands = Set.of(
            new InstrumentCommand(),
            new RestoreCommand(),
            new SweepCommand(),
            new AnalyzeCommand()
    );

    public static Command getCommand(String commandString) throws ParameterException {
        for (Command command : commands) {
            if (command.getName().equals(commandString)) {
                command.checkParams();
                return command;
            }
        }
        throw new ParameterException("Unrecognised command: " + encloseQuotes(commandString));
    }

    public static void addCommands(JCommander.Builder builder) {
        commands.forEach(command -> builder.addCommand(command.getName(), command));
    }
}
