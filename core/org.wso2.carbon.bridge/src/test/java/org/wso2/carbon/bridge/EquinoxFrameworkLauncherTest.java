/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bridge;

import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

public class EquinoxFrameworkLauncherTest {
    private EquinoxFrameworkLauncher frameworkLauncher;
    private ServletConfig servletConfig;

    @BeforeMethod
    public void setUp() throws Exception {
        frameworkLauncher = new EquinoxFrameworkLauncher();
    }

    /**
     * Test init method with a mock ServletConfig
     */
    @Test (groups = {"org.wso2.carbon.bridge"},
            description = "")
    public void testInit() {
        servletConfig = mock(ServletConfig.class);
        when(servletConfig.getInitParameter("defaultPool")).thenReturn("testpool1");
        when(servletConfig.getServletContext()).thenReturn(Mockito.mock(ServletContext.class));
        frameworkLauncher.init(servletConfig);
    }

    /**
     * Test deploy method with and without patches
     * The necessary system properties has to be set before applying patches
     *
     * @throws IOException
     */
    @Test (groups = {"org.wso2.carbon.bridge"},
            description = "")
    public void testDeployWithAndWithoutApplyPatches() throws IOException {
        assertNull(System.getProperty("applyPatches"));
        frameworkLauncher.deploy();

        TemporaryFolder patchesDirpath = new TemporaryFolder();
        TemporaryFolder carbonComponentsDirPath = new TemporaryFolder();
        TemporaryFolder carbonHome = new TemporaryFolder();
        patchesDirpath.create();
        carbonComponentsDirPath.create();
        carbonHome.create();

        carbonComponentsDirPath.newFolder("plugins");

        System.setProperty("carbon.home", carbonHome.getRoot().getAbsolutePath());
        System.setProperty("carbon.components.dir.path", carbonComponentsDirPath.getRoot().getAbsolutePath());
        System.setProperty("carbon.patches.dir.path", patchesDirpath.getRoot().getAbsolutePath());
        System.setProperty("applyPatches", "true");

        assertNotNull(System.getProperty("applyPatches"));
        frameworkLauncher.deploy();
    }

    /**
     * Test if undeploy method removes platformDirectory
     */
    @Test (groups = {"org.wso2.carbon.bridge"},
            description = "")
    public void testUndeploy() {
        servletConfig = mock(ServletConfig.class);
        when(servletConfig.getInitParameter("defaultPool")).thenReturn("testpool1");
        when(servletConfig.getServletContext()).thenReturn(Mockito.mock(ServletContext.class));
        frameworkLauncher.init(servletConfig);
        frameworkLauncher.deploy();

        assertNotNull(frameworkLauncher.getPlatformDirectory());
        frameworkLauncher.undeploy();
        assertNull(frameworkLauncher.getPlatformDirectory());
    }

    /**
     * Test if stop method removes frameworkContextClassLoader
     */
    @Test (groups = {"org.wso2.carbon.bridge"},
            description = "")
    public void testStop() {
        servletConfig = mock(ServletConfig.class);
        when(servletConfig.getInitParameter("defaultPool")).thenReturn("testpool1");
        when(servletConfig.getServletContext()).thenReturn(Mockito.mock(ServletContext.class));
        frameworkLauncher.init(servletConfig);
        frameworkLauncher.deploy();
        frameworkLauncher.stop();
        assertNull(frameworkLauncher.getFrameworkContextClassLoader());
    }

    /**
     * Test if isRunning returns false when OSGi framework is down
     */
    @Test (groups = {"org.wso2.carbon.bridge"},
            description = "")
    public void testIsRunning() {
        assertFalse(frameworkLauncher.isRunning());
    }
}
