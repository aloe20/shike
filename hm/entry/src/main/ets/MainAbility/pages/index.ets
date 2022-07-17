import router from '@system.router';
@Entry
@Component
struct Index {
  @State message: string = 'Hello World!'

  build() {
    Column() {
      Text(this.message)
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
    .width('100%').height('100%')
  }
}