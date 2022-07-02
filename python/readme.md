# Android测试

## 安装[uiautomator2](https://github.com/openatx/uiautomator2)

```shell
# Since uiautomator2 is still under development, you have to add --pre to install the development version
pip3 install --pre uiautomator2
```

## 安装[weditor](https://github.com/alibaba/web-editor)

因为uiautomator是独占资源，所以当atx运行的时候uiautomatorviewer是不能用的，为了减少atx频繁的启停，我们开发了基于浏览器技术的weditor UI查看器。

```shell
pip3 install -U weditor
```
