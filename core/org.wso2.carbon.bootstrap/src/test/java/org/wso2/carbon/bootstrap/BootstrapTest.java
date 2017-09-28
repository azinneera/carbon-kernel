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
package org.wso2.carbon.bootstrap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.utils.FileManipulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests functionality of Bootstrap class
 */
public class BootstrapTest {
    private Bootstrap bootstrap = new Bootstrap();
    private File libFile;

    /**
     * Create necessary temporary files.
     *
     * @throws IOException for file operation failures
     */
    @BeforeMethod
    public void setUp() throws IOException {
        libFile = Files.createTempDirectory("libFile").toFile();
    }

    /**
     * Test system startsup when necessary directories are
     * given through system properties
     *
     * @throws Exception for malformed URL or file operation failures
     */
    @Test
    public void testAddClassPathEntries() throws Exception {
        System.setProperty("carbon.internal.lib.dir.path", libFile.getParent());
        Path repository = Paths.get(this.getClass().getClassLoader()
                .getResource("repository").toURI().getPath());
        Path carbonHome = Files.createTempDirectory("carbonHome");
        FileManipulator.copyDir(repository.toFile(),
                Paths.get(carbonHome.toString(), "repository").toFile());
        System.setProperty("carbon.home", carbonHome.toString());
        Bootstrap.main(new String[]{});
    }

    /**
     * Test if addFileUrl method throws MalformedURLException for unknown protocols
     *
     * @throws MalformedURLException if a URL is malformed
     */
    @Test(expectedExceptions = MalformedURLException.class)
    public void testAddFileUrl() throws MalformedURLException {
        URL obj = new URL("fil://path/to/file");
        File libFile = new File(obj.toString());
        bootstrap.addFileUrl(libFile);
    }

    /**
     * Test if addJarFileUrls method throws malformedURLException for unknown protocols
     *
     * @throws MalformedURLException if a URL is malformed
     */
    @Test(expectedExceptions = MalformedURLException.class)
    public void testAddJarFileUrls() throws MalformedURLException {
        URL obj = new URL("fil://path/to/file");
        File libFile = new File(obj.toString());
        bootstrap.addFileUrl(libFile);
    }

    /**
     * Test if addJarFileUrls method adds JAR files found in the given directory
     * to the list of URLs for directories with children
     *
     * @throws Exception for malformed URL or file operation failures
     */
    @Test
    public void testAddJarFileUrlsWithChildren() throws Exception {
        File tmpFile = Files.createTempDirectory("tmp").toFile();
        File childFolder = new File(tmpFile.getAbsolutePath(), "tmp_child");
        new File(childFolder.getAbsolutePath(), "test.jar");

        assertTrue(tmpFile.exists());
        bootstrap.addJarFileUrls(tmpFile);
    }

    /**
     * Test if the getClassToLoad method returns the correct class
     * to load the rest of the app
     */
    @Test
    public void testGetClassToLoad() {
        assertNotNull(bootstrap.getClassToLoad());
        assertEquals(bootstrap.getClassToLoad(), "org.wso2.carbon.server.Main");
    }

    /**
     * Test if the getMethodToInvoke method returns main method
     * This will be invoked by the class specified by getClassToLoad method
     */
    @Test
    public void testGetMethodToInvoke() {
        assertNotNull(bootstrap.getMethodToInvoke());
        assertEquals(bootstrap.getMethodToInvoke(), "main");
    }
}
