package ness.db.postgres;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.IntegerMapper;

import ness.db.DatabasePreparer;
import ness.db.DatabasePreparers;

import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.testing.lessio.AllowDNSResolution;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowDNSResolution
@AllowNetworkAccess(endpoints="127.0.0.1:*")
public class TestPostgresPreparer
{
    @Inject
    private Lifecycle lifecycle;

    @Inject
    @Named("test")
    private IDBI testDbi;


    @Test
    public void testSimple() throws Exception
    {
        final DatabasePreparer preparer = DatabasePreparers.forPostgres(Resources.getResource(TestPostgresPreparer.class, "/sql/").toURI());

        try {
            preparer.setupDatabase("simple");
            Assert.assertTrue(preparer.exists());
        }
        finally {
            preparer.teardownDatabase();
        }

        Assert.assertFalse(preparer.exists());
    }

    @Test
    public void testGuice() throws Exception
    {
        final DatabasePreparer preparer = DatabasePreparers.forPostgres(Resources.getResource(TestPostgresPreparer.class, "/sql/").toURI());

        try {
            preparer.setupDatabase("simple");
            Assert.assertTrue(preparer.exists());

            final Module module = preparer.getGuiceModule("test");

            final Injector injector = Guice.createInjector(ConfigModule.forTesting(), new LifecycleModule(), module);

            injector.injectMembers(this);
            Assert.assertNotNull(lifecycle);
            lifecycle.executeTo(LifecycleStage.START_STAGE);

            Assert.assertNotNull(testDbi);

            testWithInjectedDbi();
            testMigratory();

            lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        }
        finally {
            preparer.teardownDatabase();
        }

        Assert.assertFalse(preparer.exists());
    }

    @Test
    public void testSimpleSchema() throws Exception
    {
        final DatabasePreparer preparer = DatabasePreparers.forPostgresSchema("trumpet_test", Resources.getResource(TestPostgresPreparer.class, "/sql/").toURI());

        try {
            preparer.setupDatabase("simple");
            Assert.assertTrue(preparer.exists());
        }
        finally {
            preparer.teardownDatabase();
        }

        Assert.assertFalse(preparer.exists());
    }

    @Test
    public void testGuiceSchema() throws Exception
    {
        final DatabasePreparer preparer = DatabasePreparers.forPostgresSchema("trumpet_test", Resources.getResource(TestPostgresPreparer.class, "/sql/").toURI());

        try {
            preparer.setupDatabase("simple");
            Assert.assertTrue(preparer.exists());

            final Module module = preparer.getGuiceModule("test");

            final Injector injector = Guice.createInjector(ConfigModule.forTesting(), new LifecycleModule(), module);

            injector.injectMembers(this);
            Assert.assertNotNull(lifecycle);
            lifecycle.executeTo(LifecycleStage.START_STAGE);

            Assert.assertNotNull(testDbi);

            testWithInjectedDbi();
            testMigratory();

            lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        }
        finally {
            preparer.teardownDatabase();
        }

        Assert.assertFalse(preparer.exists());
    }

    private void testWithInjectedDbi()
    {
        final int testValue = new Random().nextInt();

        Assert.assertNotNull(testDbi);
        final int dbTest = testDbi.withHandle(new HandleCallback<Integer>() {
                @Override
                public Integer withHandle(final Handle handle) {
                    return handle.createQuery(String.format("SELECT %d", testValue))
                    .map(IntegerMapper.FIRST)
                    .first();
                }
            });

        Assert.assertEquals(testValue, dbTest);
    }

    private void testMigratory()
    {
        Assert.assertNotNull(testDbi);
        final int dbTest = testDbi.withHandle(new HandleCallback<Integer>() {
                @Override
                public Integer withHandle(final Handle handle) {
                    return handle.createQuery("SELECT COUNT(1) FROM migratory_metadata")
                    .map(IntegerMapper.FIRST)
                    .first();
                }
            });

        Assert.assertEquals(2, dbTest);
    }
}

