import fs from 'node:fs/promises';
import path from 'node:path';
import process from 'node:process';

const SERVER_NAME = 'community-mvp';
const SERVER_VERSION = '0.1.0';
const PROTOCOL_VERSION = '2024-11-05';
const WORKSPACE_ROOT = path.resolve(process.env.COMMUNITY_MVP_MCP_WORKSPACE_ROOT || process.cwd());
const GITHUB_TOKEN = process.env.GITHUB_PERSONAL_ACCESS_TOKEN || process.env.GITHUB_TOKEN || '';
const GITHUB_REPOSITORY = process.env.COMMUNITY_MVP_GITHUB_REPOSITORY || process.env.GITHUB_REPOSITORY || '';

const tools = [
  {
    name: 'github_repo_info',
    description: '读取 GitHub 仓库基本信息、默认分支和统计数据。',
    inputSchema: {
      type: 'object',
      properties: {
        repo: {
          type: 'string',
          description: 'owner/repo，默认使用环境变量中的仓库名。'
        }
      }
    }
  },
  {
    name: 'github_list_issues',
    description: '列出仓库 issue。',
    inputSchema: {
      type: 'object',
      properties: {
        repo: { type: 'string' },
        state: { type: 'string', enum: ['open', 'closed', 'all'], default: 'open' },
        per_page: { type: 'integer', minimum: 1, maximum: 100, default: 10 }
      }
    }
  },
  {
    name: 'github_list_pull_requests',
    description: '列出仓库 pull request。',
    inputSchema: {
      type: 'object',
      properties: {
        repo: { type: 'string' },
        state: { type: 'string', enum: ['open', 'closed', 'all'], default: 'open' },
        per_page: { type: 'integer', minimum: 1, maximum: 100, default: 10 }
      }
    }
  },
  {
    name: 'github_get_issue',
    description: '读取单个 GitHub issue 的详细信息。',
    inputSchema: {
      type: 'object',
      required: ['number'],
      properties: {
        repo: { type: 'string' },
        number: { type: 'integer' }
      }
    }
  },
  {
    name: 'github_get_pull_request',
    description: '读取单个 GitHub pull request 的详细信息。',
    inputSchema: {
      type: 'object',
      required: ['number'],
      properties: {
        repo: { type: 'string' },
        number: { type: 'integer' }
      }
    }
  },
  {
    name: 'fetch_url',
    description: '抓取网页或文档文本内容。',
    inputSchema: {
      type: 'object',
      required: ['url'],
      properties: {
        url: { type: 'string' },
        maxChars: { type: 'integer', minimum: 1000, maximum: 50000, default: 20000 }
      }
    }
  },
  {
    name: 'read_workspace_file',
    description: '读取工作区中的文本文件。',
    inputSchema: {
      type: 'object',
      required: ['path'],
      properties: {
        path: { type: 'string' },
        maxChars: { type: 'integer', minimum: 1000, maximum: 50000, default: 20000 }
      }
    }
  },
  {
    name: 'list_workspace_files',
    description: '列出工作区目录下的文件。',
    inputSchema: {
      type: 'object',
      properties: {
        path: { type: 'string', default: '.' }
      }
    }
  }
];

function jsonResponse(id, result) {
  return { jsonrpc: '2.0', id, result };
}

function jsonError(id, code, message, data) {
  return {
    jsonrpc: '2.0',
    id,
    error: {
      code,
      message,
      ...(data === undefined ? {} : { data })
    }
  };
}

function send(message) {
  const body = JSON.stringify(message);
  const header = `Content-Length: ${Buffer.byteLength(body, 'utf8')}\r\n\r\n`;
  process.stdout.write(header + body);
}

async function readStdin() {
  const chunks = [];
  for await (const chunk of process.stdin) {
    chunks.push(chunk);
  }
  return Buffer.concat(chunks);
}

function parseMessages(buffer) {
  const messages = [];
  let offset = 0;

  while (offset < buffer.length) {
    const headerEnd = buffer.indexOf('\r\n\r\n', offset, 'utf8');
    if (headerEnd === -1) {
      break;
    }

    const headerText = buffer.slice(offset, headerEnd).toString('utf8');
    const contentLengthMatch = headerText.match(/Content-Length:\s*(\d+)/i);
    if (!contentLengthMatch) {
      offset = headerEnd + 4;
      continue;
    }

    const contentLength = Number(contentLengthMatch[1]);
    const bodyStart = headerEnd + 4;
    const bodyEnd = bodyStart + contentLength;
    if (bodyEnd > buffer.length) {
      break;
    }

    const body = buffer.slice(bodyStart, bodyEnd).toString('utf8');
    messages.push(JSON.parse(body));
    offset = bodyEnd;
  }

  return messages;
}

function getRepo(input) {
  const repo = typeof input?.repo === 'string' && input.repo.trim() ? input.repo.trim() : GITHUB_REPOSITORY;
  if (!repo) {
    throw new Error('未设置 GitHub 仓库，请通过 repo 参数或环境变量 GITHUB_REPOSITORY / COMMUNITY_MVP_GITHUB_REPOSITORY 提供。');
  }
  const parts = repo.split('/');
  if (parts.length !== 2 || !parts[0] || !parts[1]) {
    throw new Error(`GitHub 仓库格式不正确：${repo}，需要 owner/repo。`);
  }
  return repo;
}

function assertWithinWorkspace(inputPath) {
  const targetPath = path.resolve(WORKSPACE_ROOT, inputPath || '.');
  const relative = path.relative(WORKSPACE_ROOT, targetPath);
  if (relative.startsWith('..') || path.isAbsolute(relative)) {
    throw new Error(`路径越界：${inputPath}`);
  }
  return targetPath;
}

function toTextContent(text) {
  return [{ type: 'text', text }];
}

async function githubApi(pathname, searchParams) {
  const url = new URL(`https://api.github.com${pathname}`);
  if (searchParams) {
    for (const [key, value] of Object.entries(searchParams)) {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, String(value));
      }
    }
  }

  const headers = {
    Accept: 'application/vnd.github+json',
    'X-GitHub-Api-Version': '2022-11-28',
    'User-Agent': `${SERVER_NAME}/${SERVER_VERSION}`
  };

  if (GITHUB_TOKEN) {
    headers.Authorization = `Bearer ${GITHUB_TOKEN}`;
  }

  const response = await fetch(url, { headers });
  const text = await response.text();
  if (!response.ok) {
    throw new Error(`GitHub API 请求失败：${response.status} ${response.statusText}\n${text}`);
  }
  return JSON.parse(text);
}

async function handleToolCall(name, args = {}) {
  switch (name) {
    case 'github_repo_info': {
      const repo = getRepo(args);
      const data = await githubApi(`/repos/${repo}`);
      return toTextContent(JSON.stringify({
        full_name: data.full_name,
        description: data.description,
        default_branch: data.default_branch,
        stargazers_count: data.stargazers_count,
        forks_count: data.forks_count,
        open_issues_count: data.open_issues_count,
        language: data.language,
        html_url: data.html_url,
        updated_at: data.updated_at
      }, null, 2));
    }
    case 'github_list_issues': {
      const repo = getRepo(args);
      const issues = await githubApi(`/repos/${repo}/issues`, {
        state: args.state || 'open',
        per_page: args.per_page || 10
      });
      const filtered = issues.filter((item) => !item.pull_request).map((item) => ({
        number: item.number,
        title: item.title,
        state: item.state,
        user: item.user?.login,
        created_at: item.created_at,
        html_url: item.html_url
      }));
      return toTextContent(JSON.stringify(filtered, null, 2));
    }
    case 'github_list_pull_requests': {
      const repo = getRepo(args);
      const pulls = await githubApi(`/repos/${repo}/pulls`, {
        state: args.state || 'open',
        per_page: args.per_page || 10
      });
      const filtered = pulls.map((item) => ({
        number: item.number,
        title: item.title,
        state: item.state,
        user: item.user?.login,
        created_at: item.created_at,
        html_url: item.html_url
      }));
      return toTextContent(JSON.stringify(filtered, null, 2));
    }
    case 'github_get_issue': {
      const repo = getRepo(args);
      const issue = await githubApi(`/repos/${repo}/issues/${args.number}`);
      return toTextContent(JSON.stringify(issue, null, 2));
    }
    case 'github_get_pull_request': {
      const repo = getRepo(args);
      const pull = await githubApi(`/repos/${repo}/pulls/${args.number}`);
      return toTextContent(JSON.stringify(pull, null, 2));
    }
    case 'fetch_url': {
      const url = typeof args.url === 'string' ? args.url : '';
      if (!url) {
        throw new Error('fetch_url 需要提供 url。');
      }
      const maxChars = Number.isFinite(args.maxChars) ? args.maxChars : 20000;
      const response = await fetch(url, { headers: { 'User-Agent': `${SERVER_NAME}/${SERVER_VERSION}` } });
      const text = await response.text();
      return toTextContent(JSON.stringify({
        url,
        status: response.status,
        content_type: response.headers.get('content-type'),
        body: text.slice(0, maxChars)
      }, null, 2));
    }
    case 'read_workspace_file': {
      const filePath = typeof args.path === 'string' ? args.path : '';
      if (!filePath) {
        throw new Error('read_workspace_file 需要提供 path。');
      }
      const maxChars = Number.isFinite(args.maxChars) ? args.maxChars : 20000;
      const targetPath = assertWithinWorkspace(filePath);
      const content = await fs.readFile(targetPath, 'utf8');
      return toTextContent(JSON.stringify({
        path: path.relative(WORKSPACE_ROOT, targetPath),
        body: content.slice(0, maxChars)
      }, null, 2));
    }
    case 'list_workspace_files': {
      const dirPath = assertWithinWorkspace(typeof args.path === 'string' ? args.path : '.');
      const entries = await fs.readdir(dirPath, { withFileTypes: true });
      return toTextContent(JSON.stringify(entries.map((entry) => ({
        name: entry.name,
        type: entry.isDirectory() ? 'directory' : 'file'
      })), null, 2));
    }
    default:
      throw new Error(`未知工具：${name}`);
  }
}

async function handleMessage(message) {
  const { id, method, params } = message;

  try {
    if (method === 'initialize') {
      send(jsonResponse(id, {
        protocolVersion: params?.protocolVersion || PROTOCOL_VERSION,
        capabilities: {
          tools: {},
          resources: {}
        },
        serverInfo: {
          name: SERVER_NAME,
          version: SERVER_VERSION
        }
      }));
      return;
    }

    if (method === 'initialized') {
      return;
    }

    if (method === 'tools/list') {
      send(jsonResponse(id, { tools }));
      return;
    }

    if (method === 'tools/call') {
      const toolName = params?.name;
      const result = await handleToolCall(toolName, params?.arguments || {});
      send(jsonResponse(id, { content: result, isError: false }));
      return;
    }

    send(jsonError(id, -32601, `未知方法：${method}`));
  } catch (error) {
    send(jsonError(id, -32000, error?.message || '服务器内部错误', { stack: error?.stack }));
  }
}

async function main() {
  process.stdin.setEncoding('utf8');

  let rawBuffer = Buffer.alloc(0);
  process.stdin.on('data', (chunk) => {
    rawBuffer = Buffer.concat([rawBuffer, Buffer.from(chunk, 'utf8')]);

    while (true) {
      const separatorIndex = rawBuffer.indexOf('\r\n\r\n');
      if (separatorIndex === -1) {
        break;
      }

      const headerText = rawBuffer.slice(0, separatorIndex).toString('utf8');
      const contentLengthMatch = headerText.match(/Content-Length:\s*(\d+)/i);
      if (!contentLengthMatch) {
        rawBuffer = rawBuffer.slice(separatorIndex + 4);
        continue;
      }

      const contentLength = Number(contentLengthMatch[1]);
      const bodyStart = separatorIndex + 4;
      const bodyEnd = bodyStart + contentLength;
      if (rawBuffer.length < bodyEnd) {
        break;
      }

      const body = rawBuffer.slice(bodyStart, bodyEnd).toString('utf8');
      rawBuffer = rawBuffer.slice(bodyEnd);

      let message;
      try {
        message = JSON.parse(body);
      } catch (error) {
        send(jsonError(null, -32700, '无法解析 JSON 请求', { body }));
        continue;
      }

      void handleMessage(message);
    }
  });
}

main().catch((error) => {
  send(jsonError(null, -32000, error?.message || '启动失败', { stack: error?.stack }));
  process.exit(1);
});
