# -*- coding: utf-8 -*-
"""验收自测客户端：纯 UTF-8 链路（模拟真实浏览器提交中文）。输出 PASS/FAIL。"""
import io
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
import zipfile
from http.cookiejar import CookieJar

BASE = "http://localhost:8081"
cj = CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
results = []


def check(name, ok, detail=""):
    results.append((name, ok, detail))
    print(("PASS " if ok else "FAIL ") + name + ((" | " + str(detail)) if detail else ""))


def call(url, data=None, method=None, file=None, headers=None):
    """data: dict(str->str)。file: (field, filename, bytes, content_type)。返回 (status, bytes, resp_headers)"""
    url = BASE + url
    if file is not None:
        boundary = uuid.uuid4().hex
        body = io.BytesIO()
        for k, v in (data or {}).items():
            body.write(("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n"
                        % (boundary, k, v)).encode("utf-8"))
        field, filename, content, ctype = file
        body.write(("--%s\r\nContent-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n"
                    "Content-Type: %s\r\n\r\n" % (boundary, field, filename, ctype)).encode("utf-8"))
        body.write(content)
        body.write(("\r\n--%s--\r\n" % boundary).encode("utf-8"))
        req = urllib.request.Request(url, data=body.getvalue(), method=method or "POST")
        req.add_header("Content-Type", "multipart/form-data; boundary=" + boundary)
    elif data is not None and method != "GET":
        body = urllib.parse.urlencode(data).encode("utf-8")
        req = urllib.request.Request(url, data=body, method=method or "POST")
        req.add_header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
    else:
        if data:
            url = url + "?" + urllib.parse.urlencode(data)
        req = urllib.request.Request(url, method=method or "GET")
    for k, v in (headers or {}).items():
        req.add_header(k, v)
    try:
        resp = opener.open(req)
        return resp.status, resp.read(), dict(resp.headers)
    except urllib.error.HTTPError as e:
        return e.code, e.read(), dict(e.headers or {})


def japi(url, data=None, method=None, file=None):
    status, raw, _ = call(url, data, method, file)
    try:
        return status, json.loads(raw.decode("utf-8"))
    except Exception:
        return status, {"code": -1, "msg": raw[:120].decode("utf-8", "replace")}


def login(u, p):
    status, raw, _ = call("/login", {"username": u, "password": p})
    return status, raw.decode("utf-8", "replace")


def make_xlsx(rows):
    try:
        import openpyxl
    except ImportError:
        return None
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.append(["学号", "姓名", "成绩"])
    for r in rows:
        ws.append(r)
    buf = io.BytesIO()
    wb.save(buf)
    return buf.getvalue()


# ---------- 0. 管理员登录 ----------
s, r = login("admin", "admin123")
check("admin 登录", "admin/users" in r, r[:80])

# ---------- 1. 管理员创建账号（中文名） ----------
s, r = japi("/admin/api/users", {"username": "T09", "password": "123456",
                                 "realName": "钱导师", "role": "2", "maxQuota": "2"})
check("管理员创建导师T09(中文名)", r["code"] == 0 or "已存在" in r.get("msg", ""), r)
s, r = japi("/admin/api/users", {"username": "S09", "password": "123456",
                                 "realName": "周同学", "role": "3", "studentNo": "S09",
                                 "major": "计算机科学", "className": "计科2203"})
check("管理员创建学员S09(中文名)", r["code"] == 0 or "已存在" in r.get("msg", ""), r)

# ---------- 2. 学员重复申请拦截 ----------
s, r = login("S09", "123456")
check("S09 登录", "student/mentors" in r, r[:80])
s, r = login("T09", "123456")
check("T09 登录", "mentor/projects" in r, r[:80])

# ---------- 3. 导师发布项目（中文标题）+ 学员可见 ----------
title = "基于 Spring Boot 的在线考试系统"
s, r = japi("/mentor/api/projects", {"title": title, "description": "实现题库、组卷与自动评分"})
check("T09 发布项目(中文标题)", r["code"] == 0, r)
s, r = login("S09", "123456")
s, r = japi("/student/api/projects", {"kw": title}, method="GET")
ok = r["code"] == 0 and r["data"] and r["data"]["total"] >= 1 \
    and any(p["title"] == title for p in r["data"]["records"])
check("学员端可见且中文完好", ok, r)
if not (r["code"] == 0 and r["data"] and r["data"]["records"]):
    print("\n===== ABORT: project not visible =====")
    sys.exit(1)
pid = r["data"]["records"][0]["id"]
mid = r["data"]["records"][0]["mentorId"]

# ---------- 4. 学员申请 → 导师接受 ----------
s, r = japi("/student/api/selections", {"mentorId": str(mid), "projectId": str(pid)})
check("S09 提交双选申请(定向项目)", r["code"] == 0 or "已有待处理" in r.get("msg", ""), r)
s, r = japi("/student/api/selections", {}, method="GET")
sel = r["data"]["records"][0]
ok = sel["projectTitle"] == title and sel["studentName"] == "周同学" and sel["status"] == 0
check("申请记录中文与关联完好", ok, sel)
s, r = login("T09", "123456")
s, r = japi("/mentor/api/selections/%d/accept" % sel["id"], {})
check("T09 接受申请", r["code"] == 0, r)

# ---------- 5. 学员上传项目文件 ----------
s, r = login("S09", "123456")
zbuf = io.BytesIO()
with zipfile.ZipFile(zbuf, "w") as z:
    z.writestr("设计说明.txt", "含系统设计与测试说明".encode("utf-8"))
s, r = japi("/student/api/submissions",
            {"selectionId": str(sel["id"]), "title": "阶段成果一", "description": "第一版系统与文档"},
            file=("file", "阶段成果.zip", zbuf.getvalue(), "application/zip"))
check("S09 上传项目文件(中文文件名)", r["code"] == 0, r)
s, r = japi("/student/api/submissions", {}, method="GET")
sub = r["data"]["records"][0]
ok = r["code"] == 0 and sub["fileName"] == "阶段成果.zip" and sub["fileSize"] == len(zbuf.getvalue())
check("提交记录中文完好", ok, sub)
s, r = japi("/student/api/submissions",
            {"selectionId": str(sel["id"]), "title": "坏文件"},
            file=("file", "virus.exe", b"MZ", "application/octet-stream"))
check("上传 .exe 被拒", r["code"] == 1 and "不支持的文件类型" in r["msg"], r)
big = b"0" * (21 * 1024 * 1024)
s, r = japi("/student/api/submissions",
            {"selectionId": str(sel["id"]), "title": "超大文件"},
            file=("file", "big.zip", big, "application/zip"))
check("上传 21MB 超限被拒", r["code"] == 1 and "超出限制" in r["msg"], r)

# ---------- 6. 文件下载权限 ----------
import urllib.parse
expect_disp = "attachment; filename*=UTF-8''" + urllib.parse.quote("阶段成果.zip")
s, r = login("T09", "123456")
s, raw, hdr = call("/api/files/%d" % sub["id"])
check("导师下载学员文件(HTTP 200)", s == 200, s)
check("下载文件名与原始名一致", hdr.get("Content-Disposition", "") == expect_disp,
      hdr.get("Content-Disposition", ""))
s, r = login("S09", "123456")
s, raw, hdr = call("/api/files/%d" % sub["id"])
check("学员本人可下载", s == 200, s)
s, r = login("S01", "123456")
s, r = japi("/api/files/%d" % sub["id"])
check("其他学员下载被拒", r.get("code") == 1 and "没有权限" in r.get("msg", ""), r)

# ---------- 7. 导师指导（中文） ----------
gtext = "请补充测试用例与系统设计说明，两周内提交修改稿。"
s, r = login("T09", "123456")
s, r = japi("/mentor/api/submissions/%d/guidance" % sub["id"], {"content": gtext})
check("T09 填写指导(中文)", r["code"] == 0, r)
s, r = login("S09", "123456")
s, r = japi("/student/api/guidances", {}, method="GET")
g = r["data"]["records"][0] if r["data"]["records"] else {}
ok = g.get("content") == gtext and g.get("submissionTitle") == "阶段成果一" and g.get("mentorName") == "钱导师"
check("学员端指导中文完好", ok, g)

# ---------- 8. 成绩单导入（中文课程名/姓名） ----------
s, r = login("T09", "123456")
x1 = make_xlsx([["S09", "周同学", 95], ["S01", "王小明", 88]])
s, r = japi("/mentor/api/courses", {"courseName": "高等数学"}, file=("file", "gaoshu.xlsx", x1,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
check("E1 导入合法成绩单", r["code"] == 0 and "成功 2 条" in r["data"] and "失败 0 条" in r["data"], r)
s, r = japi("/mentor/api/courses", {"courseName": "高等数学"}, file=("file", "gaoshu.xlsx", x1,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
check("E2 重复课程名被拒", r["code"] == 1 and "已导入过" in r["msg"], r)
x2 = make_xlsx([["S09", "周同学", 90], ["S02", "李四", "缺考"], ["S03", "王五", -5], ["S04", "赵六", 77]])
s, r = japi("/mentor/api/courses", {"courseName": "概率论"}, file=("file", "gailv.xlsx", x2,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
check("E3 异常行计数正确(成功2失败2)", r["code"] == 0 and "成功 2 条" in r["data"] and "失败 2 条" in r["data"]
      and "第3行" in r["data"] and "第4行" in r["data"], r)
s, r = japi("/mentor/api/courses", {}, method="GET")
courses = {c["courseName"]: c for c in r["data"]["records"]}
check("课程名中文完好", "高等数学" in courses and "概率论" in courses, list(courses))
s, r = japi("/mentor/api/selections/%d/grades" % sel["id"], {}, method="GET")
ok = any(g["courseName"] == "高等数学" and float(g["score"]) == 95 for g in r["data"])
check("申请页可见学员成绩(按学号匹配)", ok, r["data"])
x3 = make_xlsx([["S09", "周同学", 60]])
s, r = japi("/mentor/api/courses", {"courseName": "高等数学"}, file=("file", "x.xlsx", x3,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
check("E2b 重复导入仍被拒", r["code"] == 1, r)
x4 = b"not an excel"
s, r = japi("/mentor/api/courses", {"courseName": "机器学习"}, file=("file", "bad.xlsx", x4, "application/zip"))
check("非 Excel 文件被拒", r["code"] == 1 and "无法解析" in r["msg"], r)

# ---------- 汇总 ----------
fails = [x for x in results if not x[1]]
print("\n===== SUMMARY: %d/%d PASS =====" % (len(results) - len(fails), len(results)))
for name, ok, detail in fails:
    print("FAILED:", name, "|", str(detail)[:200])
sys.exit(1 if fails else 0)
