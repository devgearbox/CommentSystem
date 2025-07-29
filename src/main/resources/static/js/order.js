//搜索和查看订单
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page loaded!');
    document.getElementById('search-button').addEventListener('click', function() {
        const searchTerm = document.getElementById('search-input').value.trim();
        if (searchTerm) {
            // 发送搜索请求到后端
            fetch('/orders/search?supplierName=' + encodeURIComponent(searchTerm))
                .then(response => response.json())
                .then(data => {
                    // 清空表格
                    const tableBody = document.getElementById('order-table-body');
                    tableBody.innerHTML = '';

                    // 填充搜索结果
                    data.forEach(order => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                                <td>${order.order_no}</td>
                                <td>${order.user.username}</td>
                                <td>${order.supplier.supplier_name}</td>
                                <td>${order.purchase_variety}</td>
                                <td>${order.purchase_quantity}</td>
                                <td>${order.create_time}</td>
                                <td>${order.order_status.label}</td>
<!--                                <td class="action">-->
<!--                                    <button class="action-btn"><i class="fas fa-edit"></i> 编辑</button>-->
<!--                                    <button class="action-btn"><i class="fas fa-eye"></i> 查看</button>-->
<!--                                </td>-->
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
        window.location.href = '/orders'
    });
});
//添加供应商
// document.addEventListener('DOMContentLoaded', function() {
//     const addBtn = document.getElementById('add-order');
//     const addModal = document.getElementById('add-modal');
//     const closeBtn = document.getElementById('add-modal-close');
// //打开和隐藏弹窗
//     addBtn.addEventListener('click', () => {
//         addModal.style.display = 'flex';
//     });
//     closeBtn.addEventListener('click', (e) => {
//         e.stopPropagation(); // 阻止事件冒泡到父元素
//         addModal.style.display = 'none';
//     });
//
// // 2. 表单提交
//     const addForm = document.getElementById('add-form');
//     addForm.addEventListener('submit', async (e) => {
//         e.preventDefault(); // 阻止默认提交
//
//         // 收集表单数据
//         const formData = new FormData(addForm);
//         const data = Object.fromEntries(formData.entries());
//
//         try {
//             // 发送 POST 请求到后端
//             const response = await fetch('/orders/add', {
//                 method: 'POST',
//                 headers: { 'Content-Type': 'application/json' },
//                 body: JSON.stringify(data)
//             });
//
//             // 解析响应 JSON（只执行一次）
//             const res = await response.json();
//
//             console.log('完整响应:', res); // 调试用
//
//             if (res.success) {
//                 alert('添加成功！');
//                 addModal.style.display = 'none';
//                 window.location.reload();
//             } else {
//                 alert('添加失败：' + (res.message || '未知错误'));
//             }
//         } catch (error) {
//             console.error('添加出错：', error);
//             alert('网络异常，请重试');
//         }
//     });
// });
//批量删除功能
document.addEventListener('DOMContentLoaded', function() {
    const batchDeleteBtn = document.getElementById('del-order'); // 对应供应商的 del-supplier
    const confirmDeleteBtn = document.getElementById('confirm-delete-order'); // 对应 confirm-delete
    const cancelDeleteBtn = document.getElementById('cancel-delete-order'); // 对应 cancel-delete
    const selectColumns = document.querySelectorAll('th:nth-child(1), td:nth-child(1)'); // 复选框列
    const selectAll = document.getElementById('select-all-order'); // 对应 select-all
    const orderChecks = document.querySelectorAll('.order-check'); // 对应 supplier-check
    let isSelectVisible = false;

    // 点击"删除订单记录"显示选择框和确认/取消按钮
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

    // 取消按钮：退出批量删除状态
    cancelDeleteBtn.addEventListener('click', () => {
        isSelectVisible = false;
        selectColumns.forEach(column => {
            column.style.display = 'none';
        });
        confirmDeleteBtn.style.display = 'none';
        cancelDeleteBtn.style.display = 'none';
        selectAll.checked = false;
        orderChecks.forEach(check => {
            check.checked = false;
        });
    });

    // 全选/取消全选
    selectAll.addEventListener('change', function() {
        orderChecks.forEach(check => {
            check.checked = selectAll.checked;
        });
    });

    // 单个选择框变化：更新全选状态
    orderChecks.forEach(check => {
        check.addEventListener('change', function() {
            selectAll.checked = Array.from(orderChecks).every(c => c.checked);
        });
    });

    // 确认删除：调用订单批量删除接口
    confirmDeleteBtn.addEventListener('click', async function() {
        const selectedIds = [];
        orderChecks.forEach(check => {
            if (check.checked) {
                selectedIds.push(parseInt(check.dataset.id));
            }
        });

        if (selectedIds.length === 0) {
            alert('请选择要删除的订单');
            return;
        }

        try {
            const response = await fetch('/orders/delete/batch', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ ids: selectedIds })
            });

            const result = await response.json();

            if (result.success) {
                alert('批量删除订单成功');
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