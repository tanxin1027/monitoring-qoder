// 使用 bcryptjs 生成密码
const bcrypt = require('bcryptjs');

const password = 'admin123';
const saltRounds = 10;

bcrypt.hash(password, saltRounds, function(err, hash) {
    if (err) {
        console.error('Error:', err);
        return;
    }
    console.log('Password:', password);
    console.log('Hash:', hash);
    
    // 验证
    bcrypt.compare(password, hash, function(err, result) {
        console.log('Verify:', result);
    });
});
