> Current file contains tips to help and understand first steps that needed to be performed to build custom images with different JetBrains products, e.g. IntelliJ Idea Ultimate Edition, WebStorm, etc.

## Introduction

1. [Prerequisities](#prerequisities)
   1. [Use built image](#use-built-image)
   2. [Build image](#build-image)
2. [The repository structure](#the-repository-structure)
3. [Supported IDEs](#supported-ides)
4. [Quick start](#quick-start)
   1. [Build IntelliJ Idea Community Edition](#build-intellij-idea-community-edition)
   2. [Build IntelliJ Idea Ultimate Edition](#build-intellij-idea-ultimate-edition)
   3. [Build WebStorm](#build-webstorm)
   4. [Manipulation with build version](#manipulation-with-build-version)
   5. [Provision activation code for offline usage](#provision-activation-code-for-offline-usage)
   6. [JetBrains product name mapping](#jetbrains-product-name-mapping)



## Prerequisities

##### Use built image

- A running instance of Eclipse Che `>= 7.15`. To install an instance of Eclipse Che, see [Installing Che](https://www.eclipse.org/che/docs/che-7/installation-guide/installing-che/).
  - Since some kubernetes configuration can be applied and kubernetes secrets use annotation `automount-workspace-secret` which is supported on Eclipse Che version `7.15` and higher. For the following information see [Mounting a secret as a file or an environment variable into a workspace container](https://www.eclipse.org/che/docs/che-7/end-user-guide/mounting-a-secret-as-a-file-or-an-environment-variable-into-a-workspace-container/)

##### Build image

- Docker `>= 18`. Latest stable version.



## The repository structure

Current repository has the following structure described below:

- `config` – contains configuration files for necessary tools and packages needed for the image build:
  - `etc/default/jetbrains` – contains xml-based files with preliminary configuration, such as default project path or terminal specific preferences. Needed for all types of JetBrains products. In resulting image these configuration files move to `${<PRODUCT_NAME>_PROPERTIES}/config/options` folder. 
  - `etc/tigervnc` – contains configuration files needed for vnc-related tools.
  - `etc/supervisord.conf` – configuration file needed for container supervisor. Entrypoint for the third-party scripts and tools that has to be run in container.
- `devfiles` – sample devfile that from which a workspace can be created:
  - `meta.yaml` – is the Che Editor descriptor, that is used to instruct Che that workspace component is treated as Che Editor.
- `doc` – developer documentation.
- `kubernetes` – resources needed for kebernetes based configuration:
  - `offline-activation-secret.yaml` – kubernetes resource which provide an ability to mount activation code for the offline usage. Doesn't need for using IntelliJ Idea Community Edition. Used only if one of the following products is used:
    - IntelliJ Idea Ultimate Edition
    - WebStorm
- `scripts` – service scripts needed to run and support correct workflow for the end user:
  - `entrypoint.sh` – Dockerfile entrypoint. Sets correct system permissions.
  - `license-configuration.sh` – script that translates mounted activation code for the offline usage into specified binary format that is suitable for JetBrains product, e.g. IntelliJ Idea Ultimate Edition, etc. **Doesn't invocate when docker built based on Intellij Idea Community Edition.**
  - `preliminary-configuration.sh` – script that performs preliminary configuration of JetBrains product.
  - `prevent-idle-timeout.sh` – script that prevent idling workspace. As the lack of developed plugins for the JetBrains products, there is a need to track activity via `sh` scripts to prevent workspace stop.
- `Dockerfile` – Docker file to built image with JetBrains product, e.g. IntelliJ Idea Community Edition, IntelliJ Idea Ultimate Edition, WebStorm, etc.



## Support IDEs

At this moment there are several JetBrains products available to build into docker image to use as Che Editor. The following list is provided below:

- IntelliJ Idea Community Edition (**by default**)
- IntelliJ Idea Ultimate Edition
- WebStorm

The following list of JetBrains product that is planned to be supported:

- GoLand
- PyCharm

Image for IntelliJ Idea Community Edition is built and published to Quay.io [repository](https://quay.io/repository/che-incubator/che-editor-intellij-community).

For the other JetBrains products there are only instructions provided how end user can build and use these images as Che Editor. List of supported JetBrains products will be updated during support add.

By default version **2020.2.3** is used for the all products. Version can be specified during build process via `--build-arg` property.

Note, JetBrains produts that requires use activation code for offline usage should have version at least **2018.1 or later**.



## Quick start

This section describes the several ways how to build images with specific JetBrains product.



##### Build IntelliJ Idea Community Edition

To build IntelliJ Idea Community Edition it is enough to call the following command inside repository folder:

```
$ docker build -t idea-ic .
```

> Optionally, you can provide build argument:
>
> ```
> PRODUCT_NAME=ideaIC
> ```
>
>  but it is uneccessary. If no `--build-arg` was passed, `ideaIC` is used instead.
>
> The whole command will look like:
>
> ```
> $ docker build -t idea-ic --build-arg PRODUCT_NAME=ideaIC .
> ```

This command will build image with **2020.2.3** version by default. Then it is enough to tag result image and push tagged image to your repository:

```
$ docker tag idea-ic:latest <username>/idea-ic:latest
$ docker push <username>/idea-ic:latest
```

Now this image can be used as Che Editor. To achive this there are two yaml configurations needs to be created:

- `workspace.yaml` – workspace configuration. Do not forged to provide correct url to the `meta.yaml` file:

  ```
  metadata:
    name: che-ideaic
  components:
    - type: cheEditor
      reference: '<url for the meta.yaml goes here>'
      alias: ideaic-editor
  apiVersion: 1.0.0
  ```

- `meta.yaml` – Che Editor configuration. Do not forget to replace `<username>` with the user name where image is pushed to:

  ```
  apiVersion: v2
  publisher: <username>
  name: ideaic-NOVNC
  version: 2020.2.3
  type: Che Editor
  displayName:  IntelliJ IDEA Community Edition
  title:  IntelliJ IDEA Community Edition (in browser using noVNC) as editor for Eclipse Che
  description:  IntelliJ IDEA Community Edition running on the Web with noVNC
  icon: https://resources.jetbrains.com/storage/products/intellij-idea/img/meta/intellij-idea_logo_300x300.png
  category: Editor
  repository: https://github.com/che-incubator/che-editor-intellij-community
  firstPublicationDate: "2020-10-27"
  spec:
    endpoints:
     -  name: "intellij"
        public: true
        targetPort: 8080
        attributes:
          protocol: http
          type: ide
          path: /vnc.html?resize=remote&autoconnect=true&reconnect=true
    containers:
     - name: ideaic-novnc
       image: "<username>/idea-ic:latest"
       mountSources: true
       volumes:
           - mountPath: "/JetBrains/ideaIC"
             name: ideaic-configuration
       ports:
           - exposedPort: 8080
       memoryLimit: "2048M"
  
  ```



As an example showed in the picture: 

![create-workspace-from-devfile](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/create-workspace-from-devfile.jpg)

Click `Create & Open` button and when workspace starts up you'll observe a workspace with IntelliJ Idea Community Edition:

![intellij-idea-community-edition](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/intellij-idea-community-edition.jpg)

##### Build IntelliJ Idea Ultimate Edition

To build Intellij Idea Ultimate Edition additional parameter should be passed through build argument:

```
PRODUCT_NAME=ideaIU
```

> Note, that product name should passed in the same way how it is provided in current example. To see mapping JetBrains product to its product name see section: [JetBrains product name mapping](#jetbrains-product-name-mapping)

Build command will look like this:

```
$ docker build -t idea-iu --build-arg PRODUCT_NAME=ideaIU .
```

This command will build image with **2020.2.3** version by default. Built image can be tagged and pushed to user repository the same way how it is described in previous section:

```
$ docker tag idea-iu:latest <username>/idea-iu:latest
$ docker push <username>/idea-iu:latest
```

The next step is to provision activation code for offline usage. To be able to use IntelliJ Idea Ultimate Edition with registered license. See section: [Provision activation code for offline usage](#provision-activation-code-for-offline-usage)

Now it is time to create workspace with the following `workspace.yaml` and `meta.yaml`:

- `workspace.yaml` – workspace configuration. Do not forged to provide correct url to the `meta.yaml` file:

  ```
  metadata:
    name: che-ideaiu
  components:
    - type: cheEditor
      reference: '<url for the meta.yaml goes here>'
      alias: ideaiu-editor
      automountWorkspaceSecrets: true
  apiVersion: 1.0.0
  ```

  > Note, that in current workspace definition there is a new property: `automountWorkspaceSecrets: true`. This property instructs Eclipse Che to provision secrets into specific component. In our case into Che Editor based on IntelliJ Idea Ultimate Edition. This parameter is **mandatory** needed to successfully register IDE with activation code for offline usage.

- `meta.yaml` – Che Editor configuration. Do not forget to replace `<username>` with the user name where image is pushed to:

  ```
  apiVersion: v2
  publisher: <username>
  name: ideaIU-NOVNC
  version: 2020.2.3
  type: Che Editor
  displayName:  IntelliJ IDEA Ultimate Edition
  title:  IntelliJ IDEA Ultimate Edition (in browser using noVNC) as editor for Eclipse Che
  description:  IntelliJ IDEA Ultimate Edition running on the Web with noVNC
  icon: https://resources.jetbrains.com/storage/products/intellij-idea/img/meta/intellij-idea_logo_300x300.png
  category: Editor
  repository: https://github.com/che-incubator/che-editor-intellij-community
  firstPublicationDate: "2020-10-27"
  spec:
    endpoints:
     -  name: "intellij"
        public: true
        targetPort: 8080
        attributes:
          protocol: http
          type: ide
          path: /vnc.html?resize=remote&autoconnect=true&reconnect=true
    containers:
     - name: ideaiu-novnc
       image: "<username>/idea-iu:latest"
       mountSources: true
       volumes:
           - mountPath: "/JetBrains/ideaIU"
             name: ideaiu-configuration
       ports:
           - exposedPort: 8080
       memoryLimit: "2048M"
  ```



As an example showed in the picture:

![create-workspace-from-devfile-ideiu](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/create-workspace-from-devfile-ideiu.jpg)

Click `Create & Open` button and when workspace starts up you'll observe a workspace with IntelliJ Idea Ultimate Edition:

![intellij-idea-ultimate-edition](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/intellij-idea-ultimate-edition.jpg)

##### Build WebStorm

The process of build is similar as build IntelliJ Idea Ultimate Edition except of using different build argument:

```
PRODUCT_NAME=WebStorm
```

> Note, that product name should passed in the same way how it is provided in current example. To see mapping JetBrains product to its product name see section: [JetBrains product name mapping](#jetbrains-product-name-mapping)

Build command will look like this:

```
$ docker build -t webstorm --build-arg PRODUCT_NAME=WebStorm .
```

This command will build image with **2020.2.3** version by default. Built image can be tagged and pushed to user repository the same way how it is described in previous section:

```
$ docker tag webstorm:latest <username>/webstorm:latest
$ docker push <username>/webstorm:latest
```

The next step is to provision activation code for offline usage. To be able to use WebStorm with registered license. See section: [Provision activation code for offline usage](#provision-activation-code-for-offline-usage)

Now it is time to create workspace with the following `workspace.yaml` and `meta.yaml`:

- `workspace.yaml` – workspace configuration. Do not forged to provide correct url to the `meta.yaml` file:

  ```
  metadata:
    name: che-webstorm
  components:
    - type: cheEditor
      reference: '<url for the meta.yaml goes here>'
      alias: webstorm-editor
      automountWorkspaceSecrets: true
  apiVersion: 1.0.0
  ```

  > Note, that in current workspace definition there is a new property: `automountWorkspaceSecrets: true`. This property instructs Eclipse Che to provision secrets into specific component. In our case into Che Editor based on IntelliJ Idea Ultimate Edition. This parameter is **mandatory** needed to successfully register IDE with activation code for offline usage.

- `meta.yaml` – Che Editor configuration. Do not forget to replace `<username>` with the user name where image is pushed to:

  ```
  apiVersion: v2
  publisher: <username>
  name: webstorm-NOVNC
  version: 2020.2.3
  type: Che Editor
  displayName:  WebStorm
  title:  WebStorm (in browser using noVNC) as editor for Eclipse Che
  description:  WebStorm running on the Web with noVNC
  icon: https://resources.jetbrains.com/storage/products/webstorm/img/meta/webstorm_logo_300x300.png
  category: Editor
  repository: https://github.com/che-incubator/che-editor-intellij-community
  firstPublicationDate: "2020-10-27"
  spec:
    endpoints:
     -  name: "intellij"
        public: true
        targetPort: 8080
        attributes:
          protocol: http
          type: ide
          path: /vnc.html?resize=remote&autoconnect=true&reconnect=true
    containers:
     - name: webstorm-novnc
       image: "<username>/webstorm:latest"
       mountSources: true
       volumes:
           - mountPath: "/JetBrains/WebStorm"
             name: webstorm-configuration
       ports:
           - exposedPort: 8080
       memoryLimit: "2048M"
  ```



As an example showed in the picture:

![create-workspace-from-devfile-webstorm](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/create-workspace-from-devfile-webstorm.jpg)

Click `Create & Open` button and when workspace starts up you'll observe a workspace with WebStorm:

![webstorm](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/webstorm.jpg)

##### Manipulation with build version

It is possible to specify version of JetBrains product through the build argument by adding:

```
PRODUCT_VERSION=2020.2.3
```

The build command will look like:

```
$ docker build -t <image name> --build-arg PRODUCT_NAME=<product name> --build-arg PRODUCT_VERSION=2020.2.3 .
```

To build IntelliJ Idea Community Edition with specified version, build command will look like:

```
$ docker build -t idea-ic --build-arg PRODUCT_VERSION=2020.2.3 .
```



##### Provision activation code for offline usage

Activation code for offline usage is a file with license code, that can be retrieved from the license management section of your JetBrains Account, for the license that is assigned to you. When you purchase a personal subscription or are assigned a commercial subscription by your organization, you'll be sent an email prompting you to create a JetBrains Account that becomes connected with the license.

> Note: if you are using an activation code to activate a Product, you will need to generate a new activation code and apply it to your product each time the subscription is renewed.

Activation code can be retieved from JetBrains account:

![jetbrains-account](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/jetbrains-account.jpg)

JetBrains provides zip archive with two types of activation code. `<License ID> - for 2018.1 or later.txt` file should be used.

![activation-code](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/activation-code.jpg)

No it's time to provision activation code for offline usage into Che. This procedure is performed through the Kubernetes Secrets.

> To understand what is Kubernetes Secret and how to operate with the last one, see: [Kubernetes Documentation](https://kubernetes.io/docs/concepts/configuration/secret/)

Let's create a Kubernetes Secret, that will instruct Che to mount activation code into container based on JetBrains specific product:

```
apiVersion: v1
kind: Secret
metadata:
  name: <secret name here>
  labels:
    app.kubernetes.io/component: workspace-secret
    app.kubernetes.io/part-of: che.eclipse.org
  annotations:
    che.eclipse.org/automount-workspace-secret: 'false'
    che.eclipse.org/mount-path: /tmp/
    che.eclipse.org/mount-as: file
data:
  <product name here (ideaIU or WebStorm)>.key: <base64 encoded data content here>

```

- `<secret name here>` – section that describes secret name. It might has different name, e.g. `ideaiu-offline-activation-code`. Secret name **should** be provided in lowercase only.
- `<product name here (ideaIU or WebStorm)>` – should be replaced with JetBrains product name. See section [JetBrains product name mapping](#jetbrains-product-name-mapping).
- `<base64 encoded data content here>` – activation code content that is encoded in base64.

Annotation `automount-workspace-secret` with the `false` value disables the mounting process until it is explicitly requested in a devfile component using the `automountWorkspaceSecrets:true` property. As you could observer in workspace.yaml above. This is default behavior to avoid mount activation code into every container except specific ones that have to work with it.

As the result, in Che Editor file with activation code for offline usage will be mounted to path `/tmp/ideaIU.key` or `/tmp/WebStorm.key`, etc. Based on you type of build. 

IntelliJ Idea Community Edition doesn't require this procedure. This has to be done for JetBrains products that need to be registered.



###### Create secret in Kubernetes Dashboard

To create secret in Kubernetes, open the Dashboard (if you prefer to create secrets through UI), choose particular namespace (step 1), navigate to Secret section (step 2) and click plus button (step 3):

![kubernetes-create-secret](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/kubernetes-create-secret.jpg)

Provide secret content and click `Upload` button:

![kubernetes-create-secret-step-2](https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/media/images/kubernetes-create-secret-step-2.jpg)

Command line equivalent is:

```
$ kubectl create -f </path/to/ideaiu-offline-activation-code.yaml> --namespace <your namespace>
```



## JetBrains product name mapping

In the following section provided mapping used internally between JetBrains product and product name during image build.

| JetBrains Product               | PRODUCT_NAME |
| :------------------------------ | ------------ |
| IntelliJ Idea Community Edition | ideaIC       |
| IntelliJ Idea Ultimate Edition  | ideaIU       |
| WebStorm                        | WebStorm     |

