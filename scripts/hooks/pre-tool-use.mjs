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

const raw = await readStdin();
let input = {};

try {
  input = raw.trim() ? JSON.parse(raw) : {};
} catch {
  input = { raw };
}

const searchText = extractText(input);
const matched = DANGEROUS_PATTERNS.find((pattern) => pattern.test(searchText));

if (matched) {
  const output = {
    hookSpecificOutput: {
      PreToolUse: {
        permissionDecision: 'deny',
        permissionDecisionReason: `命中危险操作拦截规则：${matched}`
      }
    }
  };

  process.stdout.write(JSON.stringify(output, null, 2));
} else {
  const output = {
    hookSpecificOutput: {
      PreToolUse: {
        permissionDecision: 'allow',
        permissionDecisionReason: '未命中危险操作模式，允许继续。'
      }
    }
  };

  process.stdout.write(JSON.stringify(output, null, 2));
}
