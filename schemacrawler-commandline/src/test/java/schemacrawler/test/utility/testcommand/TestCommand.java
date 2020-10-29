/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.test.utility.testcommand;

import java.io.PrintWriter;

import schemacrawler.tools.executable.BaseSchemaCrawlerCommand;

/**
 * Main executor for the script engine integration.
 *
 * @author Sualeh Fatehi
 */
public final class TestCommand extends BaseSchemaCrawlerCommand<TestOptions> {

  static final String COMMAND = "test-command";

  public TestCommand() {
    super(COMMAND);
  }

  /** {@inheritDoc} */
  @Override
  public void execute() throws Exception {
    try (final PrintWriter writer = new PrintWriter(outputOptions.openNewOutputWriter()); ) {
      writer.println("Output generated from " + this.getClass().getName());
      writer.println(commandOptions);
      writer.flush();
    }
  }

  @Override
  public boolean usesConnection() {
    return true;
  }
}
