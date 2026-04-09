import process from 'node:process';

const output = {
  continue: true,
  systemMessage: 'PreCompact：请先输出当前任务摘要、已完成内容、进行中事项、风险与下一步建议。'
};

process.stdout.write(JSON.stringify(output, null, 2));
