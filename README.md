# che-editor-intellij-community

Che editor using intellij community edition


## build image
```bash
$ docker build -t che-editor-intellij .
```

## test image locally
```bash
$ docker run --rm -it -p 8080:8080 che-editor-intellij
```
Connect from Chrome/Firefox to:
http://localhost:8080/vnc.html?resize=remote&autoconnect=1

## Test With Che
[![Contribute](https://www.eclipse.org/che/contribute.svg)](http://che.openshift.io/f?url=https://raw.githubusercontent.com/che-incubator/che-editor-intellij-community/master/devfiles/workspace.yaml)

current meta.yaml
```yaml
apiVersion: v2
publisher: fbenoit
name: intellij-NOVNC
version: 2020.2.2
type: Che Editor
displayName:  IntelliJ IDEA Community Edition
title:  IntelliJ IDEA Community Edition (in browser using noVNC) as editor for Eclipse Che
description:  IntelliJ IDEA Community Edition running on the Web with noVNC
icon: https://resources.jetbrains.com/storage/products/intellij-idea/img/meta/intellij-idea_logo_300x300.png
category: Editor
repository: https://github.com/che-incubator/che-editor-intellij-community
firstPublicationDate: "2020-09-25"
spec:
  endpoints:
  # hack (use dirigible endpoint name as it is part of liveness probes)
   -  name: "dirigible"
      public: true
      targetPort: 8080
      attributes:
        protocol: http
        type: ide
        path: /vnc.html?resize=remote&autoconnect=true&reconnect=true
  containers:
   - name: intellij-novnc
     image: "quay.io/che-incubator/che-editor-intellij-community:latest"
     mountSources: true
     volumes:
         - mountPath: "/intellij-config"
           name: intellij-config     
     volumes:
         - mountPath: "/home/user/.local"
           name: intellij-local
     volumes:
         - mountPath: "/home/user/.java"
           name: intellij-java
     ports:
         - exposedPort: 8080
     memoryLimit: "2048M"
     
```     
