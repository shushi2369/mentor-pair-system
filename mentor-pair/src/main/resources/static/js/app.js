/** 统一 AJAX 封装：code=0 回调，非 0 弹出 msg；401/403 有友好提示 */
function api(url, type, data, cb) {
    var opts = {
        url: url,
        type: type || 'GET',
        dataType: 'json',
        success: function (r) {
            if (r && r.code === 0) {
                if (cb) cb(r.data, r.msg);
            } else {
                alert((r && r.msg) || '操作失败');
            }
        },
        error: function (xhr) {
            alert(xhr.status === 401 ? '请先登录' : (xhr.status === 403 ? '没有权限执行该操作' : '请求失败，请稍后重试'));
        }
    };
    if (data instanceof FormData) {
        opts.data = data;
        opts.processData = false;
        opts.contentType = false;
    } else {
        opts.data = data || {};
    }
    $.ajax(opts);
}

function esc(s) {
    return s == null ? '' : String(s)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function fmtSize(n) {
    if (n == null) return '-';
    if (n < 1024) return n + ' B';
    if (n < 1048576) return (n / 1024).toFixed(1) + ' KB';
    return (n / 1048576).toFixed(1) + ' MB';
}

function pager(el, page, total, size, load) {
    var pages = Math.max(1, Math.ceil(total / size));
    var html = '<span class="me-2 text-secondary">共 ' + total + ' 条 · 第 ' + page + ' / ' + pages + ' 页</span>'
        + '<button class="btn btn-sm btn-outline-secondary me-1" id="pg-prev"' + (page <= 1 ? ' disabled' : '') + '>上一页</button>'
        + '<button class="btn btn-sm btn-outline-secondary" id="pg-next"' + (page >= pages ? ' disabled' : '') + '>下一页</button>';
    $(el).html(html);
    $('#pg-prev').on('click', function () { if (page > 1) load(page - 1); });
    $('#pg-next').on('click', function () { if (page < pages) load(page + 1); });
}

var SEL_STATUS = {
    0: '<span class="badge bg-warning text-dark">待处理</span>',
    1: '<span class="badge bg-success">已接受</span>',
    2: '<span class="badge bg-secondary">已拒绝</span>'
};

function selStatus(s) {
    return SEL_STATUS[s] || esc(s);
}

/** 退出并关闭系统（exe 模式下服务进程随之结束） */
function shutdownSystem() {
    if (!confirm("确认退出并关闭系统？\n关闭后需重新启动程序才能再次访问。")) {
        return;
    }
    $.ajax({
        url: "/shutdown",
        type: "POST",
        success: function () { showShutdownDone(); },
        error: function () { showShutdownDone(); }
    });
}

function showShutdownDone() {
    document.body.innerHTML =
        '<div style="min-height:100vh;display:flex;align-items:center;justify-content:center;' +
        'flex-direction:column;font-family:\'Microsoft YaHei\',sans-serif;color:#334155;">' +
        '<div style="font-size:52px;margin-bottom:16px;">&#10003;</div>' +
        '<div style="font-size:20px;font-weight:700;margin-bottom:8px;">系统已退出</div>' +
        '<div style="color:#64748b;">本页面已断开，可关闭浏览器窗口；下次使用请重新启动程序。</div>' +
        '</div>';
    document.body.style.background = "#eef2f7";
}

/** 自助修改密码（三端通用，弹窗在导航片段中） */
function openChangePassword() {
    ["pwdOld", "pwdNew", "pwdConfirm"].forEach(function (id) { $("#" + id).val(""); });
    new bootstrap.Modal(document.getElementById("pwdModal")).show();
}

function submitChangePassword() {
    var o = $("#pwdOld").val(), n = $("#pwdNew").val(), c = $("#pwdConfirm").val();
    if (!o || !n) { alert("请填写旧密码和新密码"); return; }
    if (n.length < 6) { alert("新密码长度至少 6 位"); return; }
    if (n !== c) { alert("两次输入的新密码不一致"); return; }
    api("/api/account/password", "POST", { oldPassword: o, newPassword: n }, function () {
        bootstrap.Modal.getInstance(document.getElementById("pwdModal")).hide();
        alert("密码修改成功，下次登录请使用新密码");
    });
}
