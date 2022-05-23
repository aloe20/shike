import router from '@system.router';

@Entry
@Component
struct Index {
  build() {
    Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Center, justifyContent: FlexAlign.Center }) {
      Image($r("app.media.icon"))
        .objectFit(ImageFit.Auto)
        .backgroundColor('#FFFF0000')
        .height(360)
        .padding(16)
      Text('Hello World')
        .fontSize(50)
        .fontWeight(FontWeight.Bold)
      Button('Next')
        .fontSize(40)
        .fontWeight(500)
        .width(280)
        .height(60)
        .onClick(() => {
          router.push({ uri: 'pages/details' })
        })
    }
    .width('100%')
    .height('100%')
  }
}