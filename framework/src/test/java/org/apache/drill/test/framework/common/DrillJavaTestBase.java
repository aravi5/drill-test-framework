package org.apache.drill.test.framework.common;

import org.apache.log4j.Logger;
import org.testng.annotations.*;

public class DrillJavaTestBase {
    private static final Logger LOG = Logger.getLogger(DrillJavaTestBase.class);

    @BeforeSuite(alwaysRun = true, description = "Invoked at the beginning of the Test Suite.")
    public void baseBeforeSuite() {
        LOG.info("Running Base Before Suite");
    }

    @BeforeTest(alwaysRun = true, description = "Invoked at the beginning of every Test Module.")
    public void baseBeforeTest() {
        LOG.info("Running Base Before Test");
    }

    @BeforeClass(alwaysRun = true, description = "Invoked at the beginning of every Test Class.")
    public void baseBeforeClass() {
        LOG.info("Running Base Before Class");
    }

    @BeforeMethod(alwaysRun = true, description = "Invoked before every Test Method.")
    public void baseBeforeMethod() {
        LOG.info("Running Base Before Method");
    }

    @AfterMethod(alwaysRun = true, description = "Invoked after every Test Method")
    public void baseAfterMethod() {
        LOG.info("Running Base After Method");
    }

    @AfterClass(alwaysRun = true, description = "Invoked after all tests in a Test Class finish.")
    public void baseAfterClass() {
        LOG.info("Running Base After Class");
    }

    @AfterTest(alwaysRun = true, description = "Invoked once tests in all classes in the Test Module finish.")
    public void baseAfterTest() {
        LOG.info("Running Base After Test");
    }

    @AfterSuite(alwaysRun = true, description = "Invoked once all tests in the Suite are completed.")
    public void baseAfterSuite() {
        LOG.info("Running Base After Suite");
    }
}
