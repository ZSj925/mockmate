/* =========================================================================
   mockmate · 前端公共请求层
   两个页面都先引它，再写各自的页面逻辑：<script src="js/api.js"></script>

   它负责三件事：
   1. 登录成功后把后端返回的 tokenName / tokenValue 存进 localStorage；
   2. 每次请求自动把 token 塞到请求头里（Sa-Token 默认从同名 header 读取）；
   3. 把后端统一的 Result{code,msg,data} 拆开 —— code=1 返回 data，否则抛错误，
      401 则直接清掉本地登录态并跳回登录页。
   ========================================================================= */

(function (global) {
  'use strict';

  var TOKEN_KEY = 'mockmate.token';
  var USER_KEY = 'mockmate.username';

  function readToken() {
    try {
      return JSON.parse(global.localStorage.getItem(TOKEN_KEY) || 'null');
    } catch (e) {
      return null;
    }
  }

  function getToken() {
    var token = readToken();
    return token && token.name && token.value ? token : null;
  }

  function getUsername() {
    return global.localStorage.getItem(USER_KEY) || '';
  }

  function isLoggedIn() {
    return !!getToken();
  }

  /**
   * 保存登录态
   * @param {object} loginResponse 后端 Result<LoginResponse> 里的 data，即 {tokenName, tokenValue, tokenTimeout}
   * @param {string} username      登录时填的用户名，主页用来打招呼
   */
  function saveSession(loginResponse, username) {
    global.localStorage.setItem(
      TOKEN_KEY,
      JSON.stringify({
        name: loginResponse.tokenName,
        value: loginResponse.tokenValue,
        timeout: loginResponse.tokenTimeout || 0
      })
    );
    global.localStorage.setItem(USER_KEY, username || '');
  }

  function clearSession() {
    global.localStorage.removeItem(TOKEN_KEY);
    global.localStorage.removeItem(USER_KEY);
  }

  /**
   * 跳回登录页（用 replace，避免用户按返回键又回到需要登录的页面）
   * @param {string} [reason] 显示在登录页上的提示，例如 expired
   */
  function goLogin(reason) {
    clearSession();
    var url = 'login.html';
    if (reason) {
      url += '?reason=' + encodeURIComponent(reason);
    }
    global.location.replace(url);
  }

  /**
   * 请求一个后端接口
   *
   * @param {string} path    接口路径，例如 '/auth/doLogin'
   * @param {object} [options] { method, body, headers }
   *                          body 传对象会自动 JSON.stringify
   * @returns {Promise<any>} 后端 Result 里的 data（没有 data 时是 null）
   */
  function api(path, options) {
    options = options || {};

    var headers = Object.assign({}, options.headers);
    var body = options.body;

    if (body !== undefined && body !== null) {
      if (typeof global.FormData !== 'undefined' && body instanceof global.FormData) {
        // 上传文件：原样交给浏览器。
        // 千万不要手写 Content-Type —— multipart 的 boundary 必须由浏览器生成，
        // 手写会变成 "Current request is not a multipart request"。
      } else if (typeof body !== 'string') {
        body = JSON.stringify(body);
        headers['Content-Type'] = headers['Content-Type'] || 'application/json';
      }
    }

    var token = getToken();
    if (token) {
      headers[token.name] = token.value;
    }

    return global
      .fetch(path, {
        method: options.method || 'GET',
        headers: headers,
        body: body,
        credentials: 'same-origin'
      })
      .catch(function () {
        throw new Error('连不上后端，确认服务已经启动');
      })
      .then(function (res) {
        // token 过期 / 没登录：Sa-Token 拦截器抛出，GlobalExceptionHandler 转成 401
        if (res.status === 401) {
          goLogin('expired');
          throw new Error('登录已过期，正在跳转登录页…');
        }

        return res.text().then(function (text) {
          var payload = null;
          if (text) {
            try {
              payload = JSON.parse(text);
            } catch (e) {
              throw new Error('后端返回的不是 JSON（HTTP ' + res.status + '）');
            }
          }

          if (!payload) {
            throw new Error('请求失败（HTTP ' + res.status + '）');
          }
          // 后端约定：code=1 成功，0 及其它为失败
          if (payload.code !== 1) {
            throw new Error(payload.msg || '请求失败');
          }
          return payload.data === undefined ? null : payload.data;
        });
      });
  }

  global.MM = {
    api: api,
    saveSession: saveSession,
    clearSession: clearSession,
    getToken: getToken,
    getUsername: getUsername,
    isLoggedIn: isLoggedIn,
    goLogin: goLogin
  };
})(window);
