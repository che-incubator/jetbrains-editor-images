# JetBrains Projector Editor Images

Contains utilites and scripts, which allows to run JetBrains products in Eclipse Che infrastructure using [Projector](https://github.com/JetBrains/projector-server).

Projector is a server-side libraries set, that allows to run Swing applications remotely.



## Run JetBrains IDE in Eclipse Che

In order to run JetBrains IDE in Eclipse Che infrastructure, current repository contains workspace configuration, which provides the ability to do that. To run the workspace you can use either Factory Link or create the Workspace from listed Devfiles below:

##### IntelliJ IDEA Community

- Che Factory pattern:

  ```
  https://<your-che-host>/f?url=https://github.com/che-incubator/jetbrains-editor-images/raw/master/devfiles/che-idea.yaml
  ```

- Create the Workspace from [chectl](https://github.com/che-incubator/chectl/) (Requires clone the current repository):

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ chectl workspace:create -f devfiles/che-idea.yaml
  ```

- Create the Workspace from the following workspace configuration:

  ```yaml
  metadata:
    name: che-idea
  components:
    - type: cheEditor
      reference: 'https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/meta/che-idea-latest.meta.yaml'
      alias: che-idea
  apiVersion: 1.0.0
  ```

##### PyCharm Community

- Che Factory pattern:

  ```
  https://<your-che-host>/f?url=https://github.com/che-incubator/jetbrains-editor-images/raw/master/devfiles/che-pycharm.yaml
  ```

- Create the Workspace from [chectl](https://github.com/che-incubator/chectl/) (Requires clone the current repository):

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ chectl workspace:create -f devfiles/che-pycharm.yaml
  ```

- Create the Workspace from the following workspace configuration:

  ```yaml
  metadata:
    name: che-pycharm
  components:
    - type: cheEditor
      reference: 'https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/meta/che-pycharm-latest.meta.yaml'
      alias: che-pycharm
  apiVersion: 1.0.0
  ```



## Run JetBrains IDE in Docker

In order to run JetBrains IDE in Docker, it is enough to pull one from two images, which publicitly deployed to [quay.io](https://quay.io/).

At this moment it is **community** version for **IntelliJ IDEA** and **PyCharm**.

```sh
$ docker pull quay.io/che-incubator/che-idea
$ docker pull quay.io/che-incubator/che-pycharm
```

Then it is enough to run the particular container, passing the run options, as shown below:

```sh
$ docker run --env DEV_MODE=true --rm -p 8887:8887 -it quay.io/che-incubator/che-idea
$ docker run --env DEV_MODE=true --rm -p 8887:8887 -it quay.io/che-incubator/che-pycharm
```

This will run Projector Server with the particular JetBrains IDE locally on your host.

Then navigate to [http://localhost:8887](http://localhost:8887), to access the JetBrains IDE.



## Run JetBrains IDE in Docker (manual build)

In order to build the image, need to make sure, that **Docker version higher than 18.09**, since the build scripts, is using [Docker BuildKit](https://docs.docker.com/develop/develop-images/build_enhancements/).

Clone current repository and perform the following steps:

```sh
$ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
$ ./clone-projector.sh
$ ./build-container.sh
$ ./run-container.sh
```

The following sequence will clone **Projector Client** and **Projector Server**, build the default image with **IntelliJ IDEA Community** and run it.

After that, navigate to [http://localhost:8887](http://localhost:8887), to access the JetBrains IDE.



## Scripts reference

### `clone-projector.sh`

Clones the Projector Client and Projector Server into the following location:

- `../projector-client`

- `../projector-server`

Links Projector Client with Projector Server by adding property `useLocalProjectorClient=true` to `projector-server/local.properties`.

### `build-container.sh [containerName [downloadUrl]]`

Compiles the Projector inside Docker and builds a Docker container locally.

### `build-container-dev.sh [containerName [downloadUrl]]`

Compiles the Projector on user host machine and builds a Docker container locally. **Requires configured JDK 11**.

### `run-container.sh [containerName]`

Runs the Docker container.

Starts the Projector server and hosts web client files on port 8887.

### `run-container-mounted.sh [containerName]`

Runs the Docker container and mounts `~/projector-user` hosts directory as home directory, and `~/projector-projects` as projector directory in the container, so settings and projects can be stored between container restarts.

Starts the Projector server and hosts web client files on port 8887.



## Upstream

The code in the current repository is mainly based on the upstream [projector-docker](https://github.com/JetBrains/projector-docker) with the modifications, that allows to run JetBrains products inside Eclipse Che infractructure.



## Tested IDEs

During the manual build, it is possible to provide an optional `downloadUrl` parameter of an IDE packaging.

You can find the up-to-date list of tested IDEs here: [Compatible-IDE.md](doc/Compatible-IDE.md).



## Contributing

The guide which provides necessary information how to build different JetBrains IDEs, you can find here: [Developer-Guide.md](doc/Developer-Guide.md).