package org.hibernate.tool.orm.jbt.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.PrintWriter;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.tool.orm.jbt.internal.factory.ConfigurationWrapperFactory;
import org.hibernate.tool.orm.jbt.util.JpaConfiguration;
import org.hibernate.tool.orm.jbt.util.MetadataHelper;
import org.hibernate.tool.orm.jbt.util.NativeConfiguration;
import org.hibernate.tool.orm.jbt.util.RevengConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigurationWrapperTest {

	private static final String TEST_HBM_XML_STRING =
			"<hibernate-mapping package='org.jboss.tools.hibernate.orm.runtime.v_6_5'>" +
			"  <class name='IConfigurationTest$Foo'>" + 
			"    <id name='id'/>" +
			"  </class>" +
			"</hibernate-mapping>";
	
	private ConfigurationWrapper nativeConfigurationWrapper = null;
	private NativeConfiguration wrappedNativeConfiguration = null;
	private ConfigurationWrapper revengConfigurationWrapper = null;
	private RevengConfiguration wrappedRevengConfiguration = null;
	private ConfigurationWrapper jpaConfigurationWrapper = null;
	private JpaConfiguration wrappedJpaConfiguration = null;

	@BeforeEach
	public void beforeEach() throws Exception {
		initializeFacadesAndTargets();
	}	
	
	@Test
	public void testConstruction() {
		assertNotNull(nativeConfigurationWrapper);
		assertNotNull(wrappedNativeConfiguration);
		assertNotNull(revengConfigurationWrapper);
		assertNotNull(wrappedRevengConfiguration);
		assertNotNull(jpaConfigurationWrapper);
		assertNotNull(wrappedJpaConfiguration);
	}

	private void initializeFacadesAndTargets() {
		wrappedNativeConfiguration = new NativeConfiguration();
		nativeConfigurationWrapper = ConfigurationWrapperFactory.createConfigurationWrapper(wrappedNativeConfiguration);
		wrappedRevengConfiguration = new RevengConfiguration();
		revengConfigurationWrapper = ConfigurationWrapperFactory.createConfigurationWrapper(wrappedRevengConfiguration);
		wrappedJpaConfiguration = new JpaConfiguration(null, null);
		jpaConfigurationWrapper = ConfigurationWrapperFactory.createConfigurationWrapper(wrappedJpaConfiguration);
	}
	
	@Test
	public void testGetProperty() {
		// For native configuration
		assertNull(nativeConfigurationWrapper.getProperty("foo"));
		wrappedNativeConfiguration.setProperty("foo", "bar");
		assertEquals("bar", nativeConfigurationWrapper.getProperty("foo"));
		// For reveng configuration
		assertNull(revengConfigurationWrapper.getProperty("foo"));
		wrappedRevengConfiguration.setProperty("foo", "bar");
		assertEquals("bar", revengConfigurationWrapper.getProperty("foo"));
		// For jpa configuration
		assertNull(jpaConfigurationWrapper.getProperty("foo"));
		wrappedJpaConfiguration.setProperty("foo", "bar");
		assertEquals("bar", jpaConfigurationWrapper.getProperty("foo"));
	}

	@Test
	public void testAddFile() throws Exception {
		File testFile = File.createTempFile("test", "hbm.xml");
		PrintWriter printWriter = new PrintWriter(testFile);
		printWriter.write(TEST_HBM_XML_STRING);
		printWriter.close();
		testFile.deleteOnExit();
		// For native configuration
		MetadataSources metadataSources = MetadataHelper.getMetadataSources(wrappedNativeConfiguration);
		assertTrue(metadataSources.getXmlBindings().isEmpty());
		assertSame(
				nativeConfigurationWrapper,
				nativeConfigurationWrapper.addFile(testFile));
		assertFalse(metadataSources.getXmlBindings().isEmpty());
		Binding<?> binding = metadataSources.getXmlBindings().iterator().next();
		assertEquals(testFile.getAbsolutePath(), binding.getOrigin().getName());
		// For reveng configuration
		try {
			revengConfigurationWrapper.addFile(testFile);
			fail();
		} catch (RuntimeException e) {
			assertEquals(
					e.getMessage(),
					"Method 'addFile' should not be called on instances of " + RevengConfiguration.class.getName());
		}
		// For jpa configuration
		try {
			jpaConfigurationWrapper.addFile(testFile);
			fail();
		} catch (RuntimeException e) {
			assertEquals(
					e.getMessage(),
					"Method 'addFile' should not be called on instances of " + JpaConfiguration.class.getName());
		}
	}
	
	@Test 
	public void testSetProperty() {
		// For native configuration
		assertNull(wrappedNativeConfiguration.getProperty("foo"));
		nativeConfigurationWrapper.setProperty("foo", "bar");
		assertEquals("bar", wrappedNativeConfiguration.getProperty("foo"));
		// For reveng configuration
		assertNull(wrappedRevengConfiguration.getProperty("foo"));
		revengConfigurationWrapper.setProperty("foo", "bar");
		assertEquals("bar", wrappedRevengConfiguration.getProperty("foo"));
		// For jpa configuration
		assertNull(wrappedJpaConfiguration.getProperty("foo"));
		jpaConfigurationWrapper.setProperty("foo", "bar");
		assertEquals("bar", wrappedJpaConfiguration.getProperty("foo"));
	}

}