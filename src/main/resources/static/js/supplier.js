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
                                // <td>${new Date(supplier.create_time).toLocaleString()}</td>
                                // <td>${new Date(supplier.update_time).toLocaleString()}</td>
                                <td class="action">
                                    <button class="action-btn"><i class="fas fa-edit"></i> 编辑</button>
                                    <button class="action-btn"><i class="fas fa-eye"></i> 查看</button>
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
    const closeBtn = document.getElementById('add-modal-close');
//打开和隐藏弹窗
    addBtn.addEventListener('click', () => {
        addModal.style.display = 'flex';
    });
    closeBtn.addEventListener('click', (e) => {
        e.stopPropagation(); // 阻止事件冒泡到父元素
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
    const cancelDeleteBtn = document.getElementById('cancel-delete');
    const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)');
    const selectAll = document.getElementById('select-all');
    const supplierChecks = document.querySelectorAll('.supplier-check');
    let isSelectVisible = false;

    // 点击"删除供应商"按钮显示选择框和确认/取消按钮
    batchDeleteBtn.addEventListener('click', () => {
        if (!isSelectVisible) {
            isSelectVisible = true;
            selectColumns.forEach(column => {
                column.style.display = 'table-cell';
            });
            confirmDeleteBtn.style.display = 'inline-block';
            cancelDeleteBtn.style.display = 'inline-block';
        }
    });

    // 取消按钮点击事件：直接退出批量删除状态（无提示）
    cancelDeleteBtn.addEventListener('click', () => {
        isSelectVisible = false;
        selectColumns.forEach(column => {
            column.style.display = 'none';
        });
        confirmDeleteBtn.style.display = 'none';
        cancelDeleteBtn.style.display = 'none';
        selectAll.checked = false;
        supplierChecks.forEach(check => {
            check.checked = false;
        });
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

    // 确认删除按钮点击事件 - 执行批量删除（去掉二次确认）
    confirmDeleteBtn.addEventListener('click', async function() {
        const selectedIds = [];
        supplierChecks.forEach(check => {
            if (check.checked) {
                selectedIds.push(parseInt(check.dataset.id));
            }
        });

        if (selectedIds.length === 0) {
            alert('请选择要删除的供应商');
            return;
        }

        try {
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
                window.location.reload();
            } else {
                alert('删除失败: ' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('删除请求失败:', error);
            alert('网络错误，删除失败');
        }
    });
});

//详情查看
document.addEventListener('DOMContentLoaded', function() {
    // 获取元素
    const viewModal = document.getElementById('view-modal');
    const closeBtn = document.querySelector('#view-modal .close');
    const viewButtons = document.querySelectorAll('.view-btn'); // 所有查看按钮
    // 详情字段DOM
    const currentViewId = document.getElementById('current-view-id');
    const detailFields = {
        id: document.getElementById('detail-id'),
        status: document.getElementById('detail-status'),
        name: document.getElementById('detail-name'),
        contact: document.getElementById('detail-contact'),
        phone: document.getElementById('detail-phone'),
        address: document.getElementById('detail-address'),
        varieties: document.getElementById('detail-varieties'),
        cooperation: document.getElementById('detail-cooperation'),
        createTime: document.getElementById('detail-create-time'),
        updateTime: document.getElementById('detail-update-time'),
        orderCount: document.getElementById('detail-order-count')
    };

    const viewModalContent = document.querySelector('#view-modal .modal-content');
    viewModalContent.addEventListener('click', (e) => {
        e.stopPropagation(); // 阻止事件传递到遮罩层
    });
    // 点击查看按钮：根据ID加载详情
    viewButtons.forEach(button => {
        button.addEventListener('click', async function() {
            // 1. 获取当前点击按钮对应的供应商ID
            const supplierId = this.getAttribute('data-id');
            if (!supplierId) {
                alert('未获取到供应商ID');
                return;
            }

            try {
                // 2. 发送请求获取该ID的详情（后端接口需支持根据ID查询）
                const response = await // 改为路径参数形式
                    fetch(`/suppliers/detail/${supplierId}`);
                if (!response.ok) {
                    console.error('获取详情失败');
                }
                const supplier = await response.json();

                // 3. 验证返回数据是否正确（防止ID不匹配）
                if (supplier.supplier_id !== parseInt(supplierId)) {
                    console.error('数据不匹配');
                }

                // 4. 填充详情到弹窗
                currentViewId.value = supplierId; // 存储当前查看的ID
                detailFields.id.textContent = supplier.supplier_id;
                detailFields.status.textContent = supplier.status === 1 ? '启售' : '停售';
                detailFields.name.textContent = supplier.supplier_name || '无';
                detailFields.contact.textContent = supplier.contact || '无';
                detailFields.phone.textContent = supplier.phone || '无';
                detailFields.address.textContent = supplier.address || '无';
                detailFields.varieties.textContent = supplier.varieties || '无';
                detailFields.cooperation.textContent = supplier.cooperation_start_date || '无';
                detailFields.createTime.textContent = new Date(supplier.create_time).toLocaleString() || '无';
                detailFields.updateTime.textContent = (supplier.update_time
                        ? new Date(supplier.update_time).toLocaleString()
                        : '无'
                );
                detailFields.orderCount.textContent = supplier.order_count || 0;
                // 5. 显示弹窗
                viewModal.style.display = 'flex';

            } catch (error) {
                console.error('查看详情失败：', error);
                alert('加载详情失败，请重试');
            }
        });
    });

    // 关闭弹窗
    closeBtn.addEventListener('click', () => {
        viewModal.style.display = 'none';
    });

    // 点击遮罩层关闭弹窗（优化体验）
    viewModal.addEventListener('click', (e) => {
        if (e.target === viewModal) {
            viewModal.style.display = 'none';
        }
    });
});

//编辑
document.addEventListener('DOMContentLoaded', function() {
    // 获取元素
    const editModal = document.getElementById('edit-modal');
    const closeEditBtn = document.querySelector('#edit-modal .edit-close');
    const editButtons = document.querySelectorAll('.edit-btn'); // 编辑按钮
    const editForm = document.getElementById('edit-form');

    // 编辑表单字段映射
    const editFields = {
        id: document.getElementById('edit-id'),
        name: document.getElementById('edit-name'),
        contact: document.getElementById('edit-contact'),
        phone: document.getElementById('edit-phone'),
        address: document.getElementById('edit-address'),
        varieties: document.getElementById('edit-varieties'),
        cooperation: document.getElementById('edit-cooperation'),
        status: document.getElementById('edit-status'),
        orderCount: document.getElementById('edit-order-count')
    };

    const editModalContent = document.querySelector('#edit-modal .modal-content');
    editModalContent.addEventListener('click', (e) => {
        e.stopPropagation(); // 阻止事件传递到遮罩层
    });

    // 点击编辑按钮 - 加载数据到弹窗
    editButtons.forEach(button => {
        button.addEventListener('click', async function() {
            const supplierId = this.getAttribute('data-id');
            if (!supplierId) {
                alert('未获取到供应商ID');
                return;
            }

            try {
                // 获取供应商详情
                const response = await fetch(`/suppliers/detail/${supplierId}`);
                if (!response.ok) {
                    console.error('获取供应商数据失败');
                }
                const supplier = await response.json();

                // 填充表单数据
                editFields.id.value = supplier.supplier_id;
                editFields.name.value = supplier.supplier_name || '';
                editFields.contact.value = supplier.contact || '';
                editFields.phone.value = supplier.phone || '';
                editFields.address.value = supplier.address || '';
                editFields.varieties.value = supplier.varieties || '';
                editFields.cooperation.value = supplier.cooperation_start_date || '';
                editFields.status.value = supplier.status || 1; // 默认启售
                editFields.orderCount.value = supplier.order_count || 0;

                // 显示弹窗
                editModal.style.display = 'flex';
            } catch (error) {
                console.error('加载编辑数据失败：', error);
                alert('加载数据失败，请重试');
            }
        });
    });

    // 关闭编辑弹窗
    closeEditBtn.addEventListener('click', () => {
        editModal.style.display = 'none';
    });

    // 点击遮罩层关闭弹窗
    editModal.addEventListener('click', (e) => {
        if (e.target === editModal) {
            editModal.style.display = 'none';
        }
    });
    editForm.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    // 表单提交 - 保存修改
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 收集表单数据
        const formData = new FormData(editForm);
        const data = Object.fromEntries(formData.entries());

        try {
            // 发送更新请求
            const response = await fetch(`/suppliers/update`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (result.success) {
                alert('修改成功');
                editModal.style.display = 'none';
                window.location.reload(); // 刷新页面显示最新数据
            } else {
                alert('修改失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('保存修改失败：', error);
            alert('网络错误，请重试');
        }
    });
});
