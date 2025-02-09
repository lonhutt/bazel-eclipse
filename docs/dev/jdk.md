## JDKs and the Bazel Eclipse Feature

We target an older version Java intentionally since we open source the feature publicly.
We want to be as inclusive as possible.
Currently the supported JDK is **11**.

### Eclipse SDK Project JDK Configuration

We like the project configuration approach because it will be automatically be applied when you import the projects.
The JDK version is set in each plugin's *MANIFEST.MF* file:

```
Bundle-RequiredExecutionEnvironment: JavaSE-17
```

### Eclipse SDK Workspace JDK Configuration

You can set the JDK version in your Workspace.
This will be the default in cases where the project does not define it (see above).
We don't recommend this approach because it will need to be applied for every developer manually.

- Navigate to menu *Eclipse* -> *Preferences* in your **outer** Eclipse
- Select *Java* -> *Compiler*
- Choose *11* for compliance level
