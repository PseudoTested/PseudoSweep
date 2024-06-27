package org.pseudosweep;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.pseudosweep.command.Command;
import org.pseudosweep.command.Commands;
import org.pseudosweep.logging.LogManager;
import org.pseudosweep.logging.Logger;

public class Launch {

    private static final Logger logger = LogManager.getLogger(Launch.class);

    @Parameter(names = "--help", help = true, description = "Prints usage information")
    boolean help;

    Command instantiateCommand(String... args) throws ParameterException, LaunchException {
        // build the JCommander instance
        JCommander.Builder builder = JCommander.newBuilder().
                programName(Launch.class.getCanonicalName()).
                addObject(this);
        Commands.addCommands(builder);
        JCommander jc = builder.build();

        // parse the arguments
        jc.parse(args);
        if (help) {
            jc.usage();
            System.exit(1);
        }

        // get the Command to launch
        String commandStr = jc.getParsedCommand();
        if (commandStr == null) {
            throw new ParameterException("No Command specified");
        }
        return Commands.getCommand(commandStr);
    }

    public static void main(String... args) {
        try {
            Command command = new Launch().instantiateCommand(args);
            command.launch();
        } catch (LaunchException | ParameterException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("[Hint: Use --help for help on commands and parameters]");
        } catch (Error | Exception e) {
            logger.fatal(e);
        } finally {
            System.exit(1);
        }

        // Due to the possible existence of zombie threads, we may need to force termination...
        System.exit(0);
    }
}
