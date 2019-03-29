package org.apache.drill.test.framework;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.drill.test.framework.common.DrillJavaTestBase;
import org.apache.drill.test.framework.common.DrillTestNGDefaults;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.apache.drill.test.framework.common.DrillTestNGDefaults.UNIT_GROUP;

@Test(groups = UNIT_GROUP)
public class DrillTestFrameworkUnitTests extends DrillJavaTestBase {
    private static final Logger LOG = Logger.getLogger(DrillTestFrameworkUnitTests.class);
    private static final String SAMPLE_RM_CONFIG_NAME = "sample-drill-rm-override.conf";

    @BeforeTest(alwaysRun = true)
    public void runBeforeTest() {
        LOG.debug("Running before test");
    }


    /**
     * A unit test to validate that {@link Utils#getQueryProfile(String)}
     * can be used to obtain query profile. This test also validates that
     * the returned query profile can be deserialized to {@link DrillQueryProfile}.
     *
     * @param method
     */
    @Test(groups = UNIT_GROUP)
    public void testGetQueryProfile(Method method) {
        final Properties props = Utils.createConnectionProperties();
        final ConnectionPool pool = new ConnectionPool(props);
        final String sqlStatement = "select name, val, status from sys.options where name like \'%runtime%\'";

        try (Connection connection = pool.getOrCreateConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlStatement);

            final String queryId = Utils.getQueryID(resultSet);
            DrillQueryProfile profile = Utils.getQueryProfile(queryId);
            Assert.assertEquals(profile.queryId, queryId);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test " + method.getName() + " failed due to " + e.getMessage());
        }
    }

    /**
     * A unit test to validate that {@link Utils#getQueryProfile(String)}
     * fails with the right error if a profile for a specified queryId does not exist.
     */
    @Test(groups = UNIT_GROUP)
    public void testQueryProfileDoesNotExist() {
        final String queryId = "invalidQueryId";

        try {
            Utils.getQueryProfile(queryId);
            Assert.fail("Querying invalid query profile did not throw exception!");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Could not get query profile"),
                    "Expected error message \"Could not get query profile\" " +
                            "but obtained - " + e.getMessage());
        }
    }


    /**
     * Test reading a sample RM config file in to a Java Bean.
     */
    @Test(groups = UNIT_GROUP)
    public void testReadSampleRMConfigFile() {
        try {
            DrillRMConfig drillRMConfig = DrillRMConfig.load(SAMPLE_RM_CONFIG_NAME);
            Assert.assertEquals(drillRMConfig.poolName, "root",
                    "Root resource pool name did not match");

            Assert.assertEquals(drillRMConfig.childPools.size(), 2,
                    "Number of child pools in the config did not match!");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Negative test to validate the behavior when the config file does not exist.
     */
    @Test(groups = UNIT_GROUP, expectedExceptions = com.typesafe.config.ConfigException.class)
    public void testLoadConfigWhenFileDoesNotExist() throws IOException {
        final String invalidPath = "invalid-config-file.conf";

        DrillRMConfig.load(invalidPath);
    }

    /**
     * Test reading a sample RM config file in to a Java Bean.
     */
    @Test(groups = UNIT_GROUP)
    public void testConfigFileRenderer() {
        try {
            DrillRMConfig drillRMConfig = DrillRMConfig.load(SAMPLE_RM_CONFIG_NAME);
            Assert.assertEquals(drillRMConfig.poolName, "root",
                    "Root resource pool name did not match");

            Assert.assertEquals(drillRMConfig.childPools.size(), 2,
                    "Number of child pools in the config did not match!");

            Config config = ConfigFactory.parseString(drillRMConfig.render());
            Assert.assertEquals(config.getString(DrillRMConfig.RESOURCE_POOL_NAME_KEY),
                    drillRMConfig.poolName,
                    "Pool names did not match!");
            Assert.assertEquals(config.getString(DrillRMConfig.QUEUE_SELECTION_POLICY_KEY),
                    drillRMConfig.queueSelectionPolicy,
                    "Queue selection policy did not match!");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Temporary unit test to check if testGetDrillHostnames
     * @param method
     */
    public void testGetDrillHostnames(Method method) {
        final Properties props = Utils.createConnectionProperties();
        final ConnectionPool pool = new ConnectionPool(props);
        try (Connection connection = pool.getOrCreateConnection()) {
            //TODO: Validation once ISSUE-561 is merged
            Utils.getDrillbitHosts(connection).forEach(LOG::info);

            LOG.info(Utils.execCmd("clush -a hostname -f"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test " + method.getName() + " failed due to " + e.getMessage());
        }
    }

    /**
     * Test if DrillRMConfig can be serialized to a file.
     * TODO: Read from the file to validate.
     * @throws IOException
     */
    public void testWriteRMConfigToFile() throws IOException {
        final String fileName = "tempRMConfig.conf";
        final String filePath = DrillTestDefaults.TEST_ROOT_DIR + "/conf/" + fileName;
        File file = new File(filePath);

        if(file.exists()) {
            LOG.warn(filePath + " exists! Removing the file");
            Utils.execCmd("rm -rf " + filePath);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            DrillRMConfig drillRMConfig = DrillRMConfig.load(SAMPLE_RM_CONFIG_NAME);
            writer.write(drillRMConfig.render());
        }
    }
}
