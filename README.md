# select-proxy
选择性的反向代理

本程序依赖于一个代理工具 org.mitre.dsmiley.httpproxy:smiley-http-proxy-servlet:1.11，[其对应的 github](https://github.com/mitre/HTTP-Proxy-Servlet)。
运用它可以对指定的地址反向代理。

本程序的目的是在代理前可以选择自己需要的用户，从而将对应用户的 TOKEN 添加到请求中，并代理出去。
