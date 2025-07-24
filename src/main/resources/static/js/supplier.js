//搜索和查看供应商
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page loaded!');
    document.getElementById('search-button').addEventListener('click', function() {
        const searchTerm = document.getElementById('search-input').value.trim();
        if (searchTerm) {
            // 发送搜索请求到后端
            fetch('/suppliers/search?name=' + encodeURIComponent(searchTerm))
                .then(response => response.json())
                .then(data => {
                    // 清空表格
                    const tableBody = document.getElementById('supplier-table-body');
                    tableBody.innerHTML = '';

                    // 填充搜索结果
                    data.forEach(supplier => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                                <td>${supplier.supplier_id}</td>
                                <td>${supplier.supplier_name}</td>
                                <td>${supplier.contact}</td>
                                <td>${supplier.phone}</td>
                                <td>${supplier.address}</td>
                                <td>${supplier.varieties}</td>
                                <td>${supplier.cooperation_start_date}</td>
                                <td>${supplier.status}</td>
                                <td>${new Date(supplier.create_time).toLocaleString()}</td>
                                <td>${new Date(supplier.update_time).toLocaleString()}</td>
                                <td class="action">
                                    <a href="#">编辑</a>
                                    <a href="#">查看</a>
                                </td>
                            `;
                        tableBody.appendChild(row);
                    });
                })
                .catch(error => {
                    console.error('Error fetching search results:', error);
                });
        }
    });
    document.getElementById('search-all').addEventListener('click',function (){
        window.location.href = '/suppliers'
    });
});

//添加供应商
document.addEventListener('DOMContentLoaded', function() {
    const addBtn = document.getElementById('add-supplier');
    const addModal = document.getElementById('add-modal');
    const closeBtn = document.querySelector('.close');
//打开和隐藏弹窗
    addBtn.addEventListener('click', () => {
        addModal.style.display = 'flex';
    });
    closeBtn.addEventListener('click', () => {
        addModal.style.display = 'none';
    });

// 2. 表单提交
    const addForm = document.getElementById('add-form');
    addForm.addEventListener('submit', async (e) => {
        e.preventDefault(); // 阻止默认提交

        // 收集表单数据
        const formData = new FormData(addForm);
        const data = Object.fromEntries(formData.entries());

        try {
            // 发送 POST 请求到后端
            const response = await fetch('/suppliers/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            // 解析响应 JSON（只执行一次）
            const res = await response.json();

            console.log('完整响应:', res); // 调试用

            if (res.success) {
                alert('添加成功！');
                addModal.style.display = 'none';
                window.location.reload();
            } else {
                alert('添加失败：' + (res.message || '未知错误'));
            }
        } catch (error) {
            console.error('添加出错：', error);
            alert('网络异常，请重试');
        }
    });
});

//批量删除功能
document.addEventListener('DOMContentLoaded', function() {
    // 获取元素
    const batchDeleteBtn = document.getElementById('del-supplier');
    const confirmDeleteBtn = document.getElementById('confirm-delete');
    const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)');
    const selectAll = document.getElementById('select-all');
    const supplierChecks = document.querySelectorAll('.supplier-check');
    let isSelectVisible = false; // 标记选择框是否可见

    // 点击"删除供应商"按钮显示选择框和确认按钮
    batchDeleteBtn.addEventListener('click', () => {
        if (!isSelectVisible) {
            isSelectVisible = true;
            // 显示选择框列
            selectColumns.forEach(column => {
                column.style.display = 'table-cell';
            });
            // 显示确认按钮
            confirmDeleteBtn.style.display = 'inline-block';
        }
    });

    // 全选/取消全选功能
    selectAll.addEventListener('change', function() {
        supplierChecks.forEach(check => {
            check.checked = selectAll.checked;
        });
    });

    // 单个选择框变化时更新全选状态
    supplierChecks.forEach(check => {
        check.addEventListener('change', function() {
            selectAll.checked = Array.from(supplierChecks).every(c => c.checked);
        });
    });

    // 确认删除按钮点击事件 - 执行批量删除
    confirmDeleteBtn.addEventListener('click', async function() {
        // 收集选中的ID
        const selectedIds = [];
        supplierChecks.forEach(check => {
            if (check.checked) {
                selectedIds.push(parseInt(check.dataset.id));
            }
        });

        // 验证是否选择了供应商
        if (selectedIds.length === 0) {
            alert('请选择要删除的供应商');
            return;
        }

        // 确认删除
        if (confirm(`确定要删除选中的 ${selectedIds.length} 个供应商吗？`)) {
            try {
                // 发送批量删除请求
                const response = await fetch('/delete/batch', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ ids: selectedIds })
                });

                const result = await response.json();

                if (result.success) {
                    alert('批量删除成功');
                    // 刷新页面显示最新数据
                    window.location.reload();
                } else {
                    alert('删除失败: ' + (result.message || '未知错误'));
                }
            } catch (error) {
                console.error('删除请求失败:', error);
                alert('网络错误，删除失败');
            }
        }
    });
});
