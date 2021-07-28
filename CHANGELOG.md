# Changelog
This document reflects the project's changes made after each release cycle

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

