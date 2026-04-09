import process from 'node:process';

const output = {
  continue: true,
  systemMessage: 'SessionStart：请先读取 AGENTS.md、docs/ai-workflow.md、docs/项目进度.md 以及当前任务文档，再开始分析或修改。'
};

process.stdout.write(JSON.stringify(output, null, 2));
