const Koa = require('koa');
const Mount = require('koa-mount');
const Router = require('koa-router');
const Static = require('koa-static');
const fs = require('fs');
const path = require('path');

const app = new Koa();
app.use(Static('./static'))
    .use(Mount('/vue', Static('./dist')));

const router = new Router();
router.get('/', async (ctx) => { ctx.body = '<h1>Hello Vue</h1>'; })
    .get('/banner/json', async (ctx) => {
        let resultPromise = () => {
            return new Promise((resolve, reject) => {
                fs.readFile(path.join(__dirname, '/static/json/banner.json'), (err, data) => {
                    if (err) {
                        reject({ 'code': -1, 'msg': '获取失败' });
                    } else {
                        resolve(JSON.parse(data.toString()));
                    }
                });
            });
        };
        ctx.body = await resultPromise();
    });

app.use(router.routes()).use(router.allowedMethods());
app.listen(3000, () => {
    console.log('http://localhost:3000');
});