package org.apache.sling.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.sling.upgrade.impl.UpgradeServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UpgradeServiceTest.class);

    private InputStream jar;

    private UpgradeService upgradeService;

    private UpgradeRequest request;

    @Before
    public void init() throws IOException {
        jar = getClass().getClassLoader().getResourceAsStream("sling.jar");
        upgradeService = new UpgradeServiceImpl();

        // Initialize a fake OSGi Container
        ComponentContext componentContext = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);

        // Provide some bundles
        Bundle bundle1 = Mockito.mock(Bundle.class);
        Mockito.when(bundle1.getVersion()).thenReturn(new Version("2.4.0"));
        Mockito.when(bundle1.getSymbolicName()).thenReturn("org.apache.sling.jcr.api");

        Bundle bundle2 = Mockito.mock(Bundle.class);
        Mockito.when(bundle2.getVersion()).thenReturn(new Version("3.0.0"));
        Mockito.when(bundle2.getSymbolicName()).thenReturn("org.apache.sling.jcr.base");

        Bundle bundle3 = Mockito.mock(Bundle.class);
        Mockito.when(bundle3.getVersion()).thenReturn(new Version("4.0.0"));
        Mockito.when(bundle3.getSymbolicName()).thenReturn("org.apache.sling.jcr.davex");

        Bundle[] bundles = new Bundle[] { bundle1, bundle2, bundle3 };
        Mockito.when(bundleContext.getBundles()).thenReturn(bundles);

        // Call the activator
        ((UpgradeServiceImpl) upgradeService).activate(componentContext);

        // read the request
        this.request = upgradeService.readSlingJar(jar);

    }

    @Test
    public void testUpgradeRequest() throws IOException {
        log.info("testUpgradeRequest");

        assertEquals("Apache Sling - CMS Application Builder", request.getTitle());
        assertEquals("The Apache Software Foundation", request.getVendor());
        assertEquals("0.10.1-SNAPSHOT", request.getVersion());

        log.info("Test successful!");
    }

    @Test
    public void testUpgradeRequestBundles() throws IOException {
        log.info("testUpgradeRequestBundles");
        List<BundleEntry> bundles = request.getBundles();
        assertNotNull(bundles);

        assertTrue(!bundles.isEmpty());

        for (BundleEntry bundle : bundles) {
            assertNotNull(bundle.getContents());
            assertNotNull(bundle.getStart());
            assertNotNull(bundle.getSymbolicName());
            assertNotNull(bundle.getVersion());
            switch (bundle.getSymbolicName()) {
            case "org.apache.sling.jcr.api":
                assertTrue(bundle.isInstalled());
                assertFalse(bundle.isUpdated());
                break;
            case "org.apache.sling.jcr.base":
                assertTrue(bundle.isInstalled());
                assertTrue(bundle.isUpdated());
                break;
            case "org.apache.sling.jcr.davex":
                assertTrue(bundle.isInstalled());
                assertFalse(bundle.isUpdated());
                break;
            default:
                assertTrue(bundle.isUpdated());
                assertFalse(bundle.isInstalled());
                break;
            }
            log.debug("Bundle: " + bundle.getSymbolicName());
        }

        log.info("Test successful!");
    }

    @Test
    public void testUpgradeRequestConfigs() throws IOException {
        log.info("testUpgradeRequestConfigs");
        List<ConfigEntry> configs = request.getConfigs();
        assertNotNull(configs);

        assertTrue(!configs.isEmpty());

        configs.forEach(c -> log.debug("Config: " + c.getPid()));

        log.info("Test successful!");
    }
}
