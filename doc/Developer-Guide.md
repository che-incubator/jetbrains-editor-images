# Development Guide

Current document contains tip to help and understand the first steps that needed to be performed to build custom images with different JetBrains IDEs.



## Prerequisities

In order to build locally Docker container, make sure, that **Docker version is 18.09** or higher, since the build scripts use [Docker BuildKit](https://docs.docker.com/develop/develop-images/build_enhancements/).

> For macOS users, it is needed to ensure that `gnu-getopt` is installed in the system.



## Build JetBrains IDE in Docker using custom distribution

In order to build the Docker image with the custom JetBrains IDE distribution, there is an ability to pass particular distribution through the `--tag` and `--url` parameters to `./projector.sh build`.

The complete command looks:

```sh
./projector build --tag name:tag --url downloadUrl
```

This command will perform the build of `name:tag` image with the given IDE distribution provided by `downloadUrl`. This download URL should point to `tar.gz` packaging. For example, navigate to [download page](https://www.jetbrains.com/idea/download/#section=linux), click "Other versions" and copy the link for the IDE packaging. Please ensure that you select `tar.gz` **with JBR**, not without.

Then, it is only need to call `./projector.sh run` command to run the container locally:

```sh
./projector.sh run name:tag
```

After that, navigate to [http://localhost:8887](http://localhost:8887), to access the JetBrains IDE.



##### Example of usage

Below you can find a few examples how to build different JetBrains IDEs using `--url` parameter:

- Build the Docker image with **WebStorm 2020.3.3**

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ ./projector.sh build --tag che-webstorm:latest --url https://download.jetbrains.com/webstorm/WebStorm-2020.3.3.tar.gz
  $ ./projector.sh run che-webstorm:latest
  ```

- Build the Docker image with **PyCharm Community 2020.3.5**

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ ./projector.sh build --tag che-pycharm:latest --url https://download.jetbrains.com/python/pycharm-community-2020.3.5.tar.gz
  $ ./projector.sh run che-pycharm:latest
  ```

- Build the Docker image with **IntelliJ IDEA Ultimate 2020.2.2**

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ ./projector.sh build --tag che-idea-ultimate:latest --url https://download.jetbrains.com/idea/ideaIU-2020.2.2.tar.gz
  $ ./projector.sh run che-idea-ultimate:latest
  ```

After performing any build scenario, which provided above, navigate to [http://localhost:8887](http://localhost:8887), to access the JetBrains IDE.



## Build JetBrains IDE in Docker based on the predefined configuration

It is also available to select predefined IDE distribution from the wizard during image build. When `--tag` or `--url` parameters are ommitted, then select wizard is called. Predefined configurations, that are currently supported are provided in [compatible-ide.json](../compatible-ide.json).

The complete command looks:

```sh
./projector.sh build
```

This command will prompt user to select the IDE packaging to build:

```sh
[info] Select the IDE package to build (default is 'IntelliJ IDEA Community'):
       1) IntelliJ IDEA Community
       2) PyCharm Community
```

Then prompt to choose IDE packaging version to build (in case of selecting default choice):

```sh
[info] Select the IDE package version to build (default is '2020.3.3'):
       1) 2020.3.3
       2) 2020.3.2
       3) 2020.3.1
```



## Use Docker image with JetBrains IDE in Eclipse Che

The next step will be tag the resulting image by adding your namespace using the following command pattern:

```sh
$ docker tag <containerName>:latest <username>/<containerName>:latest
$ docker push <username>/<containerName>:latest
```

for example:

```sh
$ docker tag che-pycharm:latest superuser/che-pycharm:latest
$ docker push superuser/che-pycharm:latest
```

> Make sure, that you are logged in using `$ docker login` command to be able to push an image to your namespace.

Now this image can be used as Che Editor in Eclipse Che. To achieve these there are two yaml configurations needs to be created:

- `workspace.yaml` – workspace configuration. Do not forged to provide correct url to the `meta.yaml` file:

  ```yaml
  metadata:
    name: <workspace name goes here>
  components:
    - type: cheEditor
      reference: '<url for the below meta.yaml goes here>'
  apiVersion: 1.0.0
  ```

- `meta.yaml` – Che Editor configuration. Do not forget to replace `<username>` with the user name where image is pushed to and provide the pushed image in `image` section:

  ```yaml
  apiVersion: v2
  publisher: <username>
  name: jetbrains-ide
  version: latest
  type: Che Editor
  displayName:  JetBrains IDE
  title:  JetBrains IDE as Editor for Eclipse Che
  description:  JetBrains IDE running using Projector
  icon: https://www.jetbrains.com/apple-touch-icon.png
  category: Editor
  repository: https://github.com/che-incubator/jetbrains-editor-images
  firstPublicationDate: "2021-04-10"
  spec:
    endpoints:
     -  name: "intellij"
        public: true
        targetPort: 8887
        attributes:
          protocol: http
          type: ide
          path: /projector-client/index.html?backgroundColor=434343&wss
    containers:
     - name: jetbrains-ide
       image: "<username>/<containerName>:latest"
       mountSources: true
       volumes:
           - mountPath: "/home/projector-user"
             name: projector-user
       ports:
           - exposedPort: 8887
       memoryLimit: "4096M"
  ```



## Provision activation code for offline usage

Activation code for offline usage is a file with license code, that can be retrieved from the license management section of your JetBrains Account, for the license that is assigned to you. When you purchase a personal subscription or are assigned a commercial subscription by your organization, you'll be sent an email prompting you to create a JetBrains Account that becomes connected with the license.

> Note: if you are using an activation code to activate a Product, you will need to generate a new activation code and apply it to your product each time the subscription is renewed.

> IntelliJ IDEA Community and PyCharm Community doesn't require this procedure, as they are free.

Activation code can be retieved from JetBrains account:

![jetbrains-account](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/jetbrains-account.jpg)

JetBrains provides zip archive with two types of activation code. `<License ID> - for 2018.1 or later.txt` file should be used.

![activation-code](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/activation-code.jpg)

### Provision activation code to Docker container

When you run the Docker container on your local machine, it is possible to provide the activation code for offline usage to register the JetBrains IDE. Docker image contains scripts, which perform this operation. The only you need to map the activation code, that locates on your host as volume mount into Docker container, for example:

```sh
$ docker run --env DEV_MODE=true --rm -p 8887:8887 -v <path to text file on your host>:/tmp/<product>.key -it <containerName>
```

Mount path `/tmp/<product>.key` should be used according to the JetBrains product used in the Docker image. For example:

- `/tmp/idea.key`
- `/tmp/pycharm.key`
- `/tmp/webstorm.key`
- `/tmp/phpstorm.key`
- `/tmp/goland.key`



### Provision activation code to Eclipse Che

Since Eclipse Che runs in Kubernetes environment, it is possible to provision the activation code for offline usage throught the Kubernetes Secrets.

> To understand what is Kubernetes Secret and how to operate with the last one, see: [Kubernetes Documentation](https://kubernetes.io/docs/concepts/configuration/secret/)

Let's create a Kubernetes Secret, that will instruct Eclipse Che to mount the activation code into container which is based on the JetBrains specific product:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: jetbrains-offline-activation-code
  labels:
    app.kubernetes.io/component: workspace-secret
    app.kubernetes.io/part-of: che.eclipse.org
  annotations:
    che.eclipse.org/automount-workspace-secret: 'false'
    che.eclipse.org/mount-path: /tmp/
    che.eclipse.org/mount-as: file
data:
  idea.key: <base64 encoded data content here>
  pycharm.key: <base64 encoded data content here>
  webstorm.key: <base64 encoded data content here>
  phpstorm.key: <base64 encoded data content here>
  goland.key: <base64 encoded data content here>
```

Annotation `automount-workspace-secret` with the `false` value disables the mounting process until it is explicitly requested in a workspace component using the `automountWorkspaceSecrets:true` property. So, to mount the activation codes into a Workspace, workspace configuration should be updated by adding `automountWorkspaceSecrets:true` property:

```yaml
metadata:
  name: <workspace name goes here>
components:
  - type: cheEditor
    automountWorkspaceSecrets: true
    reference: '<url for the below meta.yaml goes here>'
apiVersion: 1.0.0
```

