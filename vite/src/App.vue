<template>
    <img alt="Vue logo" src="./assets/logo.png" />
    <div class="content">{{ msg }}</div>
    <input type="button" value="调用Native方法" @click="testClick" />
</template>
<script lang="ts">
export default {
    name: 'App',
    data() {
        return {
            msg: 'Hello Vue'
        }
    },
    mounted() {
        let that = this;
        that.connectWebViewJavascriptBridge((bridge) => {
            bridge.init((message, callback) => {
                that.msg = 'data1 from native: ' + message;
                if (callback) {
                    callback({ 'Web responds': 'test bridge' });
                }
            });
            bridge.registerHandler('functionInJs', (message, callback) => {
                that.msg = 'data2 from native: ' + message;
                if (callback) {
                    callback('Web Says right back aka!');
                }
            });
        });
    },
    methods: {
        testClick() {
            let that = this;
            window.WebViewJavascriptBridge.callHandler('submitFromWeb', { 'param': 'test bridge' }, (responseData) => {
                that.msg = responseData + "";
            })
        },
        connectWebViewJavascriptBridge(callback) {
            if (window.WebViewJavascriptBridge) {
                callback(WebViewJavascriptBridge);
            } else {
                document.addEventListener('WebViewJavascriptBridgeReady', () => {
                    callback(WebViewJavascriptBridge);
                }, false);
            }
        }
    }
}
</script>
<style>
#app {
    text-align: center;
}

.content {
    font-size: 20px;
    margin-top: 16px;
    margin-bottom: 32px;
}
</style>