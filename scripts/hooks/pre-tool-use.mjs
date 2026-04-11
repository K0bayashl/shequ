import process from 'node:process';

function readStdin() {
  return new Promise((resolve) => {
    const chunks = [];
    process.stdin.on('data', (chunk) => chunks.push(Buffer.from(chunk)));
    process.stdin.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
  });
}

function extractText(value) {
  if (value === null || value === undefined) {
    return '';
  }

  if (typeof value === 'string') {
    return value;
  }

  if (Array.isArray(value)) {
    return value.map((item) => extractText(item)).join(' ');
  }

  if (typeof value === 'object') {
    return Object.values(value).map((item) => extractText(item)).join(' ');
  }

  return String(value);
}

const DANGEROUS_PATTERNS = [
  /rm\s+-rf\s+\//i,
  /rm\s+-rf\b/i,
  /del\s+\/s\s+\/q/i,
  /rd\s+\/s\s+\/q/i,
  /rmdir\s+\/s\s+\/q/i,
  /git\s+reset\s+--hard/i,
  /git\s+clean\s+-fdx/i,
  /git\s+push\s+--force/i,
  /git\s+push\s+-f/i,
  /Delete-Item\s+.*-Recurse\s+.*-Force/i,
  /Remove-Item\s+.*-Recurse\s+.*-Force/i,
  /chmod\s+777\b/i,
  /mkfs\b/i,
  /dd\s+if=/i,
  /format\s+[A-Z]:/i,
  /shutdown\s+\/s\s+\/t\s+0/i,
  /reboot\b/i
];

// 乱码特征正则：包含常见的 GBK/UTF-8 编解码错误标志
const MOJIBAKE_PATTERNS = [
  /\uFFFD/,      //  替换字符
  /锟斤拷/,       // UTF-8 转 GBK 常见乱码
  /烫烫烫/,      // 未初始化内存
  /屯屯屯/,      // 未初始化内存
  /锘/         // 带有 BOM 的 UTF-8 被当做 GBK 解析时经常出现的第一个字
];

const raw = await readStdin();
let input = {};

try {
  input = raw.trim() ? JSON.parse(raw) : {};
} catch {
  input = { raw };
}

const searchText = extractText(input);

// 1. 检查危险命令
const matchedDanger = DANGEROUS_PATTERNS.find((pattern) => pattern.test(searchText));
if (matchedDanger) {
  const output = {
    hookSpecificOutput: {
      PreToolUse: {
        permissionDecision: 'deny',
        permissionDecisionReason: `命中危险操作拦截规则：${matchedDanger}`
      }
    }
  };
  process.stdout.write(JSON.stringify(output, null, 2));
  process.exit(0);
}

// 2. 检查写入内容是否包含乱码特征
const matchedMojibake = MOJIBAKE_PATTERNS.find((pattern) => pattern.test(searchText));
// 我们通过判断入参中是否包含疑似文件写入相关的字段（如 content, parameters.content，或工具名为 create_file/edit_file）
// 但为了安全起见，只要将要输出或执行的内容包含明显乱码特征，都可以拒绝。
if (matchedMojibake) {
  const output = {
    hookSpecificOutput: {
      PreToolUse: {
        permissionDecision: 'deny',
        permissionDecisionReason: `拦截：疑似乱码或编码错误内容（匹配特征：${matchedMojibake}）。请确认文件编码格式或终端输出是否正常。`
      }
    }
  };
  process.stdout.write(JSON.stringify(output, null, 2));
  process.exit(0);
}

const output = {
  hookSpecificOutput: {
    PreToolUse: {
      permissionDecision: 'allow',
      permissionDecisionReason: '未命中危险操作模式或乱码特征，允许继续。'
    }
  }
};

process.stdout.write(JSON.stringify(output, null, 2));
