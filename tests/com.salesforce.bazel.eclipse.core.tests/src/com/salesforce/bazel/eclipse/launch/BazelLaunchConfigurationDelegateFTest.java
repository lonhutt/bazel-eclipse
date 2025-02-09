/**
 * Copyright (c) 2019, Salesforce.com, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.bazel.eclipse.launch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.salesforce.bazel.eclipse.launch.BazelLaunchConfigurationSupport.BazelLaunchConfigAttributes;
import com.salesforce.bazel.eclipse.mock.EclipseFunctionalTestEnvironmentFactory;
import com.salesforce.bazel.eclipse.mock.MockEclipse;
import com.salesforce.bazel.eclipse.mock.MockILaunch;
import com.salesforce.bazel.eclipse.mock.MockILaunchConfiguration;
import com.salesforce.bazel.eclipse.mock.MockResourceHelper;
import com.salesforce.bazel.eclipse.runtime.impl.EclipseWorkProgressMonitor;
import com.salesforce.bazel.sdk.command.test.MockCommandSimulatedOutputMatcher;
import com.salesforce.bazel.sdk.command.test.TestBazelCommandEnvironmentFactory;
import com.salesforce.bazel.sdk.path.FSPathHelper;
import com.salesforce.bazel.sdk.workspace.test.TestOptions;

public class BazelLaunchConfigurationDelegateFTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void testHappyRunLaunch() throws Exception {
        // setup functional test env
        MockEclipse mockEclipse = createMockEnvironment("runla");
        ILaunchConfiguration launchConfig = createLaunchConfiguration("run", TestOptions.JAVA_BINARY_TARGET_NAME);
        ILaunch launch = new MockILaunch(launchConfig);
        IProgressMonitor progress = new EclipseWorkProgressMonitor();
        // on Windows, the launcher is an .exe file extension
        addBazelCommandOutput(mockEclipse.getBazelCommandEnvironmentFactory(), 0,
            FSPathHelper.osSeps(".*bin/projects/libs/javalib0/" + TestOptions.JAVA_BINARY_TARGET_NAME + ".*"),
                "bazel run result"); // $SLASH_OK
        BazelLaunchConfigurationDelegate delegate = mockEclipse.getLaunchDelegate();

        // method under test
        delegate.launch(launchConfig, "debug", launch, progress);

        // verify
        MockResourceHelper mockResourceHelper = mockEclipse.getMockResourceHelper();
        String[] cmdLine = mockResourceHelper.lastExecCommandLine;
        String expectedExec = FSPathHelper.osSeps("bin/projects/libs/javalib0/" + TestOptions.JAVA_BINARY_TARGET_NAME); // $SLASH_OK
        String actualExec = cmdLine[0];

        //System.out.println("testHappyRunLaunch expectedExec = " + expectedExec + " actualPath = " + actualExec);

        assertTrue(actualExec.endsWith(expectedExec) || actualExec.endsWith(expectedExec + ".exe"));
        assertTrue(cmdLine[1].contains("debug"));
        assertTrue(cmdLine[2].contains("testvalue1"));
        assertTrue(cmdLine[3].contains("testvalue2"));
        assertTrue(cmdLine[4].contains("testvalue3"));
    }

    @Test
    public void testHappyTestLaunch() throws Exception {
        // setup functional test env
        MockEclipse mockEclipse = createMockEnvironment("testla");
        ILaunchConfiguration launchConfig = createLaunchConfiguration("test", "javalib0Test");
        ILaunch launch = new MockILaunch(launchConfig);
        IProgressMonitor progress = new EclipseWorkProgressMonitor();
        addBazelCommandOutput(mockEclipse.getBazelCommandEnvironmentFactory(), 1, "test", "bazel test result");
        BazelLaunchConfigurationDelegate delegate = mockEclipse.getLaunchDelegate();

        // method under test
        delegate.launch(launchConfig, "debug", launch, progress);

        // verify
        MockResourceHelper mockResourceHelper = mockEclipse.getMockResourceHelper();
        String[] cmdLine = mockResourceHelper.lastExecCommandLine;
        assertTrue(cmdLine[0].contains("bazel"));
        assertEquals("test", cmdLine[1]);
        assertTrue(cmdLine[2].contains("test_output"));
        // there are a bunch of other flags passed as well 3-8
        assertTrue(cmdLine[8].contains("testvalue1"));
        assertTrue(cmdLine[9].contains("testvalue2"));
        assertTrue(cmdLine[10].contains("testvalue3"));
        assertEquals("--", cmdLine[11]);
        assertEquals("//projects/libs/javalib0:javalib0Test", cmdLine[12]); // $SLASH_OK: bazel path
    }

    @Test
    public void testHappySeleniumLaunch() throws Exception {
        // setup functional test env
        MockEclipse mockEclipse = createMockEnvironment("selen");
        ILaunchConfiguration launchConfig = createLaunchConfiguration("selenium", "javalib0Test");
        ILaunch launch = new MockILaunch(launchConfig);
        IProgressMonitor progress = new EclipseWorkProgressMonitor();
        addBazelCommandOutput(mockEclipse.getBazelCommandEnvironmentFactory(), 1, "test", "bazel test result");
        BazelLaunchConfigurationDelegate delegate = mockEclipse.getLaunchDelegate();

        // method under test
        delegate.launch(launchConfig, "debug", launch, progress);

        // verify
        MockResourceHelper mockResourceHelper = mockEclipse.getMockResourceHelper();
        String[] cmdLine = mockResourceHelper.lastExecCommandLine;
        assertTrue(cmdLine[0].contains("bazel"));
        assertEquals("test", cmdLine[1]);
        assertTrue(cmdLine[2].contains("test_output"));
        // there are a bunch of other flags passed as well 3-8
        assertTrue(cmdLine[8].contains("testvalue1"));
        assertTrue(cmdLine[9].contains("testvalue2"));
        assertTrue(cmdLine[10].contains("testvalue3"));
        assertEquals("--", cmdLine[11]);
        assertEquals("//projects/libs/javalib0:javalib0Test", cmdLine[12]); // $SLASH_OK: bazel path
    }

    // HELPERS

    private MockEclipse createMockEnvironment(String key) throws Exception {
        File testTempDir = tmpFolder.newFolder();

        // during test development, it can be useful to have a stable location on disk for the Bazel workspace contents
        //testTempDir = new File("/tmp/bef/bazelws"); // $SLASH_OK: sample code
        //testTempDir.mkdirs();

        TestOptions testOptions = new TestOptions().uniqueKey(key).numberOfJavaPackages(1)
                .explicitJavaTestDeps(false).addJavaBinaryRule(true);

        boolean computeClasspaths = true;
        MockEclipse mockEclipse = EclipseFunctionalTestEnvironmentFactory
                .createMockEnvironment_Imported_All_JavaPackages(testTempDir, testOptions, computeClasspaths);

        return mockEclipse;
    }

    private MockILaunchConfiguration createLaunchConfiguration(String verb, String targetName) {
        MockILaunchConfiguration testConfig = new MockILaunchConfiguration();
        testConfig.attributes.put(BazelLaunchConfigAttributes.PROJECT.getAttributeName(), "javalib0");
        testConfig.attributes.put(BazelLaunchConfigAttributes.LABEL.getAttributeName(),
            "//projects/libs/javalib0:" + targetName); // $SLASH_OK: bazel path
        if ("test".equals(verb)) {
            testConfig.attributes.put(BazelLaunchConfigAttributes.TARGET_KIND.getAttributeName(), "java_test");
        } else if ("run".equals(verb)) {
            testConfig.attributes.put(BazelLaunchConfigAttributes.TARGET_KIND.getAttributeName(), "java_binary");
        } else if ("selenium".equals(verb)) {
            testConfig.attributes.put(BazelLaunchConfigAttributes.TARGET_KIND.getAttributeName(),
                    "java_web_test_suite");
        }

        List<String> args = new ArrayList<>();
        args.add("arg1=testvalue1");
        args.add("arg2=testvalue2");
        args.add("arg3=testvalue3");
        testConfig.attributes.put(BazelLaunchConfigAttributes.INTERNAL_BAZEL_ARGS.getAttributeName(), args);

        return testConfig;
    }

    private void addBazelCommandOutput(TestBazelCommandEnvironmentFactory env, int verbIndex, String verb,
            String resultLine) {
        List<String> outputLines = new ArrayList<>();
        outputLines.add(resultLine);
        List<String> errorLines = new ArrayList<>();

        // create a matcher such that the resultLine is only returned if a command uses the specific verb
        List<MockCommandSimulatedOutputMatcher> matchers = new ArrayList<>();
        matchers.add(new MockCommandSimulatedOutputMatcher(verbIndex, verb));

        env.commandBuilder.addSimulatedOutput("launcherbuildertest", outputLines, errorLines, matchers);
    }
}
