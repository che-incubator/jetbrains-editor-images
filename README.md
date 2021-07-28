# JetBrains Projector Editor Images

Contains utilites and scripts, which allows to run JetBrains products in Eclipse Che infrastructure using [Projector](https://github.com/JetBrains/projector-server).

Projector is a server-side libraries set, that allows to run Swing applications remotely.



## Run JetBrains IDE in Eclipse Che

In order to run JetBrains IDE in Eclipse Che infrastructure, current repository contains workspace configuration, which provides the ability to do that. To run the workspace you can use either Factory Link or create the Workspace from listed Devfiles below:

##### IntelliJ IDEA Community

- Che Factory pattern:

  ```
  https://<your-che-host>/f?url=https://github.com/che-incubator/jetbrains-editor-images/raw/main/devfiles/latest/che-idea/latest.yaml
  ```

- Create the Workspace from [chectl](https://github.com/che-incubator/chectl/) (Requires clone the current repository):

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ chectl workspace:create -f devfiles/latest/che-idea/latest.yaml
  ```

- Create the Workspace from the following workspace configuration:

  ```yaml
  metadata:
    name: che-idea
  components:
    - type: cheEditor
      reference: 'https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/meta/che-idea/latest.meta.yaml'
      alias: che-idea
  apiVersion: 1.0.0
  ```

##### PyCharm Community

- Che Factory pattern:

  ```
  https://<your-che-host>/f?url=https://github.com/che-incubator/jetbrains-editor-images/raw/main/devfiles/latest/che-pycharm/latest.yaml
  ```

- Create the Workspace from [chectl](https://github.com/che-incubator/chectl/) (Requires clone the current repository):

  ```sh
  $ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
  $ chectl workspace:create -f devfiles/latest/che-pycharm/latest.yaml
  ```

- Create the Workspace from the following workspace configuration:

  ```yaml
  metadata:
    name: che-pycharm
  components:
    - type: cheEditor
      reference: 'https://raw.githubusercontent.com/che-incubator/jetbrains-editor-images/meta/che-pycharm/latest.meta.yaml'
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
$ docker run --env --rm -p 8887:8887 -it quay.io/che-incubator/che-idea
$ docker run --env --rm -p 8887:8887 -it quay.io/che-incubator/che-pycharm
```

This will run the latest supported JetBrains IDE locally on your host.

Then navigate to [http://localhost:8887](http://localhost:8887), to access the JetBrains IDE.



## Run JetBrains IDE in Docker (manual build)

In order to build the image, need to make sure, that **Docker version higher than 18.09**, since the build scripts, is using [Docker BuildKit](https://docs.docker.com/develop/develop-images/build_enhancements/).

Clone current repository and perform the following steps:

```sh
$ git clone https://github.com/che-incubator/jetbrains-editor-images && cd jetbrains-editor-images
$ ./projector.sh build
$ ./projector.sh run CONTAINER
```

The following sequence prompt user to choose the **IDE packaging** to build. Then automatically performs clonning **Projector Client** and **Projector Server** sources, build them and build Docker image. The build image name will be printed in the end of build process, which can be run pass to `run` command to start the container locally.

After that, navigate to [http://localhost:8887](http://localhost:8887), to access the JetBrains IDE.

More information is available in [Developer Guide](doc/Developer-Guide.md).



## Scripts reference

### `projector.sh`

The main entrypoint to build and run of IntelliJ-based IDEs to run in Eclipse Che environment.

Calling the

```sh
$ ./projector.sh
```

 without any parameters and commands will print the help information section:

```sh
Usage: ./projector.sh COMMAND [OPTIONS]

Projector-based container manager

Options:
  -h, --help              Display help information
  -v, --version           Display version information
  -l, --log-level string  Set the logging level ("debug"|"info"|"warn"|"error"|"fatal") (default "info")

Commands:
  build   Build an image for the particular IntelliJ-based IDE package
  run     Start a container with IntelliJ-based IDE

Run './projector.sh COMMAND --help' for more information on a command.
To get more help with the './projector.sh' check out guides at https://github.com/che-incubator/jetbrains-editor-images/tree/main/doc
```



#### `projector.sh build`

Performs build an image for particular IntelliJ-based IDE package.

Calling the

```sh
$ ./projector.sh build --help
```

will print the help information section:

```sh
Usage: ./projector.sh build [OPTIONS]

Build an image for the particular IntelliJ-based IDE package

Note, that if '--tag' or '--url' option is missed, then interactive wizard will be invoked to choose
the predefined IDE packaging from the default configuration.

Options:
  -t, --tag string              Name and optionally a tag in the 'name:tag' format for the result image
  -u, --url string              Downloadable URL of IntelliJ-based IDE package, should be a tar.gz archive
      --run-on-build            Run the container immediately after build
      --save-on-build           Save the image to a tar archive after build. Basename of --url.
      --mount-volumes [string]  Mount volumes to the container which was started using '--run-on-build' option
                                Volumes should be separated by comma, e.g. "/l/path_1:/r/path_1,/l/path_2:/r/path_2".
                                If option value is omitted, then default value is loaded.
                                Default value: $HOME/projector-user:/home/projector-user,$HOME/projector-projects:/projects
  -p, --progress string         Set type of progress output ("auto"|"plain") (default "auto")
      --config string           Specify the configuration file for predefined IDE package list (default "compatible-ide.json")
      --prepare                 Clone and build Projector only ignoring other options. Also downloads the IDE packaging
                                by the --url option. If --url option is omitted then interactive wizard is called to choose
                                the right packaging to prepare. Used when need to fetch Projector sources only, assembly
                                the binaries and download the IDE packaging.
```



#### `projector.sh run`

Starts the container with IntelliJ-based IDE locally.

Calling the

```sh
$ ./projector.sh run --help
```

will print the help information section:

```sh
Usage: ./projector.sh run CONTAINER [OPTIONS]

Start a container with IntelliJ-based IDE

Options:
      --mount-volumes [string]  Mount volumes to the container which was started using '--run-on-build' option.
                                Volumes should be separated by comma, e.g. "/l/path_1:/r/path_1,/l/path_2:/r/path_2".
                                If option value is omitted, then default value is loaded.
                                Default value: $HOME/projector-user:/home/projector-user,$HOME/projector-projects:/projects
```



### `make-release.sh`

Performes the release process for editor images. Steps performed in this shell script:

- Fetch configuration about all supported IDEs
- Perform build the docker images locally and checks whether there are an errors during build
- Creates tag and pushes to the remote

Calling the

```sh
$ ./make-release.sh --help
```

will print the help information section:

```sh
Usage: ./make-release.sh [OPTIONS]

Performs the release of editor images.

Options:
  -h, --help              Display help information
  -v, --version           Display version information
  -t, --tag string        Release tag name (e.g. "YYYYMMDD.hashId")
  -l, --log-level string  Set the logging level ("debug"|"info"|"warn"|"error"|"fatal") (default "info")
      --skip-checks       Skip pre-release checks. WARNING! Use this option if you know what you do!
```

More information is available in [Developer Guide](doc/Developer-Guide.md).



## Upstream

The code in the current repository is mainly based on the upstream [projector-docker](https://github.com/JetBrains/projector-docker) with the modifications, that allows to run JetBrains products inside Eclipse Che infractructure.



## Tested IDEs

During the manual build, it is possible to provide an optional `downloadUrl` parameter of an IDE packaging.

You can find the up-to-date list of tested IDEs here: [Compatible-IDE.md](doc/Compatible-IDE.md).



## Contributing

The guide which provides necessary information how to build different JetBrains IDEs, you can find here: [Developer-Guide.md](doc/Developer-Guide.md).