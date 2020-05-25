package de.uni_hildesheim.sse.submitter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.submitter.i18n.I18nProviderTest;

/**
 * Test suite to run all test files.
 * @author kunold
 * @author El-Sharkawy
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    I18nProviderTest.class,
})
public class AllTests {

}
