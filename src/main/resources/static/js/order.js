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
//订单详情功能
document.addEventListener('DOMContentLoaded', function() {
    // 1. 获取 DOM 元素
    const viewModal = document.getElementById('order-view-modal');
    const closeBtn = document.querySelector('#order-view-modal .close');
    const viewButtons = document.querySelectorAll('.view-btn'); // 订单列表的“查看”按钮
    const STATUS_MAP = {
            pending: "待审核",
            shipping: "待发货",
            shipped: "已发货"
        };

    // 2. 详情字段 DOM（与弹窗 HTML 对应）
    const detailFields = {
        orderNo: document.getElementById('order-no'), // 订单编号
        orderRealName: document.getElementById('order-real-name'), // 采购人
        orderPrice: document.getElementById('order-price'), // 采购单价
        orderQuantity: document.getElementById('order-quantity'), // 数量
        orderVariety: document.getElementById('order-variety'), // 采购品种
        orderTotalPrice: document.getElementById('order-total-price'), // 订单总价
        orderSupplierName: document.getElementById('order-supplier-name'), // 供应商
        orderSupplierPhone: document.getElementById('order-supplier-phone'), // 供应商电话
        orderSupplierAddress: document.getElementById('order-supplier-address'), // 供应商地址
        orderStatus: document.getElementById('order-status'), // 订单状态
        orderDate: document.getElementById('order-date') // 下单日期
    };

    // 3. 点击“查看”按钮：获取订单 ID 并请求详情
    viewButtons.forEach(button => {
        button.addEventListener('click', async function() {
            const orderId = this.getAttribute('data-id'); // 获取订单 ID
            if (!orderId) {
                alert('未获取到订单 ID');
                return;
            }

            try {
                // 4. 调用后端接口（路径与 Controller 对应）
                const response = await fetch(`/orders/detail/${orderId}`);
                if (!response.ok) {
                    throw new Error('获取详情失败');
                }
                const order = await response.json();

                // 5. 验证数据（可选，防止 ID 不匹配）
                if (!order || order.order_id !== parseInt(orderId)) {
                    console.error('数据不匹配或为空', order);
                    return;
                }

                // 6. 填充弹窗字段
                detailFields.orderNo.textContent = order.order_no; // 订单编号
                detailFields.orderRealName.textContent = order.user.real_name || '无'; // 采购人
                detailFields.orderPrice.textContent = order.litchiVariety.price || '9.00'; // 采购单价
                detailFields.orderQuantity.textContent = order.purchase_quantity || '0'; // 数量
                detailFields.orderVariety.textContent = order.purchase_variety || '无'; // 采购品种
                detailFields.orderTotalPrice.textContent = order.total_price || '0.00'; // 订单总价
                detailFields.orderSupplierName.textContent = order.supplier.supplier_name || '无'; // 供应商名称
                detailFields.orderSupplierPhone.textContent = order.supplier.phone || '无'; // 供应商电话
                detailFields.orderSupplierAddress.textContent = order.supplier.address || '无'; // 供应商地址
                detailFields.orderStatus.textContent = STATUS_MAP[order.order_status] || "未知状态"; // 订单状态
                detailFields.orderDate.textContent = order.create_time
                    ? new Date(order.create_time).toLocaleString()
                    : '无'; // 下单日期

                // 7. 显示弹窗
                viewModal.style.display = 'flex';

            } catch (error) {
                console.error('查看订单详情失败：', error);
                alert('加载详情失败，请重试');
            }
        });
    });

    // 8. 关闭弹窗（点击关闭按钮或遮罩层）
    closeBtn.addEventListener('click', () => {
        viewModal.style.display = 'none';
    });

    viewModal.addEventListener('click', (e) => {
        if (e.target === viewModal) {
            viewModal.style.display = 'none';
        }
    });
});
// 状态修改按钮点击事件
document.addEventListener('DOMContentLoaded', function(){
    const STATUS_FLOW = [
        { value: 'pending', label: '待审核' },
        { value: 'shipping', label: '待发货' },
        { value: 'shipped', label: '已发货' }
        // 暂时屏蔽以下状态，如需开放可取消注释
        // { value: 'received', label: '已接收' },
        // { value: 'cancelled', label: '已取消' }
    ];

    // 【新增】动态渲染状态下拉框（限制可选状态）
    function renderStatusOptions(currentStatus) {
        const select = document.getElementById('new-status');
        select.innerHTML = ''; // 清空原有选项

        // 找到当前状态在流转顺序中的索引
        const currentIndex = STATUS_FLOW.findIndex(item => item.value === currentStatus);

        // 仅渲染当前状态及之后的选项
        STATUS_FLOW.forEach((item, index) => {
            if (index >= currentIndex) {
                const option = document.createElement('option');
                option.value = item.value;
                option.textContent = item.label;
                // 默认选中当前状态
                if (item.value === currentStatus) {
                    option.selected = true;
                }
                select.appendChild(option);
            }
        });
    }

    // 修改原有“状态修改按钮”点击事件
    document.querySelectorAll('.status-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const orderId = this.getAttribute('data-id');
            const currentStatus = this.getAttribute('data-status');
                    // 新增：如果是已接收或拒收状态，直接提示并返回
                    if (currentStatus === 'received' || currentStatus === 'rejected') {
                        alert('该订单状态为【' + (currentStatus === 'received' ? '已接收' : '拒收') + '】，请联系采购人协商');
                        return; // 阻止后续操作
                    }

            // 【新增】动态渲染允许的状态选项
            renderStatusOptions(currentStatus);

            // 显示弹窗（原有逻辑保留）
            document.getElementById('status-modal').style.display = 'block';
            document.getElementById('status-order-id').value = orderId;
            document.getElementById('current-status').textContent = currentStatus;
        });
    });

    // 状态修改表单提交
    document.getElementById('status-form').addEventListener('submit', function(e) {
        e.preventDefault();
        const orderId = document.getElementById('status-order-id').value;
        const newStatus = document.getElementById('new-status').value;

        // 调用后端接口
        fetch(`/api/orders/${orderId}/status?newStatus=${newStatus}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        }).then(response => {
            if (response.ok) {
                location.reload(); // 刷新页面显示最新状态
            }
        });
    });

    // 新增：弹窗关闭按钮点击事件
    document.querySelector('.status-close').addEventListener('click', function() {
        document.getElementById('status-modal').style.display = 'none';
    });

    // 可选：点击弹窗外部也关闭（增强体验）
    window.addEventListener('click', function(e) {
        const modal = document.getElementById('status-modal');
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
});
