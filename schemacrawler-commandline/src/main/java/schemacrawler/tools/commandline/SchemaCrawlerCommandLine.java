/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.tools.commandline;


import java.sql.Connection;
import java.util.logging.Level;

import schemacrawler.schemacrawler.*;
import schemacrawler.tools.databaseconnector.ConnectionOptions;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.UserCredentials;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.OutputOptions;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;
import us.fatehi.commandlineparser.CommandLineUtility;

/**
 * Utility for parsing the SchemaCrawler command-line.
 *
 * @author Sualeh Fatehi
 */
public final class SchemaCrawlerCommandLine
  implements CommandLine
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(SchemaCrawlerCommandLine.class.getName());

  private final String command;
  private final Config config;
  private final SchemaCrawlerOptions schemaCrawlerOptions;
  private final OutputOptions outputOptions;
  private final ConnectionOptions connectionOptions;

  private final DatabaseConnector databaseConnector;

  public SchemaCrawlerCommandLine(final String[] args, final Config argsMap)
    throws SchemaCrawlerException
  {
    if (argsMap == null || argsMap.isEmpty())
    {
      throw new SchemaCrawlerCommandLineException(
        "Please provide command-line arguments");
    }

    // Match the database connector in the best possible way, using the
    // server argument, or the JDBC connection URL
    final DatabaseServerTypeParser dbServerTypeParser = new DatabaseServerTypeParser();
    databaseConnector = dbServerTypeParser.parse(args);
    LOGGER.log(Level.INFO,
               new StringFormat("Using database plugin <%s>",
                                databaseConnector.getDatabaseServerType()));

    config = loadConfig(args, argsMap);

    final CommandParser commandParser = new CommandParser();
    command = commandParser.parse(args).toString();

    final SchemaCrawlerOptionsParser schemaCrawlerOptionsParser = new SchemaCrawlerOptionsParser(
      config);
    schemaCrawlerOptions = schemaCrawlerOptionsParser.getOptions();

    final OutputOptionsParser outputOptionsParser = new OutputOptionsParser(
      config);
    outputOptions = outputOptionsParser.parse(args);

    final AdditionalConfigOptionsParser additionalConfigOptionsParser = new AdditionalConfigOptionsParser(
      config);
    additionalConfigOptionsParser.loadConfig();

    final UserCredentials userCredentials = parseConnectionOptions(
      dbServerTypeParser.isBundled());
    // Connect using connection options provided from the command-line,
    // provided configuration, and bundled configuration
    connectionOptions = databaseConnector
      .newDatabaseConnectionOptions(userCredentials, config);
  }

  @Override
  public void execute()
    throws Exception
  {
    if (connectionOptions == null)
    {
      throw new SchemaCrawlerException("No connection options provided");
    }

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(
      command);
    // Configure
    executable.setOutputOptions(outputOptions);
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    executable.setAdditionalConfiguration(config);
    try (final Connection connection = connectionOptions.getConnection())
    {
      // Get partially built database specific options, built from the
      // classpath resources, and then override from config loaded in
      // from the command-line
      final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = databaseConnector
        .getSchemaRetrievalOptionsBuilder(connection);
      schemaRetrievalOptionsBuilder.fromConfig(config);

      final SchemaRetrievalOptions schemaRetrievalOptions = schemaRetrievalOptionsBuilder
        .toOptions();

      // Execute the command
      executable.setConnection(connection);
      executable.setSchemaRetrievalOptions(schemaRetrievalOptions);
      executable.execute();
    }
  }

  public final String getCommand()
  {
    return command;
  }

  public final Config getConfig()
  {
    return config;
  }

  public final ConnectionOptions getConnectionOptions()
  {
    return connectionOptions;
  }

  public final OutputOptions getOutputOptions()
  {
    return outputOptions;
  }

  public final SchemaCrawlerOptions getSchemaCrawlerOptions()
  {
    return schemaCrawlerOptions;
  }

  /**
   * Loads configuration from a number of sources, in order of priority.
   */
  private Config loadConfig(final String[] args, final Config argsMap)
    throws SchemaCrawlerException
  {
    return CommandLineUtility.loadConfig(args, argsMap, databaseConnector);
  }

  /**
   * Parse connection options, for both ways of connecting.
   */
  private UserCredentials parseConnectionOptions(final boolean isBundled)
    throws SchemaCrawlerException
  {
    final BaseDatabaseConnectionOptionsParser dbConnectionOptionsParser;
    if (!isBundled)
    {
      dbConnectionOptionsParser = new CommandLineConnectionOptionsParser(config);
    }
    else
    {
      dbConnectionOptionsParser = new BundledDriverConnectionOptionsParser(
        config);
    }
    dbConnectionOptionsParser.loadConfig();
    config.putAll(dbConnectionOptionsParser.getOptions());
    final UserCredentials userCredentials = dbConnectionOptionsParser
      .getUserCredentials();
    return userCredentials;
  }

}
