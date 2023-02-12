## JetBrains Projector Editor Images - aarch64 support (dev)
The building for aarch64 is not official support, but you can manual build it yourself in your local aarch64 machine.

There are two parts needed manauly change before building JetBrains IDE.

###  Change 1: devspaces-machineexec aarch64 support (manual build)

manual build `machineexec` first:

```
git clone https://github.com/redhat-developer/devspaces-images.git && cd devspaces-images
cd devspaces-machineexec
docker build --no-cache -t eclipse/che-machine-exec -f build/dockerfiles/Dockerfile .
```
then change `build/dockerfiles/machine-exec-provider.Dockerfile`:

```
# FROM quay.io/devspaces/machineexec-rhel8:next as machine-exec
FROM eclipse/che-machine-exec:latest as machine-exec
....
```

Reference: https://github.com/redhat-developer/devspaces-images/tree/devspaces-3-rhel-8/devspaces-machineexec

### Change 2: asset-required-rpms.txt
change `asset-required-rpms.txt` file:

```
https://rpmfind.net/linux/centos/8-stream/BaseOS/aarch64/os/Packages/libsecret-devel-0.18.6-1.el8.aarch64.rpm libsecret
```
### Finally build JetBrains IDE
```
./projector.sh build
./projector.sh run CONTAINER
```
After that, navigate to http://localhost:8887, to access the JetBrains IDE.


