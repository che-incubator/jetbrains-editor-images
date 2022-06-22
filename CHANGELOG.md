# Changelog
This document reflects the project's changes made after each release cycle

## [20220622]

### News

Incubator Plugin now provides ability to communicate with Machine Exec and allows to run Devfile Commands in developer components (containers).
Also, it provides ability to display Workspace Endpoints and ask user to forward opened ports in case if there is a new service is listening for incoming connections.

All featured changes are described in the following pull request [#121](https://github.com/che-incubator/jetbrains-editor-images/pull/121)

---

- Updated ubi8-minimal image to 8.6-751.1655117800 [#119](https://github.com/che-incubator/jetbrains-editor-images/pull/119)

### Added
- Open Dashboard action added to a toolbar [#118](https://github.com/che-incubator/jetbrains-editor-images/pull/118)

## [20220509]

### News

Provided ability to build the IntelliJ IDEA Community Edition and PyCharm Community Edition version 2022 and 2021.
  
Due to optimisation images build the following images will not build:

- `che-idea:2020.3.3`
- `che-idea:2020.3.2`
- `che-idea:2020.3.1`
- `che-pycharm:2020.3.4`
- `che-pycharm:2020.3.3`
- `che-pycharm:2020.3.2`
- `che-pycharm:2020.3.1`

How the new release scheme will work?

IntelliJ IDEA Community Edition released major version `2021.3`, but from the download page it allows downloading the latest version `2021.3.3`, so this version will be used for image `che-idea:2021.3` and the same approach for other major versions. This has been done for image count optimisation. There is a need to avoid a huge bunch of images, which needs to be maintained.
  
So with this update there will be a following list of images pushed to [quay.io](https://quay.io):

- `che-idea:2022.1` - with latest released version `2022.1`
- `che-idea:2021.3` - with latest released minor version `2021.3.3`
- `che-idea:2021.2` - with latest released minor version `2021.2.4`
- `che-idea:2021.1` - with latest released minor version `2021.1.3`
- `che-idea:2020.3` - with latest released minor version `2020.3.4`
- `che-pycharm:2022.1` - with latest released version `2022.1`
- `che-pycharm:2021.3` - with latest released minor version `2021.3.3`
- `che-pycharm:2021.2` - with latest released minor version `2021.2.4`
- `che-pycharm:2021.1` - with latest released minor version `2021.1.3`
- `che-pycharm:2020.3` - with latest released minor version `2020.3.5`

Further, when IntelliJ IDEA Community Edition or PyCharm Community Edition `2022.1` will release e.g. `2022.1.2`, this version will be used for `che-idea:2022.1` or `che-pycharm:2022.1` correspondingly.

Related changes made withing [#112](https://github.com/che-incubator/jetbrains-editor-images/pull/112)

---

- Updated ubi8 image to 8.5-239.1651231664 ([#113](https://github.com/che-incubator/jetbrains-editor-images/pull/113))
- Updated ubi8-minimal image to 8.5-243.1651231653 ([#106](https://github.com/che-incubator/jetbrains-editor-images/pull/106))
- Updated Projector Server sources to [847e4010](https://github.com/JetBrains/projector-client/commit/847e401026d6ce4c6bfb0c04d357066eb7f91761) ([#108](https://github.com/che-incubator/jetbrains-editor-images/pull/108))
- Updated Projector Client sources to [93ea76b1](https://github.com/JetBrains/projector-server/commit/93ea76b1f8c45068913445493df0e9d04bfef5c2) ([#108](https://github.com/che-incubator/jetbrains-editor-images/pull/108))


### Changed

- JCEF has been disabled by system property `-Dide.browser.jcef.enabled=false` ([#112](https://github.com/che-incubator/jetbrains-editor-images/pull/112))
- Update devfiles according to the latest changes ([#114](https://github.com/che-incubator/jetbrains-editor-images/pull/114))

## [20220413]

### News

- Updated base ubi-minimal image to 8.5-240.1648458092 ([#100](https://github.com/che-incubator/jetbrains-editor-images/pull/100))
- Updated Projector Server sources to [c3fa667](https://github.com/JetBrains/projector-server/commit/c3fa6678a4e3ce146799c20d28af963b8ddefe94) ([#101](https://github.com/che-incubator/jetbrains-editor-images/pull/101))
- Updated Projector Client sources to [2811e60](https://github.com/JetBrains/projector-client/commit/2811e60f3e11b9109c6670a42af70124d9b6c052) ([#99](https://github.com/che-incubator/jetbrains-editor-images/pull/99))

### Changed
- Revert changes related to IDE frame expansion ([#91](https://github.com/che-incubator/jetbrains-editor-images/pull/91))
  - Moved code that expands IDE frame to Plugin to Projector code base

## [20220221]

### News

- Integrated Projector [server](https://github.com/JetBrains/projector-server) and [client](https://github.com/JetBrains/projector-client) source code as git subtree to the repository which can be rebased with upstream repository by calling `./projector.sh rebase` command. ([#89](https://github.com/che-incubator/jetbrains-editor-images/pull/89)) Before performing the rebase there is some git configuration should be performed by adding remotes for the Projector server and client:

  ```sh
  $ git remote add upstream-projector-server https://github.com/JetBrains/projector-server
  $ git fetch upstream-projector-server master
  $ git remote add upstream-projector-client https://github.com/JetBrains/projector-client
  $ git fetch upstream-projector-client master
  ```
- Provided plugin for IntelliJ-Platform based IDEs that handles necessary operations with Devfile such as provision Run Configuration based on the Devfile commands ([#78](https://github.com/che-incubator/jetbrains-editor-images/pull/78))

### Added

- Suppress data sharing prompt for a user ([#81](https://github.com/che-incubator/jetbrains-editor-images/pull/81))
- Persist Java preferences folder ([#82](https://github.com/che-incubator/jetbrains-editor-images/pull/82))
- Add `PROJECTS_ROOT` to a trusted paths configuration ([#83](https://github.com/che-incubator/jetbrains-editor-images/pull/83))
- Use `PROJECT_SOURCE` as default working directory ([#85](https://github.com/che-incubator/jetbrains-editor-images/pull/85))
- Publish next tag for latest dev image ([#86](https://github.com/che-incubator/jetbrains-editor-images/pull/86))

### Changed

- Removed git patches ([#88](https://github.com/che-incubator/jetbrains-editor-images/pull/88))
- Sync devfiles with plugin-registry ([#80](https://github.com/che-incubator/jetbrains-editor-images/pull/80))
- Update from ubi8-minimal:8.5-218 to ubi8-minimal:8.5-230 ([#84](https://github.com/che-incubator/jetbrains-editor-images/pull/84))

### Fixed

- Provide user to passwd and group file ([#79](https://github.com/che-incubator/jetbrains-editor-images/pull/79))
- Fix default volume mount for local run ([#87](https://github.com/che-incubator/jetbrains-editor-images/pull/87))

## [20220117]

### News

- Provide sidecar-less model ([#70](https://github.com/che-incubator/jetbrains-editor-images/pull/70))
  Introduce new model, when editor in Eclipse Che environment injected to the runtime container. 
  This means that editor does not run in container where it was built. It left for local run, in docker environment.
  Sidecar-less model described in the following issue: [New workspace model to run VS Code as a Che editor](https://github.com/eclipse/che/issues/20435)
  
- Update devfiles to version 2.1 ([#71](https://github.com/che-incubator/jetbrains-editor-images/pull/71))
  With sidecar-less model workspace configuration now should be provided by version v2.1 instead v1 to run in Eclipse Che.

### Added

- Auto opening projects in Eclipse Che environment ([#75](https://github.com/che-incubator/jetbrains-editor-images/pull/75))

### Changed

- Update from ubi8-minimal:8.5-204 to ubi8-minimal:8.5-218 ([#67](https://github.com/che-incubator/jetbrains-editor-images/pull/67))
- Simplify entrypoint.sh by moving necessary code to ide-projector-launcher.sh ([#69](https://github.com/che-incubator/jetbrains-editor-images/pull/69))
- Update README.md and Developer-Guide.md to reflect latest changes ([#73](https://github.com/che-incubator/jetbrains-editor-images/pull/73))

### Fixed

- Update mechanism of syncing configuration ([#74](https://github.com/che-incubator/jetbrains-editor-images/pull/74))

## [20211213]

### News

- Update Projector sources ([#59](https://github.com/che-incubator/jetbrains-editor-images/pull/59))
  Now Projector Server points to the [JetBrains/projector-server@30f65af](https://github.com/JetBrains/projector-server/commit/30f65afc196605625f19b671e1cee1d012c8ee97) and Projector Client points to [JetBrains/projector-client@5f61189](https://github.com/JetBrains/projector-client/commit/5f6118900f2da668f0d84463025fea341da32175)
- Add configuration for IDEA 2020.3.4 ([#60](https://github.com/che-incubator/jetbrains-editor-images/pull/60))
- Updated UBI-minimal image to version 8.5-204 ([#61](https://github.com/che-incubator/jetbrains-editor-images/pull/61))

### Added

- Add per-arch support for installation of libsecret and libsecret-devel ([#50](https://github.com/che-incubator/jetbrains-editor-images/pull/50))

### Changed

- Add additional debug output to the prepare assembly step ([#48](https://github.com/che-incubator/jetbrains-editor-images/pull/48))
- Reorganise logging process in projector.sh and make-release.sh script ([#56](https://github.com/che-incubator/jetbrains-editor-images/pull/56))

## [20210728]

### Changed

- Change asset's names according to brew conventions ([#46](https://github.com/che-incubator/jetbrains-editor-images/pull/46))

## [20210727]

### Changed

- Prepare Dockerfile to build in OSBS ([#43](https://github.com/che-incubator/jetbrains-editor-images/pull/43))

## [20210723]

### News

- Removed auxiliary patch to support single host environment

- Build community images in Brew compliant way. ([#40](https://github.com/che-incubator/jetbrains-editor-images/pull/40))

  To be a part of Codeready Workspaces there are some important changes come:

  - Projector Build performed only on the host machine, **not inside the container**. To build the Projector Client and Projector Server it is only enough to have at least JDK 11 and configured environment variable `JAVA_HOME`.

  - IDE downloads to the host machine, **not inside the container**. This allows to apply caching mechanism to speedup the image build from ~5-6 minutes to 1-2 minutes.

  - Changed `compatible-ide.json` configuration by using the CDN links to have ability to cache downloaded packages.

  - [Internal] Removed build arguments from the Dockerfile. So to build the image performs as usual, by calling `./projector.sh build`.

  - Removed `--projector-only` parameter and introduced `--prepare` instead. This parameter will perform all necessary work to prepare the assembly before Docker build. So calling:

    ```sh
    $ ./projector.sh build --prepare
    ```

    will prepare Projector Server and Projector Client sources, download the IDE packaging and omit the Docker build step. Then Docker image can be simply built by calling:

    ```sh
    $ DOCKER_BUILDKIT=1 docker build --progress=auto -t <image_name> -f Dockerfile .
    ```

  - Removed `--no-projector-build` parameter. As far as Projector Server and Projector Client builds only on the host machine.

### Changed

- Bump Projector Client and Projector Server SHA1 revisions to fetch the latest upstream changes ([#37](https://github.com/che-incubator/jetbrains-editor-images/pull/37))
- Update ubi image version from 8.3-298 to 8.4-205 ([#38](https://github.com/che-incubator/jetbrains-editor-images/pull/38))

## [20210629]

### News

- First release

### Added

- Build Docker images using Projector ([#21](https://github.com/che-incubator/jetbrains-editor-images/pull/21))
- Add guide which describes how to connect to workspace from projector launcher ([#31](https://github.com/che-incubator/jetbrains-editor-images/pull/31))
- Align devfiles to new release scheme ([#34](https://github.com/che-incubator/jetbrains-editor-images/pull/34))

### Changed

- Support arbitrary projector-server version ([#30](https://github.com/che-incubator/jetbrains-editor-images/pull/30))
- Update projector client and server revision ([#29](https://github.com/che-incubator/jetbrains-editor-images/pull/29))
- Use correct version of Python 3 package ([#28](https://github.com/che-incubator/jetbrains-editor-images/pull/28))
- Unify package installation step for IDEs ([#26](https://github.com/che-incubator/jetbrains-editor-images/pull/26))
- Include socat in runtime image and synchronize ide launcher with upstream ([#24](https://github.com/che-incubator/jetbrains-editor-images/pull/24))
- Updated preferences and reorganized CMD with ENTRYPOINT section ([#23](https://github.com/che-incubator/jetbrains-editor-images/pull/23))

### Fixed

- Use Jsoup for parsing html representation of markdown documents ([#27](https://github.com/che-incubator/jetbrains-editor-images/pull/27))
- Set IDE window maximized at initialization step ([#25](https://github.com/che-incubator/jetbrains-editor-images/pull/25))
- Single host environment support ([#22](https://github.com/che-incubator/jetbrains-editor-images/pull/22))

