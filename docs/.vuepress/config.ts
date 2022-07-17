import { defaultTheme, defineUserConfig } from 'vuepress'
import type { DefaultThemeOptions } from 'vuepress'
import { path } from '@vuepress/utils'

export default defineUserConfig({
  // 站点配置
  lang: 'zh-CN',
  title: '文档',
  description: 'Just playing around',
  port: 8090,
  base: '/blog/',
  dest: 'public/blog',
  // 主题和它的配置
  //theme: path.resolve(__dirname, './theme'),
  head: [
    [
      'link', { rel: 'icon', href: `/images/logo.png` }
    ]
  ],
  alias: {
    '@theme/HomeFooter.vue': path.resolve(__dirname, './components/MyHomeFooter.vue'),
    '@theme/Home.vue': path.resolve(__dirname, './components/MyHome.vue'),
  },
  theme: defaultTheme({
    logo: '/images/logo.png',
    docsDir: 'docs',
    notFound: ['迷路的孩子早点回家'],
    backToHome: '跟我回家',
    navbar: [
      {
        text: '指南',
        link: '/guide/index.md'
      },
      {
        text: '文章',
        link: '/article/leetcode练习1-10.md'
      },
      {
        text: '关于',
        link: '/about.md'
      }
    ],
    sidebar: {
      '/': [
        {
          text: '首页',
          children: ['index.md', 'work.md', 'android学习大纲.md', 'app启动流程.md']
        }
      ],
      '/guide/': [
        {
          text: '指南',
          children: ['/guide/index.md', '/guide/guide2.md']
        }
      ],
      '/article/': [
        {
          text: 'LeetCode',
          children: ['/article/leetcode练习1-10.md', '/article/leetcode练习11-20.md', '/article/1-两数之和.md', '/article/7-整数反转.md', '/article/9-回文数.md']
        },
        {
          text: 'Android',
          children: ['/article/不到百行代码的日志工具类.md', '/article/Android常用加密解密实现方式.md', '/article/Android开发小技巧.md', '/article/Android事件统计之动态代理的应用.md', '/article/Android项目集成Flutter与相互通讯.md', '/article/Android项目集成React-Native与相互通讯.md', '/article/Coil用法详解.md', '/article/EditText一键清除.md', '/article/EventBus源码分析.md', '/article/Gradle常用配置讲解.md', '/article/java十大排序算法分析.md', '/article/OkHttp用法详解.md', '/article/Okio源码分析与性能优化思想.md', '/article/Retrofit用法详解.md', '/article/RxJava常用操作符.md', '/article/StartUp用法详解与原理分析.md', '/article/Lifecycle源码详解.md']
        },
        {
          text: '设计模式',
          children: ['/article/设计模式在Android中的应用-创建型模式.md', '/article/设计模式在Android中的应用-结构型模式.md', '/article/设计模式在Android中的应用-行为型模式.md']
        }
      ]
    }
  })
})