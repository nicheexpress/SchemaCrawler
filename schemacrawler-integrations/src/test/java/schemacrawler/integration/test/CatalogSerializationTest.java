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

package schemacrawler.integration.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.BaseDatabaseTest;
import sf.util.IOUtility;

public class CatalogSerializationTest
  extends BaseDatabaseTest
{

  @Test
  public void catalogSerializationWithFst()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsWithMaximumSchemaInfoLevel();

    final Catalog catalog = getCatalog(schemaCrawlerOptions);
    assertThat("Could not obtain catalog", catalog, notNullValue());
    assertThat("Could not find any schemas",
               catalog.getSchemas(),
               not(empty()));

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").orElse(null);
    assertThat("Could not obtain schema", schema, notNullValue());
    assertThat("Unexpected number of tables in the schema",
               catalog.getTables(schema),
               hasSize(10));

    final Path testOutputFile = IOUtility
      .createTempFilePath("sc_fst_serialization", "fst");
    try (
        final FSTObjectOutput fstout = new FSTObjectOutput(new FileOutputStream(testOutputFile
          .toFile()));)
    {
      fstout.writeObject(catalog);
    }
    assertThat("Catalog was not serialized",
               Files.size(testOutputFile),
               greaterThan(0L));

    Catalog catalogDeserialized = null;
    try (
        final FSTObjectInput fstin = new FSTObjectInput(new FileInputStream(testOutputFile
          .toFile()));)
    {
      catalogDeserialized = (Catalog) fstin.readObject();
    }

    final Schema schemaDeserialized = catalogDeserialized
      .lookupSchema("PUBLIC.BOOKS").orElse(null);
    assertThat("Could not obtain schema", schemaDeserialized, notNullValue());
    assertThat("Unexpected number of tables in the schema",
               catalogDeserialized.getTables(schemaDeserialized),
               hasSize(10));
  }

  @Test
  public void catalogSerializationWithJava()
    throws Exception
  {
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsWithMaximumSchemaInfoLevel();

    final Catalog catalog = getCatalog(schemaCrawlerOptions);
    assertThat("Could not obtain catalog", catalog, notNullValue());
    assertThat("Could not find any schemas",
               catalog.getSchemas(),
               not(empty()));

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").orElse(null);
    assertThat("Could not obtain schema", schema, notNullValue());
    assertThat("Unexpected number of tables in the schema",
               catalog.getTables(schema),
               hasSize(10));

    final Path testOutputFile = IOUtility
      .createTempFilePath("sc_java_serialization", "ser");
    try (
        final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(testOutputFile
          .toFile()));)
    {
      out.writeObject(catalog);
    }
    assertThat("Catalog was not serialized",
               Files.size(testOutputFile),
               greaterThan(0L));

    Catalog catalogDeserialized = null;
    try (
        final ObjectInputStream in = new ObjectInputStream(new FileInputStream(testOutputFile
          .toFile()));)
    {
      catalogDeserialized = (Catalog) in.readObject();
    }

    final Schema schemaDeserialized = catalogDeserialized
      .lookupSchema("PUBLIC.BOOKS").orElse(null);
    assertThat("Could not obtain schema", schemaDeserialized, notNullValue());
    assertThat("Unexpected number of tables in the schema",
               catalogDeserialized.getTables(schemaDeserialized),
               hasSize(10));
  }

}
