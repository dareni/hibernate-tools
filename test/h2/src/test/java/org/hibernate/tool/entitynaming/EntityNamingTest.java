/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 *
 * Copyright 2004-2021 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL),
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.tool.entitynaming;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.hibernate.tool.api.export.Exporter;
import org.hibernate.tool.api.export.ExporterConstants;
import org.hibernate.tool.api.export.ExporterFactory;
import org.hibernate.tool.api.export.ExporterType;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;
import org.hibernate.tool.api.reveng.RevengSettings;
import org.hibernate.tool.api.reveng.RevengStrategy;
import org.hibernate.tool.api.reveng.RevengStrategyFactory;
import org.hibernate.tools.test.util.JdbcUtil;
import org.hibernate.tools.test.util.ResourceUtil;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test lowercase database identifiers and specified entity package and class name.
 *
 * Settings: 1. Adjust hibernate.properties to clear hibernate.default_schema and
 * hibernate.default_catalog. Setting these values fail the classname/tablename matching logic
 * because of a call to nullifyDefaultCatalogAndSchema(table) in RootClassBinder.bind() before the
 * TableToClassName logic.
 *
 * 2. Create a custom RevengStrategy (eg RevengStrategyEntityNaming.java) with the required
 * SchemaSelection settings.
 *
 * @author Daren
 */
@TestInstance(Lifecycle.PER_CLASS)
public class EntityNamingTest {

  private static final Logger log = Logger.getLogger(EntityNamingTest.class);

  @TempDir
  public File outputDir = new File("output");

  static final String packageName = "com.entity";

  Properties hibernateProperties = new Properties();

  @BeforeAll
  public void setUp() {
    JdbcUtil.createDatabase(this);
    try {
      hibernateProperties.load(JdbcUtil.getAlternateHibernateProperties(this));
      if (log.isInfoEnabled()) {
        log.info(hibernateProperties.toString());
      }
    } catch (IOException ex) {
      throw new RuntimeException("Alternate hibernate.properties does not exist!", ex);
    }
  }

  @AfterAll
  public void tearDown() {
    JdbcUtil.dropDatabase(this);
  }

  @Test
  public void testGenerateJava() throws IOException {
    File[] revengFiles = new File[]{
      ResourceUtil.resolveResourceFile(this.getClass(), "reveng.xml")};

    RevengStrategy reveng = RevengStrategyFactory.createReverseEngineeringStrategy(
        null, revengFiles);

    reveng = new RevengStrategyEntityNaming(reveng);

    //Necessary to set the root strategy. 
    RevengSettings revengSettings 
        = new RevengSettings(reveng).setDefaultPackageName(packageName)
            .setDetectManyToMany(true)
            .setDetectOneToOne(true)
            .setDetectOptimisticLock(true);

    reveng.setSettings(revengSettings);
    MetadataDescriptor metadataDescriptor = MetadataDescriptorFactory
        .createReverseEngineeringDescriptor(reveng, hibernateProperties);

    Exporter exporter = ExporterFactory.createExporter(ExporterType.JAVA);
    exporter.getProperties().put(ExporterConstants.METADATA_DESCRIPTOR, metadataDescriptor);
    exporter.getProperties().put(ExporterConstants.DESTINATION_FOLDER, outputDir);
    exporter.getProperties().setProperty("ejb3", "true");
    exporter.start();
    String packageDir = outputDir + File.separator
        + packageName.replace(".", File.separator);
    File dummy = new File(packageDir, "Dummy.java");
    assertTrue(dummy.exists());
    File order = new File(packageDir, "Order.java");
    assertTrue(order.exists());
    File orderItem = new File(packageDir, "OrderItem.java");
    assertTrue(orderItem.exists());
    String str = new String(Files.readAllBytes(orderItem.toPath()));
    assertTrue(str.contains("private Integer oiId;"));
    assertTrue(str.contains("private Order order;"));
  }
}
