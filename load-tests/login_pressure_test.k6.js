import http from 'k6/http';
import { check, sleep, group } from 'k6';

// --- 测试配置 (Test Configuration) ---
export const options = {
  stages: [
    // 阶段1: 预热阶段 - 30秒内，虚拟用户数从0逐渐增加到10个
    { duration: '30s', target: 10, rps: 20 }, // RPS (requests per second) can also be controlled per stage
    // 阶段2: 负载阶段 - 10个虚拟用户持续运行1分钟
    { duration: '1m', target: 10, rps: 20 },
    // 阶段3: 峰值阶段 (可选) - 10秒内，用户数增加到20个 (短时峰值)
    // { duration: '10s', target: 20 },
    // { duration: '30s', target: 20 }, // 峰值持续
    // 阶段4: 收尾阶段 - 30秒内，虚拟用户数从当前值逐渐减少到0
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    // 定义性能阈值 (Performance Thresholds)
    // 95%的HTTP请求响应时间应小于500毫秒
    'http_req_duration': ['p(95)<500'],
    // HTTP请求失败率应小于1%
    'http_req_failed': ['rate<0.01'],
    // 自定义检查点的成功率应大于99%
    'checks': ['rate>0.99'],
    // 针对特定API端点的阈值 (可选)
    'http_req_duration{group:::Login API}': ['p(95)<300'], // 登录API的P95响应时间应小于300ms
  },
  // 全局标签 (Optional Global Tags)
  // tags: {
  //   test_type: 'api_load_test',
  //   environment: 'staging',
  // },
};

// --- 测试数据 (Test Data) ---
// !!! 重要: 请用你后端测试环境的有效用户凭证替换以下数据 !!!
// 理想情况下，每个虚拟用户 (VU) 应使用不同的凭证以避免冲突。
// 你可以从CSV文件加载数据，或使用k6的场景和执行器功能来更好地管理VU和数据。
const testUsers = [
  { username: 'user1_test', password: 'password123_test' },
  { username: 'user2_test', password: 'password456_test' },
  // 添加更多用户凭证...
  // 对于大量用户，考虑使用 k6 的 SharedArray 或外部文件加载:
  // import { SharedArray } from 'k6/data';
  // const data = new SharedArray('some data name', function () {
  //   return JSON.parse(open('./users.json')); // 加载 users.json 文件
  // });
];

// --- 默认函数 (每个虚拟用户执行的逻辑) ---
export default function () {
  // !!! 重要: 请根据你的后端API调整这里的URL、路径和请求体结构 !!!
  const backendApiBaseUrl = 'http://localhost:8080/api'; // 假设这是你的后端API基础URL

  // 从测试用户中选择一个 (简单轮询)
  // __VU 是虚拟用户ID (从1开始), __ITER 是迭代次数 (从0开始)
  const currentUser = testUsers[__VU % testUsers.length];

  group('Login API', function () {
    const loginUrl = `${backendApiBaseUrl}/users/login`; // 假设登录API的路径
    
    // 构造请求体
    const payload = JSON.stringify({
      // 确保这里的字段名与你的后端API期望的一致
      username: currentUser.username, 
      password: currentUser.password,
      // email: currentUser.email, // 如果你的API使用email登录
    });

    const params = {
      headers: {
        'Content-Type': 'application/json',
        // 如果需要其他请求头，请在此添加
      },
      tags: { // 为这个特定请求添加标签，方便在结果中筛选
        api_endpoint: 'login',
      }
    };

    // 发送 POST 请求
    const loginResponse = http.post(loginUrl, payload, params);

    // 检查点 (Checks): 验证响应是否符合预期
    check(loginResponse, {
      'Login: status is 200': (r) => r.status === 200,
      'Login: response body contains token': (r) => {
        try {
          const body = r.json();
          // 假设成功的响应体是JSON且包含一个名为 'token' 的字段
          return typeof body === 'object' && body !== null && 'token' in body && body.token !== null && body.token !== '';
        } catch (e) {
          return false; // 如果响应不是有效的JSON或解析失败
        }
      },
      // 你可以添加更多检查点，例如检查响应体中的其他字段
      // 'Login: response contains userId': (r) => r.json('userId') !== undefined,
    });

    // (可选) 如果登录成功，获取token并用于后续请求 (模拟更完整的用户流程)
    let authToken = null;
    if (loginResponse.status === 200) {
      try {
        authToken = loginResponse.json('token');
      } catch (e) {
        console.error("Failed to parse token from login response.");
      }
    }

    // if (authToken) {
    //   group('Protected Endpoint Example', function() {
    //     const protectedUrl = `${backendApiBaseUrl}/user/profile`; // 假设的受保护端点
    //     const protectedParams = {
    //       headers: {
    //         'Authorization': `Bearer ${authToken}`,
    //         'Content-Type': 'application/json',
    //       },
    //       tags: { api_endpoint: 'profile_read' }
    //     };
    //     const profileResponse = http.get(protectedUrl, protectedParams);
    //     check(profileResponse, {
    //       'Profile: status is 200': (r) => r.status === 200,
    //     });
    //   });
    // }
  });

  // 每个虚拟用户在两次迭代之间暂停1到3秒 (模拟思考时间)
  sleep(Math.random() * 2 + 1); 
}

// (可选) Setup 和 Teardown 函数
// export function setup() {
//   // 测试开始前执行一次，例如准备测试数据或获取全局token
//   console.log('k6 setup: Initializing test environment...');
//   // const loginRes = http.post(`${backendApiBaseUrl}/admin/login`, JSON.stringify({u:'admin',p:'adminpass'}));
//   // return { adminToken: loginRes.json('token') }; // setup返回的数据可以传递给default函数和teardown
// }

// export function teardown(data) {
//   // 测试结束后执行一次，例如清理测试数据
//   // console.log(`k6 teardown: Cleaning up... Admin token was ${data.adminToken}`);
// }

// 要运行此脚本:
// 1. 安装 k6: https://k6.io/docs/getting-started/installation/
// 2. 在项目根目录(E-scooter-Booking-System-CW-666)下创建 load-tests 文件夹。
// 3. 将此文件保存为 load-tests/login_pressure_test.k6.js。
// 4. !!! 根据你的后端API详情，修改脚本中的 backendApiBaseUrl, loginUrl, payload 结构, testUsers 数据, 以及响应检查逻辑 !!!
// 5. 确保你的后端应用在一个独立的测试环境中运行，并且数据库等依赖也已准备好。
// 6. 打开终端，导航到项目根目录。
// 7. 运行命令: k6 run load-tests/login_pressure_test.k6.js
//    (可以添加参数，例如: k6 run --vus 5 --duration 30s load-tests/login_pressure_test.k6.js 来覆盖脚本中的options) 