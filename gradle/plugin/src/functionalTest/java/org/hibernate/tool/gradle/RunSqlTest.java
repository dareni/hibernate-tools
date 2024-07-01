/*
 * This source file was generated by the Gradle 'init' task
 */
package org.hibernate.tool.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.gradle.testkit.runner.BuildResult;
import org.hibernate.tool.gradle.test.func.utils.FuncTestConstants;
import org.hibernate.tool.gradle.test.func.utils.FuncTestTemplate;
import org.junit.jupiter.api.Test;

class RunSqlTest extends FuncTestTemplate implements FuncTestConstants {

    private static final String BUILD_FILE_HIBERNATE_TOOLS_SECTION = 
    		"hibernateTools {\n" +
    		"  sqlToRun = 'create table foo (id int not null primary key, baz varchar(256))'\n" +
    		"  hibernateProperties = 'foo.bar'" +
    		"}\n";

	@Override
	public String getBuildFileHibernateToolsSection() {
	    return BUILD_FILE_HIBERNATE_TOOLS_SECTION;
	}
	
	@Override
	public String getHibernatePropertiesFileName() {
		return "foo.bar";
	}

    @Test 
    void testRunSql() throws IOException {
    	performTask("runSql", false);
    }
    
    @Override
    protected void verifyBuild(BuildResult buildResult) {
        assertTrue(buildResult.getOutput().contains("Running SQL: create table foo (id int not null primary key, baz varchar(256))"));
        assertTrue(new File(projectDir, DATABASE_FOLDER_NAME + "/" + DATABASE_FILE_NAME).exists());
    }

 }