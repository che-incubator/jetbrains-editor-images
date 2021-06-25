# End-User Guide

This document contains scenarios and descriptions of main flows for end users.



## Connect to a workspace using Projector Electron Launcher

This guide will provide the neccessary steps for the end-user to connect to running Eclipse Che workspace using Projector Electron Launcher.



##### Step 1. Create a workspace

Open [OpenShift Workspaces](https://workspaces.openshift.com/), make sure you are logged in and see CodeReady Workspaces Dashboard.

![CodeReady Workspaces - Dashboard](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/crw-dashboard.jpg)

Grab particular Devfile [from the repository](https://github.com/che-incubator/jetbrains-editor-images/tree/main/devfiles) and paste the URL for Devfile as Devfile template and create the Workspace.

![CodeReady Workspaces - New Workspace](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/crw-create-workspace.jpg)

Make sure, that your workspace successfully started and run.

![CodeReady Workspaces - Workspace List](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/crw-workspace-list.jpg)



##### Step 2. Perform oc login and port forwarding

Due to security restrictions, it is not possible to connect to the running Workspace without authentication from third-party application. So the only option to get access besides openning Workspace in browser window is to use OpenShift port forwarding. To perform this operation go to [Developer Sandbox](https://developers.redhat.com/developer-sandbox) and click to **Get started in the Sandbox**.

![CodeReady Workspaces - DevSandbox](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/dev-sandbox.jpg)

After authentication step you will be able to see OpenShift Console with your running Workspace in previous step.

![CodeReady Workspaces - DevSandbox Console](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/dev-sandbox-console.jpg)

Click to the right corner, on you login name, choose **Copy login command**.

![CodeReady Workspaces - DevSandbox Login](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/dev-sandbox-login.jpg)

Follow the steps in the wizard and obtain oc login command template with the token. Execute this command in your local terminal to authenticate your session in OpenShift. After that you will be able to execute command to obtain running pods:

```
$ oc login --token=sha256~token --server=https://api.sandbox.x8i5.p1.openshiftapps.com:6443
$ oc get pods
```

Now it is time to establish port forwarding from remote pod. JetBrains's IDE runs on 8887 port using Projector.

```
$ oc port-forward [pod from previous command] 8887:8887
```

In order of success, you will get response for port forwarding:

```
Forwarding from 127.0.0.1:8887 -> 8887
Forwarding from [::1]:8887 -> 8887
```



##### Step 3. Connect to running Workspace using Projector Electron Launcher

Projector provides electron based client which allows to connect to remote Projector Server instance. To obtain the Projector Electron Launcher navigate to the document section: [Downloading](https://jetbrains.github.io/projector-client/mkdocs/latest/ij_user_guide/accessing/#client-app-launcher). Download the Projector Launcher.

In the Projector Launcher provide URL for the connection. As far as we are mapping remote 8887 port to local one, it is only need to enter [http://localhost:8887](http://localhost:8887) 

![CodeReady Workspaces - Projector Launcher](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/projector-launcher.jpg)

After connection, you will be able to open the remote Workspace in Projector Launcher. Where you can create a project and perform your development flow.

![CodeReady Workspaces - Projector Launcher with Workspace](https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/media/images/projector-launcher-ide.jpg)