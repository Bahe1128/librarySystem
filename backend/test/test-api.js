const http = require('http');

// 测试配置
const baseUrl = 'http://localhost:5000';

// 测试函数
function testEndpoint(path, method = 'GET', data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'localhost',
      port: 5000,
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        resolve({ status: res.statusCode, body: JSON.parse(data) });
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    if (data) {
      req.write(JSON.stringify(data));
    }

    req.end();
  });
}

// 测试用例
async function runTests() {
  console.log('开始测试图书管理系统API...');

  try {
    // 测试根端点
    console.log('\n1. 测试根端点:');
    const rootResponse = await testEndpoint('/');
    console.log('状态码:', rootResponse.status);
    console.log('响应:', rootResponse.body);

    // 测试获取图书列表
    console.log('\n2. 测试获取图书列表:');
    const booksResponse = await testEndpoint('/api/books');
    console.log('状态码:', booksResponse.status);
    console.log('响应:', booksResponse.body);

    console.log('\n测试完成！');
  } catch (error) {
    console.error('测试失败:', error);
  }
}

// 运行测试
runTests();