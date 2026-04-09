import process from 'node:process';

const output = {
  continue: true,
  systemMessage: 'PostToolUse：请检查本次变更是否符合仓库规范，并在必要时补充测试或验证说明。'
};

process.stdout.write(JSON.stringify(output, null, 2));
