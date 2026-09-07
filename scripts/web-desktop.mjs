import http from "node:http";
import { spawn } from "node:child_process";

const UPSTREAM_PORT = Number(process.env.EXPO_WEB_UPSTREAM_PORT ?? 8081);
const PROXY_PORT = Number(process.env.EXPO_WEB_PORT ?? 19006);
const UPSTREAM = `http://localhost:${UPSTREAM_PORT}`;

const EXTRA_HEADERS = {
  "cross-origin-opener-policy": "same-origin",
  "cross-origin-embedder-policy": "credentialless",
};

let child = null;

function killChild() {
  if (!child || child.exitCode !== null || child.killed) return;
  if (process.platform === "win32") {
    spawn("taskkill", ["/pid", String(child.pid), "/T", "/F"], {
      stdio: "ignore",
      windowsHide: true,
    });
  } else {
    child.kill("SIGTERM");
  }
}

function waitForUpstream(retries = 120) {
  return new Promise((resolve, reject) => {
    const attempt = (left) => {
      const req = http.get(UPSTREAM, () => {
        req.destroy();
        resolve();
      });
      req.on("error", () => {
        req.destroy();
        if (left <= 0) {
          reject(new Error(`Expo dev server did not start on ${UPSTREAM}`));
        } else {
          setTimeout(() => attempt(left - 1), 500);
        }
      });
    };
    attempt(retries);
  });
}

const server = http.createServer((req, res) => {
  const upstream = new URL(req.url, UPSTREAM);
  const proxyReq = http.request(
    upstream,
    {
      method: req.method,
      headers: { ...req.headers, host: upstream.host },
    },
    (proxyRes) => {
      const headers = { ...proxyRes.headers, ...EXTRA_HEADERS };
      res.writeHead(proxyRes.statusCode ?? 502, headers);
      proxyRes.pipe(res);
    },
  );
  proxyReq.on("error", (err) => {
    res.statusCode = 502;
    res.end(`Dev proxy error: ${err.message}`);
  });
  req.pipe(proxyReq);
});

server.on("upgrade", (req, socket, head) => {
  const upstream = new URL(req.url, UPSTREAM);
  const proxyReq = http.request({
    hostname: upstream.hostname,
    port: upstream.port || UPSTREAM_PORT,
    path: upstream.pathname + upstream.search,
    method: req.method,
    headers: { ...req.headers, host: upstream.host },
  });
  proxyReq.on("upgrade", (proxyRes, proxySocket, proxyHead) => {
    const lines = ["HTTP/1.1 101 Switching Protocols"];
    for (const [key, value] of Object.entries(proxyRes.headers)) {
      lines.push(`${key}: ${value}`);
    }
    socket.write(`${lines.join("\r\n")}\r\n\r\n`);
    if (proxyHead?.length) proxySocket.unshift(proxyHead);
    proxySocket.pipe(socket);
    socket.pipe(proxySocket);
  });
  proxyReq.on("error", () => socket.destroy());
  proxyReq.end(head);
});

for (const signal of ["SIGINT", "SIGTERM", "SIGHUP"]) {
  process.on(signal, () => {
    killChild();
    process.exit(0);
  });
}

child = spawn("pnpm", ["web"], {
  shell: process.platform === "win32",
  stdio: "inherit",
});
child.on("exit", (code) => {
  if (child.__intentional) return;
  console.error(`Expo dev server exited with code ${code}.`);
  process.exit(code ?? 1);
});

try {
  await waitForUpstream();
} catch (err) {
  console.error(err.message);
  killChild();
  process.exit(1);
}

server.listen(PROXY_PORT, () => {
  console.log(`\nDesktop test server ready: http://localhost:${PROXY_PORT}`);
  console.log(`(proxying ${UPSTREAM} with COOP/COEP headers for SharedArrayBuffer)\n`);
});

process.on("exit", () => {
  child.__intentional = true;
  killChild();
});
